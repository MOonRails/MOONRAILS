package eu.moonrails.abstraction;

public abstract class DataType extends MoonRailsAbstraction {

	protected String name;

	public abstract boolean isBasicType();

	public DataType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
