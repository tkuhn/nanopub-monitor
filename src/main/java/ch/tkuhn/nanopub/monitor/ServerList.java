package ch.tkuhn.nanopub.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.nanopub.Nanopub;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;

import com.google.common.collect.ImmutableList;
import com.opencsv.CSVReader;

public class ServerList implements Serializable {

	private static final long serialVersionUID = -6272932136983159574L;

	private static ServerList serverList;
	private static ServerIpInfo monitorIpInfo;

	public static ServerList get() {
		if (serverList == null) {
			serverList = new ServerList();
		}
		return serverList;
	}

	private static Map<NanopubService,ServerData> servers = new HashMap<NanopubService,ServerData>();

	private ServerList() {
		refresh();
	}

	public List<ServerData> getServerData() {
		return ImmutableList.copyOf(servers.values());
	}

	public List<ServerData> getSortedServerData() {
		List<ServerData> s = new ArrayList<ServerData>(servers.values());
		Collections.sort(s, new Comparator<ServerData>() {
			@Override
			public int compare(ServerData o1, ServerData o2) {
				if (o1.getIpInfo() == null || o1.getIpInfo().getIp() == null) return -1;
				if (o2.getIpInfo() == null || o2.getIpInfo().getIp() == null) return 1;
				if (o1.getIpInfo().getIp().equals(o2.getIpInfo().getIp())) {
					if (o1.getServerInfo() == null || o1.getServerInfo().getPublicUrl() == null ) return -1;
					if (o2.getServerInfo() == null || o2.getServerInfo().getPublicUrl() == null) return 1;
					return o1.getServerInfo().getPublicUrl().compareTo(o2.getServerInfo().getPublicUrl());
				}
				return o1.getIpInfo().getIp().compareTo(o2.getIpInfo().getIp());
			}
		});
		return s;
	}

	public int getServerCount() {
		return servers.size();
	}

	public ServerIpInfo getMonitorIpInfo() {
		if (monitorIpInfo == null) {
			try {
				monitorIpInfo = ServerData.fetchIpInfo("");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return monitorIpInfo;
	}

	public void refresh() {
		refreshFromNanopubServerPeers();
		refreshFromGrlc();
    }

	private static final ValueFactory vf = SimpleValueFactory.getInstance();
	private static final IRI nanopubServerTypeIri = vf.createIRI("https://github.com/tkuhn/nanopub-server#service");

	private void refreshFromNanopubServerPeers() {
		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			NanopubService s = new NanopubService(vf.createIRI(si.getPublicUrl()), nanopubServerTypeIri);
			try {
				if (servers.containsKey(s)) {
					servers.get(s).update(si);
				} else {
					servers.put(s, new ServerData(si));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				if (servers.containsKey(s)) {
					servers.get(s).update(null);
				}
			}
		}
	}

	private static RequestConfig requestConfig;
	static {
		requestConfig = RequestConfig.custom().setConnectTimeout(10000)
				.setConnectionRequestTimeout(100).setSocketTimeout(10000)
				.setCookieSpec(CookieSpecs.STANDARD).build();
	}

	private void refreshFromGrlc() {
		HttpGet get = new HttpGet("http://grlc.nanopubs.lod.labs.vu.nl/api/local/local/find_valid_signed_nanopubs_with_pattern?graphpred=http%3A%2F%2Fwww.nanopub.org%2Fnschema%23hasAssertion&pred=http%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type&obj=http%3A%2F%2Fpurl.org%2Fnanopub%2Fx%2FNanopubService");
		get.setHeader("Accept", "text/csv");
		try {
			HttpResponse resp = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build().execute(get);
			int c = resp.getStatusLine().getStatusCode();
			if (c < 200 && c >= 300) {
				EntityUtils.consumeQuietly(resp.getEntity());
				throw new IOException(resp.getStatusLine().toString());
			}
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
						Nanopub np = GetNanopub.get(line[0]);
						for (Statement st : np.getAssertion()) {
							if (!st.getPredicate().equals(RDF.TYPE)) continue;
							if (!(st.getObject() instanceof IRI)) continue; 
							if (st.getObject().stringValue().equals("http://purl.org/nanopub/x/NanopubService")) continue;
							NanopubService ns = new NanopubService((IRI) st.getSubject(), (IRI) st.getObject());
							System.err.println("Nanopub service discovered: " + ns);
						}
					}
				}
			} finally {
				if (csvReader != null) csvReader.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
