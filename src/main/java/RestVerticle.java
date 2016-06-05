import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Method;

public class RestVerticle extends AbstractVerticle {
    
	EventBus eventBus;
	
    Object reqClasses[][] = new Object[][]{
    		{"/user/login", api.LoginAPI.class, "GET"},
    		{"/user/join", api.JoinAPI.class, "GET"},
    		{"/user/makeroom", api.MakeRoomAPI.class, "GET"},
    		{"/user/test", api.TestAPI.class, "GET"},
			{"/user/joinroom", api.JoinRoomAPI.class, "GET"},
			{"/user/roomlist", api.ListRoomAPI.class, "GET"},
			{"/user/gamestart", api.GameStartAPI.class, "GET"}
    		
    };
    
    public void consumerEventBus(){
    	eventBus = vertx.eventBus();
    }
 
	@Override
	public void start() throws Exception {
		super.start();
		
		consumerEventBus();

		Router router = Router.router(vertx);
		Route route = router.route("/user/*");

		route.handler(routingContext ->{

				HttpServerRequest request = routingContext.request();
				MultiMap params = request.params();


				String uri = request.uri();
				String path = request.path();

				String query = request.query();
				JsonObject param = new JsonObject();
				params.forEach(entry -> param.put(entry.getKey(), entry.getValue()));

				System.out.println(request.method().name());
				System.out.println("uri : " + uri);
				System.out.println("path : " + path);
				System.out.println("query : " + query);
				System.out.println("paramters to json : " + param.toString());
				System.out.println("paramters to json : " + params.get("user_id"));
				System.out.println("localAddress : " + request.localAddress());

				request.endHandler(new Handler<Void>() {

					@Override
					public void handle(Void empty) {

						for (int i = 0; i < reqClasses.length; i++) {
							if (path.equals(reqClasses[i][0])) {

								try {
									Class cls = (Class) reqClasses[i][1];
									Object object = cls.newInstance();
									System.out.println(object.getClass().getName() + " execute !!");
									Class[] paramTypes = {Vertx.class, HttpServerRequest.class};
									Method apiMethod = cls.getDeclaredMethod("execute", paramTypes);
									request.response().putHeader("content-type", "application/json; charset=UTF-8");
									request.response().putHeader("Access-Control-Allow-Origin", "*");
									apiMethod.invoke(object, vertx, request);
									break;

								} catch (Exception e) {
									request.response().end("error : " + e.getMessage());
									e.printStackTrace();
									break;
								}
							}
							if (i == reqClasses.length - 1) {
								request.response().end("error : " + "not exist api");
								break;
							}

						}
					}
				});

		});
	
		HttpServerOptions httpServerOptions = new HttpServerOptions();
		httpServerOptions.setCompressionSupported(true);

		vertx.createHttpServer(httpServerOptions)
				.requestHandler(router::accept)
				.listen(7010);
		
	}

	
	
	@Override
	public void stop() throws Exception {

	}

}