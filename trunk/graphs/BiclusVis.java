package graphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JTextField;

import data.MicroarrayData;

import kernel.BiclusterSelection;

import prefuse.data.Table;

import utils.ArrayUtils;
import utils.CustomColor;
import utils.GraphPoint2D;

/**
 * This class visualizes biclusters as overlapping hulls on a force-directed graph layout. 
 * It manages force-directed layout, graph displaying and parameter changes.
 * @author Roberto Ther�n & Rodrigo Santamar�a
 *
 */
public class BiclusVis extends JProcessingPanel {

	private static final long serialVersionUID = 1L;

//Main variables
/**
 * Screen height of the drawing area in pixels
 */
int screenHeight = 700;//1500
int screenWidth = 1000;//2000
int nNodes;
private int nodeSize = 17;
private int labelSize = 5;
private int labelClusterSize=labelSize*3;
private double edgeLength = 80;
double stepEdgeLength = 2;
double G = 10; //Gravity (clusters de 200+ nodos)
double stepG = 0.5;
double D = 2.0; //Depth of the well
double K = D/100000;//Para pelis parece que este funciona
double stepK = 0.00005;
private double stiffness = 0.001;//Clusters >200 nodos
double stepStiffness = 0.0005;
private double closeness = nodeSize; 

//Data file information
private String dataFile;
//private String titleFile=null;//Names for each clusters, just if necesary.
String delimiter=" ";

MicroarrayData microarrayData=null;

//Structures for graph information
Map<String,ArrayList<String>> clusters;
ArrayList<Integer> resultSets;
ArrayList<String> resultLabels;
String [] titles=null;
String [] movies=null;
int numClusters;
int numResultSets;
boolean sameCluster[][];
int maximumCast=1;
int numCentroids=1;
Hashtable paths=null;
Table nodeMovie=null;
List ids=null; //lista de centroides de diego
CustomColor lastColor=null;

ArrayList<String> geneNames=null;
ArrayList<String> conditionNames=null;

long lastClicked=0;//Para tempori

//handles data
Handle[] handles;
private int handleLength = 50;
int handleSize = 10;
int handleNum = 4;
int handleFrameY = 30;
int handleFrameX = 120; 
int handleBoxXSize = handleFrameX+handleLength;
int handleBoxYSize = handleFrameY+(handleSize*2*handleNum);

//To optimize time performance
GraphPoint2D nullVector=new GraphPoint2D(0,0);


//Overview Data
float overviewBoxLength;
float overviewBoxHeight;
float xTopOverviewBox;
float yTopOverviewBox;
float magnifierLength;
float magnifierHeight;
float xTopMagnifier;
float yTopMagnifier;
float factor = 0.1f;
float areaInc = 2.0f;//Este hay que ir multiplic�ndolo y dividir por lo mismo que se multiplique esto el factor
int maxArea=16;
float totalWidth=areaInc*screenWidth;
float totalHeight=areaInc*screenHeight;
float xTopDrawingRect;
float yTopDrawingRect;

boolean movingMagnifier = false;
//To change real coordinates
float offsetX = 0;
float offsetY = 0; 

boolean drawNodes=true;
float scapeForce=100;


//Functions Box Data
float functionsBoxLength;
float functionsBoxHeight;
float xTopFunctionsBox;
float yTopFunctionsBox;

Graph g=null;
int refreshTime=0;	//controls draw timing
int maxRefresh=10000;	//draw only after 100 computations of doLayout()
private boolean radial;


// toggle variable
float scaleFactor = (float)1;
float zoomFactor = (float)1;
private boolean showEdges = false;
private boolean useCurves = true;
boolean pauseSimulation = false;
boolean paused=false;
boolean useSpring = true;
private boolean showLabel = false;
private boolean absoluteLabelSize = false; //if TRUE, all labels have the same size despite of the name of biclusters its node is in
boolean showOverview = false;
boolean drawingOverview=false;
boolean showResume=true;
boolean showUnconnected=false;
boolean initialOrdering=false;
int connectionThreshold=0;	//Shows only clusters with at least n nodes overlapped
int nodeThreshold=1;		//Shows only nodes with at least n clusters related
boolean additionMode=false;
boolean drawAwarded=true;		//Se le puede hacer un toggle
boolean drawGlyphs=true;		//Se le puede hacer un toggle
boolean fullDrawing=false;		//Para imprimir la imagen entera sin ce�irnos a los m�rgenes de la ventana (para fotos)
int []	priorities=new int[]{Graph.EDGE, Graph.HULL, Graph.HULLLABEL, Graph.PIECHART, Graph.NODE, Graph.NODELABEL, Graph.HOVER, Graph.SEARCH, Graph.SELECT};
int []	prioritiesOverview=new int[]{Graph.HULL, Graph.NODE};

//ArrayList <CustomColor> colorResults=null;

ArrayList <MaximalCluster> clustersToRemove=null;
ArrayList <Node> nodesToRemove=null;
ArrayList <Edge> edgesToRemove=null;

boolean drawArc=false;
boolean drawHull=true;
boolean drawTitle=false;
boolean showCentroids=false;
boolean onlyIntersecting=true;
boolean movie=true;
boolean movieButActors=false;
boolean sizeRelevant=true;

boolean onlyConditions=false;
boolean onlyGenes=false;
	
GraphPoint2D vf=new GraphPoint2D();//Vector para la aplicaci�n de fuerzas en el layout

float memory; //Cantidad de memoria que nos queda

Iterator it;

//configuracion de color

//configuracion de color
static final int selectionColor=0;
static final int searchColor=1;
static final int hoverColor=2;
static final int bicColor1=3;
static final int bicColor2=4;
static final int bicColor3=5;
static final int geneLabelColor=6;
static final int conditionLabelColor=7;
static final int bicLabelColor=8;
static final int backgroundColor=9;

java.awt.Color[] paleta = new Color[10];
//public String[] textoLabel = {"Selection", "Oscar","Male", "Female", "Background", "Foreground", "Title"};
JTextField[] muestraColor = new JTextField[paleta.length];

//private boolean showLegend;

boolean repaintingAll=false;
int drawCount=0;

/**
 * Default constructor
 * 
 */ 
public BiclusVis()
{
delimiter=" ";

radial=false;
pauseSimulation=false;
initialOrdering=false;
onlyConditions=false;
onlyGenes=false;
drawNodes=true;
showResume=false;
showOverview=true;
sizeRelevant=false;


maxRefresh=1;
connectionThreshold=0;

this.movie=false;
this.movieButActors=false;
}
/**
 * Sets width and height of graph display and all related geometrical properties
 * @param w	width of the graph display
 * @param h	height of the graph display
 */
public void setup(int w, int h)//, Color hc, Color selc, Color sc)
	{
	screenWidth=w;
	screenHeight=h;
	
	setup();
	}

/**
 * Sets initial width and height to default values and sets up all related geometrical properties
 */
public void setup() {
	this.setSize(screenWidth, screenHeight);
	
	 //Overview
  overviewBoxLength = screenWidth * factor * areaInc;
  overviewBoxHeight = screenHeight * factor * areaInc;
  xTopOverviewBox = screenWidth - overviewBoxLength - 2;
  yTopOverviewBox = 0;
  magnifierLength = screenWidth * factor;
  magnifierHeight = screenHeight * factor;
  xTopMagnifier=xTopOverviewBox+(overviewBoxLength/areaInc)/2;
  yTopMagnifier = yTopOverviewBox+(overviewBoxHeight/areaInc)/2;
  offsetX = (xTopOverviewBox - xTopMagnifier) * (1 / factor); 
  offsetY = (yTopOverviewBox - yTopMagnifier) * (1 / factor); 
  
  // Functions Box
  functionsBoxLength = screenWidth / 2;
  functionsBoxHeight = screenHeight / 2;
  xTopFunctionsBox = screenWidth - functionsBoxLength - 2;
  yTopFunctionsBox = screenHeight - functionsBoxHeight;

  totalWidth=areaInc*screenWidth;
  totalHeight=areaInc*screenHeight;

  
  System.out.println("La caja en "+xTopOverviewBox+" siendo el tama�o "+screenWidth);
  
  xTopDrawingRect=((xTopMagnifier-xTopOverviewBox)/(overviewBoxLength))*areaInc*screenWidth;
  yTopDrawingRect=(yTopMagnifier/overviewBoxHeight)*areaInc*screenHeight;
  System.out.println("x e y de pintar "+xTopDrawingRect+", "+yTopDrawingRect+" xOrig: "+(xTopMagnifier-xTopOverviewBox)+" areaInc "+areaInc+" screen");
  
	  
 //Handles
  handles = new Handle[handleNum];
  for(int i=0; i<handleNum; i++) 
    handles[i] = new Handle(handleSize*2, (handleSize*2)*i +handleSize, 0 , handleSize, handles, this);
  
  handles[0].setLabel("Repulsion");
  handles[0].setMinValue(G);
  handles[0].setMaxValue(G+(handleLength*stepG));
  handles[0].setCurrentValue(G);
  handles[0].moveHandle(G);
  
  handles[1].setLabel("Cluster Size");
  handles[1].setMinValue(stiffness);
  handles[1].setMaxValue(stiffness+(handleLength*stepStiffness));
  handles[1].setCurrentValue(stiffness);
  handles[1].moveHandle(stiffness);
  
  handles[2].setLabel("Edge Size");
  handles[2].setMinValue(edgeLength);
  handles[2].setMaxValue(edgeLength+(handleLength*stepEdgeLength));
  handles[2].setCurrentValue(edgeLength);
  handles[2].moveHandle(edgeLength);
  
  handles[3].setLabel("Expansion");
  handles[3].setMinValue(K);
  handles[3].setMaxValue(K+(handleLength*stepK));
  handles[3].setCurrentValue(K);
  handles[3].moveHandle(K);

  fontA = new Font("Arial", Font.BOLD, 10);
  textAlign(CENTER); //El alineamiento de texto no se puede hacer directamente con JPanel, ni especificar la fuente
	}

//TODO: Desactivar en caso de usar en solitario
/**
 * Reconfigure dimensions of the visualization to w and h in case of resizing of the screen
 * @param w	new width of the graph display
 * @param h	new height of the graph display
 * 
 */
public void resize(int w, int h)
	{
	//int want=screenWidth;
	//int hant=screenHeight;
	if(this.getWidth()>0)
		{
		screenWidth=this.getWidth();
		screenHeight=this.getHeight();
		}
	else
		{
		screenWidth=w;
		screenHeight=h;
		}
	//size(screenWidth,screenHeight);//S�lo en caso de usarlo desde el panel, el P3D parece dar problemas
	//size(screenWidth,screenHeight,P3D);//TODO: S�lo en caso de usarlo desde el panel, el P3D parece dar problemas
	//size(screenWidth,screenHeight,OPENGL);//S�lo en caso de usarlo desde el panel, el P3D parece dar problemas
	 //Overview
	float xob=xTopOverviewBox;
	  overviewBoxLength = screenWidth * factor * areaInc;
	  overviewBoxHeight = screenHeight * factor * areaInc;
	  xTopOverviewBox = screenWidth - overviewBoxLength - 2;
	  yTopOverviewBox = 0;
	  magnifierLength = screenWidth * factor;
	  magnifierHeight = screenHeight * factor;

	  xTopMagnifier=xTopOverviewBox+xTopMagnifier-xob;

	//  yTopMagnifier = yTopOverviewBox+(h-hant);
	  //yTopMagnifier*=(overviewBoxHeight/obh);
	  //TODO: Hay que afinar m�s d�nde quedan xTop e yTopMagn por el incremento en tama�o que tienen overviewBox
	//	offsetX = (xTopOverviewBox - xTopMagnifier) * (1 / factor); 
	//	offsetY = (yTopOverviewBox - yTopMagnifier) * (1 / factor); 
	  
		offsetX = (xTopOverviewBox - xTopMagnifier) * (zoomFactor / factor); 
		offsetY = (yTopOverviewBox - yTopMagnifier) * (zoomFactor / factor); 

	  // Functions Box
	  functionsBoxLength = screenWidth / 2;
	  functionsBoxHeight = screenHeight / 2;
	  xTopFunctionsBox = screenWidth - functionsBoxLength - 2;
	  yTopFunctionsBox = screenHeight - functionsBoxHeight;

	  totalWidth=areaInc*screenWidth;
	  totalHeight=areaInc*screenHeight;
	}
	
/**
 * Drawing method for BiclusVis
 * @param g Graphics in which the visualization is to be drawn
 */
public void paintComponent(Graphics g)
	{
	gfinal=(Graphics2D)g;
	if (g != null && gfinal!=null) 
		{
		//Invalidamos la imagen antigua
		if(backBuffer != null)
			{
			backBuffer.flush();
			backBuffer = null;
			}
		// Invalidamos el contexto gr�fico antiguo		
		if(gr != null)
			{
			gr.dispose();
			gr = null;
			}	  
	  
		// Creamos una nueva imagen con el tama�o apropiado
		if (backBuffer == null && this.getWidth()>0 && this.getHeight()>0) 
			{
			backBuffer = createImage(this.getWidth(), this.getHeight());
			if(backBuffer!=null )	gr = ((Graphics2D)backBuffer.getGraphics());
			}

		if(gr!=null && backBuffer!=null)
			{
			gr.setFont(fontA);
			//Establecemos las opciones de rendering y antialiasing de la gr�fica
		    RenderingHints qualityHints = new RenderingHints(null);
		    qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		    qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		    gr.setRenderingHints(qualityHints);
		    
		    Color bg = this.getBackground();
		    Color fg = this.getForeground();
		    gr.setColor(bg);
            gr.fillRect(0, 0, this.getWidth(), this.getHeight());
            gr.setColor(fg);
			
           
		if (!pauseSimulation)		  
		  doLayout();
		   
	    refreshTime=0;

   	    this.setBackground(paleta[this.backgroundColor]);
	    
	    G = handles[0].currentValue;
	    stiffness = handles[1].currentValue;
	    edgeLength = handles[2].currentValue; 
        K = handles[3].currentValue;
      
        pushMatrix(); 
        translate(offsetX,offsetY);
        scale(zoomFactor);	//TODO: probando todav�a
        this.g.draw(priorities);
  	    popMatrix();
       
       if(showOverview)	  	    drawOverview();
	   //if(showResume)			drawResume();
	   //if(showLegend)		    drawLegend();
 	  
	   if(img != null)
	  	{
		img.flush();
		img = null;
	  	}

	     // Almaceno la imagen actual como una imagen estable sobre la que hacer los peque�os cambios (s�lo las l�neas est�n en dicha imagen)
		img  = createImage(backBuffer.getSource());
		   // Intercambio la imagen (rendering de doble buffer)
	    gfinal.drawImage(backBuffer,0,0,this);
		}
	}
	return;
}

void insertDetails()
	{
	Iterator itRel=g.getNodes().values().iterator();
	float maxRel=0;
	float minRel=1000000000;
	
	while(itRel.hasNext())
		{
		Node n=(Node)itRel.next();
		if(n.clusters.size()>maxRel)	maxRel=n.clusters.size();
		else if(n.clusters.size()<minRel)	minRel=n.clusters.size();
		}
	float den=maxRel-minRel;
	if(den==0)	den=1;
	itRel=g.getNodes().values().iterator();
	while(itRel.hasNext())
		{
		Node n=(Node)itRel.next();
		n.setRelevance((n.clusters.size()-minRel)/(den));
		if(microarrayData!=null)
			{
			if(conditionNames.contains(n.getLabel()))	
				{
				n.setGene(false);
				n.id=microarrayData.getConditionId(n.label);
				}
			else										
				{
				n.setGene(true);
				n.id=microarrayData.getGeneId(n.label);
				}
			}
		
		}

	}

/**
 * Updates this graph by showing only the biclusters that contains any of the 
 * genes or conditions selected.
 * @param	bs	BiclusterSelection to update the graph
 *
 */
public void updateGraph(BiclusterSelection bs)
	{
	this.restoreClusters();
	this.removeNonSelected(bs);
	}
/**
 * Draws a small interface with bars to change force parameters
 * @deprecated
 */
void drawHandles()
{ 
  fill(153);
  rect(0, 0, handleBoxXSize, handleBoxYSize);
	  
  for(int i=0; i<handleNum; i++) {
    handles[i].update();
    handles[i].display();
  }
}


/**
 * Draws an overall view of the complete graph  at the top-right side of the visualization
 *
 */
void drawOverview()
{  
	this.rectMode(JProcessingPanel.CORNER);
	drawingOverview=true;//To tell if all the graph must be printed or only what is in screen
	fill(0);
	noStroke();
	rect(screenWidth-overviewBoxLength,0,overviewBoxLength, overviewBoxHeight);//To avoid main graph painting in overview
	
  pushMatrix(); 
  translate(screenWidth - overviewBoxLength,0);
  scale(zoomFactor);
  scale(factor);	
  
  boolean labels=this.isShowLabel();
  setShowLabel(false);
  boolean arcs=this.isDrawPiecharts();
  setDrawPiecharts(false);
  boolean titles=this.isDrawClusterLabels();
  setDrawClusterLabels(false);
  
  g.draw();
  
  setShowLabel(labels);
  setDrawPiecharts(arcs);
  setDrawClusterLabels(titles);
  
  popMatrix();
  
  stroke(255);
  noFill();
  rect(xTopOverviewBox, yTopOverviewBox, overviewBoxLength, overviewBoxHeight);
  
  if (movingMagnifier)
  	{
	  fill(200,200,200,90); 
	  strokeWeight(3);
	  stroke(0,150,150);
  	}
  else
  	{
	  strokeWeight(1);
      noFill();
	  stroke(200,150,0);
  	}
  rect(xTopMagnifier, yTopMagnifier, magnifierLength, magnifierHeight);
  strokeWeight(1);
  drawingOverview=false;
}

/**
 * Determines if the mouse is over the rect specified
 * @param x	x position of the top-left square of the rect
 * @param y	y position of the top-left square of the rect
 * @param width	width of the rect
 * @param height	height of the rect
 * @return	true if the mouse is overt the rect specified, false otherwise
 */
boolean overRect(int x, int y, int width, int height) 
	{
	  if (mouseX >= x && mouseX <= x+width && 
	      mouseY >= y && mouseY <= y+height) 
	    return true;
	  else 
	    return false;
	}

protected int lock(int val, int minv, int maxv) 
	{ 
    return  (int)min((double)max(val, minv), (double)maxv); 
	}


/**
 * Computes the current layout of the graph depending on the position of nodes, following
 * a force directed layout in which:
 * 	1) All nodes connected are attracted by a Spring force
 * 	2) Every node is repulsed by every other node by a Gravitational force
 */
public synchronized void doLayout() 
	{
	//----------------------- spring forces -------------------------
	//calculate spring forces on each node
	if(radial)
	  	{
		//Fuerzas entre nodos
		it=g.getNodes().values().iterator();
			
		while(it.hasNext())
		  	{
			ForcedNode n=(ForcedNode)it.next();
			n.setForce(nullVector);
			
			TreeMap<String,Edge> edges = g.getEdgesFrom(n);	//Ojo, hace un new de TreeMap, pero s�lo la primera vez
			Iterator ie=edges.values().iterator();
		    while(ie.hasNext())
		    	{
		    	SpringEdge e=(SpringEdge)ie.next();
		    	GraphPoint2D f = e.getForceFrom();
		    	n.applyForce(f);
		    	}
		    
		    edges = g.getEdgesTo(n);
			ie=edges.values().iterator();
			while(ie.hasNext())
		    	{
		    	SpringEdge e=(SpringEdge)ie.next();
		    	GraphPoint2D f = e.getForceTo();
			   	n.applyForce(f);
		    	}
		   }
			
		//Para los nodos centrales
		it=g.getCenterNodes().values().iterator();
		while(it.hasNext())
		   	{
			//S�lo tenemos que coger fuerzas from de los centrales
		    ForcedNode n = (ForcedNode)it.next();
		    n.setForce(nullVector);
		   
			TreeMap<String,Edge> edges = g.getEdgesFrom(n);
			Iterator ie=edges.values().iterator();
		    while(ie.hasNext())
		    	{
		    	SpringEdge e=(SpringEdge)ie.next();
		    	GraphPoint2D f = e.getForceFrom();
		    	n.applyForce(f);
		    	}
		    }
	  	}//del if radial
	else
		{
//		Fuerzas entre nodos
		it=g.getNodes().values().iterator();
		while(it.hasNext())	{((ForcedNode)it.next()).setForce(nullVector);}
		it=g.getNodes().values().iterator();
		while(it.hasNext())
		  	{
			ForcedNode n=(ForcedNode)it.next();
			 
			TreeMap<String,Edge> edges = g.getEdgesFrom(n);
			//Otra manera, siempre que hacemos el from, aplicamos a los to,
			//de modo que no hay que hacer el to para ninguno
			Iterator ie=edges.values().iterator();
		    while(ie.hasNext())
		    	{
		    	SpringEdge e=(SpringEdge)ie.next();
		    	GraphPoint2D f = e.getForceFrom();
		    	n.applyForce(f);
		    	ForcedNode m=(ForcedNode)e.getTo();
		    	f.invert();
		    	m.applyForce(f);
		    	}
		  
		   }
		}
	  
	  //--------------------- expansion forces ----------------------
	  //calculate the anti-gravitational forces on each node
	  //this is the N^2 shittiness that needs to be optimized
	  if(!radial)
	  	{
		  
		it=g.getNodes().values().iterator();
		//  System.out.println("Nodos: "+g.getNodes().size());
		for (int i=0; i<g.getNodes().size()-1; i++) //N(N-1)/2 complexity
	  		{
			//System.out.print(i+" ");
			ForcedNode a = (ForcedNode)it.next();
			{
		
			Iterator it2=g.getNodes().values().iterator();
			for(int j=0;j<=i;j++)	it2.next();	//El mejor modo que he encontrado de aplicar el n(n-1)/2
			for (int j=i+1; j<g.getNodes().size() && it2.hasNext(); j++) 
				{
				ForcedNode b = (ForcedNode)it2.next();
				double dx = b.getX() - a.getX();
			    double dy = b.getY() - a.getY();
			   
			    if(dx!=0)
			    	{ //don't divide by zero.
				    
			    	double r = sqrt((float) (dx*dx + dy*dy));
			        
			    	//if (!a.isInSameCluster(b))
			    	//if(!sameCluster[i][j])	//El resultado es el mismo aunque apliquemos a los de dentro, y reducimos a la mitad el tiempo del layout en modo completo
			    	if(r>0.1)//To avoid extremely high forces
			    	//if(r>(b.getRelevance()+a.getRelevance()))
			       		{
			       	//	double f = -G*(a.getMass()*b.getMass()/(r*r));
			       		double f = -G/(r*r);
				    	//vf = new Vector2D(dx*f, dy*f);
			       		vf.setX(dx*f);
			       		vf.setY(dy*f);
			       		//vf.setX(f);
			       		//vf.setY(f);
			       		a.applyForce(vf); 
				    	vf.invert();
				    	b.applyForce(vf);
			       		}
			    	}
		    	}
			}//if raro
		  	}//Expansion forces
	  	}//Complete case
	  else//Radial case
	  	{
		it=g.getCenterNodes().values().iterator();
		for (int i=0; i<g.getCenterNodes().size()-1; i++) //N(N-1)/2 complexity
			 {
			 ForcedNode a = (ForcedNode)it.next();
			 Iterator it2=g.getCenterNodes().values().iterator();
			 for(int j=0;j<=i;j++)	it2.next();	
			 for (int j=i+1; j<g.getCenterNodes().size(); j++) 
		 	   	{
				ForcedNode b = (ForcedNode)it2.next();
		    	double dx = b.getX() - a.getX();
			    double dy = b.getY() - a.getY();
			    if(dx!=0)
			    	{ //don't divide by zero.
			       	double r = sqrt((float) (dx*dx + dy*dy));
			       	double f = -G*(a.getMass()*b.getMass()/(r*r));//Aqu� si que importan las masas.
			       	vf.setX(dx*f);
			       	vf.setY(dy*f);
			       	a.applyForce(vf);
			    	vf.invert();
			    	b.applyForce(vf);
			       	}
		    	}
		  	}//Expansion forces
	  	}//radial case
	  
	  boolean zoomed=false;
	  //move nodes according to forces
	  //Peripheral nodes
	  it=g.getNodes().values().iterator();
	  for (int i=0; i<g.getNodes().size(); i++) 
	  	{
	    ForcedNode n = (ForcedNode)it.next();
	          
	    if (n != g.getDragNode() && !n.isFixed())	    
	    	{
	    	while(n.getForce().getX()>scapeForce || n.getForce().getY()>scapeForce )	
	    		n.setForce(nullVector);
	    	if(n.getForce().getX()>scapeForce || n.getForce().getY()>scapeForce )	n.fix(true);
	    	n.getPosition().add(n.getForce());
	    	}
		if(!radial)
	    	{
	    	if(areaInc>=maxArea)
			{
			if(n.getX()<100)	n.setX(100);
			if(n.getY()<100)	n.setY(100);
			if(n.getX()>totalWidth-100)		n.setX(totalWidth-100);
			if(n.getY()>totalHeight-100)	n.setY(totalHeight-100);
			}
		
		    if(!zoomed)
		    	{
		    	if(areaInc<maxArea && (n.getPosition().getX()>totalWidth || n.getPosition().getY()>totalHeight ||
		    		n.getPosition().getX()<0 || n.getPosition().getY()<0 )) 
		    		{
		    		increaseOverview(2);
			    	zoomed=true;
			    	}
		    	}
	    	}
	  	}// del for
	  
	  //Center nodes if existing
	  it=g.getCenterNodes().values().iterator();
	  for (int i=0; i<g.getCenterNodes().size(); i++) 
	  	{
	    ForcedNode n = (ForcedNode) it.next();
	    n.getPosition().add(n.getForce());
	  	}
	}

/**
 * Modify the size of the total area of visualization (not the area displayed in the
 * main graph, but the total area displayed in the overview)
 * @param f	Factor of change used to multiply by the actual size. If lesser than 1, the total area is reduced.
 */
void increaseOverview(double f)
	{
	//Cambian las dimensiones totales de pantalla
    areaInc*=f;
    println("Zooming to area "+areaInc);
    this.factor/=f;
    totalHeight*=f;
    totalWidth*=f;
    
    //Centramos
    Iterator itOver=g.getCenterNodes().values().iterator();
    while(itOver.hasNext())
    	{
    	ForcedNode n=(ForcedNode)itOver.next();
    	n.setX((float)(n.getX()+screenWidth*areaInc/(f*2)));
    	n.setY((float)(n.getY()+screenHeight*areaInc/(f*2)));
    	}
    itOver=g.getNodes().values().iterator();
    while(itOver.hasNext())
    	{
    	ForcedNode n=(ForcedNode)itOver.next();
    	n.setX((float)(n.getX()+screenWidth*areaInc/(f*2)));
    	n.setY((float)(n.getY()+screenHeight*areaInc/(f*2)));
    	}
    
	float x=xTopMagnifier-xTopOverviewBox;
	float n=areaInc/(float)f;//Area inicialmente
	float m=areaInc;
	float X=xTopOverviewBox;
	float Y=yTopOverviewBox;
	float y=yTopMagnifier;
	float l=overviewBoxLength;
	float h=overviewBoxHeight;
	
    //Cambia la posici�n y tama�o de la lupa
    xTopMagnifier=X + (x/(m/n)) + (l/m)*(m-n)/2;
    yTopMagnifier=Y+(y/(m/n))+(h/m)*(m-n)/2;
    
    magnifierLength/=f;
    magnifierHeight/=f;
    
	offsetX = (xTopOverviewBox - xTopMagnifier) * (zoomFactor / factor); 
	offsetY = (yTopOverviewBox - yTopMagnifier) * (zoomFactor / factor); 
	}

//----------------------------- INTERACTIVE CONTROLS ---------------------------
/**
 * This function is called when a key is pressed
 */
protected void keyPressed() {
	char c = Character.toLowerCase(key);
	int temp=0;
		
	//System.out.println("key preseed: "+key+" "+c);
	//System.out.println("code preseed: "+keyCode);
	switch(c){
	case  '1'://radial2complete
		if(radial)
			{
			g.radial2complete();
			radial=false;
			}
		break;
	case  '2'://radial2complete
		if(!radial)
			{
			g.complete2radial();
			radial=true;
			}
		break;
	case 't':
		drawTitle=!drawTitle;
		break;
	case '4':
		additionMode=!additionMode;
		println("Addition mode: "+additionMode);
		break;	
	case '5':
		labelSize--;
		println("Labels base size: "+labelSize);
		break;	
	case '6':
		labelSize++;;
		println("Labels base size: "+labelSize);
		break;	
		
	//Change of node labels (relevance)
	case '9':
		//println("Size by box earnings");
		//insertRelevance(1);
		break;
	case '8':
		//println("Size by ratings");
		//insertRelevance(3);
		break;
	case '7':
		//println("Size by connections");
		//insertRelevance(4);
		break;
			
	case  '0'://redraw
		this.paint(gr);
		break;
		
	case 'a':
		drawArc=!drawArc;
		break;
	case 'i':
		onlyIntersecting=!onlyIntersecting;
		break;
	case 'n':
		drawNodes=!drawNodes;
		break;
	case 'h':
		drawHull=!drawHull;
		break;
		
	   case 'e':showEdges=!showEdges;
		         break;
		         
	   case 'd': D += 0.05;
	             break;
	    
	   case 's': if (D >= 0.05) 
		            D-= 0.05;        
	             break;
       
	   case 'c':useCurves=!useCurves;
                 break;
                 
	   case 'p': 
		   	pause();
	             break;
	             
	   case 'm': useSpring=!useSpring;
	             break;
	             
	   case 'l': 
		   showLabel=!showLabel;
	             break;
	   

       case 'w': closeness += 0.5;
                 break;

       case 'q': if (closeness >= 0.5) 
                 closeness-= 0.5;
                 break; 
                 
       case 'x': scaleFactor += 1;
                 nodeSize += 1;
                 break;

       case 'z': if (scaleFactor >= 2){ 
    	           scaleFactor-= 1;
                   nodeSize -= 1;}
                 break;

	   case 'v': showOverview=!showOverview;
       break;
	   case CODED:
	   		switch(keyCode)
	   			{
	   			case UP:
	   				System.out.println("up");
	   				temp=priorities[0];
	   				for(int i=0;i<priorities.length-1;i++)
	   					priorities[i]=priorities[i+1];
	   				priorities[priorities.length-1]=temp;
	   				break;
	   			case DOWN:
	   				System.out.println("down");
	   				temp=priorities[priorities.length-1];
	   				for(int i=priorities.length-1;i>0;i--)
	   					priorities[i]=priorities[i-1];
	   				priorities[0]=temp;
	   				break;
	   			}
	   		break;
	 default:
		break;
  }
}

/**
 * Slightly increases the stiffnes of spring forces 
 *
 */
public void increaseStiffness()
	{
	stiffness += 0.0005;
    handles[1].moveHandle(stiffness);
    System.out.println("Stiffness "+stiffness);
    }

/**
 * Slightly decreases the stiffnes of spring forces 
 *
 */
public void decreaseStiffness()
	{
	if (stiffness >= 0.0005) 
        stiffness-= 0.0005;
    handles[1].moveHandle(stiffness);
    System.out.println("Stiffness "+stiffness);
  }

/**
 * Increases by one unit the overlap threshold. Biclusters with less connections (counting as connection
 * each node overlap with other bicluster) than this threshold will not be drawn
 *
 */
public void increaseOverlapThreshold()
	{
	boolean wasPaused=false;
	if(!pauseSimulation)	pause();
	else			wasPaused=true;
	connectionThreshold++;
	restoreClusters();
	removeNonOverlapped();
	println("Threshold: "+connectionThreshold);
	if(!wasPaused)	pause();
	
	}

/**
 * Decreases by one unit the overlap threshold. Biclusters with less connections (counting as connection
 * each node overlap with other bicluster) than this threshold will not be drawn
 *
 */
public void decreaseOverlapThreshold()
	{
	if(connectionThreshold>0)
		{
		connectionThreshold--;
		restoreClusters();
		removeNonOverlapped();
		}
	println("Threshold: "+connectionThreshold);	
	}

/**
 * Increases by one unit the overlap node threshold. Nodes grouped by less biclusters than this threshold
 * will not be drawn
 */
public void increaseNodeThreshold()
	{
	nodeThreshold++;
	}

/**
 * Decreases by one unit the overlap node threshold. Nodes grouped by less biclusters than this threshold
 * will not be drawn
 */
public void decreaseNodeThreshold()
	{
	if(nodeThreshold>1)	nodeThreshold--;
	}

/**
 * Changes from complete graph to radial graph. Complete graph builds biclusters
 * as maximal subgraphs, while radial graph connects all nodes in the bicluster
 * to a central dummy node
 */
public void complete2radial()
	{
	if(!radial)
		{
		println("Changing to radial model");
		g.complete2radial();
		radial=true;
		}	
	}

/**
 * Changes from radia graph to complete graph. Complete graph builds biclusters
 * as maximal subgraphs, while radial graph connects all nodes in the bicluster
 * to a central dummy node
 */
public void radial2complete()
	{
	if(radial)
		{
		println("Changing to complete model");
		g.radial2complete();
		radial=false;
		}
	}

/**
 * Pauses or resumes BiclusVis computing of doLayout(). 
 *
 */
public void pause()
	{
	if (pauseSimulation == true)
		{
		pauseSimulation = false;
		println("Working ...");
		}
	 else
	 	{
	    pauseSimulation = true;
	    println("*** Pause ***");
	 	}
	}

/**
 * Zooms in the visualization
 * TODO: Still in development
 *
 */
public void zoomIn()
	{
	System.out.println("Incremento overview en un factor "+(zoomFactor+0.1)/zoomFactor);
	increaseZoom(0.1);
	System.out.println("Factor de zooom "+zoomFactor);
	}

/**
 * Zooms out the visualization
 * TODO: Still in development
 *
 */
public void zoomOut()
	{
	System.out.println("Incremento overview en un factor "+(zoomFactor-0.1)/zoomFactor);
	increaseZoom(-0.1);

	System.out.println("Factor de zooom "+zoomFactor);
	}

private void increaseZoom(double f)
	{
	float totalHeightAnt=this.totalHeight;
	float totalWidthAnt=this.totalWidth;
	
   // totalHeight/=f;
	   // totalWidth/=f;
	//----------nuevo
	totalHeight-=f*totalHeight;
	totalWidth-=f*totalWidth;
	zoomFactor+=f;
	//------------
	float marcoAlto=(float)(totalHeight-totalHeightAnt)/2.0f;
    float marcoAncho=(float)(totalWidth-totalWidthAnt)/2.0f;
    System.out.println("marco aumentado a "+marcoAncho+", "+marcoAlto);
	
    //Centramos
    Iterator itOver=g.getCenterNodes().values().iterator();
    itOver=g.getNodes().values().iterator();
    while(itOver.hasNext())
    	{
    	ForcedNode n=(ForcedNode)itOver.next();
    	n.setX((float)n.getX()+marcoAncho);
        n.setY((float)n.getY()+marcoAlto);
        }
    
    offsetX+=marcoAncho;
    offsetY+=marcoAlto;
	}

void increaseEdgeLength()
{
edgeLength += 1;
handles[2].moveHandle(edgeLength, false);	
}
void decreaseEdgeLength()
{
edgeLength -= 1;
handles[2].moveHandle(edgeLength, false);	
}

/**
 * Increases G constant for gravitational force by 0.5 
 *
 */
public void increaseG()
	{
	G += 0.5;
	handles[0].moveHandle(G, false);	
    System.out.println("G "+G);
	}
/**
 * Decreases G constant for gravitational force by 0.5 
 *
 */
public void decreaseG()
	{
	if (G >= 0.5) G -= 0.5;
	handles[0].moveHandle(G, false);	
    System.out.println("G "+G);
	}

/**
 * Increases label size of nodes by one point
 *
 */
public void increaseLabelSize()
	{
	labelSize++;
	}
/**
 * Decreases label size of nodes by one point
 *
 */
public void decreaseLabelSize()
	{
	labelSize--;
	}

/**
 * Increases label size of clusters by one point
 *
 */
public void increaseLabelClusterSize()
	{
	labelClusterSize++;
	}
/**
 * Decreases label size of clusters by one point
 *
 */
public void decreaseLabelClusterSize()
	{
	labelClusterSize--;
	}


/**
 * This function is activated when the mouse is pressed, controling navigation through the graph and
 * node selection
 */
protected void mousePressed() {
	if(!keyPressed)	g.clearSelectedNodes();
  
  g.setDragNode(null);
//	System.out.println("Hemos pulsado el rat�n "+mouseX+" magnifier a partir de "+xTopMagnifier);
  if((mouseX >= xTopMagnifier) && 
		   (mouseX <= xTopMagnifier + magnifierLength) &&
		   (mouseY >= yTopMagnifier ) && 
		   (mouseY <= yTopMagnifier + magnifierHeight)
		   ){
		System.out.println("Moving magnifier");
	  movingMagnifier = true;
  }
  
  //S�lo funciona fuera del �rea de overview
	if (mouseX < this.xTopOverviewBox || mouseY > this.yTopOverviewBox)
  	  {
	  Iterator itMouse=g.getNodes().values().iterator();
		 float xpress=mouseX+(Math.abs(xTopMagnifier-xTopOverviewBox)/overviewBoxLength)*areaInc*screenWidth;
		 float ypress=mouseY+(Math.abs(yTopMagnifier-yTopOverviewBox)/overviewBoxHeight)*areaInc*screenHeight;
		
	  while(itMouse.hasNext())
   		{
		 Node n=(Node)itMouse.next();
		 if (n.containsPoint(xpress, ypress)) 
		     {
			 if(n.isFixed() && mouseButton!=RIGHT)	n.fix(false);
				
			 g.addSelectedNode(n);
			 g.setDragNode(n);
			 }
   		}
  }
}

/**
 * This function is called each time that the mouse is moved, to check node hovering,
 * thus highlighting neighbor nodes.
 */
protected void mouseMoved() {
  if (g.getDragNode() == null) 
  	{
    Iterator itMM=g.getNodes().values().iterator();
	float xpress=(mouseX-offsetX)/zoomFactor;
	float ypress=(mouseY-offsetY)/zoomFactor;
    for(int i=0; i<g.getNodes().size(); i++) 
    	{
	    Node n = (Node)itMM.next();
	    if (n.containsPoint(xpress, ypress)) 
	      	{
	        g.setHoverNode(n);
	        break;
	      	}
    	}
    if(!itMM.hasNext())	g.setHoverNode(null);
  }
}

/**
 * This function is called when the mouse is released, to stop dragging nodes.
 */
protected void mouseReleased() {
  if(g.getDragNode()!=null)
	  {
	  if(keyPressed)	
		  {
		  g.getDragNode().fix(true);
		  }
	  g.setDragNode(null);
	  }
  
  for(int i=0; i<handleNum; i++) {
      handles[i].release();
    }
  
  movingMagnifier = false;
}

/**
 * This function is called when the mouse is dragging moving the selected node (if any) or
 * panning through the overall view
 */
protected void mouseDragged() {
  
	if (g.getDragNode() != null) 
		{
		 float xpress=(mouseX-offsetX)/zoomFactor;
		 float ypress=(mouseY-offsetY)/zoomFactor;

		 g.getDragNode().setX(xpress);
		 g.getDragNode().setY(ypress);
		}
	else if (showOverview)
		{
		if((mouseX > xTopOverviewBox) && 
		   (mouseX < xTopOverviewBox + overviewBoxLength) &&
		   (mouseY > yTopOverviewBox) && 
		   (mouseY < yTopOverviewBox + overviewBoxHeight)
		   )
			{
			if(movingMagnifier)
				{
				xTopMagnifier=mouseX-magnifierLength/2;
				if (xTopMagnifier <= screenWidth - overviewBoxLength)
				      xTopMagnifier = (screenWidth - overviewBoxLength);
			   
				yTopMagnifier=mouseY-magnifierHeight/2;
				if (yTopMagnifier <= 0)
				      yTopMagnifier = 0;
			   
			   //Factor applied to calculate the offset in the real graph
				offsetX = (xTopOverviewBox - xTopMagnifier) * (1 / factor*zoomFactor); 
				offsetY = (yTopOverviewBox - yTopMagnifier) * (1 / factor*zoomFactor); 
				}
			}	    
		}
  
	for(int i=0; i<handleNum; i++)       handles[i].update();
}

private void moveMagnifier(float newX, float newY)
	{
	   xTopMagnifier=xTopOverviewBox+newX;
	   yTopMagnifier=yTopOverviewBox+newY;
	   //Factor applied to calculate the offset in the real graph
	   offsetX = (xTopOverviewBox - xTopMagnifier) * (1 / factor); 
	   offsetY = (yTopOverviewBox - yTopMagnifier) * (1 / factor);
	}

private boolean alreadyInGraph(Graph g, String s) 
		{
		if(g.getNodes().containsKey(s))	return true;
		else 							return false;
		} 

/**
 * Searchs for and highlights the specified text in nodes and/or groups.
 * If the searched text is only present in one node/group, the visualization centers on it
 * @param text	Text to search for
 * @param searchInGroups	If true, search in made in group names, otherwise it is done in node names
 * @return	The number of occurences that contain the input text
 */
public int search(String text, boolean searchInGroups)
	{
	int num=0;
	
	System.out.println(text);
	if(text.length()>0)
		{
		g.clearSearchNodes();
		if(!searchInGroups)		//person search
			{
			Iterator itSearch=g.getNodes().values().iterator();
			while(itSearch.hasNext())
				{
				Node n=(Node)itSearch.next();
				if(n.getLabel().contains(text))
					g.addSearchNode(n);
				}
			if(g.getSearchNodes().size()==1)
				{//Center on it
				Map<String,Node> temp=g.getSearchNodes();
				Node n=temp.values().iterator().next();
	
				float newX=(float)(n.getX()-this.screenWidth/2)*(factor);
				float newY=(float)(n.getY()-this.screenHeight/2)*(factor);
				
				moveMagnifier(newX, newY);
				}
			num=g.getSearchNodes().size();
			}
		else			//group search
			{
			Iterator itSearch=g.getResults().values().iterator();
			num=0;
			while(itSearch.hasNext())
				{
				ClusterSet r=(ClusterSet)itSearch.next();
				for(int i=0;i<r.getClusters().size();i++)
					{
					MaximalCluster c=(MaximalCluster)r.getClusters().get(i);
					if(c.getLabel().contains(text))
						{
						for(int j=0;j<c.getNodes().size();j++)			g.addSearchNode(c.getNode(j));
						num++;
						}
					}
				}
			if(num==1)	//center on it
				{
				Node n=g.getSearchNodes().values().iterator().next();
				float newX=(float)(n.getX()-this.screenWidth/2)*(factor);
				float newY=(float)(n.getY()-this.screenHeight/2)*(factor);
				
				moveMagnifier(newX, newY);
				}
			}
		}
	else	
		g.clearSearchNodes();
	return num;
	}

//----------------------------- GRAPH BUILDING --------------------------------

/**
 * Builds a similarity matrix M (order nxn, being n the total number of groups) 
 * where Mij=k, with k the number of nodes that groups i and j share.
 * @return	The similarity matrix M
 */
private int[][] similarityMatrix()
	{
	int[][] sm=new int[numClusters][numClusters];
	Iterator it1=clusters.values().iterator();
	for(int i=0;i<numClusters-1;i++)
		{
		ArrayList a=(ArrayList)it1.next();
		Iterator it3=clusters.values().iterator();
		for(int j=0;j<=i;j++)	it3.next();
		for(int j=i+1;j<numClusters;j++)
			sm[i][j]=sm[j][i]=ArrayUtils.intersect(a,(ArrayList)it3.next());
		}
	return sm;
	}

private void readClusters()
	{
	numClusters=numClusters();
	System.out.println("Tenemos "+numClusters+" clusters");
	String data[] = loadStrings(getDataFile());

	String [] dataToken = data[0].split(delimiter);
	if (dataToken.length > 1)		 exit();
	
	clusters=null;
	clusters= new TreeMap<String,ArrayList<String>>();
	int cont=0;
	for (int l = 1; l < data.length; l++)
	  	{
		dataToken = data[l].split(delimiter);  
		if (dataToken.length >1)
			{
			ArrayList<String> lista=new ArrayList<String>(dataToken.length);
			for(int i=0;i<dataToken.length;i++)
				lista.add(dataToken[i]);
//			System.out.println("A�adiendo cluster de tama�o "+lista.size());
			clusters.put("cluster"+cont, lista);
			cont++;
			}
	  	}	
	}

//Read of clusters from a BicAT format
private void readClustersBicat()
	{
	String data[] = loadStrings(getDataFile());
	
	String [] dataToken = data[0].split(delimiter);
	if (dataToken.length > 1)		 exit();
	
	clusters=null;
	clusters= new TreeMap<String,ArrayList<String>>();
	int cont=0;
	geneNames=new ArrayList<String>();
	conditionNames=new ArrayList<String>();
	
	for (int l = 2; l < data.length; l++)
	  	{
		dataToken = data[l].split(delimiter);//Filas
		if (dataToken.length >1)
			{
			l++;
			dataToken = data[l].split(delimiter);//Filas  
			ArrayList<String> lista=new ArrayList<String>();
			if(!onlyConditions)
				{
				for(int i=0;i<dataToken.length;i++)		
					{
					lista.add(dataToken[i]);
					if(!geneNames.contains(dataToken[i]))	geneNames.add(dataToken[i]);
					}
				}
			l++;
			dataToken = data[l].split(delimiter);  //Columnas
			if (dataToken.length >1)
				{
				if(!onlyGenes)
					{
					for(int i=0;i<dataToken.length;i++)		
						{
						lista.add(dataToken[i]);
						if(!conditionNames.contains(dataToken[i]))	conditionNames.add(dataToken[i]);
						}
					}
				System.out.println("A�adiendo cluster "+cont+" de tama�o "+lista.size());
				//System.out.println("---------------------------");
				clusters.put("cluster"+cont, lista);
				this.numClusters++;
				cont++;
				}
			}
		}
	this.numClusters--;
	}



/**
 * Count the number of clusters in the visualization
 * @return the number of clusters
 */
public int numClusters()
	{
	int n=0;
	String data[] = loadStrings(getDataFile());

	String [] dataToken = data[0].split(delimiter);
	if (dataToken.length > 1)		 exit();
		
	for (int l = 1; l < data.length; l++)
	  	{
		dataToken = data[l].split(delimiter);  
		if (dataToken.length > 1)		n++;
		}
	return n;
	}

/**
 * Read the different sets of groups available in dataFile
 *
 */
 void readResultSets()
	{
	int n=0;
	resultSets=new ArrayList<Integer>();
	resultLabels=new ArrayList<String>();
	String data[] = loadStrings(getDataFile());
	
	String [] dataToken = data[0].split(delimiter);
	if (dataToken.length > 1)		 exit();
		
	for (int l = 1; l < data.length; l++)
	  	{
		dataToken = data[l].split(delimiter);  
		if (dataToken.length == 1)		
			{
			resultLabels.add(dataToken[0]);
			//n=Integer.valueOf(data[++l].split(delimiter)dataToken[0]).intValue();
			if(n!=0)	resultSets.add(n); 
			n=0;
			}
		if (dataToken.length > 1)		n++;
		}
	resultSets.add(n);
	}

private void readResultSetsBicat()
{
int n=0;
resultSets=new ArrayList<Integer>();
resultLabels=new ArrayList<String>();
String data[] = loadStrings(getDataFile());

String [] dataToken = data[0].split(delimiter);
if (dataToken.length > 1)		 exit();
	
for (int l = 1; l < data.length; l++)
  	{
	dataToken = data[l].split(delimiter);  
	if (dataToken.length == 1)		
		{
		resultLabels.add(dataToken[0]);
		//n=Integer.valueOf(data[++l].split(delimiter)dataToken[0]).intValue();
		if(n!=0)	
			{
			System.out.println("El conjunto tiene "+n/3+" biclusters");
			resultSets.add(n/3); 
			}
		n=0;
		}
	if (dataToken.length > 1)		n++;
	}
resultSets.add(n/3);
System.out.println("El conjunto tiene "+n/3+" biclusters");
}


private void readResultSets(String[] movies)
	{
	resultSets=new ArrayList<Integer>();
	resultLabels=new ArrayList<String>();
	resultLabels.add("Selected Films");
	if(movies!=null)	resultSets.add(movies.length);
	}


