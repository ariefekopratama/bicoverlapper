package es.usal.bicoverlapper.visualization.diagrams;

import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

/**
 * Class that displays all VisualItems in a grid. 
 * 
 */
class MicroGridLayout extends Layout {
    private double cellHeight;
    private double cellWidth;
    private int n;//total number of genes and conditions
    private int m;
   // private static final int maxLines=2000;
    
    private String grupo;//prefuse table group on which operations are made
   // private Double scale;//substituted by distortion factors
    public static double maxDistortion=0.7;//maximum area, in percentage, devoted to distortion
    public int[] geneOrder;//order in which genes and conditions must be laid out
    //public HashMap<Integer, Integer> geneOrder;
    public int[] conditionOrder;
    private String colField;//prefuse table colum field names for row and column ids
    private String rowField;
    private int xMargin;//margin to start layout
    private int yMargin;
    private int rowSelected;//number of rows/columns selected
    private int colSelected;
    private double mx;//distortion factors
    private double my;
    private double height;
    private double width;
    
    /**
     * Constructs the layout
     * @param group		group in which VisualItems are
     * @param numRows	number of rows for the grid	
     * @param numCols	number of columns for the grid
     * @param height	height in pixels of the grid
     * @param width		width in pixels of the grid
     * @param rowName	field name in the items for the int identifier of its row
     * @param colName	field name in the items for the int identifier of its column
     * @param xMargin	x coordinate of the top-left square of the layout
     * @param yMargin	y coordinate of the top-left square of the layout
     * @param mx		factor for distortion of width	
     * @param my		factor for distortion of height
     */
    public MicroGridLayout(String group, int numRows, int numCols, double height, double width, String rowName, String colName, int xMargin, int yMargin, double mx, double my) 
        {
        grupo=group;
        n=numRows;
        m=numCols;
        cellHeight=height/n;	//No influyen, son determinados por el ERenderer
        cellWidth=width/m;
        this.height=height;
        this.width=width;
        geneOrder=new int[n];
        conditionOrder=new int[m];
        for(int i=0;i<n;i++)	geneOrder[i]=i;
        for(int i=0;i<m;i++)	conditionOrder[i]=i;
        colField=colName;
        rowField=rowName;
        this.xMargin=xMargin;
        this.yMargin=yMargin;
        rowSelected=0;
        colSelected=0;
        this.mx=mx;
        this.my=my;
      //  System.out.println("Altura maxima "+height);
    	}
    
    /**
     * @see prefuse.action.Action#run()
     */
    public void run()
    	{
    	}
    
    void newOrder(int[] genesFirst, int[] conditionsFirst)
    	{
    	int init=0;
    	if(genesFirst!=null)
    	{
    	for(int i=0;i<genesFirst.length;i++)//Primero añado todos los que tienen que ir primero
    		{
    		geneOrder[genesFirst[i]]=init;//El primero pasa a ocupar la posición de uno de los que tenemos
    		init++;
    		}
    	for(int i=0;i<n;i++)//Luego el resto, si no están entre lo iniciales
    		{
    		boolean add=true;
    		for(int j=0;j<genesFirst.length;j++)	if(i==genesFirst[j])	{add=false;break;}
    		if(add)	geneOrder[i]=init++;
    		}
    	rowSelected=genesFirst.length;
    	}
    	else
    		{
    		for(int i=0;i<n;i++)	geneOrder[i]=i;
    		rowSelected=0;
    		}
    	
    	if(conditionsFirst!=null)
    	{
    	init=0;
    	for(int i=0;i<conditionsFirst.length;i++)//Primero añado todos los que tienen que ir primero
    		{
    		conditionOrder[conditionsFirst[i]]=init;//El primero pasa a ocupar la posición de uno de los que tenemos
    		init++;
    		}
    	for(int i=0;i<m;i++)//Luego el resto, si no están entre lo iniciales
    		{
    		boolean add=true;
    		for(int j=0;j<conditionsFirst.length;j++)	if(i==conditionsFirst[j])	{add=false;break;}
    		if(add)	conditionOrder[i]=init++;
    		}
    	colSelected=conditionsFirst.length;
    	}
    	else
    		{
            for(int i=0;i<m;i++)	conditionOrder[i]=i;
        	colSelected=0;
    		}
    	}
    
    void initialOrder()
    	{
    	for(int i=0;i<n;i++)	geneOrder[i]=i;
        for(int i=0;i<m;i++)	conditionOrder[i]=i;
    	}
    
