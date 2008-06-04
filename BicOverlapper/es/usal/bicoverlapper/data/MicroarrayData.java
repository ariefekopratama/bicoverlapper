package es.usal.bicoverlapper.data;

//import gov.nih.nlm.ncbi.www.soap.eutils.esearch.IdListType;
//import gov.nih.nlm.ncbi.www.soap.eutils.esummary.DocSumType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextLayout;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.Timer;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.util.Sort;
import prefuse.util.collections.IntIterator;
import test.QuickSoapTest;

/**
 * Class with data of Microarray expression levels, using Prefuse Tables
 * We will generically refer to genes and conditions, although genes could
 * be instead nucleotide sequences and conditions can be experiments, patients, etc.
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
 *  
 * @author Rodrigo Santamaría
 *
 */
public class MicroarrayData 
	{
	Table levels;//Expresion levels as usual matrix
	Table expressions;//Expresion levels, each one as tuple (gene, condition, level)
	float matrix[][];//matrix with expression levels replicated, to quicken some arithmetic operations
	Table geneLabels;//Expresion levels, each one as tuple (gene, condition, level)
	Table conditionLabels;//Expresion levels, each one as tuple (gene, condition, level)
	int maxGenes=199;//A partir de 200
	Table sparseExpressions;//As above, but sparse (only a maximum number of genes are shown)
	Table sparseGeneLabels;
	//Table sparseConditionLabels;//unnecesary by now, always show all conditions
	String[] conditionNames;
	String[] geneNames;
	int numGenes;
	int numSparseGenes;//for sparse matrices
	int numConditions;
	private int[] decimals;
	
	String chip;//kind of microarray chip (any official for Affymetrix permitted, by now), or kind of name taken by genes (geneID and ORF permitted)
	String organism;//Name of organism as registered in NCBI
	Map<Integer, GeneAnnotation> geneAnnotations;//Gene Annotations from NCBI and GeneOntology
	boolean annotationsRetrieved=false;
	
	/**
	 * Constructor from a file
	 * @param path Path to the file with microarray information
	 * @param invert true if genes are columns (genes as rows are considered as the usual option)
	 * @param rowHeader Number of initial rows with row information (usually one)
	 * @param colHeader Number of initial columns with column information (usually one)
	 * @param nd	Number of decimals to be shown if numerically showing expression levels
	 */
	
	public MicroarrayData(String path, boolean invert, int rowHeader, int colHeader, int nd) throws Exception
		{
	//	ProgressMonitorInputStream pmis=new BufferedInputStream(
	//				ProgressMonitorInputStream();	
		String description="";
		double t1=System.currentTimeMillis();
		DelimitedTextTableReader tr=new DelimitedTextTableReader("\t");
		System.out.println(path);
		tr.setHasHeader(true);
		Table levelsi=null;
		
		levelsi=tr.readTable(new FileInputStream(path));	//esto es lo que más peso tiene en tiempo (5s para 54000x9)
		//InputStream is=new InputStream(new ProgressMonitorInputStream(,"Reading "+path,new FileInputStream(path))));
		if(invert)
			{
			numGenes=levelsi.getColumnCount();//Al revés te lo digo para que me entiendas
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
				/*BufferedReader in =	new BufferedReader(new FileReader(path));
				StringTokenizer st=new StringTokenizer(in.readLine(),"\t");//El delimitador en Syntren es un tab.
				for(int i=0;i<numGenes;i++)	geneNames[i]=st.nextToken();*/
				}
			else	for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString();
			}
		else	//Si no están invertidas las primeras columnas tienen información sobre los genes
			{	//Y las primeras filas sobre las condiciones
			if(colHeader>=0)
				{
				BufferedReader in =	new BufferedReader(new FileReader(path));
				for(int i=0;i<rowHeader;i++)	in.readLine(); //Pasamos las filas con explicaciones sobre los experimentos
				for(int i=0;i<numGenes;i++)
					{
					StringTokenizer st=new StringTokenizer(in.readLine(),"\t");//El delimitador en Syntren es un tab.
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
					description=st.nextToken();//Pasamos los que no tienen q ver, que nombran las columnas de explicación de genes
					}
				for(int i=0;i<numConditions;i++)	conditionNames[i]=st.nextToken();
				}
			else	for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString();
			}
		}catch(Exception e){System.err.println("Error reading file "+path); e.printStackTrace(); System.exit(1);}
		double t2=System.currentTimeMillis();
		if(numGenes<=maxGenes)	maxGenes=1;
		
		System.out.println("Descripción: "+description);
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
					"Chip/Organism description is wrong: "+description, 
					"Wrong format", JOptionPane.ERROR_MESSAGE);
			}

		geneAnnotations=new TreeMap<Integer, GeneAnnotation>();
		/*AnnotationThread p = new AnnotationThread();
		p.setPriority(Thread.MIN_PRIORITY);
		new Thread(p).start();*/

		System.out.println("Organism: "+organism);
		System.out.println("Chip: "+chip);
		
		System.out.println("T1) Nombres "+(t2-t1)/1000);
		
		if(invert)	levels=invert(levelsi);	//OJO: Para el caso de que estén invertidos (SynTReN por ejemplo)
		else		levels=levelsi;
		
		t1=System.currentTimeMillis();
		System.out.println("T2) inversión "+(t1-t2)/1000);
		
		//System.out.println(levels.get(0,3));
		expressions=convert(levels, colHeader);
		
		t2=System.currentTimeMillis();
		System.out.println("T3) conversión "+(t2-t1)/1000);
		
		sparseGeneLabels=new Table();
		sparseGeneLabels.addColumn("name", String.class);
		sparseGeneLabels.addColumn("id", int.class);
		sparseGeneLabels.addColumn("actualId", int.class);
		sparseGeneLabels.addColumn("rowRank", int.class);//Orden en el que serán pintadas

		geneLabels=new Table();
		geneLabels.addColumn("name", String.class);
		geneLabels.addColumn("id", int.class);
		geneLabels.addColumn("rowRank", int.class);//Orden en el que serán pintadas
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
			row++;//esto en principio no es necesario
			}
		t1=System.currentTimeMillis();
		System.out.println("T4) creación de tablas "+(t1-t2)/1000);
		}

	/**
	 * Corverts matriz to a new matrix with information that prefuse graphs can manage
	 * @param matriz
	 * @param skipColumns number of columns to skip, as they are only informative
	 * @return
	 */
	private Table convert(Table matriz, int skipColumns)
		{
		Table ret=new Table();
		ret.addColumn("gene", String.class);
		ret.addColumn("condition", String.class);
		ret.addColumn("level", double.class);
		ret.addColumn("rowRank", int.class);//Para la reordenación de filas y columnas
		ret.addColumn("colRank", int.class);
		ret.addColumn("rowId", int.class);//Para la selección de filas y columnas
		ret.addColumn("colId", int.class);
		
		//Collateraly build sparse expression level matrix
		sparseExpressions=new Table();
		sparseExpressions.addColumn("gene", String.class);
		sparseExpressions.addColumn("condition", String.class);
		sparseExpressions.addColumn("level", double.class);
		sparseExpressions.addColumn("rowRank", int.class);//Para la reordenación de filas y columnas
		sparseExpressions.addColumn("colRank", int.class);
		sparseExpressions.addColumn("rowId", int.class);//Para la selección de filas y columnas
		sparseExpressions.addColumn("actualRowId", int.class);//Para la selección de filas y columnas
		sparseExpressions.addColumn("colId", int.class);
	//	System.out.println("SkipColumns "+skipColumns);
		int row=0;
		int sparseRow=0;
		int contGene=0;
		int step=1;
		if(numGenes>maxGenes)	step=numGenes/maxGenes;
		matrix=new float[numGenes][numConditions];
		
		for(int i=0;i<numGenes;i++)
			{
			for(int j=0;j<numConditions;j++)
				{
				row=ret.addRow();
				ret.setString(row, "gene", geneNames[i]);
				ret.setString(row, "condition", conditionNames[j]);
				
				//System.out.println(row+" "+i+" "+j+" "+conditionNames[j]+" "+geneNames[i]);
				if(matriz.canGet(conditionNames[j],Double.class))	
					{
					double value=((Double)matriz.get(i,j+skipColumns)).doubleValue();
					ret.setDouble(row, "level", ((Double)matriz.get(i,j+skipColumns)).doubleValue());
					matrix[i][j]=(float)value;
					}
				else												
					{
					if(matriz.canGet(conditionNames[j],Integer.class))
						{
					//	System.out.println(matriz.get(i,j+skipColumns));
						double value=((Integer)matriz.get(i,j+skipColumns)).doubleValue();
						ret.setDouble(row, "level", value);
						matrix[i][j]=(float)value;
						}
					}
				ret.setInt(row, "rowId", i);
				ret.setInt(row, "rowRank", i);
				ret.setInt(row, "colId", j);
				ret.setInt(row, "colRank", j);
				row++;
				//if(i%maxGenes==0)	//replicate for sparse matrix
				if(i%step==0)	//replicate for sparse matrix
					{
					sparseRow=sparseExpressions.addRow();
					sparseExpressions.setString(sparseRow, "gene", geneNames[i]);
					sparseExpressions.setString(sparseRow, "condition", conditionNames[j]);
					
					//System.out.println(row+" "+i+" "+j+" "+conditionNames[j]+" "+geneNames[i]);
					if(matriz.canGet(conditionNames[j],Double.class))	sparseExpressions.setDouble(sparseRow, "level", ((Double)matriz.get(i,j+skipColumns)).doubleValue());
					else												
						{
						if(matriz.canGet(conditionNames[j],Integer.class))
							{
						//	System.out.println(matriz.get(i,j+skipColumns));
							sparseExpressions.setDouble(sparseRow, "level", ((Integer)matriz.get(i,j+skipColumns)).doubleValue());
							}
						}
					sparseExpressions.setInt(sparseRow, "rowId", contGene);
					sparseExpressions.setInt(sparseRow, "actualRowId", i);
					sparseExpressions.setInt(sparseRow, "rowRank", contGene);
					sparseExpressions.setInt(sparseRow, "colId", j);
					sparseExpressions.setInt(sparseRow, "colRank", j);
					//System.out.println("Añadiendo: |"+geneNames[i]+"|\t"+contGene+"\t"+i+"\t"+contGene+"\t"+j+"\t"+j+":\t"+matriz.get(i,j+skipColumns));
					sparseRow++;
					}
				}
			if(i%step==0)	contGene++;
			}
		
		//System.out.println("Añadidas "+sparseRow+" expresiones");
		return ret;
		}
	
	/**
	 * Coge tabla e invierte filas por columnas, devolviendo la matriz inversa
	 * @param tabla
	 * @return
	 */
	private Table invert(Table tabla)
		{
		Table ret=new Table();
		
		for(int i=0;i<tabla.getRowCount();i++)//Ponemos tantas columnas como filas
			{
		//	System.out.println(conditionNames[i]);
			ret.addColumn(conditionNames[i],double.class);
			}
		int row=0;
		for(int i=0;i<tabla.getColumnCount();i++)
			{
			row=ret.addRow();
			for(int j=0;j<tabla.getRowCount();j++)
				ret.setDouble(row,conditionNames[j],((Double)tabla.get(j,i)).doubleValue());
			row++;
			}
		return ret;
		}

	public void buildSparse(LinkedList<Integer> genes)
		{
		for(int i=0;i<genes.size();i++)
			{
			Table t=sparseGeneLabels.select(ExpressionParser.predicate("actualId = "+genes.get(i)+""), new Sort());
			if(t==null || t.getRowCount()==0)
				{
				int j=0;
				while(genes.contains(sparseGeneLabels.getTuple(j).getInt("actualId")))	j++;//mientras esté en genes, probamos con la sig.
				sparseGeneLabels.setString(j, "name", geneNames[genes.get(i)]);//Nuevo nombre e id para él, el resto no cambia
				System.out.println("Añadiendo expresiones para gen "+geneNames[genes.get(i)]);
				sparseGeneLabels.setInt(j, "actualId", genes.get(i));
				int id=sparseGeneLabels.getInt(j, "id");
				int actualId=genes.get(i);
				int cont=j*numConditions;
				//TODO: también cambian las expresiones correspondientes claro
				for(int k=0;k<this.numConditions;k++)
					{
					this.sparseExpressions.setInt(cont+k, "actualRowId",actualId);
					this.sparseExpressions.setInt(cont+k, "rowId",id);
					//this.sparseExpressions.setInt(cont+k, "colId",);//En principio los datos de condiciones están bien
					//this.sparseExpressions.setString(cont+k, "condition",);
					this.sparseExpressions.setString(cont+k, "gene",geneNames[actualId]);
					this.sparseExpressions.setDouble(cont+k, "level",this.matrix[actualId][k]);
					System.out.println("Añadiendo expresión a columna "+this.sparseExpressions.getInt(cont+k, "colId")+" de condición "+this.sparseExpressions.getString(cont+k, "condition"));
					System.out.println("gene: "+geneNames[actualId]+"\tlevel: "+matrix[actualId][k]);
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
		float matrixBic[][]=new float[genes.size()][conditions.size()];
		//1) Recuperamos la matriz sobre la que calcular la constancia
		for(int i=0;i<genes.size();i++)
			{
				String gene=genes.get(i);
			int row=getGeneId(gene);
			for(int j=0;j<conditions.size();j++)
				{
				String cond=conditions.get(j);
				int col=getConditionId(cond);
				//Table t=expressions.select(ExpressionParser.predicate("gene = '"+gene+"' and condition ='"+cond+"'"), new Sort());
				//TODO: Quizás convendría tener una matriz en memoria cargada, aunque puede ser mucho peso.
				matrixBic[i][j]=matrix[row][col];
			//	System.out.print(matrix[i][j]+"\t");
				}
		//	System.out.println();
			}
		//2) Calculamos la constancia
		float sum=0;
		float sd=0;
		int num=0;
		switch(type)
			{
			case 0:
				num=genes.size();
				break;
			case 1:
				num=conditions.size();
				break;
			case 2:
				num=genes.size()*conditions.size();
				for(int i=0;i<genes.size();i++)
					for(int j=0;j<conditions.size();j++)
						sum+=matrix[i][j];
				for(int i=0;i<genes.size();i++)
					for(int j=0;j<conditions.size();j++)
						sd+=Math.abs(matrix[i][j]-sum);
				break;
			}
		
		constance=sd/num;
		return constance;
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
		for(int i=0;i<lc.size();i++)		ret.add(conditionNames[lc.get(i)]);
		return ret;
		}
	
	//Sparse tables: IN DEVELOPMENT
	public Table getSparseExpressions() {
		return sparseExpressions;
	}

	public void setSparseExpressions(Table sparseExpressions) {
		this.sparseExpressions = sparseExpressions;
	}

	public Table getSparseGeneLabels() {
		return sparseGeneLabels;
	}

	void setSparseGeneLabels(Table sparseGeneLabels) {
		this.sparseGeneLabels = sparseGeneLabels;
	}

	public int getNumSparseGenes() {
		return numSparseGenes;
	}

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
					else						;//En este caso ya está, no tenemos que quitar decimales
					}
				else		cad=cad.substring(0,pos);
				}
			}
		return cad;
		}
