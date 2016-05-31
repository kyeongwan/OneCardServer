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
		// 서버에서 공용 이벤트버스로 채팅 메시지를 받고 이벤트 버스로 전달된 채널ID와 메시지 내용으로 해당 이벤트버스를 이용해 전달하면된다 ?
		
//        vertx.setPeriodic(1000, t -> vertx.eventBus().publish("to.client.BroadcastNewsfeed", "news from the server! " + (count++)));
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
//    	sockJSHandler.socketHandler(new Handler<SockJSSocket>() {
//
//			@Override
//			public void handle(SockJSSocket socket) {
//				System.out.println("writer ID : "+ socket.writeHandlerID());
//				System.out.println("hashCode ID : "+ socket.hashCode());
//				System.out.println("uri ID : "+ socket.uri());
//				System.out.println("headers ID : "+ socket.headers().toString());
//				System.out.println("webUser ID : "+ socket.webUser().toString());
//				
//			}
//		});
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
    	
//    	eb.consumer("to.ChatVerticle.changestatus",  new Handler<Message<String>>() {
//            @Override
//            public void handle(Message<String> objectMessage) {
//            	JsonObject msgJO = new JsonObject(objectMessage.body());
//                String query = String.format("UPDATE * FROM channel_user_list WEHRE channel_id='%s'",msgJO.getString("channel_id"));
//            	eb.send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {
//            			
//            		@Override
//					public void handle(AsyncResult<Message<JsonObject>> res) {
//            			JsonArray user_list = res.result().body().getJsonArray("results");
//        				
//        				JsonObject resultJO = new JsonObject();
//        				resultJO.put("user_list", user_list);
//        				resultJO.put("type", "user_status");
//        				eb.publish("to.channel."+msgJO.getString("channel_id"), resultJO.toString());
//                    	
//            		}
//            		
//            	});
//            }
//    	});
    	
    	
//    	eb.consumer("to.ChatVerticle.notice", new Handler<Message<String>>() {
//            @Override
//            public void handle(Message<String> objectMessage) {
//            	System.out.println("Chat notice : "+ objectMessage.body());
//            	JsonObject msgJO = new JsonObject(objectMessage.body());
//            	if("app".equals(msgJO.getString("to_what"))){
//
//            		String query = String.format("SELECT channel_id FROM channel WHERE app_id = '%s'", msgJO.getString("app_id"));
//            	    eb.send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {
//
//    					@Override
//    					public void handle(AsyncResult<Message<JsonObject>> res) {
//    						JsonArray channel_list = res.result().body().getJsonArray("results");
//
//    						for(int i=0; i<channel_list.size(); i++){
//    							eb.publish("to.channel."+channel_list.getJsonObject(i).getString("channel_id"), msgJO.toString());
//    						}
//
//    						JsonObject resultJO = new JsonObject();
//							resultJO.put("result_code", 0);
//							resultJO.put("result_msg", "Chat 공지 발송을 완료하였습니다.");
//							objectMessage.reply(resultJO);
//    					}
//    				});
//
//            	}else if("channel".equals(msgJO.getString("to_what"))){
//            		eb.publish("to.channel."+msgJO.getString("channel_id"), msgJO.toString());
//            		JsonObject resultJO2 = new JsonObject();
//					resultJO2.put("result_code", 0);
//					resultJO2.put("result_msg", "GCM Push 발송을 완료하였습니다.");
//					objectMessage.reply(resultJO2);
//            	}else{
//
//            		String query = String.format("SELECT channel_id FROM channel");
//            	    eb.send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {
//
//    					@Override
//    					public void handle(AsyncResult<Message<JsonObject>> res) {
//    						JsonArray channel_list = res.result().body().getJsonArray("results");
//
//    						for(int i=0; i<channel_list.size(); i++){
//    							eb.publish("to.channel."+channel_list.getJsonObject(i).getString("channel_id"), msgJO.toString());
//    						}
//    					}
//    				});
//
//            		JsonObject resultJO3 = new JsonObject();
//					resultJO3.put("result_code", 0);
//					resultJO3.put("result_msg", "Chat 공지 발송을 완료하였습니다.");
//					objectMessage.reply(resultJO3);
//            	}
//
//
//            }
//        });
    	
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
            	
            	JsonObject notification = new JsonObject();
				notification.put("title", "껨톡");
        		notification.put("text", chatJO.getString("msg"));
        		String query = String.format("SELECT user_id FROM channel_user_list WHERE channel_id = '%s'", chatJO.getString("channel_id"));
        		eb.send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

					@Override
					public void handle(AsyncResult<Message<JsonObject>> res) {
						JsonArray ja = res.result().body().getJsonArray("results");
						StringBuilder setQuery=new StringBuilder();
						
						for(int j = 0; j < ja.size(); j++){
								 if(j != 0)
									 setQuery.append("and ");
								 else{
									 setQuery.append("WHERE ");
									 
								 }
								 
					             setQuery.append("user_id");
					             setQuery.append("='");
					             setQuery.append(ja.getJsonObject(j).getString("user_id"));
					             setQuery.append("' ");
						}
					        
						
						String query2 = String.format("SELECT gcm_id FROM user %s", setQuery);
						eb.send("to.DBVerticle.selectCustomQuery", query2, new Handler<AsyncResult<Message<JsonObject>>>() {

							@Override
							public void handle(AsyncResult<Message<JsonObject>> res) {
								JsonArray gcm_list = res.result().body().getJsonArray("results");
								String registration_ids="";
								StringBuilder sb = new StringBuilder();
								for(int i=0; i<gcm_list.size(); i++){
									if(i!=0)
									sb.append(",");
									sb.append(gcm_list.getJsonObject(i).getString("gcm_id"));
//										registration_ids.add(gcm_list.getJsonObject(i).getString("gcm_id"));
								}
								registration_ids = sb.toString();
								
								
								JsonObject body = new JsonObject();
								body.put("notification", notification);
								body.put("registration_ids", registration_ids);
								body.put("data", chatJO);
								body.put("sender_id", chatJO.getString("sender_id"));
								body.put("msg", chatJO.getString("msg"));
//								eb.send("to.GCMVerticle.chat", body.toString(), new Handler<AsyncResult<Message<JsonObject>>>() {
//
//									@Override
//									public void handle(AsyncResult<Message<JsonObject>> res) {
//										System.out.println("GCM send success : "+ res.result().body().toString());
//
//										JsonObject resultJO = new JsonObject();
//										resultJO.put("result_code", 0);
//										resultJO.put("result_msg", "Chat 공지 발송을 완료하였습니다.");
//										objectMessage.reply(resultJO);
//									}
//								});
								
								
							}
						});
						
						
					}
				});
            }
        });
        // 타 버티클에서 접속자에게 실시간 뉴스피드 전송  // 다른 버티클에서 이버티클에 메시지를 보내는데 to.server.BroadcastNewsfeed 이통로를 이용해서 보내면
        // 그것을 클라이언트에게 pub해버린다.
    }
}