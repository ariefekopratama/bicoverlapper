package es.usal.bicoverlapper.data;

import java.util.ArrayList;

public interface GeneRequester {
	public void receiveGeneAnnotations(ArrayList<GeneAnnotation> galist);
	public void receiveGOTerms(ArrayList<GOTerm> galist);
	
}
