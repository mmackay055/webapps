package webapps;
/**
 * Name: HttpHeader.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class used to read and parse an HTTP header from an InputStream.
 *              HTTP header values are stored in a HashMap with the keys being
 *              the field name in lower case. The first line of the header is stored
 *              as head1 head2 and head3.
 */


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class HttpHeader {
	
	InputStream in; // input stream header is read from
	
	HashMap<String, String> fields; // stores fields

	/**
	 * Initializes members
	 * Called by: HttpReq.parse(),
	 *     Calls: HashMap<String, String>()
	 * @param in - input stream to read header from
	 */
	public HttpHeader(InputStream in) {
		this.in = in;
		fields = new HashMap<String, String>(); // initialize map
	}
	
	/**
	 * Reads and parses header and stores values into fields map. If
	 * header is malformed or an error occurs reading from stream
	 * an exception will be thrown
	 * Called by: HttpReq.parse()
	 *     Calls: StringBuilder(), setFirstLine(), setField()
	 * @throws IOException - if error occurs reading from stream
	 * @throws HttpHeaderException - if header is malformed
	 */
	public void parse()  throws IOException, HttpHeaderException {
		int c;
		StringBuilder line = new StringBuilder();
		
		// read first line of request
		while ((c = in.read()) != -1 && (char)c != '\r' && (char)c != '\n') {
			line.append((char)c);
		}
		
		c = in.read(); // read next byte
		if ((char)c != '\n') { // make sure byte is a line feed character
			throw new HttpHeaderException("header malformed incorrect line"
											+ " terminating character: " + (char)c);
		}

		// split first line into array
		String[] params = line.toString().split(" ", 3);
		
		// set parameters from first line
		setFirstLine(params);

		// read rest of the request
		while (true) {
			line = new StringBuilder();
			
			// read line
			while ((c = in.read()) != -1 && (char)c != '\r' && (char)c != '\n') {
				line.append((char)c);
			}
			
			// check if communication before terminating characters were read
			if (c == -1) {
				throw new HttpHeaderException("failed to read entire header");
			}
			
			c = in.read(); // read next byte
			
			if ((char)c != '\n') { // make sure byte is a line feed character
				throw new HttpHeaderException("header malformed incorrect line "
												+ "terminating character: " + (char)c);
				
			}
			
			// parse line
			String lineStr = line.toString();
			if (lineStr.length() == 0) { // end of header is found
				break; // break loop since entire header was found
			}
			else {
				String[] pair = lineStr.split(": ", 2); // split line into field name an value
				setField(pair); // set field
			}
		}
	}
		
	/**
	 * Sets the parameters for the first line. Checks to make sure line only has 
	 * 3 elements or throws an exception. Adds parameters to map with keys head1, 
	 * head2, head3.
	 * Called by: parse()
	 *     Calls: none
	 * @param params - 3 element string array
	 * @throws HttpHeaderException - if array isn't 3 elements long
	 */
	public void setFirstLine(String[] params) throws HttpHeaderException {
		if (params.length != 3) {
			throw new HttpHeaderException("header malformed not enough elements first line");
		}
		
		// add params
		for (int i = 0; i < params.length; i++) {
			fields.put("head" + (i + 1), params[i]);
		}
		
	}
	
	/**
	 * Sets field from pair of strings provided. First element is field name second
	 * is field value. Throws exception if array isn't 2 elements long
	 * Called by: parse()
	 *     Calls: addField()
	 * @param pair - 2 element string array
	 * @throws HttpHeaderException - if array isn't 2 elements long
	 */
	public void setField(String[] pair) throws HttpHeaderException {
		if (pair.length != 2) { // check length of array
			throw new HttpHeaderException("invalid header field format");
		}
		else {
			addField(pair); // add field to fields map
		}
	}

	/**
	 * Adds pair to fields map with the first element as a key and the second
	 * as the value. The key is stored as lower case.
	 * Called by: setField()
	 *     Calls: none
	 * @param pair - 2 element string array 
	 */
	public void addField(String[] pair) {
		fields.put(pair[0].toLowerCase(), pair[1]);	
	}
	
	/**
	 * Gets the value of the field from the field name provided to it. All keys
	 * are stored as lower case.
	 * @param name - name of field
	 * @return - value of field as string
	 */
	public String getField(String name) {
		return fields.get(name);
	}
	
	/**
	 * Class used to raise exceptions when there are errors with the response 
	 */
	public static class HttpHeaderException extends Exception {

		/**
		 * Required to serialize object 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor 
		 */
		public HttpHeaderException() {}

		/**
		 * Constructor that sets exception message
		 * @param message - message of exception
		 */
		public HttpHeaderException(String message) {
			super(message);
		}
	}
}
