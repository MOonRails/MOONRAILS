package eu.moonrails.plugins.builtin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Arrays;

import javax.lang.model.type.UnknownTypeException;

import org.apache.commons.io.IOUtils;

import eu.moonrails.MoonRailsPlugin;
import eu.moonrails.MoonRailsPlugin.OperationVisitor;
import eu.moonrails.abstraction.AbstractionTree;
import eu.moonrails.abstraction.BasicType;
import eu.moonrails.abstraction.CompositeType;
import eu.moonrails.abstraction.DataType;
import eu.moonrails.abstraction.Parameter;
import eu.moonrails.abstraction.Service;
import eu.moonrails.abstraction.ops.Operation;
import eu.moonrails.abstraction.ops.SimpleSend;
import eu.moonrails.abstraction.ops.SimpleSubscription;

public class SingleFileArduinoPlugin extends MoonRailsPlugin {

	public static final String ID = "Arduino";

	public static final int OUTPUT_COMMENT_MAX_COLUMNS = 80;

	public static final String[] METHOD_SKIP_LIST = { "on_setup" };

	private File sourceFile;
	private File targetFile;
	private FileOutputStream outputStream;

	public SingleFileArduinoPlugin(AbstractionTree atree, File workingFolder) {
		super(atree, workingFolder);
		this.sourceFile = this.getSourceFile();
		System.out.println("Source file set to: " + this.sourceFile.getAbsolutePath());

	}

	@Override
	public void createGeneratedFiles() throws IOException {
		this.getTargetFile();

		FileInputStream fis = new FileInputStream(this.getSourceFile());
		byte[] arr = IOUtils.toByteArray(fis);

		outputStream = new FileOutputStream(this.targetFile);

		outputStream.write(arr);

		declareVariables();
		appendTemplate("setup_and_serial.ino");
		appendIds();
		appendPubSubCode();
		handleMainLoop();
		outputStream.close();

		// copies the Arduino makefile from template
		copyTemplateToWorkspace("Makefile");
	}

	private void appendIds() throws IOException {
		int id_counter = 0;
		appendCenteredComment("Appending ids");
		for (Operation op : this.getAbstractionTree().getServices().get(0).getOperations()) {
			// skip if it's in the skip list
			if (skip(op))
				continue;

			appendString("const int id_" + op.getName() + " = " + id_counter + ";");
			id_counter++;
		}
	}

	private void appendPubSubCode() throws IOException {
		appendCenteredComment("Appending Pub Sub specific");
		for (Operation op : this.getAbstractionTree().getServices().get(0).getOperations()) {
			// skip if it's in the skip list
			if (skip(op))
				continue;

			if (op instanceof SimpleSubscription) {
				handleSimpleSubscription((SimpleSubscription) op);
			}
		}
		appendCenteredComment("Done Appending Pub Sub specific");
	}

	private void declareVariables() throws IOException {
		appendCenteredComment("Declaring Operation IDs");
	}

	private void appendCenteredComment(String str) throws IOException {
		if (str.length() > OUTPUT_COMMENT_MAX_COLUMNS)
			throw new ArrayIndexOutOfBoundsException("Comment is to long to fit in one line");

		String line = "";
		int start_pos = OUTPUT_COMMENT_MAX_COLUMNS / 2 - str.length() / 2;
		for (int i = 0; i < OUTPUT_COMMENT_MAX_COLUMNS; i++) {
			if (i < start_pos || (i - start_pos) >= str.length())
				line += '-';
			else
				line += str.charAt(i - start_pos);
		}
		appendComment(line);
	}

	private void appendComment(String str) throws IOException {
		String commentStart = "//";

		if (str.length() > OUTPUT_COMMENT_MAX_COLUMNS)
			throw new ArrayIndexOutOfBoundsException("Comment is to long to fit in one line");

		appendString(commentStart + str);
	}

	private void appendString(String str) throws IOException {
		outputStream.write((str + "\n").getBytes());
	}

	@Override
	public String getDriverId() {
		return ID;
	}
	
	private void appendTemplate(String templateName) throws IOException {
		this.appendCenteredComment("Start of Template: " + templateName);
		this.appendString(this.getTemplateAsString(templateName));
		this.appendCenteredComment("End of Template: " + templateName);
	}

