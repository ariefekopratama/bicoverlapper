package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.geom.Rectangle2D;

public class LinkItem {
	private String link;
	private String title;
	private String text;
	private String shape;
	private String coords;
	private Rectangle2D.Double rectangle;
	private Circle circle;
	// inicializo los colores en blano por si no se encontrase el color del
	// elemento para que no falle el programa
	private String fg = "#000000";;
	private String bg = "#000000";;

	public Circle getCircle() {
		return circle;
	}

	public void setCircle(Circle circle) {
		this.circle = circle;
	}

	public Rectangle2D.Double getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle2D.Double rectangle) {
		this.rectangle = rectangle;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getCoords() {
		return coords;
	}

	public void setCoords(String coords) {
		this.coords = coords;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFg() {
		return fg;
	}

	public String getBg() {
		return bg;
	}

	public void setFg(String fg) {
		this.fg = fg;
	}

	public void setBg(String bg) {
		this.bg = bg;
	}

}