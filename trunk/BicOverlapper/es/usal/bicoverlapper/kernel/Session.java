package es.usal.bicoverlapper.kernel;

/*
import infovis07contest.data.Involvement;
import infovis07contest.data.Movie;
import infovis07contest.data.MovieDB;
import infovis07contest.data.Person;
*/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
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

import org.rosuda.JRI.Rengine;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



import es.usal.bicoverlapper.analysis.Analysis;
import es.usal.bicoverlapper.data.BubbleData;
import es.usal.bicoverlapper.data.DataLayer;
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.data.NetworkData;
import es.usal.bicoverlapper.data.files.DataReader;
import es.usal.bicoverlapper.kernel.configuration.ConfigurationHandler;
import es.usal.bicoverlapper.kernel.configuration.DiagramConfiguration;
import es.usal.bicoverlapper.kernel.configuration.WordCloudDiagramConfiguration;
import es.usal.bicoverlapper.kernel.panels.SearchPanel;
import es.usal.bicoverlapper.kernel.panels.ShowPanel;
import es.usal.bicoverlapper.kernel.panels.SortPanel;
import es.usal.bicoverlapper.utils.ArrayUtils;
import es.usal.bicoverlapper.utils.CustomColor;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.OverlapperDiagram;
import es.usal.bicoverlapper.visualization.diagrams.BubblesDiagram;
import es.usal.bicoverlapper.visualization.diagrams.HeatmapDiagram;
import es.usal.bicoverlapper.visualization.diagrams.ParallelCoordinatesDiagram;
import es.usal.bicoverlapper.visualization.diagrams.NetworkDiagram;
import es.usal.bicoverlapper.visualization.diagrams.WordCloudDiagram;



/**
 * Class that manages the current state of data, sending and retrieving this information of the 
 * interaction with active Diagramas. By now, four sources of data are being supported:
 * <p>
 * 	1) MultidimensionalData: expression level data taken as multidimensional data, for ParallelCoordinates
 * <p>
 * 	2) HeatmapData: more detailed, prefuse oriented data for Heatmap (newer versions will fuse it with 1))
 * <p>
 * 	3) BiclusterData: biclusters found data
 * <p>
 * 	4) TRNData: Biological Network data
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 */
public class Session implements KeyListener {
	
	// Capa de datos para interaccion
	private DataLayer capaDatos;
	private boolean isPersonas=false;
	
	public DataReader reader;
	public BicOverlapperWindow mainWindow;
	
	// Datos del fichero de trabajo
	private MultidimensionalData datos 		= null;
	private NetworkData  datosTRN 		= null;
	private BubbleData datosBubble 	= null;
	private MicroarrayData datosMicroarray 	= null;
	private String biclusterDataFile = null;
	//private Analysis biclustering =null;
	
	private boolean datosCargados;
	private boolean datosTRNCargados;
	private boolean datosBubbleCargados;
	private boolean datosMicroarrayCargados;
	private boolean datosBiclusterCargados;
//	Atributos para compartir de bicluster
	private BiclusterSelection selectedBicluster=null;
	private BiclusterSelection hoveredBicluster=null;
	
	private LinkedList<BiclusterSelection> selectionLog=null;
	private int contLog=-1;
	
	private boolean cambioGenes;
	private boolean cambioTRNGenes;
	private Vector<Double> expresionesCondicion; //Niveles de expresi�n de una determinada condici�n seleccionada (en el heatmap)
	
	
	// Atributos del frame pincipal de la aplicacion
	private JDesktopPane desktop;
			
	// Handlers de las ventanas creadas
	
	private Vector<DiagramWindow> ventanas;
	private Vector<DiagramWindow> grupoVentanasDefecto;
	
	private int numVentanaCoordenadasParalelas = 1;
	private int numVentanaBubbles = 1;
	private int numVentanaBubbleGraph = 1;
	private int numVentanaTRN = 1;
	private int numVentanaHeatmap = 1;
	private int numVentanaWordCloud = 1;
	private int numVentanas=1;
	
	private int numColores=0; //N�mero de colores que se han ido a�adiendo con BiclusVis (aka el Overlapper)
	
	private static final int xOffset = 30, yOffset = 30;
	private Color selectionColor;
	private Color searchColor;
	private Color hoverColor;
	private Color bicSet1Color;
	private Color bicSet2Color;
	private Color bicSet3Color;
	public Color lowExpColor;
	public Color avgExpColor;
	public Color hiExpColor;
	
	public String microarrayPath=null;
	public String biclusteringPath=null;
	public String trnPath=null;
	private boolean ctrlPressed=false;
	private boolean undoOrRedo=false;
	
	/**
	 * Biclustering class bound to this microarray data.  
	 */
	public Analysis analysis= null;
	private SearchPanel searchPanel;
	private ShowPanel showPanel;
	private SortPanel sortPanel;
	
	
	

