package ch.tkuhn.nanopub.monitor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GLatLng;


public class MonitorPage extends WebPage {

	private static final long serialVersionUID = -2069078890268133150L;

	public MonitorPage(final PageParameters parameters) {
		super(parameters);
		GMap map = new GMap("map");
        map.setStreetViewControlEnabled(false);
        map.setScaleControlEnabled(true);
        map.setScrollWheelZoomEnabled(true);
        List<GLatLng> points = new ArrayList<GLatLng>();
        points.add(new GLatLng(47.3667, 8.55));
        points.add(new GLatLng(45.4127, -75.6742));
        points.add(new GLatLng(52.35, 4.9167));
        map.fitMarkers(points, true);
        add(map);
    }

}
