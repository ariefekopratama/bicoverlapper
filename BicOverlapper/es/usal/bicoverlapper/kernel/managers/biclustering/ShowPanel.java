package es.usal.bicoverlapper.kernel.managers.biclustering;
import com.cloudgarden.layout.AnchorLayout;
import com.jgoodies.forms.layout.FormLayout;

import es.usal.bicoverlapper.data.GeneAnnotation;
import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Session;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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
public class ShowPanel extends javax.swing.JFrame {
	private JButton jButton1;
	private JButton jButton2;
	private JLabel jLabel2;
	private JList jList2;
	private JList jList1;
	private JLabel jLabel1;
	
	private Session session =null;
	

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ShowPanel inst = new ShowPanel();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public ShowPanel() {
		super();
		initGUI();
	}
	
	public ShowPanel(Session s) {
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
				if(a.name!=null && a.name.length()>0)	rowNames.add(session.getMicroarrayData().rname);
				if(a.description!=null && a.description.length()>0)	rowNames.add(session.getMicroarrayData().rdescription);
				}
			ListModel jList1Model = 
				new DefaultComboBoxModel(rowNames.toArray(new String[0]));
			jList1.setModel(jList1Model);
			jList1.setSelectedIndex(0);
		}
		}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jButton1 = new JButton();
				jButton1.setText("OK");
				jButton1.setLayout(null);
				jButton1.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if(session!=null)	
							{
							String del=" -- ";
							MicroarrayData md=session.getMicroarrayData();
							for(int i=0;i<md.getNumConditions();i++)
								{
								md.columnLabels[i]="";
								for(int j=0;j<jList2.getSelectedValues().length;j++)
									{
									String s=jList2.getSelectedValues()[j].toString();
									if(s!="Column ID")	md.columnLabels[i]+=md.experimentFactorValues.get(s)[i]+del;
									else				md.columnLabels[i]+=md.getConditionName(i)+del;
									}
								md.columnLabels[i]=md.columnLabels[i].substring(0, md.columnLabels[i].length()-3);
								}
							for(int i=0;i<md.getNumGenes();i++)
								{
								md.rowLabels[i]="";
								for(int j=0;j<jList1.getSelectedValues().length;j++)
									{
									String s=jList1.getSelectedValues()[j].toString();
									if(s!=md.chip)		
										{
										if(s.equals(md.rname))	md.rowLabels[i]+=md.geneAnnotations.get(i).name+del;
										if(s.equals(md.rdescription))	md.rowLabels[i]+=md.geneAnnotations.get(i).description+del;
										if(md.rowLabels[i].length()==0) md.rowLabels[i]+=md.getGeneNames()[i]+del;
										}
									else				md.rowLabels[i]+=md.getGeneNames()[i]+del;
									}
								//System.out.println(md.rowLabels[i]);
								if(!md.rowLabels[i].equals(del))			md.rowLabels[i]=md.rowLabels[i].substring(0, md.rowLabels[i].length()-3);
								else										md.rowLabels[i]+=md.getGeneNames()[i];
								}
							//System.out.println("Done!");
							session.changeLabels();
							//md.changeLabels();
							//session.updateAll();
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
				jLabel1 = new JLabel();
				jLabel1.setText("Row names");
				jLabel1.setLayout(null);
			}
			{
				Map<Integer, GeneAnnotation> ga=session.getMicroarrayData().getGeneAnnotations();
				ArrayList<String> rowNames=new ArrayList<String>();
				rowNames.add(session.getMicroarrayData().chip);
				if(ga!=null && ga.values()!=null && ga.values().size()>0)
					{
					GeneAnnotation a=ga.values().iterator().next();
					if(a.name!=null && a.name.length()>0)	rowNames.add(session.getMicroarrayData().rname);
					if(a.description!=null && a.description.length()>0)	rowNames.add(session.getMicroarrayData().rdescription);
					}
				ListModel jList1Model = 
					new DefaultComboBoxModel(rowNames.toArray(new String[0]));
				jList1 = new JList();
				jList1.setModel(jList1Model);
				jList1.setLayout(null);
				jList1.setSelectedIndex(0);
				jList1.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
			}
			{
				jLabel2 = new JLabel();
				AnchorLayout jLabel2Layout = new AnchorLayout();
				jLabel2.setText("Column names");
				jLabel2.setLayout(null);
			}
			{
				ArrayList<String> efs=session.getMicroarrayData().experimentFactors;
				String[] colNames=efs.toArray(new String[efs.size()+1]);
				for(int i=colNames.length-2;i>=0;i--)
					colNames[i+1]=colNames[i];
				colNames[0]="Column ID";
				
				ListModel jList2Model = 
					new DefaultComboBoxModel(colNames);
				jList2 = new JList();
				AnchorLayout jList2Layout1 = new AnchorLayout();
				jList2.setModel(jList2Model);
				jList2.setLayout(null);
				jList2.setSelectedIndex(0);
				jList2.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
			}
			
			
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addContainerGap(18, Short.MAX_VALUE)
				.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(jList1, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jList2, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, GroupLayout.PREFERRED_SIZE)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jButton1, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jButton2, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap());
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
				.addContainerGap(21, 21)
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addGap(0, 0, Short.MAX_VALUE)
				        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				        .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 34, Short.MAX_VALUE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jList1, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 10, Short.MAX_VALUE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 34, Short.MAX_VALUE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jList2, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 10, Short.MAX_VALUE)))
				.addContainerGap(19, 19));
			{
				ListModel jList1Model = 
					new DefaultComboBoxModel(
							new String[] { "Item One", "Item Two" });
			}
			pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
