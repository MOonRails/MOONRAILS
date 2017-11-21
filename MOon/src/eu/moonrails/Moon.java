package eu.moonrails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import eu.moonrails.abstraction.ops.Operation;
import eu.moonrails.plugins.builtin.MOXPlugin;
import eu.moonrails.plugins.builtin.SingleFileArduinoPlugin;

public class Moon {
	public static final String INI_FILENAME = 	"moonrails.ini";

	public static final String SUPPORTED_EXTENTIONS[] = { ".ino", ".c", ".cpp", ".C" };

	private static Moon moon;
	public static final String MOONRAILS_PROPERTY_PREFIX = "mor.";
	public static final String PROJECTS_FOLDER = "Projects";

	public static final String SOURCE_FOLDER = "src";

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Number of arguments must one.");
			System.out.println("./moon <Project>");
			return;
		}

		moon = new Moon(args[0]);

		System.out.println("Preparing to load project: " + moon.projectName);
		moon.loadSystemConfiguration(moon.projectName);
		File mainFile = moon.getMainSourceFile();

		CodeAbstractor loader = new CodeAbstractor(mainFile);

		System.out.println("-------------------- Moon Rails Abstraction ---------------------------");

		for (Operation op : loader.getAbstractionTree().getServices().get(0).getOperations()) {
			System.out.println("OPERATION:" + op.getName());
			System.out.println("\tComment:" + op.getComment());
			System.out.println("\tIP:" + op.getClass());
		}

		System.out.println("---------------------- Arduino Driver -----------------------------");
		MoonRailsPlugin driver = new SingleFileArduinoPlugin(loader.getAbstractionTree(), mainFile.getParentFile());
		driver.createGeneratedFiles();

		System.out.println("---------------------- MO XML Driver -----------------------------");
		MoonRailsPlugin driver3 = new MOXPlugin(loader.getAbstractionTree(), mainFile.getParentFile());
		driver3.createGeneratedFiles();

	}

	private String projectName;

	private Moon(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectSourceFolder() {
		return getProjectWorkingFolder() + File.separator + SOURCE_FOLDER;
	}

	public File getMainSourceFile() throws FileNotFoundException {

		File ret = null;
		for (String ext : SUPPORTED_EXTENTIONS) {
			File checkFile = new File(getProjectSourceFolder() + File.separator + projectName + ext);

			if (checkFile.exists() && checkFile.isFile()) {
				if (ret != null) {
					System.out.println("Warning: "
							+ ret.getAbsolutePath() + " is being superseded by "
							+ checkFile.getAbsolutePath());
				}
				ret = checkFile;
			}
		}

		if (ret == null) {
			String exts = "";
			for (String ext : SUPPORTED_EXTENTIONS) {
				if (exts != "")
					exts += ", ";
				exts += ext;
			}
			exts = "[" + exts + "]";
			throw new FileNotFoundException("Could not find main file, looking in: " + getProjectSourceFolder()
					+ File.separator + projectName + exts);
		}
		return ret;
	}

	public String getProjectWorkingFolder() {
		return System.getProperty("user.dir")
				+ File.separator + PROJECTS_FOLDER
				+ File.separator + projectName;
	}

	public void loadSystemConfiguration(String projectName) {
		File confFile = new File(getProjectWorkingFolder() + File.separator + INI_FILENAME);

		String path = confFile.getAbsolutePath();

		try {
			System.getProperties().load(new FileInputStream(path));

			System.out.println("Loaded " + path);
			for (Object p : System.getProperties().keySet()) {
				if (!p.toString().startsWith(MOONRAILS_PROPERTY_PREFIX))
					continue;
				System.out.println("\t" + p + "\t" + System.getProperty(p.toString()));
			}

		} catch (IOException e) {
			System.err.println("Error accessing " + INI_FILENAME + " file in: " + confFile.getParent());
			System.exit(1);
		}
	}

	public static String getArea() {
		return System.getProperty(Conventions.AREA_PROP, Conventions.AREA);
	}	
}
