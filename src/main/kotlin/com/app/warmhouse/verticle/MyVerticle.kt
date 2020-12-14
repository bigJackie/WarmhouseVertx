package com.app.warmhouse.verticle

import com.app.warmhouse.utils.TokenUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.*

class MyVerticle : AbstractVerticle() {

  lateinit var router : Router // declare router

  private lateinit var connectOptions : MySQLConnectOptions // Connect options

  private lateinit var poolOptions : PoolOptions  // Pool options

  lateinit var client : MySQLPool // Create the client pool

  private var tokenUtils = TokenUtils()

  override fun start(startPromise: Promise<Void>) {
    // init router
    router = Router.router(vertx)

    // allow cross-domain
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.POST));

    // configure router analysis url
    router.route("/").handler{ req ->
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Connect Success!")
    }

    val myClient = this.getConfig()
    myClient.compose { http -> this.createHttpServer(8888) }
    myClient.compose { http -> this.createHttpServer(8844) }

  }

  private fun getConfig() : Future<JsonObject> {
    val promise : Promise<JsonObject> = Promise.promise()
    val retriever = ConfigRetriever.create(vertx) // init vert.x configuration

    retriever.getConfig(Handler { ar ->
      if (ar.failed()) {
        // Failed to retrieve the configuration
        promise.fail(ar.cause())
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
        promise.complete(ar.result())
      }
    })
    return promise.future()
  }


  private fun createHttpServer(port : Int) : Future<HttpServer> {
    val promise : Promise<HttpServer> = Promise.promise()
    // bind router with the vertx HTTPServer
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(port) { http ->
        if (http.succeeded()) {
          promise.complete()
          println("HTTP server started on port $port")
        } else {
          promise.fail(http.cause());
        }
      }
    return promise.future()
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
}
