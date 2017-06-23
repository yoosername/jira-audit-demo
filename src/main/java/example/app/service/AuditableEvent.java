package example.app.service;

import java.net.URI;
import java.util.Date;

import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.user.ApplicationUser;

public class AuditableEvent {


	private ApplicationUser user;
	private String type;
	private URI url;
	private boolean isAdminOnlyAction;
	private boolean isDestructiveAction;
	private boolean isAnonimousAction;
	private Date timestamp;
	private long contentID;
	
	@Override
	public String toString() {
		return String.format(
				"%s, %s, %s, %s, %s, %s, %s", 
				timestamp,
				user,
				type,
				(contentID!=0)?contentID:'-',
				isAdminOnlyAction,
				isDestructiveAction,
				isAnonimousAction
		);
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public AuditableEvent at(Date date){
		this.setTimestamp(date);
		return this;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public AuditableEvent(){
		
	}

	public ApplicationUser getUser() {
		return user;
	}

	public AuditableEvent withUser(ApplicationUser user){
		this.setUser(user);
		return this;
	}

	public void setUser(ApplicationUser user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public AuditableEvent withType(String type){
		this.setType(type);
		return this;
	}

	public void setType(String type) {
		this.type = type;
	}

	public URI getUrl() {
		return url;
	}

	public AuditableEvent withUrl(URI uri){
		this.setUrl(uri);
		return this;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	public boolean isAdminOnlyAction() {
		return isAdminOnlyAction;
	}

	public AuditableEvent withIsAdminOnly(boolean isAdminOnly){
		this.setAdminOnlyAction(isAdminOnly);
		return this;
	}

	public void setAdminOnlyAction(boolean isAdminOnlyAction) {
		this.isAdminOnlyAction = isAdminOnlyAction;
	}

	public boolean isDestructiveAction() {
		return isDestructiveAction;
	}

	public AuditableEvent withIsDestructiveAction(boolean isDestructiveAction){
		this.setDestructiveAction(isDestructiveAction);
		return this;
	}

	public void setDestructiveAction(boolean isDestructiveAction) {
		this.isDestructiveAction = isDestructiveAction;
	}

	public boolean isAnonimousAction() {
		return isAnonimousAction;
	}

	public AuditableEvent withIsAnonimousAction(boolean isAnonimousAction){
		this.setAnonimousAction(isAnonimousAction);
		return this;
	}

	public void setAnonimousAction(boolean isAnonimousAction) {
		this.isAnonimousAction = isAnonimousAction;
	}

	public Long getContentID() {
		return this.contentID;
	}
	
	public void setContentID(Long contentID) {
		contentID = (contentID == null) ? 0 : contentID;
		this.contentID = contentID;
	}
	
	public AuditableEvent withContentID(Long id) {
		this.setContentID(id);
		return this;
	}


}
