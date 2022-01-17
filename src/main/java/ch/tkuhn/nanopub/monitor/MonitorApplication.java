package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

public class MonitorApplication extends WebApplication {

	@Override
	public Class<? extends WebPage> getHomePage() {
		return MonitorPage.class;
	}

	public void init() {
		super.init();
		getCspSettings().blocking().disabled();
	}

}
