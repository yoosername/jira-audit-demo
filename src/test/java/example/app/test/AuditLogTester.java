package example.app.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.log4j.Logger;

public class AuditLogTester{

	private final Logger logger = Logger.getLogger(AuditLogTester.class);

	private File log;
	public List<String> entries = new ArrayList<String>();
	TailerListener listener;
	Tailer tailer;
	private boolean tailing = false;

	public AuditLogTester(File log){
		this.log = log;

		// If file doesnt exists yet create it
		try {
			logger.debug("Tester created to tail the following log: " + log.getAbsolutePath());
			log.createNewFile();
		} catch (IOException e) {}

		listener = new AuditTailerListener();
		tailer = Tailer.create(log, listener, 0);
		
		logAllEntries();
	}

	public void emptyFileAndResetLog(){

		if(tailing){
			stopTailing();
		}

		try {
			// Empty the log file
			FileWriter fileOut = new FileWriter(log);
			fileOut.write("");
			fileOut.close();
		} catch (IOException e) {
			logger.debug("Error emptying log file: " + e.getMessage());
		}

		// Empty the stored log entries
		entries.clear();
		logger.debug("Log file was emptied and log reset");
		logAllEntries();
	}

	public void startTailing(){
		if(!tailing){
			new Thread(() -> {
				tailer.run();
			}).start();
		}
		tailing = true;
		logger.debug("Log tailing started");
	}

	public void stopTailing(){
		if(tailing){
			new Thread(() -> {
				tailer.stop();
			}).start();
		}
		tailing = false;
		logger.debug("Log tailing stopped");
	}

	public String getLogEntry(int at){
		String log = "";
		int actualEntryPosition = Math.min(at, entries.size()-1);
		log = entries.get(actualEntryPosition);
		logger.debug("Log at ("+at+") contains: " + log);	
		return log;
	}
	
	public String getLastLogEntry(){
		return getLastLogEntry(0);
	}
	
	public String getLastLogEntry(int increment){
		String log = "";
		if(entries.size() > 0 ){
			int lastEntryPosition = entries.size() -1; // will be zero or more here
			int lastEntryMinusIncrement = lastEntryPosition - increment; // take off the additional increment
			int actualEntryPosition = Math.max(lastEntryMinusIncrement, 0); // make sure its minimum of zero
			log = entries.get(actualEntryPosition);
		}
		logger.debug("Last log entry currently contains: " + log);	
		return log;
	}
	
	public void logAllEntries(){
		for(int n=0; n < entries.size(); n++){
			logger.debug("log entry at ("+n+") currently contains: " + entries.get(n));
		}
	}

	class AuditTailerListener extends TailerListenerAdapter{

		@Override
		public void handle(String line) {
			// Add to our entries
			entries.add(line);
		}

	}
}