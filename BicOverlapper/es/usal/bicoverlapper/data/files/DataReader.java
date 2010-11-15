package es.usal.bicoverlapper.data.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;

import prefuse.data.Node;

import es.usal.bicoverlapper.data.BubbleData;
import es.usal.bicoverlapper.data.Field;
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.MicroarrayRequester;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.data.NetworkData;
import es.usal.bicoverlapper.kernel.BicOverlapper;
import es.usal.bicoverlapper.kernel.BicOverlapperWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.WorkDesktop;

/**
 * This class implements different methods to read data file formats required by BicOverlapper
 * @author Rodrigo
 *
 */
public class DataReader {
	private final int WINDOWS=0;
	private final int LINUX=1;
	private int OS;
	
	public DataReader()
		{
		if(System.getProperty("os.name").contains("indows"))	OS=WINDOWS;
		else													OS=LINUX;//it includes macOSX, now with Linux paths
		}
	/**
	 * Reads <code>file</code> as a data frame
	 * 
	 * @param file <code>File</code> to read
	 * @param session <code>Session</code> linked to the data
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readDataFrame(File file,Session session) throws FileNotFoundException,IOException {
		
		MultidimensionalData datos = new MultidimensionalData();
		
		datos.setFileName(file.getName());
		
		BufferedReader in =	new BufferedReader(new FileReader(file));
		String variable = null;
		
		variable = in.readLine();
		Vector<String> vect = new Vector<String>(0,1);
		
		if(isVar(variable,vect)){
			Field aux = new Field((String)vect.elementAt(0));
			
			for(int i = 1; i < vect.size(); i++)
				aux.addData(Double.valueOf((String)vect.elementAt(i)).doubleValue());
			
			datos.addField(aux);
			datos.setTupleNames(null);
		}else{
			String[] id = new String[vect.size()];
			for(int i = 0; i < id.length; i++)
				id[i] = (String)vect.elementAt(i);
			
			datos.setTupleNames(id);
		}
		
		while((variable = in.readLine()) != null) {
			StringTokenizer tokens = new StringTokenizer(variable, ",");
			
			Field aux = new Field(tokens.nextToken());
			
			while(tokens.hasMoreTokens()) {
				aux.addData(Double.valueOf(tokens.nextToken()).doubleValue());
			}
			
			datos.addField(aux);			
		}		
		session.setData(datos);		
	}

	//----------------------------- FUNCIONES DE LECTURA MIAS ------------------------
	/**
	 * Reads a microarray data file with the BicOverlapper format, that is:
	 * organism/chip	col1	col2	...	colN
	 * row1	exp11	exp12	...	exp1N
	 * ...
	 * rowM	expM1	expM2	...	expMN
	 */
	public void readMicroarray(String path,Session sesion, MicroarrayRequester mr) throws Exception 
		{
		int skipColumns=1;
		int skipRows=1;
		double t1=System.currentTimeMillis();
		double t2=System.currentTimeMillis();
		MicroarrayData md=new MicroarrayData(path, false, skipRows,skipColumns,1, mr, sesion.analysis.r);
		sesion.setMicroarrayData(md);
		t1=System.currentTimeMillis();
		System.out.println("Time to load microarray data: "+(t1-t2)/1000+" seconds");
		t2=System.currentTimeMillis();
		System.out.println("Time to load fichero: "+(t2-t1)/1000+" seconds");
		}
	
	/**
	 * Reads a Transcription Regulatory Network in GML or Syntren's XML format
	 */
	public void readTRN(String path, File fichero,Session sesion, String fileType) throws FileNotFoundException,IOException 
		{
		BufferedReader in =	new BufferedReader(new FileReader(fichero));
		String variable = null;
		BicOverlapperWindow window=sesion.mainWindow;
		JDesktopPane desktop=sesion.getDesktop();
		
			
		variable = in.readLine();
		//En función de esto determinamos qué es:
		//1) Un fichero de tabla xml como los que usa Javi (usamos su método de lectura) a DatosFile
		//2) Un grafo xml de los que usamos nosotros, usamos nuestro parser a TRN Data
		//3) Un fichero txt de los que usamos para los resultados de biclustering, usamos
		//	nuestro lector a BubbleData
		BufferedReader in2=in; //Para no estropear lo que se lea de in
		NetworkData trnd=null;
		if(variable.contains("GeneNetwork"))
			{
			trnd=new NetworkData(getPath(path));//Syntren format		
			}
		else if(variable.contains("?xml"))
			{
			String linea2=in2.readLine();
			if(linea2.contains("graphml"))
				{
				trnd=new NetworkData(getPath(path));//GraphML			
				}
			}
		in.close();
		in2.close();
		if(sesion.getMicroarrayData()!=null)
			{//Set ids equivalent to the session ids
			for(int i=0;i<trnd.getGraph().getNodeCount();i++)
				{
				Node n=trnd.getGraph().getNode(i);
				n.setInt("id", sesion.getMicroarrayData().getGeneId(n.getString("name").trim()));
				}
			}
		else
			{
			for(int i=0;i<trnd.getGraph().getNodeCount();i++)
				{
				Node n=trnd.getGraph().getNode(i);
				n.setInt("id", i	);
				}
			}
		
		boolean error=false;//TODO: check possible errors
		if(!error){
			window.viewMenu.setEnabled(true);
			window.menuViewTRN.setEnabled(true);
			if(path!=null && path.length()>0)
				{
				try{
					BufferedWriter pathWriter=new BufferedWriter(new FileWriter(sesion.reader.getPath("es/usal/bicoverlapper/data/path.txt")));
					pathWriter.write(path);
					pathWriter.close();
					}catch(IOException ex){ex.printStackTrace();}
				}
				
			if(window.getActiveWorkDesktop()==null)
					window.addWorkDesktop(new WorkDesktop(desktop,sesion));
			else			
				{
				JDesktopPane p=window.getActiveWorkDesktop().getPanel();
				String title=window.getDesktop().getTitleAt(0);
				if(title.contains(".bic") || title.contains(".tmp"))
					{
					if(title.endsWith(".bic") || title.endsWith(".tmp"))
						{
						if(title.contains("|"))	title=title.substring(0, title.lastIndexOf("|")).trim();
						else					title="";
						}
					}
				title=title+" | "+fichero.getName();
				window.getDesktop().setTitleAt(0, title);
				p.setName(title);
				}
			}
		sesion.setTRNData(trnd);
		}
	
