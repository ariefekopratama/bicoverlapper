package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import keggapi.Definition;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.controller.manager.configurationManager.ConfigurationListener;
import es.usal.bicoverlapper.controller.manager.configurationManager.ConfigurationMenuManager;
import es.usal.bicoverlapper.controller.util.Translator;
import es.usal.bicoverlapper.model.gene.GeneAnnotation;
import es.usal.bicoverlapper.view.configuration.panel.KeggParameterConfigurationPanel;
import es.usal.bicoverlapper.view.diagram.Diagram;

/**
 * Class to create Kegg view
 * 
 * @author Carlos Mart�n Casado
 *
 */
public class KeggDiagram extends Diagram {
	private static final long serialVersionUID = 1L;
	private Session sesion;
	private int alto;
	private int ancho;
	private Kegg kegg;
	private ScrollablePicture picture;
	private JScrollPane pictureScrollPane;
	private JComponent panelImagen;
	private JComponent panelInferior;
	private JComponent panelComboBoxes;
	private JComponent panelProgressBar;
	private JComponent panelInferiorDerecha;
	private JComboBox combo1, combo2;
	private Definition[] definitionPathways;
	private List<LinkItem> listaElementosImg;
	private JButton botonFlechaIzq, botonFlechaDer;
	private JTextField jtf;
	private JButton botonObtenerImagen;
	
	private JProgressBar progressBar;
	
	private int valorActualCondition;
	private String organism;

	public static final String urlImagenPorDefecto = "es/usal/bicoverlapper/resources/images/keggDefaultImage.gif";	
	
	//todo lo de aqu� para abajo son atributos para el tema de los cuantiles
	private boolean configurando = false;
	
	private int scaleModeKegg;
	
	//configuraci�n del color
	private static final int lowColor = 0;
	private static final int zeroColor = 1;
	private static final int highColor = 2;
	private static final int selectionColor = 3;
	private static final int hoverColor = 4;
	
	private Color[] paleta;
	private JTextField[] muestraColor;
	private String[] textoLabel = { 	"Lowest Expression", "Zero Expression", 
										"Highest expression", "Selection", "Hover" 
								};	
	
	/**
	 * Default constructor
	 */	
	public KeggDiagram(){
		super();
	}
	
	/**
	 * Session Constructor
	 * @param session Session in which this diagram is in. It must have TRN data loaded
	 * @param dim Dimension for this diagram
	 */	
	public KeggDiagram(Session sesion, Dimension dim) {
		super(new BorderLayout());
		
		scaleModeKegg = sesion.getScaleMode();
		
		int num = sesion.getNumHeatmapDiagrams();
		this.setName("Kegg " + num);
		this.sesion = sesion;

		this.alto = (int) dim.getHeight();
		this.ancho = (int) dim.getWidth();
		this.setPreferredSize(new Dimension(ancho, alto));
		this.setSize(ancho, alto);

		//System.out.println("ESTO ES LA BIBLIOTECA sesion.getMicroarrayData().chip="+sesion.getMicroarrayData().chip);
		//System.out.println("ESTO ES EL NOMBRE DEL ORGANISMO sesion.getMicroarrayData().organism="+sesion.getMicroarrayData().organism);
		//System.out.println("ESTO ES LA BIBLIOTECA SI ARRIBA DA BIOMART sesion.getMicroarrayData().rname="+sesion.getMicroarrayData().rname);
		
		paleta = new Color[] { 	
								sesion.lowExpColor, sesion.avgExpColor,
								sesion.hiExpColor, sesion.getSelectionColor(),
								sesion.getHoverColor() 
							};		
		muestraColor = new JTextField[paleta.length];
	}
	
