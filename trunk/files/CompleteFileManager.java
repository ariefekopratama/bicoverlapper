package files;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.Integer;


/**
 * Extension of SimpeFileManager to read fields into a file.
 * NOTE: to be deprecated
 * @author Rodrigo Santamaria
 *
 */
public class CompleteFileManager extends SimpleFileManager 
{

	/*Tienen que pasarnos como argumento la lista enlazada con las lineas de fichero previamente leidas
	con el metodo leeFichero. Esto es mejor que leer el archivo cada vez que se llame al metodo, ya que
	la extraccion de varios campos solo requerira una lectura del fichero.*/

	/*Es importante aclarar que la fila y la columna indicadas son respecto al 0, es decir, la primera
	l�nea es la linea 0 y la primera columna es la columna 0.*/

	/*
	static String extraerAlfanumericoEncolumnado(LinkedList fichero, int linea, int posicion, int tamagnos[]) throws Exception
	{

	if((linea>=fichero.size()) || (posicion>=tamagnos.length))
		return null;

	int indiceComienzo=0;
	int indiceFin=0;
	String lineaFichero=(String)fichero.get(linea);

	for(int i=0; i<posicion;i++)
		indiceComienzo+=tamagnos[i];
	indiceFin=indiceComienzo+tamagnos[posicion];

	if(indiceComienzo>=lineaFichero.length())
		return null;
	if(indiceFin>=lineaFichero.length())
		return lineaFichero.substring(indiceComienzo).trim();
	else
		return lineaFichero.substring(indiceComienzo, indiceFin).trim();

	}
*/

	/**
	 * Reads a text from a field in the file, as a string
	 * @param file	file as a LinkedList of Strings
	 * @param row	row of the field to read
	 * @param column	column of the field to read
	 * @param delimiter	delimiter for fields
	 * @return	text read
	 * @throws Exception
	 */
	static String readString(LinkedList file, int row, int column, char delimiter) throws Exception
	{
	if(row>=file.size())
		return null;
		
	String lineaFichero=(String)file.get(row);
	int temp=0;
	int proximoDel=0;
	int posComienzo=0;
	int posFin=0;

	if(column!=0)
		for(proximoDel=0;(temp!=-1)&&(proximoDel<column);proximoDel++)
			{
			temp=lineaFichero.indexOf(delimiter, posComienzo);
			posComienzo=temp+1;
			}
			
	if(temp<0)
		return null;
		
	posFin=lineaFichero.indexOf(delimiter, posComienzo);

	if(posFin<0)
		return lineaFichero.substring(posComienzo);
	else
		{
		return lineaFichero.substring(posComienzo, posFin);
		}
	}

	/**
	 * Writes a text in a field
	 * @param file	file as a LinkedList of Strings
	 * @param row	row of the field to write
	 * @param column	column of the field to write
	 * @param text	text to write
	 * @param delimiter	delimiter for fields
	 * @throws Exception
	 */
	static void writeString(LinkedList file, int row, int column, String text, char delimiter) throws Exception
	{
	/*Si el fichero no tiene esa l�nea, devolvemos null.*/
	if(row>=file.size())		return;
		
	String lineaFichero=(String)file.get(row);
	StringTokenizer st=new StringTokenizer(lineaFichero, ""+delimiter ); 

	String newLine=new String();
	for(int i=0;i<st.countTokens();i++)
		{
		if(i!=0)	newLine=newLine+delimiter;
		if(i!=column)
			{
			newLine=newLine+st.nextToken();
			}
		else
			{
			st.nextToken();
			newLine=newLine+text;
			}
		}
	
	file.set(row, newLine);
	return;
	}
	
	
	/**
	 * Reads a text from a field in the file, as an integer
	 * @param file	file as a LinkedList of Strings
	 * @param row	row of the field to read
	 * @param column	column of the field to read
	 * @param delimiter	delimiter for fields
	 * @return	text read
	 * @throws Exception
	 */
	static int readInt(LinkedList file, int row, int column, char delimiter) throws Exception
	{
	/*Si el fichero no tiene esa l�nea, devolvemos null.*/
	if(row>=file.size())		return -1;
		
	String lineaFichero=(String)file.get(row);
	int temp=0;
	int proximoDel=0;
	int posComienzo=0;
	int posFin=0;

	/*Recorremos la cadena buscando el caracter delimitador que indica el principio del campo
	que nos piden. Si es el primer elemento no lo hacemos: no hay caracter delimitador antes.*/
	if(column!=0)
		for(proximoDel=0;(temp!=-1)&&(proximoDel<column);proximoDel++)
			{
			temp=lineaFichero.indexOf(delimiter, posComienzo);
			posComienzo=temp+1;
			}
			
	/*Si salimos del bucle porque no llega a encontrar tantos caracteres delimitadores como
	especificamos, es porque nos han pasado una posici�n que no est� en la cadena: devolvemos null.*/
	if(temp<0)		return -1;
		
	/*Buscamos el caracter delimitador del final del campo.*/
	posFin=lineaFichero.indexOf(delimiter, posComienzo);

	/*Si no encontrara este car�cter, el campo es el �ltimo de la l�nea.*/
	if(posFin<0)
		return (new Integer(lineaFichero.substring(posComienzo))).intValue();
	else
		return (new Integer(lineaFichero.substring(posComienzo, posFin))).intValue();
	}