	//TODO: @deprecated
	public String getPath(String path)
		{
		String p=path;
		switch(OS)
			{
			case WINDOWS:
				p=p.replace("/","\\");
				break;
			case LINUX:
				p=p.replace("\\","/");
				break;
			}
		return p;
		}

	/**
	 * Reads Biclustering Results in a BiMax format
	 * @param path
	 * @param fichero
	 * @param sesion
	 * @throws Exception
	 */
	public void readBiclusterResults(String path, String fichero,Session sesion) 
		{
		//readBiclusterResults(path, fichero, getPath(path+"\\"+fichero), sesion);
		readBiclusterResults(path, fichero, path, sesion);
		}
	
	public void readBiclusterResults(String path, String fileName, String file, Session sesion)
		{
		BicOverlapperWindow window=sesion.mainWindow;
		JDesktopPane desktop=sesion.getDesktop();
		boolean error=false;
		
		try{
			BubbleData bd=new BubbleData(file, sesion.getMicroarrayData()); 
			bd.getGraph().getNodes().getClientProperty("name");
			sesion.setBubbleData(bd);
		
			sesion.setBiclusterDataFile(file);
			
			sesion.setBubbleDataLoaded(true);//TODO para que sea true de verdad
			sesion.setBiclusterDataStatus(true);
			}
		catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null,
                    "File not found: "+file,
                    "Error",JOptionPane.ERROR_MESSAGE);
			error=true;
			}
		catch (IOException e2) 
			{	
			JOptionPane.showMessageDialog(null,
	                "I/O Error "+e2.getMessage(),
	                "Error",JOptionPane.ERROR_MESSAGE);
			error=true;
			}
		catch (Exception e3) 
			{
			JOptionPane.showMessageDialog(null,
	                "Format error "+e3.getMessage(),
	                "Error",JOptionPane.ERROR_MESSAGE);
			error=true;
			}
		if(!error)
			{
			window.viewMenu.setEnabled(true);
			window.menuViewOverlapper.setEnabled(true);
			window.menuViewBubbles.setEnabled(true);
			if(path!=null && path.length()>0)
				{
				try{
					BufferedWriter pathWriter=new BufferedWriter(new FileWriter(sesion.reader.getPath("es/usal/bicoverlapper/data/path.txt")));
					pathWriter.write(path);
					pathWriter.close();
					}catch(IOException ex){ex.printStackTrace();}
				}
				
			if(window.getActiveWorkDesktop()==null)
					window.addWorkDesktop(new WorkDesktop(desktop,sesion));
			else			
				{
				JDesktopPane p=window.getActiveWorkDesktop().getPanel();
				String title=window.getDesktop().getTitleAt(0);
				if(title.contains(".bic") || title.contains(".tmp"))
					{
					if(title.endsWith(".bic") || title.endsWith(".tmp"))
						{
						if(title.contains("|"))	title=title.substring(0, title.lastIndexOf("|")).trim();
						else					title="";
						}
					}
				title=title+" | "+fileName;
				//window.getDesktop().setTitleAt(0, p.getName()+" | "+fileName);
				//p.setName(p.getName()+" | "+fileName);
				window.getDesktop().setTitleAt(0, title);
				p.setName(title);
				}
			}
		}
	
