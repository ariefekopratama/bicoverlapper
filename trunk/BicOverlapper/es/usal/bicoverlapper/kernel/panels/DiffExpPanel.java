package es.usal.bicoverlapper.kernel.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import es.usal.bicoverlapper.analysis.Analysis;
import es.usal.bicoverlapper.analysis.AnalysisProgressMonitor;
import es.usal.bicoverlapper.analysis.AnalysisProgressMonitor.AnalysisTask;
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.files.BiclusterResultsFilter;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.utils.RUtils;

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
public class DiffExpPanel extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton OK;
	private JTextField pvalueValue;
	private JLabel jLabel2;
	private JLabel jLabel1;
	private JList group2;
	private JList group1;
	private JComboBox regulation;
	private JTextField expressionValue;
	private JLabel differentialExpression;
	private JCheckBox correction;
	private JLabel pvalue;
	private Session session;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JCheckBox addDescription;
	private JButton select;
	private JCheckBox writeToFile;
	private JTextField description;
	private DefaultComboBoxModel group1Model;
	private DefaultComboBoxModel group2Model;

	private String defaultPath;
	private File resultsFile;

	
	public DiffExpPanel() {
		super();
		initGUI();
	}
	
	public DiffExpPanel(Session s) {
		super();
		session=s;
		initGUI();
	}
	
	private void initGUI() {
		try {
			{
				this.setSize(286, 225);
			}
			this.setPreferredSize(new java.awt.Dimension(497, 490));
			this.setLayout(null);
			this.setSize(497, 490);
			//this.setToolTipText("Limma differential expression analysis between groups 1 and 2.\r\nThe genes above the thresholds are selected.");
			{
				OK = new JButton();
				this.add(OK);
				OK.setText("Differential Expression Analysis");
				OK.setBounds(120, 425, 230, 21);
				OK.addActionListener(new java.awt.event.ActionListener() {
					private AnalysisTask t;
					/* Possible combinations:
					 * 1) rest vs rest - diffexp among every combination of efvs for every ef
					 * 2) efv1 vs efv2 (different efs) - error
					 * 3) efv1 vs efv2 (same ef) - diff exp between both groups of samples. If one of the groups has only 1 sample, error
					 * 4) efv vs rest - diff exp between the efv samples and the rest of samples
					 * 4b) ef vs rest - diff exp between every efv on the ef and the rest of the samples 
					 * 5) efv vs ef (other ef) - error
					 * 6) efv vs ef (its ef) - diff exp between efv and each of the groups for the others efvs on that ef. All the efvs must have 2+ samples
					 * 7) ef vs ef (same ef) - diff exp among every combination of efvs for the given ef
					 * 8) ef vs ef (different efs) - error
					 * (non-Javadoc)
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					public void actionPerformed(java.awt.event.ActionEvent e) {
						String ef1=null;
						String efv1=null;
						boolean ne1=false;
						String g1=group1.getSelectedValue().toString();
						String g2=group2.getSelectedValue().toString();
						
						if(!g1.startsWith(" ") && !g2.startsWith(" "))//both are efs
							{
							if(!g1.equals(g2))//8)
								{
								JOptionPane.showMessageDialog(null,
						                "Experimental factors (efs) cannot be compared, choose ef values or the same ef",
						                "Error",JOptionPane.ERROR_MESSAGE);
								return;
								}
							}
						if(g1.equals("rest"))	{ef1=efv1="rest";ne1=true;}//4), 4b)
						else
							{
							efv1=g1.toString().trim();
							for(int i=group1.getSelectedIndex();i>=0;i--)//search for its ef
								{
								if(!group1Model.getElementAt(i).toString().startsWith(" "))	
								 {ef1=group1Model.getElementAt(i).toString(); break;}
								}
							}
						String ef2=null;
						String efv2=null;
						boolean ne2=false;
						if(g2.equals("rest"))	{ef2="rest"; efv2="rest"; ne2=true;}
						else
							{
							efv2=g2.toString().trim();
							for(int i=group2.getSelectedIndex();i>=0;i--)
								{
								if(!group2Model.getElementAt(i).toString().startsWith(" "))	
								 {ef2=group2Model.getElementAt(i).toString(); break;}
								}
							}
						
						if(!ef1.equals("rest") && !ef2.equals("rest") && ef1!=null && ef2!=null && !ef1.equals(ef2))	//2)
							{
							JOptionPane.showMessageDialog(null,
					                "Experimental factors values must come from the same experimental factor",
					                "Error",JOptionPane.ERROR_MESSAGE);
							return;
							}
						
						String reg="";
						switch(regulation.getSelectedIndex())
							{
							case 0: reg="all"; break;
							case 1: reg="up"; break;
							case 2: reg="down"; break;
							default: reg="all"; break;	
							}
						
						String fileName="";
						if(resultsFile!=null)			
							{
							fileName=resultsFile.getPath().replace("\\", "/");
							if(!fileName.contains(".")) 	fileName=fileName.concat(".bic");
							}
						else 
							{
							if(writeToFile.isSelected())	fileName=defaultPath;
							}
						 Analysis b=session.analysis;
						 b.setFilterOptions(null);
						
						//EFV vs rest case
						 /*if( (ef1.equals("rest") && !ef2.equals("rest") && !ef2.equals(efv2)) || (!ef1.equals("rest") && ef2.equals("rest") && !ef1.equals(efv1)) )
						 	{
							System.out.println("EFV vs rest case");
							ArrayList<Object> p=new ArrayList<Object>();
							if(!ef1.equals("rest"))
								{
								p.add(session.getMicroarrayData().getConditions(ef1, efv1, ne1));
							    p.add(session.getMicroarrayData().getConditions(ef2, efv2, ne2));
								}
							else
								{
								p.add(session.getMicroarrayData().getConditions(ef1, efv1, ne1));
							    p.add(session.getMicroarrayData().getConditions(ef2, efv2, ne2));
								}
						   p.add(correction.isSelected());
						   p.add(new Double(pvalueValue.getText()).doubleValue());
						   p.add(new Double(expressionValue.getText()).doubleValue());
						   p.add(reg);
						   p.add(fileName);
						   p.add(description.getText());
						
							AnalysisProgressMonitor apm=new AnalysisProgressMonitor(b, AnalysisProgressMonitor.AnalysisTask.LIMMA, p);
							   apm.run();
							   t=apm.getTask();
							   Thread wt=new Thread() {
									public void run() {
										try{
											String fileName=t.get();
											if(fileName==null)	
												JOptionPane.showMessageDialog(null,
									                    "No biclusters found",
									                    "Error",JOptionPane.ERROR_MESSAGE);
											
											else
												{
												if(fileName.indexOf("/")>-1)
													session.reader.readBiclusterResults(fileName.substring(0, fileName.lastIndexOf("/")),fileName.substring(fileName.lastIndexOf("/")+1), fileName, session);
												else
													session.reader.readBiclusterResults("",fileName, fileName, session);
												}
											}catch(Exception e){e.printStackTrace();}
									}
								};
							wt.start();
							setVisible(false);
			
								 
						 	}
						//----------------------- EF case
						else */if(!ef1.equals("rest") && ef1.equals(ef2) && efv1.equals(efv2))
						 	{
							 //7) Same ef, do every possible combination
							 System.out.println("Same EF case");
							 ArrayList<Object> p=new ArrayList<Object>();
							   p.add(ef1);
							   p.add(correction.isSelected());
							   p.add(new Double(pvalueValue.getText()).doubleValue());
							   p.add(new Double(expressionValue.getText()).doubleValue());
							   p.add(reg);
							   p.add(fileName);
							   p.add(description.getText());
							
							AnalysisProgressMonitor apm=new AnalysisProgressMonitor(b, AnalysisProgressMonitor.AnalysisTask.LIMMAEFALL, p);
							   apm.run();
							   t=apm.getTask();
							   Thread wt=new Thread() {
									public void run() {
										try{
											String fileName=t.get();
											if(fileName==null)	
												JOptionPane.showMessageDialog(null,
									                    "No biclusters found",
									                    "Error",JOptionPane.ERROR_MESSAGE);
											
											else
												{
												if(fileName.indexOf("/")>-1)
													session.reader.readBiclusterResults(fileName.substring(0, fileName.lastIndexOf("/")),fileName.substring(fileName.lastIndexOf("/")+1), fileName, session);
												else
													session.reader.readBiclusterResults("",fileName, fileName, session);
												}
											}catch(Exception e){e.printStackTrace();}
									}
								};
							wt.start();
							setVisible(false);
							
						 	}
						 else if(ef1.equals("rest") && ef2.equals(ef1))
						 	{// case 1, rest vs rest, perform diffexp between every combination of efvs for each ef
							 System.out.println("rest vs rest case");
							 ArrayList<Object> p=new ArrayList<Object>();
							   p.add(correction.isSelected());
							   p.add(new Double(pvalueValue.getText()).doubleValue());
							   p.add(new Double(expressionValue.getText()).doubleValue());
							   p.add(reg);
							   p.add(fileName);
							   p.add(description.getText());
							
							AnalysisProgressMonitor apm=new AnalysisProgressMonitor(b, AnalysisProgressMonitor.AnalysisTask.LIMMAALL, p);
							   apm.run();
							   t=apm.getTask();
							   Thread wt=new Thread() {
									public void run() {
										try{
											String fileName=t.get();
											if(fileName==null)	
												JOptionPane.showMessageDialog(null,
									                    "No biclusters found",
									                    "Error",JOptionPane.ERROR_MESSAGE);
											
											else
												{
												if(fileName.indexOf("/")>-1)
													session.reader.readBiclusterResults(fileName.substring(0, fileName.lastIndexOf("/")),fileName.substring(fileName.lastIndexOf("/")+1), fileName, session);
												else
													session.reader.readBiclusterResults("",fileName, fileName, session);
												}
											}catch(Exception e){e.printStackTrace();}
									}
								};
							wt.start();
							setVisible(false);
						 	}
						 else if(!efv1.equals("rest") && !efv2.equals("rest") && (efv1.equals(ef1) || efv2.equals(ef2)) && ef1.equals(ef2))//same ef, not rest
							{ 	//TODO: 4b, 5, 6
							System.out.println("EF vs EFV case");
							String ef=null, efv=null;
							if(efv1.equals(ef1)){ef=ef1; efv=efv2;}
							else if(efv2.equals(ef2)){ef=ef2; efv=efv1;}
							
							ArrayList<Object> p=new ArrayList<Object>();
							   p.add(ef);
							   p.add(efv);
							   p.add(correction.isSelected());
							   p.add(new Double(pvalueValue.getText()).doubleValue());
							   p.add(new Double(expressionValue.getText()).doubleValue());
							   p.add(reg);
							   p.add(fileName);
							   p.add(description.getText());
							
							AnalysisProgressMonitor apm=new AnalysisProgressMonitor(b, AnalysisProgressMonitor.AnalysisTask.LIMMAEF, p);
							   apm.run();
							   t=apm.getTask();
							   Thread wt=new Thread() {
									public void run() {
										try{
											String fileName=t.get();
											if(fileName==null)	
												JOptionPane.showMessageDialog(null,
									                    "No biclusters found",
									                    "Error",JOptionPane.ERROR_MESSAGE);
											
											else
												{
												if(fileName.indexOf("/")>-1)
													session.reader.readBiclusterResults(fileName.substring(0, fileName.lastIndexOf("/")),fileName.substring(fileName.lastIndexOf("/")+1), fileName, session);
												else
													session.reader.readBiclusterResults("",fileName, fileName, session);
												}
											}catch(Exception e){e.printStackTrace();}
									}
								};
							wt.start();
							setVisible(false);
							}
						else
							{
							//3,4) ---------------- EFVs case
							System.out.println("EFV vs EFV case");
							String nameG1=efv1;
							String nameG2=efv2;
							if(ef2.equals("rest"))	{efv2=efv1;ef2=ef1;}//4)
							if(ef1.equals("rest"))	{efv1=efv2;ef1=ef2;}//4)
							
							ArrayList<Object> p=new ArrayList<Object>();
							   p.add(session.getMicroarrayData().getConditions(ef1, efv1, ne1));
							   p.add(session.getMicroarrayData().getConditions(ef2, efv2, ne2));
							   p.add(nameG1);
							   p.add(nameG2);
							   p.add(correction.isSelected());
							   p.add(new Double(pvalueValue.getText()).doubleValue());
							   p.add(new Double(expressionValue.getText()).doubleValue());
							   p.add(reg);
							   p.add(fileName);
							   p.add(description.getText());
							
							AnalysisProgressMonitor apm=new AnalysisProgressMonitor(b, AnalysisProgressMonitor.AnalysisTask.LIMMA, p);
							   apm.run();
							   t=apm.getTask();
							   Thread wt=new Thread() {
									public void run() {
										try{
											String fileName=t.get();
											if(fileName==null)	
												JOptionPane.showMessageDialog(null,
									                    "No biclusters found",
									                    "Error",JOptionPane.ERROR_MESSAGE);
											
											else
												{
												if(fileName.indexOf("/")>-1)
													session.reader.readBiclusterResults(fileName.substring(0, fileName.lastIndexOf("/")),fileName.substring(fileName.lastIndexOf("/")+1), fileName, session);
												else
													session.reader.readBiclusterResults("",fileName, fileName, session);
												}
											}catch(Exception e){e.printStackTrace();}
									}
								};
							wt.start();
							setVisible(false);
							}
					}
				});
			}
			{
				pvalue = new JLabel();
				this.add(pvalue);
				pvalue.setText("p-value threshold (-log10)      <");
				pvalue.setBounds(17, 12, 208, 14);
				pvalue.setToolTipText("-log10 scale means that a p-value of 10e-6 must be specified as 6");
			}
			{
				correction = new JCheckBox();
				this.add(correction);
				correction.setText("Benjamini-Hochberg correction");
				correction.setBounds(38, 30, 227, 18);
				correction.setSelected(true);
			}
			{
				pvalueValue = new JTextField();
				this.add(pvalueValue);
				pvalueValue.setText("3");
				pvalueValue.setBounds(228, 9, 43, 21);
			}
			{
				differentialExpression = new JLabel();
				this.add(differentialExpression);
				differentialExpression.setText("Expression threshold              >");
				differentialExpression.setBounds(13, 57, 199, 14);
			}
			{
				expressionValue = new JTextField();
				this.add(expressionValue);
				expressionValue.setText("2.0");
				expressionValue.setBounds(230, 54, 43, 21);
			}
			{
				ComboBoxModel regulationModel = 
					new DefaultComboBoxModel(
							new String[] { "up and down regulated", "only up regulated", "only down regulated" });
				regulation = new JComboBox();
				this.add(regulation);
				regulation.setModel(regulationModel);
				regulation.setBounds(39, 83, 192, 21);
			}

			MicroarrayData md=session.getMicroarrayData();
			ArrayList<String> efs=new ArrayList<String>();
			efs.add("rest");
			for(String ef:md.experimentFactors)
				{
				efs.add(ef);
				ArrayList<String> alreadyAdded=new ArrayList<String>();
				for(String efv: md.experimentFactorValues.get(ef))
					{
					if(!alreadyAdded.contains(efv))	
						{
						alreadyAdded.add(efv);
						efs.add("   "+efv);
						}
					}
				}

			{
				jScrollPane1 = new JScrollPane();
				getContentPane().add(jScrollPane1);
				jScrollPane1.setBounds(13, 136, 241, 178);
				{
					group1Model = new DefaultComboBoxModel(efs.toArray(new String[0]));
					group1 = new JList();
					jScrollPane1.setViewportView(group1);

					group1.setModel(group1Model);
					group1.setSelectedIndex(0);
					group1.setBorder(new LineBorder(new java.awt.Color(0,0,0),1,false));
					group1.setLayout(null);
					group1.setBounds(13, 136, 134, 89);
					group1.setAutoscrolls(true);

				}
			}
			
			{
				jLabel1 = new JLabel();
				this.add(jLabel1);
				jLabel1.setText("Group 1");
				jLabel1.setBounds(15, 116, 79, 14);
			}
			{
				jLabel2 = new JLabel();
				this.add(jLabel2);
				jLabel2.setText("Group 2");
				jLabel2.setBounds(267, 116, 81, 14);
			}
			{
				writeToFile = new JCheckBox();
				getContentPane().add(writeToFile);
				writeToFile.setText("Write to file");
				writeToFile.setBounds(25, 336, 129, 18);
				writeToFile.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent e) {
						if(writeToFile.isSelected())	
							{
							try{
								BufferedReader pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/groupsPath.txt"));
								defaultPath=pathReader.readLine();
								}catch(IOException ex){System.err.println("pathReader has no information"); defaultPath="";}
							select.setEnabled(true);
							addDescription.setEnabled(true);
							description.setEnabled(true);
							}
						else
							{
							defaultPath="";
							select.setEnabled(false);
							addDescription.setEnabled(false);
							description.setEnabled(false);
							}
					}
				});
			}
			{
				select = new JButton();
				getContentPane().add(select);
				select.setText("Select");
				select.setBounds(188, 331, 72, 21);
				select.setEnabled(false);
				select.addActionListener(new java.awt.event.ActionListener() {

					public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser selecFile = new JFileChooser();
					selecFile.addChoosableFileFilter(new BiclusterResultsFilter());
					selecFile.setCurrentDirectory(new File(defaultPath));
					int returnval = selecFile.showSaveDialog((Component)e.getSource());
					
					if(returnval == JFileChooser.APPROVE_OPTION) {
						resultsFile = selecFile.getSelectedFile();
						}
					}
				});
				}
			{
				addDescription = new JCheckBox();
				getContentPane().add(addDescription);
				addDescription.setText("Add description line");
				addDescription.setBounds(37, 355, 194, 18);
				addDescription.setEnabled(false);
			}
			{
				description = new JTextField();
				getContentPane().add(description);
				description.setBounds(37, 382, 422, 21);
				description.setEnabled(false);
			}
			{
				jScrollPane2 = new JScrollPane();
				getContentPane().add(jScrollPane2);
				jScrollPane2.setBounds(265, 137, 216, 177);
				{
					group2Model = new DefaultComboBoxModel(efs.toArray(new String[0]));
					group2 = new JList();
					jScrollPane2.setViewportView(group2);
					group2.setModel(group2Model);
					group2.setSelectedIndex(0);
					group2.setBorder(new LineBorder(new java.awt.Color(0,0,0),1,false));
					group2.setLayout(null);
					group2.setBounds(265, 137, 216, 177);
					group2.setAutoscrolls(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
