package es.usal.bicoverlapper.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.DelimitedTextTableReader;

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
	Table geneLabels;//Expresion levels, each one as tuple (gene, condition, level)
	Table conditionLabels;//Expresion levels, each one as tuple (gene, condition, level)
	int maxGenes=2000;//A partir de 200
	Table sparseExpressions;//As above, but sparse (only a maximum number of genes are shown)
	Table sparseGeneLabels;
	//Table sparseConditionLabels;//unnecesary by now, always show all conditions
	String[] conditionNames;
	String[] geneNames;
	int numGenes;
	int numSparseGenes;//for sparse matrices
	int numConditions;
	
	/**
	 * Constructor from a file
	 * @param path Path to the file with microarray information
	 * @param invert true if genes are columns (genes as rows are considered as the usual option)
	 * @param rowHeader Number of initial rows with row information (usually one)
	 * @param colHeader Number of initial columns with column information (usually one)
	 */
	public MicroarrayData(String path, boolean invert, int rowHeader, int colHeader) throws Exception
		{
		//ProgressBarDemo jop=new ProgressBarDemo();
				
		double t1=System.currentTimeMillis();
		DelimitedTextTableReader tr=new DelimitedTextTableReader("\t");
		System.out.println(path);
		tr.setHasHeader(true);
		Table levelsi=null;
		//	try{
			levelsi=tr.readTable(new FileInputStream(path));
			System.out.println(levelsi.get(0,0).getClass()+" "+levelsi.get(0,1).getClass());
			System.out.println(levelsi.getColumnCount()+", "+levelsi.getRowCount());
			System.out.println(levelsi.get(0,0)+" "+levelsi.get(0,3));
		//	}catch(Exception e){System.err.println("Error reading file "+path); e.printStackTrace(); System.exit(1);}
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
		//jop.showMessageDialog(v,"Nº of genes: "+numGenes+" \nNº of conditions: "+numConditions,null,JOptionPane.INFORMATION_MESSAGE);		
		//jop.showInternalMessageDialog(v,"Nº of genes: "+numGenes+" \nNº of conditions: "+numConditions);
		//jop.setValue(10);
		//jop.setString("Nº of genes: "+numGenes+" \nNº of conditions: "+numConditions);
		
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
				for(int i=0;i<colHeader;i++)		st.nextToken();//Pasamos los que no tienen q ver, que nombran las columnas de explicación de genes
				for(int i=0;i<numConditions;i++)	conditionNames[i]=st.nextToken();
				}
			else	for(int i=0;i<numConditions;i++)			conditionNames[i]=new Integer(i).toString();
			}
		
		}catch(Exception e){System.err.println("Error reading file "+path); e.printStackTrace(); System.exit(1);}
		System.out.println("número de genes\t"+numGenes+" número de condiciones\t"+numConditions);
		System.out.println("GENES");
		for(int i=0;i<numGenes;i++)	System.out.println(geneNames[i]);
		System.out.println("CONDICIONES");
		for(int i=0;i<numConditions;i++)	System.out.println(conditionNames[i]);
		double t2=System.currentTimeMillis();
		if(numGenes<=maxGenes)	maxGenes=1;
		
		System.out.println("T1) "+(t2-t1)/1000);
		
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
		System.out.println("Añadidas "+numSparseGenes+" etiquetas de genes");
		
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
			row++;
			}
		t1=System.currentTimeMillis();
		System.out.println("T4) creación de tablas "+(t1-t2)/1000);
		System.out.println(numConditions+"x"+numGenes);
		System.out.println("Número de filas de las etiquetas: "+conditionLabels.getRowCount()+" "+geneLabels.getRowCount());
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
		sparseExpressions.addColumn("colId", int.class);
		System.out.println("SkipColumns "+skipColumns);
		int row=0;
		int sparseRow=0;
		int contGene=0;
		int step=1;
		if(numGenes>maxGenes)	step=numGenes/maxGenes;
		for(int i=0;i<numGenes;i++)
			{
			for(int j=0;j<numConditions;j++)
				{
				row=ret.addRow();
				ret.setString(row, "gene", geneNames[i]);
				ret.setString(row, "condition", conditionNames[j]);
				
				//System.out.println(row+" "+i+" "+j+" "+conditionNames[j]+" "+geneNames[i]);
				if(matriz.canGet(conditionNames[j],Double.class))	ret.setDouble(row, "level", ((Double)matriz.get(i,j+skipColumns)).doubleValue());
				else												
					{
					if(matriz.canGet(conditionNames[j],Integer.class))
						{
					//	System.out.println(matriz.get(i,j+skipColumns));
						ret.setDouble(row, "level", ((Integer)matriz.get(i,j+skipColumns)).doubleValue());
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
					//sparseExpressions.setInt(sparseRow, "rowId", i);
					//sparseExpressions.setInt(sparseRow, "rowRank", i);
					sparseExpressions.setInt(sparseRow, "rowId", contGene);
					sparseExpressions.setInt(sparseRow, "rowRank", contGene);
					sparseExpressions.setInt(sparseRow, "colId", j);
					sparseExpressions.setInt(sparseRow, "colRank", j);
					sparseRow++;
					}
				}
			if(i%step==0)	contGene++;
			}
		
		System.out.println("Añadidas "+sparseRow+" expresiones");
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
			System.out.println(conditionNames[i]);
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

	void buildSparse(LinkedList<Integer> genes)
		{
	
		for(int i=0;i<genes.size();i++)
			{
			Tuple t=geneLabels.getTuple(genes.get(i));
			if(!sparseGeneLabels.containsTuple(t))	//si no está, tenemos que incluirla quitando una ue no esté en genes
				{
				int j=0;
				while(genes.contains(sparseGeneLabels.getTuple(j).getInt("actualId")))	j++;//mientras esté en genes, probamos con la sig.
				
				sparseGeneLabels.setString(j, "name", geneNames[genes.get(i)]);//Nuevo nombre e id para él, el resto no cambia
				sparseGeneLabels.setInt(j, "actualId", genes.get(i));
				//TODO: también cambian las expresiones correspondientes claro
				}
			}
		}


	
	/**
	 * Returns the table with expression levels
	 * @return	Table with expression levels
	 */
	public Table getExpressions() {
		return expressions;
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
	Table getSparseExpressions() {
		return sparseExpressions;
	}

	void setSparseExpressions(Table sparseExpressions) {
		this.sparseExpressions = sparseExpressions;
	}

	Table getSparseGeneLabels() {
		return sparseGeneLabels;
	}

	void setSparseGeneLabels(Table sparseGeneLabels) {
		this.sparseGeneLabels = sparseGeneLabels;
	}

	int getNumSparseGenes() {
		return numSparseGenes;
	}

	void setNumSparseGenes(int numSparseGenes) {
		this.numSparseGenes = numSparseGenes;
	}
	}
