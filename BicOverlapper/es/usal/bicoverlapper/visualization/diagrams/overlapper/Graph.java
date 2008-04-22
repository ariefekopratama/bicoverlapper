package es.usal.bicoverlapper.visualization.diagrams.overlapper;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.utils.CustomColor;
import es.usal.bicoverlapper.utils.GraphPoint2D;
import es.usal.bicoverlapper.utils.geneticAlgorithms.GAThread;
import es.usal.bicoverlapper.utils.geneticAlgorithms.GeneticAlgorithm;
import es.usal.bicoverlapper.utils.geneticAlgorithms.GraphGeneticAlgorithm;
import gishur.core.SimpleList;
import gishur.x.Intersection;
import gishur.x.XPoint;
import gishur.x.XPolygon;
import gishur.x.XSegment;



/**
 * A Graph is a set of Nodes, grouped in (overlapped) Clusters, 
 * that are classified in ResultSets.
 * The nodes of each Cluster are joined by Edges.
 * Graph controls the structure of nodes and edges
 * BiclusVis controls the layout, interaction and manages the drawing
 * @author Roberto Ther�n and Rodrigo Santamar�a
  */
public class Graph {
	private Map<String, Node> nodes;
	private Map<String, Edge> edges;
	private Map<String, ClusterSet> results;
	private Map<String, Node> centerNodes;
	
	HashMap<Node, Map<String, Edge>> edgesFrom;
	 HashMap<Node, Map<String, Edge>> edgesTo;

	 HashMap<Node, Map<String, Edge>> dualEdgesFrom;
	 HashMap<Node, Map<String, Edge>> dualEdgesTo;
	 
	 int maxZones;
	 
	 /**
	  * Dual nodes identifying zones
	  */
	public Map<Integer, DualNode> dualNodes;
	
	/**
	 * Edges connecting dual nodes from the same contour
	 */
	public Map<Integer, Edge> dualEdges;
	 
	public boolean topographyDrawn=false;
	public boolean dualDrawn=false;
	 
  Overlapper applet;
  boolean radial;
  
  Node dragNode = null;
  Node hoverNode = null;
  private Map<String, Node> selectedNodes;
  private Map<String, Node> searchNodes;
  private Map<String, Node> centroidNodes;

  private Map<String, Cluster> selectedClusters;
  private Map<String, Cluster> hoverClusters;
  private Map<String, DualNode> hoverDualZones;
  
  //drawing priorities
  /**
   * Identifier for hull areas 
   */
  public static final int HULL=0;
  /**
   * Identifier for nodes
   */
  public static final int NODE=1;
  /**
   * Identifier for labels in hulls
   */
  public static final int HULLLABEL=2;
  /**
   * Identifier for node labels
   */
  public static final int NODELABEL=3;
  /**
   * Identifier for edges
   */
  public static final int EDGE=4;
  /**
   * Identifier for details (STILL IN DEVELOPMENT)
   */
  public static final int DETAIL=5;
  /**
   * Identifier for piecharts
   */
  public static final int PIECHART=6;
  /**
   * Identifier for hover node
   */
  public static final int HOVER=7;//Special treatment to hovered nodes
  /**
   * Identifier for selected nodes
   */
  public static final int SELECT=8;//Special treatment to selected nodes
  /**
   * Identifier for searched nodes
   */
  public static final int SEARCH=9;//Special treatment to searched nodes
  /**
   * Identifier for misplaced nodes
   */
  public static final int ERROR=10;//Special treatment to misplaced nodes
  /**
   * Identifier for dual nodes
   */
  public static final int DUAL=11;//Special treatment to dual nodes
  
  /**
   * Identifier for topography map
   */
  public static final int TOPOGRAPHY=12;//Drawing of topography map

  /**
   * Drawing of exact groups
   */
  public static final int EXACT=14;//Drawing of topography map

  public static final int MISPLACEMENT=20;
  public static final int AREA=21;
  public static final int ROUNDNESS_AREA=22;
  public static final int LENGTH_AREA=23;

  
  /**
   * Builds an empty Graph that is visualized in p
   * @param p	BiclusVis panel where the Graph is visualized
   */
  public Graph(Overlapper p) {
	    nodes = new HashMap<String, Node>();
	    edges = new HashMap<String, Edge>();
	    results = new HashMap<String, ClusterSet>();
	    centerNodes = new HashMap<String, Node>();
	  
	    edgesFrom = new HashMap<Node, Map<String,Edge>>();
	    edgesTo = new HashMap<Node, Map<String,Edge>>();
	   
	    dualEdgesFrom = new HashMap<Node, Map<String,Edge>>();
	    dualEdgesTo = new HashMap<Node, Map<String,Edge>>();
	    
	    searchNodes=new TreeMap<String, Node>();
	    selectedNodes=new TreeMap<String, Node>();
	    centroidNodes=new TreeMap<String, Node>();
	    
	    selectedClusters=new TreeMap<String, Cluster>();
	    hoverClusters=new TreeMap<String, Cluster>();
	    hoverDualZones=new TreeMap<String, DualNode>();
	    
	   	    
    applet=p;
    radial=false;
  	}
  
  /**
   * Marks as hover node n. A hover node is the node where the mouse is over 
   * @param n	hover node
   */
  public void setHoverNode(Node n) {
    hoverNode = n;
  }
  
  /**
   * Returns the hover node
   * @return	hover node
   */
  public Node getHoverNode() {
    return hoverNode;
  }
  
  /**
   * Marks n as the drag node. The drag node is a node that has been clicked and dragged
   * @param n	drag node
   */
  public void setDragNode(Node n) {
    dragNode = n;
  }
  
  /**
   * Adds n to the set of nodes searched.
   * @param n	node searched
   */
  public void addSearchNode(Node n)
	{
	  searchNodes.put(n.label, n);
	}
  
  /**
   * gets a map with all the nodes searched. The key of the map are node labels
   * @return map with searched nodes
   */
  public Map<String, Node> getSearchNodes()
	{
	  return searchNodes;
	}
  
  /**
   * gets a map with all the center nodes. The key of the map are node labels
   * @return map with center nodes
   */
  public Map<String, Node> getCentroidNodes()
	{
	  return centroidNodes;
	}
  
  /**
   * Adds a node to the graph as center node
   * @param n	center node
   */
  public void addCentroidNode(Node n)
	{
	if(n!=null)	centroidNodes.put(n.label, n);
	}
 
  /**
   * Removes all searched nodes (the nodes are kept but no longer marked as searched nodes)
   *
   */
  public void clearSearchNodes()
	{
	 searchNodes.clear();
	}

  /**
   * Removes all center nodes
   *
   */
  public void clearCentroidNodes()
	{
	 centroidNodes.clear();
	}

  /**
   * Adds node n to the set of selected nodes (all the nodes that has been clicked)
   * @param n
   */
  public void addSelectedNode(Node n)
	{
	  selectedNodes.put(n.label, n);
	}
  
  /**
   * Removes all selected nodes (the nodes are kept but no longer marked as selected nodes)
   *
   */
  public void clearSelectedNodes()
	{
	if(selectedNodes!=null)	selectedNodes.clear();
	}
  
  /**
   * Gets the node marked as being dragged
   * @return dragged node
   */
  public Node getDragNode() {
    return dragNode;
  }
  
  /**
   * Adds a new edge to the graph
   * @param e Edge to add
   */
  public void addEdge(Edge e) {
     edges.put(e.getFrom().getLabel()+"->"+e.getTo().getLabel(), e);
 
     TreeMap<String,Edge> f=getEdgesFrom(e.getFrom());
    f.put(e.getFrom().getLabel()+"->"+e.getTo().getLabel(), e);
    
    TreeMap<String,Edge> t=getEdgesTo(e.getTo());
    t.put(e.getFrom().getLabel()+"->"+e.getTo().getLabel(), e);
    e.setGraph(this);    
  }

  /**
   * Deletes all edges that come from node n
   * @param n	Node from which all edges will be removed
   */
  public void deleteEdgesFrom(Node n)
	{
  	TreeMap<String,Edge> f=(TreeMap<String,Edge>)getEdgesFrom(n);
  	Iterator<Edge> it=f.values().iterator();
	for(int i=0;i<f.size();i++)
		{
		Edge e=(Edge)it.next();
		if(edges.containsValue(e))			edges.remove(e);
		}
	f.clear();
	}
  
  /**
   * Deletes all edges that end in node n
   * @param n	Node to which all edges will be removed
   */
  public void deleteEdgesTo(Node n)
  	{
  	TreeMap<String,Edge> t=(TreeMap<String,Edge>)getEdgesTo(n);
  	Iterator<Edge> it=t.values().iterator();
	for(int i=0;i<t.size();i++)
		{
		Edge e=(Edge)it.next();
		if(edges.containsValue(e))			edges.remove(e);
		}
	t.clear();
  	}
  
  /**
   * Removes all edges that come from or arrives to node n
   * @param n	Node of which all edges will be removed
   */
  public void deleteEdgesFor(Node n)
  	{
	deleteEdgesFrom(n);
	deleteEdgesTo(n);
  	}
  
  /**
   * Returns all the edges that come from a node.
   * @param n Node to get all the edges that comes from it
   * @return	A map with edges (keys are strings in the form "sourceNodeLabel->targetNodeLabel")
   */
  public TreeMap<String,Edge> getEdgesFrom(Node n) {
	  TreeMap<String,Edge> f=(TreeMap<String,Edge>)edgesFrom.get(n);
    
    if (f == null) {
    	f=new TreeMap<String,Edge>();
      edgesFrom.put(n, f);
    }
    return f;
  }
  
  /**
   * Returns all the edges that go to a node.
   * @param n Node to get all the edges that comes from it
   * @return	A map with edges (keys are strings in the form "sourceNodeLabel->targetNodeLabel")
   */ 
  public TreeMap<String,Edge> getEdgesTo(Node n) 
  	{
	TreeMap<String,Edge> t=(TreeMap<String,Edge>)edgesTo.get(n);
    if (t == null) {
    	t=new TreeMap<String,Edge>();
      edgesTo.put(n, t);
    }
    return t;    
  }
  
  /**
   * Checks if nodes a and b are connected
   * @param a	Node to check connection with b
   * @param b	Node to check connection with a
   * @return	true if a and b are connected, false otherwise
   */
  protected boolean isConnected(Node a, Node b) 
  	{
	  if(edges.containsKey(a.label+"->"+b.label) || edges.containsKey(b.label+"->"+a.label))	return true;
	  else																						return false;
  	}
  	

  /**
   * Gets the number of edges in the graph
   * @return	the number of edges in the graph
   */
  public int getNumEdges()
	{
	return edges.size();
	}

  /**
   * Gets the number of nodes in the graph
   * @return	the number of nodes in the graph
   */
	public int getNumNodes()
		{
		return nodes.size();
		}
	
	  /**
	   * Gets the number of center nodes in the graph
	   * @return	the number of center nodes in the graph
	   */
	public int getNumCenterNodes()
		{
		return centerNodes.size();
		}

  /**
   * Returns true if Node a has no edges in either direction
   * @param a	Node to check if it has edges
   * @return	true if the Node has no connected edges, false otherwise
   */
  boolean isNotConnected(Node a) 
  	{
    Iterator<Edge> i = edges.values().iterator();
	while (i.hasNext()) 
		{
		Edge e = (Edge)i.next();    
		if (e.getFrom() == a || e.getTo() == a) 	return false;
		}
    return true;
  	}

  /**
   * Adds a node to the graph
   * @param n	Node to add
   */
  public void addNode(Node n) {
	  nodes.put(n.getLabel(), n);
   	  n.setGraph(this);
  }

  /**
   * Returns all non-center nodes in this graph
   * @return A map with all nodes in the graph. The key is a String with node labels
   */
  public Map<String,Node> getNodes() {
    return nodes;
  }
  
