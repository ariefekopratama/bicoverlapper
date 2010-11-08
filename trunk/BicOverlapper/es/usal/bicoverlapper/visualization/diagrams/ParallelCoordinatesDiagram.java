package es.usal.bicoverlapper.visualization.diagrams;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.beans.PropertyVetoException;

import java.net.URL;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;



import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.TupleSelection;
import es.usal.bicoverlapper.kernel.managers.ConfigurationMenuManager;
import es.usal.bicoverlapper.utils.Line;
import es.usal.bicoverlapper.utils.Translator;

/**
 * This diagram represents Parallel Coordinates where each coordinate is a condition of the microarray matrix
 * and each line is a gene profile.
 * It implements threshold scrolling  and axes swapping.
 * @author Rodrigo Santamaria (rodri@usal.es) from the initial code of Javier Molpeceres
 *
 */
public class ParallelCoordinatesDiagram extends Diagram {

	private static final long serialVersionUID = -3509116578978086354L;
	
	static String nombre = "Parallel Coordinates";
	
	// atributos del panel del diagrama
	private Session sesion;
	private MicroarrayData datos;
	int numC=0; //Número de coordenadas
	int numG=0; //Número de líneas
	private int alto;
	private int ancho;
	private boolean atributosIniciados = false, configurando = false, diagramaPintado = false;
		
	// definicion de margenes del diagrama
	final int margenDer = 15;
	final int margenIzq = 80;
	final int margenSup = 20;
	final int margenInf = 100;
	final int margenDiagrama = 10; // porcentaje de exceso en intervalo de representacion del diagrama
	

//	*** Buffer especial para optimización en el dibujado de las líneas de fondo
	protected Graphics2D gbBufferFondo = null;
	protected Image imgFondo = null;
	
	
	private int[] seleccionPuntos = {es.usal.bicoverlapper.kernel.Configuration.PARALLEL_COORDINATES_ID};// kernel.Configuration.DiagramaPuntosId, kernel.Configuration.BubbleGraphId,

	// configuracion de color
	private static final int colorEtiquetaVar = 0;
	private static final int colorVarSelec = 1;
	private static final int colorFondoEtiqueta = 2;//
	private static final int colorTextoEtiqueta = 3;//
	private static final int colorLineaOut = 4;
	private static final int colorLinea = 5;//
	private static final int colorLineaMarcada = 6;//
	private static final int colorEje = 7;
	private static final int colorEjeSelec = 8;
	private static final int colorFondo = 9;
	private static final int colorCotas = 10;
	private static final int colorBicluster= 11;
	
	private Color[] paleta = {Color.DARK_GRAY, new Color(0,0,255,100), new Color(255,255,255,200), 
			Color.BLACK, new Color(200,200,200), new Color(200,0,0),
			new Color(0,0,100), Color.BLACK, Color.RED, 
			  Color.WHITE, Color.LIGHT_GRAY, new Color(200,0,0,100)};
	private String[] textoLabel = {"Condition labels", "Selected lines",
			Translator.instance.configureLabels.getString("s12"), Translator.instance.configureLabels.getString("s13"),
	Translator.instance.configureLabels.getString("s14"), Translator.instance.configureLabels.getString("s15"), 
	Translator.instance.configureLabels.getString("s16"), Translator.instance.configureLabels.getString("s20"),
	Translator.instance.configureLabels.getString("s21"), Translator.instance.configureLabels.getString("s19"),
	Translator.instance.configureLabels.getString("s22"), 
	"Bicluster Color"};
	private JTextField[] muestraColor = new JTextField[paleta.length];
	
	private Point p1;
	private boolean settingSlope;
	private Point p2;

	
	// atributos propios de la representacion del diagrama
	private int longEjeX;
	private int longEjeY;
	private double intervaloVar;//, anchoTextoCuota;
	private double[] ratio;
	private double[] maxText;
	private double[] minText;
	private double[] currentTextInf;
	private double[] currentTextSup;
	//private boolean ejesRelativos = true; 
	private boolean ejesRelativos = false; 
	private boolean scrollFijado = false;
	double anchoTextoCuota;
	
	// atributos usados para la gestion del intercambio de variables
	public int[] ordenVars;
	private Line2D.Double[] ejesVars;
	private int varSeleccionada = -1, posSeleccionada;
	private Line2D.Double ejeSeleccionado, ejeReferencia = null;
		
	// atributos usados para la gestion de la seleccion de una tupla
	private int tuplaSeleccionada;
	//private  Linea[][] tuplas;
	private  Point2D.Double[][] tuplas;
	private boolean actualizarTuplas=true;
	private boolean explicitDenyOfTupleUpdate=false;

	// atributos usados para la gestion de los intervalos de
	// representacion de cada variable
	
	private Rectangle2D.Double[] scrollSup, scrollInf;
	private double[] cotaSup, cotaInf;
	double[] valorSup, valorInf;
	Rectangle2D.Double scrollSeleccionado = null;
	private int offset, altoScroll, anchoScroll, varScroll = -1, scrollPos, posRef, margenScroll = 2;
	private double nuevaCota;
	private double posY;
	private Image scrollUp, scrollSelecUp, scrollDown, scrollSelecDown;
	private String iconoScrollUp, iconoScrollDown,
				   iconoScrollSelecUp, iconoScrollSelecDown;
	private static final int Sup = 0;
	private static final int Inf = 1;
	
	private static final int maxLineas=200; //Nº máximo de líneas que se dibujan. En conjuntos de datos
	//grandes, la cantidad de líneas a dibujar reduce mucho el rendimiento, cuando el dibujar tantas líneas
	//no ayuda a nada porque es muy ruidoso. Así, dibujamos unas cuantas como guía, aunque todas se tienen
	//en cuenta a la hora de selecciones, filtrados, etc.
	
    // parametros del menu de configuracion
	private static final int isEjesRelativos = 0;
	private String[] textoParametros = {Translator.instance.configureLabels.getString("s28")+": "};
	private Object[] parametros = {new Boolean(true)};
	
	// para el repintado selectivo
	boolean moviendoEje=false;
	boolean scrollMoved=false;
	
	//Optimización del cómputo de lineas de fondo
	GeneralPath gpLineasFondo=null;

	//y position of the lowest (doy) and highest expression (upy) levels
	private int[] upy;
	private int[] doy;

	private boolean computeLinePositions=true;

	private double[] min;
	private double[] max;

	
	/**
	 * Builds a <code>Diagrama2D</code> that implements Parallel Coordinates
	 * 
	 * @param sesion <code>Sesion</code> linked to the diagram
	 * @param dim <code>Dimension</code> with default diagram's dimensions.
	 */
	public ParallelCoordinatesDiagram(Session sesion, Dimension dim){
		int num = sesion.getNumParallelCoordinatesDiagrams();
		paleta[ParallelCoordinatesDiagram.colorVarSelec]=sesion.getSelectionColor().darker();
		paleta[ParallelCoordinatesDiagram.colorBicluster]=sesion.getSelectionColor().brighter();
		paleta[ParallelCoordinatesDiagram.colorLineaMarcada]=sesion.getHoverColor();
		
		iconoScrollUp = "es/usal/bicoverlapper/resources/images/up4.png";
		iconoScrollDown = "es/usal/bicoverlapper/resources/images/up4.png";
		iconoScrollSelecUp = "es/usal/bicoverlapper/resources/images/upselec4.png";
		iconoScrollSelecDown = "es/usal/bicoverlapper/resources/images/upselec4.png";
		
		String nombre;
		nombre = Translator.instance.menuLabels.getString("s8")+" "+num;
		for(int i = num; sesion.existsName(nombre); i++, num++){
			nombre =Translator.instance.menuLabels.getString("s8")+" "+num+" ("+Translator.instance.menuLabels.getString("s16")+")";
		}
		
		this.setName(nombre);
		this.sesion = sesion;
		this.setSession(sesion);

		this.datos=sesion.getMicroarrayData();
		this.alto = (int)dim.getHeight();
		this.ancho = (int)dim.getWidth();
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);
		
		
		// Inicializamos los atributos si al iniciar el diagrama hay datos cargados
		this.iniciarAtributos();		
		
		// registramos el gestor que permite seleccionar tuplas
		GestorSeleccionarTupla gestor1 = new GestorSeleccionarTupla();
		this.addMouseMotionListener(gestor1);
		this.addMouseListener(gestor1);
			
		// registramos el gestor que permite el intercambio 
		// de posicion de variables en el diagrama
		GestorCambioVars gestor2 = new GestorCambioVars();
		this.addMouseListener(gestor2);
		this.addMouseMotionListener(gestor2);
		
