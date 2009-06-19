package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import es.usal.bicoverlapper.data.GeneRequester;
import es.usal.bicoverlapper.data.NCBIReader;
import es.usal.bicoverlapper.data.GOTerm;
import es.usal.bicoverlapper.data.GeneAnnotation;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.kernel.DiagramWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.BiclusterSelection;

import es.usal.bicoverlapper.utils.CustomColor;

public class WordCloudDiagram extends Diagram implements ActionListener,ChangeListener,MouseListener, GeneRequester
	{
	private static final long serialVersionUID = 1L;
	
	// atributos del panel del diagrama
	private Session sesion;
	//private MultidimensionalData datos;
	private boolean atributosIniciados = false, configurando = false, diagramaPintado = false;
		
	// definicion de margenes del diagrama
	private float zoom =(float) 100;
	private final int margenDer = 40;
	private final int margenIzq = 40;
	private final int margenSup = 25;
	private final int margenInf = 40;
	private final int margenDiagrama = 10; // porcentaje de exceso en intervalo de representacion del diagrama
	
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
	
	private List<GOTerm> got=null;
	private ArrayList<GeneAnnotation> annot=null;
	private boolean textChanged=false; //to not repeat hypergeometric tests and other R calls
	private boolean innerCall=false;//to differentiate updates from combo boxes from internal updates
	
	//Atributos propios de la clase --------------------
	private float maxFontSize=(float)400;
	private float minFontSize=(float) 1.5;	
	private int maxCont=0;
	private int contChar=0;
	private int maxWord=0;
	private double Y=0;
	private double  X=0;
	private int maxChar=0;
	private String nameC=MenuPanel.GO_TERMS;
	private ArrayList<String> nameSelected;
	private Color colorNameSelected;
	private Color myColor;
	private MenuPanel menuCloud=null;
	private TreeMap <String,Word> words;
	private boolean newSelection=true;

	private int contWord;
	
	private int contZoom=0;
	private boolean Ajusta=true;
	private boolean Enought=true;
	private List<String> sortedWords=null;

	private boolean doNOTupdate=false;
	
	
	public WordCloudDiagram(Session sesion, Dimension dim)
		{
		super(new BorderLayout());//
		int num = sesion.getNumWordClouds();
		this.setName("Keyword Cloud "+num);
		this.sesion = sesion;
		//this.datos = sesion.getData();
		int alto = (int)dim.getHeight();
		int ancho = (int)dim.getWidth();
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);
		menuCloud=new MenuPanel();
		CustomColor c=new CustomColor(200,0,0,200);
		
		this.colorNameSelected=new Color(c.getR(), c.getG(), c.getB());
		this.myColor=new Color(c.getR(), c.getG(), c.getB(),c.getA());
		this.colorSeleccion=new Color(c.getR(), c.getG(), c.getB(),c.getA());
		this.setBackground(Color.WHITE);
		
		this.add(menuCloud,BorderLayout.SOUTH);
		this.addMouseListener(this);
		nameSelected=new ArrayList<String>();
		Ajusta=true;

		words=new TreeMap<String,Word>();
		update();
		}
	
	private void setEnabledButon(){
	/*	menuCloud.descButton.setEnabled(true);
		menuCloud.goButton.setEnabled(true);*/
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
		maxChar=0;
		contChar=0;
		maxWord=0;
		contWord=0;
		maxCont=0;
		Ajusta=true;
		Enought=true;
		words.clear();
		colorSeleccion=sesion.getSelectionColor();
		
		if(innerCall==false)	
			{
			got=null;
			annot=null;
			textChanged=true;
			}
		//TODO: digraphs, colors
		if(this.sesion.getSelectedBicluster()!=null && sesion.getSelectedGenesBicluster()!=null && sesion.getSelectedGenesBicluster().size()>0)
			{
			if(!innerCall || annot==null || (got==null && menuCloud.size.getSelectedIndex()==MenuPanel.PVALUES)) 
				{
				Point p=new Point(0,0);
				if(this.getParent()!=null) p=this.sesion.getDiagramWindow(this.getName()).getLocation();
				if(menuCloud.size.getSelectedIndex()==MenuPanel.PVALUES) 
					{
					String ont="";
					switch(menuCloud.ontology.getSelectedIndex())
						{
						case(MenuPanel.ALL):
							doNOTupdate=true;
							menuCloud.ontology.setSelectedIndex(MenuPanel.BP);//NOTA: Si hago esto así me hace doble actualización majo
						case(MenuPanel.BP):
							ont="BP";
							break;
						case(MenuPanel.MF):
							ont="MF";
						break;
						case(MenuPanel.CC):
							ont="CC";
							break;
						}
					System.out.println("Llamando a test hipergeométrico");
					this.sesion.getMicroarrayData().getGOTermsHypergeometric(sesion.getSelectedGenesBicluster(), this, p, ont);
					}
				else		
					this.sesion.getMicroarrayData().getGeneAnnotations(sesion.getSelectedGenesBicluster(), this, p);
				return;
				}
			else
				{//Words (annot or got) do not change, just the sizes and combination
				addWords();
				}
			}
		
	}
	
