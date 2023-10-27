package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class NanopubService {

	private static final ValueFactory vf = SimpleValueFactory.getInstance();

	public static final IRI NANOPUB_SERVER_TYPE_IRI = vf.createIRI("https://github.com/tkuhn/nanopub-server#service");
	public static final IRI NANOPUB_MONITOR_TYPE_IRI = vf.createIRI("https://github.com/tkuhn/nanopub-monitor#service");
	public static final IRI LDF_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/nanopub-services#np-ldf-server");
	public static final IRI GRLC_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/nanopub-services#grlc-np-api");
	public static final IRI SIGNED_LDF_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/signed-nanopub-services#np-ldf-server");
	public static final IRI SIGNED_GRLC_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/signed-nanopub-services#grlc-np-api");
	public static final IRI SIGNED_SPARQL_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/signed-nanopub-services#np-sparql-api");
	public static final IRI NANOPUB_QUERY_TYPE_IRI = vf.createIRI("https://github.com/knowledgepixels/nanopub-query#service");

	private final IRI serviceIri;
	private final IRI typeIri;

	public NanopubService(IRI serviceIri, IRI typeIri) {
		this.serviceIri = serviceIri;
		this.typeIri = typeIri;
	}

	public IRI getServiceIri() {
		return serviceIri;
	}

	public IRI getTypeIri() {
		return typeIri;
	}

	public String getTypeLabel() {
		return typeIri.stringValue().replaceFirst("^.*/([^/]+)$", "$1");
	}

	public int getMapOffsetX() {
		if (typeIri.equals(NANOPUB_SERVER_TYPE_IRI)) {
			return 0;
		} else if (typeIri.equals(NANOPUB_MONITOR_TYPE_IRI)) {
			return 0;
		} else if (typeIri.equals(LDF_SERVICE_TYPE_IRI)) {
			return 3;
		} else if (typeIri.equals(GRLC_SERVICE_TYPE_IRI)) {
			return -3;
		} else if (typeIri.equals(SIGNED_LDF_SERVICE_TYPE_IRI)) {
			return 3;
		} else if (typeIri.equals(SIGNED_GRLC_SERVICE_TYPE_IRI)) {
			return -3;
		} else if (typeIri.equals(SIGNED_SPARQL_SERVICE_TYPE_IRI)) {
			return 3;
		} else if (typeIri.equals(NANOPUB_QUERY_TYPE_IRI)) {
			return 0;
		}
		return 0;
	}

	public int getMapOffsetY() {
		if (typeIri.equals(NANOPUB_SERVER_TYPE_IRI)) {
			return 0;
		} else if (typeIri.equals(NANOPUB_MONITOR_TYPE_IRI)) {
			return -3;
		} else if (typeIri.equals(LDF_SERVICE_TYPE_IRI)) {
			return 3;
		} else if (typeIri.equals(GRLC_SERVICE_TYPE_IRI)) {
			return 3;
		} else if (typeIri.equals(SIGNED_LDF_SERVICE_TYPE_IRI)) {
			return 0;
		} else if (typeIri.equals(SIGNED_GRLC_SERVICE_TYPE_IRI)) {
			return 0;
		} else if (typeIri.equals(SIGNED_SPARQL_SERVICE_TYPE_IRI)) {
			return -3;
		} else if (typeIri.equals(NANOPUB_QUERY_TYPE_IRI)) {
			return 3;
		}
		return 0;
	}

	public String getMapColor() {
		if (typeIri.equals(NANOPUB_SERVER_TYPE_IRI)) {
			return "orange";
		} else if (typeIri.equals(NANOPUB_MONITOR_TYPE_IRI)) {
			return "lightcoral";
		} else if (typeIri.equals(LDF_SERVICE_TYPE_IRI)) {
			return "skyblue";
		} else if (typeIri.equals(GRLC_SERVICE_TYPE_IRI)) {
			return "darkseagreen";
		} else if (typeIri.equals(SIGNED_LDF_SERVICE_TYPE_IRI)) {
			return "#4682B4";
		} else if (typeIri.equals(SIGNED_GRLC_SERVICE_TYPE_IRI)) {
			return "lightseagreen";
		} else if (typeIri.equals(SIGNED_SPARQL_SERVICE_TYPE_IRI)) {
			return "mediumpurple";
		} else if (typeIri.equals(NANOPUB_QUERY_TYPE_IRI)) {
			return "deeppink";
		}
		return "gray";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NanopubService)) return false;
		NanopubService s = (NanopubService) obj;
		return serviceIri.equals(s.serviceIri) && typeIri.equals(s.typeIri);
	}

	@Override
	public String toString() {
		return serviceIri.stringValue() + " (" + typeIri + ")";
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