/*
	private class AnnotationThread extends Thread{
		   long minPrime;
	         AnnotationThread() {
	         }
	 
	         public void run() {
	             // fill GeneAnnotations list with gene annotations
	        	 long t1=System.currentTimeMillis();
	        	 String ids="";
	        	 ArrayList<Integer> localIds=new ArrayList<Integer>();
	        	 ArrayList<String> ncbiIds=new ArrayList<String>();
	 				
		        	 for(int i=0;i<geneNames.length;i++)
		        		{
		        		IdListType list=null;
		        		//System.out.println("searching for "+geneNames[i]);
		        		if(chip.equals("GeneName"))//if chip is GeneName
		     		        list=EReader.eGeneQuery(geneNames[i]+"[gene] AND \""+organism+"\"[organism]");
		        		if(chip.equals("GeneID"))
			     		    list=EReader.eGeneQuery(geneNames[i]+"[uid]");
		        		 
		 				if(list!=null && list.getId()!=null)
		 					{
		 					if(list.getId().length>0)	
		 						{
		 						ids=ids.concat(list.getId(0)+", ");//TODO: sólo añadimos el primer gene id, suponiendo que es el del organismo bueno
		 						localIds.add(i);
		 						ncbiIds.add(list.getId(0));
		 						}
		 					//System.out.println("id is "+list.getId(0));
		 					}
		 				System.out.print(".");
		 				}
		        	 
		        	 System.out.println("\nIDs are "+ids);
		        	 System.out.println("Found IDs for "+localIds.size()+" out of the "+geneNames.length);
		 			DocSumType[] res=EReader.eGeneSummary(ids.substring(0, ids.length()-2));
		 			
		 		
		 			for(int i=0; i<res.length; i++)
		 	            {
		 				GeneAnnotation ga=new GeneAnnotation();
		 				for(int k=0; k<res[i].getItem().length; k++)
		 	                {
		 	                if(res[i].getItem()[k].get_any()!=null)
		 	                	{
		 	                	String cad=res[i].getItem(k).getName();
		 	                	//System.out.println(cad);
		 	                    if(cad.contains("Description"))
		 	                		ga.description=res[i].getItem()[k].get_any()[0].getValue();
			 	                else if(cad.contains("Name"))
		 	                		ga.name=res[i].getItem()[k].get_any()[0].getValue();
			 	              else if(cad.contains("Orgname"))
		 	                		ga.organism=res[i].getItem()[k].get_any()[0].getValue();
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
		 				ga.id=ncbiIds.get(i);
		 			//	System.out.println("Añadiendo a gen "+localIds.get(i)+" anotaciones |"+ga.name+"|   |"+ga.description+"|   |"+ga.id+"|   |"+ga.organism);
		 				System.out.println(ga.name+": "+ga.description);
		 				geneAnnotations.put(localIds.get(i), ga);
		 	            }
	        	System.out.println("Gene Annotations retrieved in "+(System.currentTimeMillis()-t1)/1000+" secs."); 
	        	annotationsRetrieved=true;
	         	}
		}//end Thread class
*/
	public Map<Integer, GeneAnnotation> getGeneAnnotations() {
		return geneAnnotations;
	}

	public void setGeneAnnotations(Map<Integer, GeneAnnotation> geneAnnotations) {
		this.geneAnnotations = geneAnnotations;
	}

	public boolean isAnnotationsRetrieved() {
		return annotationsRetrieved;
	}
	}
