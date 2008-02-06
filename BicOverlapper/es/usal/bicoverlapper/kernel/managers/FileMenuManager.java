package es.usal.bicoverlapper.kernel.managers;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;




import es.usal.bicoverlapper.data.BubbleData;
import es.usal.bicoverlapper.data.Field;
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.data.TRNData;
import es.usal.bicoverlapper.data.files.MicroarrayFilter;
import es.usal.bicoverlapper.data.files.TRNFilter;
import es.usal.bicoverlapper.kernel.BicOverlapperWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.WorkDesktop;
import es.usal.bicoverlapper.kernel.configuration.ConfigurationHandler;
import es.usal.bicoverlapper.utils.Translator;

/**
 * Class that handles the File Menu Options
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * @version 3.2, 26/3/2007
 */
public class FileMenuManager implements ActionListener {
	
	private BicOverlapperWindow ventana;
	private BufferedReader pathReader;
	private BufferedWriter pathWriter;
	private final int WINDOWS=0;
	private final int LINUX=1;
	private int OS;
	private TRNFilter trnFilter=new TRNFilter();
	private MicroarrayFilter microFilter=new MicroarrayFilter();
	
	
	
	/**
	 * Constructor to build a MenuManager
	 * 
	 * @param window <code>BicOverlapperWindow</code> that will contain the menu that this manager controls
	 */
	public FileMenuManager(BicOverlapperWindow window) {
		this.ventana = window;
		if(System.getProperty("os.name").contains("indows"))	OS=WINDOWS;
		else													OS=LINUX;//it includes macOSX, now with Linux paths
	}
	
	String getPath(String path)
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
	 * Method invoked each time that an option in the view menu is clicked
	 */
	public void actionPerformed(ActionEvent e) {
		
		boolean error=false;
		String defaultPath="";
		try{
			//pathReader=new BufferedReader(new FileReader("data\\path.txt"));
			//pathReader=new BufferedReader(new FileReader(getPath("es\\usal\\bicoverlapper\\data\\path.txt")));
			pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/path.txt"));
			defaultPath=pathReader.readLine();
			}catch(IOException ex){System.err.println("pathReader has no information"); defaultPath="";}
		
		if(e.getActionCommand().equals("Abrir")) {
			
			JFileChooser selecFile = new JFileChooser();
			selecFile.setCurrentDirectory(new File(defaultPath));
			//selecFile.setLocale(new Locale("en"));
			int returnval = selecFile.showDialog((Component)e.getSource(),"Abrir");
			
			if(returnval == JFileChooser.APPROVE_OPTION) {
				File fichero = selecFile.getSelectedFile();
				
				JDesktopPane desktop = new JDesktopPane();
				desktop.setName(fichero.getName());
				
				Session sesion = new Session(desktop);
				try {
					leerFichero(fichero,sesion);
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(null,
							Translator.instance.warningLabels.getString("s3")+" "+fichero.getName(),
                            Translator.instance.warningLabels.getString("s2"),JOptionPane.ERROR_MESSAGE);
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null,
							Translator.instance.warningLabels.getString("s4"),
                            Translator.instance.warningLabels.getString("s1"),JOptionPane.ERROR_MESSAGE);
				}
				sesion.fileLoaded();
				ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
				// Actualizar las ventanas activas				
				sesion.updateData();
			}
		}
		
