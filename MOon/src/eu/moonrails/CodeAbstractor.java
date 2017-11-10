package eu.moonrails;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.runtime.CoreException;

import eu.moonrails.abstraction.AbstractionTree;
import eu.moonrails.abstraction.BasicType;
import eu.moonrails.abstraction.CompositeType;
import eu.moonrails.abstraction.DataType;
import eu.moonrails.abstraction.Parameter;
import eu.moonrails.abstraction.Service;
import eu.moonrails.abstraction.ops.SimpleSend;
import eu.moonrails.abstraction.ops.SimpleSubscription;

public class CodeAbstractor extends ASTVisitor {
	public static Parameter createParameterFromBasicType(IType type) throws InvalidClassException {
		IBasicType basicType = (IBasicType) type;
		DataType mtype;

		switch (basicType.getKind()) {
		case eFloat:
			mtype = BasicType.FLOAT;
			break;
		case eInt:
			mtype = BasicType.INT;
			break;
		case eBoolean:
			mtype = BasicType.BOOLEAN;
			break;

		default:
			throw new InvalidClassException("Unsupported class for type:" + type.getClass());
		}

		System.out.println("mtype " + mtype);
		return new Parameter(mtype);
	}

	public static boolean isVoid(IType type) {
		if (type instanceof IBasicType) {
			return (((IBasicType) type).getKind() == Kind.eVoid);
		}
		return false;
	}

	private NodeCommentMap astCommenter;

	private AbstractionTree atree;
	private Service service;

	private IASTTranslationUnit translationUnit;

	public CodeAbstractor(String filePath) throws IOException, CoreException {
		this.atree = new AbstractionTree();
		// for demo purposes we have only one file and one service
		service = atree.addService(getServiceName(filePath));

		this.translationUnit = parseFile(filePath);
		this.shouldVisitDeclarations = true;
		translationUnit.accept(this);
	}

	private Parameter convertFromIASTParameterDeclaration(IASTParameterDeclaration declaration)
			throws InvalidClassException {
		IASTName name = declaration.getDeclarator().getName();
		IBinding b = name.resolveBinding();

		System.out.println(b.getClass());
		System.out.println(b.getClass().getSuperclass());
		if (b instanceof IParameter) {
			IParameter p = (IParameter) b;
			Parameter ret = createParameterFromType(p.getType());
			ret.setName(p.getName());
			return ret;
		}
		return null;
	}

	public Parameter createParameterFromField(IField field) throws InvalidClassException {
		Parameter ret = createParameterFromType(field.getType());
		ret.setName(field.getName());
		return ret;
	}

	public Parameter createParameterFromType(IType type) throws InvalidClassException {
		if (type instanceof IBasicType)
			return createParameterFromBasicType(type);

		if (type instanceof ICompositeType) {
			ICompositeType ctype = (ICompositeType) type;
			return new Parameter(service.getDataTypeByName(ctype.getName()));
			// throw new Parameter();
		}

		throw new InvalidClassException("Only basic tyes are supported at the moment, received: " + type.getClass());
	}

	public AbstractionTree getAbstractionTree() {
		return atree;
	}

	public IASTCompositeTypeSpecifier getCompositeSpecifier(IASTDeclaration declaration) {
		for (IASTNode node : declaration.getChildren()) {
			if (node instanceof IASTCompositeTypeSpecifier)
				return (IASTCompositeTypeSpecifier) node;
		}
		return null;
	}

	private IASTFunctionDeclarator getFunctionDeclarator(IASTDeclaration declaration) {
		for (IASTNode node : declaration.getChildren()) {
			if (node instanceof IASTFunctionDeclarator) {
				// return ((IASTFunctionDeclarator) node).getName();
				return (IASTFunctionDeclarator) node;
			}
		}
		return null;
	}

	private String getServiceName(String path) {
		int start = path.lastIndexOf('/') + 1;
		int end = path.lastIndexOf('.');
		if (start < 0)
			start = 0;
		if (end < 0)
			end = path.length();

		return path.substring(start, end);
	}

