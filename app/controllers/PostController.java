/**
 * 
 */
package controllers;

import java.io.FileInputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.BsonValueCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.FacebookPosts;
import play.Configuration;
import play.Logger;
import play.Play;
import play.api.i18n.Lang;
import play.api.libs.json.JsArray;
import play.libs.Crypto;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import util.AppConstants;
import util.MongoConnect;
import util.Utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.Block;
import com.mongodb.QueryBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.restfb.BinaryAttachment;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.Page;
import com.restfb.types.Post;
import com.restfb.util.StringUtils;

/**
 * @author dlasrado
 *
 */
public class PostController extends Application {

	private static Configuration config = Play.application().configuration();
	private static final int    TIMEOUT_IN_MILLIS = 60000;
	private static Lang lang = new Lang("en", "us");
	
    private static SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy");
	private static List<String>  postTypes = java.util.Arrays.asList(new String[]{"feed","event","photo"});	
	
	@Security.Authenticated(Secured.class)
	public static Result postPage() {
		JsonNode json = play.libs.Json.parse("{\"message\":\"New\"}");
		return ok(views.html.post.render(json));
	}
	
	@Security.Authenticated(Secured.class)
	@BodyParser.Of(BodyParser.Json.class)
    public static Result post() {

		JsonNode jsonBody = request().body().asJson();
		Logger.debug(""+jsonBody);
		Logger.debug(jsonBody.get("postType")+"");
		String postType = jsonBody.get("postType").asText();
		Logger.debug("Posting a "+ postType);
		if(!postTypes.contains(postType)) {
			
			return ok((Utility.getErrorJson("Post type is invalid. The valid values are "+ postTypes.toArray())).toString()).as(
            		AppConstants.APPLICATION_JSON);


		}
    	
    	FacebookPosts post = new FacebookPosts();
    	post.setPostType(config.getString("fb.page.name")+"/"+postType);
    	
    	List<Parameter> parameters = new ArrayList<Parameter>();
    	
    	try {
	    	if ("feed".equals(postType))
	    		parameters.add(Parameter.with("message", jsonBody.get("message").asText()));
	    	
	    	if ("event".equals(postType)) {
	    		parameters.add( Parameter.with("name", jsonBody.get("message").asText()));
	    		parameters.add( Parameter.with("start_time", Utility.getUnixTimeFromDateString(jsonBody.get("startdate").asText())));
	    		parameters.add( Parameter.with("end_time", Utility.getUnixTimeFromDateString(jsonBody.get("enddate").asText())));
	    	//BinaryAttachment.with(jsonBody.get("imagefilename").asText(), new FileInputStream(jsonBody.get("imagefile").asText()))
	    	}
	    	String scheduleOption = jsonBody.get("optionsRadios").asText();
	    	Logger.debug("Publish type "+ scheduleOption);
	    	if (scheduleOption !=null) {
	    		if("option3".equals(scheduleOption)) {
	    	
	    			parameters.add(Parameter.with("scheduled_publish_time", Utility.getUnixTimeFromDatetimeString(jsonBody.get("scheduledate").asText()
	    					+" "+ jsonBody.get("scheduletime").asText())));
	    			parameters.add(Parameter.with("published", "false"));
	    		} else if("option2".equals(scheduleOption)) {
	    			Logger.debug("unpublished post");
	    			parameters.add(Parameter.with("published", "false"));
	    		}
	    	}
	    	
	    	if (jsonBody.get("expirydate") != null && !StringUtils.isBlank(jsonBody.get("expirydate").asText())) {
	    		parameters.add(Parameter.with("expiration", Utility.getUnixTimeFromDateString(jsonBody.get("expirydate").asText())));
	    		parameters.add(Parameter.with("type", "expire_only"));
	    	}
    	} catch(ParseException e) {
    		Logger.error("Parsing exception", e);
    		return ok((Utility.getErrorJson("Invalid date")).toString()).as(
            		AppConstants.APPLICATION_JSON);
			
    	}
		
		try {
			
			//Logger.debug("access to ken is :"+Crypto.encryptAES("CAARsz0jLG0YBANP0flzyXsg8uG0LB2xJLw43gQc7HBuzGdNFh76gZCZCXZAt5At6VcSz2RroVyCuX3ZBk66nscKXo5rRZBiHq97GwPq6KsDJ7OCAFkhQhZBzOVjC3u4Bsp2SCazylrJCTdtFVAkKxW4nsWC6J3qUWWf3sDQLnTOOeezkIJxq7FVXZAIlQrZBZAMQZD"));
		
			FacebookClient facebookClient = null;
			
			if("event".equals(postType))
				facebookClient = new DefaultFacebookClient(Utility.getAppAccessToken().getAccessToken());
			else 
				facebookClient = new DefaultFacebookClient(Crypto.decryptAES(config.getString("fb.access.token")));
			//facebookClient.
			FacebookType publishMessageResponse = null;
			
			
			if ("photo".equals(postType)) {
				publishMessageResponse = facebookClient.publish(postType, FacebookType.class, 
						BinaryAttachment.with(jsonBody.get("filename").asText(), new FileInputStream(jsonBody.get("filedata").asText())),
						Parameter.with("message", jsonBody.get("message").asText()));
			} else 
				publishMessageResponse = facebookClient.publish(postType, FacebookType.class, 
						parameters.toArray(new Parameter[parameters.size()]));
			
			ObjectNode result = new JsonNodeFactory(true).objectNode();
	        result.put("post_id", publishMessageResponse.getId());
	        result.put("message", "The post has been published");
	        
	        Logger.info(result.toString());
	        
	        saveData(parameters, publishMessageResponse);
	        
	        //return ok(views.html.post.render((JsonNode) result));
	        return ok((result).toString()).as(
            		AppConstants.APPLICATION_JSON);
	        
		} catch (FacebookOAuthException authEx) {
			Logger.error("Facebook login error or authorisation failure", authEx);
			
			return ok((Utility.getErrorJson(authEx.getMessage())).toString()).as(
            		AppConstants.APPLICATION_JSON);
		}  catch (Exception e) {
			Logger.error("Exception while posting", e);
			return ok((Utility.getErrorJson(e.getMessage())).toString()).as(
            		AppConstants.APPLICATION_JSON);
		}
		//return postPage();
	}
	
