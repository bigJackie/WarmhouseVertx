package com.app.warmhouse.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message

class DemoVerticle : AbstractVerticle() {
  override fun start() {
    vertx.eventBus().consumer("demo.mysql") { msg : Message<String> ->
      msg.reply("On mysql")
    }
  }
}
