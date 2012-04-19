package es.usal.bicoverlapper.controller.kernel;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;

/**
 * Class with initial dimensions and ids for each Diagram TODO: To be deprecated
 * 
 * @author Javier Molpeceres
 */
public class Configuration {

	public Dimension dimAplicacion = new Dimension(1300, 800);// 1000x600

	public Dimension dimParallelCoordinatesWindow = new Dimension(900, 300);
	public Dimension dimNetworkWindow = new Dimension(600, 430);
	public Dimension dimHeatmapWindow = new Dimension(355, 610);// for 1280x800
	public Dimension dimOverlapperWindow = new Dimension(900, 400);
	public Dimension dimWordCloudWindow = new Dimension(355, 250);// for
																	// 1280x800

	// Carlos
	// por ejemplo esta dimension inicial
	public Dimension dimKeggWindow = new Dimension(355, 610);

	public Dimension dimDataWindow = new Dimension(350, 300);
	public Dimension dimPanelPuntos = new Dimension(900, 300);
	public Dimension dimPanelHistograma = new Dimension(350, 300);
	public Dimension dimPanelMapeo = new Dimension(350, 300);
	public Dimension dimPanelDendrograma = new Dimension(900, 300);
	public Dimension dimPanelBubbles = new Dimension(300, 300);
	public Dimension dimPanelDataSelection = new Dimension(740, 560);

	public int marginInternalWindowWidth = 3 + 3; // margin of diagrams
	public int marginInternalWindowHeight = 20 + 3; // margin of diagrams
	public int marginExternalWindowWidth = 4 + 4;
	public int marginExternalWindowHeight = 70 + 4;

	public Point initPC, initHM, initWC, initBM, initO, initTRN, initKegg;

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

	public Configuration() {
		// TODO: give values to dimensions depending on screen size
		dimAplicacion = java.awt.Toolkit.getDefaultToolkit().getScreenSize();// user
																				// screen
																				// size
		Dimension dimDesktop = new Dimension(dimAplicacion.width
				- marginExternalWindowWidth, dimAplicacion.height
				- marginExternalWindowHeight);
		System.out.println("Screen size: " + dimDesktop.width + ", "
				+ dimDesktop.height);
		dimParallelCoordinatesWindow = new Dimension(
				(int) (dimDesktop.width * 0.66),
				(int) (dimDesktop.height * 0.40));
		// dimHeatmapWindow=new Dimension((int)(dimDesktop.width*0.34),
		// (int)(dimDesktop.height)-marginInternalWindowHeight);
		dimHeatmapWindow = new Dimension((int) (dimDesktop.width * 0.34),
				(int) (dimDesktop.height * 0.66));
		dimOverlapperWindow = new Dimension((int) (dimDesktop.width * 0.66),
				(int) (dimDesktop.height * 0.60) - 40);
		dimNetworkWindow = new Dimension((int) (dimDesktop.width * 0.66),
				(int) (dimDesktop.height * 0.66));
		dimWordCloudWindow = new Dimension((int) (dimDesktop.width * 0.34),
				(int) (dimDesktop.height * 0.34) - 40);
		dimPanelBubbles = new Dimension((int) (dimDesktop.width * 0.34),
				(int) (dimDesktop.height * 0.34));
		
		
		dimKeggWindow = new Dimension((int) (dimDesktop.width * 0.45),
				(int) (dimDesktop.height * 0.55));

		initPC = new Point(0, 0);
		initHM = new Point(initPC.x + dimParallelCoordinatesWindow.width, 0);
		initO = new Point(0, initPC.y + dimParallelCoordinatesWindow.height
				- 43);
		initTRN = initO;
		initWC = new Point(initO.x + dimOverlapperWindow.width,
				(int) (dimDesktop.height) - dimWordCloudWindow.height - 32);
		initBM = initO;
		
		initKegg = new Point(0, initPC.y + dimParallelCoordinatesWindow.height + 7);

		// configureEnvironment();
	}

