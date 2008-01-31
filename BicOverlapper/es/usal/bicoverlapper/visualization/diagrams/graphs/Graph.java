package es.usal.bicoverlapper.visualization.diagrams.graphs;

import java.awt.Color;
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



/**
 * A Graph is a set of Nodes, grouped in (overlapped) Clusters, 
 * that are classified in ResultSets.
 * The nodes of each Cluster are joined by Edges.
 * Graph controls the structure of nodes and edges
 * BiclusVis controls the layout, interaction and manages the drawing
 * @author Roberto Therón and Rodrigo Santamaría
  */
public class Graph {
	private Map<String, Node> nodes;
	private Map<String, Edge> edges;
	private Map<String, ClusterSet> results;
	private Map<String, Node> centerNodes;
	
	HashMap<Node, Map<String, Edge>> edgesFrom;
	 HashMap<Node, Map<String, Edge>> edgesTo;
	 
	 
	 
  BiclusVis applet;
  boolean radial;
  
  Node dragNode = null;
  Node hoverNode = null;
  private Map<String, Node> selectedNodes;
  private Map<String, Node> searchNodes;
  private Map<String, Node> centroidNodes;

  private Map<String, Cluster> selectedClusters;
  private Map<String, Cluster> hoverClusters;
  
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
   * Builds an empty Graph that is visualized in p
   * @param p	BiclusVis panel where the Graph is visualized
   */
  public Graph(BiclusVis p) {
	    nodes = new HashMap<String, Node>();
	    edges = new HashMap<String, Edge>();
	    results = new HashMap<String, ClusterSet>();
	    centerNodes = new HashMap<String, Node>();
	  
 	    edgesFrom = new HashMap<Node, Map<String,Edge>>();
	    edgesTo = new HashMap<Node, Map<String,Edge>>();
	    
	    searchNodes=new TreeMap<String, Node>();
	    selectedNodes=new TreeMap<String, Node>();
	    centroidNodes=new TreeMap<String, Node>();
	    
	    selectedClusters=new TreeMap<String, Cluster>();
	    hoverClusters=new TreeMap<String, Cluster>();
	    
	   	    
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
  	Iterator it=f.values().iterator();
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
  	Iterator it=t.values().iterator();
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
    Iterator i;
    i = edges.values().iterator();
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
	Iterator itGraph=nodes.values().iterator();
	while(itGraph.hasNext())	//Para evitar que se dibujen varias veces en caso de q estén en varios clusters
	  	{	
		Node n =(Node)itGraph.next();
		n.setDrawn(false);
		n.setDrawnAsPiechart(false);
		n.setDrawnAsLabel(false);
	  	}

	for(int i=0;i<priorities.length;i++)	drawComponent(priorities[i]);
  	}
  
  /**
   * Draws one of the component of the graphs, as explained in draw(int[] priorities)
   * @param component	component of the graph
   */
  public void drawComponent(int component)
  	{
	BiclusVis bv=(BiclusVis)applet;
	Iterator itGraph=results.values().iterator();//Hull drawing
	
	for (int i=0; i<results.size(); i++) 
	  	{
	    ClusterSet r = (ClusterSet)itGraph.next();
	   // System.out.println(r.label+" has size "+r.getClusters().size());
	    for(int j=0;j<r.getClusters().size();j++)
	      	{
	    	Cluster c=(Cluster)r.getClusters().get(j);
	    	switch(component)
				{
				case HULL:
					if(bv.isDrawHull())	c.drawHulls();
					break;
				case NODE:
					if(bv.isDrawNodes())	c.drawNodes();
					break;
				case HULLLABEL:
					if(bv.isDrawClusterLabels())	c.drawHullLabels();
					break;
				case NODELABEL:
					if(bv.isShowLabel())	
						c.drawNodeLabels();
					break;
				case EDGE:
					if(bv.isShowEdges())	drawEdges();	
					break;
				case DETAIL:
					if(bv.isDrawNodes())	c.drawDetails();//TODO: FALTA: ahora mismo es componente integrado a NODE
					break;
				case PIECHART:
					if(bv.isDrawPiecharts())		c.drawPiecharts();
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
		    	}
		    }
	  	}
	}
  
