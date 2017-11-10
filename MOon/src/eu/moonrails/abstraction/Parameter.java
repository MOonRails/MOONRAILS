package eu.moonrails.abstraction;

public class Parameter extends MoonRailsAbstraction {
	protected String name;
	protected DataType type;

	public Parameter(DataType type, String name) {
		this(type);
		this.name = name;
	}	
	
	public Parameter(DataType type) {
		this();
		this.type = type;
	}	
	
	
	public Parameter() {
		this.name = null;
		this.type = null;
	}

	public String getName() {
		return name;
	}

	public DataType getType() {
		return type;
	}
	
	public void setType(DataType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type.toString() + "[" + name + "]";
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Parameter Int(String name) {
		return new Parameter(BasicType.INT, name);
	}

	public static Parameter Boolean(String name) {
		return new Parameter(BasicType.BOOLEAN, name);
	}

	public static Parameter Float(String name) {
		return new Parameter(BasicType.FLOAT, name);
	}

}
