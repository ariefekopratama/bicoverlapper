package es.usal.bicoverlapper.kernel.managers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;




import es.usal.bicoverlapper.data.MicroarrayRequester;
import es.usal.bicoverlapper.data.files.BiclusterResultsFilter;
import es.usal.bicoverlapper.data.files.GMLFilter;
import es.usal.bicoverlapper.data.files.MicroarrayFilter;
import es.usal.bicoverlapper.data.files.SyntrenFilter;
import es.usal.bicoverlapper.data.files.TextFileFilter;
import es.usal.bicoverlapper.data.files.XmlFileFilter;
import es.usal.bicoverlapper.kernel.BicOverlapperWindow;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Configuration;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.WorkDesktop;
import es.usal.bicoverlapper.kernel.configuration.ConfigurationHandler;
import es.usal.bicoverlapper.kernel.configuration.DiagramConfiguration;
import es.usal.bicoverlapper.kernel.configuration.WordCloudDiagramConfiguration;
import es.usal.bicoverlapper.kernel.configuration.panels.DownloadPanel;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.WordCloudDiagram;

/**
 * Class that handles the File Menu Options
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * @version 3.2, 26/3/2007
 */
public class FileMenuManager implements ActionListener, MicroarrayRequester {
	
	private BicOverlapperWindow ventana;
	private BufferedReader pathReader;
	private BufferedWriter pathWriter;

	private BufferedReader recentReader;
	private BufferedWriter recentWriter;
	
	private MicroarrayFilter microFilter=new MicroarrayFilter();
	
	public Session sesion;
	private String path;
	private boolean addVista;
	private File fichero;
	private JDesktopPane desktop;
	private boolean loadingSession;
	private Document documento;
	
	
	
