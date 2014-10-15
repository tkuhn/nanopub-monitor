package ch.tkuhn.nanopub.monitor;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
	private static Map<String,ServerIpInfo> serverIpInfos;

	private ServerList() {
		loadServers();
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
		return serverIpInfos.get(si.getPublicUrl());
	}

	private void loadServers() {
		serverInfos = new ArrayList<ServerInfo>();
		serverIpInfos = new HashMap<String,ServerIpInfo>();

		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			try {
				ServerInfo serverInfo = ServerInfo.load(serverIterator.next());
				URL serverUrl = new URL(serverInfo.getPublicUrl());
				URL geoipUrl = new URL("http://freegeoip.net/json/" + serverUrl.getHost());
				ServerIpInfo ipInfo = new Gson().fromJson(new InputStreamReader(geoipUrl.openStream()), ServerIpInfo.class);
				serverInfos.add(serverInfo);
				serverIpInfos.put(serverInfo.getPublicUrl(), ipInfo);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
    }

}
