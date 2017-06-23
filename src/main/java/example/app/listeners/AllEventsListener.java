package example.app.listeners;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.user.ApplicationUser;
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

	/**
	 * Constructor.
	 * @param eventPublisher injected {@code EventPublisher} implementation.
	 */
	@Autowired
	public AllEventsListener(
			@ComponentImport EventPublisher eventPublisher, 
			AuditService auditService,
			@ComponentImport EventTypeManager eventTypeManager) 
	{
		this.eventPublisher = eventPublisher;
		this.auditService = auditService;
		this.eventTypeManager = eventTypeManager;

		eventPublisher.register(this);
	}

	/**
	 * Called when the plugin has been enabled.
	 * @throws Exception
	 */
	@Override
	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		// register ourselves with the EventPublisher
		log.debug("****PLUGIN ENABLED*******");
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
		log.debug("****PLUGIN DISABLED*******");
	}

	/**
	 * Receives any {@code IssueEvent}s sent by JIRA.
	 * @param issueEvent the IssueEvent passed to us
	 */
	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) {

		Long eventTypeId = issueEvent.getEventTypeId();
		ApplicationUser user = issueEvent.getUser();
		Date timestamp = issueEvent.getTime();

		auditService.pushEvent(
			new AuditableEvent(eventTypeManager)
				.withUser(user)
				.withType(eventTypeId)
				.withContentID(issueEvent.getIssue().getId())
				.at(timestamp)
		);

	}

	@Override
	public void onStart() {
		// register ourselves with the EventPublisher
		log.debug("****PLUGIN ENABLED*******");
		eventPublisher.register(this);
	}

	@Override
	public void onStop() {
		// unregister ourselves with the EventPublisher
		eventPublisher.unregister(this);
		log.debug("****PLUGIN DISABLED*******");
	}
}