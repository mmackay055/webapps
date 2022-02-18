package webapps;
/**
 * Name: Srv.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description:  Abstract Class that provides members and methods to subclasses
 *               to implement a sever listening on a TCP socket. Extends the Main
 *               class to provide command line parsing functionality.
 * Inherits: Main
 * Inherited by: WebSrv, WebProxSrv
 */


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.SimpleFormatter;

import webapps.Option.OptionBool;
import webapps.Option.OptionException;
import webapps.Option.OptionInt;

/**
 * Class definition
 */
public abstract class Srv extends Main {
	OptionInt portOpt; // listen port option
	OptionBool debugOpt; // print debug logs
	Level debug; // sets debug level for threads
	ServerSocket sockSrv; // defines socket server listens on
	ExecutorService threads; // handles thread execution
	
	//logs for threads
	static Logger accessLog = Logger.getLogger("tma1.websrv.log.access");
	static Logger errorLog = Logger.getLogger("tma1.websrv.log.error");
	static final Level DEFAULT_LEVEL = Level.FINE;
	
	/**
	 * Static block sets up properties for logging
	 */
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format",
	              "[%1$tF %1$tT] [%4$-7s] %5$s %n");
		SimpleFormatter format = new SimpleFormatter();

		accessLog.addHandler(new StreamHandler(System.out, format));
		accessLog.setLevel(Level.INFO);
		
		errorLog.addHandler(new StreamHandler(System.err, format));
		errorLog.setLevel(Level.INFO);
	}
	
	/**
	 * Constructor of WebSrv initiates options and directories
	 * Called By: None
	 *     Calls: super(), OptionInt(), Executors.newCachedThreadPool(), Vector<File>()
	 */
	public Srv() throws OptionException {
		super();
		
		//initialize debug option
		debugOpt = new OptionBool("d", "Allow debug messages to be printed to error log");

		//add options
		addOption(debugOpt);
		
		//initialize thread pool
		threads = Executors.newCachedThreadPool();

		debug = DEFAULT_LEVEL;
	}

	/**
	 * Constructor of WebSrv initiates options and directories
	 * Called By: main()
	 *     Calls: super(String, String), OptionInt(), Executors.newCachedThreadPool(), Vector<File>()
	 * @param name - name of program
	 * @param desc - description of program
	 * @param usage - usage syntax of program
	 */
	public Srv(String name, String desc, String usage) throws OptionException {
		super(name, desc, usage);
		
		//initialize debug option
		debugOpt = new OptionBool("d", "Allow debug messages to be printed to error log");
		
		//add options
		addOption(debugOpt);
		
		//initialize thread pool
		threads = Executors.newCachedThreadPool();
		
		// initialize debug level
		debug = DEFAULT_LEVEL;
	}

	/**
	 * Sets directory parameters
	 * Called By: super.parseOptions()
	 *     Calls: File(), Vector<Option>.add(), Vector<File>.add()
	 * @param - url string 
	 */
	@Override
	public void setParam(String param) throws OptionException {
		params.add(param);
	}

	/**
	 * Sets options for web server
	 * 
	 * @param name - name of option
	 * @param value - value of option
	 * @throws OptionException - if error with option exists
	 */
	@Override
	public void setOption(String name, String value) throws OptionException {
		if (!options.containsKey(name))
			throw new OptionException(name + " is an invalid option");
		Option opt = options.get(name);
		opt.setValue(value);
		
		if (opt.equals(debugOpt) && ((OptionBool)opt).getValue()) {
			debug = Level.INFO;
		}
	}

	/**
	 * Starts the server listening on socket for requests
	 * @throws IOException
	 */
	public void start() throws IOException {
		sockSrv = new ServerSocket(portOpt.getValue());
		printStartMessage();
		serve();
	}

	/**
	 * Places server into infinite loop serving requests
	 * Called by: start()
	 *     Calls: SocketServer.accept(), ExecutorServer.submit()
	 */
	public void serve() {
		while(true) {
			try {
				Socket sock = sockSrv.accept(); // listen on socket, blocks
				
				// creates new thread
				Thread thread = getThread(sock);
				
				// submit thread for processing
				threads.submit(thread);
			}
			catch (IOException e) {
				errorLog.warning("failed to process socket");
			}
		}
	}
	
	/**
	 * Gets thread specific for subclass to handle socket communications
	 * @param sock - socket used for communications
	 * @return - returns thread to be submitted into processor
	 */
	public abstract Thread getThread(Socket sock);
	
	/**
	 * Print start message for server providing basic info
	 */
	public abstract void printStartMessage();
}
