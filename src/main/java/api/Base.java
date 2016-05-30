package api;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

/**
 * Created by lk on 16. 5. 31..
 */
public abstract class Base {
    Vertx vertx;
    HttpServerRequest request;
    JsonObject params;


    public void init(Vertx vertx, HttpServerRequest request) {
        this.vertx = vertx;
        this.request = request;
        MultiMap param = request.params();
        params = new JsonObject();
        param.forEach(entry -> params.put(entry.getKey(), entry.getValue()));

    }
}
