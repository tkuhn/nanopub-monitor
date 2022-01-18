package ch.tkuhn.nanopub.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorPage extends WebPage {

	private static final long serialVersionUID = -2069078890268133150L;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private String points = "";

	public MonitorPage(final PageParameters parameters) throws Exception {
		super(parameters);
		final ServerList sl = ServerList.get();
		final Set<String> ipAddresses = new HashSet<String>();
		if (MonitorConf.get().showMap()) {
			for (ServerData sd : sl.getServerData()) {
				try {
					ServerIpInfo ipInfo = sd.getIpInfo();
					NanopubService s = sd.getService();
					points += "[\"" + ipInfo.getLatitude() + "," + ipInfo.getLongitude() + "\",\"" + s.getMapColor() + "\",[" + s.getMapOffsetX() + "," + s.getMapOffsetY() + "]],";
					ipAddresses.add(ipInfo.getIp());
				} catch (Exception ex) {
					logger.error("Something went wrong while getting coordinates", ex);
				}
			}
			points = points.replaceFirst(",$", "");
		}

		add(new Label("server-count", sl.getServerCount() + ""));
		add(new Label("server-ip-count", ipAddresses.size() + ""));

		add(new DataView<ServerData>("rows", new ListDataProvider<ServerData>(sl.getSortedServerData())) {

			private static final long serialVersionUID = 4703849210371741467L;

			public void populateItem(final Item<ServerData> item) {
				ServerData d = item.getModelObject();
				ServerIpInfo i = d.getIpInfo();
				ExternalLink urlLink = new ExternalLink("urllink", d.getServiceId());
				urlLink.add(new Label("url", d.getServiceId()));
				item.add(urlLink);
				ExternalLink typeLink = new ExternalLink("typelink", d.getService().getTypeIri().stringValue());
				typeLink.add(new Label("type", d.getService().getTypeLabel()));
				typeLink.add(new AttributeModifier("style", "background: " + d.getService().getMapColor()));
				item.add(typeLink);
				Label statusLabel = new Label("status", d.getStatusString());
				if (!d.getStatusString().equals("OK")) {
					statusLabel.add(new AttributeModifier("style", "color: red"));
				}
				item.add(statusLabel);
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
				item.add(new Label("parameters", d.getParameterString()));
				item.add(new Label("description", d.getDescription()));
			}

		});

		ServerScanner.initDaemon();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
	    super.renderHead(response);
	    response.render(JavaScriptReferenceHeaderItem.forScript("var points = [" + points + "];", null));
	}

	static String formatDate(Date date) {
		if (date == null) return "";
		return dateFormat.format(date);
	}

}
