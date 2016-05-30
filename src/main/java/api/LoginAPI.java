package api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

public class LoginAPI extends Base {

    public void execute(Vertx vertx, HttpServerRequest request) {
        init(vertx, request);

        if (params.isEmpty() || checkValidation(params).getInteger("result_code") == -1) {
            request.response().end(checkValidation(params).toString());
            return;
        }

        String query = String.format("SELECT * FROM user WHERE user_id='%s' and user_pw='%s' ",
                params.getString("user_id"), params.getString("user_pw"));
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

    public void onExecute(int what, JsonObject resultJO) {

        if (resultJO.containsKey("result_code") && resultJO.getInteger("result_code") == -1) {
            request.response().end(resultJO.toString());
            return;
        }

        JsonObject rs = new JsonObject();

        if (resultJO.getJsonArray("results").size() > 0) {
            rs.put("result_code", 0);
            rs.put("result_msg", "로그인되었습니다.");
            request.response().end(rs.toString());
        } else {
            rs.put("result_code", -1);
            rs.put("result_msg", "로그인 정보가 일치하지 않습니다.");
            request.response().end(rs.toString());
        }


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

        res.put("result_code", 0);
        return res;
    }

}