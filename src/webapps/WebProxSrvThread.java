package webapps;
/**
 * Name: WebProxSrvThread.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class that extends SrvThread and handles HTTP requests for WebProxSrv.
 *              Creates two threads one for client-server communications the other for
 *              server-client communications. Logs all communications and any errors 
 *              that occur.
 *              
 * Inherits: SrvThread
 */


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class definition
 */
public class WebProxSrvThread extends SrvThread {
	Socket sockSrv; // socket for webserver communications
	InetAddress srvAdd; // address of webserver
	int srvPort; // tcp port of webserver
	ExecutorService threads; // executes communication threads
	
	/**
	 * Initializes thread by initializing members and creating
	 * a fixed thread pool of 2
	 * Called by: WebProxSrv.getThread(),
	 *     Calls; Executors.newFixedThreadPool()
	 * @param sock - client socket
	 * @param accessLog - log for logging accesses
	 * @param errorLog - log for logging errors
	 * @param debug - Log Level to indicate if debug logging should occur
	 */
	public WebProxSrvThread(Socket sock,
			Logger accessLog, 
			Logger errorLog, 
			Level debug, 
			InetAddress srvAdd, 
			int srvPort) {
		super(sock, accessLog, errorLog, debug);
		SERVER_NAME = "WebProxSrv"; // name used in logs
		this.srvAdd = srvAdd;
		this.srvPort = srvPort;
		threads = Executors.newFixedThreadPool(2);
	}

	/**
	 * Override function from SrvThread that is executed by WebProxSrv
	 * ExecutorService. Opens connection to the web server and then creates
	 * two threads one for client-server communications the other for
	 * server-client communications. Once communication threads complete 
	 * they are logged or any errors that occur are logged.
	 * 
	 * Called by: WebProxSrv ExecutorService
	 *     Calls: this.createDebugLog(), this.createErrorLog(), Socket(),
	 *            Socket.getInetAddress(), ExecutorService.submit(), this.handle(),
	 *            Socket.close()
	 */
	@Override
	public void run() {
		try {
			
			createDebugLog("connecting to server");
			
			// open server socket
			try {
				sockSrv = new Socket(srvAdd, srvPort);
			}
			catch (IOException e) {
				createErrorLog("failed to connect to webserver" + 
						sockSrv.getInetAddress().getHostAddress());
				throw new SrvThreadException(); // kill thread
			}
			
			createDebugLog("connected to server");

			createDebugLog("submit communication threads");
			
			// submit thread for client to server communications
			Future<Result> cliSrvFuture = 
					threads.submit(new WebProxSrvForwardThread(sock, 
															  sockSrv, 
															  "client to server"));
			
			// submit thread for server to client communications
			Future<Result> srvCliFuture = 
					threads.submit(new WebProxSrvForwardThread(sockSrv, 
															  sock, 
															  "server to client"));
			
			createDebugLog("submitted communication threads");
			
			createDebugLog("handle client to webserver future");
			
			// handle result from client to server communications
			handle(cliSrvFuture.get());
			
			createDebugLog("handled client to webserver future");
			
			createDebugLog("handle webserver to client future");
			
			// handle result from server to client communications
			handle(srvCliFuture.get());
			
			createDebugLog("handled webserver to client future");
		}
		catch (Exception e) {
			// log any failures that occur
			createErrorLog("thread failure: " + e.getMessage());
		}
		finally {
			// close sockets with client and webserver
			try {
				sock.close();
				sockSrv.close();
			}
			catch (IOException e) {
				errorLog.warning("failed to close sockets");
			}
		}
	}
	
	/**
	 * Handles the results of the the communication threads between
	 * client and webserver which includes creating access or error
	 * logs depending on the result
	 * Called By: this.run()
	 *     Calls: this.createErrorLog(), this.createAccessLog()
	 * @param result - Result object with information about session
	 */
	private void handle(Result result) {
		if (result.isErr()) {
			// errors occurred create error log
			createErrorLog(result);	
		}
		else {
			// no errors occurred create access log
			createAccessLog(result);
		}
	}
	
	/**
	 * Create an access log which includes the time, source, destination
	 * and bytes transmitted.
	 * Called by: this.handle()
	 *     Calls: StringBuilder(), InputStream and OutputStream get functions,
	 *            Logger.log()
	 * @param result - Result object with information about session
	 */
	private void createAccessLog(Result result) {
		StringBuilder log = new StringBuilder ("Forwared ");
		log.append(result.byteCount);
		log.append(" bytes from: ");
		log.append(result.in.getInetAddress().getHostAddress());
		log.append(":");
		log.append(result.in.getPort());
		log.append(" to: ");
		log.append(result.out.getInetAddress().getHostAddress());
		log.append(":");
		log.append(result.out.getPort());
		accessLog.log(Level.INFO, log.toString()); // send string to log
	}
	
	/**
	 * Create an error log by logging the error message from the session
	 * Called by: this.handle()
	 *     Calls: Logger.log()
	 * @param result - Result object with information about session
	 */
	private void createErrorLog(Result result) {
		errorLog.log(Level.WARNING, result.errMsg);
	}
	
	/**
	 * Simple class used to return the results of the communication
	 * in a WebProxSrvForwardThread. Only used for information.
	 *
	 */
	public static class Result {
		Socket in; // socket data was read from
		Socket out; // socket data was written to
		long byteCount; // bytes transmitted
		String errMsg; // error message
		boolean err; // indicate error had occured
		
		/*
		 * Class constructor. Initialize members.
		 */
		public Result(Socket in, Socket out) {
			this.in = in;
			this.out = out;
			byteCount = 0;
			err = false;
		}

		/**
		 * Get byteCount
		 * @return - amount of bytes
		 */
		public long getByteCount() {
			return byteCount;
		}

		/**
		 * Set byteCount
		 * @param byteCount - amount of bytes
		 */
		public void setByteCount(long byteCount) {
			this.byteCount = byteCount;
		}

		/**
		 * Get errMsg
		 * @return - error msg string
		 */
		public String getErrMsg() {
			return errMsg;
		}

		/**
		 * Set errMsg and set err member to true
		 * @param errMsg - error msg string
		 */
		public void setErrMsg(String errMsg) {
			err = true;
			this.errMsg = errMsg;
		}

		/**
		 * indicate if error occurred
		 * @return boolean indicating error occurred
		 */
		public boolean isErr() {
			return err;
		}
	}
}
