package webapps;
/**
 * Name: HttpReq.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class that reads HTTP request from socket and parses it,
 *              any errors that are found cause an HttpException or if
 *              reading the exception fails an IOException is thrown
 */


import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import webapps.HttpHeader.HttpHeaderException;

/**
 * Class definition
 */
public class HttpReq {
	InputStream req; // requests input stream
	String resource; // resource name
	String method; // HTTP method name
	String version; // HTTP version
	Socket sock; // socket where request is read from

	/**
	 * Class constructor that takes a socket object and opens the 
	 * input stream for the socket
	 * Called by: WebSrvThread.run()
	 *     Calls: Socket.getInputStream()
	 * @param sock - socket to read HTTP request from
	 * @throws IOException - if opening stream to socket fails
	 */
	public HttpReq(Socket sock) throws IOException {
		this.sock = sock; 
		req = sock.getInputStream();
	}

	/**
	 * Reads HTTP request from req member and parses the header.
	 * Checks to make sure the request is not malformed.
	 * Called by: WebSrvThread.run()
	 *     Calls; HttpHeader(), HttpHeader.parse(), HttpHeader.getField()
	 * @throws HttpException - if request is malformed
	 * @throws IOException - if failed to read request
	 */
	public void parse() throws HttpException, IOException {
		HttpHeader header = new HttpHeader(req);
		try {
			header.parse();
		}
		catch (HttpHeaderException e) {
			throw new HttpException(e.getLocalizedMessage());
		}
		
		method = header.getField("head1");
		resource = header.getField("head2");
		version = header.getField("head3");
	}
	
	/**
	 * Class used to raise exceptions when there are errors with the request
	 */
	public static class HttpException extends Exception {

		/**
		 * Required to serialize object 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor 
		 */
		public HttpException() {}

		/**
		 * Constructor that sets exception message
		 * @param message - message of exception
		 */
		public HttpException(String message) {
			super(message);
		}
	}

	/**
	 * Gets requested resource name
	 * @return - resource string
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Gets HTTP method of request
	 * @return - method string
	 */
	public String getMethod() {
		return method;
	}
}
