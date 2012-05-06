package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.*;

import keggapi.Definition;

/**
 * @author Carlos Martín Casado
 * 
 */
public class ViewKegg {

	private ScrollablePicture picture;
	private JScrollPane pictureScrollPane;
	private JComponent panelImagen;
	private JComponent panelInferior;
	private JComponent panelComboBoxes;
	private JComponent panelProgressBar;
	private JComponent panelSeleccionCondicion;
	//private JFrame frame;
	private JComboBox combo1, combo2;
	private Definition[] definitionPathways;
	private List<LinkItem> listaElementosImg;
	private JButton botonIzq, botonDer;
	private JTextField jtf;
	
	private static Kegg kegg;
	private JProgressBar progressBar;
	
	private KeggDiagram panelPrincipal;

	public static final String urlImagenPorDefecto = "http://www.uco.es/~b02robaj/sencel_archivos/image038.gif";
	
	public ViewKegg(Kegg _kegg, KeggDiagram _panelPrincipal) throws Exception{
		kegg = _kegg;
		panelPrincipal = _panelPrincipal;
		
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {  
  
            @Override  
            protected Void doInBackground() throws Exception {  
            	createAndShowGUI();
				return null;  
            }
              
        };  
          
        worker.execute();  		
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

		panelPrincipal.add(panelImagen, BorderLayout.CENTER);
		panelImagen.setLayout(new BoxLayout(panelImagen, BoxLayout.PAGE_AXIS));
		
		//para que se muestre al menos el panel cuando se disponga de él (bastante antes que de los combos)
		panelImagen.revalidate();
		
		panelProgressBar = new JPanel(new BorderLayout(10, 10));
		this.createProgressBar();
		panelPrincipal.add(panelProgressBar, BorderLayout.NORTH);
		panelPrincipal.revalidate();
		
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
		panelSeleccionCondicion = new JPanel(new BorderLayout(3, 3));
		panelSeleccionCondicion.setOpaque(true);
		
		botonIzq = new JButton(ViewKegg.crearImageIcon("es/usal/bicoverlapper/view/diagram/kegg/playIzq.png"));
		botonIzq.setBorder(null);
        botonIzq.setFocusPainted(false);
        botonIzq.setContentAreaFilled(false);
        botonIzq.setBorderPainted(false);
        botonIzq.setRolloverEnabled(false);
        botonIzq.setBackground(null);
        botonIzq.setForeground(null);
        
		botonIzq.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            	int valorActual = Integer.parseInt(jtf.getText());
            	if(valorActual-1 >= 0){
            		jtf.setText((valorActual-1) + "");
            	}
            }
        });
		
		botonDer = new JButton(ViewKegg.crearImageIcon("es/usal/bicoverlapper/view/diagram/kegg/playDer.png"));
		botonDer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            	int valorActual = Integer.parseInt(jtf.getText());
            	if(valorActual+1 < panelPrincipal.getSesion().getMicroarrayData().getNumConditions()){
            		jtf.setText((valorActual+1) + "");
            	}            
        	}
        });	
		botonDer.setBorder(null);
		botonDer.setFocusPainted(false);
		botonDer.setContentAreaFilled(false);				
		
		jtf = new JTextField("0");
		jtf.setToolTipText("Select the number of condition");
		jtf.setEditable(false);
		jtf.setPreferredSize(new Dimension(40, 25));
		jtf.setSize(new Dimension(40, 25));
		jtf.setHorizontalAlignment(JTextField.CENTER);
		
		panelSeleccionCondicion.add(botonIzq, BorderLayout.WEST);
		panelSeleccionCondicion.add(botonDer, BorderLayout.EAST);
		panelSeleccionCondicion.add(jtf, BorderLayout.CENTER);
		
		panelInferior.add(panelSeleccionCondicion, BorderLayout.EAST);
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
			if(organismo.contains(panelPrincipal.getSesion().getMicroarrayData().organism)){
				break;
			}
			organismoSeleccionado++;
		}
		combo1 = new JComboBox();
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
			            	//se pone visible la progressbar para que el usuario sepa que se está trabajando en 2º plano
			            	progressBar.setVisible(true);
			            	fillComboBox2();
			                return null;  
			            }
			              
			        };  
			          
			        worker.execute();					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		combo2 = new JComboBox();
		//combo2.setVisible(false);
		combo2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					if (combo2.getSelectedItem() != null && !combo2.getSelectedItem().equals("")){
				        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){  
				        	  
				            @Override  
				            protected Void doInBackground() throws Exception {  
				            	//se pone visible la progressbar para que el usuario sepa que se está trabajando en 2º plano
				            	progressBar.setVisible(true);				            	
				            	mountPanelsWithNewImage(urlImagenPorDefecto, true);
				            	loadKeggImage();
				                return null;  
				            }
				              
				        };  
				          
				        worker.execute();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
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
		panelImagen.setOpaque(true); // content panes must be opaque
		
		/*
		try {
			imagenPorDefecto = createImageIcon(urlImagenPorDefecto);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// Set up the scroll pane.
		picture = new ScrollablePicture(imagenPorDefecto);
		pictureScrollPane = new JScrollPane(picture);
		pictureScrollPane.setPreferredSize(new Dimension(1024, 768));
		pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

		// Put it in this panel.
		panelImagen.add(pictureScrollPane);
		*/
		
		loadImage(urlImagenPorDefecto, true);
		
		panelImagen.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));		
	}

	private void loadKeggImage() throws Exception {
		//se desactiva la posibilidad de modificar el contenido mientras se está cargando la imagen
		combo1.setEnabled(false);
		combo2.setEnabled(false);
		
		String id_pathway = kegg.getPathwayIdFromDefinition((String) combo2.getSelectedItem(), definitionPathways);

		if (id_pathway != null) {
			showImage(id_pathway);
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
			combo2.addItem(pathways[i].getDefinition());
		}
		
		//se habilita al estar relleno
		combo2.setEnabled(true);
		//se pone a invisible la progressbar
		progressBar.setVisible(false);
	}

	private void showImage(String pathway) {
		try {
			String url = kegg.generarImagenKegg(pathway, Integer.parseInt(jtf.getText()));

			URL u = new URL(url);
			InputStream in = u.openStream();
			InputStreamReader reader = new InputStreamReader(in);

			ExtractLinks fl = new ExtractLinks(kegg.getKeggElements());

			listaElementosImg = fl.getLinks(reader);

			//para que se monte el coloreado desde el programa java se pone false
			//mountPanelsWithNewImage(url.replace("html", "png"), false);
			//si se recoge la imagen ya coloreada, aquí true
			mountPanelsWithNewImage(url.replace("html", "png"), true);
		} catch (MalformedURLException mURLe) {
			mURLe.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//panelImagen.remove(panelComboBox);
		//panelImagen.remove(pictureScrollPane);
		//panelImagen.removeAll();

		// Set up the scroll pane.
		if(!isDefaultImage){
			picture = new ScrollablePicture(imagen, listaElementosImg);
		}
		else{
			picture = new ScrollablePicture(imagen);
		}
		pictureScrollPane = new JScrollPane(picture);
		pictureScrollPane.setPreferredSize(new Dimension(1024, 768));
		pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

		//panelImagen.add(pictureScrollPane);
		//panelImagen.add(panelComboBox);

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
