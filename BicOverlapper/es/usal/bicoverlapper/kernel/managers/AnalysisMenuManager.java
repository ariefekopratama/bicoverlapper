package es.usal.bicoverlapper.kernel.managers;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import es.usal.bicoverlapper.kernel.BicOverlapperWindow;
import es.usal.bicoverlapper.kernel.Configuration;
import es.usal.bicoverlapper.kernel.DiagramWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.managers.biclustering.BimaxPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.CCPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.ISAPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.PlaidPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.SearchPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.SelectPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.ShowPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.SortPanel;
import es.usal.bicoverlapper.kernel.managers.biclustering.XMotifsPanel;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.OverlapperDiagram;
import es.usal.bicoverlapper.visualization.diagrams.BubblesDiagram;
import es.usal.bicoverlapper.visualization.diagrams.HeatmapDiagram;
import es.usal.bicoverlapper.visualization.diagrams.NetworkDiagram;
import es.usal.bicoverlapper.visualization.diagrams.WordCloudDiagram;





/**
 * Class that handles Analysis options (Biclustering)
 * 
 * @author Rodrigo Santamaria
 * @version 3.2, 26/3/2007
 */
public class AnalysisMenuManager implements ActionListener{

	private BicOverlapperWindow ventana;
	private Configuration config;
	private BimaxPanel bimaxPanel=null;
	private PlaidPanel plaidPanel=null;
	private XMotifsPanel xmotifsPanel=null;
	private CCPanel ccPanel=null;
	private ISAPanel isaPanel=null;
	private SearchPanel searchPanel=null;
	private ShowPanel showPanel=null;
	private SortPanel sortPanel=null;
	private SelectPanel selectPanel=null;
	
	/**
	 * Constructor to build a MenuManager
	 * 
	 * @param window <code>BicOverlapperWindow</code> that will contain the menu that this manager controls
	 */
	public AnalysisMenuManager(BicOverlapperWindow window) {
		this.ventana = window;
	}
	/**
	 * Constructor to build a MenuManager
	 * 
	 * @param window <code>BicOverlapperWindow</code> that will contain the menu that this manager controls
	 * @param config <code>Configuration</code> with initial configuration for the views.
	 */
	public AnalysisMenuManager(BicOverlapperWindow window, Configuration config){
		this.ventana = window;
		this.config = config;
	}
	
