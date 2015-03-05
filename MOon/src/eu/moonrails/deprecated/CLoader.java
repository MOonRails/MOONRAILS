package eu.moonrails.deprecated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
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

public class CLoader {
	private NodeCommentMap astCommenter;
	private IASTTranslationUnit translationUnit;

	public CLoader(String filePath) throws IOException, CoreException {
		this.translationUnit = parseFile(filePath);

		final ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTDeclaration declaration) {
				/*
				 * System.out.println(declaration.getRawSignature());
				 * 
				 * IASTFunctionDeclarator fd = null; for(IASTNode node:
				 * declaration.getChildren()){ System.out.println("CHILD: "+
				 * node); if(node instanceof IASTFunctionDeclarator){ fd =
				 * (IASTFunctionDeclarator) node; break; } }
				 * 
				 * System.out.println(fd); IASTName name = fd.getName();
				 * 
				 * this.visit(name);
				 */
				for (IASTComment c : astCommenter.getLeadingCommentsForNode(declaration)) {
					System.out.println("TRAILING: " + c.toString());
				}

				IASTName name = getName(declaration);
				if (name != null) {
					IBinding b = name.resolveBinding();
					IFunctionType type = (b instanceof IFunction) ? ((IFunction) b).getType() : null;
					// type.
					if (type != null) {
						System.out.println("Function declared: " + name);
						System.out.println("\t Returns: " + type.getReturnType());

						for (IType f : type.getParameterTypes()) {
							if (f instanceof IProblemType)
								System.out.println("\t Receives (Problem):" + ((IProblemType) f).getMessage());
							else
								System.out.println("\t Receives:" + f);

						}
					} else {
						throw new NullPointerException();
					}
				}
				return PROCESS_SKIP;
			}

			private IASTName getName(IASTDeclaration declaration) {
				for (IASTNode node : declaration.getChildren()) {
					// System.out.println("CHILD: "+ node);
					if (node instanceof IASTFunctionDeclarator) {
						return ((IASTFunctionDeclarator) node).getName();
					}
				}
				return null;
			}
		};

		visitor.shouldVisitDeclarations = true;
		translationUnit.accept(visitor);

	}

	private IASTTranslationUnit parse(char[] code) throws CoreException {
		FileContent fc = FileContent.create("/home/feiteira/tmp/test.cpp", code);
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

}
