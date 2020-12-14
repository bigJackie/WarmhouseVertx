package com.app.warmhouse.verticle;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author: Administrator
 * @Description:
 * @Date: 2020/7/16 10:42
 * @Version: 1.0
 */
public class AppCluster {

  public static void main(String[] args) throws  UnknownHostException {
    final VertxOptions vertxOptions = new VertxOptions();
    EventBusOptions eventBusOptions = new EventBusOptions();
    // 本机局域网Ip
    String hostAddress = InetAddress.getLocalHost().getHostAddress();
    vertxOptions.setEventBusOptions(eventBusOptions).getEventBusOptions().setHost(hostAddress);

    HazelcastClusterManager clusterManager = new HazelcastClusterManager();

    vertxOptions.setClusterManager(clusterManager);

    Vertx.clusteredVertx(vertxOptions, res -> {
      Vertx result = res.result();
      result.deployVerticle(new MainClusterVerticle(), r -> {
        if (r.succeeded()) {
          System.out.println(MainClusterVerticle.class.getName() + " --> 部署成功");
        } else {
          r.cause().printStackTrace();
          System.err.println(MainClusterVerticle.class.getName() + " --> 部署失败, " + r.cause().getMessage());
        }
      });
    });
  }
}
