import play.*;


/**
 * 
 */

/**
 * @author dlasrado
 *
 */
public class Global extends GlobalSettings {

	@Override
	  public void onStart(Application app) {
	    Logger.info("Application has started");
	  }  
	  
	  @Override
	  public void onStop(Application app) {
	    Logger.info("Application shutdown...");
	  } 
	  
	  
}
