package ch.tkuhn.nanopub.monitor;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import org.nanopub.extra.server.ServerInfo;

import com.google.gson.Gson;

public class ServerData implements Serializable {

	private static final long serialVersionUID = 1383338443824756632L;

	private ServerInfo info;
	private ServerIpInfo ipInfo;
	private Date lastSeenDate;
	private String status = "NOT SEEN";

	public ServerData(ServerInfo info) {
		update(info);
		getIpInfo();
	}

	public void update(ServerInfo info) {
		if (info != null) {
			this.info = info;
			lastSeenDate = new Date();
			status = "OK";
		} else {
			status = "DOWN";
		}
	}

	public ServerInfo getServerInfo() {
		return info;
	}

	public ServerIpInfo getIpInfo() {
		if (ipInfo == null) {
			try {
				URL serverUrl = new URL(info.getPublicUrl());
				URL geoipUrl = new URL("http://freegeoip.net/json/" + serverUrl.getHost());
				ipInfo = new Gson().fromJson(new InputStreamReader(geoipUrl.openStream()), ServerIpInfo.class);
			} catch (Exception ex) {
				ex.printStackTrace();
				return ServerIpInfo.empty;
			}
		}
		return ipInfo;
	}

	public Date getLastSeenDate() {
		return lastSeenDate;
	}

	public String getStatus() {
		return status;
	}

}