	/**
	 * Constructor of the <code>Session</code> layer, linked to a <code>JDesktopPane</code>,
	 * with empty data
	 * 
	 * @param desktop <code>JDesktopPane</code> linked to this <code>Session</code>.
	 */
	public Session(JDesktopPane desktop, BicOverlapperWindow window){
		analysis=new Analysis();
		this.datosCargados = false;
		this.desktop = desktop;
		this.mainWindow = window;
		this.capaDatos = new DataLayer(this);
		this.ventanas = new Vector<DiagramWindow>(0,1);
		this.grupoVentanasDefecto = new Vector<DiagramWindow>(0,1);
		
		selectionLog=new LinkedList<BiclusterSelection>();
		selectionLog.add(new BiclusterSelection(new LinkedList<Integer>(), new LinkedList<Integer>()));
		contLog=0;
		//With black background
		//this.selectionColor=Color.BLUE;
		//this.searchColor=Color.MAGENTA;
		//this.hoverColor=Color.YELLOW;
		
		//With white background
		this.selectionColor=Color.BLUE;
		this.searchColor=new Color(0,150,0);
	//	this.hoverColor=Color.ORANGE.darker();
		this.hoverColor=Color.GREEN;
		this.bicSet1Color=CustomColor.getGoodColor(0);
		this.bicSet2Color=CustomColor.getGoodColor(1);
		this.bicSet3Color=CustomColor.getGoodColor(2);
		
		this.lowExpColor=Color.BLUE;
		this.hiExpColor=Color.RED;
		this.avgExpColor=Color.WHITE;
		
		reader=new DataReader();
		return;
	}
	
	/**
	 * Devuelve la posicion donde crear una nueva ventana.
	 * 
	 * @return <code>Point</code> con la posicion donde crear la nueva ventana.
	 */
	Point getNuevaVentanaPos(){
		return new Point(xOffset*ventanas.size(), yOffset*ventanas.size());
	}
	
	/**
	 * Checks if there is a Diagram already registered with this name
	 * 
	 * @param name String with the name to be checked
	 * @return true if a Diagram with this name exists, false otherwise.
	 */
	public boolean existsName(String name)
		{
		boolean existe = false;
		for(int i = 0; i < this.ventanas.size(); i++)
			{
			if(this.ventanas.elementAt(i).getTitle().equals(name))
				{
				existe = true;
				break;
				}
			}
		return existe;
		}
	
	/**
	 * Returns the data layer of this Session
	 * TODO: NOTE: The data layer is a typical multidimensional variables dataLayer
	 * As such, only the expression levels are considered in this way, being genes (samples)
	 * variables, and conditions (experiments) dimensions
	 * @return <code>DataLayer</code> of this <code>Session</code>.
	 */
	public DataLayer getDataLayer(){
		return this.capaDatos;
	}
	
	/**
	 * Restart the <code>Session</code> closing all registered Diagramas
	 * NOTE: data sets are not deleted
	 *
	 */
	public void restartSession(){
		
		int numVentanas = ventanas.size();
		if(numVentanas != 0){
			for(int i = 0; i < numVentanas; i++){
				DiagramWindow ventana = ventanas.firstElement();
				ventana.dispose();
			}
		}
		
		this.grupoVentanasDefecto.clear();
		this.numVentanaCoordenadasParalelas = 1;
		analysis.unloadMatrix();
		}
	
	/**
	 * Sets the configuration specified by the configuration Handler
	 * STILL IN DEVELOPMENT
	 * 
	 */
	public void setConfig(ConfigurationHandler config){
		
		this.restartSession();
		
		for(int i = 0; i < config.getSizeConfig(); i++){
			
			DiagramConfiguration configVentana = config.getWindowConfiguration(i);
			
			String nombre = configVentana.getTitle();
			int posX = configVentana.getPosX(), posY = configVentana.getPosY();
			Dimension dim = configVentana.getDim();			
			
			//boolean p=configVentana.isPersonas();
			//this.setPersonas(configVentana.isPersonas());
		//	System.out.println("Sesi�n tiene "+this.isPersonas);
			
			Vector<String> anclajes = new Vector<String>(0,1);
			for(int j = 0; j < configVentana.getNumberOfHooks(); j++){
				anclajes.add(configVentana.getHook(j));
			}
			if(anclajes.isEmpty())
				anclajes = null;
			
			Color[] paleta = new Color[configVentana.getNumberOfColors()];
			for(int j = 0; j < paleta.length; j++){
				paleta[j] = configVentana.getColor(j);
			}
			DiagramWindow ventana = null;
			switch (configVentana.getId()) {
				
			case es.usal.bicoverlapper.kernel.Configuration.PARALLEL_COORDINATES_ID:
				ParallelCoordinatesDiagram panelC = new ParallelCoordinatesDiagram(this, dim);
				ventana = new DiagramWindow(this,this.getDesktop(),panelC);
				this.setParallelCoordinates(ventana);
				break;
			case es.usal.bicoverlapper.kernel.Configuration.HEATMAP_ID:
				HeatmapDiagram panelH = new HeatmapDiagram(this, dim);
				ventana = new DiagramWindow(this,this.getDesktop(),panelH);
				panelH.create();
				panelH.run();
				this.setHeatmap(ventana);
				break;
			case es.usal.bicoverlapper.kernel.Configuration.TRN_ID:
				NetworkDiagram panelT = new NetworkDiagram(this, dim);
				ventana = new DiagramWindow(this,this.getDesktop(),panelT);
				this.setTRN(ventana);
				if(this.getTRNData()!=null)
					{
					panelT.create();
					panelT.run();
					}
				break;
			case es.usal.bicoverlapper.kernel.Configuration.BUBBLE_MAP_ID:
				BubblesDiagram panelB = new BubblesDiagram(this, dim);
				ventana = new DiagramWindow(this,this.getDesktop(),panelB);
				if(this.getBiclusterDataFile()!=null)
					{
					panelB.createAxisLayout();
					panelB.run();
					}
				this.setBubbles(ventana);
				break;
			case es.usal.bicoverlapper.kernel.Configuration.OVERLAPPER_ID:
				OverlapperDiagram panelO = new OverlapperDiagram(this, dim);
				ventana = new DiagramWindow(this,this.getDesktop(),panelO);
				if(this.getBiclusterDataFile()!=null)
					{
					panelO.create();
					panelO.run();
					}
				this.setBubbleGraph(ventana);
				break;
			case es.usal.bicoverlapper.kernel.Configuration.CLOUD_ID:
				WordCloudDiagram panelWC = new WordCloudDiagram(this, dim);
				ventana = new DiagramWindow(this,this.getDesktop(),panelWC);
				if(this.getBiclusterDataFile()!=null)
					{
					panelWC.create();
					panelWC.run();
					}
				WordCloudDiagramConfiguration wcdc=(WordCloudDiagramConfiguration)configVentana;
				panelWC.menuCloud.setIndices(wcdc.textIndex, wcdc.splitIndex, wcdc.sizeIndex, wcdc.ontologyIndex);
				this.setWordCloud(ventana);
				break;
			default: // error tipo de ventana
				break;
			}
			ventana.setTitle(nombre);
			ventana.setLocation(posX, posY);
			ventana.setHooks(anclajes);
			ventana.setPalette(paleta);
			ventana.setVisible(true);
		}
		
		//System.out.println("Propagamos (')");
		for(int i = 0; i < this.ventanas.size(); i++){
			DiagramWindow ventana = ventanas.elementAt(i);
			if(ventana.getHooks() != null){
				ventana.setExclusiveGroupStatus(false);
				if(ventana.getDiagram().getDataLayer() == this.capaDatos){
					DataLayer capaDatos = new DataLayer(this.capaDatos);
					ventana.propagar(capaDatos);
				}
			}
		}
	}
	
	
	/**
	 * Selects the elements (rows) that have values above the mean for the columns with value highEFV in the factor highEF and 
	 * low value in the columns with value lowEFV for the factor lowEFV
	 * For high and lowEFV, the wildcard "rest" can be used
	 * 
	 * For example, if we search for genes upregulated in Growth condition "lps", but downregulated for GrowthCondition "control",
	 * 
	 * highEFV - experimental factor value for which the selected elements should have high expression (above the mean)
	 * highEF  - experimental factor of the EFV above
	 * sdsAbove - number of standard deviations that the profiles should be above for every sample of the given EFV
	 * lowEFV  - experimental factor value for which the selected elements should have low expression (below the mean)
	 * lowEF  - experimental factor of the EFV above
	 * sdsAbove - number of standard deviations that the profiles should be below for every sample of the given EFV
	 * 
	 */
	public void setSelection(String highEFV, String highEF, int sdsAbove, String lowEFV, String lowEF, int sdsBelow)
		{

		LinkedList<Integer> genes=datosMicroarray.selectHiLo(highEFV, highEF, sdsAbove, lowEFV, lowEF, sdsBelow);
		LinkedList<Integer> conditions=new LinkedList<Integer>();
		for(int j=0;j<datosMicroarray.getNumConditions();j++)			conditions.add(Integer.valueOf(j));
		
		BiclusterSelection bs=new BiclusterSelection(genes, conditions);
		this.setSelectedBiclustersExcept(bs, "");
		}
	