//	public void biclusterResults
	/*
	 * Lectura de fichero tradicional de las que usa Javi, modificado para que se adapte a Syntren
	 * TODO: Un leerFichero más genérico que tenga en cuenta distintas opciones
	 */
	void leerFichero(String path, File fichero,Session sesion, int skipColumns) throws FileNotFoundException,IOException 
		{
		BufferedReader in =	new BufferedReader(new FileReader(fichero));
		String variable = null;
		
		variable = in.readLine();
		
		MultidimensionalData datos = new MultidimensionalData();
		datos.setFileName(fichero.getName());
		int numExp=0;//TODO: Nosotros no tenemos con Syntren nombres para cada experimento/dimensión así que cogemos y ponemos simplemente un número
		
		//Luego pasamos al resto de las filas, que se van a considerar siempre como variables y no como nombres de nada	
		while((variable = in.readLine()) != null) 
			{
			StringTokenizer tokens = new StringTokenizer(variable, "\t");
				
			Field aux = new Field((new Integer(numExp++).toString()));
			
			for(int i=0;i<skipColumns;i++)	tokens.nextToken();//Pasamos de los de la columna con nombres de genes	
			while(tokens.hasMoreTokens()) 
				{
				aux.addData(Double.valueOf(tokens.nextToken()).doubleValue());
				}
				
			datos.addField(aux);			
			}
		sesion.setData(datos);		
		}		
	

	/*
	 * Lectura de fichero para convertir a la estructura de javi
	 */
	private void leerFichero(String path, File fichero,Session sesion, boolean invert, boolean rowHeader, boolean topLeftWord, boolean colHeader, String delimiter) throws FileNotFoundException,IOException 
		{
		BufferedReader in =	new BufferedReader(new FileReader(fichero));
		String variable = null;
		MultidimensionalData datos = new MultidimensionalData();
		datos.setFileName(fichero.getName());
		
		if(invert)	//Cada fila es una variable
			{
			variable = in.readLine();
			
			int numExp=0;
			//Luego pasamos al resto de las filas, que se van a considerar siempre como variables y no como nombres de nada	
			while((variable = in.readLine()) != null) 
				{
				StringTokenizer tokens = new StringTokenizer(variable, "\t");
					
				Field aux = new Field((new Integer(numExp++).toString()));
				
				while(tokens.hasMoreTokens()) 
					{
					aux.addData(Double.valueOf(tokens.nextToken()).doubleValue());
					}
					
				datos.addField(aux);			
				}
			sesion.setData(datos);		
			}
		else//each row is an individual (the usual case)
			{
			String cad=null;
			if(colHeader)//The first row is the column header, with expression names
				{
				cad=in.readLine();
				StringTokenizer tokens = new StringTokenizer(cad, delimiter);
				if(topLeftWord)	tokens.nextToken();//Avoid square word if any
				while(tokens.hasMoreTokens()) //Add all variables
					{
					Field aux = new Field(tokens.nextToken());
					datos.addField(aux);			
					}
				}
			
			LinkedList <String>idtuplas=null;
			if(rowHeader)		idtuplas=new LinkedList<String>();
			
			while((cad=in.readLine())!=null)
				{	
				StringTokenizer tokens = new StringTokenizer(cad, delimiter);
				if(datos.getNumFields()==0)//There was no header, adding generic header
					{
					for(int i=0;i<tokens.countTokens();i++)
						{
						Field aux = new Field("C"+i);
						datos.addField(aux);			
						}
					}
				if(rowHeader)//then first token is the individual's name.
					{
					String id=tokens.nextToken();
					idtuplas.add(id);
					}
				if(tokens.countTokens()!=datos.getNumFields())
					{
					System.err.println("Bad format file, line length does not match with number of variables");
					System.exit(1);
					}
				for(int i=0;i<datos.getNumFields();i++)
					{
					String s=tokens.nextToken();
					datos.fieldAt(i).addData(new Double(s).doubleValue());
					}
				}
			
			String[] idarray=new String[idtuplas.size()];
			for(int i=0;i<idtuplas.size();i++)
				{
				idarray[i]=idtuplas.get(i);
				}
			datos.setTupleNames(idarray);
			sesion.setData(datos);
			}
		}		

	
	/**
	 * Metodo que nos parsea el parametro <code>variable</code> para obtener los datos contenidos y 
	 * devolverlos en el parametro <code>vect</code>. Ademas nos devuelve un boolean, cuando dicho boolean es <code>true</code> nos
	 * indica que <code>variable</code> contenia los datos de una variable, en caso de que nos devuelva <code>false</code> nos
	 * indica que <code>variable</code> contenia las etiquetas de las tuplas formadas por las variables.
	 * 
	 * @param variable <code>String</code> que va a ser parseado.
	 * @param vect Vector de <code>String</code> que contiene el parseo de <code>variable</code>.
	 * @return Devuelve un <code>true</code> si el parseo produce una variable, y devuelve <code>false</code> si el parseo
	 * produce las etiquetas de las tuplas. 
	 */
	private boolean isVar(String variable, Vector<String> vect) {
		StringTokenizer tokens = new StringTokenizer(variable,",");
		boolean isVar = true;
		
		vect.addElement(tokens.nextToken());
		
		while(tokens.hasMoreTokens()){
			
			String valor = tokens.nextToken();
			try{
				Double.valueOf(valor);
			}
			catch (NumberFormatException e){
				isVar = false;
			}
			vect.addElement(valor);
		}		
		return isVar;
	}

}
