/**
 * 
 */
package controllers;

import java.io.FileInputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.FacebookPosts;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import play.Configuration;
import play.Logger;
import play.Play;
import play.libs.Crypto;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import util.AppConstants;
import util.MongoConnect;
import util.Utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Photo;
import com.restfb.types.Post;
import com.restfb.types.Video;

/**
 * The controller for all post related actions
 * 
 * @author dlasrado
 *
 */
public class PostController extends Application {

	private static Configuration config = Play.application().configuration();

	
	
    /**
     * Render the /posts page
     * 
     * @return Result
     */
	@Security.Authenticated(Secured.class)
	public static Result postPage() {
		JsonNode json = play.libs.Json.parse("{\"message\":\"New\"}");
		return ok(views.html.post.render(json));
	}
	
	/**
	 * Publish a post to facebook and also save it to local db
	 * The data is sent as a Multipart form due to the possible file data.
	 * 
	 * @return Result
	 */
	@Security.Authenticated(Secured.class)
	public static Result post() {

		Http.MultipartFormData formData = request().body().asMultipartFormData();
		//Logger.debug(""+formData);
		Map<String, String[]> formFields = formData.asFormUrlEncoded();
		
		Logger.debug("Post request receieved for type : "+formFields.get("postType")[0]+"");
		String postType = formFields.get("postType")[0];

		//Is the post type valid?
		if(!AppConstants.ALLOWED_POSTS.contains(postType)) {
			
			return ok(views.html.error.render((Utility.getErrorJson("Post type is invalid. The valid values are "+ 
					AppConstants.ALLOWED_POSTS.toArray()))));
		}
    	
    	FacebookPosts post = new FacebookPosts();
    	post.setPostType(config.getString("fb.page.id")+"/"+postType);
    	
    	List<Parameter> parameters = getParameterList(formFields);
    	
    	try {
			
			//Logger.debug("access to ken is :"+Crypto.encryptAES("CAARsz0jLG0YBANP0flzyXsg8uG0LB2xJLw43gQc7HBuzGdNFh76gZCZCXZAt5At6VcSz2RroVyCuX3ZBk66nscKXo5rRZBiHq97GwPq6KsDJ7OCAFkhQhZBzOVjC3u4Bsp2SCazylrJCTdtFVAkKxW4nsWC6J3qUWWf3sDQLnTOOeezkIJxq7FVXZAIlQrZBZAMQZD"));
			FacebookClient facebookClient = new DefaultFacebookClient(
					Crypto.decryptAES(config.getString("fb.access.token")), Version.VERSION_2_5);
			//facebookClient.
			FacebookType publishMessageResponse = null;			
			
			//If the type is media, send the BinaryAttachment
			if (AppConstants.PHOTO.equals(postType) || AppConstants.VIDEO.equals(postType)) {
				FilePart filePart = formData.getFile("filedata");
				publishMessageResponse = facebookClient.publish(config.getString("fb.page.id")+"/"+postType, FacebookType.class, 
						BinaryAttachment.with(filePart.getFilename(), new FileInputStream(filePart.getFile())),
						parameters.toArray(new Parameter[parameters.size()]));
			} else 
				publishMessageResponse = facebookClient.publish(postType, FacebookType.class, 
						parameters.toArray(new Parameter[parameters.size()]));
	        
	        Logger.info("Post published to facebook. ID:"+  publishMessageResponse.getId());
	        
	        //Set additional param to be saved in DB
	        parameters.add(Parameter.with("post_type", postType));
	        saveData(parameters, publishMessageResponse);
	        
	        return postdetails(publishMessageResponse.getId());
	        
		} catch (FacebookOAuthException authEx) {
			Logger.error("Facebook login error or authorisation failure", authEx);
			
			return ok(views.html.error.render((Utility.getErrorJson(authEx.getMessage()))));
		}  catch (Exception e) {
			Logger.error("Exception while posting", e);
			return ok(views.html.error.render((Utility.getErrorJson(e.getMessage()))));
		}
	}
	
