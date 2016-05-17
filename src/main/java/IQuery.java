import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

public abstract class IQuery {
    JDBCClient client;
    Message<String> retMessage;
    boolean autoConClose = true;

    public IQuery(JDBCClient client, Message<String> retMessage) {
        this.client = client;
        this.retMessage = retMessage;
    }

    public IQuery(JDBCClient client, Message<String> retMessage, boolean autoConClose) {
        this.client = client;
        this.retMessage = retMessage;
        this.autoConClose = autoConClose;
    }

    public abstract void success(SQLConnection con, Object result);

    void fail(SQLConnection con, String msg){
    	JsonObject jo = new JsonObject();
    	
        jo.put("result_code", -1);
        jo.put("result_msg", msg);
    	reply(jo);
        
        System.out.println("Query Failed: "+msg);
    };

    public void reply(JsonObject msg) {
        if(retMessage == null) return;
        retMessage.reply(msg);
    }

    private void close(SQLConnection con) {
        if(con == null) return;

        con.close(done -> {
            if (done.failed()) {
                throw new RuntimeException(done.cause());
            }
        });
    }

    public void execute(String query) {
        client.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection con = res.result();
                if(query.indexOf("select")>-1||query.indexOf("SELECT")>-1){
                	con.query(query, res2 -> {
                        if (res2.succeeded()) {
                            ResultSet rs = res2.result();
                            success(con, rs);
                        }
                        else {
                            fail(con, "Failed to get ResultSet "+res2.cause().getMessage());
                            System.out.println(query);
                        }
                        if(autoConClose)
                            close(con);
                    });
                }
                else {
                    con.update(query, res2 -> {
                        if (res2.succeeded()) {
                            int count = res2.result().getUpdated();
                            success(con, count);
                        }
                        else {
                            fail(con, "Failed to update "+res2.cause().getMessage());
                            System.out.println(query);
                        }
                        if(autoConClose)
                            close(con);
                    });
                }
            } else {
                // Failed to get connection - deal with it
                fail(null, "Failed to get Connection "+res.cause().getMessage());
            }
        });
    }
}