  /**
   * Draws the edges in the graph
   *
   */
  public void drawEdges()
  	{
	Iterator itGraph=edges.values().iterator();
	while(itGraph.hasNext())
	   {
		Edge e=(Edge)itGraph.next();
		if(!centerNodes.containsValue(e.from) && !centerNodes.containsValue(e.to))		  e.draw();
	   
	   }
		  
  	itGraph=centerNodes.values().iterator();
	while(itGraph.hasNext())
	  	{
	    Node n=(Node)itGraph.next();
	    n.draw();
	    }	  
  	}
  
  /**
   * Draws all the components in the graph at a predefined order
   *
   */
  public void draw() {
	  Iterator itGraph=nodes.values().iterator();
	  while(itGraph.hasNext())	//Para evitar que se dibujen varias veces en caso de q estén en varios clusters
	  	{	
		Node n =(Node)itGraph.next();
		n.setDrawn(false);
	  	}

	  BiclusVis bv=(BiclusVis)applet;
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
		//int w=(int)(n.getLabel().length()*(bv.getLabelSize()+n.getRelevance()+2));
		//int h=(int)((bv.getLabelSize()+n.getRelevance()+2)*2);
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
		//int x=(int)n.getX();
		//int y=(int)n.getY();
		//int w=(int)(n.getWidth()*n.getClusters().size()+5);
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
    		/*if(bv.isMovie())//Caso de pelis
	    		{
			if(bv.isSizeRelevant())//Tamaños relativos a la conectividad
	    			{
	    			if(otherRoles)	n.drawShapedNode(n.clusters.size(), movieTitle, bv.paleta[bv.colorSeleccion]);
	    			else			n.drawShapedNode(n.clusters.size(), bv.paleta[bv.colorSeleccion]);
	    			}
	    		else
	    			{
	    			if(otherRoles)	n.drawShapedNode(1, movieTitle, bv.paleta[bv.colorSeleccion]);
	    			}
    			}
    		else//Caso de genes
    		*/
    			{
    			bv.rectMode(JProcessingPanel.CENTER);
    			if(n.isGene())	bv.ellipse((float) n.getX(), (float) n.getY(), n.width, n.height);
    			else			bv.rect((float) n.getX(), (float) n.getY(), n.width*factor, n.height*factor);
    			}
    		n.setDrawn(true);
	    	}
    	
    	if(hoverNode!=null && !selectedNodes.containsKey(hoverNode.label))
	    	{
	    	bv.fill(0,0,0,255);
	    	bv.textSize(bv.getLabelSize()+hoverNode.getClusters().size()*3+1); 
	    	bv.text(hoverNode.label, (float)(hoverNode.position.getX()+0.5), (float)(hoverNode.position.getY()-hoverNode.getHeight()+0.5));
	
		    	
	    	bv.fill(255,255,255,255);
	    	//if(hoverNode.clusters==null)	System.out.println("los clusters de hover Node son null "+hoverNode.getLabel());
	    	bv.textSize(bv.getLabelSize()+hoverNode.clusters.size()*3); 
	    	bv.text(hoverNode.label, (float)hoverNode.position.getX(), (float)hoverNode.position.getY()-hoverNode.getHeight());
	    	}
    	
    	/*if(bv.isShowCentroids())
    		{
    		//draw path to them
    		//((ForcedNode)hoverNode).drawPath();
    		}*/
	  	}
	  
	  
	  applet.noStroke();
	  bv.rectMode(JProcessingPanel.CORNER);
  	}
  
  /**
   * Draws the Hover node, highlighted, and also highlights their neighbors (all the nodes directly connected to it)
   *
   */
