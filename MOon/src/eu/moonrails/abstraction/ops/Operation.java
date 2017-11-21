package eu.moonrails.abstraction.ops;

import eu.moonrails.abstraction.MoonRailsAbstraction;

public abstract class Operation extends MoonRailsAbstraction {
	protected String name;

	public Operation(String name, String comment) {
		super(comment);
		this.name = name;
	}

	public Operation(String name) {
		this(name, null);
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
	
	public boolean isPublic() {
		return this.getProperty("public","true").equals("true");
	}
}
