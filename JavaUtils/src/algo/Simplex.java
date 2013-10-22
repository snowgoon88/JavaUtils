/**
 * 
 */
package algo;

import utils.JamaU;
import Jama.Matrix;

/**
 * Implements a Simplex algorithm => All b values must be positive.
 * @see http://en.wikipedia.org/wiki/Simplex_algorithm
 * 
 * @warn Maybe the algo is not working if some constraintes are negative.
 * ((As read on Wikipedia, in the initial phase creation
 * Columns of the identity matrix are added as column vectors for 
 * these variables. If the b value for a constraint equation is negative,
 * the equation is negated before adding the identity matrix columns.
 * This does not change the set of feasible solutions or the optimal
 * solution, and it ensures that the slack variables will constitute
 * an initial feasible solution.))
 * 
 * @author alain.dutech@loria.fr
 */
public class Simplex {
	
	/** Working Matrix made for the phaseI pb */
	public Matrix _phaseI = null;
	/** Phase II Matrix */
	public Matrix _phaseII = null;
	/** Pb dimension : nb constraints */
	int _M = 0;
	/** Pb dimension : nb variables */
	int _N = 0;
	
	/**
	 * Minimizes c.x with respect to A.x = b.
	 * 
	 * @param A MxN Matrix
	 * @param b Mx1 Matrix
	 * @param c 1xN Matrix (objective function)
	 */
	public Simplex( Matrix A, Matrix b, Matrix c) {
		// Build the phaseI Matrix
		_M = A.getRowDimension();
		_N = A.getColumnDimension();
		_phaseI = new Matrix( 1+1+_M, 1+1+_M+_N+1, 0.0);
		// for the objective function of the phaseI
		_phaseI.set(0, 0, 1.0); 
		_phaseI.setMatrix(0, 0, _N+2, _M+_N+1, new Matrix(1,_M,-1.0));	
		// Objective function of PL
		_phaseI.set(1, 1, 1.0);
		_phaseI.setMatrix(1, 1, 2, _N+1, c.uminus());
		// artificial variables
		for (int i = 2; i < _M+2; i++) {
			_phaseI.set(i, _N+i, 1.0);
		}
		// Constraints
		_phaseI.setMatrix(2, _M+1, 2, _N+1, A);
		_phaseI.setMatrix(2, _M+1, _M+_N+2, _M+_N+2, b);
	}
	
	/**
	 * Try to solve using Simplex.
	 * 
	 * @return true is solved, false otherwise.
	 */
	public boolean solve() {
		// Price Out phaseI
		priceOut();
		// pivot
		boolean toPivot = true;
		while(toPivot) {
			toPivot = pivot(_phaseI, 2);
		}
		if (_phaseI.get(0, _phaseI.getColumnDimension()-1) > 0.00000001) {
			// No feasible solution
			return false;
		}
		
		// Extract and phaseII
		extractPhaseII();
		toPivot = true;
		while(toPivot) {
			toPivot = pivot(_phaseI, 2);
		}
		return true;
	}
	/**
	 * Solve verbeux.
	 */
	public boolean solveVerbeux() {
		System.out.println("**** Init ************");
		System.out.println("_phI="+JamaU.matToString(_phaseI));
		
		// Price Out phaseI
		priceOut();
		System.out.println("**** Price Out *******");
		System.out.println("_phI="+JamaU.matToString(_phaseI));
		
		// pivot
		boolean toPivot = true;
		while(toPivot) {
			toPivot = pivotVerbeux(_phaseI, 2);
		}
		if (_phaseI.get(0, _phaseI.getColumnDimension()-1) > 0.00000001) {
			// No feasible solution
			return false;
		}
		
		// Extract and phaseII
		extractPhaseII();
		System.out.println("**** Phase II ********");
		System.out.println("_phII="+JamaU.matToString(_phaseII));
		toPivot = true;
		while(toPivot) {
			toPivot = pivotVerbeux(_phaseI, 2);
		}
		return true;
	}
	
	/**
	 * row-addMultiply to get rid of negative coefficient on row 0. 
	 */
	public void priceOut() {
		// Always on row 0
		int width = _phaseI.getColumnDimension();
		// For every negative coeff of row 0
		for(int i=0; i < width; i++) {
			if (_phaseI.get(0, i) < 0) {
				// Find the 1 coefficient in the I matrix below
				for (int j = 1; j < _phaseI.getRowDimension(); j++) {
					if( _phaseI.get(j, i) != 0.0) {
						Matrix oldRow = _phaseI.getMatrix(0, 0, 0, width-1);
						oldRow.plusEquals(_phaseI.getMatrix(j, j, 0, width-1));
						_phaseI.setMatrix(0, 0, 0, width-1, oldRow);
					}
				}
			}
		}
	}
	