  /**
   * Add a center node to the Graph. A center node is a dummy node, that can be used as centroid for a group.
   * Center nodes are not drawn, its purpose is to make underlying connections inside clusters.
   * @param n	Cented node added
   */
  public void addCenterNode(Node n) {
	  	centerNodes.put(n.getLabel(), n);
	    n.setGraph(this);
	  }
  
  /**
   * Adds a ClusterSet to the graph
   * DEPLOYMENT NOTE: still this not adds all nodes in the ClusterSet to the Graph, it must be done by addNode()
   * @param r	ClusterSet to add to the graph
   */
  public void addClusterSet(ClusterSet r) {
	  	results.put(r.label, r);
	    r.setGraph(this);
	  }
  
 
  /**
   * Draws the graph by layers. 
   * The position of each component in the list priorities sets its drawing order.
   * Por example, if priorities is {HULL, NODE, LABEL}, first hulls are drawn, then overlapping nodes
   * and finally labels.
   * If any element is not present in the vector, it will not be drawn.
   * Elements to draw are HULL, NODE, HULLLABEL, NODELABEL, EDGE, DETAIL, PIECHART,HOVER, SELECT and SEARCH
   * @param priorities the elements to be drawn, ordered so the first element in the list will be the first element drawn
   */
  public void draw(int [] priorities)
  	{
	Iterator<Node> itGraph=nodes.values().iterator();
	while(itGraph.hasNext())	//Para evitar que se dibujen varias veces en caso de q est�n en varios clusters
	  	{	
		Node n =(Node)itGraph.next();
		n.setDrawn(false);
		n.setDrawnAsPiechart(false);
		n.setDrawnAsLabel(false);
	  	}
	/*if(this.dualNodes!=null && dualNodes.size()>0)
		{
		Iterator<DualNode> itGraphd=dualNodes.values().iterator();
		while(itGraphd.hasNext())	//Para evitar que se dibujen varias veces en caso de q est�n en varios clusters
		  	{	
			DualNode n =(DualNode)itGraphd.next();
			n.setDrawn(false);
			}
		}*/
	topographyDrawn=false;
	dualDrawn=false;

	for(int i=0;i<priorities.length;i++)	drawComponent(priorities[i]);
	this.getApplet().rectMode(JProcessingPanel.CORNER);
  	}
  
  /**
   * Draws one of the component of the graphs, as explained in draw(int[] priorities)
   * @param component	component of the graph
   */
  public void drawComponent(int component)
  	{
	Overlapper bv=(Overlapper)applet;
	Iterator<ClusterSet> itGraph=results.values().iterator();//Hull drawing
	
	if(clusterDependent(component))
		{
		for (int i=0; i<results.size(); i++) 
	  	{
	    ClusterSet r = (ClusterSet)itGraph.next();
	    for(int j=0;j<r.getClusters().size();j++)
	      	{
	    	Cluster c=(Cluster)r.getClusters().get(j);
	    	if(!bv.excludedClusters.contains(c.label))
		    	{
		    	switch(component)
					{
					case HULL:
						if(bv.isDrawHull())				c.drawHulls();
						break;
					case NODE:
						if(bv.isDrawNodes())			c.drawNodes();
						break;
					case HULLLABEL:
						if(bv.isDrawClusterLabels())	c.drawHullLabels();
						break;
					case NODELABEL:
						if(bv.isShowLabel())			c.drawNodeLabels();
						break;
					case EDGE:
						if(bv.isShowEdges())	drawEdges();//TODO: depende de C y lo hacemos una vez por cluster!	
						break;
					case DETAIL:
						if(bv.isDrawNodes())	c.drawDetails();//TODO: FALTA: ahora mismo el componente integrado a NODE
						break;
					/*case PIECHART:
						if(bv.isDrawPiecharts())	c.drawPiecharts();
						break;*/
					case TOPOGRAPHY:
						if(bv.isDrawTopography() && !topographyDrawn)		
							{
							//double start=System.currentTimeMillis();
							c.drawTopography2(dualNodes, maxZones);//contour surfaces
							if(bv.isDrawPlateau())	c.drawTopography(dualNodes, maxZones);//plateaus
							//System.out.println("Topograf�a tarda "+(System.currentTimeMillis()-start));
							}
						break;
					case EXACT:
						c.drawTopography(dualNodes, maxZones);
						break;
			    	}
		    	}//if(!excluded)
		    }
	  	  }
		}
	else
		{
		switch(component)
			{
			case EDGE:
				if(bv.isShowEdges())	drawEdges();	
				break;
			case HOVER:
				drawHoverNode();
				break;
			case SELECT:
				drawSelectedNodes();
				break;
			case SEARCH:
				drawSearchedNodes();
				break;
			case ERROR:
				if(bv.isDrawErrors())	drawErrors();
				break;
			case DUAL:
				if(bv.isDrawDual() && !dualDrawn)		drawDual();
				break;
			case PIECHART:
				if(bv.isDrawPiecharts())	drawPies();
				break;
			}
		}
	}
  
  
  boolean clusterDependent(int c)
  	{
	switch(c)
		{
		case HULL:
		case NODE:
		case HULLLABEL:
		case NODELABEL:
		case DETAIL:
		//case PIECHART:
		case TOPOGRAPHY:	//TODO: estos dos �ltimos no deber�an estar aqu�
		case EXACT:
			return true;
		}
	return false;
  	}
  
  void drawPies()
  	{
	Iterator<Node> itGraphN=nodes.values().iterator();
	while(itGraphN.hasNext())
	  	{
	    ForcedNode n=(ForcedNode)itGraphN.next();
	    if(!applet.isDrawingOverview() || pointInScreen((GraphPoint2D)n.getPosition()))
			n.drawPie();
	    }	  
  	}
  
  /**
   * Draws the equivalent dual graph (both nodes and edges), a kind of skeleton of the graph
   */
  void drawDual()
  	{
	 if(dualNodes!=null)
	 	{
		/*Iterator<Edge> ite=dualEdges.values().iterator();
		while(ite.hasNext())
			{
			Edge e=ite.next();
			e.draw();
			}*/
		Iterator<DualNode> itGraph=dualNodes.values().iterator();
		while(itGraph.hasNext())
		   {
		   DualNode dn=itGraph.next();
		  // if(!dn.isDrawn())
			   {
			   if(applet.isComputeDualLayout())	dn.draw(false);
			   else								dn.draw(true);
			   }
		   }
		 }
	dualDrawn=true;
   	}
  
  /**
   * Draws the edges in the graph
   *
   */
  public void drawEdges()
  	{
	Iterator<Edge> itGraph=edges.values().iterator();
	while(itGraph.hasNext())
	   {
		Edge e=(Edge)itGraph.next();
		//if(!centerNodes.containsValue(e.from) && !centerNodes.containsValue(e.to))		
			e.draw();
	   
	   }
		  
  	Iterator<Node> itGraphN=centerNodes.values().iterator();
	while(itGraphN.hasNext())
	  	{
	    Node n=(Node)itGraphN.next();
	    n.centerNode=true;
	    n.draw();
	    }	  
  	}
  
  /*
   * Draws all the components in the graph at a predefined order
   *
  public void draw() {
	  Iterator itGraph=nodes.values().iterator();
	  while(itGraph.hasNext())	//Para evitar que se dibujen varias veces en caso de q est�n en varios clusters
	  	{	
		Node n =(Node)itGraph.next();
		n.setDrawn(false);
	  	}

	  Overlapper bv=(Overlapper)applet;
	  itGraph=results.values().iterator();//Hull drawing
	  for (int i=0; i<results.size(); i++) 
	  	{
	      ClusterSet r = (ClusterSet)itGraph.next();
	      for(int j=0;j<r.getClusters().size();j++)
	      	{
	    	if(bv.isRadial())	
	    		{
	    		RadialCluster c=(RadialCluster)r.getClusters().get(j);
	    		c.draw(true,false);
		      	}
	    	else
	    		{
	    		MaximalCluster c=(MaximalCluster)r.getClusters().get(j);
	    		c.draw(true,false);
		      	}
	    	}
	    }

	  drawNodes();

	  if(bv.isShowEdges())
	  	{
		itGraph=edges.values().iterator();
		while(itGraph.hasNext())
			   {
			   Edge e=(Edge)itGraph.next();
		       e.draw();
		       }
		  
	  	itGraph=centerNodes.values().iterator();
		while(itGraph.hasNext())
		  	{
		    Node n=(Node)itGraph.next();
		    n.draw();
		    }
		}

	  
	  itGraph=centroidNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		int x=(int)n.getX();
		int y=(int)n.getY();
		applet.stroke(200,200,0);
		applet.strokeWeight(2);
		applet.line(x+10, y+10, x, y);
		}
	  
	  //Searched nodes ------------------------
	  bv.rectMode(JProcessingPanel.CENTER);
	  itGraph=searchNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		int x=(int)n.getX();
		int y=(int)n.getY();
		int w=(int)(n.getWidth()*n.getClusters().size()+5);
		applet.noFill();
		applet.stroke(200,200,0);
		applet.strokeWeight(2);
		applet.ellipse(x, y, w, w);
		}
	  applet.noStroke();

	  //-------------- selected nodes
	  itGraph=selectedNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		applet.noFill();
		applet.stroke(200,200,0);
		applet.strokeWeight(2);

		bv.fill(0,0,0,255);
    	bv.textSize(bv.getLabelSize()+6);
    	bv.text(n.getLabel(), (float)(n.getX()+0.5), (float)(n.getY()-n.getHeight()+0.5));

    	
    	if(n.isGene())	bv.fill(195, 250, 190, 255);
    	else			bv.fill(165, 175, 250, 255);	
    	bv.textSize(bv.getLabelSize()+5);
    	bv.text(n.getLabel(), (float)n.getX(), (float)n.getY()-n.getHeight());
		}
	  
	  //----------------- hover node
	  if(hoverNode!=null)
	  	{
	    int factor=1;
	    if(bv.isSizeRelevant())    	factor=hoverNode.clusters.size();
		    
  		Iterator itDrawMates=hoverNode.mates.values().iterator();
    	while(itDrawMates.hasNext())
    		{
    		ForcedNode n=(ForcedNode)itDrawMates.next();
    		Color c=bv.paleta[bv.selectionColor];
    		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), 0);
			bv.stroke(c.getRed(), c.getGreen(), c.getBlue(),255);
    			bv.rectMode(JProcessingPanel.CENTER);
    			if(n.isGene())	bv.ellipse((float) n.getX(), (float) n.getY(), n.width, n.height);
    			else			bv.rect((float) n.getX(), (float) n.getY(), n.width*factor, n.height*factor);
    		n.setDrawn(true);
	    	}
    	
    	if(hoverNode!=null && !selectedNodes.containsKey(hoverNode.label))
	    	{
	    	bv.fill(0,0,0,255);
	    	bv.textSize(bv.getLabelSize()+hoverNode.getClusters().size()*3+1); 
	    	bv.text(hoverNode.label, (float)(hoverNode.position.getX()+0.5), (float)(hoverNode.position.getY()-hoverNode.getHeight()+0.5));
	
		    	
	    	bv.fill(255,255,255,255);
	    	bv.textSize(bv.getLabelSize()+hoverNode.clusters.size()*3); 
	    	bv.text(hoverNode.label, (float)hoverNode.position.getX(), (float)hoverNode.position.getY()-hoverNode.getHeight());
	    	}
    	
    	}
	  
	  
	  applet.noStroke();
	  bv.rectMode(JProcessingPanel.CORNER);
  	}
  */
/**
 * Returns true if the node n is only in clusters that have been excluded by threshold limit, false otherwise
 * @param n - Node to check if it is completely excluded from the display
 * @return true if it is excluded, false otherwise
 */
  private boolean nodeExcluded(Node n)
	{
	Overlapper bv=(Overlapper)applet;
	Iterator<Cluster> itc=n.clusters.values().iterator();
	while(itc.hasNext())
		{
		Cluster c=(Cluster)itc.next();
		if(!bv.excludedClusters.contains(c.label))	return false;
		}
	return true;
	}

