package es.usal.bicoverlapper.controller.manager;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import es.usal.bicoverlapper.controller.kernel.Configuration;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.controller.util.Translator;
import es.usal.bicoverlapper.view.diagram.bubbles.BubblesDiagram;
import es.usal.bicoverlapper.view.diagram.heatmap.HeatmapDiagram;
import es.usal.bicoverlapper.view.diagram.network.NetworkDiagram;
import es.usal.bicoverlapper.view.diagram.overlapper.OverlapperDiagram;
import es.usal.bicoverlapper.view.diagram.parallelCoordinates.ParallelCoordinatesDiagram;
import es.usal.bicoverlapper.view.diagram.wordcloud.WordCloudDiagram;
import es.usal.bicoverlapper.view.main.BicOverlapperWindow;
import es.usal.bicoverlapper.view.main.DiagramWindow;




/**
 * Class that handles the View Menu Options
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * @version 3.2, 26/3/2007
 */
public class ViewMenuManager implements ActionListener{

	private BicOverlapperWindow ventana;
	private Configuration config;
	
	/**
	 * Constructor to build a MenuManager
	 * 
	 * @param window <code>BicOverlapperWindow</code> that will contain the menu that this manager controls
	 * @param config <code>Configuration</code> with initial configuration for the views.
	 */
	public ViewMenuManager(BicOverlapperWindow window, Configuration config){
		this.ventana = window;
		this.config = config;
	}
	
	/**
	 * Method invoked each time that an option in the view menu is clicked
	 */
	public void actionPerformed(ActionEvent e) {
		
		
		if(ventana.isActiveWorkDesktop()){
			Session sesion = ventana.getActiveWorkDesktop().getSession();
			
			
			if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s8"))){
				Dimension dim = config.getSizePanelCoordenadas();
				ParallelCoordinatesDiagram panel = new ParallelCoordinatesDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				ventana.setLocation(config.initPC.x, config.initPC.y);
				
				panel.setWindow(ventana);
				
				sesion.setParallelCoordinates(ventana);
			}
			else if(e.getActionCommand().equals("Bubble Map")){
				Dimension dim = config.getDimPanelBubbles();
				BubblesDiagram panel = new BubblesDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				ventana.setLocation(config.dimParallelCoordinatesWindow.width+config.dimHeatmapWindow.width+config.marginInternalWindowWidth*4, 0);
				sesion.setBubbles(ventana);
				panel.createAxisLayout();
				panel.run();
			}
			else if(e.getActionCommand().equals("Biological Network")){
				//System.out.println("Comenzamos la vista");
				Dimension dim = config.getDimPanelTRN();
				NetworkDiagram panel = new NetworkDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				sesion.setTRN(ventana);
				//ventana.setLocation(0, config.getSizePanelCoordenadas().height+30);
				ventana.setLocation(config.initTRN.x, config.initTRN.y);
				panel.create();
				panel.run();
			//	System.out.println("Terminamos la vista");
			}
			else if(e.getActionCommand().equals("Microarray Heatmap")){
				Dimension dim = config.getDimPanelHeatmap();
				HeatmapDiagram panel = new HeatmapDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				sesion.setTRN(ventana);
				//ventana.setLocation(config.getSizePanelCoordenadas().width+config.marginWidth*2,0);
				ventana.setLocation(config.initHM.x, config.initHM.y);
				panel.create();
				panel.run();
			}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s10"))){
				Dimension dim = config.getDimPanelBiclusVis();
				OverlapperDiagram panel = new OverlapperDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				//ventana.setSize(config.getDimPanelBiclusVis());
				ventana.setLocation(0, config.getSizePanelCoordenadas().height+30);
				
				sesion.setBubbleGraph(ventana);
				
					panel.create();
					panel.run();
			
			}
			else if(e.getActionCommand().equals("Bicluster Bubble Graph")){
				Dimension dim = config.getDimPanelBiclusVis();
				OverlapperDiagram panel = new OverlapperDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				sesion.setBubbleGraph(ventana);
				//ventana.setLocation(0, config.getSizePanelCoordenadas().height);
				ventana.setLocation(config.initO.x, config.initO.y);
				
				panel.create();
				panel.run();
			}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s13")))
				{
				Dimension dim = config.getDimPanelWordCloud();
				WordCloudDiagram panel = new WordCloudDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				//ventana.setLocation(0, config.getSizePanelCoordenadas().height+30);
				//ventana.setLocation(0, config.getSizePanelCoordenadas().height+30);
				ventana.setLocation(config.initWC.x, config.initWC.y);
				sesion.setWordCloud(ventana);
				panel.repaint();
				}
		}
	}
}