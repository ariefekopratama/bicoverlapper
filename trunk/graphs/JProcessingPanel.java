package graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.Timer;

import utils.SplineFactory;

/**
 * JProcessingPanel is a JComponent that emulates most of the Processing functions.
 * For documentation about how these Processing methods work, please visit: http://processing.org/reference/
 * @author Rodrigo Santamar�a
 */
public abstract class JProcessingPanel extends JComponent implements Runnable{
	Font fontA=null;
	Graphics2D gr=null;
	Color strokeColor=new Color(255,255,255,255);
	static final float TWO_PI=(float)Math.PI*2;
	static final int CENTER=0;
	static final int LEFT=37;
	static final int RIGHT=39;
	static final int CORNER=3;
	static final int CLOSE=4;
	static final int RADIANS=5;
	static final int DEGREES=6;
	static final int STRAIGHT=7;
	static final int SPLINE=8;
	static final int OPEN=9;
	
	//static final char CODED='?';
	static final char CODED=65535;
	static final int UP=38;
	static final int DOWN=40;
	static final int SHIFT=16;
	static final int CONTROL=17;
	static final int ALT=18;
	
	int rectAl=CORNER;
	int textAl=LEFT;
	int eMode=RADIANS;
	int pMode=STRAIGHT;
	float xText=0;//Posici�n final del �ltimo tramo pintado
	float yText=0;
	boolean drawStroke=true;
	
	int mouseX=0;
	int mouseY=0;
	int mouseButton;
	boolean mousePressed=false;
	
	int keyCode;
	char key;
	boolean keyPressed=false;
	
	ArrayList<Float> poligonX;
	ArrayList<Float> poligonY;
	
	boolean started=false;
	AffineTransform at=null;
	
	Image backBuffer;
	Image img;
	Graphics2D gfinal;
	

//	********************************************************************************
//	---------------Imitaciones de las funciones de Processing-----------------------
//	********************************************************************************
	
	/**
	 * Default constructor
	 */
	public JProcessingPanel()
		{
		MouseManager mm=new MouseManager();
		this.addMouseListener(mm);
		this.addMouseMotionListener(mm);
		if(this.isFocusable())	this.setFocusable(true);
		this.addKeyListener(new KeyboardManager());
		}
//	---------------- polygons --------------
	public void beginShape()
		{
		poligonX=new ArrayList<Float>();
		poligonY=new ArrayList<Float>();
		}
	public void vertex(float x, float y)
		{
		pMode=STRAIGHT;
		poligonX.add(new Float(x));
		poligonY.add(new Float(y));
		}
	public void curveVertex(float x, float y)//NOTE: by now, just makes normal vertex!
		{
		pMode=SPLINE;
		poligonX.add(new Float(x));
		poligonY.add(new Float(y));
		}

	public Polygon endShape()
		{
		return endShape(OPEN);
		}
		
