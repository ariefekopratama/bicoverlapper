package kernel;

import java.awt.Color;
/**
 * Class with tuples (multidimensional variables) selected.
 * If our data are n m-dimensional points pi=(a1,..., aj,...am),
 * it maintains two dimensions ax, ay and two lists px1,...,pxn and 
 * py1,...,pyn, where pxi=true if the tuple pi has been selected for dimension ax,
 * and false otherwise (analogous for py and ay).
 * This was initially tailored for scatterplots, were two dimensions are compared.
 * For other visualizations, only ax and its list are used, and usually only the list
 *  
 * NOTE: this class is to be deprecated or deeply modified.
 * @author Javier Molpeceres and Rodrigo Santamaria
 */
public class TupleSelection {
	
	private String ejeX, ejeY;
	private boolean[] selecX, selecY;
	private java.awt.Color[] colorSelec;
	private boolean[] lastSelec;
	
	/**
	 * Builds a new <code>TupleSelection</code> with no element selected
	 * 
	 * @param dimX Name of the dimension (or field) on X axis
	 * @param dimY Name of the dimension (or field) on Y axis
	 * @param size Number of variables
	 */
	public TupleSelection(String dimX,String dimY,int size){
		this.ejeX = dimX;
		this.ejeY = dimY;
		this.selecX = new boolean[size];
		this.selecY = new boolean[size];
		this.lastSelec = new boolean[size];
		this.colorSelec=new Color[size];
		for(int i = 0; i < size; i++){
			this.selecX[i] = false;
			this.selecY[i] = false;
			this.colorSelec[i]=null;
			this.lastSelec[i] = false;
		}
	}
	
	/**
	 * Sets dimX name
	 * 
	 * @param dim Name of the dimension under which we select
	 */
	public void setDimX(String dim){
		this.ejeX = dim;
	}
	
	/**
	 * Sets dimY name
	 * 
	 * @param dim Name of the dimension under which we select
	 */
	public void setDimY(String dim){
		this.ejeY = dim;
	}
	
	/**
	 * Sets the selection status of a variable under X dimension
	 * 
	 * @param pos index of the variable
	 * @param value status, true if the variable is selected, false otherwise
	 */
	public void setX(int pos, boolean value){
		this.selecX[pos] = value;
	}
	
	/**
	 * Sets the selection status of a variable under Y dimension
	 * 
	 * @param pos index of the variable
	 * @param value status, true if the variable is selected, false otherwise
	 */
	public void setY(int pos, boolean value){
		this.selecY[pos] = value;
	}
	
	/**
	 * Returns the status selection of a tuple under X dimension
	 * 
	 * @param i index of the tuple
	 * @return true if the tuple i is selected, false otherwise
	 */
	public boolean isSelectedX(int i){
		return selecX[i];
	}
	
	/**
	 * Returns the status selection of a tuple under Y dimension
	 * 
	 * @param i index of the tuple
	 * @return true if the tuple i is selected, false otherwise
	 */
	public boolean getElementY(int i){
		return selecY[i];
	}
	
	/**
	 * Returns the name of selection dimension X
	 * 
	 * @return name of the dimension X
	 */
	public String getVarX(){
		return this.ejeX;
	}
	
	
	/**
	 * Returns the name of selection dimension X
	 * 
	 * @return name of the dimension X
	 */
	public String getVarY(){
		return this.ejeY;
	}
	
	/**
	 * Returns the number of tuples
	 * @return the number of tuples
	 */
	public int getNumTuples(){
		return this.selecX.length;
	}
	
	/**
	 * Returns the number of tuples selected
	 * @return the number of tuples selected
	 */
	public int getNumSelected()
	{
	int cont=0;
	for(int i=0;i<selecX.length;i++)	if(selecX[i])	cont++;
	return cont;
	}
	
	/**
	 * Returns the number of tuples selected from the subgroup of tuples filtered
	 * (non-filtered tuples are not drawn, while non-selected tuples are simply not highlighted) 
	 * TODO: Still in development.
	 * @param filter
	 */
	public int getNumSelected(TupleSelection filter)
		{
		if(filter!=null)
			{
			int cont=0;
			for(int i=0;i<selecX.length;i++)	if(selecX[i] && filter.isSelectedX(i))	cont++;
			return cont;
			}
		else	return getNumSelected();
		}
	
	Color getColorSelec(int pos) {
		return colorSelec[pos];
	}

	/**
	 * Sets the selection Color for each variable
	 * @param colorSelec	a Color array with colors for each variable
	 */
	void setColorSelec(java.awt.Color[] colorSelec) {
		this.colorSelec = colorSelec;
	}
	
	/**
	 * Sets the selection Color for a tuple
	 * @param c	a Color for the tuple
	 * @param pos			the index of the tuple
	 */
	public void setColorSelec(java.awt.Color c, int pos)
		{
		this.colorSelec[pos]=c;
		}

	/**
	 * Gets the status selection of a variable in an auxiliary variable vector
	 * @deprecated
	 */
	public boolean getLastSelec(int pos) {
		return lastSelec[pos];
	}

	/**
	 * Sets the status selection of a variable in an auxiliary variable vector
	 * @deprecated
	 */
	public void setLastSelec(boolean lastSelec, int pos) {
		this.lastSelec[pos] = lastSelec;
	}
}