package webapps;
/**
 * Name: WebSrv.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class that implements a simple web server that can read
 *              that can server resources from local directories through
 *				HTTP communications. Handles client requests by first 
 *              accepting a connection and passing connection to a thread
 *              to handle the request. The thread will search the provided
 *              directories for the requested resource and return it or
 *              return an error message to client. Starts from command line 
 *              and accepts arguments and option flags. Run with -h to see 
 *              help manual.
 *                         
 *              Basic usage:
 *              	java tma1.WebSrv [options] <directory1> <directory2> <directory3>...
 *              
 * Inherits: Srv
 */


import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import webapps.Option.OptionException;
import webapps.Option.OptionInt;

/**
 * Class definition
 */
public class WebSrv extends Srv {
	Vector<File> dirs; // directory to serve resources from
	
	/**
	 * Constructor of WebSrv initiates options and directories
	 * Called By: None
	 *     Calls: super(), OptionInt(), Vector<File>(), Main.addOption()
	 */
	public WebSrv() throws OptionException {
		super();
		
		//initialize port option with default value of 80
		portOpt = new OptionInt("p", "Set port that webserver listens on, default: 80", "80");
		
		//add options
		addOption(portOpt);
		
		// set up dirs vector
		dirs = new Vector<File>();
	}

	/**
	 * Constructor of WebSrv initiates options and directories
	 * Called By: main()
	 *     Calls: super(String, String), OptionInt(), Vector<File>()
	 * @param name - name of program
	 * @param desc - description of program
	 * @param usage - usage syntax of program
	 */
	public WebSrv(String name, String desc, String usage) throws OptionException {
		super(name, desc, usage);
		
		//initialize port option with default value of 80
		portOpt = new OptionInt("p", "Set port that webserver listens on, default: 80", "80");
		
		//add options
		addOption(portOpt);

		// set up dirs vector		
		dirs = new Vector<File>();
		
	}

	/**
	 * Sets directory parameters
	 * Called By: super.parseOptions()
	 *     Calls: super.setParam(), File(), Vector<Option>.add(), Vector<File>.add()
	 * @param - url string 
	 */
	@Override
	public void setParam(String param) throws OptionException {
		super.setParam(param);
		File dir = new File(param);
		dirs.add(dir);
	}

	/**
	 * Checks directories are valid
	 * Called By: main()
	 *     Calls: none
	 * @throws - if invalid parameter value is found
	 */
	@Override
	public void checkParams() throws OptionException {
		//ensure at least one directory is provided
		if (dirs.size() < 1) {
			throw new OptionException("directory must be provided, use -h option for help");
		}

		//ensure all directories are valid
		Iterator<File> d = dirs.iterator();
		while(d.hasNext()) {
			File dir = d.next();
			if (!dir.exists()) {
				throw new OptionException("directory does not exist: " + dir.getAbsolutePath());
			}
			if (!dir.isDirectory()) {
				throw new OptionException("can not use file as a directory, use -h option for help");
			}
			if (!dir.canRead()) {
				throw new OptionException("can't read from directory: " + dir.getAbsolutePath());
			}
		}
	}

	/**
	 * Gets thread to to handle http requests
	 * Called By: super.Srv()
	 *     Calls: WebSrvThread()
	 */
	@Override
	public Thread getThread(Socket sock) {
		WebSrvThread thread = new WebSrvThread(sock, accessLog, errorLog, debug, dirs);
		return thread;
	}
	
	/**
	 * Print start message for server providing basic info
	 * Called By: super.Srv()
	 *     Calls: none
	 */
	@Override
	public void printStartMessage() {
		System.out.println("Listening on port: " + portOpt.getValue());
		Iterator<File> d = dirs.iterator();
		while(d.hasNext()) {
			System.out.println("Serving dir: " + d.next().getAbsolutePath());
		}
	}
	
	/**
	 * Main method that runs program. Takes command line arguments
	 * @param args - options and parameters for program
	 * Calls: WebSrv(), Main.parseOptions(), Main.getHelp(), this.checkParams()
	 *        Srv.start()
	 */
	public static void main(String[] args) {
		// server set up
		WebSrv server = null;
		try {
			// load server
			server = new WebSrv("WebSrv", 
					"A simple HTTP webserver for rendering files using HTTP, "
					+ "allows multiple directorys to be provided. It will listen"
					+ "by default on port 80 which requires root privilages",
					"java tma1.WebSrv [options] <directory1> <directory2>"
					+ " <directory 3>...");

			// parse options
			server.parseOptions(args);

			// call help if option provided
			if (server.getHelp()) {
				server.printHelp();
				System.exit(0);
			}

			// check that at least one directory was provided
			server.checkParams();
			
		}
		catch(OptionException e) {
			printError(e.getMessage());
			System.exit(1);
		}
		
		// start server
		try {
			server.start();
		}
		catch (IOException e) {
			printError("failed to open port check port is not in use or user has correct permissions");
			System.exit(2);
		}
	}

}
