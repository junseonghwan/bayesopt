setwd("Dropbox/Research//Bayesian-optimization-Sobol//R")

library(DiceKriging)
library(DiceOptim)
library(lhs)

rm(list=ls())
source("perm0db.R")
source("sobol.r")

ff<-perm0db
kriging_mean<-function(xnew, m)
{
  predict.km(m, xnew, type="UK", checkNames=F)$mean
}

scaling<-function(dp)
{
  return(dp*2*d - d)
}

# generate points to evaluate using LHD
set.seed(123)
d<-3
n<-30
dp<-scaling(randomLHS(n, d))

y<-apply(dp, 1, ff)
ff(dp[1,])
y[1]
perm0db(dp[1:2,])
ff(c(1, 1/2, 1/3))
perm0db(c(1, 1/2, 1/3))
perm0db2(c(1, 1/2, 1/3))

# now fit the GP
kmfit<-km(design = dp, response = y, covtype = "gauss")

lower<-rep(-d, d)
upper<-rep(d, d)
niter<-1000
lsobol<-matrix(0, nrow = niter, ncol = d, niter)
usobol<-matrix(0, nrow = niter, ncol = d, niter)
N<-10000
minimum<-Inf
for (i in 1:niter)
{
  # compute the Sobol' indices
  for (u in 1:d)
  {
    xx<-scaling(matrix(runif(N*d, 0, 1), ncol=d))
    zz<-scaling(matrix(runif(N*d, 0, 1), ncol=d))
    
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
  if (newy < minimum)
    minimum<-newy
  
  # fit GP again (not efficient but will do for now)
  # noticed some instability in the fitting of the GP -- some params are 0
  kmfit<-km(design = dp, response = y, covtype = "gauss", nugget = 1e-8 * sd(y))
  
  print(paste("iteration ", i, ": EGO=", ego$value, ", newy=", newy, ", curr_min=", minimum, sep=""))  
}

# let's see if the true minimum is reached
yy<-apply(dp, 1, ff)
min(yy)
# the true minimum is (1, 1/2) and f(1, 1/2) = 0
dp[which.min(yy),] # this is close but not quite the minimum... so BO sort of failed here
perm0db(dp[which.min(yy),])

lsobol<-lsobol[1:i, ]
usobol<-usobol[1:i, ]
lsobol
usobol

kmfit<-km(design = dp, response = y, covtype = "gauss", nugget = 1e-4*var(y))
kmfit

true_lsobol<-rep(0, d)
true_usobol<-rep(0, d)
est_lsobol<-rep(0, d)
est_usobol<-rep(0, d)
N<-10000
for (u in 1:d)
{
  xx<-scaling(matrix(runif(N*d, 0, 1), ncol=d))
  zz<-scaling(matrix(runif(N*d, 0, 1), ncol=d))
    
  true_lsobol[u]<-lower_sobol(ff, xx = xx, zz = zz, u = u)
  true_usobol[u]<-upper_sobol(ff, xx = xx, zz = zz, u = u)
  est_lsobol[u]<-lower_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
  est_usobol[u]<-upper_sobol(kriging_mean, xx = xx, zz = zz, u = u, m = kmfit)
}

abs(est_lsobol-true_lsobol)/true_lsobol

abs(est_usobol-true_usobol)/true_usobol


