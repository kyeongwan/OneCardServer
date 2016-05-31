import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

/**
 * Created by lk on 16. 4. 28..
 */
public class Test extends AbstractVerticle {

    public static void main(String argp[]){
        Vertx v = Vertx.vertx();
        v.deployVerticle(new HttpVertical());
        v.deployVerticle(new DBVerticle());
        v.deployVerticle(new RestVerticle());
        v.deployVerticle(new ChatVerticle());
    }

    public void start(){
        Vertx v = Vertx.vertx();
        v.deployVerticle(new HttpVertical());
        v.deployVerticle(new DBVerticle());
        v.deployVerticle(new RestVerticle());
        v.deployVerticle(new ChatVerticle());
    }
}
