package es.usal.bicoverlapper.visualization.diagrams.overlapper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;

import es.usal.bicoverlapper.analysis.geneticAlgorithms.GraphGeneticAlgorithm;
import es.usal.bicoverlapper.data.GOTerm;
import es.usal.bicoverlapper.data.GeneAnnotation;
import es.usal.bicoverlapper.data.GeneRequester;
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.utils.ArrayUtils;
import es.usal.bicoverlapper.utils.CustomColor;
import es.usal.bicoverlapper.utils.GraphPoint2D;
import es.usal.bicoverlapper.visualization.diagrams.Diagram;


import prefuse.data.Table;


/**
 * This class visualizes biclusters as overlapping hulls on a force-directed graph layout. 
 * It manages force-directed layout, graph displaying and parameter changes.
 * @author Roberto Ther�n & Rodrigo Santamar�a
 *
 */
public class Overlapper extends JProcessingPanel implements GeneRequester {

	private static final long serialVersionUID = 1L;

//Main variables
/**
 * Screen height of the drawing area in pixels
 */
int screenHeight = 700;//1500
int screenWidth = 1000;//2000
int nNodes;
private int nodeSize = 17;
private int labelSize = 5;//antes 5
private int maxLabelSize=20;
private int labelClusterSize=labelSize*2;//antes *3
private double edgeLength = 100;
double stepEdgeLength = 2;
double G = 10; //Gravity (clusters de ~20 nodos)
//double G = 10; //Gravity (clusters de 200+ nodos)
double stepG = 0.5;
double D = 2.0; //Depth of the well
double K = D/100000;//Para pelis parece que este funciona
double stepK = 0.00005;
//private double stiffness = 0.001;//Clusters >200 nodos
private double stiffness = 0.001;//Clusters >20 nodos
//private double stiffness = 0.3;//Sugiyama
double stepStiffness = 0.0005;
private double closeness = nodeSize; 

protected ArrayList<Point2D.Double> selectionArea = null;
//protected Path2D.Double selectionArea = null;

public boolean move=false;
//Data file information
private String dataFile;
//private String titleFile=null;//Names for each clusters, just if necesary.
String delimiter="\t";
String groupDelimiter=null;
String posDelimiter="|";
int headerLines=0;

MicroarrayData microarrayData=null;

//Structures for graph information
Map<String,ArrayList<String>> clusters;
ArrayList<Integer> resultSets;
ArrayList<String> resultLabels;
String [] groupNames=null;
Point2D.Double [] initPos=null;
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
int cont=1;
int iteracion=0;

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
boolean computeDualLayout=false;

float threshold=0;	//Shows only clusters that fulfill threshold criteria
int thresholdType=0; //depending of this variable, threshold refers to degree of overlap (0), size of the biclusters (1) or consistency of the biclusters (2)
int nodeThreshold=0;		//Shows only nodes with at least n clusters related
float step=1;	//The step used to increment or decrement the threshold
ArrayList<String> excludedClusters=new ArrayList<String>();

boolean drawTopography=false;
boolean drawExactGroups=false;
boolean additionMode=false;
boolean drawAwarded=true;		//Se le puede hacer un toggle
boolean drawGlyphs=true;		//Se le puede hacer un toggle
boolean fullDrawing=false;		//Para imprimir la imagen entera sin ce�irnos a los m�rgenes de la ventana (para fotos)
int []	priorities=new int[]{Graph.EDGE, Graph.HULL, Graph.HULLLABEL, Graph.ZONE, Graph.PIECHART, Graph.NODE, Graph.NODELABEL, Graph.SELECT, Graph.HOVER, Graph.SEARCH, Graph.ERROR, Graph.DUAL, Graph.SURFACE};
//int []	priorities=new int[]{Graph.EDGE, Graph.HULL, Graph.HULLLABEL, Graph.PIECHART, Graph.NODE, Graph.NODELABEL, Graph.HOVER, Graph.SEARCH, Graph.SELECT, Graph.ERROR, Graph.DUAL};
//int []	priorities=new int[]{Graph.EDGE, Graph.HULL, Graph.HULLLABEL, Graph.PIECHART, Graph.NODE, Graph.NODELABEL, Graph.HOVER, Graph.SEARCH, Graph.SELECT, Graph.ERROR};
//int []	priorities=new int[]{Graph.EDGE, Graph.HULL, Graph.HULLLABEL, Graph.PIECHART, Graph.NODE, Graph.NODELABEL, Graph.HOVER, Graph.SEARCH, Graph.SELECT};
int []	prioritiesOverview=new int[]{Graph.HULL, Graph.NODE};

//ArrayList <CustomColor> colorResults=null;

ArrayList <MaximalCluster> clustersToRemove=null;
ArrayList <Node> nodesToRemove=null;
ArrayList <Edge> edgesToRemove=null;

boolean drawArc=true;
boolean drawHull=true;
boolean drawTitle=false;
boolean showCentroids=false;
boolean onlyIntersecting=true;
boolean movie=true;
boolean movieButActors=false;
boolean sizeRelevant=true;
boolean drawContour=false; //Now initially false :)
boolean drawZones=false;

boolean onlyConditions=false;
boolean onlyGenes=false;
	
GraphPoint2D vf=new GraphPoint2D();//Vector para la aplicaci�n de fuerzas en el layout

float memory; //Cantidad de memoria que nos queda

//Iterator it;

//improve conditions
float ratioError=0;
float antRatioError=0;
float totalImprove=0;
int numImproves=0;
final int maxCount=40;
int stopCount=maxCount;
LinkedList<Float> improveList=new LinkedList<Float>();
boolean drawErrors=false;
boolean drawDual=false;
boolean antialias=true;


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
static final int foregroundColor=10;
static final int nodeLabelBackgroundColor=11;
static final int hoverNodeLabelColor=12;


java.awt.Color[] paleta = new Color[10];
JTextField[] muestraColor = new JTextField[paleta.length];

boolean repaintingAll=false;
int drawCount=0;

public boolean sugiyama=false;

private Node rightButtonNode;

private boolean ctrlPressed;

private float sumOfForces;
private float antSumOfForces;

private int contStability=0;

private long totalTime;



/**
 * Default constructor
 * 
 */ 
public Overlapper()
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
threshold=0;

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

  
  //System.out.println("La caja en "+xTopOverviewBox+" siendo el tama�o "+screenWidth);
  
  xTopDrawingRect=((xTopMagnifier-xTopOverviewBox)/(overviewBoxLength))*areaInc*screenWidth;
  yTopDrawingRect=(yTopMagnifier/overviewBoxHeight)*areaInc*screenHeight;
 // System.out.println("x e y de pintar "+xTopDrawingRect+", "+yTopDrawingRect+" xOrig: "+(xTopMagnifier-xTopOverviewBox)+" areaInc "+areaInc+" screen");
  
	  
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
public synchronized void paintComponent(Graphics g)
	{
	long ts=System.currentTimeMillis();
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
		    if(antialias)	qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    else			qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		    qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		    //qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		    gr.setRenderingHints(qualityHints);
		    
		    Color bg = this.getBackground();
		    Color fg = this.getForeground();
		    gr.setColor(bg);
            gr.fillRect(0, 0, this.getWidth(), this.getHeight());
            gr.setColor(fg);
			
           
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
  	    iteracion++;
  	    
  	    if(showOverview)	  	    drawOverview();
	   
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

public void run()
{
int delay = 0; //milliseconds

ActionListener taskPerformerLayout = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
    	  long ts=System.currentTimeMillis();
    	  if(!pauseSimulation)
    	 	  	{
    		  	if(!sugiyama)	doLayout();
    		  	else			doSugiyamaLayout();
        	  	}
      }
  };
  
 	new Timer(delay, taskPerformerLayout).start();
	
	ActionListener taskPerformer = new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
	    	repaint();
	    	}
	};
 
new Timer(delay, taskPerformer).start();
}

/*
public void run()
{
	
int delay = 0; //milliseconds
totalTime=System.currentTimeMillis();

ActionListener taskPerformerLayout = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
    	//  long ts=System.currentTimeMillis();
    	  if(!pauseSimulation)
        	  	{
    		  	//if(!sugiyama)	
        	  		doLayout();
    		  	//else			doSugiyamaLayout();
        	  	}
      }
  };
  
 	new Timer(delay, taskPerformerLayout).start();
	
	ActionListener taskPerformer = new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
	    	if(pauseSimulation)
	    	 {
	    	 repaint();
	    	 }
	    }
	    
	};
 
new Timer(delay, taskPerformer).start();
  
}*/
/*
try{
	this.doLinLogLayout();
	ActionListener taskPerformer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			repaint();
		}
	};
new Timer(delay, taskPerformer).start();
	}catch(Exception e){e.printStackTrace();}
	*/

