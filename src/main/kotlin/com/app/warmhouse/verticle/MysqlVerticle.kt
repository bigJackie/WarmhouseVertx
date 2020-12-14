package com.app.warmhouse.verticle

import com.app.warmhouse.utils.TokenUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.*


class MysqlVerticle : AbstractVerticle() {
  // declare router
  lateinit var router : Router

  // Connect options
  private lateinit var connectOptions : MySQLConnectOptions

  // Pool options
  private lateinit var poolOptions : PoolOptions

  // Create the client pool
  lateinit var client : MySQLPool

  private var tokenUtils = TokenUtils()

  override fun start(startPromise: Promise<Void>){
    // init vert.x configuration
    val retriever = ConfigRetriever.create(vertx)

    retriever.getConfig(Handler { ar ->
      if (ar.failed()) {
        // Failed to retrieve the configuration
      } else {
        val config = ar.result()

        connectOptions = MySQLConnectOptions()
          .setPort(config.getJsonObject("database").getInteger("port"))
          .setHost(config.getJsonObject("database").getString("host"))
          .setDatabase(config.getJsonObject("database").getString("database"))
          .setUser(config.getJsonObject("database").getString("user"))
          .setPassword(config.getJsonObject("database").getString("password"))

        poolOptions = PoolOptions().setMaxSize(config.getJsonObject("poolOptions").getInteger("maxsize"))

        client = MySQLPool.pool(vertx, connectOptions, poolOptions)

        // init router
        router = Router.router(vertx)

        // allow cross-domain
        router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.POST));

        // configure router analysis url
        router.route("/aaa").handler{ req ->
          req.response()
            .putHeader("content-type", "text/plain")
            .end("Connect Success!")
        }

        router.route("/aaa/:name").handler{ req ->
          val name: String = req.pathParam("name")
          req.response()
            .putHeader("content-type", "text/plain")
            .end("Connect Success!,$name")
        }

        router.route("/*").handler(StaticHandler.create());

        router.route("/admin/login").handler{ req ->

          var username = req.request().getParam("username").toString()
          var password = req.request().getParam("password").toString()

          this.getConnection()
            .compose { conn -> this.getRows(conn, username, password) }
            .onSuccess { rows ->
              var token = tokenUtils.generateToken(username)
              var list = ArrayList<JsonObject>()
              rows.forEach { item ->
                var json = JsonObject()
                json.put("username", item.getValue("username"))
                json.put("password", item.getValue("password"))
                json.put("access_token", token)
                list.add(json)
              }
              client
                .preparedQuery("UPDATE admin_info SET access_token = ?, token_expire = ? WHERE username = ?")
                .execute(Tuple.of(token, System.currentTimeMillis()/1000, username)) { ar: AsyncResult<RowSet<Row?>> ->
                  if (ar.succeeded()) {
                    req.response()
                      .putHeader("content-type", "application/json")
                      .end(list.toString())
                  } else {
                    println("Failure: " + ar.cause().message)
                  }
                }
            }
        }

        router.route("/admin/checkToken").handler{ req ->

          var username = req.request().getParam("username").toString()
          var accessToken = req.request().getParam("access_token").toString()

          // Get a connection from the pool
          client.getConnection{ ar1 ->

            if (ar1.succeeded()) {
              println("Connected")
              // Obtain our connection
              var conn = ar1.result()
              // All operations execute on the same connection
              conn.preparedQuery("SELECT username, token_expire FROM admin_info WHERE username = ? and access_token = ? limit 1").execute(Tuple.of(username, accessToken)) { ar2 ->
                // Release the connection to the pool
                conn.close()
                if (ar2.succeeded()) {
                  if (ar2.result().size() > 0){
                    ar2.result().forEach { item ->
                      var timestamp = Integer.parseInt(item.getValue("token_expire").toString())
                      var nowTime = System.currentTimeMillis()/1000
                      if (nowTime - timestamp > 259200) {
                        req.response()
                          .putHeader("content-type", "text/plain")
                          .end("expired")
                      } else {
                        req.response()
                          .putHeader("content-type", "text/plain")
                          .end(item.getValue("username").toString())
                      }
                    }
                  } else {
                    req.response()
                      .putHeader("content-type", "text/plain")
                      .end("null")
                  }
                } else {
                  req.response()
                    .putHeader("content-type", "text/plain")
                    .end(ar2.cause().toString())
                }
              }
            } else {
              println("Could not connect: ${ar1.cause().toString()}")
            }
          }

        }


        // bind router with the vertx HTTPServer
        vertx
          .createHttpServer()
          .requestHandler(router)
          .listen(8888) { http ->
            if (http.succeeded()) {
              startPromise.complete()
              println("HTTP server started on port 8888")
            } else {
              startPromise.fail(http.cause());
            }
          }
      }
    })
  }

  private fun getConnection() : Future<SqlConnection> {
    val promise : Promise<SqlConnection> = Promise.promise()
    client.getConnection{ ar1 ->
      if (ar1.succeeded()) {
        println("Connected")
        // Obtain our connection
        var conn = ar1.result()
        promise.complete(conn)
      } else {
        promise.fail(ar1.cause())
      }
    }
    return promise.future()
  }

  private fun getRows(conn: SqlConnection, username : String, password : String) : Future<RowSet<Row>> {
    val promise : Promise<RowSet<Row>> = Promise.promise()
    // All operations execute on the same connection
    conn.preparedQuery("SELECT username, password FROM admin_info WHERE username = ? and password = ? limit 1")
      .execute(Tuple.of(username, password)) { ar2 ->
      // Release the connection to the pool
      conn.close()
      if (ar2.succeeded()) {
        promise.complete(ar2.result())
      } else {
        promise.fail(ar2.cause())
      }
    }
    return promise.future()
  }

}
