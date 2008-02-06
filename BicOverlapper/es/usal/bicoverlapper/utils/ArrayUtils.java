package es.usal.bicoverlapper.utils;

/**
 * Utility class for array searching
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * @version 3.2, 22/3/2007 
 */
import java.util.ArrayList;
import java.util.Vector;

public class ArrayUtils {

	
	/**
	 * Returns the number of elements that appear both in a and b
	 * @param a ArrayList of Strings
	 * @param b ArrayList of Strings
	 * @return	number of elements that appear both in a and b
	 */
	public static int intersect(ArrayList<String> a, ArrayList<String> b)
		{
		int k=0;
		if(a==null || b==null)	return k;
		for(int i=0;i<a.size();i++)
			{
			String cad=(String)a.get(i);
			for(int j=0;j<b.size();j++)
				{
				if(cad.equals((String)b.get(j)))	{k++;break;}
				}
			}
		return k;
		}

	/**
	 * Checks if an int[] array contains a value
	 * 
	 * @param array int[] to check
	 * @param value int to search in the array
	 * @return true if value in in array, false otherwise
	 */
	public static boolean contains(int[] array,int value){
		for(int i = 0; i < array.length; i++){
			if(array[i] == value)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if a Vector<String> contains a String
	 * 
	 * @param vector Vector to check
	 * @param value String to search in the vector
	 * @return true if value is in vector, false otherwise
	 */
	public static boolean contains(Vector<String> vector,String value){
		if(vector != null){
			for(int i = 0; i < vector.size(); i++){
				if(vector.elementAt(i).equals(value))
					return true;
			}
		}
		return false;
	}
}