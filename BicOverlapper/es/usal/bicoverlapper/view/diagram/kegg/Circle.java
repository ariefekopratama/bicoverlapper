package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.Point;


public class Circle{ 
    private Point center;
    private double radius;
    
    public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

    public Circle(Point center, double radius){
        this.center = center;
        this.radius = radius;
    }

    public boolean contains(Point p){  
    	double dx = Math.abs(p.getX()-center.getX());
		double dy = Math.abs(p.getY()-center.getY());
		
		if(Math.pow(dx, 2) + Math.pow(dy, 2) <= Math.pow(radius, 2))
			return true;
		else
			return false;
    }

    public double area(){ 
    	return Math.PI * radius * radius; 
    }

    public double perimeter(){ 
    	return 2 * Math.PI * radius;    
	}
}

