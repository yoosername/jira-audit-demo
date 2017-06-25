package example.app.service;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

@ExportAsService({AuditService.class})
@Named("AuditService")
public class DefaultAuditService implements AuditService {

	private static final Logger log = LoggerFactory.getLogger(DefaultAuditService.class);

	private final String CSV_LINE_FORMAT = "${timestamp},${userId},${type},${typeDescription},${url},${isAdminOnlyAction},${isDestructiveAction},${isAnonymousAction},${isContentAffectedAction},${contentID}";
	private final String JSON_LINE_FORMAT = "{\"timestamp\":${timestamp},\"userId\":${userId},\"type\":${type},\"typeDescription\":${typeDescription},\"url\":${url},\"isAdminOnly\":${isAdminOnlyAction},\"isDestructive\":${isDestructiveAction},\"isAnon\":${isAnonymousAction},\"hasContent\":${isContentAffectedAction},\"contentId\":${contentID}}";
	private final String XML_LINE_FORMAT = "<event><timestamp>${timestamp}</timestamp><userId>${userId}</userId><type>${type}</type><url>${url}</url></event>";

	public DefaultAuditService(){
		// Handle events as they come in.
		// E.g. log them to a file, POST to a webhook etc.
	}

	@Override
	public void handleEvent(AuditableEvent event) {

		// Log the event
		// Optional: Format the event using simple string interpolation
		log.info(event.format(CSV_LINE_FORMAT));
		log.info(event.format(JSON_LINE_FORMAT));
		log.info(event.format(XML_LINE_FORMAT));

		// TODO: ability to POST to webhook in customisable batch sizes and using custom string templates.

	}

}