		else if(e.getActionCommand().equals("Open TRN"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(trnFilter);
		selecFile.setCurrentDirectory(new File(defaultPath));
		//selecFile.setLocale(new Locale("en"));
		int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Open TRN");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) 
			{
			//Si no hay TRN pero hay microarray dejamos en la misma sesión
			File fichero = selecFile.getSelectedFile();
			
			String path=selecFile.getCurrentDirectory().getPath();
			Session sesion;

			JDesktopPane desktop = new JDesktopPane();
			desktop.setName(fichero.getName());
			boolean addVista=false;
			if(this.ventana.getActiveWorkDesktop()!=null)
				sesion = this.ventana.getActiveWorkDesktop().getSession();
			else
				{
				addVista=true;
				sesion = new Session(desktop);
				}
			
			try {
				leerTRN(path, fichero, sesion);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(null,
                        "File not Found: "+fichero.getName(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			} catch (IOException e2) {
				JOptionPane.showMessageDialog(null,
                        "I/O Error"+e2.getMessage(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(null,
                        "Format Error"+e3.getMessage(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			}
			
			if(!error)
				{
				// Actualizar las ventanas activas
				sesion.fileLoaded();
				sesion.updateData();
				
				try{
					//pathWriter=new BufferedWriter(new FileWriter("data\\path.txt"));
					//pathWriter=new BufferedWriter(new FileWriter(getPath("es\\usal\\bicoverlapper\\data\\path.txt")));
					pathWriter=new BufferedWriter(new FileWriter("es/usal/bicoverlapper/data/path.txt"));
					pathWriter.write(path);
					pathWriter.close();
					}catch(IOException ex){ex.printStackTrace();}
					
				ventana.menuViewTRN.setEnabled(true);
				if(addVista)	ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
				else			
					{
					JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
					ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
					p.setName(p.getName()+" | "+fichero.getName());
					}
				}
		}
	}
	else if(e.getActionCommand().equals("Open Microarray"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(microFilter);
		File f=new File(defaultPath);
		selecFile.setCurrentDirectory(f);
			int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Open Microarray");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) {
			//ProgressMonitor pm=new ProgressMonitor(this.ventana,"Loading microarray data... ","",0,100);
			//ProgressBar pm=new ProgressBar(0,100);
			//pm.setProgress(0);
			//BasicProgressBarUI bpb=new BasicProgressBarUI();
			//bpb.installUI(this.ventana.getDesktop());
			
			selecFile.setVisible(false);
			File fichero = selecFile.getSelectedFile();
		
			String path=selecFile.getCurrentDirectory().getPath();
			Session sesion;
			JDesktopPane desktop = new JDesktopPane();
			desktop.setName(fichero.getName());
			boolean addVista=false;
			if(this.ventana.getActiveWorkDesktop()!=null)
				sesion = this.ventana.getActiveWorkDesktop().getSession();
			else
				{
				addVista=true;
				sesion = new Session(desktop);
				}
				
			try {
				leerMicroarray(path, fichero,sesion);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(null,
                        "File not found: "+fichero.getName(),
                        "Error",JOptionPane.ERROR_MESSAGE);
					error=true;
			} catch (IOException e2) 
				{
				JOptionPane.showMessageDialog(null,
                        "I/O Error: "+e2.getMessage(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			} catch (Exception e3) 
				{
				JOptionPane.showMessageDialog(null,
		                "Format Error: "+e3.getMessage(),
		                "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
				}
			if(!error)
				{
				// Actualizar las ventanas activas		
				sesion.fileLoaded();
				sesion.updateData();
				
				try{
				//pathWriter=new BufferedWriter(new FileWriter("es\\usal\\bicoverlapper\\data\\path.txt"));
				pathWriter=new BufferedWriter(new FileWriter("es/usal/bicoverlapper/data/path.txt"));
				pathWriter.write(path);
				pathWriter.close();
				}catch(IOException ex){ex.printStackTrace();}
				
				ventana.menuViewParallelCoordinates.setEnabled(true);
				ventana.menuViewHeatmap.setEnabled(true);
				
				if(addVista)	ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
				else			
					{
					JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
					ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
					p.setName(p.getName()+" | "+fichero.getName());
					}
				}
		}
	}
	else if(e.getActionCommand().equals("Open Biclustering Results"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(microFilter);
		selecFile.setCurrentDirectory(new File(defaultPath));
		
		int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Open Biclustering Results");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) {
			File fichero = selecFile.getSelectedFile();
			//Perfil p=ventana.getPerfilActivo();
		
			String path=selecFile.getCurrentDirectory().getPath();
			Session sesion;
			
			JDesktopPane desktop = new JDesktopPane();
			desktop.setName(fichero.getName());
			boolean addVista=false;
			if(this.ventana.getActiveWorkDesktop()!=null)
				sesion = this.ventana.getActiveWorkDesktop().getSession();
			else
				{
				addVista=true;
				sesion = new Session(desktop);
				}
	
			try {
				leerBicluster(path, fichero.getName(),sesion);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(null,
	                    "File not found: "+fichero.getName(),
	                    "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			} catch (IOException e2) 
			{
			JOptionPane.showMessageDialog(null,
                    "I/O Error "+e2.getMessage(),
                    "Error",JOptionPane.ERROR_MESSAGE);
			error=true;
			} catch (Exception e3) 
			{
			JOptionPane.showMessageDialog(null,
	                "Format error "+e3.getMessage(),
	                "Error",JOptionPane.ERROR_MESSAGE);
			error=true;
			}
			
			if(!error)
				{
				// Actualizar las ventanas activas		
				sesion.fileLoaded();
				sesion.updateData();
				
				ventana.menuViewOverlapper.setEnabled(true);
				ventana.menuViewBubbles.setEnabled(true);
				
				try{
					//pathWriter=new BufferedWriter(new FileWriter("data\\path.txt"));
					//pathWriter=new BufferedWriter(new FileWriter(getPath("data\\path.txt")));
					pathWriter=new BufferedWriter(new FileWriter(getPath("es/usal/bicoverlapper/data/path.txt")));
					pathWriter.write(path);
					pathWriter.close();
					}catch(IOException ex){ex.printStackTrace();}
					
				if(addVista)	ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
				else			
					{
					JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
					ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
					p.setName(p.getName()+" | "+fichero.getName());
					}
				}
			}
	}
	
	//------------------------- CARGAR CONFIGURACION -------------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s17"))
				&& ventana.isActiveWorkDesktop())
		{
			JFileChooser selecFile = new JFileChooser();
			
			int returnval = selecFile.showDialog((Component)e.getSource(),Translator.instance.menuLabels.getString("s21"));
			
			if(returnval == JFileChooser.APPROVE_OPTION){
				File fichero = selecFile.getSelectedFile();
				Session sesion = ventana.getActiveWorkDesktop().getSession();
				es.usal.bicoverlapper.kernel.configuration.ConfigurationLoader.loadConfiguration(sesion,fichero);
			}			
		}
		//-------------------- GUARDAR CONFIGURACIÓN -------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s18"))
				&& ventana.isActiveWorkDesktop())
		{
			Session sesion = ventana.getActiveWorkDesktop().getSession();
			ConfigurationHandler config = sesion.getConfig();
			JFileChooser selecFile = new JFileChooser();
			
			int returnval = selecFile.showDialog((Component)e.getSource(),Translator.instance.menuLabels.getString("s22"));
			
			if(returnval == JFileChooser.APPROVE_OPTION){
				File fichero = selecFile.getSelectedFile();
				es.usal.bicoverlapper.kernel.configuration.ConfigurationLoader.saveConfiguration(config, fichero);
			}			
		}	
		
		//----------------------- SALIR -------------------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s19")))
		{
			System.exit(0);
		}
	}
	
	/**
	 * Lee el <code>fichero</code> pasado como parametro, almacena las variables en él contenidas en una estructura <code>DatosFile</code>
	 * y se la pasa al objeto <code>sesion</code> pasado como parametro.
	 * 
	 * @param fichero <code>File</code> que va a ser leido.
	 * @param sesion <code>Sesion</code> a la que se le pasa el resultado de la lectura en un objeto <code>DatosFile</code>.
	 * @throws FileNotFoundException Excepcion lanzada cuando el fichero que se va a abrir no existe.
	 * @throws IOException Excepcion lanzada cuando existe un error de E/S.
	 */
	private void leerFichero(File fichero,Session sesion) throws FileNotFoundException,IOException {
		
		MultidimensionalData datos = new MultidimensionalData();
		
		datos.setFileName(fichero.getName());
		
		BufferedReader in =	new BufferedReader(new FileReader(fichero));
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
		sesion.setData(datos);		
	}

	//----------------------------- FUNCIONES DE LECTURA MIAS ------------------------
	/*
	 * Lee un microarray y lo guarda tanto a la estructura de Prefuse en MicroarrayData como en DatosFile y DatosVar
	 */
	//private void leerMicroarray(String path, File fichero,Session sesion) throws FileNotFoundException,IOException 
	private void leerMicroarray(String path, File fichero,Session sesion) throws Exception 
		{
		int skipColumns=1;
		int skipRows=1;
		double t1=System.currentTimeMillis();
	//	try {
			MicroarrayData md=new MicroarrayData(getPath(path+"\\"+fichero.getName()), false, skipRows,skipColumns);
			double t2=System.currentTimeMillis();
			sesion.setMicroarrayData(md);
			leerFichero(path, fichero, sesion, false, true, true, true, "\t"); 
			int []nm=new int[sesion.getMicroarrayData().getNumConditions()];
			for(int i=0;i<sesion.getMicroarrayData().getNumConditions();i++)	nm[i]=2;
			sesion.getData().setDecimalCount(nm);
			t1=System.currentTimeMillis();
			System.out.println("Time to load microarray data: "+(t1-t2)/1000+" seconds");
			
			}
	
	/*
	 * Lee un fichero de grafos con estructura GML o con la estructura XML de Syntren
	 */
	private void leerTRN(String path, File fichero,Session sesion) throws FileNotFoundException,IOException 
		{
		BufferedReader in =	new BufferedReader(new FileReader(fichero));
		String variable = null;
			
		variable = in.readLine();
		//En función de esto determinamos qué es:
		//1) Un fichero de tabla xml como los que usa Javi (usamos su método de lectura) a DatosFile
		//2) Un grafo xml de los que usamos nosotros, usamos nuestro parser a TRN Data
		//3) Un fichero txt de los que usamos para los resultados de biclustering, usamos
		//	nuestro lector a BubbleData
		BufferedReader in2=in; //Para no estropear lo que se lea de in
		String linea2=in2.readLine();
		if(variable.contains("GeneNetwork") || linea2.contains("graphml"))
			{
			in.close();
			in2.close();
			//TRNData trnd=new TRNData(path+"\\"+fichero.getName());//caso 2)			
			TRNData trnd=new TRNData(getPath(path+"\\"+fichero.getName()));//caso 2)			
			sesion.setTRNData(trnd);	
			}
		}
	
//	private void leerBicluster(String path, String fichero,Session sesion) throws FileNotFoundException,IOException 
	private void leerBicluster(String path, String fichero,Session sesion) throws Exception 
		{
		//sesion.setBiclusterDataFile(path+"\\"+fichero);
		sesion.setBiclusterDataFile(getPath(path+"\\"+fichero));
		
		//Creamos los datos que necesitarán las burbujas
		//BubbleData bd=new BubbleData(path+"\\"+fichero); 
		BubbleData bd=new BubbleData(getPath(path+"\\"+fichero)); 
		sesion.setBubbleData(bd);
		
		sesion.setMicroarrayDataLoaded(false);
		sesion.setBubbleDataLoaded(true);//TODO para que sea true de verdad
		sesion.setBiclusterDataStatus(true);
		}
	
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