  /**
   * Draws the Hover node, highlighted, and also highlights their neighbors (all the nodes directly connected to it)
   *
   */
public void drawHoverNode()
  	{
    Overlapper bv=(Overlapper)applet;
	
    if(hoverNode!=null)
	  	{
	    int factor=1;
	    if(bv.isSizeRelevant())    	factor=hoverNode.clusters.size();
		    
	    Iterator<Node> itDrawMates=hoverNode.mates.values().iterator();
		Color c=bv.paleta[Overlapper.hoverColor];
		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), 0);
		bv.stroke(c.getRed(), c.getGreen(), c.getBlue(),255);
		bv.rectMode(JProcessingPanel.CENTER);
		//TODO: Excesivamente lento con clusters muy solapados
		while(itDrawMates.hasNext())
	  		{
	  		ForcedNode n=(ForcedNode)itDrawMates.next();
	  		if(n.clusters.size()>=bv.nodeThreshold && this.nodes.containsKey(n.label) && !nodeExcluded(n))
		  		{
	  			if(n.isGene())	bv.ellipse((float) n.getX(), (float) n.getY(), n.width, n.height);
	  			else			bv.rect((float) n.getX(), (float) n.getY(), n.width*factor, n.height*factor);
	  			n.setDrawn(true);
		  		}
			 }
	  	
	  	if(hoverNode!=null && !selectedNodes.containsKey(hoverNode.label))
		    	{
				int max=maxZones;
				int min=1;
				int ls=bv.getLabelSize();
				float maxLs=bv.getMaxLabelSize();
				double step=(maxLs-ls)/(max-min);
				double tam=ls+step*hoverNode.getClusters().size();
		    	if(bv.isAbsoluteLabelSize())	bv.textSize(ls+1);
	    		else				    		bv.textSize((int)tam+1);

	  	    	bv.fill(0,0,0,255);
		    	bv.text(hoverNode.label, (float)(hoverNode.position.getX()+0.5), (float)(hoverNode.position.getY()-hoverNode.getHeight()+0.5));
			    	
		    	bv.fill(255,255,255,255);
		    	bv.text(hoverNode.label, (float)hoverNode.position.getX(), (float)hoverNode.position.getY()-hoverNode.getHeight());
		    	}
	  	}
  	}
  
/**
 * Draws the Hover node, highlighted, and also highlights their neighbors (all the nodes directly connected to it)
 *
 */
