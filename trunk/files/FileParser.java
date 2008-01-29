package files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

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
							System.out.println("|"+entry+"|");
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
