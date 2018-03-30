package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.AST.Statement_Assign;
/*import cop5556fa17.image.ImageFrame;
import cop5556fa17.image.ImageSupport;*/

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		FieldVisitor fv;
		fv = cw.visitField(ACC_STATIC, "x", "I", null, null);
		fv = cw.visitField(ACC_STATIC, "y", "I", null, null);
		fv = cw.visitField(ACC_STATIC, "X", "I", null, null);
		fv = cw.visitField(ACC_STATIC, "Y", "I", null, null);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 4);
		
		fv = cw.visitField(ACC_STATIC, "DEF_X", "I", null, new Integer(256));
		fv = cw.visitField(ACC_STATIC, "DEF_Y", "I", null, new Integer(256));
		fv = cw.visitField(ACC_STATIC, "Z", "I", null, new Integer(16777215));
	
		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
			String fieldName = declaration_Variable.name;
			String fieldType = null;
			if(declaration_Variable.TYPE == Type.INTEGER) {
				fieldType = "I";
				FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, new Integer(0));
			}
			if(declaration_Variable.TYPE == Type.BOOLEAN) {
				fieldType = "Z";
				FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, new Boolean(false));
			}
			
			
			if(declaration_Variable.e != null) {
				declaration_Variable.e.visit(this, null);	
				mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
			}
		
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		expression_Binary.e0.visit(this, null);
		expression_Binary.e1.visit(this, null);
		
		if(expression_Binary.op == Kind.OP_DIV) {
			mv.visitInsn(IDIV);
		}
		
		if(expression_Binary.op == Kind.OP_PLUS) {
			mv.visitInsn(IADD);
		}
		
		if(expression_Binary.op == Kind.OP_MINUS) {
			mv.visitInsn(ISUB);	
		}
		
		if(expression_Binary.op == Kind.OP_MOD) {
			mv.visitInsn(IREM);
		}
		
		/*if(expression_Binary.op == Kind.OP_POWER) {
			
		}*/
		
		if(expression_Binary.op == Kind.OP_TIMES) {
			mv.visitInsn(IMUL);
		}
		
		if(expression_Binary.op == Kind.OP_AND) {
			mv.visitInsn(IAND);
		}
		
		if(expression_Binary.op == Kind.OP_OR) {
			mv.visitInsn(IOR);
		}
		
		if(expression_Binary.op == Kind.OP_GE) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		
		if(expression_Binary.op == Kind.OP_GT) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		
		if(expression_Binary.op == Kind.OP_LE) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		
		if(expression_Binary.op == Kind.OP_LT) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		
		if(expression_Binary.op == Kind.OP_EQ) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPNE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		
		if(expression_Binary.op == Kind.OP_NEQ) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}

		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.TYPE);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		expression_Unary.e.visit(this, null);
		/*if(expression_Unary.op == Kind.OP_PLUS) {
			
		}*/
		
		if(expression_Unary.op == Kind.OP_MINUS) {
			mv.visitInsn(INEG);
		}
		
		if(expression_Unary.op == Kind.OP_EXCL && expression_Unary.TYPE == Type.BOOLEAN) {
			Label l1 = new Label();
			mv.visitJumpInsn(IFEQ, l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		
		if(expression_Unary.op == Kind.OP_EXCL && expression_Unary.TYPE == Type.INTEGER) {
			mv.visitLdcInsn(Integer.MAX_VALUE);
			mv.visitInsn(IXOR);
		}
		
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.TYPE);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
//		throw new UnsupportedOperationException();
		index.e0.visit(this, null);
		index.e1.visit(this, null);
		if(!index.isCartesian) {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}	
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		String fieldName = expression_PixelSelector.name;
//		String fieldType = "Ljava/awt/image/BufferedImage;";
		mv.visitFieldInsn(GETSTATIC, className, fieldName, ImageSupport.ImageDesc);
		/*expression_PixelSelector.index.e0.visit(this, null);
		expression_PixelSelector.index.e1.visit(this, null);*/
		expression_PixelSelector.index.visit(this, null);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
//		throw new UnsupportedOperationException();
		
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO
		
		expression_Conditional.condition.visit(this, null);
		Label start = new Label();
		Label end = new Label();
		
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPEQ, start);
		expression_Conditional.falseExpression.visit(this, null);
		mv.visitJumpInsn(GOTO, end);
//		throw new UnsupportedOperationException();
		
		
		
		mv.visitLabel(start);
		expression_Conditional.trueExpression.visit(this, null);
		mv.visitLabel(end);
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.TYPE);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		String fieldName = declaration_Image.name;
//		String fieldType = "Ljava/awt/image/BufferedImage;";
		if(declaration_Image.source != null) {
			declaration_Image.source.visit(this, null);
			if(declaration_Image.xSize != null) {
				declaration_Image.xSize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				declaration_Image.ySize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			}
			else{
				mv.visitInsn(ACONST_NULL);
//				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
				mv.visitInsn(ACONST_NULL);
//				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
		}
		else {
			if(declaration_Image.xSize != null) {
				declaration_Image.xSize.visit(this, null);
//				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
//				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
				declaration_Image.ySize.visit(this, null);
				//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
//				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
			}
			else {
				/*mv.visitIntInsn(SIPUSH, 256);
				mv.visitIntInsn(SIPUSH, 256);*/
				/*mv.visitLdcInsn(DEF_X);
				mv.visitLdcInsn(DEF_Y);*/
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
			}		
		}
		
		FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, ImageSupport.ImageDesc, null, null);
		mv.visitFieldInsn(PUTSTATIC, className, fieldName, ImageSupport.ImageDesc);