	public void setSelection(String highEFV, String highEF, String lowEFV, String lowEF)
		{
		setSelection(highEFV, highEF, 0, lowEFV, lowEF, 0);
		}
	/**
	* @deprecated
	*/
	public ConfigurationHandler getConfig(){
		ConfigurationHandler config = new ConfigurationHandler();
		for(int i = 0; i < ventanas.size(); i++){
			DiagramWindow ventana = ventanas.elementAt(i);
						
			DiagramConfiguration configVentana = new DiagramConfiguration(ventana.getId(), ventana.getTitle(),
															ventana.getX(), ventana.getY(),	ventana.getPanelSize());
																							
			Vector<String> anclajes = ventana.getHooks();
			if((anclajes != null) && !anclajes.isEmpty()){
				for(int j = 0; j < anclajes.size(); j++){
					configVentana.addHook(anclajes.elementAt(j));
				}
			}else
				anclajes = null;
			
			Color[] paleta = ventana.getPaleta();
			if(paleta!=null)
				for(int j = 0; j < paleta.length; j++){
					configVentana.addColor(paleta[j]);
				}
			config.addWindowConfiguration(configVentana);
		}
	
		return config;
	}
	
	/**
	 * Shows an info window telling that the data File has been loaded correctly
	 *
	 */
	public void fileLoaded(){
		JOptionPane.showMessageDialog(this.desktop,Translator.instance.configureLabels.getString("s36"),null,JOptionPane.INFORMATION_MESSAGE);		
	}
	
	/**
	 * Stablish the multidimentional data source for the Diagrams (expression levels)
	 * TODO: THIS DATA LAYER WILL DISSAPEAR IN ORDER TO USE OTHER MORE SPECIFIC DATA LAYERS
	 * BY NOW, ONLY ParallelCoordinatesDiagram MAKES USE OF IT
	 * @param data <code>MultidimensionalData</code> with the whole data set of multidimensional variables
	 */
	public void setData(MultidimensionalData data) {
		this.datos = data;
		this.datosCargados = true;
		this.capaDatos.setEjes(datos.fieldAt(0).getName(),datos.fieldAt(1).getName());
		this.updateData();		
	}
	
	/**
	 * Returns MultidimensionalData of this Session
	 * 
	 * @return <code>MultidimensionalData</code> of this Session
	 */
	public MultidimensionalData getData() {
		return this.datos;
	}
	
	/**
	 * Returns all registered DiagramWindows
	 * 
	 * @return <code>Vector</code> with all DiagramWindows registered
	 */
	public Vector<DiagramWindow> getDiagramWindows() {
		return this.ventanas;
	}
	
