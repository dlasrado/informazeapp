/**
 * 
 */
package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Configuration;
import play.Logger;
import play.Play;
import play.api.libs.Crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient.AccessToken;

/**
 * A general Utility class
 * 
 * @author dlasrado
 *
 */
public class Utility {
	
	private static AccessToken appAccessToken = null;
	private static Configuration config = Play.application().configuration();
	
	private static SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy");
	private static SimpleDateFormat datetimeformat = new SimpleDateFormat("dd MMM yyyy hh:mm");
	
	public static Map<String, String> fieldSlist = new HashMap<String, String>();
	static {
		fieldSlist.put(AppConstants.PHOTO , "id,name,album,images,picture,likes,comments,sharedposts,tags,event,link");
		fieldSlist.put(AppConstants.MESSAGE , "id,message,likes,comments,shares,type");
		fieldSlist.put(AppConstants.VIDEO , "id,content_category,description,event,embed_html,icon,picture,published,source,title,likes,comments,sharedposts,captions,tags,permalink_url");
	}

	/**
	 * Returns the access token object for the applicatioin.
	 * The appId and Secret is fetched from the config.
	 * @return
	 */
	public static AccessToken getAppAccessToken() {
    	
    	//If access token expires soon, get new token
    	if(appAccessToken == null || 
    			(appAccessToken.getExpires() != null 
    				&& (new Date(System.currentTimeMillis()-5000)).before(appAccessToken.getExpires()))) {
   	
	    	appAccessToken =
	  			  new DefaultFacebookClient().obtainAppAccessToken(config.getString("fb.app.id"), 
	  					  Crypto.decryptAES(config.getString("fb.app.secret")));
    	}
    	
    	return appAccessToken;
    	
    }
	
	/**
	 * Returns a generic error json based on the message parameter
	 * @param dateString
	 * @return
	 */
	public static JSONObject getErrorJson(String message) {
		
		JSONObject result = new JSONObject();
        
        try {
			result.put("error", message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.error("Could not create a error JSON");
			throw new RuntimeException(e);
		}
        return result;
	}
	
	/**
	 * Converts the date string in dd MMM yyyy to a unixtime
	 * @param dateString
	 * @return
	 * @throws ParseException
	 */
	public static long getUnixTimeFromDateString(String dateString) throws ParseException {
		
		return dateformat.parse(dateString).getTime() / 1000L;
	}
	
	/**
	 * Converts the datetime string in dd MMM yyyy hh:mm to a unixtime
	 * @param dateString
	 * @return
	 * @throws ParseException
	 */
	public static long getUnixTimeFromDatetimeString(String dateString ) throws ParseException {
		
		return datetimeformat.parse(dateString).getTime() / 1000L;
	}
	
	/**
	 * Returns a json array conversion of the MongoDB result.
	 * @param result
	 * @return
	 */
	public static JSONArray getJsonArray(FindIterable<Document> result) {
		
		final JSONArray jsonArray = new JSONArray();
		result.forEach(new Block<Document>() {
		    @Override
		    public void apply(final Document document) {
		    	jsonArray.put(document.toJson());
		    }
		});
		return jsonArray;
	}
	
	/**
	 * Fetch the possible comma separated field list string give the post type 
	 * @param postType
	 * @return String : field list string
	 */
    public static String getFetchFieldList(String postType) {
    	
    	String fieldList =  fieldSlist.get(postType);
    	if (fieldList == null) {
    		fieldList = fieldSlist.get(AppConstants.MESSAGE);
    	}
    	return fieldList;
    }
}
