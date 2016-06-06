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

        onExecute(-2, checkValidation(params));
    }

    public void onExecute(int what, JsonObject resultJO) {

        if (resultJO.containsKey("result_code") && resultJO.getInteger("result_code") == -1) {
            request.response().end(resultJO.toString());
            return;
        }
        JsonObject rs = new JsonObject();

        switch (what) {
            case -1:
                if(resultJO.getJsonArray("results").size() < 1){
                    rs.put("result_code" , -1);
                    rs.put("result_msg", "없는 방입니다.");
                    request.response().end(rs.toString());
                }
                int limit = resultJO.getJsonArray("results").getJsonObject(0).getInteger("room_limit");
                params.put("limit", limit);
                String query3 = String.format("SELECT * FROM room_user_list WHERE room_id='%s'",
                        params.getString("room_id"));
                selectCustomQuery(-3, query3);
                break;
            case -3:
                if(resultJO.getJsonArray("results").size() >= params.getInteger("limit")){
                    rs.put("result_code" , -1);
                    rs.put("result_msg", "풀방입니다.");
                    request.response().end(rs.toString());
                    break;
                }
                params.put("cnt", resultJO.getJsonArray("results").size());
                onExecute(0, params);
                break;

            case -2:
                String query4 = String.format("SELECT * FROM channel WHERE room_id='%s'",
                        params.getString("room_id"));
                selectCustomQuery(-1, query4);
                break;
            case 0:

                if (params.containsKey("room_pw")) {
                    String query = String.format("SELECT * FROM channel WHERE room_id='%s' and room_pw='%s'",
                            params.getString("room_id"), params.getString("room_pw"));
                    selectCustomQuery(1, query);
                } else {
                    String query = String.format("INSERT INTO room_user_list SET user_id='%s', room_id='%s', user_nick='%s'",
                            params.getString("user_id"), params.getString("room_id"), params.getString("user_nick"));
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

                String query2 = String.format("INSERT INTO room_user_list SET user_id='%s', room_id='%s', user_nick='%s'",
                        params.getString("user_id"), params.getString("room_id"),  params.getString("user_nick"));
                insertCustomQuery(2, query2);
                break;

            case 2:

                String query5 = String.format("UPDATE channel set user_cnt = '%s' WHERE room_id='%s'",
                        ""+(params.getInteger("cnt") + 1),params.getString("room_id"));
                System.out.println((params));
                updateCustomQuery(3, query5);
                break;

            case 3:
                String query10 = String.format("SELECT * FROM room_user_list WHERE room_id='%s'",
                        params.getString("room_id"));
                selectCustomQuery(4, query10);
                break;

            case 4:
                rs.put("user_list",resultJO.getJsonArray("results"));
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

    public void updateCustomQuery(int what, String query) {
        System.out.println(what + " selectQuery execute");

        vertx.eventBus().send("to.DBVerticle.updateCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

            @Override
            public void handle(AsyncResult<Message<JsonObject>> res) {
                onExecute(what, res.result().body());
                System.out.println(getClass().getName() + " onExecute : " + res.result().body().toString());

            }
        });
    }

    public JsonObject checkValidation(JsonObject params) {
        JsonObject res = new JsonObject();
        if (!params.containsKey("room_id") || params.getString("room_id").isEmpty() || params.getString("room_id").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "방을 선택해주세요.");
            return res;
        }
        if (!params.containsKey("user_nick") || params.getString("user_nick").isEmpty() || params.getString("user_nick").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "유저 닉네임을 입력해주세요.");
            return res;
        }
        if (!params.containsKey("user_id") || params.getString("user_id").isEmpty() || params.getString("user_id").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "유저 아이디를 입력해주세요.");
            return res;
        }

        res.put("result_code", 0);
        return res;
    }
}
