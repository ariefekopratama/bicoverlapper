package es.usal.bicoverlapper.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Class that contains Bubble information. A bubble is considered as a group of genes
 * and conditions from a gene expression matrix. Bubble display properties such as
 * position, size, color or transparency should be defined by the user, but are
 * expected to be defined depending on elements grouped, number of elements, nature
 * and homogeneity, respectively. 
 * 
 * @author Rodrigo Santamaria (rodri@usal.es)
 *
 */
public class Bubble 
	{
	/**
	 * Names of genes in this bubble
	 */
	public ArrayList<String> genes;
	/**
	 * Names of conditions in this bubble
	 */
	public ArrayList<String> conditions;
	/**
	 * Coordinates for this bubble
	 */
	public Point2D.Float	position;
	/**
	 * Name of the biclustering methods that retrieved the bicluster represented by this bubble
	 */
	public String method;
	
	/**
	 * Size, computed as genes.length x conditions.length
	 */
	public int size;
	
	/**
	 * Homogeneity of expression levels in the bubble
	 * STILL IN DEVELOPMENT
	 */
	public double homogeneity;
	
	/**
	 * Default constructor
	 *
	 */
	public Bubble()
		{
		genes=new ArrayList<String>();
		conditions=new ArrayList<String>();
		position=new Point2D.Float(0,0);
		method="";
		size=0;
		homogeneity=0;
		}
	}
