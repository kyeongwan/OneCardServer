package api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

/**
 * Created by lk on 16. 6. 5..
 */
public class JoinRoomAPI extends Base {


    public void execute(Vertx vertx, HttpServerRequest request) {
        init(vertx, request);

        if (params.isEmpty() || checkValidation(params).getInteger("result_code") == -1) {
            request.response().end(checkValidation(params).toString());
            return;
        }

        onExecute(0, checkValidation(params));
    }

    public void onExecute(int what, JsonObject resultJO) {

        if (resultJO.containsKey("result_code") && resultJO.getInteger("result_code") == -1) {
            request.response().end(resultJO.toString());
            return;
        }
        JsonObject rs = new JsonObject();

        switch (what) {
            case 0:

                if (params.containsKey("channel_pw")) {
                    String query = String.format("SELECT * FROM channel WHERE room_id='%s' and room_pw='%s'",
                            params.getString("room_id"), params.getString("room_pw"));
                    selectCustomQuery(1, query);
                } else {
                    String query = String.format("INSERT INTO room_user_list SET user_id='%s', channel_id='%s', user_nick='%s'",
                            params.getString("user_id"), params.getString("channel_id"), params.getString("user_nick"));
                    insertCustomQuery(2, query);
                }

                break;

            case 1:
                if (resultJO.getJsonArray("results").size() < 1) {
                    rs.put("result_code", -1);
                    rs.put("result_msg", "채널 비밀번호가 올바르지 않습니다.");
                    request.response().end(rs.toString());
                    break;
                }

                String query2 = String.format("INSERT INTO channel_user_list SET user_id='%s', channel_id='%s', app_id='%s', user_nick='%s'",
                        params.getString("user_id"), params.getString("channel_id"), params.getString("app_id"), params.getString("user_nick"));
                insertCustomQuery(2, query2);
                break;

            case 2:

//		 	JsonObject user_info = new JsonObject();
//		 	user_info.put("user_id", params.getString("user_id"));
//		 	user_info.put("gcm_id", params.getString("gcm_id"));
//		 	user_info.put("user_nick", params.getString("user_nick"));
//		 	user_info.put("user_color", "#FFE400");
//
//			JsonObject jo2 = new JsonObject();
//			jo2.put("key", "users:"+params.getString("channel_id"));
//			jo2.put("value", params.getString("user_id")+","+params.getString("gcm_id")+","+params.getString("user_nick")+",#FFE400");
//			saddRedis(this, Config.saddRedis, jo2);
//
//			JsonObject table2 = new JsonObject();
//		 	table2.put("key","ch:"+user_id+":"+channel_id);
//		 	table2.put("value",user_info);
//		 	setRedis(this, Config.setRedis, table2);
//
                rs.put("result_code", 0);
                rs.put("result_msg", "채널에 입장하였습니다.");

                request.response().end(rs.toString());
                break;
        }
    }


    public void selectCustomQuery(int what, String query) {
        System.out.println(what + " selectQuery execute");

        vertx.eventBus().send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

            @Override
            public void handle(AsyncResult<Message<JsonObject>> res) {
                onExecute(what, res.result().body());
                System.out.println(getClass().getName() + " onExecute : " + res.result().body().toString());

            }
        });
    }

    public void insertCustomQuery(int what, String query) {
        System.out.println(what + " insertQuery execute");

        vertx.eventBus().send("to.DBVerticle.insertCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

            @Override
            public void handle(AsyncResult<Message<JsonObject>> res) {
                onExecute(what, res.result().body());
                System.out.println(getClass().getName() + " onExecute : " + res.result().body().toString());

            }
        });
    }

    public JsonObject checkValidation(JsonObject params) {
        JsonObject res = new JsonObject();
        if (!params.containsKey("channel_id") || params.getString("channel_id").isEmpty() || params.getString("channel_id").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "채널을 선택해주세요.");
            return res;
        }
        if (!params.containsKey("user_nick") || params.getString("user_nick").isEmpty() || params.getString("user_nick").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "유저 닉네임을 입력해주세요.");
            return res;
        }

        res.put("result_code", 0);
        return res;
    }
}