/**
 * Checks the improvement of the new iteration and stops the drawing if the layout has reached to a no-improvement behavior
 */
void checkImprovement()
	{
    if(!pauseSimulation)
	    {
	    if(cont<10)	cont++;
	    else
	    	{
	    	try{
	    	BufferedWriter bw=new BufferedWriter(new FileWriter("temp.txt",true));
	    	cont=1;
			float start=System.currentTimeMillis();
			//this.g.randomCorrection(10);
			//this.g.massCorrection(1);
			float end=System.currentTimeMillis();
			
			
		if(g.dualNodes!=null && g.dualNodes.size()>0)		ratioError=(float)this.g.getFailedPositionMetricDual();
		else												ratioError=(float)this.g.getFailedPositionMetric();
		double areaError=this.g.getContourAreaMetric();
		/*	if(antRatioError>0)		
				{
				float improve=(antRatioError-ratioError);
				numImproves++;

				if(improveList.size()>=maxCount)		improveList.removeFirst();
				improveList.addLast(new Float(improve));
				float meanImprove=0;
				for(int i=0;i<improveList.size();i++)	meanImprove+=improveList.get(i).floatValue();
				meanImprove/=improveList.size();
				
				System.out.println("Mejora actual "+improve);
				System.out.println("Mejora media ("+numImproves+"): "+meanImprove);
				if(meanImprove<=0.0001 && improve<=0.0001)	stopCount=0;
				if(stopCount==0)
	  				{
					System.out.println("Reached to the best solution: "+antRatioError+" "+ratioError+" -------------------------");
					//pauseSimulation=true;
					//this.g.fastHillClimberRelocation(5.0,0.1);
					pause();
					}
				}
		antRatioError=ratioError;
		*/
	  		if(!pauseSimulation)
  	  		{
  	    	//System.out.println(iteracion+": Tasa de error: "+ratioError+" y �rea media "+this.g.getAverageNormArea()+" t de correcci�n "+(end-start));
	  		System.out.println(iteracion+": RE "+ratioError+"\tAM "+this.g.getAverageNormArea()+"\tAR "+areaError);
	  	    bw.write(iteracion+"\t"+this.g.getAverageNormArea()+"\t"+areaError+"\t"+ratioError+"\t"+(end-start));
  	    	bw.newLine();
  	    	bw.close();
  	  		}
			}catch(Exception e){e.printStackTrace();}
	    	}
	    }
	}

void insertDetails()
	{
	Iterator<Node> itRel=g.getNodes().values().iterator();
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
	this.getGraph().updateSelection(bs);
	this.getGraph().updateLabels();
	//this.restoreClusters();
	//this.removeNonSelected(bs);
	}

/**
 * Updates this graph by showing only the biclusters that contains any of the 
 * genes or conditions selected.
 * @param	bs	BiclusterSelection to update the graph
 *
 */
