package es.usal.bicoverlapper.visualization.diagrams.overlapper;

import java.util.ArrayList;
import java.util.Iterator;

import es.usal.bicoverlapper.utils.CustomColor;
import es.usal.bicoverlapper.utils.GraphPoint2D;

/**
 * Force directed nodes. A ForcedNode is a Node to which different forces can be applied to modify its position
 * @author Roberto Ther�n and Rodrigo Santamar�a
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
    Overlapper p=(Overlapper)g.getApplet();
    if(!p.isDrawNodes() || this.isDrawn())
    	if(!centerNode && p.nodeThreshold>this.clusters.size())
    		return;
    
    int factor=1;
    if(p.isSizeRelevant())    	factor=this.clusters.size();
    
	p.noStroke();
	p.fill(255,255,255,64);

 	p.noFill();
    p.stroke(255,255,255,128);
 	/*if(p.isDrawTopography() && this.clusters.size()>p.getGraph().maxZones*.75)	{
 					int lum=255/this.clusters.size();
 					p.stroke(lum,lum,lum,128);
 					}
 	else						p.stroke(255,255,255,128);*/
    p.rectMode(Overlapper.CENTER);

   	if(isGene())	p.ellipse((float) getX(), (float) getY(), width*factor, height*factor);
	else			p.rect((float) getX(), (float) getY(), width*factor, height*factor);

  	//p.stroke(0,0,0);
  	//if(isGene())	p.ellipse((float) getX()-1, (float) getY()-1, width*factor+2, height*factor+2);
	//else			p.rect((float) getX()-1, (float) getY()-1, width*factor+2, height*factor+2);
    
  	
  	
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
    	p.textAlign(Overlapper.LEFT);
    	p.textSize(9);
    	p.fill(255,255,255,255);
    	p.stroke(1);
    	p.text(details, (int)getX()-5*label.length()+65, (int)getY()+5*factor+10);
    	p.textAlign(Overlapper.CENTER);
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
  
  //M�todo mejorado para dibujar piecharts
  public void drawPie()
	{
	Overlapper bv=(Overlapper)g.getApplet();
	final float env = 1.3f;
	float ns=bv.getNodeSize();
	
	if(!isDrawnAsPiechart() && shownClusters.size()>=bv.nodeThreshold)
		{
		float x=(float)getX();
        float y=(float)getY();
        float s=getSize();
        float senv=s*env;
        float dif=(senv-getSize())/2;
		 
        
        //Para saber qu� porci�n de c�rculo toca;
        float step = Overlapper.TWO_PI / shownClusters.size();
	    //Para hacer un sector por grupo al que pertenece
        int inter=0;
        if(bv.isOnlyIntersecting())	inter=1;
        if(shownClusters.size()>inter)
	        {
	        Iterator<Cluster> itDraw=shownClusters.values().iterator();
	        ArrayList<CustomColor> colors=new ArrayList<CustomColor>();
	        ArrayList<Integer> sizes=new ArrayList<Integer>();
	        for (int j=0; itDraw.hasNext(); j++)	//Tomamos el tama�o de las porciones por cada color
	           	{
	        	MaximalCluster c=(MaximalCluster)itDraw.next();
		    	ClusterSet r = c.myResultSet;
		    	CustomColor col = r.myColor;
		    	if(!colors.contains(col))
		    		{
		    		colors.add(col);
		    		sizes.add(1);
		    		}
		    	else
		    		{
		    		int ind=colors.indexOf(col);
		    		int tam=sizes.get(ind);
		    		sizes.set(ind, tam+1);
		    		}
	           	}
	        
	        float init=0;
	        for(int i=0;i<colors.size();i++)
	        	{
	        	CustomColor col=colors.get(i);
	        	bv.rectMode(JProcessingPanel.CENTER);
	        	bv.fill(col.getR(), col.getG(), col.getB(),150);
	        	bv.noStroke();
	        	
		        float end=init+step*sizes.get(i);
		        if(label.equals("E3A"))
		        	System.out.println("");
		        
		       bv.arc(x+1, y+1, senv, senv, init, end);//TODO: no s� por qu� los arcos no me salen bien centrados, sol temp. poner el +1
		        
		        bv.stroke(255,255,255,255);
		        bv.strokeWeight(1);
		        for(int j=0;j<sizes.get(i);j++)
			        {
				   	bv.line(x, y, (float)(x+ ns/2*Math.cos(init)), (float)(y+ ns/2*Math.sin(init)));
				   	init+=step;
			        }
				bv.fill(0,0,0,255);
		    	}
		  	}
        setDrawnAsPiechart(true);
		}//if(no ha sido ya pintada la piechart)
	}

}
