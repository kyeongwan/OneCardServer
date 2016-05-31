package Client;

import com.oracle.javafx.jmx.json.JSONException;
import io.vertx.core.json.JsonObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lk on 16. 6. 1..
 */
public class ChatTest {

    SockJSImpl sockJS;
    String channel_id = "d93be8d5-7023-4ae8-bca5-751ab2788bca";
    String nickname = "fsklasdf";
    String title = "fdasfsad";

    public ChatTest(){
        connectSockJS();
    }

    public static void main(String arg[]){
        new ChatTest();

    }

    private void connectSockJS() {
        try {
            sockJS = new SockJSImpl("http://133.130.115.228:7030" + "/eventbus", channel_id, nickname, title) {
                //channel_
                @Override
                public void parseSockJS(String s) {
                    try {
                        //System.out.println(s);
                        s = s.replace("\\\"", "\"");
                        s = s.replace("\\\\", "\\");
//                        s = s.replace("\\\\\"", "\"");
                        s = s.substring(3, s.length() - 2); // a[" ~ "] 없애기
                        System.out.println("Reci : " +  s);

                        JsonObject json = new JsonObject(s);
                        String type = json.getString("type");
                        String address = json.getString("address");
//                        final JSONObject body = json.getJSONObject("body");
                        final JsonObject body = new JsonObject(json.getString("body"));
                        String bodyType = body.getString("type");
                        String msg = body.getString("msg");
                        String nickname = body.getString("sender_nick");
                        Date myDate = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd. HH:mm");
                        String date = sdf.format(myDate);
                        final String data =  bodyType + "/&" +nickname + "/&" + msg + "/&" + date;
                        if (("to.channel."+channel_id).equals(address))
                            System.out.println("Chat : " + data);

                        System.out.println("body = " + body);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            boolean b = sockJS.connectBlocking();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