public void addWords()
	{
	switch(this.menuCloud.text.getSelectedIndex())
		{
		case MenuPanel.GO_TERM://----------------------------------------------
			switch(this.menuCloud.size.getSelectedIndex())
			{
			case MenuPanel.GENES:
				for(GeneAnnotation a : annot)
				{
				List<String> added=new ArrayList<String>();
				if(a.goTerms!=null)
					{
					for(GOTerm go : a.goTerms)
						{
						if(go!=null)
							{
							boolean add=true;
							switch(menuCloud.ontology.getSelectedIndex())
								{
								case MenuPanel.BP:
									if(!go.ontology.equals("BP"))	add=false;
									break;
								case MenuPanel.CC:
									if(!go.ontology.equals("CC"))	add=false;
									break;
								case MenuPanel.MF:
									if(!go.ontology.equals("MF"))	add=false;
									break;
								}
							if(add)
								{
								String desc=go.term;
								if(!added.contains(desc))
									{
									splitAndAdd(desc, 1, this.colorSeleccion, true);
									added.add(desc);
									}
								}
			        		}
						}
					}
				}
				break;
			case MenuPanel.OCCURRENCES:
				for(GeneAnnotation a : annot)
				{
				if(a.goTerms!=null)
					{
					for(GOTerm go : a.goTerms)
						{
						if(go!=null)
							{
							boolean add=true;
							switch(menuCloud.ontology.getSelectedIndex())
								{
								case MenuPanel.BP:
									if(!go.ontology.equals("BP"))	add=false;
									break;
								case MenuPanel.CC:
									if(!go.ontology.equals("CC"))	add=false;
									break;
								case MenuPanel.MF:
									if(!go.ontology.equals("MF"))	add=false;
									break;
								}
							if(add)
								{
								String desc=go.term;
								int oc=go.occurences;
								splitAndAdd(desc, oc, this.colorSeleccion, false);
								}
			        		}
						}
					}
				}
				break;
	
			case MenuPanel.PVALUES:
				double min=got.get(0).pvalue;
				for(GOTerm go: got)
					if(go.pvalue<min)	min=go.pvalue;
				
				System.out.println("Hay "+got.size()+" terms");
				int cont=0;
				for(GOTerm go:got)
					{
					if(go!=null)
						{
						String desc=go.term;
						int oc=(int)((1/(go.pvalue/min))/min);//TODO: en funcion de pvalue, formatear número y tamaño en concordancia
						System.out.println(cont++);
						if(desc.equals("establishment of localization in cell"))
							System.out.println("Split and add de "+desc);
						splitAndAdd(desc, oc, this.colorSeleccion, false);
		        		}
					}
				System.out.println("Finished!");
				break;
				}
			break;
		case MenuPanel.DEFINITION://---------------------------------------------
		default:
			for(int i=0;i<annot.size();i++)
			{
			GeneAnnotation ga=annot.get(i);
			if(ga!=null)
				{
				if(ga.description!=null)
					{
					String desc=ga.description;
					int oc=1;
					switch(this.menuCloud.size.getSelectedIndex())
						{
						case MenuPanel.OCCURRENCES:
							splitAndAdd(desc, oc, this.colorSeleccion, false);
							break;
						case MenuPanel.GENES:
						default:
							splitAndAdd(desc, oc, this.colorSeleccion, true);
							break;
						}
						
					}
				}
			}
			break;
		}
	
	synchronized(this){
	textChanged=true;
	System.out.println("Número de hilos: "+Thread.getAllStackTraces().size());
	if(this.getGraphics()!=null)	
		this.paintComponent(this.getGraphics());
	menuCloud.setVisible(true);
	menuCloud.repaint();
	
	innerCall=false;
	}
	}
