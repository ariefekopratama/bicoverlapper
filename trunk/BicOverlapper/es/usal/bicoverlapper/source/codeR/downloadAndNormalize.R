########################## PIPELINE
#Pipeline with several experiments related to monocyte dcs under different environments, either fungi in the environment,
#compounds, tumours, or other cells.

# Download an experiment from ArrayExpress
# experimentID - accession name of the experiment (e.g. E-MEXP-1103, E-MTAB-135)
# raw - if TRUE, the experiment raw data are downloaded and normalized by RMA. If not, the
#       processed data submitted to ArrayExpress are returned. Default TRUE
# path - path to the file into which the experiment will be stored, in BicOverlapper format.
downloadAndNormalize=function(experimentID=NA, raw=TRUE, save=FALSE, path=NA)
	{
		###################################################
		#1) Download raw data to temp dir
		###################################################
		require(ArrayExpress)
		
		if(raw==TRUE)
		{
			ae=ArrayExpress(input = experimentID, save=save, path=path)
			
			###################################################
			#1b) EF curation
			#in case it is needed
			###################################################
			
			
			###############################################
			#2) Preprocess the experiments (by now, only one an only on serial mode)
			###############################################
			
			t0 <- proc.time()
			es = expresso(ae, 
					bgcorrect.method="rma",#TODO: Check CDF files, affymetrix ones are possibly outdated
					normalize.method = "quantiles.robust", 
					pmcorrect.method = "pmonly", summary.method = "medianpolish")
			cat("*** serial preprocessing takes " , round(proc.time()[3] - t0[3], 3), "sec\n") 
			cat("Allocated memory (MB): ",sum(sapply(ls(), function(x){object.size(get(x))}))/1000000, "\n")
		}
		
		###############################################
		#3) Generate new processed file for BicOverlapper, and if necessary (i.e. if re-curated), change the processed zip to all the sdrfs
		###############################################
		
		outName=paste(path, "/", notes(es)$accession, ".txt", sep="")
		write(c(paste(experimentData(es)@title, paste(annotation(es), "db", sep="."),sep="/"), phenoData(es)$Scan.Name), outName, ncolumns=length(sampleNames(es))+1, sep="\t")
		efs=gsub("\\.", "", gsub("Factor\\.Value\\.\\.", "", varLabels(ae)[grep("Factor\\.Value\\.\\.", varLabels(ae))]))
		efvs=lapply(grep("Factor\\.Value\\.\\.", varLabels(ae)), function(y){ae[[y]]})
		for(i in 1:length(efs))
		{
			write(c(efs[[i]], efvs[[i]]), outName, ncolumns=length(sampleNames(es))+1, sep="\t", append=TRUE)
		}
		write.table(exprs(es), file=outName, sep="\t", quote=FALSE, row.names=TRUE, col.names=FALSE, append=TRUE)
	}
	
downloadAndNormalize(experimentID="E-MEXP-328", save=FALSE, path="Z:")

                                                                                                                                                        


                                                                                                                                                        