	/**
	 * Returns an specific DiagramWindow
	 * 
	 * @param name Name of the DiagramWindow to get
	 * @return <code>VentanaPanel</code> cuyo nombre coincide con el string pasado como parametro.
	 */
	public DiagramWindow getDiagramWindow(String name){
		DiagramWindow ventana = null;
		for(int i = 0; i < ventanas.size(); i++){
			DiagramWindow ventana2 = ventanas.elementAt(i);
			if(ventana2.getTitle().equals(name)){
				ventana = ventana2;
				break;
			}
		}
		return ventana;
	}
	
	/**
	 * Elimina la <code>VentanaPanel</code> pasada como parametro de la capa sesion.
	 * 
	 * @param ventana <code>VentanaPanel</code> que queremos sea eliminada.
	 */
	void removeVentana(DiagramWindow ventana) {
		for(int i = 0; i < ventanas.size(); i++){
			DiagramWindow ventanaAux = ventanas.elementAt(i);
			if(ventana.getTitle().equals(ventanaAux.getTitle())){
				ventanas.remove(i);
				removeFromGrupoDefecto(ventanaAux.getTitle());
				break;
			}
		}
	}
	
	
	
	/**
	 * Elimina la ventana cuyo nombre coincida con el parametro del grupo por defecto de actualizacion.
	 * 
	 * @param nomVentana Nombre de la ventana que queremos sea eliminada.
	 */
	void removeFromGrupoDefecto(String nomVentana){
		for(int i = 0; i < grupoVentanasDefecto.size(); i++){
			DiagramWindow ventana = grupoVentanasDefecto.elementAt(i);
			if(ventana.getTitle().equals(nomVentana)){
				grupoVentanasDefecto.remove(i);
				break;
			}
		}
	}
	
	/**
	 * A�ade la ventana pasada como parametro al grupo por defecto de actualizacion.
	 * 
	 * @param ventana <code>VentanaPanel</code> que deseamos add.
	 */
	void addToGrupoDefecto(DiagramWindow ventana){
		this.grupoVentanasDefecto.add(ventana);
	}
	
	// actualizar las ventanas activas menos las uqe tengan por t�tulo
	/**
	 * Updates all active DiagramWindows except the ones with the input name
	 * It is usually used to not update the calling DiagramWindow
	 * @param name	Name of the DiagramWindow not to update
	 */
	public void updateExcept(String name) {
		
		for(int i = 0; i < this.grupoVentanasDefecto.size(); i++){
			DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
			if(name=="" || !ventana.getTitle().contains(name))	
				ventana.updateDiagram();
		}
		
	}
	
	// actualizar las ventanas activas menos las uqe tengan por t�tulo
	/**
	 * Updates all active DiagramWindows except the ones with the input name
	 * It is usually used to not update the calling DiagramWindow
	 * @param name	Name of the DiagramWindow not to update
	 */
	public void updateOnly(String name) {
		
		for(int i = 0; i < this.grupoVentanasDefecto.size(); i++){
			DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
			if(ventana.getTitle().contains(name))	
				ventana.updateDiagram();
		}
		
	}
	
	/**
	 * Updates all active DiagramWindows
	 */
	public void updateAll()
		{
		for(int i = 0; i < this.grupoVentanasDefecto.size(); i++){
			DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
			ventana.updateDiagram();
			}
		}
	
	/**
	 * Sort columns in the matrix following the experimental factor name.
	 *  * Sends a signal to sort columns on PC and heatmaps
	 */
	public void sortColumns(String name)
		{
		int [] columnOrder=this.getMicroarrayData().sortColumnsBy(name);
		
		for(int i = 0; i < this.grupoVentanasDefecto.size(); i++){
			DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
			if(ventana.getTitle().contains("arallel"))
				((ParallelCoordinatesDiagram)ventana.getDiagram()).sortColumns(columnOrder);
			if(ventana.getTitle().contains("eatmap"))
				{
				((HeatmapDiagram)ventana.getDiagram()).setOrder(columnOrder);
				((HeatmapDiagram)ventana.getDiagram()).update();
				}
		}
		}
	
	/**
	 * Updates all active DiagramWindows configurations except the ones with the input name.
	 * It is usually used to not update the calling DiagramWindow
	 * @param name	Name of the DiagramWindow not to update
	 */
	public void updateConfigExcept(String name) {
		
		for(int i = 0; i < this.grupoVentanasDefecto.size(); i++){
			DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
			if(!ventana.getTitle().contains(name))	ventana.getDiagram().updateConfig();
		}
		
	}
	
	
	/**
	 * Check if multidimentional data are loaded
	 * 
	 * @return <code>true</code> if multidimensional data have been loaded, <code>false</code> otherwise.
	 */
	public boolean dataLoaded() {
		return datosCargados;
	}
		
	/**
	 * Updates a set of DiagramWindows 
	 * 
	 * @param updatableIds int[] array with DiagramWindows ids to update
	 */
	public void update(int[] updatableIds) 
		{
		for(int i = 0; i < this.grupoVentanasDefecto.size(); i++)
			{
			DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
			
			if(ArrayUtils.contains(updatableIds,ventana.getId()))
				ventana.updateDiagram();
			}
		}
		
	/**
	 * As above, but only update table information if flag actualizarDatos is true
	 * @param idActualizables
	 * @param actualizarDatos
	 */ 
	void update(int[] idActualizables, boolean actualizarDatos) 
			{
		//	System.out.println("Tenemos "+this.grupoVentanasDefecto.size()+" ventanas en el grupo por defecto");
			//for(int j=0;j<idActualizables.length;j++)	System.out.println("ID actualizable "+idActualizables[j]);
			for(int i = 0; i < this.grupoVentanasDefecto.size(); i++)
				{
				DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
				
				//System.out.println("ID de nuestra ventana "+ventana.getId()+" con nombre "+ventana.getName());
				if(ArrayUtils.contains(idActualizables,ventana.getId()))
					{
					//System.out.println("Actualizando panel coh id "+ventana.getId()+" y nombre "+ventana.getName());
					ventana.updateDiagram();
					}
				}
			}
			
