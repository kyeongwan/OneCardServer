package api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JoinAPI {
    //TODO 세션 시간 설정
    //String token = "";

    public JoinAPI() {
    }

    Vertx vertx;
    HttpServerRequest request;
    JsonObject params;

    public void init(Vertx vertx, HttpServerRequest request) {
        this.vertx = vertx;
        this.request = request;
        MultiMap param = request.params();
        params = new JsonObject();
        param.forEach(entry -> params.put(entry.getKey(), entry.getValue()));

    }

    public void execute(Vertx vertx, HttpServerRequest request) {
        init(vertx, request);

        if (params.isEmpty() || checkValidation(params).getInteger("result_code") == -1) {
            request.response().end(checkValidation(params).toString());
            return;
        }

        JsonObject table = new JsonObject();
        String query = String.format("SELECT * FROM user WHERE user_id='%s'", params.getString("user_id"));

        selectCustomQuery(0, query);
    }

    public void selectCustomQuery(int what,  String query){
        System.out.println(what+" selectQuery execute");

        vertx.eventBus().send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

            @Override
            public void handle(AsyncResult<Message<JsonObject>> res) {
                onExecute(what,  res.result().body());
                System.out.println(getClass().getName() + " onExecute : " + res.result().body().toString() );

            }
        });
    }


    public JsonObject checkValidation(JsonObject params) {
        JsonObject res = new JsonObject();
        if (!params.containsKey("user_id") || params.getString("user_id").isEmpty() ||
                params.getString("user_id").equals("") || params.getString("user_id").indexOf("@") < 0) {

            res.put("result_code", -1);
            res.put("result_msg", "이메일 아이디를 정확히 입력하세요.");
            return res;
        }
        if (!params.containsKey("user_pw") || params.getString("user_pw").isEmpty() || params.getString("user_pw").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "비밀번호를 정확히 입력하세요.");
            return res;
        }
        if (!params.containsKey("gcm_id") || params.getString("gcm_id").isEmpty() || params.getString("gcm_id").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "gcm id가 전달되지 않았습니다.");
            return res;
        }
        if (!params.containsKey("device_id") || params.getString("device_id").isEmpty() || params.getString("device_id").equals("")) {
            res.put("result_code", -1);
            res.put("result_msg", "device id가 전달되지 않았습니다.");
            return res;
        }
        res.put("result_code", 0);
        return res;
    }


    public void insertCustomQuery(int what,  String query){
        System.out.println(what+" insertQuery execute");

        vertx.eventBus().send("to.DBVerticle.insertCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

            @Override
            public void handle(AsyncResult<Message<JsonObject>> res) {
                onExecute(what, res.result().body());
                System.out.println(getClass().getName() + " onExecute : " + res.result().body().toString());

            }
        });
    }

    public void onExecute(int what, JsonObject resultJO) {
        if (resultJO.containsKey("result_code") && resultJO.getInteger("result_code") == -1) {
            request.response().end(resultJO.toString());
            return;
        }

        JsonObject rs = new JsonObject();

        switch (what) {
            case 0:
                JsonArray ja = resultJO.getJsonArray("results");

                if (ja.size() < 1) {
                    String query = String.format("INSERT INTO user SET user_id='%s', user_pw='%s', device_id='%s', gcm_id='%s', join_date=%s",
                            params.getString("user_id"), params.getString("user_pw"), params.getString("device_id"), params.getString("gcm_id"), "now()");

                    insertCustomQuery(1, query);
                } else {
                    rs.put("result_code", -1);
                    rs.put("result_msg", "이미 존재하는 아이디입니다.");

                    request.response().end(rs.toString());
                }
                break;
//
//            case :
//
////                token = Util.getToken(params.getString("user_id"));
//                JsonObject table = new JsonObject();
////                table.put("key", token);
//                table.put("value", params.getString("user_id") + "," + params.getString("gcm_id") + "," + System.currentTimeMillis() + "," + request.localAddress());
////                setRedis(this, Config.setSession, table);
////                Util.getCache().put(params.getString("user_id"), token);
//                break;

            case 1:

                rs.put("result_code", 0);
                rs.put("result_msg", "성공적으로 가입하였습니다.");
                //rs.put("token", token);

                request.response().end(rs.toString());
                break;

            default:
                break;
        }
    }
}