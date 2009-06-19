package es.usal.bicoverlapper.data;


import es.usal.bicoverlapper.analysis.Biclustering;
import es.usal.bicoverlapper.kernel.Session;
import es.usal.bicoverlapper.utils.AnnotationProgressMonitor;
import es.usal.bicoverlapper.utils.HypergeometricTestProgressMonitor;
import es.usal.bicoverlapper.utils.MicroarrayLoadProgressMonitor;
import gov.nih.nlm.ncbi.www.soap.eutils.esearch.IdListType;
import gov.nih.nlm.ncbi.www.soap.eutils.esummary.DocSumType;

import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

import prefuse.data.Table;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.util.Sort;
import prefuse.util.collections.IntIterator;

/**
 * Class with data of Microarray expression levels, using Prefuse Tables
 * <p>
 *
 * MicroarrayData will contain:
 *<p>
 * For each gene/condition:	
 *<p>
 * 	"name" - name of the gene/condition (String)-, 
 *<p>
 * 	"id"   - unique identifier for the gene/condition (int)- and 
 *<p>
 * 	"rowRank"/"colRank" - order in which it is drawn (int). 
 *<p>
 * For each expression level:
 *<p>
 * 	"gene" - name of the gene for this expression level (String)-, 
 *<p>
 * 	"condition" - name of the condition for this expression level (String)-, 
 *<p>
 * 	"rowId" - id of the gene for this expression level (int)-, 
 *<p>
 * 	"colId" - id of the condition for this expression level (int)-, 
 *<p>
 * 	"rowRank" - order in which the gene is drawn (int)-, 
 *<p>
 *  "colRank" - order in which the condition is drawn (int)- and 
 *<p>
 *  "level" - expression level (double)
 *<p>
 *  
 *  In addition, an ancillary sparse matrix is built to avoid performance downgrades with very
 *  large matrices. It contains only numSparseGenes, typically a number around 200. The genes are
 *  randomly selected from the original matrix, and can also be selected by rebuilding the matrix
 *  with buildSparse(). It uses Prefuse Tables as described above, except it add a actualRowId that
 *  refers to the id in the original matrix, while rowId is used for the id in the sparse matrix.
 * @author Rodrigo Santamaria
 *
 */