public void receiveGOTerms(ArrayList<GOTerm> goterms)
	{
	this.got=goterms;
	if(got==null)
	      JOptionPane.showMessageDialog(this, "No relevant GO terms on hypergeometric test for onthology: "+(String)(menuCloud.ontology.getSelectedItem()), "Warning",JOptionPane.WARNING_MESSAGE);
	addWords();
	}
public void receiveGeneAnnotations(ArrayList<GeneAnnotation> annot)
	{
	this.annot=annot;
	addWords();
	}
	
/**
 * Separates the text desc in words (use blank space as separator), and adds each word with color
 * c. Usually oc is 1, but if greater, it counts each word as this number of occurences.
 * Finally, if unique is true, it adds each word just one, even if it appears several times.
 * @param desc
 * @param oc
 * @param c
 * @param unique
 */
public void splitAndAdd(String desc, int oc, Color c, boolean unique)
	{
	String[] dw=null;
	ArrayList<String> added=new ArrayList<String>();
	switch(this.menuCloud.split.getSelectedIndex())
		{
		case 0://1-word
			dw=desc.split(" ");
			for(int j=0;j<dw.length;j++)
				{
				dw[j]=dw[j].replace("(", "").replace(")", "").trim().toLowerCase();
				if(!unique || !added.contains(dw[j]))
					addWord(dw[j], oc, c);
				added.add(dw[j]);
				}
			break;
		case 1://2-word
			dw=desc.split(" ");
			for(int j=0;j<dw.length;j++)      			
				{
				if(j<dw.length-1)	
					{
					String diword=(dw[j]+" "+dw[j+1]).trim();
					if(!unique || !added.contains(diword))
						addWord(diword, oc, c);
					added.add(diword);
					}
				}
			break;
		case 2://complete
			addWord(desc, oc, c);
			break;
		}
	return;
	}
	
	public void addWord(String w, int oc, Color colorW)
		{
		if(!valid(w))	return;
		if(words!=null && words.containsKey(w))
			{
			Word nW=(Word)words.get(w);
			//colorW=getColorW(w);
			nW.setCont(nW.cont+oc);
			if(nW.cont>maxCont) maxCont=nW.cont;
			}
		else
			{
			//colorW=getColorW(w);
			words.put(w, new Word(null,0,0,new Integer(oc),colorW));
			contChar+=w.length();
			contWord+=oc;
			}
		maxWord+=oc;
		maxChar+=w.length();
		}
	
	public boolean valid(String w)
		{
		if(w.length()<2)	return false;
		if(w.length()>4)	return true;
		if(w.equals("of") || w.equals("in") || w.equals("the") || w.equals("and"))	return false;
		if(w.equals("or") || w.equals("on") || w.equals("at") || w.equals("for"))	return false;
		if(w.equals("is") || w.equals("as") || w.equals("an") || w.equals("to"))	return false;
		if(w.equals("with") || w.equals("some") || w.equals("also") || w.equals("that"))	return false;
		if(w.equals("by") || w.equals("into") || w.equals("from") || w.equals("has"))	return false;
		if(w.equals("have") || w.equals("be") || w.equals("which") || w.equals("may"))	return false;
		return true;
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
		this.getWindow().setContentPane(this);
		this.getWindow().pack();
		}
	
	//	Método que se activa cuando cambia la selecciòn en una ventana
	//TODO: It requires that ALL the genes have retrieved the annotations, which is computationally huge
	//(when using the NCBI-QuickGO characteristic). By now, unavailable
	public void actualizar() 
		{
		/*
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
						g.add(i);
						System.out.println("Añadiendo gen "+ga.name+" con id "+i);
						break;
						}
					}
				}
			}
		
		for(int i=0;i<sesion.getMicroarrayData().getNumConditions();i++)	c.add(i);		
		es.usal.bicoverlapper.kernel.BiclusterSelection bs=new BiclusterSelection(g,c);
		sesion.setSelectedBiclusters(bs, "loud");
		update();		*/
		}
	public void run()
		{
		this.getWindow().setVisible(true); // show the window
		//TODO: Lo necesario para que empiece a correr la visualización (p. ej. en prefuse, los Visualization.run())
		}
	private void drawFondo(Graphics2D g2) {
		g2.setPaint(paleta[colorFondo]);
	    Rectangle2D.Double fondo =
			new Rectangle2D.Double(0,0,this.getWidth(),this.getHeight());
		g2.fill(fondo);
		g2.draw(fondo);			
	}

	private float getProportion(String w,int c,int ww,int wh){
		float hipo,size,cMaxProportion;
		hipo=(float)(Math.sqrt((this.getHeight()*this.getWidth())+((this.getHeight()-this.menuCloud.getHeight())*(this.getHeight()-this.menuCloud.getHeight()))));
		cMaxProportion=(float)(hipo)/(float)(Math.sqrt(maxChar));				
		minFontSize=(float)(((float)contWord/(float)maxWord));//*(float)0.15);//(-0.1)
		maxFontSize=(float)(float)zoom*(float)(cMaxProportion);//*((float)hipo)
		float newMax=(float)maxFontSize;
		float newMin=(float)minFontSize;
		float max=(float)maxCont;
		float min=(float)1;
		float key=(float)c;		
		size=(float)getInterpolation(key,max,min,newMax,newMin);
		return size;
		}
	
	private float getInterpolation(float key,float maxKey,float minKey,float newMax,float newMin)
	{
		return (float)(key-minKey)*(newMax-newMin+1)/(maxKey-minKey)+newMin;
	}
	
	public void drawWords(Graphics2D g2)
	{
	drawFondo(g2);
	RenderingHints qualityHints = new RenderingHints(null);
	qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	g2.setRenderingHints(qualityHints);

	Iterator<Word> it=words.values().iterator();
	if(sortedWords!=null && sortedWords.size()==words.size())
		for(int i=0;i<words.size();i++)
			{			
			Word w=words.get(sortedWords.get(i));
			g2.setPaint(w.getColor());
			if(w==null)System.out.println("w es null");
			if(w.getText()==null)System.out.println("wt es null");
			if(w.getText().getBounds()==null)System.out.println("wtb es null");
			((TextLayout)w.getText()).draw(g2,(float)(w.getX()),(float)(w.getY()));
			}
	else
		while(it.hasNext())
		{			
		Word w=it.next();
		if(w.getText()==null)	return;//Words still not selected/set up
		g2.setPaint(w.getColor());
		
		((TextLayout)w.getText()).draw(g2,(float)(w.getX()),(float)(w.getY()));//+maxAlto
		}
	}
	
	
	
	public void resize()
		{
		//System.out.println(this.getHeight()+" "+this.getWidth());
		textChanged=true;
		}
	
	public String formatNumber(double n)
		{
		String cad="";
		int exp=0;
		while(n*10<1)
			{
			n=n*10;
			exp--;
			}
		if(exp<0)	cad=Math.round(n*10)+"e"+(exp-1);
		else		cad=""+(Math.round(n));
		return cad;
		}
	
	public void setWords(Graphics2D g2)
		{
		double y=0;
		double x=0;
		double altoTexto =0;
		double anchoTexto=0;
		double maxAlto=0;
		double maxAncho=0;
		if(g2==null)	return;
		FontRenderContext frc=g2.getFontRenderContext();
		
		//0) Alphabetic sort of words
		if(words!=null && words.size()>0)	
			{
			String[] tal=words.keySet().toArray(new String[0]);
			sortedWords=Arrays.asList(tal);
			Collections.sort(sortedWords);
			}
		if(sortedWords==null)	return;
		//1) Primera vuelta, chequeamos el tamaño
		for(String w: sortedWords)
			{			
			Word nW=words.get(w);
			if(w.length()>0 && nW!=null)
				{
				String wc="";
				if(this.menuCloud.size.getSelectedIndex()==MenuPanel.PVALUES)
						wc=" "+w.concat("("+formatNumber(1.0/nW.cont))+")";
				else	wc=" "+w.concat("("+nW.cont+") ");
				float num=nW.cont;
				
				Font f=g2.getFont().deriveFont((float)num);
				TextLayout texto = new TextLayout(wc,f,frc);
				
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
				if(maxAncho<anchoTexto)	
					maxAncho=anchoTexto;
				nW.setX(x);
				nW.setY(y);
				nW.setText(texto);
				if(x+anchoTexto<=this.getWidth())	
					x+=anchoTexto;
				if(anchoTexto>this.getWidth())
					{
					y+=altoTexto;
					maxAlto=0;
					}
				}		
			}
		y+=maxAlto;//Si no el último salto no se tiene en cuenta.
		//2) Determinamos el factor de escala
		//double scale=Math.min(Math.floor(this.getHeight()/y), Math.floor(this.getWidth()/maxAncho));
		double scale=Math.min(this.getHeight()/y, this.getWidth()/maxAncho);
		
		//3) Segunda vuelta vuelta, con los tamaños adecuados
		// como puede haber saltos de línea por el escalado, lo hacemos en un bucle donde vamos disminuyendo poco a poco la escala
		boolean end=false;
		maxAncho=0;
		do{
		x=0;
		y=0;
		ArrayList <Word> wordsInLine=new ArrayList<Word>();
		for(String w : sortedWords)
			{	
			Word nW=words.get(w);
			
			if(w.length()>0 && nW!=null)
				{
				String wc="";
				if(this.menuCloud.size.getSelectedIndex()==MenuPanel.PVALUES)
					wc=" "+w.concat("("+formatNumber(1.0/nW.cont))+")";
				else	wc=" "+w.concat("("+nW.cont+") ");
				float num=nW.cont;
				Font f=g2.getFont().deriveFont((float)(num*scale));
				TextLayout texto = new TextLayout(wc,f,frc);
				
				altoTexto = texto.getBounds().getHeight();
				anchoTexto = texto.getBounds().getWidth();
				if(x+anchoTexto>this.getWidth())	//cambio de línea
					{
					//Sumamos a todas las de la línea anterior el maxAlto de esa línea
					for(int k=0;k<wordsInLine.size();k++)
						{
						Word w2=wordsInLine.get(k);
						w2.setY(w2.getY()+.75*maxAlto);
						}
					wordsInLine.clear();
					if(maxAncho<x)	maxAncho=x;
					x=0;	
					y+=maxAlto; 
					maxAlto=altoTexto;
					}
				if(maxAlto<altoTexto)	
					maxAlto=altoTexto;
				nW.setX(x);
				nW.setY(y);//debería ser directamente +maxAlto, pero no queda bien y no sé por qué. 
									//luego está el tema de que cuando se está viendo una, si luego hay otra mayor, no va a tener en cuenta el maxAlto total.
										//habría que ir guardando todas las que van en una línea y luego sumarlas a todas el maxAlto, durante el cambio de línea
				nW.setText(texto);
				nW.label=w;
				wordsInLine.add(nW);
				x+=anchoTexto;
				}		
			}
		y+=maxAlto;//si no la última línea no se tiene en cuenta
		for(int i=0;i<wordsInLine.size();i++)//y recolocación por el tamaño máximo tb en la última línea
			{
			Word w2=wordsInLine.get(i);
			w2.setY(w2.getY()+.75*maxAlto);
			}
		
		if(y<=this.getHeight()-menuCloud.getBounds().height && x<=this.getWidth())	
			{
			end=true;
			}
		else	
			{
			if(scale>0.2)	scale-=0.1;
			else			scale/=10;
			}
		}while(!end);
		
		textChanged=false;
		}

	public synchronized void paintComponent(Graphics g) 
		{
		if(textChanged || innerCall)	
			setWords((Graphics2D)g);
		drawWords((Graphics2D)g);				
		}
	
	public int getId(){
		return es.usal.bicoverlapper.kernel.Configuration.CLOUD_ID;
	}
	public void actionPerformed(ActionEvent e){
		String comando=e.getActionCommand();
		if (comando.equalsIgnoreCase(MenuPanel.DESCRIPTION)){
			setEnabledButon();
			if (!this.myColor.equals(sesion.getSelectionColor()))
				this.colorSeleccion=this.myColor;
			nameC=MenuPanel.DESCRIPTION;
			newSelection=true;
			this.actualizar();
			this.repaint();
		}else if(comando.equalsIgnoreCase(MenuPanel.GO_TERMS)){
			setEnabledButon();
			if (!this.myColor.equals(sesion.getSelectionColor()))
				this.colorSeleccion=this.myColor;
			//menuCloud.goButton.setEnabled(false);
			nameC=MenuPanel.GO_TERMS;
			newSelection=true;
			//ajusta=true;
			this.actualizar();
			this.repaint();
		}
	}
	public void stateChanged(ChangeEvent i){
		//zoom=(float)(menuCloud.sliderZoom.getValue()/(float)4.0);
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
	
	private class MenuPanel extends JPanel implements ActionListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = -576605444831473458L;
		public static final String DESCRIPTION="Description";
		public static final String GO_TERMS="GO Terms";
		//public static final String DISTRIBUTOR="Distributor";
		static final int ZOOM_MIN = 10;
		static final int ZOOM_MAX = 1600;
		static final int ZOOM_INIT = 15;
		
		static final String textLabel=" Text: ";
		static final String splitLabel=" Split: ";
		static final String sizeLabel=" Size: ";
		static final String ontoLabel=" Ontology: ";
		JLabel tl=new JLabel(textLabel);
		JLabel spl=new JLabel(splitLabel);
		JLabel sil=new JLabel(sizeLabel);
		JLabel onl=new JLabel(ontoLabel);
		
		String antName="";
		String[] texts;
		String[] splits;
		String[] sizes;
		String[] ontologies;
		JComboBox text, split, size, ontology;
		
		static final int DEFINITION=0;
		static final int GO_TERM=1;
		static final int WORD1=0;
		static final int WORD2=1;
		static final int COMLETE=2;
		static final int GENES=0;
		static final int PVALUES=1;
		static final int OCCURRENCES=2;//Deprecated
		static final int ALL=0;
		static final int BP=1;
		static final int MF=2;
		static final int CC=3;
		
		
		public  MenuPanel(){
			if(sesion.getMicroarrayData().searchByR==true)	
				{
				texts=new String[]{"definition", "go term"};
				splits=new String[]{"1 word", "2 words"};
		//		sizes=new String[]{"genes", "occurences"};
				sizes=new String[]{"genes"};
				ontologies=new String[]{"all", "bp", "mf", "cc"};
				}
			else											
				{
				texts=new String[]{"definition", "go term"};
				splits=new String[]{"1 word", "2 words"};
				//sizes=new String[]{"genes", "occurences"};
				sizes=new String[]{"genes"};
				ontologies=new String[]{"all", "bp", "mf", "cc"};
				}

			text=new JComboBox(texts);
			split=new JComboBox(splits);
			size=new JComboBox(sizes);
			ontology=new JComboBox(ontologies);
			text.setSelectedIndex(0);
			split.setSelectedIndex(0);
			size.setSelectedIndex(0);
			ontology.setSelectedIndex(0);
			text.addActionListener(this);
			split.addActionListener(this);
			size.addActionListener(this);
			ontology.addActionListener(this);
			ontology.setEnabled(false);
			
			this.add(tl);
			this.add(text);
			this.add(spl);
			this.add(split);
			this.add(sil);
			this.add(size);
			this.add(onl);
			this.add(ontology);
		}
		
		public void actionPerformed(ActionEvent e)
			{
			if(doNOTupdate)	{doNOTupdate=false; return;}
			JComboBox cb = (JComboBox)e.getSource();
			if(cb==text)
				{
				if(!antName.equals((String)cb.getSelectedItem()))	textChanged=true;
				else		
					textChanged=false;
				}
			else			
				textChanged=false;
			
			if(cb==ontology && size.getSelectedIndex()==PVALUES)	got=null;
			innerCall=true;
			if(cb==text && textChanged)
				{
				if( ((String)cb.getSelectedItem()).equals("go term")	)
					{
					split.addItem("complete");
					size.addItem("p-value");
					ontology.setEnabled(true);
					}
				else
					{
					split.removeItem("complete");
					size.removeItem("p-value");
					ontology.setEnabled(false);
					}
				}
	        antName = (String)cb.getSelectedItem();
	        ((WordCloudDiagram)this.getParent()).update();
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