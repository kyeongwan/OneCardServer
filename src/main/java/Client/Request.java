package Client;

public interface Request {
    void onSuccess(String receiveData);
    void onFail(String url, String error);
}