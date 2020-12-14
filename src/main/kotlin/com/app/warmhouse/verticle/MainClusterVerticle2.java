package com.app.warmhouse.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainClusterVerticle2 extends AbstractVerticle {
  public void start() {
    System.out.println("start thread" + Thread.currentThread().getName());
    //发布web服务

    // 创建HttpServer
    HttpServer server = vertx.createHttpServer();
    // 创建路由对象
    Router router = Router.router(vertx);
    // 监听/index地址
    router.route("/index").handler(request -> {
      System.out.println("haha" + Thread.currentThread().getName());
      System.out.println("时间到了，发送消息");
      JsonObject json = new JsonObject().put("info", "我是另主");
      System.out.println("send thread" + Thread.currentThread().getName());
      //通过eventbus发送请求
      vertx.eventBus().request("com.xiaoniu.bus", json, msg -> {
        System.out.println("read thread" + Thread.currentThread().getName());
        if (msg.succeeded()) {
          System.out.println(msg.result() != null
            ? ((JsonObject)msg.result().body()).encodePrettily()
            : "没有信息");
        } else {
          System.err.println(msg.cause().getMessage());
          msg.cause().printStackTrace();
        }
      });
      request.response().end("INDEX SUCCESS");
    });

    // 把请求交给路由处理--------------------(1)
    server.requestHandler(router);
    server.listen(7777);


  }
}
