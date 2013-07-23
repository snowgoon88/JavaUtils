/**
 * 
 */
package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Log "things' in a File.
 * 
 * @author thomas.moinel
 * @author alain.dutech@loria.fr
 */
public class Log<T> {
	/** Where to xrite */
	private BufferedWriter _bw;

	/** 
	 * Create with a fileName.
	 * If IOException in 'open', continue but will not log.
	 * @param filename
	 */
	public Log(String filename) {
		_bw = null;
		open(filename);
	}

	/**
	 * Open, complains if pb, but does not halt.
	 * @param filename
	 */
	private void open(String filename) {
		try {
			_bw = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			_bw = null;
			e.printStackTrace();
		}
	}

	/**
	 * Write a line, but only if _bw not null.
	 * @param line
	 */
	public void writeLine(String line) {
		if (_bw != null ) {
			try {
				_bw.write(line);
				_bw.newLine();
				_bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * write a T as a String, but only if _bw not null.
	 * @param t time
	 * @param val
	 */
	public void write(T val) {
		if (_bw != null ) {
			try {
				_bw.write(val.toString());
				_bw.newLine();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Close if needed.
	 */
	public void close() {
		try {
			if (_bw != null)
				_bw.close();
		} catch (IOException e) {
		}
	}

}
