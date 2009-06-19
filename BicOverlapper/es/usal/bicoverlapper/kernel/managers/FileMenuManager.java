package es.usal.bicoverlapper.kernel.managers;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.plaf.basic.BasicProgressBarUI;




import es.usal.bicoverlapper.analysis.Biclustering;
import es.usal.bicoverlapper.data.BubbleData;
import es.usal.bicoverlapper.data.Field;
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.MicroarrayRequester;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.data.TRNData;
import es.usal.bicoverlapper.data.files.BiclusterResultsFilter;
import es.usal.bicoverlapper.data.files.DataReader;
import es.usal.bicoverlapper.data.files.MicroarrayFilter;
import es.usal.bicoverlapper.data.files.TRNFilter;
import es.usal.bicoverlapper.data.files.TextFileFilter;
import es.usal.bicoverlapper.kernel.BicOverlapperWindow;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.WorkDesktop;
import es.usal.bicoverlapper.kernel.configuration.ConfigurationHandler;
import es.usal.bicoverlapper.utils.HelpPanel;
import es.usal.bicoverlapper.utils.MicroarrayLoadProgressMonitor;
import es.usal.bicoverlapper.utils.Translator;

/**
 * Class that handles the File Menu Options
 * 
 * @author Javier Molpeceres and Rodrigo Santamaria
 * @version 3.2, 26/3/2007
 */
public class FileMenuManager implements ActionListener, MicroarrayRequester {
	
	private BicOverlapperWindow ventana;
	private BufferedReader pathReader;
	private BufferedWriter pathWriter;
	private TRNFilter trnFilter=new TRNFilter();
	private MicroarrayFilter microFilter=new MicroarrayFilter();
	
	private Session sesion;
	private String path;
	private boolean addVista;
	private File fichero;
	private JDesktopPane desktop;
	
	
	
