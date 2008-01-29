package kernel;

import java.awt.Dimension;

/**
 * Class with initial dimensions and ids for each Diagram
 *	TODO: To be deprecated 
 * 
 * @author Javier Molpeceres
 */
public class Configuration {
	
	private Dimension dimAplicacion = new Dimension(1200,800);//1000x600
	private Dimension dimPanelPuntos = new Dimension(900,300);
	private Dimension dimPanelCoordenadas = new Dimension(900,300);
	private Dimension dimPanelHistograma = new Dimension(350,300);
	private Dimension dimPanelMapeo = new Dimension(350,300);
	private Dimension dimPanelDendrograma = new Dimension(900,300);
	private Dimension dimPanelDatos = new Dimension(350,300);
	private Dimension dimPanelBubbles = new Dimension(300,300);
	private Dimension dimPanelTRN = new Dimension(600,600);
	private Dimension dimPanelHeatmap = new Dimension(210,610);
	private Dimension dimPanelBubbleGraph = new Dimension(1000,600);
	private Dimension dimPanelDataSelection = new Dimension(740,560);
	private Dimension dimPanelWords = new Dimension(1000,500);
	
	static final int NoId = 0;
	static final int DiagramaPuntosId = 1;
	/**
	 * Unique identifier for Parallel Coordiantes Diagrams
	 */
	public static final int PARALLEL_COORDINATES_ID = 2;
	static final int DendrogramaId = 3;
	static final int HistogramaId = 4;
	static final int MapeoId = 5;
	/**
	 * Unique identifier for Bubble map Diagrams
	 */
	public static final int BUBBLE_MAP_ID = 6;
	static final int TreeMapId = 7;
	static final int CloudId = 8;
	static final int TablaDatosId = 9;
	static final int DataFilterId = 10;
	static final int DataPersonFilterId = 11;
	public static final int HEATMAP_ID = 12;
	public static final int OVERLAPPER_ID = 13;
	public static final int TRN_ID = 14;
	
	/**
	 * Devuelve la dimension por defecto de la ventana principal de la aplicacion.
	 * 
	 * @return <code>Dimension</code> por defecto de la ventana principal de la aplicacion.
	 */
	Dimension getApplicationSize(){
		return dimAplicacion;
	}
	
	/**
	 * Devuelve la dimension por defecto del panel del diagrama de puntos.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama de puntos.
	 */
	Dimension getSizePanelPuntos(){
		return dimPanelPuntos;
	}
	
	/**
	 * Returns the initial dimension for Parallel Coordinates Diagrams
	 * 
	 * @return default <code>Dimension</code> for Parallel Coordinates Diagrams 
	 */
	public Dimension getSizePanelCoordenadas() {
		return dimPanelCoordenadas;
	}

	/**
	 * Devuelve la dimension por defecto del panel del diagrama del histograma.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama del histograma.
	 */
	Dimension getSizePanelHistograma() {
		return dimPanelHistograma;
	}

	/**
	 * Devuelve la dimension por defecto del panel del diagrama del mapeo de color.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama del mapeo de color.
	 */
	Dimension getSizePanelMapeo() {
		return dimPanelMapeo;
	}
	
	/**
	 * Devuelve la dimension por defecto del panel del diagrama del dendrograma.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama del dendrograma.
	 */
	Dimension getSizePanelDendrograma() {
		return dimPanelDendrograma;
	}

	/**
	 * Devuelve la dimension por defecto de la ventana de datos.
	 * 
	 * @return <code>Dimension</code> por defecto de la ventana de datos.
	 */
	Dimension getSizePanelDatos() {
		return dimPanelDatos;
	}
	
	/**
	 * Returns the initial dimension for BubbleMap Diagrams
	 * 
	 * @return default <code>Dimension</code> for Bubble Map Diagrams 
	 */
	public Dimension getDimPanelBubbles() {
		return dimPanelBubbles;
	}

	void setDimPanelBubbles(Dimension dimPanelBubbles) {
		this.dimPanelBubbles = dimPanelBubbles;
	}

	/**
	 * Returns the initial dimension for TRN Diagrams
	 * 
	 * @return default <code>Dimension</code> for TRN Diagrams 
	 */
	public Dimension getDimPanelTRN() {
		return dimPanelTRN;
	}

	void setDimPanelTRN(Dimension dimPanelTRN) {
		this.dimPanelTRN = dimPanelTRN;
	}

	/**
	 * Returns the initial dimension for Microarray Heatmap Diagrams
	 * 
	 * @return default <code>Dimension</code> for Microarray Heatmap Diagrams 
	 */
	public Dimension getDimPanelHeatmap() {
		return dimPanelHeatmap;
	}

	void setDimPanelHeatmap(Dimension dimPanelHeatmap) {
		this.dimPanelHeatmap = dimPanelHeatmap;
	}

	/**
	 * Returns the initial dimension for BiclusVis Diagrams
	 * 
	 * @return default <code>Dimension</code> for BiclusVis Diagrams 
	 */
	public Dimension getDimPanelBiclusVis() {
		return dimPanelBubbleGraph;
	}

	void setDimPanelBubbleGraph(Dimension dimPanelBubbleGraph) {
		this.dimPanelBubbleGraph = dimPanelBubbleGraph;
	}

	Dimension getDimPanelDataSelection() {
		return dimPanelDataSelection;
	}

	void setDimPanelDataSelection(Dimension dimPanelDataSelection) {
		this.dimPanelDataSelection = dimPanelDataSelection;
	}

	Dimension getDimPanelWords() {
		return dimPanelWords;
	}

	void setDimPanelWords(Dimension dimPanelWords) {
		this.dimPanelWords = dimPanelWords;
	}
}