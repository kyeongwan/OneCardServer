import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.Set;

public class DBVerticle extends AbstractVerticle {

    JDBCClient client;

    private void init() {
    	JsonObject config = new JsonObject()
        .put("url", "jdbc:mysql://localhost:3306/oneCard?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull")
        .put("user", "root")
        .put("password", "sun54321")
        .put("max_pool_size", 30);
		client = JDBCClient.createShared(vertx, config);
    }

    void insertQuery(Message<String> msg) {
    	IQuery cb = new IQuery(client, msg) {

            @Override
            public void success(SQLConnection con, Object result) {
            	JsonObject jo = new JsonObject();
                if(result == null) {
                	jo.put("result_code", -1);
                	jo.put("result_msg", "insert fail");
                    reply(jo);
                }
                else {
                    int count = (Integer)result;
                    jo.put("result_code", 0);
                    jo.put("result_msg", "insert success");
                    jo.put("count", count);
                    reply(jo);
                }
            }
        };

    	 String str = msg.body();
         JsonObject json = new JsonObject(str);
         Set<String> names = json.fieldNames();
         StringBuilder sbNames = new StringBuilder();
         StringBuilder sbValues = new StringBuilder();
         for(String field : names) {
             if(field.equals("table_name")||field.equals("question")||field.equals("token"))
                 continue;

             sbNames.append(field);
             sbNames.append(",");

             if(field.indexOf("date")>-1){
            	 sbValues.append(json.getString(field));
            	 sbValues.append(",");
             }else{
	             sbValues.append("'");
	             sbValues.append(json.getString(field));
	             sbValues.append("',");
             }
         }
         sbNames.deleteCharAt(sbNames.length()-1);
         sbValues.deleteCharAt(sbValues.length()-1);

         String query = String.format(json.getString("question")+" INTO %s (%s) VALUES(%s)", json.getString("table_name"), sbNames, sbValues);
         cb.execute(query);
        
    }
    void insertCustomQuery(Message<String> msg) {
    	IQuery cb = new IQuery(client, msg) {

            @Override
            public void success(SQLConnection con, Object result) {
            	JsonObject jo = new JsonObject();
                if(result == null) {
                	jo.put("result_code", -1);
                	jo.put("result_msg", "insert fail");
                    reply(jo);
                }
                else {
                	int count = (Integer)result;
                	
                    jo.put("result_code", 0);
                    jo.put("result_msg", "insert success");
                    jo.put("count", count);
                    reply(jo);
                }
            }
        };

    	 String str = msg.body();
        	
         
         cb.execute(str);
    }

    void selectCustomQuery(Message<String> msg) {
        IQuery cb = new IQuery(client, msg) {
            @Override
            public void success(SQLConnection con, Object result) {
                ResultSet rs = (ResultSet)result;
                
                JsonArray jsonArray =new JsonArray();
        		
        		for(int i = 0; i<rs.getNumRows(); i++){
        			JsonObject jsonObject = new JsonObject();
        			JsonArray ja = rs.getResults().get(i);
        			
        			int j = 0;
        			for(String column : rs.getColumnNames()){
        				if (ja.getValue(j) instanceof String) {
        					jsonObject.put(column, ja.getString(j) );
        				}else {
        					jsonObject.put(column, ja.getLong(j) );
        				}
        				j++;
        			}
        			jsonArray.add(jsonObject);
        		}
        	
        		JsonObject response = new JsonObject();
        		response.put("results", jsonArray);
                response.put("result_code", 0);
                response.put("result_msg", "select success");
                reply(response);
            }
        };

        String str = msg.body();
        cb.execute(str);
    }
   
    void selectCustomQuery2(Message<String> msg) {
        IQuery cb = new IQuery(client, msg) {
            @Override
            public void success(SQLConnection con, Object result) {
                ResultSet rs = (ResultSet)result;
                
                JsonObject response = new JsonObject();
        		response.put("results",  rs.toJson() );
                reply(response);
            }
        };

        String str = msg.body();
        cb.execute(str);
    }
    
    void selectQuery(Message<String> msg) {
        IQuery cb = new IQuery(client, msg) {
            @Override
            public void success(SQLConnection con, Object result) {
                ResultSet rs = (ResultSet)result;
                
                JsonArray jsonArray =new JsonArray();
        		
        		for(int i = 0; i<rs.getNumRows(); i++){
        			JsonObject jsonObject = new JsonObject();
        			JsonArray ja = rs.getResults().get(i);
        			
        			int j = 0;
        			for(String column : rs.getColumnNames()){
        				if (ja.getValue(j) instanceof String) {
        					jsonObject.put(column, ja.getString(j) );
        				}else {
        					jsonObject.put(column, ja.getLong(j) );
        				}
        				j++;
        			}
        			jsonArray.add(jsonObject);
        		}
        	
        		JsonObject response = new JsonObject();
        		response.put("results", jsonArray);
                response.put("result_code", 0);
                response.put("result_msg", "select success");
                reply(response);
            }
        };

        String str = msg.body();
        if(str.indexOf("{") == -1) {
            cb.execute(str);
            return;
        }

        JsonObject json = new JsonObject(str);
        String table = json.getString("table_name");
//        JsonArray keys = json.getJsonArray("keys");
//        JsonArray values = json.getJsonArray("values");

        Set<String> names = json.fieldNames();
        StringBuilder sb = new StringBuilder();
        for(String field : names) {
            if("table_name".equals(field))
                continue;

            if(sb.length() > 0)
                sb.append(" AND ");
            sb.append(field);
            sb.append("=");
            if(field.indexOf("date")>-1)
            	sb.append(json.getString(field));
            else
            	sb.append("'"+json.getString(field)+"'");
        }
        String whereStr = null;
        if(sb.length() > 0)
            whereStr = "WHERE "+sb.toString();

        String query = String.format("SELECT * FROM %s %s", table, whereStr);
        cb.execute(query);
    }


    @Override
    public void start() throws Exception {
        init();

        EventBus eb = vertx.eventBus();
        eb.consumer("to.DBVerticle.selectQuery", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> objectMessage) {
            	selectQuery(objectMessage);
            }
        });

        eb.consumer("to.DBVerticle.insertQuery", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> objectMessage) {
                insertQuery(objectMessage);
            }
        });
        
        eb.consumer("to.DBVerticle.insertCustomQuery", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> objectMessage) {
                insertCustomQuery(objectMessage);
            }
        });

        eb.consumer("to.DBVerticle.selectCustomQuery", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> objectMessage) {
                selectCustomQuery(objectMessage);
            }
        });
        
        eb.consumer("to.DBVerticle.selectCustomQuery2", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> objectMessage) {
                selectCustomQuery2(objectMessage);
            }
        });
    }

    @Override
    public void stop() {
        System.out.println("+++ DB Stop!!!");
    }

}