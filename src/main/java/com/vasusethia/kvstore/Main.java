package com.vasusethia.kvstore;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String args[]) throws Exception {

    Vertx vertx = Vertx.vertx();

    DeploymentOptions options = new DeploymentOptions().setInstances(8);

    vertx.deployVerticle("com.vasusethia.kvstore.HttpVerticle", options)
      .onSuccess(res -> {
        logger.info("HTTP Verticle deployed successfully");
      })
      .onFailure(Throwable::printStackTrace);

    vertx.deployVerticle(new CleanUpJob())
      .onSuccess(res -> {
        logger.info("Clean up job deployed successfully");
      })
      .onFailure(Throwable::printStackTrace);

  }
}
