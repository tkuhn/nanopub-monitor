package ch.tkuhn.nanopub.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
