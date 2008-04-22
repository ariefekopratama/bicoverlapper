package es.usal.bicoverlapper.data.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Singleton for file parsing utilities
 * @author Rodrigo Santamaria
 *
 */
public class FileParser {
	private static final long serialVersionUID = 1898266950890009392L;

	/**
	 * Singleton instance
	 */
	public static FileParser instance = new FileParser();
	
	
	private FileParser() {
		// it's a singleton, and only supposed to be read from a file
		}

	/**
	 * Generates a synthetic data set of biclusters with the specified characteristics
	 * @param numBic Number of biclusters in the data set
	 * @param degree Degree of overlap between biclusters, as (# of connections with other biclusters)/#nodes
	 * @param size Mean size of the biclusters, as number of nodes
	 * @param sdsize Random variation in the size of biclusters
	 * @param outPath Path of the output file
	 */
	public static void buildSyntheticBiclusters(int numBic, int degree, int size, float sdsize, String outPath)
		{
		try{
			BufferedWriter out =new BufferedWriter(new FileWriter(outPath));
			out.write(""+numBic);
			out.newLine();
			out.write("Synthetic_Biclusters|Size_"+size);
			out.newLine();
			
			int numNodes=numBic*size/degree;
			System.out.println("Utilizando "+numNodes+" nodos");
			
			ArrayList<String> listaGenes=new ArrayList<String>();
			ArrayList<String> listaCondiciones=new ArrayList<String>();
			
			for(int i=0;i<numNodes/2;i++)	listaGenes.add("g"+i);	
			for(int i=0;i<numNodes/2;i++)	listaCondiciones.add("c"+i);	
			for(int i=0;i<numBic;i++)
				{
				int sizeb=(int)(size+sdsize*(-1+2*Math.random()));
				ArrayList<String> lgb=new ArrayList<String>();
				ArrayList<String> lcb=new ArrayList<String>();
				
				for(int j=0;j<sizeb/2;j++)
					{
					String s=listaGenes.get((int)(listaGenes.size()*Math.random()));
					if(!lgb.contains(s))	lgb.add(s);
					}
				for(int j=0;j<sizeb/2;j++)
					{
					String s=listaCondiciones.get((int)(listaCondiciones.size()*Math.random()));
					if(!lcb.contains(s))	lcb.add(s);
					}
				out.write(lgb.size()+" "+lcb.size());
				out.newLine();
				for(int j=0;j<lgb.size();j++)	
					if(j!=lgb.size()-1)	out.write(lgb.get(j)+" ");
					else				out.write(lgb.get(j));
				out.newLine();
				for(int j=0;j<lcb.size();j++)	
					if(j!=lcb.size()-1)	out.write(lcb.get(j)+" ");
					else				out.write(lcb.get(j));
				out.newLine();
				}
			out.close();
			}catch(Exception e){e.printStackTrace();}
			System.out.println("Finished!");
		}
	/**
	 * Converts overlapper format for biclusters to bivoc format. 
	 * Bivoc sofware and format is available at https://bioinformatics.cs.vt.edu/~ggrothau/BiVoC/
	 * 
	 * @param inPath	input path for Overlapper bicluster file
	 * @param outPath	output path for Bivoc file
	 */
	public static void convertOverlapperToBivoc(String inPath, String outPath)
		{
		try{
		BufferedReader in =	new BufferedReader(new FileReader(inPath));
		BufferedWriter out =new BufferedWriter(new FileWriter(outPath));

		String cad="";
		int cont=0;
		System.out.println("CONVERTER...");
		while((cad=in.readLine())!=null)
			{
			String text[]=cad.split(" ");
			if(text.length>1)
				{
				String entry="";
				for(int i=0;i<3;i++)
					{
					switch(i)
						{
						case 0:	//name
							text=cad.split(" ");
							entry="bic"+cont+"_"+text[0]+"_"+text[1];
							cont++;
							break;
						case 1:	//genes
							cad=in.readLine();
							text=cad.split(" ");
							for(int j=0;j<text.length;j++)	entry=entry+"\t"+text[j];
							break;
						case 2:	//conditions
							cad=in.readLine();
							text=cad.split(" ");
							for(int j=0;j<text.length;j++)	entry=entry+"\t"+text[j];
							//System.out.println("|"+entry+"|");
							break;
						}
					}
				out.write(entry);
				out.newLine();
				}
			}
		out.close();
		}catch(Exception e){e.printStackTrace();}
		System.out.println("Finished!");
		}
	
	}