	/**
	 * Updates all DiagramWindows when a new multidimensional data file is loaded
	 */
	public void updateData() {
		
		for(int i = 0; i < this.ventanas.size(); i++){
			DiagramWindow ventana = ventanas.elementAt(i);
			ventana.actualizarDatos();
		}
	}


	/**
	 * Register a ParalallelCoordinates diagram
	 * TODO: TO BE UNIFIED FOR ANY KIND OF DIAGRAM
	 * @param dw <code>DiagramWindow</code> to register
	 */
	public void setParallelCoordinates(DiagramWindow dw) {
		this.ventanas.add(dw);
		this.grupoVentanasDefecto.add(dw);
		this.numVentanaCoordenadasParalelas++;		
		this.numVentanas++;
	}

	/**
	 * Register a ParalallelCoordinates diagram
	 * TODO: TO BE UNIFIED FOR ANY KIND OF DIAGRAM
	 * @param dw <code>DiagramWindow</code> to register
	 */
	public void setHeatmap(DiagramWindow dw) {
		this.ventanas.add(dw);
		this.grupoVentanasDefecto.add(dw);
		this.numVentanaHeatmap++;		
		this.numVentanas++;
	}

	/**
	 * Returns the number of ParallelCoordinatesDiagrams for this Session
	 * 
	 * @return the number of ParallelCoordinatesDiagrams for this Session
	 */
	public int getNumParallelCoordinatesDiagrams(){
		return this.numVentanaCoordenadasParalelas;
	}
	
	/**
	 * Devuelve el panel de escritorio de la vista.
	 * 
	 * @return <code>JDesktopPane</code> de la vista.
	 */
	public JDesktopPane getDesktop() {
		return this.desktop;
	}	
	
	/**
	 * Returns the data of bubble map
	 * @return	BubbleData for this working set
	 */
	public BubbleData getBubbleData() {
		return this.datosBubble;
	}

	/**
	 * Sets the BubbleData for this Working set
	 * @param bubbleData	BubbleData to set in this working set
	 */
	public void setBubbleData(BubbleData bubbleData) {
		this.datosBubble = bubbleData;
		datosBubbleCargados = true;
		this.updateData();		
	}

	/**
	 * Returns the data of Biological Network for this working set
	 * @return	TRNData for this working set
	 */
	public NetworkData getTRNData() 
		{
		return this.datosTRN;
		}

	/**
	 * Sets the TRNData for this Working set
	 * @param TRNData	TRNData to set in this working set
	 */
	public void setTRNData(NetworkData TRNData) {
		this.datosTRN = TRNData;
		datosTRNCargados = true;
		this.updateData();		
	}

	/**
	 * Returns the number of BubbleMapDiagrams for this Session
	 * 
	 * @return the number of BubbleMapDiagrams for this Session
	 */
	public int getNumBubbleMapDiagrams() {
		return numVentanaBubbles;
	}

	/**
	 * Returns the number of TRNDiagrams for this Session
	 * 
	 * @return the number of TRNDiagrams for this Session
	 */
	public int getNumTRNDiagrams() {
		return numVentanaTRN;
	}

	/**
	 * Sets the points selected in the multidimensional data
	 * @param selec	selected points to be managed by the Session layer
	 */
	public void setSelectedPoints(TupleSelection selec)
		{
		this.getDataLayer().setPointSelection(selec);
		}
	
	/**
	 * Sets the points selected in the multidimensional data and updates
	 * all the diagrams except those that contains in its name the text
	 * specified
	 * 
	 * @param selec	selected points
	 * @param noUpdate	Diagrams that contains this String in its name won't be updated
	 */
	public void setSelectedPoints(TupleSelection selec, String noUpdate){
		this.getDataLayer().setPointSelection(selec);
		this.updateExcept(noUpdate);
	}
	
	/**
	 * Checks if TRNData are loaded
	 * @return	true if TRNData are loaded
	 */
	public boolean TRNDataLoaded() {
		return datosTRNCargados;
	}
	
	/**
	 * Checks if BubbleData are loaded
	 * @return	true if BubbleData are loaded
	 */
	public boolean datosBubbleCargados() {
		return datosBubbleCargados;
	}
	
	/**
	 * Register a BubbleMap diagram
	 * TODO: TO BE UNIFIED FOR ANY KIND OF DIAGRAM
	 * @param dw <code>DiagramWindow</code> to register
	 */
	public void setBubbles(DiagramWindow dw) {
		this.ventanas.add(dw);
		this.grupoVentanasDefecto.add(dw);
		this.numVentanaBubbles++;		
		this.numVentanas++;
	}
	
	/**
	 * Register a TRN diagram
	 * TODO: TO BE UNIFIED FOR ANY KIND OF DIAGRAM
	 * @param dw <code>DiagramWindow</code> to register
	 */
	public void setTRN(DiagramWindow dw) {
		this.ventanas.add(dw);
		this.grupoVentanasDefecto.add(dw);
		this.numVentanaTRN++;		
		this.numVentanas++;
	}

	/**
	 * Register a BicOverlapper diagram
	 * TODO: TO BE UNIFIED FOR ANY KIND OF DIAGRAM
	 * @param dw <code>DiagramWindow</code> to register
	 */
	public void setBubbleGraph(DiagramWindow dw) {
		this.ventanas.add(dw);
		this.grupoVentanasDefecto.add(dw);
		this.numVentanaBubbleGraph++;		
		this.numVentanas++;
	}
	
