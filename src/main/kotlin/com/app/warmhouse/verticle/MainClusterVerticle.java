package com.app.warmhouse.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainClusterVerticle extends AbstractVerticle {

  public void start() {
    System.out.println("start thread" + Thread.currentThread().getName());
    //发布eventbus服务
    vertx.eventBus().consumer("com.xiaoniu.bus", msg -> {
      System.out.println("read thread" + Thread.currentThread().getName());
      System.out.println("收到消息");
      System.out.println(msg != null ? ((JsonObject) msg.body()).encodePrettily() : "没有消息");
      JsonObject j = new JsonObject();
      j.put("info", "我是Main");
      msg.reply(j);
    });
    //发布web服务

    // 创建HttpServer
    HttpServer server = vertx.createHttpServer();
    // 创建路由对象
    Router router = Router.router(vertx);

    // 监听/index地址
    router.route("/index").handler(request -> {
      request.response().end("INDEX SUCCESS");
    });

    // 监听/index地址
    router.route("/index1").handler(request -> {
      request.response().end("INDEX1 SUCCESS");
    });
    // 把请求交给路由处理--------------------(1)
    server.requestHandler(router);
    server.listen(8888);
  }
}
