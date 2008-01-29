package graphs;

import utils.GraphPoint2D;

/**
 * Force directed nodes. A ForcedNode is a Node to which different forces can be applied to modify its position
 * @author Roberto Therón and Rodrigo Santamaría
 *
 */
public class ForcedNode extends Node {
  GraphPoint2D f;
  float mass = 1;
  
  /**
   * Builds a ForcedNode at starting position v
   * @param v	Initial position as (x,y)
   */
  public ForcedNode(GraphPoint2D v) {
	    super(v);
	    f=new GraphPoint2D(v.getX(), v.getY());
	    height = 20;
	    width = 20;
	  }
  
  /**
   * Builds a ForcedNode in Graph g at starting position v
   * @param g	Graph for this node
   * @param v	Starting position for this node
   * 
   */
  public ForcedNode(Graph g, GraphPoint2D v) {
	    super(g, v);
	    f=new GraphPoint2D(v.getX(), v.getY());
	    height = 20;
	    width = 20;
	  }
	  
  /**
   * Returns the mass of the ForcedNode. Some kind of forces, as gravitational forces, can have the mass of a node as a parameter to use
   * TODO: By now, this parameter is not being used to compute forces (all nodes have mass 1)
   * @return the mass of the ForcedNode.
   */
  public float getMass() {
    return mass;
  }
  
  /**
   * Sets the mass of the ForcedNode. Some kind of forces, as gravitational forces, can have the mass of a node as a parameter to use
   * @param	m the mass of the ForcedNode.
   */
  public void setMass(double m) {
  
	mass = (float)m;  
  }
  
  /**
   * Sets the force applied to it as a point v (the new position at which this node must be with the force applied)
   * @param v	the position at which this node must be with the force applied
   */
  public void setForce(GraphPoint2D v) {
		  f.setX(v.getX());
		  f.setY(v.getY());
	  }
  
  /**
   * Sets the force applied to it as the point (x,y), the new position at which this node must be with the force applied
   * @param x	x coordinate of the position at which this node must be with the force applied
   * @param y	y coordinate of the position at which this node must be with the force applied
   */
  public void setForce(double x, double y) {
	      f.setX(x);
		  f.setY(y);
	  }
 
  /**
   * Sets the size of the node, in pixels
   * @param x size of the node, in pixels
   */
  public void setSize(float x) {
	    height = width = x;
	  }
 
  /**
   * Gets the size of the node, in pixels
   * @return size of the node, in pixels
   */
  public float getSize() {
	    return height;
	  }
  
  /**
   * Returns the force applied to it as the point at which this node must be with the force applied
   * @return the point at which this node must be with the force applied
   */
  public GraphPoint2D getForce() {
    return f;
  }
  
  /**
   * Adds a force to the current total force applied to the node
   * @param v	position that the node must have if this force alone applies to it
   */
  public void applyForce(GraphPoint2D v) {
	  f.add(v);
  }
  
  /**
   * Draw the node as a circle if it is a gene, or as a square if it is a condition.
   * The node is only draw if node.isDrawn() is set to false
   */
  public void draw() 
  	{
    BiclusVis p=(BiclusVis)g.getApplet();
    if(!p.isDrawNodes() || this.isDrawn() || p.nodeThreshold>this.clusters.size())	return;
    
    int factor=1;
    if(p.isSizeRelevant())    	factor=this.clusters.size();
    
	p.noStroke();
	p.fill(255,255,255,64);

 	p.noFill();
    p.stroke(255,255,255,128);
  	p.rectMode(BiclusVis.CENTER);

	if(isGene())	p.ellipse((float) getX(), (float) getY(), width*factor, height*factor);
	else			p.rect((float) getX(), (float) getY(), width*factor, height*factor);
     
    p.fill(100,100,100,100);
    if(image.length()>0)
    	{
    	p.rectMode(JProcessingPanel.CORNER);
    	if(details.length()>0) 	p.rect((float)getX()-5*label.length()-5, (float)getY()+5*factor, 90+(float)maxChars*5, (float)100);
    	else					p.rect((float)getX()-5*label.length()-5, (float)getY()+5*factor, (float)80, (float)100);
    	}
    else if(details.length()>0)   	
    	{
    	p.rectMode(JProcessingPanel.CORNER);
    	p.rect((float)getX()-5*label.length()-5+65, (float)(int)getY()+5*factor, 10+(float)maxChars*5, (float)numLines*20);
    	}
    	
    if(image.length()>0)    
    	{
    	p.image(pimage, (int)getX()-5*label.length(), (int)getY()+5*factor+5, 60, 90);
    	}
    if(details.length()>0)	
    	{
    	p.textAlign(BiclusVis.LEFT);
    	p.textSize(9);
    	p.fill(255,255,255,255);
    	p.stroke(1);
    	p.text(details, (int)getX()-5*label.length()+65, (int)getY()+5*factor+10);
    	p.textAlign(BiclusVis.CENTER);
    	}
    if((image.length()>0 || details.length()>0) && (!g.getSelectedNodes().containsKey(this.label) && g.getHoverNode()!=this))
    	{
    	p.stroke(32,64,255,128);
 	    p.fill(255,255,255,128);
 	    p.textSize(p.getLabelSize()+4+factor*2);
     	p.text(label, (float)position.getX(), (float)position.getY());
     	p.noStroke();
  		}
    
    this.setDrawn(true);
  }
}
