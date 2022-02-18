package webapps;
/**
 * Name: Test.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 21, 2021
 * Description: Class used to test classes
 */


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import webapps.HttpHeader.HttpHeaderException;

public class Test {
	public static void main(String[] args) {
		
		/* Test the Option class and subclasses */
		System.out.println("Test creating OptString and getting members,");
		try {
			Option.OptionString optStr = new Option.OptionString("string", "This is a string option", "value");
			try {
				testStringEqual("Test getting name of option", "OptionStr.getName()", optStr.getName(), "string");
				testStringEqual("Test getting description of option", "OptionStr.getDesc()", optStr.getDesc(), "This is a string option");
				testStringEqual("Test getting help message of option", "OptionStr.getHelpMessage()", optStr.getHelpMessage(), "string - This is a string option");
				testStringEqual("Test getting value of option", "OptionStr.getValue()", optStr.getValue(), "value");
			}
			catch(TestException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}
		catch (Option.OptionException e) {
			System.err.println("error: Failed to create OptionString");
			System.exit(1);
		}
		
		System.out.println("Test creating OptInt and getting members,");
		try {
			Option.OptionInt optInt = new Option.OptionInt("int", "This is an int option", "1");
			try {
				testStringEqual("Test getting name of option", "OptionInt.getName()", optInt.getName(), "int");
				testStringEqual("Test getting description of option", "OptionInt.getDesc()", optInt.getDesc(), "This is an int option");
				testStringEqual("Test getting help message of option", "OptionInt.getHelpMessage()", optInt.getHelpMessage(), "int - This is an int option");
				testIntEqual("Test getting value of option", "OptionInt.getValue()", optInt.getValue(), 1);
			}
			catch(TestException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
			
			try {
				Option.OptionInt optIntFail = new Option.OptionInt("int1", "This is a test for invalid value", "a");
				optIntFail.getDesc();
				System.err.println("error: value of 'a' should have thrown exception");
				System.exit(1);
			}
			catch(Option.OptionException e) {
				makeResultMsg("Test setting int option to invalid value of 'a'", "OptionInt()", "Exception", "Exception", true);
			}
		}
		catch (Option.OptionException e) {
			System.err.println("error: Failed to create OptionInt");
			System.exit(1);
		}
		
		System.out.println("Test creating OptBool,");
		try {
			Option.OptionBool optBool = new Option.OptionBool("bool", "This is a bool option", "");
			try {
				testStringEqual("Test getting name of option", "OptionBool.getName()", optBool.getName(), "bool");
				testStringEqual("Test getting description of option", "OptionBool.getDesc()", optBool.getDesc(), "This is a bool option");
				testStringEqual("Test getting help message of option", "OptionBool.getHelpMessage()", optBool.getHelpMessage(), "bool - This is a bool option");
				testBoolEqual("Test getting value of option", "OptionBool.getValue()", optBool.getValue(), true);
			}
			catch(TestException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
				System.exit(1);
			}
			
			try {
				Option.OptionBool optBoolFail = new Option.OptionBool("bool1", "This is a test for invalid value", "a");
				optBoolFail.getDesc();
				System.err.println("error: value of 'a' should have thrown exception");
				System.exit(1);
			}
			catch(Option.OptionException e) {
				makeResultMsg("Test setting bool option to invalid value of 'a'", "OptionBool()", "Exception", "Exception", true);
			}
		}
		catch (Option.OptionException e) {
			System.err.println("error: Failed to create OptionBool");
			System.exit(1);
		}
		
		/* Test Main class */
		String[] opts1 = {"--option1", "str1", "--option2", "str2", "--option3", "3", "--option4", "4", "-o", "param", "--option5", "-w", "testw"};
		System.out.println("Testing command line arguments:");
		System.out.print("    ");
		for (String o : opts1) {
			System.out.print(o + " ");
		}
		System.out.println();
		try {
			TestMain main1 = new TestMain();
			main1.parseOptions(opts1);
			testStringEqual("Test option1 value from commmand line", "OptionString.getValue()", main1.option1.getValue(), "str1");
			testStringEqual("Test option2 value is default value", "OptionString.getValue()", main1.option2.getValue(), "str2");
			testIntEqual("Test option3 value is default value", "OptionInt.getValue()", main1.option3.getValue(), 3);
			testIntEqual("Test option4 value is from command line", "OptionInt.getValue()", main1.option4.getValue(), 4);
			testBoolEqual("Test option5 value is default value", "OptionBool.getValue()", main1.option5.getValue(), true);
			testBoolEqual("Test o value is from command line", "OptionBool.getValue()", main1.option6.getValue(), true);
			testStringEqual("Test w value is from command line", "OptionString.getValue()", main1.option7.getValue(), "testw");
		}
		catch(Option.OptionException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println("error: failed to parse command line variables");
			System.exit(1);

		}
		catch (TestException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		String[] opts3 = {"-option1", "str1", "--option2", "str2", "--option4", "4", "param"};
		System.out.println("Testing command line arguments:");
		System.out.print("    ");
		for (String o : opts3) {
			System.out.print(o + " ");
		}
		System.out.println();
		try {
			TestMain main3 = new TestMain();
			main3.parseOptions(opts3);
			System.err.println("error: main3 params should have thrown exception");
			System.exit(1);
		}
		catch (Option.OptionException e) {
			try {
				testStringEqual("Test parsing arguments throws exception", "", e.getMessage(),"option1 is an in valid '-' option type");
			}
			catch (TestException ee) {
				System.err.println(ee.getMessage());
				System.exit(1);
			}
		}
		
		String[] opts4 = {"--option1", "str1", "--option2", "str2", "--option4"};	
		System.out.println("Testing command line arguments:");
		System.out.print("    ");
		for (String o : opts4) {
			System.out.print(o + " ");
		}
		System.out.println();
		try {
			TestMain main4 = new TestMain();
			main4.parseOptions(opts4);
			System.err.println("error: main4 params should have thrown exception");
			System.exit(1);
		}
		catch (Option.OptionException e) {
			try {
				testStringEqual("Test parsing arguments throws exception", "", e.getMessage(),"option4 must be an integer");
			}
			catch (TestException ee) {
				System.err.println(ee.getMessage());
				System.exit(1);
			}
		}

		String[] opts5 = {"param","--option1", "str1", "--option2", "str2", "--option7", "parameter does not exist"};
		System.out.println("Testing command line arguments:");
		System.out.print("    ");
		for (String o : opts5) {
			System.out.print(o + " ");
		}
		System.out.println();
		try {
			TestMain main5 = new TestMain();
			main5.parseOptions(opts5);
			System.err.println("error: main5 params should have thrown exception");
			System.exit(1);
		}
		catch (Option.OptionException e) {
			try {
				testStringEqual("Test parsing arguments throws exception", "", e.getMessage(),"option7 is an invalid option");
			}
			catch (TestException ee) {
				System.err.println(ee.getMessage());
				System.exit(1);
			}
		}
		
		String[] opts6 = {"--option1", "str1", "-o", "--option4", "4", "param", "-w", "testw"};
		try {
			TestMain main6 = new TestMain();
			main6.parseOptions(opts6);
			testStringEqual("Test option1 value from commmand line", "OptionString.getValue()", main6.option1.getValue(), "str1");
			testStringEqual("Test option2 value is default value", "OptionString.getValue()", main6.option2.getValue(), "empty");
			testIntEqual("Test option3 value is default value", "OptionInt.getValue()", main6.option3.getValue(), 0);
			testIntEqual("Test option4 value is from command line", "OptionInt.getValue()", main6.option4.getValue(), 4);
			testBoolEqual("Test option5 value is default value", "OptionBool.getValue()", main6.option5.getValue(), false);
			testBoolEqual("Test option6 value is from command line", "OptionBool.getValue()", main6.option6.getValue(), true);
			testStringEqual("Test option7 value is from command line", "OptionString.getValue()", main6.option7.getValue(), "testw");
						
		}
		catch (Option.OptionException e) {
			e.printStackTrace();
			System.err.println("error: " + e.getMessage());
			System.exit(1);
		}
		catch (TestException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		System.out.println("Testing parsing HTTP header,");
		StringBuilder httpHeaderStr = new StringBuilder();
		httpHeaderStr.append("GET / HTTP/1.1\r\n");
		httpHeaderStr.append("Host: test.org\r\n");
		httpHeaderStr.append("Connection: keep-alive\r\n");
		httpHeaderStr.append("User-Agent: testAgent\r\n\r\n");
		InputStream in = new ByteArrayInputStream(httpHeaderStr.toString().getBytes());
		HttpHeader httpHeader = new HttpHeader(in);
		
		try {
			httpHeader.parse();
			testStringEqual("Test method value from header", "OptionString.getField(\"head1\")", httpHeader.getField("head1"), "GET");
			testStringEqual("Test resource value from header", "OptionString.getField(\"head2\")", httpHeader.getField("head2"), "/");
			testStringEqual("Test protocol value from header", "OptionString.getField(\"head3\")", httpHeader.getField("head3"), "HTTP/1.1");
			testStringEqual("Test Host value from header", "OptionString.getField(\"host\")", httpHeader.getField("host"), "test.org");
			testStringEqual("Test Connection value from header", "OptionString.getField(\"connection\")", httpHeader.getField("connection"), "keep-alive");
			testStringEqual("Test User-Agent value from header", "OptionString.getField(\"user-agent\")", httpHeader.getField("user-agent"), "testAgent");
		} catch (HttpHeaderException e){
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.err.println("unknown exception occured");
			System.exit(1);
		}
	}
	
	public static class TestMain extends Main {
		public Option.OptionString option1;
		public Option.OptionString option2;
		public Option.OptionInt option3;
		public Option.OptionInt option4;
		public Option.OptionBool option5;
		public Option.OptionBool option6;
		public Option.OptionString option7;
				
		public String parameter;
		
		public TestMain() throws Option.OptionException {
			super();
			option1 = new Option.OptionString("option1", "This is string option1");
			option2 = new Option.OptionString("option2", "This is string option2");
			option3 = new Option.OptionInt("option3", "This is int option3");
			option4 = new Option.OptionInt("option4", "This is int option4");
			option5 = new Option.OptionBool("option5", "This is bool option5");
			option6 = new Option.OptionBool("o", "This is bool option6");
			option7 = new Option.OptionString("w", "Another string test");
			addOption(option1);
			addOption(option2);
			addOption(option3);
			addOption(option4);
			addOption(option5);
			addOption(option6);
			addOption(option7);
		}
		
		public void setParam(String param) {
			parameter = param;
			params.add(parameter);
		}
		
		public void setOption(String name, String value) throws Option.OptionException {
			
			if (!options.containsKey(name))
				throw new Option.OptionException(name + " is and invalid option");
			
			Option opt = options.get(name);
			opt.setValue(value);
		}
		
		public void checkParams() throws Option.OptionException {
			
		}
	}
	
	public static void testStringEqual(String desc, String funcName, String s1, String s2) throws TestException {
		if (!s1.equals(s2)) {
			throw new TestException (makeResultMsg(desc, funcName, s1, s2, false));
		}
		else {
			System.out.println(makeResultMsg(desc, funcName, s1, s2, true)); 
		}
	}

	public static void testStringNotEqual(String desc, String funcName, String s1, String s2) throws TestException {
		if (s1.equals(s2)) {
			throw new TestException (makeResultMsg(desc, funcName, s1, "not equal", false));
		}
		else {
			System.out.println(makeResultMsg(desc, funcName, s1, "not equal", true));
		}
	}
	
	public static void testIntEqual(String desc, String funcName, int i1, int i2) throws TestException {
		if (i1 != i2) {
			throw new TestException (makeResultMsg(desc, funcName, i1, i2, false));
		}
		else {
			System.out.println(makeResultMsg(desc, funcName, i1, i2, true));
		}
	}
	
	public static void testBoolEqual(String desc, String funcName, boolean b1, boolean b2) throws TestException {
		if (b1 != b2) {
			throw new TestException (makeResultMsg(desc, funcName, b1, b2, false));
		}
		else {
			System.out.println(makeResultMsg(desc, funcName, b1, b2, true));
		}
	}
	
	public static void testNull(String desc, String funcName, Object o) throws TestException {
		if (o != null) {
			throw new TestException(makeResultMsg(desc, funcName, o, "null", false));
		}
		else {
			System.out.println(makeResultMsg(desc, funcName, o, "null", true));
		}
	}
	
	public static String makeResultMsg(String desc, String funcName, Object o1, Object o2, boolean pass) {
		String msg = desc + "," + funcName + "," + o1 + "," + o2 + ",";
		if (pass) {
			return msg + "pass";
		}
		else {
			return msg + "fail";
		}		
	}
	/**
	 * Exception used testing
	 *
	 */
	public static class TestException extends Exception {

		/**
		 * Required to serialize object 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor 
		 */
		public TestException() {}

		/**
		 * Constructor that sets exception message
		 * @param message - message of exception
		 */
		public TestException(String message) {
			super(message);
		}
	}
}
