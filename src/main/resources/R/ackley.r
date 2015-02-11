

ackley <- function(xx, a=20, b=0.2, c=2*pi)
{
  xx<-xx*32.768*2 - 32.768
  
  if (is.matrix(xx)) {
    d <- dim(xx)[2]
    
    sum1 <- rowSums(xx^2)
    sum2 <- rowSums(cos(c*xx))
    
    term1 <- -a * exp(-b*sqrt(sum1/d))
    term2 <- -exp(sum2/d)
    
    y <- term1 + term2 + a + exp(1)
    return(y)
  } else {
    d <- length(xx)
    
    sum1 <- sum(xx^2)
    sum2 <- sum(cos(c*xx))
    
    term1 <- -a * exp(-b*sqrt(sum1/d))
    term2 <- -exp(sum2/d)
    
    y <- term1 + term2 + a + exp(1)
    return(y)
  }

}

