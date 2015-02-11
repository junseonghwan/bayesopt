# xx contains bunch of Unif(0, 1) variables so need to convert to [-5, 10]x[0, 15]
branin <- function(xx, a=1, b=5.1/(4*pi^2), c=5/pi, r=6, s=10, t=1/(8*pi))
{
  if (is.matrix(xx)) {
    x1<-xx[,1]*15 - 5
    x2<-xx[,2]*15
  } else {
    x1<-xx[1]*15 - 5
    x2<-xx[2]*15
  }
  
  term1 <- a * (x2 - b*x1^2 + c*x1 - r)^2
  term2 <- s*(1-t)*cos(x1)
  
  y <- term1 + term2 + s
  return(y)
}

