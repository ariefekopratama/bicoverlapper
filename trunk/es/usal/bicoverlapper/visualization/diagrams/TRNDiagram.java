package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.BasicStroke;
import java.awt.GridBagLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.activity.Activity;
import prefuse.action.RepaintAction;
import prefuse.action.ItemAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.FocusControl;
import prefuse.controls.HoverActionControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import java.awt.event.MouseEvent;

//Search Panel
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.RegexSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.Table;
import prefuse.util.FontLib;
import prefuse.util.collections.IntIterator;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import javax.swing.BorderFactory;
import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.SwingConstants;
import javax.swing.BoxLayout;


import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.data.TRNData;
import es.usal.bicoverlapper.kernel.DiagramWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.managers.ConfigurationMenuManager;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.BubblesDiagram.NodeStrokeAction;
/*import diagramas.PanelCoordenadasParalelas.GestorCambioVars;
import diagramas.PanelCoordenadasParalelas.GestorCursor;
import diagramas.PanelCoordenadasParalelas.GestorScrolls;
import diagramas.PanelCoordenadasParalelas.GestorSeleccionarTupla;
import diagramas.PanelCoordenadasParalelas.Linea;*/
import prefuse.controls.ControlAdapter;
import prefuse.data.search.PrefixSearchTupleSet;//Search
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.TableIterator;

//Animación de color:
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.data.event.TupleSetListener;
import prefuse.data.Tuple;

/**
 * This Class implements the visualization of a TRN as a force-directed layout graph, based in prefuse library
 * @author Rodrigo Santamaria
 *
 */
public class TRNDiagram extends Diagram {

	private static final long serialVersionUID = 1L;
	
	// atributos del panel del diagrama
	private Session sesion;
	private MultidimensionalData datos;  //  @jve:decl-index=0:
	private int alto;
	private int ancho;
	private boolean atributosIniciados = false, configurando = false, diagramaPintado = false;
	
		
	// definicion de margenes del diagrama
	
	final int margenDer = 40;
	final int margenIzq = 40;
	final int margenSup = 25;
	final int margenInf = 40;
	final int margenDiagrama = 10; // porcentaje de exceso en intervalo de representacion del diagrama
	
	// configuracion de color
	private static final int selectionColor = 0;
	private static final int searchColor = 1;
	private static final int hoverColor = 2;
	private static final int colorActivation = 3;
	private static final int colorInhibition = 4;
	private static final int colorBackground = 5;
	private Color[] paleta = {null, null, null, new Color(0,0,0,123), new Color(200,200,200,123), Color.WHITE};
	private String[] textoLabel = {"Selection", "Search", "Hover", "Activation edge",
								   "Inhibition edge", "Background"};
	private JTextField[] muestraColor = new JTextField[paleta.length];

	private static final int discretizationLevels = 0;
	private int[] parameters = {128};
	private String[] parameterText = {"Discretization Levels"};

	
	// atributos de configuracion anclajes
	
	private DiagramWindow itemAñadir, itemEliminar;

	//-------------- Información propia de nuestra representación
	Visualization v; //Visualization
	Display d;		//Display
	JInternalFrame frame;	//Frame
	TRNData trnd;  //  @jve:decl-index=0:
	TRNFocusControl currentGenes;  //  @jve:decl-index=0:
	ItemAction expressionColor;
	ItemAction nodeColor;
	DataColorAction eFill, edges;
	int[] palette = new int[]{ ColorLib.gray(0,0)};
	
	/**
	 * Default constructor
	 */
	public TRNDiagram() {
		super();
	//	initialize();
	}

	/**
	 * Constructor from a path where the graph information is in
	 * @param inPath	file with Graphic informatino
	 * @param f
	 */
	/*public TRNDiagram(String inPath, JInternalFrame f)
		{
		trnd=new TRNData(inPath);
		frame=f;
		create(trnd);
		}*/
	