public void drawHoverHull()
	{
  Overlapper bv=(Overlapper)applet;
	
  if(!bv.isDrawHull() && bv.isDrawTopography() && this.hoverClusters!=null)
	  	{
		Iterator<Cluster> itDrawMates=hoverClusters.values().iterator();
	  	while(itDrawMates.hasNext())
	  		{
	  		Cluster n=(Cluster)itDrawMates.next();
	  		if(n.hull!=null)
				n.drawHulls();
	  		 }
	  	}
	}

	/**
	 *	Draws node selected. Selected nodes also show their labels, on a different color 
	 *
	 */
  public void drawSelectedNodes()
  	{
	  Overlapper bv=(Overlapper)applet;
	  //-------------- selected nodes
	  Iterator<Node> itGraph=selectedNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		if(n.clusters.size()>=bv.nodeThreshold && !nodeExcluded(n))
			{
			applet.noFill();
			applet.stroke(200,200,0);
			applet.strokeWeight(2);
	
			bv.fill(0,0,0,255);
	    	
			int max=maxZones;
			int min=1;
			int ls=bv.getLabelSize();
			float maxLs=bv.getMaxLabelSize();
			double step=(maxLs-ls)/(max-min);
			double tam=ls+step*n.getClusters().size();
	    	if(bv.isAbsoluteLabelSize())	bv.textSize(ls+1);
    		else				    		bv.textSize((int)tam+1);
    	
	    	bv.text(n.getLabel(), (float)(n.getX()+0.5), (float)(n.getY()-n.getHeight()+0.5));
	
	    	
	    	if(n.isGene())	bv.fill(195, 250, 190, 255);
	    	else			bv.fill(165, 175, 250, 255);	
	    	bv.text(n.getLabel(), (float)n.getX(), (float)n.getY()-n.getHeight());
	    	
			if(selectedNodes.containsKey(n.label))
				{
				bv.noFill();
				bv.strokeWeight(3);
				Color c=bv.paleta[Overlapper.selectionColor];
				bv.stroke(c.getRed(), c.getGreen(), c.getBlue());
				if(n.isGene())	bv.ellipse((float)n.getX(), (float)n.getY(), n.height, n.width);
				else			bv.rect((float) n.getX(), (float) n.getY(), n.width, n.height);
				}

			}
		}
  	}
  
  void updateSelection(BiclusterSelection bs)
  	{
    Overlapper bv=(Overlapper)applet;
	selectedNodes.clear();
	selectedClusters.clear();
	ArrayList<String> b=null;
	if(bs.getConditions().size()>=bv.microarrayData.getNumConditions()-1)	
		b=bv.microarrayData.getNames(bs.getGenes(), new LinkedList<Integer>());	
	else
		b=bv.microarrayData.getNames(bs.getGenes(), bs.getConditions());
	for(int i=0;i<b.size();i++)
		{
		String l=b.get(i);
		Node n=nodes.get(l);
		if(n!=null)	selectedNodes.put(l, n);
		}
  	}
  
  /**
   * Draw searched Nodes. Searched nodes are surrounded by a circle
   *
   */
  public void drawSearchedNodes()
  	{	
	Overlapper bv=(Overlapper)applet;
	bv.rectMode(JProcessingPanel.CENTER);
	Iterator<Node> itGraph=searchNodes.values().iterator();
	while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		if(n.clusters.size()>=bv.nodeThreshold)
			{
			int x=(int)n.getX();
			int y=(int)n.getY();
			int w;
			if(bv.isSizeRelevant())	w=(int)(n.getWidth()*n.getClusters().size()+5);
			else					w=(int)(n.getWidth()+5);
			applet.noFill();
			//applet.stroke(200,200,0);
			Color c=bv.paleta[Overlapper.searchColor];
    		bv.stroke(c.getRed(), c.getGreen(), c.getBlue(),255);
    		
			applet.strokeWeight(2);
			applet.ellipse(x, y, w, w);
			}
		}
	applet.noStroke();
  	}
  
  /**
   * Draws the complete Graph, with all their compounds, at a specified order
   *
   */
  public void drawFull() {
	  Iterator itGraph=nodes.values().iterator();
	  while(itGraph.hasNext())	//Para evitar que se dibujen varias veces en caso de q est�n en varios clusters
	  	{	
		Node n =(Node)itGraph.next();
		n.setDrawn(false);
	  	}

	  Overlapper bv=(Overlapper)applet;
	  
	  itGraph=results.values().iterator();//Hull drawing
	  for (int i=0; i<results.size(); i++) 
	  	{
	      ClusterSet r = (ClusterSet)itGraph.next();
	      for(int j=0;j<r.getClusters().size();j++)
	      	{
	    	if(bv.isRadial())	
	    		{
	    		RadialCluster c=(RadialCluster)r.getClusters().get(j);
	    		c.draw(true,false);
		      	}
	    	else
	    		{
	    		MaximalCluster c=(MaximalCluster)r.getClusters().get(j);
	    		c.draw(true,false);
		      	}
	    	}
	    }

	  drawNodes();
	  if(bv.isShowEdges())
	  	{
		itGraph=edges.values().iterator();
		while(itGraph.hasNext())
			   {
			   Edge e=(Edge)itGraph.next();
		       e.draw();
		       }
		  
	  	itGraph=centerNodes.values().iterator();
		while(itGraph.hasNext())
		  	{
		    Node n=(Node)itGraph.next();
		    n.draw();
		    }
		}

	  
	  itGraph=centroidNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		int x=(int)n.getX();
		int y=(int)n.getY();
		applet.stroke(200,200,0);
		applet.strokeWeight(2);
		applet.line(x+10, y+10, x, y);
		}
	  
	  bv.rectMode(JProcessingPanel.CENTER);
	  itGraph=searchNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		int x=(int)n.getX();
		int y=(int)n.getY();
		int w=(int)(n.getWidth()*n.getClusters().size()+5);
		applet.noFill();
		applet.stroke(200,200,0);
		applet.strokeWeight(2);
		applet.ellipse(x, y, w, w);
		}
	  applet.noStroke();

	  //-------------- selected nodes
	  itGraph=selectedNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		applet.noFill();
		applet.stroke(200,200,0);
		applet.strokeWeight(2);

		bv.fill(0,0,0,255);
    	bv.textSize(bv.getLabelSize()+6);
    	bv.text(n.getLabel(), (float)(n.getX()+0.5), (float)(n.getY()-n.getHeight()+0.5));

    	
    	if(n.isGene())	bv.fill(195, 250, 190, 255);
    	else			bv.fill(165, 175, 250, 255);	
    	bv.textSize(bv.getLabelSize()+5);
    	bv.text(n.getLabel(), (float)n.getX(), (float)n.getY()-n.getHeight());
		}
	  
	  //----------------- hover node
	  if(hoverNode!=null)
	  	{
	    int factor=1;
	    if(bv.isSizeRelevant())    	factor=hoverNode.clusters.size();
		    
  		Iterator itDrawMates=hoverNode.mates.values().iterator();
    	while(itDrawMates.hasNext())
    		{
    		ForcedNode n=(ForcedNode)itDrawMates.next();
    		Color c=bv.paleta[bv.selectionColor];
    		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), 0);
			bv.stroke(c.getRed(), c.getGreen(), c.getBlue(),255);
    		
			bv.rectMode(JProcessingPanel.CENTER);
    		if(n.isGene())	bv.ellipse((float) n.getX(), (float) n.getY(), n.width, n.height);
    		else			bv.rect((float) n.getX(), (float) n.getY(), n.width*factor, n.height*factor);
    		n.setDrawn(true);
	    	}
    	if(!selectedNodes.containsKey(hoverNode.label))
	    	{
	    	bv.fill(0,0,0,255);
	    	bv.textSize(bv.getLabelSize()+hoverNode.getClusters().size()*3+1); 
	    	bv.text(hoverNode.label, (float)(hoverNode.position.getX()+0.5), (float)(hoverNode.position.getY()-hoverNode.getHeight()+0.5));
	
		    	
	    	bv.fill(255,255,255,255);
	    	bv.textSize(bv.getLabelSize()+hoverNode.clusters.size()*3); 
	    	bv.text(hoverNode.label, (float)hoverNode.position.getX(), (float)hoverNode.position.getY()-hoverNode.getHeight());
	    	}
    	}
	  
	  
	  applet.noStroke();
	  bv.rectMode(JProcessingPanel.CORNER);
  	}
  
  
  //------------------------------------- DRAW NODES ------------------------------
  //NOTA: de momento uso Cluster.draw() que parece m�s r�pido, aunque no entiendo por qu�, si en principio este
  //va con iterador y accede una sola vez a cada nodo
  /**
   * Draw the nodes in the Graph
   */
  public void drawNodes()
  	{
	  Overlapper bv=(Overlapper)this.getApplet();
	  Iterator<Node> itNodes=nodes.values().iterator();

	  final float env = 1.3f;
	  float ns=bv.getNodeSize();
	  int ls=bv.getLabelSize();
	  		
	  while(itNodes.hasNext())
	    {
		 Node n=(Node)itNodes.next();
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
				 
		        
		        //Para saber qu� porci�n de c�rculo toca;
		        float arc = Overlapper.TWO_PI / n.getClusters().size();
			    //Para hacer un sector por grupo al que pertenece
		        int inter=0;
		        if(bv.isOnlyIntersecting())	inter=1;
		        if(n.getClusters().size()>inter)
		        	{
			        Iterator<Cluster> itDraw=n.getClusters().values().iterator();
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
				        //bv.arc(x, y, senv, senv, (float)0.0, (float)(Math.PI/2.0));
				        
				        bv.stroke(255,255,255,255);
				        bv.strokeWeight(1);
					   	bv.line(x, y, (float)(x+ ns/2*Math.cos(arcj)), (float)(y+ ns/2*Math.sin(arcj)));		    
					    bv.fill(0,0,0,255);
					    }
		        	}
		        }
		    if (bv.isShowLabel() && n!=bv.g.getHoverNode() && !bv.g.getSelectedNodes().containsKey(n.getLabel()) && n.getDetails().length()==0 && n.getImage().length()==0)
		    	{
	    		bv.fill(0,0,0,255);
	    		if(ls+n.getClusters().size()*2>20)		bv.textSize(20);
	    		else									bv.textSize(ls+n.getClusters().size()*2); 
	        	bv.text(n.getLabel(), (float)(n.getX()+0.5), (float)(n.getY()-n.getHeight()+0.5));

	        	Color c=null;
	        	if(n.isGene())	
	        		{
	        		c=bv.paleta[bv.geneLabelColor];
	        		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	        		}
	        	else
	        		{
	        		c=bv.paleta[bv.conditionLabelColor];
	        		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	        		}

	    		if(bv.isAbsoluteLabelSize())	bv.textSize(ls+5);
	    		else						
	    			{
		    		if(ls+n.getClusters().size()*2>20)		bv.textSize(20);
		    		else									bv.textSize(ls+n.getClusters().size()*2);
	    			}
	    		bv.text(n.getLabel(), (float)n.getX(), (float)n.getY()-n.getHeight());
		    	}
		    
		    //Finalmente pintamos el nodo en s�
		    n.draw();

			}
	    }//if(punto en pantalla)
	    }
	  }  
  
  /**
   * Returns true if point is in the visible area (the area in which we are navigating now)
   * @param point	point to check if is in the area
   * @return true if point is in the visible area
   */
  public boolean pointInScreen(GraphPoint2D point)
	{
	  Overlapper bv=(Overlapper) this.getApplet();
	float x=-bv.getOffsetX();
	float y=-bv.getOffsetY();
	float w=bv.getScreenWidth();///bv.zoomFactor;
	float h=bv.getScreenHeight();//bv.zoomFactor;
	if(point.getX()>x && point.getX()<(x+w) && point.getY()>y && point.getY()<(y+h))	return true;
	else																				return false;
	}

  
  //-------------------- TRANSFORMACIONES A RADIAL O COMPLETO --------------------
  /**
   * Converts the Clusters in the Graph to maximal complete subgraphs
   */
  public void radial2complete()
  	{
	  edges.clear();
	  Iterator<ClusterSet> it=results.values().iterator();
	  while(it.hasNext())
		{
		ClusterSet r=(ClusterSet)it.next();
		ArrayList<Cluster> c=r.getClusters();
		for(int j=0;j<c.size();j++)
			{
			//Quitamos las aristas y el nodo central
			RadialCluster rc=(RadialCluster)c.get(j);
			deleteEdgesFor(rc.getCenterNode());
			rc.deleteCenterNode();
			//Construimos las aristas con el CompleteCluster
			ArrayList<Node> nodes=rc.getNodes();
			for(int k=0;k<nodes.size();k++)
				{
				Node nk=(Node)nodes.get(k);
				rc.buildEdges(nk);
				}
			}
		}
	centerNodes.clear();
	System.out.println("Radial to complete. Number of nodes: "+getNumNodes()+", number of edges: "+getNumEdges());
  	}
  
  /**
   * Converts the clusters in the graph to radial subgraphs, adding a center node
   */
  public void complete2radial()
  	{
		edges.clear();
		edgesTo.clear();
		edgesFrom.clear();
	Iterator<ClusterSet> it=results.values().iterator();
	while(it.hasNext())
		{
		ClusterSet r=(ClusterSet)it.next();
		ArrayList<Cluster> c=r.getClusters();
		for(int j=0;j<c.size();j++)
			{
			MaximalCluster cc=(MaximalCluster)c.get(j);//Los convertimos a radiales seg�n los cogemos
			RadialCluster rc=new RadialCluster(r, cc);
			double x,y;
			x=y=0;
			ArrayList<Node> pnodes=rc.getNodes();
			for(int k=0;k<pnodes.size();k++)
				{
				Node pn=(Node)pnodes.get(k);
				x+=pn.getX();
				y+=pn.getY();
				}
			GraphPoint2D pos=new GraphPoint2D(x/pnodes.size(),y/pnodes.size());
			ForcedNode nc=new ForcedNode(this, pos);
			nc.setLabel("c"+rc.getLabel());
			nc.setMass(rc.getNodes().size());
			rc.setCenterNode(nc);
			centerNodes.put(nc.getLabel(), nc);
			rc.buildEdges();
			c.set(j, rc);
			}
		}
	//buildPeripheryEdges();
	System.out.println("Complete to radial. Number of nodes: "+getNumNodes()+" (plus "+this.getNumCenterNodes()+" dummy nodes), number of edges: "+getNumEdges());
  	}
  
  
  //--------------------------- METRICS ---------------------------------------
  /**
   * Returns the value of a homemade measure to determine the degree of
   * mispositioning of nodes
   * The formula is: (n� of nodes placed upon a hull it is not in)/(n� of nodes)
   * 
   * It will change with time to more sophisticated one
   * Now it is
   *(n� of times a node sits on a non-corresponding hull)/(n� of possible missittings)
   *
   *The n� of possible misplacements is sum(numClusters-numClustersInWhichNodeKis)
   */
  public double getFailedPositionMetric()
  	{
	double metric=0;
	double den=0;
	Iterator<Node> itn=nodes.values().iterator();
	while(itn.hasNext())
		{
		Node n=(Node)itn.next();
		den-=n.clusters.size();
		Iterator<ClusterSet> it=results.values().iterator();
		while(it.hasNext())
			{
			ClusterSet r=(ClusterSet)it.next();
			ArrayList<Cluster> c=r.getClusters();
			for(int j=0;j<c.size();j++)
				{
				Cluster cc=(Cluster)c.get(j);
				if(!cc.getNodes().contains(n) && cc.hull!=null && cc.hull.contains(n.getX(),n.getY()))
					metric++;
				den++;
				}
			}
		}
	return metric/den;
  	}
  
  public double getFailedPositionMetricDual()
	{
	double metric=0;
	double den=0;
	Iterator<DualNode> itn=dualNodes.values().iterator();
	while(itn.hasNext())
		{
		DualNode n=(DualNode)itn.next();
		den-=n.clusters.size();
		Iterator<ClusterSet> it=results.values().iterator();
		while(it.hasNext())
			{
			ClusterSet r=(ClusterSet)it.next();
			ArrayList<Cluster> c=r.getClusters();
			for(int j=0;j<c.size();j++)
				{
				Cluster cc=(Cluster)c.get(j);
				//Si los clusters del nodo dual no tienen a este, y tiene hull y ese hull contiene al nodo dual
				//OJO: sigue haciendo las zonas con los nodos antiguos, no con los duales
				if(!n.clusters.containsKey(cc.label) && cc.hull!=null && cc.hull.contains(n.getX(),n.getY()))
					metric++;
				den++;
				}
			}
		}
	return metric/den;
	}

  public int getFailedPositionMetric(Node n)
  	{
	int metric=0;
	Iterator<ClusterSet> it=results.values().iterator();
	while(it.hasNext())
		{
		ClusterSet r=(ClusterSet)it.next();
		ArrayList<Cluster> c=r.getClusters();
		for(int j=0;j<c.size();j++)
			{
			Cluster cc=(Cluster)c.get(j);
			if(!cc.getNodes().contains(n) && cc.hull!=null && cc.hull.contains(n.getX(),n.getY()))
				metric++;
			}
		}
	return metric;
	}
  
  /**
   * Returns a measure of the variation of areas
   * 0 means no variation (all clusters have similar proportional sizes, great) and the larger possitive values, the worse it gets.
   * @return
   */
  public double getContourAreaMetric()
  	{
	double metric=0;
	double den=0;
	double mean=this.getAverageNormArea();
	Iterator<ClusterSet> it=this.results.values().iterator();
	while(it.hasNext())
		{	
		ClusterSet cs=it.next();
		Iterator<Cluster> it2=cs.getClusters().iterator();
		while(it2.hasNext())
			{
			Cluster c=it2.next();
			double ac=getAreaPerNode(c);
			metric+=(ac-mean)*(ac-mean);
			den+=ac;
			}
		}
	return metric/(den*den);
  	}
  
  /**
   * Returns the area of the hull for cluster c divided by the number of nodes in the cluster
   * The computation of the area is done by triangulations using Heron's formula
   * @param c Cluster to compute its area per node
   * @return	The area per node for the chosen cluster
   */
  public double getAreaPerNode(Cluster cl)
  	{
	double area=0;
	//By bounding boxes
	//Rectangle2D rec=cl.hull.getBounds2D();
	//double bb=rec.getHeight()*rec.getWidth()/cl.clusterNodes.size();
	
	//By triangulation
	double x0=cl.hull.xpoints[0];
	double y0=cl.hull.ypoints[0];
	Point2D.Double pc=cl.getMiddlePoint();
	double xc=pc.x;
	double yc=pc.y;
	
	for(int i=1;i<cl.hull.npoints;i++)
		{
		double x1=cl.hull.xpoints[i];
		double y1=cl.hull.ypoints[i];
		double a=Math.sqrt((x1-x0)*(x1-x0)+(y1-y0)*(y1-y0));
		double b=Math.sqrt((x0-xc)*(x0-xc)+(y0-yc)*(y0-yc));
		double c=Math.sqrt((x1-xc)*(x1-xc)+(y1-yc)*(y1-yc));
		double s=(a+b+c)/2;//half of perimeter
		//Heron of Alexandria formula
		area+=Math.sqrt(s*(s-a)*(s-b)*(s-c));
		x0=x1;
		y0=y1;
		}
	return area/cl.clusterNodes.size();
  	}

  public double getArea(Polygon p)
	{
	double area=0;
	
	//By triangulation
	double x0=p.xpoints[0];
	double y0=p.ypoints[0];
	Point2D.Double pc=this.getCenter(p);
	double xc=pc.x;
	double yc=pc.y;
	
	for(int i=1;i<p.npoints;i++)
		{
		double x1=p.xpoints[i];
		double y1=p.ypoints[i];
		double a=Math.sqrt((x1-x0)*(x1-x0)+(y1-y0)*(y1-y0));
		double b=Math.sqrt((x0-xc)*(x0-xc)+(y0-yc)*(y0-yc));
		double c=Math.sqrt((x1-xc)*(x1-xc)+(y1-yc)*(y1-yc));
		double s=(a+b+c)/2;//half of perimeter
		//Heron of Alexandria formula
		area+=Math.sqrt(s*(s-a)*(s-b)*(s-c));
		x0=x1;
		y0=y1;
		}
	return area;
	}

  /**
   * Returns the average area of the hull respect to the number of nodes contained 
   * (i.e. the average area per node)
   * It uses bounding boxes to compute hull's area instead of real area.
   * Other option is to do triangulations
   * 
   */
  public double getAverageNormArea()
  	{
	double metric=0;
	double den=0;
	Iterator<ClusterSet> it=results.values().iterator();
	while(it.hasNext())
		{
		ClusterSet r=(ClusterSet)it.next();
		ArrayList<Cluster> c=r.getClusters();
		for(int j=0;j<c.size();j++)
			{
			Cluster cc=(Cluster)c.get(j);
			metric+=getAreaPerNode(cc);
			den++;
			}
		}
	
	return metric/den;	  
  	}
  
  //----------------------------------------- CORRECTIONS ------------------------------------
  /**
   * For each node, checks if it is in a zone it does not pertain.
   * If it is, it applies a random force to the node.
   * @param m - strength of the correction
   */
  public void randomCorrection(int m)
  	{
  	Iterator<Node>	itRel=nodes.values().iterator();
  	while(itRel.hasNext())
  		{
  		ForcedNode n=(ForcedNode)itRel.next();
  		int fail=getFailedPositionMetric(n);
  		if(fail>0)
  			{
  			double magnitude=fail*m*Math.random();
  			double direction=Math.PI*2*Math.random();
  			n.getPosition().add(new GraphPoint2D(magnitude*Math.cos(direction), magnitude*Math.sin(direction)));
  			}
  		}
  	}

  public void drawErrors()
  	{
	Iterator<Node>	itRel=nodes.values().iterator();
	Overlapper bv=(Overlapper)this.getApplet();
  	while(itRel.hasNext())
  		{
  		ForcedNode n=(ForcedNode)itRel.next();
  		int fail=getFailedPositionMetric(n);
  		if(fail>0)
  			{
  			bv.strokeWeight(fail+1);
  			bv.strokeColor=new Color(255,0,0);
  			bv.noFill();
  		//bv.triangle((float)(n.getX()-n.width), (float)n.getY(), (float)(n.getX()+n.width), (float)n.getY(), (float)n.getX(), (float)(n.getY()-n.getHeight()));
  		    bv.ellipse((float)n.getX(), (float)n.getY(), n.width, n.height);
  			bv.strokeWeight(1);
  			bv.noStroke();
  			}
  		}
  	}  
  /**
   * For each node, checks if it is in a zone it does not pertain.
   * If it is, it increases its mass so the gravitational forces applied will be more effective.
   * The increase in mass is proportional to the number of wrong areas in wich it is
   * @param m - strength of the correction
   */
  public void massCorrection(int m)
  	{
  	Iterator<Node>	itRel=nodes.values().iterator();
  	while(itRel.hasNext())
  		{
  		ForcedNode n=(ForcedNode)itRel.next();
  		int fail=getFailedPositionMetric(n);
  		if(fail>0)
  			{
  			
  			n.setMass((fail+1)*m);
  			System.out.println("Cambiando masa de "+n.label+" a "+((fail+1)*m));
  			//double magnitude=fail*m*Math.random();
  			//double direction=Math.PI*2*Math.random();
  			//n.applyForce(new GraphPoint2D(magnitude*Math.cos(direction), magnitude*Math.sin(direction)));
  			//n.getPosition().add(new GraphPoint2D(magnitude*Math.cos(direction), magnitude*Math.sin(direction)));
  			}
  		}
  	}

  private void buildPeripheryEdges()
  	{
	//  System.out.println("BuildPeriphery");
	Iterator it=nodes.values().iterator();
	ForcedNode linkNode=null;
	int nc=0;
	int temp=0;
	
	while(it.hasNext())
		{
		linkNode=null;
		nc=0;
		temp=0;
		ForcedNode n=(ForcedNode)it.next();
		//System.out.println("Nodo "+n.label);
		if(!centerNodes.containsValue(n))
			{
			Iterator<Node> itc=n.mates.values().iterator();
			while(itc.hasNext())
				{
				ForcedNode n2=(ForcedNode)itc.next();
				if(!centerNodes.containsValue(n2) && n!=n2)//Si no es el central o �l mismo
					{
					temp=clustersInCommon(n,n2);
					TreeMap<String,Edge> te=getEdgesTo(n2);
					//System.out.println("N�mero de nodos que van a "+n2.label+" son "+te.size());
					if(temp>nc && te.size()<=1)	//Si no tiene ya a uno encadenado
						{
						linkNode=n2;
						}
					}
				}
			if(linkNode!=null)
				{
			//	System.out.println("***Enlazando "+n.label+" a "+linkNode.label);
				
				SpringEdge e=new SpringEdge(n,linkNode);
				addEdge(e);
				}
			}
		}
  	}
  
  
  private int clustersInCommon(Node a, Node b)
  	{
	Iterator<Cluster> it=a.clusters.values().iterator();
	int numClusters=0;
	while(it.hasNext())
		{
		Cluster c=(Cluster)it.next();
		if(b.clusters.containsValue(c))	numClusters++;
		}
	return numClusters;
  	}
  
  float graphMass()
  	{
	Iterator<Node> it=centerNodes.values().iterator();
	float gm=0;
	while(it.hasNext())
		{
		gm+=((ForcedNode)it.next()).getMass();
		}
	return gm;
  	}
  
  int clustersInCommon(DualNode a, DualNode b)
  	{
	Iterator<Cluster> it=a.clusters.values().iterator();
	int cc=0;
	while(it.hasNext())
		{
		Cluster c=it.next();
		if(b.clusters.containsKey(c.label))	cc++;
		}
	return cc;
  	}
  
  //-------------------- GETTERS Y SETTERS -------------------
  /**
   * Returns the applet in which this Graph is drawn
   * @return	the applet in which this Graph is to be visualized and managed
   */
  public Overlapper getApplet() {
  	return applet;
  }

  /**
   * Sets the applet in which this Graph is drawn
   * @param applet	the applet in which this Graph is to be visualized and managed
   */
  public void setApplet(Overlapper applet) {
  	this.applet = applet;
  }

  /**
   * Sets the complete group of nodes that will be in this graph
   * @param nodes	map with all the nodes. Keys are node labels
   */
