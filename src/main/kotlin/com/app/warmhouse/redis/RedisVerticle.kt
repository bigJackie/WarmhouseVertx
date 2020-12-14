package com.app.warmhouse.redis

//import com.app.warmhouse.redis.RedisService2.Companion.create
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.serviceproxy.ServiceBinder


class RedisVerticle : AbstractVerticle() {

  override fun start(promise: Promise<Void>) {
//    println("start thread" + Thread.currentThread().name)

    RedisService.create(vertx, "0") { ready ->
      if(ready.succeeded()) {
        ServiceBinder(vertx).setAddress("db.redis").register(RedisService::class.java, ready.result())
        promise.complete()
      } else {
        promise.fail(ready.cause())
      }
    }
  }

  // 分发Redis请求
  private fun distributeRequest(message: Message<JsonObject>){
    val index = message.headers()["index"]
    if(index == null) message.reply("null") else message.reply(index.toString())
  }
}
