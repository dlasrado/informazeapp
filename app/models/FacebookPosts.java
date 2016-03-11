/**
 * 
 */
package models;

import java.io.File;
import java.util.Date;

/**
 * @author dlasrado
 *
 */
public class FacebookPosts {

	private String postType;
	private String message;
	private String name;
	private Date start_time;
	private Date end_time;
	private File attachment;
	/**
	 * @return the postType
	 */
	public String getPostType() {
		return postType;
	}
	/**
	 * @param postType the postType to set
	 */
	public void setPostType(String postType) {
		this.postType = postType;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the start_time
	 */
	public Date getStart_time() {
		return start_time;
	}
	/**
	 * @param start_time the start_time to set
	 */
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	/**
	 * @return the end_time
	 */
	public Date getEnd_time() {
		return end_time;
	}
	/**
	 * @param end_time the end_time to set
	 */
	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}
	/**
	 * @return the attachment
	 */
	public File getAttachment() {
		return attachment;
	}
	/**
	 * @param attachment the attachment to set
	 */
	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}
	
}