	/**
	 * Kegg Diagram creation
	 */
	public void create() {		
		try {
			kegg = new Kegg(sesion);
			
			//La creaci�n se hace en un hilo para no congelar la interfaz gr�fica
	        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {  
	  
	            @Override  
	            protected Void doInBackground() throws Exception {  
	            	createAndShowKeggGUI();
					return null;  
	            }
	              
	        };  
	          
	        worker.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Updates the diagram by retrieving the last selection of data
	 */
	public void update(){
		//si onlyHover es false y hay genes seleccionados...
		//hay que meter la comprobaci�n de "null != sesion.getSelectedBicluster()" porque si esto es nulo, el m�todo sesion.getSelectedGenesBicluster() da nullpointerexception
		//esto deber�a estar controlado en Session, pero yo no lo toco no siendo que repercuta en otro lado...
		if(!sesion.onlyHover && null != sesion.getSelectedBicluster() && null != sesion.getSelectedGenesBicluster()){
			//se recogen los id de los genes seleccionados
			List<String> genes = mapearInternalIdconIdGen(sesion.getSelectedGenesBicluster());
			List<Rectangle2D.Double> elementosKeggSeleccionados = new ArrayList<Rectangle2D.Double>();
			
			if (!genes.isEmpty() && listaElementosImg != null) {
				//se buscan coincidencias entre los genes seleccionados y los elementos de Kegg en la imagen
				for (LinkItem itm : listaElementosImg) {
					//si se dispone de los nombres de genes de ese elemento...
					if(null != itm.getGeneNames()){
						for (String gen : genes) {
							for (String genItm : itm.getGeneNames()) {
								if(genItm.equals(gen)){
									//si se encuentra coincidencia, se a�ade ese elemento para marcar como seleccionado en la imagen de Kegg
									elementosKeggSeleccionados.add(itm.getRectangle());
								}
							}
						}
					}								
				}
			}
			
			//si hay alg�n elemento que contenga alg�n gen seleccionado, se marcar�
			if(!elementosKeggSeleccionados.isEmpty()){
				picture.setRectangles(elementosKeggSeleccionados);
				picture.setDibujarBordeKeggElement(true);
				picture.repaint();
			}
			//si no hay ning�n elemento con ninguno de los genes seleccionadios, se dejar� la imagen tal cual
			else{
				picture.getRectangles().clear();
				picture.setDibujarBordeKeggElement(false);
				picture.repaint();
			}
		}
		else{
			//si no se ha seleccionado ning�n gen, entonces se deja la imagen original
			picture.getRectangles().clear();
			picture.setDibujarBordeKeggElement(false);
			picture.repaint();			
		}
	}
	
	/**
	 * Mapping internal ids with gen ids
	 * @param internalIdsSelected List with the internal ids
	 * @return Selected genes
	 */
	private List<String> mapearInternalIdconIdGen(List<Integer> internalIdsSelected) {
		//creaci�n de la lista de genes seleccionados
		List<String> genesSeleccionados = new LinkedList<String>();
		//obtenci�n de los genes presentes en el experimento
		Map<Integer, GeneAnnotation> mapaGenes = sesion.getMicroarrayData().getGeneAnnotations();
		//para cada uno de los genes presentes en el experimento...
		for (GeneAnnotation g : mapaGenes.values()) {
			//para cada uno de los internalId seleccionados
			for (Integer gen : internalIdsSelected) {
				//si coinciden
				if(g.internalId == (gen)){
					//se a�ade el id del gen a la lista de genes seleccionados
					genesSeleccionados.add(g.id);
				}
			}
		}
		return genesSeleccionados;
	}

	/**
	 * Makes the panel visible
	 */
	public synchronized void run() {
		this.getWindow().setVisible(true);
	}

	/**
	 * Create the Kegg GUI
	 */
	private void createAndShowKeggGUI() {
		//creaci�n del panel que albergar� la imagen
		this.creteImagePanel();
		
		//creaci�n de la barra de progreso que indicar� que se est�n cargando elementos de Kegg
		this.createProgressBar();
				
		//creaci�n del panel inferior
		//albergar� a los comboboxes, las opciones de selecci�n de condici�n y el bot�n para llamar al servidor y obtener la imagen		
		this.createLowerPanel();
	}
	
	/**
	 * Create the lower panel
	 */
	private void createLowerPanel() {		
		//creaci�n del panel inferior
		panelInferior = new JPanel(new BorderLayout(10, 10));
		panelInferior.setOpaque(true);
		
		//medidas predeterminadas principalmente por la altura, para que no se hagan demasiado anchos y antiest�ticos los combos
		panelInferior.setPreferredSize(new Dimension(100, 25));
		panelInferior.setMaximumSize(new Dimension(10000, 25));		
		
		//se a�ade el panel inferior al panel principal de la imagen
		panelImagen.add(panelInferior);		
				
		//creaci�n del panel de los comboboxes
		this.createComboBoxesPanel();
		//este panel contiene, en la parte central, al panel de los comboboxes
		panelInferior.add(panelComboBoxes, BorderLayout.CENTER);
				
		//creaci�n del panel de las condiciones
		this.createConditionsPanel();
	}

	/**
	 * Create the panel with the Kegg image
	 */	
	private void creteImagePanel() {
		//creaci�n del panel 
		panelImagen = new JPanel();
		panelImagen.setLayout(new BoxLayout(panelImagen, BoxLayout.LINE_AXIS));
		panelImagen.setOpaque(true);
		
		//se le establece al panel una imagen por defecto para que no est� en gris
		this.loadImage(urlImagenPorDefecto, true);
		
		//Se le establece un borde al panel
		panelImagen.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));		
		
		//se coloca el panel de la imagen en el centro de esta ventana
		this.add(panelImagen, BorderLayout.CENTER);
		panelImagen.setLayout(new BoxLayout(panelImagen, BoxLayout.PAGE_AXIS));
		
		//para que se muestre al menos el panel cuando se disponga de �l, dado que montar toda la interfaz tomar� un tiempo...
		panelImagen.revalidate();		
	}

