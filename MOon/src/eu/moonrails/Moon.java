package eu.moonrails;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;

import eu.moonrails.abstraction.ops.Operation;
import eu.moonrails.plugins.builtin.MOXPlugin;
import eu.moonrails.plugins.builtin.SingleFileArduinoPlugin;

public class Moon {

	public static void main(String[] args) throws Exception {
		if(args.length != 2){
			System.out.println("Number of arguments must be two.");
			System.out.println("java -jar moon.jar <original file> <target file>");
			return;
		}
		String path  = args[0];	
		System.out.println("Preparing to load file: "+path);
		
		File file = new File(path);
		File sourceFolder = file.getAbsoluteFile().getParentFile();		
		
		CodeAbstractor loader = new CodeAbstractor(path);
				
		
		System.out.println("-------------------- Moon Rails Abstraction ---------------------------");
		
		 for(Operation op: loader.getAbstractionTree().getServices().get(0).getOperations()){
			 System.out.println("OPERATION:"+ op.getName());
			 System.out.println("\tComment:"+op.getComment());
			 System.out.println("\tIP:"+op.getClass());

			 System.out.println("\t\tOP "+ op.toString());
		 }
		 
		 System.out.println("---------------------- Arduino Driver -----------------------------");
		 MoonRailsPlugin driver =  new SingleFileArduinoPlugin(loader.getAbstractionTree(), sourceFolder);
		 driver.createGeneratedFiles();

		 System.out.println("---------------------- MO XML Driver -----------------------------");
		 MoonRailsPlugin driver3 =  new MOXPlugin(loader.getAbstractionTree(), sourceFolder);
		 driver3.createGeneratedFiles();

		 
	}

}
