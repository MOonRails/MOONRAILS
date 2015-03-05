package eu.moonrails.abstraction;

public class Parameter {
	public enum Type {
		BOOLEAN, INT, FLOAT
	};

	protected String name;
	protected Type type;

	public Parameter(Type type, String name) {
		this(type);
		this.name = name;
	}	
	
	public Parameter(Type type) {
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

	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
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
		return new Parameter(Type.INT, name);
	}

	public static Parameter Boolean(String name) {
		return new Parameter(Type.BOOLEAN, name);
	}

	public static Parameter Float(String name) {
		return new Parameter(Type.FLOAT, name);
	}

}
