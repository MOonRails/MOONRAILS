package eu.moonrails.abstraction;

import java.util.Collection;
import java.util.HashMap;

public class CompositeType extends DataType {
	private HashMap<String, Parameter> parameters;

	private Service service = null;
	
	public CompositeType(String name) {
		this(name,null,null);		
	}

	public CompositeType(String name,Service service) {
		this(name,null, service);
	}

	public CompositeType(String name, String comment) {
		this(name,comment,null);
	}		

	public CompositeType(String name, String comment,Service service) {
		super(name);
		this.setComment(comment);
		this.service = service;
		parameters = new HashMap<>();
	}		

	public void addParameter(BasicType type, String name) {
		addParameter(new Parameter(type, name));
	}
	
	public void addParameter(Parameter p) {
		parameters.put(p.name, p);
	}

	public Parameter getParameter(String name) {
		return parameters.get(name);
	}

	public Collection<Parameter> getParameters() {
		return parameters.values();
	}

	public Service getService() {
		return service;
	}

	@Override
	public boolean isBasicType() {
		return false;
	}

	public void setService(Service service) {
		this.service = service;
	}
		
}
