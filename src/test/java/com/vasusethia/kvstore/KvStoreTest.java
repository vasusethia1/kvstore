package com.vasusethia.kvstore;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test for KV Store")
@Testcontainers
public class KvStoreTest {

  @Container
  private static GenericContainer postgres = (GenericContainer) new PostgreSQLContainer(DockerImageName.parse("postgres:14.1-alpine"))
    .withDatabaseName("kvstore")
    .withUsername("kojo")
    .withPassword("kojo@123")
    .withTmpFs(singletonMap("/var/lib/postgresql/data", "rw"))
    .withFileSystemBind("src/test/resources/init_db.sql", "/docker-entrypoint-initdb.d/init.sql");


  private static RequestSpecification requestSpecification;

  static void prepareSpec() {
    requestSpecification = new RequestSpecBuilder()
      .addFilters(asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))
      .setBaseUri("http://localhost:8880/")
      .build();
  }

  @BeforeAll
  static void setUp(Vertx vertx, VertxTestContext testContext) {
    prepareSpec();
    vertx.deployVerticle(new HttpVerticle()).onSuccess(res -> testContext.completeNow()).onFailure(err -> testContext.failNow(err));
  }

  @Test
  @Order(1)
  @DisplayName("PUT key")
  public void test10(VertxTestContext vertxTestContext){
    JsonObject request = new JsonObject().put("key", "PeopleProfile:1").put("value", "9176244238").put("ttl", 300);
    var responseHtmlPath = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(request.toString())
      .post("/put")
      .then()
      .assertThat()
      .statusCode(200)
      .extract().body();
    assertThat(responseHtmlPath.jsonPath().getString("message").contains("Key inserted")).isEqualTo(true);
    vertxTestContext.completeNow();
  }

  @Test
  @Order(1)
  @DisplayName("GET key")
  public void test20(VertxTestContext vertxTestContext){
    JsonObject request = new JsonObject().put("key", "PeopleProfile:1").put("value", "9176244238").put("ttl", 300);
    var responseHtmlPath = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(request.toString())
      .post("/put")
      .then()
      .assertThat()
      .statusCode(200)
      .extract().body();
    assertThat(responseHtmlPath.jsonPath().getString("message").contains("Key inserted")).isEqualTo(true);

    JsonObject getRequest = new JsonObject().put("key", "PeopleProfile:1");

    var responseHtmlPath1 = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(getRequest.toString())
      .post("/get")
      .then()
      .assertThat()
      .statusCode(200)
      .extract().body();

    assertThat(responseHtmlPath1.jsonPath().getString("value").contains("9176244238")).isEqualTo(true);
    vertxTestContext.completeNow();

  }

  @Test
  @Order(1)
  @DisplayName("DELETE key")
  public void test30(VertxTestContext vertxTestContext){
    JsonObject request = new JsonObject().put("key", "PeopleProfile:1").put("value", "9176244238").put("ttl", 300);
    var responseHtmlPath = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(request.toString())
      .post("/put")
      .then()
      .assertThat()
      .statusCode(200)
      .extract().body();
    assertThat(responseHtmlPath.jsonPath().getString("message").contains("Key inserted")).isEqualTo(true);

    JsonObject getRequest = new JsonObject().put("key", "PeopleProfile:1");

    var responseHtmlPath1 = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(getRequest.toString())
      .post("/delete")
      .then()
      .assertThat()
      .statusCode(200)
      .extract().body();

    assertThat(responseHtmlPath1.asString().contains("successfully")).isEqualTo(true);
    vertxTestContext.completeNow();

  }

  @Test
  @Order(1)
  @DisplayName("UPDATE key")
  public void test40(VertxTestContext vertxTestContext){
    JsonObject request = new JsonObject().put("key", "PeopleProfile:1").put("value", "9176244238").put("ttl", 300);
    var responseHtmlPath = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(request.toString())
      .post("/put")
      .then()
      .assertThat()
      .statusCode(200)
      .extract().body();
    assertThat(responseHtmlPath.jsonPath().getString("message").contains("Key inserted")).isEqualTo(true);


    JsonObject updateRequest = new JsonObject().put("key", "PeopleProfile:1").put("value", "9176244239").put("ttl", 300);
    JsonObject getRequest = new JsonObject().put("key", "PeopleProfile:1");

    var responseHtmlPath1 = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(updateRequest.toString())
      .post("/put")
      .then()
      .assertThat()
      .statusCode(200);

    var responseHtmlPath2 = given()
      .spec(requestSpecification)
      .accept(ContentType.HTML)
      .contentType("application/json")
      .body(getRequest.toString())
      .post("/get")
      .then()
      .assertThat()
      .statusCode(200)
      .extract().body();

    assertThat(responseHtmlPath2.jsonPath().getString("value").contains("9176244239")).isEqualTo(true);
    vertxTestContext.completeNow();

  }
}