	/**
	 * Create the panel with the available conditions
	 */
	private void createConditionsPanel() {
		//se crea un panel que albergar� lo correspondiente a la selecci�n de las condiciones
		//adem�s, tambi�n albergar� el bot�n "Get image" para realizar la llamada al servidor de Kegg y obtener la imagen coloreada
		panelInferiorDerecha = new JPanel(new BorderLayout(3, 3));
		panelInferiorDerecha.setOpaque(true);
		
		//creaci�n del bot�n izquierdo
		botonFlechaIzq = new JButton(KeggDiagram.createImageIcon("es/usal/bicoverlapper/resources/images/playIzq.png"));
		botonFlechaIzq.setToolTipText("Use the arrows to choose the condition");
		//mientras se use el skin, estas opciones son ignoradas...
		botonFlechaIzq.setBorder(null);
        botonFlechaIzq.setFocusPainted(false);
        botonFlechaIzq.setContentAreaFilled(false);
        botonFlechaIzq.setBorderPainted(false);
        //cuando se pulse sobre el boton de la izquierda, aparecer� seleccionada la condici�n anterior
        //si se llega a la condici�n m�s baja, no se hace nada
		botonFlechaIzq.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            	if(valorActualCondition-1 >= 0){
            		valorActualCondition--;
            		String texto = sesion.getMicroarrayData().getConditionName(valorActualCondition);
            		jtf.setText(texto);
            		jtf.setToolTipText(texto);
            	}
            }
        });
		
		//creaci�n del bot�n derecho
		botonFlechaDer = new JButton(KeggDiagram.createImageIcon("es/usal/bicoverlapper/resources/images/playDer.png"));
		botonFlechaDer.setToolTipText("Use the arrows to choose the condition");
        //cuando se pulse sobre el boton de la derecha, aparecer� seleccionada la condici�n siguiente
        //si se llega a la condici�n m�s alta, no se hace nada		
		botonFlechaDer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            	if(valorActualCondition+1 < sesion.getMicroarrayData().getNumConditions()){
            		valorActualCondition++;
            		String texto = sesion.getMicroarrayData().getConditionName(valorActualCondition);
            		jtf.setText(texto);
            		jtf.setToolTipText(texto);
            	}            
        	}
        });	
		//mientras se use el skin, estas opciones son ignoradas...		
		botonFlechaDer.setBorder(null);
		botonFlechaDer.setFocusPainted(false);
		botonFlechaDer.setContentAreaFilled(false);		
		botonFlechaDer.setBorderPainted(false);
		
		//creaci�n del campo de texto que contendr� el nombre de la condici�n (por defecto contendr� la de la posici�n 0)
		jtf = new JTextField(this.sesion.getMicroarrayData().getConditionName(0));
		valorActualCondition = 0;
		jtf.setToolTipText(this.sesion.getMicroarrayData().getConditionName(0));
		jtf.setEditable(false);
		jtf.setPreferredSize(new Dimension(100, 25));
		jtf.setSize(new Dimension(100, 25));
		jtf.setHorizontalAlignment(JTextField.CENTER);
		
		//creaci�n del panel que albergar� los 3 elementos relativos a la selecci�n de la condici�n
		JPanel panelSeleccionCondicion = new JPanel(new BorderLayout(3, 3));
		//se a�aden los citados elementos al panel
		panelSeleccionCondicion.add(botonFlechaIzq, BorderLayout.WEST);
		panelSeleccionCondicion.add(botonFlechaDer, BorderLayout.EAST);
		panelSeleccionCondicion.add(jtf, BorderLayout.CENTER);
		
		//este panel con la selecci�n de la condici�n se colocar� en la parte derecha del panel inferior derecha
		panelInferiorDerecha.add(panelSeleccionCondicion, BorderLayout.WEST);
		
		//creaci�n del bot�n para realizar la llamada al servidor de Kegg y obtener la imagen
		botonObtenerImagen = new JButton("Get image");
		botonObtenerImagen.setToolTipText("Click here to get the image. You should choose 1 organism and 1 pathway first.");
		botonObtenerImagen.setEnabled(false);
		//este bot�n posee un oyente que har� que, cuando sea pulsado, se desactiven todos los dem�s botones de Kegg, se muestre una barra de progreso y se realice la llamada al servidor
		//todo esto adem�s se har� desde un hilo que no bloquear� la interfaz del programa
		botonObtenerImagen.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
		        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){  
		        	  
		            @Override  
		            protected Void doInBackground() throws Exception {  
		            	//se deshabilitan los botones y combos
		            	botonObtenerImagen.setEnabled(false);
		            	botonFlechaIzq.setEnabled(false);
		            	botonFlechaDer.setEnabled(false);
		        		combo1.setEnabled(false);
		        		combo2.setEnabled(false);		            	
		            	//se pone visible la progressBar para que el usuario sepa que se est� trabajando en 2� plano
		            	progressBar.setVisible(true);				         
		            	//se monta en el panel la imagen por defecto
		            	mountPanelsWithNewImage(urlImagenPorDefecto, true);
		            	//se realiza la obtenci�n de la imagen
		            	getKeggImage();
		            	//se habilitan de nuevo los botones y combox
		            	botonObtenerImagen.setEnabled(true);
		            	botonFlechaIzq.setEnabled(true);
		            	botonFlechaDer.setEnabled(true);		
		        		combo1.setEnabled(true);
		        		combo2.setEnabled(true);	
		        		//se desactiva de la vista la progressBar
		            	progressBar.setVisible(false);		
		            	
		                return null;  
		            }
		              
		        };  
		          
		        worker.execute();				
			}
			
		});
		//se a�ade el citado bot�n al panel inferior derecha
		panelInferiorDerecha.add(botonObtenerImagen, BorderLayout.EAST);
		
		//el panel inferior derecha se a�ade, como parece l�gico, en la parte derecha del panel inferior
		panelInferior.add(panelInferiorDerecha, BorderLayout.EAST);
	}

	/**
	 * Create the panel with the progress bar
	 */	
	private void createProgressBar() {
		//creaci�n del panel para la barra de progreso
		panelProgressBar = new JPanel(new BorderLayout(10, 10));
		//creaci�n de la barra de progreso
		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(70,15));
		//texto que aparecer� si se pasa el rat�n por encima de la barra de progreso
		progressBar.setToolTipText("Loading KEGG information...");
		//es indeterminada, es decir, no se puede saber qu� porcentaje del trabajo est� hecho 
		//esto es debido a que se realiza en un servidor externo ajeno a este programa
		progressBar.setIndeterminate(true);
		//se coloca la barra en la parte derecha del panel para as� aparecer en la parte superior derecha
		panelProgressBar.add(progressBar, BorderLayout.EAST);
		//el panel de la barra de progreso aparecer� en la parte superior de la ventana
		this.add(panelProgressBar, BorderLayout.NORTH);
		this.revalidate();		
	}

	/**
	 * Create the panel with combobox 
	 */
	private void createComboBoxesPanel() {
		//creaci�n del Layout para los comboboxes y sus separaciones
		GridLayout layoutComoBoxes = new GridLayout(1,2);
		layoutComoBoxes.setHgap(10);
		layoutComoBoxes.setVgap(10);
		
		//creaci�n del panel que albergar� los comboboxes
		panelComboBoxes = new JPanel(layoutComoBoxes);
		panelComboBoxes.setOpaque(true);
		
		//se obtiene la lista de organismos
		String[] organismosSeleccionables = kegg.getOrganisms();
		int organismoSeleccionado = 0;
		//se busca en la lista el organismo cuyo microarray ha sido cargado por BicOverlapper
		for (String organismo : organismosSeleccionables) {
			if(organismo.contains(this.sesion.getMicroarrayData().organism)){
				organism = organismo;
				break;
			}
			organismoSeleccionado++;
		}
		combo1 = new JComboBox();
		combo1.setToolTipText("Choose an organism");
		//se a�aden todos los organismos al desplegable
		ComboBoxModel comboBox1Model = new DefaultComboBoxModel(organismosSeleccionables);
		combo1.setModel(comboBox1Model);
		//se selecciona en el desplegable el organismo cuyo microarray ha sido cargado, siempre que este se encuentre entre los de la lista obtenida
		//o sea, que el �ndice de organismoSeleccionado sea menor que la longitud total de los organismosSeleccionables
		if(organismoSeleccionado < organismosSeleccionables.length){
			combo1.setSelectedIndex(organismoSeleccionado);
		}
		//en caso de no encontrarse en la lista, se avisa al usuario
		else{
			String msgError = "Organism "+sesion.getMicroarrayData().organism+" not found among KEGG organisms, please select one from the leftmost combo box";
			JOptionPane.showMessageDialog(null, msgError, "Error", JOptionPane.ERROR_MESSAGE);
		}
		combo1.setPreferredSize(new Dimension(351, 23));
		combo1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {	
			        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){  
			        	  
			            @Override  
			            protected Void doInBackground() throws Exception {  
			            	botonObtenerImagen.setEnabled(false);
			            	//se pone visible la progressbar para que el usuario sepa que se est� trabajando en 2� plano
			            	progressBar.setVisible(true);
			            	fillComboBox2();
			                return null;  
			            }
			              
			        };  
			          
			        worker.execute();					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		combo2 = new JComboBox();
		combo2.setToolTipText("Choose a pathway");
		combo2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					if (combo2.getSelectedItem() != null && !combo2.getSelectedItem().equals("")){
						botonObtenerImagen.setEnabled(true);
					}
					else{
						botonObtenerImagen.setEnabled(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		panelComboBoxes.add(combo1);
		panelComboBoxes.add(combo2);

		//a continuaci�n se hace imprescindible rellenar el combobox2
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){  
      	  
            @Override  
            protected Void doInBackground() throws Exception {  
            	fillComboBox2();
                return null;  
            }
              
        };  
          
        worker.execute();
	}

	/**
	 * Load image from Kegg server in the Kegg image panel
	 */
	private void getKeggImage() {
		//se obtiene el identificador del pathway
		String id_pathway = kegg.getPathwayIdFromDefinition((String) combo2.getSelectedItem(), definitionPathways);
		
		System.out.println("id_pathway = "+id_pathway);
		
		if (id_pathway != null) {
			//se muestra la imagen
			showImage(id_pathway);			
			//una vez mostrada la iamgen, se actualiza el Diagram por si hay elementos seleccionados
			this.update();
		}
	}

	/**
	 * Fill combobox2 with the pathways
	 * @throws Exception
	 */
	private void fillComboBox2() throws Exception {
		//se deshabilita para que mientras se est� rellenando no se pueda tocar
		combo2.setEnabled(false);
		
		// se desea mostrar los pathways del organismo seleccionado en el combobox1
		String organismId = kegg.getOrganismId((String) combo1.getSelectedItem());

		Definition[] pathways = kegg.getDefinitionPathwaysFromOrganism(organismId);
		definitionPathways = pathways;
		combo2.removeAllItems();
		combo2.addItem("");
		for (int i = 0; i < pathways.length; i++) {
			//s�lo se coge la cadena hasta el "-", ya que lo que hay despu�s es el nombre del organismo
			combo2.addItem(pathways[i].getDefinition().replace(" - "+organism, ""));
			//combo2.addItem(pathways[i].getDefinition());
		}
		
		//se habilita al estar relleno
		combo2.setEnabled(true);
		//se pone a invisible la progressbar
		progressBar.setVisible(false);
	}

	/**
	 * Show the image
	 * @param pathway Pathway of the image
	 */
	private void showImage(String pathway) {
		try {
			String url = kegg.generateKeggImage(pathway, valorActualCondition);

			URL u = new URL(url);
			InputStream in = u.openStream();
			InputStreamReader reader = new InputStreamReader(in);

			ExtractLinks fl = new ExtractLinks(kegg.getKeggElements());

			listaElementosImg = fl.getLinks(reader);

			//para que se monte el coloreado desde el programa java se pone false
			//mountPanelsWithNewImage(url.replace("html", "png"), false);
			//pero para poder extraer los links y que act�e el oyente he comentado el paintComponent en ScrollablePicture
			//as�, aqu� pondr� ahora false y no se colorear� en java, s�lo se activar� el mouseListener
			mountPanelsWithNewImage(url.replace("html", "png"), false);
		} catch (MalformedURLException mURLe) {
			mURLe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mount panels of Kegg window with a new image
	 * @param url Image url
	 * @param isDefaultImage Boolean to indicate if the image to show is the default image or not
	 */
	private void mountPanelsWithNewImage(String url, boolean isDefaultImage) {
		//se eliminan todos los elementos del panel
		panelImagen.removeAll();
		//se carga la nueva imagen a mostrar
		loadImage(url, isDefaultImage);
		//se montan los elementos restantes del panel
		panelImagen.add(panelInferior);		
	}
	
	/**
	 * Load an image in the panelImagen
	 * @param url Image url
	 * @param isDefaultImage Boolean to indicate if the image to show is the default image or not
	 */
	private void loadImage(String url, boolean isDefaultImage) {
		// Get the image to use.
		ImageIcon imagen = null;
		imagen = KeggDiagram.createImageIcon(url);

		//Se crea el scrollpane
		if(!isDefaultImage){
			picture = new ScrollablePicture(imagen, listaElementosImg, this.sesion, valorActualCondition, this);
		}
		else{
			picture = new ScrollablePicture(imagen, this.sesion, this);
		}
		pictureScrollPane = new JScrollPane(picture);
		pictureScrollPane.setPreferredSize(new Dimension(1024, 768));
		pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));
		
		//se a�ade el oyente para el panel de configuraci�n al ScrollPane que contendr� la imagen
		//pero tambi�n ser� necesario a�ad�rselo a la propia imagen si se desea que funcione este bot�n derecho sobre ella
		pictureScrollPane.addMouseListener(new ConfigurationListener(this));

		//se a�ade el JScrollPane al panel de la imagen
		panelImagen.add(pictureScrollPane);
		
		//para que se recargue el panel con la imagen nueva es necesario llamar a revalidate()
		panelImagen.revalidate();
	}
	
    /**
     * Returns an ImageIcon, or null if the path was invalid
     * @param path Image path
     * @return ImageIcon with the image of the path
     */
    public static ImageIcon createImageIcon(String path) {
    	URL imgURL = null;
    	//si la ruta empieza por http, ser� una imagen en internet
    	if(path.startsWith("http")){
    		try {
				imgURL = new URL(path);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
    	}
    	//si no, ser� una imagen local
    	else{
    		imgURL = ClassLoader.getSystemResource(path);
    	}
    	
    	//si se ha conseguido una imagen, se devuelve el ImageIcon correspondiente
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } 
        //si no, se devuelve null
        else {
            System.err.println("Couldn't find file:" + path);
            return null;
        }
    }    	
    
	/**
	 * Pops up a configuration panel for heatmap visual properties
	 */
	public void configure() {
		if (!configurando) {
			configurando = true;
			JInternalFrame ventanaConfig = this.getVentanaConfig();

			// Obtenemos el gestor de eventos de configuracion
			ConfigurationMenuManager gestor = new ConfigurationMenuManager(this, ventanaConfig, paleta, muestraColor);

			JPanel panelColor = this.getPanelPaleta(paleta, textoLabel, muestraColor);
			JPanel panelParametros = new KeggParameterConfigurationPanel(sesion);
			this.setPanelParametros(panelParametros);
			JPanel panelBotones = this.getPanelBotones(gestor);

			// Configuramos la ventana de configuracion
			//this.initPanelConfig(panelColor, null, panelParametros, panelBotones);
			//para que s�lo salgan los par�metros
			this.initPanelConfig(null, null, panelParametros, panelBotones);

			ventanaConfig.setTitle(Translator.instance.configureLabels.getString("s1") + " " + this.getName());
			sesion.getDesktop().add(ventanaConfig);
			try {
				ventanaConfig.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			ventanaConfig.pack();

			//Con esto se mostrar�a como se muestra en todos los dem�s diagramas
			//ventanaConfig.setLocation(getPosition());
			//pero parece ser que quiere que salga centrada			
			int posicionX = (sesion.getMainWindow().getWidth()/2) - (ventanaConfig.getWidth()/2);
			int posicionY = (sesion.getMainWindow().getHeight()/2) - (ventanaConfig.getHeight()/2);
			//ventanaConfig.setLocation(sesion.getMainWindow().getWidth()/2, sesion.getMainWindow().getHeight()/2);
			ventanaConfig.setLocation(posicionX, posicionY);	
			
			//Se hace visible la ventana
			ventanaConfig.setVisible(true);
		}
	}    
	
	/**
	 * Notifies the end of configuration
	 */
	public void endConfig(boolean ok) {
		if (!ok) {
			configurando = false;
			return;
		}
		sesion.setSelectionColor(paleta[KeggDiagram.selectionColor]);
		sesion.setHoverColor(paleta[KeggDiagram.hoverColor]);

		//si el tipo de escala actual es diferente al que ha seleccionado el usuario...
		int scaleModeSelectedByUser = ((KeggParameterConfigurationPanel) this.getPanelParametros()).getScaleModeSelected();
		if(scaleModeKegg != scaleModeSelectedByUser){
			//se establece ese tipo de escala en la sesi�n
			sesion.setScaleMode(scaleModeSelectedByUser);
			scaleModeKegg = scaleModeSelectedByUser;
			//se informa al usuario que la nueva escala se usar� en la pr�xima imagen que se cargue
			//String msgInfo = "The new scale mode will be applied when you get a new image";
			//JOptionPane.showMessageDialog(null, msgInfo, "Information", JOptionPane.INFORMATION_MESSAGE);			
			
			//parece ser que al final se desea que se recargue la imagen directamente
			botonObtenerImagen.doClick();
		}
		
		sesion.updateConfigExcept(this.getName());
		this.configurando = false;
		
		System.out.println("scaleModeKegg = "+scaleModeKegg);
	}
	
	public void updateConfig() {		
		paleta[KeggDiagram.selectionColor] = sesion.getSelectionColor();
		paleta[KeggDiagram.hoverColor] = sesion.getHoverColor();
		
		scaleModeKegg = sesion.getScaleMode();
		
		repaintAll = true;
		this.repaint();
	}	
}