	/**
	 * Sets ups any possible environment variables required for the proper
	 * functioning of the program Environment variables should not be modified
	 * from code. Instructions for setting up on different OS:
	 * 
	 * 1) MACOS We can generate an app following
	 * http://www.centerkey.com/mac/java/ Atfer creating the app ad step 8, we
	 * need to create a prelude script and add it to the content of the app,
	 * following this one:
	 * http://www.amug.org/~glguerin/howto/More-open-files.html#prelude On this
	 * prelude script we can add everything we need, for BicOverlapper the
	 * export of R_HOME at least Then continue with step 9 for generating the
	 * installer.
	 * 
	 * NOTE: The Prelude thing does not work. It apparently works if you run the
	 * Prelude script inside the app, but not when double clicking the app. It
	 * seems we need to go Apparently Prelude solution will only work on 10.2
	 * and up (or might be done, but it will work in previous ones?)
	 * LSEnvironment only works on 10.3 and up
	 * 
	 * 
	 */
	/*
	 * public void configureEnvironment() {
	 * System.out.println(System.getProperty("os.name")); String
	 * os=System.getProperty("os.name"); try { if(os.contains("indows")) {} else
	 * if(os.contains("ac")) {
	 * System.out.println("Setting up environment for Mac"); //By now only
	 * setting up things for this one //Runtime.getRuntime().exec(
	 * "set R_HOME=/Library/Frameworks/R.framework/Resources"); } else
	 * if(os.contains("ux")) {
	 * 
	 * } } catch (IOException e) { e.printStackTrace(); }
	 * 
	 * }
	 */
	/**
	 * Devuelve la dimension por defecto de la ventana principal de la
	 * aplicacion.
	 * 
	 * @return <code>Dimension</code> por defecto de la ventana principal de la
	 *         aplicacion.
	 */
	public Dimension getApplicationSize() {
		return dimAplicacion;
	}

	/**
	 * Devuelve la dimension por defecto del panel del diagrama de puntos.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama de
	 *         puntos.
	 */
	Dimension getSizePanelPuntos() {
		return dimPanelPuntos;
	}

	/**
	 * Returns the initial dimension for Parallel Coordinates Diagrams
	 * 
	 * @return default <code>Dimension</code> for Parallel Coordinates Diagrams
	 */
	public Dimension getSizePanelCoordenadas() {
		return new Dimension(dimParallelCoordinatesWindow.width
				- marginInternalWindowWidth,
				dimParallelCoordinatesWindow.height
						- marginInternalWindowHeight);
	}

	/**
	 * Devuelve la dimension por defecto del panel del diagrama del histograma.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama del
	 *         histograma.
	 */
	Dimension getSizePanelHistograma() {
		return new Dimension(
				dimHeatmapWindow.width - marginInternalWindowWidth,
				dimHeatmapWindow.height - marginInternalWindowHeight);
	}

	/**
	 * Devuelve la dimension por defecto del panel del diagrama del mapeo de
	 * color.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama del
	 *         mapeo de color.
	 */
	Dimension getSizePanelMapeo() {
		return dimPanelMapeo;
	}

	/**
	 * Devuelve la dimension por defecto del panel del diagrama del dendrograma.
	 * 
	 * @return <code>Dimension</code> por defecto del panel del diagrama del
	 *         dendrograma.
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
		return new Dimension(
				dimNetworkWindow.width - marginInternalWindowWidth,
				dimNetworkWindow.height - marginInternalWindowHeight);
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
		return new Dimension(
				dimHeatmapWindow.width - marginInternalWindowWidth,
				dimHeatmapWindow.height - marginInternalWindowHeight);
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
		return new Dimension(dimOverlapperWindow.width
				- marginInternalWindowWidth, dimOverlapperWindow.height
				- marginInternalWindowHeight);
	}

	/**
	 * Returns the initial dimension for BiclusVis Diagrams
	 * 
	 * @return default <code>Dimension</code> for BiclusVis Diagrams
	 */
	public Dimension getDimPanelWordCloud() {
		return new Dimension(dimWordCloudWindow.width
				- marginInternalWindowWidth, dimWordCloudWindow.height
				- marginInternalWindowHeight);
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

	/**
	 * Returns the initial dimension for Kegg Diagrams
	 * 
	 * @return default <code>Dimension</code> for Kegg Diagrams
	 */
	public Dimension getDimPanelKegg() {
		return dimKeggWindow;
	}

	void setDimPanelKegg(Dimension dimPanelKegg) {
		this.dimKeggWindow = dimPanelKegg;
	}
}