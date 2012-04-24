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
	private JComponent panelComboBoxes;
	//private JFrame frame;
	private JComboBox combo1, combo2;
	private Definition[] definitionPathways;
	private List<LinkItem> listaElementosImg;
	
	private static Kegg kegg;
	
	private KeggDiagram panelPrincipal;

	private ImageIcon imagenPorDefecto = null;
	public static final String urlImagenPorDefecto = "http://www.uco.es/~b02robaj/sencel_archivos/image038.gif";
	
	/*
	public ViewKegg(Kegg _kegg) {
		kegg = _kegg;
		try {
			this.createAndShowGUI();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	*/
	
	public ViewKegg(Kegg _kegg, KeggDiagram _panelPrincipal) throws Exception{
		kegg = _kegg;
		panelPrincipal = _panelPrincipal;
		
        final SwingWorker worker = new SwingWorker(){  
  
            @Override  
            protected Object doInBackground() throws Exception {  
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
		this.creatMainPanel();

		panelPrincipal.add(panelImagen);
		panelImagen.setLayout(new BoxLayout(panelImagen, BoxLayout.PAGE_AXIS));
		
		//para que se muestre al menos el panel cuando se disponga de él (bastante antes que de los combos)
		panelImagen.revalidate();
		
		panelComboBoxes = new JPanel();
		panelComboBoxes.setOpaque(true);
		panelComboBoxes.setLayout(new FlowLayout());
		
		//set up the combobox pane.
		panelImagen.add(panelComboBoxes);

		this.createComboBoxes();

		// Display the window.
		//frame.pack();
		//frame.setVisible(true);
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
			        final SwingWorker worker = new SwingWorker(){  
			        	  
			            @Override  
			            protected Object doInBackground() throws Exception {  
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
				        final SwingWorker worker = new SwingWorker(){  
				        	  
				            @Override  
				            protected Object doInBackground() throws Exception {  
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

		fillComboBox2();
		//combo2.setVisible(true);
	}

	private void creatMainPanel() {
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
	}

	private void showImage(String pathway) {
		Kegg k;

		try {
			k = new Kegg();
			String url = k.generarImagenKegg(k, pathway);

			URL u = new URL(url);
			InputStream in = u.openStream();
			InputStreamReader reader = new InputStreamReader(in);

			ExtractLinks fl = new ExtractLinks(k.getKeggElements());

			listaElementosImg = fl.getLinks(reader);

			mountPanelsWithNewImage(url.replace("html", "png"), false);
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
		panelImagen.add(panelComboBoxes);		
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
}
