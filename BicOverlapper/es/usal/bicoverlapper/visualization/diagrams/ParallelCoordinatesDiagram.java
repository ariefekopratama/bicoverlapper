package es.usal.bicoverlapper.visualization.diagrams;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;



import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.data.MultidimensionalData;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.DiagramWindow;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.TupleSelection;
import es.usal.bicoverlapper.kernel.managers.ConfigurationMenuManager;
import es.usal.bicoverlapper.utils.Translator;

/**
 * This diagram represents Parallel Coordinates where each coordinate is a condition of the microarray matrix
 * and each line is a gene profile.
 * It implements threshold scrolling  and axes swapping.
 * @author Rodrigo Santamaria and Javier Molpeceres
 *
 */
public class ParallelCoordinatesDiagram extends Diagram {

	private static final long serialVersionUID = -3509116578978086354L;
	
	static String nombre = "Parallel Coordinates";
	
	// atributos del panel del diagrama
	private Session sesion;
	//private MultidimensionalData datos;
	private MicroarrayData datos;
	int numC=0; //Número de coordenadas
	int numG=0; //Número de líneas
	private int alto;
	private int ancho;
	private boolean atributosIniciados = false, configurando = false, diagramaPintado = false;
		
	// definicion de margenes del diagrama
	final int margenDer = 40;
	final int margenIzq = 80;
	final int margenSup = 40;
	final int margenInf = 100;
	final int margenDiagrama = 10; // porcentaje de exceso en intervalo de representacion del diagrama
	

//	*** Buffer especial para optimización en el dibujado de las líneas de fondo
	protected Graphics2D gbBufferFondo = null;
	protected Image imgFondo = null;
	
	
	private int[] seleccionPuntos = {es.usal.bicoverlapper.kernel.Configuration.PARALLEL_COORDINATES_ID};// kernel.Configuration.DiagramaPuntosId, kernel.Configuration.BubbleGraphId,
	//private int[] seleccionPuntos = {kernel.Configuracion.DiagramaPuntosId, kernel.Configuracion.BubbleGraphId,
											//kernel.Configuration.TreeMapId,  kernel.Configuration.CloudId};

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
	
	private Color[] paleta = {Color.DARK_GRAY, new Color(0,0,255,100), Color.YELLOW, 
			Color.DARK_GRAY, new Color(200,200,200), Color.LIGHT_GRAY,
			  Color.YELLOW, Color.BLACK, Color.RED, 
			  Color.WHITE, Color.LIGHT_GRAY, new Color(200,0,0,100)};
	private String[] textoLabel = {"Condition labels", "Selected lines",
			Translator.instance.configureLabels.getString("s12"), Translator.instance.configureLabels.getString("s13"),
	Translator.instance.configureLabels.getString("s14"), Translator.instance.configureLabels.getString("s15"), 
	Translator.instance.configureLabels.getString("s16"), Translator.instance.configureLabels.getString("s20"),
	Translator.instance.configureLabels.getString("s21"), Translator.instance.configureLabels.getString("s19"),
	Translator.instance.configureLabels.getString("s22"), 
	"Bicluster Color"};
	private JTextField[] muestraColor = new JTextField[paleta.length];
	
	// atributos de configuracion anclajes
	private DiagramWindow itemAñadir, itemEliminar;
	
	// atributos propios de la representacion del diagrama
	private int longEjeX;
	private int longEjeY;
	private double intervaloVar;//, anchoTextoCuota;
	private double[] ratio;
	private double[] maxVar;
	private double[] minVar;
	private double[] maxText;
	private double[] minText;
	private double[] currentTextInf;
	private double[] currentTextSup;
	private boolean ejesRelativos = true, scrollFijado = false;
	double anchoTextoCuota;
	
	// atributos usados para la gestion del intercambio de variables
	private int[] ordenVars;
	private Line2D.Double[] ejesVars;
	private int varSeleccionada = -1, posSeleccionada;
	private Line2D.Double ejeSeleccionado, ejeReferencia = null;
		
	// atributos usados para la gestion de la seleccion de una tupla
	private int tuplaSeleccionada;
	private  Linea[][] tuplas;
	private boolean actualizarTuplas=true;
	
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
	
		iconoScrollUp = "es/usal/bicoverlapper/resources/images/up.png";
		iconoScrollDown = "es/usal/bicoverlapper/resources/images/down.png";
		iconoScrollSelecUp = "es/usal/bicoverlapper/resources/images/upSelec.png";
		iconoScrollSelecDown = "es/usal/bicoverlapper/resources/images/downSelec.png";

		String nombre;
		nombre = Translator.instance.menuLabels.getString("s8")+" "+num;
		for(int i = num; sesion.existsName(nombre); i++, num++){
			nombre =Translator.instance.menuLabels.getString("s8")+" "+num+" ("+Translator.instance.menuLabels.getString("s16")+")";
		}
		
		this.setName(nombre);
		this.sesion = sesion;
		this.setSession(sesion);

	//	this.datos = sesion.getData();
		this.datos=sesion.getMicroarrayData();
		this.alto = (int)dim.getHeight();
		this.ancho = (int)dim.getWidth();
		this.setPreferredSize(new Dimension(ancho,alto));
		this.setSize(ancho,alto);
		
