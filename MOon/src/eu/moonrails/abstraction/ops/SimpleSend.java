package eu.moonrails.abstraction.ops;

import eu.moonrails.abstraction.Parameter;

public class SimpleSend extends Operation {

	private Parameter parameter;

	public SimpleSend(String name) {
		this(name, null);
	}

	public SimpleSend(String name, Parameter param) {
		super(name);
		this.parameter = param;
	}

	public boolean hasParameters() {
		return parameter != null;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	@Override
	public String toString() {
		String param = parameter == null ? "(no parameters)"
				: " will send " + parameter.getName() + "\' of type " + parameter.getName();
		return "SimpleSend " + name + param;
	}
}