	public Polygon endShape(int closingMode)
		{
		Color temp;
		switch(closingMode)
			{
			case OPEN:
				temp=gr.getColor();
				if(pMode==STRAIGHT)
					{
					int px[]=new int[poligonX.size()];
					int py[]=new int[poligonX.size()];
					for(int i=0;i<poligonX.size();i++)
						{
						px[i]=((Float)(poligonX.get(i))).intValue();
						py[i]=((Float)(poligonY.get(i))).intValue();
						}
					gr.drawPolyline(px,py,px.length);
					gr.setColor(temp);
					return new Polygon(px,py,px.length);
					}
				else
					{
					if(pMode==SPLINE)
						{
						double p[]=new double[poligonX.size()*3];
						for(int i=0;i<poligonX.size();i++)
							{
							p[i*3]=((Float)(poligonX.get(i))).intValue();
							p[i*3+1]=((Float)(poligonY.get(i))).intValue();
							p[i*3+2]=0;
							}
							
						double ps[]=SplineFactory.createCatmullRom(p, 7);
						int px[]=new int[ps.length/3];
						int py[]=new int[ps.length/3];
						for(int i=0;i<ps.length/3;i++)
							{
							px[i]=(int)ps[i*3];
							py[i]=(int)ps[i*3+1];
							}
						gr.drawPolyline(px,py,px.length);
						gr.setColor(temp);
						return new Polygon(px,py,px.length);
						}
					}
				break;
			case CLOSE:
				temp=gr.getColor();
				if(pMode==STRAIGHT)
					{
					int px[]=new int[poligonX.size()];
					int py[]=new int[poligonX.size()];
					for(int i=0;i<poligonX.size();i++)
						{
						px[i]=((Float)(poligonX.get(i))).intValue();
						py[i]=((Float)(poligonY.get(i))).intValue();
						}
					gr.fillPolygon(px,py,px.length);
					gr.setColor(strokeColor);
					gr.drawPolygon(px,py,px.length);
					gr.setColor(temp);
					return new Polygon(px,py,px.length);
					}
				else
					{
					if(pMode==SPLINE)
						{
						double p[]=new double[poligonX.size()*3];
						for(int i=0;i<poligonX.size();i++)
							{
							p[i*3]=((Float)(poligonX.get(i))).intValue();
							p[i*3+1]=((Float)(poligonY.get(i))).intValue();
							p[i*3+2]=0;
							}
							
						double ps[]=SplineFactory.createCatmullRom(p, 7);
						int px[]=new int[ps.length/3];
						int py[]=new int[ps.length/3];
						for(int i=0;i<ps.length/3;i++)
							{
							px[i]=(int)ps[i*3];
							py[i]=(int)ps[i*3+1];
							}
						gr.fillPolygon(px,py,px.length);
						gr.setColor(strokeColor);
						gr.drawPolygon(px,py,px.length);
						gr.setColor(temp);
						return new Polygon(px,py,px.length);
						}
					}
				break;
			}
		return null;
		}
//	--------------- rects -------------
	public void rect(int x, int y, int w, int h)
		{
		int xd=fixCoordinate(x,w);
		int yd=fixCoordinate(y,h);
		
		gr.fillRect(xd,yd,w,h);
		if(drawStroke)//Si se tiene que pintar el stroke
			{
			Color temp=gr.getColor();
			gr.setColor(strokeColor);
			gr.drawRect(xd, yd, w, h);
			gr.setColor(temp);
			}
		}
	public void rect(float x, float y, float w, float h)
		{
		int xd=fixCoordinate((int)x,(int)w);
		int yd=fixCoordinate((int)y,(int)h);
		
		gr.fillRect(xd,yd,(int)w,(int)h);
		if(drawStroke)
			{
			Color temp=gr.getColor();
			gr.setColor(strokeColor);
			gr.drawRect(xd,yd,(int)w,(int)h);
			gr.setColor(temp);
			}
		}
//	-------------- arcs -------------------------
	public void arc(float x, float y, float w, float h, float start, float stop)
		{//stop es en processing absoluto, y en graphics2d se refiere a la distancia desde start
		int xd=fixCoordinate((int)x,(int)w);
		int yd=fixCoordinate((int)y,(int)h);
		float startd=fixDegrees(start);
		float stopd=fixDegrees(stop);
		Color temp=gr.getColor();
		gr.fillArc((int)xd,(int)yd,(int)w,(int)h,(int)startd, (int)(startd-stopd));
		gr.setColor(strokeColor);
		gr.drawArc((int)xd,(int)yd,(int)w,(int)h,(int)startd, (int)(startd-stopd));
		gr.setColor(temp);
		}
	
	public float fixDegrees(float quant)
		{
		float degrees=quant;
		if(eMode==RADIANS)	degrees=(float)(quant*180.0/Math.PI);//Si el modo son radianes, nos han pasado radianes y tenemos q convertir
		return degrees;
		}
//	-------------- Ellipses ---------------------
	public void ellipseMode(int mode)
		{
		eMode=mode;
		}
	public void ellipse(int x, int y, int w, int h)
		{
		int xd=fixCoordinate(x,w);
		int yd=fixCoordinate(y,h);

		Color temp=gr.getColor();
		gr.fillOval(xd,yd,w,h);
		gr.setColor(strokeColor);
		gr.drawOval(xd, yd, w, h);
		gr.setColor(temp);
		}
	public void ellipse(float x, float y, float w, float h)
		{
		int xd=fixCoordinate((int)x,(int)w);
		int yd=fixCoordinate((int)y,(int)h);

		Color temp=gr.getColor();
		gr.fillOval((int)xd,(int)yd,(int)w,(int)h);
		gr.setColor(strokeColor);
		gr.drawOval((int)xd, (int)yd, (int)w, (int)h);
		gr.setColor(temp);
		}


//	-------------- files ------------------------
	public String[] loadStrings(String fileName)
		{
		//ArrayList <String>lista=new ArrayList<String>();
		try{
		BufferedReader in =	new BufferedReader(new FileReader(fileName));
		String cad="";
		int count=0;
		while((cad=in.readLine())!=null)	count++;
		in =	new BufferedReader(new FileReader(fileName));
		String []lista=new String[count];
		count=0;
		while((cad=in.readLine())!=null)	lista[count++]=cad;
		return lista;
			
		}catch(Exception e){e.printStackTrace();}
		return null;
		}
//	-------------- strokes---------------------
	public void stroke(int r, int g, int b, int a)
		{
		drawStroke=true;
		setStrokeColor(new Color(r,g,b,a));
		}

