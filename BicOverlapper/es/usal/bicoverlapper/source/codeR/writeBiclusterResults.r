# Write bicluster results to a text file
# fileName - path to the file were biclusters are written
# bicResult - biclsuters results as a bicluster class
# geneNames - array with strings with gene Names
# arrayNames - array with string with condition names
# append - if true, adds the bicluster results to previous information in the text file, if this exists. Default false.
# delimiter - delimiter string between gene and condition names. Default " ".
writeBiclusterResults=function(fileName, bicResult, bicDesc, bicNames, geneNames, arrayNames, append=FALSE, delimiter="\t")
  {
  write(length(bicResult), file=fileName, append=append)
  write(bicDesc, file=fileName, append=TRUE)
  c=1
  for(i in bicResult)
    {
		print(paste(c, bicNames[c]))
    write(c(paste(bicNames[c],length(i@rows), sep=":"), length(i@cols)), file=fileName, ncolumns=2, append=TRUE, sep =delimiter)
    write(geneNames[i@rows], file=fileName, ncolumns=length(i@rows), append=TRUE, sep =delimiter)
    write(arrayNames[i@cols], file=fileName, ncolumns=length(i@cols), append=TRUE, sep =delimiter)
	c=c+1
    }
  }
  
# Write bicluster results to a text file
# fileName - path to the file were biclusters are written
# bicResult - biclsuters results as a bicluster class
# geneNames - array with strings with gene Names
# arrayNames - array with string with condition names
# append - if true, adds the bicluster results to previous information in the text file, if this exists. Default false.
# delimiter - delimiter string between gene and condition names. Default " ".
writeBiclusterResults2=function(fileName, bicResult, bicName, geneNames, arrayNames, append=FALSE, delimiter=" ")
  {
  write(bicResult@Number, file = fileName, append = append)
  write(bicName, file = fileName, append = TRUE)
  for (i in 1:bicResult@Number) {
	  listar = row(matrix(bicResult@RowxNumber[, i]))[bicResult@RowxNumber[,i] == T]
	  listac = row(matrix(bicResult@NumberxCol[i, ]))[bicResult@NumberxCol[i,] == T]
	  write(c(length(listar), length(listac)), file = fileName, ncolumns = 2, append = TRUE, sep = delimiter)
	  write(geneNames[listar], file = fileName, ncolumns = length(listar), append = TRUE, sep = delimiter)
	  write(arrayNames[listac], file = fileName, ncolumns = length(listac), append = TRUE, sep = delimiter)
	  }
  }

  
# Write bicluster results to a text file from a list of rows and colums on each
# fileName - path to the file were biclusters are written
# listRows - list of rows for the biclusters
# listColumns - list of columns for the biclusters
# bicNames - names of the biclusters, if any
# biclusteringDescription - description of the biclustering
# append - if true, adds the bicluster results to previous information in the text file, if this exists. Default false.
# delimiter - delimiter string between gene and condition names. Default " ".
writeBiclusterResultsFromList=function(fileName, listRows, listColumns=NA, bicNames=NA, biclusteringDescription="Biclusters", append=FALSE, delimiter="\t")
  {
	  if(!is.na(listColumns) && length(listRows)!=length(listColumns))
		  stop("Number of groups must be the same for columns and rows")
	  
	  else if(!is.na(bicNames) && length(listRows)!=length(bicNames))
	      stop("The number of bicluster names must be equal to the number of biclusters, or NA")
	  
	  write(length(listRows), file = fileName, append = append)
	  write(biclusteringDescription, file = fileName, append = TRUE)
	  for (i in 1:length(listRows)) {
		  listar = listRows[[i]]
		  cat(listar, "\n")
		  if(!is.na(listColumns))
			  {
			  listac = listColumns[[i]]
			  if(!is.na(listar) && !is.na(listac))
			  	{
				  if(is.na(bicNames))	write(c(length(listar), length(listac)), file = fileName, ncolumns = 2, append = TRUE, sep = delimiter)
				  else					write(c(paste(bicNames[[i]], ":", length(listar)), length(listac)), file = fileName, ncolumns = 2, append = TRUE, sep = delimiter)
				  write(listar, file = fileName, ncolumns = length(listar), append = TRUE, sep = delimiter)
				  write(listac, file = fileName, ncolumns = length(listac), append = TRUE, sep = delimiter)
			  	}
			 }
		  else
		  	{
			if(length(listar)>0 && !is.na(listar))
				{
					listac=c()
					if(is.na(bicNames))	write(c(length(listar), length(listac)), file = fileName, ncolumns = 2, append = TRUE, sep = delimiter)
					else					write(c(paste(bicNames[[i]], ":", length(listar)), length(listac)), file = fileName, ncolumns = 2, append = TRUE, sep = delimiter)
					write(listar, file = fileName, ncolumns = length(listar), append = TRUE, sep = delimiter)
					write(listac, file = fileName, append = TRUE, sep = delimiter)
				}
			}
	  }
  }