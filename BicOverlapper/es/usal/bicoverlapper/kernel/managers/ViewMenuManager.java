package es.usal.bicoverlapper.kernel.managers;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import es.usal.bicoverlapper.kernel.BicOverlapperWindow;
import es.usal.bicoverlapper.kernel.Configuration;
import es.usal.bicoverlapper.kernel.DiagramWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.OverlapperDiagram;
import es.usal.bicoverlapper.visualization.diagrams.BubblesDiagram;
import es.usal.bicoverlapper.visualization.diagrams.HeatmapDiagram;
import es.usal.bicoverlapper.visualization.diagrams.ParallelCoordinatesDiagram;
import es.usal.bicoverlapper.visualization.diagrams.TRNDiagram;
import es.usal.bicoverlapper.visualization.diagrams.WordCloudDiagram;




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
				panel.setWindow(ventana);
				
				sesion.setParallelCoordinates(ventana);
			}
			else if(e.getActionCommand().equals("Bubble Map")){
				Dimension dim = config.getDimPanelBubbles();
				BubblesDiagram panel = new BubblesDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				ventana.setLocation(config.dimPanelCoordenadas.width+config.dimPanelHeatmap.width, 0);
				sesion.setBubbles(ventana);
				panel.createAxisLayout();
				panel.run();
			}
			else if(e.getActionCommand().equals("Transcription Network")){
				//System.out.println("Comenzamos la vista");
				Dimension dim = config.getDimPanelTRN();
				TRNDiagram panel = new TRNDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				sesion.setTRN(ventana);
				ventana.setLocation(0, config.getSizePanelCoordenadas().height+30);
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
				ventana.setLocation(config.getSizePanelCoordenadas().width,0);
				panel.create();
				//panel.create3d();
				panel.run();
			}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s10"))){
				Dimension dim = config.getDimPanelBiclusVis();
				OverlapperDiagram panel = new OverlapperDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				ventana.setLocation(0, config.getSizePanelCoordenadas().height);
				
				sesion.setBubbleGraph(ventana);
				
					panel.create();
					panel.run();
			
			}
			else if(e.getActionCommand().equals("Bicluster Bubble Graph")){
				Dimension dim = config.getDimPanelBiclusVis();
				//BiclusVisPanel panel = new BiclusVisPanel(sesion, dim, true);
				OverlapperDiagram panel = new OverlapperDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				sesion.setBubbleGraph(ventana);
				ventana.setLocation(0, config.getSizePanelCoordenadas().height);
				
				//panel.setPersonas(sesion.isPersonas());
				
				panel.create();
				panel.run();
			}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s13")))
				{
				/*if(!sesion.getMicroarrayData().isAnnotationsRetrieved())
					{
					JOptionPane.showMessageDialog(null,
							"Retrieving annotations from database, please wait to see word cloud ...", 
							"Wait for annotations", JOptionPane.INFORMATION_MESSAGE);
					return;
					}*/
				Dimension dim = config.getDimPanelWordCloud();
				WordCloudDiagram panel = new WordCloudDiagram(sesion, dim);
				DiagramWindow ventana = new DiagramWindow(sesion,sesion.getDesktop(),panel);
				panel.setWindow(ventana);
				panel.setLocation(config.getSizePanelCoordenadas().width, config.dimAplicacion.height-config.dimPanelWordCloud.height);
				sesion.setWordCloud(ventana);
				panel.repaint();
				}
		}
	}
}