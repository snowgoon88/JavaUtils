/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import org.kohsuke.args4j.ClassParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.OptionHandler;

/**
 * The ParameterFactory make the use of Parameters easy.
 * Use the "@Option" annotation (from org.kohsuke.args4j.Option) in Objects,
 * add theses Object to the ParemeterFactory.
 * Then, call:
 * <li>parseFromCLI(String[] args) to parse from the Command Line.</li>
 * <li>parseFromFile(String filename) to parse from a given file.</li>
 * <li>parse(Strin[] args) to parse from CLI first and IF "--paramFile/-f filename"
 * is given as an argument, then parse from file 'filename'.</li>
 * 
 * optionnal : use with --paramFile/-f filename. 
 * 
 * @author alain.dutech@loria.fr
 * @author Build on args4j by Kohsuke Kawaguchi (http://args4j.kohsuke.org/)
 */
public class ParameterFactory implements IParameters {
	
	/** File to read parameters from */
	@Option(name="-f",aliases={"--paramFile"},usage="File to read parameters from")
	public String paramFile = "";
	/** Flag to get usage */
	@Option(name="-h",aliases={"--help"},usage="Print list of parmaters and usage")
	public boolean _help = false;
	/** Flag to get default value */
	@Option(name="-s",aliases={"--showParam"},usage="Print list of parameters values")
	public boolean _show = false;
	
	/** List of Object with Parameters */
	ArrayList<IParameters> _toParse;
	/** Parser for the whole list of Objects */
	ListCmdLineParser _superParser;
	
	
	/**
	 * Build the ParameterFactory,
	 * adding 'this' as first Object to parse.
	 */
	public ParameterFactory() {
		_toParse = new ArrayList<IParameters>();
		_toParse.add(this);
		
		_superParser = null;
	}

	/**
	 * Parse from CommandLine then from file if given by the -f <paramFile> option.
	 * Take into account "-s/showParam" and "-h/help" parameters.
	 * 
	 * @param args
	 * @return true if all Parameters set.
	 */
	public boolean parse(String[] args) {
		if (_superParser == null) {
			_superParser = new ListCmdLineParser(null);
		}
		boolean res = parseFromCLI(args);
		if (res == true && paramFile != "") {
			res = parseFromFile( paramFile );
		}
		if (_help) printUsage(System.out);
		if (_show) printValues(System.out);
		
		return res;
	}
	/**
	 * Parse from the Command Line.
	 * @param args
	 * @return true if all Parameters set.
	 */
	public boolean parseFromCLI(String[] args) {
		if (_superParser == null) {
			_superParser = new ListCmdLineParser(null);
		}
        try {
                _superParser.parseArgument(args);
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            printUsage(System.err);
        }
        return true;
	}
	
	/**
	 * Parse from a given File
	 * @param filename
	 * @return true if all Parameters set
	 */
	public boolean parseFromFile(String filename) {
		// Open file, use Tokenizer to build a String[] then parse
		// Open file for reading
		FileReader aFile;
		try {
			aFile = new FileReader( filename );
		} catch (FileNotFoundException e) {
			System.err.println("Parameters.parseFromFile : "+filename+" NOT FOUND");
			return true; // using default
		}
		BufferedReader reader = new BufferedReader( aFile );
		
		// Read all the Token
		ArrayList<String> args = new ArrayList<String>();
		
		String lineRead;
		try {
			lineRead = reader.readLine();
			while (lineRead != null) {
				if (lineRead.startsWith("#") == false) {
					StringTokenizer st = new StringTokenizer(lineRead);
					while (st.hasMoreElements()) {
						String tok = (String) st.nextElement();
						args.add(tok);
					}
				}
				lineRead = reader.readLine();
			}
			reader.close();
	        aFile.close();
		} catch (IOException e) {
			System.err.println("Parameters.parseFromFile : IOException in "+filename);
			return true; // using default
		}
		
        try {
        	_superParser.parseArgument(args);
        } catch (CmdLineException e) {
        	// handling of wrong arguments
        	System.err.println(e.getMessage());
        	printUsage(System.err);
        }
        
        return true;	
	}
	
	/**
	 * Add an Object with parameters (annotated with @Option)
	 * 
	 * @param obj
	 */
	public void addObjectWithParameters(IParameters obj) {
		_toParse.add( obj );
	}
	
	/**
	 * Print usage
	 * 
	 * @param out A Stream for output.
	 */
	public void printUsage(OutputStream out) {
		if (_superParser == null) {
			_superParser = new ListCmdLineParser(null);
		}
		_superParser.printUsage(out);
		System.exit(0);
	}
	/** 
	 * Print Default Parameters value.
	 */
	public void printValues(OutputStream out) {
		PrintStream pout = new PrintStream(out);
		pout.println("### "+getClass().getName());
		pout.println("#     paramFile="+paramFile);
		for (IParameters itemParsed : _toParse) {
			if (itemParsed.equals(this) == false ) {
				itemParsed.printValues(out);
			}
		}
		System.exit(0);
	}
	
	/**
	 * Class that extends CmdLineParser in order to build
	 * a super Parser that will parse every object of _toParse
	 * in order to get the list of all Parameters to find, either
	 * in the CmdLine or in a file.
	 */
	@SuppressWarnings("rawtypes")
	class ListCmdLineParser extends CmdLineParser {

		List<OptionHandler> options = new ArrayList<OptionHandler>();
	
		public ListCmdLineParser(Object bean) {
			super(null);
			for (Object obj : _toParse) {
				// Parse the metadata and create the setters
				new ClassParser().parse(obj,this);
			}
			// for display purposes, we like the arguments in argument order, but the options in alphabetical order
			Collections.sort(options, new Comparator<OptionHandler>() {
				public int compare(OptionHandler o1, OptionHandler o2) {
					return o1.option.toString().compareTo(o2.option.toString());
				}
			});
			
//			System.out.println("**** RecParser ");
//			this.printUsage(System.out);
		}
		
		
	}
}
