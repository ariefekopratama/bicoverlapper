package es.usal.bicoverlapper.view.diagram.kegg;

import keggapi.PathwayElement;

public class KeggElement {

	//elemento propiamente dicho
	private PathwayElement element;
	//coloreado
	private String foreground;
	private String background;
	
	public KeggElement(PathwayElement _element, String _foreground, String _background){
		element = _element;
		foreground = _foreground;
		background = _background;	
	}

	public PathwayElement getElement() {
		return element;
	}
	
	
	public String getForeground() {
		return foreground;
	}

	public String getBackground() {
		return background;
	}

	/**
	 * Método que retorna los nombres del elemento (lo que voy a necesitar para buscar en el título y mapear un elemento del html con uno de estos)
	 * @return
	 */
	public String[] getNames() {
		return element.getNames();
	}	
}
