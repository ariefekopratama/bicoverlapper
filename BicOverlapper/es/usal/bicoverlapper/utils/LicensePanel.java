package es.usal.bicoverlapper.utils;


import java.awt.BorderLayout;

import javax.swing.*;


public class LicensePanel extends JPanel
	{

    public JTextArea license;
    public JLabel authors;
    public JLabel group;
    public JLabel lic;
    
    public LicensePanel()
    	{
    	super();
    	authors=new JLabel("Design and development by Rodrigo Santamaria and Roberto Theron");
    	group=new JLabel("      \tVISUSAL Group (http://vis.usal.es)");
    	lic=new JLabel("This software is distributed under the MIT Licence:");
    	license=new JTextArea();
    	license.setText("The MIT License\n	Copyright (c) <2008> <Universidad de Salamanca>\n\n" +
    			" Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
    			" of this software and associated documentation files (the \"Software\"), to deal\n" +
    					" in the Software without restriction, including without limitation the rights\n" +
    					" to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
    					" copies of the Software, and to permit persons to whom the Software is\n" +
    					" furnished to do so, subject to the following conditions:\n" +
    					" \n" +
    					"\n" +
    					" The above copyright notice and this permission notice shall be included in\n" +
    					" all copies or substantial portions of the Software.\n" +
    					"\n" +
    					"   THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
    							"   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
    							"   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
    							"   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
    							"   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
    							"   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\n" +
    							"   THE SOFTWARE.");
    	license.setSize(300,300);
    	this.add(authors);
    	this.add(group);
    	this.add(lic);
    	this.add(license);
        //Create and set up the window.
        JFrame frame = new JFrame("About BicOverlapper");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Create and set up the content pane.
       // JComponent newContentPane = new ProgressBarDemo();
        JComponent newContentPane = this;
         newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.add(license);
        frame.pack();
        frame.setSize(550,400);
        frame.setLocation(100,100);
        
        frame.setVisible(true);
    	}
 
	}