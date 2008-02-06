package es.usal.bicoverlapper.visualization.diagrams.overlapper;

import es.usal.bicoverlapper.utils.GraphPoint2D;

/**
 * SpringEdge implements elastic edges. Elastic edges tend to a natural length and apply an (attractive or repulsive) lineal force
 * (greater with distance to its natural length) to the nodes it connects when not in it.
 * @author Roberto Theron and Rodrigo Santamaría
 *
 */
public class SpringEdge extends Edge {
	double k;//stiffness
	double nl;//natural length.
	double lengthFactor;
	GraphPoint2D nullVector=null;
	GraphPoint2D forceFrom=null;
	GraphPoint2D forceTo=null;
	
	double kf=1;//Factor for stiffness if not equal (in prove)
	double kt=1;
	  
    
  //This edge subclass applies a spring force between the two nodes it connects
  //The spring force formula is F = k(currentLength-nl)
  //This equation is one-dimensional, and applies to the straight line
  //between the two nodes.
	  /**
	   * Builds a spring edge
	   * @param a	source node
	   * @param b	target node
	   */
  public SpringEdge(Node a, Node b) {
		  super(a, b);
		  nullVector=new GraphPoint2D(0,0);
			forceFrom=new GraphPoint2D();
			forceTo=new GraphPoint2D();
				  lengthFactor=1;
	  }

  /**
   * Sets the natural legth of the spring. At this lenght the force that the edge applies to the connecting
   * nodes is zero
   * @param l	natural length of the spring
   */
  public void setNaturalLength(double l) {
    if (l > 10)
	  nl = l;
    else
      nl = 10;
  }
  
  /**
   * Returns the natural length of the SpringEdge
   * @return the natural length of the SpringEdge
   */
  public double getNaturalLength() {
    return nl;
  }
  
  /**
   * Sets the stiffeness of the spring. The higher the stiffness, the higher the force applied to nodes when not
   * at the natural length
   * @param s	Stiffness of the spring (determines the force in which the spring tries to return to its natural length)
   */
  public void setStiffness(double s) {
	    k = s;
	  }
  
  /**
   * Returns the force applied by the edge to the target node
   * @return	The point in which the node should be if only this force is applied to it
   */
  public GraphPoint2D getForceTo() {
	  Overlapper bv=(Overlapper)g.getApplet();
	    double dx = dX();
	    double dy = dY();
	    double l = Math.sqrt((float) (dx*dx + dy*dy));
	    k = bv.getStiffness();//TODO: mejor que cuando cambie se reactualice en todas las aristas y así no haya que rebuscarlo siempre con llamadas no?
	    k=k*kt;
	    
		//nl = bv.getEdgeLength();//El nl se cambia con el handle, y así no vamos a permitir el cambio!
	      	    
	    double f = k*(l-nl);
	    
		forceTo.setX(-f*dx/l);
		forceTo.setY(-f*dy/l);
	    //if (l > bv.getCloseness())					return forceTo;
		//    else	    	  return nullVector;
		return forceTo;
		
	  }
  /**
   * Returns the force applied by the edge to the source node 
   * (it will have the same magnitude an opposite direction
   * to the force applied to the target node
   * @return	The point in which the node should be if only this force is applied to it
   */
    public GraphPoint2D getForceFrom() 
	  	{  
		double dx = dX();
	    double dy = dY();
	    double l = Math.sqrt((float) (dx*dx + dy*dy));

	    Overlapper bv=(Overlapper)g.getApplet();
	    
	    k = bv.getStiffness();
	    k=k*kf;
	   //nl = bv.getEdgeLength();
		double f = k*(l-nl);
	    
		forceFrom.setX(f*dx/l);
		forceFrom.setY(f*dy/l);
		return forceFrom;
	  }

    /**
     * @deprecated
     * @return length factor
     */
public double getLengthFactor() {
	return lengthFactor;
}
/**
 * @deprecated
 * @param lengthFactor
 */
public void setLengthFactor(double lengthFactor) {
	this.lengthFactor = lengthFactor;
}
/*
public double getKf() {
	return kf;
}

public void setKf(double kf) {
	this.kf = kf;
}

public double getKt() {
	return kt;
}

public void setKt(double kt) {
	this.kt = kt;
}
 */
}