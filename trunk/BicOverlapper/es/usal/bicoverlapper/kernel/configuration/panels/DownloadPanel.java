package es.usal.bicoverlapper.kernel.configuration.panels;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import es.usal.bicoverlapper.data.files.TextFileFilter;
import es.usal.bicoverlapper.kernel.managers.FileMenuManager;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class DownloadPanel extends javax.swing.JFrame implements ActionListener, KeyListener{
	private JLabel name;
	private JTextField jTextField1;
	private JButton selectFile;
	private JLabel Instructions;
	private JButton DownloadButton;
	private JLabel filePath;
	private String path;
	private FileMenuManager fmm;
	private JLabel jLabel1;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new DownloadPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public DownloadPanel() {
		super();
		initGUI();
	}
	
	public DownloadPanel(FileMenuManager fmm1) {
		super();
		fmm=fmm1;
		initGUI();
	}
	
	private void initGUI() {
		try {
			FormLayout thisLayout = new FormLayout(
					"max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;5dlu)", 
					"max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;15dlu), max(p;15dlu), max(p;15dlu), max(p;15dlu), max(p;15dlu), max(p;15dlu)");
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 150));
			setSize(new Dimension(400, 150));
			{
				name = new JLabel();
				this.add(name, new CellConstraints("1, 1, 1, 1, default, default"));
				name.setText("Accession name of the experiment (e.g. E-MEXP-328)   ");
			}
			{
				jTextField1 = new JTextField();
				this.add(jTextField1, new CellConstraints("2, 1, 1, 1, default, default"));
				jTextField1.setActionCommand("accessionName");
				jTextField1.addKeyListener(this);
			}
			{
				filePath = new JLabel();
				this.add(filePath, new CellConstraints("1, 2, 1, 1, default, default"));
				filePath.setText("Select a path to store the processed experiment");
			}
			{
				selectFile = new JButton();
				this.add(selectFile, new CellConstraints("2, 2, 1, 1, default, default"));
				selectFile.setActionCommand("select");
				selectFile.addActionListener(this);
				selectFile.setText("select");
			}
			{
				DownloadButton = new JButton();
				this.add(DownloadButton, new CellConstraints("2, 6, 1, 1, default, default"));
				DownloadButton.setText("download");
				DownloadButton.addActionListener(this);
				
			}
			{
				Instructions = new JLabel();
				this.add(Instructions, new CellConstraints("1, 6, 1, 1, default, default"));
				Instructions.setText("Download + RMA preprocessing");
			}
			{
				jLabel1 = new JLabel();
				getContentPane().add(jLabel1, new CellConstraints("1, 3, 1, 1, default, default"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
	    if("select".equals(e.getActionCommand())) 
	    	{
			JFileChooser selecFile = new JFileChooser();
			selecFile.addChoosableFileFilter(new TextFileFilter());
			try{
				BufferedReader pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/path.txt"));
				selecFile.setCurrentDirectory(new File(pathReader.readLine()));
				}catch(Exception ex){ex.printStackTrace();}
			
			
			int returnval = selecFile.showSaveDialog((Component)e.getSource());
			 
			if(returnval == JFileChooser.APPROVE_OPTION) 
				{
				File fichero = selecFile.getSelectedFile();
				path=selecFile.getSelectedFile().getAbsolutePath();
				if(!path.endsWith(".txt"))	path=path+".txt";
				jLabel1.setText(path);
				}
	    	}
	    else if("download".equals(e.getActionCommand()))
			{
	    	this.setVisible(false);
			fmm.downloadExperiment(jTextField1.getText(), path);	
			}
	  		}

	@Override
	public void keyPressed(KeyEvent arg0) {
		  
			   
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if(jTextField1.equals(arg0.getSource()))
	    	{
	    	try{
	    	BufferedReader pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/path.txt"));
			String defaultPath=pathReader.readLine();
			defaultPath=defaultPath.replace("\\", "/");
			defaultPath=defaultPath.substring(0, defaultPath.lastIndexOf("/"))+"/";
			
			path=defaultPath+jTextField1.getText()+".txt";
			int maxLength=50;
			String labelPath=path;
			if(labelPath.length()>maxLength)	labelPath=path.substring(0,10)+"..."+path.substring(path.length()-(maxLength-13), path.length());
	    	jLabel1.setText(labelPath);
	    	}catch(Exception ex){ex.printStackTrace();}
			}	
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	} 
	}