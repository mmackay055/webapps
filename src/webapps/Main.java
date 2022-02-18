package webapps;
/**
 * Name: Main.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 21, 2021
 * Description: Abstract class for executing assignment programs, provides
 *              functionality for parsing command line arguments
 */


import java.util.HashMap;
import java.util.Vector;
import webapps.Option.*;

/**
 * Used to build executables with a built in command line argument parser
 */
public abstract class Main {
	
	HashMap<String, Option> options;// stores all options
	Vector<String> params; // stores all parameters
	String name; // name of program
	String desc; // description of program
	String usage; // usage of program syntax
	OptionBool helpOpt;
	
	/**
	 * Initiates option and parameter data structures and sets 
	 * help option
	 * Called by; none
	 *     Calls: HashMap<String, Option>, Vector<String>, OptionBool(),
	 */
	public Main() throws OptionException {
		//create data structs for options and params
		this.options = new HashMap<String, Option>();
		this.params = new Vector<String>();
		
		//create and add help option
		try {
			helpOpt = new OptionBool("h", "print help for this application");
			addOption(helpOpt);
		}
		catch (OptionException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Initiates option and parameter data structures and sets 
	 * help option. Sets name and description of program
	 * Called by: Srv(), WebCli()
	 *     Calls: this()
	 * @param name
	 * @param desc
	 */
	public Main(String name, String desc, String usage) throws OptionException {
		this();
		this.name = name;
		this.desc = desc;
		this.usage = usage;
	}
	
	/**
	 * Parses command line arguments and calls the setOption and
	 * setParam functions to set options and parameters.
	 * Called by: WebCli.main(), WebSrv.main(), WebProxSrv.main() 
	 *     Calls: getOption(), getOptionValue()
	 * @param args - command line argument array
	 * @throws OptionException - if invalid option or value is provided
	 */
	protected void parseOptions(String [] args) throws OptionException {
		// index for arguments, used as array so it can be updated
		// from within a function
		int[] i = {0}; 
		
		for (i[0] = 0; i[0] < args.length; i[0]++) {
			String optName = null;
			
			if (args[i[0]].startsWith("--")) { // check option starts with --
				// get option name
				optName = args[i[0]].substring(2);
			}
			else if (args[i[0]].startsWith("-")) { // check option starts with -
				 
				// get option name
				optName = args[i[0]].substring(1);
				
				// throw exception if option name is greater than 1 char
				if (optName.length() > 1) {
					throw new OptionException(optName + " is an in valid '-' option type");
				}
			}
			
			// set option or parameter
			if (optName == null) {
				// no option name was found set as parameter
				setParam(args[i[0]].trim());
			}
			else {
				// get option to check for flag
				Option opt = getOption(optName);
				
				//checks if option is a flag
				if (opt.getClass() == Option.OptionBool.class) {
					setOption(optName, "");
				}
				else {
					// option needs a value
					setOption(optName, getOptionValue(args, i));
				}
			}
		}
	}
	
	/**
	 * Gets the value for the option. If option is last element
	 * of argument array then returns empty string, if next element
	 * is another option returns an empty string indicating a flag.
	 * Increments index passed to it
	 * Called by: parseOptions()
	 *     Calls: none
	 * @param opt - string array of options
	 * @param i - index of option passed as Integer class so it 
	 *             can be incremented in function
	 */
	private String getOptionValue(String[] opt, int[] i) {
		if (i[0] < opt.length - 1) {
			String val =  opt[i[0] + 1];
			if (val.startsWith("-")) {
				return "";
			}
			else {
				i[0]++;
				return val;
			}
		}
		else
			return "";
	}
	
	/**
	 * Adds option to program
	 * Called by: Main(), Srv(), WebSrv(), WebProxSrv(), WebCli()
	 *     Calls: Map<String, Option>
	 * @param opt
	 */
	protected void addOption(Option opt) {
		options.put(opt.getName(), opt);
	}
	
	/**
	 * Returns Option object specified by name
	 * Called by: parseOptions
	 *     Calls: Map<Sting, Option>.get
	 * @param name - name of Option
	 * @return Option object requested
	 * @throws OptionException - if option does not exist
	 */
	protected Option getOption(String name) throws OptionException {
		Option opt = options.get(name); 
		if (opt == null)
			throw new OptionException(name + " is an invalid option");
		
		return opt;
	}
	
	/**
	 * Prints help message to console including descriptions of options
	 * Called by: WebSrv(), WebProxSrv(), WebCli()
	 */
	public void printHelp() {
		System.out.println(name);
		System.out.println(desc);
		System.out.println();
		System.out.println("Usage:");
		System.out.println("\t" + usage);
		System.out.println();
		System.out.println("Options:");
		for (Option o : options.values()) {
			System.out.println("\t-" + o.getHelpMessage());
		}
	}
	
	/*
	 * Gets value of help option
	 */
	public boolean getHelp() {
		return helpOpt.getValue();
	}
	
	
	/**
	 * Abstract method to set parameter of program
	 * @param param - string value of parameter
	 * @throws OptionException - if error with parameter exists
	 */
	public abstract void setParam(String param) throws OptionException;
	
	/**
	 * Abstract method to set option of program
	 * @param name - name of option
	 * @param value - value of option
	 * @throws OptionException - if error with option exists
	 */
	public abstract void setOption(String name, String value) throws OptionException;
	
	/**
	 * Checks that parameters are valid
	 * @throws OptionException - if error with parameter exists
	 */
	public abstract void checkParams() throws OptionException;

	public static void printError(String errMsg) {
		System.err.println("error: " + errMsg);
	}
}
