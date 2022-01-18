package ch.tkuhn.nanopub.monitor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.nanopub.extra.server.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ServerData implements Serializable {

	private static final long serialVersionUID = 1383338443824756632L;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private NanopubService service;
	private Object info;
	private ServerIpInfo ipInfo;
	private long lastIpInfoRetrieval;
	private Date lastSeenOk;
	private String status = "NOT SEEN";
	private String distanceString = null;
	private long totalResponseTime = 0;

	int countSuccess = 0;
	int countFailure = 0;

	public ServerData(NanopubService service, Object info) {
		this.service = service;
		update(info);
		getIpInfo();
	}

	public void update(Object info) {
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
		if (now - lastIpInfoRetrieval > 1000 * 60 * 10) {
			// retry every 10 minutes
			loadIpInfo();
		}
	}

	public NanopubService getService() {
		return service;
	}

	public String getServiceId() {
		return service.getServiceIri().stringValue();
	}

	public boolean hasServiceType(IRI type) {
		return service.getTypeIri().equals(type);
	}

	public Object getServerInfo() {
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
			ipInfo = fetchIpInfo(new URL(service.getServiceIri().stringValue()).getHost());
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
		logger.info("Test result: " + service.getServiceIri() + " " + getStatusString());
	}

	public void reportTestSuccess(long responseTime) {
		lastSeenOk = new Date();
		status = "OK";
		totalResponseTime += responseTime;
		countSuccess++;
		logger.info("Test result: " + service.getServiceIri() + " " + getStatusString() + " " + responseTime + "ms");
	}

	public String getStatusString() {
		return status;
	}

	public Integer getAvgResponseTimeInMs() {
		if (countSuccess == 0) return null;
		return (int) (totalResponseTime / (float) countSuccess);
	}

	public String getAvgResponseTimeString() {
		Integer respTime = getAvgResponseTimeInMs();
		if (respTime == null) {
			return "?";
		} else {
			return respTime + " ms";
		}
	}

	public Float getSuccessRatio() {
		if (countSuccess + countFailure > 0) {
			return (float) countSuccess / (countSuccess + countFailure);
		} else {
			return null;
		}
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
			Integer distKm = getDistanceInKm();
			if (distKm == null) {
				distanceString = "?";
			} else {
				distanceString = distKm + " km";
			}
		}
		return distanceString;
	}

	public Integer getDistanceInKm() {
		ServerIpInfo monitorIpInfo = ServerList.get().getMonitorIpInfo();
		if (monitorIpInfo == null) return null;
		ServerIpInfo serverIpInfo = getIpInfo();
		Double sLat = serverIpInfo.getLatitude();
		Double sLng = serverIpInfo.getLongitude();
		if (sLat == null || sLng == null) return null;
		return (int) calculateDistance(sLat, sLng, monitorIpInfo.getLatitude(), monitorIpInfo.getLongitude());
	}

	public String getParameterString() {
		if (info instanceof ServerInfo) {
			ServerInfo si = (ServerInfo) info;
			String s = " / ";
			if (si.getUriPattern() != null) s = si.getUriPattern() + s;
			if (si.getHashPattern() != null) s = s + si.getHashPattern();
			return s;
		}
		return "";
	}

	public String getDescription() {
		if (info instanceof ServerInfo) {
			return ((ServerInfo) info).getDescription();
		}
		return "";
	}

	private static Map<String,ServerIpInfo> ipInfoMap = new HashMap<>();

	public static ServerIpInfo fetchIpInfo(String host) throws IOException {
		if (!MonitorConf.get().isGeoIpInfoEnabled()) return ServerIpInfo.empty;
		if (ipInfoMap.containsKey(host)) return ipInfoMap.get(host);
		ServerIpInfo serverIpInfo = null;
		URL geoipUrl = new URL("http://ip-api.com/json/" + host);
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) geoipUrl.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			serverIpInfo = new Gson().fromJson(new InputStreamReader(con.getInputStream()), ServerIpInfo.class);
			ipInfoMap.put(host, serverIpInfo);
		} finally {
			if (con != null) con.disconnect();
		}
		return serverIpInfo;
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
