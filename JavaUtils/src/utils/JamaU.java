package utils;

import java.text.DecimalFormat;

import javax.vecmath.Point3d;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class JamaU {
	
	/** Decimal formating */
	static DecimalFormat df5_3 = new DecimalFormat( "00.000" );
	static DecimalFormat df3_2 = new DecimalFormat( "0.00" );
	
	/** MachineEpsilon */
	static public double MACH_EPSILON = 2.220446049250313E-16;
	
	/** 
	 * Update MACH_EPSILON
	 */
	static public void updateMachEpsilon() {
		double machEps = 1.0;
		 
        do
           machEps /= 2.0f;
        while ((double) (1.0 + (machEps / 2.0)) != 1.0);
 
        MACH_EPSILON = machEps;
	}
	
	/** 
	 * Compute the dot product. Suppose that Matrix are vector (column or row)
	 * and of the same dimension.
	 * @return sum(m1.*m2)
	 */
	static public double dotP( Matrix m1, Matrix m2 ) {
		double scalarProduct = 0;
		if (m1.getColumnDimension() == 1) {
			for (int row = 0; row < m1.getRowDimension(); row++) {
				scalarProduct += m1.get(row,  0) * m2.get(row, 0);
			}
		}
		else {
			for (int col = 0; col < m1.getColumnDimension(); col++) {
				scalarProduct += m1.get(0, col) * m2.get(0, col);
			}
		}
		return scalarProduct;
	}
	
	/**
	 * Check if all elements of Matrix are nearly equal to 0, 
	 * with a threshold of JamaU.MACH_EPSILON 
	 * @param m Matrix to test
	 * @return true if every abs(m(i,j)) <= JamaU.MACH_EPSILON
	 */
	static public boolean isNearZero( Matrix m ) {
		for (int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				if (Math.abs(m.get(i, j)) > JamaU.MACH_EPSILON) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Check if all elements of Matrix are nearly equal to 0, 
	 * with a threshold of epsilon 
	 * @param m Matrix to test
	 * @param epsilon threshold
	 * @return true if every abs(m(i,j)) <= epsilon
	 */
	static public boolean isNearZero( Matrix m, double epsilon ) {
		for (int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				if (Math.abs(m.get(i, j)) > epsilon) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compute the pseudo inverse of a Matrix.
	 * 
	 * if USV* is the SingularValueDecomposition of M, then the pseudo inverse
	 * is V(S+)U* where S+ is the pseudo inverse of S. S being diagonal, its pseudo
	 * inverse is computed by taking the inverse of the element of its diagonal, 
	 * except for elements smaller that MACH_EPSI*max_size(m)*max(S)
	 * (see http://en.wikipedia.org/wiki/Moore–Penrose_pseudoinverse#Construction)
	 * 
	 * @param m Matrix
	 * @return its pseudo inverse (m+)
	 */
	static public Matrix pinv( Matrix m) {
		SingularValueDecomposition svdM = m.svd();
		
		Matrix S = svdM.getS();
		Matrix U = svdM.getU();
		Matrix V = svdM.getV();
//		System.out.println("U=\n"+JamaU.matToString(U));
//		System.out.println("S=\n"+JamaU.matToString(S));
//		System.out.println("V=\n"+JamaU.matToString(V));
		
		// Compute inverse of a diagonal matrix : inverse element of diagonal.
		// Only elements greater than MACH_EPSI*max_size(m)*max(S) are inversed
		double theta = MACH_EPSILON * Math.max(S.getColumnDimension(),S.getRowDimension());
		double maxS = -Double.MAX_VALUE;
		for (int row = 0; row < S.getRowDimension(); row++) {
			for (int col = 0; col < S.getColumnDimension(); col++) {
				if (S.get(row, col)>maxS) {
					maxS = S.get(row, col);
				}
			}
		}
		theta = theta * maxS;
		// inverse
		for (int i = 0; i < Math.min(S.getRowDimension(), S.getColumnDimension()); i++) {
			double val = S.get(i, i);
			if (val > theta) {
				S.set(i, i, 1.0/val);
			}
			else {
				S.set(i, i, 0);
			}
		}
//		System.out.println("S+=\n"+JamaU.matToString(S));
//		Matrix invSS = S.transpose().times(svdM.getS());
//		System.out.println("invSS =\n"+JamaU.matToString(invSS));
		
		Matrix pinvM = V.times(S.transpose().times(U.transpose()));
//		System.out.println("pinvM =\n"+JamaU.matToString(pinvM));
		// Pseudo Inverse
		return pinvM;
	}
	/**
	  * Computes the Moore–Penrose pseudoinverse using the SVD method.
	  * 
	  * Modified version of the original implementation by Kim van der Linde.
	  */
	public static Matrix pinv2(Matrix x) {
		if (x.rank() < 1)
			return null;
		if (x.getColumnDimension() > x.getRowDimension())
			return pinv(x.transpose()).transpose();
		
		SingularValueDecomposition svdX = new SingularValueDecomposition(x);
		double[] singularValues = svdX.getSingularValues();
		double tol = Math.max(x.getColumnDimension(), x.getRowDimension()) * singularValues[0] * MACH_EPSILON;
		double[] singularValueReciprocals = new double[singularValues.length];
		for (int i = 0; i < singularValues.length; i++)
			singularValueReciprocals[i] = Math.abs(singularValues[i]) < tol ? 0 : (1.0 / singularValues[i]);
//		for (int i = 0; i < singularValues.length; i++)
//			System.out.println("sv["+i+"]="+singularValueReciprocals[i]);
		double[][] u = svdX.getU().getArray();
		double[][] v = svdX.getV().getArray();
		int min = Math.min(x.getColumnDimension(), u[0].length);
		
		double[][] inverse = new double[x.getColumnDimension()][x.getRowDimension()];
		for (int i = 0; i < x.getColumnDimension(); i++)
			for (int j = 0; j < u.length; j++)
				for (int k = 0; k < min; k++)
					inverse[i][j] += v[i][k] * singularValueReciprocals[k] * u[j][k];
		return new Matrix(inverse);
	}
	
	/**
	 * Change a Point3d in Matrix
	 *
	 * @param p the Point3d to change
	 * @return The Matrix of the Point3d p
	 */
	public static Matrix Point3dToMatrix(Point3d p) {
		Matrix m = new Matrix(1, 3);
		m.set(0, 0, p.x);
		m.set(0, 1, p.y);
		m.set(0, 2, p.z);
		return m;
	}
	
	/**
	 * Nice String from a vector (as a Matrix(1xn)).
	 */
	static public String vecToString( Matrix vec ) {
		String str = "";
		if (vec.getRowDimension() > 0 ) {
			str += "[";
			for (int i = 0; i < vec.getColumnDimension(); i++) {
				str += df5_3.format(vec.get(0, i))+"; ";
			}
			str += "]";
		}
		return str;
	}
	/**
	 * Nice String from a Matrix.
	 */
	static public String matToString( Matrix mat ) {
		String str = "";
		for (int i = 0; i < mat.getRowDimension(); i++) {
			str += "[";
			for (int j = 0; j < mat.getColumnDimension(); j++) {
				str += df5_3.format(mat.get(i, j))+"; ";
			}
			str += "]\n";
		}
		return str;
	}
}
