package data;

import java.awt.Color;

import kernel.TupleSelection;
import kernel.Session;



/**
 * Class with the data structure shared by different diagrams
 * NOTE: to be deeply restructured
 * 
 * @author Javier Molpeceres
 * @version 3.2, 22/3/2007
 *
 */
public class DataLayer {
	
	private Session sesion;
	private String ejeX;
	private String ejeY;
	private TupleSelection selecPuntos = null;//Puntos seleccionados
	private TupleSelection filterPuntos = null;//Puntos que deben aparecer en pantalla
	private int tupla = -1;
	private java.awt.Color colorSelec, colorTupla;
	private double ejeA, ejeB, ejeC;
	private Color colorEjeA = Color.GREEN, colorEjeB = Color.BLACK, colorEjeC = Color.RED;
	
	/**
	 * Default constructor 
	 * 
	 * @param session Session this DataLayer is to be linked
	 */
	public DataLayer(Session session){
		this.sesion = session;
	}

	
	/**
	 * Copy contructor. Builds a <code>DataLayer</code> from another one
	 * 
	 * @param dataLayer DataLayer to be copied
	 */
	public DataLayer(DataLayer dataLayer){
		this.sesion = dataLayer.sesion;
		this.ejeX = dataLayer.ejeX;
		this.ejeY = dataLayer.ejeY;
		this.selecPuntos = dataLayer.selecPuntos;
		this.filterPuntos = dataLayer.filterPuntos;
		this.tupla = dataLayer.tupla;
		this.colorTupla = dataLayer.colorTupla;
		this.ejeA = dataLayer.ejeA;
		this.ejeB = dataLayer.ejeB;
		this.ejeC = dataLayer.ejeC;
		this.colorEjeA = dataLayer.colorEjeA;
		this.colorEjeB = dataLayer.colorEjeB;
		this.colorEjeC = dataLayer.colorEjeC;
	}
	

	/**
	 * Sets the axes names.
	 * @deprecated
	 * 
	 * @param xAxis Name of x axis
	 * @param yAxis Name of y axis
	 */
	public void setEjes(String xAxis, String yAxis) {
		this.ejeX = xAxis;
		this.ejeY = yAxis;
	}
	
	/**
	 * Returns the variable in the x axis
	 * @deprecated
	 * @return {@link Field} in the x axis
	 */
	public Field getXaxis() {
		return sesion.getData().getField(ejeX);
			
	}
	
	/**
	 * Returns the variable in the y axis
	 * @deprecated
	 * @return {@link Field} in the y axis
	 */
	public Field getYaxis() {
		return sesion.getData().getField(ejeY);
	}
	
	
	/**
	 * Returns the name of the @link Field the requested name
	 * @param	name	name of the field
	 * @return {@link Field} with the requested name
	 */
	public Field getAxis(String name) {
		return sesion.getData().getField(name);
		
	}
	
	/**
	 * Returns the name of the dimension set at X axis
	 * @deprecated
	 * @return name of the dimension at X axis
	 */
	public String getNameXaxis(){
		return ejeX;
	}
	
	/**
	 * Returns the name of the dimension set at Y axis
	 * @deprecated
	 * @return name of the dimension at Y axis
	 */
	public String getNameYaxis(){
		return ejeY;
	}
	
	/**
	 * Sets the selection of tuples (multidimensional points) 
	 * 
	 * @param selec @PointSelection of tuples
	 */
	public void setPointSelection(TupleSelection selec){
		this.selecPuntos = selec;		
	}
	
	/**
	 * Returns the selection of tuples (multidimensional points) 
	 * 
	 * @return @PointSelection of tuples
	 */
	public TupleSelection getPointSelection(){
		return selecPuntos;
	}
	
	/**
	 * Sets the color for tuple selection
	 * @deprecated
	 * @param colorSelec <code>Color</code> linked to selection of tuples
	 */
	public void setSelectionColor(Color colorSelec){
		this.colorSelec = colorSelec;
	}
	
	/**
	 * Returns the color used for tuple selection
	 * @deprecated
	 * 
	 * @return <code>Color</code> linked to selection of tuples
	 */
	public Color getSelectionColor(){
		return this.colorSelec;
	}

