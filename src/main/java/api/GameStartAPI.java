package api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by lk on 16. 6. 5..
 */
public class GameStartAPI extends Base {
    //TODO 세션 시간 설정
    //String token = "";

    public GameStartAPI() {
    }

    // isGame 을 1로 바꿔줘야하고,
    // 턴을 설정해 줘야함.
    // 패를 뿌려줘야 함.

    public void execute(Vertx vertx, HttpServerRequest request) {
        init(vertx, request);

        if (params.isEmpty() || checkValidation(params).getInteger("result_code") == -1) {
            request.response().end(checkValidation(params).toString());
            return;
        }

        String query = String.format("UPDATE channel set is_game = 1 WHERE room_id='%s'",
                params.getString("room_id"));
        updateCustomQuery(0, query);

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

    public void onExecute(int what, JsonObject resultJO) {

        if (resultJO.containsKey("result_code") && resultJO.getInteger("result_code") == -1) {
            request.response().end(resultJO.toString());
            return;
        }

        switch (what) {
            case 0:
                String query3 = String.format("SELECT * FROM room_user_list WHERE room_id='%s'",
                        params.getString("room_id"));
                selectCustomQuery(1, query3);
                break;
            case 1:
                int playerCnt = resultJO.getJsonArray("results").size();
                String data = null;
                Queue<Integer> list = new LinkedList<>();
                boolean[] ischeck = new boolean[54];
                for (int i = 0; i < 54; i++) {
                    ischeck[i] = false;
                }
                Random random = new Random();
                for (int i = 0; i < 54; i++) {
                    int n = random.nextInt(54);
                    if (ischeck[n]) {
                        i--;
                        continue;
                    }
                    ischeck[n] = true;
                    list.add(n);
                }
                for (int i = 0; i < playerCnt; i++) {
                    String id = resultJO.getJsonArray("results").getJsonObject(i).getString("user_id");
                    JsonObject json = new JsonObject();
                    json.put("turn", i);
                    json.put("id", id);
                    for (int k = 0; k < 5; k++) {
                        json.put("card" + k, list.poll());
                    }
                    JsonObject c = new JsonObject();
                    c.put("room_id", params.getString("room_id"));
                    c.put("data", json);
                    vertx.eventBus().send("to.ChatVerticle.gamestart", c, new Handler<AsyncResult<Message<JsonObject>>>() {

                        @Override
                        public void handle(AsyncResult<Message<JsonObject>> res) {
                            System.out.println(getClass().getName() + " onExecute : " + res.result().body().toString());
                        }
                    });
                }
                break;
        }
        JsonObject rs = new JsonObject();

        rs.put("result_code", 0);
        rs.put("result_msg", "게임이 시작되었습니다.");
        request.response().end(rs.toString());


    }

    public JsonObject checkValidation(JsonObject params) {
        JsonObject res = new JsonObject();
        if (!params.containsKey("room_id") || params.getString("room_id").isEmpty() || params.getString("room_id").equals("")) {

            res.put("result_code", -1);
            res.put("result_msg", "룸 이름이 정의되지 않았습니다.");
            return res;
        }
        res.put("result_code", 0);
        return res;
    }


}