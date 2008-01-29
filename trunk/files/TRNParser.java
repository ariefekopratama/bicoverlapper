package files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A parser for different formats of TRN information to GML format
 * Currently there's available:
 * 	- SIF (Single Input File) format (STILL IN DEVELOPMENT)
 *  - SynTReN format
 *  
 *  Final GML format has the following keys:
 *  	d0 - "node" - "name" - string - node name
 *  	d1 - "node" - "type" - string - node type
 *  	d2 - "edge" - "type" - ac|re|du - ac for activator, re for repressor and du for dual
 */
public final class TRNParser 
	{
	private static String inputPath;
	private static String outputPath;

	
	/**
	 * Converts SynTReN XML to GML.
	 * Only qualitative information is transcribed. Quantitave values are ignored
	 * @param in String path of input Syntren XML
	 * @param out String path of output GML
	 */
	public static void syntren2GML(String in, String out)
		{
		inputPath=in;
		outputPath=out;
		LinkedList<LinkedList<String>> lista=new LinkedList<LinkedList<String>>();
		BufferedReader br=null;
			try{
		br=new BufferedReader(new FileReader(inputPath));
			}catch(FileNotFoundException e){System.err.println("Error: file "+inputPath+" not found");}
		
		BufferedWriter bw=null;
		FileWriter fw=null;
		System.out.println(in);
		if(br==null)	System.out.println("No hemos podido abrir br!!");
		try{
			fw=new FileWriter(outputPath);
			bw=new BufferedWriter(fw);
			
			String linea=br.readLine();
			if(linea.trim().equals("<GeneNetwork>"))//Header
				{
				bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"  <graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n " +
						"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
						"  xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n" +
						"  http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" );
				
				bw.write("\t<key id=\"d0\" for=\"node\" attr.name=\"name\" attr.type=\"string\">\n"+
						"</key>\n");
				
				bw.write("\t<key id=\"d1\" for=\"node\" attr.name=\"type\" attr.type=\"string\">\n"+
						"\t\t<default>unknown</default>\n"+
						"</key>\n");
				
				bw.write("\t<key id=\"d2\" for=\"edge\" attr.name=\"type\" attr.type=\"string\">\n"+
						"</key>\n");
				
				bw.write("\t<graph id=\"Gene Network\" edgedefault=\"directed\">\n");
			    }
			
			linea=br.readLine();	//<Nodes> label
			linea=br.readLine();
			boolean nodo=true;
			int pos1=0;
			int pos2=0;
			String texto=null;
			while(nodo)								//Nodes
				{
				if(linea.contains("<Node"))
					{
					pos1=linea.indexOf("id=");		//<node>
					pos2=pos1+linea.substring(pos1).indexOf(" ");
					texto=linea.substring(pos1+3,pos2);
					bw.write("\t\t<node id="+texto+">\n");
					System.out.println(linea);						
					if(linea.contains("name="))
						{
						pos1=linea.indexOf("name=");		//<node>
						pos2=pos1+linea.substring(pos1).indexOf(" ");
						texto=linea.substring(pos1+6,pos2-1);
						bw.write("\t\t\t<data key=\"d0\">"+texto+"</data>\n");
						}
					if(linea.contains("type="))
						{
						pos1=linea.indexOf("type=");		//<node>
						pos2=pos1+linea.substring(pos1).indexOf(">");
						texto=linea.substring(pos1+6,pos2-1);
						bw.write("\t\t\t<data key=\"d1\">"+texto+"</data>\n");
						}
					bw.write("\t\t</node>\n");
					
					LinkedList<String> listaNodo=new LinkedList<String>();
					//Falta tomar la información de los tipos de interacción
					//OneActivator/RepressorCoop Una sola interacción de ac/re
					//ACTIVATOR/REPRESSOR        Múltiples interacciones, algunas ac, algunas re
					//NActivators/RepressorsMM	Múltiples interacciones, todas ac o re
					//external_					Auto activación (ac)
					//OneActivatorOneRepressor	Una interacción ac y una re (firstIsActivator) nos dice cuál primero
					//TwoActivatorsSynergism	Dos interacciones ac
					//NOTA: tomando el esquema XML, no hay manera de distinguir las interacciones du!!!!
					while(!(linea=br.readLine()).trim().equals("</Node>"))
						{
						if(linea.contains(">ACTIVATOR<") || linea.contains("OneActivatorCoop")
								|| linea.contains("external_"))
							{
							listaNodo.add(new String("ac"));
							}
						else 
							{
							if(linea.contains(">REPRESSOR<") || linea.contains("OneRepressorCoop"))
								{
								listaNodo.add("re");
								}
							else 
								{
								if(linea.contains("TwoActivatorsSynergism"))
									{
									listaNodo.add("ac");
									listaNodo.add("ac");
									}
								else 
									{
									if(linea.contains("OneActivatorOneRepressor"))
										{
										while(!(linea=br.readLine()).contains("firstIsActivator"))	;
										//Miramos si el primero es activador o es el segundo
										if(linea.contains("true"))
											{
											listaNodo.add("ac");
											listaNodo.add("re");//Antes du!
											}
										else
											{
											listaNodo.add("re");
											listaNodo.add("ac");//Antes du!
											}
										}
									else
										{
										if(linea.contains("NRepressorsMM"))
											{	
											linea=br.readLine();
											pos1=linea.indexOf("numReg\">");
											pos2=linea.indexOf("</parameter>");
											int num=new Integer(linea.substring(pos1+8,pos2)).intValue();
											for(int i=0;i<num;i++)	listaNodo.add("re");
											}
										else
											{
											if(linea.contains("NActivatorsMM"))
												{
												linea=br.readLine();
												pos1=linea.indexOf("numReg\">");
												pos2=linea.indexOf("</parameter>");
												int num=new Integer(linea.substring(pos1+8,pos2)).intValue();
												for(int i=0;i<num;i++)	listaNodo.add("ac");
												}
											}
										}
									}
								}
							}
						}
					lista.add(listaNodo);
					}//is a node
				linea=br.readLine();
				//nodo=false;
				if(linea.trim().equals("</Nodes>"))			nodo=false;
				}
			
	//		System.out.println("Terminados nodos, vamos con aristas");
			linea=br.readLine();//<Edges>
			linea=br.readLine();
			boolean arista=true;
			while(arista)							//Edges
				{
				if(linea.contains("<Edge"))
					{
					linea=br.readLine();
					pos1=linea.indexOf("<from>");		//<from>
					pos2=pos1+linea.substring(pos1).indexOf("</from>");
					String fuente=linea.substring(pos1+6,pos2);
					linea=br.readLine();
					pos1=linea.indexOf("<to>");			//<to>
					pos2=pos1+linea.substring(pos1).indexOf("</to>");
					String objetivo=linea.substring(pos1+4,pos2);
					bw.write("\t\t<edge source=\""+fuente+"\" target=\""+objetivo+"\">\n");
					
					//interaction
					LinkedList listaNodo=(LinkedList)lista.get((new Integer(objetivo)).intValue());
					
					String interaction=(String)listaNodo.removeFirst();
					bw.write("\t\t\t<data key=\"d2\">"+interaction+"</data>\n");
					//Falta el tipo de interacción
					bw.write("\t\t</edge>\n");
					}
					
				linea=br.readLine();
				if(linea.trim().equals("<Correlations>"))	arista=false;
				}
			bw.write("\t</graph>\n</graphml>");		//Ending
			
			bw.close();
			fw.close();
			}catch(IOException e){System.err.println("IOException: "+e.getMessage());}
		return;	
		}
	}
