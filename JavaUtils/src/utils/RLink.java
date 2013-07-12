/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import Jama.Matrix;

/**
 * @author alain.dutech@loria.fr
 *
 */
public class RLink {

	/**
	 * Read a file with a matrix written from 'R' with
	 * > write.table( object, file="filename", quote=FALSE, row.names=FALSE)
	 * 
	 * @param filename
	 * @return Matrix read from the file
	 * @throws IOException if file does not exists or cannot be read
	 */
	static public Matrix readRDataFile( String filename )
			throws IOException {
		// Open file for reading
		FileReader aFile = new FileReader( filename );
		BufferedReader reader = new BufferedReader( aFile );
		
		// First line = Header
		String lineRead = reader.readLine();
		StringTokenizer st = new StringTokenizer( lineRead, "[](,+ \t\n\r\f:");
		int nbCol = st.countTokens();
		System.out.println("extractFromHeader: nbCol="+nbCol);
		
		// Read all values
		ArrayList<Double> vals = new ArrayList<Double>();
		//int nbRow = 0;
		lineRead = reader.readLine();
		while( lineRead != null ) {
			//nbRow ++;
			st = new StringTokenizer( lineRead, "[](,+ \t\n\r\f:");
			while (st.hasMoreElements()) {
				String tok = (String) st.nextElement();
				vals.add(Double.parseDouble(tok));
			}
			lineRead = reader.readLine();
		}
		
		// Create the Matrix, but the values are packed by row, and MUST be double[]
		double[] data = new double[vals.size()];
		for (int i = 0; i < data.length; i++) {	
			data[i] = vals.get(i);                // java 1.5+ style (outboxing)
		}
		Matrix vMat = new Matrix(data, nbCol);
		
		reader.close();
		return vMat.transpose();
	}
}
