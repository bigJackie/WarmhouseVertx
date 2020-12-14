package com.app.warmhouse

import io.vertx.core.*
import io.vertx.core.eventbus.EventBusOptions
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.jvm.Throws

object MainCluster {

  private val logger: Logger =  LogManager.getLogger("MainCluster")   // log4j日志管理
  private val vertxOptions = VertxOptions()                                 // Vertx配置项
  private val eventBusOptions = EventBusOptions()                           // eventBus配置项
  private val hostAddress = InetAddress.getLocalHost().hostAddress          // 本机局域网Ip
  private val clusterManager: ClusterManager = HazelcastClusterManager()    // 集群管理

  // 主方法
  @Throws(UnknownHostException::class)
  @JvmStatic
  fun main(args: Array<String>) {

    vertxOptions.setEventBusOptions(eventBusOptions).eventBusOptions.host = hostAddress // 配置eventBus交流IP
    vertxOptions.clusterManager = clusterManager  // 配置集群管理器

    Vertx.clusteredVertx(vertxOptions, this::setCluster) // 部署集群实例
  }

  // 部署集群化的vertx实例
  private fun setCluster(res:AsyncResult<Vertx>){
    if (res.succeeded()) {
      val vertx = res.result()
      // 在异步回调中，获取了vertx实例后，再去部署模块
      // vert.x所有内部逻辑都是异步调用的，所以，如果你在异步回调前就去部署模块，最终会导致集群失败
      setVerticle(vertx, "com.app.warmhouse.verticles.HttpServerVerticle", 2) // 部署Http实例,数量:2
      setVerticle(vertx, "com.app.warmhouse.redis.RedisVerticle", 1)          // 部署Mysql实例
      logger.info("Cluster successfully")
//      setVerticle(vertx, "com.app.warmhouse.verticle.MysqlVerticle", 1) // 部署Redis实例
    } else {
      logger.error("Cluster failed")
    }
  }

  // 部署Verticle
  private fun setVerticle( vertx: Vertx, verticle: String , instance: Int) : Future<String> {
    val promise : Promise<String> = Promise.promise()
    vertx.deployVerticle(verticle, DeploymentOptions().setInstances(instance)) { res ->
      if (res.succeeded()) {
        promise.complete()
        logger.info("Deployment verticle complete: $verticle")
        logger.info("Deployment id is: " + res.result())
      } else {
        logger.error("Deployment failed")
        promise.fail(res.cause())
      }
    }
    return promise.future()
  }
}