public void setNodes(Map<String, Node> nodes) {
	this.nodes = nodes;
}

/**
 * Returns all edges in the graph as a map
 * @return	Map with all edges. Keys are Strings with edge labels
 */
public Map<String, Edge> getEdges() {
	return edges;
}

/**
 * Sets the complete group of edges that will be in this graph
 * @param edges	map with all the edges. Keys are edge labels
 */
public void setEdges(Map<String, Edge> edges) {
	this.edges = edges;
}

/**
 * Returns all clsuterSets as a map
 * @return	Map with all ClusterSets. Keys are Strings with cluster labels
 */
public Map<String, ClusterSet> getResults() {
	return results;
}

/**
 * Sets the complete group of clustersSets that will be in this graph
 * @param cs	map with all the cluster sets. Keys are clusters labels
 */
public void setClusterSets(Map<String, ClusterSet> cs) {
	this.results = cs;
}

/**
 * Returns all center nodes as a map
 * @return	Map with all center nodes. Keys are Strings with node labels
 */
public Map<String, Node> getCenterNodes() {
	return centerNodes;
}

/**
 * Sets the complete group of centerNodes that will be in this graph
 * @param centerNodes	map with all the center nodes. Keys are node labels
 */
public void setCenterNodes(Map<String, Node> centerNodes) {
	this.centerNodes = centerNodes;
}

/**
 * Returns all selected nodes as a map
 * @return	Map with all selected nodes. Keys are Strings with node labels
 */
public Map<String, Node> getSelectedNodes() {
	return selectedNodes;
}

/**
 * Gets the first selected node
 * @return	First Node selected
 */
public Node getFirstSelectedNode() {
	return (Node)selectedNodes.values().iterator().next();
}

/**
 * Sets the group of selected nodes
 * @param selectedNodes	map with all the selected nodes. Keys are node labels
 */
public void setSelectedNodes(Map<String, Node> selectedNodes) {
	this.selectedNodes = selectedNodes;
}

/**
 * Returns all selected clusters
 * @return	Map with all selected clusters. Keys are Strings with cluster labels
 */
public Map<String, Cluster> getSelectedClusters() {
	return selectedClusters;
}

/**
 * Returns all hover clusters
 * @return	Map with all hover clusters. Keys are Strings with cluster labels
 */
public Map<String, Cluster> getHoverClusters() {
	return hoverClusters;
}

/**
 * Returns the average degree of edges connected to a node
 * @return the average degree of edges connected to a node
 */
public float getConnectionDegree()
	{
	float ret=0;
	Iterator<Node> it=this.nodes.values().iterator();
	while(it.hasNext())
		{
		Node n=it.next();
		ret+=this.getEdgesFrom(n).size()+this.getEdgesTo(n).size();
		}
	return ret/this.nodes.size();
	}


/**
 * Returns the average number of biclusters in which a node is
 * @return the average number of biclusters in which a node is
 */
public float getBiclusterDegree()
	{
	float ret=0;
	Iterator<Node> it=this.nodes.values().iterator();
	while(it.hasNext())
		{
		Node n=it.next();
		ret+=n.clusters.size();
		}
	return ret/this.nodes.size();
	}

/**
 * Returns the average overlap between biclusters. For each bicluster, we sum the number of biclusters (apart from itself) in which is
 * each one of its nodes, then divide it by the number of nodes in the bicluster. 
 * Finally we make average for all biclusters.
 * @return the average number of biclusters in which a node is
 */
public float getAvgBiclusterOverlap()
	{
	float overlap1=0;
	float avgovp=0;
	
	Iterator<ClusterSet> it=this.results.values().iterator();
	while(it.hasNext())
		{
		ClusterSet cs=it.next();
		Iterator<Cluster> itc=cs.getClusters().iterator();
		while(itc.hasNext())
			{
			Cluster c=itc.next();
			for(int i=0;i<c.clusterNodes.size();i++)
				{
				Node n=c.clusterNodes.get(i);
				overlap1+=n.clusters.size()-1;
				}
			overlap1/=c.clusterNodes.size();
			avgovp+=overlap1;
			}
		}
	return avgovp/this.getApplet().numClusters;
	}

/**
 * Relocates nodes finding a better configuration following different metrics
 *(by now, only misplacement metric, but to have into account others, as distance to other members in their groups, could be advisable)
 * It moves each node an amount to each cardinal point, and takes the movement that most improves the metric.
 * If a move improves the metric, it modify the position accordingly.
 * After the movement, IF the movement has improved the metric, it decreases the jump by the coolStep and do a new iteration,
 * otherwise, we pass to another node
 * If permissive is set and the movement did not improve the metric, we apply the coolStem pand do a new iteration for this node,
 * instead of passing to another node
 */
