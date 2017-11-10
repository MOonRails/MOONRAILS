package eu.moonrails.plugins.builtin;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.moonrails.MoonRailsPlugin;
import eu.moonrails.abstraction.AbstractionTree;
import eu.moonrails.abstraction.CompositeType;
import eu.moonrails.abstraction.Parameter;
import eu.moonrails.abstraction.ops.Operation;
import eu.moonrails.abstraction.ops.SimpleSend;
import eu.moonrails.abstraction.ops.SimpleSubscription;

public class MOXPlugin extends MoonRailsPlugin {
	public static final String ID = MOXPlugin.class.getName();

	public static final String AREA = "MOR";

	private Document doc;
	private File targetFile;
	private final Element mal_specification;
	private final HashMap<Class, IPType> interaction_patern;
	HashMap<String, Element> servicesElement = new HashMap<>();
	HashMap<String, Element> compositesElement = new HashMap<>();

	class IPType {
		private String type;
		private String messageType;

		public IPType(String type, String messageType) {
			super();
			this.type = type;
			this.messageType = messageType;
		}

		public IPType(String code) {
			super();
			this.type = code + "IP";
			this.messageType = code;
		}
	}

	public MOXPlugin(AbstractionTree atree, File sourceFolder) throws ParserConfigurationException {
		super(atree, sourceFolder);
		interaction_patern = initInteractionPatternTable();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// root elements
		doc = docBuilder.newDocument();
		// Element rootElement = doc.createElement("company");
		mal_specification = createMALSpecification();
		doc.appendChild(mal_specification);
	}

	@SuppressWarnings("rawtypes")
	public HashMap<Class, IPType> initInteractionPatternTable() {
		HashMap<Class, IPType> ret = new HashMap<>();
		ret.put(SimpleSend.class, new IPType("mal:send"));
		ret.put(SimpleSubscription.class, new IPType("mal:pubsubIP", "mal:publishNotify"));
		return ret;
	}

	private Element createMALSpecification() {
		Element ret = doc.createElement("mal:specification");
		ret.setAttribute("xmlns:com", "http://www.ccsds.org/schema/COMSchema");
		ret.setAttribute("xmlns:svg", "http://www.w3.org/2000/svg");
		ret.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		ret.setAttribute("xmlns:mal", "http://www.ccsds.org/schema/ServiceSchema");

		createMALArea(ret);
		return ret;
	}

	private Element createMALArea(Element specification) {
		Element area = doc.createElement("mal:area");
		area.setAttribute("name", AREA);
		area.setAttribute("number", "99");
		area.setAttribute("version", "1");
		specification.appendChild(area);

		createOperations(area);
		createComposites(area);
		return area;
	}

	private void createComposites(Element area) {
		forEachDataType((mor_service, mor_type) -> {
			Element serv = getOrCreateService(mor_service.getName(), area);
			// data type element
			Element dte;

			if (serv.getElementsByTagName("mal:dataTypes").getLength() == 0) {
				dte = doc.createElement("mal:dataTypes");
				serv.appendChild(dte);
			} else {
				dte = (Element) serv.getElementsByTagName("dataTypes").item(0);
			}

			if (mor_type instanceof CompositeType) {
				CompositeType mor_composite = (CompositeType) mor_type;
				dte.appendChild(createComposite(mor_composite, dte.getChildNodes().getLength() + 1));
			}

			serv.appendChild(dte);
		});
	}

	private void createOperations(Element area) {
		forEachOperation((mor_service, mor_operation) -> {
			Element serv = getOrCreateService(mor_service.getName(), area);

			// Creates capability set element
			Element cse;
			// TODO: Accept multiple capability sets
			if (serv.getElementsByTagName("mal:capabilitySet").getLength() == 0) {
				cse = doc.createElement("mal:capabilitySet");
				cse.setAttribute("number", "1");
				serv.appendChild(cse);
			} else {
				cse = (Element) serv.getElementsByTagName("mal:capabilitySet").item(0);
			}
			int elements_in_capability_set = cse.getChildNodes().getLength();

			cse.appendChild(createOperation(mor_operation, elements_in_capability_set + 1));
			serv.appendChild(cse);
		});
	}

