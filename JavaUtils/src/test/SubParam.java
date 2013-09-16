/**
 * 
 */
package test;

import org.kohsuke.args4j.Option;

/**
 * Has "local" parameters.
 * To test how to set them.
 * 
 * @author alain.dutech@loria.fr
 */
public class SubParam {
	
	/** Local Parameters */
	@Option(name="--sub",aliases={"-s"},usage="SubParam")
	public String _sub = "default";
	
	public String dump() {
		StringBuffer buf = new StringBuffer();
		buf.append( "  _sub = "+_sub);
		
		return buf.toString();
	}
}
