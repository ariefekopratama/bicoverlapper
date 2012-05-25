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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import keggapi.Definition;

import prefuse.Visualization;
import es.usal.bicoverlapper.controller.kernel.Selection;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.model.gene.GeneAnnotation;
import es.usal.bicoverlapper.view.diagram.Diagram;

public class KeggDiagram extends Diagram {
	private static final long serialVersionUID = 1L;
	private Session sesion;
	private int alto;
	private int ancho;
	private Visualization v;
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

	public static final String urlImagenPorDefecto = "http://www.uco.es/~b02robaj/sencel_archivos/image038.gif";	
	
	public KeggDiagram(){
		super();
	}
	
	public KeggDiagram(Session sesion, Dimension dim) {
		super(new BorderLayout());
		int num = sesion.getNumHeatmapDiagrams();
		this.setName("Kegg " + num);
		this.sesion = sesion;

		this.alto = (int) dim.getHeight();
		this.ancho = (int) dim.getWidth();
		this.setPreferredSize(new Dimension(ancho, alto));
		this.setSize(ancho, alto);

		System.out.println("ESTO ES LA BIBLIOTECA sesion.getMicroarrayData().chip="+sesion.getMicroarrayData().chip);
		System.out.println("ESTO ES EL NOMBRE DEL ORGANISMO sesion.getMicroarrayData().organism="+sesion.getMicroarrayData().organism);
		System.out.println("ESTO ES LA BIBLIOTECA SI ARRIBA DA BIOMART sesion.getMicroarrayData().rname="+sesion.getMicroarrayData().rname);
		
	}
	
	public void create() {
		v = new Visualization();
		
		this.crearViewKegg();
	}	
	
	
	
