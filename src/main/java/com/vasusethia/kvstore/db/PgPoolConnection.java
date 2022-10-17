package com.vasusethia.kvstore.db;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class PgPoolConnection {

  public PgPool createPgPool(Vertx vertx){
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("kvstore")
      .setUser("test")
      .setPassword("test");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(10);

    return PgPool.pool(vertx, connectOptions, poolOptions);
  }
}
