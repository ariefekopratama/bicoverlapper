package es.usal.bicoverlapper.kernel;

import java.util.Iterator;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;

/*import test.QuickSoapTest;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;
import uk.ac.ebi.ook.web.services.client.QueryServiceFactory;
*/
//import es.usal.bicoverlapper.data.EReader;
//import es.usal.bicoverlapper.data.AffyReader;
import es.usal.bicoverlapper.data.EReader;
import es.usal.bicoverlapper.data.files.FileParser;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.overlapper.Graph;
//import gov.nih.nlm.ncbi.www.soap.eutils.esearch.IdListType;



/**
 * Main class to run the application BicOverlapper. It just initializes an instance of BicOverlapperWindow
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * 
*/
public class MusicOverlapper {

	/**
	 * Default constructor
	 *
	 */
	public MusicOverlapper() {		
		new MusicOverlapperWindow();
	}
	
	/**
	 * Main method
	 * 
	 * @param args Arguments taken from command line (no arguments are considered by overlapper)
	 */
	public static void main(String[] args) {
		
		try {
		      UIManager.setLookAndFeel(new SubstanceLookAndFeel());
		    } catch (UnsupportedLookAndFeelException ulafe) {
		      System.out.println("Substance failed to set");
		    }
		   
		    
		   Translator.instance=new Translator("en");
		   
		new MusicOverlapper();
	}
}