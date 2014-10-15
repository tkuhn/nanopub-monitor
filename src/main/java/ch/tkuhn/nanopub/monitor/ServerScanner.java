package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.util.thread.ICode;
import org.apache.wicket.util.thread.Task;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;

public class ServerScanner implements ICode {

	private static ServerScanner singleton;

	public static void initDaemon() {
		if (singleton != null) return;
		Task scanTask = new Task("server-scanner");
		scanTask.setDaemon(true);
		singleton = new ServerScanner();
		// TODO move frequency specification to config file
		scanTask.run(Duration.seconds(60), singleton);
	}

	private ServerScanner() {
	}

	@Override
	public void run(Logger logger) {
		logger.info("Scan servers...");
		ServerList.get().refresh();
	}

}
