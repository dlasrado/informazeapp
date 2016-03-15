package util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import play.api.i18n.Lang;
import scala.concurrent.duration.Duration;
import akka.util.Timeout;





public interface AppConstants {

	public static final String       APPLICATION_TEXT    = "application/text";
	public static final String       APPLICATION_JSON    = "application/json";
	public static final String       APPLICATION_PDF    = "application/pdf";
	public static final String       APPLICATION_XLS    = "application/vnd.ms-excel";
	
	public static Lang 				lang 				= new Lang("en", "us");
	
	public static final Timeout     TIMEOUT             = new Timeout(
            Duration.create(
                    60,
                    TimeUnit.SECONDS));
	
	//Error codes
    public static final String 		INTERNAL_SERVER_ERROR = "A500";
    public static final String 		CONFIGURATION_ERROR = "A550";
    public static final String 		PARSING_ERROR = "A575";
    public static final String 		BAD_REQUEST_ERROR = "A400";
    public static final String 		NOT_FOUND_ERROR = "A404";
    
    public static final String 		X_REQUEST_TYPE = "CLIENT_REQUEST_TYPE";
    public static final String 		WEB_CLIENT = "WEB"; 
    public static final Integer		PAGE_SIZE = 20;
    
	public static final String MESSAGE 		= "feed";
	public static final String PHOTO 		= "photos";
	public static final String VIDEO 		= "videos";
	
	public static final List<String> ALLOWED_POSTS = Arrays.asList(new String[]{MESSAGE,PHOTO,VIDEO});;
	
}
