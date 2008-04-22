package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;


import es.usal.bicoverlapper.data.Field;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.TupleSelection;

import prefuse.Visualization;
import prefuse.controls.FocusControl;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 * Extension of prefuse FocusControl for mouse interaction management in HeatmapDiagram
 * @author Rodrigo Santamaria
 *
 */
class HeatmapFocusControl extends FocusControl
{
    private boolean enabled;
    private Session sesion;
    String group;//Visualization.FOCUS
    private String activity;
    private LinkedList<Integer> genesSeleccionados;
    private LinkedList<Integer> condicionesSeleccionadas;
    private Visualization visualization;
    private String field;//la tabla
    private String fieldGeneLabels;
    private String fieldConditionLabels;
    private MicroGridLayout gl;
    
    /**
     * Session constructor
     * @param session Session to which this controller has to listen/update for changes
     * @param activity Name of the ActionList linked to this FocusControl
	 * @param group		Group linked to this FocusControl (usually, the Visualization.FOCUS_ITEMS)
     * @param expressionsGroup		name of the group that contains the exrpression levels	
     * @param geneNamesGroup	name of the group that contains the gene names
     * @param conditionNamesGroup	name of  the group that contains the condition names
     * @param mgl		MicroGridLayout to which this HeatmapFocusControl is linked
	 * @param v			Visualization linked to this FocusControl
     */
    public HeatmapFocusControl(Session session, String activity, 
		   String group, String expressionsGroup, String geneNamesGroup, String conditionNamesGroup, 
		   MicroGridLayout mgl, Visualization v)
		{
		super(group, 1, activity);
		this.activity=activity;
		this.group=group;
		enabled=true;
		this.sesion=session;
		genesSeleccionados=new LinkedList<Integer>();
		condicionesSeleccionadas=new LinkedList<Integer>();
		visualization=v;
		this.field=expressionsGroup;
		this.fieldGeneLabels=geneNamesGroup;
		this.fieldConditionLabels=conditionNamesGroup;
		gl=mgl;
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
    	//TODO: Acciones cuando se pincha en un gen
        if ( !filterCheck(item) ) return;
        if ( UILib.isButtonPressed(e, button) &&
             e.getClickCount() == ccount )
        	{
            if ( item != curFocus ) //Añadimos al foco uno que no es el último añadido
            	{
                Visualization vis = item.getVisualization();
                //TupleSet ts = vis.getFocusGroup(group);
                
                boolean genSeleccionado=false;
                boolean condicionSeleccionada=false;
                boolean expresionSeleccionada=false;
                
                if(item.getGroup().equals(field))	
                	{
                	//System.out.println("Expresión: Añadimos foco a "+item.getString("gene")+", "+item.getString("condition"));
                	expresionSeleccionada=true;
                	}
                else if(item.getGroup().equals(fieldGeneLabels))	
	            	{
	            	//System.out.println("Gen: Añadimos foco a "+item.getString("name"));
	            	genSeleccionado=true;
	            	}
                else if(item.getGroup().equals(fieldConditionLabels))	
	            	{
	            	///System.out.println("Condición: Añadimos foco a "+item.getString("name"));
	            	condicionSeleccionada=true;
	            	}
                
                boolean ctrl = e.isControlDown();
                if ( !ctrl ) //Este es el único que estará en el focus
                	{
                	curFocus = item;
                    this.clear();
                	}
                
                if(genSeleccionado)			
                	{
                	//Añadimos como punto seleccionado en las visualizaciones tradicionales
	            	Field ejeX, ejeY;//Los ejes que sean nos dan un poco igual, siempre que sea alguna de las condiciones
					ejeX = sesion.getDataLayer().getXaxis();
					ejeY = sesion.getDataLayer().getYaxis();
					//System.out.println("Nuestros nombres son "+ejeX.getNombre()+" y "+ejeY.getNombre());
	            	TupleSelection puntosSelec = new TupleSelection(ejeX.getName(),ejeY.getName(),ejeX.size());
	            	puntosSelec.setX(item.getInt("id"), true);
					sesion.setSelectedPoints(puntosSelec, "Heatmap");
					
					//Añadimos como gene seleccionado a las visualizaciones nuevas
					genesSeleccionados.add(item.getInt("id"));
					for(int i=0;i<sesion.getMicroarrayData().getNumConditions();i++)
						{
						condicionesSeleccionadas.add((Integer)i);
						//TODO: añadir todos los items a esta
						}
					}
                if(condicionSeleccionada)
                	{
                	condicionesSeleccionadas.add(item.getInt("id"));
                	sendExpressionLevels(item.getInt("id"));
                	}
                if(expresionSeleccionada)
                	{
                	Field ejeX, ejeY;//Los ejes que sean nos dan un poco igual, siempre que sea alguna de las condiciones
					ejeX = sesion.getDataLayer().getXaxis();
					ejeY = sesion.getDataLayer().getYaxis();
					TupleSelection puntosSelec = new TupleSelection(ejeX.getName(),ejeY.getName(),ejeX.size());
	            	puntosSelec.setX(item.getInt("rowId"), true);
					sesion.setSelectedPoints(puntosSelec, "Heatmap");
	        
					genesSeleccionados.add(item.getInt("rowId"));
                	condicionesSeleccionadas.add(item.getInt("colId"));
                	}
                
                this.addItems(genesSeleccionados, condicionesSeleccionadas);
            	sesion.setSelectedBiclusters(new BiclusterSelection(genesSeleccionados, condicionesSeleccionadas), "Heatmap");
        
            runActivity(vis);
         	}
            }
     	}
    