	/**
	 * Action to return posts from the page
	 * 
	 * @return
	 */
	@Security.Authenticated(Secured.class)
    public static Result posts() {
		
		FacebookClient facebookClient = new DefaultFacebookClient(
				Utility.getAppAccessToken().getAccessToken(), Version.VERSION_2_5);
		
		JsonObject feeds = facebookClient.fetchObject(config.getString("fb.page.name")+"/feed", 
				JsonObject.class);
		Logger.info(feeds.toString());
				
		return ok(views.html.posts.render(Json.parse(feeds.toString())));
	}
	
	/**
	 * Publish a unpublished post
	 * The assumption that the is_published = false check is at UI level,
	 * if not the check has to be carried out. It does not harm by not checking.
	 * 
	 * @param post_id : The facebook unique id for the unpublished post
	 * @return JSON resut
	 */
	@Security.Authenticated(Secured.class)
    public static Result publish(String post_id) {
	
		FacebookClient facebookClient = new DefaultFacebookClient(
				Crypto.decryptAES(config.getString("fb.access.token")), Version.VERSION_2_5);
		
		JsonObject publishMessageResponse = facebookClient.publish(post_id, com.restfb.json.JsonObject.class,
				Parameter.with("is_published", "true"));
        
		Logger.info("The result of publishing the unpublished post : "+ publishMessageResponse);
		
		if (publishMessageResponse.getBoolean("success")) {
			try {
				MongoDatabase db = new MongoConnect().getDB();
				db.getCollection("posts").updateOne(new Document("_id",post_id), 
						new Document("$set",new Document("published","true").append("published_date", new Date())));
			} catch (UnknownHostException e) {
				Logger.error("Mongodb host not found", e);
				return ok(views.html.error.render(Utility.getErrorJson("Mongodb host not found :"+
						e.getMessage())));
			}
		}
		//is_published
		//return ok(publishMessageResponse.toString()).as(
        	//	AppConstants.APPLICATION_JSON);
		return postdetails(post_id);
	}
	
	/**
	 * Return the list of posts saved ion the database.
	 * Max returned is AppConstants.PAGE_SIZE (20)
	 * Sort order is hardcoded as created_date (desc) for now.
	 * 
	 * @param page : The page number
	 * @return Result
	 */
	@Security.Authenticated(Secured.class)
	@BodyParser.Of(BodyParser.Json.class)
    public static Result managepost(Integer page) {

		JsonNode jsonBody = request().body().asJson();
		int skip = (page-1)*AppConstants.PAGE_SIZE;
		int limit = AppConstants.PAGE_SIZE;
		Logger.info("Fetching data for page number "+ page);
		
		if (jsonBody != null) {
			skip = jsonBody.get("skip").asInt();
			limit = jsonBody.get("limit")==null?20:jsonBody.get("limit").asInt();
			if (jsonBody.get("filters") != null) {
				
			}
		}
		JSONObject response = new JSONObject();
		try {
			MongoDatabase db = new MongoConnect().getDB();
			FindIterable<Document> result = db.getCollection("posts")
					.find(new Document("status", new Document("$ne","DELETED"))).sort(new Document("created_date", -1)).skip(skip).limit(limit);
			long count = db.getCollection("posts").count(new Document("status", new Document("$ne","DELETED")));
			
			
			response.put("totalrecords", count);
			response.put("startrec", skip+1);
			long end = skip+limit;
			if (end > count) end = count;
			response.put("endrec", end);
			response.put("records", Utility.getJsonArray(result));
			
		} catch (UnknownHostException e) {
			Logger.error("Mongodb host not found", e);
			response = Utility.getErrorJson("Mongodb host not found : "+ e.getMessage());
		}  catch (JSONException e) {
			Logger.error("Exception while creating the response json", e);
			response = Utility.getErrorJson("Exception while creating the response json");
		}
		
		return ok(views.html.managepost.render(Json.parse(response.toString())));
	}
	
