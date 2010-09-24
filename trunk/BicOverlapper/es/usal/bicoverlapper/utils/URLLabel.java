package es.usal.bicoverlapper.utils;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class URLLabel extends JLabel  {
 /**
	 * 
	 */
	private static final long serialVersionUID = -7796533770130230112L;
 private URL url;
 private String target = "";
 private Color unvisitedURL = Color.blue;
 private Color visitedURL = Color.green;

 public URLLabel(JComponent applet , String url, String text){
   this(applet, url, text, "_self");
   }

 public URLLabel
     (JComponent applet , String url, String text, String target){
   super(text);
   setForeground(unvisitedURL);
   try {
  //  this.applet = applet;
    this.url = new URL(url);
    this.target = target;
    addMouseListener( new Clicked() );
   }
   catch (Exception e) {
    e.printStackTrace();
   }
 }

 public void paint(Graphics g) {
  Rectangle r;
  super.paint(g);
  r = g.getClipBounds();
  g.drawLine(0,
    r.height - this.getFontMetrics(this.getFont()).getDescent(),
    this.getFontMetrics(this.getFont()).stringWidth(this.getText()),
    r.height - this.getFontMetrics(this.getFont()).getDescent());
 }

 public void setUnvisitedURLColor(Color c) {
  unvisitedURL = c;
 }

 public void setVisitedURLColor(Color c) {
  visitedURL = c;
 }

 class Clicked extends MouseAdapter{
  public void mouseClicked(MouseEvent me){
   setForeground(visitedURL);
  try{
	  String os=System.getProperty("os.name");
	  if(os.contains("Win"))
		Runtime.getRuntime().exec("explorer " + url); 
	  else if(os.contains("Linux"))
		Runtime.getRuntime().exec("firefox " + url); 
	  else if(os.contains("Mac"))
		Runtime.getRuntime().exec("safari " + url); 
	  
  }catch(Exception e){e.printStackTrace();}
  }
 }
}