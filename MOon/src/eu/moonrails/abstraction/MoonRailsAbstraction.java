package eu.moonrails.abstraction;

public class MoonRailsAbstraction {
	protected String comment;

	/**
	 * For simplification, if no comment, then empty string. Avoids null pointer
	 * exceptions and commands are used as string, not really processed.
	 */
	public MoonRailsAbstraction() {
		this.comment = "";
	}

	public MoonRailsAbstraction(String comment) {
		this.comment = comment;
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

}
