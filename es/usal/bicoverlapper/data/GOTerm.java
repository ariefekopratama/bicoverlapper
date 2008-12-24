package es.usal.bicoverlapper.data;


public class GOTerm {
	//From GeneOntology
	public String term=null;
	public String id=null;
	public String definition=null;
	public String ontology=null;
	public int occurences=0;
	
	public GOTerm(String term, String id, String definition, String ontology, int occurences)
		{
		this.term=term;
		this.id=id;
		this.definition=definition;
		this.ontology=ontology;
		this.occurences=occurences;
		}
}