		// registramos el gestor que permite definir los
		// intervalos de representacion usando los scrolls
		GestorScrolls gestor3 = new GestorScrolls();
		
		this.addMouseListener(gestor3);
		this.addMouseMotionListener(gestor3);
		
		// registramos el gestor del cursor
		GestorCursor gestor4 = new GestorCursor();
		this.addMouseMotionListener(gestor4);
		
	}
	
	
	public void sortColumns(int [] columnOrder)
		{
	
		//To be modified by child classes
		this.ordenVars=columnOrder.clone();
		this.computeLinePositions=true;
		actualizarTuplas=true;
		ejeReferencia = null;
		varSeleccionada = -1;
			
		gpLineasFondo=null;
		
		atributosIniciados = false;
		//calcularAtributos();
		atributosIniciados = true;
		repaintAll=true;
		repaint();
		}

	/**
	 * Paints this diagram
	 * @param g	Graphics where the parallel coordinates are painted
	 */
	public synchronized void paintComponent(Graphics g) 
	{
		//		 Invalidamos la imagen antigua
		if(backBuffer != null){
			backBuffer.flush();
			backBuffer = null;
		}
		// Invalidamos el contexto gráfico antiguo		
		if(gbBuffer != null){
			gbBuffer.dispose();
			gbBuffer = null;
		}	  
		
		
		// Creamos una nueva imagen con el tamaño apropiado
		if (backBuffer == null) {
			backBuffer = createImage(ancho, alto);
			gbBuffer = ((Graphics2D)backBuffer.getGraphics());
		 }

//		 Establecemos las opciones de rendering y antialiasing de la gráfica
	    RenderingHints qualityHints = new RenderingHints(null);
	    qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	    gbBuffer.setRenderingHints(qualityHints);
	    ((Graphics2D)g).setRenderingHints(qualityHints);
	    
	if(repaintAll)	//Repintamos toda la pantalla
		{
	//	System.out.println("FULL REDRAW");
		
		long t=System.currentTimeMillis();
		drawFondo((Graphics2D)g);
		//System.out.println("drawBackground took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
		
		if(sesion.areMicroarrayDataLoaded())
			{
			drawLineas(gbBuffer);
	
			this.diagramaPintado = true;
			}
		//System.out.println("drawLines took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
		
		//		 Se ha creado la gráfica completa ya no se debe hacer otro repintado
		repaintAll = false;
		if(img != null)
			{
		    img.flush();
		    img = null;
			}

		// Almaceno la imagen actual como una imagen estable sobre la que hacer los pequeños cambios (sólo las líneas están en dicha imagen)
		img  = createImage(backBuffer.getSource());
		// Intercambio la imagen (rendering de doble buffer)
		g.drawImage(backBuffer,0,0,this);	  
	
		//System.out.println("drawImage took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
		
		drawScrolls((Graphics2D)g);
		
		//System.out.println("drawScrolls took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
	
		
		drawEjes((Graphics2D)g);
		
		//System.out.println("drawAxes took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
	
		drawEtiquetas((Graphics2D)g);
		
		//System.out.println("drawLabels took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
		}
	else	//Son cambios menores, no hace falta repintar todo
		{
		//System.out.println("PARTIAL REDRAW");
		long t=System.currentTimeMillis();
		
		//dibujo la base (las líneas)
     	gbBuffer.drawImage(img,0,0,this);
     	
     	//System.out.println("drawImage took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
	
     	//ahora dibujo todo lo demás, que no es necesario meterlo en la imagen
     	//(además, si meto los scrols, por ejemplo, luego se me duplicarían al moverlos)
     	drawScrolls(gbBuffer);
     	
     	//System.out.println("drawScrolls took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
	
		drawEjes(gbBuffer);
		
		//System.out.println("drawAxes took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
	
		drawEtiquetas(gbBuffer);
		
		//System.out.println("drawLabels took "+(System.currentTimeMillis()-t)/1000.0);
		t=System.currentTimeMillis();
	
		g.drawImage(backBuffer, 0,0,this);
		scrollMoved=false;
		
		//System.out.println("drawImage took "+(System.currentTimeMillis()-t)/1000.0);
		}
			
	}
	

	private void iniciarAtributos(){
		
		// iniciamos los atributos de la representacion del diagrama		
		numC=datos.getNumConditions();
		numG=datos.getNumGenes();
		
		this.maxText = new double[numC];
		this.minText = new double[numC];
		this.ratio = new double[numC];
		this.currentTextInf = new double[numC];
		this.currentTextSup = new double[numC];
		this.valorInf = new double[numC];
		this.valorSup = new double[numC];
		
		// iniciamos los atributos del cambio de variables
		this.ejesVars = new Line2D.Double[numC];
		this.ordenVars = new int[numC];
		
		if(sesion.getMicroarrayData()!=null)	ordenVars=sesion.getMicroarrayData().columnOrder;
		else									for(int i = 0; i<ordenVars.length; i++) 	ordenVars[i] = i;
		
		// iniciamos los atributos de la seleccion de tuplas
		tuplas = new Point2D.Double[numG][numC];
		tuplaSeleccionada = -1;
		actualizarTuplas=true;
		
		// iniciamos los valores de la acotacion del intervalo de representacion
		this.scrollSup = new Rectangle2D.Double[numC];
		this.scrollInf = new Rectangle2D.Double[numC];
		this.cotaSup = new double[numC];
		this.cotaInf = new double[numC];
		
		calcularAtributos();
		
		// iniciamos los botones de scroll para acotar
		// el intervalo de representacion
		scrollUp = loadIcon(this.iconoScrollUp).getImage();
		scrollDown = loadIcon(this.iconoScrollDown).getImage();
		scrollSelecUp = loadIcon(this.iconoScrollSelecUp).getImage();
		scrollSelecDown = loadIcon(this.iconoScrollSelecDown).getImage();
		altoScroll = scrollUp.getHeight(null)-1;
		anchoScroll = scrollUp.getWidth(null)-1;
		
		for(int i = 0; i < numC; i++){
			
			Rectangle2D.Double scroll = new Rectangle2D.Double(margenIzq+i*intervaloVar-anchoScroll/2,
															   margenSup-altoScroll-margenScroll,anchoScroll,altoScroll);
			
			scrollSup[i] = scroll;
			
			scroll = new Rectangle2D.Double(margenIzq+i*intervaloVar-anchoScroll/2,
											margenSup+longEjeY+margenScroll,anchoScroll,altoScroll);
			
			scrollInf[i] = scroll;
			
			cotaSup[i] = margenSup-margenScroll;
			cotaInf[i] = alto-margenInf+margenScroll;
		}
		atributosIniciados = true;
	}
	
	private ImageIcon loadIcon(String name)
		{
		URL imgURL=Thread.currentThread().getContextClassLoader().getResource(name);
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(imgURL));
		}
	
	/**
	 * Cambia ratio, max var, etc. para que esté con lo último de ordenVars,
	 *  salvo los datosVar.
	 *
	 */
	private void calcularAtributos(){
		
		if(sesion.areMicroarrayDataLoaded()){
			longEjeX = ancho-margenIzq-margenDer;
			longEjeY = alto-margenSup-margenInf;
			intervaloVar = longEjeX/(numC-1);
				
			long t1=System.currentTimeMillis();
			
			double maxLineas[]=new double[numC];
			double minLineas[]=new double[numC];
			max=new double[numC];
			min=new double[numC];
			
			if(sesion.getSelectedBicluster()!=null && sesion.getSelectedBicluster().getGenes().size()>0)
				{
				for(int i=0;i<numC;i++)		maxLineas[i]=minLineas[i]=-111;
				//1) Determine max and min values for genes
				LinkedList<Integer> lg=sesion.getSelectedGenesBicluster();
				for(int i = 0; i < numC; i++)
					{
					maxLineas[i] = datos.getExpressionAt(lg.get(0), ordenVars[i]);
					minLineas[i] = maxLineas[i];
					for(int j = 1; j < lg.size(); j++)
						{
						double exp=datos.getExpressionAt(lg.get(j), ordenVars[i]);
						if(exp < minLineas[i])
							minLineas[i] = exp;
						if(exp > maxLineas[i])
							maxLineas[i] = exp;
						}
					}
				}
			else
				{
				for(int j = 0; j < numC; j++)
					{
					maxLineas[j]=sesion.getMicroarrayData().maxCols[j];
					minLineas[j]=sesion.getMicroarrayData().minCols[j];
					}
				}
			
			//System.out.println("Time to compute min/maxLineas "+(System.currentTimeMillis()-t1)/1000.0);
			t1=System.currentTimeMillis();
			
				
			for(int i=0;i<numC;i++)
				{
				min[i]=sesion.getMicroarrayData().minCols[i];
				max[i]=sesion.getMicroarrayData().maxCols[i];
				
				if(ratio[i]>0)
					{
					currentTextSup[i]=maxLineas[i];
					currentTextInf[i]=minLineas[i];
					}
				else
					{
					currentTextSup[i]=sesion.getMicroarrayData().maxCols[i];
					currentTextInf[i]=sesion.getMicroarrayData().minCols[i];
					}
				//System.out.println("ca\t"+i+"\t"+currentTextInf[i]+"\t"+currentTextSup[i]);
				}
			
			//System.out.println("Time to compute textInf/Sup "+(System.currentTimeMillis()-t1)/1000.0);
			t1=System.currentTimeMillis();
			
			
			for(int i = 0; i < numC; i++)
				{
				double margen = (sesion.getMicroarrayData().maxCols[i] - sesion.getMicroarrayData().minCols[i])*((double)margenDiagrama/100);
				double dif=sesion.getMicroarrayData().maxCols[i]-sesion.getMicroarrayData().minCols[i]+2*margen;
				if(dif>0)
					{
					if(dif<1)	dif++;
					ratio[i] = longEjeY/(dif);
					}
				else		ratio[i] = 1;
				}
			
			//System.out.println("Time to compute ratio "+(System.currentTimeMillis()-t1)/1000.0);
			t1=System.currentTimeMillis();
			
			if(!ejesRelativos)//si los ejes son absolutos, sesion.getMicroarrayData().maxCols es para todos la mayor y sesion.getMicroarrayData().minCols igual en cuanto a tamaños
				{
				double maxv = sesion.getMicroarrayData().maxCols[0], minv = sesion.getMicroarrayData().minCols[0];
				for(int i = 0; i < numC; i++)
					{
					if(sesion.getMicroarrayData().maxCols[i] > maxv)			maxv = sesion.getMicroarrayData().maxCols[i];
					if(sesion.getMicroarrayData().minCols[i] < minv)			minv = sesion.getMicroarrayData().minCols[i];
					}
				
				for(int i = 0; i < numC; i++)
					{
					double dif=(maxv-minv);
					if(dif>0)
						{
						if(dif<1)	dif++;
						ratio[i] = longEjeY / (dif);
						}
					else		ratio[i] = 1;
					}
				}
		//System.out.println("Time to compute min/max "+(System.currentTimeMillis()-t1)/1000.0);
		t1=System.currentTimeMillis();
		if(computeLinePositions)	computeLinePositions();//Time consuming. Only if it's the first time or there's a redimension
		computeLinePositions=false;
		//System.out.println("Time to compute line positions "+(System.currentTimeMillis()-t1)/1000.0);
		actualizarTuplas=true;
		}
	}
	
	private void computeLinePositions()
		{
		for(int j = 0; j < numC; j++)
			{
			int o=ordenVars[j];
			double mv=sesion.getMicroarrayData().maxCols[j];
			double r=ratio[j];
			double pos=margenIzq+(intervaloVar*j);
			for(int i = 0; i < numG; i++)
				tuplas[i][j] = new Point2D.Double(pos, (mv-datos.getExpressionAt(i, o))*r+margenSup);
			}
		}
	
	private void trasladarScrolls(){
		if(sesion.areMicroarrayDataLoaded()){
				for(int i = 0; i < numC; i++){
				
				scrollSup[i].setRect(margenIzq+i*intervaloVar-anchoScroll/2,
									 margenSup-altoScroll-margenScroll,anchoScroll,altoScroll);
				scrollInf[i].setRect(margenIzq+i*intervaloVar-anchoScroll/2,
									 margenSup+longEjeY+margenScroll,anchoScroll,altoScroll);
				
				cotaSup[i] = margenSup-margenScroll;
				cotaInf[i] = margenSup+longEjeY+margenScroll;			
			}
		}
	}
	
	private void drawScrolls(Graphics2D g2)
		{
		System.currentTimeMillis();
		for(int i = 0; i < numC; i++)
			{
			int k=ordenVars[i];
			
			//Rectangle2D.Double r=(Rectangle2D.Double)scrollSup[i].clone();
			//r.y=scrollSup[k].y;
			//g2.draw(r);
			//g2.draw(scrollSup[k]);
			if((varScroll == i) && (scrollPos== Sup))
				g2.drawImage(scrollSelecDown, (int)scrollSup[i].getX(), (int)scrollSup[k].getY(), null);
			else
				g2.drawImage(scrollDown, (int)scrollSup[i].getX(), (int)scrollSup[k].getY(), null);
			
			//Rectangle2D.Double r2=(Rectangle2D.Double)scrollInf[i].clone();
			//r2.y=scrollInf[k].y;
			//g2.draw(r2);
			//g2.draw(scrollInf[k]);
			if((varScroll == i) && (scrollPos== Inf))
				g2.drawImage(scrollSelecUp, (int)scrollInf[i].getX(), (int)scrollInf[k].getY(), null);
			else
				g2.drawImage(scrollUp, (int)scrollInf[i].getX(), (int)scrollInf[k].getY(), null);			
			}	
		return;
		}
	

	private void drawEtiquetas(Graphics2D g2) {
		double altoTexto;
		double anchoTexto;
		
		// representamos la cota asociada con el scroll fijado
		if(scrollFijado && (varScroll != -1))
			{
			int k=ordenVars[varScroll];
			double posX = 0, posY = 0, valor = 0.0;
			if(scrollPos == Sup)
				{
				posX = scrollSup[varScroll].getX();
				posY = scrollSup[k].getY();
				valor = sesion.getMicroarrayData().maxCols[k]-(nuevaCota-margenSup+margenScroll)/ratio[k];
				}
			else if(scrollPos == Inf)
				{
				posX = scrollInf[varScroll].getX();
				posY = scrollInf[k].getY();
				valor = sesion.getMicroarrayData().maxCols[k]-(nuevaCota-margenSup-margenScroll)/ratio[k];
				}
			
			Font oldFont = g2.getFont();
			g2.setFont(new Font("Arial",Font.BOLD,9));
			g2.setPaint(paleta[colorEje]);

			String cad=datos.format(valor, k);
			TextLayout cota = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
			
			altoTexto = cota.getBounds().getHeight();
			anchoTexto = cota.getBounds().getWidth();
			cota.draw(g2,(float)(posX-anchoTexto),(float)(posY+(altoScroll-altoTexto)/2+altoTexto));
			g2.setFont(oldFont);
			anchoTextoCuota = anchoTexto;
			}
		

		// imprimimos el número de elementos seleccionados
		TextLayout sele=null;
		if(sesion.getSelectedBicluster()!=null && sesion.getSelectedGenesBicluster()!=null && sesion.getSelectedGenesBicluster().size()>0)	
			{
			String cad="selected: "+sesion.getSelectedBicluster().getGenes().size();
			String ids="";
			for(int i=0;i<4;i++)
				{
				if(i<sesion.getSelectedGenesBicluster().size())
					ids=ids+sesion.getMicroarrayData().rowLabels[sesion.getSelectedGenesBicluster().get(i)]+", ";
				}
			if(sesion.getSelectedGenesBicluster().size()>=4)	ids=ids+"...  ";
			cad=cad+" ("+ids.substring(0, ids.length()-2)+")";
			sele = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
			}
		else			sele = new TextLayout("selected: 0", g2.getFont(), g2.getFontRenderContext());	
		altoTexto = sele.getBounds().getHeight();
		anchoTexto = sele.getBounds().getWidth();
		sele.draw(g2,(float)(ancho-anchoTexto-10), (float)(10));
		
		// representamos los valores de referencia de la escala
		g2.setPaint(paleta[colorCotas]);
		Font oldFont = g2.getFont();
		g2.setFont(new Font("Arial",Font.BOLD,9));			

		double valor;
		String cad;
		TextLayout minimo, maximo;
		
		//Valores ahora
		valor = currentTextSup[0];
		cad=datos.format(valor, 0);

		maximo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
		altoTexto = maximo.getBounds().getHeight();
		anchoTexto = maximo.getBounds().getWidth();
		maximo.draw(g2,(float)(margenIzq+5),(float)(margenSup+altoTexto));
		
		
		valor = currentTextInf[0];
		cad=datos.format(valor, 0);

		minimo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
		altoTexto = minimo.getBounds().getHeight();
		anchoTexto = minimo.getBounds().getWidth();
		minimo.draw(g2,(float)(margenIzq+5),(float)(alto-margenInf));

		Font f=g2.getFont();
		FontRenderContext frc=g2.getFontRenderContext();
		for(int i = 1; i < numC; i++)
			{
			int k=ordenVars[i];
			valor=currentTextSup[k];
			cad=datos.format(valor, i);
			maximo = new TextLayout(cad, f, frc);
			altoTexto = maximo.getBounds().getHeight();
			anchoTexto = maximo.getBounds().getWidth();
			float x=(float)((margenIzq+5+i*intervaloVar));
			maximo.draw(g2,x,(float)(margenSup+altoTexto));
			
			valor = currentTextInf[k];
			cad=datos.format(valor, i);
			minimo = new TextLayout(cad, f, frc);
			altoTexto = minimo.getBounds().getHeight();
			anchoTexto = minimo.getBounds().getWidth();
			minimo.draw(g2,x,(float)(alto-margenInf));
			}
			
		//imag2.setFont(oldFont);
		g2.setFont(new Font("Arial",Font.BOLD,11));			

		// representamos las etiquetas de las condiciones
		g2.setPaint(paleta[colorEtiquetaVar]);
		
		for(int i = 0; i < numC; i++)
			{
			int k=ordenVars[i];
			TextLayout texto = new TextLayout(datos.getColumnLabel(k), g2.getFont(), 
												  g2.getFontRenderContext());
			altoTexto = texto.getBounds().getHeight();
			anchoTexto = texto.getBounds().getWidth();
			
			if(i < sesion.getSelectedConditionsBicluster().size())
					g2.setPaint(paleta[colorVarSelec]);
			else
				g2.setPaint(paleta[colorEtiquetaVar]);
			
			//con rotación
			AffineTransform old=g2.getTransform();
			g2.translate((float)(margenIzq+i*intervaloVar-anchoTexto*Math.cos(Math.toRadians(45))+5),
					  (float)(alto-(margenInf-altoScroll-anchoTexto*Math.sin(Math.toRadians(45))))+10);
			g2.rotate(Math.toRadians(-45));
			texto.draw(g2,0,0);
			
			g2.setTransform(old);
			}
		

		// representamos las etiquetas de la tupla seleccionada
		if(tuplaSeleccionada != -1)
			{
			oldFont = g2.getFont();
			g2.setFont(new Font("Arial",Font.BOLD,9));
			
			TextLayout etiqValor = new TextLayout(sesion.getMicroarrayData().rowLabels[tuplaSeleccionada],g2.getFont(),
					 g2.getFontRenderContext());
			etiqValor.draw(g2,(float)(margenIzq-5-etiqValor.getBounds().getWidth()),
				   (float)((sesion.getMicroarrayData().maxCols[ordenVars[0]] - datos.getExpressionAt(tuplaSeleccionada, ordenVars[0]))*ratio[ordenVars[0]]+margenSup));	
				
			g2.setFont(oldFont);
			}
		}

	private void drawLineas(Graphics2D g2) {
		
		GeneralPath gpLineas=new GeneralPath();
		GeneralPath gpLineasSelec=new GeneralPath();
		GeneralPath gpLineasSelecBic=new GeneralPath();
		
		boolean computeFondo=false;
		if(gpLineasFondo==null)
			{
			gpLineasFondo=new GeneralPath();
			computeFondo=true;
			}
		double t2;
		
		t2=System.currentTimeMillis();
		
		BiclusterSelection selecBic = this.sesion.getSelectedBicluster();
		float maxSelecY[]=new float[numC];
		float minSelecY[]=new float[numC];
		
		//------------------------------- preparación de las líneas con el gp
		boolean[] first=new boolean[numC];
		for(int i=0;i<numC;i++)		first[i]=true;
		
		if(selecBic!=null)
			{
			int nc=selecBic.getConditions().size();
			int ng=selecBic.getGenes().size();
			if(nc<numC-1 && ng<maxLineas)//Partial profile
				{
				for(int i: selecBic.getGenes())
					{
					if(tuplaSeleccionada!=i)
						{
						gpLineasSelecBic.append(getLine(i,0, nc-1),false);
						int init=nc-1;
						if(init<0)	init=0;
						gpLineasSelec.append(getLine(i,init, numC-1), false);
						}
					}
				}
			else		//Whole profile
				{
				if(ng<maxLineas)//We need to compute the whole lines
					{
					for(int i : selecBic.getGenes())
						{
						for(int j = 0; j < numC; j++)
							{
							float x1=(float)tuplas[i][j].getX();
							float y1=(float)tuplas[i][ordenVars[j]].getY();
							
							if(j==0)	gpLineasSelecBic.moveTo(x1, y1);
							else		gpLineasSelecBic.lineTo(x1, y1);
							}
						}
					}
				else
					{
					for(int i : selecBic.getGenes())
						{	
						for(int j = 0; j < numC; j++)
							{
							float y1=(float)tuplas[i][j].getY();
							if(first[j])
								{
								maxSelecY[j]=minSelecY[j]=y1;	
								first[j]=false;
								}
							else
								{
								if(y1>maxSelecY[j]) maxSelecY[j]=y1;	
								if(y1<minSelecY[j]) minSelecY[j]=y1;
								}
							}
						}
					}
				
				}
			}//if there's a selection
		
		//System.out.println("---Time to compute selected lines"+(System.currentTimeMillis()-t2)/1000.0);
		t2=System.currentTimeMillis();
		
		//------------------ BACKGROUND------------------------
		if(computeFondo)	//draw mean, max, min
			{
			imgFondo = createImage(ancho, alto);
			Graphics2D gbTemp = ((Graphics2D)imgFondo.getGraphics());
			 RenderingHints qualityHints = new RenderingHints(null);
			    qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			    qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			    gbTemp.setRenderingHints(qualityHints);   
			drawFondo(gbTemp);
			int maxFold=2;
			for(int s=maxFold;s>=0;s--)
				{
				int []px=new int[numC*2];
				int []py=new int[numC*2];
				for(int i=0;i<numC;i++)	//upper points
					{
					int k=ordenVars[i];
					double val=Math.min(sesion.getMicroarrayData().averageCols[k]+sesion.getMicroarrayData().sdCols[k]*(s+1), sesion.getMicroarrayData().maxCols[k]);
					py[i]=(int)(Math.max(margenSup+(sesion.getMicroarrayData().maxCols[k]-val)*ratio[k], margenSup));
					px[i]=(int)(margenIzq+intervaloVar*i);
					}
				for(int i=0;i<numC;i++)	//lower points
					{
					int c=numC-i-1;
					int k=ordenVars[c];
					//TODO: Rematar esto
					double val=Math.max(sesion.getMicroarrayData().averageCols[k]-sesion.getMicroarrayData().sdCols[k]*(s+1), sesion.getMicroarrayData().minCols[k]);
					py[i+numC]=(int)(Math.min(margenSup+(sesion.getMicroarrayData().maxCols[k]-val)*ratio[k], margenSup+this.longEjeY));
					px[i+numC]=(int)(margenIzq+intervaloVar*(c));
					}
				int grey=220-40*(maxFold-s);
				gbTemp.setPaint(new Color(grey,grey,grey));
				gbTemp.fillPolygon(px,py,px.length);
				}//OK
			
			if(min!=null && max!=null)
				{
				int []linex=new int[numC];
				upy=new int[numC];
				doy=new int[numC];
				int [] meany=new int[numC];
				int grey=170;
				for(int i=0;i<numC;i++)
					{
					int k=ordenVars[i];
					upy[i]=(int)(Math.max(margenSup+(sesion.getMicroarrayData().maxCols[k]-max[k])*ratio[k], margenSup));
					linex[i]=(int)(margenIzq+intervaloVar*i);
					doy[i]=(int)(Math.min(margenSup+(sesion.getMicroarrayData().maxCols[k]-min[k])*ratio[k], margenSup+this.longEjeY));
					meany[i]=(int)(Math.min(margenSup+(sesion.getMicroarrayData().maxCols[k]-sesion.getMicroarrayData().averageCols[k])*ratio[k], margenSup+this.longEjeY));
					}
				gbTemp.setPaint(new Color(grey,grey,grey));
				gbTemp.drawPolyline(linex,upy,linex.length);
				gbTemp.drawPolyline(linex,doy,linex.length);
				gbTemp.setPaint(new Color(240,240,240));
				gbTemp.drawPolyline(linex,meany,linex.length);
				}//OK
			}
		
		
		//System.out.println("---Time to draw background shapes"+(System.currentTimeMillis()-t2)/1000.0);
		t2=System.currentTimeMillis();
		g2.drawImage(imgFondo,0,0,this);
	
		t2=System.currentTimeMillis();
		if(this.settingSlope && p1!=null && p2!=null)
			g2.draw(new Line2D.Float(p1.x, p1.y, p2.x, p2.y));
		
		//System.out.println("---Time to draw slope"+(System.currentTimeMillis()-t2)/1000.0);
		t2=System.currentTimeMillis();
	
		if(selecBic==null)
			{
			g2.setPaint(this.sesion.getSelectionColor());
			g2.draw(gpLineas);
			
			g2.setPaint(this.sesion.getSelectionColor());
			g2.draw(gpLineasSelec);
			}
		else
			{
			//lines of genes of the bicluster but not of conditions in the bicluster
			if(selecBic.getGenes().size()<maxLineas)
				{
				g2.setPaint(paleta[colorBicluster].darker().darker());
				g2.draw(gpLineasSelec);
				g2.setPaint(paleta[colorBicluster].brighter().brighter());
				g2.draw(gpLineasSelecBic);
				}
			else 
				{
				int []px=new int[numC*2];
				int []py=new int[numC*2];
				for(int i=0;i<numC;i++)	//upper points
					{
					int k=ordenVars[i];
					
					py[i]=(int)maxSelecY[k];
					px[i]=(int)(margenIzq+intervaloVar*i);
					}
				for(int i=0;i<numC;i++)	//lower points
					{
					int c=numC-i-1;
					int k=ordenVars[c];
					py[i+numC]=(int)minSelecY[k];
					px[i+numC]=(int)(margenIzq+intervaloVar*(c));
					}
				Color c=sesion.getSelectionColor();
				Color sc=new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
				g2.setPaint(sc);
				g2.fillPolygon(px,py,px.length);
				}//Large polygon selected
			}
		
		
		//System.out.println("---Time to draw selected lines"+(System.currentTimeMillis()-t2)/1000.0);
		t2=System.currentTimeMillis();
	
		if(tuplaSeleccionada > -1)
			{
			g2.setPaint(paleta[colorLineaMarcada]);
			g2.setStroke(new BasicStroke(3f)); 
			for(int j = 0; j < (numC-1); j++)
				{
				g2.drawLine((int)tuplas[tuplaSeleccionada][j].x, (int)tuplas[tuplaSeleccionada][ordenVars[j]].y, (int)tuplas[tuplaSeleccionada][j+1].x, (int)tuplas[tuplaSeleccionada][ordenVars[j+1]].y);
				}
			g2.setStroke(new BasicStroke(1f)); 
			}
		
		//System.out.println("---Time to draw hovered line"+(System.currentTimeMillis()-t2)/1000.0);
		t2=System.currentTimeMillis();
	
		//System.out.println("Draw lineas tarda "+(t2-t1)/1000);
	}

	//As above, but gives only the line between the corresponding vars
	private GeneralPath getLine(int i, int beginVar, int endVar)
		{
		GeneralPath gp=new GeneralPath();
		gp=new GeneralPath();
		if(beginVar<0 || endVar>=numC)	
			System.err.println("Line out of bounds");
		for(int j=beginVar;j<=endVar;j++)
			{
			double x=tuplas[i][j].x;
			double y=tuplas[i][ordenVars[j]].y;
			if(j==beginVar)		gp.moveTo((float)x, (float)y);
			else				gp.lineTo((float)x, (float)y);
			}
		return gp;
		}
	
	private void drawFondo(Graphics2D g2) {
		g2.setPaint(paleta[colorFondo]);
	    Rectangle2D.Double fondo =
			new Rectangle2D.Double(0,0,ancho,alto);
		g2.fill(fondo);
		g2.draw(fondo);	
	}
	
	private void drawEjes(Graphics2D g2) {
				
		g2.setPaint(paleta[colorEje]);
		
		for(int i = 0; i < numC; i++){
			Line2D.Double ejeY = new Line2D.Double(margenIzq+intervaloVar*i,margenSup,
										   margenIzq+intervaloVar*i,alto-margenInf);
			ejesVars[i] = ejeY;
			g2.draw(ejeY);
		}
		Line2D.Double ejeX = new Line2D.Double(margenIzq,alto-margenInf,ancho-margenDer,alto-margenInf);
		g2.draw(ejeX);
		if(ejeReferencia != null){
			g2.setPaint(paleta[colorEjeSelec]);
			g2.draw(ejeReferencia);
		}
	}
		
	public int getId(){
		return es.usal.bicoverlapper.kernel.Configuration.PARALLEL_COORDINATES_ID;
	}
	
	/**
	 * Sets the color palette used in the diagram
	 * @param palette colors to be used in the diagram
	 */
	public void setPalette(Color[] palette){
		this.paleta = palette;
	}
	
	/**
	 * Return the color palette used in the diagram
	 * @return palette of colors used in the diagram
	 */
	public Color[] getColors(){
		return this.paleta;
	}
	
	
	
	public void setHeight(int alto){
		this.alto = alto;
	}
	
	public void setWidth(int ancho){
		this.ancho = ancho;
	}

	/**
	 * Updates parallel coordinates with information in the session layer, and repaints it
	 */
	public void update() {
		repaintAll=true;
		gpLineasFondo=null;
		//long t1=System.currentTimeMillis();
		//fitScrolls();
		//System.out.println("fit scrolls tarda "+(System.currentTimeMillis()-t1));
		//t1=System.currentTimeMillis();
		
		fitSelectedConditions();
		
		atributosIniciados = false;
		//calcularAtributos();
		atributosIniciados = true;
	
		//System.out.println("calcular atributos tarda "+(System.currentTimeMillis()-t1));
		//t1=System.currentTimeMillis();
		if(sesion.getSelectedConditionsBicluster()==null || sesion.getSelectedConditionsBicluster().size()==0 || sesion.getSelectedConditionsBicluster().size()==sesion.getMicroarrayData().getNumConditions())
				this.explicitDenyOfTupleUpdate=true;//We don't need to resort samples, which is very time consuming (about 5s for a 5000genes x 70 samples)
		repaint();
		
		//System.out.println("repintar tarda "+(System.currentTimeMillis()-t1));
	}
	
	/**
	 * Resort the axes so the selected conditions are the first ones
	 */
	private void fitSelectedConditions()
		{
		if(sesion==null || sesion.getSelectedBicluster()==null || sesion.getSelectedBicluster().getConditions().size()>=sesion.getMicroarrayData().getNumConditions())	return;
		
		for(int i=0;i<sesion.getSelectedBicluster().getConditions().size();i++)
			{
			int cib=((Integer)sesion.getSelectedBicluster().getConditions().get(i)).intValue();
			//Buscamos donde estaba antes cib
			int posCib=-1;
			
			for(int j=0;j<ordenVars.length;j++)
				if(ordenVars[j]==cib)	
					{
					posCib=j;
					break;
					}
			
			//y cambiamos las posiciones
			ordenVars[posCib]=ordenVars[i];
			ordenVars[i]=cib;
			}
		return;
		}
	
	/**
	 * Resets scrolls to their initial positions
	 */
	 void resetScrolls()
	 	{
		 for(int i = 0; i < numC; i++){
				
				Rectangle2D.Double scroll = new Rectangle2D.Double(margenIzq+i*intervaloVar-anchoScroll/2,
																   margenSup-altoScroll-margenScroll,anchoScroll,altoScroll);
				
				scrollSup[i] = scroll;
				
				scroll = new Rectangle2D.Double(margenIzq+i*intervaloVar-anchoScroll/2,
												margenSup+longEjeY+margenScroll,anchoScroll,altoScroll);
				
				scrollInf[i] = scroll;
				
				cotaSup[i] = margenSup-margenScroll;
				cotaInf[i] = alto-margenInf+margenScroll;
			} 
	 	}
	 
	 void fitScrolls()
		{
		if(sesion.getSelectedBicluster()!=null )
			{
			double maxLineas[]=new double[numC];
			double minLineas[]=new double[numC];
			for(int i=0;i<numC;i++)		maxLineas[i]=minLineas[i]=-111;
		
			//1) Determine max and min values for genes
			LinkedList<Integer> lg=sesion.getSelectedGenesBicluster();
			for(int i=0;i<lg.size();i++)
				{
				int pos=lg.get(i);
				for(int j = 0; j < numC; j++)
					{
					double y=0;
					
					y=tuplas[pos][j].getY();
					if(maxLineas[j]==-111)	maxLineas[j]=minLineas[j]=y;
					else
						{
						if(maxLineas[j]<y)	maxLineas[j]=y;
						if(minLineas[j]>y)	minLineas[j]=y;
						}
					}
				}
			
			
			//2) Set labels and scroll positions
			for(int i=0;i<numC;i++)
				{
				scrollSup[i].y=minLineas[i]-this.altoScroll;
				scrollInf[i].y=maxLineas[i];
				cotaSup[i]=scrollSup[i].y+altoScroll;
				cotaInf[i]=scrollInf[i].y;
				
				double valor = (cotaSup[i]);
				currentTextSup[i]=valor;
				valor = (cotaInf[i]);
				currentTextInf[i]=valor;
				}
			
			//3) Highlight selected lines TODO: here check how we do this to replicate when sorting
			if(sesion.getSelectedBicluster().getConditions().size()<sesion.getMicroarrayData().getNumConditions()-1)
				{
				for(int i=0;i<sesion.getSelectedBicluster().getConditions().size();i++)
					{
					int cib=((Integer)sesion.getSelectedBicluster().getConditions().get(i)).intValue();
					//Buscamos donde estaba antes cib
					int posCib=-1;
					
					for(int j=0;j<ordenVars.length;j++)
						if(ordenVars[j]==cib)	
							{
							posCib=j;
							break;
							}
					
					//		y las posiciones
					ordenVars[posCib]=ordenVars[i];
					ordenVars[i]=cib;
					
					//Cambiamos las cotas//CHECK: possibly unnecessary with the new changes
					double auxCotaSup = cotaSup[i], auxCotaInf = cotaInf[i];
					cotaSup[i] = cotaSup[posCib];
					cotaSup[posCib] = auxCotaSup;
					cotaInf[i] = cotaInf[posCib];
					cotaInf[posCib] = auxCotaInf;
					
					double auxYSup = scrollSup[i].y, auxYInf = scrollInf[i].y;
					scrollSup[i].y = scrollSup[posCib].y;
					scrollSup[posCib].y = auxYSup;
					scrollInf[i].y = scrollInf[posCib].y;
					scrollInf[posCib].y = auxYInf;
					
					actualizarTuplas=true;
					}
				}
			}
		else	//If the selection has no genes, we reset the scrolls to min and max
			{
			//iniciarAtributos();
			}
		}

	 
	public void resize(){
		atributosIniciados = false;
		datos = sesion.getMicroarrayData();
		computeLinePositions=true;
		calcularAtributos();
		trasladarScrolls();
		//fitScrolls();
		gpLineasFondo=null;
		atributosIniciados = true;
		repaintAll=true;
	}
	
	/**
	 * Updates parallel coordinates with information of a new set of data
	 */
	public void updateData(){
		if(sesion.getMicroarrayData()!=datos)
			{
			atributosIniciados = false;
			//datos = sesion.getData();
			datos = sesion.getMicroarrayData();
			this.iniciarAtributos();
			atributosIniciados = true;
			repaintAll=true;
			}
	}
	
	private void crearPanelParametros(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(this.textoParametros[ParallelCoordinatesDiagram.isEjesRelativos]),constraints);
		JRadioButton boton = new JRadioButton();
		boton.setSelected((Boolean)this.parametros[isEjesRelativos]);
		constraints.gridx = 1;
		constraints.weightx = 1.0;
		constraints.anchor = GridBagConstraints.WEST;
		panel.add(boton,constraints);
		
		this.setPanelParametros(panel);
	}
	
	/**
	 * Collects parameters from parallel coordinates configuration panel
	 * TODO: Still in development
	 */
	public void collectParameters(){
		JPanel panel = this.getPanelParametros();
		
		JRadioButton boton = (JRadioButton)panel.getComponent(1);
		this.parametros[ParallelCoordinatesDiagram.isEjesRelativos] = new Boolean(boton.isSelected());
		this.ejesRelativos = ((Boolean)this.parametros[isEjesRelativos]).booleanValue();
		this.calcularAtributos();
		
	}
	
	/**
	 * Pops up a configuration panel for parallel coordinates properties
	 * TODO: Still in development
	 */
	public void configure(){
		if(!configurando){
			configurando = true;
			
			// Obtenemos y configuramos la ventana de configuracion
			
			JInternalFrame ventanaConfig = this.getVentanaConfig();
			
			// Obtenemos el gestor de eventos de configuracion
			
			//GestorMenuConfiguracion gestor = new GestorMenuConfiguracion(this,ventanaConfig,paleta,muestraColor);
			ConfigurationMenuManager gestor = new ConfigurationMenuManager(this,ventanaConfig,paleta,muestraColor, colorVarSelec);
			
			// Creamos los paneles de configuracion
			this.crearPanelParametros();
			
			JPanel panelColor = this.getPanelPaleta(paleta, textoLabel, muestraColor);
		//	JPanel panelAnclajes = this.getPanelAnclajes(sesion, gestor);
		//	JPanel panelParametros = this.getPanelParametros();
			JPanel panelBotones = this.getPanelBotones(gestor);
			
			// Configuramos la ventana de configuracion
			
			this.initPanelConfig(panelColor, null, null, panelBotones);
							
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
	public void endConfig(){
		sesion.setSelectionColor(paleta[ParallelCoordinatesDiagram.colorVarSelec]);
		sesion.updateConfigExcept(this.getName());
		repaintAll=true;
		paintComponent(getGraphics());
		this.configurando = false;
	}
	
	public void updateConfig()
		{
		paleta[ParallelCoordinatesDiagram.colorVarSelec]=sesion.getSelectionColor();
		paleta[ParallelCoordinatesDiagram.colorBicluster]=sesion.getSelectionColor();
		repaintAll=true;
		paintComponent(getGraphics());
		}
	
	// Clase gestora de la seleccion de tuplas
	private class GestorSeleccionarTupla implements MouseMotionListener, MouseListener{

		
		public void mouseClicked(MouseEvent e) {
			BiclusterSelection selecBic=sesion.getSelectedBicluster();
			if(!scrollFijado && sesion.areMicroarrayDataLoaded() && selecBic!=null && (selecBic.getGenes().size()>0))
			         {
                    int zonaSelec = 2;
                    tuplaSeleccionada = -1;
                    
                 	for(int j : selecBic.getGenes())
	             			{
	                         for(int i = 0; i < (sesion.getMicroarrayData().getNumConditions()-1); i++)	//for each sample
	                         	{
	                        	int k=ordenVars[i];
	                        	int k2=ordenVars[i+1];
	                        	Line2D.Double l=new Line2D.Double(tuplas[j][i].x, tuplas[j][k].y,tuplas[j][i+1].x,tuplas[j][k2].y);
	                          	 if((l.ptSegDist(e.getPoint()) < zonaSelec)
	                                            && !scrollSup[i].contains(e.getPoint()) && !scrollInf[i].contains(e.getPoint()))
	                            	{
	                                tuplaSeleccionada = j;
	                                break;
	                                }
	                            }
	                        if(tuplaSeleccionada != -1)
	                        		{
	                        		LinkedList<Integer> genes=new LinkedList<Integer>();
	                        		LinkedList<Integer> conditions=new LinkedList<Integer>();
	                        		genes.add(tuplaSeleccionada);
	                        		for(int k=0;k<numC;k++)
			        						conditions.add(Integer.valueOf(ordenVars[k]));
			        				sesion.setSelectedBiclustersExcept(new BiclusterSelection(genes,conditions), "arallel");
	        						tuplaSeleccionada=-1;
			        				update();
	        						break;
	                    			}
	             		}                     
			         }
			}
		
		public void mouseMoved(MouseEvent e)
			{
			BiclusterSelection selecBic=sesion.getSelectedBicluster();
			if(sesion.areMicroarrayDataLoaded() && selecBic!=null && (selecBic.getGenes().size()>0))
		        {
                int zonaSelec = 2;
                tuplaSeleccionada = -1;
                
                 for(int j : selecBic.getGenes())
             			{
                         for(int i = 0; i < (sesion.getMicroarrayData().getNumConditions()-1); i++)	//for each sample
                         	{
                        	 int k=ordenVars[i];
                        	 int k2=ordenVars[i+1];
                         	Line2D.Double l=new Line2D.Double(tuplas[j][i].x,tuplas[j][k].y,tuplas[j][i+1].x,tuplas[j][k2].y);
                            if((l.ptSegDist(e.getPoint()) < zonaSelec)
                                            && !scrollSup[i].contains(e.getPoint()) && !scrollInf[i].contains(e.getPoint()))
                            	{
                                tuplaSeleccionada = j;
                                break;
                                }
                            }
                        if(tuplaSeleccionada != -1)
                        		{
                            	//System.out.println("Tupla localizada en "+(System.currentTimeMillis()-t1)+", es la numero "+tuplaSeleccionada+", que es la "+sesion.getMicroarrayData().getGeneNames()[tuplaSeleccionada]);
                            	repaintAll=true;
                        		repaint();
                    			break;
                    			}
             			}
         	     }

			}
		
		public void mouseDragged(MouseEvent e)
			{
			if(scrollFijado || ejeSeleccionado!=null || e.isControlDown())	return; 
			//Draw the line
			p2=e.getPoint();
			repaintAll=true;
			explicitDenyOfTupleUpdate=true;
			//update();
			repaint();
			
			}

		public void mousePressed(MouseEvent e) 
			{
			//take first point of the slope
			if(scrollFijado || ejeSeleccionado!=null || e.isControlDown())	return; 
			p1=e.getPoint();
			settingSlope=true;
			}

		public void mouseReleased(MouseEvent e) 
			{
			if(scrollFijado || ejeSeleccionado!=null || e.isControlDown())	return; 
			
			//take last point of the slope and select all the tuples matching, from the selected ones or if ctrl pressed, from everyones
			p2 = e.getPoint();
			if(p1.equals(p2))	return;//it's a click, not a slope
			
			int interval=-1;
			settingSlope=false;	
			for(int i=0;i<sesion.getMicroarrayData().getNumConditions()-1;i++)
				{
				int k=ordenVars[i];
				int k2=ordenVars[i+1];
			if(p1.x>=scrollSup[k].x && p2.x<=scrollSup[k2].x)
						{interval=i; break;}
				}
			if(interval<0) return;
			double slope=new Line(p1.x, p1.y, p2.x, p2.y).getSlope();
			double noise=0.5;

			LinkedList<Integer> genes=new LinkedList<Integer>();
			LinkedList<Integer> conditions=new LinkedList<Integer>();
			
			int k=ordenVars[interval];
			int k2=ordenVars[interval+1];
			if(sesion.getSelectedBicluster()!=null && sesion.getSelectedGenesBicluster()!=null && sesion.getSelectedGenesBicluster().size()>0)
				{//In this case, select only from the previous selection
				for(int j : sesion.getSelectedGenesBicluster())
					{
					Line ll=new Line(tuplas[j][interval].x, tuplas[j][k].y, tuplas[j][interval+1].x, tuplas[j][k2].y);
					double s=ll.getSlope();
					if(s>slope-noise && s<slope+noise)
						genes.add(Integer.valueOf(j));
					}
				}
			else
				{
				for(int j = 0; j < numG; j++)	//for each row
		 			{
					Line ll=new Line(tuplas[j][interval].x, tuplas[j][k].y, tuplas[j][interval+1].x, tuplas[j][k2].y);
					double s=ll.getSlope();
					if(s>slope-noise && s<slope+noise)
						genes.add(Integer.valueOf(j));
					}
				}
			
		repaintAll=true;
		
		for(int j=0;j<numC;j++)
			conditions.add(Integer.valueOf(j));
		

		sesion.setSelectedBiclustersExcept(new BiclusterSelection(genes,conditions), "arallel");
		//update();
		repaint();
		}
	
		public void mouseEntered(MouseEvent e) 
			{
			}

		public void mouseExited(MouseEvent e) 
			{
			}		
	}
	
	// Clase gestora del cambio de variables (cambia las posiciones de los ejes)
	private class GestorCambioVars implements MouseListener, MouseMotionListener{

		public void mouseClicked(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			if(sesion.areMicroarrayDataLoaded() && atributosIniciados){
					ejeSeleccionado = null;
				varSeleccionada = -1;
				int tamZona = 4;
							
				for(int i = 0; i < ejesVars.length; i++) {
					if ((Math.abs(e.getPoint().getX()- ejesVars[i].getX1()) < tamZona)
						&& ((e.getPoint().getY() > margenSup) && (e.getPoint().getY() < upy[i])
						|| (e.getPoint().getY() > doy[i]) && (e.getPoint().getY() < (alto-margenInf)))
								
						&& !inScroll(e.getPoint(), scrollSup[i], 5, true) && !inScroll(e.getPoint(),scrollInf[i],5, false)) {
						posSeleccionada = i;
						varSeleccionada = ordenVars[i];
						ejeSeleccionado = ejesVars[i];
						ejeReferencia = new Line2D.Double(ejeSeleccionado.getP1(),ejeSeleccionado.getP2());
						System.out.println("Seleccionada variable "+varSeleccionada +" en eje "+ejeSeleccionado);
						break;
					}
				}
			}
		}
			
		public void  mouseReleased(MouseEvent e) {
			System.out.println("mouseReleased in GestorCambioVars");
			if(sesion.areMicroarrayDataLoaded() && (ejeSeleccionado != null) && varSeleccionada!=-1)
				{
				int nuevaPosicion = 0;
				
				if(e.getX() < margenIzq)
					nuevaPosicion = 0;
				else if(e.getX() > (longEjeX+margenIzq))
					nuevaPosicion = numC-1;
				else if(e.getX() < ejeSeleccionado.getX1())
					nuevaPosicion = (int)((e.getX()-margenIzq)/intervaloVar)+1;					
				else 
					nuevaPosicion = (int)((e.getX()-margenIzq)/intervaloVar);
				
				if(posSeleccionada != nuevaPosicion)
					{
					int[] aux = new int[numC];
					aux[nuevaPosicion] = varSeleccionada;
										
					if(nuevaPosicion < posSeleccionada){
						
						for(int i = nuevaPosicion+1; i < (posSeleccionada+1); i++){
							aux[i] = ordenVars[i-1];							
						}
						
						for(int i = nuevaPosicion; i < (posSeleccionada+1); i++){
							ordenVars[i] = aux[i];
						}
					}
					else{
						
						for(int i = posSeleccionada; i < nuevaPosicion; i++){
							aux[i] = ordenVars[i+1];
						}
						
						for(int i = posSeleccionada; i < (nuevaPosicion+1); i++){
							ordenVars[i] = aux[i];
						}
					}

					/*double auxCotaSup = cotaSup[posSeleccionada], auxCotaInf = cotaInf[posSeleccionada];
					cotaSup[posSeleccionada] = cotaSup[nuevaPosicion];
					cotaSup[nuevaPosicion] = auxCotaSup;
					cotaInf[posSeleccionada] = cotaInf[nuevaPosicion];
					cotaInf[nuevaPosicion] = auxCotaInf;
					
					double auxYSup = scrollSup[posSeleccionada].y, auxYInf = scrollInf[posSeleccionada].y;
					scrollSup[posSeleccionada].y = scrollSup[nuevaPosicion].y;
					scrollSup[nuevaPosicion].y = auxYSup;
					scrollInf[posSeleccionada].y = scrollInf[nuevaPosicion].y;
					scrollInf[nuevaPosicion].y = auxYInf;
					*/
					actualizarTuplas=true;
				}
				ejeReferencia = null;
				varSeleccionada = -1;
					
				gpLineasFondo=null;
				
				atributosIniciados = false;
				//calcularAtributos();
				//trasladarScrolls();
				atributosIniciados = true;
				repaintAll=true;
				//update();
				repaint();
				
			}

		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseDragged(MouseEvent e) {
			
			if(sesion.areMicroarrayDataLoaded() && (ejeSeleccionado != null)){
									
				double offset = ejeSeleccionado.getX1() - e.getPoint().getX();
				ejeReferencia.setLine(ejeSeleccionado.getX1()-offset,ejeSeleccionado.getY1(),
										  ejeSeleccionado.getX2()-offset,ejeSeleccionado.getY2());
				
				repaint((int)ejeReferencia.getX1()-80,0,160,alto);
			}
		}

		public void mouseMoved(MouseEvent e) {}		
	}
	
	/**
	 * Determines if p is inside a rectangle r, with a vertical margin above or below
	 * @param p point to check
	 * @param r rectangle
	 * @param margin margin in pixels for a rectangle above or below r
	 * @param up if true, the margin is set above
	 * @return
	 */
	private boolean inScroll(Point2D p, Rectangle2D.Double r, int margin, boolean up)
		{
		if(up)
			{
			if(p.getX()>=r.x && p.getX()<=r.x+r.width && p.getY()>=r.y-margin && p.getY()<=r.y+r.height)
				return true;
			}
		else 
			{
			if(p.getX()>=r.x && p.getX()<=r.x+r.width && p.getY()>=r.y && p.getY()<=r.y+r.height+margin)
				return true;
			}
		return false;
		}
	
	// Clase gestora de los scrolls
	private class GestorScrolls implements MouseListener,MouseMotionListener {

		public void mouseClicked(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			
			if(sesion.areMicroarrayDataLoaded()){
				posRef = e.getY();
				offset = 0;
				scrollFijado = true;
				scrollSeleccionado=null;
				
				
				for(int i = 0; i < numC; i++){
					int k=ordenVars[i];
					Rectangle2D.Double rs=(Rectangle2D.Double)scrollSup[k].clone();
					rs.x=scrollSup[i].x;
					
					Rectangle2D.Double ri=(Rectangle2D.Double)scrollInf[k].clone();
					ri.x=scrollInf[i].x;
				
					if(inScroll(e.getPoint(), rs, 5, true)){
						scrollSeleccionado = scrollSup[k];
						varScroll = i;
						scrollPos = Sup;
						nuevaCota = cotaSup[k];
						posY = scrollSup[k].getY();
						break;
					}
					else if(inScroll(e.getPoint(), ri,5, false)){
						scrollSeleccionado = scrollInf[k];
						varScroll = i;
						scrollPos = Inf;
						nuevaCota = cotaInf[k];
						posY = scrollInf[k].getY();
						break;
					}
				}
				if(scrollSeleccionado==null)	scrollFijado=false;
			}
			return;
		}

		public void mouseReleased(MouseEvent e) {
			if(ejeSeleccionado!=null || scrollSeleccionado==null)	return; //estamos en un cambio de ejes o no hay scroll seleccionado
			LinkedList<Integer> genes=new LinkedList<Integer>();
			LinkedList<Integer> conditions=new LinkedList<Integer>();
				
			if(sesion.areMicroarrayDataLoaded()){
					if(varScroll > -1){
						int k=ordenVars[varScroll];
					if(scrollPos == Sup){
						cotaSup[k]=nuevaCota;
						currentTextSup[k] =sesion.getMicroarrayData().maxCols[k]-(nuevaCota-margenSup+margenScroll)/ratio[k];
					}
					else{
						cotaInf[k] = nuevaCota;
						currentTextInf[k] =sesion.getMicroarrayData().maxCols[k]-(nuevaCota-margenSup+margenScroll)/ratio[k];
					}
				}

				//Si no se ha pulsado Control, el movimiento del scroll tiene en cuenta las 
				//restricciones existentes en otras variables/la seleccion anterior
				System.out.println("ctrl: "+e.isControlDown()+"\talt: "+e.isAltDown()+"\taltgr: "+e.isAltGraphDown()+"\tshift: "+e.isShiftDown()+"\tmeta: "+e.isMetaDown());
				long t=System.currentTimeMillis();
				if(!e.isControlDown())
					{
					if(sesion.getSelectedBicluster()!=null && sesion.getSelectedGenesBicluster()!=null && sesion.getSelectedGenesBicluster().size()>0)
						{
						for(int i : sesion.getSelectedGenesBicluster())
							{
							int j;
							for(j=0;j<numC;j++)//TODO: we should try first the changed condition, which is the most probable to discriminate
								{
								int k=ordenVars[j];
								double y1=tuplas[i][k].y;
								if(y1 < cotaSup[k] || y1 > cotaInf[k])
									break;//not this gene, try the next (break conditions loop)
								}//If bicluster selected
							if(j==numC)		genes.add(Integer.valueOf(i));
							}
						}	
					else //This is the most time consuming loop, specially if we have lots of genes and conditions. (50000x100, for example)
						{//TODO: it can be done quicker if we start by the selected scroll, which will be the one removing a larger number of things
						long t1=System.currentTimeMillis();
						for(int i=0;i<numG;i++)
							{
							int j;
							for(j=0;j<numC;j++)
								{
								int k=ordenVars[j];
								double y1=tuplas[i][k].y;
								if(y1 < cotaSup[k] || y1 > cotaInf[k])
									break;//not this gene, try the next (break conditions loop)
								}
							if(j==numC)		genes.add(Integer.valueOf(i));
							}
					//	System.out.println("Time to do selection: "+(System.currentTimeMillis()-t1)/1000.0);
						}
					}
				//Si está pulsado Control, las restricciones en otras variables/selecciones anteriores no se tienen en cuenta
				else
					{
					for(int i=0;i<numG;i++)
						{
						boolean add=true;
						if((tuplas[i][varScroll].y < cotaSup[varScroll]) || (tuplas[i][varScroll].y > cotaInf[varScroll]))
									add=false;
						if(add)		genes.add(Integer.valueOf(i));
						}
					}
			//	System.out.println("Time to set selection "+(System.currentTimeMillis()-t)/1000.0);
				t=System.currentTimeMillis();
				
				scrollSeleccionado = null;
				scrollFijado = false;
				varScroll = -1;
				
				explicitDenyOfTupleUpdate=true;
				
				repaintAll=true;//De momento asi, pero con tiempo, si sigue yendo lento, las de fondo se pueden dejar pintadas de por vida
				//Salvo movimientos de los ejes, ojo.
				
				//genes were added in the above loop, then we select all the conditions
				for(int j=0;j<(numC);j++)			conditions.add(Integer.valueOf(j));
					
				System.out.println("Time to set selected bicluster "+(System.currentTimeMillis()-t)/1000.0);
				
				sesion.setSelectedBiclustersExcept(new BiclusterSelection(genes,conditions), "arallel");
				//fitScrolls();//By now, trying without fitting the scrolls, it gives clearer lines
				repaint();
			}
			return;
		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseDragged(MouseEvent e) {
			if(sesion.areMicroarrayDataLoaded()){
					if(varScroll > -1)
					{
				
					int k=ordenVars[varScroll];
					offset = e.getY() - posRef;
										
					if(scrollPos == Sup){
						nuevaCota = cotaSup[k]+offset;
						
						if((nuevaCota <= cotaInf[k]) && (nuevaCota >= (margenSup-margenScroll))){
							double posX = scrollSup[k].getX();
							scrollSup[k].setRect(posX,posY+offset,anchoScroll,altoScroll);
						}
						else if(nuevaCota > cotaInf[k]){
							nuevaCota = cotaInf[k];
						}
						else {
							nuevaCota = margenSup-margenScroll;
							}
					}
					
					if(scrollPos == Inf){
						nuevaCota = cotaInf[k]+offset;
						
						if((nuevaCota >= cotaSup[k]) && (nuevaCota <= (alto-margenInf+margenScroll))){
							double posX = scrollInf[k].getX();
							scrollInf[k].setRect(posX,posY+offset,anchoScroll,altoScroll);				
						}
						else if(nuevaCota < cotaSup[k]){
							nuevaCota = cotaSup[k];
						}
						else {
							nuevaCota = alto-margenInf+margenScroll;
						}
					}
					scrollMoved=true;
					paintComponent(getGraphics());
				}
			}			
		}

		public void mouseMoved(MouseEvent e) {}
		
	}
	
	// clase gestora de la imagen del cursor
	private class GestorCursor implements MouseMotionListener {

		//Selection by slope
	/*	public void mousePressed(MouseEvent e)
			{
			}
		public void mouseReleased(MouseEvent e)
			{
			}
		*/
		public void mouseDragged(MouseEvent e) 
			{
			
			}

		public void mouseMoved(MouseEvent e) {
			if(sesion.areMicroarrayDataLoaded() && atributosIniciados && diagramaPintado){
				int zonaSelec = 2;
				
				boolean zonaScroll = false;
				for(int i = 0; i < numC; i++)
					{	
					int k=ordenVars[i];
					Rectangle2D.Double rs=(Rectangle2D.Double)scrollSup[i].clone();
					rs.y=scrollSup[k].y;
					
					Rectangle2D.Double ri=(Rectangle2D.Double)scrollInf[i].clone();
					ri.y=scrollInf[k].y;
				
					//if(inScroll(e.getPoint(), scrollSup[i], 5, true) || inScroll(e.getPoint(),scrollInf[i],5, false)) 
					if(inScroll(e.getPoint(), rs, 5, true) || inScroll(e.getPoint(),ri,5, false)) 
						{
						zonaScroll = true;
						break;
						}
					}
				
				boolean zonaEje = false;
				if(!zonaScroll)
					{
					for(int i = 0; i < ejesVars.length; i++) 
						{
						if(ejesVars[i]==null)	System.out.println("Ejes vars "+i+" es null");
						if ((Math.abs(e.getPoint().getX()- ejesVars[i].getX1()) < zonaSelec)
							&& ((e.getPoint().getY() > margenSup) && (e.getPoint().getY() < upy[i])
								|| (e.getPoint().getY() > doy[i]) && (e.getPoint().getY() < (alto-margenInf)))
												
							&& !scrollSup[i].contains(e.getPoint()) && !scrollInf[i].contains(e.getPoint())) 
							{
							zonaEje = true;
							break;
							}
						}
					}
				
				if(zonaEje){
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				}
				else if(zonaScroll){
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				}
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}		
		}
	}
	
}









