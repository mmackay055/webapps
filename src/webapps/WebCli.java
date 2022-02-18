package webapps;
/**
 * Name: WebCli.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 28, 2021
 * Description: Class that implements a simple web client that can read
 *              that can read resources from HTTP/HTTPs web servers
 *              and FTP servers. Extends Main class for it's command line
 *              parsing. Run with -h to see help menu
 *              
 *               Basic usage:
 *              	java tma1.WebCli [options] <url>
 *        
 * Inherits: Srv
 */


import webapps.Option.OptionException;
import webapps.Option.OptionString;
import webapps.Resp.RespException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Class definition
 */
public class WebCli extends Main {
	URL url; // holds url client is requesting
	Resp resp; // holds response of client request
	
	OptionString fileOutOpt; // option to write to file
	
	/**
	 * Constructor of WebCli initiates options
	 * Called By: None
	 *     Calls: super(), OptionString(), Main.addOption()
	 */
	public WebCli() throws OptionException {
		super();
		fileOutOpt = new OptionString("f", "Provide file to write resouce to");
		addOption(fileOutOpt);
	}
	
	/**
	 * Constructor of WebCli initiates options and sets name and description
	 * of client program
	 * Called By: main()
	 *     Calls: super(), OptionString(), Main.addOption()
	 */
	public WebCli (String name, String desc, String usage) throws OptionException{
		super(name, desc, usage);
		fileOutOpt = new OptionString("f", "Provide file to write resouce to");
		addOption(fileOutOpt);
	}
	
	/**
	 * Sets url parameter
	 * Called By: super.parseOptions()
	 *     Calls: URL(), Vector<String>.add()
	 * @param - url string 
	 */
	@Override
	 public void setParam(String param) throws OptionException {
		// create new url object
		try {
			url = new URL(param);
			params.add(param); // add to parameter vector
		}
		catch (MalformedURLException e) {
			throw new OptionException("url protocol is not supported or url is malformed");
		}
	}

	/**
	 * Sets options specific for client program throws exception
	 * if option doesn't exist
	 * Called By: super.parseOptions()
	 *     Calls: Option.setValue(), Map<string, Option>.get(), 
	 *     Map<String, Option>.containsKey() 	
	 * @param name - name of option
	 * @param value - value of option
	 * @throws OptionException - if error with option exists
	 */	
	@Override
	public void setOption(String name, String value) throws OptionException {
		if (!options.containsKey(name))
			throw new OptionException(name + " is and invalid option");

		Option opt = options.get(name);
		opt.setValue(value);

	}

	/**
	 * Checks that url was provided in the command line
	 * Called By: main()
	 *     Calls: none
	 */
	@Override
	public void checkParams() throws OptionException {
		if (params.size() < 1) {
			throw new OptionException("url must be provided, use -h option for help");
		}
		else if (params.size() > 1) {
			throw new OptionException("only one url can be provided, use -h option for help");
		}
	}
	
	/**
	 * Sends the request for the resource at url provided from command line, performs
	 * request specific to the protocol specified in the url. Throws exception if
	 * protocol is not specified
	 * Called By: main()
	 *     Calls: URL.getProtocol(), HttpResp(), FttpResp()
	 * @throws IOException - if error occurs while making connection or reading content
	 * @throws RespException - if error occurs specific to request type
	 */
	public void sendRequest() throws IOException, RespException {	
		String protocol = url.getProtocol(); // get url protocol
		if (protocol.contentEquals("http") || protocol.contentEquals("https")) {
			resp = new HttpResp(url); // perform HTTP/HTTPS request
		}
		else if (protocol.contentEquals("ftp")) {
			resp = new FtpResp(url); // perform FTP request
		}
		else { // protocol not supported
			throw new RespException("protocol not supported by client");
		}
	}
	
	/**
	 * Reads response and sends to output stream
	 * Called By: main()
	 *     Calls: HttpResp.getContent(OutputStream)
	 * @param out - output stream to write content to
	 * @throws UnsupportedEncodingException - if encoding isn't supported by client
	 * @throws IOException - if error occurs connecting to host or reading content
	 */
	public void readResponse(OutputStream out) throws UnsupportedEncodingException, IOException {
		resp.getContent(out);
	}
	
	/**
	 * Reads response and returns it as a string
	 * Called By: none
	 *     Calls: HttpResp.getContent()
	 * @return - string object containing content from response
	 * @throws UnsupportedEncodingException - if encoding isn't supported by client
	 * @throws IOException - if error occurs connecting to host or reading content
	 */
	public String readResponse() throws UnsupportedEncodingException, IOException {
		return resp.getContent();
	}
	
	/**
	 * Close connection to host
	 * Called by: main()
	 *     Calls: HttpResp.close()
	 */
	public void close() {
		resp.close();
	}

	/**
	 * Main method that runs program. Takes command line arguments
	 * Calls: WebCli(), Main.parseOptions(), Main.getHelp(), this.checkParams()
	 *	      WebCli.sendRequest(), WebCli.readRequest(), WebCli.close()
	 * @param args
	 */
	public static void main(String[] args) {
		// client set up
		WebCli client = null;
		try {
			// load client
			client = new WebCli("WebCli", 
					"A web client for performing simple requests for resources from servers. "
					+ "Supports protocols HTTP, HTTPS and FTP. All fetched data is printed "
					+ "to console by default.",
					"java tma1.WebCli [Options] <url>");
			
			// parse options
			client.parseOptions(args);
			
			//call help if option provided
			if (client.getHelp()) {
				client.printHelp();
				System.exit(0);
			}
			
			// check required parameters such as url
			client.checkParams();

		}
		catch (OptionException e) { // if error occurs with options from command line
			printError(e.getMessage());
			System.exit(1);
		}
		
		// complete connect to client
		try {
			client.sendRequest();
		}
		catch (IOException e) { // if error occurs while connecting to host
			printError("failed to connect to host");
			System.exit(2);
		}
		catch (RespException e) { // if error occurs specific to protocol
			printError(e.getMessage());
			System.exit(3);
		}

		// read response
		try {
			if (client.fileOutOpt.isDefault()) { // default write to standard out
				client.readResponse(new BufferedOutputStream(System.out)); // sends content to console
			}
			else {
				try (OutputStream fileOut = new BufferedOutputStream( // write to file opt
											new FileOutputStream(client.fileOutOpt.getValue()))) {
					client.readResponse(fileOut);
				}
				catch (IOException e) {
					printError("failed to write to file " + client.fileOutOpt.getValue());
					System.exit(6);
				}
			}
		}
		catch (UnsupportedEncodingException e) { // if encoding isn't supported
			printError("document encoding isn't supported by client");
			System.exit(4);
		}
		catch (IOException e) { // error occurs reading content from host 
			printError("failed reading from host");
			System.exit(5);
		}
		
		client.close();
		
		System.exit(0);
	}
}
