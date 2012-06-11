package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import es.usal.bicoverlapper.controller.kernel.Selection;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.model.gene.GeneAnnotation;


public class ScrollablePicture extends JLabel implements Scrollable, MouseListener {

	private static final long serialVersionUID = -8290814532621669389L;
	private int maxUnitIncrement = 1;
	private boolean missingPicture = false;
	private List<LinkItem> listaElementosImg;
	private Session sesion;
	private int valorActualCondition;
	
	private boolean dibujarBordeKeggElement = false;
	private List<Rectangle2D.Double> rectangles = new ArrayList<Rectangle2D.Double>();
	
	//parámetro que indica la distancia a la cual se dibujará el borde de selección del elemento
	public static final int distanciaAlElemento = 2;
	
	// Este es el método que permite realizar la modificación del
	// canal alfa de la imagen
	private AlphaComposite creaComposite(float alfa) {
		int tipo = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(tipo, alfa));
	}

	// Este método es el encargado de dibujar en pantalla los
	// cuadrados azul y verde, opaco uno y con variación en el canal
	// alfa del cuadrado verde
	private void dibujaCuadrados(Graphics2D g2, float alfa, Rectangle2D.Double rectangle, String bg) {
		Composite compOriginal = g2.getComposite();
		// Ahora se modifica el canal alfa del objeto para poderlo hacer
		// transparente
		g2.setComposite(creaComposite(alfa));
		g2.setPaint(Color.decode(bg));
		g2.fill(rectangle);
		// Se recuperan los valores originales del objeto Graphics2D
		g2.setComposite(compOriginal);
	}

	private void dibujaCirculos(Graphics2D g2, float alfa, Circle circle, String bg) {
		Composite compOriginal = g2.getComposite();
		// Ahora se modifica el canal alfa del objeto para poderlo hacer
		// transparente
		g2.setComposite(creaComposite(alfa));
		g2.setPaint(Color.decode(bg));
		g2.fillOval((int) (circle.getCenter().getX() - circle.getRadius()),
				(int) (circle.getCenter().getY() - circle.getRadius()),
				2 * (int) circle.getRadius(), 2 * (int) circle.getRadius());
		// Se recuperan los valores originales del objeto Graphics2D
		g2.setComposite(compOriginal);
	}

	/**
	 * Método que colorea la imagen con la imagen extraída del html
	 * 
	 * En principio DEPRECATED puesto que coloreo la imagen en el servidor
	 */
	/*
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if (listaElementosImg != null) {
			for (LinkItem itm : listaElementosImg) {
				if (itm.getRectangle() != null) {
					dibujaCuadrados(g2, 0.3F, itm.getRectangle(), itm.getBg());
				} else if (itm.getCircle() != null) {
					dibujaCirculos(g2, 0.3F, itm.getCircle(), itm.getBg());
				}
			}
		}
	}
	*/

	public ScrollablePicture(ImageIcon i, Session _sesion) {
		super(i);
		sesion = _sesion;
		if (i == null) {
			missingPicture = true;
			setText("No picture found.");
			setHorizontalAlignment(CENTER);
			setOpaque(true);
			setBackground(Color.white);
		}

		//the movement of the scrollbar depends on the screen
		maxUnitIncrement = (int)((double)Toolkit.getDefaultToolkit().getScreenResolution() / (double)2.54);

		// Let the user scroll by dragging to outside the window.
		setAutoscrolls(true); // enable synthetic drag events
		addMouseListener(this); // handle mouse drags
	}

	public ScrollablePicture(ImageIcon i, List<LinkItem> listaElementosImg, Session _sesion, int numCondition) {
		super(i);
		sesion = _sesion;
		valorActualCondition = numCondition;
		if (i == null) {
			missingPicture = true;
			setText("No picture found.");
			setHorizontalAlignment(CENTER);
			setOpaque(true);
			setBackground(Color.white);
		}
		
		//the movement of the scrollbar depends on the screen
		maxUnitIncrement = (int)((double)Toolkit.getDefaultToolkit().getScreenResolution() / (double)2.54);

		// Let the user scroll by dragging to outside the window.
		setAutoscrolls(true); // enable synthetic drag events
		addMouseListener(this); // handle mouse drags
		this.listaElementosImg = listaElementosImg;

		repaint();
	}

	// La diferencia entre presionar y hacer click, es que puedes presionar
	// sobre el panel, pero si no sueltas, no has hecho click, sólo has
	// presionado

	/**
	 * Método que detecta la presión del botón izquierdo del ratón sobre el
	 * panel
	 */
	public void mousePressed(MouseEvent e) {
		// System.out.println("mouse Pressed "+e.getX()+" "+e.getY());
	}

	/**
	 * Método que detecta la liberación del botón izquierdo del ratón sobre el
	 * panel
	 */
	public void mouseReleased(MouseEvent e) {
		// System.out.println("mouse Released "+e.getX()+" "+e.getY());
	}

	/**
	 * Método que detecta la entrada del cursor del ratón sobre el panel
	 */
	public void mouseEntered(MouseEvent e) {
		// System.out.println("mouse Entered "+e.getX()+" "+e.getY());
	}

	/**
	 * Método que detecta la salida del cursor del ratón del panel
	 */
	public void mouseExited(MouseEvent e) {
		// System.out.println("mouse Exited "+e.getX()+" "+e.getY());
	}

	/**
	 * Método que detecta el click del botón izquierdo del ratón sobre el panel y coloreará si fuese pertinene el elemento
	 */
	public void mouseClicked(MouseEvent e) {
		this.detectarYColorearSeleccion(e);
	}
	
	/**
	 * Detección de la posición de clic de ratón y coloreado del elemento en caso de que se haya pinchado sobre alguno
	 * @param e Evento de ratón
	 */
	private void detectarYColorearSeleccion(MouseEvent e) {
		if (listaElementosImg != null) {
			for (LinkItem itm : listaElementosImg) {
				if (itm.getRectangle() != null && itm.getRectangle().outcode(e.getX(), e.getY()) == 0) {
					System.out.println("Rectangle: Has picado sobre "+ itm.getTitle());
					
					//se actualizan el resto de vistas con la selección
					LinkedList<Integer> conditions = new LinkedList<Integer>();
					conditions.add(valorActualCondition);
					
					//si se dispone de los nombres de genes de ese elemento...
					if(null != itm.getGeneNames()){
						LinkedList<Integer> genesSeleccionados = this.mapearGenesConInternalId(itm.getGeneNames());
						if(null != genesSeleccionados){
							//parámetros para pintar el reborde del rectángulo
							dibujarBordeKeggElement = true;
							rectangles.add(itm.getRectangle());
							this.repaint();
							
							//Si no se quiere actualizar la propia vista Kegg
							//sesion.setSelectedBiclustersExcept(new Selection(genesSeleccionados, conditions), "Kegg");	
							
							//Si se desea actualizar la propia vista Kegg (así se consigue que si se selecciona un elemento y hay repetidos en la imagen, se autoseleccionen)
							sesion.setSelectedBicluster(new Selection(genesSeleccionados, conditions));	
							sesion.updateAll();
							
							//una vez encontrada coincidencia, en principio no habría problema en salir del bucle
							break;
						}
						else{
							JOptionPane.showMessageDialog(null, "Sorry, views can't be updated", "Error", JOptionPane.ERROR_MESSAGE);	
						}
					}
					
				} else if (itm.getCircle() != null && itm.getCircle().contains(new Point(e.getX(), e.getY()))) {
					System.out.println("Circle: Has picado sobre "+ itm.getTitle());
				}
				else{
					//si se hace clic y no hay coincidencias, se pintará la imagen sin nada seleccionado
					//dibujarBordeKeggElement = false;
					//rectangles.clear();
					//repaint();
				}
			}
		}		
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if(dibujarBordeKeggElement){
			//se modifica el grosor de las líneas
		    g2.setStroke(new BasicStroke(3.0f));			
		    //se modifica el color de las líneas
		    g2.setPaint(sesion.getSelectionColor());
		    for (Rectangle2D.Double rectangle : rectangles) {
		    	g2.drawRect((int)rectangle.getX() - distanciaAlElemento, (int)rectangle.getY() - distanciaAlElemento, (int)rectangle.getWidth()+(2*distanciaAlElemento), (int)rectangle.getHeight()+(2*distanciaAlElemento));
			}
		}
	}		

	private LinkedList<Integer> mapearGenesConInternalId(String[] geneNames) {
		LinkedList<Integer> genesSeleccionados = new LinkedList<Integer>();
		Map<Integer, GeneAnnotation> mapaGenes = sesion.getMicroarrayData().getGeneAnnotations();
		for (GeneAnnotation g : mapaGenes.values()) {
			for (String gen : geneNames) {
				if(g.id.equals(gen)){
					genesSeleccionados.add(g.internalId);
				}
			}
		}
		return genesSeleccionados;
	}

	public Dimension getPreferredSize() {
		if (missingPicture) {
			return new Dimension(320, 480);
		} else {
			return super.getPreferredSize();
		}
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public void setMaxUnitIncrement(int pixels) {
		maxUnitIncrement = pixels;
	}
	
	//Estos métodos sirven para desplazar el scroll tantos píxeles como se devuelven en el return
	//si hiciesen return 0 no se podría mover el scroll pinchando en las flechitas o en una parte del scroll donde no esté la barra
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// Get the current position.
		int currentPosition = 0;
		if (orientation == SwingConstants.HORIZONTAL) {
			currentPosition = visibleRect.x;
		} else {
			currentPosition = visibleRect.y;
		}

		// Return the number of pixels between currentPosition
		// and the nearest tick mark in the indicated direction.
		if (direction < 0) {
			int newPosition = currentPosition
					- (currentPosition / maxUnitIncrement) * maxUnitIncrement;
			return (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			return ((currentPosition / maxUnitIncrement) + 1)
					* maxUnitIncrement - currentPosition;
		}
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width - maxUnitIncrement;
		} else {
			return visibleRect.height - maxUnitIncrement;
		}
	}

	public List<Rectangle2D.Double> getRectangles() {
		return rectangles;
	}

	public void setRectangles(List<Rectangle2D.Double> rectangles) {
		this.rectangles = rectangles;
	}

	public boolean isDibujarBordeKeggElement() {
		return dibujarBordeKeggElement;
	}

	public void setDibujarBordeKeggElement(boolean dibujarBordeKeggElement) {
		this.dibujarBordeKeggElement = dibujarBordeKeggElement;
	}
}
