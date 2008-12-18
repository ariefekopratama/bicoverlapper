# TODO: Add comment
# 
# Author: Rodrigo
###############################################################################

setwd("C:\\Documents and Settings\\Rodrigo\\Escritorio\\distribucion\\data\\lastfm\\distances")
names=scan("indices.txt", what="character", sep="\n")
m=read.table("simMatrix.txt", sep=",", row.names=names, col.names=names)
m2=as.matrix(m)
m2=m2+1
m2=1/m2
diag(m2)=0
m=m2
width=800
height=600
write.table(m2, "allTags.sim", quote=TRUE, sep="\t")
projCMD=cmdscale(m)

library(MASS)
projMDS=isoMDS(m)
projSammon=sammon(m)
#projShepard=Shepard(m, isoMDS(m))

oldpar=par(c("mai", "mar", "mgp", "xpd"))
par(mar=c(0,0,0,0), mai=c(0,0,0,0))
#proj=projCMD
#proj=projMDS$points
proj=projSammon$points
plot(proj, type="p", cex=0.01)
text(proj[,1], proj[,2], labels=rownames(proj), cex=0.7)

proj[,1]=proj[,1]*(width/max(proj[,1]))
proj[,2]=proj[,2]*(height/max(proj[,2]))

#write in the right format
write.table(proj, "allTags.2d", quote=FALSE, col.names=FALSE, sep=";")

