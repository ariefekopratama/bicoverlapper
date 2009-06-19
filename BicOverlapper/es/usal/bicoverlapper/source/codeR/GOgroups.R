# TODO: Add comment
# 
# Author: Rodrigo
###############################################################################

#Returns a goterms object with the attributes:
#ids: GOIDs annotated for the specified group of genes in the specified database
#evidences: number of times that the GOID is found in the specified group of genes. Note that a gene can
#			be annotated more than once with the same GOID, if it comes from different evidences
#			(for example, inferred from electronic annotation and inferred from mutant phenotype)
#terms: GO term
#ontologies: GO ontology in which the term is in
#definitions: natural text with a definition of the term
getGOTerms=function(group, database)
	{
	setClass("goterms", representation(ids="character", evidences="integer", terms="character", ontologies="character", definitions="character"))
	terms=c()
	terms=sapply(group, function(x){
			names(get(x, database))	
			})
	terms=as.character(unlist(terms))
	terms=rle(sort(terms))
	goterms=new("goterms")
	goterms@ids=terms$values
	goterms@evidences=terms$lengths
	desc=sapply(goterms@ids, function(x){get(x, GOTERM)})
	goterms@terms=sapply(desc, function(x){Term(x)})
	goterms@ontologies=sapply(desc, function(x){Ontology(x)})
	goterms@definitions=sapply(desc, function(x){Definition(x)})
	goterms
	}
	
#Returns a goterms object from GOIDs with the attributes:
#ids: GOIDs annotated for the specified group of genes in the specified database
#evidences: number of times that the GOID is found in the specified group of genes. Note that a gene can
#			be annotated more than once with the same GOID, if it comes from different evidences
#			(for example, inferred from electronic annotation and inferred from mutant phenotype)
#terms: GO term
#ontologies: GO ontology in which the term is in
#definitions: natural text with a definition of the term
getGOTermsByGOID=function(goids)
	{
	setClass("goterms", representation(ids="character", evidences="integer", terms="character", ontologies="character", definitions="character"))
	goterms=new("goterms")
	
	goterms@ids=goids
	#goterms@evidences=rep(1, length(goids))
	desc=mget(goterms@ids, GOTERM)
	goterms@terms=sapply(desc, function(x){Term(x)})
	goterms@ontologies=sapply(desc, function(x){Ontology(x)})
	goterms@definitions=sapply(desc, function(x){Definition(x)})
	goterms
	}

	
	
