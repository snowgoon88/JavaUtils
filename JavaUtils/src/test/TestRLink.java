/**
 * 
 */
package test;

import java.io.IOException;

import utils.JamaU;
import Jama.Matrix;
import utils.RLink;

/**
 * The aim is to test the interface, through files, with the 'R software'
 * 
 * @author alain.dutech@loria.fr
 *
 */
public class TestRLink {

	/**
	 * Creation and all the tests
	 */
	public TestRLink() {
		System.out.println("***** TestRlink *****");
		
//		try {
//			FileWriter aFile = new FileWriter( "testWrite.txt");
//			aFile.write("Ipsum locust");
//			aFile.flush();
//			aFile.close();
//		} catch (IOException e) { 
//			e.printStackTrace();
//		}
	}
	public void run() {
		boolean res;
		int nbTest = 0;
		int nbPassed = 0;
		try {
			nbTest ++;
			res = testReadRDataFile();
			if (res) {
				System.out.println("testReadRData >> "+res);
				nbPassed ++;
			}
			else {
				System.err.println("testReadRData >> "+res);
			}
			
			nbTest ++;
			res = testWriteRDataFile();
			if (res) {
				System.out.println("testReadRData >> "+res);
				nbPassed ++;
			}
			else {
				System.err.println("testReadRData >> "+res);
			}
			
			if (nbTest > nbPassed) {
				System.err.println("FAILURE : only "+nbPassed+" success out of "+nbTest);
				System.exit(1);
			}
			else {
				System.out.println("SUCCESS : "+nbPassed+" success out of "+nbTest);
				System.exit(0);
			}
		} catch (IOException e) {
			System.err.println("Some test failed badly !!!");
			e.printStackTrace();
		}
	}

	/**
	 * Read a file written in R.
	 * 
	 * @param filename
	 * @return true if test OK
	 * @throws IOException 
	 */
	public boolean testReadRDataFile() 
			throws IOException {
		// target Matrix -row by row
		Matrix A = new Matrix(new double [][] {{1,2},{3,4},{5,6}});
		
		Matrix mRead = RLink.readRDataFile( "src/test/matA.Rdata" );
		System.out.println("mRead = "+JamaU.matToString(mRead));
		
		// Check they are equal
		if (JamaU.isNearZero(A.minus(mRead)) == false ) {
			System.err.println("testReadRData FAILED");
			System.err.println("target MATRIX="+JamaU.matToString(A));
			System.err.println("read   MATRIX="+JamaU.matToString(mRead));
			return false;
		}
		return true;
	}
	
	/**
	 * Write a Matrix as a file for R.
	 * 
	 * @return true if test OK
	 * @throws IOException 
	 */
	public boolean testWriteRDataFile() throws IOException {
		// target Matrix -row by row
		Matrix B = new Matrix(new double [][] {{1,2,3,4,5},{2,3,4,5,6},{5,4,3,2,1}});
		
		RLink.writeRDataFile( B, "src/test/matB.Rdata");
		
		Matrix mRead = RLink.readRDataFile( "src/test/matB.Rdata" );
		System.out.println("mRead = "+JamaU.matToString(mRead));
		
		// Check they are equal
		if (JamaU.isNearZero(B.minus(mRead)) == false ) {
			System.err.println("testWriteRData FAILED");
			System.err.println("target MATRIX="+JamaU.matToString(B));
			System.err.println("read   MATRIX="+JamaU.matToString(mRead));
			return false;
		}
		return true;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRLink app = new TestRLink();
		app.run();
	}

}
