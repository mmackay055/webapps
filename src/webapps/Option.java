package webapps;
/**
 * Name: Option.java
 * Course: COMP 489
 * Assignment: tma1
 * Student: Matt MacKay
 * 
 * Date: Dec. 21, 2021
 * Description: Abstract class for handling options on the command line. 
 *              Used by Main class and it's subclasses. 
 */


/**
 * Option abstract class is used to hand command line arguments.
 * Subclasses exist for specific data types or options
 */
public abstract class Option {
	
	private String name; // name of option
	private String desc; // description
	
	
	/**
	 * Default constructor
	 */
	public Option() {}
	
	/**
	 * Constructor for option
	 * @param name - name of option
	 * @param desc - description of option
	 */
	public Option(String name, String desc) throws OptionException {
		this.name = name;
		this.desc = desc;
	}
	
	/**
	 * Constructor for option
	 * @param name - name of option
	 * @param desc - description of option
	 * @param value - value of option
	 */
	public Option(String name, String desc, String value) throws OptionException {
		this.name = name;
		this.desc = desc;
		setValue(value);
	}
	
	/**
	 * Parses into appropriate data type and sets value
	 * of option
	 * @param value - string value of option
	 */
	public abstract void setValue(String value) throws OptionException;
	
	/**
	 * Gets help message for option
	 * @return help message as string
	 */
	public String getHelpMessage() {
		return getName() + " - " + getDesc();
	}
	
	/**
	 * Get name of option
	 * @return name of option as string
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get description of option
	 * @return description as string
	 */
	public String getDesc() {
		return desc;
	}
	
	/**
	 * Checks to see if value equals default 
	 * value for class
	 */
	public abstract boolean isDefault();
	
	/**
	 * Subclass of Option, used for string options
	 */
	public static class OptionString extends Option{
		private static final String defaultValue = "empty";
		
		private String value; // value of option
		
		/*
		 * Constructor
		 */
		public OptionString(String name, String desc) throws OptionException {
			super(name, desc, defaultValue);
		}
		
		/*
		 * Constructor
		 */
		public OptionString(String name, String desc, String value) throws OptionException {
			super(name, desc, value);
		}
		
		/**
		 * Sets value of option throws exception if value
		 * string is empty
		 * @param value - value of option as string
		 */
		public void setValue(String value) throws OptionException {
			/*if (value.isEmpty()) {
				throw new OptionException(getName() + " must have value");
			}*/
			this.value = value;
		}
		
		/**
		 * Gets value of option
		 * @return option value as string
		 */
		public String getValue() {
			return value;
		}
		
		/**
		 * Checks to see if value equals default 
		 * value for class
		 */
		public boolean isDefault() {
			return value.contentEquals(defaultValue);
		}
	}
	
	
	/**
	 * Subclass of Option, used for integer options
	 */
	public static class OptionInt extends Option {
		private static final int defaultValue = 0;
		
		private int value; // value of option
		
		/*
		 * Constructor
		 */
		public OptionInt(String name, String desc) throws OptionException {
			super(name, desc);
			value = defaultValue;
		}
		
		/*
		 * Constructor
		 */
		public OptionInt(String name, String desc, String value) throws OptionException {
			super(name, desc, value);
		}
		
		/**
		 * Sets value of option by parsing string value into
		 * integer. Throws exception if parsing fails
		 */
		public void setValue(String value) throws OptionException {
			try {
				setValue(Integer.parseInt(value));
			}
			catch (NumberFormatException e) {
				throw new OptionException(getName() + " must be an integer");
			}
		}
	
		/**
		 * Get value as integer
		 * @return value
		 */
		public int getValue() {
			return value;
		}
		
		/**
		 * Set value as Integer just used internally
		 * @param value
		 */
		private void setValue(int value) {
			this.value = value;
		}
		
		/**
		 * Checks to see if value equals default 
		 * value for class
		 */
		public boolean isDefault() {
			return value == defaultValue;
		}
	}
	
	/**
	 * Subclass of Option, used for boolean or flag options
	 */
	public static class OptionBool extends Option {

		private boolean value; // value of option
		
		/*
		 * Constructor
		 */
		public OptionBool(String name, String desc) throws OptionException {
			super(name, desc);
			value = false;
		}
		
		/*
		 * Constructor
		 */
		public OptionBool(String name, String desc, String value) throws OptionException {
			super(name, desc, value);
		}
		
		/**
		 * Sets value of option by parsing string value into
		 * integer. Throws exception if parsing fails
		 */
		public void setValue(String value) throws OptionException {
			if (!value.isEmpty()) {
				throw new OptionException(getName() + " is a flag should not have a value");
			}
			setValue(true);
		}
	
		/**
		 * Get value as integer
		 * @return value
		 */
		public boolean getValue() {
			return value;
		}
		
		/**
		 * Set value as Integer just used internally
		 * @param value
		 */
		private void setValue(boolean value) {
			this.value = value;
		}
		
		/**
		 * Checks to see if value equals default 
		 * value for class
		 */
		public boolean isDefault() {
			return !value; //default value is false for booleans
		}
	}	
	
	/**
	 * Exception used for any option exceptions
	 *
	 */
	public static class OptionException extends Exception {

		/**
		 * Required to serialize object 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor 
		 */
		public OptionException() {}

		/**
		 * Constructor that sets exception message
		 * @param message - message of exception
		 */
		public OptionException(String message) {
			super(message);
		}
	}
}
