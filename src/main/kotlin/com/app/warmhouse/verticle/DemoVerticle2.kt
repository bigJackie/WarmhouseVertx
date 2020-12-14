package com.app.warmhouse.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message

class DemoVerticle2 : AbstractVerticle() {
  override fun start() {
    vertx.eventBus().consumer("demo.redis") { msg : Message<String> ->
      val index = msg.body().toString()
      msg.reply("On Redis, dB$index")
    }
  }
}
