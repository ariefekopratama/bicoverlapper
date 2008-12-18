package es.usal.bicoverlapper.kernel.managers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import es.usal.bicoverlapper.kernel.MusicOverlapperWindow;
import es.usal.bicoverlapper.utils.HelpPanel;
import es.usal.bicoverlapper.utils.LicensePanel;
import es.usal.bicoverlapper.utils.Translator;


/**
 * This class manages Help Menu selections
 * @author Rodrigo Santamaria
 *
 */
public class HelpMenuManager implements ActionListener{
	
	private MusicOverlapperWindow ventana;
	
	/**
	 * Constructor
	 * 
	 * @param window <code>BicOverlapperWindow</code> that contains the help menu
	 */
	public HelpMenuManager(MusicOverlapperWindow window) {
		this.ventana = window;
	}
	
	/**
	 * Method invoked each time that an option in the help menu is clicked
	 */
	public void actionPerformed(ActionEvent e) {
		
			//System.out.println(e.getActionCommand());
			
		if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s24")))
			{
			LicensePanel lp = new LicensePanel();
			}
		if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s25")))
			{
			//System.out.println("Aquí estamos");
			HelpPanel hp = new HelpPanel();
			//Sesion sesion = ventana.getVistaActiva().getSesion();
			// JOptionPane.showMessageDialog(ventana,"A complete usage guide can be found at http://vis.usal.es/bicoverlapper/userGuide.htm", "Contents", JOptionPane.INFORMATION_MESSAGE);
				}
		}
}
