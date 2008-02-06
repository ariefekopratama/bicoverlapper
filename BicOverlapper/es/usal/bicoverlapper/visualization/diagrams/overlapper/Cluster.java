package es.usal.bicoverlapper.visualization.diagrams.overlapper;


import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import es.usal.bicoverlapper.utils.CustomColor;
import es.usal.bicoverlapper.utils.GraphPoint2D;


/**
 * Abstract class for cluster representation. Methods to manage edges are left to child classes.
 * A cluster is any group of nodes, overlapped or not. For this documentation purposes,
 * cluster, bicluster and group are synonyms, although of course they actually mean very different things.
 */
public abstract class Cluster 
	{
	protected Graph myGraph = null;
	protected ClusterSet myResultSet = null;
	protected ArrayList<Node> clusterNodes;
	CustomColor labelColor = new CustomColor(0,200,200,150);
	protected String label;
	protected String title;//Just for film tests
	protected int order;
	protected GraphPoint2D group[]=null; //Positions of all nodes in this cluster
	Polygon hull=null;
	
	//----- constructors
	/**
	 * Builds an empty Cluster
	 */
	public Cluster() {
		clusterNodes = new ArrayList<Node>();
	  }
	  
	/**
	 * Builds a cluster that correponds to the cluster set r
	 * @param r	The ClusterSet in which this cluster is
	 */  
	public Cluster(ClusterSet r) {
			clusterNodes = new ArrayList<Node>();
			myResultSet = r;
			myGraph = myResultSet.myGraph;
		  } 
	
	/**
	 * Builds a cluster with name l that is in the ClusterSet r
	 * @param r	ClusterSet in whith this cluster is
	 * @param l	name for this cluster
	 */
	  public Cluster(ClusterSet r, String l) {
			clusterNodes = new ArrayList<Node>();
			myResultSet = r;
			label=l;
			myGraph = myResultSet.myGraph;
		  } 

	 //---- drawing functions
	  /**
	   * Draws the Cluster. Which means drawing all their nodes, the surrounding hull, and node and title labels
	   * if corresponding flags are on
	   */
	  
	  public void draw()
	  	{
		 draw(true, true);
	  	}
	  
	  /**
	   * Draw the hull of the cluster. The hull of the cluster is its surrounding area, determined by their most peripheral nodes
	   *
	   */
	  void drawHulls()
		{
 	    Overlapper bv=(Overlapper) myGraph.getApplet();
		  
 	    if(myGraph.getHoverClusters().containsKey(this.label))
 	 	{
 	    	Color c=bv.paleta[Overlapper.hoverColor];
			 bv.fill(c.getRed(), c.getGreen(), c.getBlue(), myResultSet.myColor.getA());
			 bv.stroke(c.getRed(), c.getGreen(), c.getBlue(),255);
	 	    }
	    	
 	    else
 	    	{
 	    	if(myGraph.getSelectedClusters().containsKey(this.label))
 	    	 	{
 	    		Color c=bv.paleta[Overlapper.selectionColor];
 				 bv.fill(c.getRed(), c.getGreen(), c.getBlue(), myResultSet.myColor.getA());
 				 bv.stroke(c.getRed(), c.getGreen(), c.getBlue(),255);
 		 		}
 	    	else
 	    	 	{
 				 bv.fill(myResultSet.myColor.getR(), myResultSet.myColor.getG(), myResultSet.myColor.getB(), myResultSet.myColor.getA());
 				 bv.stroke(myResultSet.myColor.getR(), myResultSet.myColor.getG(), myResultSet.myColor.getB(),255);
 		 	    }
	 	   }
			 
		 int numNodes=0;
			 
		 for(int i=0;i<clusterNodes.size();i++)
		 	{
			Node n = (Node)clusterNodes.get(i);
		    if(n.clusters.size()>=bv.nodeThreshold)	numNodes++;
		 	}
		 if(numNodes<2)	return;
		 group=new GraphPoint2D[numNodes];
		 
		  float meanx=0;
		  float meany=0;
		  //----------------------------- HULL DRAWING --------------------------
		  boolean hullInScreen=false;
		  int cont=0;
		  for (int i=0; i<clusterNodes.size(); i++) 
		  		{
		       Node n = (Node)clusterNodes.get(i);
		       if(n.clusters.size()>=bv.nodeThreshold)
			       {
			       if(!hullInScreen && !bv.isDrawingOverview() && pointInScreen((GraphPoint2D)n.getPosition()))	hullInScreen=true;
		           group[cont++] = convertRefFrame((GraphPoint2D)n.getPosition());
		           meanx+=n.getX();
		    	   meany+=n.getY();
			       }
		  		}
		  
		  hullInScreen=true;//TODO: Sólo para pruebas de métricas!
		  
		  if(bv.isDrawingOverview() || hullInScreen)
		  {
		  if(clusterNodes.size()>1)
			  {
			  presort(group);
			  
			  ArrayList groupHull;
			  groupHull = chainHull_2D(group, group.length);
			  
		      bv.strokeWeight(1);//2 para fotos pequeñas
			  bv.beginShape();
			  for (int i=0; i<groupHull.size(); i++) 
			  	{
			      GraphPoint2D p = convertRefFrame((GraphPoint2D)groupHull.get(i));
			      if(bv.isUseCurves()) 
			    	 bv.curveVertex((float)p.getX(), (float)p.getY());
			      else	  
			         bv.vertex((float)p.getX(), (float)p.getY());
			    }
			  //Para cerrar la curva
			  if(bv.isUseCurves())
			     bv.curveVertex((float)(convertRefFrame((GraphPoint2D)groupHull.get(0))).getX(), (float)(convertRefFrame((GraphPoint2D)groupHull.get(0))).getY());
			
			  hull=bv.endShape(JProcessingPanel.CLOSE);
			  bv.noStroke();
			  }
		  }
		}

	  /**
	   * Draw the label of the Cluster in the center of its hull
	   *
	   */
	void drawHullLabels()
		{
		Overlapper bv=(Overlapper) myGraph.getApplet();
		  
		int lcs=bv.getLabelClusterSize();
		float meanx=0;
		float meany=0;
		  
		  
		  //----------------------------- HULL DRAWING --------------------------
		  boolean hullInScreen=false;
		  for (int i=0; i<clusterNodes.size(); i++) 
		  		{
			       Node n = (Node)clusterNodes.get(i);
			       if(!hullInScreen && !bv.isDrawingOverview() && pointInScreen((GraphPoint2D)n.getPosition()))	hullInScreen=true;
		           meanx+=n.getX();
		    	   meany+=n.getY();
		  		}
		  
		  if(bv.isDrawingOverview() || hullInScreen)	//TODO: En este caso, no creo que sea muy importante controlar si está o no dentro
			  {
			  if(getLabel()!=null)
			  	{
				bv.textAlign(JProcessingPanel.CENTER);
				bv.textSize(lcs);
				Color c=bv.paleta[bv.bicLabelColor];
	    		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), 150);
				
				bv.text(getLabel().toUpperCase(), meanx/clusterNodes.size(), meany/clusterNodes.size());  
			  	}
			  }
		}
	
	/**
	 * Draw all the nodes in the Cluster as pie charts.
	 */
	void drawPiecharts()
		{
		Overlapper bv=(Overlapper) myGraph.getApplet();
		final float env = 1.3f;
		float ns=bv.getNodeSize();
		bv.rectMode(JProcessingPanel.CENTER);
		
		for(int i=0;i<clusterNodes.size();i++)
		    {
			Node n=clusterNodes.get(i);
			if(!n.isDrawnAsPiechart() && n.clusters.size()>=bv.nodeThreshold)
				{
				if(bv.isDrawingOverview() || pointInScreen((GraphPoint2D)n.getPosition()))
					{
			    	float x=(float)n.getX();
			        float y=(float)n.getY();
			        float s=((ForcedNode)n).getSize();
			        float senv=s*env;
					 
			        
			        //Para saber qué porción de círculo toca;
			        float arc = Overlapper.TWO_PI / n.getClusters().size();
				    //Para hacer un sector por grupo al que pertenece
			        int inter=0;
			        if(bv.isOnlyIntersecting())	inter=1;
			        if(n.getClusters().size()>inter)
			        	{
				        Iterator itDraw=n.getClusters().values().iterator();
				        for (int j=0; itDraw.hasNext(); j++)
				           	{
				        	MaximalCluster c=(MaximalCluster)itDraw.next();
					    	ClusterSet r = c.myResultSet;
					    	CustomColor col = r.myColor;
					    	bv.fill(col.getR(), col.getG(), col.getB(),100);
					    	bv.strokeWeight(ns/3);
					        bv.stroke(col.getR(), col.getG(), col.getB(), col.getA()+50);
					        
					        float arcj=arc*j;
					       
					        bv.arc(x, y, senv, senv, arcj, arc*(j+1));
					        
					        bv.stroke(255,255,255,255);
					        bv.strokeWeight(1);
						   	bv.line(x, y, (float)(x+ ns/2*Math.cos(arcj)), (float)(y+ ns/2*Math.sin(arcj)));		    
						    bv.fill(0,0,0,255);
				        	}
			        	}
			        
			        n.setDrawnAsPiechart(true);
				    }//if(punto en pantalla)
				}//if(no ha sido ya pintada la piechart)
		    }
		}
	
	/**
	 * Draws the labels of all the nodes in the Cluster
	 */
	void drawNodeLabels()
		{
		Overlapper bv=(Overlapper) myGraph.getApplet();
		int ls=bv.getLabelSize();
		Color cg=bv.paleta[bv.geneLabelColor];
    	Color cc=bv.paleta[bv.conditionLabelColor];
    	
		
		for(int i=0;i<clusterNodes.size();i++)
		    {
			Node n=clusterNodes.get(i);
			
			if(!n.isDrawnAsLabel())
			{
			if(bv.isDrawingOverview() || pointInScreen((GraphPoint2D)n.getPosition()))
			{
			if (n!=bv.g.getHoverNode() && !bv.g.getSelectedNodes().containsKey(n.getLabel()) && n.getDetails().length()==0 && n.getImage().length()==0)
			   	{
			    bv.fill(labelColor.getR(), labelColor.getG(), labelColor.getB(), labelColor.getA());
			   
		    	bv.fill(0,0,0,255);
		    	if(ls+n.getClusters().size()*2>20)		bv.textSize(20);
		    	else									bv.textSize(ls+n.getClusters().size()*2); 
		       	bv.text(n.getLabel(), (float)(n.getX()+0.5), (float)(n.getY()-n.getHeight()+0.5));
		        	

	        	if(n.isGene())	
	        		bv.fill(cg.getRed(), cg.getGreen(), cg.getBlue(), cg.getAlpha());
	        	else
	        		bv.fill(cc.getRed(), cc.getGreen(), cc.getBlue(), cc.getAlpha());
	        	
		    	if(bv.isAbsoluteLabelSize())	bv.textSize(ls);
	    		else						
	    			{
		    		if(ls+n.getClusters().size()*2>20)		bv.textSize(20);
		    		else									bv.textSize(ls+n.getClusters().size()*2);
	    			}
	    		bv.text(n.getLabel(), (float)n.getX(), (float)n.getY()-n.getHeight());
		    	}
			n.setDrawnAsLabel(true);
		    }//if(punto en pantalla)
			}
		  }
		  bv.textSize(10);
		}
	
	/**
	 * Draws nodes as circles
	 */
	void drawNodes()//TODO: Ojo, esto va a ForcedNode.draw, que dibuja aparte de la forma los details
		{
		Overlapper bv=(Overlapper) myGraph.getApplet();
		for(int i=0;i<clusterNodes.size();i++)
		    {
			Node n=clusterNodes.get(i);
			if(bv.isDrawingOverview() || pointInScreen((GraphPoint2D)n.getPosition()))
				{
				if(!n.isDrawn())		n.draw();
				}//if(punto en pantalla)
		
		    }
		}
	
	/**
	 * STILL UNDER DEVELOPMENT
	 *
	 */
	void drawDetails()
		{
		
		}

	/**
	 * STILL UNDER DEVELOPMENT
	 *
	 */
	void drawEdges()
		{
		
		}
	
	/**
	 * Draws the cluster
	 * @param drawHulls	If true, the wrapping hull is drawn
	 * @param drawNodes	If true, the included nodes are drawn
	 */
	  void draw (boolean drawHulls, boolean drawNodes)
	  		{
		  Overlapper bv=(Overlapper) myGraph.getApplet();
		  
		   float ns=bv.getNodeSize();
			  int ls=bv.getLabelSize();
			  int lcs=bv.getLabelClusterSize();
	     
		  final float env = 1.3f;
		  bv.fill(myResultSet.myColor.getR(), myResultSet.myColor.getG(), myResultSet.myColor.getB(), myResultSet.myColor.getA());
		 bv.stroke(myResultSet.myColor.getR(), myResultSet.myColor.getG(), myResultSet.myColor.getB(),255);
		  
		 if(group==null || group.length!=clusterNodes.size())	group=new GraphPoint2D[clusterNodes.size()];
		  
		  float meanx=0;
		  float meany=0;
		  
		  
		  //----------------------------- HULL DRAWING --------------------------
		  if(drawHulls)
		  {
		  boolean hullInScreen=false;
		  for (int i=0; i<clusterNodes.size(); i++) 
		  		{
		       Node n = (Node)clusterNodes.get(i);
		       if(!hullInScreen && !bv.isDrawingOverview() && pointInScreen((GraphPoint2D)n.getPosition()))	hullInScreen=true;
	           group[i] = convertRefFrame((GraphPoint2D)n.getPosition());
	           meanx+=n.getX();
	    	   meany+=n.getY();
		  		}
		  
		  
		  if(bv.isDrawingOverview() || hullInScreen)
		  {
		  if(bv.isDrawHull() && clusterNodes.size()>1)
			  {
			  presort(group);
			  
			  ArrayList groupHull;
			  //No funciona para un hull de menos de 3 elementos
			  //groupHull = simpleHull_2D(group, clusterNodes.size());
			  
			  groupHull = chainHull_2D(group, clusterNodes.size());
			  
		      bv.strokeWeight(1);//2 para fotos pequeñas
			  bv.beginShape();
			  for (int i=0; i<groupHull.size(); i++) 
			  	{
			      GraphPoint2D p = convertRefFrame((GraphPoint2D)groupHull.get(i));
			      if(bv.isUseCurves()) 
			    	 bv.curveVertex((float)p.getX(), (float)p.getY());
			      else	  
			         bv.vertex((float)p.getX(), (float)p.getY());
			    }
			  //Para cerrar la curva
			  if(bv.isUseCurves())
			     bv.curveVertex((float)(convertRefFrame((GraphPoint2D)groupHull.get(0))).getX(), (float)(convertRefFrame((GraphPoint2D)groupHull.get(0))).getY());
			
			  bv.endShape(JProcessingPanel.CLOSE);
			  bv.noStroke();
			  }
		  }
		  if(bv.isDrawingOverview() || hullInScreen)
		  {
		  if(bv.isDrawClusterLabels()&& getLabel()!=null)
		  	{
			bv.textAlign(JProcessingPanel.CENTER);
			//bv.textSize(ls*3);
			bv.textSize(lcs);
			Color c=bv.paleta[bv.bicLabelColor];
    		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), 150);
			
			//bv.fill(bv.paleta[bv.colorTitle].getRed(), bv.paleta[bv.colorTitle].getGreen(), bv.paleta[bv.colorTitle].getBlue(), 150);
		    bv.text(getLabel().toUpperCase(), meanx/clusterNodes.size(), meany/clusterNodes.size());  
		  	}
		  }
		  }//if(drawHulls)
		  
		  //------------------ DRAW NODES ---------------------------
		  if(drawNodes)
		  {
		  for(int i=0;i<clusterNodes.size();i++)
		    {
			Node n=clusterNodes.get(i);
			if(bv.isDrawingOverview() || pointInScreen((GraphPoint2D)n.getPosition()))
			{
			if(!n.isDrawn())
				{
		        if(bv.isDrawPiecharts())
			        {
		        	float x=(float)n.getX();
			        float y=(float)n.getY();
			        float s=((ForcedNode)n).getSize();
			        float senv=s*env;
					 
			        
			        //Para saber qué porción de círculo toca;
			        float arc = Overlapper.TWO_PI / n.getClusters().size();
				    //Para hacer un sector por grupo al que pertenece
			        int inter=0;
			        if(bv.isOnlyIntersecting())	inter=1;
			        if(n.getClusters().size()>inter)
			        	{
				        Iterator itDraw=n.getClusters().values().iterator();
				        for (int j=0; itDraw.hasNext(); j++)
				           	{
				        	MaximalCluster c=(MaximalCluster)itDraw.next();
					    	ClusterSet r = c.myResultSet;
					    	CustomColor col = r.myColor;
					    	bv.rectMode(JProcessingPanel.CENTER);
					    	bv.fill(col.getR(), col.getG(), col.getB(),100);
					    	bv.strokeWeight(ns/3);
					        bv.stroke(col.getR(), col.getG(), col.getB(), col.getA()+50);
					        
					        float arcj=arc*j;
					       
					        System.out.print("Dibujo arco centrado en "+x+", "+y);
					        bv.arc(x, y, senv, senv, arcj, arc*(j+1));
					        
					        bv.stroke(255,255,255,255);
					        bv.strokeWeight(1);
						   	bv.line(x, y, (float)(x+ ns/2*Math.cos(arcj)), (float)(y+ ns/2*Math.sin(arcj)));		    
						    bv.fill(0,0,0,255);
						    }
			        	}
			        }
			    if (bv.isShowLabel() && n!=bv.g.getHoverNode() && !bv.g.getSelectedNodes().containsKey(n.getLabel()) && n.getDetails().length()==0 && n.getImage().length()==0)
			    	{
			    	bv.fill(labelColor.getR(), labelColor.getG(), labelColor.getB(), labelColor.getA());
			    		{
			    		bv.fill(0,0,0,255);
			    		if(ls+n.getClusters().size()*2>20)		bv.textSize(20);
			    		else									bv.textSize(ls+n.getClusters().size()*2); 
			        	bv.text(n.getLabel(), (float)(n.getX()+0.5), (float)(n.getY()-n.getHeight()+0.5));

			        	
			        	if(n.isGene())	bv.fill(195, 250, 190, 255);
			        	else			bv.fill(165, 175, 250, 255);	

			    		if(bv.isAbsoluteLabelSize())	bv.textSize(ls);
			    		else						
			    			{
				    		if(ls+n.getClusters().size()*2>20)		bv.textSize(20);
				    		else									bv.textSize(ls+n.getClusters().size()*2);
			    			}
			    		}
		        	bv.text(n.getLabel(), (float)n.getX(), (float)n.getY()-n.getHeight());
			    	}
			    
			    //Finalmente pintamos el nodo en sí
			    ((ForcedNode)n).draw();

				}
		    }//if(punto en pantalla)
		    }
		  }
		  bv.textSize(10);
	  }
	  
	  /**
	   * For each other node in the cluster, an edge is added from n to it.
	   * @param n - node to which attach new edges
	   */
	  void buildEdges(Node n){
	  	Overlapper bv= (Overlapper)myGraph.getApplet();
		    for (int i=0; i<clusterNodes.size(); i++) {
		       Node m = (Node)clusterNodes.get(i);
			      
		       SpringEdge e1=alreadyConnected(n,m);
		       if (e1!=null)
		       		{
		    	   	e1.setLengthFactor(e1.getLengthFactor()*0.8);
		       		}
		       else
		       		{
		    	   if(n!=m)
		    	   		{
		    	        SpringEdge e = new SpringEdge(n, m);
		 	       		e.setNaturalLength(bv.getEdgeLength());
		    	     	myResultSet.myGraph.addEdge(e);
		    	   		}
		       		}
		       
		       }
	  }

	
	//------------ getters and setters -----
	/**
	 * Sets the graph in which this cluster is to be drawn
	 * @param h the graph
	 */
	public void setGraph(Graph h) {
        myGraph = h;
      }
    
	/**
	 * Sets the ClusterSet in which this node is
	 * @param rs	the ClusterSet
	 */
    public void setClusterSet(ClusterSet rs) {
        myResultSet = rs;
	      }
    
    /**
     * Returns the ClusterSet in which this node is
     * @return	the ClusterSet
     */
    public ClusterSet getClusterSet() {
		return myResultSet;
	}
	
    /**
     * Removes all nodes in the cluster
     *
     */
	public void removeNodes()
		{
		clusterNodes.clear();
		}
	
	/**
	 * Gets the cluster's name
	 * @return	String with the cluster's label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the cluster's name
	 * @param label	String with the cluster's name
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the order of the cluster, in the case of initial ordering of cluster for a better layour
	 * STILL IN DEVELOPMENT
	 */
	public int getOrder() {
		return order;
	}

	
	/**
	 * Sets the order of the cluster, in the case of initial ordering of cluster for a better layour
	 * STILL IN DEVELOPMENT
	 */
	public void setOrder(int order) {
		this.order = order;
	}


	/**
	 * Gets the color used for labelling cluster names
	 * @return color used for labelling cluster names
	 */
	public CustomColor getLabelColor() {
		return labelColor;
	}

	
	/**
	 * Sets the color used for labelling cluster names
	 * @param labelColor color used for labelling cluster names
	 */
	public void setLabelColor(CustomColor labelColor) {
		this.labelColor = labelColor;
	}
	 
	/**
	 * Gets the nodes grouped in this cluster
	 * @return	nodes grouped in this cluster
	 */
	ArrayList<Node> getNodes() {
		    return clusterNodes;
		  }
	
	/**
	 * Get the nodes grouped in this cluster at position i
	 * @param pos	postion of the node requested. Position is detemined by the order in which the nodes are added
	 * @return	a node grouped in this cluster
	 */
	ForcedNode getNode(int pos) {
		    return (ForcedNode)clusterNodes.get(pos);
		  }
	 
	  //-------------- UTILS FOR CLUSTER DRAWING ------------
	  /**
	   * Returns the center of the cluster, computed as the mean of each cluster node's coordinates
	   * returns	point with the coordinates of the middle point
	   */
	  public Point2D.Double getMiddlePoint()
	  	{
		float meanx=0;
		float meany=0;
		
		for (int i=0; i<clusterNodes.size(); i++) 
			{
			Node n = (Node)clusterNodes.get(i);
			meanx+=n.getX();
			meany+=n.getY();
			}
		return new Point2D.Double(meanx/clusterNodes.size(), meany/clusterNodes.size());
	  	}
	  
	    /**
	     * Computes the wrapping hull by using Melkman algorithm
	     * @param V
	     * @param n
	     * @return
	     */
	    protected ArrayList simpleHull_2D(GraphPoint2D V[], int n)
	    {
	        // initialize a deque D[] from bottom to top so that the
	        // 1st three vertices of V[] are a counterclockwise triangle
	  	  
	        
	  	  GraphPoint2D D[] = new GraphPoint2D[2*n+1];
	  	  
	  	  int bot = n-2, top = bot+3;   // initial bottom and top deque indices
	  	    D[bot] = D[top] = V[2];       // 3rd vertex is at both bot and top
	  	    if (isLeft(V[0], V[1], V[2]) > 0) {
	  	        D[bot+1] = V[0];
	  	        D[bot+2] = V[1];          // ccw vertices are: 2,0,1,2
	  	    }
	  	    else {
	  	        D[bot+1] = V[1];
	  	        D[bot+2] = V[0];          // ccw vertices are: 2,1,0,2
	  	    }
	       
	  	    // compute the hull on the deque D[]
	  	    for (int i=3; i < n; i++) {   // process the rest of vertices
	  	        // test if next vertex is inside the deque hull
	  	        if ((isLeft(D[bot], D[bot+1], V[i]) > 0) &&
	  	            (isLeft(D[top-1], D[top], V[i]) > 0) )
	  	             continue;// skip an interior vertex
	  	        
	  	        // incrementally add an exterior vertex to the deque hull
	  	        // get the rightmost tangent at the deque bot
	  	        while (isLeft(D[bot], D[bot+1], V[i]) <= 0)
	  	            ++bot;                // remove bot of deque
	  	        D[--bot] = V[i];          // insert V[i] at bot of deque
	  	      
	  	        // get the leftmost tangent at the deque top
	  	        while (isLeft(D[top-1], D[top], V[i]) <= 0)
	  	            --top;                // pop top of deque
	  	        D[++top] = V[i];          // push V[i] onto top of deque
	  	       
	  	    }


	        // transcribe deque D[] to the output hull array H[]
	        // h hull vertex counter
	        ArrayList<GraphPoint2D> hull = new ArrayList<GraphPoint2D>((top-bot)+1);
	        for (int h=0; h <= (top-bot); h++)
	            hull.add(D[bot + h]);

	      
	        return hull;
	    }
	   
	protected ArrayList chainHull_2D( GraphPoint2D P[], int n)
	    {
		    GraphPoint2D Hu[] = new GraphPoint2D[2*n];
		    int realSize = n;
	        // the output array H[] will be used as the stack
	        int    bot=0, top=(-1);  // indices for bottom and top of the stack
	        int    i;                // array scan index

	        // Get the indices of points with min x-coord and min|max y-coord
	        int minmin = 0, minmax;
	        double xmin = P[0].getX();
	        for (i=1; i<n; i++)
	            if (P[i].getX() != xmin) break;
	        minmax = i-1;
	        if (minmax == n-1) {       // degenerate case: all x-coords == xmin
	            Hu[++top] = P[minmin];
	            if (P[minmax].getY() != P[minmin].getY()) // a nontrivial segment
	                Hu[++top] = P[minmax];
	            Hu[++top] = P[minmin];           // add polygon endpoint
	            //realSize = top + 1;
	        }

	        // Get the indices of points with max x-coord and min|max y-coord
	        int maxmin, maxmax = n-1;
	        double xmax = P[n-1].getX();

	        for (i=n-2; i>=0; i--)
	            if (P[i].getX() != xmax) break;
	        maxmin = i+1;

	        // Compute the lower hull on the stack H
	        Hu[++top] = P[minmin];      // push minmin point onto stack
	        i = minmax;
	        while (++i <= maxmin)
	        {
	            // the lower line joins P[minmin] with P[maxmin]
	            if (isLeft( P[minmin], P[maxmin], P[i]) >= 0 && i < maxmin)
	                continue;          // ignore P[i] above or on the lower line

	            while (top > 0)        // there are at least 2 points on the stack
	            {
	                // test if P[i] is left of the line at the stack top
	                if (isLeft( Hu[top-1], Hu[top], P[i]) > 0)
	                    break;         // P[i] is a new hull vertex
	                else
	                    top--;         // pop top point off stack
	            }
	            Hu[++top] = P[i];       // push P[i] onto stack
	        }

	        // Next, compute the upper hull on the stack H above the bottom hull
	        if (maxmax != maxmin)      // if distinct xmax points
	            Hu[++top] = P[maxmax];  // push maxmax point onto stack
	        bot = top;                 // the bottom point of the upper hull stack
	        i = maxmin;
	        while (--i >= minmax)
	        {
	            // the upper line joins P[maxmax] with P[minmax]
	            if (isLeft( P[maxmax], P[minmax], P[i]) >= 0 && i > minmax)
	                continue;          // ignore P[i] below or on the upper line

	            while (top > bot)    // at least 2 points on the upper stack
	            {
	                // test if P[i] is left of the line at the stack top
	                if (isLeft( Hu[top-1], Hu[top], P[i]) > 0)
	                    break;         // P[i] is a new hull vertex
	                else
	                    top--;         // pop top point off stack
	            }
	            Hu[++top] = P[i];       // push P[i] onto stack
	        }
	        if (minmax != minmin)
	            Hu[++top] = P[minmin];  // push joining endpoint onto stack

	        realSize = top + 1;
	        ArrayList<GraphPoint2D> hull = new ArrayList<GraphPoint2D>(Hu.length);
	        for (int h=0; h < realSize; h++)
	            hull.add(Hu[h]);

	      
	        return hull;
	    }

	    
	  /**
	   * Método auxiliar para indicar si 
	   * un punto está Left|On|Right de una línea infinita.
	   * Usa http://softsurfer.com/Archive/algorithm_0101/algorithm_0101.htm
	   * @param P0
	   * @param P1
	   * @param P2
	   * @return >0 for P2 left of the line through P0 and P1, =0 for P2 on the line, <0 for P2 right of the line

	   */  
	    private double isLeft( GraphPoint2D P0, GraphPoint2D P1, GraphPoint2D P2 )
	    {
	    	double res;
	    	res = (P1.getX() - P0.getX()) * (P2.getY() - P0.getY());
	    	res = res - ((P2.getX() - P0.getX()) * (P1.getY() - P0.getY())); 
	    	return res; 
	    }

	    /**
	     * Returns true if Edge e is in this Cluster
	     * @param e 
	     * @return true if Edge e is in this Cluster
	     */
	    public boolean alreadyConnected(Edge e)
	    {
	    	
	    	for (int i = 0; i < myGraph.getEdges().size(); i++)
	    	  {
	    		Edge f = (Edge) myGraph.getEdges().get(i);
	    		if (e.getFrom() == f.getFrom() &&
	    		    e.getTo() == f.getTo())
	    			return true;
	    	  }
	    	
	        return false;        
	    }

	    /**
	     * Returns the edge connecting n and m (despite the direction), or null if it does not exist
	     * @param n	Node to see if it's connected by any edge with m
	     * @param m	Node to see if its's connected by any edge with n
	     * @return	the edge connecting n and m, or null if no edge connects them
	     */
	    protected final SpringEdge alreadyConnected(Node n, Node m)
	    	{
	    	SpringEdge ret=(SpringEdge)myGraph.getEdges().get(n.getLabel()+"->"+m.getLabel());	//MIramos a ver si está en un sentido
	    	if(ret==null)	ret=(SpringEdge)myGraph.getEdges().get(m.getLabel()+"->"+n.getLabel());//O en el otro
	    	return ret; //Devolvemos n->m, m->n o null si no está.
	    	}

	    protected GraphPoint2D convertRefFrame(GraphPoint2D p)
	    	{
	    	Overlapper bv=(Overlapper)myGraph.getApplet();
	    	return (new GraphPoint2D(p.getX(), bv.getScreenHeight() - p.getY()));        
	    	}
	   
	    /**
	     * Orders vectors
	     * @param dataSet
	     */
	    protected void presort(GraphPoint2D dataSet[])
	    {
	    	
		int j = 0;
		boolean exchanged;
		
		do 
			{
			exchanged = false;	
			for (int i = 1; i < dataSet.length - j; i++)
				{
				
				if (dataSet[i].getX() < dataSet[i-1].getX())
					//if (dataSet[i].getY() <= dataSet[i-1].getY())
						{ 
						   
						   double tmpX, tmpY;
						   
						   tmpX = dataSet[i-1].getX();
						   dataSet[i-1].setX(dataSet[i].getX());
						   dataSet[i].setX(tmpX);
						   
						   tmpY = dataSet[i-1].getY();
						   dataSet[i-1].setY(dataSet[i].getY());
						   dataSet[i].setY(tmpY);
						   
						   exchanged = true;
				    //}//if
					}//if	
				} //for
			j++;
			}while(exchanged == true);
	    }
	    
	protected boolean pointInScreen(GraphPoint2D point)
		{
		Overlapper bv=(Overlapper) myGraph.getApplet();
		float x=-bv.getOffsetX();
		float y=-bv.getOffsetY();
		float w=bv.getScreenWidth();
		float h=bv.getScreenHeight();
		if(point.getX()>x && point.getX()<(x+w) && point.getY()>y && point.getY()<(y+h))	return true;
		else																				return false;
		}
	}
