package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.managers.ConfigurationMenuManager;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.overlapper.Overlapper;
import es.usal.bicoverlapper.visualization.diagrams.overlapper.Graph;
import es.usal.bicoverlapper.visualization.diagrams.overlapper.Node;

//Vector Format Image export
import org.freehep.util.export.ExportDialog;
import org.freehep.graphicsio.emf.EMFExportFileType;
import org.freehep.graphicsio.pdf.PDFExportFileType;
import org.freehep.graphicsio.ps.PSExportFileType;

/**
 * BiclusVisPanel contains a BiclusVisDiagram and a toolbar with different options to apply
 * to the Diagram 
 * 
 * @author Rodrigo Santamaria
 *
 */
public class OverlapperDiagram extends Diagram
	{
private static final long serialVersionUID = 1L;
	
	// atributos del panel del diagrama
	private Session sesion;
	protected MultidimensionalData datos=null;
	private int alto;
	private int ancho;
	//private boolean atributosIniciados = false, configurando = false, diagramaPintado = false;
	
	// definicion de margenes del diagrama
	
	final int margenDer = 40;
	final int margenIzq = 40;
	final int margenSup = 25;
	final int margenInf = 40;
	final int margenDiagrama = 10; // porcentaje de exceso en intervalo de representacion del diagrama
	
	// configuracion de color
	private static final int selectionColor=0;
	private static final int searchColor=1;
	private static final int hoverColor=2;
	private static final int bicColor1=3;
	private static final int bicColor2=4;
	private static final int bicColor3=5;
	private static final int geneLabelColor=6;
	private static final int conditionLabelColor=7;
	private static final int bicLabelColor=8;
	private static final int backgroundColor=9;

	private Color[] paleta = {null, null, null, null, null, null, Color.BLACK, Color.WHITE, Color.YELLOW, Color.BLACK};
	private String[] textoLabel = {"Selection", "Search","Hover", "Set 1", "Set 2", "Set 3", "Gene labels", "Condition labels",
			"Bicluster labels","Background"};
	private JTextField[] muestraColor = new JTextField[paleta.length];
	
	
	//Información propia de nuestro panel
	Overlapper bv;
	JToolBar jtb;
	JLabel found;
	JRadioButton forFilms;
	JRadioButton forPersons;
	boolean configurando=false;
	
	/**
	 * Default constructor
	 */
	public OverlapperDiagram()
		{
		super();
		}
	
	/**
	 * Constructor that sets the Session and Dimension of the panel
	 * @param session	Session to which this diagram is listening
	 * @param dim	Dimension of the panel
	 */
	public OverlapperDiagram(Session session, Dimension dim)
		{
		super(new BorderLayout());//
		int num = session.getNumBubbleMapDiagrams();
		this.sesion = session;
			{
			datos=session.getData();
			this.setName(Translator.instance.menuLabels.getString("s10")+" "+num);
			}
		
		paleta[OverlapperDiagram.selectionColor]=sesion.getSelectionColor();
		paleta[OverlapperDiagram.searchColor]=sesion.getSearchColor();
		paleta[OverlapperDiagram.hoverColor]=sesion.getHoverColor();
		paleta[OverlapperDiagram.bicColor1]=sesion.getBicSet1();
		paleta[OverlapperDiagram.bicColor2]=sesion.getBicSet2();
		paleta[OverlapperDiagram.bicColor3]=sesion.getBicSet3();
		paleta[OverlapperDiagram.geneLabelColor]=new Color(195, 250, 190, 255);
		paleta[OverlapperDiagram.conditionLabelColor]=new Color(165, 175, 250, 255);

		this.alto = (int)dim.getHeight();
		this.ancho = (int)dim.getWidth();
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);
		this.setPosition(new Point(1000,1000));
		}
	

	/**
	 * Once the panel is built and session is set, this method builds the interface, including the BiclusVis visualization
	 */
	public void create()
		{
			{
			bv=new Overlapper();
			bv.setPalette(paleta);
			bv.setDataFile(sesion.getBiclusterDataFile());
			bv.setup(ancho,alto);//, sesion.getHoverColor(), sesion.getSelectionColor(), sesion.getSearchColor());
			if(sesion.getMicroarrayData()!=null)	bv.setMicroarrayData(sesion.getMicroarrayData());
			bv.buildGraph();
			bv.init();	//TODO: Probando a controlarlo desde el panel
			
			JToolBar jtb=new JToolBar();
			addButtons(jtb);
			this.getWindow().add(bv, BorderLayout.CENTER);
			this.getWindow().add(jtb, BorderLayout.SOUTH);
				
			bv.addMouseListener(new GestorMouse());	
			this.getWindow().setContentPane(this);
			this.getWindow().pack();
			}
		}
	
	private void addButtons(JToolBar toolBar) 
		{
	    JButton button = null;
	    JToggleButton tb = null;
	    
	    //toolBar.setLayout(null);
	    
	    //play-pause
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/Pause24.gif", "pause",
	                                  "Pause simulation",
	                                  "Pause");
	    toolBar.add(button);

	    //overview
	    tb = makeNavigationToggleButton("es/usal/bicoverlapper/resources/images/Zoom24.gif", "overview",
	  	                                  "Show overview",
	                                  "Overview", false);
	    toolBar.add(tb);
	   
	    
	    //first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/ZoomOut24.gif", "zoom out",
	                                  "Zoom Out",
	                                  "Zoom Out");
	    toolBar.add(button);
		   
	    //first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/ZoomIn24.gif", "zoom in",
	                                  "Zoom In",
	                                  "Zoom In");
	    toolBar.add(button);
	    
	    
	    //first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/radial.gif", "change model",
	                                  "change edge model",
	                                  "Change to radial model");
	    toolBar.add(button);

	    //show labels
	    tb = makeNavigationToggleButton("es/usal/bicoverlapper/resources/images/names.gif", "labels",
	  	                                  "Show names",
	                                  "Names", false);
	    toolBar.add(tb);

	    //relative label size
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/absoluteSize.gif", "absolute label size",
                  "Absolute label size",
            	  "Name size");
	    toolBar.add(button);

	    //decrease label size
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/namesMinus.gif", "labels minus",
	                                  "Decrease name size",
	                                  "Names minus");
	    toolBar.add(button);

	    //increase label size
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/namesPlus.gif", "labels plus",
	                                  "Increase name size",
	                                  "Names plus");
	    toolBar.add(button);

	    //decrease Threshold
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/Down24.gif", "decrease threshold",
	                                  "Decrease threshold",
	                                  "Decrease threshold");
	    toolBar.add(button);
	    
	    //increase Threshold
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/Up24.gif", "increase threshold",
	                                  "Increase threshold",
	                                  "Increase threshold");
	    toolBar.add(button);

	    //show hulls
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/withoutHull.png", "draw hulls",
	  	                                  "Hide zones",
	                                  "Draw hulls");
	    toolBar.add(button);
	    
	    //	  	first button
	    tb = makeNavigationToggleButton("es/usal/bicoverlapper/resources/images/arc3.png", "draw arcs",
	                                  "Draw nodes as piecharts",
	                                  "Draw arcs", false);
	    toolBar.add(tb);

	    //	  	first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/shrink.png", "shrink",
	                                  "Decrease cluster size",
	                                  "Shrink");
	    toolBar.add(button);
	    
	    //	  	first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/expand.png", "expand",
	                                  "Increase cluster size",
	                                  "Expand");
	    toolBar.add(button);
		
	    //	  	first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/repulse.png", "repulse",
	                                  "Increase repulsion",
	                                  "Repulse");
	    toolBar.add(button);
		
	    //	  	first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/attract.png", "attract",
	                                  "Decrease repulsion",
	                                  "Attract");
	    toolBar.add(button);

	    //	  	first button
	    tb = makeNavigationToggleButton("es/usal/bicoverlapper/resources/images/sizeGlyphs.png", "relative size",
	                                  "Change to relative size",
	                                  "Node Size", false);
	    toolBar.add(tb);

	    tb = makeNavigationToggleButton("es/usal/bicoverlapper/resources/images/hideEdges.png", "hide edges",
		                "Hide Edges",
		                "Hide Edges", true);
		toolBar.add(tb);
		tb = makeNavigationToggleButton("es/usal/bicoverlapper/resources/images/hideNodes.png", "hide nodes",
		        "Hide nodes",
		        "Hide Nodes", false);
		toolBar.add(tb);
	    
		//	  	first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/config.gif", "color",
	                                  "color configuration",
	                                  "Color");
	    toolBar.add(button);

	    //	  	first button
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/export.png", "export",
	                                  "Export image",
	                                  "Export");
	    toolBar.add(button);
  
	/*    JLabel cent=new JLabel(" Centroids: ");
	    toolBar.add(cent);
	    
	    JTextField numCent=new JTextField(5);
	    numCent.setMaximumSize(new Dimension(30,30));
	    numCent.setText(Integer.valueOf(bv.getNumCentroids()).toString());
	    numCent.addKeyListener(new GestorCent());
	    toolBar.add(numCent, BorderLayout.EAST);

	    
	    JLabel cast=new JLabel(" Cast: ");
	    toolBar.add(cast);
	    
	    JTextField maxCast=new JTextField(5);
	    maxCast.setMaximumSize(new Dimension(30,30));
	    maxCast.setText(Integer.valueOf(bv.getMaximumCast()).toString());
	    maxCast.addKeyListener(new GestorCast());
	    toolBar.add(maxCast, BorderLayout.EAST);
*/
	    JLabel search=new JLabel(" Search: ");
	    toolBar.add(search);
	    
	    JTextField buscar=new JTextField(20);
	    buscar.setMaximumSize(new Dimension(150,30));
	    buscar.addKeyListener(new GestorBusqueda());
	   
	    
	    toolBar.add(buscar, BorderLayout.EAST);
	    found=new JLabel("");
	    toolBar.add(found);
	/*	
	    forFilms=new JRadioButton("for films");
	    forPersons=new JRadioButton("for persons", true);
	    toolBar.add(forPersons);
	    toolBar.add(forFilms);*/
	    }
	
	protected JButton makeNavigationButton(String imageName,
            String actionCommand,
            String toolTipText,
            String altText) 
		{
		//Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(new GestorBotones());
		//button.setIcon(new ImageIcon(imageName));
		button.setIcon(loadIcon(imageName));
		
		return button;
		}

	protected JToggleButton makeNavigationToggleButton(String imageName,
            String actionCommand,
            String toolTipText,
            String altText,  boolean selected) 
		{
		//Create and initialize the button.
		JToggleButton button = new JToggleButton();
		try{
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(new GestorBotones());

		button.setIcon(loadIcon(imageName));
		button.setSelected(selected);
		}catch(Exception e){e.printStackTrace();};
		
		return button;
		}

	
	/**
	 * Makes the panel visible
	 *
	 */
	public void run()
		{
		this.getWindow().setVisible(true); // show the window
		}
	
	/*
	public void actualizarDatos() 
		{
		//TODO: Actualizar lo que sea necesario cogiéndolo de sesión.getXXX()

	//	this.repaint();		
		}*/
	
	
	public void resize()
		{
		bv.resize(this.ancho, this.alto);
		}
	
	/**
	 * Rebuilds the graph with the actual selection.
	 */
	public void update() 
		{
		//TODO: Lo que sea necesario para actualizar, sin necesidad de que haya cambio de datos de sesión
		//System.out.println("Actualizando datos!!!!!!!!!!!");
		if(sesion.getSelectedBicluster()!=null)
			{
			if(bv!=null)		bv.updateGraph(sesion.getSelectedBicluster());
			//else				bv.buildGraph(sesion.getSelectedBicluster());
			}
	//	this.repaint();
		}
	
	/**
	 * Builds the graph with the actual selection
	 * @deprecated
	 * TODO: reprogram this method
	 */
	public void createGraph()
		{
		if(bv==null)	
			{
			create();
			run();
			}
		}

	/**
	 * Returns the id for this kind of panel
	 */
	public int getId()
		{
		return es.usal.bicoverlapper.kernel.Configuration.OVERLAPPER_ID;
		}
	
	
	private class GestorBusqueda implements KeyListener{
			
		public GestorBusqueda(){}
		
		public void keyPressed(KeyEvent e)
			{
			//System.out.println("keyPressed en Panel");
			}
		public void keyTyped(KeyEvent e)
			{
			//System.out.println("keyTyped en Panel");
			}
		public void keyReleased(KeyEvent e)
			{
			String text=((JTextField)e.getSource()).getText(); 
		//	int num=bv.search(text, forFilms.isSelected());
			int num=bv.search(text, false);
			found.setText(" "+Integer.valueOf(num).toString());
			}
	}
	private ImageIcon loadIcon(String name)
		{
		URL imgURL=Thread.currentThread().getContextClassLoader().getResource(name);
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(imgURL));
		}
	
	/**
	 * Esta clase implementa un gestor para añadir un anclaje a través del panel correspondiente en la ventana de configuracion.
	 * 
	 * @author Javier Molpeceres Ortego
	 *
	 */
	private class GestorBotones implements ActionListener{
		
		public GestorBotones(){}
		
		public void actionPerformed(ActionEvent e)
		{
		//	System.out.println(e.getActionCommand());
		if ("pause".equals(e.getActionCommand())) 
			{
			JButton b=(JButton)e.getSource();
					
			if(!bv.isPauseSimulation())//No estaba en pausa, va a pasar a estarlo, cambiamos la imagen a play
				{
				b.setIcon(loadIcon("images/play24.gif"));
				b.setToolTipText("Restore simulation");
				}
			else	//Volvemos a poner el de pause
				{
				//b.setIcon(new ImageIcon("images/pause24.gif"));
				b.setIcon(loadIcon("images/pause24.gif"));
				b.setToolTipText("Pause simulation");
				}
			bv.pause();
			}
		
		else if("overview".equals(e.getActionCommand()))
			{
			bv.setShowOverview(!bv.isShowOverview());
	        }
		else if("zoom in".equals(e.getActionCommand()))
			{
			//bv.setShowOverview(!bv.isShowOverview());
			bv.zoomIn();
	        }
		else if("zoom out".equals(e.getActionCommand()))
			{
			//bv.setShowOverview(!bv.isShowOverview());
			bv.zoomOut();
	        }
		else if("increase threshold".equals(e.getActionCommand()))
			{
			bv.increaseNodeThreshold();
			//bv.increaseOverlapThreshold();
			}			
		else if("decrease threshold".equals(e.getActionCommand()))
			{
			bv.decreaseNodeThreshold();
			//bv.decreaseOverlapThreshold();
			}			
		else if("change model".equals(e.getActionCommand()))
			{
			if(bv.isRadial())//No estaba en pausa, va a pasar a estarlo, cambiamos la imagen a play
				{
				JButton b=(JButton)e.getSource();
						
				bv.radial2complete();
				b.setIcon(loadIcon("images/radial.gif"));
				}
			else	//Volvemos a poner el de pause
				{
				JButton b=(JButton)e.getSource();
				bv.complete2radial();
				b.setIcon(loadIcon("images/complete.gif"));
				b.setToolTipText("Change to complete model");
				}
			
	        }			
		else if("labels".equals(e.getActionCommand()))
			{
			bv.setShowLabel(!bv.isShowLabel());
	        }				
		else if("labels plus".equals(e.getActionCommand()))
			{
			bv.increaseLabelSize();
	        }				
	    else if("labels minus".equals(e.getActionCommand()))
			{
			bv.decreaseLabelSize();
		    }			
	    else if("absolute label size".equals(e.getActionCommand()))
			{
	    	JButton b=(JButton)e.getSource();
	    	if(bv.isAbsoluteLabelSize())	
	    		{
	    		b.setIcon(loadIcon("images/absoluteSize.gif"));
	    		b.setToolTipText("Absolute label size");
	    		}
	    	else
	    		{
	    		b.setIcon(loadIcon("images/relativeSize.gif"));
	    		b.setToolTipText("Relative label size");
	    		}
			bv.setAbsoluteLabelSize(!bv.isAbsoluteLabelSize());
		    }			
	    else if("cluster labels".equals(e.getActionCommand()))
			{
	    	bv.setDrawClusterLabels(!bv.isDrawClusterLabels());
		    }			
	    else if("increase cluster label size".equals(e.getActionCommand()))
			{
	    	bv.increaseLabelClusterSize();
		    }			
	    else if("decrease cluster label size".equals(e.getActionCommand()))
			{
	    	bv.decreaseLabelClusterSize();
		    }			
	    else if("draw hulls".equals(e.getActionCommand()))
			{
	    	JButton b=(JButton)e.getSource();
	    	if(bv.isDrawHull())	
	    		{
	    		b.setIcon(loadIcon("images/withHull.png"));
	    		b.setToolTipText("Draw zones");
	    		}
	    	else				
	    		{
	    		b.setIcon(loadIcon("images/withoutHull.png"));
	    		b.setToolTipText("Hide zones");
	    		}
	    	bv.setDrawHull(!bv.isDrawHull());
		    }			
	    else if("draw arcs".equals(e.getActionCommand()))
			{
	    	bv.setDrawPiecharts(!bv.isDrawPiecharts());
		    }			
	  /*  else if("addition".equals(e.getActionCommand()))
			{
	    	bv.setAdditionMode(!bv.isAdditionMode());
	    	sesion.setAdditionMode(bv.isAdditionMode());
		    }*/	
	    else if("attract".equals(e.getActionCommand()))
			{
	    	bv.decreaseG();
		    }			
	    else if("repulse".equals(e.getActionCommand()))
			{
	    	bv.increaseG();
		    }	
	    else if("shrink".equals(e.getActionCommand()))
			{
	    	bv.increaseStiffness();
		    }
	    else if("expand".equals(e.getActionCommand()))
			{
	    	bv.decreaseStiffness();
		    }
	   /* else if("resume".equals(e.getActionCommand()))
			{
	    	bv.setShowResume(!bv.isShowResume());
		    }
	    else if("centroids".equals(e.getActionCommand()))
			{
	    	bv.setShowCentroids(!bv.isShowCentroids());
		    }
	    else if("cast".equals(e.getActionCommand()))
			{
	    	String text=((JTextField)e.getSource()).getText(); 
	
			bv.setMaxCast(Integer.valueOf(text).intValue());
			construirGrafo();
			}*/
	    else if("relative size".equals(e.getActionCommand()))
			{
	    	bv.setSizeRelevant(!bv.isSizeRelevant());
			}
	    else if("color".equals(e.getActionCommand()))
			{
	    	configure();
			}
	   /* else if("show legend".equals(e.getActionCommand()))
			{
	    	bv.setShowLegend(!bv.isShowLegend());
			}
	    else if("show glyphs".equals(e.getActionCommand()))
			{
	    	JButton b=(JButton)e.getSource();
	    	if(bv.isDrawGlyphs())	b.setIcon(loadIcon("images/withGlyphs.gif"));
	    	else					b.setIcon(loadIcon("images/withoutGlyphs.gif"));
	    	bv.setDrawGlyphs(!bv.isDrawGlyphs());
			}*/
	    else if("hide nodes".equals(e.getActionCommand()))
			{
	    	bv.setDrawNodes(!bv.isDrawNodes());
			}
	    else if("hide edges".equals(e.getActionCommand()))
			{
	    	bv.setShowEdges(!bv.isShowEdges());
			}
	    else if("export".equals(e.getActionCommand()))
			{
	    	if(!bv.isPauseSimulation())	bv.pause();

	    	try{
	    		//----- Vector File Formats
	            ExportDialog export = new ExportDialog();
	            export.addExportFileType(new PDFExportFileType());
	            export.addExportFileType(new EMFExportFileType());
	            export.addExportFileType(new PSExportFileType());
		        export.showExportDialog( (Component)e.getSource(), "Export view as ...", bv, "export" );
	    	}catch(Exception ex){ex.printStackTrace();}

	    	if(bv.isPauseSimulation())	bv.pause();
	    	
			}
		}
	}
		
	public void configure()
		{
		if(!configurando)
		{
		configurando = true;
		
		// Obtenemos y configuramos la ventana de configuracion
		JInternalFrame ventanaConfig = this.getVentanaConfig();
		// Obtenemos el gestor de eventos de configuracion
		

		ConfigurationMenuManager gestor = new ConfigurationMenuManager(this,ventanaConfig,paleta,muestraColor);
		
		JPanel panelColor = this.getPanelPaleta(paleta, textoLabel, muestraColor);
		JPanel panelAnclajes = this.getPanelAnclajes(sesion, gestor);
		JPanel panelParametros = this.getPanelParametros();
		JPanel panelBotones = this.getPanelBotones(gestor);
		
		// Configuramos la ventana de configuracion
		//this.initPanelConfig(panelColor, panelAnclajes, panelParametros, panelBotones);
		this.initPanelConfig(panelColor, null, null, panelBotones);
					
		// Mostramos la ventana de configuracion
		ventanaConfig.setLocation(this.getWidth()/2, this.getHeight()/2);
		ventanaConfig.setTitle(Translator.instance.configureLabels.getString("s1")+" "+this.getName());
		sesion.getDesktop().add(ventanaConfig);
		try {
			ventanaConfig.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		ventanaConfig.pack();
		ventanaConfig.moveToFront();
		ventanaConfig.setVisible(true);
		}	
	}		
		
	public void endConfig(){
		sesion.setSelectionColor(paleta[OverlapperDiagram.selectionColor]);
		sesion.setSearchColor(paleta[OverlapperDiagram.searchColor]);
		sesion.setHoverColor(paleta[OverlapperDiagram.hoverColor]);
		sesion.setBicSet1Color(new Color(paleta[OverlapperDiagram.bicColor1].getRed(), paleta[OverlapperDiagram.bicColor1].getGreen(), paleta[OverlapperDiagram.bicColor1].getBlue(),100));
		sesion.setBicSet2Color(new Color(paleta[OverlapperDiagram.bicColor2].getRed(), paleta[OverlapperDiagram.bicColor2].getGreen(), paleta[OverlapperDiagram.bicColor2].getBlue(),100));
		sesion.setBicSet3Color(new Color(paleta[OverlapperDiagram.bicColor3].getRed(), paleta[OverlapperDiagram.bicColor3].getGreen(), paleta[OverlapperDiagram.bicColor3].getBlue(),100));
		
		if(bv!=null)			bv.setPalette(paleta);
		sesion.updateConfigExcept(this.getName());

		this.configurando = false;
	}
	
	public void updateConfig()
		{
		paleta[OverlapperDiagram.selectionColor]=sesion.getSelectionColor();
		paleta[OverlapperDiagram.searchColor]=sesion.getSearchColor();
		paleta[OverlapperDiagram.hoverColor]=sesion.getHoverColor();
		paleta[OverlapperDiagram.bicColor1]=sesion.getBicSet1();
		paleta[OverlapperDiagram.bicColor2]=sesion.getBicSet2();
		paleta[OverlapperDiagram.bicColor3]=sesion.getBicSet3();
		if(bv!=null)			bv.setPalette(paleta);
		
		}
	
	/*private void crearPanelParametros(){
		JPanel panel = new JPanel();
		
		this.setPanelParametros(panel);
	}*/
		
	private class GestorMouse implements MouseListener{
		
		public GestorMouse(){}
		
		public void mouseClicked(MouseEvent e)
			{
			Graph g=bv.getGraph();
			LinkedList<Integer> genes=new LinkedList<Integer>();
			 LinkedList<Integer> conditions=new LinkedList<Integer>();
			 Map<String, Node> map=g.getSelectedNodes();
			 Iterator itg=map.values().iterator();
			 while(itg.hasNext())
				 {
				 Node n=(Node)itg.next();
				 int id=0;
				 if(sesion.getMicroarrayData()!=null)
					 {
					 if(n.isGene())	
						 {
						 if(n.getId()<0)	id=sesion.getMicroarrayData().getGeneId(n.getLabel());//TODO: intentar librarse de esto con una llamada a update data
						 else				id=n.getId();
						 genes.add(id);
						 }
					 else
						 {
						 if(n.getId()<0)	id=sesion.getMicroarrayData().getConditionId(n.getLabel());
						 else				id=n.getId();
						 conditions.add(id);
						 }
					 }
				// System.out.println("Seleccionado "+n.getLabel()+" con id "+n.getId());
				 }
			 BiclusterSelection bs=new BiclusterSelection(genes,conditions);
			// System.out.println("Seleccionados "+genes.size()+" genes y "+conditions.size()+" conditions");
			 sesion.setSelectedBiclusters(bs, "lapper");
			
			/*
			if(e.getClickCount()==2)//Pinchamos dos 
				{
				if(bv.isAdditionMode())
					{
					utils.CustomColor c=new utils.CustomColor();
					c=c.getGoodColor(sesion.getNumVentanas()+sesion.getNumColores()-2);
					sesion.setNumColores(sesion.getNumColores()+1);
					bv.setLastColor(c);
					}
				bv.expandNode(e.getPoint().x, e.getPoint().y);
				}
				*/
			/*if(e.isControlDown())
				{
				String page=bv.getPage(e.getPoint().x, e.getPoint().y);
				
				Dimension dim = new Dimension(200,400);
				PanelHTMLInfo panelInfo = new PanelHTMLInfo(sesion, dim, page);
				VentanaPanel ventana = new VentanaPanel(sesion,sesion.getDesktop(),panelInfo);
				panelInfo.setVentana(ventana);
				sesion.setHTMLInfo(ventana);
				
				//panel.create();
				//panel.run();
				}
				*/
			}
		public void mouseEntered(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseMoved(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		}

	}