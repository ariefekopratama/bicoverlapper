package es.usal.bicoverlapper.data;

import java.util.ArrayList;

public class GeneAnnotation {
	//From NCBI, obtained by eUtils
	/**
	 * Gene name as in NCBI
	 */
	public String name;
	/**
	 * NCBI gene id
	 */
	public String id;
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

	//From GeneOntology
	public ArrayList<String> goTerms;
	
	public String getDetailedForm()
		{
		String form="";
		if(name!=null && name.length()>0)				form=form.concat("Name: "+name+"\n");
		if(id!=null && id.length()>0)					form=form.concat("ID:      "+id+"\n");
		if(type!=null && type.length()>0)				form=form.concat("Type:  "+type+"\n");
		if(locus!=null && locus.length()>0)				form=form.concat("Loc.:  "+locus+"\n");
		if(organism!=null && organism.length()>0)		form=form.concat("Org.:   "+organism+"\n");
		if(aliases!=null && aliases.size()>0)
			{
			String al="";
			for(int i=0;i<aliases.size();i++)	al=al.concat(aliases.get(i)+", ");
			form=form.concat("Aka:     "+al.substring(0, al.length()-2));	
			}
		if(description!=null && description.length()>0)	
			{
			//form=form.concat("Desc:  "+description+"\n");
			String[] tok=description.split(" ");
			int cont=0;
			for(int i=0;i<tok.length;i++)
				{
				form=form.concat(tok[i]+" ");
				if(cont++>=5)	
					{
					form=form.concat("\n");
					cont=0;
					}
				}
			}
		return form;
		}
}
