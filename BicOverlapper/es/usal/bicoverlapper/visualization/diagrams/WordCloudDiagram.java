package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

//import es.usal.bicoverlapper.data.EReader;
import es.usal.bicoverlapper.data.EReader;
import es.usal.bicoverlapper.data.GeneAnnotation;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.kernel.DiagramWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.BiclusterSelection;

import es.usal.bicoverlapper.utils.CustomColor;
//import gov.nih.nlm.ncbi.www.soap.eutils.esearch.IdListType;
//import gov.nih.nlm.ncbi.www.soap.eutils.esummary.DocSumType;

public class WordCloudDiagram extends Diagram implements ActionListener,ChangeListener,MouseListener
	{
	private static final long serialVersionUID = 1L;
	
	// atributos del panel del diagrama
	private Session sesion;
	private MultidimensionalData datos;
	private int alto;
	private int ancho;
	private boolean atributosIniciados = false, configurando = false, diagramaPintado = false;
		
	// definicion de margenes del diagrama
	private float zoom =(float) 100;
	final int margenDer = 40;
	final int margenIzq = 40;
	final int margenSup = 25;
	final int margenInf = 40;
	final int margenDiagrama = 10; // porcentaje de exceso en intervalo de representacion del diagrama
	
	// configuracion de color
	
	private static final int colorEtiquetaVar = 0;
	private static final int colorVarSelec = 1;
	private static final int colorFondoEtiqueta = 2;
	private static final int colorTextoEtiqueta = 3;
	private static final int colorLineaOut = 4;
	private static final int colorLinea = 5;
	private static final int colorLineaSelec = 6;
	private static final int colorLineaMarcada = 7;
	private static final int colorEje = 8;
	private static final int colorEjeSelec = 9;
	private static final int colorFondo = 3;
	private Color[] paleta = {Color.BLUE, Color.RED, Color.YELLOW, Color.WHITE, Color.ORANGE, Color.BLUE,
							  Color.RED, Color.YELLOW, Color.BLACK, Color.RED, Color.LIGHT_GRAY,Color.WHITE};
	private String[] textoLabel = {"Color Etiqueta Variable", "Color Variable Seleccionada", "Color Fondo Etiqueta", "Color Texto Etiqueta",
								   "Color Linea Excluida", "Color Linea", "Color Linea Seleccionada", "Color Linea Marcada", "Color Ejes",
								   "Color Eje Seleccionado", "Color Fondo"};
	private JTextField[] muestraColor = new JTextField[paleta.length];
	
	//private int[] seleccionId = {kernel.Configuracion.DiagramaPuntosId, kernel.Configuracion.CoordenadasId, kernel.Configuracion.DendrogramaId, kernel.Configuracion.BubbleGraphId, 
		//	kernel.Configuracion.TreeMapId};

	
	// atributos de configuracion anclajes
	
	private DiagramWindow itemAñadir, itemEliminar;	
	//private Frame frame;
	
	
	//Atributos propios de la clase --------------------
	private float maxFontSize=(float)400;
	private float minFontSize=(float) 1.5;	
	private int maxCont=0;
	private int totChar=0;
	private int contChar=0;
	private int maxWord=0;
	private double Y=0;
	private double  X=0;
	private int maxChar=0;
	private String nameC=panelMenu.GO_TERMS;
	private ArrayList<String> nameSelected;
	private Color colorNameSelected;
	private Color myColor;
	private panelMenu menuCloud=new panelMenu();
	private TreeMap <String,Word> words;
	private boolean newSelection=true;

	private boolean existNameSelected;
	private int contWord;
	private float escala=0;
	//private 
	private int contZoom=0;
	private boolean Ajusta=true;

	private boolean Enought=true;	
	
	
	public WordCloudDiagram(Session sesion, Dimension dim)
		{
		super(new BorderLayout());//
		int num = sesion.getNumWordClouds();
		this.setName("Keyword Cloud "+num);
		this.sesion = sesion;
		this.datos = sesion.getData();
		this.alto = (int)dim.getHeight();
		this.ancho = (int)dim.getWidth();
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);		
		
		CustomColor c=new CustomColor(200,0,0,200);
		
		this.colorNameSelected=new Color(c.getR(), c.getG(), c.getB());
		this.myColor=new Color(c.getR(), c.getG(), c.getB(),c.getA());
		this.colorSeleccion=new Color(c.getR(), c.getG(), c.getB(),c.getA());
		this.setBackground(Color.WHITE);
		
		this.add(menuCloud,BorderLayout.SOUTH);
		menuCloud.descButton.addActionListener(this);
		menuCloud.goButton.addActionListener(this);
		menuCloud.goButton.setEnabled(false);
		menuCloud.sliderZoom.addChangeListener(this);
		menuCloud.sliderZoom.setVisible(false);
		this.addMouseListener(this);
		nameSelected=new ArrayList<String>();
		Ajusta=true;

		words=new TreeMap<String,Word>();
		update();
		}
	
	private void setEnabledButon(){
		menuCloud.descButton.setEnabled(true);
		menuCloud.goButton.setEnabled(true);
	}
	
	private Color getColorW(String w){		
		Color colorW=(Color)this.colorSeleccion;
		if (nameSelected.size()>0)
		{
			for (int j=0;j<nameSelected.size();j++)
				{
				if(nameSelected.get(j).equalsIgnoreCase(w)){
					colorW=WordCloudDiagram.this.colorNameSelected;
					}
				}
			}
		
		return colorW;
	}
	
	
	public void update() 
		{
		contZoom=0;
		Dimension dim = new Dimension(ancho,alto);
		this.setPreferredSize(dim);
		this.setSize(dim);		
		maxChar=0;
		contChar=0;
		maxWord=0;
		contWord=0;
		maxCont=0;
		words.clear();
		Color colorW;
		Ajusta=true;
		Enought=true;
		getTotChar();
		this.colorSeleccion=this.sesion.getSelectionColor();
		
		if(this.sesion.getSelectedBicluster()!=null)
			{
			ArrayList<String> names=sesion.getMicroarrayData().getGeneNames(sesion.getSelectedGenesBicluster());
			
			//******************** GENE ANNOTATIONS FROM MICROARRAY DATA ******************
			for(int i=0;i<names.size();i++)
				{
				GeneAnnotation ga=sesion.getMicroarrayData().getGeneAnnotations().get(sesion.getMicroarrayData().getGeneId(names.get(i)));
				if(ga!=null && ga.description!=null && ga.description.length()>0)
					{
					String desc=ga.description;
	        		String[] dw=desc.split(" ");
	        		for(int j=0;j<dw.length;j++)
	        			{
	        			String w=dw[j].replace("(", "").replace(")", "").trim().toLowerCase();
	        			if(words.containsKey(w))
							{
							Word nW=(Word)words.get(w);
							colorW=getColorW(w);
							TextLayout text=nW.getText();
							double x=nW.getX();
							double y=nW.getY();
							words.put(w,new Word(text,x,y,nW.cont+1,colorW));
							if ((nW.cont+1)>maxCont) maxCont=(nW.cont+1);
							}
						else
							{
							colorW=getColorW(w);
							words.put(w, new Word(null,0,0,new Integer(1),colorW));
							contChar+=w.length();
							contWord+=1;
							}
						maxWord+=1;
						maxChar+=w.length();
	        			}
					}
				}
			//******************** NCBI HTTP Browsing **************************
			/*
			String ids="";
			for(int i=0;i<names.size();i++)	//get NCBI ids
				{
				IdListType list=EReader.eGeneQuery(names.get(i)+" AND Escherichia coli");
				if(list!=null && list.getId()!=null)
					{
					if(list.getId().length>0)	ids=ids.concat(list.getId(0)+", ");//TODO: sólo añadimos el primer gene id, suponiendo que es el del organismo bueno
					}
				}
			DocSumType[] res=EReader.eGeneSummary(ids.substring(0, ids.length()-2));
			for(int i=0; i<res.length; i++)
	            {
	            for(int k=0; k<res[i].getItem().length; k++)
	                {
	                if(res[i].getItem()[k].get_any()!=null)
	                	if(res[i].getItem(k).getName().contains("Description"))
	                		{
	                		String desc=res[i].getItem()[k].get_any()[0].getValue();
	                		String[] dw=desc.split(" ");
	                		for(int j=0;j<dw.length;j++)
	                			{
	                			String w=dw[j].replace("(", "").replace(")", "").trim().toLowerCase();
	                			System.out.println("añadiendo "+w);
								if(words.containsKey(w))
									{
									Word nW=(Word)words.get(w);
									colorW=getColorW(w);
									TextLayout text=nW.getText();
									double x=nW.getX();
									double y=nW.getY();
									words.put(w,new Word(text,x,y,nW.cont+1,colorW));
									if ((nW.cont+1)>maxCont) maxCont=(nW.cont+1);
									}
								else
									{
									colorW=getColorW(w);
									words.put(w, new Word(null,0,0,new Integer(1),colorW));
									contChar+=w.length();
									contWord+=1;
									}
								maxWord+=1;
								maxChar+=w.length();
	                			}
	                		}
	                }
	            }*/
			
		float pMax=(float)(((float)maxChar*(float)100.0)/(float)totChar);//((-1.0)*zoom)+			
		float pDifSelec=(float)((float)(((((float)maxChar-(float)contChar))*(float)100.0)/(float)totChar));
		float pDif=(float)((float)(((((float)totChar-(float)maxChar))    )/(float)totChar));//*(float)100.0
		zoom=(float)((float)pMax+(float)pDifSelec)-((float)((float)pMax+(float)pDifSelec)*((float)1.0 -(float)pDif   ));///(float)100.0
		menuCloud.sliderZoom.setValue(Math.round(zoom*4));
		
		//this.repaint();
		if(this.getGraphics()!=null)	this.paintComponent(this.getGraphics());
		}
	else	//Perform the operation under all genes with annotations
		{
		//************* ALL DATA FROM GENEANNOTATIONS ********************
		  Iterator<es.usal.bicoverlapper.data.GeneAnnotation> it=this.sesion.getMicroarrayData().getGeneAnnotations().values().iterator();
		while(it.hasNext())
			{
			es.usal.bicoverlapper.data.GeneAnnotation ga=it.next();
			if(ga.description!=null && ga.description.length()>0)
				{
				String desc=ga.description;
        		String[] dw=desc.split(" ");
        		for(int j=0;j<dw.length;j++)
        			{
        			String w=dw[j].replace("(", "").replace(")", "").trim().toLowerCase().replace(",", "").replace(".", "");
        			//System.out.println("añadiendo "+w);
					if(words.containsKey(w))
						{
						Word nW=(Word)words.get(w);
						colorW=getColorW(w);
						TextLayout text=nW.getText();
						double x=nW.getX();
						double y=nW.getY();
						words.put(w,new Word(text,x,y,nW.cont+1,colorW));
						if ((nW.cont+1)>maxCont) maxCont=(nW.cont+1);
						}
					else
						{
						colorW=getColorW(w);
						words.put(w, new Word(null,0,0,new Integer(1),colorW));
						contChar+=w.length();
						contWord+=1;
						}
					maxWord+=1;
					maxChar+=w.length();
        			}
				}
			}
        //***************** STATIC TESTING *************************
		/*String desc="uridine phosphorylase";
		String[] dw=desc.split(" ");
		for(int j=0;j<dw.length;j++)
			{
			String w=dw[j].replace("(", "").replace(")", "").trim().toLowerCase();
			System.out.println("añadiendo "+w);
			if(words.containsKey(w))
				{
				Word nW=(Word)words.get(w);
				colorW=getColorW(w);
				TextLayout text=nW.getText();
				double x=nW.getX();
				double y=nW.getY();
				words.put(w,new Word(text,x,y,nW.cont+1,colorW));
				if ((nW.cont+1)>maxCont) maxCont=(nW.cont+1);
				}
			else
				{
				colorW=getColorW(w);
				words.put(w, new Word(null,0,0,new Integer(1),colorW));
				contChar+=w.length();
				contWord+=1;
				}
			}*/
		//*************

		float pMax=(float)(((float)maxChar*(float)100.0)/(float)totChar);//((-1.0)*zoom)+			
		float pDifSelec=(float)((float)(((((float)maxChar-(float)contChar))*(float)100.0)/(float)totChar));
		float pDif=(float)((float)(((((float)totChar-(float)maxChar))    )/(float)totChar));//*(float)100.0
		zoom=(float)((float)pMax+(float)pDifSelec)-((float)((float)pMax+(float)pDifSelec)*((float)1.0 -(float)pDif   ));///(float)100.0
		menuCloud.sliderZoom.setValue(Math.round(zoom*4));
		
		//this.repaint();
		if(this.getGraphics()!=null)	this.paintComponent(this.getGraphics());
		}
	}
	
	/*
	private void setSelectedPoints(Vector<String> titulos)
		{
		SeleccionPuntos sp=new SeleccionPuntos("","",this.sesion.getDatos(this.isPersonas()).getNumElems());
		for(int i=0;i<titulos.size();i++)
			{
			String titulo=titulos.get(i);
			for(int j=0;j<this.sesion.getDatos(this.isPersonas()).getNumElems();j++)
				{
				String id=(String)this.sesion.getDatos(this.isPersonas()).getIdentificador(j);
				if(id.equalsIgnoreCase(titulo))	
					{
					sp.setX(j, true);
					sp.setLastSelec(true, j);
					sp.setColorSelec(this.colorSeleccion, j);
					}
				}
			}
		this.sesion.getCapaDatos().setColorSelec(this.colorSeleccion);
		this.sesion.getCapaDatos().setSelecPuntos(sp);
		this.getVentana().actualizar(seleccionId);
		this.repaint();
		}
	
	private void getSelection() {		
		Vector<String> titulos=new Vector<String>();		
		if(nameC.equalsIgnoreCase(panelMenu.GENRE))
			{
			Iterator it=MovieDB.instance.movies.values().iterator();
			while(it.hasNext())
				{
				Movie m=(Movie)it.next();
				List<Genre> l=m.genres;
				for(int i=0;i<l.size();i++)
					{
					for(int j=0;j<nameSelected.size();j++)
						{
						if(nameSelected.get(j).equalsIgnoreCase(l.get(i).getName()))
							{
							titulos.add(m.title);
							}
						}
					}
				}
			}
		else if(nameC.equalsIgnoreCase(panelMenu.KEYWORD))
			{
			Iterator it=MovieDB.instance.movies.values().iterator();
			while(it.hasNext())
				{
				Movie m=(Movie)it.next();
				List<String> l=m.keywords;
				for(int i=0;i<l.size();i++)
					{
					String w=(String)l.get(i);				
					for(int j=0;j<nameSelected.size();j++)
						{
						if(nameSelected.get(j).equalsIgnoreCase(w))
							{
							titulos.add(m.title);
							}
						}
					}
				}
			}
		else if(nameC.equalsIgnoreCase(panelMenu.DISTRIBUTOR))
			{
			Iterator it=MovieDB.instance.movies.values().iterator();
			while(it.hasNext())
				{
				Movie m=(Movie)it.next();
				List<String> l=m.companies;
				for(int i=0;i<l.size();i++)
					{
					String w=(String)l.get(i);
					for(int j=0;j<nameSelected.size();j++)
						{
						if(nameSelected.get(j).equalsIgnoreCase(w))
							{
							titulos.add(m.title);
							}
						}
					}			
				}
			}
		setSelectedPoints(titulos);
	}*/
	
	public void create()
		{
		//Añadir el display o papplet o frame de visualización a la ventana
		//Asociar este panel a la ventana: 
		//Opcionalmente, se pueden inicializar aquí los datos del frame, en vez de hacerlo en el constructor
		//this.getVentanta().add(framequehagafalta);
		this.getWindow().setContentPane(this);
		this.getWindow().pack();
		}
	
	//	Método que se activa cuando cambia la selecciòn en una ventana
	public void actualizar() 
		{
		LinkedList<Integer> g=new LinkedList<Integer>();
		LinkedList<Integer> c=new LinkedList<Integer>();
		Iterator<GeneAnnotation> it=sesion.getMicroarrayData().getGeneAnnotations().values().iterator();
		for(int i=0;i<sesion.getMicroarrayData().getNumGenes();i++)
			{
			GeneAnnotation ga=sesion.getMicroarrayData().getGeneAnnotations().get(i);
			if(ga!=null)
				{
				for(int j=0;j<nameSelected.size();j++)
					{
					String name=nameSelected.get(j);
					if(ga.description.contains(name))
						{
						//g.add(sesion.getMicroarrayData().getGeneId(ga.name));
						g.add(i);
						System.out.println("Añadiendo gen "+ga.name+" con id "+i);
						break;
						}
					}
				}
			}
		/*while(it.hasNext())
			{
			GeneAnnotation ga=it.next();
			
			for(int i=0;i<nameSelected.size();i++)
				{
				String name=nameSelected.get(i);
				if(ga.description.contains(name))
					{
					g.add(sesion.getMicroarrayData().getGeneId(ga.name));
					System.out.println("Añadiendo gen "+ga.name+" con id "+sesion.getMicroarrayData().getGeneId(ga.name));
					break;
					}
				}
			}*/
		for(int i=0;i<sesion.getMicroarrayData().getNumConditions();i++)	c.add(i);		
		es.usal.bicoverlapper.kernel.BiclusterSelection bs=new BiclusterSelection(g,c);
		sesion.setSelectedBiclusters(bs, "loud");
		update();		
		}
	public void run()
		{
		this.getWindow().setVisible(true); // show the window
		//TODO: Lo necesario para que empiece a correr la visualización (p. ej. en prefuse, los Visualization.run())
		}
	private void drawFondo(Graphics2D g2) {
		g2.setPaint(paleta[colorFondo]);
	    Rectangle2D.Double fondo =
			new Rectangle2D.Double(0,0,ancho,alto);
		g2.fill(fondo);
		g2.draw(fondo);			
	}
	public void redimensionar(){		
		Dimension dim = new Dimension(ancho,alto);		
		this.setPreferredSize(dim);
		this.setSize(dim);		
		this.repaintAll=true;
		//System.out.println("drawFondo:"+ancho+","+alto+"\n");
	}
	private float getProportion(String w,int c,int ww,int wh){
		float hipo,size,cMaxProportion;
		hipo=(float)(Math.sqrt((this.ancho*this.ancho)+((this.alto-this.menuCloud.getHeight())*(this.alto-this.menuCloud.getHeight()))));
		cMaxProportion=(float)(hipo)/(float)(Math.sqrt(maxChar));				
		//System.out.println(" z: "+zoom);
		//minFontSize=(float)(float)zoom*(float)(-1.0)*(float)(1)/(float)5;
		minFontSize=(float)(((float)contWord/(float)maxWord));//*(float)0.15);//(-0.1)
		maxFontSize=(float)(float)zoom*(float)(cMaxProportion);//*((float)hipo)
		float newMax=(float)maxFontSize;
		float newMin=(float)minFontSize;
		float max=(float)maxCont;
		float min=(float)1;
		float key=(float)c;		
		//System.out.println("mi= "+min+" ma= "+max+" nMi = "+newMin+" newMax= "+newMax+" c="+c);
		size=(float)getInterpolation(key,max,min,newMax,newMin);
			//(float)((minFontSize)*(float)(c));//key +": ma:"+max+"/"+min+" -> nMa:"+newMax+"/"+newMin+
			//	System.out.println(" mFS= "+minFontSize+" sz = "+size+" "+w+"\n");		
		return size;
	}
	private float getInterpolation(float key,float maxKey,float minKey,float newMax,float newMin)
	{
		return (float)(key-minKey)*(newMax-newMin+1)/(maxKey-minKey)+newMin;
	}
	public void drawWords(Graphics2D g2)
	{
		drawFondo(g2);
		Iterator it=words.keySet().iterator();
		while(it.hasNext())
			{			
			String w=(String)it.next();
			if(w.length()>0)
				{
				Word nW=words.get(w);
				g2.setPaint(nW.getColor());
				if(nW.text!=null)
					{
					if(nW.getText().getBounds().getHeight()>1)
						{
						
						((TextLayout)nW.getText()).draw(g2,(float)(nW.getX()),(float)(nW.getY()));//+maxAlto
						//double x=(double) + nW.x + (float)(nW.getText().getBounds().getWidth()+ nW.getText().getBounds().getX()-nW.getContText().getBounds().getWidth());
						
						//g2.setPaint(new Color(255,255,255,150));
						//((TextLayout)nW.getContText()).draw(g2,(float)(x),(float)(nW.y));
						//g2.setPaint(nW.getColor());
						}
					}
				}		
			}
	}
	
	
	private void getTotChar() {
	totChar=1;
	/*Iterator it=MovieDB.instance.movies.values().iterator();
	while(it.hasNext())
		{
		Movie m=(Movie)it.next();
		if(nameC.equalsIgnoreCase(panelMenu.GENRE))
		{
			List<Genre> l=m.genres;
			for(int i=0;i<l.size();i++)
				{
				String w=(String)l.get(i).name();				
				totChar+=w.length();
				}
			//totChar=maxChar;
			}
		else  
		if(nameC.equalsIgnoreCase(panelMenu.KEYWORD))
		{
			List<String> l=m.keywords;
			for(int i=0;i<l.size();i++)
				{
				String w=(String)l.get(i);				
				totChar+=w.length();
				}			
			}
		else if(nameC.equalsIgnoreCase(panelMenu.DISTRIBUTOR))
			{
				List<String> l=m.companies;
				for(int i=0;i<l.size();i++)
					{
					String w=(String)l.get(i);
					totChar+=w.length();
					}			
				}
			}*/
		}
	
	public void setWords(Graphics2D g2)
		{
		double y=0;
		double x=0;
		double altoTexto =0;
		double anchoTexto=0;
		double maxAlto=0,AreaText=0;		 

		//1) Primera vuelta, chequeamos el tamaño
		Iterator<String> it=words.keySet().iterator();
		while(it.hasNext())
			{			
			String w=(String)it.next();
			if(w.length()>0)
				{
				Word nW=words.get(w);
				String wc=" "+w.concat("("+nW.cont+") ");
				float num=nW.cont;
				
				Font f=g2.getFont().deriveFont((float)num);
				TextLayout texto = new TextLayout(wc,f,g2.getFontRenderContext());
				//TextLayout contText = new TextLayout(" <"+nW.cont+"> ",f,g2.getFontRenderContext());
				altoTexto = texto.getBounds().getHeight();
				anchoTexto = texto.getBounds().getWidth();
				if(x+anchoTexto>this.getWidth())	
					{
					x=0;	
					y+=maxAlto; 
					maxAlto=altoTexto;
					}
				if(maxAlto<altoTexto)	
					maxAlto=altoTexto;
				nW.setX(x);
				//nW.setY(y+maxAlto);
				nW.setY(y+0.75*maxAlto);
				nW.setText(texto);
				//nW.setContText(contText);
				if(x+anchoTexto<this.getWidth())	
					x+=anchoTexto;
				}		
			}
		y+=maxAlto;//Si no el último salto no se tiene en cuenta.
		//2) Determinamos el factor de escala
		double scale=Math.floor(this.getHeight()/y);
		if(scale>50)	scale=50;
		//System.out.println("Escala es "+scale);
		
		//3) Segunda vuelta vuelta, con los tamaños adecuados
		// como puede haber saltos de línea por el escalado, lo hacemos en un bucle donde vamos disminuyendo poco a poco la escala
		boolean end=false;
		do{
		x=0;
		y=0;
		ArrayList <Word> wordsInLine=new ArrayList<Word>();
		it=words.keySet().iterator();
		while(it.hasNext())
			{			
			String w=(String)it.next();
			if(w.length()>0)
				{
				Word nW=words.get(w);
				String wc=" "+w.concat("("+nW.cont+") ");
				float num=nW.cont;
				//float num=words.get(w).intValue();
				//if((float)num>(float)1)
					{
					Font f=g2.getFont().deriveFont((float)(num*scale));
					TextLayout texto = new TextLayout(wc,f,g2.getFontRenderContext());
					//TextLayout contText = new TextLayout(" <"+nW.cont+"> ",f,g2.getFontRenderContext());
					altoTexto = texto.getBounds().getHeight();
					anchoTexto = texto.getBounds().getWidth();
					if(x+anchoTexto>this.getWidth())	//cambio de línea
						{
						//Sumamos a todas las de la línea anterior el maxAlto de esa línea
						for(int i=0;i<wordsInLine.size();i++)
							{
							Word w2=wordsInLine.get(i);
							w2.setY(w2.getY()+.75*maxAlto);
							//System.out.println("Palabra "+w2.label+" en "+w2.x+", "+w2.y+" con altura "+w2.getText().getBounds());
							}
						wordsInLine.clear();
						x=0;	
						y+=maxAlto; 
						maxAlto=altoTexto;
						}
					if(maxAlto<altoTexto)	
						maxAlto=altoTexto;
					nW.setX(x);
					//nW.setY(y+.75*maxAlto);//debería ser directamente +maxAlto, pero no queda bien y no sé por qué. 
					nW.setY(y);//debería ser directamente +maxAlto, pero no queda bien y no sé por qué. 
										//luego está el tema de que cuando se está viendo una, si luego hay otra mayor, no va a tener en cuenta el maxAlto total.
											//habría que ir guardando todas las que van en una línea y luego sumarlas a todas el maxAlto, durante el cambio de línea
					nW.setText(texto);
					nW.label=w;
					wordsInLine.add(nW);
					//nW.setContText(contText);
					if(x+anchoTexto<this.getWidth())	
						x+=anchoTexto;
					}
				}		
			}
		y+=maxAlto;//si no la última línea no se tiene en cuenta
		for(int i=0;i<wordsInLine.size();i++)//y recolocación por el tamaño máximo tb en la última línea
			{
			Word w2=wordsInLine.get(i);
			w2.setY(w2.getY()+.75*maxAlto);
			//System.out.println("Palabra "+w2.label+" en "+w2.x+", "+w2.y+" con altura "+w2.getText().getBounds());
			}
		
		if(y<=this.getHeight()-menuCloud.getBounds().height)	
			{
			//System.out.println("Terminamos, y es "+y+" frente a altura de "+this.getHeight());
			end=true;
			}
		else	scale-=0.1;
		//System.out.println("Escala es "+scale);
		}while(!end);
		}

	/*
	public void setWords(Graphics2D g2)
		{
		double y=0;
		double x=0;
		double altoTexto =0;
		double anchoTexto=0;
		double maxAlto=0,AreaText=0;		 
		//animacion
		//do{
			Iterator it=words.keySet().iterator();
			while(it.hasNext())
				{			
				String w=(String)it.next();
				if(w.length()>0)
					{
					Word nW=words.get(w);
					String wc=w.concat(" <"+nW.cont+"> ");
					//float num=getProportion(wc,nW.cont,this.getWidth(),this.getHeight());
					float num=nW.cont;
					
					//float num=words.get(w).intValue();
					//if((float)num>(float)1)
						{
						Font f=g2.getFont().deriveFont((float)num);
						TextLayout texto = new TextLayout(wc,f,g2.getFontRenderContext());
						TextLayout contText = new TextLayout(" <"+nW.cont+"> ",f,g2.getFontRenderContext());
						altoTexto = texto.getBounds().getHeight();
						anchoTexto = texto.getBounds().getWidth();
						if(x+anchoTexto>this.getWidth())	
							{
							x=0;	
							y+=maxAlto; 
							maxAlto=altoTexto;
							}
						if(maxAlto<altoTexto)	
							maxAlto=altoTexto;
						nW.setX(x);
						nW.setY(y+maxAlto);//-altoTexto
						nW.setText(texto);
						nW.setContText(contText);
						if(x+anchoTexto<this.getWidth())	
							x+=anchoTexto;
						if(anchoTexto>this.getWidth()*(float)0.95)
							Enought=false;
						}
					}		
				}
			if(Enought)
				{
				if (Ajusta)reajusta(maxAlto,y);
				}
			else
				reversa(maxAlto,y);
		}*/
	
	public void paintComponent(Graphics g) 
		{
		Graphics2D g2=(Graphics2D)g;
		setWords(g2);
		drawWords(g2);				
		}
	private void reversa(double maxAlto,double y) 
		{
		float tamPasoEscala=(float)Math.abs((this.getHeight()-this.menuCloud.getHeight())-(y+maxAlto));
		tamPasoEscala/=(float)4.2;
		escala=(float)(((float)this.getHeight())/((float)(y+maxAlto)));
		zoom=(float)zoom-((float)(zoom/tamPasoEscala)*((float)escala));
		Enought=true;
		Ajusta=false;
		this.repaint();
		}	
	private void reajusta(double maxAlto,double y) {
		if((this.getHeight()-this.menuCloud.getHeight())>(y+maxAlto)  )
			{
			if(((this.getHeight()-this.menuCloud.getHeight())-(y+maxAlto))>((this.getHeight()-this.menuCloud.getHeight())*0.081))
				{
					float tamPasoEscala=(float)Math.abs((this.getHeight()-this.menuCloud.getHeight())-(y+maxAlto));
					tamPasoEscala/=(float)4.2;
					escala=(float)(((float)(this.getHeight()-this.menuCloud.getHeight()))/((float)(y+maxAlto)));
					zoom=(float)zoom+((float)(zoom/tamPasoEscala)*((float)escala));
					tamPasoEscala+=contZoom;
					//System.out.println("ajustando positivo"+contZoom);
					contZoom+=1;
					Ajusta=true;
					this.repaint();
				}
			}
		else if((y+maxAlto)>(this.getHeight()-this.menuCloud.getHeight()) )
			{
			if(((y+maxAlto)-(this.getHeight()-this.menuCloud.getHeight()))>((this.getHeight()-this.menuCloud.getHeight())*(float)0.081))
				{
					float tamPasoEscala=(float)Math.abs((this.getHeight()-this.menuCloud.getHeight())-(y+maxAlto));
					tamPasoEscala/=(float)4.2;
					escala=(float)(((float)this.getHeight())/((float)(y+maxAlto)));
					zoom=(float)zoom-((float)(zoom/tamPasoEscala)*((float)escala));
					tamPasoEscala+=contZoom;
					contZoom+=1;
					//System.out.println("ajustando"+contZoom);
					Ajusta=true;
					this.repaint();
				}
			}
		else
			Ajusta=false;
	}
	public void setAlto(int alto){
		this.alto = alto;
	}
	public void setAncho(int ancho){
		this.ancho = ancho;
	}
	public int getId(){
		return es.usal.bicoverlapper.kernel.Configuration.CLOUD_ID;
	}
	public void actionPerformed(ActionEvent e){
		String comando=e.getActionCommand();
		if (comando.equalsIgnoreCase(panelMenu.DESCRIPTION)){
			setEnabledButon();
			if (!this.myColor.equals(this.sesion.getDataLayer().getSelectionColor()))
				this.colorSeleccion=this.myColor;
			menuCloud.descButton.setEnabled(false);
			nameC=panelMenu.DESCRIPTION;
			newSelection=true;
			//ajusta=true;
			this.actualizar();
			this.repaint();
		}else if(comando.equalsIgnoreCase(panelMenu.GO_TERMS)){
			setEnabledButon();
			if (!this.myColor.equals(this.sesion.getDataLayer().getSelectedTupleColor()))
				this.colorSeleccion=this.myColor;
			menuCloud.goButton.setEnabled(false);
			nameC=panelMenu.GO_TERMS;
			newSelection=true;
			//ajusta=true;
			this.actualizar();
			this.repaint();
		}
	}
	public void stateChanged(ChangeEvent i){
		zoom=(float)(menuCloud.sliderZoom.getValue()/(float)4.0);
		//System.out.println("Z: "+zoom);
		this.repaint();
	}
	public void mouseClicked(MouseEvent e) 
		{				
		}
	public void mouseEntered(MouseEvent e) 
		{
		/*X=e.getX();
		Y=e.getY();
		//System.out.println("(X"+X+", "+Y+")");
		Iterator it=words.keySet().iterator();
		while(it.hasNext())
			{
			
			String w=(String)it.next();
			if(w.length()>0 )//&& words.get(w).x!=0.0 && words.get(w).y!=0.0)
				{
				//System.out.println("("+words.get(w).x+", "+words.get(w).y+")");
				TextLayout T=(TextLayout)words.get(w).text;			
				if(T!=null)
					{		
					//if (newSelection)
						//words.get(w).setColor(PanelWords.this.colorSeleccion);	
					//System.out.println("T.with"+rW.getWidth());
					if(X<(T.getBounds().getWidth()+words.get(w).x) 	&& X>words.get(w).x &&  
					   Y<(T.getBounds().getHeight()+words.get(w).y)	&& Y>words.get(w).y)
						{
						if(nameSelected.contains(w))
							{
							//nameSelected.remove(w);
							words.get(w).setColor(PanelWords.this.colorSeleccion);
							T.getBounds().setFrame(T.getBounds().getX(), T.getBounds().getY(), 20, 20);
							System.out.println("W: "+nameSelected.toString());
							}
						else
							{
							//nameSelected.add(w);
							words.get(w).setColor(colorNameSelected);
							System.out.println("W: "+nameSelected.toString());
							}
						existNameSelected=true;
						this.repaint();
						}
					else
						{
						existNameSelected=false;
						}
					}
				//else
				//	System.out.println("Esta vacio el TextLayout");
				}
			}*/
		}
	public void mouseExited(MouseEvent e) {
		
	}
	public void mousePressed(MouseEvent e) 
		{
		//if (!this.myColor.equals(this.sesion.getDataLayer().getSelectedTupleColor())){
		//	this.colorSeleccion=this.myColor;
		//	this.sesion.getDataLayer().setSelectionColor(this.colorSeleccion);
		}
	
		
	public void mouseReleased(MouseEvent e) 
		{
		if(!e.isControlDown())
			{
			nameSelected=new ArrayList<String>();
			newSelection=true;
			}
		else
			newSelection=false;
		
		X=e.getX();
		Y=e.getY();
		Iterator<String> itw=words.keySet().iterator();
		while(itw.hasNext())
			{
			String w=itw.next();
			if(w.length()>0 )//&& words.get(w).x!=0.0 && words.get(w).y!=0.0)
				{
				TextLayout T=(TextLayout)words.get(w).text;			
				if(T!=null)
					{	
					if (newSelection)
						words.get(w).setColor(this.colorSeleccion);
					//System.out.println("T.with"+rW.getWidth());
					if(X<(T.getBounds().getWidth()+words.get(w).x) 	&& X>words.get(w).x &&  
					   Y<(words.get(w).y)	&& Y>(words.get(w).y-T.getBounds().getHeight()))
						{
						if(nameSelected.contains(w))
							{
							nameSelected.remove(w);
							words.get(w).setColor(this.colorSeleccion);
							System.out.println("W: "+nameSelected.toString());
							}
						else
							{
							nameSelected.add(w);
							words.get(w).setColor(colorNameSelected);
							System.out.println("W: "+nameSelected.toString());
							}
						this.actualizar();
						break;
						//this.repaint();
						}
					}				
				}
			}
		}
	
	
	private class Word
	{
		TextLayout text;	
		TextLayout contText;
		String label;
		double x;
		double y;
		int cont;
		Color color;
		
		public Word(TextLayout text, double x, double y, int cont,Color c)
			{
			this.text=text;			
			this.x=x;
			this.y=y;
			this.cont=cont;	
			this.color=c;
			}
		public int getCont()
			{
			return this.cont;
			}
		public double getX()
			{
			return this.x;
			}
		public double getY()
			{
			return this.y;
			}
		public TextLayout  getText()
			{
			return this.text;
			}
		public void setText(TextLayout text) {
			this.text = text;
		}
		public void setX(double x) {
			this.x = x;
		}
		public void setY(double y) {
			this.y = y;
		}
		public void setCont(int cont) {
			this.cont = cont;
		}
		public Color getColor() {
			return color;
		}
		public void setColor(Color color) {
			this.color = color;
		}
		public TextLayout getContText() {
			return contText;
		}
		public void setContText(TextLayout contText) {
			this.contText = contText;
		}
		
	}
	}
class panelMenu extends JPanel{
	public static final String DESCRIPTION="Description";
	public static final String GO_TERMS="GO Terms";
	//public static final String DISTRIBUTOR="Distributor";
	static final int ZOOM_MIN = 10;
	static final int ZOOM_MAX = 1600;
	static final int ZOOM_INIT = 15;
	
	JButton descButton= new JButton(DESCRIPTION);
	JButton goButton= new JButton(GO_TERMS);
	//JButton botonDistributor= new JButton(DISTRIBUTOR);
	JSlider sliderZoom= new JSlider(JSlider.HORIZONTAL,ZOOM_MIN, ZOOM_MAX, ZOOM_INIT);
	
	public  panelMenu(){
		descButton.setActionCommand(DESCRIPTION);
		goButton.setActionCommand(GO_TERMS);
	//	botonDistributor.setActionCommand(DISTRIBUTOR);
		
		sliderZoom.setMajorTickSpacing(40);
		sliderZoom.setMinorTickSpacing(1);
		sliderZoom.setPaintTicks(true);
		//sliderZoom.setPaintLabels(true);

		this.setLayout(new GridLayout(1,4));
		this.add(goButton);
		this.add(descButton);
		//this.add(botonDistributor);
		this.add(sliderZoom);
	}
}