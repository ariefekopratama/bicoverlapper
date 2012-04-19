package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.*;


public class ScrollablePicture extends JLabel implements Scrollable, MouseListener {

	private static final long serialVersionUID = -8290814532621669389L;
	private int maxUnitIncrement = 1;
	private boolean missingPicture = false;
	private List<LinkItem> listaElementosImg;

	// Este es el m�todo que permite realizar la modificaci�n del
	// canal alfa de la imagen
	private AlphaComposite creaComposite(float alfa) {
		int tipo = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(tipo, alfa));
	}

	// Este m�todo es el encargado de dibujar en pantalla los
	// cuadrados azul y verde, opaco uno y con variaci�n en el canal
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

	public ScrollablePicture(ImageIcon i) {
		super(i);
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

	public ScrollablePicture(ImageIcon i, List<LinkItem> listaElementosImg) {
		super(i);
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
	// sobre el panel, pero si no sueltas, no has hecho click, s�lo has
	// presionado

	/**
	 * M�todo que detecta la presi�n del bot�n izquierdo del rat�n sobre el
	 * panel
	 */
	public void mousePressed(MouseEvent e) {
		// System.out.println("mouse Pressed "+e.getX()+" "+e.getY());
	}

	/**
	 * M�todo que detecta la liberaci�n del bot�n izquierdo del rat�n sobre el
	 * panel
	 */
	public void mouseReleased(MouseEvent e) {
		// System.out.println("mouse Released "+e.getX()+" "+e.getY());
	}

	/**
	 * M�todo que detecta la entrada del cursor del rat�n sobre el panel
	 */
	public void mouseEntered(MouseEvent e) {
		// System.out.println("mouse Entered "+e.getX()+" "+e.getY());
	}

	/**
	 * M�todo que detecta la salida del cursor del rat�n del panel
	 */
	public void mouseExited(MouseEvent e) {
		// System.out.println("mouse Exited "+e.getX()+" "+e.getY());
	}

	/**
	 * M�todo que detecta el click del bot�n izquierdo del rat�n sobre el panel
	 */
	public void mouseClicked(MouseEvent e) {
		// saySomething("Mouse clicked (# of clicks: "+ e.getClickCount() + ")",
		// e);
		// System.out.println("mouse Clicked "+e.getX()+" "+e.getY());

		if (listaElementosImg != null) {
			for (LinkItem itm : listaElementosImg) {
				if (itm.getRectangle() != null
						&& itm.getRectangle().outcode(e.getX(), e.getY()) == 0) {
					System.out.println("Rectangle: Has picado sobre "
							+ itm.getTitle());
				} else if (itm.getCircle() != null
						&& itm.getCircle().contains(
								new Point(e.getX(), e.getY()))) {
					System.out.println("Circle: Has picado sobre "
							+ itm.getTitle());
				}
			}
		}

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
	
	//Estos m�todos sirven para desplazar el scroll tantos p�xeles como se devuelven en el return
	//si hiciesen return 0 no se podr�a mover el scroll pinchando en las flechitas o en una parte del scroll donde no est� la barra
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
}
