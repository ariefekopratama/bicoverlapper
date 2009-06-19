# Write bicluster results to a text file
# fileName - path to the file were biclusters are written
# bicResult - biclsuters results as a bicluster class
# geneNames - array with strings with gene Names
# arrayNames - array with string with condition names
# append - if true, adds the bicluster results to previous information in the text file, if this exists. Default false.
# delimiter - delimiter string between gene and condition names. Default " ".
writeBiclusterResults=function(fileName, bicResult, bicName, geneNames, arrayNames, append=FALSE, delimiter=" ")
  {
  write(length(bicResult), file=fileName, append=append)
  write(bicName, file=fileName, append=TRUE)
  for(i in bicResult)
    {
    write(c(length(i@rows), length(i@cols)), file=fileName, ncolumns=2, append=TRUE, sep =delimiter)
    write(geneNames[i@rows], file=fileName, ncolumns=length(i@rows), append=TRUE, sep =delimiter)
    write(arrayNames[i@cols], file=fileName, ncolumns=length(i@cols), append=TRUE, sep =delimiter)
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