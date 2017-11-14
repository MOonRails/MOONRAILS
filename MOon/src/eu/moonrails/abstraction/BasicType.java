package eu.moonrails.abstraction;

public class BasicType extends DataType{
	public enum BasicTypeEnum {
		BOOLEAN, UBYTE,INT, FLOAT
	}
	
	private BasicTypeEnum type;
	
	public static final BasicType INT = newInt();
	public static final BasicType BOOLEAN = newBoolean();
	public static final BasicType FLOAT = newFloat();
	public static final BasicType UBYTE = newUnsignedByte();

	
	
	private BasicType(String name,BasicTypeEnum e) {
		super(name);
		this.type = e;
	}
	
	public static BasicType newInt() {
		return new BasicType("Integer",BasicTypeEnum.INT);
	}

	public static BasicType newBoolean() {
		return new BasicType("Boolean",BasicTypeEnum.BOOLEAN);
	}

	public static BasicType newUnsignedByte() {
		return new BasicType("UnsignedByte",BasicTypeEnum.UBYTE);
	}

	public static BasicType newFloat() {
		return new BasicType("Float",BasicTypeEnum.FLOAT);
	}

	@Override
	public boolean isBasicType() {
		return true;
	}


	public BasicTypeEnum asEnum() {
		return type;		
	}
	
}
