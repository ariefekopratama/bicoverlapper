package diagrams;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;

import data.Field;

import prefuse.Visualization;
import prefuse.controls.FocusControl;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

import kernel.BiclusterSelection;
import kernel.TupleSelection;
import kernel.Session;

/**
 * Extension of prefuse FocusControl for mouse interaction management in TRN Diagram
 * @author Rodrigo Santamaria
 *
 */
class TRNFocusControl extends FocusControl
	{
    private boolean enabled;
    private Session sesion;
    private String group;
    private String activity;
    private LinkedList<Integer> nodosSeleccionados;
    private LinkedList<Integer> condicionesSeleccionadas;//all, allways
    private Visualization visualization;
	private Field ejeX, ejeY;//Los ejes que sean nos dan un poco igual, siempre que sea alguna de las condiciones
	private TupleSelection puntosSelec;
    
	/**
	 * Constructor
	 * @param session	Session to which this controller has to listen/update for changes
	 * @param activity	ActionList linked to this FocusControl
	 * @param group		Item group linked to this FocusControl
	 * @param v			Visualization linked to this FocusControl
	 */
    public TRNFocusControl(Session session, String activity, String group, Visualization v)
		{
		super(group, 1, activity);
		this.activity=activity;
		this.group=group;
		enabled=true;
		this.sesion=session;
		nodosSeleccionados=new LinkedList<Integer>();
		condicionesSeleccionadas=new LinkedList<Integer>();
		if(session.getMicroarrayData()!=null)
			for(int i=0;i<session.getMicroarrayData().getNumConditions()-1;i++)//TODO: arreglar esto con nuevas cosas en BiclusterSelection, y no tener que andar seleccionando condiciones y marranadas
				condicionesSeleccionadas.add((Integer)i);
	
		visualization=v;
		}

    /**
     * Indicates if this Control is currently enabled.
     * @return true if the control is enabled, false if disabled
     */
    public boolean isEnabled(){ return enabled;}
    
    /**
     * Sets the enabled status of this control.
     * @param enabled true to enable the control, false to disable it
     */
    public void setEnabled(boolean enabled){this.enabled=enabled;}
    
//  -- Actions performed on VisualItems ------------------------------------

    /**
     * Checks for mouse events, changing Session status if necessary
     */
    public void itemClicked(VisualItem item, MouseEvent e)
     	{
    	if ( !filterCheck(item) ) return;
        if ( UILib.isButtonPressed(e, button) &&
             e.getClickCount() == ccount )
        	{
            if ( item != curFocus ) //Añadimos al foco uno que no es el último añadido
            	{
                Visualization vis = item.getVisualization();
                TupleSet ts = vis.getFocusGroup(group);

                
                boolean ctrl = e.isControlDown();
                if ( !ctrl ) //Este es el único que estará en el focus
                	{
                	curFocus = item;
                    ts.clear();//nuevo, para borrar mierda que se pudiera meter con los addItem
                    ts.setTuple(item);

                    nodosSeleccionados.clear();
                    nodosSeleccionados.add(Integer.valueOf(item.getRow()));
                	System.out.println("Selecionamos + "+nodosSeleccionados.size()+" y "+condicionesSeleccionadas.size());
                    sesion.setSelectedBiclusters(new BiclusterSelection(nodosSeleccionados, condicionesSeleccionadas), "Transcription");
                	
            		
                	}

                else if ( ts.containsTuple(item) ) //Ya estaba, lo quitamos, tanto si hay ctrl como si no
                	{
                	System.out.println("Este nodo ya estaba, lo quitamos");
                	
                    nodosSeleccionados.remove(Integer.valueOf(item.getRow()));//Me coge el índice, no el elemento
                	ts.removeTuple(item);
                	if(nodosSeleccionados.size()==0)	sesion.setSelectedBiclusters(new BiclusterSelection(nodosSeleccionados, new LinkedList<Integer>()), "Transcription");
                	else								sesion.setSelectedBiclusters(new BiclusterSelection(nodosSeleccionados, condicionesSeleccionadas), "Transcription");
                	}
                	else 
                	{
               		System.out.println("Hay ctrl, añadimos sin mas");
                	nodosSeleccionados.add(Integer.valueOf(item.getRow()));
                  	sesion.setSelectedBiclusters(new BiclusterSelection(nodosSeleccionados, condicionesSeleccionadas), "Transcription");
                    ts.addTuple(item);

	            	/*if(puntosSelec!=null)
	            		{
	            		puntosSelec.setX(item.getRow(), true);
	            		sesion.setSelecPuntos(puntosSelec, "Transcription");
	                	}*/
					}
                runActivity(vis);
             
            	
            	}
            else if ( e.isControlDown() ) //Si no es el últimos añadido y pulsamos control
            	{
            	System.out.println("Si no es el último añadido y hay ctrl, lo quitamos");
                Visualization vis = item.getVisualization();
                TupleSet ts = vis.getFocusGroup(group);
                ts.removeTuple(item);
                curFocus = null;
                
            	nodosSeleccionados.remove(nodosSeleccionados.indexOf(item.getRow()));
            	if(nodosSeleccionados.size()==0)	sesion.setSelectedBiclusters(new BiclusterSelection(nodosSeleccionados, new LinkedList<Integer>()), "Transcription");
            	else								sesion.setSelectedBiclusters(new BiclusterSelection(nodosSeleccionados, condicionesSeleccionadas), "Transcription");
        //    	sesion.setSelectedBiclusters(new SeleccionBiclusters(nodosSeleccionados, new LinkedList<Integer>()), "Transcription");

            	/*if(puntosSelec!=null)
	            	{
	            	puntosSelec.setX(item.getRow(), false);
					sesion.setSelecPuntos(puntosSelec, "Transcription");
	            	}*/
            	
                runActivity(vis);
            	}
        	}
     	}
    
    //Cuando se utilizan estos métodos, no se vuelve a llamar a Sesión, porque caeríamos
    //en un bucle infinito!
	void addItem(VisualItem item)
		{
		System.out.println("TRN: Vamos a añadir items");
		
		Visualization vis=item.getVisualization();
		TupleSet ts = vis.getFocusGroup(group);
		ts.addTuple(item);
		nodosSeleccionados.add(item.getRow());//antes add(item)
		runActivity(vis);
		}
	
	void addItems(VisualItem[] item)
		{
		for(int i=0;i<item.length;i++)	addItem(item[i]);
		}
	
	
	void clear()
		{
		System.out.println("TRN: Vamos a borrar");
		if(nodosSeleccionados.size()>0)
			{
			TupleSet ts = visualization.getFocusGroup(group);
			ts.clear();
			nodosSeleccionados.clear();
			}
		}
    
    
    private void runActivity(Visualization vis) 
    	{
        if ( activity != null )          vis.run(activity);
    	}
	}