	/**
	 * Returns the post details given the post_id
	 *  
	 * @param post_id : Facebook unique Id for the post
	 * @return Result
	 */
	@Security.Authenticated(Secured.class)
	@BodyParser.Of(BodyParser.Json.class)
    public static Result postdetails(String post_id) {

		
        FindIterable<Document> result = null;
        try{
	        MongoDatabase db = new MongoConnect().getDB();
	        result = db.getCollection("posts").find(new Document("_id",post_id));
	        
	        if (result.first()==null) {
	        	Logger.error("The post not found in local store");
	        	throw new RuntimeException("The post not found in local store");
	        }
        } catch (UnknownHostException e) {
			Logger.error("Mongodb host not found", e);
			return null;
		}
        
        String postType = result.first().getString("post_type");
        
        FacebookClient facebookClient = new DefaultFacebookClient(
        		Crypto.decryptAES(config.getString("fb.access.token")), Version.VERSION_2_5);
        //Page page = facebookClient.fetchObject(config.getString("fb.page.id")+"/"+ postType, Page.class);
        
        FacebookPosts fbPost = null;
        if (AppConstants.VIDEO.equals(postType)) {
        	
        	/*Logger.debug(facebookClient.fetchObject( post_id, JsonObject.class,
        		Parameter.with("fields", Utility.getFetchFieldList(postType))
        		).toString());*/
        	fbPost = getFacebookPost(facebookClient.fetchObject( post_id, Video.class,
        		Parameter.with("fields", Utility.getFetchFieldList(postType))
        		));
        	
        } else if (AppConstants.PHOTO.equals(postType)) {
        	
        	fbPost = getFacebookPost(facebookClient.fetchObject( post_id, Photo.class,
        		Parameter.with("fields", Utility.getFetchFieldList(postType))
        		));
        	
        } else {
        	
        	fbPost = getFacebookPost(facebookClient.fetchObject( post_id, Post.class,
        		Parameter.with("fields", Utility.getFetchFieldList(postType))
        		));
        	
        }
        
        return ok(views.html.postdetails.render(fbPost, result.first().toJson()));
        
	}
	
	/**
	 * deletes the given post_id
	 *  
	 * @param post_id : Facebook unique Id for the post
	 * @return Result
	 */
	@Security.Authenticated(Secured.class)
    public static Result deletepost(String post_id) {
	
		FacebookClient facebookClient = new DefaultFacebookClient(
				Crypto.decryptAES(config.getString("fb.access.token")), Version.VERSION_2_5);
		
		boolean status = facebookClient.deleteObject(post_id, 
				Parameter.with("post_id", post_id));
        
		Logger.info("The result of deleting the post : "+ status);
		
		if (status) {
			try {
				MongoDatabase db = new MongoConnect().getDB();
				db.getCollection("posts").updateOne(new Document("_id",post_id), 
						new Document("$set",new Document("status","DELETED").append("deleted_date", new Date())));
			} catch (UnknownHostException e) {
				Logger.error("Mongodb host not found", e);
				return ok(views.html.error.render(Utility.getErrorJson("Mongodb host not found :"+
						e.getMessage())));
			}
			JsonNode json = play.libs.Json.parse("{\"message\":\"success\"}");
			return ok(json);
		} else {
			JsonNode json = play.libs.Json.parse("{\"message\":\"failed\"}");
			return ok(json);
		}
		
	}
	/*
	 * The helper method to return a list of parameter for the post call.
	 * The input is formURLEncoded fields submitted from the UI
	 */
	private static List<Parameter> getParameterList(Map<String, String[]> formFields) {
		
		List<Parameter> parameters = new ArrayList<Parameter>();
    	
    	try {
	    	parameters.add(Parameter.with("message", formFields.get("message")[0]));
	    	
	    	/* Events are not supported after 2.0 of Graph API
	    	if ("events".equals(postType)) {
	    		parameters.add( Parameter.with("name", formFields.get("message")[0]));
	    		parameters.add( Parameter.with("start_time", Utility.getUnixTimeFromDateString(formFields.get("startdate")[0])));
	    		parameters.add( Parameter.with("end_time", Utility.getUnixTimeFromDateString(formFields.get("enddate")[0])));
	    	//BinaryAttachment.with(jsonBody.get("imagefilename").asText(), new FileInputStream(jsonBody.get("imagefile").asText()))
	    	}
	    	*/
	    	String scheduleOption = formFields.get("optionsRadios")[0];
	    	Logger.debug("Publish schedule type "+ scheduleOption);
	    	if (scheduleOption !=null) {
	    		if("option3".equals(scheduleOption)) {
	    	
	    			parameters.add(Parameter.with("scheduled_publish_time", 
	    					Utility.getUnixTimeFromDatetimeString(formFields.get("scheduledate")[0]
	    					+" "+ formFields.get("scheduletime")[0])));
	    			parameters.add(Parameter.with("published", "false"));
	    		} else if("option2".equals(scheduleOption)) {
	    			Logger.debug("unpublished post");
	    			parameters.add(Parameter.with("published", "false"));
	    		}
	    	}
    	} catch(ParseException e) {
    		Logger.error("Parsing exception", e);
    		throw new RuntimeException(Utility.getErrorJson("Invalid date :"+
    				e.getMessage()).toString(),e);
			
    	}
    	return parameters;
	}
	
