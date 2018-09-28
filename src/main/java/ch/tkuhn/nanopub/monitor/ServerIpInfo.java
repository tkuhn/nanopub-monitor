package ch.tkuhn.nanopub.monitor;

import java.io.Serializable;

public class ServerIpInfo implements Serializable {

	private static final long serialVersionUID = 4805668042976093282L;

	private Double lat = null;
	private Double lon = null;
	private String country = "unknown country";
	private String city = "unknown city";
	private String query = "unknown IP";

	public static ServerIpInfo empty = new ServerIpInfo();

	private ServerIpInfo() {
	}

	public Double getLatitude() {
		return lat;
	}

	public Double getLongitude() {
		return lon;
	}

	public String getCountryName() {
		return country;
	}

	public String getCity() {
		return city;
	}

	public String getIp() {
		return query;
	}
}
