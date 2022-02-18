package webapps;
/**
 * Name: HttpResp.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 28, 2021
 * Description: Subclass of Resp specific for HTTP/HTTPS responses
 * 
 * Inherits Resp
 */

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Class definition
 */
public class HttpResp extends Resp {
	String encodingType; // type of encoding of response currently not used
	String charset; // charset of response
	long contentLength;
	HttpURLConnection urlc; // connection object of response
	int code;
	HttpHeader header;
	
	/**
	 * Class constructor that takes a URL object and retrieves content from 
	 * url. Any problem with connecting to host or reading content will throw
	 * exceptions
	 * Called by: WebCli.sendRequest()
	 *     Calls: checkResp(), BufferedInputStream(), URL.getInputStream()
	 * @param url - URL object that represents location reading from
	 * @throws IOException - Thrown if reading from host fails
	 * @throws RespException - Thrown if error occurs specific to HTTP
	 */
	public HttpResp(URL url) throws IOException, RespException {
		urlc = (HttpURLConnection)url.openConnection(); //make http connection

		checkResp();//checks response code, throws exception if error
		
		// get input stream for content
		content = new BufferedInputStream(urlc.getInputStream());

		//Set charset of response if it is specified in header
		if (contentType.contains("charset=")) {
			String[] arr = contentType.split("charset=");
			charset = (arr.length > 1) ? arr[1] : "";
		}
		else {// charset not specified, set to empty string
			charset = "";
		}
	}

	/**
	 * Gets HTTP content from input stream and passes it through output stream,
	 * if charset was specified uses Reader to read content otherwise just 
	 * reads bytes 
	 * Called by: WebCli.readResponse()
	 *     Calls: InputStreamReader(), InputStream.transferTo(), OutputStream.flush()
	 *            Reader.read(), OutputStream.write()
	 * @param out - output stream to send content
	 * @throws UnsupportedEncodingException - if encoding is not supported by client
	 * @throws IOException - if host is unreachable
	 */
	@Override
	public void getContent(OutputStream out) throws UnsupportedEncodingException, IOException {
		int c;
		//check if charset is specified
		if (charset.length() > 0) { // charset is specified use reader
			Reader r = new InputStreamReader(content, charset);
			while ((c = r.read()) != -1) { // read all content
				out.write((char)c);// write to output
			}
		}
		else {	
			content.transferTo(out); // write to output
		}
		out.flush(); // flush any bytes remaining in buffer
	}
	
	/**
	 * Gets content and returns it as a string by using a string buffer to read
	 * content then returns string
	 * Called By: WebCli.readResponse()
	 *     Calls: InputStreamReader(), Reader.read()
	 * @return content as string
	 * @throws UnsupportedEncodingException - if encoding is not supported by client
	 * @throws IOException - if host is unreachable
	 */
	@Override
	public String getContent() throws UnsupportedEncodingException, IOException {
		StringBuilder buf = new StringBuilder();
		int c;
		//check if charset is specified
		if (charset.length() > 0) { // charset is specified use reader
			Reader r = new InputStreamReader(content, charset);
			while ((c = r.read()) != -1) {
				buf.append((char)c); //add to string buffer
			}
		}
		else {
			while((c = content.read()) != -1) {
				buf.append((char)c); //add to string buffer
			}
		}
		return buf.toString(); // return string
	}

	/**
	 * Get encoding type of response
	 * @return
	 */
	public String getEncodingType() {
		return encodingType;
	}

	/**
	 * Set encoding type of response
	 * @param encodingType
	 */
	public void setEncoding(String encodingType) {
		this.encodingType = encodingType;
	}

	/**
	 * Checks the status of the response throws exception if problem occurs.
	 * Follows provided url if permanent move response code is returned by
	 * creating new connection. If response code is less than 200 or greater
	 * than 399 throws exception with response code message.
	 * Called by: HttpResp()
	 *     Calls: URL.openConnection(), URL()
	 * @throws RespException - HTTP response code is not valid
	 * @throws IOException - failed to connect to client
	 */
	@Override
	public void checkResp() throws RespException, IOException {
		code = urlc.getResponseCode();
		
		if (code == 301) {// check for moved permanent redirect
			URL url = new URL(urlc.getHeaderField("Location"));
			urlc = (HttpURLConnection)url.openConnection();// create new connection with moved url
		}
		else if (code < 200 || code > 399) { // check for response code
			throw new RespException(code + " " + urlc.getResponseMessage());
		}
	}

	/**
	 * Close connection to host
	 * Called by: WebCli.close()
	 *     Calls: URLConnection.disconnect()
	 */	
	@Override
	public void close() {
		urlc.disconnect();
	}
}
