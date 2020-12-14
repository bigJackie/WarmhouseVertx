package com.app.warmhouse.verticle;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author: Administrator
 * @Description:
 * @Date: 2020/7/16 10:42
 * @Version: 1.0
 */
public class AppCluster2 {

  public static void main(String[] args) throws  UnknownHostException {
    final VertxOptions vertxOptions = new VertxOptions();
    EventBusOptions eventBusOptions = new EventBusOptions();
    // 本机Ip
    String hostAddress = InetAddress.getLocalHost().getHostAddress();
    vertxOptions.setEventBusOptions(eventBusOptions).getEventBusOptions().setHost(hostAddress);

    HazelcastClusterManager clusterManager = new HazelcastClusterManager();

    vertxOptions.setClusterManager(clusterManager);
    Vertx.clusteredVertx(vertxOptions, res -> {
      Vertx result = res.result();
      result.deployVerticle(new MainClusterVerticle2(), r -> {
        if (r.succeeded()) {
          System.out.println(MainClusterVerticle2.class.getName() + " --> 部署成功");
        } else {
          r.cause().printStackTrace();
          System.err.println(MainClusterVerticle2.class.getName() + " --> 部署失败, " + r.cause().getMessage());
        }
      });
    });
  }
}
