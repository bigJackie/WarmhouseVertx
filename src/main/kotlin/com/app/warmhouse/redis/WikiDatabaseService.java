package com.app.warmhouse.redis;


import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;


/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@ProxyGen
@VertxGen
public interface WikiDatabaseService {

  @GenIgnore
  static WikiDatabaseService create(Handler<AsyncResult<WikiDatabaseService>> readyHandler) {
    return new WikiDatabaseServiceImpl(readyHandler);
  }

//  @GenIgnore
//  static WikiDatabaseService createProxy(Vertx vertx, String address) {
//    return new WikiDatabaseServiceVertxEBProxy(vertx, address);
//  }
}
