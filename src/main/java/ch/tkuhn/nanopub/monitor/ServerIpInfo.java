package ch.tkuhn.nanopub.monitor;

public class ServerIpInfo {

	private double latitude;
	private double longitude;
	private String country_name;
	private String city;

	private ServerIpInfo() {
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getCountryName() {
		return country_name;
	}

	public String getCity() {
		return city;
	}

}
