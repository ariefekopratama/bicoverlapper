# TODO: Add comment
# 
# Author: Rodrigo
###############################################################################


library(hgu95av2.db)
library(YEAST)
library(GO.db)
library(annotate)

#YEAST()
#hgu95av2()

yeastGroup=c("YCR012W", "YGR192C", "YJR009C", "YKL060C", "YKL152C")
homoGroup=c("738_at", "40840_at", "41668_r_at")

get(yeastGroup,YEASTGENENAME)
n=names(mget(yeastGroup, YEASTGO))
all=mget(yeastGroup, YEASTGO)
names(mget(homoGroup, hgu95av2GO))

l=getGOTerms(yeastGroup, YEASTGO)
