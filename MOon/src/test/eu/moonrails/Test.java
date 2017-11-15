package test.eu.moonrails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.CommentHandler;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.pdom.PDOMWriter.FileInAST;
//import org.eclipse.core.runtime.CoreException;

public class Test {

	private static NodeCommentMap astCommenter;

	public static void main_test(String[] args) throws Exception {
		String code = "typedef float myType; void f(int i) {}; int rint(float a, double b){}; void f(double d) {}; "
				+ "void main() { myType var = 4; f(var); }";
		IASTTranslationUnit translationUnit = parse(code.toCharArray());
		translationUnit = parseFile("/does-not-exist/HelloWorld.c");
		
		final ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTDeclaration declaration) {
				for (IASTComment c : astCommenter.getLeadingCommentsForNode(declaration)) {
					System.out.println("TRAILING: " + c.toString());
				}

				IASTName name = getName(declaration);

				IBinding b = name.resolveBinding();
				IFunctionType type = (b instanceof IFunction) ? ((IFunction) b).getType() : null;
				// type.
				if (type != null) {
					System.out.println("Function declared: " + name);
					System.out.println("\t Returns: " + type.getReturnType());
				} else {
					throw new NullPointerException();
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

			@Override
			public int visit(IASTName name) {
				// Looking only for references, not declarations
				if (name.isDeclaration()) {

					IBinding b = name.resolveBinding();
					IFunctionType type = (b instanceof IFunction) ? ((IFunction) b).getType() : null;
					// type.
					if (type != null) {
						// System.out.print("Referencing " + name + ", type " +
						// ASTTypeUtil.getType(type));

						System.out.println(">> " + name.getClass().getSimpleName());
						System.out.println(">> " + name.getParent().getClass().getSimpleName());
						IASTFunctionDeclarator fd = (IASTFunctionDeclarator) name.getParent();

						for (IASTComment c : astCommenter.getLeadingCommentsForNode(fd)) {
							System.out.println("## " + c.toString());
						}

						System.out.println("Function declared: " + name);
						System.out.println("\t Returns: " + type.getReturnType());

						if (type.getParameterTypes().length == 0) {
							System.out.println("\t Receives: <nothing>");
						} else {
							System.out.println("\t Receives: ");
							for (IType t : type.getParameterTypes()) {
								System.out.println("\t\t" + t);
							}
						}

						for (IASTComment c : astCommenter.getLeadingCommentsForNode(name.getParent())) {
							System.out.println("TRAILING: " + c.toString());
						}

						// List<IASTComment> trailing =
						// astCommenter.getTrailingCommentsForNode(name);
						// List<IASTComment> freestanding =
						// astCommenter.getFreestandingCommentsForNode(name);
						// List<IASTComment> leading =
						// astCommenter.getLeadingCommentsForNode(name);

						for (IASTComment c : astCommenter.getAllCommentsForNode(name)) {
							System.out.println("TRAILING: " + c.toString());
						}

					}

				}
				return ASTVisitor.PROCESS_CONTINUE;
			}

			@Override
			public int visit(IASTTranslationUnit tu) {
				System.out.println("visiting: " + tu.toString());
				for (IASTComment comment : tu.getComments()) {
					System.out.println("COMMENT:: " + comment.toString());
				}
				return super.visit(tu);
			}
		};
		visitor.shouldVisitDeclarations = true;
		translationUnit.accept(visitor);
	}

	private static IASTTranslationUnit parse(char[] code) throws Exception {
		FileContent fc = FileContent.create("/home/feiteira/tmp/test.cpp", code);
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		IASTTranslationUnit tu = GCCLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);

		astCommenter = ASTCommenter.getCommentedNodeMap(tu);
		return tu;
	}

	private static IASTTranslationUnit parseFile(String path) throws Exception {
		FileInputStream fis = new FileInputStream(new File(path));
		char[] arr = IOUtils.toCharArray(fis);
		return parse(arr);
	}

	public static void addCommentsToMap(IASTTranslationUnit ast, NodeCommentMap commentMap) {
		if (ast == null || commentMap.isASTCovered(ast)) {
			return;
		}
		IASTComment[] commentsArray = ast.getComments();
		List<IASTComment> comments = new ArrayList<>(commentsArray.length);
		for (IASTComment comment : commentsArray) {
			if (comment.isPartOfTranslationUnitFile()) {
				comments.add(comment);
			}
		}
		CommentHandler commentHandler = new CommentHandler(comments);
		ASTCommenterVisitor commenter = new ASTCommenterVisitor(commentHandler, commentMap);

		commentMap.setASTCovered(ast);
		ast.accept(commenter);
	}
}
