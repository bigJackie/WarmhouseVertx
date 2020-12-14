package com.app.warmhouse.verticle

import com.app.warmhouse.utils.TokenUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple


class MysqlVerticle2 : AbstractVerticle() {
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
        router.route("/app").handler{ req ->
          req.response()
            .putHeader("content-type", "text/plain")
            .end("Connect Successapp!")
        }



        router.route("/api").handler{ req ->

          var sql = req.request().getParam("sql").toString()
//          var username = req.request().getParam("username").toString()
//          var password = req.request().getParam("password").toString()
//          "SELECT username, password FROM admin_info WHERE username = ? and password = ? limit 1"
          // Get a connection from the pool
          client.getConnection{ ar1 ->

            if (ar1.succeeded()) {
              println("Connected")
              // Obtain our connection
              var conn = ar1.result()
              // All operations execute on the same connection
              conn.preparedQuery(sql).execute() { ar2 ->
                // Release the connection to the pool
                conn.close()
                if (ar2.succeeded()) {
                  var list = ArrayList<JsonObject>()
                  ar2.result().forEach { item ->
                    var json = JsonObject()
                    json.put("username", item.getValue("username"))
                    json.put("password", item.getValue("password"))
                    list.add(json)
                  }
                  req.response()
                    .putHeader("content-type", "application/json")
                    .end(list.toString())
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
}
//router.route("/test/jackie").handler{ req ->
//
//          var page = Integer.valueOf(req.request().getParam("page"))
//
//          // Get a connection from the pool
//          client.getConnection{ ar1 ->
//
//            if (ar1.succeeded()) {
//              println("Connected")
//              // Obtain our connection
//              var conn = ar1.result()
//              var offset = (page-1)*5
//              // All operations execute on the same connection
//              conn.preparedQuery("SELECT id, name, authority FROM blog_class limit 5 offset ?").execute(Tuple.of(offset)) { ar2 ->
//                // Release the connection to the pool
//                conn.close()
//                if (ar2.succeeded()) {
//                  var list = ArrayList<JsonObject>()
//                  ar2.result().forEach { item ->
//                    var json = JsonObject()
//                    json.put("id", item.getValue("id"))
//                    json.put("name", item.getValue("name"))
//                    json.put("authority", item.getValue("authority"))
//                    list.add(json)
//                  }
//                  req.response()
//                    .putHeader("content-type", "application/json")
//                    .end(list.toString())
//                } else {
//                  req.response()
//                    .putHeader("content-type", "text/plain")
//                    .end(ar2.cause().toString())
//                }
//              }
//            } else {
//              println("Could not connect: ${ar1.cause().toString()}")
//            }
//          }
//
//        }