	/**
	 * Register a WordCloud diagram
	 * TODO: TO BE UNIFIED FOR ANY KIND OF DIAGRAM
	 * @param dw <code>DiagramWindow</code> to register
	 */
	public void setWordCloud(DiagramWindow dw) {
		this.ventanas.add(dw);
		this.grupoVentanasDefecto.add(dw);
		this.numVentanaWordCloud++;		
		this.numVentanas++;
	}

	/**
	 * Returns the bicluster(s) selected
	 * @return	BiclusterSelection with genes and conditions in biclusters selected
	 */
	public BiclusterSelection getSelectedBicluster() 
		{
		return selectedBicluster;
		}
	
	
	/**
	 * Returns the genes in the bicluster(s) selected
	 * @return	LinkedList with integer identifiers of genes in the biclusters selected
	 */
	public LinkedList<Integer> getSelectedGenesBicluster() 
	{
	return selectedBicluster.getGenes();
	}
	
	/**
	 * Returns the conditions in the bicluster(s) selected
	 * @return	LinkedList with integer identifiers of conditions in the biclusters selected
	 */
	public LinkedList<Integer> getSelectedConditionsBicluster() 
	{
	if(selectedBicluster!=null)	return selectedBicluster.getConditions();
	else						return new LinkedList<Integer>();
	}

	
	/**
	 * Updates a group of Diagrams
	 * @param name	String with the text that must contain all Diagrams updated 
	 */
	public void update(String name) {
		
		for(int i = 0; i < this.grupoVentanasDefecto.size(); i++){
			DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
			if(ventana.getTitle().contains(name))	ventana.updateDiagram();
		}
	}	
	
	/*
	 * Se llama desde cualquier panel cuando se selecciona algo que sea un bicluster
	 * (pudiendo ser un bicluster cosas sin condiciones o sin genes, es decir, conjuntos
	 * de genes o de condiciones solamente)
	 * Para evitar actualizaciones, noActualizar indicar� que se deben actualizar todas
	 * las ventanas menos las que contengan la cadena noActualizar
	 */
	/**
	 * Sets the biclusters selected, as the list of genes and conditions grouped by these biclusters
	 * @param	selectedBic	BiclusterSelecteon with genes and conditions contained in the biclusters selected
	 * @param noUpdate	Updates all Diagrams except those that contains this String
	 */
	public void setSelectedBiclustersExcept(BiclusterSelection selectedBic, String noUpdate) 
		{
		this.selectedBicluster = selectedBic;
		if(!undoOrRedo)
			{
			//for(int i=contLog+1;i<selectionLog.size();i++)	
				//selectionLog.remove(i);
			if(contLog<selectionLog.size()-1)
				for(int i=selectionLog.size()-1;i>=contLog;i--)	
					selectionLog.addLast(selectionLog.get(i));
			selectionLog.add(selectedBicluster);
			contLog=selectionLog.size()-1;
			if(selectionLog.size()>10)   	
		    	{
		    	selectionLog.removeFirst();
		    	contLog--;
		    	}
			}
		else	undoOrRedo=false;
		TupleSelection sp=this.getDataLayer().getPointSelection(); 
		if(this.getData()!=null)
			{
			if(sp==null)	sp=new TupleSelection("genes", "conditions", this.getData().getNumTuples());
			for(int i=0;i<sp.getNumTuples();i++)		{sp.setX(i, false); sp.setY(i, false);}
			if(selectedBic.getGenes().size()>0)
				for(int i=0;i<selectedBic.getGenes().size();i++)
					{
					sp.setX(selectedBic.getGenes().get(i), true);
					}
			this.getDataLayer().setPointSelection(sp);
			}
			
		
		this.updateExcept(noUpdate);
		}
	
	public void setSelectedBiclustersOnly(BiclusterSelection selectedBic, String onlyUpdate) 
		{
		this.selectedBicluster = selectedBic;
		if(!undoOrRedo)
			{
			if(contLog<selectionLog.size()-1)
				for(int i=selectionLog.size()-1;i>=contLog;i--)	
					selectionLog.addLast(selectionLog.get(i));
			selectionLog.add(selectedBicluster);
			contLog=selectionLog.size()-1;
			if(selectionLog.size()>10)   	
		    	{
		    	selectionLog.removeFirst();
		    	contLog--;
		    	}
			}
		else	undoOrRedo=false;
		TupleSelection sp=this.getDataLayer().getPointSelection(); 
		if(this.getData()!=null)
			{
			if(sp==null)	sp=new TupleSelection("genes", "conditions", this.getData().getNumTuples());
			for(int i=0;i<sp.getNumTuples();i++)		{sp.setX(i, false); sp.setY(i, false);}
			if(selectedBic.getGenes().size()>0)
				for(int i=0;i<selectedBic.getGenes().size();i++)
					{
					sp.setX(selectedBic.getGenes().get(i), true);
					}
			this.getDataLayer().setPointSelection(sp);
			}
			
		
		this.updateOnly(onlyUpdate);
		}
	public void setSelectedBicluster(BiclusterSelection selectedBic) 
		{
		this.selectedBicluster = selectedBic;
		}
	
	/**
	 * For Ctrl-Z
	 */
	public void undo()
		{
		undoOrRedo=true;
		System.out.println("Undo");
		if(contLog>0)	contLog--;
		setSelectedBiclustersExcept(selectionLog.get(contLog), "");
		System.out.println("contlog "+contLog);
		}

