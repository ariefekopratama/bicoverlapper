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

	public KeggDiagram(){
		super();
	}
	
	public KeggDiagram(Session sesion, Dimension dim) {
		super(new BorderLayout());
		int num = sesion.getNumHeatmapDiagrams();
		this.setName("Kegg " + num);
		this.sesion = sesion;
		//this.md = sesion.getMicroarrayData();
		this.alto = (int) dim.getHeight();
		this.ancho = (int) dim.getWidth();
		this.setPreferredSize(new Dimension(ancho, alto));
		this.setSize(ancho, alto);
		/*
		this.paleta = new Color[] { sesion.lowExpColor, sesion.avgExpColor,
				sesion.hiExpColor, sesion.getSelectionColor(),
				sesion.getHoverColor() };
		muestraColor = new JTextField[paleta.length];
		*/
	}
	
	public void create() {
		v = new Visualization();
		
		this.crearViewKegg();
	}	
	
	private void crearViewKegg() {
		Kegg kegg;
		try {
			kegg = new Kegg();
			ViewKegg vk = new ViewKegg(kegg, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void run() {
		this.getWindow().setVisible(true);
	}
}
