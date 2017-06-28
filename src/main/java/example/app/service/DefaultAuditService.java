package example.app.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.apache.log4j.Logger;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

@ExportAsService({AuditService.class})
@Named("AuditService")
public class DefaultAuditService implements AuditService {

	private final Logger log = Logger.getLogger(DefaultAuditService.class);
	private List<AuditableEvent> events;

	public final static String CSV_LINE_FORMAT = "${timestamp},${userId},${type},${typeDescription},${referer},${url},${isAdminOnlyAction},${isDestructiveAction},${isAnonymousAction},${isContentAffectedAction},${contentID}";
	public final static String JSON_LINE_FORMAT = "{\"timestamp\":${timestamp},\"userId\":${userId},\"type\":${type},\"typeDescription\":${typeDescription},\"referer\":${referer},\"url\":${url},\"isAdminOnly\":${isAdminOnlyAction},\"isDestructive\":${isDestructiveAction},\"isAnon\":${isAnonymousAction},\"hasContent\":${isContentAffectedAction},\"contentId\":${contentID}}";
	public final static String XML_LINE_FORMAT = "<event><timestamp>${timestamp}</timestamp><userId>${userId}</userId><type>${type}</type><referer>${referer}</referer><url>${url}</url></event>";

	public DefaultAuditService(){
		// Handle events as they come in.
		events = new ArrayList<AuditableEvent>();
	}

	@Override
	public void handleEvent(AuditableEvent event) {

		// Store the event
		events.add(event);

		// Log the event
		logEvent(formatEvent(event, CSV_LINE_FORMAT));

		// TODO: POST to webhook ( batch / template )

	}

	@Override
	public String formatEvent(AuditableEvent event, String format){
		return event.format(format);
	}

	@Override
	public void logEvent(String event){
		log.info(event);
	}

	@Override
	public List<AuditableEvent> getEvents(){
		return this.events;
	}

	@Override
	public void flushEvents() {
		this.events = new ArrayList<AuditableEvent>();
	}

	@Override
	public AuditableEvent getLastEvent() {
		return this.events.get(events.size());
	}

}
