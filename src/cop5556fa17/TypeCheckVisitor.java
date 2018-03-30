package cop5556fa17;

import java.awt.Image;
import java.net.URL;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
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
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.SymbolTable;


public class TypeCheckVisitor implements ASTVisitor {
	

		SymbolTable s = new SymbolTable();
		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		Type a = TypeUtils.getType(declaration_Variable.type);
		declaration_Variable.TYPE = a;
		if(declaration_Variable.e != null){
			Expression e = (Expression) declaration_Variable.e.visit(this, null);
			if(declaration_Variable.TYPE != e.TYPE){
				throw new SemanticException(declaration_Variable.firstToken, "Error Encountered");
			}
		}
		
		Declaration dec = s.lookupType(declaration_Variable.name);
		if(dec == null){
			s.insert(declaration_Variable.name, declaration_Variable);
		}
		else{
			throw new SemanticException(declaration_Variable.firstToken, "Error Returned");
		}
		return declaration_Variable;
		
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		Expression e0 = (Expression) expression_Binary.e0.visit(this, null);
		Expression e1 = (Expression) expression_Binary.e1.visit(this, null);
		
		if(e0.TYPE != e1.TYPE){
			throw new SemanticException(expression_Binary.firstToken, "Error Encountered");
		}
		expression_Binary.TYPE = null;
		
		if(expression_Binary.op == Kind.OP_EQ || expression_Binary.op == Kind.OP_NEQ){
			expression_Binary.TYPE = Type.BOOLEAN;
		}
		else if((expression_Binary.op == Kind.OP_GE || expression_Binary.op == Kind.OP_GT || expression_Binary.op == Kind.OP_LT ||
				expression_Binary.op == Kind.OP_LE) && e0.TYPE == Type.INTEGER){
			expression_Binary.TYPE = Type.BOOLEAN;
		}
		else if((expression_Binary.op == Kind.OP_AND || expression_Binary.op == Kind.OP_OR) && (e0.TYPE == Type.INTEGER || e0.TYPE == Type.BOOLEAN)){
			expression_Binary.TYPE = e0.TYPE;
		}
		else if((expression_Binary.op == Kind.OP_DIV || expression_Binary.op == Kind.OP_MINUS || expression_Binary.op == Kind.OP_MOD || 
				expression_Binary.op == Kind.OP_PLUS || expression_Binary.op == Kind.OP_POWER || expression_Binary.op == Kind.OP_TIMES) &&
 				e0.TYPE == Type.INTEGER){
			expression_Binary.TYPE = Type.INTEGER;
		}
		else{
			throw new SemanticException(expression_Binary.firstToken, "Error Encountered");
		}
		
		return expression_Binary;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		Expression e = (Expression) expression_Unary.e.visit(this, null);
		Type t = e.TYPE;
		expression_Unary.TYPE = null;
		
		if(expression_Unary.op == Kind.OP_EXCL && (t == Type.BOOLEAN || t == Type.INTEGER)){
			expression_Unary.TYPE = t;
		}
		else if((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS) && t == Type.INTEGER){
			expression_Unary.TYPE = Type.INTEGER;
		}
		else{
			throw new SemanticException(expression_Unary.firstToken, "Error Encountered");
		}
		
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		Expression e0 = (Expression) index.e0.visit(this, null);
		Expression e1 = (Expression) index.e1.visit(this, null);
		
		if(e0.TYPE != Type.INTEGER || e1.TYPE != Type.INTEGER){
			throw new SemanticException(index.firstToken, "Error Encountered");
		}
		
		if(e0.firstToken.kind == Kind.KW_r && e1.firstToken.kind == Kind.KW_a){
			index.isCartesian = false;
		}
		else{
			index.isCartesian = true;
		}
		
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		//expression_PixelSelector.index.visit(this, null);
		Index i = expression_PixelSelector.index;
		if(i != null) {
			i.visit(this, null);
		}
		Declaration dec = s.lookupType(expression_PixelSelector.name);
		if(dec == null){
			throw new SemanticException(expression_PixelSelector.firstToken, "Error Encountered");
		}
		expression_PixelSelector.TYPE = null;
		
		if(dec.TYPE == Type.IMAGE){
			expression_PixelSelector.TYPE = Type.INTEGER;
		}
		else if(expression_PixelSelector.index == null){
			expression_PixelSelector.TYPE = dec.TYPE;
		}
		else{
			throw new SemanticException(expression_PixelSelector.firstToken, "Error Encountered");
		}
		
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Expression ec = (Expression) expression_Conditional.condition.visit(this, null);
		Expression et = (Expression) expression_Conditional.trueExpression.visit(this, null);
		Expression ef = (Expression) expression_Conditional.falseExpression.visit(this, null);
		
		if((ec.TYPE != Type.BOOLEAN) || (et.TYPE != ef.TYPE)){
			throw new SemanticException(expression_Conditional.firstToken, "Error Encountered");
		}
		
		expression_Conditional.TYPE = et.TYPE;
		return expression_Conditional;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		
		declaration_Image.TYPE = Type.IMAGE;
		if(declaration_Image.xSize != null && declaration_Image.ySize != null){
			Expression e0 = (Expression) declaration_Image.xSize.visit(this, null);
			Expression e1 = (Expression) declaration_Image.ySize.visit(this, null);
			if(declaration_Image.xSize.TYPE != Type.INTEGER || declaration_Image.ySize.TYPE != Type.INTEGER){
				throw new SemanticException(declaration_Image.firstToken, "Error Encountered");
			}
		}
		