	/**
	 * Sets the selection of an unique tuple 
	 * @deprecated
	 * @param row row id of the tuple selected
	 */
	public void setSelectedTuple(int row){
		this.tupla = row;
	}
	
	/**
	 * Gets the selection of an unique tuple 
	 * @deprecated
	 * @return row of the tuple selected
	 */
	public int getSelectedTuple(){
		return this.tupla;
	}
	
	/**
	 * Sets the color of the selected unique tuple
	 * @deprecated
	 * 
	 * @param color <code>Color</code> linked to the selected tuple
	 */
	public void setSelectedTupleColor(Color color){
		this.colorTupla = color;
	}
	
	/**
	 * Gets the color of the selected unique tuple
	 * @deprecated
	 * 
	 * @return <code>Color</code> linked to the selected tuple
	 */
	public Color getSelectedTupleColor(){
		return this.colorTupla;
	}
	
	/**
	 * Establece la coordenada determinada por el primer eje del diagrama de mapeo de color.
	 * 
	 * @param valor Coordenada del primer eje.
	 */
	 void setEjeA(double valor){
		this.ejeA = valor;
	}
	
	/**
	 * Devuelve la coordenada del primer eje del diagrama de mapeo de color.
	 * 
	 * @return Coordenada del primer eje.
	 */
	double getEjeA(){
		return this.ejeA;
	}
	
	/**
	 * Establece la coordenada determinada por el segundo eje del diagrama de mapeo de color.
	 * 
	 * @param valor Coordenada del segundo eje.
	 */
	 void setEjeB(double valor){
		this.ejeB = valor;
	}
	
	/**
	 * Devuelve la coordenada del segundo eje del diagrama de mapeo de color.
	 * 
	 * @return Coordenada del segundo eje.
	 */
	 double getEjeB(){
		return this.ejeB;
	}
	
	/**
	 * Establece la coordenada determinada por el tercer eje del diagrama de mapeo de color.
	 * 
	 * @param valor Coordenada del tercer eje.
	 */
	 void setEjeC(double valor){
		this.ejeC = valor;
	}
	
	/**
	 * Devuelve la coordenada del tercer eje del diagrama de mapeo de color.
	 * 
	 * @return Coordenada del tercer eje.
	 */
	 double getEjeC(){
		return this.ejeC;
	}
	
	/**
	 * Establece el color que marca el primer eje para el gradiente del mapeo de color.
	 * 
	 * @param color Color para el primer eje.
	 */
	 void setColorEjeA(Color color){
		this.colorEjeA = color;
	}
	
	/**
	 * Devuelve el color para el primer eje del mapeo del color.
	 * 
	 * @return Color del primer eje.
	 */
	 Color getColorEjeA(){
		return this.colorEjeA;
	}
	
	/**
	 * Establece el color que marca el segundo eje para el gradiente del mapeo de color.
	 * 
	 * @param color Color para el segundo eje.
	 */
	 void setColorEjeB(Color color){
		this.colorEjeB = color;
	}
	
	/**
	 * Devuelve el color para el segundo eje del mapeo del color.
	 * 
	 * @return Color del segundo eje.
	 */
	 Color getColorEjeB(){
		return this.colorEjeB;
	}
	
	/**
	 * Establece el color que marca el tercer eje para el gradiente del mapeo de color.
	 * 
	 * @param color Color para el tercer eje.
	 */
	 void setColorEjeC(Color color){
		this.colorEjeC = color;
	}
	
	/**
	 * Devuelve el color para el tercer eje del mapeo del color.
	 * 
	 * @return Color del tercer eje.
	 */
	 Color getColorEjeC(){
		return this.colorEjeC;
	}
	
	/**
	 * FILTERS: STILL IN DEVELOPMENT
	 * @return points filtered. Points filtered will be point that aren't selected but also aren't drawn at all
	 */
	public TupleSelection getFilterPoints() {
		return filterPuntos;
	}

	/**
	 * FILTERS: STILL IN DEVELOPMENT
	 * param filterPoints points filtered. Points filtered will be point that aren't selected but also aren't drawn at all
	 */
	public void setFilterPoints(TupleSelection filterPoints) {
		this.filterPuntos = filterPoints;
	}	
}