public class MicroarrayData 
	{
	Table levels;//Expresion levels as usual matrix
	Table expressions;//Expresion levels, each one as tuple (gene, condition, level)
	/**
	 * matrix with expression levels replicated, to quicken some arithmetic operations
	 */
	public double matrix[][];
	Table geneLabels;
	Table conditionLabels;
	int maxGenes=199;//A partir de 200
	Table sparseExpressions;//As above, but sparse (only a maximum number of genes are shown)
	Table sparseGeneLabels;
	
	/**
	 * Names for the conditions. It is recommended not to use very large names or the visualizations
	 * could be very messy
	 */
	public String[] conditionNames;
	/**
	 * Gene names.
	 */
	public String[] geneNames;
	
	/**
	 * Biclustering class bound to this microarray data.  
	 */
	public Biclustering biclustering= null;
	/**
	 * Session class into which this microarray data is loaded
	 */
	Session session=null;
	
	/**
	 * Type of gene names used. If GENENAME, annotations are searched with Entrez Gene and QuickGO
	 */
	public String rname="GENENAME";
	public String rdescription="DESCRIPTION";
	public String rgo="GO";
	public Rengine re=null;
	
	int numGenes;
	int numSparseGenes;//for sparse matrices
	int numConditions;
	private int[] decimals;
	
	/**
	 * Average expression value of the whole expression matrix
	 */
	public double average=0;
	
	
	
	String chip;//kind of microarray chip (any official for Affymetrix permitted, by now), or kind of name taken by genes (geneID and ORF permitted)
	String organism;//Name of organism as registered in NCBI
	public Map<Integer, GeneAnnotation> geneAnnotations;//Gene Annotations from NCBI and GeneOntology
	public boolean searchByR=false; //if there is information in the file about an available R package, gene annotations are taken from there, otherwise they're searched in NCBI
	boolean annotationsRetrieved=false;
	
	public AnnotationTask t;
	private AnnotationProgressMonitor amd;
	
	//GeneAnnotation retrieval
	private GeneRequester geneRequester;
	private HypergeometricTestTask ht;
	private LoadTask loadTask;
	private MicroarrayRequester microarrayRequester;
	
	/**
	 * Constructor from a file
	 * @param path Path to the file with microarray information
	 * @param invert true if genes are columns (genes as rows are considered as the usual option)
	 * @param rowHeader Number of initial rows with row information (usually one)
	 * @param colHeader Number of initial columns with column information (usually one)
	 * @param nd	Number of decimals to be shown if numerically showing expression levels
	 */
	public MicroarrayData(String path, boolean invert, int rowHeader, int colHeader, int nd, MicroarrayRequester mr) throws Exception
		{
		MicroarrayLoadProgressMonitor pmd=new MicroarrayLoadProgressMonitor();
		loadTask=new LoadTask();
		loadTask.path=path;
		loadTask.invert=invert;
		loadTask.rowHeader=rowHeader;
		loadTask.colHeader=colHeader;
		loadTask.nd=nd;
		pmd.setTask(loadTask);
		pmd.run();
		if(mr!=null)
			{
			microarrayRequester=mr;
			Thread wt=new Thread() {
				public void run() {
					try{microarrayRequester.receiveMatrix(loadTask.get());}catch(Exception e){e.printStackTrace();}
				}
			};
			wt.start();
			}
		}

	/**
	 * Corverts the expression matrix mat to a new matrix with information that Prefuse graphs can manage
	 * @param mat
	 * @param skipColumns number of columns to skip, as they are only informative
	 * @return
	 */
	private Table convert(Table mat, int skipColumns)
		{
		Table ret=new Table();
		ret.addColumn("gene", String.class);
		ret.addColumn("condition", String.class);
		ret.addColumn("level", double.class);
		ret.addColumn("rowRank", int.class);//For row/column reordering
		ret.addColumn("colRank", int.class);
		ret.addColumn("rowId", int.class);//For row/column selection
		ret.addColumn("colId", int.class);
		
		//Build sparse expression level matrix
		sparseExpressions=new Table();
		sparseExpressions.addColumn("gene", String.class);
		sparseExpressions.addColumn("condition", String.class);
		sparseExpressions.addColumn("level", double.class);
		sparseExpressions.addColumn("rowRank", int.class);
		sparseExpressions.addColumn("colRank", int.class);
		sparseExpressions.addColumn("rowId", int.class);
		sparseExpressions.addColumn("actualRowId", int.class);//Ids in the whole matrix ret
		sparseExpressions.addColumn("colId", int.class);
		
		int row=0;
		int sparseRow=0;
		int contGene=0;
		int step=1;
		if(numGenes>maxGenes)	step=numGenes/maxGenes;
		matrix=new double[numGenes][numConditions];
		average=0;
		for(int i=0;i<numGenes;i++)
			{
			for(int j=0;j<numConditions;j++)
				{
				row=ret.addRow();
				ret.setString(row, "gene", geneNames[i]);
				ret.setString(row, "condition", conditionNames[j]);
				
				if(mat.canGet(conditionNames[j],Double.class))	
					{
					matrix[i][j]=mat.getDouble(i, j+skipColumns);
					ret.setDouble(row, "level", matrix[i][j]);
					average+=matrix[i][j];
					}
				else												
					{
					if(mat.canGet(conditionNames[j],Integer.class))
						{
						double value=((Integer)mat.get(i,j+skipColumns)).doubleValue();
						matrix[i][j]=(float)value;
						average+=value;
						ret.setDouble(row, "level", matrix[i][j]);
						}
					}
				ret.setInt(row, "rowId", i);
				ret.setInt(row, "rowRank", i);
				ret.setInt(row, "colId", j);
				ret.setInt(row, "colRank", j);
				row++;
				if(i%step==0)	//replicate for sparse matrix
					{
					sparseRow=sparseExpressions.addRow();
					sparseExpressions.setString(sparseRow, "gene", geneNames[i]);
					sparseExpressions.setString(sparseRow, "condition", conditionNames[j]);
					
					if(mat.canGet(conditionNames[j],Double.class))	sparseExpressions.setDouble(sparseRow, "level", ((Double)mat.get(i,j+skipColumns)).doubleValue());
					else												
						{
						if(mat.canGet(conditionNames[j],Integer.class))
							{
							sparseExpressions.setDouble(sparseRow, "level", matrix[i][j]); }
						}
					sparseExpressions.setInt(sparseRow, "rowId", contGene);
					sparseExpressions.setInt(sparseRow, "actualRowId", i);
					sparseExpressions.setInt(sparseRow, "rowRank", contGene);
					sparseExpressions.setInt(sparseRow, "colId", j);
					sparseExpressions.setInt(sparseRow, "colRank", j);
					sparseRow++;
					}
				}
			if(i%step==0)	contGene++;
			}
		
		average/=numGenes*numConditions;
		return ret;
		}
	
	
	/*public void receiveMatrix(int status)
	{
		System.out.println("Finished microarray data reading wiht "+status);
	}*/

	/**
	 * Inverts rows and columns
	 * @param table
	 * @return
	 */
	private Table invert(Table table)
		{
		Table ret=new Table();
		
		for(int i=0;i<table.getRowCount();i++)//Ponemos tantas columnas como filas
			{
			ret.addColumn(conditionNames[i],double.class);
			}
		int row=0;
		for(int i=0;i<table.getColumnCount();i++)
			{
			row=ret.addRow();
			for(int j=0;j<table.getRowCount();j++)
				ret.setDouble(row,conditionNames[j],((Double)table.get(j,i)).doubleValue());
			row++;
			}
		return ret;
		}

	/**
	 * Builds an internal sparse matrix that contains the specified genes. A sparse matrix is a reduced matrix
	 * from the whole matrix, with just some genes to avoid visualization or computing downgrade.
	 * It contains only numSparseGenes (typically, a number around 200).
	 * The rest of genes up to numSparseGenes are randomly selected
	 * TODO: make the selection totally random after previous selections
	 * 
	 * @param genes
	 */
	public void buildSparse(LinkedList<Integer> genes)
		{
		buildSparseGeneMatrix();
		for(int i=0;i<genes.size();i++)
			{
			Table t=sparseGeneLabels.select(ExpressionParser.predicate("actualId = "+genes.get(i)+""), new Sort());
			if(t==null || t.getRowCount()==0)
				{
				int j=0;
				while(genes.contains(sparseGeneLabels.getTuple(j).getInt("actualId")))	j++;//mientras est� en genes, probamos con la sig.
				sparseGeneLabels.setString(j, "name", geneNames[genes.get(i)]);//Nuevo nombre e id para �l, el resto no cambia
				sparseGeneLabels.setInt(j, "actualId", genes.get(i));
				int id=sparseGeneLabels.getInt(j, "id");
				int actualId=genes.get(i);
				int cont=j*numConditions;
				for(int k=0;k<this.numConditions;k++)
					{
					this.sparseExpressions.setInt(cont+k, "actualRowId",actualId);
					this.sparseExpressions.setInt(cont+k, "rowId",id);
					this.sparseExpressions.setString(cont+k, "gene",geneNames[actualId]);
					this.sparseExpressions.setDouble(cont+k, "level",matrix[actualId][k]);
					}
				}
			}
		return;
		}

	/**
	 * Returns the table with expression levels
	 * @return	Table with expression levels
	 */
	public Table getExpressions() {
		
		return expressions;
	}
	
	/**
	 * Returns the expression level of gene i under condition j
	 * @param i	position in the matrix of the gene
	 * @param j	position in the matrix of the condition
	 * @return	expression level in row i, column j
	 */
	public double getExpressionAt(int i, int j)
		{
		return matrix[i][j];
		}

	/**
	 * Sets the table with expression levels
	 * @param expressions	Table with expression levels (see the description of this class)
	 */
	public void setExpressions(Table expressions) {
		this.expressions = expressions;
	}

	/**
	 * Returns the number of genes in the Microarray
	 * @return the number of genes in the Microarray
	 */
	public int getNumGenes() {
		return numGenes;
	}

	/**
	 * Returns the number of conditions in the Microarray
	 * @return the number of conditions in the Microarray
	 */
	public int getNumConditions() {
		return numConditions;
	}

	/**
	 * Returns the gene information
	 * @return	a Table with gene names, ids and ranks
	 */
	public Table getGeneLabels() {
		return geneLabels;
	}

	/**
	 * Returns the condition information
	 * @return a Table with condition names, ids, and ranks
	 */
	public Table getConditionLabels() {
		return conditionLabels;
	}
	
	/**
	 * Returns the condition information
	 * @return a Table with condition names, ids, and ranks
	 */
	public Table getConditionLabelsSameLength() {
		Table t=conditionLabels;
		int lmax=0;
		for(int j=0;j<t.getRowCount();j++)
			{
			int l=t.getString(j,"name").length();
			if(l>lmax)		lmax=l;
			}
		for(int j=0;j<t.getRowCount();j++)
			{
			String s=t.getString(j,"name");
			while(s.length()<lmax)	s=s+" ";
			}
		return t;
	}
	
	/**
	 * Returns the condition names
	 * @return	an array of strings with condition names, ordered by id
	 */
	public String[] getConditionNames() {
		return conditionNames;
	}
	
	/**
	 *	Returns the name of the condition at the specified position 
	 * @param pos	position of the condition in the matrix
	 * @return	name of the condition
	 */
	public String getConditionName(int pos)
		{
		return conditionNames[pos];
		}

	/**
	 * Returns the gene names
	 * @return	an array of strings with gene names, ordered by id
	 */
	public String[] getGeneNames() {
		return geneNames;
	}

	/**
	 * Returns the condition id of a condition name
	 * @param conditionName	condition name to know its id
	 * @return	id for the condition name, or -1 if it is not found
	 */
	public int getConditionId(String conditionName)
		{
		int id=-1;
		for(int i=0;i<conditionNames.length;i++)
			if(conditionNames[i].equals(conditionName))	
				return i;
		return id;
		}
	
	/**
	 * Returns the gene id of a gene name
	 * @param geneName	gene name to know its id
	 * @return	id for the gene name, or -1 if it is not found
	 */
	public int getGeneId(String geneName)
		{
		int id=-1;
		for(int i=0;i<geneNames.length;i++)
			if(geneNames[i].equals(geneName))	
				return i;
		return id;
		}
	
	/**
	 * Return the sparse id of an actual gene id
	 * @param geneId
	 * @returns sparse gene id, or -1 if not in the sparse matrix
	 */
	public int getSparseGeneId(int id)
		{
		int sid=-1;
		IntIterator it=this.sparseGeneLabels.rows(ExpressionParser.predicate("actualId="+id));
		sid=sparseGeneLabels.getInt((Integer)it.next(), "id");
		return sid;
		}
	
	/**
	 * Returns the constance by rows, columns or both, for a subset and genes and/or conditions
	 * The subset of genes and the subset of conditions may be null, but not both of them. If one 
	 * of them is null, all gene/condition profile is considered.
	 * @param genes - subgroup of genes
	 * @param conditions - subgroup of conditions 
	 * @param type - type of constance, by rows (0), by columns (1) or by both rows and columns (2)
	 * @return the constance value for the subgroup
	 */
	public float getConstance(ArrayList<String> genes, ArrayList<String> conditions, int type)
		{
		float constance=0;
		double matrixBic[][]=new double[genes.size()][conditions.size()];
		//1) Recuperamos la matriz sobre la que calcular la constancia
		for(int i=0;i<genes.size();i++)
			{
				String gene=genes.get(i);
			int row=getGeneId(gene);
			for(int j=0;j<conditions.size();j++)
				{
				String cond=conditions.get(j);
				int col=getConditionId(cond);
				matrixBic[i][j]=matrix[row][col];
				}
			}
		//2) Calculamos la constancia
		float sum=0;
		float sd=0;
		int num=0;
		switch(type)
			{
			case 0:
				num=genes.size();
				double[] midpoint=new double[conditions.size()];
				for(int i=0;i<conditions.size();i++)
					for(int j=0;j<genes.size();j++)
						midpoint[i]+=matrixBic[j][i];
				for(int i=0;i<conditions.size();i++)	midpoint[i]/=genes.size();
				for(int i=0;i<genes.size();i++)
					{
					double[] p0=new double[conditions.size()];
					for(int j=0;j<conditions.size();j++)	p0[j]=matrixBic[i][j];
					sd+=euclideanDistance(p0, midpoint);
					}
				constance=(float)Math.sqrt(sd/num);
					
				break;
			case 1:
				num=conditions.size();
				break;
			case 2:
				num=genes.size()*conditions.size();
				for(int i=0;i<genes.size();i++)
					for(int j=0;j<conditions.size();j++)
						sum+=matrixBic[i][j];
				for(int i=0;i<genes.size();i++)
					for(int j=0;j<conditions.size();j++)
						sd+=Math.abs(matrixBic[i][j]-sum);
				constance=sd/num;
				break;
			}
		
		return constance;
		}
	
	private double euclideanDistance(double[] p1, double[] p2)
	{
	double ret=0;
	for(int i=0;i<p1.length;i++)
		ret+=(p1[i]-p2[i])*(p1[i]-p2[i]);
	return Math.sqrt(ret);
	}
    
	/**
	 * Returns a list of gene and condition names from two lists with genes and conditions ids
	 * @param lg	list of gene ids 
	 * @param lc	list of condition ids 
	 * @return	An ArrayList with Strings with corresponding gene and condition names
	 */
	public ArrayList<String> getNames(LinkedList<Integer> lg, LinkedList<Integer> lc)
		{
		ArrayList<String> ret=new ArrayList<String>();
		for(int i=0;i<lg.size();i++)		ret.add(geneNames[lg.get(i)]);
		for(int i=0;i<lc.size();i++)		ret.add(conditionNames[lc.get(i)]);
		return ret;
		}
	/**
	 * Returns a list of gene names from a list with genes ids
	 * @param lg	list of gene ids
	 * @return	An ArrayList with Strings with corresponding gene and condition names
	 */
	public ArrayList<String> getGeneNames(LinkedList<Integer> lg)
		{
		ArrayList<String> ret=new ArrayList<String>();
		for(int i=0;i<lg.size();i++)		ret.add(geneNames[lg.get(i)]);
		return ret;
		}
	
	/**
	 * Returns a list of condition names a lists and conditions ids
	 * @param lc	list of condition ids 
	 * @return	An ArrayList with Strings with corresponding gene and condition names
	 */
	public ArrayList<String> getConditionNames(LinkedList<Integer> lc)
		{
		ArrayList<String> ret=new ArrayList<String>();
		if(conditionNames!=null)	
			for(int i=0;i<lc.size();i++)		ret.add(conditionNames[lc.get(i)]);
		return ret;
		}
	
	/**
	 * Returns the Prefuse Table with expression levels for the sparse matrix
	 */
	public Table getSparseExpressions() {
		return sparseExpressions;
	}

	/**
	 * Returns the Prefuse Table with gene labels for the sparse matrix.
	 * @return
	 */
	public Table getSparseGeneLabels() {
		return sparseGeneLabels;
	}

	/**
	 * Returns the number of genes in the sparse matrix
	 * @return
	 */
	public int getNumSparseGenes() {
		return numSparseGenes;
	}
	/**
	 * Sets the number of genes in the sparse matrix
	 * @param numSparseGenes
	 */
	void setNumSparseGenes(int numSparseGenes) {
		this.numSparseGenes = numSparseGenes;
	}
	
	/**
	 * Returns a string with the requested value as a text string, for a determinate dimension
	 * @param value	value to format to text
	 * @param dim	dimension to which the value pertains
	 * @return	a text string with the value, formatted to have the number of decimals set for its dimension
	 */
	public String format(double value, int dim)
		{
		int nc=decimals[dim];
		String cad;
		if(nc>0)	cad=new Double(value).toString();
		else		cad=new Double(Math.abs(Math.rint(value))).toString();
		int pos=0;
		if((pos=cad.indexOf("."))>=0)
			{
			int pos2=0;
			if((pos2=cad.indexOf("E"))>=0)
				{
				if(nc>0)	cad=cad.substring(0,pos)+cad.substring(pos, pos+1+nc)+cad.substring(pos2);
				else		cad=cad.substring(0,pos)+cad.substring(pos2);
				}
			else
				{
				if(nc>0)
					{
					if(pos+1+nc<=cad.length())	cad=cad.substring(0,pos)+cad.substring(pos, pos+1+nc);
					else						;//En este caso ya est�, no tenemos que quitar decimales
					}
				else		cad=cad.substring(0,pos);
				}
			}
		return cad;
		}

	/**
	 * This class inits all the libraries and soap services required to get gene annotations
	 * @author Rodrigo
	 *
	 */
	private class AnnotationStartThread extends Thread{
		   long minPrime;
	         AnnotationStartThread() {
	         }
	         
	         /**
	          * Tries to call BioConductor methods to automatically install a library
	          * @param exp REXP with the console into which to call for the install methods
	          * @param lib Library name to install
	          * @return
	          */
	         public int installPackage(REXP exp, String lib)
	         	{
	        	exp=re.eval("source(\"http://bioconductor.org/biocLite.R\")");
	    	    if(exp==null)	System.out.println("sourcing bioconductor returns null");
	    	    exp=re.eval("biocLite(\""+lib+"\")");
	    	    if(exp==null)	System.out.println("installing "+lib+" returns null");
	    	    exp=re.eval("library("+lib+")");
	    	    if(exp==null)
	    	    	{
	    	    	JOptionPane.showMessageDialog(null,
						"Package "+lib+" is not installed in R and could not be installed automatically\n Please install the package manually through the R console \nIn the meantime, annotations and Tag clouds won't be available", 
						"Missing R package", JOptionPane.WARNING_MESSAGE);
	    	    	return -1;
	    	    	
	    	    	}
	    	    return 0;
		     	}
	         
	         //thru R (quicker and simpler)
	         /**
	          * Loads R console and the required libraries. By now: annotate, GO.db, GOstats.
	          * It also loads the file GOgroups.R in the resources package and the annotation package
	          * specified in the microarray data file if available.
	          */
	         public void run()
	         	{
	        	System.out.println("Library is: "+System.getProperty("java.library.path"));
	        	try{
	        	if (!Rengine.versionCheck()) 
	        		{
	        		JOptionPane.showMessageDialog(null,
							" Version mismatch - Java files don't match library version", 
							"Version mismatch", JOptionPane.WARNING_MESSAGE);
		    	    System.err.println("** Version mismatch - Java files don't match library version.");
	    			}
	        	}catch(Exception e)
	        		{
	        		System.out.println("Library is: "+System.getProperty("java.library.path"));
	        		System.err.println("R cannot be loaded: " + e.getMessage());
	        		e.printStackTrace();
	        		}
    	        System.out.println("Creating Rengine (with arguments)");
    		
    	        re=new Rengine(new String[]{"--vanilla"}, false, null);
    	        System.out.println("Rengine created, waiting for R");
    			// the engine creates R in a new thread, so we should wait until it's ready
    	        if (!re.waitForR()) 
    	        	{
    	            System.err.println("Cannot load R");
    	            return;
    	        	}
	    	    System.out.println("R started");
	    	    REXP exp=null;
	    	    
	    	    if(chip.contains("yeast") || chip.contains("YEAST") || chip.contains("ygs98") || chip.contains("pombe") )
	    	    	{
	    	    	if(chip.equals("YEAST"))
	    	    		rgo="GOTERM";
	    	    	}
	    	    else
	    	    	{
	    	    	rname="SYMBOL";
	    	    	rdescription="GENENAME";
	    	    	}
	    	    //R specific
	    	    if(chip.equals("GeneName") || chip.equals("GeneID"))
	    	    	{
	    	    	searchByR=false;
	    	    	//Initial search by NCBI takes some time, do it in background when loading
	    	    	Thread wt=new Thread() {
	    				public void run() {
	    					try{
	    						LinkedList<Integer> l=new LinkedList<Integer>();
	    		    	    	l.add(getGeneId(geneNames[0]));
	    		    	    	t=new AnnotationTask(l);
	    		    	    	t.execute();
	    						}catch(Exception e){e.printStackTrace();}
	    				}
	    			};
	    			wt.start();
	    	    	}
	    	    else
		    	    {
		    	    exp=re.eval("library("+chip+")");
		    	    if(exp==null)	System.out.println("library "+chip+" returns null in R");
			        if(exp==null)
			        	{
			        	if(installPackage(exp, chip)<0)
			    	    	{
			        		chip="GeneName";
			    	    	searchByR=false;
			    	    	}
			    	    else	searchByR=true;
						}
			        else	searchByR=true;
					
			        //GO specific
		    	    exp=re.eval("library(GO.db)");
		    	    if(exp==null)	installPackage(exp, "GO.db");
		    	    exp=re.eval("library(GOstats)");
		    	    if(exp==null)	installPackage(exp, "GOstats");
		    	    exp=re.eval("library(annotate)");
		    	    if(exp==null)	installPackage(exp, "annotate");
		    	    
		    	    exp=re.eval("source(\"es/usal/bicoverlapper/source/codeR/GOgroups.R\")");
		    	     
		    	    chip=chip.replace(".db", "");
		    	    }
	         	}
	}//end Thread class

	/**
	 * Returns a map with the GeneAnnotations for the genes that have been annotated right now
	 *  (to not downgrade memory and computing performance by retrieving all the annotations at once, 
	 *  annotations are retrieved on demand). The Integer key refers to the Prefuse table "rowId".
	 * @return
	 */
	public Map<Integer, GeneAnnotation> getGeneAnnotations() {
		return geneAnnotations;
	}
	/**
	 * Returns the gene annotations for the gene with the corresponding id.
	 * Annotations are retrieved from BioConductor annotation package or by Entrez Gene and QuickGO 
	 * if there was no annotation package or it cannot be successfully loaded. 
	 * @param id "rowId" in the Prefuse Table for the desired gene
	 */
	public void getGeneAnnotation(Integer id)
		{
		LinkedList<Integer> l=new LinkedList<Integer>();
		l.add(id);
		getGeneAnnotations(l, null, new Point(0,0));
		}
	/*
 	public GeneAnnotation getGeneAnnotationR(Integer id)
		{
		GeneAnnotation ga=geneAnnotations.get(id);
		if(ga==null)
			{
			ga=new GeneAnnotation();
			String gene=geneNames[id];
	    	System.out.println("GENE "+gene);
	    	REXP exp=re.eval("group=c(\""+gene+"\")");
	    	ga.id=gene;
	    	exp=re.eval("get(\""+gene+"\","+chip+rname+")");
	    	if(exp!=null)	
	    		{
	    		System.out.println("name: "+exp.asString());
				ga.name=exp.asString();
	    		}
	    	exp=re.eval("get(\""+gene+"\","+chip+rdescription+")");
	    	if(exp!=null)	
	    		{
	    		System.out.println("desc: "+exp.asString());
				ga.description=exp.asString();
	    		}
	    	geneAnnotations.put(id, ga);
			}
		return ga;
		}*/
	
	/**
	 * Retrieves the gene annotations for the specified gene. It used an annotation task progress
	 * monitor, so the results of the task are received by a GeneRequester.
	 * @param gene name of the gene as it is in the "gene" field of the Prefuse tables
	 * @param gr GeneRequester interface that received the results
	 * @param p Point with the optional location of the task bar. If set to null, it is located at (0,0).
	 */
	public void getGeneAnnotation(String gene, GeneRequester gr, Point p)
		{
		int id=getGeneId(gene);
		if(id<0)
			{
			System.err.println("Gene "+gene+" does not exist in the microarray");
			return;
			}
		LinkedList<Integer> ids=new LinkedList<Integer>();
		ids.add(id);
		getGeneAnnotations(ids, gr, p);
		}

	/**
	 * Returns the GOTerms associated to a group of genes (as a whole, not individually related
	 * to each gene) up to a given p-value cutoff.
	 * @param genes "rowId" identifiers of genes
	 * @param cutoff typically 0.1 or lower
	 * @return
	 */
   public ArrayList<GOTerm> getGOTermsHypergeometric(LinkedList<Integer> genes, double cutoff)
 	 	{
	    return getGOTermsHypergeometric(getGeneNames(genes), cutoff);
 	 	}
   
   /**
    * Returns the GOTerms associated to a group of genes (as a whole, not individually related
	 * to each gene) up to a given p-value cutoff.
	 * @param genes gene names
    * @param cutoff typically 0.1 or lower
    * @return
    */
	public ArrayList<GOTerm> getGOTermsHypergeometric(ArrayList<String> genes, double cutoff)
		{
		String n="c(\"";
		String universe="c(\"";
	    for(int i=0;i<genes.size();i++)
	    	{
	    	if(i<genes.size()-1)	n=n.concat(genes.get(i)+"\", \"");
	    	else					n=n.concat(genes.get(i)+"\")");
	    	}
	    for(int i=0;i<this.geneNames.length;i++)
	    	{
	    	if(i<geneNames.length-1)	universe=universe.concat(geneNames[i]+"\", \"");
	    	else						universe=universe.concat(geneNames[i]+"\")");
	    	}
		    
		REXP exp=null;
		exp=re.eval("chipEntrezUniverse <- unique(unlist(mget("+universe+", "+chip+"ENTREZID)))");
		exp=re.eval("selectedEntrezIds <- unlist(mget("+n+","+chip+"ENTREZID))");

	    exp=re.eval("params <- new(\"GOHyperGParams\", geneIds = selectedEntrezIds," +
				"universeGeneIds = chipEntrezUniverse, annotation = \""+chip+"\"," +
				"ontology = \"BP\", pvalueCutoff = "+cutoff+", conditional = FALSE," +
				"testDirection = \"over\")");
		exp=re.eval("hgOver <- hyperGTest(params)");
		exp=re.eval("df=summary(hgOver, pvalue=0.1)");
	    exp=re.eval("df$Term");
    	String[] t=exp.asStringArray();
    	exp=re.eval("df$GOBPID");
    	String[] ids=exp.asStringArray();
    	exp=re.eval("df$Count");
    	int[] evs=exp.asIntArray();
    	exp=re.eval("df$Pvalue");
    	double[] pval=exp.asDoubleArray();
    	if(evs!=null && ids !=null && t!=null 
    			&& t.length==evs.length && t.length==ids.length && evs.length==ids.length &&
    			ids.length==t.length)
    		{
    		ArrayList<GOTerm> got=new ArrayList<GOTerm>();
    		for(int i=0;i<evs.length;i++)
    			{
    			GOTerm g=new GOTerm(t[i], ids[i], "", "", "", evs[i]);
    			g.pvalue=pval[i];
    			got.add(g);
    			}
    		return got;
    		}
    	return null;
		}
	
	/**
	 * TODO: continue the checking from here on
	 * @param genes
	 * @param oncePerGene
	 * @return
	 */
	public List<GOTerm> getGOTerms(ArrayList<String> genes, boolean oncePerGene)
		{
		//1) Complete gene annotations 
		if(this.searchByR)		return getGOTermsR(genes);
		else					return getGOTermsQuickGO(genes, oncePerGene);
	    }
	
	/**
	 * Returns a list of GOTerms, the field occurrences in GOTerm defines the number of 
	 * times that the GOTerm appears in genes in the input list. 
	 * @param genes
	 * @param oncePerGene if true, each GOTerm is counted one time even if there are several occurrences for a gene
	 * 						(it can happen that a gene has been annotated several times with the same GOTerm, if the annotations comes from several sources (at least, this is the protocol of QuickGO) 
	 * @return
	 */
	public List<GOTerm> getGOTermsQuickGO(ArrayList<String> genes, boolean oncePerGene)
		{
		HashMap<String, GOTerm> gom=new HashMap<String, GOTerm>();
		for(String gene : genes)
			{
			GeneAnnotation ga=geneAnnotations.get(this.getGeneId(gene));
			List<GOTerm> gol=null;
			
			if(ga.goTerms==null || ga.goTerms.size()==0)
						gol=getTermsQuickGO(ga, oncePerGene);
			else		gol=ga.goTerms;
			for(GOTerm go : gol)
				{
				if(gom.containsKey(go.term))	gom.get(go.term).occurences++;
				else							gom.put(go.term, go);
				}
			}
		return Arrays.asList(gom.values().toArray(new GOTerm[0]));
		}
	
	public ArrayList<GOTerm> getGOTermsR(ArrayList<String> genes)
		{
		REXP exp=null;
	       
	    String n="c(\"";
	    for(int i=0;i<genes.size();i++)
	    	{
	    	if(i<genes.size()-1)	n=n.concat(genes.get(i)+"\", \"");
	    	else					n=n.concat(genes.get(i)+"\")");
	    	}

	    exp=re.eval("got=getGOTerms("+n+", "+chip+rgo+")");
	    if(exp==null)	System.err.println("Error getting GO terms with R");
    	exp=re.eval("got@terms");
    	if(exp==null)	return null;
    	String[] t=exp.asStringArray();
    	exp=re.eval("got@definitions");
    	String[] d=exp.asStringArray();
    	exp=re.eval("got@ontologies");
    	String[] o=exp.asStringArray();
    	exp=re.eval("got@ids");
    	String[] ids=exp.asStringArray();
    	exp=re.eval("got@evidences");
    	int[] evs=exp.asIntArray();
    	ArrayList<GOTerm> got=new ArrayList<GOTerm>();
		
    	if(evs!=null && ids !=null && o!=null && d!=null && t!=null 
    			&& t.length==d.length && o.length==ids.length && evs.length==ids.length &&
    			ids.length==t.length)
    		{
        	System.out.println("Retrieved "+t.length+" genes");
    		for(int i=0;i<evs.length;i++)
    			{
    			GOTerm g=new GOTerm(t[i], ids[i], d[i], o[i], "", evs[i]);
    			got.add(g);
    			}
    		return got;
    		}
    	return null;
		}

	public ArrayList<GOTerm> getGOTermsRbyID(String[] goids)
	{
	REXP exp=null;
       
    String n="c(\"";
    for(int i=0;i<goids.length;i++)
    	{
    	if(i<goids.length-1)	n=n.concat(goids[i]+"\", \"");
    	else					n=n.concat(goids[i]+"\")");
    	}
    exp=re.eval("got=getGOTermsByGOID("+n+")");
    if(exp==null)	System.err.println("Error getting GO terms by ID with R");
	exp=re.eval("got@terms");
	if(exp==null)	return null;
	String[] t=exp.asStringArray();
	exp=re.eval("got@definitions");
	String[] d=exp.asStringArray();
	exp=re.eval("got@ontologies");
	String[] o=exp.asStringArray();
	exp=re.eval("got@ids");
	String[] ids=exp.asStringArray();
	exp=re.eval("got@evidences");
	int[] evs=exp.asIntArray();
	if(evs.length==0)
		{
		evs=new int[goids.length];
		for(int i=0;i<evs.length;i++)	evs[i]=1;
		}
	ArrayList<GOTerm> got=new ArrayList<GOTerm>();
	
	if(evs!=null && ids !=null && o!=null && d!=null && t!=null) 
		//	&& t.length==d.length && o.length==ids.length && evs.length==ids.length &&
		//	ids.length==t.length)
		{
    	for(int i=0;i<evs.length;i++)
			got.add(new GOTerm(t[i], ids[i], d[i], o[i], "", evs[i]));
		return got;
		}
	return null;
	}

	public void getGeneAnnotations(LinkedList<Integer> genes, GeneRequester gr, Point location)
		{
		getGeneAnnotations(genes, gr, location, false);
		}
	//JDK1.6
	public void getGOTermsHypergeometric(LinkedList<Integer> genes, GeneRequester gr, Point location, String ontology) 
	{
	ht=new HypergeometricTestTask(genes, ontology);
	HypergeometricTestProgressMonitor hpm=null;
	if(location!=null)	hpm=new HypergeometricTestProgressMonitor(location);
	else				hpm=new HypergeometricTestProgressMonitor(new Point(0,0));
	hpm.setTask(ht);
	hpm.run();
	if(gr!=null)
		{
		geneRequester=gr;
		Thread wt=new Thread() {
			public void run() {
				try{geneRequester.receiveGOTerms(ht.get());}catch(Exception e){e.printStackTrace();}
			}
		};
		wt.start();
		}
	}

	//***** FOR JDK 1.6
	public void getGeneAnnotations(LinkedList<Integer> genes, GeneRequester gr, Point location, boolean pValues) 
		{
		t=new AnnotationTask(genes);
		if(location!=null)	amd=new AnnotationProgressMonitor(location);
		else				amd=new AnnotationProgressMonitor(new Point(0,0));
		amd.setTask(t);
		amd.run();
		if(gr!=null)
			{
			geneRequester=gr;
			Thread wt=new Thread() {
				public void run() {
					try{geneRequester.receiveGeneAnnotations(t.get());}catch(Exception e){e.printStackTrace();}
				}
			};
			wt.start();
			}
		}
	// FOR JDK 1.6
	//public class HypergeometricTestTask extends SwingWorker15<ArrayList<GOTerm>, Void>// implements Runnable
	public class HypergeometricTestTask extends SwingWorker<ArrayList<GOTerm>, Void>// implements Runnable
		{
		public LinkedList<Integer> genes;
		public ArrayList<GOTerm> golist=null;
		public String message="";
		private int progress;
		public String ontology="";
		public HypergeometricTestTask(LinkedList<Integer> genes, String ontology)
			{
			this.genes=genes;
			this.ontology=ontology;
			golist=new ArrayList<GOTerm>();
			}
		@Override
		public ArrayList<GOTerm> doInBackground()
			{
			golist=getGOTermsHypergeometric(genes, ontology, 0.001);
			progress=100;
			setProgress(progress);
			
			done();
			return golist;
			}
		
		public ArrayList<GOTerm> getGOTermsHypergeometric(LinkedList<Integer> genes, String ontology, double cutoff)
	 	 	{
		    return getGOTermsHypergeometric(getGeneNames(genes), ontology, cutoff);
	 	 	}
		
		/**
		 * Call to hyperGTest from GOstats Bioconductor library
		 * @param genes - list of gene ids. HyperGTest will use its corresponding EntrezIDs to perform the test
		 * @param ontology - either "BP", "MF" or "CC". Only in capitals, and only this values, or R will fail
		 * @param cutoff - Hypergeometric Test cutoff
		 * @return - all the GOTerms that pass the hypergeometric tests, with pvalue below 0.1
		*/ 
		public ArrayList<GOTerm> getGOTermsHypergeometric(ArrayList<String> genes, String ontology, double cutoff)
			{
			String n="c(\"";
			String universe="c(\"";
		    for(int i=0;i<genes.size();i++)
		    	{
		    	if(i<genes.size()-1)	n=n.concat(genes.get(i)+"\", \"");
		    	else					n=n.concat(genes.get(i)+"\")");
		    	}
		    for(int i=0;i<geneNames.length;i++)
		    	{
		    	if(i<geneNames.length-1)	universe=universe.concat(geneNames[i]+"\", \"");
		    	else						universe=universe.concat(geneNames[i]+"\")");
		    	}
			    
			REXP exp=null;
			exp=re.eval("chipEntrezUniverse <- unique(unlist(mget("+universe+", "+chip+"ENTREZID)))");
			exp=re.eval("selectedEntrezIds <- unlist(mget("+n+","+chip+"ENTREZID))");

		    exp=re.eval("params <- new(\"GOHyperGParams\", geneIds = selectedEntrezIds," +
					"universeGeneIds = chipEntrezUniverse, annotation = \""+chip+"\"," +
					"ontology = \""+ontology+"\", pvalueCutoff = "+cutoff+", conditional = FALSE," +
					"testDirection = \"over\")");
			exp=re.eval("hgOver <- hyperGTest(params)");
			exp=re.eval("df=summary(hgOver, pvalue=0.1)");
		    exp=re.eval("df$Term");
	    	String[] t=exp.asStringArray();
	    	exp=re.eval("df$GOBPID");
	    	String[] ids=exp.asStringArray();
	    	exp=re.eval("df$Count");
	    	int[] evs=exp.asIntArray();
	    	exp=re.eval("df$Pvalue");
	    	double[] pval=exp.asDoubleArray();
	    	if(evs!=null && ids !=null && t!=null 
	    			&& t.length==evs.length && t.length==ids.length && evs.length==ids.length &&
	    			ids.length==t.length)
	    		{
	    		ArrayList<GOTerm> got=new ArrayList<GOTerm>();
	    		for(int i=0;i<evs.length;i++)
	    			{
	    			GOTerm g=new GOTerm(t[i], ids[i], "", "", "", evs[i]);
	    			g.pvalue=pval[i];
	    			got.add(g);
	    			}
	    		return got;
	    		}
	    	return null;
			}
		
		@Override
		public void done() {
			}
		}
	
	//**********************************************************************
	public class AnnotationTask extends SwingWorker<ArrayList<GeneAnnotation>, Void>// implements Runnable
	{
		public String gene;
		public String message;
		public int id;
		public LinkedList<Integer> genes;
		public GeneAnnotation ga;
		public AnnotationProgressMonitor apm;
		public ArrayList<GeneAnnotation> galist=null;
		
		
		public AnnotationTask(int id)
			{
			this.id=id;
			}
		public AnnotationTask(LinkedList<Integer> genes)
			{
			this.genes=genes;
			galist=new ArrayList<GeneAnnotation>();
			}
		
		/**
		 * Builds up an R structure c("id1", "id2", ...) for the elemnts in the list
		 * @param list
		 * @return
		 */
		private String getGroup(String[] list)
			{
			String ret="c(";
			for(String i:list)
				{
				ret+="\""+i+"\",";
				}
			return ret.substring(0, ret.length()-1)+")";
			}
		
		/**
		 * Annotations retrieved with method mget() from R
		 * @return
		 */
		public ArrayList<GeneAnnotation> getMultipleGeneAnnotationsR()
			{
			int progress=0;
			long t1=System.currentTimeMillis();
			
			message="searching for selected genes in "+organism+" ...";
	 		progress+=100*0.5/genes.size();
	 		setProgress(progress);
	 		
	 		//0) Build the group of genes to search for
	 		String group="";
	 		for(int g=0;g<genes.size();g++)
				{
				Integer id=genes.get(g);
				group=group+"\""+geneNames[id]+"\",";
				}
	 		group=group.substring(0, group.length()-1);
			REXP exp=re.eval("group=c("+group+")");
			
			RList names=null, descriptions=null;
	    	
			//1) Search for gene names
	    	exp=re.eval("mget(group,"+chip+rname+")");
	    	if(exp!=null)	
	    		{
	    		names=exp.asList();
	    		System.out.println("names retrieved correctly for "+names.keys().length+" genes");
	    		}
	    	
	    	//2) Search for gene descriptions
	    	exp=re.eval("mget(group,"+chip+rdescription+")");
	    	if(exp!=null)	
	    		{
	    		descriptions=exp.asList();
	    		System.out.println("descriptions retrieved correctly for "+descriptions.keys().length+" genes");
	    		}
	    	
	    	//3) Search for GO terms
	    	RList go=null;
	    	exp=re.eval("mget(group,"+chip+rgo+")");
	    	if(exp!=null)	
	    		{
	    		go=exp.asList();
	    		System.out.println("go terms retrieved correctly for "+descriptions.keys().length+" genes");
	    		}
	    	
	    	//4) Add all the new information to the genes
	    	for(int g=0;g<genes.size();g++)
				{
				Integer id=genes.get(g);
				String gene=geneNames[id];
				
				message="searching for "+gene+" in "+organism+" ...";
		 		progress+=100*0.5/genes.size();
		 		setProgress(progress);
		 		
				GeneAnnotation ga=geneAnnotations.get(id);
				if(ga==null)
					{
					ga=new GeneAnnotation();
					ga.description=descriptions.at(g).asString();
					ga.name=names.at(g).asString();
					
					RList goterms=go.at(g).asList();
					if(goterms!=null)
						{
						String ggo=getGroup(goterms.keys());
						ga.goTerms=getGOTermsRbyID(goterms.keys());
						}
					geneAnnotations.put(id, ga);
					System.out.println("Setting "+ga.name+"\t"+ga.description+"\tto gene "+geneNames[id]);
					}
				galist.add(ga);
				}
	
	    	System.out.println("Finished: annotations retrieved in "+(System.currentTimeMillis()-t1)/1000+" secs.");
			message="Finished: annotations retrieved in "+(System.currentTimeMillis()-t1)/1000+" secs.";
			progress=100;
			setProgress(progress);
			
			return galist;
			}
		
		/**
		 * Loop with use of method get() from R
		 * @return
		 */
		public ArrayList<GeneAnnotation> getGeneAnnotationR()
			{
			int progress=0;
			long t1=System.currentTimeMillis();
			
			for(int g=0;g<genes.size();g++)
				{
				Integer id=genes.get(g);
				String gene=geneNames[id];
				
				message="searching for "+gene+" in "+organism+" ...";
		 		progress+=100*0.5/genes.size();
		 		setProgress(progress);
		 		
				GeneAnnotation ga=geneAnnotations.get(id);
				if(ga==null)
					{
					ga=new GeneAnnotation();
					System.out.println("GENE "+gene);
			    	REXP exp=re.eval("group=c(\""+gene+"\")");
			    	ga.id=gene;
			    	exp=re.eval("get(\""+gene+"\","+chip+rname+")");
			    	if(exp!=null)	
			    		{
			    		System.out.println("name: "+exp.asString());
						ga.name=exp.asString();
			    		}
			    	exp=re.eval("get(\""+gene+"\","+chip+rdescription+")");
			    	if(exp!=null)	
			    		{
			    		System.out.println("desc: "+exp.asString());
						ga.description=exp.asString();
			    		}
			    	
			    	//GoTerms
			    	ArrayList<String> al=new ArrayList<String>();
			    	al.add(ga.id);
			    	ga.goTerms=getGOTermsR(al);
			    	geneAnnotations.put(id, ga);
					}
				galist.add(ga);
				}
			
	    	System.out.println("Finished: annotations retrieved in "+(System.currentTimeMillis()-t1)/1000+" secs.");
	    	message="Finished: annotations retrieved in "+(System.currentTimeMillis()-t1)/1000+" secs.";
			progress=100;
			setProgress(progress);
			
			return galist;
			}
		
		public ArrayList<GeneAnnotation> getGeneAnnotationNCBI()
			{
			double progress=0;
			long t1=System.currentTimeMillis();
			for(int g=0;g<genes.size();g++)
				{
				String gene=geneNames[genes.get(g)];
		    	ArrayList<String> ncbiIds=new ArrayList<String>();
		    	
		 		message="searching for "+gene+" in "+organism+" ...";
		 		progress+=100*0.5/genes.size();
		 		setProgress((int)progress);
		 		ga=geneAnnotations.get(genes.get(g));
		 		if(ga==null)
			 		{
		 			long t2=System.currentTimeMillis();
					
			 		IdListType list=null;
			 		if(chip.equals("GeneName"))//if chip is GeneName
						{
			 			System.out.println("searching for "+gene+"[gene] AND \""+organism+"\"[organism]");
				 		list=NCBIReader.eGeneQuery(gene+"[gene] AND \""+organism+"\"[organism]");
						}
			 		if(chip.equals("GeneID"))
			 			{
			 			System.out.println("searching for "+gene+"[uid] AND \""+organism+"\"[organism]");
				 		list=NCBIReader.eGeneQuery(gene+"[uid] AND \""+organism+"\"[organism]");
			 			}
			 		
					
			 		if(list!=null && list.getId()!=null)
						{
						if(list.getId().length>0)	
							{
							if(list.getId().length>1)
								System.out.println("WARNING: found "+list.getId().length+" coincidences, taking the first one as the best match");
							ncbiIds.add(list.getId(0));
							}
						
						message="Annotations found, collecting ...";
				 		progress+=100*0.5/genes.size();
				 		if(progress>=100)	progress=99;
				 		setProgress((int)progress);
						}
					else	
						{
						System.err.println("Nothing found for gene "+gene);
						ga=new GeneAnnotation();
						ga.id=gene;
						
						geneAnnotations.put(genes.get(g), ga);
						
						message="Nothing found for gene "+gene;
						progress+=100*0.5/genes.size();
						if(progress>=100)	progress=99;
				 		setProgress((int)progress);
				 		}
			 		
			 		if(list!=null && list.getId()!=null)
			 			{
						DocSumType[] res=NCBIReader.eGeneSummary(list.getId(0));
						ga=new GeneAnnotation();
						for(int i=0; i<res.length; i++)
							{
							for(int k=0; k<res[i].getItem().length; k++)
					          {
					          if(res[i].getItem()[k].get_any()!=null)
					          	{
					          	String cad=res[i].getItem(k).getName();
					          	ga.ncbiId=res[i].getId();
					          	if(cad.contains("Description"))
					          		ga.description=res[i].getItem()[k].get_any()[0].getValue();
					            else if(cad.contains("Name"))
					          		ga.name=res[i].getItem()[k].get_any()[0].getValue();
					            else if(cad.contains("Orgname"))
					          		ga.organism=res[i].getItem()[k].get_any()[0].getValue();
					            else if(cad.contains("Symbol"))
					          		ga.symbol=res[i].getItem()[k].get_any()[0].getValue();
					            else if(cad.contains("Gene type"))
					          		ga.type=res[i].getItem()[k].get_any()[0].getValue();
					            else if(cad.contains("OtherAliases"))
					          		{
					               	ga.aliases=new ArrayList<String>();
					               	String aliases=res[i].getItem()[k].get_any()[0].getValue();
					               	String [] al=aliases.split(",");
					               	for(int j=0;j<al.length;j++)
					               		ga.aliases.add(al[j].trim());
					          		}
					          	}
					          }
							ga.id=gene;
							System.out.println(ga.name+": "+ga.description);
							geneAnnotations.put(genes.get(g), ga);
							}
			 			}
			 		System.out.println("Annotations for "+gene+" in "+(System.currentTimeMillis()-t2)/1000.0+" secs.");
			 		t2=System.currentTimeMillis();
					
			 		//Try to retrieve GOTerms via QuickGO
			 		getTermsQuickGO(ga, true);
			 		System.out.println("GO terms for "+gene+" in "+(System.currentTimeMillis()-t2)/1000.0+" secs.");
			 		}
		 		galist.add(ga);
				}
			
			message="Finished: annotations retrieved in "+(System.currentTimeMillis()-t1)/1000+" secs.";
			System.out.println(message);
			progress=100;
			setProgress((int)progress);
			
			return galist;
			}
		
		
		@Override
		//public Void doInBackground()
		public ArrayList<GeneAnnotation> doInBackground()
			{
			if(searchByR)
				//getGeneAnnotationR();
				getMultipleGeneAnnotationsR();
			else
				getGeneAnnotationNCBI();
			done();
			return galist;
			}
		
		@Override
		public void done() {
			}
	
	}
	
	/**
	 * Searches for GO Terms by using QuickGO web service
	 * GOTerms retrieved are stored in the GeneAnnotation object passed as parameter
	 * @param ga GeneAnnotation for the gene to search for. If available, it will search for NCBI id,
	 * 			 if nothing is found it will search for symbol, if nothing again for name and if not
	 * 			 for each one of the aliases.
	 * 			NOTE: the QuickGO web service do not allow to select the database, so when the NCBI id
	 * 				search is done, we can get matches not coming from NCBI but from other databases.
	 * 				This is solved by checking each entry with the gene symbol.
	 * 				Anyway, it is a workaround i don't like
	 * @param unique It is usual to find the same GO term coming from different databases or evidences
	 * 					If unique is true, each GO term will be only stored once.
	 */	
	public List<GOTerm> getTermsQuickGO(GeneAnnotation ga, boolean unique)
		{
		ArrayList<GOTerm> gol=requestQuickGO(ga.ncbiId, unique);
		if(gol==null || gol.size()==0)	//try with symbol
			gol=requestQuickGO(ga.symbol, unique);
		if(gol==null || gol.size()==0)	//try with name
			gol=requestQuickGO(ga.name, unique);
		
		if(gol==null || gol.size()==0)
			{//try with aliases
			for(String alias : ga.aliases)
				{
				gol=requestQuickGO(alias, unique);
				if(gol.size()>0)	break;
				}
			}
		if(gol!=null && gol.size()>0)			ga.goTerms=gol;
		return gol;
		}
	
	/**
	 * Retrieves the terms from QuickGO.
	 * @param database Database from which the identifier comes (see http://www.ebi.ac.uk/QuickGO/FAQs.html#FAQ11 for available databases)
	 * 					We typically use it with EntrezGene ids.
	 * @param id Identifier of a protein or gene in a given database
	 */
	/*public List<GOTerm> getTermsQuickGO(String database, String id, boolean unique)
	{
	ArrayList<GOTerm> gol=requestQuickGO(database, id, unique);
	return gol;
	}*/
	
	//public static ArrayList<GOTerm> requestQuickGO(String database, String name, boolean unique)
	public static ArrayList<GOTerm> requestQuickGO(String name, boolean unique)
		{
		try{
		//URL u=new URL("http://www.ebi.ac.uk/ego/GAnnotation?protein="+name+"&format=tsv");
			//URL u=new URL("http://www.ebi.ac.uk/QuickGO/GAnnotation?protein="+name+"&format=tsv");
			//URL u=new URL("http://www.ebi.ac.uk/QuickGO/GAnnotation?db="+database+"&protein="+name+"&format=tsv");//QuicGO web service does not work if you specify db!
			URL u=new URL("http://www.ebi.ac.uk/QuickGO/GAnnotation?&protein="+name+"&format=tsv");//so just this way... risky if the NCBI id coincides with other id from other database!
			 // Connect
	        HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
	        // Get data
	        BufferedReader rd=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	        // Read data
	        List<String> columns=Arrays.asList(rd.readLine().split("\t"));
	        System.out.println(columns);
	        // Collect the unique terms as a sorted set
	        //ArrayList<GOTerm> terms=new ArrayList<GOTerm>();
	        HashMap<String, GOTerm> terms=new HashMap<String, GOTerm>();
	        
	        // Find which column contains GO IDs
	        int idIndex=columns.indexOf("GO ID");
	        int termIndex=columns.indexOf("GO Name");
	        int evIndex=columns.indexOf("Evidence");
	        int aspectIndex=columns.indexOf("Aspect");
		    // Read the annotations line by line
	        String line;
	        while ((line=rd.readLine())!=null) {
	            // Split them into fields
	            String[] fields=line.split("\t");
	            String aspect=fields[aspectIndex];
	            if(aspect.equals("Component"))	aspect="CC";
	            else if(aspect.equals("Process"))		aspect="BP";
	            else if(aspect.equals("Function"))		aspect="MF";
	            GOTerm go=new GOTerm(fields[termIndex],fields[idIndex],null, aspect, fields[evIndex], 1);
	            
	            // Record the GO ID
	            //if(!terms.contains(go))	terms.add(go);
	            //else	terms.get(terms.indexOf(go)).occurences++;
	            if(!unique)
	            	{
	            	if(!terms.containsKey(go.term))	terms.put(go.term,go);
	            	else							terms.get(go.term).occurences++;
	            	}
	            else if(!terms.containsKey(go.term))	terms.put(go.term,go);
 	        }
	        // close input when finished
	        rd.close();
	        System.out.println("Found "+terms.size()+" terms");
	        List<GOTerm> list=Arrays.asList(terms.values().toArray(new GOTerm[0]));
	          // Write out the unique terms
	        for (GOTerm term : list) {
		            System.out.println(term.term);
		    }
	        return new ArrayList<GOTerm>(list);
	        }catch(IOException e){e.printStackTrace();}
	    return null;
		}
	
	//public class LoadTask extends SwingWorker<Void, Void> 
	public class LoadTask extends SwingWorker<Integer, Void> 
	{
	public String path;
	public boolean invert;
	public int nd;
	public String message="";
	public int rowHeader, colHeader;
	
	@Override
	public Integer doInBackground() 
		{
		int progress=0;
		String name=path.replace("\\","/");
		message="Reading matrix "+name.substring(name.lastIndexOf("/")+1, path.indexOf("."));
		progress+=10;
		setProgress(progress);
		
		String description="";
		double t1=System.currentTimeMillis();
		double start=System.currentTimeMillis();
		DelimitedTextTableReader tr=new DelimitedTextTableReader("\t");
		System.out.println(path);
		tr.setHasHeader(true);
		Table levelsi=null;
		try{
		levelsi=tr.readTable(new FileInputStream(path));	//esto es lo que m�s peso tiene en tiempo (5s para 54000x9)
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,
					"Error reading the file: "+e.getMessage(), 
					"I/O Error", JOptionPane.ERROR_MESSAGE);
			message="Error reading the file: "+e.getMessage();
			setProgress(100);
			return 1;
		}
		if(invert)
			{
			numGenes=levelsi.getColumnCount();//Al rev�s te lo digo para que me entiendas
			numConditions=levelsi.getRowCount();
			}
		else
			{
			numConditions=levelsi.getColumnCount()-rowHeader;
			numGenes=levelsi.getRowCount();
			}
		message=numGenes+" genes\n"+numConditions+" conditions";
		progress+=60;
		setProgress(progress);
		
		conditionNames=new String[numConditions];
		geneNames=new String[numGenes];
		decimals=new int[numConditions];
		for(int i=0;i<numConditions;i++)	decimals[i]=nd;
		System.out.println("Microarray matrix with "+numGenes+" genes and "+numConditions+" conditions");
		
		try{
		//Leemos la primera fila, que tiene los nombres de los genes
		if(invert)
			{
			if(rowHeader>=1)
				{
				BufferedReader in =	new BufferedReader(new FileReader(path));
				StringTokenizer st=new StringTokenizer(in.readLine(),"\t");//El delimitador en Syntren es un tab.
				for(int i=0;i<numGenes;i++)	geneNames[i]=st.nextToken();
				}
			else	for(int i=0;i<numGenes;i++)	geneNames[i]=new Integer(i).toString();
			
			
			if(colHeader>=1)
				{
				}
			else	for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString();
			}
		else	//Si no est�n invertidas las primeras columnas tienen informaci�n sobre los genes
			{	//Y las primeras filas sobre las condiciones
			if(colHeader>=0)
				{
				BufferedReader in =	new BufferedReader(new FileReader(path));
				for(int i=0;i<rowHeader;i++)	in.readLine(); //Pasamos las filas con explicaciones sobre los experimentos
				for(int i=0;i<numGenes;i++)
					{
					String cad=in.readLine();
					if(cad.contains("\t\t"))
						{
						JOptionPane.showMessageDialog(null,
								"Empty sample name(s) found, remove additional tabs between fields.", 
								"Wrong format", JOptionPane.ERROR_MESSAGE);
						return 1;
						}
					StringTokenizer st=new StringTokenizer(cad,"\t");//El delimitador en Syntren es un tab.
					geneNames[i]=st.nextToken();
					}
				}
			else	for(int i=0;i<numGenes;i++)	geneNames[i]=new Integer(i).toString();
			
			
			if(rowHeader>=1)
				{
				BufferedReader in =	new BufferedReader(new FileReader(path));
				StringTokenizer st=new StringTokenizer(in.readLine(),"\t");//El delimitador en Syntren es un tab.
				for(int i=0;i<colHeader;i++)		
					{
					description=st.nextToken();//Pasamos los que no tienen q ver, que nombran las columnas de explicaci�n de genes
					}
				for(int i=0;i<numConditions;i++)	conditionNames[i]=st.nextToken();
				}
			else	for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString();
			}
		}catch(Exception e){System.err.println("Error reading file "+path); e.printStackTrace(); return 1;}
		double t2=System.currentTimeMillis();
		if(numGenes<=maxGenes)	maxGenes=1;
		
		
		//System.out.println("Descripci�n: "+description);
		if(description.contains("/"))
			{
			chip=description.substring(description.indexOf("/")+1);
			organism=description.substring(0, description.indexOf("/"));
			if(chip.length()<2)	
				{
				JOptionPane.showMessageDialog(null,
						"Chip name is wrong: "+chip, 
						"Wrong format", JOptionPane.ERROR_MESSAGE);
				return 1;
				}
			if(organism.length()<2)
				{
				JOptionPane.showMessageDialog(null,
						"Organism name is wrong: "+organism, 
						"Wrong format", JOptionPane.ERROR_MESSAGE);
				return 1;
				}
			}
		else
			{
			JOptionPane.showMessageDialog(null,
					"Chip/Organism description is wrong, use \"/\" as separator: "+description, 
					"Wrong format", JOptionPane.ERROR_MESSAGE);
			return 1;
			}

		message="Organism: "+organism+"\nPlatform: "+chip+"\nRetrieving gene annotations ...";
		progress+=10;
		setProgress(progress);
		
		geneAnnotations=new TreeMap<Integer, GeneAnnotation>();
		AnnotationStartThread p = new AnnotationStartThread();
		p.setPriority(Thread.MIN_PRIORITY);
		new Thread(p).start();

		//System.out.println("T1) Gene Names "+(t2-t1)/1000);
		
		if(invert)	levels=invert(levelsi);	//OJO: Para el caso de que est�n invertidos (SynTReN por ejemplo)
		else		levels=levelsi;
		t1=System.currentTimeMillis();
		//System.out.println("T2) inversi�n "+(t1-t2)/1000);
		
		expressions=convert(levels, colHeader);
		
		t2=System.currentTimeMillis();
		//System.out.println("T3) conversi�n "+(t2-t1)/1000);
		
		sparseGeneLabels=new Table();
		sparseGeneLabels.addColumn("name", String.class);
		sparseGeneLabels.addColumn("id", int.class);
		sparseGeneLabels.addColumn("actualId", int.class);
		sparseGeneLabels.addColumn("rowRank", int.class);//Orden en el que ser�n pintadas

		geneLabels=new Table();
		geneLabels.addColumn("name", String.class);
		geneLabels.addColumn("id", int.class);
		geneLabels.addColumn("rowRank", int.class);//Orden en el que ser�n pintadas
		int row=0;
		int sparseRow=0;
		int step=1;
		
		if(numGenes>maxGenes)	step=numGenes/maxGenes;
		for(int i=0;i<numGenes;i++)
			{
			row=geneLabels.addRow();
			geneLabels.setString(row, "name", geneNames[i]);
			geneLabels.setInt(row, "id", i);
			geneLabels.setInt(row, "rowRank", i);
			row++;
			if(i%step==0)
				{
				sparseRow=sparseGeneLabels.addRow();
				sparseGeneLabels.setString(sparseRow, "name", geneNames[i]);
				sparseGeneLabels.setInt(sparseRow, "id", sparseRow);
				sparseGeneLabels.setInt(sparseRow, "actualId", i);
				sparseGeneLabels.setInt(sparseRow, "rowRank", sparseRow);
				sparseRow++;
				}
			}

		numSparseGenes=sparseRow;
		
		conditionLabels=new Table();
		conditionLabels.addColumn("name", String.class);
		conditionLabels.addColumn("id", int.class);
		conditionLabels.addColumn("colRank", int.class);
		row=0;
		for(int i=0;i<numConditions;i++)
			{
			row=conditionLabels.addRow();
			conditionLabels.setString(row, "name", conditionNames[i]);
			conditionLabels.setInt(row, "id", i);
			conditionLabels.setInt(row, "colRank", i);
			}
		
		message="Computed sparse representation";
		progress+=10;
		setProgress(progress);
		
		//setupR();
		t1=System.currentTimeMillis();
		System.out.println("T4) creacion de tablas "+(t1-t2)/1000);
		message="Microarray data loaded in "+(t1-start)/1000+" seconds";
		progress=100;
		setProgress(progress);
		return 0;
		}
	
	@Override
	public void done() {
	    Toolkit.getDefaultToolkit().beep();
		}
	}


