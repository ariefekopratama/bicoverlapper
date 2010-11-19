package es.usal.bicoverlapper.kernel;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jvnet.lafwidget.LafWidget;
import org.jvnet.lafwidget.tabbed.DefaultTabPreviewPainter;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.TabCloseListener;

import es.usal.bicoverlapper.data.files.FileParser;
import es.usal.bicoverlapper.kernel.managers.AnalysisMenuManager;
import es.usal.bicoverlapper.kernel.managers.FileMenuManager;
import es.usal.bicoverlapper.kernel.managers.HelpMenuManager;
import es.usal.bicoverlapper.kernel.managers.ViewMenuManager;
import es.usal.bicoverlapper.utils.Translator;


/**
 * Class to build main BicOverlapper interface: menu and desktop panel
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * @version 3.2 22/3/2007
 */
public class BicOverlapperWindow extends JFrame{

	private static final long serialVersionUID = -8081801349787389293L;
	
	private final String titulo = Translator.instance.menuLabels.getString("s20");
	
	private JTabbedPane desktop;
	private Vector<WorkDesktop> vistas;
	private WorkDesktop vistaActiva = null;
	private int indexClosing;
	private DefaultTabPreviewPainter visor;
	/**
	 * Menu item to export selected entities
	 */
	public JMenuItem menuArchivoExportSelection;
	/**
	 * Menu item to view heatmaps
	 */
	public JMenuItem menuViewHeatmap;
	/**
	 * Menu item to view parallel coordinates
	 */
	public JMenuItem menuViewParallelCoordinates;
	/**
	 * Menu item to view transcription regulatory networks
	 */
	public JMenuItem menuViewTRN;
	/**
	 * Menu item to view bubble map
	 */
	public JMenuItem menuViewBubbles;
	/**
	 * Menu item to view overlapper
	 */
	public JMenuItem menuViewOverlapper;
	/**
	 * Menu item to view go tag cloud
	 */
	public JMenuItem menuViewCloud;
	
	public JMenu analysisMenu, viewMenu;

	private FileMenuManager gestorMenuArchivo;
	
