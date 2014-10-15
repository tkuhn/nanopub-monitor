package ch.tkuhn.nanopub.monitor;

public class ServerIpInfo {

	private Double latitude;
	private Double longitude;
	private String country_name;
	private String city;

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
