package es.usal.bicoverlapper.kernel.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import es.usal.bicoverlapper.kernel.BicOverlapperWindow;
import es.usal.bicoverlapper.utils.Translator;


/**
 * This class manages Help Menu selections
 * @author Rodrigo Santamaria
 *
 */
public class HelpMenuManager implements ActionListener{
	
	
	/**
	 * Constructor
	 * 
	 * @param window <code>BicOverlapperWindow</code> that contains the help menu
	 */
	public HelpMenuManager() {
	}
	
	/**
	 * Method invoked each time that an option in the help menu is clicked
	 */
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s24")))
			{
			try{
				java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://vis.usal.es/bicoverlapper/contact.htm"));
			}catch(Exception ex){ex.printStackTrace();}
			}
		if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s25")))
			{
			try{
				java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://vis.usal.es/bicoverlapper/documents/userGuide.pdf"));
			}catch(Exception ex){ex.printStackTrace();}
			
			}
		}
}
