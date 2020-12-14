package com.app.warmhouse.verticles

import com.app.warmhouse.redis.RedisService
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.get
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class HttpServerVerticle : AbstractVerticle() {

  private val logger: Logger =  LogManager.getLogger("HttpServerVerticle")  // log4j日志管理
  private lateinit var redisService: RedisService

  override fun start() {
    redisService = RedisService.createProxy(vertx, "db.redis")
    //发布web服务

    // 创建HttpServer
    val server = vertx.createHttpServer()
    // 创建路由对象
    val router = Router.router(vertx)
    // 获取body参数
    router.route().handler(BodyHandler.create())

    // 监听/index地址
    router.route("/api/mysql/").handler(this::mysqlRequest)
    router.route("/api/redis/").handler(this::redisDistributor)

    // 把请求交给路由处理
    server.requestHandler(router)
    server.listen(8888){ res ->
      if (res.succeeded()) logger.info("Run HttpServer successfully at port 8888")
      else logger.error("Run HttpServer failed")
    }
  }

  private fun mysqlRequest(ctx: RoutingContext) {
    vertx.eventBus().request("db.mysql", "") { reply: AsyncResult<Message<String>> ->
      ctx.request().response().end(reply.result().body())
    }
  }

  // redis请求过滤分发
  private fun redisDistributor(ctx: RoutingContext) {
    when(ctx.bodyAsJson.get<String>("dataType")) {
      "String" -> redisStringRequest(ctx)
    }
  }

  // redis String操作
  private fun redisStringRequest(ctx: RoutingContext) {
    when(ctx.bodyAsJson.get<String>("method")) {
      "GET" -> {
        redisService.getString(ctx.bodyAsJson){ reply ->
          if (reply.succeeded()) {
            ctx.response().end(reply.result().toString())
          } else {
            ctx.fail(reply.cause())
          }
        }
      }
      "SET" -> {

      }
    }
  }
}
