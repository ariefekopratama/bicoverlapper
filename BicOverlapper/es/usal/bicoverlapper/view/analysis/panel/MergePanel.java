package es.usal.bicoverlapper.view.analysis.panel;
import com.cloudgarden.layout.AnchorLayout;
import com.jgoodies.forms.layout.FormLayout;

import es.usal.bicoverlapper.controller.kernel.Selection;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.controller.manager.FileMenuManager;
import es.usal.bicoverlapper.model.gene.GeneAnnotation;
import es.usal.bicoverlapper.model.microarray.MicroarrayData;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;

import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.SwingUtilities;


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
public class MergePanel extends javax.swing.JFrame {
	private JButton jButton1;
	private JButton jButton2;
	private JLabel jLabel2;
	private JList jList2;

	private Session session =null;
	

	
	
	public MergePanel() {
		super();
		initGUI();
	}
	
	public MergePanel(Session s) {
		super();
		session=s;
		initGUI();
	}
	
	public void updateLists()
		{
		{
			Map<Integer, GeneAnnotation> ga=session.getMicroarrayData().getGeneAnnotations();
			ArrayList<String> rowNames=new ArrayList<String>();
			rowNames.add(session.getMicroarrayData().chip);
			if(ga!=null && ga.values()!=null && ga.values().size()>0)
				{
				GeneAnnotation a=ga.values().iterator().next();
				if(a.getName()!=null && a.getName().length()>0)	rowNames.add(session.getMicroarrayData().rname);
				if(a.getDescription()!=null && a.getDescription().length()>0)	rowNames.add(session.getMicroarrayData().rdescription);
				}
		}
		}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jButton1 = new JButton();
				//AnchorLayout jList1Layout = new AnchorLayout();
				jButton1.setText("Merge");
				jButton1.setLayout(null);
				jButton1.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						session=session.getMainWindow().getActiveWorkDesktop().getSession();
						if(session!=null)	
							{
							String s=jList2.getSelectedValues()[0].toString();
							String p=session.getMicroarrayData().path+"/"+session.getMicroarrayData().name+"-merged.txt";
							
							session.getAnalysis().mergeColumns(s, p);
							session.microarrayPath=p;
							
							//TODO: Not too elegant/SE
							FileMenuManager fmm=(FileMenuManager)session.getMicroarrayData().microarrayRequester;
							fmm.setFichero(new File(p));
							fmm.prepareDesktop();
							try{
								fmm.getSesion().getReader().readMicroarray(fmm.getFichero(), fmm.getSesion(), session.getMicroarrayData().microarrayRequester);
							}catch(Exception ex){ex.printStackTrace();}
							setVisible(false);
							}
						}
					});
			}
			
			{
				jButton2 = new JButton();
				AnchorLayout jButton2Layout = new AnchorLayout();
				jButton2.setText("Cancel");
				jButton2.setLayout(null);
				jButton2.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						setVisible(false);
						}
				});
			}
			{
				jLabel2 = new JLabel();
				AnchorLayout jLabel2Layout = new AnchorLayout();
				jLabel2.setText("Merge samples by:");
				jLabel2.setLayout(null);
			}
			{
				ArrayList<String> efs=session.getMicroarrayData().experimentFactors;
				String[] colNames=efs.toArray(new String[efs.size()]);
				
				ListModel jList2Model = 
					new DefaultComboBoxModel(colNames);
				jList2 = new JList();
				AnchorLayout jList2Layout1 = new AnchorLayout();
				jList2.setModel(jList2Model);
				jList2.setLayout(null);
				jList2.setSelectedIndex(0);
				jList2.setAutoscrolls(true);
				jList2.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addContainerGap(35, 35)
				.addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jList2, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jButton1, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jButton2, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap());
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE))
				    .addGroup(thisLayout.createSequentialGroup()
				        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE))
				    .addGroup(thisLayout.createSequentialGroup()
				        .addComponent(jList2, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap(19, Short.MAX_VALUE));

			{
				ListModel jList1Model = 
					new DefaultComboBoxModel(
							new String[] { "Item One", "Item Two" });
			}
			pack();
			this.setSize(160, 204);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
