package es.usal.bicoverlapper.data;

import java.util.Vector;


/**
 * Class with Data Utilities, as functions for normalization, average, deviation, etc.
 * 
 * @author Javier Molpeceres
 * @version 3.2, 26/3/2007
 */
public abstract class DataUtils {

	/**
	 * Returns the mean of a vector
	 * @param vect <code>Vector<Double></code> to compute the mean
	 * @return <code>double</code> with the mean of vect.
	 */
	public static double avg(Vector<Double> vect){
		double suma = 0;
		for(int i = 0; i < vect.size(); i++){
			suma += vect.elementAt(i).doubleValue();
		}
		return suma/vect.size();
	}
	
	/**
	 * Returns the standard deviation for a vector
	 * 
	 * @param vect <code>Vector<Double></code> to compute the deviation
	 * @return <code>double</code>with the standard deviation of vect
	 */
	public static double dev(Vector<Double> vect){
		double media = DataUtils.avg(vect), suma = 0;
		for(int i = 0; i < vect.size(); i++){
			suma += Math.pow(Math.abs(vect.elementAt(i)-media),2);
		}
		return Math.sqrt(suma/(vect.size()-1));
	}
	
	/**
	 * Returns the covariance between to variables or vectors
	 * 
	 * @param varX <code>Vector</code> with variable X
	 * @param varY <code>Vector</code> with variable Y
	 * @return <code>double</code> covariance(X,Y)
	 */
	public static double covar(Vector<Double> varX, Vector<Double> varY){
		double suma = 0, mediaX = DataUtils.avg(varX), mediaY = DataUtils.avg(varY);
		for(int i = 0; i < varX.size(); i++){
			suma += (varX.elementAt(i)-mediaX)*(varY.elementAt(i)-mediaY);
		}
		return suma/(varX.size()-1);
	}
	
	/**
	 * Returns the Pearson correlation coefficient for two variables
	 * @param varX <code>Vector</code> with variable X
	 * @param varY <code>Vector</code> with variable Y
	 * @return <code>double</code> with Pearson coefficient
	 */
	static double pearson(Vector<Double> varX, Vector<Double> varY){
		return DataUtils.covar(varX, varY)/(DataUtils.dev(varX)*DataUtils.dev(varY));
	}
	
	/**
	 * Returns the sum of the powered values in a vector
	 * 
	 * @param vect <code>Vector</code> with data to sum
	 * @param power <code>int</code> with potence for values in the vector
	 * @return <code>double</code> with de poweredSum
	 */
	static double poweredSum(Vector<Double> vect,int power){
		double sumatorio = 0;
		for(int i = 0; i < vect.size(); i++){
			sumatorio += Math.pow(vect.elementAt(i).doubleValue(),power);
		}
		return sumatorio;
	}
	
	/**
	 * Returns the sum of the product of the powered values of two vectors
	 * @param varX <code>Vector</code> with variable X.
	 * @param varY <code>Vector</code> with variable Y.
	 * @param powerX <code>int</code> with power for elements in variable X.
	 * @param powerY <code>int</code> with power for elements in variable Y.
	 * @return <code>double</code> with the power-product sumatory
	 */
	static double sumXY(Vector<Double> varX, Vector<Double> varY, int powerX, int powerY){
		double sumatorio = 0;
		for(int i = 0; i < varX.size(); i++){
			sumatorio += Math.pow(varX.elementAt(i).doubleValue(), powerX)*Math.pow(varY.elementAt(i).doubleValue(), powerY);
		}
		return sumatorio;
	}
	
	/**
	 * Devuelve el coeficiente independiente del Least Square Error - Linear de dos variables.
	 * 
	 * @param varX Variable independiente para el LSE.
	 * @param varY Variable dependiente para el LSE.
	 * @return <code>double</code> coeficiente independiente del LSE.
	 */
	static double indepLSE(Vector<Double> varX, Vector<Double> varY){
		double sumX = poweredSum(varX,1);
		double sumX2 = poweredSum(varX,2);
		double sumY = poweredSum(varY,1);
		double sumXY = sumXY(varX,varY,1,1);
		
		return (sumY*sumX2-sumX*sumXY)/(varX.size()*sumX2-sumX*sumX);
	}
	
	/**
	 * Devuelve el coeficiente dependiente del Least Square Error - Linear de dos variables.
	 * 
	 * @param varX Variable independiente para el LSE.
	 * @param varY Variable dependiente para el LSE.
	 * @return <code>double</code> coeficiente dependiente del LSE.
	 */
	static double depenLSE(Vector<Double> varX, Vector<Double> varY){
		double sumX = poweredSum(varX,1);
		double sumX2 = poweredSum(varX,2);
		double sumY = poweredSum(varY,1);
		double sumXY = sumXY(varX,varY,1,1);
		
		return (varX.size()*sumXY-sumX*sumY)/(varX.size()*sumX2-sumX*sumX);
	}
}