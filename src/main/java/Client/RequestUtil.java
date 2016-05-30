package Client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by lk on 16. 3. 22..
 */
public class RequestUtil extends Thread{

    Request callback;
    private String url;
    private String body;
    private String type;
    private String encode = "UTF-8";

    private RequestUtil(String url, String parameter, Request callback){
        this.url = url;
        this.callback = callback;
        this.body = parameter;
        this.type = "POST";
    }

    private RequestUtil(String url, Request callback){
        this.url = url;
        this.callback = callback;
        this.type = "GET";
    }

    public static void post(String url, String parameter, Request callback){
        RequestUtil req = new RequestUtil(url, parameter, callback);
        req.start();
    }

    public static void post(String url, String parameter, String encode, Request callback){
        RequestUtil req = new RequestUtil(url, parameter, callback);
        req.setEncode(encode);
        req.start();
    }

    public static void get(String url, Request callback){
        RequestUtil req = new RequestUtil(url, callback);
        req.start();
    }

    public void run(){
        try {
            URL u = new URL(url);

            HttpURLConnection hucon = (HttpURLConnection) u.openConnection();
            hucon.setRequestMethod(type);
            hucon.setDoInput(true);
            hucon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            if(type.equals("POST")) {
                hucon.setDoOutput(true);
                OutputStream os = hucon.getOutputStream();
                os.write(body.getBytes(encode));
                os.flush();
                os.close();
            }else {
                hucon.setDoOutput(false);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(hucon.getInputStream(), encode ));


            String buf;
            String reciveData = "";
            while( ( buf = br.readLine() ) != null ) {
                reciveData += buf;
            }
            callback.onSuccess(reciveData);
            br.close();


        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.onFail(url, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFail(url, e.toString());
        }
    }

    public void setEncode(String encode){
        this.encode = encode;
    }
}