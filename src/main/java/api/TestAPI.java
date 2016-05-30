package api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

public class TestAPI extends Base{

	public TestAPI() { }

	public JsonObject checkValidation(JsonObject Params) {
		return null;
	}

	public void execute(Vertx vertx,HttpServerRequest request) {
		init(vertx, request);
	
//		 vertx.eventBus().send("to.MonitorVerticle.test", "test", new Handler<AsyncResult<Message<Object>>>() {
//             @Override
//             public void handle(AsyncResult<Message<Object>> messageAsyncResult) {
////                 System.out.println(messageAsyncResult.succeeded());
////                 System.out.println("cluster success : " + );
//                 request.response().end(messageAsyncResult.result().body().toString());
//             }
//         });
				
	}
//
//	@Override
//	public void onExecute(int what, JsonObject resultJO) {
//
//		if(resultJO.containsKey("result_code") && resultJO.getInteger("result_code")==-1){
//			request.response().end(resultJO.toString());
//			return;
//		}
//		JsonObject rs = new JsonObject();
//
//		switch (what) {
//
//		case Config.customQuery:
//			rs.put("result_code", 0);
//			rs.put("result_msg", "success to query");
//			rs.put("result", resultJO.getJsonObject("results"));
//			request.response().end(rs.toString());
//		}
//	}
}