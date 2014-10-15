package ch.tkuhn.nanopub.monitor;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.nanopub.extra.server.ServerInfo;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GLatLng;

public class MonitorPage extends WebPage {

	private static final long serialVersionUID = -2069078890268133150L;

	public MonitorPage(final PageParameters parameters) throws Exception {
		super(parameters);
		final ServerList sl = ServerList.get();

		GMap map = new GMap("map");
		map.setStreetViewControlEnabled(false);
		map.setScaleControlEnabled(true);
		map.setScrollWheelZoomEnabled(true);
		List<GLatLng> points = new ArrayList<GLatLng>();
		for (ServerIpInfo ipInfo : sl.getServerIpInfos()) {
			points.add(new GLatLng(ipInfo.getLatitude(), ipInfo.getLongitude()));
		}
		map.fitMarkers(points, true);
		add(map);

		add(new Label("server-count", sl.getServerCount() + ""));
		long minNanopubCount = 0;
		for (ServerInfo serverInfo : sl.getServerInfos()) {
			if (serverInfo.getNextNanopubNo()-1 > minNanopubCount) {
				minNanopubCount = serverInfo.getNextNanopubNo()-1;
			}
		}
		add(new Label("min-nanopub-count", minNanopubCount + ""));

		add(new DataView<ServerInfo>("rows", new ListDataProvider<ServerInfo>(sl.getServerInfos())) {

			private static final long serialVersionUID = 4703849210371741467L;

			public void populateItem(final Item<ServerInfo> item) {
				ServerInfo s = item.getModelObject();
				ServerIpInfo i = sl.getServerIpInfo(s);
				ExternalLink urlLink = new ExternalLink("urllink", s.getPublicUrl());
				urlLink.add(new Label("url", s.getPublicUrl()));
				item.add(urlLink);
				item.add(new Label("nanopubcount", s.getNextNanopubNo()-1));
				item.add(new Label("lastseen", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(sl.getLastSeenDate(s))));
				item.add(new Label("location", i.getCity() + ", " + i.getCountryName()));
				item.add(new Label("admin", s.getAdmin()));
			}

		});

		ServerScanner.initDaemon();
	}

}
