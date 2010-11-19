package es.usal.bicoverlapper.analysis;


import org.rosuda.JRI.REXP;

/**
 * This class performs biclustering analysis by means of R 
 * DEPRECATED by class Analysis
 * @author Rodrigo Santamaria
 *
 */
public class Biclustering
	{
	//MicroarrayData md=null;
	//public Rengine r=null;
	Analysis a;
	REXP exp=null;
	String defaultPath="";
	int[] filterOptions=null;
	
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
	public Biclustering(Analysis anew)
		{
		//this.md=md;
		a=anew;
		/*try{
			BufferedReader	pathReader=new BufferedReader(new FileReader("es/usal/bicoverlapper/data/path.txt"));
			defaultPath=pathReader.readLine();
		}catch(Exception e){e.printStackTrace();}*/
		}
	
	/**
	 * Loads the libraries in R. An R console must have been started in the sesion to do this.
	 * Right now, the library to load is "biclust"
	 * It also loads some internal r scripts that extend the biclust package.
	 */
	/*public void loadR()
		{
		if(md==null)	{System.err.println("No microarray loaded");	return;}
		if(md.re==null)	{System.err.println("No R console started");	return;}
		r=md.re;
		exp=r.eval("library(biclust)");
        if(exp==null)
        	{
        	exp=r.eval("install.packages(\"biclust\")");
        	exp=r.eval("library(biclust)");
        	if(exp==null)
    	    	{
    	    	JOptionPane.showMessageDialog(null,
					"Package biclust is not installed in R and could not be installed automatically\n Please install the package manually through the R console \nIn the meantime, Plaid, Bimax, xMotifs and Cheng&Church biclustering won't be available", 
					"Missing R package", JOptionPane.WARNING_MESSAGE);
    	    	}
        	}
       	exp=r.eval("library(isa2)");
       	if(exp==null)
		   	{
       	 	exp=r.eval("install.packages(\"isa2\")");
        	exp=r.eval("library(isa2)");
        	if(exp==null)
        		{
    	    	JOptionPane.showMessageDialog(null,
					"Package isa2 is not installed in R and could not be installed automatically\n Please install the package manually through the R console \nIn the meantime, ISA algorithm won't be available", 
					"Missing R package", JOptionPane.WARNING_MESSAGE);
		    	}
	    	return;
			}
        exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/binarize.r\")");
        exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/helpers.r\")");
        exp=r.eval("source(\"es/usal/bicoverlapper/source/codeR/writeBiclusterResults.r\")");
    	}*/
	
	/**
	 * Loads the gene expression matrix into the R session.
	 */
	/*public void loadMatrix()
		{
		if(md.matrix!=null)
			{
			int nc=md.getNumConditions();
			int ng=md.getNumGenes();
			exp=r.eval("m<-matrix(NA, "+ng+", "+nc+")");
			if(exp==null)	{System.err.println("Matrix cannot be load in R"); return;}
			for(int i=0;i<ng;i++)
				{
				String v="c(";
				for(int j=0;j<nc;j++)
					{
					if(j<nc-1)	v=v.concat(md.matrix[i][j]+", ");
					else		v=v.concat(md.matrix[i][j]+")");
			    	}
				exp=r.eval("m["+(i+1)+",]<-"+v);
				}
			String names="c(";
			for(int i=0;i<ng;i++)
				{
				if(i<ng-1)	names=names.concat("\""+md.geneNames[i]+"\", ");
				else		names=names.concat("\""+md.geneNames[i]+"\")");
				}
			exp=r.eval("rownames(m)<-"+names);
			names="c(";
			for(int i=0;i<nc;i++)
				{
				
				if(i<nc-1)	names=names.concat("\""+md.conditionNames[i]+"\", ");
				else		names=names.concat("\""+md.conditionNames[i]+"\")");
				}
			exp=r.eval("colnames(m)<-"+names);
			}
		else{System.err.println("No matrix loaded"); return;}
		}*/
	
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
		if(a==null)	
			{
			System.err.println("no R console");
			}
		String lowCad="TRUE";
		if(under==false)	lowCad="FALSE";
		if(percentage)	exp=a.r.eval("loma<- binarizeByPercentage(m,"+threshold+", error=0.1, gap=mean(m), low="+lowCad+")");
		else			exp=a.r.eval("loma<- binarize(m,"+threshold+", low="+lowCad+")");
		exp=a.r.eval("res <- biclust(x=loma, method=BCBimax(), minr="+minr+", minc="+minc+", number="+maxNumber+")");
		exp=a.r.eval("res@Number");
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
		exp=a.r.eval("res2=convertclust(res)");
		exp=a.r.eval("length(res2)");
		filter();
		exp=a.r.eval("length(res2)");
		//TODO: if length>0
		exp=a.r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		exp=a.r.eval("rm(loma)");
		exp=a.r.eval("rm(res)");
		exp=a.r.eval("rm(res2)");
		return outFile;
		}
	
	
	
	
	/**
	 * Filters the bicluster results by using the current filter options.
	 */
	public void filter()
		{
		if(filterOptions==null || filterOptions.length!=4)	return;
		exp=a.r.eval("length(res2)");
		exp=a.r.eval("res2=filterclust(res2, overlapThreshold="+filterOptions[0]+", maxNumber="+filterOptions[1]+", maxRows="+filterOptions[3]+", maxCols="+filterOptions[2]+")");
		exp=a.r.eval("length(res2)");
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
		if(a==null)	
			{
			System.err.println("Bimax: no R console");
			return "";
			}
		exp=a.r.eval("res <- biclust(x=m, method=BCPlaid(), cluster=\""+cluster+"\", row.release="+rrel+", col.release="+crel+")");
		exp=a.r.eval("res@Number");
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

		exp=a.r.eval("res2=convertclust(res)");
		filter();
		exp=a.r.eval("length(res2)");
		exp=a.r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		//exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", rownames(m), colnames(m))");
		exp=a.r.eval("rm(res)");
		exp=a.r.eval("rm(res2)");
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
		if(a==null)	
			{
				System.err.println("ISA: no R console");
				return "";
			}
		//exp=r.eval("res <- isa(m, thr.row="+rowThreshold+", thr.col="+colThreshold+", no.seeds="+numSeeds+")");
		exp=a.r.eval("res <- isa(m, no.seeds="+numSeeds+")");
		exp=a.r.eval("res$columns");
		if(exp==null)
			{
			System.err.println("ISA algorithm did not run correctly");
			return null;
			}
		exp=a.r.eval("dim(res$columns)[2]");
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
		exp=a.r.eval("res2=convertISAclust(res, row.thr="+rowThreshold+", col.thr="+colThreshold+")");
		filter();
		exp=a.r.eval("length(res2)");
		exp=a.r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		exp=a.r.eval("rm(res)");
		exp=a.r.eval("rm(res2)");
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
		if(a==null)	
			{
				System.err.println("Xmotifs: no R console");
				return "";
			}
		String boolCad="TRUE";
		if(quantiles==false)	boolCad="FALSE";
		
		exp=a.r.eval("dima=discretize(x=m, nof="+disc+", quant="+boolCad+")");
		exp=a.r.eval("res <- biclust(x=dima, method=BCXmotifs(), ns="+ns+", nd="+nd+", sd="+sd+", alpha="+alpha+", number="+number+")");
		exp=a.r.eval("res@Number");
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
	
		exp=a.r.eval("res2=convertclust(res)");
		filter();
		exp=a.r.eval("length(res2)");
		//exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", rownames(m), colnames(m))");
		exp=a.r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
		exp=a.r.eval("rm(dima)");
		exp=a.r.eval("rm(res)");
		exp=a.r.eval("rm(res2)");
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
	if(a==null)	
		{
			System.err.println("Xmotifs: no R console");
			return "";
		}

	exp=a.r.eval("res <- biclust(x=m, method=BCCC(), delta="+delta+", alpha="+alpha+", number="+number+")");
	exp=a.r.eval("res@Number");
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


	exp=a.r.eval("res2=convertclust(res)");
	filter();
	exp=a.r.eval("length(res2)");
	//exp=r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", rownames(m), colnames(m))");
	exp=a.r.eval("writeBiclusterResults(\""+outFile+"\", res2, \""+description.replace(" ", "_")+"\", paste(\"B\",+c(1:length(res2)), sep=\"\"), rownames(m), colnames(m))");
	exp=a.r.eval("rm(res)");
	exp=a.r.eval("rm(res2)");
	return outFile;
	}

	}
