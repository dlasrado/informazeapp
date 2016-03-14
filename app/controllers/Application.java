package controllers;

import static play.data.Form.form;

import java.util.Date;

import play.Configuration;
import play.Logger;
import play.Play;
import play.api.i18n.Lang;
import play.api.libs.Crypto;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.AppConstants;
import util.Utility;
import views.html.login;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.Page;
import com.restfb.types.Post;

/**
 * The main controller class to render general application requests 
 * @author dlasrado
 *
 */
public class Application extends Controller {

	private static Configuration config = Play.application().configuration();
	private static Lang lang = new Lang("en", "us");
	
	/**
	 * The root access
	 * 
	 * @return
	 */
	@Security.Authenticated(Secured.class)
    public static Result index() {
		return ok(
	            login.render(form(Login.class))
	        );
    }

    /**
     * Home page controller action.
     * 
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result home() {
 
    	Logger.info("Accessing home page");
    	
    	final String requestType = request().getHeader(AppConstants.X_REQUEST_TYPE);
    	//Design to handle web and restful API calls using same method
    	final boolean isWebClient = requestType==null?true:
    		(AppConstants.WEB_CLIENT.equals(requestType)?true:false);
        
    	FacebookClient facebookClient = new DefaultFacebookClient(
    			Utility.getAppAccessToken().getAccessToken());
    	//Get page object
        Page page = facebookClient.fetchObject(
        		config.getString("fb.page.name"), Page.class);
        //Get all recent posts
        Connection<Post> posts = facebookClient.fetchConnection(
        		config.getString("fb.page.name")+"/feed", Post.class);

        
        ObjectNode result = new JsonNodeFactory(true).objectNode();
        result.put("total_posts", posts.getData().size());
        result.put("total_likes", page.getLikes()==null?0L:page.getLikes());
        result.put("total_checkins", page.getCheckins()==null?0:page.getCheckins());
        result.put("total_talking", page.getTalkingAboutCount()==null?0:
        	page.getTalkingAboutCount());
        
        if (isWebClient) {
    		return ok(views.html.posts.render((JsonNode) result));
    	} else {
    		return ok(((JsonNode) result).toString()).as(
        		AppConstants.APPLICATION_JSON);
    	}
        
    }
    
    
    /**
     * Login page action which renders the login page
     * 
     * @return
     */
    public static Result login() {
        return ok(
            login.render(form(Login.class))
        );
    }
    
    /**
     * Autheicates the given username and password.
     * Basic authentication is implemented.
     * 
     * @return
     */
    public static Result authenticate() {
    	Form<Login> loginForm = form(Login.class).bindFromRequest();
    	Logger.info(loginForm.name());
    	//Autenticate the user and reder based on validity
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            session().clear();
            session("email", loginForm.get().email);
            return redirect(
                routes.Application.home()
            );
        }
    }

    /**
     * Log out action which
     * @return
     */
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(
            routes.Application.login()
        );
    }
    
    /**
     * The inner class for Login form
     * 
     * Only 2 fields are mandatory, email and password
     *
     */
    public static class Login {

    	@Constraints.Required
        public String email;
    	@Constraints.Required
        public String password;

        public String validate() {
        	Logger.info(email +","+password);
        	Logger.info((String)config.getList("app.user.list").get(0));
            if (!config.getList("app.user.list").contains(email) || !"welcome".equals(password)) {
              return "Invalid user or password";
            }
            return null;
        }
    }
    
   
    
	/**
	 * Creates error response for an error code
	 * 
	 * @param errorCode
	 * @param uuid
	 * @param lang
	 * @return
	 */
	public static JsonNode getErrorResponse(String errorCode, String errorId,
	        Lang lang) {

		
		JsonNodeFactory jsonFactory = new JsonNodeFactory(true);
		ObjectNode json = jsonFactory.objectNode();

		json.put("error_code", errorCode);
		json.put("error_message", Messages.get(lang, errorCode));
		json.put("error_uuid", errorId);
		
		return json;
	}
	
	
}
