package webapps;
/**
 * Name: FtpResp.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 28, 2021
 * Description: Subclass of Resp specific for FTP responses
 * 
 * Inherits: Resp
 */


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class definition
 */
public class FtpResp extends Resp {
	long contentLength; // length of content
	URLConnection urlc; // connection of response
	
	/**
	 * Class constructor creates URLConnection with url and gets input stream from it
	 * Called by: WebCli.sendRequest()
	 *     Calls: URL.openConnection(), BufferedInputStream, URLConnection.getInputStream()
	 * @param url - url of ftp source
	 * @throws IOException - if connection fails
	 * @throws RespException - 
	 */
	public FtpResp(URL url) throws IOException, RespException {
		urlc = url.openConnection(); // opens connection
		
		checkResp(); // checks response of connection for errors
		
		// gets input stream of content
		content = new BufferedInputStream(urlc.getInputStream());
	}


	/**
	 * Gets content and passes it through output stream writing bytes to output
	 * Called by; WebCli.readResponse()
	 *     Calls: InputStream.read(), OutputStream.write(), OutputStream.flush()
	 * @param out - output stream to send content
	 * @throws UnsupportedEncodingException - if encoding is not supported by client
	 * @throws IOException - if host is unreachable
	 */
	@Override
	public void getContent(OutputStream out) throws UnsupportedEncodingException, IOException {
		int c;
		while((c = content.read()) != -1) { // read all content
			out.write(c); // write content to output
		}
		out.flush();
	}

	/**
	 * Gets content and returns it as a string by using a string buffer to read
	 * content then returns string
	 * Called by; WebCli.readResponse()
	 *     Calls: InputStream.read()
	 * @return content as string
	 * @throws UnsupportedEncodingException - if encoding is not supported by client
	 * @throws IOException - if host is unreachable
	 */
	@Override
	public String getContent() throws UnsupportedEncodingException, IOException {
		StringBuilder buf = new StringBuilder();
		int c;
		while((c = content.read()) != -1) {
			buf.append((char)c);
		}
		
		return buf.toString();
	}

	/**
	 * Checks response by checking the content length field. If the 
	 * field doesn't exist the service failed to connect possibly due
	 * to authentication issues and if the content length is less than
	 * 0 then the resouces was not found at the URL
	 * Called by: this()
	 *     Calls: none
	 * @throws RespException - error occured when getting content
	 * @throws IOException - failed to connect to client
	 */
	@Override
	public void checkResp() throws RespException, IOException {
		String field = urlc.getHeaderField("Content-length");
		if (field == null) {
			throw new RespException("failed to connect to ftp service possibly due to authentication");
		}
		contentLength = Long.parseLong(field);
		if (contentLength < 0) {
			throw new RespException("no resource found at location");
		}
	}
	
	/**
	 * Close connection to host, this class does not need to be closed
	 */	
	@Override
	public void close() {}
}