	/**
	 * Constructor to build a MenuManager
	 * 
	 * @param window <code>BicOverlapperWindow</code> that will contain the menu that this manager controls
	 */
	public FileMenuManager(BicOverlapperWindow window) {
		this.ventana = window;
	}
	
	
	/**
	 * Method invoked each time that an option in the view menu is clicked
	 */
	public void actionPerformed(ActionEvent e) {
		
		String defaultPath="";
		try{
			pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/path.txt"));
			defaultPath=pathReader.readLine();
			if(defaultPath==null)	defaultPath="";
			}catch(IOException ex){System.err.println("pathReader has no information"); defaultPath="";}
		
		if(e.getActionCommand().equals("Load Network"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(new SyntrenFilter());
		selecFile.addChoosableFileFilter(new GMLFilter());
		selecFile.setCurrentDirectory(new File(defaultPath));
		int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Load Network");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) 
			{
			//Si no hay TRN pero hay microarray dejamos en la misma sesión
			fichero = selecFile.getSelectedFile();
			path=fichero.getAbsolutePath();
			if(selecFile.getFileFilter().getDescription().contains("yntren"))
					readTRN("syntren");
			else	readTRN("gml");
			}
	}
	else if(e.getActionCommand().equals("Load Expression Data"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(microFilter);
		File f=new File(defaultPath);
		selecFile.setCurrentDirectory(f);
		int returnval = selecFile.showDialog(
			(Component)e.getSource(),"Load Expression Data");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) 
			{
			selecFile.setVisible(false);
			fichero = selecFile.getSelectedFile();
			path=fichero.getPath();
			readMicroarray();
			}
	}
	else if(e.getActionCommand().equals("Load Groups"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(new BiclusterResultsFilter());
		selecFile.setCurrentDirectory(new File(defaultPath));
		
		int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Open Biclustering Results");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) {
			fichero = selecFile.getSelectedFile();
			path=fichero.getAbsolutePath();
			readBiclustering();
			}
	}
		
	//------------------- EXPORT SELECTION ------------------
	else if(e.getActionCommand().equals("Export Selection"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(new TextFileFilter());
		selecFile.setCurrentDirectory(new File(defaultPath));
		
		int returnval = selecFile.showSaveDialog((Component)e.getSource());
		 
		if(returnval == JFileChooser.APPROVE_OPTION) 
			{
			File fichero = selecFile.getSelectedFile();
			
			String path=selecFile.getCurrentDirectory().getPath();
			writeSelection(fichero.getName(), path);
			}
		}
		
		//------------------- Download AE experiment ------------------
	else if(e.getActionCommand().equals("Download AE experiment"))
		{
		DownloadPanel dp=new DownloadPanel(this);
		dp.setLocation((ventana.getWidth()-dp.getWidth())/2, (ventana.getHeight()-dp.getHeight())/2);
		dp.setVisible(true);
		}
	//------------------------- CARGAR CONFIGURACION -------------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s17")))
		{
			JFileChooser selecFile = new JFileChooser();
			selecFile.addChoosableFileFilter(new XmlFileFilter());
			
			int returnval = selecFile.showDialog((Component)e.getSource(),Translator.instance.menuLabels.getString("s21"));
			
			if(returnval == JFileChooser.APPROVE_OPTION){
				File fichero = selecFile.getSelectedFile();
				/*Session sesion = ventana.getActiveWorkDesktop().getSession();
				es.usal.bicoverlapper.kernel.configuration.ConfigurationLoader.loadConfiguration(sesion,fichero);*/
				loadSession(fichero);
			}			
		}
		//-------------------- GUARDAR CONFIGURACIÓN -------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s18"))
				&& ventana.isActiveWorkDesktop())
			{
			JFileChooser selecFile = new JFileChooser();
			selecFile.addChoosableFileFilter(new XmlFileFilter());
			selecFile.setCurrentDirectory(new File(defaultPath));
			
			int returnval = selecFile.showSaveDialog((Component)e.getSource());
			if(returnval == JFileChooser.APPROVE_OPTION)
				{
				String s=selecFile.getSelectedFile().getAbsolutePath();
				s=s.contains(".")?s:s.concat(".xml");
				saveSession(s);
				}
			}
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("openLastProject")))
			{
			//if(sesion!=null)	closeSession();
			if(sesion!=null && ventana.getActiveWorkDesktop()!=null)
				{
				ventana.getActiveWorkDesktop().getPanel().removeAll();
				ventana.getActiveWorkDesktop().getPanel().setName("");
				ventana.getDesktop().setTitleAt(0, "");
				}
			loadSession(new File("es/usal/bicoverlapper/data/recent1.xml"));
			}
		else if(e.getActionCommand().charAt(1)==')')
			{
			//if(sesion!=null)	closeSession();
			if(sesion!=null && ventana.getActiveWorkDesktop()!=null)	ventana.getActiveWorkDesktop().getPanel().setName("");
			loadSession(new File("es/usal/bicoverlapper/data/recent"+e.getActionCommand().charAt(0)+".xml"));
			}
		
