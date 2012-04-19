package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import javax.swing.ButtonGroup;
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
	private static final int foregroundColor=10;
	private static final int nodeLabelBackgroundColor=11;
	private static final int hoverNodeLabelColor=12;
	

	//Black background
	//private Color[] paleta = {null, null, null, null, null, null, new Color(195, 250, 190, 255), new Color(165, 175, 250, 255), Color.YELLOW, Color.BLACK, Color.WHITE, new Color(0,0,0,0), Color.WHITE};
	//White background
	private Color[] palette = {null, null, null, null, null, null, Color.DARK_GRAY, Color.DARK_GRAY, Color.BLUE, Color.WHITE, Color.DARK_GRAY, new Color(0,0,0,0), Color.BLACK};
	private String[] textoLabel = {"Selection", "Search","Hover", "Set 1", "Set 2", "Set 3", "Gene labels", "Condition labels",
			"Bicluster labels","Background", "Foreground", "Node Label Bgr.", "Hover Node Label"};
	private JTextField[] muestraColor = new JTextField[palette.length];
	
	
	//Informaci�n propia de nuestro panel
	Overlapper bv;
	JToolBar jtb;
	JLabel found;
	JRadioButton forFilms;
	JRadioButton forPersons;
	boolean configurando=false;
	
	int thresholdOption=0;
	float thresholdValue=0;
	boolean thresholdChanged=false;
	JTextField value, step;
	ButtonGroup cg; 
	JRadioButton constancet;
	
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
		
		palette[OverlapperDiagram.selectionColor]=sesion.getSelectionColor();
		palette[OverlapperDiagram.searchColor]=sesion.getSearchColor();
		palette[OverlapperDiagram.hoverColor]=sesion.getHoverColor();
		palette[OverlapperDiagram.bicColor1]=sesion.getBicSet1();
		palette[OverlapperDiagram.bicColor2]=sesion.getBicSet2();
		palette[OverlapperDiagram.bicColor3]=sesion.getBicSet3();

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
			bv.setPalette(palette);
			bv.setDataFile(sesion.getBiclusterDataFile());
			bv.setup(ancho,alto);//, sesion.getHoverColor(), sesion.getSelectionColor(), sesion.getSearchColor());
			if(sesion.getMicroarrayData()!=null)	bv.setMicroarrayData(sesion.getMicroarrayData());
			bv.buildGraph();
			bv.init();	
			
			JToolBar jtb=new JToolBar();
			addButtons(jtb);
			this.getWindow().add(bv, BorderLayout.CENTER);
			this.getWindow().add(jtb, BorderLayout.SOUTH);
				
			bv.addMouseListener(new MouseManager());	
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
	    
	    //decrease Threshold
	    button = makeNavigationButton("es/usal/bicoverlapper/resources/images/threshold.png", "set threshold",
	                                  "Set threshold",
	                                  "Set threshold");
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
	    buscar.addKeyListener(new SearchManager());
	   
	    
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
		button.addActionListener(new ButtonManager());
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
		button.addActionListener(new ButtonManager());

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
		//TODO: Actualizar lo que sea necesario cogi�ndolo de sesi�n.getXXX()

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
		//TODO: Lo que sea necesario para actualizar, sin necesidad de que haya cambio de datos de sesi�n
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
	
	
	private class SearchManager implements KeyListener{
			
		public SearchManager(){}
		
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
	
	
	private class ConfigurationManager implements ActionListener{
		public ConfigurationManager(){}
		
		public void actionPerformed(ActionEvent e)
		{
			int temp=thresholdOption;
		if ("overlap".equals(e.getActionCommand()))		thresholdOption=0;
		else if("size".equals(e.getActionCommand()))	thresholdOption=1;
		else if("constance".equals(e.getActionCommand()))	
			{
		/*	if(sesion.getMicroarrayData()==null)
				{
				JOptionPane.showMessageDialog(null,"This threshold can only be set if Microarray Data are loaded","Error",JOptionPane.INFORMATION_MESSAGE);
				Enumeration en=cg.getElements();
				for(int i=0;i<cg.getButtonCount();i++)
					{
					JRadioButton b=(JRadioButton)en.nextElement();
					if(i==temp)	b.setSelected(true);
					else		b.setSelected(false);
					}
				}
			else	*/thresholdOption=2;
			}
		if(temp!=thresholdOption)	thresholdChanged=true;
		}
	}
	/**
	 * Esta clase implementa un gestor para a�adir un anclaje a trav�s del panel correspondiente en la ventana de configuracion.
	 * 
	 * @author Javier Molpeceres Ortego
	 *
	 */
	private class ButtonManager implements ActionListener{
		
		public ButtonManager(){}
		
		public void actionPerformed(ActionEvent e)
		{
		//	System.out.println(e.getActionCommand());
		if ("pause".equals(e.getActionCommand())) 
			{
			JButton b=(JButton)e.getSource();
					
			if(!bv.isPauseSimulation())//No estaba en pausa, va a pasar a estarlo, cambiamos la imagen a play
				{
				b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/Play24.gif"));
				b.setToolTipText("Restore simulation");
				}
			else	//Volvemos a poner el de pause
				{
				b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/Pause24.gif"));
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
			bv.modifyThreshold(bv.getStep());
			thresholdValue=bv.getThresholdValue();
			}		
		 else if("set threshold".equals(e.getActionCommand()))
			{
	    	configure(1);
			}
		else if("decrease threshold".equals(e.getActionCommand()))
			{
			bv.modifyThreshold(-bv.getStep());
			thresholdValue=bv.getThresholdValue();
			}			
		else if("change model".equals(e.getActionCommand()))
			{
			if(bv.isRadial())//No estaba en pausa, va a pasar a estarlo, cambiamos la imagen a play
				{
				JButton b=(JButton)e.getSource();
						
				bv.radial2complete();
				b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/radial.gif"));
				}
			else	//Volvemos a poner el de pause
				{
				JButton b=(JButton)e.getSource();
				bv.complete2radial();
				b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/complete.gif"));
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
	    		b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/absoluteSize.gif"));
	    		b.setToolTipText("Absolute label size");
	    		}
	    	else
	    		{
	    		b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/relativeSize.gif"));
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
	    		b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/withHull.png"));
	    		b.setToolTipText("Draw zones");
	    		}
	    	else				
	    		{
	    		b.setIcon(loadIcon("es/usal/bicoverlapper/resources/images/withoutHull.png"));
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
	    	configure(0);
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
		
	public void configure(int sel)
		{
		if(!configurando)
		{
		configurando = true;
		
		// Obtenemos y configuramos la ventana de configuracion
		JInternalFrame ventanaConfig = this.getVentanaConfig();
		// Obtenemos el gestor de eventos de configuracion
		

		ConfigurationMenuManager gestor = new ConfigurationMenuManager(this,ventanaConfig,palette,muestraColor);
		
		JPanel panelColor = this.getPanelPaleta(palette, textoLabel, muestraColor);
		JPanel panelAnclajes = this.getPanelAnclajes(sesion, gestor);
		JPanel panelParametros = this.getThresholdParams();
		JPanel panelBotones = this.getPanelBotones(gestor);
		
		// Configuramos la ventana de configuracion
		//this.initPanelConfig(panelColor, panelAnclajes, panelParametros, panelBotones);
		this.initPanelConfig(panelColor, null, panelParametros, panelBotones, sel);
					
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
	
	private JPanel getThresholdParams()
		{
		if(this.getPanelParametros()!=null)
			{
			if(!constancet.isEnabled() && sesion.getMicroarrayData()!=null)
				{
				constancet.setEnabled(true);
				constancet.setText("Constance threshold");
				bv.setMicroarrayData(sesion.getMicroarrayData());//TODO: Esto de duplicar todo el microarray es regulero
				}
			value.setText(""+bv.getThresholdValue());
			return this.getPanelParametros();
			}
			
		JPanel panelP = new JPanel();
		
        panelP.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		panelP.setBackground(Color.LIGHT_GRAY);
		
		JRadioButton sizet=new JRadioButton("Size threshold");
		JRadioButton overlapt=new JRadioButton("Overlap threshold");
		constancet=new JRadioButton("Constance threshold");
		sizet.setToolTipText("Only biclusters with more genes+conditions than 'value' will be shown");
		overlapt.setToolTipText("Only nodes within more biclusters than 'value' will be shown");
		constancet.setToolTipText("Only biclusters with less standard deviation than 'value' will be shown");
		overlapt.setSelected(true);
		sizet.setActionCommand("size");
		overlapt.setActionCommand("overlap");
		constancet.setActionCommand("constance");
		if(sesion.getMicroarrayData()!=null && bv.getMicroarrayData()==null)	bv.setMicroarrayData(sesion.getMicroarrayData());//TODO: Esto de duplicar todo el microarray es regulero
		ConfigurationManager gconf=new ConfigurationManager();
		sizet.addActionListener(gconf);
		overlapt.addActionListener(gconf);
		constancet.addActionListener(gconf);
		
		cg=new ButtonGroup();
		cg.add(sizet);
		cg.add(overlapt);
		cg.add(constancet);
		
		//------
		JPanel radioPanel=new JPanel(new GridLayout(3,0));
		radioPanel.add(sizet);
		radioPanel.add(overlapt);
		radioPanel.add(constancet);
		//-----
		
		value=new JTextField(""+bv.getThresholdValue());
		value.setToolTipText("Value to be compared with the threshold criterion");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 6;
		panelP.add(radioPanel,c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 1;
		c.insets=new Insets(10,0,0,0);
		panelP.add(new JLabel("value   "),c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 1;
		c.ipadx = 10;
		panelP.add(value,c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 2;
		c.insets=new Insets(10,0,0,0);
		panelP.add(new JLabel("step   "),c);
		step=new JTextField(""+bv.getStep());
		step.setToolTipText("Step by which threshold is to be modified by means of '^' and 'V' buttons");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 2;
		c.ipadx = 10;
		panelP.add(step,c);
		
		/*panelP.add(radioPanel);
		panelP.add(new JLabel("value"));
		panelP.add(value);*/
		
		if(this.sesion.getMicroarrayData()==null)	
			{
			constancet.setText("Constance threshold (microarray data required)");
			constancet.setEnabled(false);
			}
		this.setPanelParametros(panelP);
		return this.getPanelParametros();
		}
	
	
	public void endConfig(){
		
		System.out.println("Opci�n "+thresholdOption+", "+value.getText());
		float temp=new Float(value.getText()).floatValue();
		if(temp!=thresholdValue)
			{
			thresholdChanged=true;
			thresholdValue=temp;
			}
		
		if(thresholdChanged)
			{
			bv.setThreshold(thresholdOption, thresholdValue);
			}
		bv.setStep(new Float(step.getText()).floatValue());
		
		sesion.setSelectionColor(palette[OverlapperDiagram.selectionColor]);
		sesion.setSearchColor(palette[OverlapperDiagram.searchColor]);
		sesion.setHoverColor(palette[OverlapperDiagram.hoverColor]);
		sesion.setBicSet1Color(new Color(palette[OverlapperDiagram.bicColor1].getRed(), palette[OverlapperDiagram.bicColor1].getGreen(), palette[OverlapperDiagram.bicColor1].getBlue(),100));
		sesion.setBicSet2Color(new Color(palette[OverlapperDiagram.bicColor2].getRed(), palette[OverlapperDiagram.bicColor2].getGreen(), palette[OverlapperDiagram.bicColor2].getBlue(),100));
		sesion.setBicSet3Color(new Color(palette[OverlapperDiagram.bicColor3].getRed(), palette[OverlapperDiagram.bicColor3].getGreen(), palette[OverlapperDiagram.bicColor3].getBlue(),100));
		
		if(bv!=null)			bv.setPalette(palette);
		sesion.updateConfigExcept(this.getName());

		this.configurando = false;
	}
	
	public void updateConfig()
		{
		palette[OverlapperDiagram.selectionColor]=sesion.getSelectionColor();
		palette[OverlapperDiagram.searchColor]=sesion.getSearchColor();
		palette[OverlapperDiagram.hoverColor]=sesion.getHoverColor();
		palette[OverlapperDiagram.bicColor1]=sesion.getBicSet1();
		palette[OverlapperDiagram.bicColor2]=sesion.getBicSet2();
		palette[OverlapperDiagram.bicColor3]=sesion.getBicSet3();
		if(bv!=null)			bv.setPalette(palette);
		
		}
	
	private class MouseManager implements MouseListener{
		
		public MouseManager(){}
		
		public void mouseClicked(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
			{
			
			Graph g=bv.getGraph();
			LinkedList<Integer> genes=new LinkedList<Integer>();
			LinkedList<Integer> conditions=new LinkedList<Integer>();
				 Map<String, Node> map=g.getSelectedNodes();
				 Iterator<Node> itg=map.values().iterator();
				 while(itg.hasNext())
					 {
					 Node n=itg.next();
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
					 }
				if(genes.size()>0 || conditions.size()>0)
					{
					 BiclusterSelection bs=new BiclusterSelection(genes,conditions);
					 sesion.setSelectedBiclusters(bs, "lapper");
					}
			}
		public void mouseEntered(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseMoved(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		}

	}