package com.app.warmhouse.verticles

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class MysqlVerticle : AbstractVerticle() {
  override fun start() {
    println("start thread" + Thread.currentThread().name)
    //发布web服务

    // 创建HttpServer
    val server = vertx.createHttpServer()
    // 创建路由对象
    val router = Router.router(vertx)
    // 监听/index地址
    router.route("/index").handler { request: RoutingContext ->
      println("haha" + Thread.currentThread().name)
      println("时间到了，发送消息")
      val json = JsonObject().put("info", "我是另主")
      println("send thread" + Thread.currentThread().name)
      //通过eventbus发送请求
      vertx.eventBus().request("com.xiaoniu.bus", json) { msg: AsyncResult<Message<Any>?> ->
        println("read thread" + Thread.currentThread().name)
        if (msg.succeeded()) {
          println(if (msg.result() != null) (msg.result()!!.body() as JsonObject).encodePrettily() else "没有信息")
        } else {
          System.err.println(msg.cause().message)
          msg.cause().printStackTrace()
        }
      }
      request.response().end("INDEX SUCCESS")
    }

    // 把请求交给路由处理--------------------(1)
    server.requestHandler(router)
    server.listen(7777)
  }
}
