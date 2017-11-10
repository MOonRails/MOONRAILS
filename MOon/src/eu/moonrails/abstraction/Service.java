package eu.moonrails.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import eu.moonrails.abstraction.ops.Operation;

public class Service extends MoonRailsAbstraction {
	private String name;
	private int id;

	protected ArrayList<Operation> operations;
	protected HashMap<String,DataType> dataTypes;

	
	public Service(String name) {
		this(name, 0);
	}

	public Service(String name, int id) {
		this.setName(name);
		this.setId(id);
		this.operations=new ArrayList<>();
		this.dataTypes =  new HashMap<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Operation addOperation(Operation operation) {
		this.operations.add(operation);
		return operation;
	}

	public ArrayList<Operation> getOperations() {
		return this.operations;
	}
	
	public DataType addDataType(DataType dataType) {
		this.dataTypes.put(dataType.name,dataType);
		return dataType;
	}

	public Collection<DataType> getDataTypes() {
		return this.dataTypes.values();
	}
	
	public DataType getDataTypeByName(String name) {
		return this.dataTypes.get(name);
	}

}
