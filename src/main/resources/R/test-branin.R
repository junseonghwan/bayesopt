setwd("Dropbox/Research//Bayesian-optimization-Sobol//R")

library(DiceKriging)
library(DiceOptim)
library(lhs)

rm(list=ls())
source("brainin.r")
source("sobol.r")

ff<-branin
kriging_mean<-function(xnew, m)
{
  predict.km(m, xnew, type="UK", checkNames=F)$mean
}

# generate points to evaluate using LHD
set.seed(123)
d<-2
n<-9
dp<-randomLHS(n, d)

y<-ff(dp)
ff(dp[1,])
y[1]

# now fit the GP
kmfit<-km(design = dp, response = y, covtype = "gauss")

lower<-c(0, 0)
upper<-c(1, 1)
niter<-100
lsobol<-matrix(0, nrow = niter, ncol = d, niter)
usobol<-matrix(0, nrow = niter, ncol = d, niter)
N<-10000
EIs<-rep(0, niter)
t1 <- proc.time()
for (i in 1:niter)
{
  print(paste("iteration ", i))
  
  # compute the Sobol' indices
  for (u in 1:d)
  {
    xx<-cbind(runif(N, 0, 1), runif(N, 0, 1))
    zz<-cbind(runif(N, 0, 1), runif(N, 0, 1))
    
    lsobol[i,u]<-lower_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
    usobol[i,u]<-upper_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
  }

  ego<-max_EI(kmfit, type="UK", lower = lower, upper = upper)
  EIs[i]<-ego$value
  # evaluate the function
  newx<-ego$par
  newy<-ff(newx)
  dp<-rbind(dp, newx)
  y<-c(y, newy)
  
  if (ego$value == 0)
    break

  # fit GP again (not efficient but will do for now)
  kmfit<-km(design = dp, response = y, covtype = "gauss", nugget = 1e-8 * var(y))
}
t2 <- proc.time()
(t2 - t1)[3]

plot(1:i, EIs[1:i], type='l')

# let's see if the minimum is found
min(ff(dp))
dp[which.min(ff(dp)),]
# this is one of the solutions provided here: http://www.sfu.ca/~ssurjano/branin.html
# so BO works, now how about Sobol' indices?

lsobol<-lsobol[1:i, ] # why some rows are 0?
usobol<-usobol[1:i, ]
lsobol
usobol

diff(lsobol)/lsobol[-i,]

# compute Sobol indices using branin function directly and compare to what we get
true_lsobol<-rep(0, d)
true_usobol<-rep(0, d)
est_lsobol<-rep(0, d)
est_usobol<-rep(0, d)
N<-10000
for (u in 1:d)
{
  xx<-cbind(runif(N, 0, 1), runif(N, 0, 1))
  zz<-cbind(runif(N, 0, 1), runif(N, 0, 1))
  
  true_lsobol[u]<-lower_sobol(ff, xx = xx, zz = zz, u = u)
  true_usobol[u]<-upper_sobol(ff, xx = xx, zz = zz, u = u)
  est_lsobol[u]<-lower_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
  est_usobol[u]<-upper_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
}

est_lsobol
true_lsobol