//		throw new UnsupportedOperationException();
		return  null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		/*String fieldName = source_StringLiteral.fileOrUrl;
		String fieldType = "Ljava/lang/String;";
		mv.visitFieldInsn(GETSTATIC, className , fieldName , fieldType);*/
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
//		throw new UnsupportedOperationException();
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
//		source_CommandLineParam
		// TODO
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, null);
		mv.visitInsn(AALOAD);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		String fieldName = source_Ident.name;
//		String fieldType = "Ljava/lang/String;";
		mv.visitFieldInsn(GETSTATIC, className , fieldName , ImageSupport.StringDesc);
		return null;
//		throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		String fieldName = declaration_SourceSink.name;
//		String fieldType = "Ljava/lang/String;";
		FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, ImageSupport.StringDesc, null, null);
		if(declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, null);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, ImageSupport.StringDesc);
		}
	
		//throw new UnsupportedOperationException();
		return null;
	}
	
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO
		mv.visitLdcInsn(new Integer(expression_IntLit.value));
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg.visit(this, null);
		
		if(expression_FunctionAppWithExprArg.function == Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		
		if(expression_FunctionAppWithExprArg.function == Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		
			expression_FunctionAppWithIndexArg.arg.e0.visit(this, null);
			expression_FunctionAppWithIndexArg.arg.e1.visit(this, null);
			if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_x) {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			}
			if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_y) {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
			}
			if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_a) {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			}
			if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_r) {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		if(expression_PredefinedName.kind == Kind.KW_r) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			//mv.visitVarInsn(ILOAD, 1);
		}
		else if(expression_PredefinedName.kind == Kind.KW_a) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			//mv.visitVarInsn(ILOAD, 2);
		}
		else if(expression_PredefinedName.kind == Kind.KW_R) {
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			mv.visitVarInsn(ISTORE, 3);
			mv.visitVarInsn(ILOAD, 3);
		}
		else if(expression_PredefinedName.kind == Kind.KW_A) {
			mv.visitLdcInsn(new Integer(0));
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitVarInsn(ILOAD, 4);
		}
		else if(expression_PredefinedName.kind == Kind.KW_DEF_X) {
			mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
		}
		else if(expression_PredefinedName.kind == Kind.KW_DEF_Y) {
			mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
		}
		else if(expression_PredefinedName.kind == Kind.KW_Z) {
			mv.visitFieldInsn(GETSTATIC, className, "Z", "I");
		}
		else if(expression_PredefinedName.kind == Kind.KW_x) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		}
		else if(expression_PredefinedName.kind == Kind.KW_y) {
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
		}
		else if(expression_PredefinedName.kind == Kind.KW_X) {
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
		}
		else if(expression_PredefinedName.kind == Kind.KW_Y) {
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
		}
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		String fieldName = statement_Out.name;
		String fieldType = null;
		/*SymbolTable s = new SymbolTable();
		Declaration dec = s.lookupType(statement_Out.name);*/
		if(statement_Out.getDec().TYPE == Type.INTEGER) {
			//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			fieldType = "I";
		}
		if(statement_Out.getDec().TYPE == Type.BOOLEAN) {
			//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			fieldType = "Z";
		}
		if(statement_Out.getDec().TYPE == Type.IMAGE) {
//			fieldType = "Ljava/awt/image/BufferedImage;";
			mv.visitFieldInsn(GETSTATIC, className, fieldName, ImageSupport.ImageDesc);
			/*mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeFunctions.className, "globalLogAddImage", "("+ImageSupport.ImageDesc + ")V", false);
			mv.visitFieldInsn(GETSTATIC, className, fieldName, fieldType);*/
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().TYPE);
			statement_Out.sink.visit(this, null);
			return null;
		}
		
		//statement_Out.sink.visit(this, null);
		//FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		//mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mv.visitFieldInsn(GETSTATIC, className, fieldName, fieldType);
		CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().TYPE);
		if(statement_Out.getDec().TYPE == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
		
		if(statement_Out.getDec().TYPE == Type.INTEGER) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		}
		
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		String fieldName = statement_In.name;
		String fieldType = null;
		//SymbolTable s = new SymbolTable();
		statement_In.source.visit(this, null);
		//Declaration dec = s.lookupType(statement_In.name);
		if(statement_In.getDec().TYPE == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			fieldType = "I";
		}
		if(statement_In.getDec().TYPE == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			fieldType = "Z";
		}
		if(statement_In.getDec().TYPE == Type.IMAGE) {
			fieldType = ImageSupport.ImageDesc;
			Declaration_Image dec = (Declaration_Image) statement_In.d;
			/*mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);*/
//			mv.visitIntInsn(SIPUSH, 256);
//			mv.visitIntInsn(SIPUSH, 256);
			if(statement_In.source != null) {
				//dec.source.visit(this, null);
				if(dec.xSize != null) {
					dec.xSize.visit(this, null);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					dec.ySize.visit(this, null);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}
				else{
					mv.visitInsn(ACONST_NULL);
					//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
					mv.visitInsn(ACONST_NULL);
					//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
				}
				
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			}
			
		}
		//FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		//CodeGenUtils.genLogTOS(GRADE, mv, statement_In.getDec().TYPE);
		// TODO (see comment )
		return null;