public void drawHoverNode()
  	{
    BiclusVis bv=(BiclusVis)applet;
	
    if(hoverNode!=null)
	  	{
	    int factor=1;
	    if(bv.isSizeRelevant())    	factor=hoverNode.clusters.size();
		    
		Iterator itDrawMates=hoverNode.mates.values().iterator();
	  	while(itDrawMates.hasNext())
	  		{
	  		ForcedNode n=(ForcedNode)itDrawMates.next();
	  		if(n.clusters.size()>=bv.nodeThreshold && this.nodes.containsKey(n.label))
		  		{
//	  		bv.fill(bv.paleta[bv.colorSeleccion].getRed(), bv.paleta[bv.colorSeleccion].getGreen(), bv.paleta[bv.colorSeleccion].getBlue(), 0);
				//bv.stroke(bv.paleta[bv.colorSeleccion].getRed(), bv.paleta[bv.colorSeleccion].getGreen(), bv.paleta[bv.colorSeleccion].getBlue(),255);
	  			Color c=bv.paleta[bv.hoverColor];
	    		bv.fill(c.getRed(), c.getGreen(), c.getBlue(), 0);
				bv.stroke(c.getRed(), c.getGreen(), c.getBlue(),255);
	    		
	  			bv.rectMode(JProcessingPanel.CENTER);
	  			if(n.isGene())	bv.ellipse((float) n.getX(), (float) n.getY(), n.width, n.height);
	  			else			bv.rect((float) n.getX(), (float) n.getY(), n.width*factor, n.height*factor);
	  			n.setDrawn(true);
		  		}
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
  	}
  
	/**
	 *	Draws node selected. Selected nodes also show their labels, on a different color 
	 *
	 */
  public void drawSelectedNodes()
  	{
	  BiclusVis bv=(BiclusVis)applet;
	  //-------------- selected nodes
	  Iterator itGraph=selectedNodes.values().iterator();
	  while(itGraph.hasNext())
	  	{
		Node n=(Node)itGraph.next();
		if(n.clusters.size()>=bv.nodeThreshold)
			{
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
	    	
			if(selectedNodes.containsKey(n.label))
				{
				bv.noFill();
				bv.strokeWeight(3);
				Color c=bv.paleta[BiclusVis.selectionColor];
				bv.stroke(c.getRed(), c.getGreen(), c.getBlue());
				if(n.isGene())	bv.ellipse((float)n.getX(), (float)n.getY(), n.height, n.width);
				else			bv.rect((float) n.getX(), (float) n.getY(), n.width, n.height);
				}

			}
		}
  	}
  
  void updateSelection(BiclusterSelection bs)
  	{
    BiclusVis bv=(BiclusVis)applet;
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
	BiclusVis bv=(BiclusVis)applet;
	bv.rectMode(JProcessingPanel.CENTER);
	Iterator itGraph=searchNodes.values().iterator();
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
			Color c=bv.paleta[bv.searchColor];
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
	  while(itGraph.hasNext())	//Para evitar que se dibujen varias veces en caso de q estén en varios clusters
	  	{	
		Node n =(Node)itGraph.next();
		n.setDrawn(false);
	  	}

	  BiclusVis bv=(BiclusVis)applet;
	  
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
  //NOTA: de momento uso Cluster.draw() que parece más rápido, aunque no entiendo por qué, si en principio este
  //va con iterador y accede una sola vez a cada nodo
  /**
   * Draw the nodes in the Graph
   */
  public void drawNodes()
  	{
	  BiclusVis bv=(BiclusVis)this.getApplet();
	  Iterator itNodes=nodes.values().iterator();

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
				 
		        
		        //Para saber qué porción de círculo toca;
		        float arc = BiclusVis.TWO_PI / n.getClusters().size();
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
		    
		    //Finalmente pintamos el nodo en sí
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
	  BiclusVis bv=(BiclusVis) this.getApplet();
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
	  Iterator it=results.values().iterator();
	  while(it.hasNext())
		{
		ClusterSet r=(ClusterSet)it.next();
		ArrayList c=r.getClusters();
		for(int j=0;j<c.size();j++)
			{
			//Quitamos las aristas y el nodo central
			RadialCluster rc=(RadialCluster)c.get(j);
			deleteEdgesFor(rc.getCenterNode());
			rc.deleteCenterNode();
			//Construimos las aristas con el CompleteCluster
			ArrayList nodes=rc.getNodes();
			for(int k=0;k<nodes.size();k++)
				{
				Node nk=(Node)nodes.get(k);
				rc.buildEdges(nk);
				}
			}
		}
	centerNodes.clear();
	System.out.println("Cambiamos de radial a completo: n="+getNumNodes()+" e="+getNumEdges());
  	}
  
  /**
   * Converts the clusters in the graph to radial subgraphs, adding a center node
   */
  public void complete2radial()
  	{
		edges.clear();
		edgesTo.clear();
		edgesFrom.clear();
	Iterator it=results.values().iterator();
	while(it.hasNext())
		{
		ClusterSet r=(ClusterSet)it.next();
		ArrayList<Cluster> c=r.getClusters();
		for(int j=0;j<c.size();j++)
			{
			MaximalCluster cc=(MaximalCluster)c.get(j);//Los convertimos a radiales según los cogemos
			RadialCluster rc=new RadialCluster(r, cc);
			double x,y;
			x=y=0;
			ArrayList pnodes=rc.getNodes();
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
	buildPeripheryEdges();
	System.out.println("Cambiamos de completo a radial: n="+getNumNodes()+"(+"+getNumCenterNodes()+") e="+getNumEdges());
  	}
  
  /**
   * Returns the value of a homemade measure to determine the degree of
   * mispositioning of nodes
   * The formula is: (nº of nodes placed upon a hull it is not in)/(nº of nodes)
   * 
   * It will change with time to more sophisticated one
   * Now it is
   *(nº of times a node sits on a non-corresponding hull)/(nº of possible missittings)
   *
   *The nº of possible missitting is sum(numClusters-numClustersInWhichNodeKis)
   */
  public double getFailedPositionMetric()
  	{
	double metric=0;
	double den=0;
	Iterator itn=nodes.values().iterator();
	while(itn.hasNext())
		{
		Node n=(Node)itn.next();
		den-=n.clusters.size();
		Iterator it=results.values().iterator();
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
	//return metric/nodes.size();
	return metric/den;
  	}
  
  /**
   * Returns the average area of the hull respect to the number of nodes contained
   * It uses bounding boxes to compute hull's area instead of real area.
   * Other option is to do triangulations
   * 
   */
  public double getAverageNormArea()
  	{
	double metric=0;
	double den=0;
	Iterator it=results.values().iterator();
	while(it.hasNext())
		{
		ClusterSet r=(ClusterSet)it.next();
		ArrayList<Cluster> c=r.getClusters();
		for(int j=0;j<c.size();j++)
			{
			Cluster cc=(Cluster)c.get(j);
			Rectangle2D rec=cc.hull.getBounds2D();
			metric+=rec.getHeight()*rec.getWidth()/cc.clusterNodes.size();
			den++;
			}
		}
	
	return metric/den;	  
  	}
  
  private void buildPeripheryEdges()
  	{
	  System.out.println("BuildPeriphery");
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
		System.out.println("Nodo "+n.label);
		if(!centerNodes.containsValue(n))
			{
			Iterator itc=n.mates.values().iterator();
			while(itc.hasNext())
				{
				ForcedNode n2=(ForcedNode)itc.next();
				if(!centerNodes.containsValue(n2) && n!=n2)//Si no es el central o él mismo
					{
					temp=clustersInCommon(n,n2);
					TreeMap<String,Edge> te=getEdgesTo(n2);
					System.out.println("Número de nodos que van a "+n2.label+" son "+te.size());
					if(temp>nc && te.size()<=1)	//Si no tiene ya a uno encadenado
						{
						linkNode=n2;
						}
					}
				}
			if(linkNode!=null)
				{
				System.out.println("***Enlazando "+n.label+" a "+linkNode.label);
				
				SpringEdge e=new SpringEdge(n,linkNode);
				addEdge(e);
				}
			}
		}
  	}
  
  
  private int clustersInCommon(Node a, Node b)
  	{
	Iterator it=a.clusters.values().iterator();
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
	Iterator it=centerNodes.values().iterator();
	float gm=0;
	while(it.hasNext())
		{
		gm+=((ForcedNode)it.next()).getMass();
		}
	return gm;
  	}
  
  //-------------------- GETTERS Y SETTERS -------------------
  /**
   * Returns the applet in which this Graph is drawn
   * @return	the applet in which this Graph is to be visualized and managed
   */
  public BiclusVis getApplet() {
  	return applet;
  }

  /**
   * Sets the applet in which this Graph is drawn
   * @param applet	the applet in which this Graph is to be visualized and managed
   */
  public void setApplet(BiclusVis applet) {
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
  
  
}