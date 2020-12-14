/*
 *  Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *  Copyright (c) 2017 INSA Lyon, CITI Laboratory.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.warmhouse;

import com.app.warmhouse.redis.RedisVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import com.app.warmhouse.redis.RedisService;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) throws Exception {

    Promise<String> dbVerticleDeployment = Promise.promise();
    vertx.deployVerticle(new RedisVerticle(), dbVerticleDeployment);

    dbVerticleDeployment.future().compose(id -> {

      Promise<String> httpVerticleDeployment = Promise.promise();
      vertx.deployVerticle(
        "com.app.warmhouse.verticles.HttpServerVerticle",
        new DeploymentOptions().setInstances(2),
        httpVerticleDeployment);

      return httpVerticleDeployment.future();

    });
  }
}
