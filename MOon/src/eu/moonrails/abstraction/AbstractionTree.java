package eu.moonrails.abstraction;

import java.util.ArrayList;

public class AbstractionTree {

	ArrayList<Service> services;
	
		
	public AbstractionTree() {
		super();
		this.services = new ArrayList<Service>();
	}


	public Service addService(String name){
		Service service = new Service(name,this.services.size());
		this.services.add(service);
		return service;
	}
	
	public ArrayList<Service> getServices(){
		return this.services;
	}
	
}
