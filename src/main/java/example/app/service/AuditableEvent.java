package example.app.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

public class AuditableEvent {

	private String userId;
	private String type;
	private String typeDescription;
	private String referer;
	private String url;
	private boolean isAdminOnlyAction;
	private boolean isDestructiveAction;
	private boolean isAnonymousAction;
	private boolean isContentAffectedAction;
	private Date timestamp;
	private long contentID;

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

	public String getUserId() {
		return userId;
	}

	public AuditableEvent withUserId(String userId){
		this.setUserId(userId);
		return this;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getUrl() {
		return url;
	}

	public AuditableEvent withUrl(String string){
		this.setUrl(string);
		return this;
	}

	public void setUrl(String url) {
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

	public boolean isAnonymousAction() {
		return isAnonymousAction;
	}

	public AuditableEvent withIsAnonymousAction(boolean isAnonimousAction){
		this.setAnonymousAction(isAnonymousAction);
		return this;
	}

	public void setAnonymousAction(boolean isAnonimousAction) {
		this.isAnonymousAction = isAnonimousAction;
	}

	public Long getContentID() {
		return this.contentID;
	}

	public void setContentID(Long contentID) {
		this.contentID = contentID;
		this.isContentAffectedAction(true);
	}

	public AuditableEvent withContentID(Long id) {
		this.setContentID(id);
		return this;
	}

	public boolean isContentAffectedAction() {
		return isContentAffectedAction;
	}

	public AuditableEvent isContentAffectedAction(boolean isContentAffectedAction) {
		this.isContentAffectedAction = isContentAffectedAction;
		return this;
	}

	public String getTypeDescription() {
		return typeDescription;
	}

	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}

	public AuditableEvent withTypeDescription(String description) {
		this.setTypeDescription(description);
		return this;
	}
	
	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}
	
	public AuditableEvent withReferer(String referer) {
		this.setReferer(referer);
		return this;
	}

	/**
	 * Format
	 * Receives {@code String}.
	 * @param templatee the String passed to us
	 */
	public String format(String template){

		Map<String, String> context = new HashMap<String, String>();
		context.put("userId", String.valueOf(this.getUserId()));
		context.put("type", String.valueOf(this.getType()));
		context.put("typeDescription", String.valueOf(this.getTypeDescription()));
		context.put("referer", String.valueOf(this.getReferer()));
		context.put("url", String.valueOf(this.getUrl()));
		context.put("isAdminOnlyAction", String.valueOf(this.isAdminOnlyAction()));
		context.put("isDestructiveAction", String.valueOf(this.isDestructiveAction()));
		context.put("isAnonymousAction", String.valueOf(this.isAnonymousAction()));
		context.put("isContentAffectedAction", String.valueOf(this.isContentAffectedAction()));
		context.put("timestamp", this.getTimestamp().toString());
		context.put("contentID", String.valueOf(this.getContentID()));
		
		StrSubstitutor sub = new StrSubstitutor(context);
		String parsedTemplate = sub.replace(template);
		
		return parsedTemplate;

	}


}
