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
	private String subStatus = "?";
	private long responseTime = -1;

	int countUp = 0;
	int countDown = 0;
	int countSuccess = 0;
	int countFailure = 0;

	public ServerData(ServerInfo info) {
		update(info);
		getIpInfo();
	}

	public void update(ServerInfo info) {
		if (info != null) {
			this.info = info;
			lastSeenDate = new Date();
			countUp++;
			status = "UP";
		} else {
			countDown++;
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

	public void reportTestFailure(String message) {
		this.subStatus = message;
		countFailure++;
	}

	public void reportTestSuccess(long responseTime) {
		this.subStatus = "OK";
		this.responseTime = responseTime;
		countSuccess++;
	}

	public String getStatusString() {
		return status + ", " + subStatus;
	}

	public String getResponseTimeString() {
		if (responseTime < 0) return "";
		return responseTime + "";
	}

	public String getUpRatioString() {
		if (countUp + countDown > 0) {
			return (((float) countUp / (countUp + countDown)) * 100) + "%";
		} else {
			return "?";
		}
	}

	public String getSuccessRatioString() {
		if (countSuccess + countFailure > 0) {
			return (((float) countSuccess / (countSuccess + countFailure)) * 100) + "%";
		} else {
			return "?";
		}
	}

}
