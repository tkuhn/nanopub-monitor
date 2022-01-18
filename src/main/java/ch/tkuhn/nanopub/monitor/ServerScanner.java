package ch.tkuhn.nanopub.monitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Random;

import org.apache.commons.lang.time.StopWatch;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.wicket.util.thread.ICode;
import org.apache.wicket.util.thread.Task;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.nanopub.extra.server.NanopubServerUtils;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.trusty.TrustyNanopubUtils;
import org.slf4j.Logger;

import com.opencsv.CSVReader;

import net.trustyuri.TrustyUriUtils;

public class ServerScanner implements ICode {

	private static ServerScanner singleton;
	private static Task scanTask;
	private static Random random = new Random();

	public static void initDaemon() {
		if (singleton != null) {
			if (singleton.aliveAtTime + 10 * 60 * 1000 < System.currentTimeMillis()) {
				singleton.logger.info("No sign of life of the daemon for 10 minutes. Starting new one.");
				singleton = null;
				scanTask.interrupt();
			} else {
				return;
			}
		}
		scanTask = new Task("server-scanner");
		scanTask.setDaemon(true);
		singleton = new ServerScanner();
		scanTask.run(Duration.ofSeconds(MonitorConf.get().getScanFreq()), singleton);
	}

	private Logger logger;
	private long aliveAtTime;

	private ServerScanner() {
		stillAlive();
	}

	@Override
	public void run(Logger logger) {
		this.logger = logger;
		logger.info("Scan servers...");
		ServerList.get().refresh();
		stillAlive();
		testServers();
	}

