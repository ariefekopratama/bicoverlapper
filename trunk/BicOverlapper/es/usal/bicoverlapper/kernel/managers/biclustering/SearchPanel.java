package es.usal.bicoverlapper.kernel.managers.biclustering;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Rectangle;
import java.awt.GridLayout;
import java.awt.Label;
import javax.swing.JRadioButton;
import java.awt.Checkbox;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JButton;



import es.usal.bicoverlapper.analysis.AnalysisProgressMonitor;
import es.usal.bicoverlapper.analysis.Analysis;
import es.usal.bicoverlapper.analysis.AnalysisProgressMonitor.AnalysisTask;
import es.usal.bicoverlapper.data.files.BiclusterResultsFilter;
import es.usal.bicoverlapper.data.files.TextFileFilter;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.WorkDesktop;
import es.usal.bicoverlapper.utils.Translator;
import javax.swing.JComboBox;

public class SearchPanel{
	
	private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="28,8"
	private JLabel jLabel = null;
	private JLabel jLabel2 = null;
	private JTextField jTextField221 = null;
	private JButton jButton1 = null;
	private Session session =null;
	public File resultsFile=null;
	public String defaultPath="";  //  @jve:decl-index=0:
	protected AnalysisTask t;
	private JComboBox jComboBox = null;
	private JButton jButton11 = null;
	public SearchPanel()
	{}	
	public SearchPanel(Session s)
	{
		session=s;
	}	

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getJPanel() {
		return jPanel;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getJPanel2() {
		if (jPanel == null) {
			jLabel2 = new JLabel();
			jLabel2.setText("in");
			jLabel2.setToolTipText("Biclusters with at least this number of conditions are searched");
			jLabel2.setBounds(new Rectangle(10, 37, 78, 16));
			jLabel = new JLabel();
			jLabel.setText("Search for");
			jLabel.setToolTipText("Biclusters with at least this number of genes are searched");
			jLabel.setBounds(new Rectangle(10, 13, 72, 16));
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setSize(new Dimension(241, 111));
			jPanel.add(jLabel, null);
			jPanel.add(jLabel2, null);
			jPanel.add(getJTextField221(), null);
			jPanel.add(getJButton1(), null);
			jPanel.add(getJComboBox(), null);
			jPanel.add(getJButton11(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jTextField221	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField221() {
		if (jTextField221 == null) {
			jTextField221 = new JTextField();
			jTextField221.setBounds(new Rectangle(95, 9, 124, 20));
			jTextField221.setText("");
		}
		return jTextField221;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setBounds(new Rectangle(21, 65, 82, 26));
			jButton1.setToolTipText("Search for and select matching elements");
			jButton1.setText("Search");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(session!=null)	
						{
						BiclusterSelection sb=session.getMicroarrayData().search(jTextField221.getText(),jComboBox.getSelectedIndex(), false);
						if(sb.getGenes().size()>0 || sb.getConditions().size()>0)
							{
							session.setSelectedBiclustersExcept(sb,"");
							((JFrame)(getJPanel2().getTopLevelAncestor())).dispose();
							}
						else
							JOptionPane.showMessageDialog(null,
									"No genes or conditions found", 
									"Search failed", JOptionPane.INFORMATION_MESSAGE);
				    	    	
						
						}
					}
			});
		}
		return jButton1;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			jComboBox.setBounds(new Rectangle(97, 34, 121, 19));
			jComboBox.addItem("Anywhere");
			jComboBox.addItem("Gene names");
			jComboBox.addItem("Condition names");
			jComboBox.addItem("Gene annotations");
		}
		return jComboBox;
	}
	/**
	 * This method initializes jButton11	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton11() {
		if (jButton11 == null) {
			jButton11 = new JButton();
			jButton11.setBounds(new Rectangle(137, 65, 75, 26));
			jButton11.setToolTipText("Search for matching elements and add to current selection");
			jButton11.setText("Add");
			jButton11.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(session!=null)	
						{
						BiclusterSelection sb=session.getMicroarrayData().search(jTextField221.getText(),jComboBox.getSelectedIndex(), false);
						if(sb.getGenes().size()>0 || sb.getConditions().size()>0)
							{
							BiclusterSelection csb=session.getSelectedBicluster();
							if(csb!=null)
								{
								csb.getGenes().addAll(sb.getGenes());
								csb.getConditions().addAll(sb.getConditions());
								session.setSelectedBiclustersExcept(csb,"");
								}
							else
								session.setSelectedBiclustersExcept(sb,"");
							
							((JFrame)(getJPanel2().getTopLevelAncestor())).dispose();
							}
						else
							JOptionPane.showMessageDialog(null,
									"No genes or conditions found", 
									"Search failed", JOptionPane.INFORMATION_MESSAGE);
				    	    	
						
						}
					}
			});
	
		}
		return jButton11;
	}

}
