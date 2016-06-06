package api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ListRoomAPI extends Base {

    JsonArray ja;
    JsonArray list;

    public ListRoomAPI() {
    }


    public void execute(Vertx vertx, HttpServerRequest request) {
        init(vertx, request);

        onExecute(1, params);
    }


    public void onExecute(int what, JsonObject resultJO) {

        if (resultJO.containsKey("result_code") && resultJO.getInteger("result_code") == -1) {
            request.response().end(resultJO.toString());
            return;
        }

        JsonObject rs = new JsonObject();

        switch (what) {
            case 1:
                String query2;
                if(params.containsKey("room_name")){
                    query2 = String.format("SELECT * FROM channel WHERE is_game = 0 and room_name = '" + params.getString("room_name")+"'");
                }else{
                    query2 = String.format("SELECT * FROM channel WHERE is_game = 0");
                }
                selectCustomQuery(2, query2);


                break;
            case 2:

                ja = resultJO.getJsonArray("results");
                System.out.println(ja.toString());
                for (int i = 0; i < ja.size(); i++) {
                    //ja.getJsonObject(i).put("room_name", (resultJO.getJsonArray("results").getJsonObject(i).getString("room_name")));
                    //ja.getJsonObject(i).put("room_id", (resultJO.getJsonArray("results").getJsonObject(i).getString("room_id")));
                    //ja.getJsonObject(i).put("is_private", (resultJO.getJsonArray("results").getJsonObject(i).getString("is_private")));
                }

                JsonArray js = new JsonArray();
                int start_index;
                int end_index;
                if(!params.containsKey("start_index")){
                    start_index = 0;
                }else{
                    start_index = params.getInteger("start_index");
                }
                if(!params.containsKey("end_index")){
                    end_index = 6;
                }else {
                    end_index = params.getInteger("end_index");
                }

                if(ja.size() < start_index){
                    JsonObject res = new JsonObject();
                    res.put("result_code", -1);
                    res.put("result_msg", "맨 끝입니다.");
                    request.response().end(res.toString());
                    break;
                }

                if(ja.size() < end_index + 1){
                    end_index = ja.size() - 1;
                }

                for(int i=0; i< end_index - start_index; i++){
                    js.add(ja.getJsonObject(start_index + i));
                }

                rs.put("result_code", 0);
                rs.put("result_msg", "채널 리스트 입니다.");
                rs.put("list_channel", ja);
                request.response().end(rs.toString());
                break;
            case 3:
                for (int i = 0; i < ja.size(); i++) {
                    ja.getJsonObject(i).put("channel_name", resultJO.getJsonArray("results").getJsonObject(i).getString("channel_name"));
                    ja.getJsonObject(i).put("channel_cate", resultJO.getJsonArray("results").getJsonObject(i).getString("channel_cate"));
                }

                rs.put("result_code", 0);
                rs.put("result_msg", "채널 리스트 입니다.");
                rs.put("list_channel", ja);
                request.response().end(rs.toString());

                break;
        }
    }


    public void selectCustomQuery(int what,  String query){
        System.out.println(what+" selectQuery execute");

        vertx.eventBus().send("to.DBVerticle.selectCustomQuery", query, new Handler<AsyncResult<Message<JsonObject>>>() {

            @Override
            public void handle(AsyncResult<Message<JsonObject>> res) {
                onExecute(what, res.result().body());
                System.out.println(getClass().getName() + " onExecute : " + res.result().body().toString());

            }
        });
    }


}