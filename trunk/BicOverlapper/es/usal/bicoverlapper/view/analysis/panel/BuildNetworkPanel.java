package es.usal.bicoverlapper.view.analysis.panel;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import es.usal.bicoverlapper.controller.analysis.Analysis;
import es.usal.bicoverlapper.controller.analysis.AnalysisProgressMonitor;
import es.usal.bicoverlapper.controller.analysis.AnalysisProgressMonitor.AnalysisTask;
import es.usal.bicoverlapper.controller.data.filter.GMLFilter;
import es.usal.bicoverlapper.controller.kernel.Session;


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
public class BuildNetworkPanel extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1359500023070611175L;
	private JLabel JLabel1;
	private JTextField sdThreshold;
	private JLabel jLabel3;
	private JButton selecFile;
	private JCheckBox write;
	private JButton build;
	private JTextField distanceThreshold;
	private JComboBox distanceMethod;
	private JLabel jLabel2;

	private Session session;
	private String defaultPath;
	private File resultsFile;


	/**
	* Auto-generated main method to display this JFrame
	*/
	
	public BuildNetworkPanel() {
		super();
		initGUI();
	}

	public BuildNetworkPanel(Session s) {
		super();
		initGUI();
		session=s;
	}
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);
			this.setPreferredSize(new java.awt.Dimension(383, 222));
			this.setSize(497, 490);
			
			{
				JLabel1 = new JLabel();
				getContentPane().add(JLabel1);
				JLabel1.setText("Filter genes with expression variation above");
				JLabel1.setBounds(14, 42, 345, 14);
			}
			{
				sdThreshold = new JTextField();
				getContentPane().add(sdThreshold);
				sdThreshold.setText("0.5");
				sdThreshold.setBounds(336, 39, 28, 21);
			}
			{
				jLabel2 = new JLabel();
				getContentPane().add(jLabel2);
				jLabel2.setText("Link genes with ");
				jLabel2.setBounds(14, 77, 105, 14);
			}
			{
				ComboBoxModel distanceMethodModel = 
					new DefaultComboBoxModel(
							new String[] { "euclidean", "maximum", "manhattan", "canberra", "binary", "minkowsky", "mutualinfo"
									 });
				distanceMethod = new JComboBox();
				getContentPane().add(distanceMethod);
				distanceMethod.setModel(distanceMethodModel);
				distanceMethod.setBounds(119, 70, 112, 21);
				distanceMethod.setSelectedIndex(0);
			}
			{
				jLabel3 = new JLabel();
				getContentPane().add(jLabel3);
				jLabel3.setText("distance below");
				jLabel3.setBounds(238, 77, 105, 14);
			}
			{
				distanceThreshold = new JTextField();
				getContentPane().add(distanceThreshold);
				distanceThreshold.setText("1.0");
				distanceThreshold.setBounds(336, 70, 28, 21);
			}
			{
				build = new JButton();
				getContentPane().add(build);
				build.setText("Build Correlation Network");
				build.setBounds(100, 153, 189, 21);
				build.addActionListener(new java.awt.event.ActionListener() {
					private AnalysisTask t;
					
					public void actionPerformed(java.awt.event.ActionEvent e) {
						
						String fileName="";
						if(resultsFile!=null)			
							{
							fileName=resultsFile.getPath().replace("\\", "/");
							if(!fileName.contains(".")) 	fileName=fileName.concat(".gml");
							}
						else 
							{
							if(write.isSelected())	fileName=defaultPath;
							}
						session=session.getMainWindow().getActiveWorkDesktop().getSession();
						
						 Analysis b=session.getAnalysis();
						 b.setFilterOptions(null);
						
						 System.out.println("Build network");
						 ArrayList<Object> p=new ArrayList<Object>();
						   p.add(new Double(sdThreshold.getText()).doubleValue());
						   p.add(distanceMethod.getSelectedItem());
						   p.add(new Double(distanceThreshold.getText()).doubleValue());
						   p.add(fileName);
						   
						AnalysisProgressMonitor apm=new AnalysisProgressMonitor(b, AnalysisProgressMonitor.AnalysisTask.CORRELATION_NETWORK, p);
						   apm.run();
						   t=apm.getTask();
						   Thread wt=new Thread() {
								public void run() {
									try{
										String fileName=t.get();
										if(fileName==null)	
											JOptionPane.showMessageDialog(null,
								                    "Network not generated",
								                    "Error",JOptionPane.ERROR_MESSAGE);
										
										else
											{
											File file=new File(fileName);
											String path=file.getAbsolutePath();
											session.getReader().readTRN(path, file, session, "gml");
												
											/*if(fileName.indexOf("/")>-1)
												session.getReader().readTRN(path, file, session, "gml");
											else
												{
												session.getReader().readTRN("", new File(fileName.substring(fileName.lastIndexOf("/")+1)), session, "gml");
												}*/
											}
										}catch(Exception e){e.printStackTrace();}
								}
							};
						wt.start();
						setVisible(false);
						
					 	}
				});
			}
			{
				write = new JCheckBox();
				getContentPane().add(write);
				write.setText("Write to File ...");
				write.setBounds(26, 116, 126, 21);
				write.setEnabled(true);
				write.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent e) {
						if(write.isSelected())	
							{
							try{
								BufferedReader pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/networkPath.txt"));
								defaultPath=pathReader.readLine();
								}catch(IOException ex){System.err.println("pathReader has no information"); defaultPath="";}
							write.setEnabled(true);
							selecFile.setEnabled(true);
							}
						else
							{
							defaultPath="";
							selecFile.setEnabled(false);
							}
					}
				});
			}
			{
				selecFile = new JButton();
				getContentPane().add(selecFile);
				selecFile.setText("Select");
				selecFile.setBounds(152, 116, 70, 21);
				selecFile.setEnabled(false);
				selecFile.addActionListener(new java.awt.event.ActionListener() {

					public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser selecFile = new JFileChooser();
					selecFile.addChoosableFileFilter(new GMLFilter());
					selecFile.setCurrentDirectory(new File(defaultPath));
					int returnval = selecFile.showSaveDialog((Component)e.getSource());
					
					if(returnval == JFileChooser.APPROVE_OPTION) {
						resultsFile = selecFile.getSelectedFile();
						}
					}
				});
			}
			pack();
			this.setSize(383, 222);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}

}