	@Security.Authenticated(Secured.class)
	@BodyParser.Of(BodyParser.Json.class)
    public static Result posts() {

		JsonNode jsonBody = request().body().asJson();
		
		FacebookClient facebookClient = new DefaultFacebookClient(Utility.getAppAccessToken().getAccessToken());
		
		JsonObject feeds = facebookClient.fetchObject(config.getString("fb.page.name")+"/feed", JsonObject.class);
		Logger.info(feeds.toString());
		
		
		
		return ok(views.html.posts.render(Json.parse(feeds.toString())));
	}
	
	@Security.Authenticated(Secured.class)
	@BodyParser.Of(BodyParser.Json.class)
    public static Result managepost() {

		JsonNode jsonBody = request().body().asJson();
		int skip = 0;
		int limit = 20;
		
		
		if (jsonBody != null) {
			skip = jsonBody.get("skip").asInt();
			limit = jsonBody.get("limit")==null?20:jsonBody.get("limit").asInt();
			if (jsonBody.get("filters") != null) {
				
			}
		}
		JSONObject response = new JSONObject();
		try {
			MongoDatabase db = new MongoConnect().getDB();
			FindIterable<Document> result = db.getCollection("posts").find().sort(new Document("created_date", -1)).skip(skip).limit(limit);
			long count = db.getCollection("posts").count();
			
			
			response.put("totalrecords", count);
			response.put("records", getJsonArray(result));
			
		} catch (UnknownHostException e) {
			Logger.error("Mongodb host not found", e);
		}  catch (JSONException e) {
			Logger.error("Exception while creating the response json", e);
		}
		
		
		
		return ok(views.html.managepost.render(Json.parse(response.toString())));
	}
	
	@Security.Authenticated(Secured.class)
	@BodyParser.Of(BodyParser.Json.class)
    public static Result postdetails(String post_id) {

		FacebookClient facebookClient = new DefaultFacebookClient(Utility.getAppAccessToken().getAccessToken());
        Page page = facebookClient.fetchObject(config.getString("fb.page.name"), Page.class);
        Post post = facebookClient.fetchObject( post_id, Post.class);
        
        /*for(List<Post> posts : postfeed) {
        	for (Post post : posts) {
        		post.
        	}
        }
        posts.get*/
		return ok(views.html.postdetails.render(post));
	}
	
	private static void saveData(List<Parameter> parameters, FacebookType response) {
		
		Document json = new Document();
		
		for (Parameter param : parameters) {
			json.put(param.name, param.value);
		}
		json.put("_id", response.getId());
		json.put("post_type", ""+response.getType());
		json.put("created_date", new Date());
		
		if (json.get("published") == null || "true".equals(json.get("published"))) {
			json.put("published", "true");
			json.put("published_date", new Date());
		}
		
		try {
			MongoDatabase db = new MongoConnect().getDB();
			db.getCollection("posts").insertOne(json);
		} catch (UnknownHostException e) {
			Logger.error("Mongodb host not found", e);
		}
	}
	
	private static JSONArray getJsonArray(FindIterable<Document> result) {
		
		final JSONArray jsonArray = new JSONArray();
		result.forEach(new Block<Document>() {
		    @Override
		    public void apply(final Document document) {
		    	jsonArray.put(document.toJson());
		    }
		});
		return jsonArray;
	}
}
