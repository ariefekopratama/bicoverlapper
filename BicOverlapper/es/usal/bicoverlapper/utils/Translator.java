package es.usal.bicoverlapper.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class manage Resource Bundles with translated names of labels that appear in 
 * the application (menu names, window titles, etc.)
 * STILL IN DEVELOPMENT: by now, only English texts exist.
 * @author Rodrigo Santamaria
 *
 */
public class Translator {
	
	/**
	 * English instance of the translator
	 */
	public static Translator instance =new Translator("en");
	
	Locale currentLocale;
	
	//public ResourceBundle scatterLabels;
	//public ResourceBundle tableLabels;
	/**
	 * Labels for menu options
	 */
	public ResourceBundle menuLabels;
	/**
	 * Labels for configurations
	 */
	public ResourceBundle configureLabels;
	/**
	 * Labels for warnings
	 */
	public ResourceBundle warningLabels;
	
	/**
	 * Constructor of the translator for a determined language
	 * STILL IN DEVELOPMENT: by now, only "en" labels exist
	 * @param language
	 */
	public Translator(String language)
		{
		Locale currentLocale=new Locale(language);
		System.out.println(System.getProperty("os.name"));
		//URL imgURL=Thread.currentThread().getContextClassLoader().getResource("translation/LabelsBundle");
		
		
		menuLabels=ResourceBundle.getBundle("translation/LabelsBundle", currentLocale);
		configureLabels=ResourceBundle.getBundle("translation/LabelsConfiguration", currentLocale);
		warningLabels=ResourceBundle.getBundle("translation/LabelsWarnings", currentLocale);
		/*
		if(System.getProperty("os.name").contains("indows"))	
			{
			menuLabels=ResourceBundle.getBundle("translation\\LabelsBundle", currentLocale);
			//scatterLabels=ResourceBundle.getBundle("translation\\LabelsScatter", currentLocale);
			configureLabels=ResourceBundle.getBundle("translation\\LabelsConfiguration", currentLocale);
			warningLabels=ResourceBundle.getBundle("translation\\LabelsWarnings", currentLocale);
			//tableLabels=ResourceBundle.getBundle("translation\\LabelsTable", currentLocale);
			}
		else
			{
			menuLabels=ResourceBundle.getBundle("translation/LabelsBundle", currentLocale);
			//scatterLabels=ResourceBundle.getBundle("translation/LabelsScatter", currentLocale);
			configureLabels=ResourceBundle.getBundle("translation/LabelsConfiguration", currentLocale);
			warningLabels=ResourceBundle.getBundle("translation/LabelsWarnings", currentLocale);
			//tableLabels=ResourceBundle.getBundle("translation/LabelsTable", currentLocale);
			}
			*/
		}
}
