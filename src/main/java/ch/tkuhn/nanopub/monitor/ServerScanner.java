package ch.tkuhn.nanopub.monitor;

import java.io.InputStream;
import java.util.Random;

import net.trustyuri.TrustyUriUtils;

import org.apache.commons.lang.time.StopWatch;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.wicket.util.thread.ICode;
import org.apache.wicket.util.thread.Task;
import org.apache.wicket.util.time.Duration;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.nanopub.extra.server.NanopubServerUtils;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.trusty.TrustyNanopubUtils;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;

public class ServerScanner implements ICode {

	private static ServerScanner singleton;
	private static Random random = new Random();
	private Logger logger;

	public static void initDaemon() {
		if (singleton != null) return;
		Task scanTask = new Task("server-scanner");
		scanTask.setDaemon(true);
		singleton = new ServerScanner();
		scanTask.run(Duration.seconds(MonitorConf.get().getScanFreq()), singleton);
	}

	private ServerScanner() {
	}

	@Override
	public void run(Logger logger) {
		this.logger = logger;
		logger.info("Scan servers...");
		ServerList.get().refresh();
		testServers();
	}

	private void testServers() {
		for (ServerData d : ServerList.get().getServerData()) {
			logger.info("Testing server " + d.getServerInfo().getPublicUrl() + "...");
			ServerInfo i = d.getServerInfo();
			if (i.getNextNanopubNo() == 0) continue;
			try {
				long npNo = (long) (random.nextDouble() * (i.getNextNanopubNo()));
				logger.info("Trying to retrieve nanopub number " + npNo);
				int pageNo = (int) (npNo / i.getPageSize());
				int rowNo = (int) (npNo % i.getPageSize());
				int r = 0;
				for (String nanopubUri : NanopubServerUtils.loadNanopubUriList(i, pageNo)) {
					if (rowNo < r) {
						r++;
						continue;
					}
					String ac = TrustyUriUtils.getArtifactCode(nanopubUri);
					HttpGet get = new HttpGet(i.getPublicUrl() + ac);
					get.setHeader("Accept", "application/trig");
					StopWatch watch = new StopWatch();
					watch.start();
					HttpResponse resp = HttpClientBuilder.create().build().execute(get);
					watch.stop();
					logger.info("Response time in milliseconds: " + watch.getTime());
					if (!wasSuccessful(resp)) {
						logger.info("Test failed. HTTP code " + resp.getStatusLine().getStatusCode());
						d.reportTestFailure("INACCESSIBLE");
					} else {
						InputStream in = resp.getEntity().getContent();
						Nanopub np = new NanopubImpl(in, RDFFormat.TRIG);
						if (TrustyNanopubUtils.isValidTrustyNanopub(np)) {
							logger.info("Test succeeded on nanopub: " + np.getUri());
							d.reportTestSuccess(watch.getTime());
						} else {
							logger.info("Test failed. Not a trusty nanopub: " + np.getUri());
							d.reportTestFailure("BROKEN");
						}
					}
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private boolean wasSuccessful(HttpResponse resp) {
		int c = resp.getStatusLine().getStatusCode();
		return c >= 200 && c < 300;
	}

}
