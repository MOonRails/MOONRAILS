package eu.moonrails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import eu.moonrails.abstraction.AbstractionTree;
import eu.moonrails.abstraction.DataType;
import eu.moonrails.abstraction.Service;
import eu.moonrails.abstraction.ops.Operation;

public abstract class MoonRailsPlugin {

	public static final String GENERATED_FILES_TARGET_FOLDER = File.separator + "gen";
	public static final String TEMPLATES_FOLDER = File.separator + "templates";
	public static final String RESOURCES_FOLDER = File.separator + ".moon";
	public static final String PLUGINS_FOLDER = File.separator + "plugins";

	private AbstractionTree abstractionTree;
	private File workingFolder;
	private File sourceFolder;
	private File templatesFolder;
	private File pluginFolder;

	public abstract String getDriverId();

	public abstract void createGeneratedFiles() throws Exception;

	public MoonRailsPlugin(AbstractionTree atree, File sourceFolder) {
		this.abstractionTree = atree;
		this.sourceFolder = sourceFolder;
		this.workingFolder = new File(sourceFolder.getAbsoluteFile().getParentFile() + GENERATED_FILES_TARGET_FOLDER
				+ File.separator + this.getDriverId());
		this.workingFolder.mkdirs();

		this.pluginFolder = new File(sourceFolder.getAbsoluteFile().getParentFile().getParentFile().getParentFile()
				+ RESOURCES_FOLDER
				+ PLUGINS_FOLDER
				+ File.separator + this.getDriverId());

		this.templatesFolder = new File(pluginFolder.getAbsolutePath() + TEMPLATES_FOLDER);

		System.out.println("Driver working folder is: " + this.workingFolder.getAbsolutePath());
	}

	public void forEachService(ServiceVisitor sev) {
		for (Service s : this.getAbstractionTree().getServices()) {
			sev.visit(s);
		}
	}

	/**
	 * 
	 * Iterates through all operations.
	 * 
	 * @param ov
	 */
	public void forEachOperation(OperationVisitor ov) {
		this.forEachService((tsev) -> {
			for (Operation o : tsev.getOperations()) {
				ov.visit(tsev, o);
			}
		});
	}

	/**
	 * 
	 * Iterates through all data types in
	 * 
	 * @param tv
	 */
	public void forEachDataType(TypeVisitor tv) {
		this.forEachService((tsev) -> {
			for (DataType t : tsev.getDataTypes()) {
				tv.visit(tsev, t);
			}
		});

	}

	/**
	 * 
	 * Iterates through all operations in a service.
	 * 
	 * @param ov
	 */
	public void forEachOperation(Service tsev, OperationVisitor ov) {
		for (Operation o : tsev.getOperations()) {
			ov.visit(tsev, o);
		}
	}

	/**
	 * 
	 * Iterates through all data types in a service.
	 * 
	 * @param ov
	 */
	public void forEachDataType(Service tsev, TypeVisitor tv) {
		for (DataType t : tsev.getDataTypes()) {
			tv.visit(tsev, t);
		}
	}

	/**
	 *
	 * Iterates through through all files
	 * 
	 * @param fiv
	 * @throws IOException
	 */
	public void forEachSourceFile(FileVisitor fiv) throws IOException {
		forEachSourceFile(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		}, fiv);
	}

	/**
	 * 
	 * Iterates through all files that match filter.
	 * 
	 * @param filter
	 * @param fiv
	 * @throws IOException
	 */
	public void forEachSourceFile(FilenameFilter filter, FileVisitor fiv) throws IOException {
		File[] list = this.getSourceFolder().listFiles(filter);
		for (File f : list) {
			fiv.visit(f);
		}
	}

	public AbstractionTree getAbstractionTree() {
		return this.abstractionTree;
	}

	public File getSourceFolder() {
		return this.sourceFolder;
	}

	public void setSourceFolder(File sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public void setAbstractionTree(AbstractionTree abstractionTree) {
		this.abstractionTree = abstractionTree;
	}

	public File getWorkingFolder() {
		return this.workingFolder;
	}

	public File getWorkingFile(String filename) {
		return new File(this.workingFolder + File.separator + filename);
	}

	public File getTemplate(String templateName) {// throws FileNotFoundException {
		File ret = new File(this.templatesFolder.getAbsolutePath() + File.separator + templateName);
		// if (!ret.exists())
		// throw new FileNotFoundException("A template with this name does not exist:" +
		// templateName);
		return ret;
	}

	public String getTemplateAsString(String templateName) throws IOException {
		FileInputStream fis = new FileInputStream(getTemplate(templateName));
		char[] arr = IOUtils.toCharArray(fis);
		return new String(arr);
	}

	public File copyTemplateToWorkspace(String templateName) throws FileNotFoundException, IOException {
		return copyTemplateToWorkspace(templateName, null);
	}

	public File copyTemplateToWorkspace(String templateName, String target) throws FileNotFoundException, IOException {
		if (target == null)
			target = templateName;
		FileUtils.copyFile(getTemplate(templateName), fileOnWorkingFolder(target));
		return fileOnWorkingFolder(target);
	}

	private File fileOnWorkingFolder(String filename) {
		return new File(this.workingFolder + File.separator + filename);
	}

	public interface ServiceVisitor {
		public void visit(Service s);
	}

	public interface OperationVisitor {
		public void visit(Service s, Operation o);
	}

	public interface TypeVisitor {
		public void visit(Service s, DataType dt);
	}

	public interface FileVisitor {
		public void visit(File f);
	}

	public class MoonRailsDriverRuntimeException extends RuntimeException {
		public MoonRailsDriverRuntimeException(String string) {
			super(string);
		}

		private static final long serialVersionUID = 1L;
	}

}