	/**
	 * Session Constructor
	 * @param session	Session in which this diagram is in. It must have TRN data loaded
	 * @param dim		Dimension for this diagram
	 */
	public TRNDiagram(Session session, Dimension dim)
		{
		super(new BorderLayout());
		int num = session.getNumTRNDiagrams();
		this.setName("Transcription Network "+num);
		this.sesion = session;
		this.datos = session.getData();
		this.trnd = session.getTRNData();
		this.alto = (int)dim.getHeight();
		this.ancho = (int)dim.getWidth();
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);
		

		paleta[TRNDiagram.selectionColor]=sesion.getSelectionColor();
		paleta[TRNDiagram.searchColor]=sesion.getSearchColor();
		paleta[TRNDiagram.hoverColor]=sesion.getHoverColor();
		}
	
	/**
	 * Takes the TRN data again loaded in the Session an rebuilds the diagram 
	 */
	public void updateData() 
		{
		this.trnd = sesion.getTRNData();
		this.repaint();		
		}

	private void iniciarAtributos(){
		trnd=sesion.getTRNData(); //Iniciamos los datos de la vista
		if(trnd==null)	System.out.println("No tenemos los datos!!!");
		else			System.out.println("Tenemos los datos!!!");
		/*
		// iniciamos los atributos de la representacion del diagrama		
		this.maxVar = new double[datos.getNumVars()];
		this.minVar = new double[datos.getNumVars()];
		this.ratio = new double[datos.getNumVars()];
		
		// iniciamos los atributos del cambio de variables
		this.ejesVars = new Line2D.Double[datos.getNumVars()];
		this.ordenVars = new int[datos.getNumVars()];
		for(int i = 0; i<ordenVars.length; i++)
			ordenVars[i] = i;
		
		// iniciamos los atributos de la seleccion de tuplas
		tuplas = new Linea[datos.getNumElems()][datos.getNumVars()-1];
		tuplaSeleccionada = -1;
		
		// iniciamos los valores de la acotacion del intervalo de representacion
		this.scrollSup = new Rectangle2D.Double[datos.getNumVars()];
		this.scrollInf = new Rectangle2D.Double[datos.getNumVars()];
		this.cotaSup = new int[datos.getNumVars()];
		this.cotaInf = new int[datos.getNumVars()];
		
		this.calcularAtributos();
		
		// iniciamos los botones de scroll para acotar
		// el intervalo de representacion
		
		scrollUp = new ImageIcon(this.iconoScrollUp).getImage();
		scrollDown = new ImageIcon(this.iconoScrollDown).getImage();
		scrollSelecUp = new ImageIcon(this.iconoScrollSelecUp).getImage();
		scrollSelecDown = new ImageIcon(this.iconoScrollSelecDown).getImage();
						
		altoScroll = scrollUp.getHeight(null)-1;
		anchoScroll = scrollUp.getWidth(null)-1;
		
		for(int i = 0; i < datos.getNumVars(); i++){
			
			Rectangle2D.Double scroll = new Rectangle2D.Double(margenIzq+i*intervaloVar-anchoScroll/2,
															   margenSup-altoScroll-margenScroll,anchoScroll,altoScroll);
			
			scrollSup[i] = scroll;
			
			scroll = new Rectangle2D.Double(margenIzq+i*intervaloVar-anchoScroll/2,
											margenSup+longEjeY+margenScroll,anchoScroll,altoScroll);
			
			scrollInf[i] = scroll;
			
			cotaSup[i] = margenSup-margenScroll;
			cotaInf[i] = alto-margenInf+margenScroll;
		}
		*/
		
		atributosIniciados = true;
	}
	
	/**
	 * Generates the trn graph
	 *
	 */
	public void create()
		{
		iniciarAtributos();
		create(trnd);
		}
	
	/**
	 * Constructs a JPanel with all prefuse stuff to represent a Transcriptional Regulatory
	 * Network with a Directed Acylic Graph
	 * @param trnd - Graph to be represented
	 */
	void create(TRNData trnd)
		{
		//----------------------------------VISUALIZATION
		v = new Visualization();
		v.add("graph", trnd.getGraph());		//Le añadimos el grafo
		//v.addFocusGroup("ingroup('_bicluster_')");//Grupo de biclusters
		v.getGroup("graph.nodes").addColumn("expressionLevel", double.class);

		//Hacemos más gruesos los bordes para poderlos utilizar en la interacción
		TupleSet ts=v.getGroup("graph.nodes");
		Iterator it=ts.tuples();
		BasicStroke bs=new BasicStroke(3);
		while(it.hasNext())
			{
			VisualItem vi=(VisualItem)it.next();
			vi.setStroke(bs);
			}
		
        //Forma de los nodos (etiquetados por nombre, redondeados)
		LabelRenderer r = new LabelRenderer("name");
		r.setRoundedCorner(8, 8); // round the corners
		
			
		//Forma de las aristas (dirigidas, curvadas)
		EdgeRenderer edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE,
				                			Constants.EDGE_ARROW_FORWARD);
		edgeRenderer.setArrowHeadSize(5,10);
		
		//Renderer con etiquetas y aristas
		v.setRendererFactory(new DefaultRendererFactory(r, edgeRenderer));
		
	
		
		//--------- color
		int[] ePalette = new int[]{ paleta[TRNDiagram.colorActivation].getRGB(), ColorLib.gray(100,123), paleta[TRNDiagram.colorInhibition].getRGB()};

		//Item action (colores de los nodos y sus etiquetas bajo distintas circunstancias)
		nodeColor=new NodeColorAction("graph.nodes", VisualItem.STROKECOLOR, sesion.getHoverColor().darker(), sesion.getSearchColor(), sesion.getSelectionColor());
		ItemAction textColor=new TextColorAction("graph.nodes");

		//	 map nominal data values to colors using our provided palette
		ColorAction text = new ColorAction("graph.nodes",
		    VisualItem.TEXTCOLOR, ColorLib.gray(0));
		//	 use light grey for edges
	//	ColorAction edges = new ColorAction("graph.edges",
	//    VisualItem.STROKECOLOR, ColorLib.gray(200));
		edges = new DataColorAction("graph.edges", "type",
		    Constants.NOMINAL, VisualItem.STROKECOLOR, ePalette);
		eFill = new DataColorAction("graph.edges", "type",
			    Constants.NOMINAL, VisualItem.FILLCOLOR, ePalette);
	
		//palette=ColorLib.getCoolPalette();
		int paletteTemp[]=ColorLib.getInterpolatedPalette(ColorLib.rgb(0,255,0), ColorLib.rgb(0, 0, 0));
		int paletteTemp2[]=ColorLib.getInterpolatedPalette(ColorLib.rgb(0,0,0), ColorLib.rgb(255, 0, 0));
		palette=new int[100];
		for(int i=0;i<50;i++)	palette[i]=paletteTemp[i];
		for(int i=50;i<100;i++)	palette[i]=paletteTemp2[i-50];
		
		for(int i=0;i<palette.length;i++)	palette[i]=ColorLib.setAlpha(palette[i], 100);
		palette[palette.length-1]=ColorLib.setAlpha(palette[palette.length-1],0);
		expressionColor=new ExpressionColorAction("graph.nodes", "expressionLevel", palette);
		
		//	create an action list containing all color assignments
		ActionList color = new ActionList();
		color.add(text);
		color.add(edges);
		color.add(eFill);
		
		color.add(nodeColor);
		color.add(textColor);
		
		color.add(expressionColor);
		
		color.add(new RepaintAction());
		v.putAction("color", color);
		
		
		
		// layout
		//ActionList layout = new ActionList(Activity.INFINITY);
		ActionList layout = new ActionList(10000);//Mejor, se queda quieto al final
		ForceDirectedLayout fdl=new ForceDirectedLayout("graph");
		fdl.setLayoutAnchor( (Point2D)(new Point2D.Double(1024,768)));

		layout.add(fdl);
		layout.add(new RepaintAction());
		v.putAction("layout", layout);
		
	    // filtering
		ActionList filter=new ActionList();
	    filter.add(nodeColor);
    	filter.add(new RepaintAction());
        v.putAction("filter", filter);
        
        // animated transition
        ActionList animate = new ActionList(1250);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(new QualityControlAnimator());
        animate.add(new VisibilityAnimator("graph"));
