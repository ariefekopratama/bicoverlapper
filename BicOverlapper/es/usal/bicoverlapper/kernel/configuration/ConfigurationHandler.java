package es.usal.bicoverlapper.kernel.configuration;

import java.util.Vector;

/**
 * Class to store the {@link ConfigurationWindow}s of the {@link es.usal.bicoverlapper.kernel.DiagramWindow}s in the desktop,
 * so it can be saved and loaded
 * TO BE REPLACED BY AN ArrayList<WindowConfiguration>
 * @author Javier Molpeceres and Rodrigo Santamaria
 */

public class ConfigurationHandler {
	
	private Vector<ConfigurationWindow> configVentanas;
	
	/**
	 * Default constructor with no configuration information
	 *
	 */
	public ConfigurationHandler(){
		configVentanas = new Vector<ConfigurationWindow>(0,1);
	}
	
	/**
	 * Gets the number of WindowConfigurations stored
	 * 
	 * @return the number of WindowConfigurations
	 */
	public int getSizeConfig(){
		return this.configVentanas.size();
	}
	
	/**
	 * Returns the i-WindowConfiguration
	 * 
	 * @param i index of the WindowConfiguration required
	 * @return the i-WindowConfiguration
	 */
	public ConfigurationWindow getWindowConfiguration(int i){
		return (ConfigurationWindow)this.configVentanas.get(i);
	}
	
	/**
	 * Adds as WindowConfiguration
	 * 
	 * @param config the WindowConfiguration to be added
	 */
	public void addWindowConfiguration(ConfigurationWindow config){
		configVentanas.add(config);		
	}
}