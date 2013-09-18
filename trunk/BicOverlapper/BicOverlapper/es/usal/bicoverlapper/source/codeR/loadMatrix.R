# Methods to load matrices into ExpressionSets
# 
# Author: rodri
###############################################################################


#Returns a ExpressionSet from a BicOverlapper format matrix
#duplicates determines the procedure in case of finding duplicated rows. Either average their expression profiles ("average"),
#take the expression of the first one ("first") or rename adding (a), (b), etc. ("rename")
loadMatrix=function(filePath=NULL, numEFs=0, duplicates="average")
	{
	require(Biobase)
	require(utils)
	#require(stringr)
	
	#Read file
	m=read.csv(filePath, sep="\t", header=FALSE)#, stringsAsFactors=FALSE)
	colnames(m)=gsub("[ ]+$", "", as.character(unlist(m[1,])))
	m=m[-1,]
		
	#Parse experimental factors
	efs=c()
	efvs=list()
	if(numEFs>0)
		for(i in 1:numEFs)
		{
			row=gsub("[ ]+$", "", as.character(unlist(m[1,])))
			efvs=c(efvs,list(row[-1]))
			efs=c(efs,row[1])
			m=m[-1,]
		}
		
	#Parse gene names and expression values
	geneNames=gsub("[ ]+", "", as.character(m[,1]))
	ann=gsub("^.*/", "", colnames(m)[1])
	
	m=m[,-1]
		
	m=apply(m, 2,function(x){as.numeric(x)})
	rownames(m)=geneNames
	
	#NEW: produce average on unique rows?
	if(duplicates=="average")
		m=t(sapply(unique(geneNames), function(x)
				{
				colMeans(m[which(rownames(m)==x),,drop=F])	
				}))
	else if(duplicates=="remove")
		{
		m=m[-which(geneNames %in% geneNames[which(duplicated(geneNames))]),]	
		}
	else if(duplicates=="rename")
		{
		geneNames2=(sapply(1:length(geneNames), function(x)
						{
						id=geneNames[x]
						ids=which(rownames(m)==id)
						if(length(ids)==1)		id
						else	
							paste(id, paste(paste("(",which(ids==x),sep=""),")",sep=""))	
						}))
		rownames(m)=geneNames2
		}
	else if(duplicates=="first")
		{
		m=m[-which(duplicated(geneNames)),]	
		}
			
	#Build ExpressionSet
	if(is.null(efs)==FALSE)	
		{
		df=data.frame(efvs)
		colnames(df)=paste("FactorValue.",efs,sep="")
		rownames(df)=colnames(m)
		es=new("ExpressionSet", exprs=m, annotation=ann, phenoData=new("AnnotatedDataFrame", data=df))
		}
	else
		{
		es=new("ExpressionSet", exprs=m, annotation=ann)
		}
	es
	}

	
#	saveMatrix=function(es=NA, fileName=NA, efs=NA)
#		{
#		require(MASS)
#		towrite=cbind(featureNames(es), exprs(es))
#		towrite=rbind(c("Time", as.character(pData(es)[, "FactorValue.Time"])), towrite)towrite=rbind(c("Experiment", exps), towrite)
#		write.matrix(towrite, file=fileName, sep="\t")
#		}
	