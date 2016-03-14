/**
 * 
 */
package models;

import java.util.Date;
import java.util.List;

import com.restfb.types.Comment;

/**
 * @author dlasrado
 *
 */
public class FacebookPosts {

	private String postType;
	private String message;
	private String name;
	private Date created_time;
	private String embedHTML;
	private String picture;
	private Long likesCount = 0L;
	private Long sharesCount = 0L;
	private int commentsCount = 0;
	private List<Comment> comments;
	private String postId;
	private String description;
	private String source;

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
	 * @return the created_time
	 */
	public Date getCreated_time() {
		return created_time;
	}
	/**
	 * @param created_time the created_time to set
	 */
	public void setCreated_time(Date created_time) {
		this.created_time = created_time;
	}
	/**
	 * @return the embedURL
	 */
	public String getEmbedHTML() {
		return embedHTML;
	}
	/**
	 * @param embedURL the embedURL to set
	 */
	public void setEmbedHTML(String embedHTML) {
		this.embedHTML = embedHTML;
	}
	/**
	 * @return the picture
	 */
	public String getPicture() {
		return picture;
	}
	/**
	 * @param picture the picture to set
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}
	/**
	 * @return the likeCount
	 */
	public Long getLikesCount() {
		return likesCount;
	}
	/**
	 * @param likeCount the likeCount to set
	 */
	public void setLikesCount(Long likeCount) {
		if(likeCount == null)
			likeCount = 0L;
		this.likesCount = likeCount;
	}
	/**
	 * @return the shareCount
	 */
	public Long getSharesCount() {
		return sharesCount;
	}
	/**
	 * @param shareCount the shareCount to set
	 */
	public void setSharesCount(Long shareCount) {
		if(shareCount == null)
			shareCount = 0L;
		this.sharesCount = shareCount;
	}
	/**
	 * @return the commentsCount
	 */
	public int getCommentsCount() {
		return commentsCount;
	}
	/**
	 * @param commentsCount the commentsCount to set
	 */
	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}
	/**
	 * @return the comments
	 */
	public List<Comment> getComments() {
		return comments;
	}
	/**
	 * @param comments the comments to set
	 */
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	/**
	 * @return the postId
	 */
	public String getPostId() {
		return postId;
	}
	/**
	 * @param postId the postId to set
	 */
	public void setPostId(String postId) {
		this.postId = postId;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
}
