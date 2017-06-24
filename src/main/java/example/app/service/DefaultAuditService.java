package example.app.service;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

@ExportAsService({AuditService.class})
@Named("AuditService")
public class DefaultAuditService implements AuditService {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultAuditService.class);
	
	public DefaultAuditService(){
		// Nothing to do right now but examples could be
		// Configure specific event transformers etc from the admin page
	}

	@Override
	public void pushEvent(AuditableEvent event) {
		// For now lets just log the event as passed
		log.info(event.toString());
		
		// TODO: Batch up and send to predefined remotes.
		// E.g. elasticsearch, POST webhook as attached file
		// Should be able to define the following things
		// 	Batch size ( i.e. no of events before processing / default to something average like 100 )
		//  serializer. Which class to use to serialize the event stream
		//       example custom JSON, XML etc
		//  webhook address
		//  Whether data should be added to POST body or as file attachment.
	}
	
}
