/**
 * 
 */
package utils;

import java.io.OutputStream;

/**
 * Interface for Classes that have Parameters to be set 
 * with ParameterFactory.
 * 
 * @author alain.dutech@loria.fr
 */
public interface IParameters {
	
	/** 
	 * Should print out
	 * "### ClassName"<br>
	 * "#     paramName=paramValue"<br>
	 * 
	 * @param out
	 */
	public void printValues(OutputStream out);

}
