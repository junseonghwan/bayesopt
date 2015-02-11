source("brainin.r")
source("sobol.r")

N<-100
d<-2
x<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
f<-apply(x, 1, branin)
d<-cbind(x, f)
write.table(d, "branin-output.csv", sep=",", row.names = F, col.names = F)

lsobol<-rep(0, 2)
usobol<-rep(0, 2)
N<-1000000
xx<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
zz<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
lsobol[1]<-lower_sobol(branin, xx, zz, 1)

xx<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
zz<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
lsobol[2]<-lower_sobol(branin, xx, zz, 2)

xx<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
zz<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
usobol[1]<-upper_sobol(branin, xx, zz, 1)

xx<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
zz<-matrix(runif(N*d, 0, 1), ncol=2, byrow=T)
usobol[2]<-upper_sobol(branin, xx, zz, 2)

lsobol
usobol
