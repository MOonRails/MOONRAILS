package eu.moonrails.abstraction;

import java.util.Properties;

public class MoonRailsAbstraction {
	protected String comment;
	protected Properties properties;

	/**
	 * For simplification, if no comment, then empty string. Avoids null pointer
	 * exceptions and commands are used as string, not really processed.
	 */
	public MoonRailsAbstraction() {
		this("");
	}

	public MoonRailsAbstraction(String comment) {
		this.comment = comment;
		properties = new Properties();

	}

	public String getComment() {
		return comment;
	}

	public boolean isCommented() {
		return comment != "";
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);		
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getProperty(String key,String def) {
		return properties.getProperty(key,def);
	}

}