	/**
	 * Cherche les pivots.
	 * 
	 * PhaseI : cherche pivot entre col 2->width-1 et row 2->nbRow
	 * PhaseII : cherche pivot entre col 1->width-1 et row 1-<nbRow
	 * 
	 * @param a : either _phaseI or _phaseII
	 * @param startIndice : 2 pour phaseI et 1 pour phaseII (!!!!)
	 */
	public boolean pivot( Matrix a, int startIndice) {
		int width = a.getColumnDimension();
		// First non-null coef for nonbasic variables (col > 0)
		// Look for first positive coefficient
		for (int i = startIndice; i < width-1; i++) {
			if( a.get(0, i) > 0) {
				// Find the best minimum ratio row
				int bestRow = 0;
				double minRatio = Float.MAX_VALUE;
				for (int j = startIndice; j < a.getRowDimension(); j++) {
					double elem = a.get(j, i);
					if ( elem > 0) {
						double ratio = a.get(j, width-1) / elem;
						if (ratio < minRatio) {
							minRatio = ratio;
							bestRow = j;
						}
					}
				}
				//System.out.println("pivot element at ("+bestRow+", "+i+")");
				
				// Change this row => multiply by inv
				Matrix row = a.getMatrix(bestRow, bestRow, 0, width-1);
				row.timesEquals(1.0/a.get(bestRow, i));
				a.setMatrix(bestRow, bestRow, 0, width-1, row);
				//System.out.println("rowInv\n"+JamaU.matToString(a));
				// Alter other rows 
				for (int j = 0; j < a.getRowDimension(); j++) {
					if( j != bestRow) {
						Matrix curRow = a.getMatrix(j, j, 0, width-1);
						curRow.minusEquals(row.times(a.get(j,i)));
						a.setMatrix(j, j, 0, width-1, curRow);
					}
				}
				//System.out.println("pivoted\n"+JamaU.matToString(a));
				return true;
			}
		}
		return false;
	}
	public boolean pivotVerbeux( Matrix a, int startIndice) {
		int width = a.getColumnDimension();
		// First non-null coef for nonbasic variables (col > 0)
		// Look for first positive coefficient
		for (int i = startIndice; i < width-1; i++) {
			if( a.get(0, i) > 0) {
				// Find the best minimum ratio row
				int bestRow = 0;
				double minRatio = Float.MAX_VALUE;
				for (int j = startIndice; j < a.getRowDimension(); j++) {
					double elem = a.get(j, i);
					if ( elem > 0) {
						double ratio = a.get(j, width-1) / elem;
						if (ratio < minRatio) {
							minRatio = ratio;
							bestRow = j;
						}
					}
				}
				// Solution si bestRow > 0
				if (bestRow > 0) {
					System.out.println("pivot element at ("+bestRow+", "+i+")");

					// Change this row => multiply by inv
					Matrix row = a.getMatrix(bestRow, bestRow, 0, width-1);
					row.timesEquals(1.0/a.get(bestRow, i));
					a.setMatrix(bestRow, bestRow, 0, width-1, row);
					System.out.println("rowInv\n"+JamaU.matToString(a));
					// Alter other rows 
					for (int j = 0; j < a.getRowDimension(); j++) {
						if( j != bestRow) {
							Matrix curRow = a.getMatrix(j, j, 0, width-1);
							curRow.minusEquals(row.times(a.get(j,i)));
							a.setMatrix(j, j, 0, width-1, curRow);
						}
					}
					System.out.println("pivoted\n"+JamaU.matToString(a));
					return true;
				}
				else {
					System.out.println("No pivot point at col="+i);
					return false;
				}
			}
		}
		return false;
	}
	/**
	 * Une fois la phaseI terminée, on en extrait la matrice pour
	 * la phaseII.
	 */
	public void extractPhaseII() {
		// indices 1...N+1, M+N+2
		int[] colIndices = new int[_N+2];
		for (int i = 0; i < colIndices.length-1; i++) {
			colIndices[i] = i+1;
		}
		colIndices[_N+1]=_M+_N+2;
		_phaseII = _phaseI.getMatrix(1, _M+1, colIndices);
	}

	/**
	 * Return the minimum point : value corresponding to the identity
	 * Matrix in _phaseII
	 * 
	 * @return Matrix 1xN of solution.
	 */
	public Matrix getFeasibleSolution() {
		Matrix sol = new Matrix(1, _N, 0);
		// Cherche les colonnes correspondant à la matrice identité
		for( int col=1; col < _N+2; col++) {
			int nbUn = 0;
			int indUn = -1;
			for (int row = 1; row < _M+1; row++) {
				if (Math.abs(_phaseII.get(row, col) - 1.0) < 0.0000001 ) {
					nbUn += 1;
					indUn = row;
				}
				else if (Math.abs(_phaseII.get(row, col)) > 0.0000001) {
					nbUn = _M +1;
					
				}
			}
			if (nbUn == 1) {
				sol.set(0,col-1, _phaseII.get(indUn, _phaseII.getColumnDimension()-1));
			}
		}
		
		return sol;
	}
	/**
	 * Return the minimum value.
	 */
	public double getMinimum() {
		return _phaseII.get(0, _phaseII.getColumnDimension()-1);
	}

}
