package eu.moonrails.plugins.builtin;

import java.io.File;

import eu.moonrails.MoonRailsPlugin;
import eu.moonrails.abstraction.AbstractionTree;

public class JavaCLIPlugin extends MoonRailsPlugin{
	public static final String ID = "JavaCLI";
	
	public JavaCLIPlugin(AbstractionTree atree, File sourceFolder) {
		super(atree, sourceFolder);
	}

	@Override
	public String getDriverId() {
		return ID;
	}

	@Override
	public void createGeneratedFiles() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