	/**
	 * For Ctrl-Y
	 */
	public void redo()
		{
		undoOrRedo=true;
		System.out.println("Redo");
		if(contLog<selectionLog.size()-1)	contLog++;
		setSelectedBiclustersExcept(selectionLog.get(contLog), "");
		System.out.println("contlog "+contLog);
		}
	
	/**
	 * For Ctrl-F
	 */
	public void search()
		{
		//Search & selection box
		searchPanel=new SearchPanel(this);
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		window.setTitle("Search");
		JComponent newContentPane = searchPanel.getJPanel2();
		newContentPane.setOpaque(true); //content panes must be opaque
		window.setContentPane(newContentPane);
		window.setAlwaysOnTop(true);
		//Display the window.
		window.pack();
		window.setSize(new Dimension(241, 150));
		window.setLocation((getDesktop().getWidth()-window.getWidth())/2, (getDesktop().getHeight()-window.getHeight())/2);
	//	searchPanel.getjButton1().requestFocusInWindow();
		window.setVisible(true);
		}
	
	/**
	 * For Ctrl-S
	 */
	public void show()
		{
		//Show label names box
		if(showPanel==null)		showPanel=new ShowPanel(this);
		else					showPanel.updateLists();
		showPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		showPanel.setTitle("Show");
		showPanel.setAlwaysOnTop(true);
		
		//Display the window.
		showPanel.pack();
		showPanel.setLocation((getDesktop().getWidth()-showPanel.getWidth())/2, (getDesktop().getHeight()-showPanel.getHeight())/2);
		showPanel.setVisible(true);
		}
	
	public void sort()
		{
		//Sorting columns
		if(sortPanel==null)		sortPanel=new SortPanel(this);
		else					sortPanel.updateLists();
		sortPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		sortPanel.setTitle("Sort");
		sortPanel.setAlwaysOnTop(true);
		
		//Display the window.
		sortPanel.pack();
		sortPanel.setLocation((getDesktop().getWidth()-sortPanel.getWidth())/2, (getDesktop().getHeight()-sortPanel.getHeight())/2);
		sortPanel.setVisible(true);
		}
	
	 /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
    	return;
    }
    
    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) 
    	{
         int keyCode = e.getKeyCode();
         if(keyCode==17)//Ctrl
          	 ctrlPressed=true;
    	}
    
    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
         if(ctrlPressed)
        	{
         	if(e.getKeyCode()==90)		undo();//crtl-Z
        	else if(e.getKeyCode()==89)	redo();//ctrl-Y
        	else if(e.getKeyCode()==70) search();//ctrl-F
        	else if(e.getKeyCode()==76) show();//ctrl-L
        	else if(e.getKeyCode()==83) sort();//ctrl-S
        	}
        else
        	{
            int keyCode = e.getKeyCode();
            if(keyCode==17)//Ctrl
           	ctrlPressed=false;
        	}
    }
    