    //NOTA: OJO, sólo se toca al principio, y con cada selecci'on en otras vistas
    /**
     * @see prefuse.action.Action#run(double)
     */
    public void run(double frac) 
    	{
    	//System.out.println("*************** MICROGRIDLAYOUT.RUN *********************");
    	double t1=System.currentTimeMillis();
    	TupleSet ts=getVisualization().getGroup(grupo);
 	    Iterator iter=ts.tuples();
        double x=0;
        double y=0;

        
        double normalw=cellWidth;
        double normalh=cellHeight;
        double distortedw=normalw*mx;
        double distortedh=normalh*my;
        
        double miniw=(this.getLayoutBounds().getWidth()-distortedw*colSelected)/(m-colSelected);
        double minih=(this.getLayoutBounds().getHeight()-distortedh*rowSelected)/(n-rowSelected);

        if(rowSelected>0 || colSelected>0)//In case there are something selected it is distorted
	        {
	        if(colSelected>0 && distortedw*colSelected>maxDistortion*width)
		    	{
		    	distortedw=maxDistortion*width/colSelected;
		    	miniw=(this.getLayoutBounds().getWidth()-distortedw*colSelected)/(m-colSelected);
		        }
		    if(rowSelected>0 && distortedh*rowSelected>maxDistortion*height)
	        	{
		    	distortedh=maxDistortion*height/rowSelected;
		    	minih=(this.getLayoutBounds().getHeight()-distortedh*rowSelected)/(n-rowSelected);
		        }
	        while(iter.hasNext())
	        	{
	        	VisualItem item = (VisualItem)iter.next();
	        	int i=geneOrder[item.getInt(rowField)];
	            int j=conditionOrder[item.getInt(colField)];
	            int icont=item.getInt(rowField);
	            int jcont=item.getInt(colField);
	            
	        	double h=0;
	        	double w=0;
	        	if(i>=rowSelected)	h=minih;
	        	else		   		h=distortedh;
	        	
	        	if(colSelected==0 || colSelected==m-1)//we have selected gene profiles only
	        		w=normalw;
	        	else
	        		{
	        		if(j>=colSelected)	w=miniw;
	        		else			w=distortedw;
	        		}
	        	
	        	
	        	ExpressionRenderer er=(ExpressionRenderer)item.getRenderer();
	        	er.setHeight(h, icont);
	            er.setWidth(w, jcont);
	            
	            if(i>=rowSelected)	y=yMargin+rowSelected*distortedh+(i-rowSelected)*minih;
	        	else				y=yMargin+i*distortedh;
	        	
	        	if(colSelected==m-1)//we have selected gene profiles only (all conditions selected)
	        		x=xMargin+j*normalw;
	        	else
	        		{
		        	if(j>=colSelected)	x=xMargin+colSelected*distortedw+(j-colSelected)*miniw;
		        	else				x=xMargin+j*distortedw;
	        		}
		        	
	        	setX(item, null, x);
	            setY(item, null, y);
	            }
	        
	        //Rectangle surrounding the selection
	       /* VisualItem rect=(VisualItem)this.getVisualization().getGroup("rectangle").tuples().next();
	        setX(rect,null, xMargin);
	        setY(rect,null, yMargin);
	        ExpressionRenderer sr=(ExpressionRenderer)rect.getRenderer();
	        int hc=(int)(distortedh*rowSelected);
	        int wc=(int)(distortedw*colSelected);
	        if(colSelected==m-1)	wc=(int)width;
	        sr.setHeight(hc, 0);
	        sr.setWidth(wc, 0);*/
	         }
        else
        	{//otherwise, it is built as normal
	        x=xMargin;
	        y=yMargin;
	        
	    	ts=getVisualization().getGroup(grupo);
	 	    iter=ts.tuples();
	        x=0;
	        y=0;
	        
	        while(iter.hasNext())
	        	{
	        	VisualItem item = (VisualItem)iter.next();
	        	ExpressionRenderer er=((ExpressionRenderer)item.getRenderer());
	        	int ci=item.getInt("colId");
	        	int ri=item.getInt("rowId");
	        	double w=er.getWidth(ci);
	        	double h=er.getHeight(ri);
	        	
	            x=xMargin+conditionOrder[ci]*w;
	            y=yMargin+geneOrder[ri]*h;
	            
	            item.setX(x);//mas rápido que this.setX()
	            item.setY(y);
	            }
	        
//	      Rectangle surrounding the selection
	      /*  VisualItem rect=(VisualItem)this.getVisualization().getGroup("rectangle").tuples().next();
	        setX(rect,null, xMargin);
	        setY(rect,null, yMargin);
	       // ExpressionRenderer sr=(ExpressionRenderer)rect.getRenderer();
	        //sr.setBaseHeight(0, 0);
	        //sr.setBaseWidth(0, 0);*/

	        }
    //    System.out.println("Tiempo tardado en MicroGridLayout.run(): "+(System.currentTimeMillis()-t1)/1000);
      }
} // end of inner class MicroGridLayout

