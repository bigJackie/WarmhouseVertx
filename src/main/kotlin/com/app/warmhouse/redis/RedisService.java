package com.app.warmhouse.redis;


import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.RedisAPI;

import java.util.Set;


@ProxyGen
@VertxGen
public interface RedisService {

  @GenIgnore
  static RedisService create(Vertx vertx, String db, Handler<AsyncResult<RedisService>> readyHandler) {
    return new RedisServiceImpl(vertx, db, readyHandler);
  }

  @GenIgnore
  static RedisService createProxy(Vertx vertx, String address) {
    return new RedisServiceVertxEBProxy(vertx, address);
  }

  @Fluent
  RedisService getString(JsonObject obj, Handler<AsyncResult<String>> resultHandler);
}
