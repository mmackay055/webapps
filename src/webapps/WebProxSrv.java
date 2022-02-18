package webapps;
/**
 * Name: WebProxSrv.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class that implements a simple web proxy for web servers.
 *              Listens for requests from clients and forwards requests to
 *              the web server. Each request is handled by a thread which
 *              then establishes two more threads one for client to server
 *              communications the other for server to client communications.
 *              Starts from the command line and accepts arguments and option
 *              flags. Run with -h to see help manual.
 *              
 *              Basic usage:
 *              	java tma1.WebProxSrv [options] <webserver ip address>
 *              
 * Inherits: Srv
 */


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import webapps.Option.OptionException;
import webapps.Option.OptionInt;

/**
 * Class definition
 */
public class WebProxSrv extends Srv {

	InetAddress srvAdd; // address of web server
	OptionInt srvPort; // listening port of web server
	
	/**
	 * Constructor of WebProxSrv, initiates options with default values
	 * @throws OptionException
	 * Called By: None
	 *     Calls: super(), OptionInt(), Main.addOption()
	 */
	public WebProxSrv() throws OptionException {
		super();
		
		//initialize port option with default value of 8080
		portOpt = new OptionInt("p", 
				"Set port that proxy server listens on, default: 8080", 
				"8080");
		
		//initialize server port option with default value of 80
		srvPort = new OptionInt("s", 
				"Set port that the webserver is listening on, default: 80", 
				"80");
		
		//add options
		addOption(portOpt);
		addOption(srvPort);
		
	}

	/**
	 * Constructor of WebProxSrv, initiates options with default values
	 * and sets name and description of program for the help manual
	 * Called By: main()
	 *     Calls: super(String, String), OptionInt(), Vector<File>()
	 * @param name - name of program
	 * @param desc - description of program
	 * @param usage - usage syntax of program
	 * @throws OptionException - if default options fail to set
	 */
	public WebProxSrv(String name, String desc, String usage) throws OptionException {
		super(name, desc, usage);
		
		//initialize port option with default value of 8080
		portOpt = new OptionInt("p", 
				"Set port that proxy server listens on, default: 8080", 
				"8080");
		
		//initialize server port option with default value of 80
		srvPort = new OptionInt("s", 
				"Set port that the webserver is listening on, default: 80", 
				"80");
		
		//add options
		addOption(portOpt);
		addOption(srvPort);
				
	}

	/**
	 * Sets the server ip address parameter only allows.
	 * If called multiple times ip address will be overwritten
	 * Called By: super.parseOptions()
	 *     Calls: super.setParam(), InetAddress.getByName()
	 * @param - string value of ip address or hostname
	 * @OptionException - if ip/hostname is invalid
	 */
	@Override
	public void setParam(String param) throws OptionException {
		super.setParam(param);

		try {
			srvAdd = InetAddress.getByName(param);
		}
		catch (UnknownHostException e) {
			throw new OptionException("unknown host address: " + param); 
		}
	}
	
	/**
	 * Ensures the server address is set otherwise throws an exception
	 * Called By: main()
	 *     Calls: none
	 * @OptionException - if srvAdd member is null
	 */
	@Override
	public void checkParams() throws OptionException {
		if (srvAdd == null) {
			throw new OptionException("webserver address parameter must be specified");
		}
	}
	
	/**
	 * Returns a thread to handle the client-server communications
 	 * Called By: super.Srv()
	 *     Calls: WebProxSrvThread()
	 * @sock - Socket to communicate with the client through
	 */
	@Override
	public Thread getThread(Socket sock) {
		// create thread to handle communications between client and server
		WebProxSrvThread thread = new WebProxSrvThread(sock, 
				accessLog, 
				errorLog, 
				debug, 
				srvAdd, 
				srvPort.getValue());
		return thread;
	}

	/**
	 * Prints a starting message on command line to provide
	 * Called By: super.Srv()
	 *     Calls: none
	 * basic information
	 */
	@Override
	public void printStartMessage() {
		System.out.println("Listening on port: " + portOpt.getValue());
		System.out.println("Forwarding to " + srvAdd.getHostName() + 
				":" + srvPort.getValue());
	}

	/**
	 * Main method that runs program. Takes command line arguments
	 * Calls: WebProxSrv(), Main.parseOptions(), Main.getHelp(), this.checkParams()
	 *	      Srv.start()
	 * @param args - options and parameter for program
	 */
	public static void main(String[] args) {
		// server set up
		WebProxSrv server = null;
		try {
			// load server
			server = new WebProxSrv("WebProxSrv", 
					"A simple proxy server for forwarding HTTP requests "
					+ "from clients to a webserver. By default the server will listen on "
					+ "port 8080 and will attempt to connect to the server on port 80.",
					"java tma1.WebProxSrv [options] <webserver ip address>");

			// parse options
			server.parseOptions(args);

			// call help if option provided
			if (server.getHelp()) {
				server.printHelp();
				System.exit(0);
			}

			// check that server ip was provided
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
