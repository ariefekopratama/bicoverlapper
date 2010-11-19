package es.usal.bicoverlapper.data;


import es.usal.bicoverlapper.kernel.Selection;
import es.usal.bicoverlapper.utils.AnnotationProgressMonitor;
import es.usal.bicoverlapper.utils.AnnotationProgressMonitor2;
import es.usal.bicoverlapper.utils.HypergeometricTestProgressMonitor;
import es.usal.bicoverlapper.utils.MicroarrayLoadProgressMonitor;
import es.usal.bicoverlapper.utils.RUtils;
import es.usal.bicoverlapper.utils.Sizeof;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
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
	 * TODO: remove, may lead to memory problems in large matrices
	 */
	public double matrix[][];
	Table geneLabels;
	Table conditionLabels;
	int maxGenes=199;//A partir de 200
	Table sparseExpressions;//As above, but sparse (only a maximum number of genes are shown)
	Table sparseGeneLabels;
	
	/**
	 * Names for the conditions.
	 */
	public String[] conditionNames;
	/**
	 * Gene names.
	 */
	public String[] geneNames;
	
	/**
	 * Labels for the conditions. They are built initially from conditionNames and geneNames, but the user can change them
	 * to any combination of values present for gene ids or for experimental factors. 
	 */
	public String[] columnLabels;
	/**
	 * Gene names.
	 */
	public String[] rowLabels;
	
	public ArrayList<String> experimentFactors;
	/**
	 * Every key is a experimental factor, every array contains all the values for that factor (e.g. key="Time", values="0 min, 0 min, 20 min, 40 min") 
	 */
	public HashMap<String,String[]> experimentFactorValues;
	
	//For any other annotation present in the data in the first place
	public ArrayList<String> geneFactors;
	public HashMap<String,String[]> geneFactorValues;
	
	
	/**
	 * Session class into which this microarray data is loaded
	 */
	//Session session=null;
	
	/**
	 * Type of gene names used. If GENENAME, annotations are searched with Entrez Gene and QuickGO
	 */
	public String rname="GENENAME";
	public String rdescription="DESCRIPTION";
	public String rgo="GO";
	/**
	 * Usually the R resource to search for gene annotations is an environment like for example illuminaHumanv1.db,
	 * but in some cases it is a database that cannot be accessed with get, mget; but with queries, and there is no
	 * environment counterpart. On these cases, in example Illumina Human HT12 for which there is no environment db
	 * but there is lumiHumanIDMapping package, isRDatabase is set to true and the search is done by dbGetQuery in R.
	 */
	public boolean isRDatabase=false;
	/**
	 * As isRDatabase, but with bioMaRt package, where getGene will be used for annotations
	 */
	public boolean isBioMaRt=false;
	
	/**
	 * As isRDatabase, but with GO.db package, where annotations of GO terms are in GOTERM
	 */
	public boolean isGO=false;
	
	public Rengine re=null;
	
	int numGenes;
	int numSparseGenes;//for sparse matrices
	int numConditions;
	private int[] decimals;
	
	/**
	 * Average expression value of the whole expression matrix
	 */
	public double average=0;
	
	/**
	 * Maximum expression value of the whole expression matrix
	 */
	public double max;
	/**
	 * Minimum expression value of the whole expression matrix
	 */
	public double min;
	
	public String chip;//kind of microarray chip (any official for Affymetrix permitted, by now), or kind of name taken by genes (geneID and ORF permitted)
	public String organism;//Name of organism as registered in NCBI
	public Map<Integer, GeneAnnotation> geneAnnotations;//Gene Annotations from NCBI and GeneOntology
	public Map<String, GOTerm> GOTerms;//GO terms stored. The key is the go id
	public boolean searchByR=false; //if there is information in the file about an available R package, gene annotations are taken from there, otherwise they're searched in NCBI
	boolean annotationsRetrieved=false;
	
	public AnnotationTask annotTask;
	private AnnotationProgressMonitor annotProgressMon;
	
	//GeneAnnotation retrieval
	private GeneRequester geneRequester;
	private HypergeometricTestTask ht;
	private LoadTask loadTask;
	private MicroarrayRequester microarrayRequester;
	public int[] columnOrder;
	public double[] averageCols;
	public double[] minCols;
	public double[] maxCols;
	public double[] sdCols;
	private AnnotationTask at;
	private AnnotationProgressMonitor2 amd2;
	
	/**
	 * Constructor from a file
	 * @param path Path to the file with microarray information
	 * @param invert true if genes are columns (genes as rows are considered as the usual option)
	 * @param rowHeader Number of initial rows with row information (usually one)
	 * @param colHeader Number of initial columns with column information (usually one)
	 * @param nd	Number of decimals to be shown if numerically showing expression levels
	 */
	public MicroarrayData(String path, boolean invert, int rowHeader, int colHeader, int nd, MicroarrayRequester mr, Rengine r) throws Exception
		{
		MicroarrayLoadProgressMonitor pmd=new MicroarrayLoadProgressMonitor();
		loadTask=new LoadTask();
		loadTask.path=path;
		loadTask.invert=invert;
		loadTask.rowHeader=rowHeader;
		loadTask.colHeader=colHeader;
		loadTask.nd=nd;
		experimentFactors=new ArrayList<String>();
		experimentFactorValues=new HashMap<String,String[]>();
		this.re=r;
		
		at=new AnnotationTask();
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
	 * Given an experimental factor (e.g. "Time") it returns its values for the conditions (e.g. "0 min", "0 min", "40 min", "40 min")
	 * @param experimentFactor
	 * @return
	 */
	public String[] getExperimentFactorValues(String experimentFactor)
		{
		return this.experimentFactorValues.get(experimentFactor);
		}
	/**
	 * Sets the table geneLabels and conditionLabels to fit with rowLabels and columnLabels values
	 */
	public void changeLabels()
		{
		for(int i=0;i<rowLabels.length;i++)
			{
			geneLabels.setString(i, "name", rowLabels[i]);
			}
		for(int i=0;i<columnLabels.length;i++)
			{
			conditionLabels.setString(i, "name", columnLabels[i]);
			}
		}
	
	
	/**
	 * Corverts the expression matrix mat to a new matrix with information that Prefuse graphs can manage
	 * @param mat
	 * @param skipColumns number of columns to skip, as they are only informative
	 * @return
	 */
	private Table convert(Table mat, int skipRows, int skipColumns)
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
		averageCols=new double[numConditions];
		minCols=new double[numConditions];
		maxCols=new double[numConditions];
		sdCols = new double[numConditions];
		for(int i=0;i<numConditions;i++)	
			{
			averageCols[i]=sdCols[i]=0;
		    minCols[i]=maxCols[i]=new Double(mat.getString(0+skipRows , i+skipColumns)).doubleValue();
			}
		
		max=min=new Double(mat.getString(0+skipRows , 0+skipColumns)).doubleValue();
		
		for(int i=0;i<numGenes;i++)
			{
			for(int j=0;j<numConditions;j++)
				{
				row=ret.addRow();
				ret.setString(row, "gene", geneNames[i].trim());
				ret.setString(row, "condition", conditionNames[j].trim());
				matrix[i][j]=new Double(mat.getString(i+skipRows , j+skipColumns)).doubleValue();
				ret.setDouble(row, "level", matrix[i][j]);
				average+=matrix[i][j];
				averageCols[j]+=matrix[i][j];
				if(max<matrix[i][j])	max=matrix[i][j];
				if(min>matrix[i][j])	min=matrix[i][j];
				if(maxCols[j]<matrix[i][j])
					maxCols[j]=matrix[i][j];
				if(minCols[j]>matrix[i][j])	
					minCols[j]=matrix[i][j];
				
			
				ret.setInt(row, "rowId", i);
				ret.setInt(row, "rowRank", i);
				ret.setInt(row, "colId", j);
				ret.setInt(row, "colRank", j);
				row++;
				if(i%step==0)	//replicate for sparse matrix
					{
					sparseRow=sparseExpressions.addRow();
					sparseExpressions.setString(sparseRow, "gene", geneNames[i].trim());
					sparseExpressions.setString(sparseRow, "condition", conditionNames[j].trim());
					
					sparseExpressions.setDouble(sparseRow, "level", matrix[i][j]);
			
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
		
		for(int i=0;i<numConditions;i++)	averageCols[i]/=numGenes;
		average/=numGenes*numConditions;
		computeSd();
		return ret;
		}
	
	/**
	 * Computes the standard deviation of each column. Mean should be previously computed (right now it's done in convert())
	 */
	private void computeSd()
		{
		long t=System.currentTimeMillis();
		for(int i = 0; i < numConditions; i++)
			{
			for(int j = 0; j < numGenes; j++)
				sdCols[i]+=Math.abs(averageCols[i]-matrix[j][i]);
			sdCols[i]/=numGenes;
			}
		System.out.println("Time to compute sd "+(System.currentTimeMillis()-t)/1000.0);
		}
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
				sparseGeneLabels.setString(j, "name", rowLabels[genes.get(i)]);//Nuevo nombre e id para �l, el resto no cambia
				sparseGeneLabels.setInt(j, "actualId", genes.get(i));
				int id=sparseGeneLabels.getInt(j, "id");
				int actualId=genes.get(i);
				int cont=j*numConditions;
				for(int k=0;k<this.numConditions;k++)
					{
					this.sparseExpressions.setInt(cont+k, "actualRowId",actualId);
					this.sparseExpressions.setInt(cont+k, "rowId",id);
					this.sparseExpressions.setString(cont+k, "gene",rowLabels[actualId]);
					this.sparseExpressions.setDouble(cont+k, "level",matrix[actualId][k]);
					}
				}
			}
		
		//sortColumns();//refresh the rowRanks to fit with the columnOrder established
		return;
		}

	/**
	 * Builds a small form for the sample with id i
	 * @param i
	 * @return
	 */
	public String getDetailedSampleForm(int i)
		{
		String form="";
		form=form.concat("SampleId: "+conditionNames[i]+"\n");
		for(String s:experimentFactors)
			{
			form=form.concat(s+": "+experimentFactorValues.get(s)[i]+"\n");
			}
		form=form.concat("\n");
		return form;
		//TODO: Properly finish the from for samples
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
		if(i>=0 && i<this.numGenes && j>=0 && j<this.numConditions)
			return matrix[i][j];
		else	return this.average;//TODO: this should be NA
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
	 *	Returns the name of the condition at the specified position 
	 * @param pos	position of the condition in the matrix
	 * @return	name of the condition
	 */
	public String getColumnLabel(int pos)
		{
		return columnLabels[pos];
		}

	/**
	 *	Returns the name of the condition at the specified position 
	 * @param pos	position of the condition in the matrix
	 * @return	name of the condition
	 */
	public String getRowLabel(int pos)
		{
		if(pos>=0 && pos<rowLabels.length)	return rowLabels[pos];
		else								return "NA";
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
	
	/**
	 * Returns the euclidean distance between two points
	 * @param p1 first point
	 * @param p2 second point
	 * @return euclidean distance
	 * TODO: move to utils
	 */
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
	
	
	public String getGeneName(int lg)
	{
	return geneNames[lg];
	}
	
	/**
	 * Returns a list of gene names from a list with genes ids
	 * @param lg	list of gene ids
	 * @return	An ArrayList with Strings with corresponding gene and condition names
	 */
	public ArrayList<String> getRowLabels(LinkedList<Integer> lg)
		{
		ArrayList<String> ret=new ArrayList<String>();
		for(int i=0;i<lg.size();i++)		ret.add(rowLabels[lg.get(i)]);
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
	 * Returns a list of condition names a lists and conditions ids
	 * @param lc	list of condition ids 
	 * @return	An ArrayList with Strings with corresponding gene and condition names
	 */
	public ArrayList<String> getColumnLabels(LinkedList<Integer> lc)
		{
		ArrayList<String> ret=new ArrayList<String>();
		if(columnLabels!=null)	
			for(int i=0;i<lc.size();i++)		ret.add(columnLabels[lc.get(i)]);
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
	 * Searches for genes or conditions that contain the string "what" in the field where.
	 * If found, they are selected and the visualizations updated
	 * @param what
	 * @param where
	 * @param exact if exact is true, it searches for exact matches of the string what (only for gene names search)
	 */
	public Selection search(String what, int where, boolean exact)
		{
		LinkedList<Integer> genes=new LinkedList<Integer>();
		LinkedList<Integer> conditions=new LinkedList<Integer>();
		long t=System.currentTimeMillis();
		System.out.println("Searching for "+what+" in "+where);
		switch(where)
			{
			case 0://anywhere
				genes.addAll(searchAnnotations(what, exact));
				conditions.addAll(searchConditions(what));
				break;
			case 1://gene names
				genes.addAll(searchGenes(what, exact));
				break;
			case 2://condition names
				conditions.addAll(searchConditions(what));
				break;
			case 3://gene annotations (descriptions/go terms, etc.)
				searchAnnotations(what, null);
				break;
			}
		System.out.println("found "+genes.size()+" genes and "+conditions.size()+" conditions");
		System.out.println("Time for the search: "+(System.currentTimeMillis()-t)/1000.0);
		return new Selection(genes, conditions);
		}
	
	public LinkedList<Integer> searchAnnotations(String what, LinkedList<Integer> alreadyIn)
		{
		LinkedList<Integer> genes=new LinkedList<Integer>();
		Iterator<GeneAnnotation> it=geneAnnotations.values().iterator();
		while(it.hasNext())
			{
			GeneAnnotation ga=it.next();
			if(!alreadyIn.contains(getGeneId(ga.id)))
				{
				if((ga.id!=null && ga.id.contains(what)) || (ga.description!=null && ga.description.contains(what)) || 
						(ga.aliases!=null && ga.aliases.contains(what)) || (ga.symbol!=null && ga.symbol.contains(what)) || 
						(ga.organism!=null && ga.organism.contains(what)) || (ga.locus!=null && ga.locus.contains(what)) ||
						(ga.entrezId!=null && ga.entrezId.contains(what)) || (ga.name!=null && ga.name.contains(what)) )
					genes.add(getGeneId(ga.id));
				else if(ga.goTerms!=null)
					{
					for(GOTerm gt : ga.goTerms)
						{
						if((gt.definition!=null && gt.definition.contains(what)) || 
								(gt.term!=null && gt.term.contains(what)))
							{
							genes.add(getGeneId(ga.id));
							break;
							}
						}
					}
				}
			}
		return genes;
		}
	
	public LinkedList<Integer> searchAnnotations(String what, boolean exact)
		{
		LinkedList<Integer> genes=new LinkedList<Integer>();
		Iterator<GeneAnnotation> it=geneAnnotations.values().iterator();
		while(it.hasNext())
			{
			GeneAnnotation ga=it.next();
			if(!exact)
				{
				if((ga.id!=null && ga.id.contains(what)) || (ga.description!=null && ga.description.contains(what)) || 
						(ga.aliases!=null && ga.aliases.contains(what)) || (ga.symbol!=null && ga.symbol.contains(what)) || 
						(ga.organism!=null && ga.organism.contains(what)) || (ga.locus!=null && ga.locus.contains(what)) ||
						(ga.entrezId!=null && ga.entrezId.contains(what)) || (ga.name!=null && ga.name.contains(what)) )
					genes.add(getGeneId(ga.id));
				else if(ga.goTerms!=null)
					{
					for(GOTerm gt : ga.goTerms)
						{
						if((gt.definition!=null && gt.definition.contains(what)) || 
								(gt.term!=null && gt.term.contains(what)))
							{
							genes.add(getGeneId(ga.id));
							break;
							}
						}
					}
				}
			else
				{
				if((ga.id!=null && ga.id.equals(what)) || (ga.description!=null && ga.description.equals(what)) || 
						(ga.aliases!=null && ga.aliases.equals(what)) || (ga.symbol!=null && ga.symbol.equals(what)) || 
						(ga.organism!=null && ga.organism.equals(what)) || (ga.locus!=null && ga.locus.equals(what)) ||
						(ga.entrezId!=null && ga.entrezId.equals(what)) || (ga.name!=null && ga.name.equals(what)) )
					genes.add(getGeneId(ga.id));
				else if(ga.goTerms!=null)
					{
					for(GOTerm gt : ga.goTerms)
						{
						if((gt.definition!=null && gt.definition.equals(what)) || 
								(gt.term!=null && gt.term.equals(what)))
							{
							genes.add(getGeneId(ga.id));
							break;
							}
						}
					}
				}
			}
		return genes;
		}
	
	public LinkedList<Integer> searchGenes(String what, boolean exact)
		{
		LinkedList<Integer> genes=new LinkedList<Integer>();
		for(String s:geneNames)
			{
			if(!exact)
				{
				if(s.contains(what))
					{
					genes.add(getGeneId(s));
					//System.out.println("Adding gene "+s+"\t"+getGeneId(s)+"\t"+this.getGeneNames()[getGeneId(s)]);
					}
				}
			else
				{
				if(s.trim().equals(what.trim()))
					{
					genes.add(getGeneId(s));
					//System.out.println("Adding gene "+s+"\t"+getGeneId(s)+"\t"+this.getGeneNames()[getGeneId(s)]);
					}
				}
					
			}
		return genes;
		}
	
	public GeneAnnotation searchGene(String geneName)
		{
		GeneAnnotation ga=null;
		for(String s:geneNames)
			{
			if(s.trim().equals(geneName.trim()))
					{
					ga=geneAnnotations.get(getGeneId(s));
					//System.out.println("Adding gene "+s+"\t"+getGeneId(s)+"\t"+this.getGeneNames()[getGeneId(s)]);
					}
			}
		return ga;
		}
	
	public LinkedList<Integer> searchConditions(String what)
		{
		LinkedList<Integer> conditions=new LinkedList<Integer>();
		for(String s:conditionNames)
			{
			if(s.contains(what))
				conditions.add(getGeneId(s));
			}
		
		for(int i=0;i<numConditions;i++)
			for(String cad:this.experimentFactors)
				{
				if(experimentFactorValues.get(cad)[i].contains(what))
					conditions.add(i);
				}
			
		return conditions;
		}
/**
	 * Returns a string with the requested value as a text string, for a determinate dimension
	 * @param value	value to format to text
	 * @param dim	dimension to which the value pertains (it determines the number of decimals for that dimension)
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
					if(pos+1+nc<=cad.length())	
						{
						double number;
						//double number = (double)(int)((value+0.005)*100.0)/100.0;
						if(nc>0) number = (double)(int)((value+Math.signum(value)*((0.1*(nc))/2))*(10.0*(nc)))/(10.0*(nc));
						else	 number = Math.rint(value);
						cad=new Double(number).toString();
						}
					else						;//En este caso ya esta, no tenemos que quitar decimales
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
	        	REXP exp=null;
	    	    
	    	   if(chip.contains("yeast") || chip.contains("YEAST") || chip.contains("ygs98") || chip.contains("pombe") )
	    	       	{
	    	    	if(chip.equals("YEAST"))
	    	    		rgo="GOTERM";
	    	    	}
	    	   else if(chip.startsWith("org."))
		    	   	{
	    	    	rname="GENENAME";
	    	    	rdescription="DESCRIPTION";
	    	       	}
	    	   //There are some cases with no dedicated environment R annotation package. On these cases (i.e. Illumina Human HT12), there are either 
	    	   //database packages (lumiHumanIDMapping) or entries in biomaRt (illumina_humanht_12) package
	    	   //In the case of illumina databases, it must be specified as follows: lumiHumanIDMapping.HumanHT12_V3_0_R2_11283641_A
	    	   else if(chip.startsWith("lumiHumanIDMapping"))
		    	   	{
		    		rname="Symbol";
		    		rdescription="none";
		    		isRDatabase=true;
		    	   	}
	    	   //Similar to the above situation, but in this case we use bioMaRt instead of a dedicated environment database.
	    	   else if(chip.startsWith("biomaRt"))
	    	   		{
	    		    chip=chip.substring(chip.indexOf(".")+1);
		    	    if(organism.equals("Homo sapiens"))	rname="hgnc_symbol";
		    	    else if(organism.equals("Schizosaccharomyces pombe")) rname="external_gene_id";
	    		    else								rname="symbol";
	    		    rdescription="description";
	    		    isBioMaRt=true;
	    		 	}
	    	   else if(chip.startsWith("KEGG"))
	    	   		{
	    		    rname="PATHID2NAME";	
	    	    	rdescription=null;//no description in KEGG.db
	    	    	}
	    	   else if(chip.equals("GO.db"))
	    	   		{
	    		    rname="Term";
	    		    rdescription="Definition";
	    		    isGO=true;
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
	    						AnnotationTask at=new AnnotationTask(new int[]{getGeneId(geneNames[0])});
	    		    	    	at.execute();
	    						
	    						}catch(Exception e){e.printStackTrace();}
	    				}
	    			};
	    			wt.start();
	    	    	}
	    	    else
		    	    {
	    	    	//GO specific
		    	    exp=re.eval("library(GO.db)");
		    	    if(exp==null)	installPackage(exp, "GO.db");
		    	    exp=re.eval("library(GOstats)");
		    	    if(exp==null)	installPackage(exp, "GOstats");
		    	    exp=re.eval("library(annotate)");
		    	    if(exp==null)	installPackage(exp, "annotate");
		    	    
		    	    exp=re.eval("source(\"es/usal/bicoverlapper/source/codeR/GOgroups.R\")");
		    	    exp=re.eval("source(\"es/usal/bicoverlapper/source/codeR/geneAnnotation.R\")");
		    	     
	    	    	long t=System.currentTimeMillis();
	    	    	if(!isBioMaRt) //in this case, the chip contains the name of the R annotation package
	    	    		{
			    	    exp=re.eval("library("+chip+")");
			    	    System.out.println("It takes "+(System.currentTimeMillis()-t)+" to load the library");
			    	    if(exp==null)	System.out.println("library "+chip+" returns null in R");
			    	    else			System.out.println("library "+chip+" loaded correctly");
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
	    	    		}
	    	    	else	
	    	    		{
	    	    		searchByR=true;
			    	    }
			        
		    	    chip=chip.replace(".db", "");
		    	    
		    	    getGeneAnnotationsLite();
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
		long t1=System.currentTimeMillis();
		exp=re.eval("chipEntrezUniverse <- unique(unlist(mget("+universe+", "+chip+"ENTREZID, ifnotfound=NA)))");
		exp=re.eval("chipEntrezUniverse <- chipEntrezUniverse[which(!is.na(chipEntrezUniverse))]");
		exp=re.eval("selectedEntrezIds <- unlist(mget("+n+","+chip+"ENTREZID, ifnotfound=NA))");
		exp=re.eval("selectedEntrezIds <- selectedEntrezIds[which(!is.na(selectedEntrezIds))]");
			
		System.out.println("Time preparing Ids: "+(System.currentTimeMillis()-t1)/1000.0);
		t1=System.currentTimeMillis();
		
	    exp=re.eval("params <- new(\"GOHyperGParams\", geneIds = selectedEntrezIds," +
				"universeGeneIds = chipEntrezUniverse, annotation = \""+chip+"\"," +
				"ontology = \"BP\", pvalueCutoff = "+cutoff+", conditional = FALSE," +
				"testDirection = \"over\")");
		exp=re.eval("hgOver <- hyperGTest(params)");
		System.out.println("Time hypergeometric test: "+(System.currentTimeMillis()-t1)/1000.0);
		t1=System.currentTimeMillis();
		
		exp=re.eval("df=summary(hgOver, pvalue=0.1)");
	    exp=re.eval("df$Term");
    	String[] t=exp.asStringArray();
    	exp=re.eval("df$GOBPID");
    	String[] ids=exp.asStringArray();
    	exp=re.eval("df$Count");
    	int[] evs=exp.asIntArray();
    	exp=re.eval("df$Pvalue");
    	double[] pval=exp.asDoubleArray();
    	
    	System.out.println("Time preparing summary: "+(System.currentTimeMillis()-t1)/1000.0);
		
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

	public GOTerm getGOTermByID(String goid)
		{
		REXP exp=null;
	   // long t1=System.currentTimeMillis();
	    //String n="c(\"";
	    exp=re.eval("got=getGOTerm(\""+goid+"\")");
	    if(exp==null)	System.err.println("Error getting GO terms by ID with R");
		
	   // System.out.println("La parte de R tarda "+(System.currentTimeMillis()-t1));
	  //  t1=System.currentTimeMillis();
	    
	    exp=re.eval("got@terms");
		if(exp==null)	return null;
		String t=exp.asString();
		exp=re.eval("got@definitions");
		String d=exp.asString();
		exp=re.eval("got@ontologies");
		String o=exp.asString();
		exp=re.eval("got@ids");
		String ids=exp.asString();
		exp=re.eval("got@evidences");
		int evs=1;
		
	//	ArrayList<GOTerm> got=new ArrayList<GOTerm>();
		if(ids !=null && o!=null && d!=null && t!=null) 
			{
			//System.out.println("El resto tarda "+(System.currentTimeMillis()-t1));
		    return new GOTerm(t, ids, d, o, "", evs);
	    	}
		//System.out.println("El resto tarda "+(System.currentTimeMillis()-t1));
		return null;

		}
	
	public ArrayList<GOTerm> getGOTermsRbyID(String[] goids)
	{
	REXP exp=null;
   // long t1=System.currentTimeMillis();
    ArrayList<GOTerm> got=new ArrayList<GOTerm>();
    int toRetrieve=0;
    
	String n="c(\"";
    for(int i=0;i<goids.length;i++)
    	{
    	GOTerm gt=GOTerms.get(goids[i]);
    	if(gt!=null)	got.add(gt);
    	else		
    		{
    		toRetrieve++;
    		n=n.concat(goids[i]+"\", \"");
    		}
    	}
    n=n.substring(0,n.length()-3)+")";
    exp=re.eval("got=getGOTermsByGOID("+n+")");//<-tarda entre 30 y 100ms, lo cual puede hacer m�s de un minuto para arrays de 10000 genes
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
		evs=new int[toRetrieve];
		for(int i=0;i<evs.length;i++)	evs[i]=1;
		}
	
	if(evs!=null && ids !=null && o!=null && d!=null && t!=null) 
	//if(evs!=null && ids !=null && o!=null && t!=null) 
			{
    		for(int i=0;i<evs.length;i++)
    			{
    			GOTerm gt=new GOTerm(t[i], ids[i], d[i], o[i], "", evs[i]);
    			this.GOTerms.put(ids[i], gt);
    			got.add(gt);
    			//	got.add(new GOTerm(t[i], ids[i], "", o[i], "", evs[i]));
    			}
    //	System.out.println("El resto tarda "+(System.currentTimeMillis()-t1));
    	return got;
		}
	//System.out.println("El resto tarda "+(System.currentTimeMillis()-t1));
	return null;
	}

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

	/** 
	 * Gets gene annotations for basic info, except thinks like GO terms that can take longer
	 */
	public void getGeneAnnotationsLite()
		{
		int[] listgenes=new int[numGenes];
		for(int i=0;i<numGenes;i++)		
			listgenes[i]=i;

		getGeneAnnotations(listgenes, null, false, null, null, false);
		}
	
	public void getGeneAnnotations(int[] genes, GeneRequester gr, boolean showProgress, JLabel label, Point location, boolean searchGO) 
		{
		at=new AnnotationTask(genes);
		at.searchGO=searchGO;
		if(showProgress)
			{
			if(location!=null)	
				{
				annotProgressMon=new AnnotationProgressMonitor(location);
				annotProgressMon.setTask(at);
				annotProgressMon.run();
				}
			else
				{
				amd2=new AnnotationProgressMonitor2(label);
				amd2.setTask(at);
				amd2.run();
				}
			}
		
		if(gr!=null)
			{
			geneRequester=gr;
			Thread wt=new Thread() {
				public void run() {
					try{geneRequester.receiveGeneAnnotations(at.get());}catch(Exception e){e.printStackTrace();}
				}
			};
			wt.start();
			}
		else
			{
			at.execute();
			}
		}
	
	public class HypergeometricTestTask extends SwingWorker<ArrayList<GOTerm>, Void>// implements Runnable
		{
		public LinkedList<Integer> genes;
		public ArrayList<GOTerm> golist=null;
		public String message="";
		public String errorMessage="";
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
			try{
				golist=getGOTermsHypergeometric(genes, ontology, 0.001);
				progress=100;
				setProgress(progress);
				done();
			}catch(Exception e)
				{
				progress=100;
				setProgress(progress);
				done();
				JOptionPane.showMessageDialog(null,
						"Error performing the hypergeometric test: "+e.getMessage()+"\n"+errorMessage, 
						"Analysis Error", JOptionPane.ERROR_MESSAGE);
				}
			
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
			long t1=System.currentTimeMillis();
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
			
		    System.out.println("Time in building the universe "+(System.currentTimeMillis()-t1)/1000.0);
			t1=System.currentTimeMillis();
			
			REXP exp=null;
			
			//In the case of BioMaRt, we must select the corresponding species package (org.*)
			String annotationPackage=chip;
			if(isBioMaRt)	annotationPackage=getAnnotationPackage();
			
			//Prepare universe
			exp=re.eval("universe");
			if(exp==null)
				{
				if(!chip.startsWith("org."))
					{
					if(isBioMaRt) //ensembl ids -> the work is done via the corresponding species annotation package (org.*)
						{
						String env=annotationPackage.replace(".db", "ENSEMBL");
						exp=re.eval("universe <- mappedkeys("+env+")");
						exp=re.eval("ref=unlist(mget(universe, "+env+", ifnotfound=NA))");
						}
					else  // platform packages have as ids the probe ids
						{
						exp=re.eval("universe <- unique(unlist(mget("+universe+", "+chip+"ENTREZID, ifnotfound=NA)))");
						if(exp==null)
							errorMessage=chip+" database does not have ENTREZIDs, hypergeometric test cannot be run";
						}
					}
				else	//species (org.) packages has as base ids special ids
					{
					System.out.println("org database!");
					exp=re.eval("universe <- "+universe);
					}
		    	}
			
			//Prepare selected Ids
			if(isBioMaRt)
				{
				exp=re.eval("selected=names(ref)[which(ref %in% "+n+")]");
				}
			else
				{
				if(!chip.startsWith("org."))//EntrezIDs on no org.* annotation packages
					{
					exp=re.eval("selected <- unlist(mget("+n+","+chip+"ENTREZID, ifnotfound=NA))");
					}
				else//species ids for org. packages
					{
					exp=re.eval("selected <- "+n);
					}
				}
			
			exp=re.eval("params <- new(\"GOHyperGParams\", geneIds = selected," +
					"universeGeneIds = universe, annotation = \""+annotationPackage+"\"," +
					"ontology = \""+ontology+"\", pvalueCutoff = "+cutoff+", conditional = FALSE," +
					"testDirection = \"over\")");
		    
		    System.out.println("Time in mapping to ids "+(System.currentTimeMillis()-t1)/1000.0);
			t1=System.currentTimeMillis();
					    
			exp=re.eval("hgOver <- hyperGTest(params)");
			
			System.out.println("Time in hypergeometric test "+(System.currentTimeMillis()-t1)/1000.0);
			t1=System.currentTimeMillis();
				
			exp=re.eval("df=summary(hgOver, pvalue="+cutoff+")");
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
	    		
	    		System.out.println("Time in preparing the summary "+(System.currentTimeMillis()-t1)/1000.0);
				t1=System.currentTimeMillis();
				
	    		return got;
	    		}
	    	return null;
			}
		
		@Override
		public void done() {
			}
		}
	
	/**
	 * Returns the corresponding annotation package for the organism of the loaded microarray
	 * @return
	 */
	public String getAnnotationPackage()
		{
		String cad=null;
		StringTokenizer st=new StringTokenizer(organism);
		int numTokens=st.countTokens();
		String abbv=""+st.nextToken().toUpperCase().charAt(0)+st.nextToken().toLowerCase().charAt(0);
		if(numTokens==2)
			{
			//it works for everything except E. coli and Malaria
			cad="org."+abbv+".eg.db";
			
			if(abbv.equals("Pf"))//this makes malaria
				cad="org."+abbv+".plasmo.db";
			if(cad.contains("Ec"))//this makes E. coli (default K12)
				cad="org."+abbv+"K12.eg.db";
			}
		else
			{
			//this makes E. coli distinction
			if(organism.contains("K12"))
				cad="org."+abbv+"K12.eg.db";
			else if(organism.contains("Sakai"))
				cad="org."+abbv+"Sakai.eg.db";
			}
		return cad; 
		}
	
	/**
	 * Returns a list of Entrez IDs for every gene in the sample (if available).
	 * NOTE: The returning list is unsorted
	 * @return
	 */
	public String[] getEntrezIds()
		{
		Iterator<GeneAnnotation> it=geneAnnotations.values().iterator();
		ArrayList<String> ret=new ArrayList<String>();
		while(it.hasNext())
			{
			GeneAnnotation ga=it.next();
			if(ga.entrezId!=null)	ret.add(ga.entrezId);
			}
		return ret.toArray(new String[ret.size()]);
		}
	
	//**********************************************************************
	public class AnnotationTask extends SwingWorker<ArrayList<GeneAnnotation>, Void>// implements Runnable
	{
		public String gene;
		public String message;
		public int id;
		//public LinkedList<Integer> genes;
		public int[] genes;
		public GeneAnnotation ga;
		public AnnotationProgressMonitor apm;
		public ArrayList<GeneAnnotation> galist=null;
		public boolean searchGO;
		
		public AnnotationTask()
			{
			genes=null;
			galist=new ArrayList<GeneAnnotation>();
			searchGO=true;
			}
		
		public AnnotationTask(int id)
			{
			this.id=id;
			searchGO=true;
			}
		
		public AnnotationTask(int[] genes)
			{
			this.genes=genes;
			galist=new ArrayList<GeneAnnotation>();
			searchGO=true;
			}
		
		
		
		/**
		 * Annotations retrieved with method mget() from R
		 * It take about 5s to retrieve the annotations for 800 genes
		 * @return
		 */
		public ArrayList<GeneAnnotation> getMultipleGeneAnnotationsR()
		{
		System.out.println("---getMultipleGeneAnnotationsR---");
			
		galist=new ArrayList<GeneAnnotation>();
		int progress=0;
		long t0=System.currentTimeMillis();
		
		message="searching for selected genes in "+organism+" ...";
		System.out.println(message);
 		setProgress(progress);
 		
 		//0) Build the group of genes to search for
 		String group="";
 		
 		for(int i=0; i<genes.length; i++) //this takes a lot if we have a lot of genes
 			group=group+"\""+geneNames[genes[i]]+"\",";
		group=group.substring(0, group.length()-1);
		REXP exp=re.eval("group=c("+group+")");
		
		String[] names=null, descriptions=null, entrezs=null, ensembls=null;
    	
		message="searching for gene names...";
		System.out.println(message);
 		progress+=5;
 		setProgress(progress);
 		//1) Search for gene names
 		if(!searchGO)
	 		{
	 		if(!isBioMaRt)
	 			{
	 			if(isGO)
	 				{
	 				exp=re.eval("golist=unlist(mget(group,GOTERM, ifnotfound=NA))");
	 				exp=re.eval("sapply(golist, function(x){x@Term})");
			    	if(exp!=null)	
			    		{
			    		names=exp.asStringArray();
			    		}
	 				}
	 			else{
		 			exp=re.eval("unlist(mget(group,"+chip+rname+", ifnotfound=NA))");
			    	if(exp!=null)	
			    		{
			    		names=exp.asStringArray();
			    		}
			    	message="searching for gene descriptions...";
			        System.out.println(message);
			        progress+=5;
			     	setProgress(progress);
			     	//2) Search for gene descriptions
			        exp=re.eval("unlist(mget(group,"+chip+rdescription+", ifnotfound=NA))");
			        if(exp!=null)
			        	{
			       		descriptions=exp.asStringArray();
			    		}
	 				}
	    		}
	 		else
	 			{
	 			if(re.eval("martEnsembl")==null)
	 				exp=re.eval("martEnsembl=getEnsemblMart(species=\""+organism+"\")");
	 			
	 			if(!chip.equals("ensembl_gene_id"))//it's a bit slower if we don't search for ensembl gene ids
	 				exp=re.eval("df=getBMatts(group, mart=martEnsembl, type=\""+chip+"\", attributes=c(\"ensembl_gene_id\",\""+rname+"\",\"description\"))$ids");
	 			else
	 				exp=re.eval("df=getBMGenes(group, mart=martEnsembl, species=\""+organism+"\", type=\""+chip+"\")");
	 			if(!chip.equals(rname))
	 				{
	 				exp=re.eval("df[,\""+rname+"\"]");
		 			if(exp!=null)
		 				names=exp.asStringArray();
	 				}
	 			message="searching for gene descriptions...";
	        	System.out.println(message);
	        	progress+=5;
	     		setProgress(progress);
	     		
	     		exp=re.eval("df[,\""+rdescription+"\"]");
	 			if(exp!=null)	
	        		descriptions=exp.asStringArray();
	 			
	 			exp=re.eval("df[,\"entrezgene\"]");
	 			if(exp!=null)	
	        		entrezs=exp.asStringArray();
	 			if(!rname.equals("ensembl_gene_id"))
	 				{
		 			exp=re.eval("df[,\"entrezgene\"]");
		 			if(exp!=null)	
		        		ensembls=exp.asStringArray();
	 				}
	 			}
	 		}    	
 		
    	
 		
    	
    	message="searching for go terms...";
    	System.out.println(message);
 		progress+=5;
 		setProgress(progress);
 		
 		//3) Search for GO terms
 		RList go=null;
	    if(searchGO)
	 		{
 			int toRetrieve=0;
    		String[] goids;
    	    ArrayList<GOTerm> got=new ArrayList<GOTerm>();
    	
    	    //Search by platform of by biomart
    		if(!isBioMaRt)
	    		{
		    	exp=re.eval("go=mget(group,"+chip+rgo+", ifnotfound=NA)");
		    	exp=re.eval("go");
				if(exp!=null)	
		    		{
		    		go=exp.asList();
		    		exp=re.eval("l=unique(unlist(go))");
		    		exp=re.eval("l[grep(\"GO:.*\", l)]");
		    		}
	    		}
    		else
		    	{
		    	//BIOMART SEARCH
    			if(re.eval("martEnsembl")==null)
	 				exp=re.eval("martEnsembl=getEnsemblMart(species=\""+organism+"\")");
	 			
		    	exp=re.eval("df=getBMGO(group, mart=martEnsembl, type=\""+chip+"\")");
		    	exp=re.eval("df");
		    	if(exp!=null)
			    	{
		    		go=exp.asList();
		    	    exp=re.eval("unique(unlist(df))");
			    	}
		    	else
		    		{
		    		System.err.println("GO es null aqu� ya!!"+re.eval("unique(unlist(df))"));
		    		}
		    	}
    		
    		//Either case, get GO definitions
			if(exp!=null)
				{
	    		goids=exp.asStringArray();
	    	    String n="c(\"";
	    	    for(int i=0;i<goids.length;i++)	//Select the ones not already stored in java
	    	    	{
	    	    	if(goids[i]!=null && goids[i].startsWith("GO:"))
		    	    	{
	    	    		GOTerm gt=GOTerms.get(goids[i]);
		    	    	if(gt!=null)	got.add(gt);
		    	    	else		
		    	    		{
		    	    		toRetrieve++;
		    	    		n=n.concat(goids[i]+"\", \"");
		    	    		}
		    	    	}
	    	    	}
	    	    n=n.substring(0,n.length()-3)+")";
	    	    exp=re.eval("got=getGOTermsByGOID("+n+")");//<-tarda entre 30 y 100ms, lo cual puede hacer m�s de un minuto para arrays de 10000 genes
	    	    if(exp==null)	System.err.println("Error getting GO terms by ID with R");
	    		
	    	    //Add new ones to java map
	    	    exp=re.eval("got@terms");
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
	    			evs=new int[toRetrieve];
	    			for(int i=0;i<evs.length;i++)	evs[i]=1;
	    			}
	    		
	    		if(evs!=null && ids !=null && o!=null && d!=null && t!=null) 
					{
		    		for(int i=0;i<evs.length;i++)
		    			{
		    			if(!ids[i].equals("biological_process") && !ids[i].equals("molecular_function") && !ids[i].equals("cellular_component"))
			    			{
			    			message="adding term "+ids[i];
			    			progress+=84.0/evs.length;
			    	 		setProgress(progress);
			    	 		
			    			GOTerm gt=new GOTerm(t[i], ids[i], d[i], o[i], "", evs[i]);
			    			GOTerms.put(ids[i], gt);
			    			}
		    			}
		    		}
	    		}//if(exp!=null)
		    }//if(searchGO)
    	//System.out.println("GOTerms size: "+GOTerms.size()+ " go es "+go+" searchGO es "+searchGO);
    	
    	//4) Add all the new information to the genes
    	for(int g=0;g<genes.length;g++)
    		{
    		Integer id=genes[g];
			
			GeneAnnotation ga=geneAnnotations.get(id);
			if(ga==null)
				{
				ga=new GeneAnnotation();
				ga.internalId=id;
				if(descriptions!=null)	ga.description=descriptions[g];
				if(names!=null)			
					{
					if(names[g]==null || names[g].equals("NA"))	ga.name=geneNames[g];//Set as gene name its id
					else						ga.name=names[g];
					}
				if(entrezs!=null)	ga.entrezId=entrezs[g];
				if(ensembls!=null)	ga.ensemblId=entrezs[g];
				
				ga.id=geneNames[g];
				geneAnnotations.put(id, ga);
				}
			else
				{
				if(descriptions!=null)	ga.description=descriptions[g];
				if(names!=null)			ga.name=names[g];
				ga.internalId=id;
				}
			//In addition, if searched and found, we add the GO terms		
			if(go!=null)
				{
				RList goterms=null;
				String[] goids=null;
				
				if(isBioMaRt)
					{
					goids=go.at(g).asStringArray();
					}
				else			
					{
					goterms=go.at(ga.id).asList();
					if(goterms!=null)	goids=goterms.keys();
					}
				if(goids!=null)
					{
					if(ga.goTerms==null)	
						ga.goTerms=new ArrayList<GOTerm>();
					for(int i=0;i<goids.length;i++)
						{
						if(goids[i].startsWith("GO:"))
							{
							GOTerm gt=GOTerms.get(goids[i]);
							if(gt!=null && !ga.goTerms.contains(gt))	
								if(!gt.term.equals("biological_process") && !gt.term.equals("molecular_function") && !gt.term.equals("cellular_component"))
						    		ga.goTerms.add(gt);
							}
						}
					}
				}
			galist.add(ga);
			}

    	System.out.println("Finished: annotations retrieved in "+(System.currentTimeMillis()-t0)/1000.0+" secs.");
		message="Finished: annotations retrieved in "+(System.currentTimeMillis()-t0)/1000.0+" secs.";
		progress=100;
		setProgress(progress);

		System.out.println("---END getMultipleGeneAnnotationsR---");

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
			
			//for(int g=0;g<genes.size();g++)
			for(int g=0;g<genes.length;g++)
				{
				//Integer id=genes.get(g);
				Integer id=genes[g];
				String gene=geneNames[id];
				
				message="searching for "+gene+" in "+organism+" ...";
		 		//progress+=100*0.5/genes.size();
				progress+=100*0.5/genes.length;
		 		setProgress(progress);
		 		
				GeneAnnotation ga=geneAnnotations.get(id);
				if(ga==null)
					{
					ga=new GeneAnnotation();
			//		System.out.println("GENE "+gene);
			    	REXP exp=re.eval("group=c(\""+gene+"\")");
			    	ga.id=gene;
			    	exp=re.eval("get(\""+gene+"\","+chip+rname+")");
			    	if(exp!=null)	
			    		{
			 //   		System.out.println("name: "+exp.asString());
						ga.name=exp.asString();
			    		}
			    	exp=re.eval("get(\""+gene+"\","+chip+rdescription+")");
			    	if(exp!=null)	
			    		{
			 //   		System.out.println("desc: "+exp.asString());
						ga.description=exp.asString();
			    		}
			    	galist.add(ga);
					}
				if(ga!=null && ga.goTerms==null)
					{
			    	//GoTerms
			    	ArrayList<String> al=new ArrayList<String>();
			    	al.add(ga.id);
			    	ga.goTerms=getGOTermsR(al);
			    	geneAnnotations.put(id, ga);
					}
				}
			
	    	//System.out.println("Finished: annotations retrieved in "+(System.currentTimeMillis()-t1)/1000+" secs.");
	    	message="Finished: annotations retrieved in "+(System.currentTimeMillis()-t1)/1000.0+" secs.";
	    	System.out.println(message);
			progress=100;
			setProgress(progress);
			
			return galist;
			}
		
		public ArrayList<GeneAnnotation> getGeneAnnotationNCBI()
			{
			double progress=0;
			long t1=System.currentTimeMillis();
			//for(int g=0;g<genes.size();g++)
			for(int g=0;g<genes.length;g++)
				{
				//String gene=geneNames[genes.get(g)];
				String gene=geneNames[genes[g]];
		    	ArrayList<String> ncbiIds=new ArrayList<String>();
		    	
		 		message="searching for "+gene+" in "+organism+" ...";
		 		//progress+=100*0.5/genes.size();
		 		progress+=100*0.5/genes.length;
		 		setProgress((int)progress);
		 		//ga=geneAnnotations.get(genes.get(g));
		 		ga=geneAnnotations.get(genes[g]);
		 		if(ga==null)
			 		{
		 	 		IdListType list=null;
			 		if(chip.equals("GeneName"))//if chip is GeneName
				 		list=NCBIReader.eGeneQuery(gene+"[gene] AND \""+organism+"\"[organism]");
			 		else if(chip.equals("GeneID"))
				 		list=NCBIReader.eGeneQuery(gene+"[uid] AND \""+organism+"\"[organism]");
			 		else if(chip.equals("anything"))
						list=NCBIReader.eGeneQuery(gene+" AND \""+organism+"\"[organism]");
					
			 		if(list!=null && list.getId()!=null)
						{
						if(list.getId().length>0)	
							{
							if(list.getId().length>1)
								System.out.println("WARNING: found "+list.getId().length+" coincidences, taking the first one as the best match");
							ncbiIds.add(list.getId(0));
							}
						
						message="Annotations found, collecting ...";
						progress+=100*0.5/genes.length;
						if(progress>=100)	progress=99;
				 		setProgress((int)progress);
						}
					else	
						{
						System.err.println("Nothing found for gene "+gene);
						ga=new GeneAnnotation();
						ga.id=gene;
						
						geneAnnotations.put(genes[g], ga);
						
						message="Nothing found for gene "+gene;
						progress+=100*0.5/genes.length;
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
					          	ga.entrezId=res[i].getId();
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
							geneAnnotations.put(genes[g], ga);
							}
			 			}
					
			 		//Try to retrieve GOTerms via QuickGO
			 		getTermsQuickGO(ga, true);
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
		public ArrayList<GeneAnnotation> doInBackground()
			{
			synchronized(re)
				{
				if(searchByR)
					getMultipleGeneAnnotationsR();
				else
					getGeneAnnotationNCBI();
				done();
				 return galist;
				}
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
		ArrayList<GOTerm> gol=requestQuickGO(ga.entrezId, unique);
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
	        //System.out.println(columns);
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
	        //System.out.println("Found "+terms.size()+" terms");
	        List<GOTerm> list=Arrays.asList(terms.values().toArray(new GOTerm[0]));
	          // Write out the unique terms
	        /*for (GOTerm term : list) {
		            System.out.println(term.term);
		    }*/
	        return new ArrayList<GOTerm>(list);
	        }catch(IOException e){e.printStackTrace();}
	    return null;
		}
	
	public class LoadTask extends SwingWorker<Integer, Void> 
	{
	public String path;
	public boolean invert;
	public int nd;
	public String message="";
	public int rowHeader, colHeader;//Row header is the number of initial rows dedicated to information about samples. colHeader is the number of initial columns dedicated to information about genes (by now, just 1)
	//Right now, rowHeader is computed as the number of rows before the first one that contains numbers, which is 1 (sample IDs) or more, if experimental factors are described
	//TODO colHeader is fixed to one by now.
	//TODO rowHeader right now cannot account for numeric experiment factors
	@Override
	public Integer doInBackground() 
		{
		//System.out.println("Memory reserved before loading: "+Sizeof.usedMemory());
		int progress=0;
		String name=path.replace("\\","/");
		message="Reading matrix "+name.substring(name.lastIndexOf("/")+1, name.indexOf("."));
		progress+=10;
		setProgress(progress);
		
		String description="";
		double t1=System.currentTimeMillis();
		double start=System.currentTimeMillis();
		DelimitedTextTableReader tr=new DelimitedTextTableReader("\t");
		System.out.println("Loading "+path);
		tr.setHasHeader(true);
		Table levelsi=null;
		try{
		levelsi=tr.readTable(new FileInputStream(path));	//esto es lo que mas peso tiene en tiempo (5s para 54000x9)
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
		else	//We determine the number of EFs by finding the first double. Therefore, EFVs should be strings that cannot be interpreted as doubles!
			{
			try{
				BufferedReader in =	new BufferedReader(new FileReader(path));
				int numLines=levelsi.getRowCount();
				rowHeader=0;
				boolean stop=false;
				for(int i=0;i<numLines;i++)	
					{
					String st=in.readLine();
					Scanner scanner = new Scanner(st);
					scanner.useDelimiter("\\t");
					String scan="";
					int colCont=0;
					while(scanner.hasNext())
						{
					    if (scanner.hasNextDouble()) 
				        	{stop=true; break;}
					    scan=scanner.next();
					    colCont++;
					    double scand=-333;
					    try{scand=new Double(scan.trim());}catch(NumberFormatException nfe){}
					    if(scand!=-333){stop=true; break;}
					    }
			        if(stop)
			        	{
			        	System.out.println("Out because of "+scanner.next());
			        	//colHeader=colCont;//By now, only one column
						break;
			        	}
			        rowHeader++;
					}
				}catch(Exception e){e.printStackTrace();}
				System.out.println("Number of row headers: "+rowHeader);
			numConditions=levelsi.getColumnCount()-colHeader;
			numGenes=levelsi.getRowCount()-rowHeader+1;
			}
		message=numGenes+" rows\n"+numConditions+" columns";
		progress+=60;
		setProgress(progress);
		
		conditionNames=new String[numConditions];
		geneNames=new String[numGenes];
		decimals=new int[numConditions];
		for(int i=0;i<numConditions;i++)	decimals[i]=nd;
		columnOrder=new int[numConditions];
		for(int i=0;i<numConditions;i++)	columnOrder[i]=i;
		System.out.println("Matrix with "+numGenes+" rows and "+numConditions+" columns");
	
		
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
			BufferedReader in =	new BufferedReader(new FileReader(path));
			StringTokenizer st=null;
			
			//Read sample related info
			if(rowHeader>=0)
				{
				st=new StringTokenizer(in.readLine(), "\t"); //First row contains sample names and the different gene names maybe
				for(int i=0;i<colHeader;i++)		//Depending on the colHeader, it can also contain additional information. TODO right now colHeader is always 1, with organism/geneID info
					{
					description=st.nextToken();//Pasamos los que no tienen q ver, que nombran las columnas de explicaci�n de genes
					
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
					}
				for(int i=0;i<numConditions;i++)	conditionNames[i]=st.nextToken().trim();
				for(int i=0;i<rowHeader-1;i++)	//Read experiment factors
					{
					st=new StringTokenizer(in.readLine(), "\t"); 
					String ef=st.nextToken().trim();
					for(int j=0;j<colHeader-1;j++)	st.nextToken();//Avoid the blanks due to column Headers
					String[] efvs=new String[numConditions];
					int cont=0;
					while(st.hasMoreTokens())		efvs[cont++]=st.nextToken().trim();
					experimentFactors.add(ef);
					experimentFactorValues.put(ef, efvs);
					}
				}
			else	//Use integers as identifiers
				for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString().trim();
				
			//Read gene related info (except expression levels)
			if(colHeader>0)
				{
				for(int i=0;i<numGenes;i++)//Read gene names
					{
					String cad=in.readLine();
					if(cad.contains("\t\t"))
						{
						JOptionPane.showMessageDialog(null,
								"Empty sample name(s) found, remove additional tabs between fields.", 
								"Wrong format", JOptionPane.ERROR_MESSAGE);
						return 1;
						}
					st=new StringTokenizer(cad,"\t");//El delimitador en Syntren es un tab.
					geneNames[i]=st.nextToken().trim();
					}
				}
			else	//Set up numbers as geneNames
				for(int i=0;i<numGenes;i++)	geneNames[i]=new Integer(i).toString().trim();
			}
		
		rowLabels=geneNames.clone();//TODO: check
		columnLabels=conditionNames.clone();
		
		}catch(Exception e){System.err.println("Error reading file "+path); e.printStackTrace(); return 1;}
		double t2=System.currentTimeMillis();
		
		
		

		message="Organism: "+organism+"\nPlatform: "+chip+"\nRetrieving gene annotations ... (in background)";
		progress+=10;
		setProgress(progress);
		
		geneAnnotations=new TreeMap<Integer, GeneAnnotation>();
		GOTerms=new TreeMap<String, GOTerm>();
		AnnotationStartThread p = new AnnotationStartThread();
		p.setPriority(Thread.MIN_PRIORITY);
		new Thread(p).start();

		//System.out.println("T1) Gene Names "+(t2-t1)/1000);
		
		if(invert)	levels=invert(levelsi);	//OJO: Para el caso de que est�n invertidos (SynTReN por ejemplo)
		else		levels=levelsi;
		t1=System.currentTimeMillis();
		//System.out.println("T2) inversi�n "+(t1-t2)/1000);
		
		expressions=convert(levels, rowHeader-1, colHeader);
		
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
			geneLabels.setString(row, "name", rowLabels[i]);
			geneLabels.setInt(row, "id", i);
			geneLabels.setInt(row, "rowRank", i);
			row++;
			if(i%step==0)
				{
				sparseRow=sparseGeneLabels.addRow();
				sparseGeneLabels.setString(sparseRow, "name", rowLabels[i]);
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
			conditionLabels.setString(row, "name", columnLabels[i]);
			conditionLabels.setInt(row, "id", i);
			conditionLabels.setInt(row, "colRank", i);
			}
		
		message="Computed sparse representation";
		progress+=10;
		setProgress(progress);
		
		t1=System.currentTimeMillis();
		System.out.println("T4) creacion de tablas "+(t1-t2)/1000);
		message="Microarray data loaded in "+(t1-start)/1000+" seconds";
		progress=100;
		setProgress(progress);
		System.out.println("Memory reserved after loading: "+Sizeof.usedMemory());
		try{Sizeof.runGC(1);}catch(Exception e){e.printStackTrace();}
		System.out.println("Memory reserved after loading & GC: "+Sizeof.usedMemory());
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
		sparseGeneLabels.setString(sparseRow, "name", rowLabels[i]);
			sparseGeneLabels.setInt(sparseRow, "id", sparseRow);
		sparseGeneLabels.setInt(sparseRow, "actualId", i);
		sparseGeneLabels.setInt(sparseRow, "rowRank", sparseRow);
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
			sparseExpressions.setString(sparseRow, "gene", rowLabels[i]);
			sparseExpressions.setString(sparseRow, "condition", columnLabels[j]);
					
			//TODO This if else is probably stupid
			//if(levels.canGet(conditionNames[j],Double.class))	sparseExpressions.setDouble(sparseRow, "level", ((Double)levels.get(i,j+1)).doubleValue());
			//else
			sparseExpressions.setDouble(sparseRow, "level", matrix[i][j]); 
			
			sparseExpressions.setInt(sparseRow, "rowId", contGene);
			sparseExpressions.setInt(sparseRow, "actualRowId", i);
			sparseExpressions.setInt(sparseRow, "rowRank", contGene);
			sparseExpressions.setInt(sparseRow, "colId", j);
			sparseExpressions.setInt(sparseRow, "colRank", j);
			sparseRow++;
			}
		if(i%step==0)	contGene++;
		}
	return;
	}

public void loadMicroarray(String path, boolean invert, int rowHeader, int colHeader, int nd) 
	{
	int progress=0;
	progress+=10;
	
	String description="";
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

	
	expressions=convert(levels, rowHeader, colHeader);
	
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
	
	/**
	 * Sorts columns (specially useful for parallel coordinates and heatmaps)
	 * @param factor
	 */
	public int[] sortColumnsBy(String factor)
		{
		REXP exp=re.eval("as.integer(rank("+ RUtils.getRList(experimentFactorValues.get(factor))+", " +
				"ties.method=\"first\")-1)");//TODO: null pointer if it's not a factor but the ids (it could also be done by sort(..., dindex=T)$ix)
		columnOrder=exp.asIntArray();
		sortColumns();
		return getColumnOrder();
		}
	
	/**
	 * Refresh colRanks in sparse expressions and in condition labels with the current columnOrder
	 */
	public void sortColumns()
		{
		for(int i=0;i<this.numSparseGenes;i++)
			for(int j=0;j<this.numConditions;j++)
				{
				sparseExpressions.setInt(i*numConditions+j, "colRank", columnOrder[j]);
				}
	    for(int i=0;i<this.numConditions;i++)
 	    	{
 	    	conditionLabels.setInt(i, "colRank", columnOrder[i]);
 	    	}
	 	}
	
	/**
	 * Returns the current columnOrder c, where c[i]=j means that the current position for column i is now j.
	 * @return
	 */
	public int[] getColumnOrder()
		{
		int[] co=new int[columnOrder.length];
		for(int j=0;j<columnOrder.length;j++)		co[columnOrder[j]]=j;  
		return co;
		}
	
	
	public LinkedList<Integer> selectHiLo(String highEFV, String highEF, int sdsAbove, String lowEFV, String lowEF, int sdsBelow)
		{
		if(!experimentFactors.contains(highEF) && !highEF.equals("none333") && !highEF.equals("rest") )
			{System.err.println("Experimental factor "+highEF+" does not exist"); return null;}
		if(!experimentFactors.contains(lowEF) && !lowEF.equals("none333") && !lowEF.equals("rest"))
			{System.err.println("Experimental factor "+highEF+" does not exist"); return null;}
		
		LinkedList<Integer> ret=new LinkedList<Integer>(); 
		String[] efvsH=experimentFactorValues.get(highEF);
		String[] efvsL=experimentFactorValues.get(lowEF);
		if(highEF.equals("rest"))	efvsH=efvsL;
		if(lowEF.equals("rest"))	efvsL=efvsH;
		
		for(int i=0;i<numGenes;i++)
			{
			boolean add=true;
			for(int j=0;j<numConditions;j++)
				{
				if(!highEFV.equals("none333") && ((highEFV.equals("rest") && !efvsH[j].equals(lowEFV)) || efvsH[j].equals(highEFV))) //TODO:the first part of the if could be done less times.
					if(matrix[i][j]<averageCols[j]+sdsAbove*sdCols[j])	{add=false; break;}
				if(!lowEFV.equals("none333") && ((lowEFV.equals("rest") && !efvsL[j].equals(highEFV)) || efvsL[j].equals(lowEFV)))
					if(matrix[i][j]>averageCols[j]+sdsBelow*sdCols[j])	{add=false; break;}
				}
			if(add)	ret.add(i);
			}
		return ret;
		}
	/**
	 * Returns the ids of the conditions that have the corresponding experimental factor value for a given experimental factor
	 * @param ef - experimental factor to be checked
	 * @param efv - experimental factor value that have the conditions to be returned
	 * @param notEqual - if true, returns all the conditions except the ones corresponding to the efv for ef 
	 * @return
	 */
	public Integer[] getConditions(String ef, String efv, boolean notEqual)
		{
		if(ef==null || efv==null || ef.length()==0 || efv.length()==0 )
			{System.err.println("No ef or efv specified"); return null;}
		LinkedList<Integer> ret=new LinkedList<Integer>();
		String[] efvs=experimentFactorValues.get(ef);
		for(int i=0;i<efvs.length;i++)
			{
			if(notEqual)	{if(!efvs[i].equals(efv))	ret.add(i);}
			else			{if(efvs[i].equals(efv))		ret.add(i);}
			}
		return ret.toArray(new Integer[ret.size()]);
		}
	public LinkedList<Integer> selectHiLo(String highEFV, String highEF, String lowEFV, String lowEF)
		{
		return selectHiLo(highEFV, highEF, 0, lowEFV, lowEF, 0);
		}
}