//        animate.add(new PolarLocationAnimator("graph.nodes", linear));
        animate.add(new ColorAnimator("graph.nodes"));
        animate.add(new RepaintAction());
        v.putAction("animate", animate);
        v.alwaysRunAfter("filter", "animate");
        v.alwaysRunAfter("layout", "animate");////Igual queda un poco mal
        
        // animate paint change
        ActionList animatePaint = new ActionList(400);
        animatePaint.add(new ColorAnimator("graph.nodes"));
        animatePaint.add(new RepaintAction());
        v.putAction("animatePaint", animatePaint);
		
		//Escucha por cambios en el conjunto de elementos encontrados
		SearchTupleSet sts = new PrefixSearchTupleSet();//Le añadimos un conjunto de búsqueda
        v.addFocusGroup(Visualization.SEARCH_ITEMS, sts);		
        sts.addTupleSetListener(new TupleSetListener() 
        	{
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) 
            	{
                v.cancel("animatePaint");
                v.run("color");
                v.run("animatePaint");
            	}
        	});  

        //----- display
		// create a new Display that pull from our Visualization
        if(d==null)		
        	{
        	d = new Display(v);
        	d.setSize(800, 600); // set display size
    		d.setHighQuality(true);
    		d.pan(400,300);
    		
    		d.addControlListener(new DragControl()); // drag items around
    		d.addControlListener(new PanControl());  // pan with background left-drag
    		d.addControlListener(new ZoomControl()); // zoom with vertical right-drag
    		d.addControlListener(new WheelZoomControl()); // zoom to fit screen
    		d.addControlListener(new ZoomToFitControl()); // zoom to fit screen
    		d.addControlListener(new HoverActionControl("color")); // zoom with vertical right-drag
    		currentGenes=new TRNFocusControl(sesion,"filter", Visualization.FOCUS_ITEMS, v);
    		d.addControlListener(currentGenes); // zoom with vertical right-drag

    		//FRAME
    		this.getWindow().add(d);
        	}
        else	d.setVisualization(v);
						
		// Caja de búsqueda:
        SearchQueryBinding sq = new SearchQueryBinding(
        	  (Table)v.getGroup("graph.nodes"), "name",
              (SearchTupleSet)v.getGroup(Visualization.SEARCH_ITEMS));
              JSearchPanel search = sq.createSearchPanel();
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5,5,4,0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
        
        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
        
        d.addControlListener(new ControlAdapter() 
       		{
            public void itemEntered(VisualItem item, MouseEvent e) 
            	{
                if ( item.canGetString("name") )
                    title.setText(item.getString("name"));
            	}
            public void itemExited(VisualItem item, MouseEvent e) 
            	{
                title.setText(null);
            	}
       		});
        
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(Box.createHorizontalGlue());
        box.add(search);
        box.add(Box.createHorizontalStrut(3));
        
        this.add(d, BorderLayout.CENTER);	//El display con el grafo
        this.add(box, BorderLayout.SOUTH);	//La caja de búsqueda
        
        Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.DARK_GRAY;

        UILib.setColor(this, BACKGROUND, FOREGROUND);
        this.getWindow().setContentPane(this);
		this.getWindow().pack();           // layout components in window
	}
	
	/**
	 * Runs the visualizations of the diagram.
	 *
	 */
	public void run()
		{
		this.getWindow().setVisible(true);
	
		v.run("color");  // assign the colors
		v.run("layout"); // start up the animated layout
		v.run("filter");
		v.run("animate");
		}
	
	/**
	 * Repaints the Diagram, taking into account the selections in the Session
	 */
	public void repaint()
		{
		//Miramos qué tenemos que hacer con posibles selecciones de biclusters
		if(v!=null && sesion!=null && sesion.getSelectedBicluster()!=null)
			{
		//	System.out.println("REPINTAMOS TRN");
			LinkedList l=sesion.getSelectedGenesBicluster();
			//Quitamos todos los que estuvieran antes en el bicluster
			currentGenes.clear();
			if(l.size()>0)
				{//marcamos como seleccionados todos los nodos uqe estén en l
				int inicio=0;
				for(int i=inicio;i<l.size();i++)//Las que vienen  de las burbujas meten un elemento primero!!!
					{
					String id=((Integer)(l.get(i))).toString();
					Node n=trnd.getGraph().getNode(new Integer(id).intValue());
					for(int j=0;j<n.getColumnCount();j++)	System.out.println(n.getColumnName(j));
					VisualItem item=v.getVisualItem("graph.nodes", n);
					currentGenes.addItem(item);
					}
				}
			l=sesion.getSelectedConditionsBicluster();
			System.out.println("El número de condiciones seleccionado es "+ l.size());
			if(l.size()==1)
				{
				Vector<Double> vect=sesion.getConditionExpressions();
				Iterator it=v.items("graph.nodes");
				
				for(int i=0;i<vect.size();i++)
					{
					VisualItem item=(VisualItem)it.next();
					item.setDouble("expressionLevel", vect.get(i));
					//item.setInt("_color", )
					}
				}
			v.cancel("animatePaint");
			v.run("color");
			v.run("animatePaint");
			}
		}

	/**
	 * Updates the Diagram with the session layer current information.
	 */
	public void update() 
		{
		System.out.println("Repintamos PanelTRN");
		this.repaint();
		}
	
	public void updateConfig(){
		paleta[TRNDiagram.selectionColor]=sesion.getSelectionColor();
		paleta[TRNDiagram.searchColor]=sesion.getSearchColor();
		paleta[TRNDiagram.hoverColor]=sesion.getHoverColor();
		
		nodeColor=new NodeColorAction("graph.nodes", VisualItem.STROKECOLOR, sesion.getHoverColor().darker(), sesion.getSearchColor(), sesion.getSelectionColor());
		
		//	create an action list containing all color assignments
		ActionList color = (ActionList)v.getAction("color");
		color.remove(nodeColor);
		color.add(nodeColor);
		v.putAction("color", color);
		
		
		// filtering
		ActionList filter=(ActionList)v.getAction("filter");
        filter.remove(nodeColor);
		filter.add(nodeColor);
        v.putAction("filter", filter);
        	
        ActionList animate = (ActionList)v.getAction("layout");
        animate.setDuration(0);
        
        
		run();
		this.repaint();
		this.configurando = false;
	}

	/*
	private void crearPanelParametros(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("Parameters",constraints);
		JRadioButton boton = new JRadioButton();
		boton.setSelected((Boolean)this.parametros[isEjesRelativos]);
		constraints.gridx = 1;
		constraints.weightx = 1.0;
		constraints.anchor = GridBagConstraints.WEST;
		panel.add(boton,constraints);
		
		this.setPanelParametros(panel);
	}*/

	
	public int getId(){
		return es.usal.bicoverlapper.kernel.Configuration.TRN_ID;
	}
	
	

	/**
	 * Pops up a configuration panel for parallel coordinates properties
	 * TODO: Still in development
	 */
	public void configure(){
		if(!configurando){
			configurando = true;
			
			// Obtenemos y configuramos la ventana de configuracion
			
			JInternalFrame ventanaConfig = this.getVentanaConfig();
			
			// Obtenemos el gestor de eventos de configuracion
			
			//GestorMenuConfiguracion gestor = new GestorMenuConfiguracion(this,ventanaConfig,paleta,muestraColor);
			//ConfigurationMenuManager gestor = new ConfigurationMenuManager(this,ventanaConfig,paleta,muestraColor, colorVarSelec);
			ConfigurationMenuManager gestor = new ConfigurationMenuManager(this,ventanaConfig,paleta,muestraColor);
			
			// Creamos los paneles de configuracion
			
			//this.crearPanelParametros();
			
			JPanel panelColor = this.getPanelPaleta(paleta, textoLabel, muestraColor);
			//JPanel panelAnclajes = this.getPanelAnclajes(sesion, gestor);
			//JPanel panelParametros = this.getPanelParametros();
			JPanel panelBotones = this.getPanelBotones(gestor);
			
			// Configuramos la ventana de configuracion
			
		//	this.initPanelConfig(panelColor, panelAnclajes, panelParametros, panelBotones);
			this.initPanelConfig(panelColor, null, null, panelBotones);
							
			// Mostramos la ventana de configuracion
			
			ventanaConfig.setLocation(getPosition());
			ventanaConfig.setTitle(Translator.instance.configureLabels.getString("s1")+" "+this.getName());
			sesion.getDesktop().add(ventanaConfig);
			try {
				ventanaConfig.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			ventanaConfig.pack();
			ventanaConfig.setVisible(true);
		}
	}
	
	/**
	 * Notifies the end of configuration
	 */
	public void endConfig(){
		sesion.setSelectionColor(paleta[TRNDiagram.selectionColor]);
		sesion.setSearchColor(paleta[TRNDiagram.searchColor]);
		sesion.setHoverColor(paleta[TRNDiagram.hoverColor]);
		
		int[] ePalette = new int[]{ paleta[TRNDiagram.colorActivation].getRGB(), ColorLib.gray(100,123), paleta[TRNDiagram.colorInhibition].getRGB()};
		nodeColor=new NodeColorAction("graph.nodes", VisualItem.STROKECOLOR, sesion.getHoverColor().darker(), sesion.getSearchColor(), sesion.getSelectionColor());
		
		edges = new DataColorAction("graph.edges", "type",
			    Constants.NOMINAL, VisualItem.STROKECOLOR, ePalette);
		eFill = new DataColorAction("graph.edges", "type",
			    Constants.NOMINAL, VisualItem.FILLCOLOR, ePalette);
	
		//	create an action list containing all color assignments
		ActionList color=(ActionList)v.getAction("color");
		color.remove(nodeColor);
		color.remove(edges);
		color.remove(eFill);

		color.add(edges);
		color.add(eFill);
		color.add(nodeColor);
		
		v.putAction("color", color);

	    // filtering
		ActionList filter=(ActionList)v.getAction("filter");
	    filter.remove(nodeColor);
		filter.add(nodeColor);
    	v.putAction("filter", filter);
    	
    	ActionList animate = (ActionList)v.getAction("layout");
          animate.setDuration(0);
        

		this.run();
		this.configurando = false;
		
		
	}

	
	/**
     * Set node fill colors
     */
    static class ExpressionColorAction extends DataColorAction 
    	{
		ExpressionColorAction(String group, String sel, int[] palette) 
        	{
            super(group, sel, Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
            //add("_hover", ColorLib.gray(120,229)); //Pinta amarillo si pasamos por encima de un nodo
           // add("ingroup('_search_')", ColorLib.rgba(0,255,0,250));
           // add("ingroup('_focus_')", ColorLib.rgba(255,0,255,229));//Pinta morado si pinchamos en un nodo
           // add("ingroup('_bicluster_')", ColorLib.rgba(255,255,0,229));//Pinta morado si pinchamos en un nodo
            }
		} // end of inner class NodeColorAction

    static class NodeColorAction extends ColorAction 
	{
	NodeColorAction(String group, String field, Color hoverColor, Color searchColor, Color selectionColor) 
    	{
        super(group, field, ColorLib.gray(0,0));
        
        //add("_hover", ColorLib.gray(120,229)); //Pinta amarillo si pasamos por encima de un nodo
        add("_hover", ColorLib.rgba(hoverColor.getRed(), hoverColor.getGreen(), hoverColor.getBlue(),hoverColor.getAlpha())); //Pinta amarillo si pasamos por encima de un nodo
        add("ingroup('_search_')", ColorLib.rgba(searchColor.getRed(), searchColor.getGreen(), searchColor.getBlue(), searchColor.getAlpha()));//Verde los buscados
        add("ingroup('_focus_')", ColorLib.rgba(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), selectionColor.getAlpha()));
        
        add("ingroup('_bicluster_')", ColorLib.rgba(255,255,0,229));//Pinta morado si pinchamos en un nodo
        }
	} // end of inner class NodeColorAction
    
    /**
     * Set node label colors
     */
    static class TextColorAction extends ColorAction 
    	{
		TextColorAction(String group) 
        	{
            super(group, VisualItem.TEXTCOLOR, ColorLib.gray(0));
            add("_hover", ColorLib.rgb(255,0,0));
    		}
    	} // end of inner class NodeColorAction

}
