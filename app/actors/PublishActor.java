package actors;

import play.Logger;
import util.Utility;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.FacebookType;

import models.FacebookPosts;
import akka.actor.UntypedActor;

public class PublishActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Exception {
		// TODO Auto-generated method stub
		
		if(message instanceof FacebookPosts) {
			
			FacebookPosts post = (FacebookPosts)message;
			
			try {
			
				FacebookClient facebookClient = new DefaultFacebookClient(
						Utility.getAppAccessToken().getAccessToken());
				//facebookClient.
				
				Parameter parameters = Parameter.with("application", "Infomaze");
				if (post.getMessage() != null)
					parameters = parameters.with("message", post.getMessage());
				
				if (post.getName() != null)
					parameters = parameters.with("name", post.getName());
				
				FacebookType publishMessageResponse =
					        facebookClient.publish(post.getPostType(), FacebookType.class, parameters);				
				
				ObjectNode result = new JsonNodeFactory(true).objectNode();
		        result.put("post_id", publishMessageResponse.getId());
		        result.put("message", "Published the post");
		        
		        getSender().tell(result, getSelf());
			} catch (FacebookOAuthException authEx) {
				Logger.error("Facebook login error or authorisation failure", authEx);
			}
		} else {
			Logger.error("Invalid object recieved to post");
            unhandled(message);
		}
	}
}
