package Client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TestClient {

    public static void main(String arg[]) throws UnsupportedEncodingException {
        RequestUtil request = null;

        //String body = "name=강경완&email=lk@lemonlab.co.6&password=adad&phonenumber=010-0000-7&gcmtoken=fdafsd";
        String body = "{\"user_id\":\"fff\"}";
        request.post("http://127.0.0.1:7010/user/join?user_id=aaaa@aaa&user_pw=" + URLEncoder.encode("마마마마마", "UTF-8"), body, "UTF-8", new Request() {
            @Override
            public void onSuccess(String receiveData) {
                System.out.println(receiveData);
            }

            @Override
            public void onFail(String url, String error) {
                System.out.println(error);
            }
        });
//
//        request.get("http://lemonlab.co.kr/ssu/php/here/hereUser/lk@lemonlab.co.kr", new Request() {
//            @Override
//            public void onSuccess(String receiveData) {
//                System.out.println(receiveData);
//            }
//
//            @Override
//            public void onFail(String url, String error) {
//
//            }
//        });
    }
}