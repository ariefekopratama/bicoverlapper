package data;
import java.util.Vector;

/**
 * Class that represents a field (column) of the tuple set
 * 
 * @author Javier Molpeceres
 */
public class Field {
	
	private String name;
	private Vector<Double> data;
	
	/**
	 * Builds a field
	 * 
	 * @param n name of the field
	 */
	public Field(String n) {
		this.name = n;
		this.data = new Vector<Double>();
	}
	
	/**
	 * Returns the name of the field
	 * 
	 * @return the name of the field
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the name of the field
	 * 
	 * @param n name of the field
	 */
	public void setName(String n) {
		this.name = n;
	}
	
	/**
	 * Returns the value of all the tuples for this field
	 * 
	 * @return <code>Vector<Double></code> with values of tuples for the field
	 */
	public Vector<Double> getData(){
		return this.data;
	}
	
	/**
	 * Adds a new tuple value for this field
	 * 
	 * @param i double value to add to field
	 */
	public void addData(double i) {
		this.data.add(new Double(i));
	}
	
	/**
	 * Returns the tuple value for this field at a determinate position
	 * 
	 * @param index position of the value to be returned
	 * @return double value requested
	 */
	public double getData(int index) {
		Double valor = (Double)this.data.elementAt(index);
		return valor.doubleValue();
	}
	
	/**
	 * Returns the number of tuples that contain this field
	 * 
	 * @return number of tuples
	 */
	public int size() {
		return this.data.size();
	}
}