	public void stroke(int r, int g, int b)
		{
		drawStroke=true;
		setStrokeColor(new Color(r,g,b,255));
		}
	public void stroke(int c)
		{
		drawStroke=true;
		setStrokeColor(new Color(c,c,c,255));
		}
	public void strokeWeight(int w)
		{
		drawStroke=true;
		gr.setStroke(new BasicStroke(w));
		}
	public void strokeWeight(float w)
		{
		drawStroke=true;
		gr.setStroke(new BasicStroke(w));
		}

	public void noStroke()
		{
		//strokeWeight(0);
		drawStroke=false;
		}
	public void setStrokeColor(Color colorStroke) 
		{
		this.strokeColor = colorStroke;
		}
//	--------------- lines ---------------------
	public void line(int x1, int y1, int x2, int y2)
		{	
		gr.setColor(strokeColor);
		gr.drawLine(x1, y1, x2, y2);
		}

	public void line(float x1, float y1, float x2, float y2)
		{	
		gr.setColor(strokeColor);
		gr.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
		}

//	----------------- filling --------------------
	public void fill(int r, int g, int b, int a)
		{
		gr.setColor(new Color(r,g,b,a));
		}
	public void fill(int r, int g, int b)
		{
		gr.setColor(new Color(r,g,b,255));
		}
	public void fill(int c)
		{
		gr.setColor(new Color(c,c,c));
		}
	public void noFill()
		{
		gr.setColor(new Color(0,0,0,0));
		}

//	---------------- Strings------------------
	public void text(String cad, float x, float y)
		{
		cad=cad.replace("\n\n", "\n \n");
		StringTokenizer st=new StringTokenizer(cad,"\n");
		float yd=y;
		while(st.hasMoreTokens())
			{
			String cad2=st.nextToken();
			TextLayout text = new TextLayout(cad2, gr.getFont(), gr.getFontRenderContext());
			float xd=fixCoordinate(x, text);
			text.draw(gr,xd, yd);
			
			yText=y;
			switch(textAl)
				{
				case LEFT:
					xText=x+(float)text.getBounds().getWidth();
					break;
				case RIGHT:
					xText=x;
					break;
				case CENTER:
					xText=x+(float)text.getBounds().getWidth()/2;
					break;
				}
			yd+=text.getBounds().getHeight()+2;
			}
		}

	public void textSize(int s)
		{
		Font f=gr.getFont();
		String name=f.getFontName();
		int style=f.getStyle();
		gr.setFont(new Font(name,style,s));
		}
	public void textSize(float s)
		{
		textSize((int)s);
		}

	public void text(String cad)
		{
		float x=xText;
		float y=yText;
		TextLayout text = new TextLayout(cad, gr.getFont(), gr.getFontRenderContext());
		float xd=fixCoordinate(x, text);
		text.draw(gr,xd, y);
		yText=y;
		switch(textAl)
			{
			case LEFT:
				xText=x+(float)text.getBounds().getWidth();
				break;
			case RIGHT:
				xText=x;
				break;
			case CENTER:
				xText=x+(float)text.getBounds().getWidth()/2;
				break;
			}
		}

//	--------------- system ------------------
	public void println(String cad)
		{
		System.out.println(cad);
		}

	public void exit()
		{
		System.exit(1);
		}

//	----------------- triangle -----------------------
	public void triangle(float x1, float y1, float x2, float y2, float x3, float y3)
		{
		beginShape();
		vertex(x1,y1);
		vertex(x2,y2);
		vertex(x3,y3);
		endShape(CLOSE);
		}

//	--------------- images ----------------------
	public Image loadImage(String name)
		{
		try{
			URL imgURL=new URL(name);
			return Toolkit.getDefaultToolkit().getImage(imgURL);
			}catch(MalformedURLException e){e.printStackTrace();}
		return null;
		}
	
	public void image(Image img, int x, int y, int w, int h)
		{
		gr.drawImage(img, x, y, w, h, null);
		}


//	---------------Drawing modes-----------------------
	public void rectMode(int mode)
		{
		rectAl=mode;
		}
	public void textAlign(int mode)
		{
		textAl=mode;
		}
	public int fixCoordinate(int p, int l)
		{
		if(rectAl==CORNER)	return p;
		else				return p-l/2;
		}
	public float fixCoordinate(float p, TextLayout cad)
		{
		if(textAl==LEFT)	return p;
		else
			{
			if(textAl==RIGHT)
				{
				return p-(float)cad.getBounds().getWidth();
				}
			else//Center
				{
				return p-(float)cad.getBounds().getWidth()/2;
				}
			}
		}

//	---------------- Transforms ----------------
	public void pushMatrix()	//NOTA: de momento s�lo por compatibilidad, no se exactamente la utilidad q tienen
		{
		at=gr.getTransform();
		
		}
	public void popMatrix()
		{
		gr.setTransform(at);
		}

