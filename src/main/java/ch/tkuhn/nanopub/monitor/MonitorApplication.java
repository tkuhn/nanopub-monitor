package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.Folder;

public class MonitorApplication extends WebApplication {

	private Folder uploadFolder = null;

	@Override
	public Class<? extends WebPage> getHomePage() {
		return MonitorPage.class;
	}

	public void init() {
		super.init();
		uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
		uploadFolder.mkdirs();
		getCspSettings().blocking().disabled();
	}

	public Folder getUploadFolder() {
		return uploadFolder;
	}

}
