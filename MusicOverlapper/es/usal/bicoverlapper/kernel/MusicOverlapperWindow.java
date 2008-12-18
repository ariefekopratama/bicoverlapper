package es.usal.bicoverlapper.kernel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
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
public class MusicOverlapperWindow extends JFrame{

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
	
	/**
	 * Default constructor
	 *
	 */
	public MusicOverlapperWindow() {
		
		this.desktop = new JTabbedPane();
		
		this.initDesktop();
		
		Configuration config = new Configuration();
		
		JMenuBar menu = crearMenu(config);
		
		this.setJMenuBar(menu);
		this.setTitle(titulo);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(config.getApplicationSize());
		this.setVisible(true);
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
		JMenu menuArchivo = new JMenu(Translator.instance.menuLabels.getString("s1"));
			
		
		// Añadimos item "Abrir Bicluster" al menu "Archivo"
		JMenuItem menuFileOpenGroups = 
			new JMenuItem("Open Tag Groups");		
		menuArchivo.add(menuFileOpenGroups);

		
		// Añadimos separador al menu
		menuArchivo.addSeparator();

		menuArchivoExportSelection = 
			new JMenuItem("Export Selection");		
		menuArchivo.add(menuArchivoExportSelection);

		
		// Añadimos separador al menu
		menuArchivo.addSeparator();
		
		// Añadimos item "Cargar Configuracion" al menu "Archivo"
		JMenuItem menuArchivoCargarConfig =
			new JMenuItem(Translator.instance.menuLabels.getString("s17"));
		menuArchivo.add(menuArchivoCargarConfig);
		
		// Añadimos item "Guardar Configuracion" al menu "Archivo"
		JMenuItem menuArchivoGuardarConfig =
			new JMenuItem(Translator.instance.menuLabels.getString("s18"));
		menuArchivo.add(menuArchivoGuardarConfig);
		
		// Añadimos separador al menu
		menuArchivo.addSeparator();
		
		// Añadimos item "Salir" al menu "Archivo"
		JMenuItem menuArchivoSalir = 
			new JMenuItem(Translator.instance.menuLabels.getString("s19"));		
		menuArchivo.add(menuArchivoSalir);
		
		// Añadimos el gestor de eventos a los items del menu "Archivo"
		FileMenuManager gestorMenuArchivo = new FileMenuManager(this);
		
		menuFileOpenGroups.addActionListener(gestorMenuArchivo);

		menuArchivoExportSelection.addActionListener(gestorMenuArchivo);
		
		menuArchivoGuardarConfig.addActionListener(gestorMenuArchivo);
		menuArchivoCargarConfig.addActionListener(gestorMenuArchivo);

		menuArchivoSalir.addActionListener(gestorMenuArchivo);
				
		// Creamos menu "Ver"
		JMenu menuVer = new JMenu(Translator.instance.menuLabels.getString("s2"));
//		 Añadimos item "Microarray Heatmap" al menu "Ver"
		menuViewOverlapper =
			new JMenuItem(Translator.instance.menuLabels.getString("s10"));

		
		menuVer.add(menuViewOverlapper);

		// Añadimos el gestor de eventos a los items del menu "Ver"
		ViewMenuManager gestorMenuVer = new ViewMenuManager(this,config);
		menuViewOverlapper.addActionListener(gestorMenuVer);

		//		 Creamos menu Ayuda
		JMenu menuAyuda =  new JMenu(Translator.instance.menuLabels.getString("s23"));
		HelpMenuManager gestorMenuAyuda = new HelpMenuManager(this);

		
		//Añadimos item "Abrir Microarray" al menu "Archivo"
		JMenuItem menuAyudaAcercaDe = 
			new JMenuItem(Translator.instance.menuLabels.getString("s24"));		
		JMenuItem menuAyudaContents = 
			new JMenuItem(Translator.instance.menuLabels.getString("s25"));		
		menuAyuda.add(menuAyudaContents);
		menuAyuda.add(menuAyudaAcercaDe);
		
		menuAyudaContents.addActionListener(gestorMenuAyuda);
		menuAyudaAcercaDe.addActionListener(gestorMenuAyuda);
		
		// Creamos una barra de menu a la que añadimos los menus
		JMenuBar menu = new JMenuBar();
		
		menu.add(menuArchivo);
		menu.add(menuVer);
		menu.add(menuAyuda);
		
		
		//Inicialmente, todas las vistas están deshabilitadas en lo que no se carguen los ficheros adecuados
		menuArchivoExportSelection.setEnabled(false);

		menuViewOverlapper.setEnabled(false);
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