package com.vasusethia.kvstore;

import com.vasusethia.kvstore.db.SqlQueries;
import io.vertx.core.AbstractVerticle;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CleanUpJob extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(CleanUpJob.class);

  int timeInterval = 43200;

  @Override
  public void start(){
    logger.info("Setting up the cleaning job");

    vertx
      .setPeriodic(21600000, res -> {

        logger.info("Executing a batch delete to delete the expired keys and deleted keys {}", res);

        PgConnectOptions connectOptions = new PgConnectOptions()
          .setPort(5432)
          .setHost("localhost")
          .setDatabase("kvstore")
          .setUser("kojo")
          .setPassword("kojo@123");

        PoolOptions poolOptions = new PoolOptions()
          .setMaxSize(1);

        PgPool.client(vertx, connectOptions, poolOptions)
          .preparedQuery(SqlQueries.CLEANUP_DELETE)
          .execute()
          .onSuccess(rows -> logger.info("Cleanup job {} executed successfully", res))
          .onFailure(Throwable::printStackTrace);
      });
  }
}
