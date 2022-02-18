package webapps;
/**
 * Name: Resp.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 28, 2021
 * Description: Abstract class for handling responses from URL requests
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Class definition
  */
public abstract class Resp {
	InputStream content; // input stream of response
	String contentType; // holds content type of response

	/**
	 * Class constructor ensures contentType defaults to empty string
	 */
	public Resp() {
		contentType = "";
	}

	/**
	 * Class constructor that sets content type
	 * @param contentType
	 */
	public Resp(String contentType) {
		setContentType(contentType);
	}

	/**
	 * Gets contentType member
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets contentType member
	 * @param contentType - new content type string
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets content and passes it through output stream 
	 * @param out - output stream to send content
	 * @throws UnsupportedEncodingException - if encoding is not supported by client
	 * @throws IOException - if host is unreachable
	 */
	public abstract void getContent(OutputStream out) throws UnsupportedEncodingException, IOException;

	/**
	 * Gets content and returns it as a string
	 * @return content as string
	 * @throws UnsupportedEncodingException - if encoding is not supported by client
	 * @throws IOException - if host is unreachable
	 */
	public abstract String getContent() throws UnsupportedEncodingException, IOException;

	/**
	 * Checks the status of the response throws exception if problem occurs
	 * @throws RespException - error occured when getting content
	 * @throws IOException - failed to connect to client
	 */
	public abstract void checkResp() throws RespException, IOException;

	/**
	 * Close connection to host
	 */
	public abstract void close();
	
	/**
	 * Class used to raise exceptions when there are errors with the response 
	 */
	public static class RespException extends Exception {

		/**
		 * Required to serialize object 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor 
		 */
		public RespException() {
		}

		/**
		 * Constructor that sets exception message
		 * @param message - message of exception
		 */
		public RespException(String message) {
			super(message);
		}
	}
}
