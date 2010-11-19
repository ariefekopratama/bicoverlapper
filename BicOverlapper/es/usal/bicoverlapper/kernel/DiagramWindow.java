package es.usal.bicoverlapper.kernel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyVetoException;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;


import es.usal.bicoverlapper.data.DataLayer;
import es.usal.bicoverlapper.utils.ArrayUtils;
import es.usal.bicoverlapper.visualization.diagrams.Diagram;

/**
 * This JInternalFrame implements the frame in which diagrams will be drawn.
 * It basically controls the Session layer management
 * 
 * @author Javier Molpeceres and Rodrigo Santamar�a
 */
public class DiagramWindow extends JInternalFrame {

	private static final long serialVersionUID = -2835508191615121342L;
	
	private DiagramWindow thisVentana;
	private Diagram diagram;
	private Vector<String> hooks;
	private Session sesion;
	private boolean flag = true, grupoDefault = true;
	
	/**
	 * Basic Constructor 
	 * @param session	Session layer to which this DiagramWindow has notify and read changes
	 * @param desktop	JDesktopPane in which the DiagramWindow is in
	 * @param diagram		Diagram to display in this DiagramWIndow
	 */
	public DiagramWindow (Session session,JDesktopPane desktop,Diagram diagram){
		this.diagram = diagram;
		this.sesion = session;
		this.setContentPane(diagram);
		this.setTitle(diagram.getName());
		this.diagram.setWindow(this);
		this.pack();
		this.setClosable(true);
		this.setResizable(true);
		this.setMaximizable(true);
						
		GestorVentana gestor = new GestorVentana();
		this.addComponentListener(gestor);
		this.addInternalFrameListener(gestor);
				
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		//this.setLocation(sesion.getNuevaVentanaPos());
		this.setVisible(true);
		
		this.diagram.setFocusable(true);
		this.diagram.addKeyListener(sesion);
		
		desktop.add(this);
		try {
			this.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		this.thisVentana = this;
		this.hooks = new Vector<String>(0,1);
	}
	
	/**
	 * Returns the diagram held by this DiagramWindow
	 * @return	Diagram held by this DiagramWindow
	 */	public Diagram getDiagram(){
		return this.diagram;
	}
	
	 
	/*void propagar(DataLayer capaDatos){
		this.getDiagram().setDataLayer(capaDatos);
		for(int i = 0; i < this.hooks.size(); i++){
			DiagramWindow ventana = sesion.getDiagramWindow(this.hooks.elementAt(i));
			if(ventana.getDiagram().getDataLayer() != capaDatos)
				ventana.propagar(capaDatos);
		}
	}*/
	
	/**
	 * Sets the update status for this DiagramWindow
	 * By default, all DiagramWindows are linked
	 * TODO: STILL IN DEVELPOMENT
	 * @param status true if there is an exclusive group of DiagramWindows linked to this one. 
	 * 			Otherwise, this DiagramWindow is linked to every other
	 */
	public void setExclusiveGroupStatus(boolean status){
		this.grupoDefault = status;
	}
	
	/**
	 * Returns true if there is a group of DiagramWindows to be linked to this one.
	 * By default, all DiagramWindows are linked
	 * TODO: STILL IN DEVELPOMENT
	 * @return true if there is a group of DiagramWindow linked to this one
	 */
	public boolean isExclusiveGroup(){
		return this.grupoDefault;
	}
	
	void setFlag(boolean flag){
		this.flag = flag;
	}
	
	/**
	 * Returns the id for this DiagramWindow. Id is unique for each kind of diagram (@see Diagram)
	 * @return	the id for this DiagramWindow
	 */
	public int getId(){
		return this.diagram.getId();
	}
	
	/**
	 * Return the size of the diagram held
	 * @return	the size of the Diagram
	 */
	public Dimension getPanelSize(){
		return this.diagram.getSize();
	}
		
	/**
	 * STILL IN DEVELOPMENT
	 * Hooks serve to define an specific set of DiagramWindows from which this DiagramWindow
	 * listen for changes.
	 * Default behaviour is to listen for any DiagramWindow, and is the one implemented by now
	 * @param hooks
	 */
	public void setHooks(Vector<String> hooks){
		this.hooks = hooks;
	}
	
	/**
	 * STILL IN DEVELOPMENT
	 * @return	a vector with the titles of the windows linked to this DiagramWindow
	 */
	public Vector<String> getHooks(){
		return this.hooks;
	}
	
	/**
	 * Sets the palette of Colors to use for this DiagramWindow
	 * @param p	Color[] with colors to use in the Diagram
	 */
	public void setPalette(Color[] p){
		this.diagram.setPalette(p);
	}
	
	/**
	 * Returns the color palette used by this diagram.
	 * The indices in each diagram explain what meaning have each color
	 * @return	The palette for this diagram
	 */
	public Color[] getPaleta(){
		return this.diagram.getColors();
	}
	
	// a�ade ventana a la lista de anclajes
	/**
	 * TODO: STILL IN DEVELOPMENT
	 *//*
	public void addHook(DiagramWindow dw){
		if((dw != null) && !this.hooks.contains(dw.getTitle())){
			if(grupoDefault){
				sesion.removeFromGrupoDefecto(dw.getTitle());
				grupoDefault = false;
				if(dw.isExclusiveGroup()){
					this.diagram.setDataLayer(new DataLayer(this.sesion.getDataLayer()));					
				}
				else{
					this.diagram.setDataLayer(dw.getDiagram().getDataLayer());					
				}					
			}
			else{
				if(dw.isExclusiveGroup()){
					
				}
				else{
					if(this.getDiagram().getDataLayer() != dw.getDiagram().getDataLayer()){
						this.propagar(dw.getDiagram().getDataLayer());
					}
				}
			}
			this.hooks.addElement(dw.getTitle());
			dw.setFlag(this.flag);
			dw.addHook(this);
		}
	}
	*/
	// elimina ventana de la lista de anclajes
	/**
	 * TODO: STILL IN DEVELOPMENT
	 */
	/*public void removeHook(DiagramWindow dw){
		if((dw != null) && this.hooks.contains(dw.getTitle())){
			for(int i = 0; i < this.hooks.size(); i++){
				if(this.hooks.elementAt(i).equals(dw.getTitle())){
					this.hooks.remove(i);
					break;
				}
			}
			dw.removeHook(this);
			if(this.hooks.isEmpty()){
				sesion.addToGrupoDefecto(dw);
				grupoDefault = true;
				this.getDiagram().setDataLayer(sesion.getDataLayer());
			}
		}
	}*/
	
	/**
	 * Returns the data layer from the session, which includes all the different sources of data loaded
	 * @return	the DataLayer
	 * @deprecated
	 */
	/*public DataLayer getDataLayer()
		{
		return this.sesion.getDataLayer();
		}*/
	
	void update(boolean flag, int[] updatableIds){
		if(this.flag != flag)
			this.update(updatableIds);
	}
	
	void sortColumns(int [] columnOrder)
		{
		//To be modified by child classes
		}
	/**
	 * Sends a message to all DiagramWindows with id in updatableIds to
	 * update their visualizations with the data state in Session layer
	 * @param updatableIds	int[] list with DiagramWindow ids to update
	 */
	public void update(int[] updatableIds){
		
		this.diagram.update();
		this.flag = !flag;
	//	System.out.println("Actualizando....");
		if(!grupoDefault){
			for(int i = 0; i < hooks.size(); i++)
				{
				DiagramWindow ventana = sesion.getDiagramWindow(hooks.elementAt(i));
			//	System.out.println(ventana.getId());
				if(ArrayUtils.contains(updatableIds,ventana.getId())){
					ventana.update(this.flag,updatableIds);					
				}
			}			
		}
		else
			{
		//	System.out.println("Actualizamos todos los ids actualizables por esta vista");
			sesion.update(updatableIds);
			}
	}
	
	void updateDiagram(){
	//	System.out.println("Actualizando panel de ventana "+this.getId());
		this.diagram.update();
	}

	void actualizarDatos(){
		this.diagram.updateData();		
	}
	
	// clase gestora del comportamiento de la ventana
	private class GestorVentana implements InternalFrameListener, ComponentListener{
		
		public void componentResized(ComponentEvent e) {
			
			DiagramWindow ventana = (DiagramWindow)e.getSource();
			Diagram panel = (Diagram)getContentPane();
			panel.setHeight(ventana.getContentPane().getHeight());
			panel.setWidth(ventana.getContentPane().getWidth());
			panel.resize();		
		}

		public void componentMoved(ComponentEvent e) {
			diagram.setPosition(getLocation());
		}

		public void componentShown(ComponentEvent e) {}

		public void componentHidden(ComponentEvent e) {}

		public void internalFrameOpened(InternalFrameEvent e) {}

		public void internalFrameClosing(InternalFrameEvent e) {
			/*if(hooks!=null)
				{	
				int numAnclajes = hooks.size();
				for(int i = 0; i < numAnclajes; i++)
					{
					DiagramWindow ventana = sesion.getDiagramWindow(hooks.firstElement());
					ventana.removeHook(thisVentana);
					}
				}*/
		}

		public void internalFrameClosed(InternalFrameEvent e) {
			
			sesion.removeVentana(thisVentana);
			diagram.removeAll();
			Runtime.getRuntime().gc();
		}

		public void internalFrameIconified(InternalFrameEvent e) {}

		public void internalFrameDeiconified(InternalFrameEvent e) {}

		public void internalFrameActivated(InternalFrameEvent e) {}

		public void internalFrameDeactivated(InternalFrameEvent e) {}
	}	
}