public void buildSparseGeneMatrix()
	{
	sparseGeneLabels=new Table();
	sparseGeneLabels.addColumn("name", String.class);
	sparseGeneLabels.addColumn("id", int.class);
	sparseGeneLabels.addColumn("actualId", int.class);
	sparseGeneLabels.addColumn("rowRank", int.class);//Orden en el que ser�n pintadas

	int sparseRow=0;
	int step=1;
	
	if(numGenes>maxGenes)	step=numGenes/maxGenes;
	for(int i=0;i<numGenes;i+=step)
		{
		sparseRow=sparseGeneLabels.addRow();
		sparseGeneLabels.setString(sparseRow, "name", geneNames[i]);
		sparseGeneLabels.setInt(sparseRow, "id", sparseRow);
		sparseGeneLabels.setInt(sparseRow, "actualId", i);
		sparseGeneLabels.setInt(sparseRow, "rowRank", sparseRow);
	//	if(sparseRow<20)	System.out.println(sparseRow+": "+geneNames[i]+"\t"+i+"\t");
		sparseRow++;
		}
	
	//Build sparse expression level matrix
	sparseExpressions=new Table();
	sparseExpressions.addColumn("gene", String.class);
	sparseExpressions.addColumn("condition", String.class);
	sparseExpressions.addColumn("level", double.class);
	sparseExpressions.addColumn("rowRank", int.class);
	sparseExpressions.addColumn("colRank", int.class);
	sparseExpressions.addColumn("rowId", int.class);
	sparseExpressions.addColumn("actualRowId", int.class);//Ids in the whole matrix ret
	sparseExpressions.addColumn("colId", int.class);
	
	int row=0;
	sparseRow=0;
	int contGene=0;
	step=1;
	if(numGenes>maxGenes)	step=numGenes/maxGenes;
	for(int i=0;i<numGenes;i+=step)
		{
		for(int j=0;j<numConditions;j++)
			{
			row++;
			sparseRow=sparseExpressions.addRow();
			sparseExpressions.setString(sparseRow, "gene", geneNames[i]);
			sparseExpressions.setString(sparseRow, "condition", conditionNames[j]);
			
			if(levels.canGet(conditionNames[j],Double.class))	sparseExpressions.setDouble(sparseRow, "level", ((Double)levels.get(i,j+1)).doubleValue());
			else												
				{
				if(levels.canGet(conditionNames[j],Integer.class))
					{
					sparseExpressions.setDouble(sparseRow, "level", matrix[i][j]); }
					}
			sparseExpressions.setInt(sparseRow, "rowId", contGene);
			sparseExpressions.setInt(sparseRow, "actualRowId", i);
			sparseExpressions.setInt(sparseRow, "rowRank", contGene);
			sparseExpressions.setInt(sparseRow, "colId", j);
			sparseExpressions.setInt(sparseRow, "colRank", j);
			sparseRow++;
			}
		if(i%step==0)	contGene++;
		}
	
	//System.out.println("\n\n");
	return;
	}

