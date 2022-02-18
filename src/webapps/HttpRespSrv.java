package webapps;
/**
 * Name: HttpRespSrv.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class that is used by WebSrvThread to create and 
 *  			send a HTTP response. Allows data to be sent from a
 *              File object or a String
 */


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.Socket;
import java.net.URLConnection;
import java.util.Date;

/**
 * Class definition
 */
public class HttpRespSrv {
	int code; // HTTP response code
	String codeMessage; // HTTP response message
	long contentLength; // length of content
	String contentType; // type of content
	String server; // name of server that is responding
	InputStream content; // stream to read content from
	Socket sock; // socket to write content to
	
	// carriage return and new line pair used to build response 
	final static byte[] CRN_PAIR = {'\r', '\n'};

	/**
	 * Class constructor to that initiates members in class
	 * Called by: this()
	 *     Calls: getCodeMessage()
	 * @param sock - socket to write response to
	 * @param server - name of server sending response
	 * @param code - HTTP response code
	 */
	public HttpRespSrv(Socket sock, String server, int code) {
		this.server = server;
		this.code = code;
		this.codeMessage = getCodeMessage(code);
		this.sock = sock;
	}

	/**
	 * Class constructor used when data sent in response is from
	 * a file.
	 * Called by: WebSrvThread.sendResponse()
	 *     Calls: BufferInputStream(), FileInputSteam(), 
	 *            URLConnection.getFileNameMap(),getContentTypeFor()
 	 * @param sock - socket to write response to
	 * @param server - name of server sending response
	 * @param code - HTTP response code
	 * @param content - File that is sent to client
	 * @throws FileNotFoundException - file is not found when trying to read from it
	 */
	public HttpRespSrv(Socket sock, String server, int code, File content) throws FileNotFoundException {
		this(sock, server, code); // initiate members
	
		// get input stream from File
		this.content = new BufferedInputStream(new FileInputStream(content));
		
		// set content length
		contentLength = content.length();
		
		// gets content type
		// adapted from Java Network Programming 4th Edition Elliotte Rusty Harold pg 326
		contentType = URLConnection.getFileNameMap().getContentTypeFor(content.getName());
		
		// check if type was found
		if (contentType == null) {
			contentType = "";
		}
	}
	
	/**
	 * Class constructor used when data sent if response is from string
	 * Called by: WebSrvThread.sendErrorResponse()
	 *     Calls: ByteArrayInputSteam()
 	 * @param sock - socket to write response to
	 * @param server - name of server sending response
	 * @param code - HTTP response code
	 * @param content - string to send to client
	 */
	public HttpRespSrv(Socket sock, String server, int code, String content) {
		this(sock, server, code);  // initiate members
		
		// get input stream from string
		this.content = new ByteArrayInputStream(content.getBytes());
		
		// set content length
		contentLength = content.length();
		
		// set content type
		contentType = "text/html";
	}

	/**
	 * Builds and sends a HTTP response to client. If error occurs
	 * while writing to client throws an IOException.
	 * Called by: WebSrvThread.sendErrorResponse(), WebSrvThread.sendResponse()
	 *     Calls: sendHeader(), InputStream.transferTo(), OutputStream.flush()
	 * @return - byte count written to client
	 * @throws IOException - if error occurs writing to client
	 */
	public long send() throws IOException {
		// get output stream from socket
		BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());

		//send header
		sendHeader(out);
		
		//send content
		long byteCount =  content.transferTo(out);
		
		// flush output to ensure all bytes are transmitted
		out.flush();
		
		return byteCount;
	}

	/**
	 * Sends header to client using provided OutputStream. Throws an IOException
	 * if error occurs while writing to client
	 * Called by: HttpRespSrv.send()
	 *     Calls: OutputStream.write(), OutputStream.flush()
	 * @param out - output stream to write header to
	 * @return byte count written
	 * @throws IOException - if error occurred writing to client
	 */
	private long sendHeader(OutputStream out) throws IOException {
		long byteCount = 0;
		
		// add header line
		String field = "HTTP/1.1 ";
		out.write(field.getBytes());
		String codeStr = String.valueOf(code); 
		out.write(codeStr.getBytes());
		out.write(" ".getBytes());
		out.write(codeMessage.getBytes());
		out.write(CRN_PAIR);
		byteCount += field.length() + codeStr.length() + 1 + codeMessage.length() + CRN_PAIR.length;
		
		// add date line
		field = "Date: ";
		out.write(field.getBytes());
		Date cur = new Date();
		byte[] curByte = cur.toString().getBytes();
		out.write(curByte);
		out.write(CRN_PAIR);
		byteCount += field.length() + curByte.length + CRN_PAIR.length;
		
		//add server line
		field = "Server: ";
		out.write(field.getBytes());
		out.write(server.getBytes());
		out.write(CRN_PAIR);
		byteCount += field.length() + server.length() + CRN_PAIR.length;
		
		//add connection line
		field = "Connection: close";
		out.write(field.getBytes());
		out.write(CRN_PAIR);
		byteCount += field.length() + CRN_PAIR.length;
		
		//add content-type line
		field = "Content-type: ";
		out.write(field.getBytes());
		out.write(contentType.getBytes());
		out.write(CRN_PAIR);
		byteCount += field.length() + contentType.length() + CRN_PAIR.length;
				
		//add content-length line
		field = "Content-length: ";
		out.write(field.getBytes());
		String contentLengthStr = String.valueOf(contentLength);
		out.write(contentLengthStr.getBytes());
		out.write(CRN_PAIR);
		byteCount += field.length() + contentLengthStr.length() + CRN_PAIR.length;
		
		//add extra CRN pair to indicate end of header
		out.write(CRN_PAIR);
		byteCount += CRN_PAIR.length;
		
		out.flush(); // flush bytes
				
		return byteCount;
	}
	
	/**
	 * Gets response message based on code passed to it. If
	 * code is not known a "Unknown code" message is returned
	 * Called By: HttpRespSrv(), WebSrvThread.sendErrorResponse()
	 * @param code - HTTP response code
	 * @return string HTTP response message
	 */
	public static String getCodeMessage(int code) {
		switch (code) {
		case 200:
			return "OK";
		case 400:
			return "Bad Request";
		case 403:
			return "Forbidden";
		case 404:
			return "Not Found";
		case 405:
			return "Method Not Allowed";
		case 408:
			return "Request Timeout";
		case 500:
			return "Internal Server Error";
		default:
			return "Unknown code";
		}
	}
}