		es.usal.bicoverlapper.utils.CustomColor c=new es.usal.bicoverlapper.utils.CustomColor();
		
		// Inicializamos los atributos si al iniciar el diagrama hay datos cargados
		//if(sesion.dataLoaded())
				this.iniciarAtributos();		
		
		// registramos el gestor que permite seleccionar tuplas
		GestorSeleccionarTupla gestor1 = new GestorSeleccionarTupla();
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
	
	/**
	 * Paints this diagram
	 * @param g	Graphics where the parallel coordinates are painted
	 */
	public void paintComponent(Graphics g) 
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
		drawFondo(gbBuffer);	
		//if(sesion.dataLoaded())
		if(sesion.areMicroarrayDataLoaded())
			{
			drawLineas(gbBuffer);
	
			this.diagramaPintado = true;
			}

		//		 Se ha creado la gráfica completa ya no se debe hacer otro repintado
		repaintAll = false;
		if(img != null){
		   img.flush();
		   img = null;
		}

		 // Almaceno la imagen actual como una imagen estable sobre la que hacer los pequeños cambios (sólo las líneas están en dicha imagen)
		 img  = createImage(backBuffer.getSource());
		 // Intercambio la imagen (rendering de doble buffer)
		 g.drawImage(backBuffer,0,0,this);	  
		
