package es.usal.bicoverlapper.analysis;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import es.usal.bicoverlapper.data.MicroarrayData;
import es.usal.bicoverlapper.utils.RUtils;

/**
 * This class performs different analysis by means of R.
 * It will be the only one maintaining an Rengine for the use of other classes too. 
 * @author Rodrigo Santamaria
 *
 */
public class Analysis 
	{
	MicroarrayData microarrayData=null;
	public MicroarrayData getMicroarrayData() {
		return microarrayData;
	}

	public void setMicroarrayData(MicroarrayData md) {
		this.microarrayData = md;
	}
	public Rengine r=null;
	REXP exp=null;
	String defaultPath="";
	int[] filterOptions=null;
	private boolean matrixLoaded=false;
	
	public int[] getFilterOptions() {
		return filterOptions;
	}

	public void setFilterOptions(int[] filterOptions) {
		this.filterOptions = filterOptions;
	}

	/**
	 * Initiates the biclustering analyzer by loading the required R libraries and sourcing files,
	 * and by loading the microarray data matrix into R
	 * @param session
	 */
	public Analysis()
		{
		try{
			BufferedReader	pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/path.txt"));
			defaultPath=pathReader.readLine();
			startR();
		}catch(Exception e){e.printStackTrace();}
		}
	
	public void startR()
		{
		
		System.out.println("Library is: "+System.getProperty("java.library.path"));
    	System.out.println("Creating Rengine (with arguments)");
	    System.out.println("R_HOME is: "+System.getenv("R_HOME"));
        r=new Rengine(new String[]{"--vanilla"}, false, null);
        System.out.println("Rengine created, waiting for R");
		// the engine creates R in a new thread, so we should wait until it's ready
        if (!r.waitForR()) 
        	{
            System.err.println("Cannot load R");
            return;
        	}
	    System.out.println("R started");
	    loadR();
	    System.out.println("required R/Bioconductor libraries loaded");
	    }
	
	/**
	 * Loads the libraries in R. An R console must have been started in the sesion to do this.
	 * Right now, the library to load is "biclust"
	 * It also loads some internal r scripts that extend the biclust package.
	 * TODO: LoadR and loadMatrix should possibly be loaded with the microarra, we should check the memory/loadtime trade-off
	 */
	public void loadR() //TODO: maybe good to make it in background or load the big libraries (GO.db, etc) on demand.
		{
		if(r==null)	{System.err.println("No R console started");	return;}
		/*loadRLibrary("biclust");
		loadRLibrary("isa2");
		loadRLibrary("GO.db");
		loadRLibrary("GOstats");*/
	       	
        exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/binarize.r\")");
        exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/helpers.r\")");
        exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/writeBiclusterResults.r\")");
       }
	
	public void loadRLibrary(String library)
		{
		exp=r.eval("library("+library+")");
        if(exp==null)
        	{
        	exp=r.eval("install.packages(\""+library+"\")");
        	exp=r.eval("library("+library+")");
        	if(exp==null)
    	    	{
    	    	JOptionPane.showMessageDialog(null,
					"Package biclust is not installed in R and could not be installed automatically\n Please install the package manually through the R console \nIn the meantime, Plaid, Bimax, xMotifs and Cheng&Church biclustering won't be available", 
					"Missing R package", JOptionPane.WARNING_MESSAGE);
    	    	}
        	}
		}
	
	/**
	 * Loads the gene expression matrix into the R session.
	 */
	public void loadMatrix()
		{
		if(microarrayData.matrix!=null)
			{
			int nc=microarrayData.getNumConditions();
			int ng=microarrayData.getNumGenes();
			exp=r.eval("m<-matrix(NA, "+ng+", "+nc+")");
			if(exp==null)	{System.err.println("Matrix cannot be load in R"); return;}
			for(int i=0;i<ng;i++)
				{
				String v="c(";
				for(int j=0;j<nc;j++)
					{
					if(j<nc-1)	v=v.concat(microarrayData.matrix[i][j]+", ");
					else		v=v.concat(microarrayData.matrix[i][j]+")");
			    	}
				exp=r.eval("m["+(i+1)+",]<-"+v);
				}
			String names="c(";
			for(int i=0;i<ng;i++)
				{
				if(i<ng-1)	names=names.concat("\""+microarrayData.geneNames[i]+"\", ");
				else		names=names.concat("\""+microarrayData.geneNames[i]+"\")");
				}
			exp=r.eval("rownames(m)<-"+names);
			names="c(";
			for(int i=0;i<nc;i++)
				{
				
				if(i<nc-1)	names=names.concat("\""+microarrayData.conditionNames[i]+"\", ");
				else		names=names.concat("\""+microarrayData.conditionNames[i]+"\")");
				}
			exp=r.eval("colnames(m)<-"+names);
			matrixLoaded = true;
			}
		else{System.err.println("No matrix loaded"); return;}
		}
	
	/**
	 * Removes any possible stored data about the expression matrix
	 * By now, I feel no need of unloading packages/sources, etc. (should go to unloadR)
	 */
	public void unloadMatrix()
		{
		if(r!=null)	
			{
			r.eval("rm(m)");
			}
		}
	/**
	 * Performs the BiMax biclustering with the R implementation in package biclust	 
	 * @param percentage - if true the previous binarization step is done by percentage (only the specified percentage of expression levels will be 1)
	 * @param threshold - the expression or percentage limit for binarization
	 * @param under - if true, the values under the threshold are set to 1 and the expression levels above the threshold are set to 0 (viceversa if false) 
	 * @param minr - minimum number of rows (genes) that biclusters must have
	 * @param minc - minimum number of columns (conditions) that biclusters must have
	 * @param maxNumber - maximum number of biclusters to find
	 * TODO: check that they are the larger biclusters
	 * @param outFile - file to write down the results
	 * @param description - description about the biclustering to add to header of the file
	 * @return The name of the file where the results are stored
	 */
	public String bimax(boolean percentage, double threshold, boolean under, int minr, int minc, int maxNumber, String outFile, String description)
		{
		if(r==null)	{ System.err.println("No R console"); return ""; }
		if(!matrixLoaded)	loadMatrix();
		loadRLibrary("biclust");
		
		String lowCad="TRUE";
		if(under==false)	lowCad="FALSE";
		if(percentage)	exp=r.eval("loma<- binarizeByPercentage(m,"+threshold+", error=0.1, gap=(max(m)-min(m))/1000, low="+lowCad+")");
		else			exp=r.eval("loma<- binarize(m,"+threshold+", low="+lowCad+")");
		exp=r.eval("res <- biclust(x=loma, method=BCBimax(), minr="+minr+", minc="+minc+", number="+maxNumber+")");
		exp=r.eval("res@Number");
		if(exp==null)
			{
			System.err.println("Bimax did not run correctly");
			return "";
			}
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="bimax"+(int)(100000*Math.random())+".tmp"; 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				if(percentage)	outFile=outFile.concat("bimax_mr"+minr+"_mc"+minc+"_mb"+maxNumber+"_perc"+threshold);
				else			outFile=outFile.concat("bimax_mr"+minr+"_mc"+minc+"_mb"+maxNumber+"_value"+threshold);
				outFile=outFile.replace(".", "-");
				outFile=outFile.concat(".bic");
				}
			
			}
		if(description.length()==0)
			{
			description="Bimax";
			}
		exp=r.eval("res2=convertclust(res)");
		exp=r.eval("length(res2)");
		filter();
		exp=r.eval("length(res2)");
		//TODO: if length>0
		exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		exp=r.eval("rm(loma)");
		exp=r.eval("rm(res)");
		exp=r.eval("rm(res2)");
		return outFile;
		}
	
	
	
	
	/**
	 * Filters the bicluster results by using the current filter options.
	 */
	public void filter()
		{
		if(filterOptions==null || filterOptions.length!=4)	return;
		exp=r.eval("length(res2)");
		exp=r.eval("res2=filterclust(res2, overlapThreshold="+filterOptions[0]+", maxNumber="+filterOptions[1]+", maxRows="+filterOptions[3]+", maxCols="+filterOptions[2]+")");
		exp=r.eval("length(res2)");
		return;
		}
	
	/**
	 * Executes Plaid Model algorithm as implemented in R package biclust
	 * @param outFile - file to write down the results
	 * @param description - description about the biclustering to add to header of the file
	 * @return the name of the file with the results
	 */
	public String plaid(String cluster, float rrel, float crel, String outFile, String description)
		{
		if(r==null)	
			{
			System.err.println("Bimax: no R console");
			return "";
			}
		if(!matrixLoaded)	loadMatrix();
		loadRLibrary("biclust");
		
		exp=r.eval("res <- biclust(x=m, method=BCPlaid(), cluster=\""+cluster+"\", row.release="+rrel+", col.release="+crel+")");
		exp=r.eval("res@Number");
		if(exp==null)
			{
			System.err.println("Plaid model algorithm did not run correctly");
			return null;
			}
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="plaid"+(int)(100000*Math.random())+".tmp";//TODO: erase when exiting 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.concat("plaid");
				outFile=outFile.replace(".", "-");
				outFile=outFile.concat(".bic");
				}
			
			}
		if(description.length()==0)
			{
			description="Plaid";
			}

		exp=r.eval("res2=convertclust(res)");
		filter();
		exp=r.eval("length(res2)");
		exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		//exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", rownames(m), colnames(m))");
		exp=r.eval("rm(res)");
		exp=r.eval("rm(res2)");
		return outFile;
		}
	
	

	/**
	 * Executes ISA algorithm as implemented in R package isa2
	 * ISA takes random gene profiles and starts computing the similarity of other gene profiles, similarly to other algorithms like Cheng and Church
	 * @param rowThreshold - threshold for rows. NOTE that this is not the threshold used as parameter of isa() but in a postfiltering of biclusters
	 * @param colThershold - threshold for columns. NOTE that this is not the threshold used as parameter of isa() but in a postfiltering of biclusters
	 * @param numSeeds - number of initial seeds. They are chosen randomly and one bicluster is generated for each
	 * @param outFile - file to write down the results
	 * @param description - description about the biclustering to add to header of the file
	 * @return the name of the file with the results
	 */
	public String isa2(float rowThreshold, float colThreshold, int numSeeds, String outFile, String description)
		{
		if(r==null)	
			{
			System.err.println("No R console");
			return "";
			}
		if(!matrixLoaded)	loadMatrix();
		loadRLibrary("isa2");
		
		//exp=r.eval("res <- isa(m, thr.row="+rowThreshold+", thr.col="+colThreshold+", no.seeds="+numSeeds+")");
		exp=r.eval("res <- isa(m, no.seeds="+numSeeds+")");
		exp=r.eval("res$columns");
		if(exp==null)
			{
			System.err.println("ISA algorithm did not run correctly");
			return null;
			}
		exp=r.eval("dim(res$columns)[2]");
		if(exp.asInt()==0)
			{
			System.err.println("ISA algorithm did not find any bicluster");
			return null;
			}
			
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="isa"+(int)(100000*Math.random())+".tmp";//TODO: erase when exiting 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.concat("isa");
				outFile=outFile.replace(".", "-");
				outFile=outFile.concat(".bic");
				}
			
			}
		if(description.length()==0)
			{
			description="ISA";
			}

