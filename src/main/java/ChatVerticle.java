import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class ChatVerticle extends AbstractVerticle {

	BridgeOptions bridgeOptions;
	SockJSHandler sockJSHandler;
	EventBus eb;
	SockJSHandler sc;
	String permissions[] = {
            "to.server.channel",
			"to.channel.channel_id"
    };
	
	Router router;
    int count;
    @Override
    public void start() throws Exception {
    	eb = vertx.eventBus();
    	createOptions();
        router = Router.router(vertx);
        addSockJS(router);
        createHttpSvr(router);
        addCustomEvent();
        permitInit();
    }

    private void permitInit(){
    	String query = "SELECT * FROM channel";
    	eb.send("to.DBVerticle.selectQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

			@Override
			public void handle(AsyncResult<Message<JsonObject>> res) {

				for(int i = 0; i < res.result().body().getJsonArray("result").size(); i++){
					String a[]=res.result().body().getJsonArray("result").getString(i).split(":");
//					permissions[permissions.length]="to.channel."+a[2];
					System.out.println("permmission : "+ a[2]);
					bridgeOptions.addOutboundPermitted(new PermittedOptions().setAddress("to.channel."+a[2]));
				}
				sockJSHandler.bridge(bridgeOptions);
		    	router.route("/eventbus/*").handler(sockJSHandler);
//				createOptions();
			}
    	});
    	
    }
    
    private void createHttpSvr(Router router) {
        StaticHandler sHandler = StaticHandler.create("./www");
        sHandler.setCachingEnabled(false);
        router.route().handler(sHandler);
        vertx.createHttpServer().requestHandler(router::accept).listen(7030);
        
    }

    // only for addSockJS method
    private void createOptions() {
    	bridgeOptions = new BridgeOptions();
        for(String permission : permissions) {
            if(permission.startsWith("to.channel"))
            	bridgeOptions.addOutboundPermitted(new PermittedOptions().setAddress(permission));
            else if(permission.startsWith("to.server"))
            	bridgeOptions.addInboundPermitted(new PermittedOptions().setAddress(permission));
            else
                System.out.println(permission+" is wrong permission!!");
        }
    }

    
    //이건 클라이언트와 이벤트 버스를 공유하기 위한 부분이다. 클라이언트와 서버사이의 Bridge를 만드는 것! /eventbus 로 request가 오는 것은
    //SockJS로 받아 클라이언트와의 브릿지를 두어 이벤트 버스를 공유하게끔 한다.
    //그건 이벤트버스로 메시지를 보내는 것임을 명시하고 있다. 이벤트버스의 데이터 형태로와서 해당 주소를 찾아간다!
    private void addSockJS(Router router) {
    	createOptions();
    	sockJSHandler = SockJSHandler.create(vertx);
    	sockJSHandler.bridge(bridgeOptions);
    	router.route("/eventbus/*").handler(sockJSHandler);
    }
    
    private void addCustomEvent() {	
    	eb.consumer("to.ChatVerticle.userstatus",  new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> objectMessage) {
            	JsonObject msgJO = new JsonObject(objectMessage.body());
                String query = String.format("SELECT * FROM channel_user_list WEHRE channel_id='%s'",msgJO.getString("channel_id"));
            	eb.send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {
            			
            		@Override
					public void handle(AsyncResult<Message<JsonObject>> res) {
            			JsonArray user_list = res.result().body().getJsonArray("results");
        				
        				JsonObject resultJO = new JsonObject();
        				resultJO.put("user_list", user_list);
        				resultJO.put("type", "user_status");
        				eb.publish("to.channel."+msgJO.getString("channel_id"), resultJO.toString());
                    	
            		}
            		
            	});
            }
    	});
    	
        eb.consumer("to.ChatVerticle.permit",new Handler<Message<String>>() {	// 방이 생성되었을 떄 퍼미션 설정
            @Override
            public void handle(Message<String> objectMessage) {
            	System.out.println("permit add : "+ objectMessage.body());
               	bridgeOptions.addOutboundPermitted(new PermittedOptions().setAddress("to.channel."+objectMessage.body()));
               	sockJSHandler.bridge(bridgeOptions);
            	router.route("/eventbus/*").handler(sockJSHandler);
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.put("result_code",0);
            	jsonObject.put("result_msg", "permit add success");
            	System.out.println("permit add success");
            	objectMessage.reply(jsonObject);
            }
        });
            	
        eb.consumer("to.server.channel", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> objectMessage) {
            	//Util.countConsumer("to.server.channel", eb);
            	
            	System.out.println("eventbus chat msg : " + objectMessage.body());
            	JsonObject chatJO = objectMessage.body();
            	
            	String type			 = chatJO.getString("type");  // 공지 notice, 일반 normal, 귓속말 hidden
                String sender_id 	 = chatJO.getString("sender_id");
                String sender_nick   = chatJO.getString("sender_nick");
                String receiver_id   = chatJO.getString("receiver_id");   // 없을 수 있음
                String receiver_nick = chatJO.getString("receiver_nick"); // 없을 수 있음
            	String channel_id	 = chatJO.getString("channel_id");
            	Long date 		 = System.currentTimeMillis();
            	String msg			 = chatJO.getString("msg");
            	chatJO.put("date", date);
            	eb.publish("to.channel."+channel_id, chatJO.toString());

            }
        });
    }
}