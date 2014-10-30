package ch.tkuhn.nanopub.monitor;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.nanopub.extra.server.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GLatLng;

public class MonitorPage extends WebPage {

	private static final long serialVersionUID = -2069078890268133150L;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public MonitorPage(final PageParameters parameters) throws Exception {
		super(parameters);
		final ServerList sl = ServerList.get();

		if (MonitorConf.get().showMap()) {
			GMap map = new GMap("map");
			map.setStreetViewControlEnabled(false);
			map.setScaleControlEnabled(true);
			map.setScrollWheelZoomEnabled(true);
			List<GLatLng> points = new ArrayList<GLatLng>();
			for (ServerData sd : sl.getServerData()) {
				try {
					ServerIpInfo ipInfo = sd.getIpInfo();
					points.add(new GLatLng(ipInfo.getLatitude(), ipInfo.getLongitude()));
				} catch (Exception ex) {
					logger.error("Something went wrong while getting coordinates", ex);
				}
			}
			map.fitMarkers(points, true);
			add(map);
		} else {
			add(new Label("map"));
		}

		add(new Label("server-count", sl.getServerCount() + ""));
		long minNanopubCount = 0;
		for (ServerData sd : sl.getServerData()) {
			ServerInfo serverInfo = sd.getServerInfo();
			if (serverInfo.getNextNanopubNo() > minNanopubCount) {
				minNanopubCount = serverInfo.getNextNanopubNo();
			}
		}
		add(new Label("min-nanopub-count", minNanopubCount + ""));

		add(new DataView<ServerData>("rows", new ListDataProvider<ServerData>(sl.getServerData())) {

			private static final long serialVersionUID = 4703849210371741467L;

			public void populateItem(final Item<ServerData> item) {
				ServerData d = item.getModelObject();
				ServerInfo s = d.getServerInfo();
				ServerIpInfo i = d.getIpInfo();
				ExternalLink urlLink = new ExternalLink("urllink", s.getPublicUrl());
				urlLink.add(new Label("url", s.getPublicUrl()));
				item.add(urlLink);
				item.add(new Label("status", d.getStatusString()));
				item.add(new Label("upratio", d.getUpRatioString()));
				item.add(new Label("successratio", d.getSuccessRatioString()));
				item.add(new Label("resptime", d.getAvgResponseTimeString()));
				item.add(new Label("dist", d.getDistanceString()));
				item.add(new Label("lastseen", formatDate(d.getLastSeenDate())));
				item.add(new Label("nanopubcount", s.getNextNanopubNo()));
				item.add(new Label("location", i.getCity() + ", " + i.getCountryName()));
				item.add(new Label("admin", s.getAdmin()));
			}

		});

		ServerScanner.initDaemon();
	}

	private static String formatDate(Date date) {
		if (date == null) return "";
		return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(date);
	}

}
