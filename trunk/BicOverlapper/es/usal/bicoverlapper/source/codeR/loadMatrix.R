# Methods to load matrices into ExpressionSets
# 
# Author: rodri
###############################################################################


#Returns a ExpressionSet from a BicOverlapper format matrix
loadMatrix=function(filePath=NULL, numEFs=0)
	{
	require(Biobase)
	require(utils)
	require(stringr)
	
	#Read file
	m=read.csv(filePath, sep="\t")
	
			
	#Parse experimental factors
	efs=c()
	efvs=list()
	if(numEFs>0)
		for(i in 1:numEFs)
		{
			row=str_trim(as.character(as.matrix(m[1,])))
			efvs=c(efvs,list(row[-1]))
			efs=c(efs,row[1])
			m=m[-1,]
		}
	
	#Parse gene names and expression values
	geneNames=gsub("[ ]+", "", as.character(m[,1]))
	ann=colnames(m)[1]
	m=m[,-1]
	m=apply(m, 2,function(x){as.numeric(x)})
	rownames(m)=geneNames
	
	#Build ExpressionSet
	df=data.frame(efvs)
	colnames(df)=paste("FactorValue.",efs,sep="")
	rownames(df)=colnames(m)
	es=new("ExpressionSet", exprs=m, annotation=ann, phenoData=new("AnnotatedDataFrame", data=df))#TODO: annotation may be wrong!
	es
	}
