package webapps;
/**
 * Name: WebSrvThread.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class that implements Runnable and is used to handle
 *              HTTP requests received by WebSrv. It reads the client's
 *              request than response with the appropriate content, and
 *              HTTP header
 *              
 * Inherits: SrvThread
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import webapps.HttpReq.HttpException;

/**
 * Class definition
 */
public class WebSrvThread extends SrvThread {

	Vector<File> dirs; // vector of directories for thread to search for files
	HttpReq req; // request from client

	/**
	 * Class constructor that initiates the class members  
	 * Called by: WebSrv.getThread()
	 *     Calls: super()
	 * @param sock - socket used for HTTP session
	 * @param dirs - vector of directories for thread to search for files 
	 * @param accessLog - log that stores web server accesses
	 * @param errorLog - log to store any errors that occur processing request
	 * @param debug - stores information about HTTP request
	 */
	public WebSrvThread(Socket sock, 
			Logger accessLog, 
			Logger errorLog, 
			Level debug, 
			Vector<File> dirs) {
		super(sock, accessLog, errorLog, debug);
		SERVER_NAME = "WebSrv";
		this.dirs = dirs;
	}

	/**
	 * Override function from SrvThread that is executed by
	 * WebSrv ExecutorService. Starts by reading request from client,
	 * then checks for any errors with request. If error is found, such
	 * as a file not existing, then an error response is returned to
	 * client. If no errors occur then the requested file will be sent
	 * to client.
	 * Called by: WebSrv ExecutorService
	 *     Calls: Socket.setSoTimeOut(), createDebugLog(), HttpReq(),
	 *            HttpReq.parse(), sendResponse(), sendErrorResponse(),
	 *            createAccessLog(), Socket.close()
	 */
	@Override
	public void run() {
		//Main try block that catches any uncaught errors that are then logged
		try {
			// set socket timeout for non responsive clients
			try {
				sock.setSoTimeout(2000);// raise exception if read blocks 2 seconds
			} catch (SocketException e) {
				throw new Exception("set socket timeout failed");
			}
	
			
			createDebugLog("Starting thread for client: " 
		+ sock.getInetAddress().getHostAddress());
			
			int code = -1; // stores HTTP status code to send in response
			long byteCount = -1; // stores byte count of response

			
			// creates HttpReq which opens socket to read from
			try {
				req = new HttpReq(sock);
			}
			catch (IOException e) {
				errorLog.warning("Failed to open client socket");
			}

			createDebugLog("Request parsing");

			// reads and parses request from client
			try {
				req.parse();
			}
			catch (SocketTimeoutException e) {
				code = 408;
				byteCount = sendErrorResponse(code);
			}
			catch (IOException e) {
				errorLog.warning("Failed to read from client socket");
			}
			catch (HttpException e) { // client sent malformed request
				code = 400;
				byteCount = sendErrorResponse(code);
			}

			
			createDebugLog("Request parsed");

			// check GET is requested
			if (code == -1 && !req.getMethod().contentEquals("GET")) {
				createDebugLog("method: " + req.getMethod());
				code = 405;
				byteCount = sendErrorResponse(code);
			}

			// loops through directories to search for requested file
			File resource = null;
			if (code == -1) {
				Iterator<File> d = dirs.iterator();
				while (d.hasNext()) {
					
					File dir = d.next();
					
					createDebugLog("searching for " + req.getResource() + 
							"in dir: " + dir.getAbsolutePath());
					
					// check if file exists in directory break loop if found
					File file = new File(dir, req.getResource());
					if (file.exists()) {
						resource = file;
						break;
					}
				}
			}

			// check if requested file existed
			if (code == -1 && resource == null) {
				code = 404;
				byteCount = sendErrorResponse(code);
			}

			// check if file can be read by server
			if (code == -1 && !resource.canRead()) {
				code = 403;
				byteCount = sendErrorResponse(code);
			}

			try {
				// builds response with file contents and sends to client				
				if (code == -1) {
					code = 200;
					byteCount = sendResponse(code, resource);
				}
				
				// log access if bytes were written to client
				if (byteCount > -1) {
					createAccessLog(code, byteCount);
				}
			}
			catch (FileNotFoundException e) {
				int codeErr = 404;
				long byteCountErr = sendErrorResponse(codeErr);
				createAccessLog(code, byteCountErr);
			}
			catch (IOException e) {
				errorLog.warning("failed writing to client: " + 
			sock.getInetAddress().getHostAddress());
			}
		}
		catch (Exception ee) {
			createErrorLog ("thread failure: " + ee.getMessage());
			ee.printStackTrace();
		}
		finally {
			// close socket with client
			try {
				sock.close();	
			}
			catch (IOException e) {
				errorLog.warning("failed to close socket");
			}
		}
	}

	/**
	 * Builds and sends response to client. HTTP header is first sent to client
	 * then followed by the file requested by client
	 * Called by: run()
	 *     Calls: createDebugLog(), HttpRespSrv(), HttpResp.send()
	 * @param code - HTTP response code
	 * @param resource - file to send to client
	 * @return - the byte count written to client
	 * @throws FileNotFoundException - if file could not be found
	 * @throws IOException - if writing to client fails
	 */
	private long sendResponse(int code, File resource)
			throws FileNotFoundException, IOException{
		createDebugLog("Build response with: " + resource.getAbsolutePath());

		// build HTTP response
		HttpRespSrv resp = new HttpRespSrv(sock, SERVER_NAME, code, resource);
		
		createDebugLog("Send response");
		
		// send HTTP response and return byte sent count
		return resp.send();
	}

	/**
	 * Builds and sends error response to client. HTTP header is first sent to client
	 * then followed by simple HTML file
	 * Called by: run()
	 *     Calls: createDebugLog(), HttpRespSrv(), HttpResp.send(), StringBuilder()
	 * @param code - HTTP response code
	 * @return - the byte count written to client
	 */
	private long sendErrorResponse(int code) {
		createDebugLog("Send error response");
		
		// build html to send to client
		StringBuilder content = new StringBuilder("<!DOCTYPE html><html><head></head><body>");
		content.append(HttpRespSrv.getCodeMessage(code));
		content.append("</body></html>");


		// build HTTP response
		HttpRespSrv resp = new HttpRespSrv(sock, SERVER_NAME, code, content.toString());

		// send HTTP response
		long byteCount = 0;
		try {
			byteCount = resp.send();
		}
		catch (IOException e) {
			errorLog.warning("failed writing error message to client: " + 
		sock.getInetAddress().getHostAddress());
		}
		return byteCount; // return byte sent count
	}

	/**
	 * Creates an access log
	 * Called by: run()
	 *     Calls: Logger.log(), StringBuilder()
	 * @param code - HTTP response code
	 * @param byteCount - byte count written to client
	 */
	protected void createAccessLog(int code, long byteCount) {
		StringBuilder log = new StringBuilder (sock.getInetAddress().getHostAddress());
		log.append(" ");
		log.append(code);
		log.append(" ");
		log.append(byteCount);
		log.append(" ");
		log.append(req.getResource());	
		accessLog.log(Level.INFO, log.toString());
	}
}