	/**
	 * Default constructor
	 *
	 */
	public BicOverlapperWindow() {
		
		this.desktop = new JTabbedPane();
		
		this.initDesktop();
		
		Configuration config = new Configuration();
		
		JMenuBar menu = crearMenu(config);
		
		this.setJMenuBar(menu);
		this.setTitle(titulo);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(config.getApplicationSize());
		this.setVisible(true);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent e) {
		    	System.out.println("Closing...");
		    	FileParser.deleteFiles(".", "tmp");
		    }
		});
		desktop.setFocusable(true);
	}

	private void initDesktop(){
		
		SubstanceLookAndFeel.registerTabCloseChangeListener(this.desktop,
		        new TabCloseListener() {
		          public void tabClosing(JTabbedPane tabbedPane,Component tabComponent){
		        	  vistas.remove(tabbedPane.getSelectedIndex());
		        	  indexClosing = tabbedPane.getSelectedIndex();
		        	  if(vistas.isEmpty())
		        		  vistaActiva = null;
		          }

		          public void tabClosed(JTabbedPane tabbedPane,Component tabComponent){
		        	  if(indexClosing != 0){
			        	  tabbedPane.setSelectedIndex(indexClosing-1);
			        	  vistaActiva = vistas.elementAt(indexClosing-1);
		        	  }
		        	  else if(vistas.size() > 0){
		        		  tabbedPane.setSelectedIndex(0);
			        	  vistaActiva = vistas.elementAt(0);
		        	  }
		        	  else if(vistas.size() == 0){
		        		  desktop.putClientProperty(LafWidget.TABBED_PANE_PREVIEW_PAINTER,null);
		        	  }
		        	  actualizarTabPreview();
		          }
		        });
		    
		    this.desktop.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					actualizarTabPreview();
					if(!vistas.isEmpty())
						vistaActiva = vistas.elementAt(desktop.getSelectedIndex());				
				}			
			});
		    
		    this.visor = new DefaultTabPreviewPainter();
			
			this.vistas = new Vector<WorkDesktop>(0,1);
			
			this.desktop.setBackground(Color.LIGHT_GRAY);
					
			this.getContentPane().add(desktop);
	}
	
	private JMenuBar crearMenu(Configuration config){
		
		// Creamos menu "Archivo"
		JMenu fileMenu = new JMenu(Translator.instance.menuLabels.getString("s1"));
			
		//Añadimos item "Abrir Microarray" al menu "Archivo"
		JMenuItem menuArchivoAbrirMicroarray = 
			new JMenuItem("Load Expression Data");		
		fileMenu.add(menuArchivoAbrirMicroarray);
		
		
		// Añadimos item "Abrir Bicluster" al menu "Archivo"
		JMenuItem menuArchivoAbrirBicluster = 
			new JMenuItem("Load Groups");		
		fileMenu.add(menuArchivoAbrirBicluster);

		// Añadimos item "Abrir TRN" al menu "Archivo"
		JMenuItem menuArchivoAbrirTRN = 
			new JMenuItem("Load Network");		
		fileMenu.add(menuArchivoAbrirTRN);
		// 
	
	
		// Añadimos separador al menu
		fileMenu.addSeparator();
		
		//Añadimos item "Download expression data"
		JMenuItem downloadAEmenu = 
			new JMenuItem("Download AE experiment");		
		fileMenu.add(downloadAEmenu);

		
		// Añadimos separador al menu
		fileMenu.addSeparator();

		menuArchivoExportSelection = 
			new JMenuItem("Export Selection");		
		fileMenu.add(menuArchivoExportSelection);

		
		// Añadimos separador al menu
		fileMenu.addSeparator();
		
		// Añadimos item "Cargar Configuracion" al menu "Archivo"
		JMenuItem menuArchivoCargarConfig =
			new JMenuItem(Translator.instance.menuLabels.getString("s17"));
		fileMenu.add(menuArchivoCargarConfig);
		
		// Añadimos item "Guardar Configuracion" al menu "Archivo"
		JMenuItem menuArchivoGuardarConfig =
			new JMenuItem(Translator.instance.menuLabels.getString("s18"));
		fileMenu.add(menuArchivoGuardarConfig);
		
		JMenuItem fileMenuOpenLastProject =
			new JMenuItem(Translator.instance.menuLabels.getString("openLastProject"));
		fileMenu.add(fileMenuOpenLastProject);
		
		JMenu fileMenuRecentProjects =
			new JMenu(Translator.instance.menuLabels.getString("recentProjects"));
		
		
		// Añadimos el gestor de eventos a los items del menu "Archivo"
		gestorMenuArchivo = new FileMenuManager(this);
		
		ArrayList<String> list=gestorMenuArchivo.recentFileList();
		for(String s:list)
			{
			JMenuItem jmi =	new JMenuItem(s);
			fileMenuRecentProjects.add(jmi);
			jmi.addActionListener(gestorMenuArchivo);
			}
		fileMenu.add(fileMenuRecentProjects);
		
		menuArchivoAbrirTRN.addActionListener(gestorMenuArchivo);
		menuArchivoAbrirMicroarray.addActionListener(gestorMenuArchivo);
		downloadAEmenu.addActionListener(gestorMenuArchivo);
		menuArchivoAbrirBicluster.addActionListener(gestorMenuArchivo);

		menuArchivoExportSelection.addActionListener(gestorMenuArchivo);
		
		menuArchivoGuardarConfig.addActionListener(gestorMenuArchivo);
		menuArchivoCargarConfig.addActionListener(gestorMenuArchivo);
		fileMenuOpenLastProject.addActionListener(gestorMenuArchivo);

		//menuArchivoSalir.addActionListener(gestorMenuArchivo);
		
		//Create menu "Analysis"
		AnalysisMenuManager amm = new AnalysisMenuManager(this);
		analysisMenu=new JMenu(Translator.instance.menuLabels.getString("analysis"));
		analysisMenu.setEnabled(false);
		JMenuItem menuAnalysisBimax =
			new JMenuItem(Translator.instance.menuLabels.getString("bimax"));
		JMenuItem menuAnalysisPlaid =
			new JMenuItem(Translator.instance.menuLabels.getString("plaid"));
		JMenuItem menuAnalysisISA =
			new JMenuItem(Translator.instance.menuLabels.getString("isa"));
		JMenuItem menuAnalysisXMotifs =
			new JMenuItem(Translator.instance.menuLabels.getString("xmotifs"));
		JMenuItem menuAnalysisCChurch =
			new JMenuItem(Translator.instance.menuLabels.getString("cc"));
		JMenuItem menuAnalysisSearch =
			new JMenuItem(Translator.instance.menuLabels.getString("search"));
		JMenuItem menuAnalysisShow =
			new JMenuItem(Translator.instance.menuLabels.getString("show"));
		JMenuItem menuAnalysisSort =
			new JMenuItem(Translator.instance.menuLabels.getString("sort"));
		JMenuItem menuAnalysisSelect =
			new JMenuItem(Translator.instance.menuLabels.getString("select"));
		JMenuItem menuAnalysisDifexp =
			new JMenuItem(Translator.instance.menuLabels.getString("difexp"));
		JMenuItem menuAnalysisBuildNetwork =
			new JMenuItem(Translator.instance.menuLabels.getString("buildnet"));
		
		menuAnalysisSort.setMnemonic('S');
		menuAnalysisSearch.setMnemonic('F');
		menuAnalysisShow.setMnemonic('L');
		
		analysisMenu.add(menuAnalysisSearch);
		analysisMenu.add(menuAnalysisShow);
		analysisMenu.add(menuAnalysisSort);
		analysisMenu.addSeparator();
		analysisMenu.add(menuAnalysisSelect);
		analysisMenu.add(menuAnalysisDifexp);
		analysisMenu.addSeparator();
		analysisMenu.add(menuAnalysisBimax);
		analysisMenu.add(menuAnalysisPlaid);
		analysisMenu.add(menuAnalysisISA);
		analysisMenu.add(menuAnalysisXMotifs);
		analysisMenu.add(menuAnalysisCChurch);
		analysisMenu.addSeparator();
		analysisMenu.add(menuAnalysisBuildNetwork);
				
		
		menuAnalysisBimax.addActionListener(amm);
		menuAnalysisPlaid.addActionListener(amm);
		menuAnalysisISA.addActionListener(amm);
		menuAnalysisXMotifs.addActionListener(amm);
		menuAnalysisCChurch.addActionListener(amm);
		menuAnalysisSearch.addActionListener(amm);
		menuAnalysisShow.addActionListener(amm);
		menuAnalysisSort.addActionListener(amm);
		menuAnalysisSelect.addActionListener(amm);
		menuAnalysisDifexp.addActionListener(amm);
		menuAnalysisBuildNetwork.addActionListener(amm);
						
		// Creamos menu "Ver"
		viewMenu = new JMenu(Translator.instance.menuLabels.getString("s2"));
		viewMenu.setEnabled(false);
		/*
		// Añadimos item "Diagrama Puntos" al menu "Ver"
		JMenuItem menuVerDiagPuntos =
			new JMenuItem(Translator.instance.menuLabels.getString("s5"));
				
		menuVer.add(menuVerDiagPuntos);
		
		// Añadimos item "Histograma" al menu "Ver"
		JMenuItem menuVerHistograma =
			new JMenuItem(Translator.instance.menuLabels.getString("s6"));
		
		menuVer.add(menuVerHistograma);
		
		// Añadimos item "Mapeo de Color" al menu "Ver"
		JMenuItem menuVerMapeo =
			new JMenuItem(Translator.instance.menuLabels.getString("s7"));
		
		menuVer.add(menuVerMapeo);		
		*/
		// Añadimos item "Coordenadas Paralelas" al menu "Ver"
		menuViewParallelCoordinates =
			new JMenuItem(Translator.instance.menuLabels.getString("s8"));
		
		viewMenu.add(menuViewParallelCoordinates);

		// Añadimos item "Microarray Heatmap" al menu "Ver"
		menuViewHeatmap =
			new JMenuItem("Microarray Heatmap");
		
		viewMenu.add(menuViewHeatmap);
		/*
		// Añadimos item "Dendrograma" al menu "Ver"
		JMenuItem menuVerDendrograma =
			new JMenuItem(Translator.instance.menuLabels.getString("s9"));
		
		menuVer.add(menuVerDendrograma);
		*/

		// Añadimos separador al menu
		viewMenu.addSeparator();

		// Añadimos item "Bubbles" al menu "Ver"
		menuViewBubbles =new JMenuItem("Bubble Map");
		
		viewMenu.add(menuViewBubbles);
		
		
//		 Añadimos item "Microarray Heatmap" al menu "Ver"
		menuViewOverlapper =new JMenuItem(Translator.instance.menuLabels.getString("s10"));

		
		viewMenu.add(menuViewOverlapper);

		// Añadimos separador al menu
		viewMenu.addSeparator();

		//		 Añadimos item "WordCloud" al menu "Ver"
		menuViewCloud =	new JMenuItem(Translator.instance.menuLabels.getString("s13"));
		viewMenu.add(menuViewCloud);

		// Añadimos item "Biological Network" al menu "Ver"
		menuViewTRN =new JMenuItem("Biological Network");
		
		viewMenu.add(menuViewTRN);


		
		// Añadimos el gestor de eventos a los items del menu "Ver"
		ViewMenuManager gestorMenuVer = new ViewMenuManager(this,config);
		menuViewParallelCoordinates.addActionListener(gestorMenuVer);
		menuViewHeatmap.addActionListener(gestorMenuVer);
		menuViewTRN.addActionListener(gestorMenuVer);
		
		//menuVerDendrograma.addActionListener(gestorMenuVer);
/*
		menuVerTreeMap.addActionListener(gestorMenuVer);
		menuVerDetails.addActionListener(gestorMenuVer);
	*/	
		
		menuViewCloud.addActionListener(gestorMenuVer);
	
		menuViewOverlapper.addActionListener(gestorMenuVer);
		menuViewBubbles.addActionListener(gestorMenuVer);
	

//		 Creamos menu Ayuda
		JMenu helpMenu =  new JMenu(Translator.instance.menuLabels.getString("s23"));
		HelpMenuManager gestorMenuAyuda = new HelpMenuManager(this);

		
		//Añadimos item "Abrir Microarray" al menu "Archivo"
		JMenuItem menuAyudaAcercaDe = 
			new JMenuItem(Translator.instance.menuLabels.getString("s24"));		
		JMenuItem menuAyudaContents = 
			new JMenuItem(Translator.instance.menuLabels.getString("s25"));		
		helpMenu.add(menuAyudaContents);
		helpMenu.add(menuAyudaAcercaDe);
		
		menuAyudaContents.addActionListener(gestorMenuAyuda);
		menuAyudaAcercaDe.addActionListener(gestorMenuAyuda);
		
		// Creamos una barra de menu a la que añadimos los menus
		JMenuBar menu = new JMenuBar();
		
		menu.add(fileMenu);
		menu.add(analysisMenu);
		menu.add(viewMenu);
		menu.add(helpMenu);
		
		
		//Inicialmente, todas las vistas están deshabilitadas en lo que no se carguen los ficheros adecuados
		menuArchivoExportSelection.setEnabled(false);

		menuViewParallelCoordinates.setEnabled(false);
		menuViewHeatmap.setEnabled(false);
		
		menuViewTRN.setEnabled(false);

		menuViewBubbles.setEnabled(false);
		menuViewOverlapper.setEnabled(false);
	//	menuViewCloud.setEnabled(false);
		
		//TODO: Además, para una versión oficial, de momento deshabilitamos el resto también
		//menuArchivoAbrirTRN.setEnabled(false);
		//menuArchivoAbrirMicroarray.setEnabled(false);
		
		return menu;
	}
	
	/**
	 * Actualiza la vista del mosaico de pestañas.
	 *
	 */
	void actualizarTabPreview(){
		this.desktop.revalidate();
		this.desktop.repaint();
	}
	
	/**
	 * Adds a work desktop, where visualization for a working data set will be added.
	 * Each WorkDesktop is a Tab added to the application desktop
	 * STILL IN DEVELOPMENT: By now, ony one working desktop is built. Besides, it has no much more sene
	 * 					to have more than one work desktop, since only one can be active at
	 * 					the same time
	 * 
	 * @param wd WorkDesktop to add
	 */
	public void addWorkDesktop(WorkDesktop wd){
		if(this.vistas.size() == 0){
			this.getContentPane().remove(this.desktop);
			this.desktop = new JTabbedPane();
			this.initDesktop();
			this.desktop.putClientProperty(LafWidget.TABBED_PANE_PREVIEW_PAINTER,this.visor);
		}
		wd.getPanel().putClientProperty(SubstanceLookAndFeel.TABBED_PANE_CLOSE_BUTTONS_PROPERTY,Boolean.TRUE);
		this.vistas.add(wd);
		this.desktop.addTab(wd.getPanel().getName(), wd.getPanel());
		this.desktop.setSelectedIndex(vistas.size()-1);
		this.setActiveWorkDesktop(wd);
		this.actualizarTabPreview();
	}
	
	/**
	 * Stablishes the active WorkDesktop
	 * 
	 * @param wd the WorkDesktop to activate
	 */
	public void setActiveWorkDesktop(WorkDesktop wd){
		this.vistaActiva = wd;
	}
	
	/**
	 * Gets the active WorkDesktop
	 * 
	 * @return the active WorkDesktop
	 */
	public WorkDesktop getActiveWorkDesktop(){
		return this.vistaActiva;
	}
	
	/**
	 * Checks if there is an active WorkDesktop
	 * 
	 * @return true if there is an active WorkDesktop
	 */
	public boolean isActiveWorkDesktop(){
		return (this.vistaActiva != null);
	}
	
	/**
	 * Returns the title of the application
	 * 
	 * @return String with the title of the application
	 */
	public String getWindowTitle(){
		return this.titulo;
	}

	/**
	 * Returns the application desktop in which WorkDesktop are added
	 * @return	the application desktop
	 */
	public JTabbedPane getDesktop() {
		return desktop;
	}

	
	/**
	 * Sets the application desktop
	 * @param desktop	the application desktop
	 */
	public void setDesktop(JTabbedPane desktop) {
		this.desktop = desktop;
	}
}