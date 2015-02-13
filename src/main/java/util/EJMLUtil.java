package util;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.CholeskyDecomposition;
import org.ejml.factory.DecompositionFactory;
import org.ejml.simple.SimpleMatrix;

public class EJMLUtil {

	public static SimpleMatrix rep(int d, double val) {
		SimpleMatrix vector = new SimpleMatrix(d, 1);
		for (int i = 0; i < d; i++)
		{
			vector.set(i, val);
		}
		return vector;
	}
	
	/**
	 * Compute the inverse of a symmetric, real matrix via Cholesky decomposition
	 * @param mat
	 * @return
	 */
	public static SimpleMatrix invertSymmetricMatrix(SimpleMatrix mat) {
		CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(mat.numRows(), false);

		if( !chol.decompose(mat.getMatrix()))
		{
		  throw new RuntimeException("Cholesky decomposition failed! Most likely an issue with the design points.");
		}

		SimpleMatrix Ltransposed = SimpleMatrix.wrap(chol.getT(null));
		SimpleMatrix L = Ltransposed.transpose();
		
		return Ltransposed.invert().mult(L.invert());
	}
	

}