		Declaration d = s.lookupType(declaration_Image.name);
		if(d == null){
			s.insert(declaration_Image.name, declaration_Image);
		}
		else{
			throw new SemanticException(declaration_Image.firstToken, "Error Returned");
		}
		if(declaration_Image.source != null){
			declaration_Image.source.visit(this, null);
		}
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		
		try{
			java.net.URL a = new URL(source_StringLiteral.fileOrUrl);
			source_StringLiteral.TYPE = Type.URL;
		}
		catch(Exception e){
			source_StringLiteral.TYPE = Type.FILE;
		}
		
		return source_StringLiteral;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		Expression e = (Expression) source_CommandLineParam.paramNum.visit(this, null);
		source_CommandLineParam.TYPE = null;
		
		if(e.TYPE != Type.INTEGER){
			throw new SemanticException(source_CommandLineParam.firstToken, "Error Encountered");
		}
		
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		Declaration dec = s.lookupType(source_Ident.name);
		if(dec == null){
			throw new SemanticException(source_Ident.firstToken, "Error Encountered");
		}
		//System.out.println("hello");
		source_Ident.TYPE = dec.TYPE;
		
		if(source_Ident.TYPE != Type.FILE && source_Ident.TYPE != Type.URL){
			throw new SemanticException(source_Ident.firstToken, "Error Encountered");
		}
		
		return source_Ident;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		
//		Type a = TypeUtils.getType(declaration_SourceSink.type);
		Type a = TypeUtils.getType(declaration_SourceSink.firstToken);
		declaration_SourceSink.TYPE = a;
		Source s1 = (Source) declaration_SourceSink.source.visit(this, null);
		
		if(s1.TYPE != declaration_SourceSink.TYPE && declaration_SourceSink.source.TYPE != null){
			throw new SemanticException(declaration_SourceSink.firstToken, "Error Encountered");
		}
		
		Declaration d = s.lookupType(declaration_SourceSink.name);
		if(d == null){
			s.insert(declaration_SourceSink.name, declaration_SourceSink);
		}
		else{
			throw new SemanticException(declaration_SourceSink.firstToken, "Error Encountered");
		}
		
		
		return declaration_SourceSink;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		expression_IntLit.TYPE = Type.INTEGER;
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		Expression e0 = (Expression) expression_FunctionAppWithExprArg.arg.visit(this, null);
		if(e0.TYPE != Type.INTEGER){
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Error Encountered");
		}
		
		expression_FunctionAppWithExprArg.TYPE = Type.INTEGER;
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		Index i = expression_FunctionAppWithIndexArg.arg;
		if(i != null) {
			i.visit(this, null);
		}
		expression_FunctionAppWithIndexArg.TYPE = Type.INTEGER;
		return expression_FunctionAppWithIndexArg;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.TYPE = Type.INTEGER;
		return expression_PredefinedName;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		Declaration dec = s.lookupType(statement_Out.name);
		statement_Out.setDec(dec);
		if(dec == null){
			throw new SemanticException(statement_Out.firstToken, "Error Encountered");
		}
		Sink s = (Sink) statement_Out.sink.visit(this, null);
		if(((dec.TYPE != Type.INTEGER && dec.TYPE != Type.BOOLEAN) || s.TYPE != Type.SCREEN) && ((dec.TYPE != Type.IMAGE) || (s.TYPE != Type.FILE && s.TYPE != Type.SCREEN))){
			throw new SemanticException(statement_Out.firstToken, "Error Encountered");
		}
		
		return statement_Out;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		Declaration dec = s.lookupType(statement_In.name);
		if(dec == null){
			throw new SemanticException(statement_In.firstToken, "Error Encountered");
		}
		
		//Type a = TypeUtils.getType(statement_In.name);
		Source s = (Source) statement_In.source.visit(this, null);
		statement_In.setDec(dec);
		statement_In.d = dec;
		/*if(dec == null || dec.TYPE != s.TYPE){
			throw new SemanticException(statement_In.firstToken, "Error Encountered");
		}*/
		return statement_In;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		LHS l = (LHS) statement_Assign.lhs.visit(this, null);
		Expression e = (Expression) statement_Assign.e.visit(this, null);
		if(l.TYPE != e.TYPE && l.TYPE != Type.IMAGE){
			throw new SemanticException(statement_Assign.firstToken, "Error Encountered");
		}
		
		statement_Assign.isCartesian = l.isCartesian;
		return statement_Assign;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		Declaration d = s.lookupType(lhs.name);
		if(d == null){
			throw new SemanticException(lhs.firstToken, "Error Encountered");
		}
		lhs.dec = d;
		lhs.TYPE = d.TYPE;
		if(lhs.index != null){
			Index i = (Index) lhs.index.visit(this, null);
			lhs.isCartesian = i.isCartesian;
		}
		
		return lhs;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		sink_SCREEN.TYPE = Type.SCREEN;
		return sink_SCREEN;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		Declaration dec = s.lookupType(sink_Ident.name);
		if(dec == null){
			throw new SemanticException(sink_Ident.firstToken, "Error Encountered");
		}
		sink_Ident.TYPE = dec.TYPE;
		if(sink_Ident.TYPE != Type.FILE){
			throw new SemanticException(sink_Ident.firstToken, "Error Encountered");
		}
		
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.TYPE = Type.BOOLEAN;
		
		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		Declaration dec = s.lookupType(expression_Ident.name);
		if(dec == null){
			throw new SemanticException(expression_Ident.firstToken, "Error Encountered");
		}
		expression_Ident.TYPE = dec.TYPE;
		
		return expression_Ident;
	}

}
