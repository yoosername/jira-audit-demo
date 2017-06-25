package example.app.service;

public interface AuditService {

	public void handleEvent(AuditableEvent event);
	
}
