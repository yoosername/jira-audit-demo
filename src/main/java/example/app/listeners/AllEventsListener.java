package example.app.listeners;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.DashboardViewEvent;
import com.atlassian.jira.event.ExportEvent;
import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.event.ProjectDeletedEvent;
import com.atlassian.jira.event.ProjectUpdatedEvent;
import com.atlassian.jira.event.issue.MentionIssueCommentEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueSearchEvent;
import com.atlassian.jira.event.issue.IssueViewEvent;
import com.atlassian.jira.event.issue.QuickBrowseEvent;
import com.atlassian.jira.event.issue.QuickSearchEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.event.user.LoginEvent;
import com.atlassian.jira.event.user.LogoutEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.HttpServletVariables;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import example.app.service.AuditService;
import example.app.service.AuditableEvent;

/**
 * Simple JIRA listener using the atlassian-event library
 */
@Component
public class AllEventsListener implements LifecycleAware, InitializingBean, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(AllEventsListener.class);

	private final EventPublisher eventPublisher;
	private final AuditService auditService;
	private final EventTypeManager eventTypeManager;

	private HttpServletVariables servletVars;

	/**
	 * Constructor.
	 * @param eventPublisher injected {@code EventPublisher} implementation.
	 * @param auditService injected {@code AuditService} implementation.
	 * @param eventTypeManager injected {@code EventTypeManager} implementation.
	 */
	@Autowired
	public AllEventsListener(
		@ComponentImport EventPublisher eventPublisher, 
		AuditService auditService,
		@ComponentImport EventTypeManager eventTypeManager,
		@ComponentImport HttpServletVariables servletVars
	) 
	{
		this.eventPublisher = eventPublisher;
		this.auditService = auditService;
		this.eventTypeManager = eventTypeManager;
		this.servletVars = servletVars;
	}

	/**
	 * Called when the plugin has been enabled.
	 * @throws Exception
	 */
	@Override
	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		// register ourselves with the EventPublisher
		log.debug("=======================     AUDIT PLUGIN ENABLED     ========================");
		eventPublisher.register(this);
	}

	/**
	 * Called when the plugin is being disabled or removed.
	 * @throws Exception
	 */
	@Override
	@PreDestroy
	public void destroy() throws Exception {
		// unregister ourselves with the EventPublisher
		eventPublisher.unregister(this);
		log.debug("=======================     AUDIT PLUGIN DISABLED     ========================");
	}


	// https://developer.atlassian.com/jiradev/jira-platform/jira-architecture/jira-technical-overview/jira-specific-atlassian-events
	// DONE: Add Dashboard view event
	// DONE: Add login / logout events
	// DONE: Add Project create / delete / modify events
	// DONE: Add Issue create / delete / modify events
	// DONE: Add Issue View events
	// DONE: Add Export Events
	// PARTIALLY DONE: Add Issue workflow events. = currently issue event with source = workflow
	// DONE: Add JQL Search Events
	// TODO: Add User modification events
	

	private String getCurrentUri(){
		HttpServletRequest req = servletVars.getHttpRequest();
	    return req.getRequestURI();
	}
	
	private String getCurrentReferer(){
		HttpServletRequest req = servletVars.getHttpRequest();
	    return (req.getHeader("referer")!=null)?req.getHeader("referer"):"";
	}
		
	@EventListener
	// For testing - show us all the different events when they fire
	public void onJiraEvent(JiraEvent event) {
		log.debug("got JIRA event {}", event);
	}
	
	private String getCurrentUserId(){
		JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
		ApplicationUser user = context.getLoggedInUser();
		String userId = (user != null) ? user.getUsername() : "unknown";
	    return userId;
	}


	/**
	 * Receives any {@code MentionIssueCommentEvent}s sent by JIRA.
	 * @param mentionIssueCommentEvent the MentionIssueCommentEvent passed to us
	 */
	@EventListener
	public void onExportEvent(MentionIssueCommentEvent mentionIssueCommentEvent) {

		Date timestamp = new Date();
		
		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(mentionIssueCommentEvent.getFromUser().getUsername())
				.withType("User Mention")
				.withTypeDescription("from:"+mentionIssueCommentEvent.getFromUser()+",to:"+mentionIssueCommentEvent.getToUsers()+",msg:"+mentionIssueCommentEvent.getMentionText())
				.isContentAffectedAction(true)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
		);

	}
	
	/**
	 * Receives any {@code ExportEvent}s sent by JIRA.
	 * @param exportEvent the ExportEvent passed to us
	 */
	@EventListener
	public void onExportEvent(ExportEvent exportEvent) {

		Date timestamp = exportEvent.getTime();
		String name = exportEvent.calculateEventName();
		
		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(getCurrentUserId())
				.withType(name)
				.withTypeDescription(exportEvent.getParams().toString())
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
		);

	}
	
	/**
	 * Receives any {@code IssueViewEvent}s sent by JIRA.
	 * @param IssueViewEvent the IssueViewEvent passed to us
	 */
	@EventListener
	public void onQuickBrowseEvent(QuickBrowseEvent quickBrowseEvent) {

		Date timestamp = quickBrowseEvent.getTime();
		
		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(getCurrentUserId())
				.withType("Quick Browse")
				.withTypeDescription(quickBrowseEvent.getIssueKey())
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
		);

	}
	
	/**
	 * Receives any {@code QuickSearchEvent}s sent by JIRA.
	 * @param quickSearchEvent the QuickSearchEvent passed to us
	 */
	@EventListener
	public void onQuickSearchEvent(QuickSearchEvent quickSearchEvent) {

		Date timestamp = quickSearchEvent.getTime();
		
		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(getCurrentUserId())
				.withType("Quick Search")
				.withTypeDescription(quickSearchEvent.getSearchString())
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
		);

	}
	
	/**
	 * Receives any {@code IssueSearchEvent}s sent by JIRA.
	 * @param issueSearchEvent the IssueSearchEvent passed to us
	 */
	@EventListener
	public void onIssueSearchEvent(IssueSearchEvent issueSearchEvent) {

		String type = issueSearchEvent.getType();
		Date timestamp = issueSearchEvent.getTime();
		
		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(getCurrentUserId())
				.withType(type)
				.withTypeDescription(issueSearchEvent.getQuery())
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
		);

	}
	
	/**
	 * Receives any {@code IssueViewEvent}s sent by JIRA.
	 * @param IssueViewEvent the IssueViewEvent passed to us
	 */
	@EventListener
	public void onIssueViewEvent(IssueViewEvent issueViewEvent) {

		Long eventTypeId = issueViewEvent.getId();
		EventType type = eventTypeManager.getEventType(eventTypeId);
		Date timestamp = issueViewEvent.getTime();
		
		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(getCurrentUserId())
				.withType(type.getName())
				.withTypeDescription(issueViewEvent.getParams().toString())
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
		);

	}
	
	/**
	 * Receives any {@code DashboardViewEvent}s sent by JIRA.
	 * @param dashboardViewEvent the DashboardViewEvent passed to us
	 */
	@EventListener
	public void onDashboardViewEvent(DashboardViewEvent dashboardViewEvent) {

		String typeName = "Dashboard View";
		Date timestamp = dashboardViewEvent.getTime();

		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(getCurrentUserId())	
				.withType(typeName)
				.withTypeDescription(dashboardViewEvent.getId().toString())
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
		);

	}
	
	/**
	 * Receives any {@code IssueEvent}s sent by JIRA.
	 * @param issueEvent the IssueEvent passed to us
	 */
	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) {

		Long eventTypeId = issueEvent.getEventTypeId();
		EventType type = eventTypeManager.getEventType(eventTypeId);
		String typeName = type.getName();
		ApplicationUser user = issueEvent.getUser();
		Date timestamp = issueEvent.getTime();
		String typeDescription = "";
		try{
			typeDescription = issueEvent.getParams().get("eventsource").toString();
		}catch(Exception e){}

		auditService.handleEvent(
			new AuditableEvent()
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withUserId(user.getUsername())
				.withType(typeName)
				.withTypeDescription(typeDescription)
				.withContentID(issueEvent.getIssue().getId())
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction((typeName.equals("Issue Deleted")?true:false))
				.at(timestamp)
		);

	}

	/**
	 * User Login Event
	 * Receives any {@code LoginEvent}s sent by JIRA.
	 * @param event the LoginEvent passed to us
	 */
	@EventListener
	public void onLoginEvent(LoginEvent event) {

		ApplicationUser user = event.getUser();
		Date timestamp = event.getTime();

		auditService.handleEvent(
				new AuditableEvent()
				.withUserId(user.getUsername())
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withType("Login")
				.withTypeDescription("login")
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
				);

	}

	/**
	 * User Logout Event
	 * Receives any {@code LogoutEvent}s sent by JIRA.
	 * @param event the LogoutEvent passed to us
	 */
	@EventListener
	public void logoutEvent(LogoutEvent event) {

		ApplicationUser user = event.getUser();
		Date timestamp = event.getTime();

		auditService.handleEvent(
				new AuditableEvent()
				.withUserId(user.getUsername())
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withType("Logout")
				.withTypeDescription("logout")
				.isContentAffectedAction(false)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
				);
	}

	/**
	 * Project created event
	 * Receives any {@code ProjectCreatedEvent}s sent by JIRA.
	 * @param event the ProjectCreatedEvent passed to us
	 */
	@EventListener
	public void projectCreatedEvent(ProjectCreatedEvent event) {

		ApplicationUser user = event.getUser();
		Date timestamp = event.getTime();
		Long id = event.getProject().getId();        

		auditService.handleEvent(
				new AuditableEvent()
				.withUserId(user.getUsername())
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withType("Project Created")
				.withContentID(id)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
				);
	}

	/**
	 * Project deleted event
	 * Receives any {@code ProjectDeletedEvent}s sent by JIRA.
	 * @param event the ProjectDeletedEvent passed to us
	 */
	@EventListener
	public void projectDeletedEvent(ProjectDeletedEvent event) {

		ApplicationUser user = event.getUser();
		Date timestamp = event.getTime();
		Long id = event.getProject().getId();

		auditService.handleEvent(
				new AuditableEvent()
				.withUserId(user.getUsername())
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withType("Project Deleted")
				.withContentID(id)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(true)
				.at(timestamp)
				);
	}

	/**
	 * Project updated event
	 * Receives any {@code ProjectUpdatedEvent}s sent by JIRA.
	 * @param event the ProjectUpdatedEvent passed to us
	 */
	@EventListener
	public void projectUpdatedEvent(ProjectUpdatedEvent event) {

		ApplicationUser user = event.getUser();
		Date timestamp = event.getTime();
		Long id = event.getProject().getId();

		auditService.handleEvent(
				new AuditableEvent()
				.withUserId(user.getUsername())
				.withReferer(getCurrentReferer())
				.withUrl(getCurrentUri())
				.withType("Project Updated")
				.withContentID(id)
				.withIsAnonymousAction(false)
				.withIsAdminOnly(false)
				.withIsDestructiveAction(false)
				.at(timestamp)
				);
	}

	/**
	 * When this plugin is enabled register our event handlers
	 */
	@Override
	public void onStart() {
		// register ourselves with the EventPublisher
		eventPublisher.register(this);
		log.debug("****PLUGIN ENABLED*******");
	}

	/**
	 * When this plugin is disabled unregister our event handlers
	 */
	@Override
	public void onStop() {
		// unregister ourselves with the EventPublisher
		eventPublisher.unregister(this);
		log.debug("****PLUGIN DISABLED*******");
	}

}