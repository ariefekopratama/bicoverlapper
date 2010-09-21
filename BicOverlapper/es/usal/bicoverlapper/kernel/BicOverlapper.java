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
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.NCBIReader;
import es.usal.bicoverlapper.data.files.FileParser;
import es.usal.bicoverlapper.data.files.TRNParser;
import es.usal.bicoverlapper.utils.Translator;
import es.usal.bicoverlapper.visualization.diagrams.overlapper.Graph;



/**
 * Main class to run the application BicOverlapper. It just initializes an instance of BicOverlapperWindow
 * 
 * @author Rodrigo Santamaria (from a project of Javier Molpeceres)
 * 
*/
public class BicOverlapper {

	/**
	 * Default constructor
	 *
	 */
	public BicOverlapper() {		
		new BicOverlapperWindow();
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
		   
		   //  TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/chapintp.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/chapintp.gml");
		   // TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/hsp90intp.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/hsp90intp.gml");
		 //TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/stress/TFs/reg0000001s.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/stress/TFs/reg0000001s.gml");
		// TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/networks/TFs/Balaji2006/tnetSimple.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/networks/TFs/Balaji2006/tnetSimple.gml");
				new BicOverlapper();
	}
}