	private void testServers() {
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();
		HttpClient c = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		for (ServerData d : ServerList.get().getServerData()) {
			logger.info("Testing server " + d.getServiceId() + "...");
			stillAlive();
			if (d.hasServiceType(NanopubService.NANOPUB_SERVER_TYPE_IRI)) {
				ServerInfo i = (ServerInfo) d.getServerInfo();
				if (i.getNextNanopubNo() == 0) continue;
				try {
					long npNo = (long) (random.nextDouble() * (i.getNextNanopubNo()));
					logger.info("Trying to retrieve nanopub number " + npNo);
					int pageNo = (int) (npNo / i.getPageSize()) + 1;
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
						HttpResponse resp = c.execute(get);
						watch.stop();
						if (!wasSuccessful(resp)) {
							logger.info("Test failed. HTTP code " + resp.getStatusLine().getStatusCode());
							d.reportTestFailure("DOWN");
						} else {
							InputStream in = resp.getEntity().getContent();
							Nanopub np = new NanopubImpl(in, RDFFormat.TRIG);
							if (TrustyNanopubUtils.isValidTrustyNanopub(np)) {
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
					d.reportTestFailure("INACCESSIBLE");
				}
			} else if (d.hasServiceType(NanopubService.GRLC_SERVICE_TYPE_IRI)) {
				logger.info("Trying to access " + d.getServiceId() + "get_nanopub_count...");
				try {
					HttpGet get = new HttpGet(d.getServiceId() + "get_nanopub_count");
					get.setHeader("Accept", "text/csv");
					StopWatch watch = new StopWatch();
					watch.start();
					HttpResponse resp = c.execute(get);
					watch.stop();
					if (!wasSuccessful(resp)) {
						logger.info("Test failed. HTTP code " + resp.getStatusLine().getStatusCode());
						d.reportTestFailure("DOWN");
					} else {
						CSVReader csvReader = null;
						try {
							csvReader = new CSVReader(new BufferedReader(new InputStreamReader(resp.getEntity().getContent())));
							String[] line = null;
							int n = 0;
							while ((line = csvReader.readNext()) != null) {
								n++;
								if (n == 1) {
									// ignore header line
								} else {
									if (line[0].matches("[0-9]{8,}")) {
										d.reportTestSuccess(watch.getTime());
									} else {
										d.reportTestFailure("BROKEN");
									}
									break;
								}
							}
						} catch (Exception ex) {
							logger.info("Test failed. Exception: " + ex.getMessage());
							d.reportTestFailure("BROKEN");
						} finally {
							if (csvReader != null) csvReader.close();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					d.reportTestFailure("INACCESSIBLE");
				}
			} else if (d.hasServiceType(NanopubService.LDF_SERVICE_TYPE_IRI)) {
				logger.info("Trying to access " + d.getServiceId() + "?object=http%3A%2F%2Fwww.nanopub.org%2Fnschema%23Nanopublication...");
				try {
					HttpGet get = new HttpGet(d.getServiceId() + "?object=http%3A%2F%2Fwww.nanopub.org%2Fnschema%23Nanopublication");
					get.setHeader("Accept", "application/n-quads");
					StopWatch watch = new StopWatch();
					watch.start();
					HttpResponse resp = c.execute(get);
					watch.stop();
					if (!wasSuccessful(resp)) {
						logger.info("Test failed. HTTP code " + resp.getStatusLine().getStatusCode());
						d.reportTestFailure("DOWN");
					} else {
						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
							String line = null;
							int count = 0;
							while ((line = reader.readLine()) != null) {
								if (line.contains(" <http://www.nanopub.org/nschema#Nanopublication> ")) count = count + 1;
							}
							if (count >= 100) {
								d.reportTestSuccess(watch.getTime());
							} else {
								d.reportTestFailure("BROKEN");
							}
						} catch (Exception ex) {
							logger.info("Test failed. Exception: " + ex.getMessage());
							d.reportTestFailure("BROKEN");
						} finally {
							if (reader != null) reader.close();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					d.reportTestFailure("INACCESSIBLE");
				}
			} else if (d.hasServiceType(NanopubService.NANOPUB_MONITOR_TYPE_IRI)) {
				logger.info("Trying to access " + d.getServiceId() + ".csv...");
				try {
					HttpGet get = new HttpGet(d.getServiceId() + ".csv");
					get.setHeader("Accept", "text/csv");
					StopWatch watch = new StopWatch();
					watch.start();
					HttpResponse resp = c.execute(get);
					watch.stop();
					if (!wasSuccessful(resp)) {
						logger.info("Test failed. HTTP code " + resp.getStatusLine().getStatusCode());
						d.reportTestFailure("DOWN");
					} else {
						CSVReader csvReader = null;
						try {
							csvReader = new CSVReader(new BufferedReader(new InputStreamReader(resp.getEntity().getContent())));
							String[] line = null;
							int n = 0;
							while ((line = csvReader.readNext()) != null) {
								n++;
								if (n == 1) {
									// ignore header line
								} else {
									if (line[0].startsWith("http")) {
										d.reportTestSuccess(watch.getTime());
									} else {
										d.reportTestFailure("BROKEN");
									}
									break;
								}
							}
						} catch (Exception ex) {
							logger.info("Test failed. Exception: " + ex.getMessage());
							d.reportTestFailure("BROKEN");
						} finally {
							if (csvReader != null) csvReader.close();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					d.reportTestFailure("INACCESSIBLE");
				}
			} else {
				logger.info("Trying to access " + d.getServiceId() + "...");
				try {
					HttpGet get = new HttpGet(d.getServiceId());
					StopWatch watch = new StopWatch();
					watch.start();
					HttpResponse resp = c.execute(get);
					watch.stop();
					if (!wasSuccessful(resp)) {
						logger.info("Test failed. HTTP code " + resp.getStatusLine().getStatusCode());
						d.reportTestFailure("DOWN");
					} else {
						d.reportTestSuccess(watch.getTime());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					d.reportTestFailure("INACCESSIBLE");
				}
			}
		}
	}

	private boolean wasSuccessful(HttpResponse resp) {
		int c = resp.getStatusLine().getStatusCode();
		return c >= 200 && c < 300;
	}

	private void stillAlive() {
		aliveAtTime = System.currentTimeMillis();
	}

}
