package es.usal.bicoverlapper.data;
import java.util.Vector;

/**
 * Set of multidimensional variables or tuples, built as a set of  @link Field
 * TO BE REPLACED COMPLETELY BY PREFUSE TABLES
 * @author Javier Molpeceres and Rodrigo Santamaria
 */
public class MultidimensionalData {
	
	private String nomFile;
	private Vector<Field> vars;
	private String[] idTuplas = null;
	private int[] numDecimales = null;
	
	/**
	 * Builds and empty MultidimensionalData
	 * 
	 */
	public MultidimensionalData() {
		this.vars = new Vector<Field>(0,1);
	}

	/**
	 * Adds a @link Field to the Multidimensional Set
	 * 
	 * @param field <code>Field</code> with the new variable
	 */
	public void addField(Field field) {
		this.vars.add(field);		
	}
	
	/**
	 * Returns the field with the corresponding name
	 * 
	 * @param name Name of the requested field
	 * @return a <code>Field</code> with the requested field
	 */
	public Field getField(String name) {
		
		Field var = null;
		
		for(int i=0; i<vars.size(); i++) {
			if(((Field)vars.elementAt(i)).getName().equals(name)) {
				var = (Field)vars.elementAt(i);
				break;
			}
		}
		
		return var;
	}
	
	
	/**
	 * Returns the position of a field with the corresponding name
	 * The position of the field is the order in which it has been added
	 * @param name	name of the requested field
	 * @return		position of the requested field
	 */
	public int getFieldPos(String name)
		{
		for(int i=0; i<vars.size(); i++) 	if(((Field)vars.elementAt(i)).getName().equals(name))	return i;
		return -1;
		}
	
	
	/**
	 * Returns the variable at the requested position. The position of the variable
	 * is the order in which it has been added
	 * 
	 * @param index Position at which the variable is
	 * @return a <code>Field</code> with the requested variable
	 */
	public Field fieldAt(int index) {
		return (Field)vars.elementAt(index);
	}
	
	/**
	 * Returns a string with the requested value as a text string, for a determinate dimension
	 * @param value	value to format to text
	 * @param dim	dimension to which the value pertains
	 * @return	a text string with the value, formatted to have the number of decimals set for its dimension
	 */
	public String format(double value, int dim)
		{
		int nc=numDecimales[dim];
		String cad;
		if(nc>0)	cad=new Double(value).toString();
		else		cad=new Double(Math.abs(Math.rint(value))).toString();
		int pos=0;
		if((pos=cad.indexOf("."))>=0)
			{
			int pos2=0;
			if((pos2=cad.indexOf("E"))>=0)
				{
				if(nc>0)	cad=cad.substring(0,pos)+cad.substring(pos, pos+1+nc)+cad.substring(pos2);
				else		cad=cad.substring(0,pos)+cad.substring(pos2);
				}
			else
				{
				if(nc>0)
					{
					if(pos+1+nc<=cad.length())	cad=cad.substring(0,pos)+cad.substring(pos, pos+1+nc);
					else						;//En este caso ya está, no tenemos que quitar decimales
					}
				else		cad=cad.substring(0,pos);
				}
			}
		return cad;
		}

	static String formatDecimals(double value, int nc)
	{
	String cad;
	if(nc>0)	cad=new Double(value).toString();
	else		cad=new Double(Math.abs(Math.rint(value))).toString();
	int pos=0;
	if((pos=cad.indexOf("."))>=0)
		{
		int pos2=0;
		if((pos2=cad.indexOf("E"))>=0)
			{
			if(nc>0)	cad=cad.substring(0,pos)+cad.substring(pos, pos+1+nc)+cad.substring(pos2);
			else		cad=cad.substring(0,pos)+cad.substring(pos2);
			}
		else
			{
			if(nc>0)	
				{
				if(cad.substring(pos+1).length()>nc)	cad=cad.substring(0,pos)+cad.substring(pos, pos+1+nc);
				else									cad=cad.substring(0,pos);
				}
			else		cad=cad.substring(0,pos);
			}
		}
	return cad;
	}

	/**
	 * Removes all the fields in the set
	 *
	 */
	public void removeAllVars() {
		this.vars.removeAllElements();
	}
	
	/**
	 * Returns the number of fields in the set
	 * 
	 * @return the number of fields in the set
	 */
	public int getNumFields() {
		return this.vars.size();
	}
	
	/**
	 * Returns the number of tuples in the set
	 * NOTE: STILL IN DEVELOPMENT - there is no control of the number of tuples
	 * that each field has. Theoretically, all fields must have all the tuples,
	 * but in practice, tuples with different dimensions can be added
	 * 
	 * @return number of tuples in the multidimensional set
	 */
	public int getNumTuples() {
		return this.fieldAt(1).size();		
	}
	
	/**
	 * Sets the path file from which the variables in this set has been taken
	 * @deprecated
	 * 
	 * @param path Path of the file from which the variables in the set come
	 */
	public void setFileName(String path){
		this.nomFile = path;
	}
	
	/**
	 * Returns  the path file from which the variables in this set has been taken
	 * @deprecated
	 * 
	 * @return path of the file from which the variables in the set come
	 */
	public String getFileName(){
		return this.nomFile;
	}
	
	/**
	 * Sets the names of the tuples
	 * 
	 * @param names array with the names of the tuples
	 */
	public void setTupleNames(String[] names){
		this.idTuplas = names;
	}
	
	/**
	 * Returns the name of the tuple i
	 * 
	 * @param i position of the requested tuple
	 * @return name of the requested tuple
	 */
	public String getTupleName(int i){
		return this.idTuplas[i];
	}
	
	/**
	 * Checks if tuple names have been set
	 * 
	 * @return true if tuples have names, false otherwise
	 */
	public boolean hasTupleNames(){
		return !(this.idTuplas == null);
	}

	int[] getNumDecimales() {
		return numDecimales;
	}

	int getNumDecimales(int pos) {
		return numDecimales[pos];
	}

	/**
	 * Sets the maximum number of decimals for each field
	 * @param numDecimals	array with the number of decimals for each field
	 */
	public void setDecimalCount(int[] numDecimals) {
		this.numDecimales = numDecimals;
	}
}