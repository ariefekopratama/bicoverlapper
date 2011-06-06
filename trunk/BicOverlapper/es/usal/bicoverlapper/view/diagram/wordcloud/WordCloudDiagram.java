package es.usal.bicoverlapper.view.diagram.wordcloud;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import prefuse.Constants;
import prefuse.action.ActionList;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

import es.usal.bicoverlapper.controller.kernel.Selection;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.controller.manager.configurationManager.ConfigurationMenuManager;
import es.usal.bicoverlapper.controller.util.ArrayUtils;
import es.usal.bicoverlapper.controller.util.Translator;
import es.usal.bicoverlapper.model.gene.GeneAnnotation;
import es.usal.bicoverlapper.model.gene.GeneRequester;
import es.usal.bicoverlapper.model.goterm.GOTerm;

import es.usal.bicoverlapper.utils.color.CustomColor;
import es.usal.bicoverlapper.view.configuration.panel.HeatmapParameterConfigurationPanel;
import es.usal.bicoverlapper.view.configuration.panel.WordCloudParameterConfigurationPanel;
import es.usal.bicoverlapper.view.diagram.Diagram;
import es.usal.bicoverlapper.view.diagram.heatmap.ExpressionColorAction;
import es.usal.bicoverlapper.view.diagram.heatmap.HeatmapDiagram;

