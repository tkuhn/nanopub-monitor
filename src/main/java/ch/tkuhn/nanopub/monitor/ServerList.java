package ch.tkuhn.nanopub.monitor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;

import com.google.common.collect.ImmutableList;

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

	private static Map<String,ServerData> servers = new HashMap<String,ServerData>();

	private ServerList() {
		refresh();
	}

	public List<ServerData> getServerData() {
		return ImmutableList.copyOf(servers.values());
	}

	public ServerData getServerData(String serverUrl) {
		return servers.get(serverUrl);
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
		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			String url = si.getPublicUrl();
			try {
				if (servers.containsKey(url)) {
					servers.get(url).update(si);
				} else {
					servers.put(url, new ServerData(si));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				if (servers.containsKey(url)) {
					servers.get(url).update(null);
				}
			}
		}
    }

}
