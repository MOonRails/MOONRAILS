package eu.moonrails.abstraction.ops;

import eu.moonrails.abstraction.Parameter;

public class SimpleSubscription extends Operation {

	private Parameter subscriptionType;

	public SimpleSubscription(String name) {
		super(name);
	}

	public SimpleSubscription(String name, Parameter subscriptionType, String comment) {
		super(name, comment);
		this.subscriptionType = subscriptionType;
	}

	public Parameter getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(Parameter subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	@Override
	public String toString() {
		return "SimpleSubscription " + name + " will publish on the type " + subscriptionType.getType().toString();
	}

	@Override
	public boolean hasParameters() {
		return false;
	}

}
