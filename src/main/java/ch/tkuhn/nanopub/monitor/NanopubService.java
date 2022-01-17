package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;

public class NanopubService {

	private IRI serviceIri;
	private IRI typeIri;

	public NanopubService(IRI serviceIri, IRI typeIri) {
		this.serviceIri = serviceIri;
		this.typeIri = typeIri;
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

}