public class WordCloudDiagram extends Diagram implements ChangeListener,MouseListener, GeneRequester
	{
	private static final long serialVersionUID = 1L;
	
	// atributos del panel del diagrama
	private Session sesion;
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
	private static final int colorFondo = 2;
	private Color[] paleta = {Color.BLUE, Color.RED, Color.WHITE};
	private String[] textoLabel = {"Word", "Selected word","Background"};
	private JTextField[] muestraColor = new JTextField[paleta.length];
	
	public List<GOTerm> got=null;
	private ArrayList<GeneAnnotation> annot=null;
	public boolean textChanged=false; //to not repeat hypergeometric tests and other R calls
	public boolean innerCall=false;//to differentiate updates from combo boxes from internal updates
	
	//Atributos propios de la clase --------------------
	private float maxFontSize=(float)400;
	private float minFontSize=(float) 1.5;	
	private double maxCont=0;
	private int contChar=0;
	private int maxWord=0;
	private double Y=0;
	private double  X=0;
	private int maxChar=0;
	private String nameC=WordCloudParameterConfigurationPanel.GO_TERMS;
	private ArrayList<String> nameSelected;
	//private Color colorNameSelected;
	public WordCloudParameterConfigurationPanel menuCloud=null;
	

	private TreeMap <String,Word> words;
	private boolean newSelection=true;

	private int contWord;
	
	private int contZoom=0;
	private boolean Ajusta=true;
	private boolean Enought=true;
	private List<String> sortedWords=null;

	public boolean doNOTupdate=false;

	private JLabel progress;
	
	
	public WordCloudDiagram(Session sesion, Dimension dim)
		{
		super(new BorderLayout());//
		int num = sesion.getNumWordClouds();
		this.setName("Keyword Cloud "+num);
		this.sesion = sesion;
		int alto = (int)dim.getHeight();
		int ancho = (int)dim.getWidth();
		this.setLayout(null);
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);
		//menuCloud=new MenuPanel();
		
		menuCloud=new WordCloudParameterConfigurationPanel(this);
		progress=new JLabel("");
		progress.setForeground(Color.LIGHT_GRAY);
		progress.setFont(new Font("Arial", Font.PLAIN, 10));
		progress.setBounds(this.getWidth()-32, this.getHeight()-12, 30, 10);
		
		
		this.colorSeleccion=paleta[colorEtiquetaVar];
		this.setBackground(paleta[colorFondo]);
		
		this.add(progress);
		this.addMouseListener(this);
		nameSelected=new ArrayList<String>();
		Ajusta=true;

		words=new TreeMap<String,Word>();
		update();
		}
	
	public WordCloudParameterConfigurationPanel getMenuCloud() {
		return menuCloud;
	}

	public void setMenuCloud(WordCloudParameterConfigurationPanel menuCloud) {
		this.menuCloud = menuCloud;
	}
	
	private void setEnabledButon(){
	}
	
	private Color getColorW(String w){		
		Color colorW=(Color)this.colorSeleccion;
		if (nameSelected.size()>0)
		{
			for (int j=0;j<nameSelected.size();j++)
				{
				if(nameSelected.get(j).equalsIgnoreCase(w)){
					colorW=paleta[colorVarSelec];
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
		
		if(innerCall==false)	
			{
			got=null;
			annot=null;
			textChanged=true;
			}
		//TODO: digraphs, colors
		if(this.sesion.getSelectedBicluster()!=null && sesion.getSelectedGenesBicluster()!=null && sesion.getSelectedGenesBicluster().size()>0)
			{
			if(!innerCall || annot==null || (got==null && menuCloud.size.getSelectedIndex()==WordCloudParameterConfigurationPanel.PVALUES)) 
				{
				Point p=new Point(0,0);
				if(this.getParent()!=null) p=this.sesion.getDiagramWindow(this.getName()).getLocation();
				if(menuCloud.size.getSelectedIndex()==WordCloudParameterConfigurationPanel.PVALUES) 
					{
					String ont="";
					switch(menuCloud.ontology.getSelectedIndex())
						{
						case(WordCloudParameterConfigurationPanel.ALL):
							doNOTupdate=true;
							menuCloud.ontology.setSelectedIndex(WordCloudParameterConfigurationPanel.BP);//NOTA: Si hago esto así me hace doble actualización majo
						case(WordCloudParameterConfigurationPanel.BP):
							ont="BP";
							break;
						case(WordCloudParameterConfigurationPanel.MF):
							ont="MF";
						break;
						case(WordCloudParameterConfigurationPanel.CC):
							ont="CC";
							break;
						}
					if(this.sesion.getMicroarrayData().isBioMaRt)
						{
						System.out.println("No annotation package especified, trying with "+sesion.getMicroarrayData().getAnnotationPackage());
						sesion.analysis.loadRLibrary(sesion.getMicroarrayData().getAnnotationPackage());
						}
					this.sesion.getMicroarrayData().getGOTermsHypergeometric(sesion.getSelectedGenesBicluster(), this, p, ont);
					}
				else
					{this.sesion.getMicroarrayData().getGeneAnnotations(ArrayUtils.toIntArray(sesion.getSelectedGenesBicluster()), this, true, progress, null, true);}
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
	long t1=System.currentTimeMillis();
	int cont=0;
	if(annot==null)	{System.err.println("WordCloudDiagram: addWords no annotations found"); return;}
	
	switch(this.menuCloud.text.getSelectedIndex())
		{
		case WordCloudParameterConfigurationPanel.GO_TERM://----------------------------------------------
			switch(this.menuCloud.size.getSelectedIndex())
			{
			case WordCloudParameterConfigurationPanel.GENES:
				cont=0;
				for(GeneAnnotation a : annot)
				{
				ArrayList<String> added=new ArrayList<String>();
				if(a.goTerms!=null)
					{
					if(a.goTerms.size()>0)	cont++;
					for(GOTerm go : a.goTerms)
						{
						if(go!=null)
							{
							boolean add=true;
							switch(menuCloud.ontology.getSelectedIndex())
								{
								case WordCloudParameterConfigurationPanel.BP:
									if(!go.ontology.equals("BP"))	add=false;
									break;
								case WordCloudParameterConfigurationPanel.CC:
									if(!go.ontology.equals("CC"))	add=false;
									break;
								case WordCloudParameterConfigurationPanel.MF:
									if(!go.ontology.equals("MF"))	add=false;
									break;
								}
							if(add)
								{
								String desc=go.term;
								//System.out.println("Adding desc "+desc);
								if(!added.contains(desc))
									{
									splitAndAdd(desc, 1, 1, this.colorSeleccion, true, added);
									added.add(desc);
									}
								}
			        		}
						}
					}
				}
				
				break;
			case WordCloudParameterConfigurationPanel.OCCURRENCES:
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
								case WordCloudParameterConfigurationPanel.BP:
									if(!go.ontology.equals("BP"))	add=false;
									break;
								case WordCloudParameterConfigurationPanel.CC:
									if(!go.ontology.equals("CC"))	add=false;
									break;
								case WordCloudParameterConfigurationPanel.MF:
									if(!go.ontology.equals("MF"))	add=false;
									break;
								}
							if(add)
								{
								String desc=go.term;
								int oc=go.occurences;
								splitAndAdd(desc, oc, oc, this.colorSeleccion, false, null);
								}
			        		}
						}
					}
				}
				break;
	
			case WordCloudParameterConfigurationPanel.PVALUES:
				cont=0;
				for(GeneAnnotation a : annot)
					if(a.goTerms!=null && a.goTerms.size()>0)	cont++;
			
				for(GOTerm go:got)
					{
					if(go!=null)
						{
						String desc=go.term;
						double size=Math.abs(Math.log10(go.pvalue));
						splitAndAdd(desc, go.pvalue, size, this.colorSeleccion, false, null);
						}
					}
				break;
				}
			break;
		case WordCloudParameterConfigurationPanel.DEFINITION://---------------------------------------------
		default:
			cont=0;
			for(int i=0;i<annot.size();i++)
			{
			GeneAnnotation ga=annot.get(i);
			if(ga!=null)
				{
				if(ga.description!=null)
					{
					if(ga.description.length()>0)	cont++;
					String desc=ga.description;
					int oc=1;
					switch(this.menuCloud.size.getSelectedIndex())
						{
						case WordCloudParameterConfigurationPanel.OCCURRENCES:
							splitAndAdd(desc, oc, oc, this.colorSeleccion, false, null);
							break;
						case WordCloudParameterConfigurationPanel.GENES:
						default:
							splitAndAdd(desc, oc, oc, this.colorSeleccion, true, null);
							break;
						}
					}
				}
			}
			break;
		}
	
	System.out.println("Time in adding the words: "+(System.currentTimeMillis()-t1)/1000.0+" s");
	
	//synchronized(this){
	textChanged=true;
	if(this.getGraphics()!=null)	
		this.paintComponent(this.getGraphics());
	System.out.println("Time in painting: "+(System.currentTimeMillis()-t1)/1000.0+" s");
	t1=System.currentTimeMillis();
	menuCloud.setVisible(true);
	menuCloud.repaint();
	
	innerCall=false;
	if(cont>=0 && cont<=sesion.getSelectedBicluster().getGenes().size())
		{
		System.out.println(cont+"/"+sesion.getSelectedBicluster().getGenes().size());
		progress.setText(cont+"/"+sesion.getSelectedBicluster().getGenes().size());
		progress.setToolTipText("Annotations retrieved for "+cont+" of the "+sesion.getSelectedBicluster().getGenes().size()+" selected genes");
		}
	System.out.println("Time in the rest: "+(System.currentTimeMillis()-t1)/1000.0+" s");
	//}
	
	}

