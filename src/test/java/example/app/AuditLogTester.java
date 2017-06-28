package example.app;

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
		
		logger.debug("Contents of log are: \"" + getLastLogEntry() + "\"");
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
		} catch (IOException e) {}

		// Empty the stored log entries
		entries.clear();
	}

	public void startTailing(){
		if(!tailing){
			new Thread(() -> {
				tailer.run();
			}).start();
		}
		tailing = true;
	}

	public void stopTailing(){
		if(tailing){
			new Thread(() -> {
				tailer.stop();
			}).start();
		}
		tailing = false;
	}

	public String getLastLogEntry(){
		String log = "";
		if(entries.size() > 0 ){
			log = entries.get(entries.size()-1);
		}
		logger.debug("Log currently contains: " + log);	
		return log;
	}

	class AuditTailerListener extends TailerListenerAdapter{

		@Override
		public void handle(String line) {
			// Add to our entries
			entries.add(line);
		}

	}
}