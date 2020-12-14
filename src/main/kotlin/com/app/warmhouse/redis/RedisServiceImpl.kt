package com.app.warmhouse.redis

import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.math.log


class RedisServiceImpl(vertx: Vertx, db: String, readyHandler: Handler<AsyncResult<RedisService>>) : RedisService {

  private val logger: Logger =  LogManager.getLogger("RedisVerticle")   // log4j日志管理
  private val promise : Promise<JsonObject> = Promise.promise()
  private lateinit var redisOptions: RedisOptions // Redis配置
  private lateinit var redisClient: Redis         // Redis客户端

  private lateinit var redis: RedisAPI           // RedisAPI
  private var vertx: Vertx = vertx
  private var readyHandler: Handler<AsyncResult<RedisService>> = readyHandler

  init{
    val retriever = ConfigRetriever.create(vertx)         // 初始化vertx配置
    retriever.getConfig { ar ->
      if (ar.failed()) {
        logger.error("Get local redis config failed")
        promise.fail (ar.cause())
        this.readyHandler.handle(Future.failedFuture(ar.cause()))
      } else {
        logger.info("Get local redis config successfully")
        val config = ar.result()

        // 配置Redis参数
        redisOptions = RedisOptions()
          .setConnectionString(config.getJsonObject("database").getJsonObject("redis").getString("host") + db)
          .setPassword(config.getJsonObject("database").getJsonObject("redis").getString("password"))

        // 配置Redis客户端
        redisClient = Redis.createClient(this.vertx,redisOptions)
        redisClient.connect().onSuccess{
          logger.info("Connect redis successfully")
        }.onFailure{
          logger.error("Connect redis failed")
        }

        // 配置redisAPI
        redis = RedisAPI.api(redisClient)
        this.readyHandler.handle(Future.succeededFuture(this))
      }
    }
  }

  override fun getString(obj: JsonObject, resultHandler: Handler<AsyncResult<String>>): RedisService {
    val list: MutableList<String> = mutableListOf()
    for (i in 1 until obj.size() - 1){
      list.add(obj.getString("ar$i"))
    }
    redis.mget(list).onSuccess{ value ->
        logger.debug("Redis get string [$list] successfully")
        resultHandler.handle(Future.succeededFuture(value.toString()))
    }.onFailure{ error ->
        logger.warn("Redis get string failed")
        resultHandler.handle(Future.failedFuture(error.cause))
    }
    return this
  }

}
