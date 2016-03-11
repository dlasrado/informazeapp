package util;

import java.net.UnknownHostException;




import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;



public class MongoConnect {

	private MongoClient                      mongoClient = null;
    private MongoDatabase db = null;
    
    private final static String DB_NAME = "appdb";
	
    public MongoConnect() throws UnknownHostException {
        if (mongoClient == null) {
        	mongoClient = getClient("localhost", "27017"); 
        }
    }
	private MongoClient getClient(String host, String port)
			throws UnknownHostException {
		return new MongoClient(host, Integer.parseInt(port));

	}
	public MongoDatabase getDB() {
		if (db == null) {
			db = mongoClient.getDatabase(DB_NAME);
		}
		return db;
	}
}
