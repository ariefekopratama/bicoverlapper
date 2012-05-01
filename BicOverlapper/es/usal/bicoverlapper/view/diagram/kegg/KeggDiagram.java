package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.BorderLayout;
import java.awt.Dimension;

import prefuse.Visualization;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.view.diagram.Diagram;

public class KeggDiagram extends Diagram {
	private static final long serialVersionUID = 1L;
	private Session sesion;
	private int alto;
	private int ancho;
	private Visualization v;
	private Kegg kegg;

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
	
	private void crearViewKegg() {
		try {
			kegg = new Kegg(sesion);
			ViewKegg vk = new ViewKegg(kegg, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void run() {
		this.getWindow().setVisible(true);
	}

	public Session getSesion() {
		return sesion;
	}

	public void setSesion(Session sesion) {
		this.sesion = sesion;
	}
}