	/**
	 * Constructor to build a MenuManager
	 * 
	 * @param window <code>BicOverlapperWindow</code> that will contain the menu that this manager controls
	 */
	public FileMenuManager(BicOverlapperWindow window) {
		this.ventana = window;
	}
	
	
	/**
	 * Method invoked each time that an option in the view menu is clicked
	 */
	public void actionPerformed(ActionEvent e) {
		
		boolean error=false;
		String defaultPath="";
		try{
			pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/path.txt"));
			defaultPath=pathReader.readLine();
			if(defaultPath==null)	defaultPath="";
			}catch(IOException ex){System.err.println("pathReader has no information"); defaultPath="";}
		
		if(e.getActionCommand().equals("Open TRN"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(trnFilter);
		selecFile.setCurrentDirectory(new File(defaultPath));
		//selecFile.setLocale(new Locale("en"));
		int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Open TRN");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) 
			{
			//Si no hay TRN pero hay microarray dejamos en la misma sesión
			File fichero = selecFile.getSelectedFile();
			
			path=selecFile.getCurrentDirectory().getPath();
			
			desktop = new JDesktopPane();
			desktop.setName(fichero.getName());
			boolean addVista=false;
			if(this.ventana.getActiveWorkDesktop()!=null)
				sesion = this.ventana.getActiveWorkDesktop().getSession();
			else
				{
				addVista=true;
				sesion = new Session(desktop, ventana);
				}
			
			try {
				sesion.reader.readTRN(path, fichero, sesion);
				//leerTRN(path, fichero, sesion);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(null,
                        "File not Found: "+fichero.getName(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			} catch (IOException e2) {
				JOptionPane.showMessageDialog(null,
                        "I/O Error"+e2.getMessage(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(null,
                        "Format Error"+e3.getMessage(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
			}
			
			if(!error)
				{
				// Actualizar las ventanas activas
				sesion.fileLoaded();
				sesion.updateData();
				
				try{
					pathWriter=new BufferedWriter(new FileWriter("es/usal/bicoverlapper/data/path.txt"));
					pathWriter.write(path);
					pathWriter.close();
					}catch(IOException ex){ex.printStackTrace();}
					
				ventana.menuViewTRN.setEnabled(true);
				if(addVista)	ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
				else			
					{
					JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
					ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
					p.setName(p.getName()+" | "+fichero.getName());
					}
				}
		}
	}
	else if(e.getActionCommand().equals("Open Microarray"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(microFilter);
		File f=new File(defaultPath);
		selecFile.setCurrentDirectory(f);
			int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Open Microarray");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) {
			selecFile.setVisible(false);
			fichero = selecFile.getSelectedFile();
		
			path=selecFile.getCurrentDirectory().getPath();
			desktop = new JDesktopPane();
			desktop.setName(fichero.getName());
			addVista=false;
			
			if(this.ventana.getActiveWorkDesktop()!=null)
				{
				addVista=false;
				sesion = this.ventana.getActiveWorkDesktop().getSession();
				}
			else
				{
				addVista=true;
				sesion = new Session(desktop, ventana);
				}
			
			ventana.analysisMenu.setEnabled(false);
			ventana.viewMenu.setEnabled(false);
			try {
				sesion.reader.readMicroarray(path, fichero, sesion, this);
				} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(null,
                        "File not found: "+fichero.getName(),
                        "Error",JOptionPane.ERROR_MESSAGE);
					error=true;
				} catch (IOException e2) 
				{
				JOptionPane.showMessageDialog(null,
                        "I/O Error: "+e2.getMessage(),
                        "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
				} catch (Exception e3) 
				{
				JOptionPane.showMessageDialog(null,
		                "Format Error: "+e3.getMessage(),
		                "Error",JOptionPane.ERROR_MESSAGE);
				error=true;
				}
		/*		System.out.println("Continuing");
			//---
			if(!error && sesion.getMicroarrayData()!=null)
				{
				// Actualizar las ventanas activas		
				//sesion.fileLoaded();
				sesion.updateData();
				
				try{
				pathWriter=new BufferedWriter(new FileWriter("es/usal/bicoverlapper/data/path.txt"));
				pathWriter.write(path);
				pathWriter.close();
				}catch(IOException ex){ex.printStackTrace();}
				
				ventana.analysisMenu.setEnabled(true);
				
				ventana.viewMenu.setEnabled(true);
				ventana.menuViewParallelCoordinates.setEnabled(true);
				ventana.menuViewHeatmap.setEnabled(true);
				
			//	ventana.menuViewCloud.setEnabled(true);
				
				ventana.menuArchivoExportSelection.setEnabled(true);
				
				if(addVista)	ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
				else			
					{
					JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
					ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
					p.setName(p.getName()+" | "+fichero.getName());
					}
				sesion.getMicroarrayData().biclustering= new Biclustering(sesion.getMicroarrayData());
				}
	*/		
		}
	}
	else if(e.getActionCommand().equals("Open Biclusters"))
		{
		JFileChooser selecFile = new JFileChooser();
		selecFile.addChoosableFileFilter(new BiclusterResultsFilter());
		selecFile.setCurrentDirectory(new File(defaultPath));
		
		int returnval = selecFile.showDialog(
				(Component)e.getSource(),"Open Biclustering Results");
		 
		if(returnval == JFileChooser.APPROVE_OPTION) {
			File fichero = selecFile.getSelectedFile();
			//Perfil p=ventana.getPerfilActivo();
		
			path=selecFile.getCurrentDirectory().getPath();
			desktop = new JDesktopPane();
			desktop.setName(fichero.getName());
			boolean addVista=false;
			if(this.ventana.getActiveWorkDesktop()!=null)
				sesion = this.ventana.getActiveWorkDesktop().getSession();
			else
				{
				addVista=true;
				sesion = new Session(desktop, ventana);
				}
	
			sesion.reader.readBiclusterResults(path, fichero.getName(), sesion);
			
			/*if(!error)
				{
				sesion.reader.biclusterResultsLoaded(sesion, ventana);
					
				try{
					pathWriter=new BufferedWriter(new FileWriter(sesion.reader.getPath("es/usal/bicoverlapper/data/path.txt")));
					pathWriter.write(path);
					pathWriter.close();
					}catch(IOException ex){ex.printStackTrace();}
					
				if(addVista)	ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
				else			
					{
					JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
					ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
					p.setName(p.getName()+" | "+fichero.getName());
					}
				}*/
			}
	}
		
	//------------------- EXPORT SELECTION ------------------
	else if(e.getActionCommand().equals("Export Selection"))
		{
		JFileChooser selecFile = new JFileChooser();
		//selecFile.addChoosableFileFilter(new BiclusterResultsFilter());
		selecFile.addChoosableFileFilter(new TextFileFilter());
		selecFile.setCurrentDirectory(new File(defaultPath));
		
		int returnval = selecFile.showSaveDialog((Component)e.getSource());
		 
		if(returnval == JFileChooser.APPROVE_OPTION) 
			{
			File fichero = selecFile.getSelectedFile();
			
			String path=selecFile.getCurrentDirectory().getPath();
			writeSelection(fichero.getName(), path);
			}
		}
	//------------------------- CARGAR CONFIGURACION -------------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s17"))
				&& ventana.isActiveWorkDesktop())
		{
			JFileChooser selecFile = new JFileChooser();
			
			int returnval = selecFile.showDialog((Component)e.getSource(),Translator.instance.menuLabels.getString("s21"));
			
			if(returnval == JFileChooser.APPROVE_OPTION){
				File fichero = selecFile.getSelectedFile();
				Session sesion = ventana.getActiveWorkDesktop().getSession();
				es.usal.bicoverlapper.kernel.configuration.ConfigurationLoader.loadConfiguration(sesion,fichero);
			}			
		}
		//-------------------- GUARDAR CONFIGURACIÓN -------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s18"))
				&& ventana.isActiveWorkDesktop())
		{
			Session sesion = ventana.getActiveWorkDesktop().getSession();
			ConfigurationHandler config = sesion.getConfig();
			JFileChooser selecFile = new JFileChooser();
			
			int returnval = selecFile.showDialog((Component)e.getSource(),Translator.instance.menuLabels.getString("s22"));
			
			if(returnval == JFileChooser.APPROVE_OPTION){
				File fichero = selecFile.getSelectedFile();
				es.usal.bicoverlapper.kernel.configuration.ConfigurationLoader.saveConfiguration(config, fichero);
			}			
		}	
		
		//----------------------- SALIR -------------------------------
		else if(e.getActionCommand().equals(Translator.instance.menuLabels.getString("s19")))
		{
			System.exit(0);
		}
	}
	
	public void receiveMatrix(int status)
	{
		System.out.println("Finished microarray data reading with status: "+status);
		//---
		if(status==0 && sesion.getMicroarrayData()!=null)
			{
			// Actualizar las ventanas activas		
			//sesion.fileLoaded();
			sesion.updateData();
			
			try{
			pathWriter=new BufferedWriter(new FileWriter("es/usal/bicoverlapper/data/path.txt"));
			pathWriter.write(path);
			pathWriter.close();
			}catch(IOException ex){ex.printStackTrace();}
			
			ventana.analysisMenu.setEnabled(true);
			
			ventana.viewMenu.setEnabled(true);
			ventana.menuViewParallelCoordinates.setEnabled(true);
			ventana.menuViewHeatmap.setEnabled(true);
			
			ventana.menuArchivoExportSelection.setEnabled(true);
			
			if(addVista)	ventana.addWorkDesktop(new WorkDesktop(desktop,sesion));
			else			
				{
				JDesktopPane p=ventana.getActiveWorkDesktop().getPanel();
				ventana.getDesktop().setTitleAt(0, p.getName()+" | "+fichero.getName());
				p.setName(p.getName()+" | "+fichero.getName());
				}
			sesion.getMicroarrayData().biclustering= new Biclustering(sesion.getMicroarrayData());
			}

	}

	private void writeSelection(String file, String path)
		{
		try{
		BufferedWriter bw;
		if(!file.contains(".")) bw=new BufferedWriter(new FileWriter(path+"\\"+file+".txt"));
		else					bw=new BufferedWriter(new FileWriter(path+"\\"+file));
		
		if(ventana.getActiveWorkDesktop()!=null && ventana.getActiveWorkDesktop().getSession().getSelectedBicluster()!=null)
			{
			BiclusterSelection bs=ventana.getActiveWorkDesktop().getSession().getSelectedBicluster();
			
			bw.write(bs.getGenes().size()+" "+bs.getConditions().size());
			bw.newLine();
			ArrayList<String> g=ventana.getActiveWorkDesktop().getSession().getMicroarrayData().getGeneNames(bs.getGenes());
			
			for(int i=0;i<bs.getGenes().size();i++)
				{
				
				bw.write(g.get(i)+" ");
				}
			bw.newLine();
			ArrayList<String> c=ventana.getActiveWorkDesktop().getSession().getMicroarrayData().getConditionNames(bs.getConditions());
			for(int i=0;i<c.size();i++)
				{
				bw.write(c.get(i)+" ");
				}
			bw.close();
			}
		else
			{
			JOptionPane.showMessageDialog(null,
					"No selection done, select some genes or conditions first",
                    Translator.instance.warningLabels.getString("s2"),JOptionPane.ERROR_MESSAGE);
			}
		}catch(Exception e){e.printStackTrace();}
		}
}