//		throw new UnsupportedOperationException();
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if(statement_Assign.lhs.TYPE == Type.INTEGER || statement_Assign.lhs.TYPE == Type.BOOLEAN) {
			statement_Assign.e.visit(this, null);
			statement_Assign.lhs.visit(this, null);
		}
		
		if(statement_Assign.lhs.TYPE == Type.IMAGE) {
					
			/*String name=statement_Assign.lhs.name;		
			
			mv.visitInsn(ICONST_0);
			mv.visitInsn(DUP);
			
			Label yLabel = new Label();
			mv.visitLabel(yLabel);
			mv.visitFieldInsn(PUTSTATIC, className,"y", "I");
			mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
			
			Label end = new Label();
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitJumpInsn(IF_ICMPGE, end);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(DUP);
			
			Label xLabel = new Label();
			mv.visitLabel(xLabel);
			mv.visitFieldInsn(PUTSTATIC, className,"x", "I");
			mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX",ImageSupport.getXSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
			
			Label getylabel = new Label();
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitJumpInsn(IF_ICMPGE, getylabel);
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			mv.visitFieldInsn(GETSTATIC, className,"x", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, xLabel);
			mv.visitLabel(getylabel);
			mv.visitFieldInsn(GETSTATIC, className,"y", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, yLabel);
			mv.visitLabel(end);*/
			
			String fieldName = statement_Assign.lhs.name;
			
			mv.visitFieldInsn(GETSTATIC, className, fieldName, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX",ImageSupport.getXSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
			
			
			mv.visitFieldInsn(GETSTATIC, className, fieldName, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY",ImageSupport.getYSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
			
			mv.visitLdcInsn(new Integer(0));
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			Label l4 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l4);
			Label end = new Label();
			mv.visitJumpInsn(GOTO, end);
			
			mv.visitLabel(l4);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			Label l5 = new Label();
			mv.visitJumpInsn(GOTO, l5);
			
			mv.visitLabel(l5);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			Label l6 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l6);
			Label l8 = new Label();
			mv.visitJumpInsn(GOTO, l8);
			
			mv.visitLabel(l8);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitJumpInsn(GOTO, l3);
			
			mv.visitLabel(l6);
			statement_Assign.e.visit(this, null);
			statement_Assign.lhs.visit(this, null);
			
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			mv.visitJumpInsn(GOTO, l5);
			
			mv.visitLabel(end);
			}
		
			return null;
			
			
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
/*		SymbolTable s = new SymbolTable();
		Declaration d = s.lookupType(lhs.name);*/
		String fieldName = lhs.name;
		String fieldType = null;
		
		if(lhs.TYPE == Type.INTEGER) {
			if(lhs.index != null) {
				lhs.index.visit(this, null);
			}
			fieldType = "I";
			//FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		else if(lhs.TYPE == Type.BOOLEAN) {
			if(lhs.index != null) {
				lhs.index.visit(this, null);
			}

			fieldType = "Z";
			//FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		
		else if(lhs.TYPE == Type.IMAGE) {
//			fieldType = "Ljava/awt/image/BufferedImage;";
			mv.visitFieldInsn(GETSTATIC, className, fieldName, ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
		}
		
		return null;
//		throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
//		throw new UnsupportedOperationException();
		String fieldName = sink_Ident.name;
//		String fieldType = "Ljava/lang/String;";
		mv.visitFieldInsn(GETSTATIC, className, fieldName, ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		if(expression_BooleanLit.value == true) {
			mv.visitInsn(ICONST_1);
		}
		
		if(expression_BooleanLit.value == false) {
			mv.visitInsn(ICONST_0);
		}
		
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		
		//SymbolTable s = new SymbolTable();
		//Declaration dec = s.lookupType(expression_Ident.name);
		
		String fieldName = expression_Ident.name;
		String fieldType = null;
		
		if(expression_Ident.TYPE == Type.INTEGER) {
			//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			fieldType = "I";
		}
		if(expression_Ident.TYPE == Type.BOOLEAN) {
			//mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			fieldType = "Z";
		}
		//FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, "Ljava/lang/String;", null, null);
		mv.visitFieldInsn(GETSTATIC, className , fieldName , fieldType);
		
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.TYPE);
		return null;
	}

}
