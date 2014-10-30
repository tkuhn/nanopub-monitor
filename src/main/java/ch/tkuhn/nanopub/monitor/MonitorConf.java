package ch.tkuhn.nanopub.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MonitorConf {

	private static MonitorConf obj = new MonitorConf();

	public static MonitorConf get() {
		return obj;
	}

	private Properties conf;

	private MonitorConf() {
		conf = new Properties();
		InputStream in = MonitorConf.class.getResourceAsStream("conf.properties");
		try {
			conf.load(in);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		in = MonitorConf.class.getResourceAsStream("local.conf.properties");
		if (in != null) {
			try {
				conf.load(in);
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}

	public int getScanFreq() {
		return Integer.parseInt(conf.getProperty("scan-freq"));
	}

	public boolean showMap() {
		return Boolean.parseBoolean(conf.getProperty("show-map"));
	}

	public boolean isGeoIpInfoEnabled() {
		return Boolean.parseBoolean(conf.getProperty("get-geoip-info"));
	}

}
