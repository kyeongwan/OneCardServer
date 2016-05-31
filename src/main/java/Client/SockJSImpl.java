package Client;

import com.oracle.javafx.jmx.json.JSONException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SockJSImpl extends WebSocketClient {

    private Map<String, String> openHandShakeFields;
    private final static String dictionary = "abcdefghijklmnopqrstuvwxyz0123456789_";
    private String roomname;
    private Timer timer;
    private String nickname = "";
    private String title;

    public SockJSImpl(String serverURI, String roomname, String nickname, String title) throws URISyntaxException {
        super(new URI(generatePrimusUrl(serverURI)), new Draft_17());
        System.out.println("Test");
        this.openHandShakeFields = new HashMap<>();
        this.roomname = roomname;
        this.nickname = nickname;
        this.title = title;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Open");
        Iterator<String> it = handshakedata.iterateHttpFields();
        while (it.hasNext()) {
            String key = it.next();
            openHandShakeFields.put(key, handshakedata.getFieldValue(key));
        }

        scheduleHeartbeat();
        registAddress("to.channel." + roomname);
    }

    @Override
    public void onMessage(String s) {
        JsonObject response;
        if (s.charAt(0) == 'o' || s.charAt(0) == 'h') {
            // ignore
        } else if (s.charAt(0) == 'a') {
            parseSockJS(s);
        } else {
            System.out.println("onMessage " + s);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println(ex.toString());
    }

    public void parseSockJS(String s) {
        try {

            s = s.replace("\\\"", "\"");
            s = s.replace("\\\\\"", "\"");
            s = s.substring(3, s.length() - 2); // a[" ~ "] 없애기

            JsonObject json = new JsonObject(s);
            String type = json.getString("type");
            String address = json.getString("address");
            String body = json.getString("body");

            if ("to.channel.channel_id".equals(address))
                System.out.printf("%s, %s, %s\n", type, address, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * JSON을 websocket 전송용 문자열로 변환하여 전
     *
     * @param json
     */
    public void send(JsonObject json) {
        String str = json.toString();
        str = str.replaceAll("\"", "\\\\\"");
        str = "[\"" + str + "\"]";
        send(str);
    }

    void registAddress(String address) {
        JsonObject obj = new JsonObject();
        try {
            obj.put("type", "register");
            obj.put("address", address);
            System.out.println("RegistAddress" + address);
            send(obj);
            joinLogSend();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void joinLogSend() {
        JsonObject log = new JsonObject();
        try {
            log.put("type", "publish");
            log.put("address", "to.server.channel");
            JsonObject body = new JsonObject();
            body.put("type", "log");
            body.put("channel_id", roomname);
            body.put("sender_id", "aaa");
            body.put("sender_nick", nickname);
            body.put("msg", "님이 " + title + "방에 입장하셨습니다.");
            log.put("body", body);
            send(log);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 9초마다 Heartbeat. 10초 이내로 보내야 하기 때문에 9초 설정.
     */
    void scheduleHeartbeat() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String ping = "[\"{\\\"type\\\":\\\"ping\\\"}\"]";
                send(ping);

                scheduleHeartbeat();
            }
        }, 9000);

    }

    private static char randomCharacterFromDictionary() {
        int rand = (int) (Math.random() * dictionary.length());
        return dictionary.charAt(rand);
    }


    private static String randomStringOfLength(int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            s.append(randomCharacterFromDictionary());
        }
        return s.toString();
    }

    private static String generatePrimusUrl(String baseUrl) {
        Random r = new Random();
        int server = r.nextInt(1000);
        String connId = randomStringOfLength(8);
        return baseUrl + "/" + server + "/" + connId + "/websocket";
    }

    public void closeSession() {
        timer.cancel();
    }
}