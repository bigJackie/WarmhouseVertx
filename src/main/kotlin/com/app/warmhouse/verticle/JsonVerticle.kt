package com.app.warmhouse.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject

class JsonVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    vertx
      .createHttpServer()
      .requestHandler { req ->
        if (req.path().startsWith("/sss")){
          req.response()
            .putHeader("content-type", "application/json")
            .end(JsonObject().put("name","Jackie").toString())
        }
      }
      .listen(8888) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}
