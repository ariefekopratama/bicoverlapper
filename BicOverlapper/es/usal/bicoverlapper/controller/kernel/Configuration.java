package es.usal.bicoverlapper.controller.kernel;

import java.awt.Dimension;
import java.awt.Point;

/**
 * Class with initial dimensions and ids for each Diagram
 *	TODO: To be deprecated 
 * 
 * @author Javier Molpeceres
 */
public class Configuration {
	
	public Dimension dimAplicacion = new Dimension(1300,800);//1000x600
	
	public Dimension dimParallelCoordinatesWindow = new Dimension(900,300);
	public Dimension dimNetworkWindow = new Dimension(600,430);
	public Dimension dimHeatmapWindow = new Dimension(355,610);//for 1280x800
	public Dimension dimOverlapperWindow = new Dimension(900,400);
	public Dimension dimWordCloudWindow = new Dimension(355,250);//for 1280x800
	
	
	public Dimension dimDataWindow = new Dimension(350,300);
	public Dimension dimPanelPuntos = new Dimension(900,300);
	public Dimension dimPanelHistograma = new Dimension(350,300);
	public Dimension dimPanelMapeo = new Dimension(350,300);
	public Dimension dimPanelDendrograma = new Dimension(900,300);
	public Dimension dimPanelBubbles = new Dimension(300,300);
	public Dimension dimPanelDataSelection = new Dimension(740,560);
	
	
	public int marginInternalWindowWidth=3+3; //margin of diagrams
	public int marginInternalWindowHeight=20+3; //margin of diagrams
	public int marginExternalWindowWidth=4+4;
	public int marginExternalWindowHeight=70+4;
	
	public Point initPC, initHM, initWC, initBM, initO, initTRN;
	
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
	public static final int CLOUD_ID = 15;
	
	public Configuration()
		{
		//TODO: give values to dimensions depending on screen size
		dimAplicacion=java.awt.Toolkit.getDefaultToolkit().getScreenSize();//user screen size
		Dimension dimDesktop=new Dimension(dimAplicacion.width-marginExternalWindowWidth, dimAplicacion.height-marginExternalWindowHeight);
		System.out.println("Screen size: "+dimDesktop.width+", "+dimDesktop.height);
		dimParallelCoordinatesWindow=new Dimension((int)(dimDesktop.width*0.66), (int)(dimDesktop.height*0.40));
		dimHeatmapWindow=new Dimension((int)(dimDesktop.width*0.34), (int)(dimDesktop.height)-marginInternalWindowHeight);
		dimOverlapperWindow=new Dimension((int)(dimDesktop.width*0.66), (int)(dimDesktop.height*0.60)-40);
		dimNetworkWindow=new Dimension((int)(dimDesktop.width*0.66), (int)(dimDesktop.height*0.66));
		dimWordCloudWindow=new Dimension((int)(dimDesktop.width*0.34), (int)(dimDesktop.height*0.34));
		dimPanelBubbles=new Dimension((int)(dimDesktop.width*0.34), (int)(dimDesktop.height*0.34));
		
		initPC=new Point(0,0);
		initHM=new Point(initPC.x+dimParallelCoordinatesWindow.width,0);
		initO=new Point(0, initPC.y+dimParallelCoordinatesWindow.height-43);
		initTRN=initO;
		initWC=new Point(initO.x+dimOverlapperWindow.width,(int)(dimDesktop.height)-dimWordCloudWindow.height-32);
		}
	/**
	 * Devuelve la dimension por defecto de la ventana principal de la aplicacion.
	 * 
	 * @return <code>Dimension</code> por defecto de la ventana principal de la aplicacion.
	 */
	public Dimension getApplicationSize(){
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
		return new Dimension(dimParallelCoordinatesWindow.width-marginInternalWindowWidth, dimParallelCoordinatesWindow.height-marginInternalWindowHeight);
		}

	/**
	 * Devuelve la dimension por defecto del panel del diagrama del histograma.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama del histograma.
	 */
	Dimension getSizePanelHistograma() {
		return new Dimension(dimHeatmapWindow.width-marginInternalWindowWidth, dimHeatmapWindow.height-marginInternalWindowHeight);
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
		return dimDataWindow;
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
		return new Dimension(dimNetworkWindow.width-marginInternalWindowWidth, dimNetworkWindow.height-marginInternalWindowHeight);
	}

	void setDimPanelTRN(Dimension dimPanelTRN) {
		this.dimNetworkWindow = dimPanelTRN;
	}

	/**
	 * Returns the initial dimension for Microarray Heatmap Diagrams
	 * 
	 * @return default <code>Dimension</code> for Microarray Heatmap Diagrams 
	 */
	public Dimension getDimPanelHeatmap() {
		return new Dimension(dimHeatmapWindow.width-marginInternalWindowWidth, dimHeatmapWindow.height-marginInternalWindowHeight);
		}

	void setDimPanelHeatmap(Dimension dimPanelHeatmap) {
		this.dimHeatmapWindow = dimPanelHeatmap;
	}

	/**
	 * Returns the initial dimension for BiclusVis Diagrams
	 * 
	 * @return default <code>Dimension</code> for BiclusVis Diagrams 
	 */
	public Dimension getDimPanelBiclusVis() {
		return new Dimension(dimOverlapperWindow.width-marginInternalWindowWidth, dimOverlapperWindow.height-marginInternalWindowHeight);
		}
	
	/**
	 * Returns the initial dimension for BiclusVis Diagrams
	 * 
	 * @return default <code>Dimension</code> for BiclusVis Diagrams 
	 */
	public Dimension getDimPanelWordCloud() {
		return new Dimension(dimWordCloudWindow.width-marginInternalWindowWidth, dimWordCloudWindow.height-marginInternalWindowHeight);
		}
	void setDimPanelWordCloud(Dimension dimPanelWords) {
		this.dimWordCloudWindow = dimPanelWords;
	}


	void setDimPanelBubbleGraph(Dimension dimPanelBubbleGraph) {
		this.dimOverlapperWindow = dimPanelBubbleGraph;
	}

	Dimension getDimPanelDataSelection() {
		return dimPanelDataSelection;
	}

	void setDimPanelDataSelection(Dimension dimPanelDataSelection) {
		this.dimPanelDataSelection = dimPanelDataSelection;
	}

}