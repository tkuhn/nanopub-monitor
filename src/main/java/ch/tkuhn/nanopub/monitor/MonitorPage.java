package ch.tkuhn.nanopub.monitor;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GLatLng;

import com.google.gson.Gson;


public class MonitorPage extends WebPage {

	private static final long serialVersionUID = -2069078890268133150L;

	private static List<ServerInfo> serverList;
	private static Map<String,IpInfo> serverIpinfo;

	public MonitorPage(final PageParameters parameters) throws Exception {
		super(parameters);
		loadServers();

		GMap map = new GMap("map");
		map.setStreetViewControlEnabled(false);
		map.setScaleControlEnabled(true);
		map.setScrollWheelZoomEnabled(true);
		List<GLatLng> points = new ArrayList<GLatLng>();
		for (IpInfo ipInfo : serverIpinfo.values()) {
			points.add(new GLatLng(ipInfo.latitude, ipInfo.longitude));
		}
		map.fitMarkers(points, true);
		add(map);

		add(new Label("server-count", serverList.size() + ""));
		long minNanopubCount = 0;
		for (ServerInfo serverInfo : serverList) {
			if (serverInfo.getNextNanopubNo()-1 > minNanopubCount) {
				minNanopubCount = serverInfo.getNextNanopubNo()-1;
			}
		}
		add(new Label("min-nanopub-count", minNanopubCount + ""));

		add(new DataView<ServerInfo>("rows", new ListDataProvider<ServerInfo>(serverList)) {

			private static final long serialVersionUID = 4703849210371741467L;

			public void populateItem(final Item<ServerInfo> item) {
				ServerInfo s = item.getModelObject();
				item.add(new Label("url", s.getPublicUrl()));
				item.add(new Label("admin", s.getAdmin()));
			}

		});
	}

	private void loadServers() throws Exception {
		if (serverList != null) return;
		serverList = new ArrayList<ServerInfo>();
		serverIpinfo = new HashMap<String,IpInfo>();

		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			try {
				ServerInfo serverInfo = ServerInfo.load(serverIterator.next());
				serverList.add(serverInfo);
				URL serverUrl = new URL(serverInfo.getPublicUrl());
				URL geoipUrl = new URL("http://freegeoip.net/json/" + serverUrl.getHost());
				IpInfo ipInfo = new Gson().fromJson(new InputStreamReader(geoipUrl.openStream()), IpInfo.class);
				serverIpinfo.put(serverInfo.getPublicUrl(), ipInfo);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
    }

	private static class IpInfo {
		public double latitude;
		public double longitude;
	}

}
