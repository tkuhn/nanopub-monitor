package ch.tkuhn.nanopub.monitor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import org.nanopub.extra.server.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ServerData implements Serializable {

	private static final long serialVersionUID = 1383338443824756632L;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ServerInfo info;
	private ServerIpInfo ipInfo;
	private long lastIpInfoRetrieval;
	private Date lastSeenOk;
	private String status = "NOT SEEN";
	private String distanceString = null;
	private long totalResponseTime = 0;

	int countSuccess = 0;
	int countFailure = 0;

	public ServerData(ServerInfo info) {
		update(info);
		getIpInfo();
	}

	public void update(ServerInfo info) {
		if (info != null) {
			this.info = info;
		}
		ensureIpInfoLoaded();
	}

	private void ensureIpInfoLoaded() {
		if (ipInfo != null && ipInfo != ServerIpInfo.empty) {
			// already loaded
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastIpInfoRetrieval > 1000 * 60 * 60 * 12) {
			// retry every 12 hours
			loadIpInfo();
		}
	}

	public ServerInfo getServerInfo() {
		return info;
	}

	public ServerIpInfo getIpInfo() {
		if (ipInfo == null) {
			loadIpInfo();
		}
		return ipInfo;
	}

	private void loadIpInfo() {
		lastIpInfoRetrieval = System.currentTimeMillis();
		try {
			ipInfo = fetchIpInfo(new URL(info.getPublicUrl()).getHost());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			if (ipInfo == null) {
				ipInfo = ServerIpInfo.empty;
			}
		}
	}

	public Date getLastSeenDate() {
		return lastSeenOk;
	}

	public void reportTestFailure(String message) {
		status = message;
		countFailure++;
		logger.info("Test result: " + info.getPublicUrl() + " " + getStatusString());
	}

	public void reportTestSuccess(long responseTime) {
		lastSeenOk = new Date();
		status = "OK";
		totalResponseTime += responseTime;
		countSuccess++;
		logger.info("Test result: " + info.getPublicUrl() + " " + getStatusString() + " " + responseTime + "ms");
	}

	public String getStatusString() {
		return status;
	}

	public String getAvgResponseTimeString() {
		if (countSuccess == 0) return "?";
		return (int) (totalResponseTime / (float) countSuccess) + " ms";
	}

	public String getSuccessRatioString() {
		if (countSuccess + countFailure > 0) {
			return (((float) countSuccess / (countSuccess + countFailure)) * 100) + "%";
		} else {
			return "?";
		}
	}

	public String getDistanceString() {
		if (distanceString == null) {
			ServerIpInfo monitorIpInfo = ServerList.get().getMonitorIpInfo();
			if (monitorIpInfo == null) return "?";
			ServerIpInfo serverIpInfo = getIpInfo();
			Double sLat = serverIpInfo.getLatitude();
			Double sLng = serverIpInfo.getLongitude();
			if (sLat == null || sLng == null) return "?";
			int distKm = (int) calculateDistance(sLat, sLng, monitorIpInfo.getLatitude(), monitorIpInfo.getLongitude());
			distanceString = distKm + " km";
		}
		return distanceString;
	}

	public static ServerIpInfo fetchIpInfo(String host) throws IOException {
		if (!MonitorConf.get().isGeoIpInfoEnabled()) return ServerIpInfo.empty;
		URL geoipUrl = new URL("http://freegeoip.net/json/" + host);
		return new Gson().fromJson(new InputStreamReader(geoipUrl.openStream()), ServerIpInfo.class);
	}

	private static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371.0;
		double latD = Math.toRadians(lat2 - lat1);
		double lngD = Math.toRadians(lng2 - lng1);
		double sinLatD = Math.sin(latD / 2);
		double sinLngD = Math.sin(lngD / 2);
		double a = Math.pow(sinLatD, 2) + Math.pow(sinLngD, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return earthRadius * c;
	}

}
