/**
 * 
 */
package test;


import java.io.OutputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import utils.IParameters;
import utils.ParameterFactory;

/**
 * 
 * @author alain.dutech@loria.fr
 */
public class TestParam implements IParameters {
	
	/** Global Parameters */
	@Option(name="--root",aliases={"-r"},usage="RootParam")
	public String _root = "default";
	
	/**
	 * Creation and all the tests
	 */
	public TestParam() {
		System.out.println("***** TestParser *****");
	}
		
	public void run(String[] args) {	
		boolean res;
		int nbTest = 0;
		int nbPassed = 0;


		nbTest ++;
		res = testBasic(args);
		if (res) {
			System.out.println("testBasic >> "+res);
			nbPassed ++;
		}
		else {
			System.err.println("testBasic >> "+res);
		}
		nbTest ++;
		res = testFactory(args);
		if (res) {
			System.out.println("testNested >> "+res);
			nbPassed ++;
		}
		else {
			System.err.println("testNested >> "+res);
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
	 * Basic : Parameters from CLI, and from given File
	 * run with --root CLI --paramFile src/test/paramBasic.txt
	 * @return
	 */
	boolean testBasic(String[] args) {
		ParameterFactory paramFactory = new ParameterFactory();
		SubParam nested = new SubParam();
		paramFactory.addObjectWithParameters(this);
		paramFactory.addObjectWithParameters(nested);
		
		boolean res = _root.equals("default");
		if (res == false) {
			System.err.println("testBasic : _root should no be = "+_root);
			return res;
		}
		res = nested._sub.equals("default");
		if (res == false) {
			System.err.println("testBasic : nested._sub should no be = "+nested._sub);
			return res;
		}
		paramFactory.parseFromCLI(args);
		res = _root.equals("CLI");
		if (res == false) {
			System.err.println("testBasic, parseCLI : _root should no be = "+_root);
			System.err.println("SURE you ran with --root CLI ?");
			return res;
		}
		res = nested._sub.equals("default");
		if (res == false) {
			System.err.println("testBasic, parseCLI : nested._sub should no be = "+nested._sub);
			System.err.println("SURE you ran with --root CLI ?");
			return res;
		}
		
		paramFactory.parseFromFile("src/test/paramBasic.txt");
		res = _root.equals("ROOTF");
		if (res == false) {
			System.err.println("testBasic, parseFile : _root should no be = "+_root);
			return res;
		}
		res = nested._sub.equals("SUBF");
		if (res == false) {
			System.err.println("testBasic, parseFile : nested._sub should no be = "+nested._sub);
			return res;
		}
		
		System.out.println("After reading src/test/paramBasic.txt");
		System.out.println("_root="+_root+" nested._sub="+nested._sub);
		
		return res;
	}
	/**
	 * Nested : Parameters at global and local
	 * run with --root CLI --paramFile src/test/paramBasic.txt
	 * @param args
	 * @return
	 */
	boolean testFactory(String[] args) {
		ParameterFactory paramFactory = new ParameterFactory();
		SubParam nested = new SubParam();
		paramFactory.addObjectWithParameters(this);
		paramFactory.addObjectWithParameters(nested);
		_root = "reset";
		nested._sub = "reset";
		
		paramFactory.parse(args);
		boolean res = _root.equals("ROOTF");
		if (res == false) {
			System.err.println("testFactory : _root should no be = "+_root);
			return res;
		}
		res = nested._sub.equals("SUBF");
		if (res == false) {
			System.err.println("testFactory : nested._sub should no be = "+nested._sub);
			return res;
		}
		
		return res;
	}
	
	@Override
	public void printValues(OutputStream out) {
		PrintStream pout = new PrintStream(out);
		pout.println("### "+getClass().getName());
		pout.println("#     root="+_root);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TestParam app = new TestParam();
		app.run(args);

	}

}