/*
 * Versi�n 1 iteraci�n para cada nodo, 1 paso
public void fastHillClimberRelocation(double jump, double coolStep, boolean permissive)
	{
	double start=System.currentTimeMillis();
	boolean stop=false;
	double step=jump;
	double itFail=this.getFailedPositionMetric();
	System.out.println("Error antes de correcci�n "+itFail);
	int priorities[]={Graph.HULL, Graph.NODE};
	
	while(step>0)
	{
	Iterator<Node> it=nodes.values().iterator();
	while(it.hasNext())
		{
		Node n=it.next();
		double totalFail=this.getFailedPositionMetric();
		GraphPoint2D pant=new GraphPoint2D(n.getX(), n.getY());
		double totalFailDir[]=new double[4];//0-east, 1-west, 2-south, 3-north

		//east
		n.setX((float)(n.getX()+step));
		this.draw(priorities);
		totalFailDir[0]=this.getFailedPositionMetric();
		
		//west
		n.setX((float)(n.getX()-2*step));
		this.draw(priorities);
		totalFailDir[1]=this.getFailedPositionMetric();
		
		//south
		n.setX(pant.getX());
		n.setY((float)(n.getY()+step));
		this.draw(priorities);
		totalFailDir[2]=this.getFailedPositionMetric();
		
		//north
		n.setY(n.getY()-2*step);
		this.draw(priorities);
		totalFailDir[3]=this.getFailedPositionMetric();
		n.setY(n.getY()+step);
		
		int min=0;
		for(int i=1;i<4;i++)
		if(totalFailDir[i]<totalFailDir[min])	min=i;
		if(totalFailDir[min]<totalFail)	
			{
			System.out.println("Modificado "+n.label+" a "+min+" distancia "+step+" ("+totalFail+"-->"+totalFailDir[min]);
			switch(min)
				{
				case 0:
					n.setX((float)(n.getX()+step));
					break;
				case 1:
					n.setX((float)(n.getX()-step));
					break;
				case 2:
					n.setY((float)(n.getY()+step));
					break;
				case 3:
					n.setY((float)(n.getY()-step));
					break;
				}
			}
		draw(priorities);
		}//for each node
	step-=coolStep;
	System.out.println("new step "+step);
	}//for each step
	double end=System.currentTimeMillis();
	System.out.println("Improvement finished!: "+(end-start)/1000+" s");
	System.out.println("Error tras correcci�n "+this.getFailedPositionMetric());
	}
*/
//Versi�n 1 iteraci�n para cada nodo -> todos los pasos (de momento parece m�s r�pida y da mejores resultados)
public void fastHillClimberRelocation(double jump, double coolStep, boolean permissive, int[] metrics, double[] weights)
	{
	double start=System.currentTimeMillis();
	boolean stop=false;
	double step=jump;
	//double itFail=this.getFailedPositionMetric();
	double itFail=getMetric(metrics, weights);
	
	System.out.println("Error antes de correcci�n "+itFail);
	int priorities[]={Graph.HULL, Graph.NODE};
	
	Iterator<Node> it=nodes.values().iterator();
	//LinkedList<Node> list=sortNodes(0);
	while(it.hasNext())
	//for(int k=0;k<list.size();k++)
		{
		Node n=it.next();
		//Node n=list.get(k);
		step=jump;
		stop=false;
		while(!stop)
			{
			//double totalFail=this.getFailedPositionMetric();
			double totalFail=this.getMetric(metrics, weights);
			GraphPoint2D pant=new GraphPoint2D(n.getX(), n.getY());
			double totalFailDir[]=new double[4];//0-east, 1-west, 2-south, 3-north

			//east
			n.setX((float)(n.getX()+step));
			this.draw(priorities);
			//totalFailDir[0]=this.getFailedPositionMetric();
			totalFailDir[0]=this.getMetric(metrics, weights);
				
			//west
			n.setX((float)(n.getX()-2*step));
			this.draw(priorities);
			//totalFailDir[1]=this.getFailedPositionMetric();
			totalFailDir[1]=this.getMetric(metrics, weights);
			
			//south
			n.setX(pant.getX());
			n.setY((float)(n.getY()+step));
			this.draw(priorities);
			//totalFailDir[2]=this.getFailedPositionMetric();
			totalFailDir[2]=this.getMetric(metrics, weights);
			
			//north
			n.setY(n.getY()-2*step);
			this.draw(priorities);
			//totalFailDir[3]=this.getFailedPositionMetric();
			totalFailDir[3]=this.getMetric(metrics, weights);
			n.setY(n.getY()+step);
			
			int min=0;
			for(int i=1;i<4;i++)
			if(totalFailDir[i]<totalFailDir[min])	min=i;
			if(totalFailDir[min]>=totalFail)	
				{//No improvement, ending
				if(!permissive)	stop=true;
				step-=5*coolStep;
				}
			else
				{
				System.out.println("Modificado "+n.label+", con conectividad "+n.clusters.size()+" a "+min+" distancia "+step+" ("+totalFail+"-->"+totalFailDir[min]);
				switch(min)
					{
					case 0:
						n.setX((float)(n.getX()+step));
						break;
					case 1:
						n.setX((float)(n.getX()-step));
						break;
					case 2:
						n.setY((float)(n.getY()+step));
						break;
					case 3:
						n.setY((float)(n.getY()-step));
						break;
					}
				step-=coolStep;
				}
			if(step<=0)	stop=true;
			draw(priorities);
			}
		}
	double end=System.currentTimeMillis();
	System.out.println("Improvement finished!: "+(end-start)/1000+" s");
	//System.out.println("Error tras correcci�n "+this.getFailedPositionMetric());
	System.out.println("Error tras correcci�n "+this.getMetric(metrics, weights));
	}

/**
 * Trying to apply heuristics to relocation of nodes
 */
public void intelligentFastHill()
	{
	//Voy a probar simplemente a mover a la zona a la que pertenecen a cada nodo que est� mal posicionado
	double start=System.currentTimeMillis();
	double itFail=this.getFailedPositionMetric();
	Overlapper ov=this.getApplet();

	System.out.println("Error antes de correcci�n "+itFail);
	
	Iterator<Node> it=nodes.values().iterator();
	while(it.hasNext())
		{
		Node n=it.next();
		double fail=this.getFailedPositionMetric(n);
	//	if(fail>0)
			{
			correctPosition(n);
			}
		}
	ov.paintComponent(ov.getGraphics());
	double end=System.currentTimeMillis();
	System.out.println("Improvement finished!: "+(end-start)/1000+" s");
	System.out.println("Error tras correcci�n "+this.getFailedPositionMetric());
	}

/**
 * Search for the correct zone for this node and put it inside, at the center of the zone 
 */
public void correctPosition(Node n)
	{
	System.out.println("---Corrigiendo posici�n de "+n.label+" en "+n.clusters.size()+" contornos");
	Iterator<Cluster> it=n.clusters.values().iterator();
	Polygon p1=it.next().hull;
	if(n.clusters.size()>1)//Si est� en m�s de un cluster, la intersecci�n de sus contornos
		{
		while(it.hasNext())
			{
			p1=intersect(p1,it.next().hull);
			}
		if(p1.npoints>0)
			{
			Point2D.Double pos=getCenter(p1);
			n.setPosition((float)pos.x, (float)pos.y);
			System.out.println("La intersecci�n tiene un �rea de "+getArea(p1)+" con "+p1.npoints+" puntos, colocamos en "+pos.x+", "+pos.y);
			}
		else	System.out.println("�rea de intersecci�n nula!");
		}
	else	System.out.println("Es un nodo en un solo �rea");
	}

/**
 * Returns the center point of a polygon
 * @param p Polygon to get the center
 * @return
 */
public Point2D.Double getCenter(Polygon p)
	{
	int x=0;
	int y=0;
	for(int i=0;i<p.npoints;i++)
		{
		x+=p.xpoints[i];
		y+=p.ypoints[i];
		}
	return new Point2D.Double(x/p.npoints,y/p.npoints);
	}
/*
public static Polygon intersect(Polygon p1, Polygon p2)
	{
	Polygon p=new Polygon();
	Polygon psin1=new Polygon();
	Polygon psin2=new Polygon();
	Polygon pi=new Polygon();
	
	for(int i=0;i<p1.xpoints.length-1;i++)//Compute the intersection points of lines
		{
		double x1=p1.xpoints[i];
		double y1=p1.ypoints[i];
		double x2=p1.xpoints[i+1];
		double y2=p1.ypoints[i+1];
		double minx1=Math.min(x1, x2);
		double maxx1=Math.max(x1, x2);
		double miny1=Math.min(y1, y2);
		double maxy1=Math.max(y1, y2);
		boolean intersected=false;
		
		for(int j=0;j<p2.xpoints.length-1;j++)
			{
			double x3=p2.xpoints[j];
			double y3=p2.ypoints[j];
			double x4=p2.xpoints[j+1];
			double y4=p2.ypoints[j+1];
			double minx2=Math.min(x3, x4);
			double maxx2=Math.max(x3, x4);
			double miny2=Math.min(y3, y4);
			double maxy2=Math.max(y3, y4);

			//System.out.println("Chequeando "+x1+", "+y1+"-->"+x2+", "+y2+" con "+x3+", "+y3+"-->"+x4+", "+y4);
			//line-line intersection
			double den=((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
			if(den!=0)
				{
				double xi=((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/den;
				double yi=((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/den;
				//System.out.println("punto de intersecci�n: "+xi+", "+yi);//OJO, da el punto prolongando las l�neas!
				if(xi>=minx1 && xi>=minx2 && xi<=maxx1 && xi<=maxx2 && yi>=miny1 && yi>=miny2 && yi<=maxy1 && yi<=maxy2 )
					{
					//System.out.println("Es punto realmente de intersecci�n de los segmentos");
					p.addPoint((int)xi, (int)yi);
					intersected=true;
					}
				}
			}
		}
	return p;
	}*/
/*
 * Version with gishur package (it seems not to work when using points of Overlapper, but it works with simpler examples!)
 * That is because it fails if two intersecting polygons share an edge (if they have parallel edges?)
 */

public static Polygon intersect(Polygon p1, Polygon p2)
	{
	Polygon p=new Polygon();
	
	XPoint[] puntos1=new XPoint[p1.npoints];
	XPoint[] puntos2=new XPoint[p2.npoints];
	//ArrayList<XPoint> puntos1=new ArrayList<XPoint>();
	//ArrayList<XPoint> puntos2=new ArrayList<XPoint>();
	
	for(int i=0;i<p1.npoints-1;i++)	puntos1[i]=new XPoint(p1.xpoints[i], p1.ypoints[i]);//Hasta npoints-1 porque el �ltimo punto es el primero
	for(int i=0;i<p2.npoints-1;i++)	puntos2[i]=new XPoint(p2.xpoints[i], p2.ypoints[i]);
	XPolygon xp1=new XPolygon(puntos1);
	XPolygon xp2=new XPolygon(puntos2);
	/*for(int i=0;i<p1.npoints-1;i++)	puntos1.add(new XPoint(p1.xpoints[i], p1.ypoints[i]));//Hasta npoints-1 porque el �ltimo punto es el primero
	for(int i=0;i<p2.npoints-1;i++)	
		{//Aseguramos que no se repinten puntos, pues esto da problemas al algoritmo de intersecci�n
		XPoint punto=new XPoint(p2.xpoints[i], p2.ypoints[i]);
		while(puntos1.contains(punto))	{punto.x=punto.x+5;}
		puntos2.add(punto);
		}
	
	XPolygon xp1=new XPolygon((XPoint[])puntos1.toArray());
	XPolygon xp2=new XPolygon((XPoint[])puntos2.toArray());
*/
	
	if(xp1==null || xp2==null)	System.out.println("Error, uno de los pol�gonos es nulo!!!");
	SimpleList l=xp1.polygonIntersection(xp2);
	System.out.println("Intersecci�n de "+l.length()+" poligonos");
	XPolygon xp=(XPolygon)l.getValueAt(0);
	System.out.println("Pol�gono de "+xp.countBorderSegments()+" segmentos");
	
	XSegment s=xp.borderSegment(0);
	XPoint punto1=s.getStartPoint();
	p.addPoint((int)punto1.x, (int)punto1.y);
	
	for(int i=0;i<xp.countBorderSegments()-1;i++)
		{
		s=xp.borderSegment(i);
		XPoint punto2=s.getEndPoint();
		
		p.addPoint((int)punto2.x, (int)punto2.y);
		//System.out.println("s"+i+": de "+punto1.x+", "+punto1.y+" --> "+punto2.x+", "+punto2.y);
		System.out.println("s"+i+": de "+punto1.x+", "+punto1.y+" --> "+punto2.x+", "+punto2.y);
		}
	//}catch(NullPointerException e){e.printStackTrace(); return null;}
	return p;
	}

