import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Created by lk on 16. 4. 28..
 */
public class HttpServerVertical extends AbstractVerticle {

    Router router;
    public void start() {
        router = Router.router(vertx);

        StaticHandler sHandler = StaticHandler.create("./www");
        sHandler.setCachingEnabled(false);
        router.route().handler(sHandler);
        vertx.createHttpServer().requestHandler(router::accept).listen(8081);
    }


    public void stop() {
    }
}