	/**
	 * Method invoked each time that an option in the view menu is clicked
	 */
	public void actionPerformed(ActionEvent e) {
		
		if(ventana.isActiveWorkDesktop())
			{
			Session sesion = ventana.getActiveWorkDesktop().getSession();
			
			//------------------------------- SHOW BICLUSTERING WINDOWS---------------------
			if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("bimax")))
				{
				if(bimaxPanel==null)			
					bimaxPanel=new BimaxPanel(sesion);
				
				// Mostramos la ventana de configuracion
				JFrame window = new JFrame();
				window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				window.setTitle("Bimax biclustering");
				JComponent newContentPane = bimaxPanel.getJPanel2();
				newContentPane.setOpaque(true); //content panes must be opaque
				window.setContentPane(newContentPane);
				window.setAlwaysOnTop(true);
				//Display the window.
				window.pack();
				window.setSize(new Dimension(241, 435));
				window.setLocation(200,200);
				window.setVisible(true);
				}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("plaid")))
				{
				if(plaidPanel==null)			
					plaidPanel=new PlaidPanel(sesion);
				
				// Mostramos la ventana de configuracion
				// Mostramos la ventana de configuracion
				JFrame window = new JFrame();
				window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				window.setTitle("Plaid Model biclustering");
				JComponent newContentPane = plaidPanel.getJPanel2();
				newContentPane.setOpaque(true); //content panes must be opaque
				window.setContentPane(newContentPane);
				window.setAlwaysOnTop(true);
				//Display the window.
				window.pack();
				window.setSize(new Dimension(241, 411));
				window.setLocation(200,200);
				window.setVisible(true);
				}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("opsm")))
				{
					
				}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("isa")))
				{
				if(isaPanel==null)			
					isaPanel=new ISAPanel(sesion);
				
				// Mostramos la ventana de configuracion
				// Mostramos la ventana de configuracion
				JFrame window = new JFrame();
				isaPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				isaPanel.setTitle("ISA biclustering");
				isaPanel.setAlwaysOnTop(true);
				//Display the window.
				isaPanel.pack();
				//window.setSize(new Dimension(241, 355));
				isaPanel.setLocation(200,200);
				isaPanel.setVisible(true);	
				}
			
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("spectral")))
				{
					
				}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("cc")))
				{
				if(ccPanel==null)			
					ccPanel=new CCPanel(sesion);
				
				// Mostramos la ventana de configuracion
				// Mostramos la ventana de configuracion
				JFrame window = new JFrame();
				window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				window.setTitle("Cheng&Church biclustering");
				JComponent newContentPane = ccPanel.getJPanel2();
				newContentPane.setOpaque(true); //content panes must be opaque
				window.setContentPane(newContentPane);
				window.setAlwaysOnTop(true);
				//Display the window.
				window.pack();
				window.setSize(new Dimension(241, 355));
				window.setLocation(200,200);
				window.setVisible(true);
				}

			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("xmotifs")))
				{
				if(xmotifsPanel==null)			
					xmotifsPanel=new XMotifsPanel(sesion);
				
				// Mostramos la ventana de configuracion
				// Mostramos la ventana de configuracion
				JFrame window = new JFrame();
				window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				window.setTitle("XMotifs biclustering");
				JComponent newContentPane = xmotifsPanel.getJPanel2();
				newContentPane.setOpaque(true); //content panes must be opaque
				window.setContentPane(newContentPane);
				window.setAlwaysOnTop(true);
				//Display the window.
				window.pack();
				//window.setBounds(window.getContentPane().getBounds());
				window.setSize(new Dimension(241, 443));
				window.setLocation(200,200);
				window.setVisible(true);
				}
			//
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("search")))
				{
				//Search & selection box
				if(searchPanel==null)			
					searchPanel=new SearchPanel(sesion);
				JFrame window = new JFrame();
				window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				window.setTitle("Search");
				JComponent newContentPane = searchPanel.getJPanel2();
				newContentPane.setOpaque(true); //content panes must be opaque
				window.setContentPane(newContentPane);
				window.setAlwaysOnTop(true);
				//Display the window.
				window.pack();
				//window.setBounds(window.getContentPane().getBounds());
				window.setSize(new Dimension(241, 150));
				window.setLocation(200,200);
				window.setVisible(true);
				}
			
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("show")))
				{
				//Show label names box
				if(showPanel==null)		showPanel=new ShowPanel(sesion);
				else					showPanel.updateLists();
				showPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				showPanel.setTitle("Show");
				showPanel.setAlwaysOnTop(true);
				
				//Display the window.
				showPanel.pack();
				showPanel.setVisible(true);
				}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("sort")))
				{
				//Show label names box
				if(sortPanel==null)		sortPanel=new SortPanel(sesion);
				else					sortPanel.updateLists();
				sortPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				sortPanel.setTitle("Sort Samples");
				sortPanel.setAlwaysOnTop(true);
				
				//Display the window.
				sortPanel.pack();
				sortPanel.setVisible(true);
				}
			else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("select")))
				{
				//Show label names box
				if(selectPanel==null)		selectPanel=new SelectPanel(sesion);
				//else					selectPanel.updateLists();
				selectPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				selectPanel.setTitle("Select Profiles");
				selectPanel.setAlwaysOnTop(true);
				
				//Display the window.
				selectPanel.pack();
				selectPanel.setVisible(true);
				}
			}
		}
	

	
	
}