/*
public static Polygon intersect(Polygon p1, Polygon p2)
	{
	Polygon p=new Polygon();
	Polygon psin1=new Polygon();
	Polygon psin2=new Polygon();
	Polygon pi=new Polygon();
	//Overlapper ov=this.getApplet();
	ArrayList<Integer> lineas1=new ArrayList<Integer>();//Identificador de v�rtice de inicio de l�nea en las que se producen las intersecciones 
	ArrayList<Integer> lineas2=new ArrayList<Integer>(); 
	
	for(int i=0;i<p1.xpoints.length;i++)//Compute the intersection points of lines
		{
		double x1=p1.xpoints[i];
		double y1=p1.ypoints[i];
		double x2=0;
		double y2=0;
		if(i<p1.xpoints.length-1)	
			{
			x2=p1.xpoints[i+1];
			y2=p1.ypoints[i+1];
			}
		else
			{
			x2=p1.xpoints[0];
			y2=p1.ypoints[0];
			}
		
		double minx1=Math.min(x1, x2);
		double maxx1=Math.max(x1, x2);
		double miny1=Math.min(y1, y2);
		double maxy1=Math.max(y1, y2);
		
		int intersected=0;//Counts the number of intersections with this side of the polygon
		double xi=0;
		double yi=0;
		double x3=0;
		double y3=0;
		double x4=0;
		double y4=0;
		
		
		for(int j=0;j<p2.xpoints.length;j++)
			{
			x3=p2.xpoints[j];
			y3=p2.ypoints[j];
			if(j<p2.xpoints.length-1)
				{
				x4=p2.xpoints[j+1];
				y4=p2.ypoints[j+1];
				}
			else
				{
				x4=p2.xpoints[0];
				y4=p2.ypoints[0];
				}
			
			double minx2=Math.min(x3, x4);
			double maxx2=Math.max(x3, x4);
			double miny2=Math.min(y3, y4);
			double maxy2=Math.max(y3, y4);
	
			//System.out.println("Chequeando "+x1+", "+y1+"-->"+x2+", "+y2+" con "+x3+", "+y3+"-->"+x4+", "+y4);
			//line-line intersection
			double den=((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
			if(den!=0)
				{
				xi=((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/den;
				yi=((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/den;
				//System.out.println("punto de intersecci�n: "+xi+", "+yi);//OJO, da el punto prolongando las l�neas!
				if(xi>=minx1 && xi>=minx2 && xi<=maxx1 && xi<=maxx2 && yi>=miny1 && yi>=miny2 && yi<=maxy1 && yi<=maxy2 )
					{
					//System.out.println("Es punto realmente de intersecci�n de los segmentos");
					p.addPoint((int)xi, (int)yi);
					lineas1.add(i);
					lineas2.add(j);
					intersected++;
					}
				}
			}
		}
	
	//Build the final polygon from the intersection points in p and the polygons p1 and p2
	Polygon np=new Polygon();
	for(int i=0;i<p.npoints-1;i++)
		{
		int k1, k2;
		if(i%2==0)
			{
			k1=lineas1.get(i);//First line intersected
			if(lineas1.size()<(i+1))	k2=lineas1.get(i+1);//Second line intersected
			else						k2=lineas1.get(0);
			}
		else
			{
			k1=lineas2.get(i);//First line intersected
			if(lineas2.size()<(i+1))	k2=lineas2.get(i+1);//Second line intersected
			else						k2=lineas2.get(0);
			}
			
		np.addPoint(p.xpoints[i], p.ypoints[i]);
		for(int j=k1+1;j<k2+1;j++)
			{
			if(i%2==0)	np.addPoint(p1.xpoints[j], p1.ypoints[j]);
			else		np.addPoint(p2.xpoints[j], p2.ypoints[j]);
			}
		}
	
	for(int i=0;i<np.npoints;i++)
		System.out.println("p"+i+": "+np.xpoints[i]+", "+np.ypoints[i]);

	return p;
	}
*/


//Versi�n original que por ahora es la que m�s convence
public void fastHillClimberRelocation(double jump, double coolStep, boolean permissive)
{
double start=System.currentTimeMillis();
boolean stop=false;
double step=jump;
double itFail=this.getFailedPositionMetric();

System.out.println("Error antes de correcci�n "+itFail);
int priorities[]={Graph.HULL, Graph.NODE};

Iterator<Node> it=nodes.values().iterator();
while(it.hasNext())
	{
	Node n=it.next();
	step=jump;
	stop=false;
	while(!stop)
		{
		double totalFail=this.getFailedPositionMetric();
		GraphPoint2D pant=new GraphPoint2D(n.getX(), n.getY());
		double totalFailDir[]=new double[4];//0-east, 1-west, 2-south, 3-north

		//east
		n.setX((float)(n.getX()+step));
		this.draw(priorities);
		totalFailDir[0]=this.getFailedPositionMetric();
			
		//west
		n.setX((float)(n.getX()-2*step));
		this.draw(priorities);
		totalFailDir[1]=this.getFailedPositionMetric();
		
		//south
		n.setX(pant.getX());
		n.setY((float)(n.getY()+step));
		this.draw(priorities);
		totalFailDir[2]=this.getFailedPositionMetric();
		
		//north
		n.setY(n.getY()-2*step);
		this.draw(priorities);
		totalFailDir[3]=this.getFailedPositionMetric();
		n.setY(n.getY()+step);
		
		int min=0;
		for(int i=1;i<4;i++)
		if(totalFailDir[i]<totalFailDir[min])	min=i;
		if(totalFailDir[min]>=totalFail)	
			{//No improvement, ending
			if(!permissive)	stop=true;
			step-=5*coolStep;
			}
		else
			{
			System.out.println("Modificado "+n.label+", con conectividad "+n.clusters.size()+" a "+min+" distancia "+step+" ("+totalFail+"-->"+totalFailDir[min]);
			switch(min)
				{
				case 0:
					n.setX((float)(n.getX()+step));
					break;
				case 1:
					n.setX((float)(n.getX()-step));
					break;
				case 2:
					n.setY((float)(n.getY()+step));
					break;
				case 3:
					n.setY((float)(n.getY()-step));
					break;
				}
			step-=coolStep;
			}
		if(step<=0)	stop=true;
		draw(priorities);
		}
	}
double end=System.currentTimeMillis();
System.out.println("Improvement finished!: "+(end-start)/1000+" s");
System.out.println("Error tras correcci�n "+this.getFailedPositionMetric());
}

public void fastHillClimber2()
	{
	double start=System.currentTimeMillis();
	double dist=0;
	double minDist=0;
	Cluster minC=null;
	Line2D.Double minLine=null;
	double itFail=this.getFailedPositionMetric();

	System.out.println("Error antes de correcci�n "+itFail);
	int priorities[]={Graph.HULL, Graph.NODE};

	Iterator<Node> it=nodes.values().iterator();
	while(it.hasNext())
		{
		Node n=it.next();
		double nodeFail=this.getFailedPositionMetric(n);
		if(nodeFail>0)//De momento s�lo para los nodos fallidos, que si no puede ser mucha carga
			{
			System.out.println("Mejorando "+n.label+", en posici�n "+n.getX()+", "+n.getY());
			dist=minDist=100000000;
			minLine=new Line2D.Double();
			minC=null;
			double x0=n.getX();
			double y0=n.getY();
			Iterator<ClusterSet> ics=this.results.values().iterator();
			while(ics.hasNext())//Buscamos la l�nea m�s pr�xima del contorno m�s proximo a nuestro nodo
				{
				ClusterSet cs=ics.next();
				
				for(int i=0;i<cs.getClusters().size();i++)
					{
					Cluster c=cs.getClusters().get(i);
					if(!c.getNodes().contains(n) && c.hull!=null)
						{
						double x1=c.hull.xpoints[0];
						double y1=c.hull.ypoints[0];
						for(int j=1;j<c.hull.npoints;j++)
							{
							double x2=c.hull.xpoints[j];
							double y2=c.hull.ypoints[j];
							Line2D.Double linea=new Line2D.Double(x1,y1,x2,y2);
							dist=linea.ptSegDist(x0, y0);
							if(dist<minDist)	
								{
								minDist=dist;
								minLine=linea;
								minC=c;
								}
							
							x1=x2;
							y1=y2;
							}
						}
					}
				}
			System.out.println("El segmento m�s cercano no incluido es el que va de "+minLine.getX1()+", "+minLine.getY1()+" a "+minLine.getX2()+", "+minLine.getY2()+"; con distancia"+minDist+"; del cluster "+minC.label);
			double x1=minLine.getX1();
			double y1=minLine.getY1();
			double x2=minLine.getX2();
			double y2=minLine.getY2();
			//perpendicular line to p1->p2 passing by p0 has same slope, but negative
			double m=-(y1-y2)/(x1-x2);
			//point-slope line formula: y-y0=m(x-x0); used to determine a second point in perpendicular line
			//the slope of the perpendicular 
			double x4,y4;
			if(x0!=0)	
				{
				x4=0;
				y4=(-m*x0+y0);//the first '-' is because of java y coordinates, growing downwards
				}
			else
				{
				x4=1;
				y4=(m*(1-x0)+y0);
				}
			Point2D.Double pi=lineIntersection(x1,y1,x2,y2,x0,y0,x4,y4);
			if(pi!=null)
				{
				System.out.println("El punto de intersecci�n con prolongaci�n de "+x0+", "+y0+" es "+pi.x+", "+pi.y);
				if(x0<pi.x)	n.setX(pi.x+1);
				else		n.setX(pi.x-1);//TODO: esto no es muy exacto, deber�a ser en oblicuo
				if(y0<pi.y)	n.setX(pi.y+1);
				else		n.setX(pi.y-1);//TODO: esto no es muy exacto, deber�a ser en oblicuo
				draw(priorities);
				double newFail=this.getFailedPositionMetric();
				if(newFail<itFail)		
					{
					System.out.println("Moviendo "+n.label+" para pasar a error "+newFail);
					itFail=newFail;
					}
				else
					{
					draw(priorities);
					n.setX(x0);
					n.setY(y0);
					}
				}
			}
		}
	double end=System.currentTimeMillis();
	System.out.println("Improvement finished!: "+(end-start)/1000+" s");
	System.out.println("Error tras correcci�n "+this.getFailedPositionMetric());
	
	}

public Point2D.Double lineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
	//line-line intersection point
	//http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
	double den=(y4-y3)*(x2-x1)-(x4-x3)*(y2-y1);
	double numa=(x4-x3)*(y1-y3)-(y4-y3)*(x1-x3);
	double numb=(x2-x1)*(y1-y3)-(y2-y1)*(x1-x3);
	double ua=numa/den;
	double ub=numb/den;
	if(numa==0 && numb==0)	
		{
		System.err.println("Las l�neas coinciden");
		return null;
		}
	if((ua>=0 && ua<=1 && ub>=0 && ub<=1))
		{
		System.err.println("Las l�neas"+x1+", "+y1+" -> "+x2+", "+y2+" y "+x3+", "+y3+" -> "+x4+", "+y4+" son paralelas");
		return null;
		}
	double xi=x1+ua*(x2-x1);
	double yi=y1+ub*(y2-y1);
	return new Point2D.Double(xi,yi);
	}
	
public double pointLineDistance(double x1, double y1, double x2, double y2, double x0, double y0)
	{
	//point-line distance
	//http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
	double den=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
	double dist=Math.abs((x2-x1)*(y1-y0)-(x1-x0)*(y2-y1))/den;
	return dist;
	}
		
//Adaptaci�n a dual de la versi�n original
public void fastHillClimberRelocationDual(double jump, double coolStep, boolean permissive)
{
double start=System.currentTimeMillis();
boolean stop=false;
double step=jump;
double itFail=this.getFailedPositionMetricDual();

System.out.println("Error antes de correcci�n (dual) "+itFail);
int priorities[]={Graph.HULL, Graph.NODE};

Iterator<DualNode> it=dualNodes.values().iterator();
while(it.hasNext())
	{
	DualNode n=it.next();
	step=jump;
	stop=false;
	while(!stop)
		{
		double totalFail=this.getFailedPositionMetricDual();
		GraphPoint2D pant=new GraphPoint2D(n.getX(), n.getY());
		double totalFailDir[]=new double[4];//0-east, 1-west, 2-south, 3-north

		//east
		n.setX((float)(n.getX()+step));
		this.draw(priorities);
		totalFailDir[0]=this.getFailedPositionMetricDual();
			
		//west
		n.setX((float)(n.getX()-2*step));
		this.draw(priorities);
		totalFailDir[1]=this.getFailedPositionMetricDual();
		
		//south
		n.setX(pant.getX());
		n.setY((float)(n.getY()+step));
		this.draw(priorities);
		totalFailDir[2]=this.getFailedPositionMetricDual();
		
		//north
		n.setY(n.getY()-2*step);
		this.draw(priorities);
		totalFailDir[3]=this.getFailedPositionMetricDual();
		n.setY(n.getY()+step);
		
		int min=0;
		for(int i=1;i<4;i++)
		if(totalFailDir[i]<totalFailDir[min])	min=i;
		if(totalFailDir[min]>=totalFail)	
			{//No improvement, ending
			if(!permissive)	stop=true;
			step-=5*coolStep;
			}
		else
			{
			System.out.println("Modificado "+n.label+", con conectividad "+n.clusters.size()+" a "+min+" distancia "+step+" ("+totalFail+"-->"+totalFailDir[min]);
			switch(min)
				{
				case 0:
					n.setX((float)(n.getX()+step));
					break;
				case 1:
					n.setX((float)(n.getX()-step));
					break;
				case 2:
					n.setY((float)(n.getY()+step));
					break;
				case 3:
					n.setY((float)(n.getY()-step));
					break;
				}
			step-=coolStep;
			}
		if(step<=0)	stop=true;
		draw(priorities);
		}
	}
double end=System.currentTimeMillis();
System.out.println("Improvement finished!: "+(end-start)/1000+" s");
System.out.println("Error tras correcci�n (dual) "+this.getFailedPositionMetricDual());
}