    //Cuando se llama, se ponen como uno de los primeros a este perfil genético
	void addItem(VisualItem item)
		{
		Visualization vis=item.getVisualization();
		//Añadimos tupla al Focus
		//TupleSet ts = vis.getFocusGroup(group);
		//ts.addTuple(item);
		
//		Cambiamos su orden en la visualización
		item.setInt("rowRank", -1);	
		
//		Cambiamos también el orden en los nombres
		Iterator it=vis.items(fieldGeneLabels, "name=\""+item.getString("gene")+"\"");
		VisualItem geneLabel=(VisualItem)it.next();//Debería haber sólo uno
		geneLabel.setInt("rowRank",-1);
		
		genesSeleccionados.add(item.getRow());
		runActivity(vis);
		}
	
	void addItems(VisualItem[] item)
		{
		for(int i=0;i<item.length;i++)	addItem(item[i]);
		}
	
	/**
	 * Pone el rowRank, colRank a -1 en todos los "geneLabels", "conditionLabels" y "matrix" que coincidan
	 * en id (en los dos primeros casos) o en rowId y colId con algún elemento de gid y cid
	 * TODO: esta es la que funciona bien!
	 */
	void addItems(LinkedList<Integer> gid, LinkedList<Integer> cid)
		{
		//System.out.println("Cambiando los rangos");
		int [] genes=null;
		int [] conds=null;
		if(gid.size()>0)	genes= new int[gid.size()];
		if(cid.size()>0)	conds=new int[cid.size()];

		for(int i=0;i<gid.size();i++)	genes[i]=(new Integer((gid.get(i)).toString())).intValue();
		for(int i=0;i<cid.size();i++)	conds[i]=(new Integer((cid.get(i)).toString())).intValue();
		gl.newOrder(genes, conds);
		Iterator it;
		String condition;
		
		//Cambiamos las etiquetas de los genes y condiciones para que estén los primeros los seleccionados
		for(int i=0;i<gid.size();i++)
			{
			//it=visualization.items(fieldGeneLabels,"id="+gid.get(i));//Cogemos la etiqueta de gen
			it=visualization.items(fieldGeneLabels,"actualId="+gid.get(i));//Cogemos la etiqueta de gen, en el caso de sparse tiene que ser por el actualId!
			VisualItem glabel=(VisualItem)it.next();
			glabel.setInt("rowRank",-(gid.size()-i));//the same order that puts gl.newOrder()
			}

		for(int i=0;i<cid.size();i++)
			{
			condition=(cid.get(i)).toString();
			it=visualization.items(fieldConditionLabels,"id="+condition);//Cogemos la etiqueta de gen
			VisualItem clabel=(VisualItem)it.next();//there should be only one
			clabel.setInt("colRank",-(gid.size()-i));
			}
		runActivity(visualization);
		//runActivity("color");
		}
	
	//Añade todos los niveles de expresión que tengan un gen con ese id
	void addItems(String name)
		{
		Iterator it=visualization.items(field,"gene=\""+name+"\"");
		
		while(it.hasNext())
			addItem((VisualItem)it.next());
		}

	
	void clear()
		{
	//	System.out.println("HM: Vamos a borrar lo que hubiera");
		//Y de la etiqueta
		Iterator it2=visualization.items(fieldGeneLabels, "rowRank<0");
		while(it2.hasNext())
			{
			VisualItem itemLabel=((VisualItem)it2.next());
		//	System.out.println("Quitando "+itemLabel.getString("name"));
			itemLabel.setInt("rowRank", itemLabel.getInt("id"));
			}

//			Y de la etiqueta de condiciones
		Iterator it3=visualization.items(fieldConditionLabels, "colRank<0");
		while(it3.hasNext())
			{
			VisualItem itemLabel=((VisualItem)it3.next());
			itemLabel.setInt("colRank", itemLabel.getInt("id"));
			}
			//TupleSet ts = visualization.getFocusGroup(group);//Limpiamos de focus
			//ts.clear();
   	    gl.initialOrder();
		genesSeleccionados.clear();
		condicionesSeleccionadas.clear();
		}
    
	private void sendExpressionLevels(int colid)
		{
		Vector<Double> vect=new Vector<Double>(0,1);
		Iterator it=visualization.items(field,"colId="+colid);
		while(it.hasNext())
			vect.add( ((VisualItem)it.next()).getDouble("level"));
		
		sesion.setConditionExpressions(vect);
		}
	
    private void runActivity(Visualization vis) 
    	{
        if ( activity != null )        
        	{
        	vis.run(activity);
        	vis.run("color");
        	}
    	}


}
