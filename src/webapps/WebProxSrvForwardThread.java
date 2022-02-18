package webapps;
/**
 * Name: WebProxSrvThreadDirect.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 29, 2021
 * Description: Class that extends Callable and is used by WebProxSrvThread to handle
 *              single direction communications between two hosts. Any data received
 *              from the input socket is forwarded to the output socket. The thread
 *              continues to forward until communications are ended by hosts connected
 *              to the socket. Once communications is completed a WebProxSrv.Result
 *              object is returned summarizing the session.
 *              
 * Implements Callable
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import webapps.WebProxSrvThread.Result;

/**
 * Class definition
 */
public class WebProxSrvForwardThread implements Callable<Result>{
	Socket sockIn; // socket to read from
	Socket sockOut; // socket to write to
	String name; // name of thread, used in logs
	
	/**
	 * Initializes thread by setting members
	 * Called by: WebProxSrvThread()
	 *     Calls: none
	 * @param sockIn - socket to read from
	 * @param sockOut - socket to write to
	 * @param name - name of thread
	 */
	public WebProxSrvForwardThread(Socket sockIn, Socket sockOut, String name) {
		this.sockIn = sockIn;
		this.sockOut = sockOut;
		this.name = name;
	}


	/**
	 * Executed by ExecutorService and forwards all bytes from input socket
	 * to output socket then returns a Result object summerizing session
	 * Called by: WebProxSrvThread ExecutorService
	 *     Calls: WebProxSrvThread.Result(), forward()
	 */
	@Override
	public Result call() {
		// create new Result object
		Result result = new Result(sockIn, sockOut);
		
		// forward communications
		try {
			result.setByteCount(forward()); // send response and set byte count
		}
		catch (IOException e) { // failed to forward
			// create and set error message
			result.setErrMsg(name + " failed forwarding " + 
						sockIn.getInetAddress().getHostAddress() + 
						" to " + sockOut.getInetAddress().getHostAddress() + 
						" " + e);
		}
		return result; // return Result object
	}
	
	/**
	 * Forwards bytes from the input to output sockets any IO errors that
	 * occur throw an exception
	 * Called by: call()
	 *     Calls: BufferedInputStream(), Socket.getInputStream(),
	 *            BufferedOutputStream(), Socket.getOutputStream(),
	 *            OutputStream.write(), OutputStream.flush(),
	 *            InputStream.read()
	 * @return count of bytes forwarded as long
	 * @throws IOException - if error occurs reading/writing bytes
	 */
	private long forward() throws IOException {
		// get streams from sockets
		InputStream in = new BufferedInputStream(sockIn.getInputStream());
		OutputStream out = new BufferedOutputStream(sockOut.getOutputStream());
		
		long byteCount = 0; // holds byte count
		int b; // holds byte count read in read operation
		int buffSz = sockIn.getReceiveBufferSize(); // size of input buffer size 
		byte[] bytes = new byte[buffSz]; // create array of input buffer size

		// continually reads bytes from input and writes to output until
		// communications are ended by input socket
		while ((b = in.read(bytes, 0, buffSz)) != -1) {
			out.write(bytes, 0, b); // write bytes read to output socket
			out.flush(); // flush bytes written
			byteCount += b; // add bytes written to byte count
		}
		return byteCount; // return byte count
	}
}
