package example.app.services;

import java.util.List;

import example.app.models.AuditableEvent;

public interface AuditService {

	public void handleEvent(AuditableEvent event);
	public String formatEvent(AuditableEvent event, String format);
	public void logEvent(String event);
	public List<AuditableEvent> getEvents();
	public AuditableEvent getLastEvent();
	public void flushEvents();
	
}
