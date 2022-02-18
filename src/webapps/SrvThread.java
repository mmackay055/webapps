package webapps;
/**
 * Name: SrvThread.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Abstract Class that extends Thread and is used to handle
 *              TCP socket connections received by server program. It 
 *              provides members and methods for a basic server thread
 *              to handle communications
 *              
 * Inherits: Thread
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class definition
 */
public abstract class SrvThread extends Thread {
	static String SERVER_NAME = "Srv"; // name of server program calling thread
	Level debug; // sets level for debugging
	
	Socket sock; // socket used for communication
	Logger accessLog; // log that stores server accesses
	Logger errorLog; // log to store any errors that occur communication
		
	/**
	 * Class constructor that initiates the class members  
	 * Called By: WebSrvThread(), WebProxSrvThread()
	 *     Calls: none
	 * @param sock - socket used for communication session
	 * @param accessLog - log that stores server accesses
	 * @param errorLog - log to store any errors that during session
	 * @param debug - indicates level to for debugging
	 */
	public SrvThread(Socket sock, Logger accessLog, Logger errorLog, Level debug) {
		this.sock = sock;
		this.accessLog = accessLog;
		this.errorLog = errorLog;
		this.debug = debug;
	}

	/**
	 * Gets input stream from socket and logs any errors opening stream
	 * Called by: none
	 *     Calls: Socket.getInputStream(), BufferedInputStream, this.createErrorLog()
	 * 
	 * @param sock - socket to get stream from
	 * @param failMsg - message to log if getting stream fails
	 * @return - InputStream from socket
	 * @throws SrvThreadException - if failed to get stream
	 */
	protected InputStream getInputStream(Socket sock, String failMsg) throws SrvThreadException {
		InputStream str;
		try {
			str = new BufferedInputStream(sock.getInputStream());
		}
		catch (IOException e) {
			createErrorLog(failMsg);
			throw new SrvThreadException();
		}
		return str;
	}
	
	/**
	 * Gets output stream from socket and logs any errors opening stream
	 * Called by: none
	 *     Calls: Socket.getOutputStream(), BufferedOutputStream, this.createErrorLog()
	 * @param sock - socket to get stream from
	 * @param failMsg - message to log if getting stream fails
	 * @return - OutputStream from socket
	 * @throws SrvThreadException - if failed to get stream
	 */
	protected OutputStream getOutputStream(Socket sock, String failMsg) throws SrvThreadException {
		OutputStream str;
		try {
			str = new BufferedOutputStream(sock.getOutputStream());
		}
		catch (IOException e) {
			createErrorLog(failMsg);
			throw new SrvThreadException();
		}
		return str;
	}
	
	/**
	 * Creates a debug log that starts with server name in order to filter logs
	 * Called by: WebSrvThread.run(), WebProxSrvThread.run()
	 *     Calls: Logger.log()
	 * @param msg - message to send to debug log
	 */
	protected void createDebugLog(String msg) {
		errorLog.log(debug, SERVER_NAME + ": " + msg);
	}
	
	/**
	 * Creates an error log
	 * Called by: SrvThread.getInputStream(), SrvThread.getOutputStream(),
	 *            WebSrvThread.run(), WebProxSrvThread.run()
	 *     Calls: Logger.log()
	 * 
	 * @param msg - message to send to error log
	 */
	protected void createErrorLog(String msg) {
		errorLog.log(Level.WARNING, msg);
	}
	
	/**
	 * Class used to raise exceptions when there are errors with the session 
	 */
	public static class SrvThreadException extends Exception {

		/**
		 * Required to serialize object 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor 
		 */
		public SrvThreadException() {}

		/**
		 * Constructor that sets exception message
		 * @param message - message of exception
		 */
		public SrvThreadException(String message) {
			super(message);
		}
	}
}
