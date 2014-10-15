package ch.tkuhn.nanopub.monitor;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

public class ServerList implements Serializable {

	private static final long serialVersionUID = -6272932136983159574L;

	private static ServerList serverList;

	public static ServerList get() {
		if (serverList == null) {
			serverList = new ServerList();
		}
		return serverList;
	}

	private static List<ServerInfo> serverInfos;
	private static Map<String,ServerIpInfo> serverIpInfos = new HashMap<String,ServerIpInfo>();
	private static Map<String,Date> lastSeen = new HashMap<String,Date>();

	private ServerList() {
		refresh();
		for (ServerInfo si : serverInfos) {
			getServerIpInfo(si);
		}
	}

	public List<ServerInfo> getServerInfos() {
		return ImmutableList.copyOf(serverInfos);
	}

	public int getServerCount() {
		return serverInfos.size();
	}

	public Collection<ServerIpInfo> getServerIpInfos() {
		return serverIpInfos.values();
	}

	public ServerIpInfo getServerIpInfo(ServerInfo si) {
		if (!serverIpInfos.containsKey(si.getPublicUrl())) {
			try {
				URL serverUrl = new URL(si.getPublicUrl());
				URL geoipUrl = new URL("http://freegeoip.net/json/" + serverUrl.getHost());
				ServerIpInfo ipInfo = new Gson().fromJson(new InputStreamReader(geoipUrl.openStream()), ServerIpInfo.class);
				serverIpInfos.put(si.getPublicUrl(), ipInfo);
			} catch (Exception ex) {
				ex.printStackTrace();
				return ServerIpInfo.empty;
			}
		}
		return serverIpInfos.get(si.getPublicUrl());
	}

	public Date getLastSeenDate(ServerInfo si) {
		return lastSeen.get(si.getPublicUrl());
	}

	public void refresh() {
		List<ServerInfo> si = new ArrayList<ServerInfo>();

		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			try {
				ServerInfo serverInfo = ServerInfo.load(serverIterator.next());
				si.add(serverInfo);
				serverInfos = si;
				lastSeen.put(serverInfo.getPublicUrl(), new Date());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
    }

}
