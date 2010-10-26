package es.usal.bicoverlapper.utils;


import java.awt.BorderLayout;

import javax.swing.*;



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
public class LicensePanel extends JPanel
	{

    public JLabel authors;
    public JLabel group;
    public JLabel lic;
    private JTextArea jTextArea1;

    public LicensePanel()
    	{
    	super();
    	authors=new JLabel("Design and development by Rodrigo Santamaria and Roberto Theron");
    	lic=new JLabel("This software is distributed under the MIT Licence:");
    	
    	this.add(authors);
    	authors.setBounds(158, 5, 400, 15);
    	{
    		group=new JLabel("VISUSAL Group (http://vis.usal.es)");
    		this.add(group);
    		group.setBackground(new java.awt.Color(39,157,217));
    		group.setBounds(255, 26, 195, 15);
    		group.setForeground(new java.awt.Color(64,184,255));
    	}
    	this.add(lic);
    	lic.setBounds(204, 53, 292, 15);
    	{
    		jTextArea1 = new JTextArea();
    		this.add(jTextArea1);
    		jTextArea1.setEditable(false);
    		jTextArea1.setText("\t\t   The MIT License\n" +
    				"\tCopyright (c) <2008> <Universidad de Salamanca>" +
    				"\n    Permission is hereby granted, free of charge, to any person obtaining a copy" +
    				"\n   of this software and associated documentation files (the \"Software\"), to deal" +
    				 "\n   in the Software without restriction, including without limitation the rights" +
    				 "\n   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell" +
    				 "\n   copies of the Software, and to permit persons to whom the Software is" +
    				 "\n   furnished to do so, subject to the following conditions:"+
    				 "\n\n       The above copyright notice and this permission notice shall be included in"+
    				 "\n      all copies or substantial portions of the Software." +
    				 "\n" +
    				 "\n   THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR" +
    				 "\n   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,"+
    				 "\n   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE"+
    				 "\n   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER"+
    				 "\n   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,"+ 
    				 "\n   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN" +
    				 "\n   THE SOFTWARE.");
    		jTextArea1.setBounds(81, 85, 535, 289);
    	}
    	//Create and set up the window.
        JFrame frame = new JFrame("About BicOverlapper");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Create and set up the content pane.
       // JComponent newContentPane = new ProgressBarDemo();
        JComponent newContentPane = this;
         newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setSize(700,400);
        frame.setLocation(100,100);
        
        frame.setVisible(true);
    	}
    
    private void initGUI() {
    	try {
    		{
	    		this.setPreferredSize(new java.awt.Dimension(700, 394));
	    		this.setLayout(null);
	    	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }

	}