public synchronized void receiveGOTerms(ArrayList<GOTerm> goterms)
	{
	System.out.println("receiveGOTerms");
	this.got=goterms;
	if(got==null)
	      JOptionPane.showMessageDialog(this, "No relevant GO terms on hypergeometric test for onthology: "+(String)(menuCloud.ontology.getSelectedItem()), "Warning",JOptionPane.WARNING_MESSAGE);
	addWords();
	}
public synchronized void receiveGeneAnnotations(ArrayList<GeneAnnotation> annot)
	{
	if(doNOTupdate)	{doNOTupdate=false; return;}
	System.out.println("receiveGeneAnnotations");
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
public void splitAndAdd(String desc, double value, double size, Color c, boolean unique, ArrayList<String> alreadyAdded)
	{
	ArrayList<String> added=new ArrayList<String>();
	if(alreadyAdded!=null)		added=alreadyAdded;
	//1) split
	String[] dw=splitter(desc);
	//2) add
	for(int j=0;j<dw.length;j++)
		{
		dw[j]=dw[j].replace("(", "").replace(")", "").trim().toLowerCase();
		dw[j]=dw[j]+" ";
		if(!unique || !added.contains(dw[j]))
			addWord(dw[j], value, size, c);
		added.add(dw[j]);
		}
	
	return;
	}
	
public String[] splitter(String desc)
	{
	String[] dw=null;
	ArrayList<String> added=new ArrayList<String>();
	switch(this.menuCloud.split.getSelectedIndex())
		{
		case 0://1-word
			return desc.split(" ");
		case 1://2-word
			dw=desc.split(" ");
			for(int j=0;j<dw.length;j++)      			
				{
				if(j<dw.length-1)	
					{
					String diword=(dw[j]+" "+dw[j+1]).trim();
					added.add(diword);
					}
				}
			return added.toArray(new String[added.size()]);
		case 2://complete
			return new String[]{desc};
		}
	return null;
	}


public void addWord(String w, double value, double size, Color colorW)
		{
		if(!valid(w))	return;
		if(words!=null && words.containsKey(w))
			{
			Word nW=(Word)words.get(w);
			//colorW=getColorW(w);
			nW.setCont(nW.size+size);
			if(nW.size>maxCont) maxCont=nW.size;
			}
		else
			{
			//colorW=getColorW(w);
			words.put(w, new Word(null,0,0,value, size,colorW));
			contChar+=w.length();
			contWord+=size;
			}
		maxWord+=size;
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
	
	
	public void create()
		{
		//add el display o papplet o frame de visualización a la ventana
		//Asociar este panel a la ventana: 
		//Opcionalmente, se pueden inicializar aquí los datos del frame, en vez de hacerlo en el constructor
		this.getWindow().setContentPane(this);
		this.getWindow().pack();
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
	
	progress.setBounds(this.getWidth()-progress.getWidth()-2, this.getHeight()-progress.getHeight()-2, 30, 10);
	}
	
	
	
	public void resize()
		{
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
		double separationSpace=1.5;
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
				if(this.menuCloud.size.getSelectedIndex()==WordCloudParameterConfigurationPanel.PVALUES)
					wc=w.concat("("+formatNumber(nW.value)+")");
				else
					wc=w.concat("("+(int)(nW.size)+")");
				double num=nW.size;
				
				Font f=g2.getFont().deriveFont((float)num);
				TextLayout texto = new TextLayout(wc,f,frc);
				altoTexto = texto.getBounds().getHeight();
				anchoTexto = texto.getBounds().getWidth()+separationSpace*nW.size;
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
		double scale=Math.min(this.getHeight()/y, this.getWidth()/maxAncho);
		
		//3) Segunda vuelta vuelta, con los tamaños adecuados
		// como puede haber saltos de línea por el escalado, lo hacemos en un bucle donde vamos disminuyendo poco a poco la escala
		boolean end=false;
		boolean increase=false;
		//boolean increaseAnt=increase;
		//boolean first=true;
		
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
				if(this.menuCloud.size.getSelectedIndex()==WordCloudParameterConfigurationPanel.PVALUES)
					wc=w.concat("("+formatNumber(nW.value))+")";
				else	
					wc=w.concat("("+(int)nW.size+")");
				double num=nW.size;
				Font f=g2.getFont().deriveFont((float)(num*scale));
				TextLayout texto = new TextLayout(wc,f,frc);
				
				altoTexto = texto.getBounds().getHeight();
				anchoTexto = texto.getBounds().getWidth()+separationSpace*nW.size;
				if(x+anchoTexto>this.getWidth())	//change to next line
					{
					//Sumamos a todas las de la línea anterior el maxAlto de esa linea
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
		
		int limitX=this.getWidth();
		int limitY=this.getHeight()-menuCloud.getBounds().height;
		//System.out.println("Occupied size: "+Math.floor((x/limitX)*100)+", "+Math.floor((y/limitY)*100));
		
		if( (y<=limitY && x<=limitX) )//smaller
			{
			end=true;
			increase=true;
			}
		else//larger
			{
			increase=false;
			}

		

		if(!end)	
			{
			if(increase)	
				{
				System.out.println("Increase scale "+scale);
				scale*=1.5;
				}
			else
				{
				System.out.println("Decrease scale "+scale);
				scale/=1.5;
				}
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
		return es.usal.bicoverlapper.controller.kernel.Configuration.CLOUD_ID;
	}
	
	/*public void actionPerformed(ActionEvent e){
		String comando=e.getActionCommand();
		if (comando.equalsIgnoreCase(WordCloudParameterConfigurationPanel.DESCRIPTION)){
			setEnabledButon();
			if (!this.myColor.equals(sesion.getSelectionColor()))
				this.colorSeleccion=this.myColor;
			nameC=WordCloudParameterConfigurationPanel.DESCRIPTION;
			newSelection=true;
			this.actualizar();
			this.repaint();
		}else if(comando.equalsIgnoreCase(WordCloudParameterConfigurationPanel.GO_TERMS)){
			setEnabledButon();
			if (!this.myColor.equals(sesion.getSelectionColor()))
				this.colorSeleccion=this.myColor;
			//menuCloud.goButton.setEnabled(false);
			nameC=WordCloudParameterConfigurationPanel.GO_TERMS;
			newSelection=true;
			//ajusta=true;
			this.actualizar();
			this.repaint();
		}
	}*/
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
		/*if(!e.isShiftDown())//TODO: not used right now
			{
			nameSelected=new ArrayList<String>();
			newSelection=true;
			}
		else
			newSelection=false;*/
		nameSelected=new ArrayList<String>();
		newSelection=true;
		
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
							words.get(w).setColor(paleta[colorVarSelec]);
							System.out.println("W: "+nameSelected.toString());
							//TODO: Now the search is tricky because it depends on what genes have annotations searched for.
							//We should focus on the ones selected, or on the whole (with Ctrl pressed, for example) by making first a whole search
							Selection s=null;
							if(!e.isControlDown())
								{
								s=sesion.getMicroarrayData().search(nameSelected.get(0).trim(), 0, true, sesion.getSelectedGenesBicluster());
								}
							else
								{
								this.doNOTupdate=true;
								sesion.getMicroarrayData().getGeneAnnotations(ArrayUtils.toIntArray(sesion.getMicroarrayData().getNonAnnotatedGeneIds()), this, true, progress, null, true);
								s=sesion.getMicroarrayData().search(nameSelected.get(0).trim(), 0, true, null);
								System.out.println("Search by all not currently available");
								}
							
							if(s!=null && s.getGenes().size()>0)
								{
								sesion.setSelectedBicluster(s);
								sesion.updateAll();
								}
							else
								{
								System.err.println("No genes found");
								}
							}
						break;
						}
					}				
				}
			}
		}
	
	/**
	 * Pops up a configuration panel for heatmap visual properties
	 */
	public void configure(){
		if(!configurando){
			configurando = true;
			JInternalFrame ventanaConfig = this.getVentanaConfig();
			
			// Obtenemos el gestor de eventos de configuracion
			ConfigurationMenuManager gestor = new ConfigurationMenuManager(this,ventanaConfig,paleta,muestraColor);
			
			JPanel panelColor = this.getPanelPaleta(paleta, textoLabel, muestraColor);
			//JPanel panelParametros=new WordCloudParameterConfigurationPanel(this);
			JPanel panelParametros=menuCloud;
			this.setPanelParametros(panelParametros);
			JPanel panelBotones = this.getPanelBotones(gestor);
				
			// Configuramos la ventana de configuracion
			this.initPanelConfig(panelColor, null, panelParametros, panelBotones);
							
			// Mostramos la ventana de configuracion
			ventanaConfig.setLocation(getPosition());
			ventanaConfig.setTitle(Translator.instance.configureLabels.getString("s1")+" "+this.getName());
			sesion.getDesktop().add(ventanaConfig);
			try {
				ventanaConfig.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			ventanaConfig.pack();
			ventanaConfig.setVisible(true);
		}
	}
	
	/**
	 * Notifies the end of configuration
	 */
	public void endConfig(boolean ok){
		if(!ok)	{configurando=false; return;}
		
		colorSeleccion=paleta[colorEtiquetaVar];
		this.configurando = false;
		sesion.setSelectionColor(colorSeleccion);
		sesion.updateConfigExcept(this.getName());
		this.update();
	}

	
	private class Word
		{
		TextLayout text;	
		TextLayout contText;
		String label;
		double x;
		double y;
		double size;//size for this word, usually related to the figure of merit
		double value;//figure of merit for this word (number of occurences, p-value, etc.)
		Color color;
		
		public Word(TextLayout text, double x, double y, double value, double size,Color c)
			{
			this.text=text;			
			this.x=x;
			this.y=y;
			this.value=value;	
			this.size=size;	
			this.color=c;
			}
		public double getSize()
			{
			return this.size;
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
		public void setCont(double cont) {
			this.size = cont;
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