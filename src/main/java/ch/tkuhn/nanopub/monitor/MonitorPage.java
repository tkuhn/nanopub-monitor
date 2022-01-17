package ch.tkuhn.nanopub.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
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

public class MonitorPage extends WebPage {

	private static final long serialVersionUID = -2069078890268133150L;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private String coordinates = "";

	public MonitorPage(final PageParameters parameters) throws Exception {
		super(parameters);
		final ServerList sl = ServerList.get();
		final Set<String> ipAddresses = new HashSet<String>();
		if (MonitorConf.get().showMap()) {
			for (ServerData sd : sl.getServerData()) {
				try {
					ServerIpInfo ipInfo = sd.getIpInfo();
					coordinates += "\"" + ipInfo.getLatitude() + "," + ipInfo.getLongitude() + "\",";
					ipAddresses.add(ipInfo.getIp());
				} catch (Exception ex) {
					logger.error("Something went wrong while getting coordinates", ex);
				}
			}
			coordinates = coordinates.replaceFirst(",$", "");
		}

		add(new Label("server-count", sl.getServerCount() + ""));
		add(new Label("server-ip-count", ipAddresses.size() + ""));

		add(new DataView<ServerData>("rows", new ListDataProvider<ServerData>(sl.getSortedServerData())) {

			private static final long serialVersionUID = 4703849210371741467L;

			public void populateItem(final Item<ServerData> item) {
				ServerData d = item.getModelObject();
				ServerInfo s = d.getServerInfo();
				ServerIpInfo i = d.getIpInfo();
				ExternalLink urlLink = new ExternalLink("urllink", s.getPublicUrl());
				urlLink.add(new Label("url", s.getPublicUrl()));
				item.add(urlLink);
				ExternalLink typeLink = new ExternalLink("typelink", "https://github.com/tkuhn/nanopub-server#service");
				typeLink.add(new Label("type", "Nanopub Server"));
				item.add(typeLink);
				item.add(new Label("status", d.getStatusString()));
				item.add(new Label("successratio", d.getSuccessRatioString()));
				item.add(new Label("resptime", d.getAvgResponseTimeString() + " (" + d.getDistanceString() + ")"));
				item.add(new Label("lastseen", formatDate(d.getLastSeenDate())));
				if (i == null) {
					item.add(new Label("ip", "unknown"));
					item.add(new Label("location", "unknown"));
				} else {
					item.add(new Label("ip", i.getIp()));
					item.add(new Label("location", i.getCity() + ", " + i.getCountryName()));
				}
				item.add(new Label("parameters", getPatternString(s)));
				item.add(new Label("nanopubcount", s.getNextNanopubNo()));
				item.add(new Label("description", s.getDescription()));
			}

		});

		ServerScanner.initDaemon();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
	    super.renderHead(response);
	    response.render(JavaScriptReferenceHeaderItem.forScript("var points = [" + coordinates + "];", null));
	}

	private static String getPatternString(ServerInfo si) {
		String s = " / ";
		if (si.getUriPattern() != null) s = si.getUriPattern() + s;
		if (si.getHashPattern() != null) s = s + si.getHashPattern();
		return s;
	}

	private static String formatDate(Date date) {
		if (date == null) return "";
		return dateFormat.format(date);
	}

}