	public void update(){
		//si onlyHover es false y hay genes seleccionados...
		//hay que meter la comprobación de "null != sesion.getSelectedBicluster()" porque si esto es nulo, el método sesion.getSelectedGenesBicluster() da nullpointerexception
		//esto debería estar controlado en Session, pero yo no lo toco no siendo que repercuta en otro lado...
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
									//si se encuentra coincidencia, se añade ese elemento para marcar como seleccionado en la imagen de Kegg
									elementosKeggSeleccionados.add(itm.getRectangle());
								}
							}
						}
					}								
				}
			}
			
			//si hay algún elemento que contenga algún gen seleccionado, se marcará
			if(!elementosKeggSeleccionados.isEmpty()){
				picture.setRectangles(elementosKeggSeleccionados);
				picture.setDibujarBordeKeggElement(true);
				picture.repaint();
			}
			//si no hay ningún elemento con ninguno de los genes seleccionadios, se dejará la imagen tal cual
			else{
				picture.getRectangles().clear();
				picture.setDibujarBordeKeggElement(false);
				picture.repaint();
			}
		}
		else{
			//si no se ha seleccionado ningún gen, entonces se deja la imagen original
			picture.getRectangles().clear();
			picture.setDibujarBordeKeggElement(false);
			picture.repaint();			
		}
	}
	
	private List<String> mapearInternalIdconIdGen(List<Integer> internalIds) {
		List<String> genesSeleccionados = new LinkedList<String>();
		Map<Integer, GeneAnnotation> mapaGenes = sesion.getMicroarrayData().getGeneAnnotations();
		for (GeneAnnotation g : mapaGenes.values()) {
			for (Integer gen : internalIds) {
				if(g.internalId == (gen)){
					genesSeleccionados.add(g.id);
				}
			}
		}
		return genesSeleccionados;
	}
	
	private void crearViewKegg() {
		try {
			kegg = new Kegg(sesion);
			
	        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {  
	  
	            @Override  
	            protected Void doInBackground() throws Exception {  
	            	createAndShowGUI();
					return null;  
	            }
	              
	        };  
	          
	        worker.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void run() {
		this.getWindow().setVisible(true);
	}
	
	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 * 
	 * @throws MalformedURLException
	 */
	protected static ImageIcon createImageIcon(String path)	throws MalformedURLException {
		URL imgURL = new URL(path);
		return new ImageIcon(imgURL);
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 * 
	 * @throws Exception
	 */
	private void createAndShowGUI() throws Exception {
		// Create and set up the content pane.
		this.creatPanelImagen();

		this.add(panelImagen, BorderLayout.CENTER);
		panelImagen.setLayout(new BoxLayout(panelImagen, BoxLayout.PAGE_AXIS));
		
		//para que se muestre al menos el panel cuando se disponga de él (bastante antes que de los combos)
		panelImagen.revalidate();
		
		panelProgressBar = new JPanel(new BorderLayout(10, 10));
		this.createProgressBar();
		this.add(panelProgressBar, BorderLayout.NORTH);
		this.revalidate();
		
		//se crea el Layout y sus separaciones
		GridLayout layoutComoBoxes = new GridLayout(1,2);
		layoutComoBoxes.setHgap(10);
		layoutComoBoxes.setVgap(10);
		
		panelComboBoxes = new JPanel(layoutComoBoxes);
		panelComboBoxes.setOpaque(true);
		
		panelInferior = new JPanel(new BorderLayout(10, 10));
		panelInferior.setOpaque(true);
		panelInferior.add(panelComboBoxes, BorderLayout.CENTER);
		
		//medidas predeterminadas principalmente por la altura, para que no se hagan demasiado anchos y antiestéticos los combos
		panelInferior.setPreferredSize(new Dimension(100, 25));
		panelInferior.setMaximumSize(new Dimension(10000, 25));		
		
		//set up the combobox pane.
		panelImagen.add(panelInferior);

		this.createComboBoxes();
		this.createPanelCondiciones();
	}

	private void createPanelCondiciones() {
		panelInferiorDerecha = new JPanel(new BorderLayout(3, 3));
		panelInferiorDerecha.setOpaque(true);
		
		botonFlechaIzq = new JButton(KeggDiagram.crearImageIcon("es/usal/bicoverlapper/view/diagram/kegg/playIzq.png"));
		botonFlechaIzq.setToolTipText("Use the arrows to choose the condition");
		botonFlechaIzq.setBorder(null);
        botonFlechaIzq.setFocusPainted(false);
        botonFlechaIzq.setContentAreaFilled(false);
        botonFlechaIzq.setBorderPainted(false);
        botonFlechaIzq.setRolloverEnabled(false);
        botonFlechaIzq.setBackground(null);
        botonFlechaIzq.setForeground(null);
        
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
		
		botonFlechaDer = new JButton(KeggDiagram.crearImageIcon("es/usal/bicoverlapper/view/diagram/kegg/playDer.png"));
		botonFlechaDer.setToolTipText("Use the arrows to choose the condition");
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
		botonFlechaDer.setBorder(null);
		botonFlechaDer.setFocusPainted(false);
		botonFlechaDer.setContentAreaFilled(false);				
		
		jtf = new JTextField(this.sesion.getMicroarrayData().getConditionName(0));
		valorActualCondition = 0;
		jtf.setToolTipText(this.sesion.getMicroarrayData().getConditionName(0));
		jtf.setEditable(false);
		jtf.setPreferredSize(new Dimension(100, 25));
		jtf.setSize(new Dimension(100, 25));
		jtf.setHorizontalAlignment(JTextField.CENTER);
		
		JPanel panelSeleccionCondicion = new JPanel(new BorderLayout(3, 3));
		panelInferiorDerecha.add(panelSeleccionCondicion, BorderLayout.WEST);
		
		botonObtenerImagen = new JButton("Get image");
		botonObtenerImagen.setToolTipText("Click here to get the image. You should choose 1 organism and 1 pathway first.");
		botonObtenerImagen.setEnabled(false);
		botonObtenerImagen.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
		        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){  
		        	  
		            @Override  
		            protected Void doInBackground() throws Exception {  
		            	botonObtenerImagen.setEnabled(false);
		            	botonFlechaIzq.setEnabled(false);
		            	botonFlechaDer.setEnabled(false);
		            	//se pone visible la progressbar para que el usuario sepa que se está trabajando en 2º plano
		            	progressBar.setVisible(true);				            	
		            	mountPanelsWithNewImage(urlImagenPorDefecto, true);
		            	loadKeggImage();
		            	botonObtenerImagen.setEnabled(true);
		            	botonFlechaIzq.setEnabled(true);
		            	botonFlechaDer.setEnabled(true);		            	
		                return null;  
		            }
		              
		        };  
		          
		        worker.execute();				
			}
			
		});
		panelInferiorDerecha.add(botonObtenerImagen, BorderLayout.EAST);
		
		panelSeleccionCondicion.add(botonFlechaIzq, BorderLayout.WEST);
		panelSeleccionCondicion.add(botonFlechaDer, BorderLayout.EAST);
		panelSeleccionCondicion.add(jtf, BorderLayout.CENTER);
		
		panelInferior.add(panelInferiorDerecha, BorderLayout.EAST);
	}


	private void createProgressBar() {
		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(70,15));
		progressBar.setToolTipText("Loading KEGG information...");
		progressBar.setIndeterminate(true);
		panelProgressBar.add(progressBar, BorderLayout.EAST);
	}


	private void createComboBoxes() throws Exception {
		//se obtiene la lista de organismos
		String[] organismosSeleccionables = kegg.getOrganism();
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
		//se añaden todos los organismos al desplegable
		ComboBoxModel comboBox1Model = new DefaultComboBoxModel(organismosSeleccionables);
		combo1.setModel(comboBox1Model);
		//se selecciona en el desplegable el organismo cuyo microarray ha sido cargado
		combo1.setSelectedIndex(organismoSeleccionado);
		combo1.setPreferredSize(new java.awt.Dimension(351, 23));
		combo1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {	
			        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){  
			        	  
			            @Override  
			            protected Void doInBackground() throws Exception {  
			            	botonObtenerImagen.setEnabled(false);
			            	//se pone visible la progressbar para que el usuario sepa que se está trabajando en 2º plano
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

		//a continuación se hace imprescindible rellenar el combobox2
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){  
      	  
            @Override  
            protected Void doInBackground() throws Exception {  
            	fillComboBox2();
                return null;  
            }
              
        };  
          
        worker.execute();
	}

	private void creatPanelImagen() {
		panelImagen = new JPanel();
		panelImagen.setLayout(new BoxLayout(panelImagen, BoxLayout.LINE_AXIS));
		panelImagen.setOpaque(true);
		
		loadImage(urlImagenPorDefecto, true);
		
		panelImagen.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));		
	}

	private void loadKeggImage() throws Exception {
		//se desactiva la posibilidad de modificar el contenido mientras se está cargando la imagen
		combo1.setEnabled(false);
		combo2.setEnabled(false);
		
		String id_pathway = kegg.getPathwayIdFromDefinition((String) combo2.getSelectedItem(), definitionPathways);
		
		System.out.println("id_pathway = "+id_pathway);
		
		if (id_pathway != null) {
			//se muestra la imagen
			showImage(id_pathway);			
			//una vez mostrada la iamgen, se actualiza el Diagram por si hay elementos seleccionados
			this.update();
		}
		
		//se habilitan los combobox de nuevo
		combo1.setEnabled(true);
		combo2.setEnabled(true);	
		//se desactiva de la vista la progressBar
    	progressBar.setVisible(false);
	}

	private void fillComboBox2() throws Exception {
		//se deshabilita para que mientras se está rellenando no se pueda tocar
		combo2.setEnabled(false);
		
		// se desea mostrar los pathways del organismo seleccionado en el combobox1
		String organismId = kegg.searchOrganism((String) combo1.getSelectedItem());

		Definition[] pathways = kegg.getDefinitionPathwaysFromOrganism(organismId);
		definitionPathways = pathways;
		combo2.removeAllItems();
		combo2.addItem("");
		for (int i = 0; i < pathways.length; i++) {
			//sólo se coge la cadena hasta el "-", ya que lo que hay después es el nombre del organismo
			combo2.addItem(pathways[i].getDefinition().replace(" - "+organism, ""));
			//combo2.addItem(pathways[i].getDefinition());
		}
		
		//se habilita al estar relleno
		combo2.setEnabled(true);
		//se pone a invisible la progressbar
		progressBar.setVisible(false);
	}

	private void showImage(String pathway) {
		try {
			String url = kegg.generarImagenKegg(pathway, valorActualCondition);

			URL u = new URL(url);
			InputStream in = u.openStream();
			InputStreamReader reader = new InputStreamReader(in);

			ExtractLinks fl = new ExtractLinks(kegg.getKeggElements());

			listaElementosImg = fl.getLinks(reader);

			//para que se monte el coloreado desde el programa java se pone false
			//mountPanelsWithNewImage(url.replace("html", "png"), false);
			//pero para poder extraer los links y que actúe el oyente he comentado el paintComponent en ScrollablePicture
			//así, aquí pondré ahora false y no se coloreará en java, sólo se activará el mouseListener
			mountPanelsWithNewImage(url.replace("html", "png"), false);
		} catch (MalformedURLException mURLe) {
			mURLe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void mountPanelsWithNewImage(String url, boolean isDefaultImage) {
		//se eliminan todos los elementos del panel
		panelImagen.removeAll();
		//se carga la nueva imagen a mostrar
		loadImage(url, isDefaultImage);
		//se montan los elementos restantes del panel
		panelImagen.add(panelInferior);		
	}
	
	private void loadImage(String url, boolean isDefaultImage) {
		// Get the image to use.
		ImageIcon imagen = null;
		try {
			imagen = createImageIcon(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		//Se crea el scrollpane
		if(!isDefaultImage){
			picture = new ScrollablePicture(imagen, listaElementosImg, this.sesion, valorActualCondition);
		}
		else{
			picture = new ScrollablePicture(imagen, this.sesion);
		}
		pictureScrollPane = new JScrollPane(picture);
		pictureScrollPane.setPreferredSize(new Dimension(1024, 768));
		pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

		panelImagen.add(pictureScrollPane);
		
		//para que se recargue el panel con la imagen nueva es necesario llamar a revalidate()
		panelImagen.revalidate();
	}
	
    /**
     * Método que crear un ImageIcon a partir de una ruta
     * @param path Ruta de la imagen
     * @return ImageIcon con la imagen contenida en el path
     */
    public static ImageIcon crearImageIcon(String path) {
        java.net.URL imgURL = ClassLoader.getSystemResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("No se encuentra el fichero:" + path);
            return null;
        }
    }    	
}
