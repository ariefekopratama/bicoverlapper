package es.usal.bicoverlapper.data;

/**
 * GOTerm refers to a GO Term annotated to a gene or a group of genes.
 * @author Rodrigo
 *
 */
public class GOTerm {
	/**
	 * Name of the GO term
	 */
	public String term;
	/**
	 * GO ID
	 */
	public String id;
	/**
	 * Further definition of the GO Term. It's available in R packages, but not in QuickGO
	 */
	public String definition;
	/**
	 * Ontology, either BP, MF or CC
	 */
	public String ontology;
	/**
	 * Type of evidence, for example IEA
	 */
	public String evidence;
	/**
	 * In the case of a GOTerm associated to a single gene, it conveys the number of times that
	 * these GOTerm has been associated to the gene. It's usually 1, but, for example in QuickGO,
	 * GOTerms can be repeated several times, each time referring to a different institution or 
	 * author annotating the gene with this GO term
	 * 
	 * In the case of a GOTerm associated to a group of genes, it conveys the number of genes that 
	 * have this GO Term
	 */
	public int occurences;//In case of hypergeometric test, t
	/**
	 * In the case of a GOTerm associated to a single gene, it means nothing, and is usually 
	 * undefined
	 * 
	 * In the case of a GOTerm associated to a group of genes, it is the p-value of a hypergeometric
	 * test against all the genes in the microarray data matrix.
	 */
	public double pvalue;//In case of hypergeometric tests
	
	public GOTerm()
		{
		term="";
		id="";
		definition="";
		ontology="";
		evidence="";
		occurences=0;
		}
	
	public GOTerm(String t, String i,String d,String o, String e, int oc)
		{
		term=t;
		id=i;
		definition=d;
		ontology=o;
		evidence=e;
		occurences=oc;
		}
}
