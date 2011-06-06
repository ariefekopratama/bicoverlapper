package es.usal.bicoverlapper.view.main;

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
import es.usal.bicoverlapper.controller.data.parser.FileParser;
import es.usal.bicoverlapper.controller.data.parser.TRNParser;
import es.usal.bicoverlapper.controller.data.reader.NCBIReader;
import es.usal.bicoverlapper.controller.util.Translator;
import es.usal.bicoverlapper.model.microarray.MicroarrayData;
import es.usal.bicoverlapper.view.analysis.panel.TrickPanel;
import es.usal.bicoverlapper.view.diagram.overlapper.Graph;



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
		   
		   System.out.println(System.getProperty("user.language"));
		   Translator.instance=new Translator("en");
		   Runtime runtime = Runtime.getRuntime();  
		   System.out.println("max memory: " + runtime.maxMemory() / 1024);  
		   //  TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/chapintp.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/chapintp.gml");
		   // TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/hsp90intp.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/processed/hsp90intp.gml");
		 //TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/stress/TFs/reg0000001s.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/stress/TFs/reg0000001s.gml");
		// TRNParser.list2GML("/Users/rodri/Documents/workspace/sybaris/data/manuel/networks/TFs/Balaji2006/tnetSimple.txt", "/Users/rodri/Documents/workspace/sybaris/data/manuel/networks/TFs/Balaji2006/tnetSimple.gml");
				new BicOverlapper();
				
	}
}