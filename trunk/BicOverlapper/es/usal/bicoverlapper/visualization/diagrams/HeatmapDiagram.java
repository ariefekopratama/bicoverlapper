package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.distortion.BifocalDistortion;
import prefuse.action.layout.Layout;
import prefuse.controls.AnchorUpdateControl;
import prefuse.controls.ControlAdapter;
import prefuse.controls.HoverActionControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Table;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.Sort;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.DataLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.DiagramWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.managers.ConfigurationMenuManager;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.TRNDiagram.NodeColorAction;

/**
 * This diagram represents a Microarray data matrix as the typical expression level heatmap.
 * It implements bifocal distortion to see gene and expression profiles and it is linked to other
 * views by the session layer. 
 * The structure of the heatmap is based in Prefuse library.
 * @author Rodrigo Santamaria
 *
 */
public class HeatmapDiagram extends Diagram
	{
private static final long serialVersionUID = 1L;
	
	// atributos del panel del diagrama
	private Session sesion;
	MultidimensionalData datos;  //  @jve:decl-index=0:
	private int alto;
	private int ancho;
	boolean atributosIniciados = false, configurando = false, diagramaPintado = false;
		
	// definicion de margenes del diagrama
	
	final int margenDer = 40;
	final int margenIzq = 40;
	final int margenSup = 25;
	final int margenInf = 40;
	final int margenDiagrama = 10; // porcentaje de exceso en intervalo de representacion del diagrama
	
	// configuracion de color
	static final int lowColor = 0;
	static final int zeroColor = 1;
	static final int highColor = 2;
	static final int selectionColor = 3;
	static final int hoverColor = 4;
	//private Color[] paleta = {Color.BLUE, Color.WHITE, Color.RED, Color.BLUE, Color.YELLOW};
	private Color[] paleta;
	String[] textoLabel = {"Lowest Expression", "Zero Expression", "Highest expression", "Selection", "Hover"};
	//JTextField[] muestraColor =new JTextField[paleta.length];
	JTextField[] muestraColor;
	
	// atributos de configuracion anclajes
	DiagramWindow itemToAdd, itemToRemove;

	// Informaci�n propia del heatmap
	MicroarrayData md;
	Visualization v; //Visualization
	Visualization v2; //TODO: Visualization of conditions (testing)
	
	Display d;		//Display (equivalente al frame en otros casos)
	//FisheyeDistortion fd;
	//BifocalDistortion fd;
//	private HeatmapFocusControl currentGenes;
	private HeatmapFocusControl currentLevels;
	
	int contName=0;
//	int geneMargin=0;
	int geneMargin=100;
	int conditionMargin=100;
//	int conditionMargin=0;
	//private double m_scale=60;	
	private double m_scale=2*4;	//OJOOO: Important�simo: m_scale*rango debe ser inexorablemente < 1, o salen escalas negativas y todo se va a la mierda
	private int[] palette;
	MicroGridLayout gl;
	
	ExpressionColorAction exprColor;
	StrokeColorAction strokeColor;
	ColorAction rectangleStrokeColor;
	
	BiclusterSelection bicAnt=null;//para evitar bucles infinitos create-update
	
	/**
	 * Default constructor
	 */
	public HeatmapDiagram() 
		{
		super();
		}
	
	/**
	 * Session Constructor
	 * @param sesion	Session to which this diagram is linked
	 * @param dim		Default dimensions for the diagram
	 */
	public HeatmapDiagram(Session sesion, Dimension dim)
		{
		super(new BorderLayout());
		int num = sesion.getNumHeatmapDiagrams();
		this.setName("Microarray Heatmap "+num);
		this.sesion = sesion;
		this.md = sesion.getMicroarrayData();
		this.alto = (int)dim.getHeight();
		this.ancho = (int)dim.getWidth();
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);
		this.paleta=new Color[]{sesion.lowExpColor, sesion.avgExpColor, sesion.hiExpColor, sesion.getSelectionColor(), sesion.getHoverColor()};
		muestraColor=new JTextField[paleta.length];
		}
	
	//As create(), but uses less massive matrices
	/**
	 * Generate internal structure of the heatmap from the specified tables
	 * @param expressions	table with expression levels, each row corresponding to a gene
	 * @param genes			table with gene names
	 * @param conditions	table with condition names
	 */
	public void create(Table expressions, Table genes, Table conditions)
		{
		cancelAllActions();
		v = new Visualization();
		v.addTable("matrix", expressions);					//Le a�adimos nuestra tabla
		v.addTable("geneLabels", genes);				//Le a�adimos nuestra tabla
		v.addTable("conditionLabels", conditions);		//Le a�adimos nuestra tabla
		generateRest();
		}
	
	public void update(Table expressions, Table genes, Table conditions)
		{
		if(v==null)	{create(expressions, genes, conditions); return;}
		v.removeGroup("matrix");
		v.removeGroup("geneLabels");
		v.removeGroup("conditionLabels");
		v.addTable("matrix", expressions);					//Le a�adimos nuestra tabla
		v.addTable("geneLabels", genes);				//Le a�adimos nuestra tabla
		v.addTable("conditionLabels", conditions);		//Le a�adimos nuestra tabla
		}
	
	/**
	 * Generates internal structure of the heatmap from the data taken from the session layer
	 *
	 */
	public void create()
		{
		//--------------------------- visualization -----------------------
		cancelAllActions();
		v = new Visualization();
		//v.addTable("matrix", md.getExpressions());					//Le a�adimos nuestra tabla
		//v.addTable("geneLabels", md.getGeneLabels());				//Le a�adimos nuestra tabla
		v.addTable("matrix", md.getSparseExpressions());		//SPARSE
		v.addTable("geneLabels", md.getSparseGeneLabels());		//SPARSE
		v.addTable("conditionLabels", md.getConditionLabels());		//Le a�adimos nuestra tabla
		
		generateRest();
		update();
		}
	
	void generateRest()
		{
		Table rect=new Table();//Para el rectangulo que rodea a la seleccion
		rect.addColumn("rowId", int.class);
		rect.addColumn("colId", int.class);
		int row=rect.addRow();
		rect.setInt(row, "rowId", 0);
		rect.setInt(row, "colId", 0);
		
		
		//v.addTable("rectangle", rect);
		
		double ts=System.currentTimeMillis();
		double t1=System.currentTimeMillis();
		double t2=System.currentTimeMillis();
		Rectangle2D rc=new Rectangle2D.Double(geneMargin,0,ancho-geneMargin,conditionMargin);
		Rectangle2D rg=new Rectangle2D.Double(0,conditionMargin,geneMargin,alto-conditionMargin);
		Rectangle2D rdata=new Rectangle2D.Double(geneMargin,conditionMargin,ancho-geneMargin,alto-conditionMargin);

		//El renderer tendr� tres partes ahora
		int paletteTemp[]=ColorLib.getInterpolatedPalette(paleta[HeatmapDiagram.lowColor].getRGB(), paleta[HeatmapDiagram.zeroColor].getRGB());
		int paletteTemp2[]=ColorLib.getInterpolatedPalette(paleta[HeatmapDiagram.zeroColor].getRGB(), paleta[HeatmapDiagram.highColor].getRGB());
		//TODO: revisar, hay dos niveles de negro, el ultimo de temp y el primero de temp2
		palette=new int[paletteTemp.length+paletteTemp2.length];
		for(int i=0;i<paletteTemp.length;i++)	palette[i]=paletteTemp[i];
		for(int i=paletteTemp.length;i<palette.length;i++)	palette[i]=paletteTemp2[i-paletteTemp.length];
		
		
		final int ngtot=md.getNumGenes();
		final int ng=md.getNumSparseGenes(); //SPARSE
		final int nc=md.getNumConditions();
		
		
        v.setRendererFactory(new RendererFactory() {
        	AbstractShapeRenderer sr = new ExpressionRenderer(ng, nc, alto-conditionMargin, ancho-geneMargin);
        	Renderer arY = new LabelRenderer2("name");
            Renderer arX = new RotationLabelRenderer("name",300);//270 or 300
        //	 Renderer arX = new RotationLabelRenderer("name",270);//270 or 300
                    
          //  AbstractShapeRenderer rRect = new ExpressionRenderer(1, 1, alto-conditionMargin, ancho-geneMargin);
        	         
            public Renderer getRenderer(VisualItem item) {
            	if(item.isInGroup("geneLabels"))			return arY;
            	else
            		{
            		if(item.isInGroup("conditionLabels"))	return arX;
            		else
            			return sr;
            		}
                }
        });
    	
      t1=System.currentTimeMillis();
		//System.out.println("2) Renderers: "+(t1-t2)/1000);
        
		//ItemAction rectSize=new SizeAction("rectangle", 100);
		//ItemAction rectStroke=new StrokeAction("rectangle", new BasicStroke(2));

    	//exprColor=new LevelColorAction("matrix", "level", palette);
  		//exprColor.setMaxScale(max);
    	//exprColor.setMinScale(min);
      	double min=DataLib.min(md.getExpressions(), "level").getDouble("level");
    	double max=DataLib.max(md.getExpressions(), "level").getDouble("level");
        //exprColor.setOrdinalMap(m_omap.keySet().toArray());
    	
    	exprColor=new ExpressionColorAction("matrix", "level", Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
    	System.out.println("Max y min son: "+max+", "+min);
      	Map m_omap = DataLib.ordinalMap(md.getExpressions(), "level");
      	Object[] ar=m_omap.keySet().toArray();
      	List<Object> al=Arrays.asList(ar);
      	List<Double> ad=new ArrayList<Double>();
      	for(Object o : al)    		
      		{
      		ad.add((Double)o);
      		}
      	Collections.sort(ad);
      	exprColor.setOrdinalMap(ad.toArray());
      	
    //	strokeColor=new StrokeColorAction("matrix", "level", palette, sesion.getHoverColor(), sesion.getSelectionColor());
      	ColorAction textColor=new ColorAction("matrix", VisualItem.TEXTCOLOR, ColorLib.gray(255,0));
		
		ColorAction labelGeneTextColor=new ColorAction("geneLabels", VisualItem.TEXTCOLOR, ColorLib.gray(0));
		labelGeneTextColor.add("_hover", ColorLib.rgb(255,0,0));
		ColorAction labelGeneStroke=new ColorAction("geneLabels", VisualItem.STROKECOLOR, ColorLib.gray(100));
		
		ColorAction labelConditionTextColor=new ColorAction("conditionLabels", VisualItem.TEXTCOLOR, ColorLib.gray(0));
		labelConditionTextColor.add("_hover", ColorLib.rgb(255,0,0));
		ColorAction labelConditionColor=new ColorAction("conditionLabels", VisualItem.FILLCOLOR, ColorLib.gray(255,0));
//		ColorAction labelConditionStroke=new ColorAction("conditionLabels", VisualItem.STROKECOLOR, ColorLib.gray(100));
		//	labelGeneStroke.add("_hover", ColorLib.rgb(255,0,0));
		//TODO: Hace falta implementar una acci�n para rotar las etiquetas de las condiciones (ver RotationControl)
		
		rectangleStrokeColor=new ColorAction("rectangle", VisualItem.STROKECOLOR, sesion.getSelectionColor().getRGB());
		ColorAction rectangleFillColor=new ColorAction("rectangle", VisualItem.FILLCOLOR, ColorLib.alpha(0));
		
		ActionList color = new ActionList();
		color.add(exprColor);
		//color.add(strokeColor);
		color.add(textColor);
		color.add(labelConditionColor);
//		color.add(labelConditionStroke);
		color.add(labelGeneTextColor);
		color.add(labelConditionTextColor);
		color.add(new RepaintAction());
		
		
		v.putAction("color", color);
		
		t2=System.currentTimeMillis();
		//System.out.println("3) Colors: "+(t2-t1)/1000);

		// -----------------------------layout--------------------------
		VerticalLineLayout2 ylabels=new VerticalLineLayout2((alto-conditionMargin), "geneLabels", ng,m_scale);
		ylabels.setLayoutBounds(rg);
        
        HorizontalLineLayout xlabels = new HorizontalLineLayout((ancho-geneMargin),"conditionLabels", nc,m_scale/2);
        xlabels.setLayoutBounds(rc);

        gl=new MicroGridLayout("matrix",ng,  ngtot, nc, 
        		alto-conditionMargin, ancho-geneMargin, "actualRowId", "colId",geneMargin, conditionMargin, m_scale/2, m_scale, "MicroTal construido "+contName);//sparse
        contName++;
        xlabels.setLayoutAnchor(new Point2D.Double(geneMargin, conditionMargin));//TODO: No podr�an funcionar dos AxisLayout?
		gl.setLayoutBounds(rdata);
		
		ActionList layout = new ActionList();
		layout.add(gl);
		layout.add(xlabels);
		layout.add(ylabels);
		layout.add(new RepaintAction());
		
		v.putAction("layout", layout);
		
		t2=System.currentTimeMillis();
		//System.out.println("4) Layouts: "+(t2-t1)/1000);

		//---------------DISTORTION
		/*ActionList distortion = new ActionList();
		MicroGridDistortion fd=new MicroGridDistortion(1.0/md.getNumConditions(),m_scale/2,1.0/ng,m_scale, rdata,ng,nc, "matrix", this.sesion, gl);
		
		fd.setLayoutBounds(rdata);
		fd.setGroup("matrix");
		distortion.add(fd);
		
		BifocalDistortion2 fdc=new BifocalDistortion2(1.0/nc,m_scale/2,0,0);
		fdc.setLayoutBounds(rdata);
		fdc.setLayoutBounds(rc);
		fdc.setGroup("conditionLabels");
		distortion.add(fdc);
		
		
		
		//BifocalDistortion fdg=new BifocalDistortion(0,0,1.0/ng,m_scale);
		BifocalDistortion2 fdg=new BifocalDistortion2(0,0,1.0/ng,m_scale);
		fdg.setLayoutBounds(rg);
		fdg.setGroup("geneLabels");
		distortion.add(fdg);
		v.putAction("distortion", distortion);
		
		t2=System.currentTimeMillis();
		//System.out.println("5) Distortions: "+(t2-t1)/1000);
		*/
		//------------- display y controladores interactivos--------------
		d = new Display(v);
		d.setHighQuality(true);

		d.addControlListener(new PanControl());  // pan with background left-drag
		d.addControlListener(new ZoomControl()); // zoom with vertical right-drag
		d.addControlListener(new WheelZoomControl()); // zoom to fit screen

		d.addControlListener(new HeatmapHoverControl(sesion, "layout", Visualization.FOCUS_ITEMS, "matrix", "geneLabels", "conditionLabels", gl, ylabels,v));
//		d.addControlListener(new HoverActionControl("color")); // zoom with vertical right-drag
		d.addControlListener(new ConfigurationControl()); // zoom with vertical right-drag
		
	//	AnchorUpdateControl auc=new AnchorUpdateControl(new Layout[]{fd, fdg,fdc},"distortion");//
	//	d.addControlListener(auc);

		currentLevels=new HeatmapFocusControl(sesion, "layout", Visualization.FOCUS_ITEMS, "matrix", "geneLabels", "conditionLabels", gl, ylabels, xlabels, v);
		d.addControlListener(currentLevels);
		t2=System.currentTimeMillis();

		//---------------- frame ----------------------
		this.getWindow().add(d);
		
		this.add(d, BorderLayout.CENTER);	//El display con el grafo
        Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.DARK_GRAY;

        UILib.setColor(this, BACKGROUND, FOREGROUND);
        this.getWindow().setContentPane(this);
		this.getWindow().pack();           // layout components in window
		}
	
	public int getId(){
		return es.usal.bicoverlapper.kernel.Configuration.HEATMAP_ID;
	}

	/**
	 * Run the diagram, making it visible and running prefuse actions
	 *
	 */
	public synchronized void run()
		{
		this.getWindow().setVisible(true);
		
		v.run("colorRectangle");
		v.run("color");  // assign the colors
		v.run("distortion");
		v.run("layout"); // start up the animated layout
		v.runAfter("distortion","layout");
		v.runAfter("layout","color");
		v.runAfter("color", "colorRectangle");
		}
	
	void cancelAllActions()
		{
		if(v!=null)
			{
			v.cancel("colorRectangle");
			v.cancel("color");
			v.cancel("distortion");
			v.cancel("layout");
			}
		if(d!=null)
			{
			d.removeAll();
			d.invalidate();
			this.getWindow().remove(d);
			}
		}
	
	public void update() 
		{
		if(v!=null && sesion!=null)
			{
			//Tenemos un bicluster seleccionado
			if(sesion.getSelectedBicluster()!=null && sesion.getSelectedBicluster()!=bicAnt && sesion.getSelectedGenesBicluster().size()<=md.getNumSparseGenes())
				{
				md.buildSparse(sesion.getSelectedBicluster().getGenes());
				//create(md.getSparseExpressions(),md.getSparseGeneLabels(),md.getConditionLabels());
				update(md.getSparseExpressions(),md.getSparseGeneLabels(),md.getConditionLabels());
				
				currentLevels.clear();
				LinkedList<Integer> lg=sesion.getSelectedGenesBicluster();
				LinkedList<Integer> lc=sesion.getSelectedConditionsBicluster();
				if(lg.size()>0 || lc.size()>0)			currentLevels.addItems(lg, lc);
				}
			}
		synchronized(this){this.repaint();}
		}

	public void updateConfig(){
		paleta[HeatmapDiagram.selectionColor]=sesion.getSelectionColor();
		paleta[HeatmapDiagram.hoverColor]=sesion.getHoverColor();
		
		//Molar�a a�adirle el discretizador a la paleta, no es dif�cil 
		int paletteTemp[]=ColorLib.getInterpolatedPalette(paleta[HeatmapDiagram.lowColor].getRGB(), paleta[HeatmapDiagram.zeroColor].getRGB());
		int paletteTemp2[]=ColorLib.getInterpolatedPalette(paleta[HeatmapDiagram.zeroColor].getRGB(), paleta[HeatmapDiagram.highColor].getRGB());
		palette=new int[paletteTemp.length+paletteTemp2.length];
		for(int i=0;i<paletteTemp.length;i++)	palette[i]=paletteTemp[i];
		for(int i=paletteTemp.length;i<palette.length;i++)	palette[i]=paletteTemp2[i-paletteTemp.length];

		
		exprColor=new ExpressionColorAction("matrix", "level", Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
      	double min=DataLib.min(md.getExpressions(), "level").getDouble("level");
      	double max=DataLib.max(md.getExpressions(), "level").getDouble("level");
      	exprColor.setMaxScale(max);
      	exprColor.setMinScale(min);
    	//exprColor=new LevelColorAction("matrix", "level", palette);
		strokeColor=new StrokeColorAction("matrix", "level", palette, sesion.getHoverColor(), sesion.getSelectionColor());
		
		
		rectangleStrokeColor=new ColorAction("rectangle", VisualItem.STROKECOLOR, sesion.getSelectionColor().getRGB());
		
		ActionList color = (ActionList)v.getAction("color");
		color.remove(exprColor);
		color.remove(strokeColor);
		color.add(exprColor);
		color.add(strokeColor);
		
		
		v.putAction("color", color);

		ActionList colorRectangle= (ActionList)v.getAction("colorRectangle");
		colorRectangle.remove(rectangleStrokeColor);
		colorRectangle.add(rectangleStrokeColor);

		v.putAction("colorRectangle", colorRectangle);
    
        
		run();
		this.repaint();
		this.configurando = false;
	}
	
	
	//--------------------- Private classes--------------
    private static class LevelColorAction extends DataColorAction 
	{
	LevelColorAction(String group, String sel, int[] palette) 
    	{
        super(group, sel, Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
		}
	
	} // end of inner class LevelColorAction

    static class StrokeColorAction extends DataColorAction 
	{
	StrokeColorAction(String group, String sel, int[] palette, Color hc, Color sc) 
    	{
		super(group, sel, Constants.ORDINAL, VisualItem.STROKECOLOR, palette);
        add("_hover", ColorLib.rgb(hc.getRed(),hc.getGreen(),hc.getBlue())); 
        add("ingroup('_focus_')", ColorLib.rgb(sc.getRed(),sc.getGreen(),sc.getBlue()));
		}
	} // end of inner class NodeColorAction
    
    static class LabelRenderer2 extends LabelRenderer
    	{
    	LabelRenderer2(String group)
    		{
    		super(group);
    		setVerticalPadding(0);
    		setHorizontalPadding(0);
    		setHorizontalAlignment(Constants.RIGHT);
    		setHorizontalTextAlignment(Constants.RIGHT);
     		}
    	}

    /**
     * Lines up all VisualItems vertically. Also scales the size such that
     * all items fit within the maximum layout size, and updates the
     * Display to the final computed size.
     */
    class VerticalLineLayout2 extends Layout {
        private double m_maxHeight = 600;
        private String grupo;
        private Double scale;
        private int[] geneOrder;
        private double distortion;
        private double maxDistortion=0.7;
        private int[] hoverGenes;//En principio s�lo uno
        
        VerticalLineLayout2(double maxHeight, String group, int rowNumber, double d) {
            m_maxHeight = maxHeight;
            grupo=group;
            geneOrder=new int[rowNumber];
            distortion=d;
            initialOrder();
        }
       
        public void run(double frac) {
            // first pass --> ponemos las etiquetas al tama�o m�ximo que tendremos
           // double w = 0;
            double h = 0;

            TupleSet ts=v.getGroup(grupo);
     	    Sort orden=new Sort(new String[]{"rowRank"});
     	    Iterator iter=ts.tuples(null,orden);
     	    double h1=0;
     	 
 	    	h1=m_maxHeight/ts.getTupleCount();
 	    	double canonicalh=0;
     	    while ( iter.hasNext() ) {
               VisualItem item = (VisualItem)iter.next();
               item.setSize(1.0);
               canonicalh=item.getBounds().getHeight();
                h += item.getBounds().getHeight();
           }//A un tama�o de 1, vemos cu�nto ocupan en altura
         
           scale = h > m_maxHeight ? m_maxHeight/h : 1.0; //Escalamos seg�n la altura m�xima qu tengamos
     	   //0) SELECTION OR HOVER
           if(sesion.getSelectedBicluster()!=null || (sesion.getHoveredBicluster()!=null && sesion.getHoveredBicluster().getGenes().size()>0))
	    	   {
	           double normalh=h1;
	           int ng=0;
	           int ns=0;
	           int nh=0;
	           int hoverGene=-1;
	          
	           if(sesion.getSelectedBicluster()!=null)	
	        	   ns=sesion.getSelectedBicluster().getGenes().size();
	           if(sesion.getHoveredBicluster()!=null )	
	        	   {
	        	   if(sesion.getHoveredBicluster().getGenes().size()>0) 
	        		  hoverGene=hoverGenes[0];
	        	   nh=sesion.getHoveredBicluster().getGenes().size();
	        	   }
	           if(nh>0 && (ns==0 || hoverGene>ns))  	ng=ns+nh;
	           else								        ng=ns;//hovering coincides with a selection
	           
	           //System.out.println("N�mero de filas a distorsionar: "+ng);
	           
	           double distortedh=normalh*distortion;
	           double distorteds=distortedh/canonicalh;
	
	           double minih=(this.getLayoutBounds().getHeight()-distortedh*ng)/(geneOrder.length-ng);
	           double minis=minih/canonicalh;
	           int cont=0;
	           double height=this.getLayoutBounds().getHeight();
	
			    if(distortedh*ng>maxDistortion*height)
		           	{
		   	    	distortedh=height*maxDistortion/ng;
		   	    	minih=(m_maxHeight-distortedh*ng)/(geneOrder.length-ng);
		   	    	distorteds=distortedh/canonicalh;
		   	    	minis=minih/canonicalh;
		   	        }
	
		        h1=m_maxHeight/ts.getTupleCount();
	            Display d = v.getDisplay(0);
	            Insets ins = d.getInsets();//espacio que el display deja en sus bordes
	           
	            // second pass
	            h = ins.top+conditionMargin;
	            double ih, y=0, x=ins.left+geneMargin-10;
	            iter=ts.tuples(null,orden);
	            cont=0;
	            while ( iter.hasNext() ) 
	           	   {
	               VisualItem item = (VisualItem)iter.next();
	               if((ns>0 && cont<sesion.getSelectedBicluster().getGenes().size()) || (hoverGenes!=null && geneOrder[item.getInt("id")]==hoverGenes[0])) 
		            {
	               	item.setSize(distorteds); 
	               	item.setEndSize(distorteds); 
	               	ih=distortedh;
	               	cont++;
	               	}
	               else
	               	{
	               	item.setSize(minis); 
	               	item.setEndSize(minis);
	               	ih=minih;
	               	}
	               
	               y = h+(ih/2);
	               setX(item, null, x);
	               setY(item, null, y);
	               h += ih;
	           	}
	    	   }
           //3) NORMAL
           else
           		{
	            h1=m_maxHeight/ts.getTupleCount();
	            Display d = v.getDisplay(0);
	            Insets ins = d.getInsets();//espacio que el display deja en sus bordes
	            
	            // second pass
	           h = ins.top+conditionMargin;
	           double ih, y=0, x=ins.left+geneMargin-10;
	           iter=ts.tuples(null,orden);
	
	            while ( iter.hasNext() ) 
	            	{
	                VisualItem item = (VisualItem)iter.next();
	                item.setSize(scale); item.setEndSize(scale);
	                
	                Rectangle2D b = item.getBounds();
	               // w = Math.max(w, b.getWidth());
	                ih = b.getHeight(); //Escalamos tambi�n los bordes, adem�s del tama�o del texto
	
	                y = h+(ih/2);
	                setX(item, null, x);
	                setY(item, null, y);
	                h += h1;
	            	}
	     	    }
          }
        
        void newOrder(int[] genesFirst)
	    	{
	    	/*int init=0;
	    	for(int i=0;i<genesFirst.length;i++)
	    		{
	    		geneOrder[init]=genesFirst[i];//El primero pasa a ocupar la posici�n de uno de los que tenemos
	    		geneOrder[genesFirst[i]]=init;//y viceversa
	    		init++;
	    		}
	    	init=0;*/
        	int init=0;
        	if(genesFirst!=null)
        	{
        	for(int i=0;i<genesFirst.length;i++)//Primero a�ado todos los que tienen que ir primero
        		{
        		geneOrder[genesFirst[i]]=init;//El primero pasa a ocupar la posici�n de uno de los que tenemos
        		init++;
        		}
        	for(int i=0;i<geneOrder.length;i++)//Luego el resto, si no est�n entre los iniciales
        		{
        		boolean add=true;
        		for(int j=0;j<genesFirst.length;j++)	
        			if(i==genesFirst[j])	
        				{add=false;break;}
        		if(add)	
        			geneOrder[i]=init++;
        		}
        	}
        	return;
	    }
        
        void newHover(int[] genes)
	    	{
	    	hoverGenes=genes;
	    	if(genes!=null)	
	    		for(int i=0;i<hoverGenes.length;i++)	
	    		//hoverGenes[i]=geneOrder[genes[i]];
	    			hoverGenes[i]=geneOrder[hoverGenes[i]];
	    	return;
	    	}
        
        void initialOrder()
	    	{
	    	for(int i=0;i<geneOrder.length;i++)	geneOrder[i]=i;
	    	}
    
    
		Double getScale() {
			return scale;
		}

		void setScale(Double scale) {
			this.scale = scale;
		}
    } // end of inner class VerticalLineLayout2

    class ConfigurationControl extends ControlAdapter{
    	ConfigurationControl()
    		{
    		super();
    		}
    	public void mouseReleased(MouseEvent e)
    		{
    	//	System.out.println("Configuracion");
    		if(e.getButton() == MouseEvent.BUTTON3)
    			{
    			configure();
    			}
    		}
    	}
    /**
     * Lines up all VisualItems horizontally. Also scales the size such that
     * all items fit within the maximum layout size, and updates the
     * Display to the final computed size.
     */
    class HorizontalLineLayout extends Layout {
        private double m_maxWidth = 100;
        private String grupo;
        private Double scale;
        private int[] condOrder;
        private double distortion;
        private double maxDistortion=0.7;
     
        
        HorizontalLineLayout(double maxWidth, String group, int colNumber, double d) {
//        	System.out.println("Anchura m�xima "+m_maxWidth);
            m_maxWidth = maxWidth;
            grupo=group;
            condOrder=new int[colNumber];
            distortion=d;
            initialOrder();
        }
       
        public double getSeparation(VisualItem item)
        	{
        	double h=((RotationLabelRenderer)item.getRenderer()).getTextHeight(item);
        	double r=((RotationLabelRenderer)item.getRenderer()).itemRotation;
        	
        	double rc=r-90;
        	rc*=Math.PI/180;
        	double sep=h*Math.abs(Math.cos(rc));
        	return sep;
 	         
        	}
        
        public void run(double frac) {
            double w = 0;
            double w1=(ancho-geneMargin)/condOrder.length;
   	      
            TupleSet ts=v.getGroup(grupo);
        	Sort orden=new Sort(new String[]{"colRank"});
     	    Iterator iter=ts.tuples(null,orden);
     		double canonicalw=0;
         	
     	    while ( iter.hasNext() ) 
     	    	{
               VisualItem item = (VisualItem)iter.next();
               item.setSize(1.0);
               w+=getSeparation(item);
               
	           canonicalw=((RotationLabelRenderer)item.getRenderer()).getTextHeight(item);
     	    	}//A un tama�o de 1, vemos cu�nto ocupan en altura
         
           scale = w > m_maxWidth ? m_maxWidth/w : 1.0; //Escalamos seg�n la altura m�xima qu tengamos
           Display d = v.getDisplay(0);
           Insets ins = d.getInsets();//espacio que el display deja en sus bordes
           
           // second pass
           w=geneMargin;
          double iw;
          double x=0;
          double y=ins.top+conditionMargin-10;
          iter=ts.tuples(null,orden);
          
        //0) SELECTION OR HOVER
          if(sesion.getSelectedBicluster()!=null ||  (sesion.getHoveredBicluster()!=null && sesion.getHoveredBicluster().getConditions().size()>0 && sesion.getHoveredBicluster().getConditions().size()<condOrder.length))
     		    {
        	    int nc=0;
	            int ns=0;
	            int nh=0;
	            int hoverCondition=-1;
	            if(sesion.getSelectedBicluster()!=null)	
	            	ns=sesion.getSelectedBicluster().getConditions().size();
	            if(sesion.getHoveredBicluster()!=null && sesion.getHoveredBicluster().getConditions().size()>0 && sesion.getHoveredBicluster().getConditions().size()<condOrder.length)	
	        	   {
	        	   //hoverCondition=sesion.getHoveredBicluster().getConditions().get(0);
	            	hoverCondition=condOrder[sesion.getHoveredBicluster().getConditions().get(0)];
		        	   nh=sesion.getHoveredBicluster().getConditions().size();
	        	   }
	            if(nh>0 && nh<condOrder.length && (ns==0 || hoverCondition>ns))  	
	            	nc=ns+nh;
		        else								        
		        	nc=ns;//hovering coincides with a selection
		         
	 	    	double normalw=w1;
 	            double normals=normalw/canonicalw;
 	            double width=this.getLayoutBounds().getWidth();
 	            double distortedw=normalw*distortion;
 	            double distorteds=distortedw/canonicalw;

 	            double miniw=(width-distortedw*nc)/(condOrder.length-nc);
 	            double minis=miniw/canonicalw;
 	            int cont=0;

      		    if(distortedw*nc>maxDistortion*width)
 	            	{
 	    	    	distortedw=width*maxDistortion/nc;
 	    	    	miniw=(m_maxWidth-distortedw*nc)/(condOrder.length-nc);
 	    	    	distorteds=distortedw/canonicalw;
 	    	    	minis=miniw/canonicalw;
 	    	        }

     	    	
                while ( iter.hasNext() ) 
	            	{
                    VisualItem item = (VisualItem)iter.next();
               
	                if(nc==condOrder.length || nc==0)
     	                {
	                	item.setSize(normals); 
	                	item.setEndSize(normals); 
	                	iw=normalw;
	                	}
	                else
		                {
		              //  if((ns>0 && cont<ns) || item.getInt("id")==hoverCondition) 
	                	if((ns>0 && cont<ns) || condOrder[item.getInt("id")]==hoverCondition) 
	   		                {
		                	item.setSize(distorteds); 
		                	item.setEndSize(distorteds); 
		                	iw=distortedw;
		                	cont++;
		                	}
		                else
		                	{
		                	item.setSize(minis); 
		                	item.setEndSize(minis);
		                	iw=miniw;
		                	}
		                }
	                
   	                x = w;
	                setX(item, null, x+item.getBounds().getWidth());
	  	            setY(item, null, y);
	  	            if(item.getString("name").equals("E1A"))
	                	System.out.println("Posicionando en "+(x+item.getBounds().getWidth())+" con iw "+iw);
	                w += iw;
	            	}
     	    	}
          
          //3) NORMAL
          else
          	{
        	while ( iter.hasNext() ) 
	           {
	           VisualItem item = (VisualItem)iter.next();
               item.setSize(scale); item.setEndSize(scale);
               iw = (ancho-geneMargin)/(double)condOrder.length; //Escalamos tambi�n los bordes, adem�s del tama�o del texto
               x=w;
       
               setX(item, null, x+item.getBounds().getWidth());
               setY(item, null, y);
               w += iw;
               }
    	    }
	    }
        
      void newOrder(int[] genesFirst)
	    	{
	    	int init=0;
	    	for(int i=0;i<genesFirst.length;i++)
	    		{
	    		condOrder[init]=genesFirst[i];//El primero pasa a ocupar la posici�n de uno de los que tenemos
	    		condOrder[genesFirst[i]]=init;//y viceversa
	    		init++;
	    		}
	    	init=0;
	    	}
        void initialOrder()
	    	{
	    	for(int i=0;i<condOrder.length;i++)	condOrder[i]=i;
	    	}
    
    
		Double getScale() {
			return scale;
		}

		void setScale(Double scale) {
			this.scale = scale;
		}
    } // end of inner class HorizontalLineLayout


    
   
   /**
    * Returns the expression level palette used
    * @return the expression level palette
    */
    /*public  int[] getColors() {
		return palette;
	}*/

    /**
     * Sets the expression level palette. About a hundred colors are enough for a nice palette
     * @param palette	the colors of the palette
     */
	void setPalette(int[] palette) {
		this.palette = palette;
	}
	
	
	/**
	 * Pops up a configuration panel for parallel coordinates properties
	 * TODO: Still in development
	 */
	public void configure(){
		if(!configurando){
			configurando = true;
			JInternalFrame ventanaConfig = this.getVentanaConfig();
			
			// Obtenemos el gestor de eventos de configuracion
			
			ConfigurationMenuManager gestor = new ConfigurationMenuManager(this,ventanaConfig,paleta,muestraColor);
			
			JPanel panelColor = this.getPanelPaleta(paleta, textoLabel, muestraColor);
			//JPanel panelAnclajes = this.getPanelAnclajes(sesion, gestor);
			JPanel panelParametros = this.getPanelParametros();
			JPanel panelBotones = this.getPanelBotones(gestor);
			
			// Configuramos la ventana de configuracion
			
			this.initPanelConfig(panelColor, null, panelParametros, panelBotones);
							
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
		
		sesion.setSelectionColor(paleta[HeatmapDiagram.selectionColor]);
		sesion.setHoverColor(paleta[HeatmapDiagram.hoverColor]);

		//Molar�a a�adirle el discretizador a la paleta, no es dif�cil 
		int paletteTemp[]=ColorLib.getInterpolatedPalette(paleta[HeatmapDiagram.lowColor].getRGB(), paleta[HeatmapDiagram.zeroColor].getRGB());
		int paletteTemp2[]=ColorLib.getInterpolatedPalette(paleta[HeatmapDiagram.zeroColor].getRGB(), paleta[HeatmapDiagram.highColor].getRGB());

		palette=new int[paletteTemp.length+paletteTemp2.length];
		for(int i=0;i<paletteTemp.length;i++)	palette[i]=paletteTemp[i];
		for(int i=paletteTemp.length;i<palette.length;i++)	palette[i]=paletteTemp2[i-paletteTemp.length];

		
		exprColor=new ExpressionColorAction("matrix", "level", Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
      	double min=DataLib.min(md.getExpressions(), "level").getDouble("level");
      	double max=DataLib.max(md.getExpressions(), "level").getDouble("level");
      	exprColor.setMaxScale(max);
      	exprColor.setMinScale(min);
      	//exprColor=new LevelColorAction("matrix", "level", palette);
      	strokeColor=new StrokeColorAction("matrix", "level", palette, sesion.getHoverColor(), sesion.getSelectionColor());
		
		
		rectangleStrokeColor=new ColorAction("rectangle", VisualItem.STROKECOLOR, sesion.getSelectionColor().getRGB());
		
		ActionList color = (ActionList)v.getAction("color");
		color.remove(exprColor);
		color.remove(strokeColor);
		color.add(exprColor);
		color.add(strokeColor);
		
		
		v.putAction("color", color);

		ActionList colorRectangle= (ActionList)v.getAction("colorRectangle");
		colorRectangle.remove(rectangleStrokeColor);
		colorRectangle.add(rectangleStrokeColor);

		v.putAction("colorRectangle", colorRectangle);

		this.run();
		//this.repaint();
		sesion.updateConfigExcept(this.getName());
		this.configurando = false;
	}

   }