public void loadMicroarray(String path, boolean invert, int rowHeader, int colHeader, int nd) 
	{
	int progress=0;
	progress+=10;
	
	String description="";
	double t1=System.currentTimeMillis();
	DelimitedTextTableReader tr=new DelimitedTextTableReader("\t");
	System.out.println(path);
	tr.setHasHeader(true);
	Table levelsi=null;
	try{
	levelsi=tr.readTable(new FileInputStream(path));	//esto es lo que m�s peso tiene en tiempo (5s para 54000x9)
	}catch(Exception e){
		JOptionPane.showMessageDialog(null,
				"Error reading the file: "+e.getMessage(), 
				"I/O Error", JOptionPane.ERROR_MESSAGE);
		return;
	}
	if(invert)
		{
		numGenes=levelsi.getColumnCount();//Al rev�s te lo digo para que me entiendas
		numConditions=levelsi.getRowCount();
		}
	else
		{
		numConditions=levelsi.getColumnCount()-rowHeader;
		numGenes=levelsi.getRowCount();
		}
	
	conditionNames=new String[numConditions];
	geneNames=new String[numGenes];
	decimals=new int[numConditions];
	for(int i=0;i<numConditions;i++)	decimals[i]=nd;
	System.out.println("Microarray matrix with "+numGenes+" genes and "+numConditions+" conditions");
	
	try{
	//Leemos la primera fila, que tiene los nombres de los genes
	if(invert)
		{
		if(rowHeader>=1)
			{
			BufferedReader in =	new BufferedReader(new FileReader(path));
			StringTokenizer st=new StringTokenizer(in.readLine(),"\t");//El delimitador en Syntren es un tab.
			for(int i=0;i<numGenes;i++)	geneNames[i]=st.nextToken();
			}
		else	for(int i=0;i<numGenes;i++)	geneNames[i]=new Integer(i).toString();
		
		
		if(colHeader>=1)
			{
			}
		else	for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString();
		}
	else	//Si no est�n invertidas las primeras columnas tienen informaci�n sobre los genes
		{	//Y las primeras filas sobre las condiciones
		if(colHeader>=0)
			{
			BufferedReader in =	new BufferedReader(new FileReader(path));
			for(int i=0;i<rowHeader;i++)	in.readLine(); //Pasamos las filas con explicaciones sobre los experimentos
			for(int i=0;i<numGenes;i++)
				{
				String cad=in.readLine();
				if(cad.contains("\t\t"))
					{
					JOptionPane.showMessageDialog(null,
							"Empty sample name(s) found, remove additional tabs between fields.", 
							"Wrong format", JOptionPane.ERROR_MESSAGE);
					}
				StringTokenizer st=new StringTokenizer(cad,"\t");//El delimitador en Syntren es un tab.
				geneNames[i]=st.nextToken();
				}
			}
		else	for(int i=0;i<numGenes;i++)	geneNames[i]=new Integer(i).toString();
		
		
		if(rowHeader>=1)
			{
			BufferedReader in =	new BufferedReader(new FileReader(path));
			StringTokenizer st=new StringTokenizer(in.readLine(),"\t");//El delimitador en Syntren es un tab.
			for(int i=0;i<colHeader;i++)		
				{
				description=st.nextToken();//Pasamos los que no tienen q ver, que nombran las columnas de explicaci�n de genes
				}
			for(int i=0;i<numConditions;i++)	conditionNames[i]=st.nextToken();
			}
		else	for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString();
		}
	}catch(Exception e){System.err.println("Error reading file "+path); e.printStackTrace(); System.exit(1);}
	double t2=System.currentTimeMillis();
	if(numGenes<=maxGenes)	maxGenes=1;
	
	
	//System.out.println("Descripci�n: "+description);
	if(description.contains("/"))
		{
		chip=description.substring(description.indexOf("/")+1);
		organism=description.substring(0, description.indexOf("/"));
		if(chip.length()<2)	
			JOptionPane.showMessageDialog(null,
					"Chip name is wrong: "+chip, 
					"Wrong format", JOptionPane.ERROR_MESSAGE);
		if(organism.length()<2)	
			JOptionPane.showMessageDialog(null,
					"Organism name is wrong: "+organism, 
					"Wrong format", JOptionPane.ERROR_MESSAGE);
		}
	else
		{
		JOptionPane.showMessageDialog(null,
				"Chip/Organism description is wrong, use \"/\" as separator: "+description, 
				"Wrong format", JOptionPane.ERROR_MESSAGE);
		}

	
	geneAnnotations=new TreeMap<Integer, GeneAnnotation>();
	AnnotationStartThread p = new AnnotationStartThread();
	p.setPriority(Thread.MIN_PRIORITY);
	new Thread(p).start();

	if(invert)	levels=invert(levelsi);	//OJO: Para el caso de que est�n invertidos (SynTReN por ejemplo)
	else		levels=levelsi;
	t1=System.currentTimeMillis();
	
	expressions=convert(levels, colHeader);
	
	t2=System.currentTimeMillis();
	
	sparseGeneLabels=new Table();
	sparseGeneLabels.addColumn("name", String.class);
	sparseGeneLabels.addColumn("id", int.class);
	sparseGeneLabels.addColumn("actualId", int.class);
	sparseGeneLabels.addColumn("rowRank", int.class);//Orden en el que ser�n pintadas

	geneLabels=new Table();
	geneLabels.addColumn("name", String.class);
	geneLabels.addColumn("id", int.class);
	geneLabels.addColumn("rowRank", int.class);//Orden en el que ser�n pintadas
	int row=0;
	int sparseRow=0;
	int step=1;
	
	if(numGenes>maxGenes)	step=numGenes/maxGenes;
	for(int i=0;i<numGenes;i++)
		{
		row=geneLabels.addRow();
		geneLabels.setString(row, "name", geneNames[i]);
		geneLabels.setInt(row, "id", i);
		geneLabels.setInt(row, "rowRank", i);
		row++;
		if(i%step==0)
			{
			sparseRow=sparseGeneLabels.addRow();
			sparseGeneLabels.setString(sparseRow, "name", geneNames[i]);
			sparseGeneLabels.setInt(sparseRow, "id", sparseRow);
			sparseGeneLabels.setInt(sparseRow, "actualId", i);
			sparseGeneLabels.setInt(sparseRow, "rowRank", sparseRow);
			sparseRow++;
			}
		}

	numSparseGenes=sparseRow;
	
	conditionLabels=new Table();
	conditionLabels.addColumn("name", String.class);
	conditionLabels.addColumn("id", int.class);
	conditionLabels.addColumn("colRank", int.class);
	row=0;
	for(int i=0;i<numConditions;i++)
		{
		row=conditionLabels.addRow();
		conditionLabels.setString(row, "name", conditionNames[i]);
		conditionLabels.setInt(row, "id", i);
		conditionLabels.setInt(row, "colRank", i);
		}
	}
}