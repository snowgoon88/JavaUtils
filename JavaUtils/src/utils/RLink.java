/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
	 * > write.table( object, file="filename", row.names=FALSE, col.names=FALSE)
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
		
		// Not interested by comment
		String lineRead = reader.readLine();
		while (lineRead.startsWith("#")) {
			lineRead = reader.readLine();
		}
		// First line = get nb of columns
		StringTokenizer st = new StringTokenizer( lineRead, "[](,+ \t\n\r\f:");
		int nbCol = st.countTokens();
		System.out.println("extractFromHeader: nbCol="+nbCol);
		
		// Read all values
		ArrayList<Double> vals = new ArrayList<Double>();
		//int nbRow = 0;
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
		aFile.close();
		return vMat.transpose();
	}
	
	/**
	 * Write a Matrix in a file for R to read.
	 * > B <- read.table(file="filename")
	 * 
	 * @param mat Matrix to be written
	 * @param filename Name of file
	 * @throws IOException if cannot open file for writing.
	 */
	static public void writeRDataFile( Matrix mat, String filename )
			throws IOException {
		// Open file for writing
		FileWriter aFile = new FileWriter( filename );
		BufferedWriter writer = new BufferedWriter( aFile );
		
		// Header
		writer.write("# Created by RLink.writeRDataFile -- read with 'read.table(file=filename)'\n");
		
		// Row by Row
		for (int i = 0; i < mat.getRowDimension(); i++) {
			writer.write(Double.toString(mat.get(i, 0)));
			for (int j = 1; j < mat.getColumnDimension(); j++) {
				writer.write("\t"+Double.toString(mat.get(i, j)));
			}
			writer.write("\n");
		}
		writer.close();
		aFile.close();
	}
}
