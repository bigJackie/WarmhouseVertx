package com.app.warmhouse.verticle

import io.netty.util.internal.logging.InternalLogger
import io.netty.util.internal.logging.Log4J2LoggerFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.jvm.Throws

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
//    this.setVerticle("com.app.warmhouse.verticle.JsonVerticle")
//    this.setVerticle("com.app.warmhouse.verticle.MysqlVerticle")
//    vertx.deployVerticle(DemoVerticle())
//    vertx.deployVerticle(DemoVerticle2())
    val logger:Logger =  LogManager.getLogger("MainVerticle")
//    logger.error("感受着对方")

    this.setVerticle("com.app.warmhouse.verticle.DemoVerticle")
    this.setVerticle("com.app.warmhouse.verticle.DemoVerticle2")
    val router =  Router.router(vertx)
    router.route("/api/db").handler(this::mysqlDemo)
    router.route("/api/db/:index").handler(this::redisDemo)
    vertx.createHttpServer().requestHandler(router).listen(8888)
  }

  @Throws(Exception::class)
  override fun stop() {
    super.stop()
  }

  fun mysqlDemo(ctx: RoutingContext) {
    vertx.eventBus().request("demo.mysql", "") { reply: AsyncResult<Message<String>> ->
      ctx.request().response().end(reply.result().body().toString())
    }
  }

  fun redisDemo(ctx: RoutingContext) {
    val index = ctx.pathParam("index")
    vertx.eventBus().request("demo.redis", index) { reply: AsyncResult<Message<String>> ->
      ctx.request().response().end(reply.result().body())
    }
  }

  private fun setVerticle( verticle: String ) : Future<String> {
    val promise : Promise<String> = Promise.promise()
    vertx.deployVerticle(verticle) { res ->
      if (res.succeeded()) {
        promise.complete()
        println("Deployment complete: $verticle")
        println("Deployment id is: " + res.result())
      } else {
        println("Deployment failed!")
        promise.fail(res.cause())
      }
    }
    return promise.future()
  }
}
