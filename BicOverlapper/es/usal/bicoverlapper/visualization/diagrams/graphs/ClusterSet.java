package es.usal.bicoverlapper.visualization.diagrams.graphs;


import java.util.ArrayList;

import es.usal.bicoverlapper.utils.CustomColor;



/**
 * ClusterSet keeps track of all the (bi)clusters returned by certain (bi)clustering algorithm, or 
 * any other organization of cluster as a group that would be necessary
 * Each ClsuterSet has a representative color an label to distingish from other ClusterSets.
 * @author Roberto Therón and Rodrigo Santamaría
 *
 */
public class ClusterSet {
	private ArrayList<Cluster> clusters;
	CustomColor myColor = null;
	Graph myGraph = null;
	String label = "";
	
	/**
	 * Builds an empty ClusterSet
	 *
	 */
	public ClusterSet() {
		clusters = new ArrayList<Cluster>();
		}
	
	/**
	 * Adds a new cluster to the ClusterSet
	 * @param c Cluster to add to de ClusterSet
	 */
	public void addCluster(Cluster c){
		clusters.add(c);   
	}
	
	/**
	 * Set the Graph in which this ClusterSet is to be drawn
	 * @param h	Graph for the ClusterSet
	 */	
    public void setGraph(Graph h) {
     myGraph = h;
    }
    
    /**
     * Sets the CustomColor associated to this ClusterSet
     * @param c Color for this ClusterSet
     */
    public void setColor(CustomColor c) {
        myColor = c;
       }
    
    /**
     * Sets the label (name or brief description) of this ClusterSet
     * @param s	String with the label of the ClusterSet
     */
    public void setLabel(String s) {
        label = s;
      }
    
    /**
     * Draw all the clusters (groups) in this ResultSet
     *
     */
    public void draw() {
    	for (int i=0; i<clusters.size(); i++) {
    	      MaximalCluster c = (MaximalCluster)clusters.get(i);
    	      c.draw();
    	    }  
    	}

    /**
     * Gets all the Clusters in this ClusterSet as an ArrayList
     * @return	ArrayList with all the Clusters in the ClusterSet
     */
	public ArrayList<Cluster> getClusters() {
		return clusters;
	}

	/**
	 * Sets all the clusters to be in this ClusterSet
	 * @param clusters	ArrayList with all the Clusters to be in this ClusterSet
	 */
	public void setClusters(ArrayList<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	/**
	 * Removes a Cluster from the ClusterSet, if it is in
	 * @param c	Cluster to remove
	 */
	public void removeCluster(Cluster c)
		{
		clusters.remove(c);
		}
}