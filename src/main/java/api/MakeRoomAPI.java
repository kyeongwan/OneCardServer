package api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class MakeRoomAPI extends Base {
    String channel_id = "";

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
                channel_id = UUID.randomUUID().toString();

                String query = "";
                if ("off".equals(params.getString("is_private")))
                    query = String.format("INSERT INTO channel SET room_id='%s', room_name='%s', is_private='%s', "
                                    + "room_limit='%s'",
                            channel_id,  params.getString("room_name"),
                            params.getString("is_private"), params.getString("room_limit"));
                else if ("on".equals(params.getString("is_private")))
                    query = String.format("INSERT INTO channel SET room_id='%s', room_name='%s', is_private='%s', "
                                    + "room_pw='%s', room_limit='%s'",
                            channel_id,  params.getString("room_name"),
                            params.getString("is_private"), params.getString("room_pw"), params.getString("room_limit"));

                insertCustomQuery(1, query);
                // 방 정보를 디비에 넣음
                break;

            case 1:
                setPermission(2, channel_id);
                break;

            case 2:
                String query2 = String.format("INSERT INTO room_user_list SET user_id='%s', room_id='%s', user_nick='%s'",
                        params.getString("user_id"), channel_id, params.getString("user_nick"));
                // 유저를 방에 넣음.
                insertCustomQuery(3, query2);
                break;
            case 3:
                rs.put("result_code", 0);
                rs.put("result_msg", "채널이 생성되었습니다.");
                rs.put("channel_id", channel_id);
                request.response().end(rs.toString());
                break;

        }

    }

    public JsonObject checkValidation(JsonObject params) {
        JsonObject res = new JsonObject();


        if (!params.containsKey("room_name") || params.getString("room_name").isEmpty() || params.getString("room_name").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "방 이름을 정확히 입력해주세요.");
            return res;
        }
        if (!params.containsKey("is_private") || params.getString("is_private").isEmpty() || params.getString("is_private").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "비밀방 여부를 선택해주세요.");
            return res;
        }

        if(params.getString("is_private") == "on"){
            if (!params.containsKey("room_pw") || params.getString("room_pw").isEmpty() || params.getString("room_pw").equals("")) {
                res.put("result_code", -1);
                res.put("result_msg", "비밀번호를 입력해주세요.");
                return res;
            }
        }
        if (!params.containsKey("room_limit") || params.getString("room_limit").isEmpty() || params.getString("room_limit").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "방 입장 제한 수를 설정해주세요.");
            return res;
        }

        res.put("result_code", 0);
        return res;
    }

    public void setPermission(int what, String channel_id) {
        System.out.println(" Permission set execute");

        vertx.eventBus().send("to.ChatVerticle.permit", channel_id, new Handler<AsyncResult<Message<JsonObject>>>() {

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
}