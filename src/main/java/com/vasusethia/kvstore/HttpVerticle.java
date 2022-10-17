package com.vasusethia.kvstore;

import com.vasusethia.kvstore.db.PgPoolConnection;
import com.vasusethia.kvstore.db.SqlQueries;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class HttpVerticle extends AbstractVerticle {

  private static Logger logger = LoggerFactory.getLogger(HttpVerticle.class);

  PgPool pgPool;
  @Override
  public void start(Promise<Void> startPromise){

    logger.info("Starting the kv store api");
    startHttpServer(startPromise, vertx);
  }

  public void startHttpServer(Promise<Void> startPromise, Vertx vertx){

    final Router router = Router.router(vertx);
    this.pgPool = new PgPoolConnection().createPgPool(vertx);

    BodyHandler bodyHandler = BodyHandler.create();

    router.route().handler(bodyHandler);
    attachRoutes(router);
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8880)
      .onSuccess(res -> {
        logger.info("Started the http server on port 8888");
        startPromise.complete();
      })
      .onFailure(err -> startPromise.fail(err.getLocalizedMessage()));

  }

  public void attachRoutes(Router router) {
    logger.info("Attaching the routes");

    router
      .post("/get")
      .handler(ctx -> {
        String key = ctx.body().asJsonObject().getString("key");
        logger.info("Received a get request for the key {}", key);
        if (key == null || key.isEmpty()) {
          ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Empty key").toString());
        }

        pgPool
          .getConnection(conn -> {
            if (conn.succeeded()) {
              conn.result().preparedQuery(SqlQueries.GET).execute(Tuple.of(key))
                .onSuccess(rows -> {
                  if (rows.rowCount() == 0) {
                    logger.info("Unable to find the key in the db {}", key);
                    ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Key not found").toString());
                  } else {
                    logger.info("Found the key {}", key);
                    Row row = rows.iterator().next();
                    JsonObject returnValue = new JsonObject()
                      .put("value", row.getString("value"));
                    ctx.response().setStatusCode(200).end(returnValue.toString());
                  }
                  conn.result().close();
                })
                .onFailure(err -> {
                  err.printStackTrace();
                  conn.result().close();
                });
              conn.result().close();
            }
          });
      });

    router
      .post("/delete")
      .handler(ctx -> {
        String key = ctx.body().asJsonObject().getString("key");
        logger.info("Received a delete request for the key {}", key);
        if (key == null || key.isEmpty()) {
          ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Empty key").toString());
        }

        pgPool
          .getConnection(conn -> {
            if (conn.succeeded()) {
              conn.result().preparedQuery(SqlQueries.DELETE).execute(Tuple.of(key))
                .onSuccess(rows -> {
                  if (rows.rowCount() == 0) {
                    logger.info("Deleted the key");
                    ctx.response().setStatusCode(200).end("Unable to find the key");
                  } else {
                    ctx.response().setStatusCode(200).end("key deleted successfully");
                  }
                })
                .onFailure(Throwable::printStackTrace);
            }
            conn.result().close();
          });

      });

    router
      .post("/put")
      .handler(ctx -> {
        String key = ctx.body().asJsonObject().getString("key");
        String value = ctx.body().asJsonObject().getString("value");
        Integer ttl = ctx.body().asJsonObject().getInteger("ttl");
        logger.info("Received a put request for the key {}", key);
        if (key == null || key.isEmpty()) {
          ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Empty key").toString());
          return;
        }

        if(value == null || value.isEmpty()){
          ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Empty value").toString());
          return;
        }

        if(ttl == null || ttl < 0){
          ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Invalid ttl").toString());
          return;
        }

        //calculate the expiry timestamp since epoch
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, ttl);
        Long expiryTimeStamp = calendar.getTimeInMillis() / 1000L;

        pgPool
          .getConnection(conn -> {
            if (conn.succeeded()) {
              conn.result().preparedQuery(SqlQueries.PUT).execute(Tuple.of(key, value, expiryTimeStamp))
                .onSuccess(res -> {
                  logger.info("Key inserted successfully {}", key);
                  ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Key inserted").toString());
                })
                .onFailure(err -> {
                  err.printStackTrace();
                  logger.error("Error while inserting the key " + key + " error:- " + err.getLocalizedMessage());
                  ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Error while inserting key").toString());
                });
            }
            conn.result().close();
          });
      });
  }
}
