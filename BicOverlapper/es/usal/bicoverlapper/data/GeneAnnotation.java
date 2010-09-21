package es.usal.bicoverlapper.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import es.usal.bicoverlapper.data.GOTerm;

public class GeneAnnotation {
	
	
	/**
	 * Internal id used only by BicOverlapper
	 */
	public int internalId;
	//From NCBI, obtained by eUtils
	/**
	 * Gene name as in NCBI
	 */
	public String name;
	/**
	 * NCBI gene symbol
	 */
	public String symbol;
	/**
	 * Gene ID in the loaded data matrix
	 */
	public String id;
	/**
	 * NCBI gene id (entrezId)
	 */
	public String entrezId;
	
	/**
	 * Gene type as in NCBI (by now not taken)
	 */
	public String type;
	/**
	 * Locus tag as in NCBI (by now not taken)
	 */
	public String locus;
	/**
	 * NCBI gene short description
	 */
	public String description;
	/**
	 * Gene organism as in NCBI
	 */
	public String organism;
	
	/**
	 * NCBI identified aliases
	 */
	public ArrayList<String> aliases;

	public ArrayList<GOTerm> goTerms;
	
	public GeneAnnotation()
		{
		aliases=new ArrayList<String>();
		//goTerms=new ArrayList<GOTerm>();//null if they haven't been searched yet, empty if they were searched and not found
		}
	public String getDetailedForm()
		{
		String form="";
		if(name!=null && name.length()>0)				form=form.concat("Name: "+name+"\n");
		if(id!=null && id.length()>0)					form=form.concat("ID:      "+id+"\n");
		if(type!=null && type.length()>0)				form=form.concat("Type:  "+type+"\n");
		if(locus!=null && locus.length()>0)				form=form.concat("Loc.:  "+locus+"\n");
		if(aliases!=null && aliases.size()>0)
			{
			String al="";
			for(int i=0;i<aliases.size();i++)	al=al.concat(aliases.get(i)+", ");
			form=form.concat("Alias:  "+al.substring(0, al.length()-2)+"\n");	
			}
		if(organism!=null && organism.length()>0)		form=form.concat("Org.:   "+organism+"\n");
		if(description!=null && description.length()>0)	
			{
			form=form.concat("Desc.:  ");
			String[] tok=description.split(" ");
			int cont=0;
			for(int i=0;i<tok.length;i++)
				{
				form=form.concat(tok[i]+" ");
				if(cont++>=5 || i==tok.length-1)	
					{
					form=form.concat("\n      ");
					cont=0;
					}
				}
			}
		if(goTerms!=null && goTerms.size()>0)
			{
			//1) Sort terms
			List<String> terms=new ArrayList<String>();
			for(GOTerm go : goTerms)	terms.add(go.term);
			Collections.sort(terms);
			ArrayList<GOTerm> newgo=new ArrayList<GOTerm>();
			for(String term: terms)
				for(GOTerm go: goTerms)
					if(go.term.equals(term))	newgo.add(go);
			
			goTerms=newgo;
			
			//2) Add them
			form=form.concat("\nGO Terms:  ");
			for(GOTerm go : goTerms)
				{
				form=form.concat("\n     "+go.term);
				}
			}
		return form;
		}
}
