package eu.moonrails.abstraction.ops;

import eu.moonrails.abstraction.MoonRailsAbstraction;

public abstract class Operation extends MoonRailsAbstraction {
	protected String name;
	protected String comment;

	public Operation(String name, String comment) {
		super();
		this.name = name;
		this.comment = comment;
	}

	public Operation(String name) {
		this(name, null);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract boolean hasParameters();
	
	public boolean ifTypeThenDo(Class classType, DoTask task) {
		if (this.getClass() == classType) {			
			task.task(this);
			return true;
		}
		return false;
	}

	public interface DoTask {
		public void task(Operation o);
	}
}
