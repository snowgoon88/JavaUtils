/**
 * 
 */
package test;

import java.io.OutputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import utils.IParameters;

/**
 * Has "local" parameters.
 * To test how to set them.
 * 
 * @author alain.dutech@loria.fr
 */
public class SubParam implements IParameters {
	
	/** Local Parameters */
	@Option(name="--sub",aliases={"-s"},usage="SubParam")
	public String _sub = "default";
	
	public String dump() {
		StringBuffer buf = new StringBuffer();
		buf.append( "  _sub = "+_sub);
		
		return buf.toString();
	}
	
	@Override
	public void printValues(OutputStream out) {
		PrintStream pout = new PrintStream(out);
		pout.println("### "+getClass().getName());
		pout.println("#     sub="+_sub);
	}
}
