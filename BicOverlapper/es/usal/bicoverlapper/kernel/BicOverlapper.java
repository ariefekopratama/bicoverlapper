package es.usal.bicoverlapper.kernel;

import java.awt.Polygon;
import java.awt.geom.Line2D;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;

import es.usal.bicoverlapper.data.files.FileParser;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.overlapper.Graph;



/**
 * Main class to run the application BicOverlapper. It just initializes an instance of BicOverlapperWindow
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * 
*/
public class BicOverlapper {

	/**
	 * Default constructror
	 *
	 */
	public BicOverlapper() {		
		new BicOverlapperWindow();
	}
	
	/**
	 * Main method
	 * 
	 * @param args Arguments taken from command line (no arguments are considered by overlapper)
	 */
	public static void main(String[] args) {
		
		try {
		      UIManager.setLookAndFeel(new SubstanceLookAndFeel());
		    } catch (UnsupportedLookAndFeelException ulafe) {
		      System.out.println("Substance failed to set");
		    }
		   
		    
		   Translator.instance=new Translator("en");
			    
		new BicOverlapper();
	/*	int num=10;
		int deg=5;
		int size=10;
		int sdsize=0;*/
	//	for(int i=10;i<200;i+=10)//para biclusters de 1 a 200
		//	for(int j=10;j<100;j+=10)//Para tamaños de 10 a 100
			//	for(int k=1;k<20;k++)//Para grados de solapamiento de 1 a 20
				//	FileParser.buildSyntheticBiclusters(i,k,j, 5, "bics"+i+"-"+j+"("+5+")-"+k+".bic");
		//FileParser.buildSyntheticBiclusters(num,deg,size, sdsize, "bics"+num+"-"+size+"("+sdsize+")-"+deg+".bic");
		//FileParser.buildSyntheticBiclusters(3,2,60, 10, "Venn3.bic");
		System.out.println("Termina main");
		//Intersección de dos triángulos en la misma arista de uno de ellos
		/*
		Polygon p1=new Polygon();
		p1.addPoint(0, 0);
		p1.addPoint(20, 0);
		p1.addPoint(10, 20);
		//p1.addPoint(0, 0);
		Polygon p2=new Polygon();
		p2.addPoint(0, -10);
		p2.addPoint(20, -10);
		p2.addPoint(10, 10);
		//p1.addPoint(0, 1);
		Graph.intersect(p1, p2);
		*/
		/*Line2D.Double line=new Line2D.Double(0,0,2,0);
		System.out.println("La distancia de 0,1 es "+line.ptSegDist(0,1));
		System.out.println("La distancia de 2,1 es "+line.ptSegDist(2,1));
		System.out.println("La distancia de 1,1 es "+line.ptSegDist(1,1));
		System.out.println("La distancia de 3,0 es "+line.ptSegDist(3,0));
		System.out.println("La distancia de 1,0 es "+line.ptSegDist(1,0));
		*/
		//Intersección de dos cuadrados en distintas aristas
		/*
		Polygon p1=new Polygon();
		p1.addPoint(0, 0);
		p1.addPoint(20, 0);
		p1.addPoint(20, 20);
		p1.addPoint(0, 20);
		//p1.addPoint(0, 0);
		Polygon p2=new Polygon();
		p2.addPoint(10, 10);
		p2.addPoint(30, 10);
		p2.addPoint(30, 30);
		p1.addPoint(10, 30);
		Graph.intersect(p1, p2);
*/
		/*
		Polygon p1=new Polygon();
		p1.addPoint(205, 496);
		p1.addPoint(336, 578);
		p1.addPoint(856, 517);
		//p1.addPoint(774, 179);
		p1.addPoint(390, 266);
		p1.addPoint(205, 496);
		Polygon p2=new Polygon();
		p2.addPoint(124,765);
		p2.addPoint(467,881);
		p2.addPoint(856,517);
		p2.addPoint(774,179);
		//p2.addPoint(205,496);
		p2.addPoint(124,765);
		*/
		//Graph.intersect(p1, p2);
		
	}
}