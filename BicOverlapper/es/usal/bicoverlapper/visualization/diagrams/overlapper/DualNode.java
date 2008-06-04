package es.usal.bicoverlapper.visualization.diagrams.overlapper;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import es.usal.bicoverlapper.utils.GraphPoint2D;

/**
 * Force directed nodes for a Dual Graph. Each dual node will correspond to all nodes that are grouped in exactly the 
 * same list of biclusters, thus a zone of the equivalent dual graph.
 * In this case, mates only refers to dualNodes and clusters to all clusters in the zone represented by this DualNode.
 * In addition, a new map subNodes referes to all the ForcedNodes represented by this DualNode
 * @author Rodrigo Santamaría
 *
 */
public class DualNode extends ForcedNode {

	Map<String,Node>	subNodes;//List of nodes represented by this node
	public Polygon hull=null;			//Hull formed by the nodes in this dual node

  /**
   * Builds a DualNode at starting position v
   * @param v	Initial position as (x,y)
   */
  public DualNode(GraphPoint2D v) {
	    super(v);
	    subNodes=new TreeMap<String,Node>();
	  }
  
  /**
   * Builds a DualNode in Graph g at starting position v
   * @param g	Graph for this node
   * @param v	Starting position for this node
   * 
   */
  public DualNode(Graph g, GraphPoint2D v) {
	    super(g, v);
	    subNodes=new TreeMap<String,Node>();
	  }
  
  public DualNode(Graph g, Map<String,Cluster> zone, Map<String, Node> sub)
  	{
	super(g,new GraphPoint2D(0,0));
	clusters=new HashMap<String,Cluster>();
	clusters.putAll(zone);
	subNodes=new TreeMap<String,Node>();
	subNodes.putAll(sub);
	Point2D.Double p=getMiddlePoint();
	position=new GraphPoint2D(p.x,p.y);
	}
  
  /**
   * Sets the position of subnodes surrounding the position of this node on a circular fashion
   */
  public void positionSubNodes()
  	{
	Iterator<Node> it=subNodes.values().iterator();
	float radius=0;
	double r=0;
	double degree=0;
	double x0=position.getX();
	double y0=position.getY();
	int contCircles=0;
	double restRadius=0;//the part of the circumference in which another node can't fit,
	double restRadiusDegrees=0;//the rest radius divided by the number of circles for a determinate circunference
	
	while(it.hasNext())
		{	
		Node n=it.next();
		r=n.getWidth()/2+1;//supposed equal to getHeight() and referred to diameter, not radius (hence the /2)
		double x=x0+Math.cos(degree)*radius;
		double y=y0+Math.sin(degree)*radius;
		n.setPosition((float)x,(float)y);
		
		contCircles++;
		
		//if(contCircles>=(Math.PI*radius/r)-1) //PI*R/r is the maximum number of circles of radius r in a ring of radius R
		if(contCircles>=Math.floor(Math.PI*radius/r)) //PI*R/r is the maximum number of circles of radius r in a ring of radius R
			{//increase to the next ring
			radius+=r*2;
			contCircles=0;
			degree=0;
			restRadius=(Math.PI*radius/r)-Math.floor(Math.PI*radius/r);
			restRadiusDegrees=restRadius/Math.floor(Math.PI*radius/r);
			}
		else	//just increase the degree
			{
			//If PI*(R/r) is the number of circles, the radians for each one are: 2PI/(PI*R/r) -> 2r/R
			//degree+=2*r/radius;
			degree+=2*r/radius+restRadiusDegrees/2;
			}
		}
  	}
	  
    
  /**
   * Draw the dual node as a circle, with area proportional to the number of nodes that
   * are in the zone
   * @param dependent if true, dual node position is the middle point of all their related nodes, otherwise, it is assumed
   * 				that the dual node has its own position computed elsewhere
   */
  public void draw(boolean dependent) 
  	{
    Overlapper p=(Overlapper)g.getApplet();
    //System.out.println("dependente es "+dependent);
    float radius=(float)(this.getHeight()*Math.log(this.subNodes.size()));
   // float radius=this.getHeight();
    if(dependent)
    	{
	    Point2D.Double p0=getMiddlePoint();
	    this.position.setX(p0.x);
	    this.position.setY(p0.y);
    	}
	    
	p.noFill();
	p.stroke(0,200,0,255);
    p.strokeWeight(2);
  	p.rectMode(Overlapper.CENTER);
  	p.ellipse((float)position.getX(), (float)position.getY(), radius, radius);

    p.fill(100,100,100,100);
    this.setDrawn(true);
  }
  
  public void refreshPosition()
  	{
	    Point2D.Double p0=getMiddlePoint();
	    this.position.setX(p0.x);
	    this.position.setY(p0.y);
  	}
  
  /**
   * Returns the radius of the area covered by all the nodes in the dual node
   * @return
   */
  public double getGroupRadius()
  	{
	Iterator<Node> it=subNodes.values().iterator();
	double radius=0;
	double r=0;
	int contCircles=0;
	while(it.hasNext())
		{	
		Node n=it.next();
		r=n.getWidth()/2+1;//supposed equal to getHeight() and referred to diameter, not radius (hence the /2)
		
		contCircles++;
		if(contCircles>=(Math.PI*radius/r)-1) //PI*R/r is the maximum number of circles of radius r in a ring of radius R
			{//increase to the next ring
			radius+=r*2;
			contCircles=0;
			}
		}
	return radius;
  	}
  
  /**
   * Returns the center of the cluster, computed as the mean of each cluster node's coordinates
   * returns	point with the coordinates of the middle point
   */
  public Point2D.Double getMiddlePoint()
  	{
	float meanx=0;
	float meany=0;
	Iterator<Node> it=subNodes.values().iterator();
	while(it.hasNext())
		{
		Node n = it.next();
		meanx+=n.getX();
		meany+=n.getY();
		}
	return new Point2D.Double(meanx/subNodes.size(), meany/subNodes.size());
  	}
  
  /**
   * Returns the number of clusters in common between this dual node and the dual node taken as parameter
   */
  public int clustersInCommon(DualNode dn)
  	{
	int num=0;
	Iterator<Cluster> it=this.clusters.values().iterator();
	while(it.hasNext())
		{
		Cluster c=it.next();
		if(dn.clusters.containsKey(c.label))	num++;
		}
	return num;
  	}
  
  /**
   * Determines if the dual node is adjacent to other dual node. Adjacency is defined as that both nodes share exactly the same number of
   * groups except one.
   * @param dn
   * @return
   */
  public boolean adjacent(DualNode dn)
  	{
	if(dn.clusters.size()>this.clusters.size()+1 || dn.clusters.size()<this.clusters.size()-1)	return false;
	int num=clustersInCommon(dn);
	int minSize=Math.min(this.clusters.size(), dn.clusters.size());
	if(num!=minSize)	return false;
	return true;
	}
}

