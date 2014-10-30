package ch.tkuhn.nanopub.monitor;

import java.io.Serializable;

public class ServerIpInfo implements Serializable {

	private static final long serialVersionUID = 4805668042976093282L;

	private Double latitude = 0.0;
	private Double longitude = 0.0;
	private String country_name = "unknown country";
	private String city = "unknown city";

	public static ServerIpInfo empty = new ServerIpInfo();

	private ServerIpInfo() {
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getCountryName() {
		return country_name;
	}

	public String getCity() {
		return city;
	}

}