	/*
	 * Save the parameter and response data in the local data store
	 * This is useful for tracking and quick fetch
	 */
	private static void saveData(List<Parameter> parameters, FacebookType response) {
		
		Document json = new Document();
		
		for (Parameter param : parameters) {
			json.put(param.name, param.value);
		}
		json.put("_id", response.getId());
		
		json.put("created_date", new Date());
		json.put("status", "ACTIVE");
		
		if (json.get("published") == null || "true".equals(json.get("published"))) {
			json.put("published", "true");
			json.put("published_date", new Date());
		}
		
		try {
			MongoDatabase db = new MongoConnect().getDB();
			db.getCollection("posts").insertOne(json);
		} catch (UnknownHostException e) {
			Logger.error("Mongodb host not found", e);
			throw new RuntimeException(Utility.getErrorJson("Mongodb host not found :"+
					e.getMessage()).toString(),e);
		}
	}
	
	/*
	 * Build a generic facebook object from various NamedFacebookType's
	 * 
	 */
	private static FacebookPosts getFacebookPost (NamedFacebookType post) {
		FacebookPosts fbPost = new FacebookPosts();
		if (post instanceof Video) {
			Video video = (Video) post;
			
			fbPost.setPostId(video.getId());
			fbPost.setPostType(video.getType());
			fbPost.setEmbedHTML(video.getEmbedHtml());
			fbPost.setComments((video.getComments()==null)?null:video.getComments().getData());
			fbPost.setSource(video.getSource());
			//video.getLength()
		} else if (post instanceof Photo) {
			Photo photo = (Photo) post;
			
			fbPost.setPostId(photo.getId());
			fbPost.setPostType(photo.getType());
			fbPost.setPicture(photo.getPicture());
			fbPost.setName(photo.getName());
			fbPost.setLikesCount(new Long(photo.getLikes().size()));
			//fbPost.setSharesCount(photo.get);
			fbPost.setComments(photo.getComments());
		} else {
			Post thispost = (Post) post;
			
			fbPost.setPostId(thispost.getId());
			fbPost.setPostType(thispost.getType());
			fbPost.setMessage(thispost.getMessage());
			fbPost.setLikesCount(thispost.getLikesCount());
			fbPost.setSharesCount(thispost.getSharesCount());
			fbPost.setComments((thispost.getComments()==null)?null:thispost.getComments().getData());
			//thispost.getComments().getData().get(0).getFrom().getName().
		}
		return fbPost;
	}
	
	
}
