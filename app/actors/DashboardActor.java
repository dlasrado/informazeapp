package actors;

import play.Configuration;
import play.Logger;
import play.Play;
import util.Utility;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.json.JsonObject;
import com.restfb.types.Page;
import com.restfb.types.Post;

import akka.actor.UntypedActor;



public class DashboardActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Exception {
		

			
			Configuration config = Play.application().configuration();
			
			try {
			
				FacebookClient facebookClient = new DefaultFacebookClient(Utility.getAppAccessToken().getAccessToken());
		        Page page = facebookClient.fetchObject(config.getString("fb.page.name"), Page.class);
		        Connection<Post> posts = facebookClient.fetchConnection(config.getString("fb.page.name")+"/feed", Post.class);
		
		        Logger.info("Total posts : "+posts.getData().size());
		        Logger.info("Total posts : "+posts.getTotalCount());
		        Logger.info("Total likes : "+page.getLikes());
		        ObjectNode result = new JsonNodeFactory(true).objectNode();
		        result.put("total_posts", posts.getData().size());
		        result.put("total_likes", page.getLikes()==null?0L:page.getLikes());
		        result.put("total_checkins", page.getCheckins()==null?0:page.getCheckins());
		        result.put("total_talking", page.getTalkingAboutCount()==null?0:page.getTalkingAboutCount());
		        getSender().tell(result, getSelf());
	            
	        } catch (Exception e) {
	            Logger.error("Error in configuration", e);
	            getSender().tell(e,
	                    getSelf());
	        }
		
	}

}
