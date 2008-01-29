package kernel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;

import files.FileParser;

import utils.Translator;

/**
 * Main class to run the application BicOverlapper. It just initializes an instance of BicOverlapperWindow
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * 
*/
public class BicOverlapper {

	/**
	 * Default constructror
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
			    
		new BicOverlapper();
		
	}
}