void gaRelocation()
	{
	//GraphGeneticAlgorithm gag=new GraphGeneticAlgorithm(100,60,10,0.8,0,0,0,this);
	//int numPop=this.nodes.size()*4;
	//int numParents=(int)(0.1*numPop);
	//int numMutant=(int)(0.4*numPop);
	int numPop=400;
	int numParents=(int)(0.10*numPop);
	int numMutant=(int)(0.4*numPop);
	GraphGeneticAlgorithm gag=new GraphGeneticAlgorithm(numPop,numParents,numMutant,0.8,0,0,0,this);
	//GAThread gat=new GAThread(gag,1000,(float)0.001);
	//gat.start();
	System.out.println("Error antes de GA "+this.getFailedPositionMetric());
	gag.ejecutar(5, 0.001);
	System.out.println("Error tras GA "+this.getFailedPositionMetric());
	this.getApplet().paintComponent(this.getApplet().getGraphics());
	}

double getMetric(int[] metrics, double[] weights)
	{
	double m=0;
	for(int i=0;i<metrics.length;i++)
		{
		switch(metrics[i])
			{
			case MISPLACEMENT:
				m+=weights[i]*this.getFailedPositionMetric();
				break;
			case AREA:
				m+=weights[i]*this.getContourAreaMetric();
				break;
			case ROUNDNESS_AREA:
				break;
			case LENGTH_AREA:
				break;
				
			}
		}
	return m;
	}

/**
 * Gets the nodes in the graph, ordered by some criterion (0-connectivity, 1-outermost node in some bicluster)
 * @param criterion
 * @return
 */
LinkedList<Node> sortNodes(int criterion)
	{
	LinkedList<Node> l=new LinkedList<Node>();
	Iterator<Node> it=this.nodes.values().iterator();
	while(it.hasNext())
		{
		Node n=it.next();
		int i;
		for(i=0;i<l.size();i++)
			{
			Node n2=l.get(i);
			if(n.clusters.size()>n2.clusters.size())	
				{
				l.add(i, n);
				break;
				}
			}
		if(i==l.size())		l.addLast(n);
		}
	
	LinkedList<Node> l2=new LinkedList<Node>();
	for(int i=l.size()-1;i>=0;i--)		l2.add(l.get(i));
	l=l2;
	for(int i=0;i<l.size();i++)	System.out.print(l.get(i).clusters.size()+" ");
	return l;
	}

/**
 * Builds a complete dual graph from the initial graph, saving it in dualNodes and dualEdges
 * We call it complete dual because the nodes are those of a dual graph, but the edges join every node in the dual
 * graph that corresponds to the same group, so it forms complete 'dual' subgraphs.
 */
void buildCompleteDualGraph()
	{
    dualNodes = new HashMap<Integer, DualNode>();
    dualEdges = new HashMap<Integer, Edge>();
    HashMap<Integer, Map<String, Cluster>> zones=new HashMap<Integer, Map<String, Cluster>>();
	
    //1) Adding nodes
    Iterator<Node> it=nodes.values().iterator();
    int cont=0;
	while(it.hasNext())
		{
		Node n=it.next();
		if(!zones.containsKey(n.clusters.hashCode()))
			{
			zones.put(new Integer(n.clusters.hashCode()), n.clusters);
			TreeMap<String, Node> m=getDualMates(n);
			DualNode dn=new DualNode(this,n.clusters,m);
			dn.label=(cont++)+"";
			dn.setMass(m.size());
			dualNodes.put(dn.hashCode(), dn);
			Iterator<Cluster> itc=n.clusters.values().iterator();
			while(itc.hasNext())
				{
				Cluster c=itc.next();
				c.clusterDualNodes.add(dn);
				}
			}
		}
	//2) Adding edges
	/*
	Object[] lista=dualNodes.values().toArray();
	for(int i=0;i<dualNodes.size();i++)
		{
		//DualNode dn=it2.next();
		DualNode dn=(DualNode)(lista[i]);
	//	Iterator<DualNode> it3=dualNodes.values().iterator();
		//while(it3.hasNext())
		for(int j=i+1;j<dualNodes.size();j++)
			{
			//DualNode dn2=it3.next();
			DualNode dn2=(DualNode)(lista[j]);
			//if(dn!=dn2)
				{
				Iterator<Cluster> itbic=dn.clusters.values().iterator();
				while(itbic.hasNext())
					{
					Cluster c=itbic.next();
					if(dn2.clusters.containsValue(c))//Coinciden, as� que creamos una arista entre ellos	
						{
						SpringEdge e=new SpringEdge(dn, dn2);
						e.setGraph(this);
						e.nl=this.getApplet().getEdgeLength()+(dn.getGroupRadius()+dn2.getGroupRadius());
						e.k=this.getApplet().getStiffness()*dn.subNodes.size()*dn2.subNodes.size()*2;
						//System.out.println("Longitud para edge de "+e.nl+", radios de "+dn.getGroupRadius()+" y "+dn2.getGroupRadius());
						if(!dualEdges.containsValue(e) && !dualEdges.containsValue(new SpringEdge(dn2, dn)))	
							{
							System.out.println("A�adiendo arista de "+dn.label+" a "+dn2.label);
							dualEdges.put(e.hashCode(), e);
							break;
							}
						}
					}
				}
			}
	
	*/
	Iterator<DualNode> it2=dualNodes.values().iterator();
	while(it2.hasNext())
		{
		DualNode dn=it2.next();
		Iterator<DualNode> it3=dualNodes.values().iterator();
		while(it3.hasNext())
			{
			DualNode dn2=it3.next();
			if(dn!=dn2)
				{
				Iterator<Cluster> itbic=dn.clusters.values().iterator();
				while(itbic.hasNext())
					{
					Cluster c=itbic.next();
					if(dn2.clusters.containsValue(c))//Coinciden, as� que creamos una arista entre ellos	
						{
						SpringEdge e=new SpringEdge(dn, dn2);
						e.setGraph(this);
						e.nl=this.getApplet().getEdgeLength()+(dn.getGroupRadius()+dn2.getGroupRadius());
						e.k=this.getApplet().getStiffness()*dn.subNodes.size()*dn2.subNodes.size();
						//System.out.println("Longitud para edge de "+e.nl+", radios de "+dn.getGroupRadius()+" y "+dn2.getGroupRadius());
						if(!dualEdges.containsValue(e) && !dualEdges.containsValue(new SpringEdge(dn2, dn)))	
							{
							//System.out.println("A�adiendo arista de "+dn.label+" a "+dn2.label);
							dualEdges.put(e.hashCode(), e);
							break;
							}
						}
					}
				}
			}
			
		}
	return;
	}

/**
 * Builds the dual graph from the initial graph
 */
void buildDualGraph()
	{
	dualNodes = new HashMap<Integer, DualNode>();
	dualEdges = new HashMap<Integer, Edge>();
	HashMap<Integer, Map<String, Cluster>> zones=new HashMap<Integer, Map<String, Cluster>>();
	
	//1) Adding nodes
	Iterator<Node> it=nodes.values().iterator();
	int cont=0;
	while(it.hasNext())
		{
		Node n=it.next();
		if(!zones.containsKey(n.clusters.hashCode()))
			{
			zones.put(new Integer(n.clusters.hashCode()), n.clusters);
			TreeMap<String, Node> m=getDualMates(n);
			DualNode dn=new DualNode(this,n.clusters,m);
			dn.label=(cont++)+"";
			dn.setMass(m.size());
			dualNodes.put(dn.hashCode(), dn);
			Iterator<Cluster> itc=n.clusters.values().iterator();
			while(itc.hasNext())
				{
				Cluster c=itc.next();
				c.clusterDualNodes.add(dn);
				}
			}
		}
	//2) Adding edges
	Iterator<DualNode> it2=dualNodes.values().iterator();
	while(it2.hasNext())//for each node
		{
		DualNode dn=it2.next();
		Iterator<DualNode> it3=dualNodes.values().iterator();
		while(it3.hasNext())//for each other node
			{
			DualNode dn2=it3.next();
			if(dn!=dn2)
				{
				if(dn.adjacent(dn2))	//add edge
					{
					SpringEdge e=new SpringEdge(dn, dn2);
					e.setGraph(this);
					e.nl=this.getApplet().getEdgeLength()+(dn.getGroupRadius()+dn2.getGroupRadius());
					e.k=this.getApplet().getStiffness()*dn.subNodes.size()*dn2.subNodes.size();
					System.out.println("Longitud para edge de "+e.nl+", radios de "+dn.getGroupRadius()+" y "+dn2.getGroupRadius());
					dualEdges.put(e.hashCode(), e);
					}
				}
			}
		}
	}

/**
 * Return all the edges linked to the corresponding DualNode
 * @param n DualNode to which get the edges
 * @return The edges linked to n
 */
ArrayList<Edge> getDualEdges(DualNode n)
	{
	ArrayList<Edge> ret=new ArrayList<Edge>();
	Iterator<Edge> it=dualEdges.values().iterator();
	while(it.hasNext())
		{
		Edge e=it.next();
		if(e.from==n || e.to==n)	ret.add(e);
		}
	return ret;
	}

/**
 * Return all the edges coming from the corresponding DualNode
 * @param n DualNode to which get the edges
 * @return The edges linked to n
 */
ArrayList<Edge> getDualEdgesFrom(DualNode n)
	{
	ArrayList<Edge> ret=new ArrayList<Edge>();
	Iterator<Edge> it=dualEdges.values().iterator();
	while(it.hasNext())
		{
		Edge e=it.next();
		if(e.from==n)	ret.add(e);
		}
	return ret;
	}

/**
 * Returns a TreeMap with all the nodes that are exactly in the same biclusters than n, including itself
 */
TreeMap<String,Node> getDualMates(Node n)
	{
	TreeMap<String, Node> mates=new TreeMap<String, Node>();
	//mates.put(n.label, n);
	Iterator<Node> it=nodes.values().iterator();
	while(it.hasNext())
		{
		Node n2=it.next();
		if(n2.clusters.equals(n.clusters) && !mates.containsKey(n2.label))	mates.put(n2.label, n2);
		}
	return mates;
	}

void computeMaxZones()
	{
	Iterator<Node> it=nodes.values().iterator();
	int max=0;
	while(it.hasNext())
		{
		Node n=it.next();
		if(n.clusters.size()>max)	max=n.clusters.size();	
		}
	maxZones=max;
	/*maxZones=0;
	int cont=0;
	while(cont<max)
		{
		cont++;
		it=nodes.values().iterator();
		while(it.hasNext())
			{
			Node n=it.next();
			if(n.clusters.size()==cont)
				{
				maxZones++;
				break;
				}
			}
		}*/
	System.out.println("Hay un m�ximo de "+maxZones+" zonas posibles de un total de "+max+" para hacer superficies");
	/*
	Iterator<DualNode> it=dualNodes.values().iterator();
	maxZones=0;
	while(it.hasNext())
		{
		DualNode n=it.next();
		if(n.clusters.size()>maxZones && n.subNodes.size()>1)	maxZones=n.clusters.size();	
		}*/
	
	}

public Map<String, DualNode> getHoverDualZones() {
	return hoverDualZones;
}

public void setHoverDualZones(Map<String, DualNode> hoverDualZones) {
	this.hoverDualZones = hoverDualZones;
}

public void refreshDualPositions()
	{
	Iterator<DualNode> itd=dualNodes.values().iterator();
	while(itd.hasNext())
		{
		DualNode dn=itd.next();
		dn.refreshPosition();
		}
	}
}

