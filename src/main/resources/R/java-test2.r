R<-matrix(scan(), ncol=10, byrow=T)
R

chol(R)

x<-scan()
R2<-exp2(x, 10)
round(R2, 3)
R
R2

max(R2-R)
chol(R2)

x<-matrix(scan(), ncol=3, byrow=T)
y<-scan()
library(DiceKriging)
?km
kmfit<-km(design = x, response = y, covtype = "gauss")
kmfit
kmfit@logLik
kmfit@covariance
1/(2*0.4178^2)
1/(2*0.3897^2)
1/(2*1.9588^2)


y<-scan()
x<-0:9/10
plot(x, y, type='l')

R<-exp2(x, 1.0)
L<-t(chol(R))
V<-scan()
sd<-sqrt(2)
mu<-1

y<-mu+sd*L%*%V
y

Rinv<-solve(R)
Rinv
mu+R[1,]%*%Rinv%*%(y-mu)
y[1]

kmfit<-km(design = as.matrix(x), response = y, covtype = "gauss", nugget=1e-8)
kmfit

1/(2*0.9807^2)

plot(x, y, type = 'l')

R<-exp2(x, 0.5)
R[1,]
solve(R)


x<-0:100/100
R<-exp2(x, 1.0)
dim(R)
R<-R + diag(1e-8, length(x))
L<-t(chol(R))
V<-rnorm(length(x), 0, 1)
sd<-sqrt(2)
mu<-1

y<-mu+sd*L%*%V
y
plot(x, y)

kmfit<-km(design = as.matrix(x), response = y, covtype = "gauss", nugget=1e-8*var(y))
kmfit

1/(2*0.6774^2) # not very close to the true value of 1
predict.km(object = kmfit, newdata = x[1], type="UK", checkNames = FALSE)$mean
y[1]

predict.km(object = kmfit, newdata = x[2], type="UK", checkNames = FALSE)$mean
y[2]

x1<-0.9553987761041387
x2<-0.13982343354746524
branin(c(x1, x2))

x<-c(0.7202847547336406, 0.8465341521145167)
branin(x)

X<-scan()
X<-matrix(X, ncol=2, byrow = T)
y<-branin(X)
kmfit<-km(design = X, response = y, covtype = "gauss")
kmfit

lower<-c(0, 0)
upper<-c(1, 1)
ego<-max_EI(kmfit, type="UK", lower = lower, upper = upper)
ego

library(DiceOptim)

kriging_mean<-function(xnew, m)
{
  predict.km(m, xnew, type="UK", checkNames=F)$mean
}