//		exp=r.eval("res2=convertISAclust(res)");
		exp=r.eval("res2=convertISAclust(res, row.thr="+rowThreshold+", col.thr="+colThreshold+")");
		filter();
		exp=r.eval("length(res2)");
		exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		exp=r.eval("rm(res)");
		exp=r.eval("rm(res2)");
		return outFile;
		}

	/**
	 * Executes xMotifs algorithm as implemented in R package biclust
	 * @param disc Number of levels to use to discretize the gene expression matrix
	 * @param quantiles if true, levels are computed by means of quantiles, otherwise by their raw expression levels
	 * @param ns see biclust xMotifs help
	 * @param nd see biclust xMotifs help
	 * @param sd see biclust xMotifs help
	 * @param alpha see biclust xMotifs help
	 * @param number see biclust xMotifs help
	 * @param outFile - file to write down the results
	 * @param description - description about the biclustering to add to header of the file
	 * @return the name of the file with the results
	 */
	public String xmotifs(int disc, boolean quantiles, int ns, int nd, int sd, double alpha, int number, String outFile, String description)
		{
		if(r==null)	{ System.err.println("No R console"); return ""; }
		if(!matrixLoaded)	loadMatrix();
		loadRLibrary("biclust");
		
		
		String boolCad="TRUE";
		if(quantiles==false)	boolCad="FALSE";
		
		exp=r.eval("dima=discretize(x=m, nof="+disc+", quant="+boolCad+")");
		exp=r.eval("res <- biclust(x=dima, method=BCXmotifs(), ns="+ns+", nd="+nd+", sd="+sd+", alpha="+alpha+", number="+number+")");
		exp=r.eval("res@Number");
		if(exp==null)
			{
			System.err.println("Xmotifs biclustering did not run correctly");
			return null;
			}
		
		if(outFile.length()==0)	//tempfile
		{
		outFile="xmotifs"+(int)(100000*Math.random())+".tmp";//TODO: erase when exiting 
		}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.concat("xmotifs");
				outFile=outFile.replace(".", "-");
				outFile=outFile.concat(".bic");
				}
			
			}
		if(description.length()==0)
			{
			description="XMotifs";
			}
	
		exp=r.eval("res2=convertclust(res)");
		filter();
		exp=r.eval("length(res2)");
		//exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", rownames(m), colnames(m))");
		exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		exp=r.eval("rm(dima)");
		exp=r.eval("rm(res)");
		exp=r.eval("rm(res2)");
		return outFile;
		}
	
	/**
	 * Executes Cheng and Church algorithm as implemented in R package biclust
	 * @param delta see biclust xMotifs help
	 * @param alpha see biclust xMotifs help
	 * @param number maximum number of biclusters to find
	 * @param outFile - file to write down the results
	 * @param description - description about the biclustering to add to header of the file
	 * @return the name of the file with the results
	 */
	public String chengChurch(float delta, float alpha, int number, String outFile, String description)
	{
		if(r==null)	{ System.err.println("No R console"); return ""; }
		if(!matrixLoaded)	loadMatrix();
		loadRLibrary("biclust");
		
		

	exp=r.eval("res <- biclust(x=m, method=BCCC(), delta="+delta+", alpha="+alpha+", number="+number+")");
	exp=r.eval("res@Number");
	if(exp==null)
		{
		System.err.println("Cheng and Church biclustering did not run correctly");
		return null;
		}
	
	if(outFile.length()==0)	//tempfile
	{
	outFile="cc"+(int)(100000*Math.random())+".tmp";//TODO: erase when exiting 
	}
	else
		{
		if(!outFile.contains("."))	//automatic name
			{
			outFile=outFile.replace("\\","/");
			if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
				outFile=outFile.concat("/");
			outFile=outFile.concat("chengchurch");
			outFile=outFile.replace(".", "-");
			outFile=outFile.concat(".bic");
			}
		
		}
	if(description.length()==0)
		{
		description="ChengChurch";
		}


	exp=r.eval("res2=convertclust(res)");
	filter();
	exp=r.eval("length(res2)");
	//exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", rownames(m), colnames(m))");
	exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
	exp=r.eval("rm(res)");
	exp=r.eval("rm(res2)");
	return outFile;
	}
	
	/**
	 * Returns the top threshold ids that have a distance lower than the threshold respect to the gene passed as argument
	 * @param maxDistance
	 * @param gene
	 * @return
	 */
	//public ArrayList<Integer> getSimilarProfiles(double threshold, int gene)
	public String[] getSimilarProfiles(int threshold, String gene)
		{
		if(r==null)		{ System.err.println("no R console"); return null; }
		if(microarrayData==null)		{ System.err.println("no microarray data"); return null; }
		
		//ArrayList<Integer> neighbors=new ArrayList<Integer>();
		//exp=r.eval("d=dist(m[c("+gene+",genes),])[1:length(genes)]");
		//exp=r.eval("genes[order(d)]");
		r.eval("d=sapply(rownames(m), function(x){      sqrt(sum((m[x,]-m[\""+gene+"\",])*(m[x,]-m[\""+gene+"\",])))     })");
		//exp=r.eval("rownames(m)[order(d)][2:"+(threshold+1)+"]");//the first one is actually itself
		exp=r.eval("rownames(m)[order(d)][1:"+(threshold+1)+"]");//the first one is actually itself
		return(exp.asStringArray());
		}

	public void downloadExperiment(String id, String path) 
		{
		loadRLibrary("ArrayExpress");
		loadRLibrary("affy");
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/downloadAndNormalize.R\")");
		System.out.println("Calling download and normalize");
		String fileName=path.substring(path.lastIndexOf("/")+1);
		path=path.substring(0, path.lastIndexOf("/"));
		exp=r.eval("downloadAndNormalize(experimentID=\""+id+"\", path=\""+path+"\", fileName=\""+fileName+"\")");
		if(exp==null)
			System.err.println("Error, cannot download and normalize experiment");
		else
			System.out.println("Experiment downloaded and normalized");
		}
	/**
	 * Performs differential expression analysis via limma
	 * TODO: make it iterative so we can do every single group/combination upon the selection
	 * @param group1 - first group of samples
	 * @param group2 - second group of samples
	 * @param bh - if true, Benjamini and Hochberg correction to p-values is computed
	 * @param pvalue - threshold for statistical significance
	 * @param elevel - threshold for differential expression (log10)
	 * @param reg - regulation (can be up, down or all)
	 */
    public String limma(Integer[] group1, Integer[] group2, boolean bh, double pvalue, double elevel, String reg, String outFile, String description)
    	{
    	if(!matrixLoaded)	loadMatrix();
		loadRLibrary("limma");
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/difAnalysis.R\")");
		for(int i=0;i<group1.length;i++)  group1[i]=group1[i]+1;
		for(int i=0;i<group2.length;i++)  group2[i]=group2[i]+1;
		exp=r.eval("degs=diffAnalysis(m, "+RUtils.getRList(group1)+", nameG1=\"Group 1\", "+RUtils.getRList(group2)+", nameG2=\"Group 2\", " +
				"interestingNames=c(), pvalT="+pvalue+", diffT="+elevel+", byRank=FALSE, " +
						"numRank=50, BH.correct="+bh+", print=FALSE, return =\""+reg+"\")");
		if(exp==null)
			{System.out.println("Error, cannot perform differential expression analysis"); return null;}
		exp=r.eval("lr=list(rownames(m)[degs])");
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="limma"+(int)(100000*Math.random())+".tmp"; 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.replace(".", "-");
				outFile=outFile.concat(".bic");
				}
			}
		
		exp=r.eval("writeBiclusterResultsFromList(\""+outFile+"\", lr, NA, bicNames=c(\"DEGs\"), biclusteringDescription=\"DEGs found with limma via BicOverlapper\")");
		return outFile;
		}
    
    /**
	 * Like limma, but in this case the differential expression analysis is performed for every
	 * Experimental factor value on a experimental factor against a given factor value
	 * @param ef - experimental factor
	 * @param efv - experimental factor value for differential expression against the rest
	 * @param bh - if true, Benjamini and Hochberg correction to p-values is computed
	 * @param pvalue - threshold for statistical significance
	 * @param elevel - threshold for differential expression (log10)
	 * @param reg - regulation (can be up, down or all)
	 */
    public String limmaEF(String ef, String efv, boolean bh, double pvalue, double elevel, String reg, String outFile, String description)
    	{
    	if(!matrixLoaded)	loadMatrix();
		loadRLibrary("limma");
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/writeBiclusterResults.r\")");
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/difAnalysis.R\")");
		System.out.println("degs=diffAnalysisEF(m, ef="+RUtils.getRList(microarrayData.getExperimentFactorValues(ef))+", efv=\""+efv+"\", " +
				"interestingNames=c(), pvalT="+pvalue+", diffT="+elevel+", byRank=FALSE, " +
				"numRank=50, BH.correct="+bh+", print=FALSE, return =\""+reg+"\")");
		exp=r.eval("degs=diffAnalysisEF(m, ef="+RUtils.getRList(microarrayData.getExperimentFactorValues(ef))+", efv=\""+efv+"\", " +
				"interestingNames=c(), pvalT="+pvalue+", diffT="+elevel+", byRank=FALSE, " +
				"numRank=50, BH.correct="+bh+", print=FALSE, return =\""+reg+"\")");
		if(exp==null)
			{System.out.println("Error, cannot perform differential expression analysis"); return null;}
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="limma"+(int)(100000*Math.random())+".tmp"; 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.concat(".bic");
				}
			}
		
		exp=r.eval("writeBiclusterResultsFromList(\""+outFile+"\", degs, NA, bicNames=names(degs), biclusteringDescription=\"DEGs found with limma via BicOverlapper\")");
		return outFile;
		}
    /**
	 * Like limmaEF, but in this case the differential expression analysis is performed for every
	 * possible combination of experimental factor values of the experimental factor passed as argument
	 * @param ef - experimental factor
	 * @param bh - if true, Benjamini and Hochberg correction to p-values is computed
	 * @param pvalue - threshold for statistical significance
	 * @param elevel - threshold for differential expression (log10)
	 * @param reg - regulation (can be up, down or all)
	 */
    public String limmaEFall(String ef, boolean bh, double pvalue, double elevel, String reg, String outFile, String description)
    	{
    	if(!matrixLoaded)	loadMatrix();
		loadRLibrary("limma");
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/writeBiclusterResults.r\")");
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/difAnalysis.R\")");
		System.out.println("degs=diffAnalysisEFall(m, ef="+RUtils.getRList(microarrayData.getExperimentFactorValues(ef))+", " +
				"interestingNames=c(), pvalT="+pvalue+", diffT="+elevel+", byRank=FALSE, " +
				"numRank=50, BH.correct="+bh+", print=FALSE, return =\""+reg+"\")");
		exp=r.eval("degs=diffAnalysisEFall(m, ef="+RUtils.getRList(microarrayData.getExperimentFactorValues(ef))+", " +
				"interestingNames=c(), pvalT="+pvalue+", diffT="+elevel+", byRank=FALSE, " +
				"numRank=50, BH.correct="+bh+", print=FALSE, return =\""+reg+"\")");
		if(exp==null)
			{System.out.println("Error, cannot perform differential expression analysis"); return null;}
		//exp=r.eval("lr=list(rownames(m)[degs])");
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="limma"+(int)(100000*Math.random())+".tmp"; 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.concat(".bic");
				}
			}
		String status="";
		if(reg.equals("up"))	status="over-";
		if(reg.equals("down"))	status="under-";
		String desc="Differentially "+status+"expressed genes found with limma for every combination of EFVs on "+ef+" (dexp="+elevel+", p-val=10e-"+pvalue;
		if(!bh) desc+=")";
		else	desc+=" (BH corrected))"; 
		if(description!=null && description.length()>0)	desc=description;
		exp=r.eval("writeBiclusterResultsFromList(\""+outFile+"\", degs$degs, NA, bicNames=degs$names, biclusteringDescription=\""+desc+"\")");
		return outFile;
		}
    

    /**
	 * Like limmaEFall, but now we perform  over every combination of EFVs FOR EACH EF on the microarray
	 * @param bh - if true, Benjamini and Hochberg correction to p-values is computed
	 * @param pvalue - threshold for statistical significance
	 * @param elevel - threshold for differential expression (log10)
	 * @param reg - regulation (can be up, down or all)
	 */
    public String limmaAll(boolean bh, double pvalue, double elevel, String reg, String outFile, String description)
    	{
    	if(!matrixLoaded)	loadMatrix();
		loadRLibrary("limma");

		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/writeBiclusterResults.r\")");
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/difAnalysis.R\")");
		exp=r.eval("efs=list()");
		for(int i=0;i<microarrayData.experimentFactors.size();i++)
			exp=r.eval("efs=c(efs, list("+RUtils.getRList(microarrayData.experimentFactorValues.get(microarrayData.experimentFactors.get(i)))+"))");
		
		System.out.println("degs=diffAnalysisAll(m, ef=efs, " +
				"efNames="+RUtils.getRList(microarrayData.experimentFactors.toArray(new String[0]))+", "+
				"interestingNames=c(), pvalT="+pvalue+", diffT="+elevel+", byRank=FALSE, " +
				"numRank=50, BH.correct="+bh+", print=FALSE, return =\""+reg+"\")");
		exp=r.eval("degs=diffAnalysisAll(m, ef=efs, " +
				"efNames="+RUtils.getRList(microarrayData.experimentFactors.toArray(new String[0]))+", "+
				"interestingNames=c(), pvalT="+pvalue+", diffT="+elevel+", byRank=FALSE, " +
				"numRank=50, BH.correct="+bh+", print=FALSE, return =\""+reg+"\")");
		if(exp==null)
			{System.out.println("Error, cannot perform differential expression analysis"); return null;}
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="limma"+(int)(100000*Math.random())+".tmp"; 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.concat(".bic");
				}
			}
		String status="";
		if(reg.equals("up"))	status="over-";
		if(reg.equals("down"))	status="under-";
		String[] desc=new String[microarrayData.experimentFactors.size()];
		for(int i=0;i<desc.length;i++)
			{
			desc[i]="Differentially "+status+"expressed genes found with limma for every combination of EFVs on "+microarrayData.experimentFactors.get(i)+" (dexp="+elevel+", p-val=10e-"+pvalue;
			if(!bh) desc[i]+=")";
			else	desc[i]+=" (BH corrected))"; 
			if(description!=null && description.length()>0)	desc[i]=description;
			}
		exp=r.eval("lr=lapply(degs, function(x){x$deg})");
		exp=r.eval("ln=lapply(degs, function(x){x$names})");
		exp=r.eval("writeBiclusterResultsFromListArray(\""+outFile+"\", lr, listArrayColumns=NA, listArrayNames=ln, descriptions="+RUtils.getRList(desc)+")");

		return outFile;
		}

	public String buildCorrelationNetwork(double sdThreshold, String distanceMethod,
			double distanceThreshold, String outFile) {
		if(!matrixLoaded)	loadMatrix();
		String error="";
		
		if(outFile.length()==0)	//tempfile
			{
			outFile="correlationNetwork"+(int)(100000*Math.random())+".tmp"; 
			}
		else
			{
			if(!outFile.contains("."))	//automatic name
				{
				outFile=outFile.replace("\\","/");
				if(!outFile.endsWith("\\") && !outFile.endsWith("/"))
					outFile=outFile.concat("/");
				outFile=outFile.concat(".gml");
				}
			}
		
		exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/buildNetwork.R\")");
		System.out.println("err=buildCorrelationNetwork(gmlFile=\""+outFile+"\", "+
				"mat=m, distanceMethod=\""+distanceMethod+"\", deviationThreshold="+
				sdThreshold+", distanceThreshold="+distanceThreshold+")");
				
		exp=r.eval("buildCorrelationNetwork(gmlFile=\""+outFile+"\", "+
				"mat=m, distanceMethod=\""+distanceMethod+"\", deviationThreshold="+
				sdThreshold+", distanceThreshold="+distanceThreshold+")");
		
		if(exp.asString()!=null && exp.asString().startsWith("Error"))
			{
			System.err.println("buildCorrelationNetwork: "+exp.asString());
			JOptionPane.showMessageDialog(null,
	                exp.asString(),
	                "Error",JOptionPane.ERROR_MESSAGE);
			return null;
			}
		return outFile;
	}
	}