	/**
	 * Reads a text from a field in the file, as a float
	 * @param file	file as a LinkedList of Strings
	 * @param row	row of the field to read
	 * @param column	column of the field to read
	 * @param delimiter	delimiter for fields
	 * @return	text read
	 * @throws Exception
	 */
	static float readFloat(LinkedList file, int row, int column, char delimiter) throws Exception
	{
	if(row>=file.size())		return -1;
		
	String lineaFichero=(String)file.get(row);
	int temp=0;
	int proximoDel=0;
	int posComienzo=0;
	int posFin=0;

	if(column!=0)
		for(proximoDel=0;(temp!=-1)&&(proximoDel<column);proximoDel++)
			{
			temp=lineaFichero.indexOf(delimiter, posComienzo);
			posComienzo=temp+1;
			}
			
	if(temp<0)		return -1;
		
	posFin=lineaFichero.indexOf(delimiter, posComienzo);

	if(posFin<0)
		return (new Float(lineaFichero.substring(posComienzo))).floatValue();
	else
		return (new Float(lineaFichero.substring(posComienzo, posFin))).floatValue();
	}

	/*Devolvemos un Objeto Integer en vez de un simple entero para poder devolver null cuando
	haya determinados errores (dentro del rango de int no podemos devolver ning�n valor como
	error ya que el valor almacenado en el campo del fichero puede ser cualquiera). Dado que la
	extracci�n del valor entero desde el Integer es muy sencilla, no creemos que esta decisi�n
	dificulte la interfaz. Lo mismo se aplicar� a los m�todos correspondientes a float y double.*/ 
	/*static Integer extraerIntEncolumnado(LinkedList fichero, int linea, int posicion, int tamagnos[]) throws Exception
	{
	Integer devolver;

	if((linea>=fichero.size()) || (posicion>=tamagnos.length))
		return null;

	int indiceComienzo=0;
	int indiceFin=0;
	String lineaFichero=(String)fichero.get(linea);

	for(int i=0; i<posicion;i++)
		indiceComienzo+=tamagnos[i];
	indiceFin=indiceComienzo+tamagnos[posicion];

	if(indiceComienzo>=lineaFichero.length())
		return null;

	if(indiceFin>=lineaFichero.length())
		devolver=new Integer(lineaFichero.substring(indiceComienzo).trim());
	else
		devolver=new Integer(lineaFichero.substring(indiceComienzo, indiceFin).trim());
		
	return devolver;
	}
*/
	/*
	 * Returns the number of rows of the file nombreFichero
	 */
	/*static int numRows(String nombreFichero)
		{
		int n=0;
		try{
		FileReader in=new FileReader(nombreFichero);
		BufferedReader fichero=new BufferedReader(in);
		while(fichero.readLine()!=null)	n++;
		}catch(Exception e){System.err.println(e.getStackTrace()); System.exit(1);}
		return n;
		}
	*/
}
