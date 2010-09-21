# Functions to convert different formats
# 
# Author: rodri
###############################################################################


getBMGenes=function(geneids, species="Homo sapiens", type)
{
	require(biomaRt)
	spec=c()
	specens=c()
	
	if(length(strsplit(species, " ")[[1]])==2) 	
	{
		spec=tolower(paste( substr(strsplit(species, " ")[[1]][1],0,1), substr(strsplit(species, " ")[[1]][2],0,2), sep=""))
		specens=tolower(paste( substr(strsplit(species, " ")[[1]][1],0,1), strsplit(species, " ")[[1]][2], "_gene_ensembl", sep=""))
	}
	if(length(spec)==0)	stop("Species name is wrong")
	mart = useMart("ensembl", dataset=specens)
	if(species=="Homo sapiens")
		geneens=getGene( id = geneids, type = type, mart = mart)[,c("hgnc_symbol","description","ensembl_gene_id")]
	else
		geneens=getGene( id = geneids, type = type, mart = mart)[,c("symbol","description","ensembl_gene_id")]
	geneens
}