	private File getSourceFile() {
		File[] list = this.getSourceFolder().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ino");
			}
		});
		switch (list.length) {
		case 0:
			return null;
		case 1:
			return list[0];
		default:
			throw new ArrayIndexOutOfBoundsException(
					"More than one file match. This driver supports only one .ino file.");
		}
	}

	private File getTargetFile() {
		if (this.targetFile == null) {
			String folderPath = this.getWorkingFolder().getAbsolutePath() + File.separator
					+ this.getAbstractionTree().getServices().get(0).getName();

			// create the sketch folder
			File folder = new File(folderPath);
			folder.mkdir();
			// create the file
			this.targetFile = new File(folderPath + File.separator + this.sourceFile.getName());
			System.out.println("Target file set to:" + this.targetFile.getAbsolutePath());
		}
		return this.targetFile;
	}

	private void handleSimpleSubscription(SimpleSubscription op) throws IOException {
		appendCenteredComment("Start PubSub " + op.getName());
		String data = getTemplateAsString("pubsub.ino");
		data = data.replaceAll("%OP%", op.getName());
		appendString(data);
		appendCenteredComment("End PubSub " + op.getName());
	}

	private void handleMainLoop() throws IOException {
		String data = getTemplateAsString("main_loop.ino");

		String tmp = "";
		for (Operation op : this.getAbstractionTree().getServices().get(0).getOperations()) {
			// skip if it's in the skip list
			if (skip(op))
				continue;
			tmp += "      case id_" + op.getName() + ":{\n";
			tmp += stringForReadParam(op);
			tmp += "		break;}\n";
		}

		data = data.replace("%CASE%", tmp);
		appendString(data);
	}

	public void forEachExternalOperation(Service tsev, OperationVisitor ov) {
		forEachOperation(tsev, (s, o) -> {
			if (!Arrays.asList(METHOD_SKIP_LIST).contains(o.getName()))
				ov.visit(s, o);
		});
	}

	private boolean skip(Operation o) {
		return Arrays.asList(METHOD_SKIP_LIST).contains(o.getName());
	}

	private String stringForReadParam(Operation op) {
		String ret = "";

		DataType type = BasicType.INT;
		if (op.hasParameters()) {
			if (op instanceof SimpleSend) {
				type = ((SimpleSend) op).getParameter().getType();
			} else if (op instanceof SimpleSubscription) {
				type = BasicType.INT;
			} else {
				throw new UnknownTypeException(null, op);
			}

			if (type.isBasicType()) {
				ret += OnType(BasicType.BOOLEAN, type,
						(t) -> "		bool param = params.charAt(0) == '0'?false:true;\n");
				ret += OnType(BasicType.INT, type, (t) -> "		long param = atol(params.c_str());\n");
				ret += OnType(BasicType.UBYTE, type, (t) -> "		unsigned char param = atoi(params.c_str());\n");
				ret += OnType(BasicType.FLOAT, type, (t) -> "		float param = atof(params.c_str());\n");
			} else {
				ret += readComposite((CompositeType) type);
			}

		}

		if (op instanceof SimpleSend) {
			// if no parameter, then just invoke
			if (!op.hasParameters()) {
				ret += "		" + op.getName() + "();\n";
			} else {
				ret += "		" + op.getName() + "(param);\n";
			}

		} else if (op instanceof SimpleSubscription) {
			ret += "		long param = atol(params.c_str());\n";
			ret += "		register_" + op.getName() + "(param);\n";
		}
		return ret;
	}

	private String readComposite(CompositeType type) {
		String ret = "";
		// declare
		ret += "\t\t " + type.getName() + " param;\n";

		int cnt = 0;
		ret += "\t\t char *p = (char *)params.c_str();\n";

		for (Parameter p : type.getParameters()) {
			final String const_var = "str_param" + cnt;
			ret += "\t\t\t const char * " + const_var + " = strtok_r(p, \",\", &p);//reads next field in line\n";

			ret += "\t\t\t param." + p.getName() + " = ";
			ret += OnType(BasicType.BOOLEAN, p.getType(), (t) -> " (*" + const_var + " == '0')?false:true;\n");
			ret += OnType(BasicType.INT, p.getType(), (t) -> "	atol(" + const_var + ");\n");
			ret += OnType(BasicType.UBYTE, p.getType(), (t) -> "	(unsigned char) atoi(" + const_var + ");\n");
			ret += OnType(BasicType.FLOAT, p.getType(), (t) -> " atof(" + const_var + ");\n");
			cnt++;
		}

		return ret;
	}

	public String OnType(DataType ref, DataType type, OnTypeDo task) {
		if (ref.getName().equals(type.getName()))
			return task.task(type);
		return "";
	}

	public interface OnTypeDo {
		public String task(DataType type);
	}

	public String getArduinoTypeName(DataType dt) {
		// composites go by name, easy
		if (!dt.isBasicType())
			return dt.getName();

		BasicType basicType = (BasicType) dt;
		switch (basicType.asEnum()) {
		case BOOLEAN:
			return dt.getName();
		case FLOAT:
			return dt.getName();
		case UBYTE:
			return "(unsigned char)";
		case INT:
			return dt.getName();

		}

		try {
			throw new InvalidClassException("Unsuported data type by " + this.getClass());
		} catch (InvalidClassException e) {
			throw new RuntimeException(e);
		}
	}

}