/**
	 * Sets the genes and conditions hovered
	 * @param	hoveredBic	BiclusterSelecteon with genes and conditions contained in the biclusters selected
	 * @param responsible	Name of the view responsible of the selection
	 */
	public void setHoveredBicluster(BiclusterSelection hoveredBic, String responsible) 
		{
		this.hoveredBicluster = hoveredBic;
		}
	
	public BiclusterSelection getHoveredBicluster()
		{
		return hoveredBicluster;
		}
	/*
	 * Se llama en Bubble cuando se le selecciona alguna burbuja
	 */
	void setSelectedHeatmapBiclusters(BiclusterSelection selectedBic) 
		{
		this.selectedBicluster = selectedBic;
		this.update("Bubble");
		this.update("Transcription");
		}
	
	boolean isCambioGenes() {
		return cambioGenes;
	}

	void setCambioGenes(boolean cambioGenes) {
		this.cambioGenes = cambioGenes;
	}

	boolean isCambioTRNGenes() {
		return cambioTRNGenes;
	}

	void setCambioTRNGenes(boolean cambioTRNGenes) {
		this.cambioTRNGenes = cambioTRNGenes;
	}

	
	//-----------------------------------------------------
	/**
	 * Gets the number of Heatmaps opened
	 * @return	the number of Heatmaps opened
	 */
	public int getNumHeatmapDiagrams() {
		return numVentanaHeatmap;
	}

	/**
	 * Gets the number of WordClouds opened
	 * @return	the number of WordClouds opened
	 */
	public int getNumWordClouds() {
		return numVentanaWordCloud;
	}

	/**
	 * Checks if microarray data have been loaded
	 * @return	true if microarray data are loaded
	 */
	public boolean areMicroarrayDataLoaded() {
		return datosMicroarrayCargados;
	}

	/**
	 * Sets the status of microarray data
	 * @param microarrayDataStatus	if it is true, microarray data status is set to loaded.
	 */
	public void setMicroarrayDataLoaded(boolean microarrayDataStatus) {
		this.datosMicroarrayCargados = microarrayDataStatus;
	}

	/**
	 * Returns the MicroarrayData
	 * @return	a MicroarrayData class with microarray data for this session
	 */
	public MicroarrayData getMicroarrayData() {
		return datosMicroarray;
	}

	/**
	 * Sets the microarray data for this session. Note that it does not automatically calls to
	 * setMicroarrayDataLoaded(true).
	 * @param md	the microarray data for this session.
	 */
	public void setMicroarrayData(MicroarrayData md) {
		this.datosMicroarray = md;
		this.setMicroarrayDataLoaded(true);
	//	this.analysis.setMicroarrayData(md);
		}
	
	/**
	 * If a single condition is selected in any view, this method returns the expression
	 * levels for all genes in the selected condition
	 * @return	Vector<Double> with gene expression values for the selected condition
	 */
	public Vector<Double> getConditionExpressions() {
		return expresionesCondicion;
	}

	/**
	 * Stores the expression levels for all genes for a single condition
	 * TODO: To be deprecated
	 * @param 	conditionExpressions Vector<Double> with gene expression values for the selected condition
	 */
	public void setConditionExpressions(Vector<Double> conditionExpressions) {
		this.expresionesCondicion = conditionExpressions;
	}

	public void changeLabels()
		{
		if(getMicroarrayData()!=null)
			{
			getMicroarrayData().changeLabels();
			if(this.getTRNData()!=null)		
				{
				for(int i = 0; i < this.grupoVentanasDefecto.size(); i++){
					DiagramWindow ventana = this.grupoVentanasDefecto.elementAt(i);
					if(ventana.getDiagram().getClass()==NetworkDiagram.class)	
						((NetworkDiagram)ventana.getDiagram()).changeLabels();
					}
				}
			updateAll();
			}
		}
	/**
	 * Gets the number of DiagramWindows opened for this Session
	 * @return	the number of DiagramWindows opened
	 */
	public int getNumWindows() {
		return numVentanas;
	}

	/**
	 * Gets the number of BiclusVisWindows opened for this Session
	 * @return	the number of BiclusVisWindows opened
	 */
	public int getNumBiclusVisDiagrams() {
		return numVentanaBubbleGraph;
	}

	int getNumColores() {
		return numColores;
	}

	void setNumColores(int numColores) {
		this.numColores = numColores;
	}


	/**
	 * Gets the name of the file containing Bicluster data used
	 * @return	String with the path to the file containing Bicluster data used
	 */
	public String getBiclusterDataFile() {
		return biclusterDataFile;
	}

	/**
	 * Sets the name of the file containing Bicluster data used
	 * param biclusterDataFile	String with the path to the file containing Bicluster data used
	 */
	public void setBiclusterDataFile(String biclusterDataFile) {
		this.biclusterDataFile = biclusterDataFile;
	}

	/**
	 * Checks the bubble data status
	 * @return	true if bubble data have been loaded, false otherwise
	 * @deprecated	these data are now internally generated from bicluster data
	 */
	public boolean areBubbleDataLoaded() {
		return datosBubbleCargados;
	}

	/**
	 * Sets the bubble data status
	 * @param bubbleDataStatus	true if bubble data are loaded, false otherwise
	 * @deprecated
	 */
	public void setBubbleDataLoaded(boolean bubbleDataStatus) {
		this.datosBubbleCargados = bubbleDataStatus;
	}

	/**
	 * Checks the bicluster data status
	 * @return	true if bicluster data have been loaded, false otherwise
	 */
	public boolean areBiclusterDataLoaded() {
		return datosBiclusterCargados;
	}

	/**
	 * Sets the bicluster data status
	 * @param biclusterDataStatus	true if bicluster data are loaded, false otherwise
	 */
	public void setBiclusterDataStatus(boolean biclusterDataStatus) {
		this.datosBiclusterCargados = biclusterDataStatus;
	}

	/**
	 * Returns the color used to highlight searched items
	 * @return the color used to highlight searched items
	 */
	public Color getSearchColor() {
		return searchColor;
	}

	/**
	 * Returns the color used to highlight selected items
	 * @return the color used to highlight selected items
	 */
	public Color getSelectionColor() {
		return selectionColor;
	}

	/**
	 * Returns the color used to highlight hover items
	 * @return the color used to highlight hover items
	 */
	public Color getHoverColor() {
		return hoverColor;
	}

	/**
	 * Returns the color used to draw biclusters in the first bicluster set
	 * @return the color used to draw biclusters in the first bicluster set
	 */
	public Color getBicSet1() {
		return bicSet1Color;
	}

	/**
	 * Returns the color used to draw biclusters in the second bicluster set
	 * @return the color used to draw biclusters in the second bicluster set
	 */
	public Color getBicSet2() {
		return bicSet2Color;
	}

	/**
	 * Returns the color used to draw biclusters in the third bicluster set
	 * @return the color used to draw biclusters in the third bicluster set
	 */
	public Color getBicSet3() {
		return bicSet3Color;
	}

	/**
	 * Sets the color for the bicluster set 1
	 * @param bicSet1Color	color for the bicluster set 1
	 */
	public void setBicSet1Color(Color bicSet1Color) {
		this.bicSet1Color = bicSet1Color;
	}

	/**
	 * Sets the color for the bicluster set 2
	 * @param bicSet2Color	color for the bicluster set 2
	 */
	public void setBicSet2Color(Color bicSet2Color) {
		this.bicSet2Color = bicSet2Color;
	}

	/**
	 * Sets the color for the bicluster set 3
	 * @param bicSet3Color	color for the bicluster set 3
	 */
	public void setBicSet3Color(Color bicSet3Color) {
		this.bicSet3Color = bicSet3Color;
	}

	/**
	 * Sets the hover color (color for selectionable items when passed over them)
	 * @param hoverColor	color for hovering
	 */
	public void setHoverColor(Color hoverColor) {
		this.hoverColor = hoverColor;
	}

	/**
	 * Sets the search color
	 * @param searchColor	color for searched elements
	 */
	public void setSearchColor(Color searchColor) {
		this.searchColor = searchColor;
	}

	/**
	 * Sets the selection color 
	 * @param selectionColor	color for selected elements
	 */
	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
	}

	/*public Analysis getBiclustering() {
		return biclustering;
	}

	public void setBiclustering(Analysis biclustering) {
		this.biclustering = biclustering;
	}*/
	
	
	
}