	private IASTTranslationUnit parse(char[] code) throws CoreException {
		FileContent fc = FileContent.create("/tmp/test.cpp", code);
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		IASTTranslationUnit tu = GPPLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);

		this.astCommenter = ASTCommenter.getCommentedNodeMap(tu);
		return tu;
	}

	private IASTTranslationUnit parseFile(String path) throws IOException, CoreException {
		FileInputStream fis = new FileInputStream(new File(path));
		char[] arr = IOUtils.toCharArray(fis);
		return this.parse(arr);
	}

	public void processComposite(IASTCompositeTypeSpecifier composite, List<IASTComment> list)
			throws InvalidClassException {
		IASTName name = composite.getName();
		IBinding b = name.resolveBinding();
		ICompositeType ctype = (ICompositeType) b; // So far I'm assuming this will always cast

		String comment = list.size() > 0 ? list.get(0).toString() : "TODO: No comment provided";

		System.out.println(ctype);

		CompositeType comp = new CompositeType(ctype.getName(), comment,service);

		for (IField field : ctype.getFields()) {
			System.out.println("\tField " + field.getName() + " :: " + field.getType());
			comp.addParameter(createParameterFromField(field));
		}

		service.addDataType(comp);
		System.out.println("COMP " + name);

	}

	private void processFunction(IASTName iastName, IType returnType, IASTParameterDeclaration[] parameters,
			List<IASTComment> list) throws InvalidClassException {
		String name = iastName.toString();
		String comment = list.size() > 0 ? list.get(0).toString() : "TODO: No comment provided";

		// its a simple send
		if (isVoid(returnType) && parameters.length < 2) {
			Parameter param = null;
			if (parameters.length > 0) {
				param = convertFromIASTParameterDeclaration(parameters[0]);
			}
			service.addOperation(new SimpleSend(iastName.toString(), param)).setComment(comment);
		} else
		// its a simple pub-sub
		if (name.startsWith("publish") && !isVoid(returnType) && parameters.length == 0) {
			service.addOperation(
					new SimpleSubscription(iastName.toString(), createParameterFromType(returnType), comment));
		} else {
			System.out.println("Skipping method: " + name);
		}
	}

	private void processFunctionDeclarator(IASTFunctionDeclarator declarator) throws InvalidClassException {
		IASTName name = declarator.getName();
		IBinding b = name.resolveBinding();
		IFunctionType type = (b instanceof IFunction) ? ((IFunction) b).getType() : null;
		if (type != null) {
			System.out.println("Function declared: " + name);
			System.out.println("\t Returns: " + type.getReturnType());

			// creates a list of all the parameter declarations
			ArrayList<IASTParameterDeclaration> parameters = new ArrayList<>();
			for (IASTNode tmp : declarator.getChildren()) {
				if (tmp instanceof IASTParameterDeclaration) {
					parameters.add((IASTParameterDeclaration) tmp);
				}
			}

			for (IType f : type.getParameterTypes()) {
				if (f instanceof IProblemType)
					System.out.println("\t Receives (Problem):" + ((IProblemType) f).getMessage());
				else
					System.out.println("\t Receives:" + f);
			}
			IASTParameterDeclaration[] arr_params = parameters.toArray(new IASTParameterDeclaration[parameters.size()]);
			processFunction(name, type.getReturnType(), arr_params,
					astCommenter.getLeadingCommentsForNode(declarator.getParent()));
		} else {
			throw new NullPointerException("Null function type");
		}
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		try {
			// it is a composite
			IASTCompositeTypeSpecifier composite = getCompositeSpecifier(declaration);
			if (composite != null) {
				processComposite(composite, astCommenter.getLeadingCommentsForNode(declaration));
			}

			// now let's check for functions
			IASTFunctionDeclarator functionDeclarator = getFunctionDeclarator(declaration);

			if (functionDeclarator != null) {
				for (IASTComment c : astCommenter.getLeadingCommentsForNode(declaration)) {
					System.out.println("TRAILING: " + c.toString());
				}

				processFunctionDeclarator(functionDeclarator);
			}

		} catch (InvalidClassException e) {
			throw new RuntimeException(e);
		}
		return PROCESS_SKIP;
	}

}
