package es.usal.bicoverlapper.utils;
import javax.swing.GroupLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;

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
public class HelpPanel extends JPanel
	{
    JLabel text;
    URLLabel link;
	public HelpPanel()
		{
		super();
		this.setLayout(null);
		text=new JLabel("\n\nYou can find full documentation about BicOverlapper at\n");
		this.add(text);
		text.setBounds(12, 12, 364, 15);
		{
			link = new URLLabel(null, "http://carpex.usal.es/~visusal/bicoverlapper/", "http://carpex.usal.es/~visusal/bicoverlapper/");
			this.add(link);
			link.setText("http://carpex.usal.es/~visusal/bicoverlapper/");
			link.setBounds(61, 25, 267, 15);
		}
		//Create and set up the window.
	    JFrame frame = new JFrame("Help");
	    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	
	    //Create and set up the content pane.
	   // JComponent newContentPane = new ProgressBarDemo();
	    JComponent newContentPane = this;
	     newContentPane.setOpaque(true); //content panes must be opaque
	    frame.setContentPane(newContentPane);

	    //Display the window.
	    //frame.add(license);
	    frame.pack();
	    frame.setLocation(100,100);
	    frame.setVisible(true);
		}
	
	private void initGUI() {
		try {
			{
				this.setPreferredSize(new java.awt.Dimension(383, 70));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	}