public void updateGraph()
	{
	this.getGraph().updateLabels();
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
	Color bg=this.paleta[Overlapper.backgroundColor];
	Color fg=this.paleta[Overlapper.foregroundColor];
	fill(bg.getRed(), bg.getGreen(), bg.getBlue());
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
  
  g.draw(prioritiesOverview);
  
  setShowLabel(labels);
  setDrawPiecharts(arcs);
  setDrawClusterLabels(titles);
  
  popMatrix();
  
  stroke(fg.getRed(), fg.getGreen(), fg.getBlue());
//stroke(255);
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
/*
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
			Iterator<Edge> ie=edges.values().iterator();
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
			Iterator<Edge> ie=edges.values().iterator();
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
			Iterator<Edge> ie=edges.values().iterator();
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
			Iterator<Node> it2=g.getNodes().values().iterator();
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
			       		double f = -G*(a.getMass()*b.getMass()/(r*r));
			       	//	double f = -G/(r*r);
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
			 Iterator<Node> it2=g.getCenterNodes().values().iterator();
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
	*/
//Variaci�n del anterior.
//Ahora en est� mal hecho, ya que se aplican las fuerzas antes de asegurar que todas las fuerzas se le 
//han aplicado, pero quiero ver si eso afecta sustancialmente, ya que si no lo hace en una iteraci�n lo hace para la
//siguiente, ganamos tiempo (la ganancia es poco importante no obstante) y, sobre todo, tengo todo preparado para aplicar 
//m�tricas de manera particular
public synchronized void doLayout() 
	{
	if(computeDualLayout && g.dualNodes!=null && g.dualNodes.size()>0)	{doDualLayout(); return;}
	sumOfForces=0;
	Iterator<Node> it=g.getNodes().values().iterator();
	for (int i=0; i<g.getNodes().size(); i++) //N(N-1)/2 complexity
	  	{
		ForcedNode n=(ForcedNode)it.next();
			//------------------- spring force ------------------------------
			TreeMap<String,Edge> edges = g.getEdgesFrom(n);
			//Otra manera, siempre que hacemos el from, aplicamos a los to,
			//de modo que no hay que hacer el to para ninguno
			Iterator<Edge> ie=edges.values().iterator();
		    while(ie.hasNext())
		    	{
		    	SpringEdge e=(SpringEdge)ie.next();
		    	GraphPoint2D f = e.getForceFrom();
		    	n.applyForce(f);
		    	ForcedNode m=(ForcedNode)e.getTo();
		    	f.invert();
		    	m.applyForce(f);
		    	sumOfForces+=Math.abs(Math.sqrt(f.getX()*f.getX()+f.getY()*f.getY()));
		    	}
		    
		    //------------------- expansion force --------------------------------
		    Iterator<Node> it2=g.getNodes().values().iterator();
			for(int j=0;j<=i;j++)	it2.next();	//El mejor modo que he encontrado de aplicar el n(n-1)/2
			for (int j=i+1; j<g.getNodes().size() && it2.hasNext(); j++) 
				{
				ForcedNode b = (ForcedNode)it2.next();
				double dx = b.getX() - n.getX();
			    double dy = b.getY() - n.getY();
			   
			    if(dx!=0)
			    	{ //don't divide by zero.
				    
			    	double r = sqrt((float) (dx*dx + dy*dy));
			        
			    	if(r>0.1)//To avoid extremely high forces
			       		{
			       		double f = -G*(n.getMass()*b.getMass()/(r*r));
			       		
			       		vf.setX(dx*f);
			       		vf.setY(dy*f);
			       		n.applyForce(vf); 
				    	vf.invert();
				    	b.applyForce(vf);
				    	sumOfForces+=Math.abs(Math.sqrt(vf.getX()*vf.getX()+vf.getY()*vf.getY()));
					    }
			    	}
			    
			}
		applyLayout(n);
	   }//for (each node)
	//System.out.println("LinLog energy: "+g.getLinLogEnergy()+" minimum possible: "+g.getMinimalLinLogEnergy());
	//System.out.println("Sum of forces "+sumOfForces);
	/*System.out.print(".");
	if(antSumOfForces!=0)	
		{
		if(Math.abs(antSumOfForces-sumOfForces)<0.001)
			{
			contStability++;
			if(contStability>4)	
				{
				pause(); contStability=0; 
				System.out.println("Total time: "+ (System.currentTimeMillis()-totalTime)/1000.0+"\tForce: "+this.sumOfForces+" ("+this.antSumOfForces+")");
				}
			}
		else	contStability=0;
		}
	antSumOfForces=sumOfForces;*/
	}

private void applyLayout(ForcedNode n)
	{
	//-----------------------
	boolean zoomed=false;
	
	if (n != g.getDragNode() && !n.isFixed())	    
		{
    	while(n.getForce().getX()>scapeForce || n.getForce().getY()>scapeForce )	
    		n.setForce(nullVector);
    	if(n.getForce().getX()>scapeForce || n.getForce().getY()>scapeForce )	n.fix(true);
    	n.getPosition().add(n.getForce());
    	}
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
	n.setForce(nullVector);	//una vez cambiada la posici�n, estas fuerzas ya no act�an sobre el nodo
	if(g.dualNodes!=null && g.dualNodes.size()>0)	{g.refreshDualPositions();}
	}
	
//Variaci�n del anterior.
//Ahora en est� mal hecho, ya que se aplican las fuerzas antes de asegurar que todas las fuerzas se le 
//han aplicado, pero quiero ver si eso afecta sustancialmente, ya que si no lo hace en una iteraci�n lo hace para la
//siguiente, ganamos tiempo (la ganancia es poco importante no obstante) y, sobre todo, tengo todo preparado para aplicar 
//m�tricas de manera particular
public synchronized void doSugiyamaLayout() 
	{
	Iterator<GroupSet> itGraph=g.getResults().values().iterator();//Hull drawing
	
	for (int i=0; i<g.getResults().size(); i++) 
	  	{
	    GroupSet r = (GroupSet)itGraph.next();
	    for(int j=0;j<r.getClusters().size();j++)
	      	{
	    	SugiyamaCluster c=(SugiyamaCluster)r.getClusters().get(j);
	    	
	    	//---------------- SPRING FORCES S5, S7, S8
	    	//1) apply forces to the vertical and horizontal axes (S7, S8)
	    	SpringEdge e=null;
	    	GraphPoint2D f=null;
	    	e=(SpringEdge)c.getHorizontal();
	    	f = e.getSugiyamaForceS7();
	    	((ForcedNode)e.getFrom()).applyForce(f);
	    	f.invert();
	    	((ForcedNode)e.getTo()).applyForce(f);
	    	
	    	
	    	e=(SpringEdge)c.getVertical();
	    	f = e.getSugiyamaForceS8();
	    	((ForcedNode)e.getFrom()).applyForce(f);
	    	f.invert();
	    	((ForcedNode)e.getTo()).applyForce(f);
	    	
	    	//2) apply spring forces to the radial edges (S5)
	    	for(int k=0;k<c.getRadials().size();k++)
	    		{
	    		e=(SpringEdge)c.getRadials().get(k);
	    		f = e.getSugiyamaForceS5();
	    		((ForcedNode)e.getFrom()).applyForce(f);
		    	f.invert();
		    	((ForcedNode)e.getTo()).applyForce(f);
	    		}
	    	//2.5) and to the axial edges (between the center and the vertices (not specified, i use S5)
	    	for(int k=0;k<c.axials.size();k++)
	    		{
	    		e=(SpringEdge)c.axials.get(k);
	    		f = e.getSugiyamaForceS5();
	    		((ForcedNode)e.getFrom()).applyForce(f);
		    	f.invert();
		    	((ForcedNode)e.getTo()).applyForce(f);
	    		}
	    	
	    	//--------------- ATTRACTIVE FORCES (A1, A2)
	    	//3) apply attraction to the peripheral edges
	    	for(int k=0;k<c.getPeripherals().size();k++)
	    		{
	    		e=(SpringEdge)c.getPeripherals().get(k);
	    		f = e.getSugiyamaForceA1();
	    		((ForcedNode)e.getFrom()).applyForce(f);
		    	f.invert();
		    	((ForcedNode)e.getTo()).applyForce(f);
	    		}
	    
	    	//3.7) Repulsion between center nodes of non-overlapped clusters
	    	Iterator<GroupSet> itGraph2=g.getResults().values().iterator();//Hull drawing
	    	for (int k=0; k<g.getResults().size(); k++) 
		  	{
		    GroupSet r2 = (GroupSet)itGraph2.next();
		    for(int m=0;m<r2.getClusters().size();m++)
		      	{
		    	SugiyamaCluster c2=(SugiyamaCluster)r.getClusters().get(j);
		    	if(c2!=c && c2.nodesInCommon(c)==0)
		    		{
		    		double dx = c2.getCenter().getX() - c.getCenter().getX();
				    double dy = c2.getCenter().getY() - c.getCenter().getY();
				    double d = Math.sqrt((float) (dx*dx + dy*dy));
				    double lr=c.getNodes().size()*c2.getNodes().size();
				    if(d<lr)
			           	{
				    	double Cs=0.3*(3/g.getNodes().size());
					    double fr = -Cs*d;
			       		vf.setX(dx*fr);
			       		vf.setY(dy*fr);
			       		c.getCenter().applyForce(vf); 
				    	vf.invert();
				    	c2.getCenter().applyForce(vf);
			           	}
		    		}
	    		}
	      	}
	    	}
	  	}
	//4) Repulsive force among all nodes (normal and dummy centers) if unrelated
	//4a) Nodes in different groups
	Iterator<Node> it1=g.getNodes().values().iterator();
	for(int k=0;k<g.getNodes().size();k++)//for each node in the cluster
		{
		ForcedNode n=(ForcedNode)it1.next();
		Iterator<Node> it=g.getNodes().values().iterator();
		int cont=0;
		while(cont++<=k)	it.next();
		while(it.hasNext())	//for each node in the graph not already computed
    		{
    		ForcedNode n2=(ForcedNode)it.next();
    		//if(!n.mates.containsValue(n2))	//if not in the same group
    			{//apply repulsion allways
    			
    			double dx = n2.getX() - n.getX();
			    double dy = n2.getY() - n.getY();
			    double d = Math.sqrt((float) (dx*dx + dy*dy));
			    double lr=this.edgeLength;//+n.mates.size()+n2.mates.size();
			    if(d<lr)
		           	{
			    	double Cs=0.3*(3/g.getNodes().size());
				    double fr = -Cs*d;
		       		vf.setX(dx*fr);
		       		vf.setY(dy*fr);
		       		n.applyForce(vf); 
			    	vf.invert();
			    	n2.applyForce(vf);
		           	}
	        	}
    		}
		}
	//5) Relocate nodes upon the applied forces
	it1=g.getNodes().values().iterator();
	while(it1.hasNext())
		{
		applyLayout((ForcedNode)it1.next());
		}
	itGraph=g.getResults().values().iterator();//Hull drawing
	for (int i=0; i<g.getResults().size(); i++) 
		{
		GroupSet r = (GroupSet)itGraph.next();
		for(int j=0;j<r.getClusters().size();j++)
		  	{
			SugiyamaCluster c=(SugiyamaCluster)r.getClusters().get(j);
			applyLayout(c.getCenter());
			applyLayout(c.getTop());
			applyLayout(c.getBottom());
			applyLayout(c.getLeft());
			applyLayout(c.getRight());
		  	}
		}
	}

public synchronized void doLinLogLayout()
	{
	int numPop=50;
	int numParents=(int)(0.10*numPop);
	//int numMutant=(int)(0.8*numPop);//un 80% de los grafos mutan en 1 nodo (muy poco!)
	int numMutant=(int)(0.6*numPop*g.getNodes().size()*2);//un 40% de los nodos de todos los individuos de la poblaci�n mutan, en x o en y (el *2)
	int mutFactor=20;//la variaci�n es de un valor aleatorio en [-10,10] pixeles
	GraphGeneticAlgorithm gag=new GraphGeneticAlgorithm(numPop,numParents,numMutant,mutFactor,0,0,0,this.g);
	System.out.println("Error antes de GA "+g.getLinLogEnergy());
	gag.ejecutar(1000, g.getMinimalLinLogEnergy());
	System.out.println("Error tras GA "+g.getLinLogEnergy());
	//paintComponent(this.getGraphics());
	
	}
/**
 * As doLayout, but now applied to dual nodes and edges. As there are much less edges, spring and expansion constants must change
 */
public synchronized void doDualLayout()
	{	
	this.stiffness=1;
	sumOfForces=0;
	Iterator<DualNode> it=g.dualNodes.values().iterator();
	for (int i=0; i<g.dualNodes.size(); i++) //N(N-1)/2 complexity
	  	{
		DualNode n=it.next();
		ArrayList<Edge> ed=g.getDualEdgesFrom(n);
		for(int j=0;j<ed.size();j++)//----------------- spring force
			{
			SpringEdge e=(SpringEdge)ed.get(j);
			//GraphPoint2D f = e.getForceFrom();
			GraphPoint2D f = e.getDualForceFrom();
	    	n.applyForce(f);
	       	sumOfForces+=Math.abs(Math.sqrt(f.getX()*f.getX()+f.getY()*f.getY()));
			 
	    	}
		//System.out.println("Fuerza de spring para "+n.label+": "+n.getForce().getX()+", "+n.getForce().getY());
		Iterator<DualNode> it2=g.dualNodes.values().iterator();
		while(it2.hasNext())		
			{
			DualNode b=it2.next();
			
			if(n!=b)
				{
				double dx = b.getX() - n.getX(); //---------------------- expansion force
			    double dy = b.getY() - n.getY();
			   
			    if(dx!=0)					
			    	{ //don't divide by zero.
				    
			    	double r = sqrt((float) (dx*dx + dy*dy));
			        
			    	if(r>0.1)//To avoid extremely high forces
			       		{
			       		//double f = -G*(n.getMass()*b.getMass()/(r*r));
			    		double f = -G/(r*r);
				       		
			       		vf.setX(dx*f);
			       		vf.setY(dy*f);
			       		n.applyForce(vf); 
				    	vf.invert();
				    	b.applyForce(vf);
				       	sumOfForces+=Math.abs(Math.sqrt(vf.getX()*vf.getX()+vf.getY()*vf.getY()));
						 
			       		}
			    	}
			    	
				}
			}
		//System.out.println("Fuerza de repulsi�n para "+n.label+": "+n.getForce().getX()+", "+n.getForce().getY());
		
		boolean zoomed=false;
		
		if (n != g.getDragNode() && !n.isFixed())	    
			{
	    	while(Math.abs(n.getForce().getX())>scapeForce || Math.abs(n.getForce().getY())>scapeForce )	
	    		{n.setForce(nullVector);
	    		//pause();
	    		}
	    	if(Math.abs(n.getForce().getX())>scapeForce || Math.abs(n.getForce().getY())>scapeForce )	n.fix(true);
	    	n.getPosition().add(n.getForce());
	    //	System.out.println("Fuerza total aplicada a "+n.hashCode()+": "+n.getForce().getX()+", "+n.getForce().getY());
	    	n.positionSubNodes();
	    	}
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
    	n.setForce(nullVector);	//una vez cambiada la posici�n, estas fuerzas ya no act�an sobre el nodo
    	
	   }//for (each node)
	
	/*System.out.println("Sum of forces "+sumOfForces);
	if(antSumOfForces!=0)	
		{
		if(antSumOfForces-sumOfForces<0.001)
			{
			contStability++;
			if(contStability>4)	
				{
				pause(); contStability=0; 
				System.out.println("Total time: "+ (System.currentTimeMillis()-totalTime)/1000.0);
				}
			}
		else	contStability=0;
		}
	antSumOfForces=sumOfForces;*/
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
 //   println("Zooming to area "+areaInc);
    this.factor/=f;
    totalHeight*=f;
    totalWidth*=f;
    
    //Centramos
    Iterator<Node> itOver=g.getCenterNodes().values().iterator();
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
	switch(c){
	case '1':
		decreaseG();
		break;
	case '2':
		increaseG();
		break;
	case '3':
		decreaseStiffness();
		break;
	case 't':
		drawTitle=!drawTitle;
		break;
	case '4':
		//additionMode=!additionMode;
		increaseStiffness();
		break;	
	case '5':
		labelSize--;
		break;	
	case '6':
		labelSize++;;
		break;	
		
	case  '0'://redraw
		this.paint(gr);
		break;
		
	case 'a':
		drawArc=!drawArc;
		break;
	case 'n':
		drawNodes=!drawNodes;
		break;
	case 'h':
		drawHull=!drawHull;
		break;
		
	   case 'e':showEdges=!showEdges;
		         break;
		         
	   case 'd': 
		   		this.increaseEdgeLength();
	             break;
	    
	   case 's':  
		   		this.decreaseEdgeLength();
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
       case 'x':
    	   drawContour=!drawContour;
    	   break;
       case 'z':
    	   drawZones=!drawZones;
    	   break;

	   case 'v': showOverview=!showOverview;
			break;
	   case 'r': drawErrors=!drawErrors;
		break;
	   case 'u': 
		   drawDual=!drawDual;
		   break;
	   case 'j': computeDualLayout=!computeDualLayout;
		break;
	   case '.': increaseLabelClusterSize();
	   	break;
	   case ',': 
		   decreaseLabelClusterSize();
		   break;
	   case CODED:
		   switch(keyCode)
	   			{
	   			case UP:
	   				temp=priorities[0];
	   				for(int i=0;i<priorities.length-1;i++)
	   					priorities[i]=priorities[i+1];
	   				priorities[priorities.length-1]=temp;
	   				break;
	   			case DOWN:
	   				temp=priorities[priorities.length-1];
	   				for(int i=priorities.length-1;i>0;i--)
	   					priorities[i]=priorities[i-1];
	   				priorities[0]=temp;
	   				break;
	   			case LEFT:
	   				antialias=!antialias;
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
	threshold++;
	restoreClusters();
	removeByThreshold();
	println("Threshold: "+threshold);
	if(!wasPaused)	pause();
	
	}

/**
 * Decreases by one unit the overlap threshold. Biclusters with less connections (counting as connection
 * each node overlap with other bicluster) than this threshold will not be drawn
 *
 */
public void decreaseOverlapThreshold()
	{
	if(threshold>0)
		{
		threshold--;
		restoreClusters();
		removeByThreshold();
		}
	println("Threshold: "+threshold);	
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
 * Adds a value to the threshold that is in application
 * @param step real value to add to the threshold
 */
public void modifyThreshold(float step)
	{
	if(thresholdType==0)		
		{
		nodeThreshold+=step;
		if(nodeThreshold<0)		nodeThreshold=0;
		}
	else						
		{
		threshold+=step;
		if(threshold<0)	threshold=0;
		computeExcludedClusters();
		}
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
	//System.out.println("Incremento overview en un factor "+(zoomFactor+0.1)/zoomFactor);
	increaseZoom(0.1);
	//System.out.println("Factor de zooom "+zoomFactor);
	}

/**
 * Zooms out the visualization
 * TODO: Still in development
 *
 */
public void zoomOut()
	{
	//System.out.println("Incremento overview en un factor "+(zoomFactor-0.1)/zoomFactor);
	increaseZoom(-0.1);

	//System.out.println("Factor de zooom "+zoomFactor);
	}

private void increaseZoom(double f)
	{
	//float totalHeightAnt=this.totalHeight;
	//float totalWidthAnt=this.totalWidth;
	
	//----------nuevo
	totalHeight-=f*totalHeight;
	totalWidth-=f*totalWidth;
	//screenHeight-=f*screenHeight;
	//screenWidth-=f*screenWidth;
	zoomFactor+=f;
	
	//------------
	//float marcoAlto=(float)(totalHeight-totalHeightAnt)/2.0f;
    //float marcoAncho=(float)(totalWidth-totalWidthAnt)/2.0f;
    
    //Centramos
    /*Iterator<Node> itOver=g.getNodes().values().iterator();
    while(itOver.hasNext())
    	{
    	ForcedNode n=(ForcedNode)itOver.next();
    	n.setX((float)n.getX()+marcoAncho);
        n.setY((float)n.getY()+marcoAlto);
        }
    
    offsetX+=marcoAncho;
    offsetY+=marcoAlto;*/
	//System.out.println("Con zoom"+zoomFactor+", screen is "+getOffsetX()+", "+getOffsetY()+", "+getScreenHeight()+", "+getScreenWidth());
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
	if(labelSize>1)	labelSize--;
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
	//if(labelClusterSize>5)	
	labelClusterSize--;
	}


/**
 * This function is activated when the mouse is pressed, controling navigation through the graph and
 * node selection
 */
protected void mousePressed() {
  
	g.setDragNode(null);
	move=false;
	if((mouseX >= xTopMagnifier) && 
		   (mouseX <= xTopMagnifier + magnifierLength) &&
		   (mouseY >= yTopMagnifier ) && 
		   (mouseY <= yTopMagnifier + magnifierHeight)
		   )
  	{
	movingMagnifier = true;
	return;
  	}
  
  //S�lo funciona fuera del �rea de overview
  //lo pasamos al release
  if (mouseX < this.xTopOverviewBox || mouseY > this.yTopOverviewBox)
  	  {
		float xpress=(mouseX-offsetX)/zoomFactor;
		float ypress=(mouseY-offsetY)/zoomFactor;
	
	  for(Node n : g.getNodes().values())
	  	{
	    if (n.containsPoint(xpress, ypress)) 
		     {
			 if(n.isFixed() && mouseButton!=RIGHT)	n.fix(false);
			 g.setDragNode(n);
			 break;
			 }
   		}
	  if(g.getDragNode()==null)//Hemos pinchado fuera de un nodo
	  	{
		selectionArea = new ArrayList<Point2D.Double>();
		selectionArea.add(new Point2D.Double(xpress,ypress));
		}
   	}
}

/**
 * This function is called each time that the mouse is moved, to check node hovering,
 * thus highlighting neighbor nodes.
 */
protected void mouseMoved() {
   if (!(mouseX < this.xTopOverviewBox || mouseY > (this.yTopOverviewBox+this.overviewBoxHeight)) )	return;//Si estamos en el overview no miramos

  if (g.getDragNode() == null) 
  	{
    float xpress=(mouseX-offsetX)/zoomFactor;
	float ypress=(mouseY-offsetY)/zoomFactor;
	if(!this.drawDual)
		{
		Iterator<Node> itMM=g.getNodes().values().iterator();
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
	else	//hover, but for dual nodes
		{
		Iterator<DualNode> itMM=g.dualNodes.values().iterator();
		
		 for(int i=0; i<g.dualNodes.size(); i++) 
	    	{
			DualNode n = (DualNode)itMM.next();
		    if (n.containsPoint(xpress, ypress)) 
		      	{
		        g.setHoverNode(n);
		        break;
		      	}
	    	}
	    if(!itMM.hasNext())	g.setHoverNode(null);
		}
	g.getHoverClusters().clear();
    if(g.getHoverNode()==null && this.drawHull==true)//Search for hover hull
    	{
    	for(int i=0;i<=this.numClusters;i++)
    		{
    		Group mc=this.getClusterInPos(i);
    		if(mc!=null && mc.hull!=null && mc.hull.contains(xpress, ypress) && !excludedClusters.contains(mc.label))	
    				{
    				g.getHoverClusters().put(mc.label, mc);
    				}
    		}
    	if(g.dualNodes!=null && g.dualNodes.size()>0)
    		{
    		g.getHoverDualZones().clear();
    		Iterator<DualNode> itd=g.dualNodes.values().iterator();
    		while(itd.hasNext())
    			{
    			DualNode dn=itd.next();
    			if(dn.hull!=null && dn.hull.contains(xpress, ypress))
    				g.getHoverDualZones().put(dn.label,dn);
    			}
    		}
    	}
  }
}

public void receiveGOTerms(ArrayList<GOTerm> got)
	{
	
	}

public void receiveGeneAnnotations(ArrayList<GeneAnnotation> ga)
	{
	if(rightButtonNode!=null && ga.size()==1)
		{
		rightButtonNode.setDetails(ga.get(0).getDetailedForm());
		}
	}
/**
 * This function is called when the mouse is released, to stop dragging nodes.
 */
protected void mouseReleased() {
  
  movingMagnifier = false;
  boolean nodeSelected=false;
	
  if(g.getDragNode()!=null)	//--------drag of nodes
	  {
	  if(keyPressed)	
		  {
		  g.getDragNode().fix(true);
		  }
	  g.setDragNode(null);
	  }
  else
  	{
	if(selectionArea!=null)	//--------area selection
		{
		g.clearSelectedNodes();
		g.getSelectedClusters().clear();
		Iterator<Node> it=g.getNodes().values().iterator();
		while(it.hasNext())
			{
			Node n=it.next();
			
			Polygon pol=new Polygon();
			for(Point2D.Double p: selectionArea)	pol.addPoint((int)p.x, (int)p.y);
			if(pol.contains(n.getX(), n.getY()))
				{
				g.addSelectedNode(n);
				nodeSelected=true;
				}
			}
		selectionArea=null;
		}
  	}

  if (!move && (mouseX < this.xTopOverviewBox || mouseY > (this.yTopOverviewBox+this.overviewBoxHeight)) )
	 {
	 float xpress=(mouseX-offsetX)/zoomFactor;
	 float ypress=(mouseY-offsetY)/zoomFactor;
	 if(!drawDual)
		 {
		 Iterator<Node> itMouse=g.getNodes().values().iterator();
		
		   
		  //----------- LEFT BUTTON
		  if(mouseButton==LEFT)
			  {
			  while(itMouse.hasNext())	//--------single node selection
		 		{
				 Node n=(Node)itMouse.next();
				 if (n.containsPoint(xpress, ypress)) 
				     {
					 if(n.isFixed() && mouseButton!=RIGHT)	n.fix(false);
					 nodeSelected=true;	
					 if(!keyPressed)	
						{
						g.clearSelectedNodes();
						g.getSelectedClusters().clear();
						}
					 if(n!=g.getDragNode())	g.addSelectedNode(n);
					 }
		 		}
			  if(!nodeSelected && this.drawHull)	//---------- group selection
			  	{
				for(int i=0;i<this.numClusters;i++)
			  		{
			  		Group mc=this.getClusterInPos(i);
			  		if(mc!=null && mc.hull!=null && mc.hull.contains(xpress, ypress) && !excludedClusters.contains(mc.label))
			  			{
			  			if(!keyPressed)	
							{
							g.clearSelectedNodes();
							g.getSelectedClusters().clear();
							}
						
			  			g.getSelectedClusters().put(mc.label, mc);
			  			for(int j=0;j<mc.getNodes().size();j++)
			  				g.addSelectedNode(mc.getNode(j));	
			  			}
			  		}
			  	}
			  }
		  //-------- RIGHT BUTTON----------------> Search for details
		  else if(mouseButton==RIGHT)
		  	{
			 rightButtonNode=null;
			 while(itMouse.hasNext())
		 		{
				 Node n=(Node)itMouse.next();
				 if (n.containsPoint(xpress, ypress))	
					 {
					 rightButtonNode=n;
					 break;
					 }
		 		}
			 if(rightButtonNode==null)	return;
			 if(rightButtonNode.details.length() > 0)	rightButtonNode.details="";
			 else
				 {
				 if(this.getMicroarrayData()==null)	{System.err.println("Error: Microarray data are not loaded, impossible to retrieve additional info about genes"); return;}
				 if(rightButtonNode.isGene())
				 	{
					 GeneAnnotation ga=this.getMicroarrayData().geneAnnotations.get(rightButtonNode.id);
					 if(ga!=null && ga.goTerms!=null)
					 	{
						rightButtonNode.setDetails(ga.getDetailedForm());
					 	}
					 else
					 	{	 
						rightButtonNode.details="searching...";
						//this.getMicroarrayData().getGeneAnnotation(rightButtonNode.labelId, this, null, true);
						this.getMicroarrayData().getGeneAnnotations(new int[]{this.microarrayData.getGeneId(rightButtonNode.labelId)},this, true, null, null, true);
					 	}
				 	}
				 else
				 	{
					rightButtonNode.setDetails(this.getMicroarrayData().getDetailedSampleForm(rightButtonNode.id));
					System.out.println(rightButtonNode.details);
				 	}
				 }
		  	}//end if(rigth button)
		 }//if !drawDual
	  else
	 	{
		System.out.println("Selection of dual node"); 
		Iterator<DualNode> itMM=g.dualNodes.values().iterator();
		
		 for(int i=0; i<g.dualNodes.size(); i++) 
	    	{
			DualNode n = (DualNode)itMM.next();
		    if (n.containsPoint(xpress, ypress)) 
		      	{
		    	g.clearSelectedNodes();
		    	g.clearSelectedDualNodes();
		    	g.getSelectedClusters().clear();
		    	
		    	g.addSelectedDualNode(n);
				g.addSelectedNodes(n.subNodes);	
	  		    break;
		      	}
	    	}
	    if(!itMM.hasNext())	g.setHoverNode(null);
	 	}
	  }//if !move
	  move=false;
}


/**
 * This function is called when the mouse is dragging moving the selected node (if any) or
 * panning through the overall view
 */
protected void mouseDragged() {
	boolean navigate=false;
	 if (showOverview)
		{
		if((mouseX > xTopOverviewBox) && 
		   (mouseX < xTopOverviewBox + overviewBoxLength) &&
		   (mouseY > yTopOverviewBox) && 
		   (mouseY < yTopOverviewBox + overviewBoxHeight)
		   )
			{
			if(movingMagnifier)
				{
				navigate=true;
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
	 if(!navigate)
	 	{
		move=true;
		float xpress=(mouseX-offsetX)/zoomFactor;
		float ypress=(mouseY-offsetY)/zoomFactor;
		if(g.getDragNode()!=null)
			{
			 g.getDragNode().setX(xpress);
			 g.getDragNode().setY(ypress);
			}
		else	//Selecci�n de �rea
			{
			selectionArea.add(new Point2D.Double(xpress, ypress));
			}
	 	}
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
	
	if(text.length()>0)
		{
		g.clearSearchNodes();
		if(!searchInGroups)		//person search
			{
			for(Node n : g.getNodes().values())
				{
				if(n.label.contains(text) || n.labelId.contains(text))
			//	if(n.getLabel().contains(text))
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
			num=0;
			for(GroupSet r: g.getResults().values())
				{
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
	Iterator<ArrayList<String>> it1=clusters.values().iterator();
	for(int i=0;i<numClusters-1;i++)
		{
		ArrayList<String> a=(ArrayList<String>)it1.next();
		Iterator<ArrayList<String>> it3=clusters.values().iterator();
		for(int j=0;j<=i;j++)	it3.next();
		for(int j=i+1;j<numClusters;j++)
			sm[i][j]=sm[j][i]=ArrayUtils.intersect(a,it3.next());
		}
	return sm;
	}

private void readClusters()
	{
	numClusters=numClusters();
	String data[] = loadStrings(getDataFile());
	clusters= new TreeMap<String,ArrayList<String>>();
	groupNames=new String[numClusters];
	int cont=0;
	
	if(groupDelimiter==null)
		{
		String [] dataToken = data[0].split(delimiter);
		if (dataToken.length > 1)		 exit();
		
		for (int l = headerLines; l < data.length; l++)
		  	{
			dataToken = data[l].split(delimiter);  
			if (dataToken.length >1)
				{
				ArrayList<String> lista=new ArrayList<String>(dataToken.length);
				for(int i=0;i<dataToken.length;i++)
					lista.add(dataToken[i]);
				groupNames[cont]="cluster"+cont;
				clusters.put("cluster"+cont, lista);
				cont++;
				}
		  	}
		}
	else
		{
		String [] prevDataToken = null;//For nested groups
		String [] dataToken = data[0].split(delimiter);
		int mainGroup=0;
		int hierarchy=0;
		
		if(this.posDelimiter!=null)
			{
			this.initPos=new Point2D.Double[numClusters];
			}
		if (dataToken.length > 1)		 exit();
		else if(dataToken[0].split(groupDelimiter).length > 1)	exit();
			
		for (int l = headerLines; l < data.length; l++)
		  	{
			if(l>headerLines)	prevDataToken=dataToken;
			if(data[l].startsWith("\t"))
				{
				hierarchy=data[l].split("\t").length-1;
				System.out.println("Empieza con tab");
				}
			else
				{
				mainGroup=cont;
				hierarchy=0;
				}
			dataToken = data[l].split(groupDelimiter);  
			if (dataToken.length ==2 )//First field has group name and field two elements
				{
				String groupName=dataToken[0];
				String[] elements=dataToken[1].split(delimiter);
				ArrayList<String> lista=new ArrayList<String>(elements.length);
				for(int i=0;i<elements.length;i++)
					{
					String element=null;
					
					if(posDelimiter!=null)
						{
						String[] elementToken=elements[i].split(this.posDelimiter);
						if(elementToken.length==3)
							{
							element=elementToken[0];
							initPos[cont]=new Point2D.Double();
							initPos[cont].x=new Double(elementToken[1]).doubleValue();
							initPos[cont].y=new Double(elementToken[2]).doubleValue();
							}
						else
							System.err.println("Format Error: there are no three fields for each element");
						}
					else	element=elements[i];
					if(hierarchy>0)	
						{
						for(int j=0;j<hierarchy;j++)
							((ArrayList<String>)clusters.get(groupNames[mainGroup+j])).add(elements[i]);
						}
					lista.add(elements[i]);
					}
				groupNames[cont]=groupName;
				clusters.put(groupName, lista);
				cont++;
				}
			}
		}
	return;
	}


/** Read of clusters from a BicAT format, this is:
	number_of_biclusters
	nameClusterSet
	numRows	numCols
	row1	row2	row3	...	rowN
	col1	col2	col3	...	colM
 * 
 */
private void readClustersBicat()
	{
	String data[] = loadStrings(getDataFile());
	
	String [] dataToken = data[0].split(delimiter);
	if (dataToken.length > 1)		 exit();
	else	groupNames=new String[new Integer(dataToken[0]).intValue()];

	clusters=null;
	clusters= new TreeMap<String,ArrayList<String>>();
	int cont=0;
	geneNames=new ArrayList<String>();
	conditionNames=new ArrayList<String>();
	
	for (int l = 2; l < data.length; l++)
	  	{
		dataToken = data[l].split(delimiter);//Filas
		String groupName=""+cont;
		if(groupDelimiter!=null && data[l].contains(groupDelimiter))
			groupName=dataToken[0].split(groupDelimiter)[0];
		
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
				}
			if(lista.size()>0)
				{
				groupNames[cont]=groupName;
				clusters.put(groupNames[cont], lista);
				this.numClusters++;
				cont++;
				}
			}
		}
	}



/**
 * Count the number of clusters in the visualization
 * @return the number of clusters
 */
public int numClusters()
	{
	int n=0;
	String data[] = loadStrings(getDataFile());
	if(groupDelimiter==null)
		{
		String [] dataToken = data[0].split(delimiter);
		if (dataToken.length > 1)		 exit();
			
		for (int l = 1; l < data.length; l++)
		  	{
			dataToken = data[l].split(delimiter);  
			if (dataToken.length > 1)		n++;
			}
		}
	else
		{
		String [] dataToken = data[0].split(delimiter);
		if (dataToken.length > 1)		 exit();
		else if(dataToken[0].split(groupDelimiter).length > 1)	exit();
			
		for (int l = 1; l < data.length; l++)
		  	{
			dataToken = data[l].split(groupDelimiter);  
			if (dataToken.length > 1)		n++;
			}
		}
	return n;
	}

/**
 * Adds just one resultSet with all the clusters
 */
 void readResultSet()
	{
	int n=0;
	resultSets=new ArrayList<Integer>();
	resultLabels=new ArrayList<String>();
	String data[] = loadStrings(getDataFile());
	
	if(groupDelimiter==null)	//In the case there's no group delimiter, groups of 1 element are not allowed, lines with just one element are treated as separators of sets of groups
		{
		String [] dataToken = data[0].split(delimiter);
		if (dataToken.length > 1)		 exit();
		resultLabels.add(data[1]);
			
		for (int l = 2; l < data.length; l++)
		  	{
			dataToken = data[l].split(delimiter);  
			if (dataToken.length == 1)		
				{
				resultLabels.add(dataToken[0]);
				if(n!=0)	resultSets.add(n); 
				n=0;
				}
			if (dataToken.length > 1)		n++;
			}
		resultSets.add(n);
		}
	else
		{
		String [] dataToken = data[0].split(delimiter);
		if (dataToken.length > 1)		 exit();
		else if(dataToken[0].split(groupDelimiter).length > 1)	exit();
			
		for (int l = 1; l < data.length; l++)
		  	{
			dataToken = data[l].split(groupDelimiter);  
			if (dataToken.length == 1)		
				{
				resultLabels.add(dataToken[0]);
				if(n!=0)	resultSets.add(n); 
				n=0;
				}
			if (dataToken.length > 1)		n++;
			}
		resultSets.add(n);
		}
	}

private void readResultSetsBicat()
{
int n=0;
resultSets=new ArrayList<Integer>();
resultLabels=new ArrayList<String>();
String data[] = loadStrings(getDataFile());

String [] dataToken = data[0].split(delimiter);//This first one has the number of biclusters
if (dataToken.length > 1)		 exit();
boolean skipNext=false;	
for (int l = 1; l < data.length; l++)
  	{
	dataToken = data[l].split(delimiter);  
	if (!skipNext && (data[l].length()>0 && dataToken.length == 1))		
		{
		resultLabels.add(dataToken[0]);
		if(n!=0)	
			{
			resultSets.add(n/3); 
			}
		n=0;
		}
	if (data[l].length()==0 || dataToken.length > 0)		n++;//==0 for the case of no-conditions or no-genes
	if(dataToken[0].equals("1") || dataToken[0].endsWith(this.groupDelimiter+" 1"))	
		skipNext=true; //Next line with just one element is because of a group of only one element, not because of a change in set.
	else	
		skipNext=false;
	}
resultSets.add(n/3);
}


private void readResultSets(String[] movies)
	{
	resultSets=new ArrayList<Integer>();
	resultLabels=new ArrayList<String>();
	resultLabels.add("ResultSet");
	if(movies!=null)	resultSets.add(movies.length);
	}


/**
 * Reads a file with groups entered with the following structure:
 * element1 element2 ... elementN
 * It�s equivalent to call to readFile(" ", null)
 * 
 * @param groupDelimiter
 * @param delimiter
 */
void readFile()
	{
	readResultSet();
	readClusters();
	}
 /**
  * Reads a file with groups entered with the following structure:
  * groupName|groupDelimiter|element1|delimiter|element2|delimiter|...|elementN
  * 
  * That is, if groupDelimiter is ":" and delimiter is ", " then
  * 2:alternative, alternative rock, alternative metal
  * could be an entry of the file
  * @param groupDelimiter
  * @param delimiter
  */
 void readFile(String groupDelimiter, String delimiter, String posDelimiter, int headerLines)
 	{
	this.delimiter=delimiter;
	this.groupDelimiter=groupDelimiter;
	this.headerLines=headerLines;
	this.posDelimiter=posDelimiter;
	readFile();
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
	  removeByThreshold();
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
  removeByThreshold();
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

	GraphPoint2D randomPos = new GraphPoint2D(x,y);
	
	for (int i = 0; i < c.getNodes().size(); i++) //Para cada nodo del cluster
	   {
	   ForcedNode n=c.getNode(i);
	   String nodeLabel = n.getLabel();
	   
	   
	   if (!list.contains(nodeLabel))
	   		{
		    double newX=randomPos.getX()-cellWidth/2+random((float)cellWidth);
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
	Iterator<GroupSet> itOrder=g.getResults().values().iterator();
	int cont=0;
	
	//Put order in cluster objects
	while(itOrder.hasNext())
		{
		GroupSet r=itOrder.next();
		ArrayList<Group> clusters1=r.getClusters();
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
		GroupSet r=(GroupSet)itOrder.next();
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

Group getClusterInPos(int pos)
	{
	//System.out.println("GetCluster in pos "+pos);
	for(int i=0;i<resultSets.size();i++)
		{
		int numRS=((Integer)(resultSets.get(i))).intValue();
		if(pos>=numRS)	
			pos-=numRS;
		else			
			{
			Map<String,GroupSet> m=g.getResults();
			GroupSet r=m.get(resultLabels.get(i));
			try{return r.getClusters().get(pos);
			}catch(Exception e){return null;}
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
		GroupSet r=(GroupSet)itClus.next();
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
void removeByThreshold()
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
		System.out.println(i+" tiene "+list[i]);
		if(list[i]<threshold)
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
			if(threshold>0)
				{//Tenemos que comprobar que no quitamos un nodo que est� en otros clusters
				//TODO: No s� si es mejor dejar el nodo con la informaci�n de que pertenece a varios clusters o quitarla tambi�n
				Iterator itRNO=g.getResults().values().iterator();
				while(itRNO.hasNext())
					{
					GroupSet r=(GroupSet)itRNO.next();
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
	if(bs.getConditions().size()>=microarrayData.getNumConditions()-1)	b=microarrayData.getNames(bs.getGenes(), new LinkedList<Integer>());	
	else														b=microarrayData.getNames(bs.getGenes(), bs.getConditions());
	for(int i=0;i<numClusters;i++)
		{
		ArrayList<String> a=clusters.get("cluster"+i);
		list[i]=ArrayUtils.intersect(a, b);
		}
	return list;
	}
/**
 * Deletes all clusters that haven't any of the genes or conditions in BiclusterSelection
 *
 */
void removeNonSelected(BiclusterSelection bs)
	{
	//System.out.println("REMOVE non Selected -----------------------------");
	//System.out.println("N� de clusters "+clusters.size());
	int[] list=selectionList(bs);
	//System.out.println("Tenemos una lista de "+list.length);
	
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
	System.out.println("Clusters to hide: "+clustersToRemove.size());
	
	//Para cada uno de ellos, eliminamos sus nodos s�lo si no 
	//est�n en ning�n bicluster que est� seleccionado
	//los eliminamos de la lista clusters y disminuimos
	//el n�mero de clusters del resultSet.

	for(int i=0;i<clustersToRemove.size();i++)//Para cada cluster a quitar
		{
		
		int pos=(Integer)(posToRem.get(i)).intValue();
		MaximalCluster c=(MaximalCluster)clustersToRemove.get(i);
		
		ArrayList lista=c.getNodes();
		for(int k=0;k<lista.size();k++)//Para cada nodo del cluster
			{
			Node nr=(Node)lista.get(k);
			Map<String, Group> map=nr.clusters;//cogemos los clusters en los que est�
			boolean toRemove=true;
			for(int j=0;j<list.length;j++)
				{
				if(list[j]>0)	//si para alguno de los clusters que se queda
					{
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
	}


/**
 * Restore all groups to the visualization
 *
 */
void restoreClusters()
	{
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
	    		ret.setNaturalLength(ret.getNaturalLength()*0.8);
	    		}
	    	else			
	    		{
	    		g.addEdge(e);//Se encarga de ponerlo en el from y en el to.
	    		}
			}
	
	this.numClusters=clusters.size();//TODO: deprecate numClusters por clusters.size()
	return;
	}



/**
 * Builds a graph according to dataFile information in the Session layer
 */
public void buildGraph()
	{
	totalTime=System.currentTimeMillis();
	//Preliminary reading of biclusters and ordering
	groupDelimiter=":";
	delimiter="\t";
	readResultSetsBicat();
	readClustersBicat();
	
	//readFile(null, " ", null, 2);//Yaxi's format
	//readFile(null, ", ", null, 2);//Yaxi's format
	//readFile(":", ", ", "|",2);//Yaxi's formatdf
	
	if(initialOrdering && radial)	g=buildOrderedRadialGraph();
	else
		{
		if(initialOrdering)			g=buildOrderedCompleteGraph();
		else
			{
			if(radial)		g=buildRadialGraph();
			else			g=buildCompleteGraph();
			//else			g=buildSugiyamaGraph();
			//this.sugiyama=true;
			}
		}
	
	insertDetails();
	Runtime.getRuntime().gc();//Al final del constructor quiz�s sea buena idea
	g.buildCompleteDualGraph();
	//g.buildDualGraph();
	g.computeMaxZones();
	System.out.println("Graph with "+this.g.getNodes().size()+" nodes, "+this.clusters.size()+" biclusters, "+this.getGraph().getEdges().size()+" edges, "+this.getGraph().getConnectionDegree()+" degree, "+this.getAvgSize()+" avg size "+this.g.getBiclusterDegree()+" avg # of biclusters for each node");
	//System.out.println("Average number of nodes overlapped for each bicluster: "+this.g.getAvgBiclusterOverlap());
	if(g.dualNodes!=null)	System.out.println("Dual graph with "+this.g.dualNodes.size()+" nodes and "+this.g.dualEdges.size()+" edges");
	
	}

/**
 * Test to check the time performance of the algorithm (comment the run code and call at the end
 * of the buildGraph() method.
 * @param numTimes
 */
public void timeTest(int numTimes)
	{
	double total=0;
	for(int i=0;i<numTimes;i++)
		{
		buildGraph();
		double time=System.currentTimeMillis();
		int cont=0;
		while(!pauseSimulation && cont<3000)	{doLayout();	cont++;}
		sumOfForces=this.antSumOfForces=0;
		double took=(System.currentTimeMillis()-time)/1000.0;
		System.out.println("Time for running "+i+" is "+took+" and it took "+cont+" iterations");
		pauseSimulation=false;
		total+=took;
		//g.draw(this.priorities);
		}
	System.out.println("Average is"+total/numTimes);
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
	  GroupSet r = null;
	  MaximalCluster c = null;
	  g = new Graph(this);
	  
	  //Uniform distribution of clusters
	  double cellWidth, cellHeight;//Screen initial areas for each cluster
	  cellWidth=screenWidth/Math.sqrt(numClusters);
	  cellHeight=screenHeight/Math.sqrt(numClusters);
	  double x,y;
	  
	  x=screenWidth*(areaInc-1)*0.75-cellWidth/2;
	  y=screenHeight*(areaInc-1)*0.75+cellHeight/2;
	  
	  //El contador de nodos usados en lugar de filas por columnas 
	  nNodes = 0;
	  int color = Overlapper.bicColor1;
	  int clusterCount=0;

		
	  for(int i=0;i<resultSets.size();i++)//Para cada result set
	  	{
		//New ResultSet
		r = new GroupSet();
		r.setLabel((String)resultLabels.get(i));
		
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
			c.setLabel(groupNames[clusterCount]);
			
			if(groupNames!=null)	
				c.setLabel(groupNames[clusterCount]);
			for (int k = 0; k < clusters.get(groupNames[clusterCount]).size(); k++) //Para cada nodo en el cluster
				{
			   String nodeLabel =(String)clusters.get(groupNames[clusterCount]).get(k);
			   ForcedNode n;
			
			   if(!alreadyInGraph(g,nodeLabel))
			   	  {
				  n = new ForcedNode(new GraphPoint2D(x -cellWidth/2+random((float)cellWidth),y-cellHeight/2+random((float)cellHeight)));
				  n.setLabel(nodeLabel);
				  n.labelId=nodeLabel;
				  if(conditionNames!=null && conditionNames.contains(nodeLabel))	n.setGene(false);
				  else										n.setGene(true);
				  if(microarrayData!=null)	
					  {
					  if(n.isGene())	n.id=microarrayData.getGeneId(n.label);
					  else				n.id=microarrayData.getConditionId(n.label);
					  }
				  n.setMass(1.0);
				  n.setSize(nodeSize);
				  g.addNode(n);
				  nNodes++;
				  }
			   else	       n=(ForcedNode)g.getNodes().get(nodeLabel);
		        
			  c.addNode(n);
			  }//each node in cluster
			//TODO: determine if it has exactly the same elements that any other group to displace group labels
			displaceLabel(c, g);
			c.notifyNodesInCluster();
			r.addCluster(c);
		    clusterCount++;
			}//each cluster in resultSet
	    // And the last ResultSet must also be added to the graph
	    g.addClusterSet(r);
	  	}//each resultSet
	  
	  println("Number of nodes: "+g.getNumNodes()+", central nodes: "+g.getNumCenterNodes()+", edges: "+g.getNumEdges());
	  
	  return g;
	}

private void displaceLabel(MaximalCluster mc, Graph graph)
	{
	if(graph==null || graph.getResults()==null) return;
	Point2D.Double p=mc.getMiddlePoint();
	Iterator<GroupSet> itGraph=graph.getResults().values().iterator();//Hull drawing
	
	for (int i=0; i<graph.getResults().size(); i++) 
	  	{
	    GroupSet r = (GroupSet)itGraph.next();
	    for(int j=0;j<r.getClusters().size();j++)
	      	{
	    	MaximalCluster c=(MaximalCluster)r.getClusters().get(j);
	    	if(c.getMiddlePoint().distance(p)<=0.1)
	    		{
	    		System.out.println("Displacement! "+c.label);
	    		mc.labelDisplacement++;
	    		}
	      	}
	  	}
	}

/**
 * M�todo para crear un caso de resultados de biclustering
 * @return	
 */
Graph buildSugiyamaGraph() 
	{
	  Graph g;
	  GroupSet r = null;
	  SugiyamaCluster c = null;
	  g = new Graph(this);
	  
	  //Uniform distribution of clusters
	  double cellWidth, cellHeight;//Screen initial areas for each cluster
	  cellWidth=screenWidth/Math.sqrt(numClusters);
	  cellHeight=screenHeight/Math.sqrt(numClusters);
	  double x,y;
	  x=-cellWidth/2;
	  y=cellHeight/2;
	  
	  //El contador de nodos usados en lugar de filas por columnas 
	  nNodes = 0;
	  int color = Overlapper.bicColor1;
	  int clusterCount=0;

		
	  for(int i=0;i<resultSets.size();i++)//Para cada result set
	  	{
		//New ClusterSet
		r = new GroupSet();
		r.setLabel((String)resultLabels.get(i));
		
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
			
			c = new SugiyamaCluster(r, "cluster"+new Integer(clusterCount).toString());
			c.setLabel("cluster"+clusterCount);
			if(groupNames!=null)	
				c.setLabel(groupNames[clusterCount]);
			for (int k = 0; k < clusters.get("cluster"+clusterCount).size(); k++) //Para cada nodo en el cluster
			   {
			   String nodeLabel =(String)clusters.get("cluster"+clusterCount).get(k);
			   ForcedNode n;
			   if(!alreadyInGraph(g,nodeLabel))
			   	  {
				  n = new ForcedNode(new GraphPoint2D(x -cellWidth/2+random((float)cellWidth),y-cellHeight/2+random((float)cellHeight)));
				  n.setLabel(nodeLabel);
				  n.setGene(true);
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
	  
	  g.buildSugiyamaStructure();
	  
	  println("Number of nodes: "+g.getNumNodes()+", central nodes: "+g.getNumCenterNodes()+", edges: "+g.getNumEdges());
	  
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
	return screenHeight;
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
	return screenWidth;
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
	int color=Overlapper.bicColor1;
	if(g!=null)
		{
		for(int i=0;i<g.getResults().size();i++)
			{
			GroupSet cs=g.getResults().get(resultLabels.get(i));
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
 * Returns true if misplacement errors are being drawn
 * @return true if misplacement errors are being drawn	
 */
boolean isDrawErrors() {
	return drawErrors;
}

/**
 * Sets the drawing or hiding of misplacement errors
 * @param drawErrors	if true, misplacement errors will be drawn
 */
void setDrawErrors(boolean drawErrors) {
	this.drawErrors = drawErrors;
}

/**
 * Returns true if dual nodes are being drawn
 * @return true if dual nodes are being drawn	
 */
boolean isDrawDual() {
	return drawDual;
}

/**
 * Sets the drawing or hiding of equivalent dual graph
 * @param drawDual	if true, equivalent dual graph will be drawn
 */
void setDrawDual(boolean drawD) {
	this.drawDual = drawD;
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
 * Sets the threshold used to display biclusters or nodes. By now three kinds of threshold can be set, by degree of overlap (0),
 * size of the biclusters (1) or constance of the biclusters (2). Degree of overlap is computed as the number of biclusters where
 * a node (gene or condition) is in. Size is computed as the number of genes and conditions in the bicluster and constance as the 
 * standard deviation from the mean in the bicluster (this one only works if Microarray expression levels are loaded)
 * @param type - type of threshold
 * @param value - value of the threshold (possitive)
 */
public void setThreshold(int type, float value)
	{
	
	this.thresholdType=type;
	threshold=value;
	if(this.getMicroarrayData()!=null)
		System.out.println("Tenemos MD");
	else	System.out.println("No Tenemos MD");
	if(type!=0)		//Bicluster-oriented threshold
		{
		nodeThreshold=0;
		computeExcludedClusters();
		}
	else		//Node-oriented threshold
		{
		this.excludedClusters.clear();
		nodeThreshold=(int)(threshold);
		}
	
	
	//removeByThreshold();
	}

/**
 * Returns the current value of the threshold
 * @return numeric threshold
 */
public float getThresholdValue()
	{
	if(thresholdType==0)		return nodeThreshold;
	else						return threshold;
	}

/**
 * Returns the step applied to decrement or increment the threshold
 * @return numeric step
 */
public float getStep()
	{
	return step;
	}

/**
 * Sets the step applied to decrement or increment the threshold (default 1.0)
 * @param v - new step (if negative or zero, step is not changed)
 */
public void setStep(float v)
	{
	if(v>0)	step=v;
	}

private void computeExcludedClusters()
	{
	excludedClusters=new ArrayList<String>();
	for(int i=0;i<numClusters;i++)
		{
		try{
		Group c=getClusterWithLabel("cluster"+i);
		switch(thresholdType)
			{
			case 0: //overlap
				System.out.println("Nothing to do");
				break;
			case 1://size
				if(c.getNodes().size()<threshold)
					excludedClusters.add(c.label);
				break;
			case 2: //constance
				ArrayList<String>genes=new ArrayList<String>();
				ArrayList<String>conditions=new ArrayList<String>();
				for(int j=0;j<c.getNodes().size();j++)
					{
					ForcedNode n=c.getNode(j);
					if(n.isGene())	genes.add(n.label);
					else			conditions.add(n.label);
					}
				float constance=microarrayData.getConstance(genes, conditions, 2);
				if(constance>threshold)
					{
					excludedClusters.add(c.label);
					//System.out.println("Quitamos "+c.label+" poque tiene constancia "+constance);
					}
				else	System.out.println("Dejamos "+c.label+" poque tiene constancia "+constance);
				break;
			}
		}catch(Exception e){
			e.printStackTrace();}
		}
	//Update cluster information in nodes
	Iterator<Node> it=g.getNodes().values().iterator();
	while(it.hasNext())
		{
		Node n=it.next();
		n.shownClusters.clear();
		n.shownClusters.putAll(n.clusters);
		for(int i=0;i<excludedClusters.size();i++)
			{
			String l=(String)excludedClusters.get(i);
			if(n.shownClusters.containsKey(l))	n.shownClusters.remove(l);
			}
		}
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

/**
 * Returns the average size of the biclusters being represented
 * @return the average size of the biclusters being represented
 */
public float getAvgSize()
	{
	float ret=0;
	Iterator<ArrayList<String>> it=this.clusters.values().iterator();
	
	while(it.hasNext())
		{
		ArrayList<String> lista=it.next();
		ret+=lista.size();
		}
	return ret/this.clusters.size();
	}
public boolean isDrawTopography() {
	return drawTopography;
}
public void setDrawTopography(boolean drawTopography) {
	this.drawTopography = drawTopography;
}
public boolean isDrawExactGroups() {
	return drawExactGroups;
}
public void setDrawExactGroups(boolean drawExactGroups) {
	this.drawExactGroups = drawExactGroups;
}
public boolean isComputeDualLayout() {
	return computeDualLayout;
}
public void setComputeDualLayout(boolean computeDualLayout) {
	this.computeDualLayout = computeDualLayout;
}
public boolean isDrawContour() {
	return drawContour;
}
public void setDrawContour(boolean drawContour) {
	this.drawContour = drawContour;
}
public boolean isDrawZones() {
	return drawZones;
}
public void setDrawPlateau(boolean drawPlateau) {
	this.drawZones = drawPlateau;
}
public int getMaxLabelSize() {
	return maxLabelSize;
}
public void setMaxLabelSize(int maxLabelSize) {
	this.maxLabelSize = maxLabelSize;
}
}