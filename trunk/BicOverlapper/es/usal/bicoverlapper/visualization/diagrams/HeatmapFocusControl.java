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
    private LinkedList<Integer> genesSparseSeleccionados;
    private LinkedList<Integer> condicionesSeleccionadas;
    private Visualization visualization;
    private String field;//la tabla
    private String fieldGeneLabels;
    private String fieldConditionLabels;
    private MicroGridLayout gl;
    private HeatmapDiagram.VerticalLineLayout2 vl2;
    private HeatmapDiagram.HorizontalLineLayout hl;
    
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
		   MicroGridLayout mgl,  HeatmapDiagram.VerticalLineLayout2 vl2, HeatmapDiagram.HorizontalLineLayout hl, Visualization v)
		{
		super(group, 1, activity);
		this.activity=activity;
		this.group=group;
		enabled=true;
		this.sesion=session;
		genesSeleccionados=new LinkedList<Integer>();
		genesSparseSeleccionados=new LinkedList<Integer>();
		condicionesSeleccionadas=new LinkedList<Integer>();
		visualization=v;
		this.field=expressionsGroup;
		this.fieldGeneLabels=geneNamesGroup;
		this.fieldConditionLabels=conditionNamesGroup;
		gl=mgl;
		this.vl2=vl2;
		this.hl=hl;
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
     * Checks for mouse clicks, changing Session status if necessary
     */
    public void itemClicked(VisualItem item, MouseEvent e)
     	{
        if ( !filterCheck(item) ) return;
        if ( UILib.isButtonPressed(e, button) &&
             e.getClickCount() == ccount )
        	{
            if ( item != curFocus ) //A�adimos al foco uno que no es el �ltimo a�adido
            	{
                Visualization vis = item.getVisualization();
                
                boolean genSeleccionado=false;
                boolean condicionSeleccionada=false;
                boolean expresionSeleccionada=false;
                
                if(item.getGroup().equals(field))               	expresionSeleccionada=true;
                else if(item.getGroup().equals(fieldGeneLabels))   	genSeleccionado=true;
                else if(item.getGroup().equals(fieldConditionLabels))  	condicionSeleccionada=true;
                
                boolean ctrl = e.isControlDown();
                if ( !ctrl ) //Este es el �nico que estar� en el focus
                	{
                	curFocus = item;
                    this.clear();
                	}
                
                if(genSeleccionado)			
                	{
                	System.out.println("A�adiendo gen "+item.getInt("id")+" "+item.getString("name"));
                	genesSeleccionados.add(item.getInt("actualId"));
                	for(int i=0;i<sesion.getMicroarrayData().getNumConditions();i++)
						{
						condicionesSeleccionadas.add((Integer)i);
						//TODO: a�adir todos los items a esta
						}
					}
                if(condicionSeleccionada)
                	{
                	condicionesSeleccionadas.add(item.getInt("id"));
                	sendExpressionLevels(item.getInt("id"));
                	}
                if(expresionSeleccionada)
                	{
                	genesSeleccionados.add(item.getInt("actualRowId"));//Sparse
                	condicionesSeleccionadas.add(item.getInt("colId"));
                	}
                
                this.addItems(genesSeleccionados, condicionesSeleccionadas);
            	sesion.setSelectedBiclusters(new BiclusterSelection(genesSeleccionados, condicionesSeleccionadas), "Heatmap");
        
            runActivity(vis);
         	}
            }
     	}
    
    //Cuando se llama, se ponen como uno de los primeros a este perfil gen�tico
	void addItem(VisualItem item)
		{
		Visualization vis=item.getVisualization();
		//A�adimos tupla al Focus
		//TupleSet ts = vis.getFocusGroup(group);
		//ts.addTuple(item);
		
//		Cambiamos su orden en la visualizaci�n
		item.setInt("rowRank", -1);	
		
//		Cambiamos tambi�n el orden en los nombres
		Iterator it=vis.items(fieldGeneLabels, "name=\""+item.getString("gene")+"\"");
		VisualItem geneLabel=(VisualItem)it.next();//Deber�a haber s�lo uno
		geneLabel.setInt("rowRank",-1);
		
		genesSeleccionados.add(item.getRow());
		runActivity(vis);
		}
	
	void addItems(VisualItem[] item)
		{
		for(int i=0;i<item.length;i++)	addItem(item[i]);
		}
	
	
	/**
	 * Like addItems, but it only for hovering. That is, the position is not changed, they are only distorted
	 */
	void addItemsForHover(LinkedList<Integer> gid, LinkedList<Integer> cid)
		{
		int [] genes=null;
		int [] conds=null;
		if(gid.size()>0)	genes= new int[gid.size()];
		if(cid.size()>0)	conds=new int[cid.size()];

		for(int i=0;i<gid.size();i++)	
			genes[i]=sesion.getMicroarrayData().getSparseGeneId(gid.get(i));
	
		for(int i=0;i<cid.size();i++)	
			conds[i]=(new Integer((cid.get(i)).toString())).intValue();
		//gl.newOrder(genes, conds);
		//vl2.newOrder(genes);
		
		gl.newHover(genes, conds);
		Iterator it;
		String condition;
		
		//Cambiamos las etiquetas de los genes y condiciones para que est�n los primeros los seleccionados
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

	/**
	 * Changes rowRank, colRank to -1 in "geneLabels", "conditionLabels" y "matrix" elements which
	 *  "id" (two first cases) or "rowId" and "colId" coincide with gid and cid.
	 * Deben pas�rsele rowIds, no actualRowIds!
	 */
	void addItems(LinkedList<Integer> gid, LinkedList<Integer> cid)
		{
		//System.out.println("Cambiando los rangos");
		int [] genes=null;
		int [] conds=null;
		if(gid.size()>0)	genes= new int[gid.size()];
		if(cid.size()>0)	conds=new int[cid.size()];

		//for(int i=0;i<gid.size();i++)	genes[i]=(new Integer((gid.get(i)).toString())).intValue();//rowIds, seg�n como se le pasa
		for(int i=0;i<gid.size();i++)	
			{
			//genes[i]=(new Integer((gid.get(i)).toString())).intValue();//rowIds, seg�n como se le pasa
			genes[i]=sesion.getMicroarrayData().getSparseGeneId(gid.get(i));
			}
		for(int i=0;i<cid.size();i++)	conds[i]=(new Integer((cid.get(i)).toString())).intValue();
		gl.newOrder(genes, conds);
		vl2.newOrder(genes);
		hl.newOrder(conds);
		Iterator it;
		String condition;
		
		//Cambiamos las etiquetas de los genes y condiciones para que est�n los primeros los seleccionados
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
	
	//A�ade todos los niveles de expresi�n que tengan un gen con ese id
	void addItems(String name)
		{
		Iterator it=visualization.items(field,"gene=\""+name+"\"");
		
		while(it.hasNext())
			addItem((VisualItem)it.next());
		}

	
	void clear()
		{
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