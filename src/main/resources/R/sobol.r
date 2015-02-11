# fn: function to evaluate -- takes in a matrix of size Nxd
# xx: matrix of dimension Nxd, where N is the number of MC samples, d is the number of input variables
# zz: matrix of dim Nxd
# u: set of variable indices
lower_sobol<-function(fn, xx, zz, u, ...)
{
  zz[,u]<-xx[,u]
  N<-dim(xx)[1]
  
  fxx<-fn(xx, ...)
  fzz<-fn(zz, ...)
  
  mu_hat<-mean((fxx + fzz)/2)
  cross_moment<-mean(fxx*fzz)
  return(cross_moment - mu_hat^2)
}

upper_sobol<-function(fn, xx, zz, u, ...)
{
  zz[,-u]<-xx[,-u]
  N<-dim(xx)[1]

  fxx<-fn(xx, ...)
  fzz<-fn(zz, ...)
  
  return(sum((fxx - fzz)^2)/(2*N))
}

true_lower_sobol<-function(fn, xx, zz, u, scale_fn, ...)
{
  zz[,u]<-xx[,u]
  N<-dim(xx)[1]
  
  fxx<-apply(scale_fn(xx), 1, fn, ...)
  fzz<-apply(scale_fn(zz), 1, fn, ...)
  
  mu_hat<-mean((fxx + fzz)/2)
  cross_moment<-mean(fxx*fzz)
  return(cross_moment - mu_hat^2)
}

true_upper_sobol<-function(fn, xx, zz, u, scale_fn, ...)
{
  zz[,-u]<-xx[,-u]
  N<-dim(xx)[1]
  
  fxx<-apply(scale_fn(xx), 1, fn, ...)
  fzz<-apply(scale_fn(zz), 1, fn, ...)
    
  return(sum((fxx - fzz)^2)/(2*N))
}


mu<-function(fn, xx, ...)
{
  N<-dim(xx)[1]
  f<-foreach (i = 1:N, .combine='c') %dopar%
  {
    fn(xx[i,], ...)
  }
  return (mean(f))
}

s2<-function(fn, xx, mu_hat, ...)
{
  N<-dim(xx)[1]
  f<-foreach (i = 1:N, .combine='c') %dopar%
  {
    (fn(xx[i,], ...) - mu_hat)^2
  }
  return (sum(f)/(N-1))
}

# proposition 7.1 in Owen (2013)
bilinear_contrast<-function(fn, xx, zz, u, ...)
{
  zz[,u]<-xx[,u]
  N<-dim(xx)[1]
 
  f0<-mu(oakoh04, xx)
  f0_prime<-mu(oakoh04, zz)
  var_hat<-s2(oakoh04, xx, mu_hat = f0)
  var_hat_prime<-s2(oakoh04, zz, mu_hat = f0_prime)
  
  fxz<-foreach (i = 1:N, .combine='c') %dopar%
  {
    fx<-fn(xx[i,], ...)
    fz<-fn(zz[i,], ...)
    fx*fz
  }
  
  ret<-mean(fxz) - ((f0 + f0_prime)/2)^2 + (var_hat + var_hat_prime)/(4*N)
  ret<-ret*2*N/(2*N - 1)
  return(ret)
}