	public void scale(int f)
		{
		gr.scale(f, f);
		}
	public void scale(float f)
		{
		gr.scale(f, f);
		}

	public void translate(int x, int y)
		{
		gr.translate(x, y);
		}
	public void translate(float x, float y)
		{
		gr.translate(x, y);
		}

//	----------------- Math ---------------
	public double sqrt(float x)
		{
		return Math.sqrt(x);
		}
	public double max(double x, double y)
		{
		return Math.max(x, y);
		}
	public double min(double x, double y)
		{
		return Math.min(x, y);
		}

	public double random(double x)
		{
		return x*Math.random();
		}

	public float sin(float x)
		{
		return (float)Math.sin((float)x);
		}
	public float cos(float x)
		{
		return (float)Math.cos((float)x);
		}
	
	//----------------- run control -----------------------
	public void init()
		{
		started=true;
		new Thread(this).start();
		}
	public void stop()
		{
		started=false;
		}
	public void run()
		{
		//int delay = 100; //milliseconds
		int delay = 0; //milliseconds
		//int delay = 100000; //milliseconds
		ActionListener taskPerformer = new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		    	  repaint();
		      }
		  };
		new Timer(delay, taskPerformer).start();
		}
	
	//public abstract void draw();
	protected abstract void mouseMoved();
	protected abstract void mousePressed();
	protected abstract void mouseDragged();
	protected abstract void mouseReleased();
	protected void mouseMovedAction()		{		mouseMoved();		}
	protected void mousePressedAction()		{		mousePressed();		}
	protected void mouseDraggedAction()		{		mouseDragged();		}
	protected void mouseReleasedAction()		{		mouseReleased();		}
	
	protected abstract void keyPressed();
	protected void keyPressedAction()		{		keyPressed();		}
	//public abstract void keyReleased();
	//public void keyReleasedAction()		{		keyReleased();		}
	
//	********************************************************************************
	
	//Key and mouse managers*******************************************************
	public class MouseManager implements MouseListener, MouseMotionListener
	{

	public void mouseClicked(MouseEvent e) 
		{
		switch(e.getButton())
			{
			case MouseEvent.BUTTON1:
				mouseButton=LEFT;
				break;
			case MouseEvent.BUTTON2:
				mouseButton=CENTER;
				break;
			case MouseEvent.BUTTON3:
				mouseButton=RIGHT;
				break;
			}
		mouseX=e.getX();
		mouseY=e.getY();
		requestFocusInWindow();
		}

	public void mousePressed(MouseEvent e) 
		{
		switch(e.getButton())
			{
			case MouseEvent.BUTTON1:
				mouseButton=LEFT;
				break;
			case MouseEvent.BUTTON2:
				mouseButton=CENTER;
				break;
			case MouseEvent.BUTTON3:
				mouseButton=RIGHT;
				break;
			}
		mouseX=e.getX();
		mouseY=e.getY();
		mousePressed=true;
		requestFocusInWindow();
		mousePressedAction();
		}
		
	public void mouseReleased(MouseEvent e) 
		{
		mouseX=e.getX();
		mouseY=e.getY();
		mousePressed=false;
		mouseReleasedAction();
		}

	public void mouseEntered(MouseEvent e) 
		{
		mouseX=e.getX();
		mouseY=e.getY();
		}

	public void mouseExited(MouseEvent e) 
		{
		mouseX=e.getX();
		mouseY=e.getY();
		}

	public void mouseDragged(MouseEvent e) 
		{
		mouseX=e.getX();
		mouseY=e.getY();
		mouseDraggedAction();
		}

	public void mouseMoved(MouseEvent e) 
		{
		mouseX=e.getX();
		mouseY=e.getY();
		mouseMovedAction();
		}		
	}

	public class KeyboardManager implements KeyListener
		{
		public void keyPressed(KeyEvent e)
			{
			keyCode=e.getKeyCode();
			key=e.getKeyChar();
			keyPressed=true;
			keyPressedAction();
			}
		public void keyReleased(KeyEvent e)
			{
			keyCode=e.getKeyCode();
			key=e.getKeyChar();
			keyPressed=false;
			//keyReleased();
			}
		public void keyTyped(KeyEvent e)
			{
			keyCode=e.getKeyCode();
			key=e.getKeyChar();
			//keyTyped();
			}
		}

}
