package test;

import algo.Simplex;
import utils.JamaU;
import Jama.Matrix;

public class TestSimplex {

	/** Moment des muscles Mono et Bi, en tenant compte des signes (évite les (-1)^j)*/
	Matrix _hm = new Matrix( new double[][] {{-0.03, 0.04}, {-0.04, 0.02}});
	Matrix _hb = new Matrix( new double[][] {{-0.05, -0.04}, {0.05, 0.02}});
	
	/**
	 * Creation and all the tests
	 */
	public TestSimplex() {
		System.out.println("***** TestSimplex *****");
	}
		
	public void run(String[] args) {	
		boolean res;
		int nbTest = 0;
		int nbPassed = 0;
		
		nbTest ++;
		res = testWiki(args);
		if (res) {
			System.out.println("testWiki >> "+res);
			nbPassed ++;
		}
		else {
			System.err.println("testWiki >> "+res);
		}
		nbTest ++;
		res = testSolve(args);
		if (res) {
			System.out.println("testSolve >> "+res);
			nbPassed ++;
		}
		else {
			System.err.println("testSolve >> "+res);
		}
		
		if (nbTest > nbPassed) {
			System.err.println("FAILURE : only "+nbPassed+" success out of "+nbTest);
			System.exit(1);
		}
		else {
			System.out.println("SUCCESS : "+nbPassed+" success out of "+nbTest);
			System.exit(0);
		}
	}
	
	/**
	 * Test sur l'exemple de Wikipedia : http://en.wikipedia.org/wiki/Simplex_algorithm
	 * @param args
	 * @return
	 */
	boolean testWiki(String[] args) {
		// See http://en.wikipedia.org/wiki/Simplex_algorithm
		Matrix A = new Matrix( new double[][] {{3, 2, 1}, {2, 5, 3}});
		Matrix b = new Matrix( new double[][] {{10},{15}});
		Matrix c = new Matrix( new double[][] {{-2, -3, -4}});
		
		System.out.println("**** Init ************");
		Simplex simp = new Simplex(A, b, c);
		System.out.println("_phI="+JamaU.matToString(simp._phaseI));
		// expected result
		Matrix phI = new Matrix( new double[][] {{1, 0, 0, 0, 0, -1, -1, 0},
				{0, 1, 2, 3, 4, 0, 0, 0}, {0, 0, 3, 2, 1, 1, 0, 10},
				{0, 0, 2, 5, 3, 0, 1, 15}});
		boolean res = (phI.minus(simp._phaseI).normF() < JamaU.MACH_EPSILON);
		if (res == false) {
			System.err.println("_ph1 should be\n"+JamaU.matToString(phI));
			return res;
		}
		
		System.out.println("**** Price Out *******");
		simp.priceOut();
		System.out.println("_phI="+JamaU.matToString(simp._phaseI));
		// expected result
		Matrix pout1 = new Matrix(new double[][] {{1, 0, 5, 7, 4, 0, 0, 25}});
		res = (pout1.minus(simp._phaseI.getMatrix(0, 0, 0, simp._phaseI.getColumnDimension()-1)).normF() < JamaU.MACH_EPSILON);
		if (res == false) {
			System.err.println("_ph1 first row should be\n"+JamaU.matToString(pout1));
			return res;
		}
		
		res = true;
		while(res) {
			System.out.println("**** Pivot *******");
			res = simp.pivot(simp._phaseI, 2);
		}
		System.out.println("**** After Pivot *****");
		System.out.println("_phI="+JamaU.matToString(simp._phaseI));
		// expected result
		Matrix nullSom = new Matrix(new double[][] {{1,0,0,0,0,-1,-1,0}});
		res = (nullSom.minus(simp._phaseI.getMatrix(0, 0, 0, simp._phaseI.getColumnDimension()-1)).normF() < 0.0001);
		if (res == false) {
			System.err.println("_ph1 first row should be\n"+JamaU.matToString(nullSom));
			return res;
		}
		
		System.out.println("**** Phase II ********");
		simp.extractPhaseII();
		System.out.println("_phII="+JamaU.matToString(simp._phaseII));
		res = true;
		while(res) {
			System.out.println("**** Pivot *******");
			res = simp.pivot(simp._phaseII, 1);
		}
		System.out.println("**** After PivotII ***");
		System.out.println("_phII="+JamaU.matToString(simp._phaseII));
		
		// Expected result
		Matrix fin = new Matrix(new double[][] {{1,0,-3.5714,0,-18.5714},
				{0,1,0.1429,0,2.1429}, {0,0,1.5714,1,3.5714}});
		res = (fin.minus(simp._phaseII).normF() < 0.001);
		if (res == false) {
			System.err.println("_phII first row should be\n"+JamaU.matToString(fin));
			return res;
		}
		
		System.out.println("**** Solution ********");
		Matrix sol = simp.getFeasibleSolution();
		System.out.println("sol="+JamaU.vecToString(sol));
		// Expected Result
		Matrix feasible = new Matrix(new double[][] {{2.1429,0,3.5714}});
		res = (feasible.minus(sol).normF() < 0.001);
		if (res == false) {
			System.err.println("Sol should be "+JamaU.matToString(fin));
			return res;
		}
		double val = simp.getMinimum();
		System.out.println("val="+val);
		// Expected Result
		double min = -18.5714;
		res = (Math.abs(val-min) < 0.001);
		if (res == false) {
			System.err.println("Min should be "+min);
			return res;
		}
		return res;
	}
	/**
	 * Test de la fonction 'solve'
	 * @param args
	 * @return
	 */
	boolean testSolve(String[] args) {
		// See http://en.wikipedia.org/wiki/Simplex_algorithm
		Matrix A = new Matrix( new double[][] {{3, 2, 1}, {2, 5, 3}});
		Matrix b = new Matrix( new double[][] {{10},{15}});
		Matrix c = new Matrix( new double[][] {{-2, -3, -4}});
		
		System.out.println("**** Init ************");
		Simplex simp = new Simplex(A, b, c);
		
		boolean res = simp.solve();
		if (res==false) {
			System.err.println("Pb should be solvable");
			return res;
		}
		return res;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TestSimplex app = new TestSimplex();
		app.run(args);

	}
	
}
