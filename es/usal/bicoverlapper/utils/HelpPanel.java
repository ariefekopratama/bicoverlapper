package es.usal.bicoverlapper.utils;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class HelpPanel extends JPanel
	{
    JLabel text;
    URLLabel link;
	public HelpPanel()
		{
		super();
		text=new JLabel("\n\nYou can find full documentation about BicOverlapper at\n");
		link=new URLLabel(this, "http://vis.usal.es/bicoverlapper", "http://vis.usal.es/bicoverlapper", "_blank");
		this.add(text);
		this.add(link);
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
	    frame.setSize(320,100);
	    frame.setLocation(100,100);
	    frame.setVisible(true);
		}
	
	}