		drawEjes((Graphics2D)g);
		drawScrolls((Graphics2D)g);
		drawEtiquetas((Graphics2D)g);
		}
	else	//Son cambios menores, no hace falta repintar todo
		{
		//dibujo la base (las líneas)
     	gbBuffer.drawImage(img,0,0,this);
     	//ahora dibujo todo lo demás, que no es necesario meterlo en la imagen
     	//(además, si meto los scrols, por ejemplo, luego se me duplicarían al moverlos)
		drawEjes(gbBuffer);
		drawEtiquetas(gbBuffer);
		drawScrolls(gbBuffer);
		g.drawImage(backBuffer, 0,0,this);
		scrollMoved=false;
		}
			
	}
	

	
	private class Linea{
		public Line2D.Float linea;
		public int numTupla;
		
		public Linea(Line2D.Float linea, int numTupla){
			this.linea = linea;
			this.numTupla = numTupla;
		}
	}
	
	private void iniciarAtributos(){
		
		// iniciamos los atributos de la representacion del diagrama		
		//int numC=datos.getNumFields();
		numC=datos.getNumConditions();
		numG=datos.getNumGenes();
		
		this.maxVar = new double[numC];
		this.minVar = new double[numC];
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
		
		
		for(int i = 0; i<ordenVars.length; i++)
			ordenVars[i] = i;
		
		// iniciamos los atributos de la seleccion de tuplas
		//tuplas = new Linea[datos.getNumTuples()][datos.getNumFields()-1];
		tuplas = new Linea[datos.getNumGenes()][numC-1];
		tuplaSeleccionada = -1;
		actualizarTuplas=true;
		
		// iniciamos los valores de la acotacion del intervalo de representacion
		this.scrollSup = new Rectangle2D.Double[numC];
		this.scrollInf = new Rectangle2D.Double[numC];
		this.cotaSup = new double[numC];
		this.cotaInf = new double[numC];
		
		this.calcularAtributos();
		
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
		
		//if(sesion.dataLoaded()){
		if(sesion.areMicroarrayDataLoaded()){
				longEjeX = ancho-margenIzq-margenDer;
			longEjeY = alto-margenSup-margenInf;
			intervaloVar = longEjeX/(numC-1);
					
			for(int i = 0; i < numC; i++){
				
				//maxVar[i] = datos.fieldAt(ordenVars[i]).getData(0);
				//minVar[i] = datos.fieldAt(ordenVars[i]).getData(0);
				maxVar[i] = datos.getExpressionAt(0, ordenVars[i]);
				minVar[i] = datos.getExpressionAt(0, ordenVars[i]);
				
				for(int j = 0; j < numG; j++){
					
					//if(datos.fieldAt(ordenVars[i]).getData(j) < minVar[i])
					if(datos.getExpressionAt(j, ordenVars[i]) < minVar[i])
						{
						//minVar[i] = datos.fieldAt(ordenVars[i]).getData(j);
						minVar[i] = datos.getExpressionAt(j, ordenVars[i]);
						}
					
					//if(datos.fieldAt(ordenVars[i]).getData(j) > maxVar[i])
					//maxVar[i] = datos.fieldAt(ordenVars[i]).getData(j);
					if(datos.getExpressionAt(j, ordenVars[i]) > maxVar[i])
						maxVar[i] = datos.getExpressionAt(j, ordenVars[i]);
				}			
			}
				
			for(int i=0;i<numC;i++)
				{
			//	System.out.println("Variable "+i+" en ["+minText[i]+", "+maxText[i]+"]");
				maxText[i]=maxVar[i];
				minText[i]=minVar[i];
				currentTextSup[i]=maxText[i];
				currentTextInf[i]=minText[i];
				}
			
			for(int i = 0; i < numC; i++)
				{
				double margen = (maxVar[i] - minVar[i])*((double)margenDiagrama/100);
				maxVar[i] += margen;
				minVar[i] -= margen;
				ratio[i] = longEjeY/(maxVar[i]-minVar[i]);
				}
			
			
			if(!ejesRelativos){
				
				double max = maxVar[0], min = minVar[0];
				for(int i = 0; i < numC; i++){
					if(maxVar[i] > max)
						max = maxVar[i];
					
					if(minVar[i] < min)
						min = minVar[i];
				}
				
				for(int i = 0; i < numC; i++){
					maxVar[i] = max;
					minVar[i] = min;
					ratio[i] = longEjeY / (max - min);
				}
			}
		actualizarTuplas=true;
		}
	}
	
	private void trasladarScrolls(){
		//if(sesion.dataLoaded()){
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
		for(int i = 0; i < numC; i++)
			{
			g2.draw(scrollSup[i]);
			if((varScroll == i) && (scrollPos== Sup))
				g2.drawImage(scrollSelecDown, (int)scrollSup[i].getX(), (int)scrollSup[i].getY(), null);
			else
				g2.drawImage(scrollDown, (int)scrollSup[i].getX(), (int)scrollSup[i].getY(), null);
			
			g2.draw(scrollInf[i]);
			if((varScroll == i) && (scrollPos== Inf))
				g2.drawImage(scrollSelecUp, (int)scrollInf[i].getX(), (int)scrollInf[i].getY(), null);
			else
				g2.drawImage(scrollUp, (int)scrollInf[i].getX(), (int)scrollInf[i].getY(), null);			
			}		
		}
	

	private void drawEtiquetas(Graphics2D g2) {
		
		double altoTexto;
		double anchoTexto;
		
		// representamos la cota asociada con el scroll fijado
		if(scrollFijado && (varScroll != -1)){
			double posX = 0, posY = 0, valor = 0.0;
			if(scrollPos == Sup){
				
				posX = scrollSup[varScroll].getX();
				posY = scrollSup[varScroll].getY();
				valor = maxVar[varScroll]-(nuevaCota-margenSup+margenScroll)/ratio[varScroll];
				currentTextSup[varScroll]=valor;
			}else if(scrollPos == Inf){
				posX = scrollInf[varScroll].getX();
				posY = scrollInf[varScroll].getY();
				valor = maxVar[varScroll]-(nuevaCota-margenSup-margenScroll)/ratio[varScroll];
				currentTextInf[varScroll]=valor;
				
			}
			Font oldFont = g2.getFont();
			g2.setFont(new Font("Arial",Font.BOLD,9));
			g2.setPaint(paleta[colorCotas]);

			String cad=datos.format(valor, varScroll);
			TextLayout cota = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
			
			altoTexto = cota.getBounds().getHeight();
			anchoTexto = cota.getBounds().getWidth();
			cota.draw(g2,(float)(posX-anchoTexto),(float)(posY+(altoScroll-altoTexto)/2+altoTexto));
			g2.setFont(oldFont);
			anchoTextoCuota = anchoTexto;
		}

		// imprimimos el número de elementos seleccionados
		if(sesion.getDataLayer().getPointSelection()!=null)
			{
			//TextLayout sele = new TextLayout("selected:" +new Integer(sesion.getDataLayer().getPointSelection().getNumSelected(this.getDataLayer().getFilterPoints())).toString(), g2.getFont(), g2.getFontRenderContext());
			TextLayout sele = new TextLayout("selected: "+sesion.getSelectedBicluster().getGenes().size(), g2.getFont(), g2.getFontRenderContext());
			altoTexto = sele.getBounds().getHeight();
			anchoTexto = sele.getBounds().getWidth();
			sele.draw(g2,(float)(ancho-anchoTexto-10), (float)(10));
			}
		
		// representamos los valores de referencia de la escala
		{
			g2.setPaint(paleta[colorCotas]);
			Font oldFont = g2.getFont();
			g2.setFont(new Font("Arial",Font.BOLD,9));			

			
			double valor =maxText[0];
			String cad=datos.format(valor, 0);
			
			TextLayout maximo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
			altoTexto = maximo.getBounds().getHeight();
			anchoTexto = maximo.getBounds().getWidth();
			//maximo.draw(g2,(float)((margenIzq-anchoTexto-5)),(float)(margenSup+altoTexto));
			
			valor = minText[0];
			cad=datos.format(valor, 0);
			//TextLayout minimo = new TextLayout(new Double(valor).toString(), g2.getFont(), g2.getFontRenderContext());
			TextLayout minimo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
			altoTexto = minimo.getBounds().getHeight();
			anchoTexto = minimo.getBounds().getWidth();
			//minimo.draw(g2,(float)((margenIzq-anchoTexto-5)),(float)(alto-margenInf));
			
			//Valores ahora
			//valor=Math.rint(currentTextSup[0]*nc)/nc;
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

			if(ejesRelativos)
				{
				for(int i = 1; i < numC; i++)
					{
					valor = maxText[i];
					cad=datos.format(valor, i);

					maximo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
					altoTexto = maximo.getBounds().getHeight();
					anchoTexto = maximo.getBounds().getWidth();
					//maximo.draw(g2,(float)((margenIzq-anchoTexto-5+i*intervaloVar)),(float)(margenSup+altoTexto));
					
					valor=minText[i];
					cad=datos.format(valor, i);
					minimo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
					altoTexto = minimo.getBounds().getHeight();
					anchoTexto = minimo.getBounds().getWidth();
					//minimo.draw(g2,(float)((margenIzq-anchoTexto-5+i*intervaloVar)),(float)(alto-margenInf));

					//Valores ahora
					valor=currentTextSup[i];

					cad=datos.format(valor, i);

					maximo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
					altoTexto = maximo.getBounds().getHeight();
					anchoTexto = maximo.getBounds().getWidth();
					maximo.draw(g2,(float)((margenIzq+5+i*intervaloVar)),(float)(margenSup+altoTexto));
					
					valor = currentTextInf[i];
					cad=datos.format(valor, i);
					minimo = new TextLayout(cad, g2.getFont(), g2.getFontRenderContext());
					altoTexto = minimo.getBounds().getHeight();
					anchoTexto = minimo.getBounds().getWidth();
					minimo.draw(g2,(float)((margenIzq+5+i*intervaloVar)),(float)(alto-margenInf));
					}
				}
				
			g2.setFont(oldFont);
		}
		

		
		// representamos las etiquetas de las variables
		g2.setPaint(paleta[colorEtiquetaVar]);
		
		for(int i = 0; i < numC; i++){
			//TextLayout texto = new TextLayout(datos.fieldAt(ordenVars[i]).getName(), g2.getFont(), 
			TextLayout texto = new TextLayout(datos.getConditionName(ordenVars[i]), g2.getFont(), 
												  g2.getFontRenderContext());
			altoTexto = texto.getBounds().getHeight();
			anchoTexto = texto.getBounds().getWidth();
			
			if(varSeleccionada == ordenVars[i])
				g2.setPaint(paleta[colorVarSelec]);
			else
				g2.setPaint(paleta[colorEtiquetaVar]);
			
			//Sin rotación
			//texto.draw(g2,(float)(margenIzq+i*intervaloVar-anchoTexto/2),
			//	  (float)(alto-(margenInf-altoScroll-altoTexto)/2));

			
			//con rotación
			AffineTransform old=g2.getTransform();
			g2.translate((float)(margenIzq+i*intervaloVar-anchoTexto*Math.cos(Math.toRadians(45))+5),
					  (float)(alto-(margenInf-altoScroll-anchoTexto*Math.sin(Math.toRadians(45))))+10);
			g2.rotate(Math.toRadians(-45));
			
			texto.draw(g2,0,0);
			
			g2.setTransform(old);
			
		}
		
		// representamos las etiquetas de la tupla seleccionada
		
		int margenEtiq = 2, margenRecuadro = 2;
		
		if(tuplaSeleccionada != -1){
			for(int i = 0; i < numC; i++){
			//	double valor = datos.fieldAt(ordenVars[i]).getData(tuplaSeleccionada);
				double valor = datos.getExpressionAt(tuplaSeleccionada, ordenVars[i]);
				valor = Math.rint(valor*100)/100;
				TextLayout etiqValor = new TextLayout(new Double(valor).toString(),g2.getFont(),
																 g2.getFontRenderContext());
				altoTexto = etiqValor.getBounds().getHeight();
				anchoTexto = etiqValor.getBounds().getWidth();
				
				Rectangle2D.Double recuadroEtiq = new Rectangle2D.Double(intervaloVar*i+margenIzq,
										   (maxVar[i] - valor)*ratio[i]+margenSup-margenEtiq-altoTexto-2*margenRecuadro,
										   anchoTexto+2*margenRecuadro,altoTexto+2*margenRecuadro);
				
				g2.setPaint(paleta[colorFondoEtiqueta]);
				g2.fill(recuadroEtiq);
				g2.setPaint(paleta[colorTextoEtiqueta]);
				g2.draw(recuadroEtiq);
				etiqValor.draw(g2,(float)(intervaloVar*i+margenIzq+margenRecuadro),
							      (float)((maxVar[i] - valor)*ratio[i]+margenSup-margenEtiq-margenRecuadro));				
			}
		}
	}

	boolean lineaInLimites(int i)
	{
	for(int j=0;j<numC-1;j++)
		{
		if((tuplas[i][j].linea.getY1() < cotaSup[j]) || (tuplas[i][j].linea.getY1() > cotaInf[j])
			|| (tuplas[i][j].linea.getY2() < cotaSup[j+1]) || (tuplas[i][j].linea.getY2() > cotaInf[j+1]))
			{
			return false;
			}
		}
	return true;
	}
	
	/*
	 * Como el anterior, pero devuelve null o el path iterator si entra en los límites
	 */
	ParallelLine lineInBounds(int i)
		{
		//GeneralPath gp=new GeneralPath();
		ParallelLine pl=new ParallelLine();
		pl.gp=new GeneralPath();
		for(int j=0;j<numC-1;j++)
			{
			if((tuplas[i][j].linea.getY1() < cotaSup[j]) || (tuplas[i][j].linea.getY1() > cotaInf[j])
				|| (tuplas[i][j].linea.getY2() < cotaSup[j+1]) || (tuplas[i][j].linea.getY2() > cotaInf[j+1]))
				{
				//return null;
				pl.isBackground=false;
				}
			if(j==0)	
				pl.gp.moveTo((float)tuplas[i][j].linea.getX1(), (float)tuplas[i][j].linea.getY1());
			pl.gp.lineTo((float)tuplas[i][j].linea.getX2(), (float)tuplas[i][j].linea.getY2());
			}
		return pl;
		}
	
	private void drawLineas(Graphics2D g2) {
		
		Point2D.Double puntoInicio = new Point2D.Double(), puntoFin = new Point2D.Double();
		GeneralPath gpLineas=new GeneralPath();
		GeneralPath gpLineasSelec=new GeneralPath();
		GeneralPath gpLineasSelecBic=new GeneralPath();
		
		boolean computeFondo=false;
		if(gpLineasFondo==null)
			{
			gpLineasFondo=new GeneralPath();
			computeFondo=true;
			}
		double t1,t2;
		t1=System.currentTimeMillis();
		if(actualizarTuplas)
			{
			for(int i = 0; i < numG; i++)
				{
				for(int j = 0; j < (numC-1); j++)
					{
					puntoInicio.setLocation(margenIzq+(intervaloVar*j),
								//(maxVar[j]-datos.fieldAt(ordenVars[j]).getData(i))*ratio[j]+margenSup);
							(maxVar[j]-datos.getExpressionAt(i, ordenVars[j]))*ratio[j]+margenSup);
					puntoFin.setLocation(puntoInicio.getX()+intervaloVar,
							//(maxVar[j+1]-datos.fieldAt(ordenVars[j+1]).getData(i))*ratio[j+1]+margenSup);
							(maxVar[j+1]-datos.getExpressionAt(i, ordenVars[j+1]))*ratio[j+1]+margenSup);
							
				//	System.out.println(datos.varAt(ordenVars[j]).datoAt(i)+" "+ i+", "+j);
					Line2D.Float linea = new Line2D.Float(puntoInicio,puntoFin);
									
					Linea lineaTupla = new Linea(linea,i);				
					tuplas[i][j] = lineaTupla;											
					}
				}
			actualizarTuplas=false;
			}
		t2=System.currentTimeMillis();
		
		BiclusterSelection selecBic = this.sesion.getSelectedBicluster();
		/*TupleSelection selecPuntos = this.getDataLayer().getPointSelection();
		TupleSelection filterPuntos = this.getDataLayer().getFilterPoints();
		int ejeSelecX = -1;
		int ejeSelecY = -1;
		
		if(selecPuntos != null)
			{
			for(int i = 0; i < numC; i++)
				{
				//if(datos.fieldAt(ordenVars[i]).getName() == selecPuntos.getVarX())
				if(datos.getConditionName(ordenVars[i]) == selecPuntos.getVarX())
					ejeSelecX = i;
				//else if(datos.fieldAt(ordenVars[i]).getName() == selecPuntos.getVarY())
				else if(datos.getConditionName(ordenVars[i]) == selecPuntos.getVarY())
						ejeSelecY = i;
				}			
			}*/
		
		//------------------------------- preparación de las líneas con el gp
		
		int step=1;
		int numLineasFondo=0;
		if(numG>maxLineas)	step=numG/maxLineas;
		//System.out.println("step es "+step);
		for(int i=0;i<numG;i++)
			{
			//if(filterPuntos==null || filterPuntos.isSelectedX(i))//Si no hay filtro o este elemento ha sido filtrado
				{
				if(tuplaSeleccionada!=i)
					{
					//if(selecPuntos!=null && selecPuntos.getNumTuples()>0 && (selecPuntos.isSelectedX(i) || selecPuntos.getElementY(i)) )
					if(selecBic!=null && selecBic.getGenes().contains(i) )
						{
						/*if(selecBic==null)
							{
							gpLineasSelec.append(getLine(i), false);
							}
						else*/
							{
							int nc=selecBic.getConditions().size();
							if(nc<numC-1)
								{
								gpLineasSelecBic.append(getLine(i,0, nc-1),false);
								int init=nc-1;
								if(init<0)	init=0;
								gpLineasSelec.append(getLine(i,init, numC-1), false);
								}
							else
								{
								for(int j = 0; j < numC-1; j++)
									{
									if(j==0)	
										gpLineasSelecBic.moveTo((float)tuplas[i][j].linea.getX1(), (float)tuplas[i][j].linea.getY1());
									gpLineasSelecBic.lineTo((float)tuplas[i][j].linea.getX2(), (float)tuplas[i][j].linea.getY2());
									}
								}
							}
						}
					if(computeFondo && i%step==0)		
						{
						gpLineasFondo.append(getLine(i), false);
						numLineasFondo++;
						}
					}
				}//if(filtro)
			}
		
		t1=System.currentTimeMillis();
		if(computeFondo)
			{
			imgFondo = createImage(ancho, alto);
			Graphics2D gbTemp = ((Graphics2D)imgFondo.getGraphics());
			 RenderingHints qualityHints = new RenderingHints(null);
			    qualityHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			    qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			    gbTemp.setRenderingHints(qualityHints);   
			drawFondo(gbTemp);
			if(step>1)
				{
				int []px=new int[numC*2];
				int []py=new int[numC*2];
				for(int i=0;i<numC;i++)
					{
					py[i]=(int)(margenSup+maxVar[i]);
					px[i]=(int)(margenIzq+intervaloVar*i);
					}
				int cont=numC;
				for(int i=(numC)*2-1;i>=numC;i--)
					{
					py[i]=(int)(margenSup+longEjeY+minVar[i-(numC-1)-1]);
					px[cont++]=(int)(margenIzq+intervaloVar*(i-(numC)));
					}
				gbTemp.setPaint(new Color(220,220,220));
				gbTemp.fillPolygon(px,py,px.length);
				}
			
			gbTemp.setPaint(paleta[colorLineaOut]);
			gbTemp.draw(gpLineasFondo);
			}
		g2.drawImage(imgFondo,0,0,this);
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
			if(this.getDataLayer().getSelectionColor()!=null)
				{
				g2.setPaint(this.sesion.getSelectionColor().darker());
				g2.draw(gpLineasSelec);
				
				g2.setPaint(this.sesion.getSelectionColor().brighter());
				g2.draw(gpLineasSelecBic);
				}
			else									
				{
				g2.setPaint(paleta[colorBicluster].darker().darker());
				g2.draw(gpLineasSelec);
				g2.setPaint(paleta[colorBicluster].brighter().brighter());
				g2.draw(gpLineasSelecBic);
				}
			}
		
		if(tuplaSeleccionada > -1)
			{
			g2.setPaint(paleta[colorLineaMarcada]);
			for(int j = 0; j < (numC-1); j++)
				{
				g2.draw(tuplas[tuplaSeleccionada][j].linea);
				}
			}
		t1=System.currentTimeMillis();
	}

	private GeneralPath getLine(int i)
		{
		GeneralPath gp=new GeneralPath();
		gp=new GeneralPath();
		Line2D.Float l;
		for(int j=0;j<numC-1;j++)
			{
			l=tuplas[i][j].linea;
			if(j==0)
				gp.moveTo(l.getX1(), l.getY1());
			gp.lineTo(l.getX2(), l.getY2());
			}
		return gp;
		}
	//As above, but gives only the line between the corresponding vars
	private GeneralPath getLine(int i, int beginVar, int endVar)
		{
		GeneralPath gp=new GeneralPath();
		gp=new GeneralPath();
		Line2D.Float l;
		if(beginVar<0 || endVar>=numC)	
			System.err.println("Line out of bounds");
		for(int j=beginVar;j<endVar;j++)
			{
			l=tuplas[i][j].linea;
			if(j==beginVar)
				gp.moveTo(l.getX1(), l.getY1());
			gp.lineTo(l.getX2(), l.getY2());
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
	
	DiagramWindow getItemAñadir(){
		return this.itemAñadir;
	}
	
	DiagramWindow getItemEliminar(){
		return this.itemEliminar;
	}
	
	public void setHeight(int alto){
		this.alto = alto;
	}
	
	public void setWidth(int ancho){
		this.ancho = ancho;
	}

	/**
	 * Updates parallel coordinates with information in the session layer
	 */
	public void update() {
		repaintAll=true;
		fitScrolls();
		this.repaint();
	}
	
	
	 void fitScrolls()
		{
		//TupleSelection selecPuntos = this.getDataLayer().getPointSelection();
		//TupleSelection filterPuntos = this.getDataLayer().getFilterPoints();
		double maxLineas[]=new double[numC];
		double minLineas[]=new double[numC];
		for(int i=0;i<numC;i++)		maxLineas[i]=minLineas[i]=-111;
		
		if(sesion.getSelectedBicluster()!=null && sesion.getSelectedBicluster().getGenes().size()>0)
			{
			//1) Determine max and min values for genes
			LinkedList<Integer> lg=sesion.getSelectedGenesBicluster();
			for(int i=0;i<lg.size();i++)
				{
				int pos=lg.get(i);
				for(int j = 0; j < (numC-1); j++)
					{
					double y=0;
					if(j==0)	
						{
						y=tuplas[pos][j].linea.getY1();
						if(maxLineas[j]==-111)	maxLineas[j]=minLineas[j]=y;
						else
							{
							if(maxLineas[j]<y)	maxLineas[j]=y;
							if(minLineas[j]>y)	minLineas[j]=y;
							}
						}
					
					y=tuplas[pos][j].linea.getY2();
					if(maxLineas[j+1]==-111)	maxLineas[j+1]=minLineas[j+1]=y;
					else
						{
						if(maxLineas[j+1]<y)	maxLineas[j+1]=y;
						if(minLineas[j+1]>y)	minLineas[j+1]=y;
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
				
				
				double valor = maxVar[i]-(cotaSup[i]-margenSup+margenScroll)/ratio[i];
				currentTextSup[i]=valor;
				valor = maxVar[i]-(cotaInf[i]-margenSup-margenScroll)/ratio[i];
				currentTextInf[i]=valor;
				}
			
			//3) Highlight selected lines
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
					
					//Cambiamos las cotas
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
					calcularAtributos();
					
					actualizarTuplas=true;
					}
				}
					
			}
			
		}

	public void resize(){
		atributosIniciados = false;
		//datos = sesion.getData();
		datos = sesion.getMicroarrayData();
		this.calcularAtributos();
		this.trasladarScrolls();
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
			JPanel panelAnclajes = this.getPanelAnclajes(sesion, gestor);
			JPanel panelParametros = this.getPanelParametros();
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
	private class GestorSeleccionarTupla implements MouseListener{

		public void mouseClicked(MouseEvent e) {
			/*
			if(sesion.datosCargados())
				{
				int zonaSelec = 2;
				tuplaSeleccionada = -1;
				for(int i = 0; i < (datos.getNumVars()-1); i++){
					for(int j = 0; j < datos.getNumElems(); j++){
						if((tuplas[j][i].linea.ptSegDist(e.getPoint()) < zonaSelec)
							&& !scrollSup[i].contains(e.getPoint()) && !scrollInf[i].contains(e.getPoint())){
							tuplaSeleccionada = tuplas[j][i].numTupla;
							break;
						}
					}
					if(tuplaSeleccionada != -1)
						break;
				}				
			}*/
//			repaint();
		}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}		
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
						&& (e.getPoint().getY() > margenSup) && (e.getPoint().getY() < (alto-margenInf))
						&& !scrollSup[i].contains(e.getPoint()) && !scrollInf[i].contains(e.getPoint())) {
						posSeleccionada = i;
						varSeleccionada = ordenVars[i];
						ejeSeleccionado = ejesVars[i];
						ejeReferencia = new Line2D.Double(ejeSeleccionado.getP1(),ejeSeleccionado.getP2());
						break;
					}
				}
			}
		}
			
		public void  mouseReleased(MouseEvent e) {
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

					for(int i=0;i<ordenVars.length;i++)	System.out.print(ordenVars[i]+" ");
					double auxCotaSup = cotaSup[posSeleccionada], auxCotaInf = cotaInf[posSeleccionada];
					cotaSup[posSeleccionada] = cotaSup[nuevaPosicion];
					cotaSup[nuevaPosicion] = auxCotaSup;
					cotaInf[posSeleccionada] = cotaInf[nuevaPosicion];
					cotaInf[nuevaPosicion] = auxCotaInf;
					
					double auxYSup = scrollSup[posSeleccionada].y, auxYInf = scrollInf[posSeleccionada].y;
					scrollSup[posSeleccionada].y = scrollSup[nuevaPosicion].y;
					scrollSup[nuevaPosicion].y = auxYSup;
					scrollInf[posSeleccionada].y = scrollInf[nuevaPosicion].y;
					scrollInf[nuevaPosicion].y = auxYInf;
					
					actualizarTuplas=true;
				}
				ejeReferencia = null;
				varSeleccionada = -1;
								
				calcularAtributos();
				fitScrolls();
				update();
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
	
	// Clase gestora de los scrolls
	private class GestorScrolls implements MouseListener,MouseMotionListener {

		public void mouseClicked(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			
			if(sesion.areMicroarrayDataLoaded()){
				posRef = e.getY();
				offset = 0;
				scrollFijado = true;
				
				for(int i = 0; i < numC; i++){
					
					if(scrollSup[i].contains(e.getPoint())){
						scrollSeleccionado = scrollSup[i];
						varScroll = i;
						scrollPos = Sup;
						nuevaCota = cotaSup[i];
						posY = scrollSup[varScroll].getY();
						break;
					}
					else if(scrollInf[i].contains(e.getPoint())){
						scrollSeleccionado = scrollInf[i];
						varScroll = i;
						scrollPos = Inf;
						nuevaCota = cotaInf[i];
						posY = scrollInf[varScroll].getY();
						break;
					}
				}				
			}
		}

		public void mouseReleased(MouseEvent e) {
			if(ejeSeleccionado!=null)	return; //estamos en un cambio de ejes
			LinkedList<Integer> genes=new LinkedList<Integer>();
			LinkedList<Integer> conditions=new LinkedList<Integer>();
				
			if(sesion.areMicroarrayDataLoaded()){
					if(varScroll > -1){
					if(scrollPos == Sup){
						cotaSup[varScroll]=nuevaCota;
						currentTextSup[varScroll] =maxVar[varScroll]-(nuevaCota-margenSup+margenScroll)/ratio[varScroll];
					}
					else{
						cotaInf[varScroll] = nuevaCota;
						currentTextInf[varScroll] =maxVar[varScroll]-(nuevaCota-margenSup+margenScroll)/ratio[varScroll];
					}
				}

				TupleSelection selec=new TupleSelection("","",numG);
				//Si no se ha pulsado Control, el movimiento del scroll tiene en cuenta las 
				//restricciones existentes en otras variables
				TupleSelection selec2=selec;
				int cont=numG;
				
				if(!e.isControlDown())
					{
					for(int i=0;i<numG;i++)
						{
						selec.setX(i,true);
						selec.setY(i,true);
						selec.setLastSelec(true, i);
						selec.setColorSelec(colorSeleccion, i);
						
						for(int j=0;j<numC-1;j++)
							{
							if(j==0)
									{
								if((tuplas[i][j].linea.getY1() < cotaSup[j]) || (tuplas[i][j].linea.getY1() > cotaInf[j]))
									{
									selec.setX(i,false);
									selec.setY(i,false);
									selec.setLastSelec(false, i);
									selec.setColorSelec(null, i);
									cont--;
									break;
									}
								}
							if((tuplas[i][j].linea.getY2() < cotaSup[j+1]) || (tuplas[i][j].linea.getY2() > cotaInf[j+1]))
								{
								selec.setX(i,false);
								selec.setY(i,false);
								selec.setLastSelec(false, i);
								selec.setColorSelec(null, i);
								cont--;
								break;
								}
							}
							
						}
					}
				//Si está pulsado Control, las restricciones en otras variables se condicionan
				//al intervalo seleccionado en este scroll
				else
					{
					for(int i=0;i<numG;i++)
							{
						selec.setX(i,true);
						selec.setY(i,true);
						selec.setLastSelec(true, i);
						selec.setColorSelec(colorSeleccion, i);
						
						if(varScroll==0)
							{
							if((tuplas[i][varScroll].linea.getY1() < cotaSup[varScroll]) || (tuplas[i][varScroll].linea.getY1() > cotaInf[varScroll]))
								{
								selec.setX(i,false);
								selec.setY(i,false);
								selec.setLastSelec(false, i);
								selec.setColorSelec(null, i);
								cont--;
								}
							}
						else
							{
							if((tuplas[i][varScroll-1].linea.getY2() < cotaSup[varScroll]) || (tuplas[i][varScroll-1].linea.getY2() > cotaInf[varScroll]))
								{
								selec.setX(i,false);
								selec.setY(i,false);
								selec.setLastSelec(false, i);
								selec.setColorSelec(null, i);
								cont--;
								}
							}
							
						}
					}
				
				scrollSeleccionado = null;
				scrollFijado = false;
				varScroll = -1;

				repaintAll=true;//De momento así, pero con tiempo, si sigue yendo lento, las de fondo se pueden dejar pintadas de por vida
				//Salvo movimientos de los ejes, ojo.
				getDataLayer().setPointSelection(selec);
				
				getDataLayer().setSelectionColor(colorSeleccion);
				
				for(int i=0;i<selec.getNumTuples();i++)
					{
					if(selec.isSelectedX(i))	genes.add(Integer.valueOf(i));
					}
				for(int j=0;j<(numC-1);j++)
					{
					conditions.add(Integer.valueOf(j));
					}
				
				int maxLines=200;
				if(genes.size()>maxLines)	
					{
					JOptionPane.showMessageDialog(null,
							genes.size()+" lines selected, please select up to "+maxLines+" lines" , "Too much lines", JOptionPane.INFORMATION_MESSAGE);
					//fitScrolls();
					}
				else
					{
					sesion.setSelectedBiclusters(new BiclusterSelection(genes,conditions), "arallel");
					getWindow().update(seleccionPuntos);
					}
				
			}
		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseDragged(MouseEvent e) {
			if(sesion.areMicroarrayDataLoaded()){
					if(varScroll > -1)
					{
				
					offset = e.getY() - posRef;
										
					if(scrollPos == Sup){
						nuevaCota = cotaSup[varScroll]+offset;
						
						if((nuevaCota <= cotaInf[varScroll]) && (nuevaCota >= (margenSup-margenScroll))){
							double posX = scrollSup[varScroll].getX();
							scrollSup[varScroll].setRect(posX,posY+offset,anchoScroll,altoScroll);					
						}
						else if(nuevaCota > cotaInf[varScroll]){
							nuevaCota = cotaInf[varScroll];
						}
						else {
							nuevaCota = margenSup-margenScroll;
						}
					}
					
					if(scrollPos == Inf){
						nuevaCota = cotaInf[varScroll]+offset;
						
						if((nuevaCota >= cotaSup[varScroll]) && (nuevaCota <= (alto-margenInf+margenScroll))){
							double posX = scrollInf[varScroll].getX();
							scrollInf[varScroll].setRect(posX,posY+offset,anchoScroll,altoScroll);				
						}
						else if(nuevaCota < cotaSup[varScroll]){
							nuevaCota = cotaSup[varScroll];
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

		public void mouseDragged(MouseEvent e) 
			{
			
			}

		public void mouseMoved(MouseEvent e) {
			if(sesion.areMicroarrayDataLoaded() && atributosIniciados && diagramaPintado){
				int zonaSelec = 2;
				
				boolean zonaEje = false;
				for(int i = 0; i < ejesVars.length; i++) {
					if ((Math.abs(e.getPoint().getX()- ejesVars[i].getX1()) < zonaSelec)
						&& (e.getPoint().getY() > margenSup) && (e.getPoint().getY() < (alto-margenInf))
						&& !scrollSup[i].contains(e.getPoint()) && !scrollInf[i].contains(e.getPoint())) {
						zonaEje = true;
						break;
					}
				}
				
				boolean zonaTupla = false;
				if(!zonaEje){
					for(int i = 0; i < (numC-1); i++){
						for(int j = 0; j < numG; j++){
							if((tuplas[j][i].linea.ptSegDist(e.getPoint()) < zonaSelec)
								&& !scrollSup[i].contains(e.getPoint()) && !scrollInf[i].contains(e.getPoint())){
								zonaTupla = true;
								break;
							}
						}
						if(zonaTupla)
							break;
					}		
				}
				
				boolean zonaScroll = false;
				if(!zonaEje && !zonaTupla){
					for(int i = 0; i < numC; i++){					
						if(scrollSup[i].contains(e.getPoint()) || scrollInf[i].contains(e.getPoint())){
							zonaScroll = true;
							break;
						}
					}
				}
				
				if(zonaEje){
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				}
				else if(zonaTupla){
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				else if(zonaScroll){
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				}
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}		
		}
	}
	
	private class ParallelLine
		{
		public GeneralPath gp;
		public boolean isBackground;
		}
}