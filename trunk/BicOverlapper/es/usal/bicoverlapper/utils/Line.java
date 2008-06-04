package es.usal.bicoverlapper.utils;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


public class Line extends Line2D.Double{
	
	static final long serialVersionUID=667488;
	double slope;
	
	public Line(double x1, double y1, double x2, double y2)
		{
		super(x1,y1,x2,y2);
		slope=-(y1-y2)/(x1-x2);//negative because of java coordinates
		return;
		}
	
	/**
	 * Returns a line parallel to this one that passed by x,y
	 * @param x
	 * @param y
	 * @return
	 */
	public Line getParallel(double x0, double y0)
		{
		//using point-slope line formula: y-y0=m(x-x0)
		double xf=0;
		double yf=0;
		double m=slope;
		if(x0!=0)	
			{
			xf=0;
			yf=(-m*x0+y0);
			}
		else
			{
			xf=1;
			yf=(m*(1-x0)+y0);
			}
		return new Line(x0,y0,xf,yf);
		}
	
	/**
	 * Gets a line of the same length and position that this line (respect to its initial and ending points),
	 * at a distance d
	 * @return
	 */
	public Line getParallelSegment(double d)
		{
		double angle=Math.atan(slope)+Math.PI/2;
		double x3=x1+Math.cos(angle)*d;
		double y3=y1-Math.sin(angle)*d;//minus because of opposite java coordinate sistem
		double x4=x2+Math.cos(angle)*d;
		double y4=y2-Math.sin(angle)*d;
		return new Line(x3,y3,x4,y4);
		}
	/**
	 * Returns a line perpendicular to this one that passes by x,y
	 * @param x
	 * @param y
	 * @return
	 */
	public Line getPerpendicular(double x0, double y0)
		{
		//using point-slope line formula: y-y0=m(x-x0)
		double xf=0;
		double yf=0;
		double m=-slope;
		if(x0!=0)	
			{
			xf=0;
			yf=(-m*x0+y0);
			}
		else
			{
			xf=1;
			yf=(m*(1-x0)+y0);
			}
		return new Line(x0,y0,xf,yf);
		}
	
	/**
	 * Returns a point that is in a perpendicular line crossing with this line at the start or the end,
	 * and at a determinate distance 
	 * @param start
	 * @param distance
	 * @return
	 */
	public Point2D.Double getPerpendicularPoint(double x0, double y0, double distance)
		{
		double angle=Math.atan(slope)+Math.PI/2;
		double x3=x0+Math.cos(angle)*distance;
		double y3=y0-Math.sin(angle)*distance;
		return new Point2D.Double(x3,y3);
		}
	/**
	 * Returns the intersection point with line l, or null if lines are parallel or coincident
	 * @param l
	 * @return
	 */
	public Point2D.Double intersection(Line l)
		{
		//line-line intersection point
		//http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
		double x3=l.x1;
		double y3=l.y1;
		double x4=l.x2;
		double y4=l.y2;
		
		double den=(y4-y3)*(x2-x1)-(x4-x3)*(y2-y1);
		double numa=(x4-x3)*(y1-y3)-(y4-y3)*(x1-x3);
		double numb=(x2-x1)*(y1-y3)-(y2-y1)*(x1-x3);
		double ua=numa/den;
		double ub=numb/den;
		if(numa==0 && numb==0)	
			{
			System.err.println("Las líneas coinciden");
			return null;
			}
		if((ua>=0 && ua<=1 && ub>=0 && ub<=1))
			{
			System.err.println("Las líneas"+x1+", "+y1+" -> "+x2+", "+y2+" y "+x3+", "+y3+" -> "+x4+", "+y4+" son paralelas");
			return null;
			}
		double xi=x1+ua*(x2-x1);
		double yi=y1+ub*(y2-y1);
		return new Point2D.Double(xi,yi);
		}
		
	public double getSlope() {
		return slope;
	}
}
