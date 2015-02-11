setwd("Dropbox/Research//Bayesian-optimization-Sobol//R")

library(DiceKriging)
library(DiceOptim)
library(lhs)

rm(list=ls())
source("ackley.r")
source("sobol.r")

ff<-ackley
kriging_mean<-function(xnew, m)
{
  predict.km(m, xnew, type="UK", checkNames=F)$mean
}

# generate points to evaluate using LHD
set.seed(123)
d<-3
n<-10
dp<-randomLHS(n, d)
y<-ff(dp)

ff(dp[1,])
y[1]

# now fit the GP
kmfit<-km(design = dp, response = y, covtype = "gauss", nugget=1e-8*var(y))
kmfit

lower<-rep(0, d)
upper<-rep(1, d)
niter<-200
lsobol<-matrix(0, nrow = niter, ncol = d, niter)
usobol<-matrix(0, nrow = niter, ncol = d, niter)
N<-10000
for (i in 1:niter)
{
  print(paste("iteration ", i))
  
  # compute the Sobol' indices
  for (u in 1:d)
  {
    xx<-matrix(runif(N*d), ncol = d, byrow = T)
    zz<-matrix(runif(N*d), ncol = d, byrow = T)

    lsobol[i,u]<-lower_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
    usobol[i,u]<-upper_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
  }

  ego<-max_EI(kmfit, type="UK", lower = lower, upper = upper)
  # evaluate the function
  newx<-ego$par
  newy<-ff(newx)
  dp<-rbind(dp, newx)
  y<-c(y, newy)
  
  if (ego$value == 0)
    break

  # fit GP again (not efficient but will do for now)
  kmfit<-km(design = dp, response = y, covtype = "gauss", nugget = 1e-8*var(y))
}

# let's see if the minimum is found
min(ff(dp))
dp[which.min(ff(dp)),]
# this is one of the solutions provided here: http://www.sfu.ca/~ssurjano/branin.html
# so BO works, now how about Sobol' indices?

lsobol<-lsobol[1:i, ] # why the row 14 is 0? TODO: investigate -- there might be a bug in sobol functions
usobol<-usobol[1:i, ]
lsobol
usobol

# compute Sobol indices using branin function directly and compare to what we get
true_lsobol<-rep(0, d)
true_usobol<-rep(0, d)
est_lsobol<-rep(0, d)
est_usobol<-rep(0, d)
for (u in 1:d)
{
  xx<-matrix(runif(N*d, 0, 1), ncol=d)
  zz<-matrix(runif(N*d, 0, 1), ncol=d)
  
  true_lsobol[u]<-lower_sobol(ff, xx = xx, zz = zz, u = u)
  true_usobol[u]<-upper_sobol(ff, xx = xx, zz = zz, u = u)
  est_lsobol[u]<-lower_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
  est_usobol[u]<-upper_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
}

est_lsobol
true_lsobol

# doesn't seem to work well, because the squared exponential is not a suitable function for this case
kmfit