 void readFile()
	{
	readResultSets();
	readClusters();
	}

Graph buildRandomGraph() {
  Graph g;
  int nNodes = 15;
  int nEdges = 40;
  g = new Graph(this);
  
  for (int i=0; i<nNodes; i++) {
    ForcedNode n = new ForcedNode(new GraphPoint2D(screenWidth/4 + random(screenWidth/2), screenHeight/4 + random(screenHeight/2)));
    n.setLabel(i+"");
    n.setMass(1.0 + random(3));
    g.addNode(n);
  }
  
  for (int i=0; i<nEdges; i++) {
    Node a = (Node)g.getNodes().get((int)random(g.getNodes().size()));
    Node b = (Node)g.getNodes().get((int)random(g.getNodes().size()));
    if (a != b && !(g.isConnected(a,b))) {
      SpringEdge e = new SpringEdge(a, b);
      e.setNaturalLength(10+random(90));
      g.addEdge(e);
    }
  }
  
  return g;
}

/**
 * Builds a graph where each bicluster has a radial internal structure, thus reducing the number
 * of edges respect to complete structure.
 * @return	The radial graph built
 */
Graph buildRadialGraph()
	{
	Graph g=buildCompleteGraph();
	g.complete2radial();
	return g;
	}

/**
 * Reorders clusters so similar clusters appear together. Simple reordering as follows:
 * Take a cluster a, put it first
 * Find the cluster b most overlapped to cluster b. Put it second
 * Find cluster c most overlapped to cluster c. Put it third
 * ... 
 * 
 * Of course it is not the best way to order biclusters.
 */
int[] reorderClusters()
	{
	int[][] m=similarityMatrix();
		
	int pos=0;
	int maxPos=0;
	int maxValue=0;
	int []orden=new int[numClusters];
	for(int i=0;i<numClusters;i++)	orden[i]=-1;
	orden[0]=0;
	for(int i=1;i<numClusters;i++)
		{
		maxValue=0;
		maxPos=0;
		for(int j=0;j<numClusters;j++)
			{
			if(m[pos][j]>maxValue)//Search the maximum coincidence cluster
				{
				if(!isInArray(orden,j))
					{
					maxPos=j;
					maxValue=m[pos][j];
					}
				}
			}
		if(maxValue>0)
			{
			orden[i]=maxPos;
			pos=maxPos;
			}
		else	//If there is no intersection, takes the following not already included.
			{
			for(int k=1;k<numClusters;k++)	
				if(!isInArray(orden,k))	
					{
					orden[i]=k;
					pos=k;
					}
			}
		}
	System.out.println("ORDEN: ");
	for(int i=0;i<numClusters;i++)	System.out.print(orden[i]+" ");
	return orden;
	}

boolean isInArray(int [] a, int m)
	{
	for(int i=0;i<a.length;i++)		if(a[i]==m)	return true;
	return false;
	}

/**
 * Returns a list where true means that cluster i is overlapped with some other bicluster, false it it's completely unconnected
 * @return
 */
boolean[] connectedList()
	{
	int[][] m=similarityMatrix();
	for(int i=0;i<numClusters;i++)
	  {
	  for(int j=0;j<numClusters;j++)
		  {
		  System.out.print(m[i][j]+" ");
		  }
	  System.out.println();
	  }	
	
	boolean list[]=new boolean[numClusters];
	for(int i=0;i<numClusters;i++)
		{
		list[i]=false;
		for(int j=0;j<numClusters;j++)
			{
			if(m[i][j]>0)
				{
				list[i]=true;
				break;
				}
			}
		}
	return list;
	}

/**
 * Returns a list where each value is the number of clusters overlapped with cluster i
 * @return
 */
int[] overlapList()
	{
	int list[]=new int[numClusters];
	for(int i=0;i<numClusters;i++)
		{
		ArrayList a=clusters.get("cluster"+i);
		list[i]=0;
		for(int j=0;j<numClusters;j++)
			{
			ArrayList b=clusters.get("cluster"+j);
			if(i!=j)			list[i]+=ArrayUtils.intersect(a,b);
			}
		}
	return list;
	}

Graph buildOrderedRadialGraph()
	{
	g=buildRadialGraph();
	  
	  RadialCluster c = null;

	  //Reorder clusters to get the best initial configuration, plotting together clusters with 
	  //higher intersections.
	  System.out.println("Tenemos "+numClusters+" clusters");
	  removeNonOverlapped();
	  System.out.println("No solapados tenemos "+numClusters);
	  RadialCluster[] clu=orderRadialClusters();//A�ade un campo orden a cada cluster
		
	  
	  
	  //Cambiamos las dimensiones m�ximas del �rea de dibujo en funci�n del n�mero de clusters
		offsetX=-(screenWidth*(areaInc-1)/2);
		offsetY=-(screenHeight*(areaInc-1)/2);
		  
	  //Uniform distribution of clusters
	  double cellWidth, cellHeight;//Screen initial areas for each cluster
	  double clustersPerRow=Math.ceil(Math.sqrt(numClusters));
	  /*if(numClusters<40)
		  {
		  cellWidth=screenWidth/clustersPerRow;
		  cellHeight=screenHeight/clustersPerRow;
		  }
	  else
	  	  {
		  cellWidth=totalWidth/clustersPerRow;
		  cellHeight=totalHeight/clustersPerRow;
		  }*/
	  float e=totalWidth*totalHeight/numClusters;
	  while(e<100000 && areaInc<maxArea)//Espacio m�nimo que quiero que tenga cada cluster
	  	{
		increaseOverview(2);
		e=totalWidth*totalHeight/numClusters;  
	  	}
	  System.out.println("Espacio que necesitan "+areaInc + "e es "+e);
	  
	  cellWidth=totalWidth/clustersPerRow;
	  cellHeight=totalHeight/clustersPerRow;
	  
	  
	  
	  System.out.println("Cuadr�cula de "+clustersPerRow+"x"+clustersPerRow+" con dimensiones "+cellWidth+"x"+cellHeight);
		  
	  double x,y;
	  x=-cellWidth/2;
	  y=cellHeight/2;

	  
	  //El contador de nodos usados en lugar de filas por columnas 
	  nNodes = 0;
	 ArrayList<String> list=new ArrayList<String>();
	  
	 
	  for(int l=0;l<clu.length;l++)//Para cada cluster
	  	{
		c=clu[l];
		int o=c.getOrder();
		
		double row=Math.floor(o/clustersPerRow);
		double col=Math.floor(o%clustersPerRow);
		//System.out.println("Orden "+o+" posici�n "+row+", "+col);
		
		
		y=screenHeight*(areaInc-1)/2+col*cellHeight+cellHeight/2;
		if(row%2==0)	x=screenWidth*(areaInc-1)/2+row*cellWidth+cellWidth/2;				//direcci�n ->
		else			x=screenWidth*(areaInc-1)/2+screenWidth-row*cellWidth-cellWidth/2;	//direcci�n <-
		GraphPoint2D randomPos = new GraphPoint2D(x,y);
		c.getCenterNode().setPosition(randomPos);
		//float radio=5*c.getPeripheralNodes().size();
		//float pasoAngular=TWO_PI/c.getPeripheralNodes().size();
		float radio=5*c.getNodes().size();
		float pasoAngular=TWO_PI/c.getNodes().size();
		//System.out.println("Colocando "+((Node)c.getCenterNode()).getLabel()+" en pos "+x+","+y);
		
		for (int i = 0; i < c.getNodes().size(); i++) //Para cada nodo del cluster
		   {
		   ForcedNode n=c.getNode(i);
		   String nodeLabel = n.getLabel();
		   
		   if (!list.contains(nodeLabel))
		   		{
			    double newX=randomPos.getX()+radio*cos(i*pasoAngular);
			    double newY=randomPos.getY()+radio*sin(i*pasoAngular);

			    n.setPosition(new GraphPoint2D(newX,newY));
		   		list.add(nodeLabel);
		   		}
		  }
		}
	  
	  System.out.println("Terminada la construccion");
	  return g;
	}

Graph buildOrderedCompleteGraph()
{
  g=buildCompleteGraph();
  
  MaximalCluster c = null;

  //Reorder clusters to get the best initial configuration, plotting together clusters with 
  //higher intersections.
  System.out.println("Tenemos "+numClusters+" clusters");
  removeNonOverlapped();
  System.out.println("No solapados tenemos "+numClusters);
  MaximalCluster[] clu=orderClusters();//A�ade un campo orden a cada cluster
	
  //Uniform distribution of clusters
  double cellWidth, cellHeight;//Screen initial areas for each cluster
  double clustersPerRow=Math.ceil(Math.sqrt(numClusters));
  System.out.println("Cuadr�cula de "+clustersPerRow);
  
  float e=totalWidth*totalHeight/numClusters;
  while(e<100000 && areaInc<maxArea)//Espacio m�nimo que quiero que tenga cada cluster
  	{
	increaseOverview(2);
	e=totalWidth*totalHeight/numClusters;  
  	}
  System.out.println("Espacio que necesitan "+areaInc + "e es "+e);
  
  cellWidth=screenWidth/clustersPerRow;
  cellHeight=screenHeight/clustersPerRow;
  
  double x,y;
  x=-cellWidth/2;
  y=cellHeight/2;

  
  //El contador de nodos usados en lugar de filas por columnas 
  nNodes = 0;
 ArrayList<String> list=new ArrayList<String>();
  
  for(int l=0;l<clu.length;l++)//Para cada cluster
  	{
	c=clu[l];
	int o=c.getOrder();
	
	double row=Math.floor(o/clustersPerRow);
	double col=Math.floor(o%clustersPerRow);
	
	y=screenHeight*(areaInc-1)/2+col*cellHeight+cellHeight/2;
	if(row%2==0)	x=screenWidth*(areaInc-1)/2+row*cellWidth+cellWidth/2;				//direcci�n ->
	else			x=screenWidth*(areaInc-1)/2+screenWidth-row*cellWidth-cellWidth/2;	//direcci�n <-

/*	y=row*cellHeight+cellHeight/2;
	if(col%2==0)	x=col*cellWidth+cellWidth/2;				//direcci�n ->
	else			x=screenWidth-col*cellWidth-cellWidth/2;	//direcci�n <-*/
	GraphPoint2D randomPos = new GraphPoint2D(x,y);
	
	for (int i = 0; i < c.getNodes().size(); i++) //Para cada nodo del cluster
	   {
	   ForcedNode n=c.getNode(i);
	   String nodeLabel = n.getLabel();
	   
	   
	   if (!list.contains(nodeLabel))
	   		{
		    double newX=randomPos.getX() -cellWidth/2+random((float)cellWidth);
		    double newY=randomPos.getY()-cellHeight/2+random((float)cellHeight);

		    n.setPosition(new GraphPoint2D(newX,newY));
	   		list.add(nodeLabel);
	   		}
	  }
	}
  
  System.out.println("Terminada la construccion");
  return g;	
}

MaximalCluster[] orderClusters()
	{
	System.out.println("Ordering clusters");
	
	int []list=reorderClusters();
	System.out.println("La lista de ordenaci�n tiene "+list.length);
	MaximalCluster[] cl=new MaximalCluster[list.length];
	Iterator itOrder=g.getResults().values().iterator();
	int cont=0;
	
	//Put order in cluster objects
	while(itOrder.hasNext())
		{
		ClusterSet r=(ClusterSet)itOrder.next();
		ArrayList clusters1=r.getClusters();
		for(int i=0;i<clusters1.size();i++)
			{
			MaximalCluster c=(MaximalCluster)clusters1.get(i);
			c.setOrder(list[cont]);
			cl[cont]=c;
			cont++;
			}
		}
	
	//Order cluster list
	MaximalCluster[] clo=new MaximalCluster[cl.length];
	
	for(int orden=0;orden<cl.length;orden++)
		{
		for(int i=0;i<cl.length;i++)
			{
			if(cl[i].getOrder()==orden)
				{
				clo[orden]=cl[i];
				break;
				}
			}
		}
	return clo;
	}

RadialCluster[] orderRadialClusters()
	{
	int []list=reorderClusters();
	RadialCluster[] cl=new RadialCluster[list.length];
	Iterator itOrder=g.getResults().values().iterator();
	int cont=0;
	
	//Put order in cluster objects
	while(itOrder.hasNext())
		{
		ClusterSet r=(ClusterSet)itOrder.next();
		ArrayList clusters1=r.getClusters();
		for(int i=0;i<clusters1.size();i++)
			{
			RadialCluster c=(RadialCluster)clusters1.get(i);
			c.setOrder(list[cont]);
			cl[cont]=c;
			cont++;
			}
		}
	
	//Order cluster list
	RadialCluster[] clo=new RadialCluster[cl.length];
	
	for(int orden=0;orden<cl.length;orden++)
		{
		for(int i=0;i<cl.length;i++)
			{
			if(cl[i].getOrder()==orden)
				{
				clo[orden]=cl[i];
				break;
				}
			}
		}
	return clo;
	}

MaximalCluster getClusterInPos(int pos)
	{
	//System.out.println("GetCluster in pos "+pos);
	for(int i=0;i<resultSets.size();i++)
		{
		int numRS=((Integer)(resultSets.get(i))).intValue();
		if(pos>=numRS)	
			pos-=numRS;
		else			
			{
			Map<String,ClusterSet> m=g.getResults();
			ClusterSet r=m.get(resultLabels.get(i));
			return (MaximalCluster)(r.getClusters().get(pos));
			}
		}
	return null;
	}

/**
 * Returns the group with the required label as name
 * @param label	text of the label of the group
 * @return the group with this label
 */
MaximalCluster getClusterWithLabel(String label)
{
	Iterator itClus=g.getResults().values().iterator();
	while(itClus.hasNext())
		{
		ClusterSet r=(ClusterSet)itClus.next();
		for(int i=0;i<r.getClusters().size();i++)
			{
			MaximalCluster c=(MaximalCluster)r.getClusters().get(i);
			if(label.equals(c.getLabel()))	return c;
			}
		}
	return null;
}


/**
 * Deletes all clusters that aren't overlapped enough by means of threshold, along with
 * all its edges and the nodes that aren't in any other cluster.
 *
 */
void removeNonOverlapped()
	{
	System.out.println("REMOVE NON OVERLAPPED -----------------------------");
	System.out.println("N� de clusters "+clusters.size());
	int[] list=overlapList();
	System.out.println("Tenemos una lista de "+list.length);
	
	ArrayList<MaximalCluster> tentativeClusters=new ArrayList<MaximalCluster>();
	nodesToRemove=new ArrayList<Node>();
	edgesToRemove=new ArrayList<Edge>();
	
	ArrayList<Integer> posToRem=new ArrayList<Integer>();
	
	//Determinamos todos lo que hay que quitar
	for(int i=0;i<list.length;i++)
		{
		if(list[i]<connectionThreshold)
			{
			MaximalCluster c=getClusterWithLabel("cluster"+i);
			tentativeClusters.add(c);
			posToRem.add(new Integer(i));
			}
		}
	
	if(tentativeClusters.size()>=numClusters)
		System.out.println("No clusters removed because the whole visualization will be removed");
	else
		clustersToRemove=tentativeClusters;
	
	System.out.println("Clusters to remove: "+clustersToRemove.size());
	//Para cada uno de ellos, eliminamos sus nodos,
	//los eliminamos de la lista clusters y disminuimos
	//el n�mero de clusters del resultSet.
	for(int i=0;i<clustersToRemove.size();i++)
		{
		int pos=(Integer)(posToRem.get(i)).intValue();
		MaximalCluster c=(MaximalCluster)clustersToRemove.get(i);
		
		ArrayList lista=c.getNodes();
		for(int k=0;k<lista.size();k++)//Para cada nodo del cluster
			{
			Node nr=(Node)lista.get(k);
			boolean toRemove=true;
			if(connectionThreshold>0)
				{//Tenemos que comprobar que no quitamos un nodo que est� en otros clusters
				//TODO: No s� si es mejor dejar el nodo con la informaci�n de que pertenece a varios clusters o quitarla tambi�n
				Iterator itRNO=g.getResults().values().iterator();
				while(itRNO.hasNext())
					{
					ClusterSet r=(ClusterSet)itRNO.next();
					for(int m=0;m<r.getClusters().size();m++)//Si el nodo est� en alg�n otro cluster
						{
						MaximalCluster c2=(MaximalCluster)r.getClusters().get(m);
						if(!clustersToRemove.contains(c2) && c2.getNodes().contains(nr))//Que no sea uno a quitar
							{
							toRemove=false;
							break;
							}
						}
					}
				}
			if(toRemove)	
				{
				nodesToRemove.add(nr);
				g.getNodes().remove(nr.getLabel());//y a s� mismo
				}
			}
		
		edgesToRemove.addAll(c.removeEdges());
		
		c.getClusterSet().removeCluster(c);
		clusters.remove(c.getLabel());
		for(int j=0;j<resultSets.size();j++)
			{
			int numRS=((Integer)(resultSets.get(j))).intValue();
			if(pos>=numRS)	pos-=numRS;
			else			
				{
				resultSets.set(j, numRS-1);
				break;
				}
			}
		numClusters--;
		}
	}


/**
 * 
 * @return	a list with the number of genes and conditions that bicluster i has from the selected ones
 */
private int[] selectionList(BiclusterSelection bs)
	{
	int list[]=new int[numClusters];
	ArrayList<String> b=null;
	if(bs.getConditions().size()==microarrayData.getNumConditions())	b=microarrayData.getNames(bs.getGenes(), new LinkedList<Integer>());	
	else														b=microarrayData.getNames(bs.getGenes(), bs.getConditions());
	for(int i=0;i<numClusters;i++)
		{
		ArrayList<String> a=clusters.get("cluster"+i);
		list[i]=ArrayUtils.intersect(a, b);
		System.out.println("Bicluster "+i+" has "+list[i]+" nodes related");
		}
	return list;
	}
/**
 * Deletes all clusters that haven't any of the genes or conditions in BiclusterSelection
 *
 */
void removeNonSelected(BiclusterSelection bs)
	{
	System.out.println("REMOVE non Selected -----------------------------");
	System.out.println("N� de clusters "+clusters.size());
	int[] list=selectionList(bs);
	System.out.println("Tenemos una lista de "+list.length);
	
	ArrayList<MaximalCluster> tentativeClusters=new ArrayList<MaximalCluster>();
	nodesToRemove=new ArrayList<Node>();
	edgesToRemove=new ArrayList<Edge>();
	
	ArrayList<Integer> posToRem=new ArrayList<Integer>();
	
	//Determinamos todos lo que hay que quitar
	for(int i=0;i<list.length;i++)
		{
		if(list[i]<=0)//This bicluster is not selected
			{
			MaximalCluster c=getClusterWithLabel("cluster"+i);
			tentativeClusters.add(c);
			posToRem.add(new Integer(i));
			}
		}
	
	if(tentativeClusters.size()>=numClusters+1)
		System.out.println("No clusters removed because the whole visualization will be removed");
	else
		{
		clustersToRemove=tentativeClusters;
		}
	System.out.println("To hide "+clustersToRemove.size()+" out of "+this.numClusters+" total clusters");
	
	//Para cada uno de ellos, eliminamos sus nodos s�lo si no 
	//est�n en ning�n bicluster que est� seleccionado
	//los eliminamos de la lista clusters y disminuimos
	//el n�mero de clusters del resultSet.

	System.out.println("Number of clusters before selection: "+clusters.size());
	System.out.println("Number of nodes before selection: "+g.getNodes().size());
	for(int i=0;i<clustersToRemove.size();i++)//Para cada cluster a quitar
		{
		
		int pos=(Integer)(posToRem.get(i)).intValue();
		MaximalCluster c=(MaximalCluster)clustersToRemove.get(i);
		
		ArrayList lista=c.getNodes();
		for(int k=0;k<lista.size();k++)//Para cada nodo del cluster
			{
			Node nr=(Node)lista.get(k);
			Map<String, Cluster> map=nr.clusters;//cogemos los clusters en los que est�
			boolean toRemove=true;
			for(int j=0;j<list.length;j++)
				{
				if(list[j]>0)	//si para alguno de los clusters que se queda
					{
					//MaximalCluster c2=getClusterWithLabel("cluster"+j);
					//if(c2!=c)
					if(map.containsKey("cluster"+j) && !c.label.equals("cluster"+j))//est� este nodo
						{
						toRemove=false;	//lo dejamos
						break;
						}
					}
				}
			if(toRemove)	//se quita solo si no est� en alg�n cluster seleccionado	
				{
				nodesToRemove.add(nr);
				g.getNodes().remove(nr.getLabel());//y a s� mismo
				}
			}
		edgesToRemove.addAll(c.removeEdges());
		
		c.getClusterSet().removeCluster(c);
		clusters.remove(c.getLabel());
		for(int j=0;j<resultSets.size();j++)
			{
			int numRS=((Integer)(resultSets.get(j))).intValue();
			if(pos>=numRS)	pos-=numRS;
			else			
				{
				resultSets.set(j, numRS-1);
				break;
				}
			}
		numClusters--;
		}
	System.out.println("Nodes after selection "+g.getNumNodes());
	System.out.println("Clusters after selection "+clusters.size());
	}


/**
 * Restore all groups to the visualization
 *
 */
void restoreClusters()
	{
	//Recuperamos las listas de apoyo
	//if(movieButActors)	readClustersPersons(movies);
	//else				readClusters(movies, maximumCast);
	//readResultSets(movies);
	
	System.out.println("ANTES DE RESTAURAR "+g.getNodes().size()+" nodos y "+clusters.size()+" clusters");
	//Recuperamos los nodos y los reponemos en sus clusters
	if(nodesToRemove!=null)
		for(int i=0;i<nodesToRemove.size();i++)
			{
			Node n=nodesToRemove.get(i);
			g.addNode(n);
			}

	//Recuperamos las estructuras de los clusters
	if(clustersToRemove!=null)
		for(int i=0;i<clustersToRemove.size();i++)
			{
			MaximalCluster c=clustersToRemove.get(i);
			//System.out.println("Recuperamos clusters "+c.getLabel());
			c.getClusterSet().addCluster(c);
			ArrayList<Node> cnodes=c.getNodes();
			ArrayList<String> clabels=new ArrayList<String>();
			for(int j=0;j<cnodes.size();j++)	clabels.add(cnodes.get(j).label);
			clusters.put(c.label, clabels);
			}
		
	
	//Recuperamos las aristas y conectamos sus nodos
	if(edgesToRemove!=null)
		for(int i=0;i<edgesToRemove.size();i++)
			{
			Edge e=edgesToRemove.get(i);
			//Miramos si ya estaba
			SpringEdge ret=(SpringEdge)g.getEdges().get(e.getTo().getLabel()+"->"+e.getFrom().getLabel());	//MIramos a ver si est� en un sentido
	    	if(ret==null)	ret=(SpringEdge)g.getEdges().get(e.getFrom().getLabel()+"->"+e.getTo().getLabel());//O en el otro
	    	if(ret!=null)	
	    		{
	    		ret.setLengthFactor(ret.getLengthFactor()*0.8);
	    		}
	    	else			
	    		{
	    		g.addEdge(e);//Se encarga de ponerlo en el from y en el to.
	    		}
			}
	
	System.out.println("TRAS RESTAURAR "+g.getNodes().size()+" nodos y "+clusters.size()+" clusters");
	this.numClusters=clusters.size();//TODO: deprecate numClusters por clusters.size()
	return;
	}



/**
 * Builds a graph according to dataFile information in the Session layer
 */
public void buildGraph()
	{
	//Preliminary reading of biclusters and ordering
	readResultSetsBicat();
	readClustersBicat();
	if(initialOrdering && radial)	g=buildOrderedRadialGraph();
	else
		{
		if(initialOrdering)			g=buildOrderedCompleteGraph();
		else
			{
			if(radial)		g=buildRadialGraph();
			else			g=buildCompleteGraph();
			}
		}
	
	insertDetails();
	Runtime.getRuntime().gc();//Al final del constructor quiz�s sea buena idea
	}

/**
 * Removes all the information about the graph
 */
public void clearGraph()
	{
	if(g!=null)
		{
		g.setNodes(null);
		g.setEdges(null);
		g.setCenterNodes(null);
		g.setClusterSets(null);
		clusters=null;
		resultSets=null;
		}
	  Runtime.getRuntime().gc();//Al final del constructor quiz�s sea buena idea
	}

/**
 * M�todo para crear un caso de resultados de biclustering
 * @return	
 */
Graph buildCompleteGraph() 
	{
	  Graph g;
	  ClusterSet r = null;
	  MaximalCluster c = null;
	  g = new Graph(this);
	  
	  //Uniform distribution of clusters
	  double cellWidth, cellHeight;//Screen initial areas for each cluster
	  cellWidth=screenWidth/Math.sqrt(numClusters);
	  cellHeight=screenHeight/Math.sqrt(numClusters);
	  double x,y;
	  x=-cellWidth/2;
	  y=cellHeight/2;
	  
	 // if(titles==null && getTitleFile()!=null)	titles=loadStrings(getTitleFile());
	  
	  //El contador de nodos usados en lugar de filas por columnas 
	  nNodes = 0;
	  int color = this.bicColor1;
	  int clusterCount=0;

	  CustomColor nc = new CustomColor();
		
	  for(int i=0;i<resultSets.size();i++)//Para cada result set
	  	{
		//New ResultSet
		r = new ClusterSet();
		r.setLabel((String)resultLabels.get(i));
		
		//r.setColor(nc.getGoodColor(color++));
		//if(this.colorResults!=null && this.colorResults.get(color)!=null)	r.setColor(this.colorResults.get(color++));
		//else			
		r.setColor(new CustomColor(paleta[color++]));
		
		r.setGraph(g);
		g.addClusterSet(r);
		for(int j=0;j<resultSets.get(i);j++)//Para cada cluster en el resultset
			{
			//Uniform position
			x+=cellWidth;
			if(x>=screenWidth)	
				{
				x=cellWidth/2;
				y+=cellHeight;
				}
			
			c = new MaximalCluster(r, "cluster"+new Integer(clusterCount).toString());
			c.setLabel("cluster"+clusterCount);
			if(titles!=null)	
				c.setLabel(titles[clusterCount]);
			System.out.println("Buscando cluster "+clusterCount+" de un total de "+this.numClusters);
			for (int k = 0; k < clusters.get("cluster"+clusterCount).size(); k++) //Para cada nodo en el cluster
			   {
			   String nodeLabel =(String)clusters.get("cluster"+clusterCount).get(k);
			   ForcedNode n;
			   //System.out.println(nodeLabel);
			
			   if(!alreadyInGraph(g,nodeLabel))
			   	  {
				  n = new ForcedNode(new GraphPoint2D(x -cellWidth/2+random((float)cellWidth),y-cellHeight/2+random((float)cellHeight)));
				  n.setLabel(nodeLabel);
				  n.setMass(1.0);
				  n.setSize(nodeSize);
				  g.addNode(n);
				  nNodes++;
				  }
			   else	       n=(ForcedNode)g.getNodes().get(nodeLabel);
		        
			  c.addNode(n);
			  }//each node in cluster
			c.notifyNodesInCluster();
			r.addCluster(c);
		    clusterCount++;
			}//each cluster in resultSet
	    // And the last ResultSet must also be added to the graph
	    g.addClusterSet(r);
	  	}//each resultSet
	  
	  println("n�mero de nodos: "+g.getNumNodes()+", con centrales "+g.getNumCenterNodes()+" y aristas "+g.getNumEdges());
	  
	  return g;
	}

//----------------------------- GETTERS AND SETTERS ------------------------------
/*
 * TODO: Muchos de estos par�metros, aunque est�n en biclusVis, hay que asegurar que se usan m�s en
 * esta clase que en las inferiores
 */

/**
 * Returns the path from which the bicluster are read
 * @return the path from which the bicluster are read
 */
public String getDataFile() {
	return dataFile;
}

/**
 * Sets the path from which the biclusters are to be read
 * @param dataFile	path from which the biclusters are to be read
 */
public void setDataFile(String dataFile) {
	this.dataFile = dataFile;
}

/**
 * Returns true if edges are being drawn, false otherwise
 * @return true if edges are being drawn, false otherwise
 */
public boolean isShowEdges() {
	return showEdges;
}

/**
 * Sets the drawing of edges
 * @param showEdges	if true, edges will be drawn
 */
public void setShowEdges(boolean showEdges) {
	this.showEdges = showEdges;
}

/**
 * Returns the stiffness of spring forces
 * @return	the stiffness of spring forces
 */
public double getStiffness() {
	return stiffness;
}

/**
 * Sets the stiffness of spring forces
 * @param stiffness	The value for stiffnes in spring forces. Too high values could lead to unestable layouts
 */
public void setStiffness(double stiffness) {
	this.stiffness = stiffness;
}

/**
 * Returns the optimal length between two nodes connected by a spring
 * @return	the optimal length between two nodes connected by a spring
 */
public double getEdgeLength() {
	return edgeLength;
}

/**
 * Sets the optimal length between two nodes connected by an edge (spring)
 * @param edgeLength	the optimal length between two nodes connected by an edge
 */
public void setEdgeLength(double edgeLength) {
	this.edgeLength = edgeLength;
}

/**
 * Returns true if curved hulls are being used, false if polygonal hulls
 * @return true if curved hulls are being used, false if polygonal hulls
 */
public boolean isUseCurves() {
	return useCurves;
}

/**
 * Sets the mode to draw hulls
 * @param useCurves	if true, curved shapes are drawn as hulls, otherwise, polygonal hulls are drawn
 */
public void setUseCurves(boolean useCurves) {
	this.useCurves = useCurves;
}

/**
 * Returns the size of nodes (its radius)
 * @return the size of nodes
 */
public int getNodeSize() {
	return nodeSize;
}

/**
 * Sets the size (radius) of nodes
 * @param nodeSize radius for nodes
 */
public void setNodeSize(int nodeSize) {
	this.nodeSize = nodeSize;
}

/**
 * Returns true if node labels are being drawn 
 * @return true if node labels are being drawn
 */
public boolean isShowLabel() {
	return showLabel;
}

/**
 * Sets the displaying of node labels
 * @param showLabel	if true, node labels are drawn, otherwise they are hidden
 */
public void setShowLabel(boolean showLabel) {
	this.showLabel = showLabel;
}

/**
 * Returns the height of the screen where the graph is displayed
 * @return the height of the screen where the graph is displayed
 */
public float getScreenHeight() {
	return screenHeight/zoomFactor;
}

/**
 * Sets the height of the screen where the graph is displayed
 * @param screenHeight height of the screen where the graph is displayed
 */
public void setScreenHeight(int screenHeight) {
	this.screenHeight = screenHeight;
}

/**
 * Returns the width of the screen where the graph is displayed
 * @return the width of the screen where the graph is displayed
 */
public float getScreenWidth() {
	return screenWidth/zoomFactor;
}

/**
 * Sets the width of the screen where the graph is displayed
 * @param screenWidth width of the screen where the graph is displayed
 */
public void setScreenWidth(int screenWidth) {
	this.screenWidth = screenWidth;
}

/**
 * @deprecated
 */
public int getHandleLength() {
	return handleLength;
}

/**
 * @deprecated
 * 
 */
public void setHandleLength(int handleLength) {
	this.handleLength = handleLength;
}

/**
 * Returns true if clusters are being considered as radial subgraphs connected by a dummy node
 * @return true if clusters are being considered as radial subgraphs connected by a dummy node
 */
public boolean isRadial() {
	return radial;
}

/**
 * Sets the biclusters build mode
 * @param radial	if true, radial mode is used (a dummy node for each cluster joined to each node in the cluster),
 * otherwise, complete mode is used (all nodes are connected with every other node in the cluster)
 */
public void setRadial(boolean radial) {
	this.radial = radial;
}

/**
 * Returns true if piecharts are being drawn for nodes
 * @return true if piecharts are being drawn for nodes
 */
public boolean isDrawPiecharts() {
	return drawArc;
}

/**
 * Sets the piechart mode
 * @param drawPiechart	if true, piecharts are drawn for nodes
 */
public void setDrawPiecharts(boolean drawPiechart) {
	this.drawArc = drawPiechart;
}

/**
 * Returns true if piecharts must be drawn only for nodes in more than one bicluster
 * @return true if piecharts must be drawn only for nodes in more than one bicluster
 */
public boolean isOnlyIntersecting() {
	return onlyIntersecting;
}

/**
 * Sets a filter that only draws piecharts for nodes in more than one bicluster
 * @param onlyIntersecting	true if piechart must be drawn only in nodes in more than one bicluster, false if piecharts must be drawn in every node
 */
public void setOnlyIntersecting(boolean onlyIntersecting) {
	this.onlyIntersecting = onlyIntersecting;
}

/**
 * Retuns the drawing state of hulls
 * @return	true if hulls are drawn, false if they are hidden
 */
public boolean isDrawHull() {
	return drawHull;
}

/**
 * Sets the drawing state of hulls
 * @param drawHull	true for hull drawing, false for hull hidding
 */
public void setDrawHull(boolean drawHull) {
	this.drawHull = drawHull;
}

/**
 * Returns the draw state of cluster labels
 * @return	true if cluster labels are drawn, false if they are hidden
 */
public boolean isDrawClusterLabels() {
	return drawTitle;
}

/**
 * Sets the drawing state of cluster labels.
 * @param drawClusterLabels	true for cluster labels drawing, false for hidding
 */
public void setDrawClusterLabels(boolean drawClusterLabels) {
	this.drawTitle = drawClusterLabels;
}

/**
 * Returns true if in addition mode
 * TODO: still in development
 * @return true if in addition mode
 */
public boolean isAdditionMode() {
	return additionMode;
}

/**
 * Sets the addition mode
 * TODO: Still in development
 * @param additionMode
 */
public void setAdditionMode(boolean additionMode) {
	this.additionMode = additionMode;
}

/**
 * Gets the size of node labels
 * @return	the size of node labels
 */
public int getLabelSize() {
	return labelSize;
}

/**
 * Sets the size of node labels
 * @param labelSize	the new label size
 */
public void setLabelSize(int labelSize) {
	this.labelSize = labelSize;
}

/**
 * Returns true if the force simulation is paused
 * @return	true if the simulation is paused, false otherwise
 */
public boolean isPauseSimulation() {
	return pauseSimulation;
}

/**
 * Pauses or resumes simulation
 * @param pauseSimulation	boolean, true if we want to pause simulation, false if we want it to resume
 */
public void setPauseSimulation(boolean pauseSimulation) {
	this.pauseSimulation = pauseSimulation;
}

/**
 * Returns true if the overall view is being displayed, false otherwise
 * @return	true if the overall view is being displayed, false otherwise
 */
public boolean isShowOverview() {
	return showOverview;
}

/**
 * Sets the visualization of the overall view
 * @param showOverview	boolean, if true, overall view will be displayed, otherwiste it will be hidden
 */
public void setShowOverview(boolean showOverview) {
	this.showOverview = showOverview;
}

float getXTopDrawingRect() {
	return xTopDrawingRect;
}

void setXTopDrawingRect(float topDrawingRect) {
	xTopDrawingRect = topDrawingRect;
}

float getYTopDrawingRect() {
	return yTopDrawingRect;
}

void setYTopDrawingRect(float topDrawingRect) {
	yTopDrawingRect = topDrawingRect;
}

float getOffsetX() {
	return offsetX;
}

void setOffsetX(float offsetX) {
	this.offsetX = offsetX;
}

float getOffsetY() {
	return offsetY;
}

void setOffsetY(float offsetY) {
	this.offsetY = offsetY;
}

boolean isDrawingOverview() {
	return drawingOverview;
}

void setDrawingOverview(boolean drawingOverview) {
	this.drawingOverview = drawingOverview;
}



public void setPalette(java.awt.Color[] paleta)
	{
	this.paleta=paleta;
	int color=BiclusVis.bicColor1;
	if(g!=null)
		{
		for(int i=0;i<g.getResults().size();i++)
			{
			ClusterSet cs=g.getResults().get(resultLabels.get(i));
			cs.myColor=new CustomColor(this.paleta[color++]);
			}
		}
	}

/**
 * Returns true if size is being used to convey the degree of interconnection of nodes
 * @return true if size is being used to convey the degree of interconnection of nodes
 */
public boolean isSizeRelevant() {
	return sizeRelevant;
}

/**
 * Sets if size is used to convey the degree of interconnection of nodes
 * @param sizeRelevant	true if size is to be used to convey the degree of interconnection of nodes
 */
public void setSizeRelevant(boolean sizeRelevant) {
	this.sizeRelevant = sizeRelevant;
}

/**
 * Returns the number of dummy nodes used
 * @return the number of dummy nodes used
 */
public int getNumCentroids() {
	return numCentroids;
}

/**
 * Returns the size of cluster labels
 * @return the size of cluster labels
 */
public int getLabelClusterSize() {
	return labelClusterSize;
}

/**
 * Sets the size of cluster labels
 * @param labelClusterSize	the new size of cluster labels
 */
public void setLabelClusterSize(int labelClusterSize) {
	this.labelClusterSize = labelClusterSize;
}

/**
 * Returns true if nodes are being drawn
 * @return true if nodes are being drawn	
 */
public boolean isDrawNodes() {
	return drawNodes;
}

/**
 * Sets the drawing or hiding of nodes
 * @param drawNodes	if true, nodes will be drawn
 */
public void setDrawNodes(boolean drawNodes) {
	this.drawNodes = drawNodes;
}

/**
 * Returns true if the size of node labels is not being used to convey the degree of connection of nodes
 * @return true if the size of node labels is not being used to convey the degree of connection of nodes
 */
public boolean isAbsoluteLabelSize() {
	return absoluteLabelSize;
}

/**
 * Sets if node label is used to convey the degree of connection of nodes.
 * If this is the case, standard label size is increased by the number of groups in which it is minus 1
 * @param absoluteLabelSize	true if we want node size to convey the degree of connection of nodes, false otherwise
 */
public void setAbsoluteLabelSize(boolean absoluteLabelSize) {
	this.absoluteLabelSize = absoluteLabelSize;
}

/**
 * Gets the graph which is displayed by BiclusVis
 * @return	Graph displayed by BiclusVis
 */
public Graph getGraph() {
	return g;
}

/**
 * Gets MicroarrayData() for this BiclusVis
 * TODO: STILL IN DEVELOPMENT: the only thing we want for MicroarrayData are
 * complete gene and condition ids to set in the nodes 
 * @return	the MicroarrayData for this BiclusVis
 */
public MicroarrayData getMicroarrayData() {
	return microarrayData;
}

/**
 * Sets MicroarrayData for this BiclusVis
 * TODO: STILL IN DEVELOPMENT: the only thing we want for MicroarrayData are
 * complete gene and condition ids to set in the nodes 
 * @param microarrayData	the MicroarrayData for this BiclusVis
 */
public void setMicroarrayData(MicroarrayData microarrayData) {
	this.microarrayData = microarrayData;
}
}