		//----------------------- SALIR -------------------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s19")))
		{
			System.exit(0);
		}
	}
	
	public void downloadExperiment(String id, String path)
		{
		System.out.println("Downloading experiments "+path);
		fichero=new File(path);
		prepareDesktop();
		boolean error=false;
		try {
			sesion.analysis.loadRscripts();
			sesion.analysis.downloadExperiment(id, path);
			sesion.reader.readMicroarray(path, sesion, this);
			sesion.microarrayPath=fichero.getAbsolutePath();
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
		
		
		}
	
	public void receiveMatrix(int status)
	{
		System.out.println("Finished microarray data reading with status: "+status);
		//---
		if(status==0)//&& sesion.getMicroarrayData()!=null)
			{
			// Actualizar las ventanas activas		
			sesion.microarrayPath=fichero.getAbsolutePath();
			try{
			pathWriter=new BufferedWriter(new FileWriter("es/usal/bicoverlapper/data/path.txt"));
			pathWriter.write(sesion.microarrayPath);
			pathWriter.close();
			}catch(IOException ex){ex.printStackTrace();}
			
				
			ventana.analysisMenu.setEnabled(true);
			
			ventana.viewMenu.setEnabled(true);
			ventana.menuViewParallelCoordinates.setEnabled(true);
			ventana.menuViewHeatmap.setEnabled(true);
			
			ventana.menuArchivoExportSelection.setEnabled(true);
			
			if(addVista)	
				ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
			else			
				{
				JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
				if(p.getName().length()>0 && !	p.getName().contains(".txt"))	
					{
					ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
					p.setName(p.getName()+" | "+fichero.getName());
					}
				else
					{
					ventana.getDesktop().setTitleAt(0, fichero.getName());
					p.setName(fichero.getName());
					}
				}
			sesion.updateData();
			sesion.analysis.setMicroarrayData(sesion.getMicroarrayData());
			if(loadingSession)
				loadSessionAfterMicroarray();
			}

	}

	/**
	 * Closes the current session
	 */
	public void closeSession()
		{
		sesion.restart();
		ventana.getDesktop().removeAll();
		}
	public void loadSession(File file){
		
		try {
			loadingSession = true;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(
				new org.xml.sax.ErrorHandler(){
					
					public void fatalError(SAXParseException exception) throws SAXException {}
					
					public void error(SAXParseException e) throws SAXParseException {
						throw e;
					}
					
					public void warning(SAXParseException err) throws SAXParseException{
						System.out.println("** Warning"
								+ ", line " + err.getLineNumber()
								+ ", uri " + err.getSystemId());
						System.out.println("   " + err.getMessage());
					}
				}
			);
			documento = builder.parse(file);
			
			//1) Load files in paths
			NodeList micro = documento.getElementsByTagName("microarray_path");
			String mp=((Element)micro.item(0)).getFirstChild().getNodeValue();
			fichero=new File(mp);
			path=fichero.getPath();
			readMicroarray();
			//When the microarray is read, the session continues loading at loadSessionAfterMicroarray
			
		} catch (SAXException sxe) {
			// error generado durante el parsing
			Exception x = sxe;
			if(sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();
		} catch (ParserConfigurationException pce) {
			// el parser con las opciones especificadas no se puede construir
			pce.printStackTrace();
		} catch (IOException ioe){
			// error E/S
			ioe.printStackTrace();
		} catch (Exception e){
		// error E/S
		e.printStackTrace();
		}
	}
	
	public void loadSessionAfterMicroarray()
		{
		try{
			loadingSession=false;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(
				new org.xml.sax.ErrorHandler()
					{
					public void fatalError(SAXParseException exception) throws SAXException {}
					
					public void error(SAXParseException e) throws SAXParseException {
						throw e;
					}
					
					public void warning(SAXParseException err) throws SAXParseException{
						System.out.println("** Warning"
								+ ", line " + err.getLineNumber()
								+ ", uri " + err.getSystemId());
						System.out.println("   " + err.getMessage());
					}
				}
			);
		
		//1) Load biclusters and trn
		NodeList bic = documento.getElementsByTagName("biclusters_path");
		if(bic!=null && bic.item(0)!=null)
			{
			sesion.biclusteringPath=((Element)bic.item(0)).getFirstChild().getNodeValue();
			fichero=new File(sesion.biclusteringPath);
			path=fichero.getAbsolutePath();
			readBiclustering();
			}
		
		NodeList trn = documento.getElementsByTagName("trn_path");
		if(trn!=null && trn.item(0)!=null)
			{
			sesion.trnPath=((Element)trn.item(0)).getFirstChild().getNodeValue();
			fichero=new File(sesion.trnPath);
			path=fichero.getAbsolutePath();
			
			readTRN("syntren");//TODO: Change to different types
			}
		
		//2) Load Visualizations
		ConfigurationHandler config = new ConfigurationHandler();
		NodeList ventanas = documento.getElementsByTagName("window");
		
		for(int i = 0; i < ventanas.getLength(); i++)
			{
			Element ventana = (Element) ventanas.item(i);
			// procesamos cada nodo ventana para construir su configuracion asociada
			Node nodo = ventana.getFirstChild();
			int identificador = new Integer(nodo.getFirstChild().getNodeValue()).intValue();
							
			nodo = nodo.getNextSibling();
			String nombre = nodo.getFirstChild().getNodeValue();
			
			nodo = nodo.getNextSibling();
			Element elemento = (Element)nodo;
			int posX, posY;
			posX = Integer.valueOf(elemento.getAttribute("x")).intValue();
			posY = Integer.valueOf(elemento.getAttribute("y")).intValue();
			
			nodo = nodo.getNextSibling();
			elemento = (Element)nodo;
			Dimension tam = new Dimension(Integer.valueOf(elemento.getAttribute("width")).intValue(),
										  Integer.valueOf(elemento.getAttribute("height")).intValue());
			
			nodo=nodo.getNextSibling();
			elemento=(Element)nodo;
			
			//if(identificador==Configuration.CLOUD_ID)	
				DiagramConfiguration configventana = new DiagramConfiguration(identificador, nombre, posX, posY, tam);
			//else										
			//	WordCloudDiagramConfiguration configventana = new WordCloudDiagramConfiguration(identificador, nombre, posX, posY, tam);
				
			// procesar paleta de colores
			if(nodo!=null)
				{
				NodeList colores = nodo.getChildNodes();
				for(int j = 0; j < colores.getLength(); j++)
					{
					Element color = (Element)colores.item(j);
					Color color2 = new Color(Integer.valueOf(color.getAttribute("red")).intValue(),
										     Integer.valueOf(color.getAttribute("green")).intValue(),
										     Integer.valueOf(color.getAttribute("blue")).intValue());
					configventana.addColor(color2);
					}
				}
			// procesar lista de anclajes TODO: By now, the hooks are ignored
			/*nodo = nodo.getNextSibling();
			nodo.getNodeName()
			if(nodo != null){
				NodeList anclajes = nodo.getChildNodes();
				for(int j = 0; j < anclajes.getLength(); j++){
					configventana.addHook(anclajes.item(j).getFirstChild().getNodeValue());
				}
				}*/
			//process settings dependend on the type of window	
			if(identificador==Configuration.CLOUD_ID)	
				{
				WordCloudDiagramConfiguration wccd = new WordCloudDiagramConfiguration(configventana);
				nodo =nodo.getNextSibling();
				elemento=(Element)nodo;
				wccd.textIndex=Integer.valueOf(elemento.getAttribute("text")).intValue();
				wccd.splitIndex=Integer.valueOf(elemento.getAttribute("split")).intValue();
				wccd.sizeIndex=Integer.valueOf(elemento.getAttribute("size")).intValue();
				wccd.ontologyIndex=Integer.valueOf(elemento.getAttribute("ontology")).intValue();
				
				config.addWindowConfiguration(wccd);
				}
			else	config.addWindowConfiguration(configventana);
			}
		this.sesion.setConfig(config);
		
		//2) Set selection
		LinkedList<Integer> g=new LinkedList<Integer>();
		NodeList genes = documento.getElementsByTagName("gene");
		for(int i = 0; i < genes.getLength(); i++)
			{
			Element gene = (Element) genes.item(i);
			g.add(new Integer(gene.getFirstChild().getNodeValue()));
			}
		LinkedList<Integer> c=new LinkedList<Integer>();
		NodeList conditions = documento.getElementsByTagName("condition");
		for(int i = 0; i < conditions.getLength(); i++)
			{
			Element condition = (Element) conditions.item(i);
			c.add(new Integer(condition.getFirstChild().getNodeValue()));
			}
		BiclusterSelection bs=new BiclusterSelection(g,c);
		if(bs.getGenes().size()>0 || bs.getConditions().size()>0)
			sesion.setSelectedBiclustersExcept(bs, "");
		} catch (ParserConfigurationException pce) {
			// el parser con las opciones especificadas no se puede construir
			pce.printStackTrace();
		} catch (Exception e){
		// error E/S
		e.printStackTrace();
		}
	}
	
	public ArrayList<String> recentFileList()
		{
		ArrayList<String> recent=new ArrayList<String>();
		try{
		for(int i=1;i<=5;i++)
			{
			File file=new File("es/usal/bicoverlapper/data/recent"+i+".xml");
			if(!file.exists())	break;
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(
				new org.xml.sax.ErrorHandler(){
					
					public void fatalError(SAXParseException exception) throws SAXException {}
					
					public void error(SAXParseException e) throws SAXParseException {
						throw e;
					}
					
					public void warning(SAXParseException err) throws SAXParseException{
						System.out.println("** Warning"
								+ ", line " + err.getLineNumber()
								+ ", uri " + err.getSystemId());
						System.out.println("   " + err.getMessage());
					}
				}
			);
			documento = builder.parse(file);
			
			//1) Load files in paths
			String name=i+") ";
			/*NodeList node = documento.getElementsByTagName("microarray_path");
			if(node!=null && node.item(0)!=null)
				{
				String s=((Element)node.item(0)).getFirstChild().getNodeValue().replace("\\", "/");
				name+=s.substring(s.lastIndexOf("/")+1)+" | ";
				}
			node = documento.getElementsByTagName("biclusters_path");
			if(node!=null && node.item(0)!=null)
				{
				String s=((Element)node.item(0)).getFirstChild().getNodeValue().replace("\\", "/");
				name+=s.substring(s.lastIndexOf("/")+1)+" | ";
				}
			node = documento.getElementsByTagName("trn_path");
			if(node!=null && node.item(0)!=null)
				{
				String s=((Element)node.item(0)).getFirstChild().getNodeValue().replace("\\", "/");
				name+=s.substring(s.lastIndexOf("/"))+" | ";
				}*/
			NodeList node = documento.getElementsByTagName("project_path");
			if(node!=null && node.item(0)!=null)
				{
				String s=((Element)node.item(0)).getFirstChild().getNodeValue().replace("\\", "/");
				name+=s.substring(s.lastIndexOf("/")+1);
				}
			recent.add(name);
			}
		}catch(Exception e){e.printStackTrace(); return null;}
		return recent;
		}
	public void saveSession(String fileName)
		{
		try {
			if(fileName==null)		return;
			File file=null;
			//TODO: Pass recent1 to recent2... and delete recent5, for example
			int cont=5;
			file=new File("es/usal/bicoverlapper/data/recent"+cont+".xml");
			while(!file.exists() && cont>0)
				{
				cont--;
				file=new File("es/usal/bicoverlapper/data/recent"+cont+".xml");
				}
			for(int i=cont;i>0;i--)
				{
				file=new File("es/usal/bicoverlapper/data/recent"+i+".xml");
				if(i==5)	file.delete();
				else		file.renameTo(new File("es/usal/bicoverlapper/data/recent"+(i+1)+".xml"));
				}
			
			file=new File(fileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation implementation = builder.getDOMImplementation();
			DocumentType doctype = implementation.createDocumentType("project", null,"project.dtd");
			Document documento = implementation.createDocument(null, "project", doctype);
			
			//1) Write paths of the open files
			Element paths = documento.createElement("paths");
		
			Element projectPath = documento.createElement("project_path");
			projectPath.appendChild(documento.createTextNode(fileName));
			paths.appendChild(projectPath);
		
			if(this.sesion.microarrayPath!=null)
				{
				Element microPath = documento.createElement("microarray_path");
				microPath.appendChild(documento.createTextNode(sesion.microarrayPath));
				paths.appendChild(microPath);
				}
			if(sesion.biclusteringPath!=null)
				{
				Element bicPath = documento.createElement("biclusters_path");
				bicPath.appendChild(documento.createTextNode(sesion.biclusteringPath));
				paths.appendChild(bicPath);
				}
			if(sesion.trnPath!=null)
				{
				Element netPath = documento.createElement("trn_path");
				netPath.appendChild(documento.createTextNode(sesion.trnPath));
				paths.appendChild(netPath);
				}
			documento.getDocumentElement().appendChild(paths);
			
			//2) Write selection if any
			if(this.sesion.getSelectedBicluster()!=null)
				{
				Element selection=documento.createElement("selection");
				Element genes=documento.createElement("genes");
				for(Integer g:sesion.getSelectedBicluster().getGenes())
					{
					Element gene = documento.createElement("gene");
					gene.appendChild(documento.createTextNode(g.toString()));
					genes.appendChild(gene);
					}
				selection.appendChild(genes);
				Element conditions=documento.createElement("conditions");
				for(Integer c:sesion.getSelectedBicluster().getConditions())
					{
					Element condition = documento.createElement("condition");
					condition.appendChild(documento.createTextNode(c.toString()));
					conditions.appendChild(condition);
					}
				selection.appendChild(conditions);
				documento.getDocumentElement().appendChild(selection);
				}
			
			//3) Write configuration of the open windows
			ConfigurationHandler config=sesion.getConfig();
			
			Element config_Ventanas = documento.createElement("windows");
						
			DiagramConfiguration configVentana;
						
			for(int i = 0; i < config.getSizeConfig(); i++){
				
				configVentana = config.getWindowConfiguration(i);
				
				Element ventana = documento.createElement("window");
								
				Element identificador = documento.createElement("id");
				identificador.appendChild(documento.createTextNode(new Integer(configVentana.getId()).toString()));
				ventana.appendChild(identificador);
				
				Element nombre = documento.createElement("name");
				nombre.appendChild(documento.createTextNode(configVentana.getTitle()));
				ventana.appendChild(nombre);
								
				Element posicion = documento.createElement("location");
				Integer posX = new Integer(configVentana.getPosX());
				posicion.setAttribute("x", posX.toString());
				Integer posY = new Integer(configVentana.getPosY());
				posicion.setAttribute("y", posY.toString());
				ventana.appendChild(posicion);
				
				Element tamanyo = documento.createElement("size");
				Integer tamX = new Integer(configVentana.getDim().width);
				tamanyo.setAttribute("width", tamX.toString());
				Integer tamY = new Integer(configVentana.getDim().height);
				tamanyo.setAttribute("height", tamY.toString());
				ventana.appendChild(tamanyo);

				Element paleta = documento.createElement("palette");
				for(int j = 0; j < configVentana.getNumberOfColors(); j++){
					Element color = documento.createElement("color");
					Color color2 = configVentana.getColor(j);
					
					Integer componente = new Integer(color2.getRed());
					color.setAttribute("red", componente.toString());
					componente = new Integer(color2.getGreen());
					color.setAttribute("green", componente.toString());
					componente = new Integer(color2.getBlue());
					color.setAttribute("blue", componente.toString());
					
					paleta.appendChild(color);
				}
				ventana.appendChild(paleta);
				
				//TODO: Configuration of specific windows (for example, comboboxes on wc)
				if(configVentana.getId()==Configuration.CLOUD_ID)
					{
					Element configwc = documento.createElement("wordCloudSettings");
							
					WordCloudDiagram wc=(WordCloudDiagram)(sesion.getDiagramWindow(configVentana.getTitle()).getDiagram());
					configwc.setAttribute("text", wc.getMenuCloud().text.getSelectedIndex()+"");
					configwc.setAttribute("split", wc.getMenuCloud().split.getSelectedIndex()+"");
					configwc.setAttribute("size", wc.getMenuCloud().size.getSelectedIndex()+"");
					configwc.setAttribute("ontology", wc.getMenuCloud().ontology.getSelectedIndex()+"");
					ventana.appendChild(configwc);
					}
				config_Ventanas.appendChild(ventana);
			}
			
			documento.getDocumentElement().appendChild(config_Ventanas);
			documento.getDocumentElement().normalize();
			Source source = new DOMSource(documento);
			source.setSystemId("project.dtd");
			Result result = new StreamResult(file);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setURIResolver(transformer.getURIResolver());
			transformer.transform(source, result);
			
			if(fileName!=null)
				copy(file, new File("es/usal/bicoverlapper/data/recent1.xml"));
		} catch (ParserConfigurationException pce) {
			// el parser con las opciones especificadas no se puede construir
			pce.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		}
	
	}
	
	private void copy(File fromFile, File toFile)
		{
		FileInputStream from = null;
	    FileOutputStream to = null;
	    try {
	      from = new FileInputStream(fromFile);
	      to = new FileOutputStream(toFile);
	      byte[] buffer = new byte[4096];
	      int bytesRead;

	      while ((bytesRead = from.read(buffer)) != -1)
	        to.write(buffer, 0, bytesRead); // write
	    } catch(Exception e){e.printStackTrace();}
	    finally {
	      if (from != null)
	        try {
	          from.close();
	        } catch (IOException e) {
	          ;
	        }
	      if (to != null)
	        try {
	          to.close();
	        } catch (IOException e) {
	          ;
	        }
	    }
		}
	private void writeSelection(String file, String path)
		{
		try{
		BufferedWriter bw;
		if(!file.contains(".")) bw=new BufferedWriter(new FileWriter(path+"\\"+file+".txt"));
		else					bw=new BufferedWriter(new FileWriter(path+"\\"+file));
		
		if(ventana.getActiveWorkDesktop()!=null && ventana.getActiveWorkDesktop().getSession().getSelectedBicluster()!=null)
			{
			BiclusterSelection bs=ventana.getActiveWorkDesktop().getSession().getSelectedBicluster();
			
			bw.write(bs.getGenes().size()+" "+bs.getConditions().size());
			bw.newLine();
			ArrayList<String> g=ventana.getActiveWorkDesktop().getSession().getMicroarrayData().getGeneNames(bs.getGenes());
			
			for(int i=0;i<bs.getGenes().size();i++)
				{
				
				bw.write(g.get(i)+" ");
				}
			bw.newLine();
			ArrayList<String> c=ventana.getActiveWorkDesktop().getSession().getMicroarrayData().getConditionNames(bs.getConditions());
			for(int i=0;i<c.size();i++)
				{
				bw.write(c.get(i)+" ");
				}
			bw.close();
			}
		else
			{
			JOptionPane.showMessageDialog(null,
					"No selection done, select some genes or conditions first",
                    Translator.instance.warningLabels.getString("s2"),JOptionPane.ERROR_MESSAGE);
			}
		}catch(Exception e){e.printStackTrace();}
		}
	
	public void readTRN(String fileType)
		{
		boolean error=false;
		boolean addVista=false;
		if(this.ventana.getActiveWorkDesktop()!=null)
			sesion = this.ventana.getActiveWorkDesktop().getSession();
		else
			{
			desktop = new JDesktopPane();
			desktop.setName(fichero.getName());
			addVista=true;
			sesion = new Session(desktop, ventana);
			}
		
		try {
			sesion.reader.readTRN(path, fichero, sesion, fileType);
			sesion.trnPath=fichero.getAbsolutePath();
			
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
			sesion.updateData();
			
			try{
				pathWriter=new BufferedWriter(new FileWriter("es/usal/bicoverlapper/data/path.txt"));
				pathWriter.write(path);
				pathWriter.close();
				}catch(IOException ex){ex.printStackTrace();}
				
			ventana.viewMenu.setEnabled(true);
			ventana.menuViewTRN.setEnabled(true);
			ventana.analysisMenu.setEnabled(true);
			}
		}
	
	private void prepareDesktop()
		{
		desktop = new JDesktopPane();
		desktop.setName(fichero.getName());
		
		if(sesion==null)
			{
			addVista=true;
			sesion = new Session(desktop, ventana);
			}
		else
			{
			sesion.setDesktop(desktop);
			sesion.setMainWindow(ventana);
			}
		
		ventana.analysisMenu.setEnabled(false);
		ventana.viewMenu.setEnabled(false);
		}
	
	public void readMicroarray()
		{
		boolean error=false;
		prepareDesktop();
		System.out.println("---desktop ready, session started");
		try {
			sesion.reader.readMicroarray(path, sesion, this);
			sesion.microarrayPath=fichero.getAbsolutePath();
			
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
		}
	
	public void readBiclustering()
		{
		desktop = new JDesktopPane();
		desktop.setName(fichero.getName());
		boolean addVista=false;
		if(this.ventana.getActiveWorkDesktop()!=null)
			sesion = this.ventana.getActiveWorkDesktop().getSession();
		else
			{
			addVista=true;
			sesion = new Session(desktop, ventana);
			}
	
		sesion.reader.readBiclusterResults(path, fichero.getName(), sesion);
		sesion.biclusteringPath=fichero.getAbsolutePath();
		}
	}