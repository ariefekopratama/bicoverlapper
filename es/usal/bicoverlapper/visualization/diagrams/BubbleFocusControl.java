 package es.usal.bicoverlapper.visualization.diagrams;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import es.usal.bicoverlapper.data.BubbleData;
import es.usal.bicoverlapper.data.Field;
import es.usal.bicoverlapper.kernel.BiclusterSelection;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.kernel.TupleSelection;

import prefuse.Visualization;
import prefuse.controls.FocusControl;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;


/**
 * Extension of prefuse FocusControl for mouse interaction management in BubblesDiagram
 * @author Rodrigo Santamaria
 *
 */
class BubbleFocusControl extends FocusControl
	{
    private boolean enabled;
    private Session sesion;
    private String group;
    private String activity;
    private LinkedList nodosSeleccionados;
    private Visualization visualization;
    
    /**
     * Session constructor
     * @param session Session to which this controller has to listen/update for changes
     * @param activity Name of the ActionList linked to this FocusControl
	 * @param group		Group linked to this FocusControl (usually, the Visualization.FOCUS_ITEMS)
     * @param v			Visualization linked to this FocusControl
     */
    public BubbleFocusControl(Session session, String activity, String group, Visualization v)
		{
		super(group, 1, activity);
		this.activity=activity;
		this.group=group;
		enabled=true;
		this.sesion=session;
		nodosSeleccionados=new LinkedList();
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
     * Selects all biclusters that have, at least, one element in common with
     * the genes and conditions in the selection
     * @param bs	BiclusterSelection with the genes and conditions selected
     * TODO: STILL IN DEVELOPMENT
     */
    void setSelectedBiclustersUnion(BiclusterSelection bs)
	    {
    	if(visualization!=null && sesion!=null && sesion.getSelectedBicluster()!=null)	//version modo OR
			//Se eligen todos los biclusters que tengan alguno de los genes o condiciones que vienen
			{
			LinkedList<Integer> lg=sesion.getSelectedGenesBicluster();
			LinkedList<Integer> lc=sesion.getSelectedConditionsBicluster();
			//Quitamos todos los que estuvieran antes en el bicluster
			this.clear();
			BubbleData bd=sesion.getBubbleData();
			if(sesion.getMicroarrayData()!=null)
				{
				ArrayList<String> lgn=sesion.getMicroarrayData().getGeneNames(lg);
				ArrayList<String> lcn=sesion.getMicroarrayData().getConditionNames(lc);
				for(int i=0;i<bd.getGraph().getNodeCount();i++)
					{
					Node n=bd.getGraph().getNode(i);
					ArrayList<String> lista=(ArrayList<String>)n.get("genes");
					boolean added=false;
					for(int j=0;j<lista.size();j++)
						{
						if(lgn.contains(lista.get(j)))	
								{
								VisualItem item=visualization.getVisualItem("graph.nodes", n);
								this.addItem(item);
								added=true;
								break;
								}
						}
					if(!added)
						{
						lista=(ArrayList<String>)n.get("conditions");
							for(int j=0;j<lista.size();j++)
							{
							if(lcn.contains(lista.get(j)))	
									{
									VisualItem item=visualization.getVisualItem("graph.nodes", n);
									this.addItem(item);
									break;
									}
							}
						}
					
					}
				}
			}
	    }
	    
    /**
     * Selects all biclusters that contains all the genes and conditions in the selection
     * @param bs	BiclusterSelection with the genes and conditions selected
     */
    void setSelectedBiclustersIntersection(BiclusterSelection bs)
	    {
    	if(visualization!=null && sesion!=null && sesion.getSelectedBicluster()!=null)	//version modo OR
			//Se eligen todos los biclusters que tengan alguno de los genes o condiciones que vienen
			{
		//	System.out.println("REPINTAMOS Bubble");
			LinkedList<Integer> lg=sesion.getSelectedGenesBicluster();
			LinkedList<Integer> lc=sesion.getSelectedConditionsBicluster();
			//Quitamos todos los que estuvieran antes en el bicluster
			this.clear();
			BubbleData bd=sesion.getBubbleData();
			if(sesion.getMicroarrayData()!=null)
				{
				ArrayList<String> lgn=sesion.getMicroarrayData().getGeneNames(lg);
				ArrayList<String> lcn=sesion.getMicroarrayData().getConditionNames(lc);
				for(int i=0;i<bd.getGraph().getNodeCount();i++)
					{
					boolean add=true;
					Node n=bd.getGraph().getNode(i);
					ArrayList<String> lista=(ArrayList<String>)n.get("genes");
					for(int j=0;j<lgn.size();j++)
						{
					//	if(!lgn.contains(lista.get(j)))	
						if(!lista.contains(lgn.get(j)))	
									{	add=false; break;}
						}
				
					if(add && lcn.size()<sesion.getMicroarrayData().getConditionNames().length-1)//Si todavía se puede añadir
						{
						lista=(ArrayList<String>)n.get("conditions");
						for(int j=0;j<lcn.size();j++)
							{
							if(!lista.contains(lcn.get(j)))	
									{
									add=false;
									break;
									}
							}
						}
					if(add)
						{
						VisualItem item=visualization.getVisualItem("graph.nodes", n);
						this.addItem(item);
						}
					}
				}
			}
	    }

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
                //	System.out.println("BubbleFocus: ünico nodo a estar en el focus");
                	curFocus = item;
                	ts.clear();
                    ts.setTuple(item);
                    nodosSeleccionados.clear();
                    nodosSeleccionados.add(item.getRow());
                    
                   // if(sesion.getData()!=null)
	                    {
	                    //LinkedList lg=(LinkedList)item.get("genes");
	                    /*Field ejeX, ejeY;//Los ejes que sean nos dan un poco igual, siempre que sea alguna de las condiciones
						ejeX = sesion.getDataLayer().getXaxis();
						ejeY = sesion.getDataLayer().getYaxis();*/
						
						/*
						TupleSelection puntosSelec = new TupleSelection(ejeX.getName(),ejeY.getName(),ejeX.size());
		            	for(int i=0;i<lg.size();i++)
		            		{
		            		String name=(String)lg.get(i);
		            		int pos=0;
		            		
		            		for(int j=0;j<geneNames.length;j++)
		            			{
		            			if(geneNames[j].equals(name))	{pos=j; break;}
		            			}
		            		puntosSelec.setX(pos, true);
		            		}
						sesion.setSelectedPoints(puntosSelec, "Bubble");*/
	                    //	ArrayList lg=(ArrayList)item.get("genes");
		                //    String geneNames[]=this.sesion.getMicroarrayData().getGeneNames();
						sesion.setSelectedBiclusters(new BiclusterSelection(getListFrom((ArrayList)item.get("genes"),true),getListFrom((ArrayList)item.get("conditions"),false)), "Bubble");
	            		}
                 	}
                else if ( ts.containsTuple(item) ) //Ya estaba, lo quitamos, tanto si hay ctrl como si no
                	{
                	//System.out.println("Bubble Focus: Este nodo ya estaba, lo quitamos");
                    ts.removeTuple(item);
                   // nodosSeleccionados.remove(item.getRow());
                   // sesion.setSelectedGenes(new SeleccionGenes(l));
            		}
                	else 
                	{
                	//	System.out.println("Bubble Focus: Hay ctrl, añadimos sin mas");
                	//nodosSeleccionados.add(item.getRow());
                	//sesion.setSelectedTRNGenes(new SeleccionTRNGenes(nodosSeleccionados));
                    ts.addTuple(item);
                	}
                runActivity(vis);
             
            	
            	}
            else if ( e.isControlDown() ) //Si no es el últimos añadido y pulsamos control
            	{
            	//System.out.println("Si no es el último añadido y hay ctrl, lo quitamos");
                Visualization vis = item.getVisualization();
                TupleSet ts = vis.getFocusGroup(group);
                ts.removeTuple(item);
                curFocus = null;
                
            	//nodosSeleccionados.remove(nodosSeleccionados.indexOf(item.getRow()));
            	//sesion.setSelectedTRNGenes(new SeleccionTRNGenes(nodosSeleccionados));

                runActivity(vis);
            	}
        	}
     	}
    
    public LinkedList<Integer> getListFrom(ArrayList<String> l,boolean genes)
    	{
    	LinkedList<Integer> lista=new LinkedList<Integer>();
    	String names[];
    	if(genes) names=this.sesion.getMicroarrayData().getGeneNames();
    	else		names=this.sesion.getMicroarrayData().getConditionNames();
		
    	//System.out.println("Elementos "+l.size());
		for(int i=0;i<l.size();i++)
    		{
    		String name=(String)l.get(i);
    		int pos=0;
    		
    		for(int j=0;j<names.length;j++)
    			{
    			if(names[j].equals(name))	{pos=j; break;}
    			}
    	//	System.out.println("Añadiendo "+name+" de pos "+pos);
    		lista.add(Integer.valueOf(pos));
    		}
		
    	return lista;
    	}
    //Cuando se llama, se pone como seleccionada una burbuja
	void addItem(VisualItem item)
		{
	//	System.out.println("BF: Vamos a añadir lo que nos parezca");
		Visualization vis=item.getVisualization();
		TupleSet ts = vis.getFocusGroup(group);
		ts.addTuple(item);
		nodosSeleccionados.add(item.getRow());//antes add(item)
        //nodosSeleccionados =(LinkedList)item.get("genes");//De momento sólo permitimos uno a la vez

		//System.out.println(ts.getTupleCount());
		runActivity(vis);
		}
	
	void addItems(VisualItem[] item)
		{
		for(int i=0;i<item.length;i++)	addItem(item[i]);
		}
	
	
	/**
	 * Removes all selections previously done
	 *
	 */
	public void clear()
		{
	//	System.out.println("BF: Vamos a borrar lo que hubiera");
		if(nodosSeleccionados.size()>0)
			{
			//VisualItem item=(VisualItem)nodosSeleccionados.removeFirst();
			//Visualization vis=item.getVisualization();
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
