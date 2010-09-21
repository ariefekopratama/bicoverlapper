package es.usal.bicoverlapper.utils;

import java.util.ArrayList;

/** Utility class with methods that would be very slow to run on R
 * 
 * @author rodri
 *
 */
public final class RUtils {
	
	/**
	 * Converts a list of list L (where Ln contains the elements related to the object n)
	 *   to a matrix A, elements are in columns, and objects in rows, where Aij is 1 if the element j is in the list Li corresponding to the object i 
	 * @return
	 */
	public final int[][] listToMatrix(ArrayList<ArrayList<String>> list, int nrows, int ncols)
		{
		int[][] mat=new int[nrows][ncols];
		for(int i=0;i<nrows;i++)	for(int j=0;j<ncols;j++)	mat[i][j]=0;
		
		for(ArrayList<String> l : list)
			{
			for(String s : l)
				{
				System.out.println(s);
				}
			}
		return mat;
		}
	
	public String sayHello(String cad)
		{
		return "Hello"+cad;
		}
}
