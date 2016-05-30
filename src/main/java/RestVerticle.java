import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
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
//    		{"/user/login", api.LoginAPI.class, "POST"},
    		{"/user/join", api.JoinAPI.class, "POST"}
//    		{"/user/registApp", com.soma.ggamtalk.api.RegistAppAPI.class, "GET"},
//    		{"/user/listApp", com.soma.ggamtalk.api.ListAppAPI.class, "GET"},
//    		{"/user/makeChannel", com.soma.ggamtalk.api.MakeChannelAPI.class, "GET"},
//    		{"/user/listChannel", com.soma.ggamtalk.api.ListChannelAPI.class, "GET"},
//    		{"/user/registNick", com.soma.ggamtalk.api.RegistNickAPI.class, "GET"},
//    		{"/user/joinChannel", com.soma.ggamtalk.api.JoinChannelAPI.class, "GET"},
//    		{"/user/withdrawChannel", com.soma.ggamtalk.api.WithdrawChannelAPI.class, "GET"},
//    		{"/user/getGlobalConfig", com.soma.ggamtalk.api.GetGlobalConfigAPI.class, "GET"},
//    		{"/user/setGlobalConfig", com.soma.ggamtalk.api.SetGlobalConfigAPI.class, "GET"},
//    		{"/user/searchChannel", com.soma.ggamtalk.api.SearchChannelAPI.class, "GET"},
//    		{"/user/setAppConfig", com.soma.ggamtalk.api.SetAppConfigAPI.class, "GET"},
//    		{"/user/getAppConfig", com.soma.ggamtalk.api.GetAppConfigAPI.class, "GET"},
//    		{"/user/getNoticeList", com.soma.ggamtalk.api.GetNoticeListAPI.class, "GET"},
//    		{"/user/changeChannelSet", com.soma.ggamtalk.api.ChangeChannelSetAPI.class, "GET"},
//    		{"/user/customQuery", com.soma.ggamtalk.api.CustomQueryAPI.class, "GET"},
//    		{"/user/appChannel", com.soma.ggamtalk.api.AppChannelAPI.class, "GET"},
//    		{"/user/deleteUser", com.soma.ggamtalk.api.DeleteUserAPI.class, "GET"},
//    		{"/user/logout", com.soma.ggamtalk.api.LogoutAPI.class, "GET"},
//    		{"/user/changePassword", com.soma.ggamtalk.api.ChangePasswordAPI.class, "GET"},
//    		{"/user/removeApp", com.soma.ggamtalk.api.RemoveAppAPI.class, "GET"},
//    		{"/user/removeChannel", com.soma.ggamtalk.api.RemoveChannelAPI.class, "GET"},
//    		{"/user/userListChannel", com.soma.ggamtalk.api.UserListChannelAPI.class, "GET"},
//    		{"/user/noticePush", com.soma.ggamtalk.api.NoticePushAPI.class, "GET"},
//    		{"/user/changeUserColor", com.soma.ggamtalk.api.ChangeUserColorAPI.class, "GET"},
//    		{"/user/traffic", com.soma.ggamtalk.api.TrafficAPI.class, "GET"},
//    		{"/user/test", com.soma.ggamtalk.api.TestAPI.class, "GET"}
    		
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
		
		route.handler(new Handler<RoutingContext>(){

			@Override
			public void handle(RoutingContext routingContext) {
				
				HttpServerRequest request = routingContext.request();
				MultiMap params = request.params();
				
				String uri = request.uri();
				String path = request.path();
				
				String query = request.query();
				JsonObject param = new JsonObject();
				params.forEach(entry -> param.put(entry.getKey(), entry.getValue()));
//				params.forEach(entry -> param.put(entry.getKey(), new String(entry.getValue().toString().getBytes(), "UTF-8")));
				
				System.out.println(request.method().name());
				System.out.println("uri : " + uri);
				System.out.println("path : " + path);
				System.out.println("query : " + query);
				System.out.println("paramters to json : " + param.toString());
				System.out.println("paramters to json : " + params.get("user_id"));
				System.out.println("localAddress : " + request.localAddress());

//				request.handler(new Handler<Buffer>() {
//
//					@Override
//					public void handle(Buffer buffer) {
//						System.out.println("buffer : " + buffer.toString());
//						
//						ObjectMapper m = new ObjectMapper();
//						try {
//							JsonNode rootNode = m.readTree(buffer.toString());
//							System.out.println("buffer : " + buffer.toString());
//							String jsonOutput = m.writeValueAsString(rootNode);
//							System.out.println("jsonOutput : " + jsonOutput);
//							
//						}catch(Exception e){
//						}
//					}
//					
//				});
				request.endHandler(new Handler<Void>() {
					
					@Override
					public void handle(Void empty) {
						
						for(int i=0; i<reqClasses.length; i++) {
							if(path.equals(reqClasses[i][0])) {
								
								try {
									Class cls = (Class)reqClasses[i][1];
									Object object = cls.newInstance();
									System.out.println(object.getClass().getName() + " execute !!");
									Class[] paramTypes = {Vertx.class, HttpServerRequest.class };
									Method apiMethod = cls.getDeclaredMethod("execute", paramTypes);
									request.response().putHeader("content-type", "application/json; charset=UTF-8");
									request.response().putHeader("Access-Control-Allow-Origin", "*" );
									apiMethod.invoke(object, vertx, request);  
									break;
									
								} catch (Exception e) {
									request.response().end("error : "+e.getMessage());
									e.printStackTrace();
									break;
								} 
							}
							if(i==reqClasses.length -1){
								request.response().end("error : "+ "not exist api");
								break;
							}
							
						}
					}
				});
			}
			
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