/**
 * 
 */
package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.Configuration;
import play.Play;
import play.api.libs.Crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient.AccessToken;

/**
 * @author dlasrado
 *
 */
public class Utility {
	
	private static AccessToken appAccessToken = null;
	private static Configuration config = Play.application().configuration();
	
	private static SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy");
	private static SimpleDateFormat datetimeformat = new SimpleDateFormat("dd MMM yyyy hh:mm");

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
	
	public static JsonNode getErrorJson(String message) {
		
		ObjectNode result = new JsonNodeFactory(true).objectNode();
        
        result.put("error", message);
        return result;
	}
	
	public static long getUnixTimeFromDateString(String dateString) throws ParseException {
		
		return dateformat.parse(dateString).getTime() / 1000L;
	}
	
	public static long getUnixTimeFromDatetimeString(String dateString ) throws ParseException {
		
		return datetimeformat.parse(dateString).getTime() / 1000L;
	}
    
}