	private Element createOperation(Operation mor_operation, int nuber) {
		final IPType ip = interaction_patern.get(mor_operation.getClass());
		final Element op = doc.createElement(ip.type);
		final Element messages = doc.createElement("mal:messages");
		boolean op_type_found = false;

		op.setAttribute("name", mor_operation.getName());
		op.setAttribute("number", "" + nuber);
		op.setAttribute("supportInReplay", "false");
		op.setAttribute("comment", mor_operation.getComment());

		op_type_found |= mor_operation.ifTypeThenDo(SimpleSend.class, (mop) -> {
			messages.appendChild(defineSimpleSend((SimpleSend) mop));
		});

		op_type_found |= mor_operation.ifTypeThenDo(SimpleSubscription.class, (mop) -> {
			messages.appendChild(defineSimpleSubscription((SimpleSubscription) mop));
		});

		if (!op_type_found) {
			throw new MoonRailsDriverRuntimeException("Operation type \'" + mor_operation.getClass().getCanonicalName()
					+ "\' not supported by this driver. ");
		}

		if (messages != null) {
			op.appendChild(messages);
		}
		return op;
	}

	private Element createComposite(CompositeType mor_composite, int shortFormPart) {
		Element comp = doc.createElement("mal:composite");

		comp.setAttribute("name", mor_composite.getName());
		comp.setAttribute("shortFormPart", shortFormPart + "");
		comp.setAttribute("comment", mor_composite.getComment());

		for (Parameter mor_parameter : mor_composite.getParameters()) {
			Element field = doc.createElement("mal:field");
			field.setAttribute("canBeNull", "false");// by convention this is never false
			field.setAttribute("name", mor_parameter.getName());// by convention this is never false

			Element type = createTypeFromParameter(mor_parameter);
			field.appendChild(type);

			comp.appendChild(field);
		}

		return comp;
	}

	private Element defineSimpleSend(SimpleSend mop) {
		Element msg = doc.createElement("mal:send");

		// does this simple send, also send an parameter?
		if (mop.hasParameters()) {
			Element field = doc.createElement("mal:field");
			field.setAttribute("name", mop.getParameter().getName());

			// TODO hardcoded to MAL types, composites are not supported
			Element type = createTypeFromParameter(mop.getParameter());

			field.appendChild(type);
			msg.appendChild(field);
		}
		return msg;
	}


	private Element createTypeFromParameter(Parameter p) {
		Element type =  doc.createElement("mal:type");
		// TODO lists are not supported
		type.setAttribute("list", "false");
		type.setAttribute("name", p.getType().getName());

		if (p.getType().isBasicType()) {			
			type.setAttribute("area", "MAL");
		} else {// composite
			type.setAttribute("area", AREA);
			CompositeType ctype = (CompositeType) p.getType();
			type.setAttribute("service", ctype.getService().getName());
		}
		return type;
	}

	private Element defineSimpleSubscription(SimpleSubscription mop) {
		Element msg = doc.createElement("mal:publishNotify");
		Element type = doc.createElement("mal:type");
		// TODO hardcoded to MAL types, composites are not supported
		type.setAttribute("area", "MAL");

		// TODO lists are not supported
		type.setAttribute("list", "false");

		type.setAttribute("name", mop.getSubscriptionType().getType().getName());

		msg.appendChild(type);
		return msg;

	}

	private Element createService(Element area) {
		Element ret = doc.createElement("mal:service");
		ret.setAttribute("xsi:type", "com:ExtendedServiceType");
		area.appendChild(ret);
		return ret;
	}

	private Element getOrCreateService(String name, Element area) {
		Element serv = servicesElement.get(name);
		// create service if it does not exist
		if (serv == null) {
			serv = createService(area);
			serv.setAttribute("name", name);
			serv.setAttribute("number", "" + (servicesElement.keySet().size() + 1));
			area.appendChild(serv);
			servicesElement.put(name, serv);
		}
		return serv;
	}

	@Override
	public String getDriverId() {
		return ID;
	}

	@Override
	public void createGeneratedFiles() throws TransformerException {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource source = new DOMSource(doc);

		StreamResult result = new StreamResult(this.getTargetFile());
		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);
	}

	private File getTargetFile() {
		if (this.targetFile == null) {
			// TODO: Right now only supports one service
			this.targetFile = new File(this.getWorkingFolder().getAbsolutePath() + File.separator
					+ this.getAbstractionTree().getServices().get(0).getName() + ".xml");
			System.out.println("Target file set to:" + this.targetFile.getAbsolutePath());
		}
		return this.targetFile;
	}

}
