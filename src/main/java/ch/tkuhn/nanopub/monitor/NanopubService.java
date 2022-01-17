package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;

public class NanopubService {

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
