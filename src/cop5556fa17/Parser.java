package cop5556fa17;



import static cop5556fa17.Scanner.Kind.EOF;
import static cop5556fa17.Scanner.Kind.IDENTIFIER;
import static cop5556fa17.Scanner.Kind.INTEGER_LITERAL;
import static cop5556fa17.Scanner.Kind.KW_SCREEN;
import static cop5556fa17.Scanner.Kind.KW_Y;
import static cop5556fa17.Scanner.Kind.KW_a;
import static cop5556fa17.Scanner.Kind.KW_boolean;
import static cop5556fa17.Scanner.Kind.KW_file;
import static cop5556fa17.Scanner.Kind.KW_image;
import static cop5556fa17.Scanner.Kind.KW_int;
import static cop5556fa17.Scanner.Kind.KW_r;
import static cop5556fa17.Scanner.Kind.KW_url;
import static cop5556fa17.Scanner.Kind.KW_x;
import static cop5556fa17.Scanner.Kind.KW_y;
import static cop5556fa17.Scanner.Kind.LSQUARE;
import static cop5556fa17.Scanner.Kind.OP_ASSIGN;
import static cop5556fa17.Scanner.Kind.OP_COLON;
import static cop5556fa17.Scanner.Kind.OP_DIV;
import static cop5556fa17.Scanner.Kind.OP_EQ;
import static cop5556fa17.Scanner.Kind.OP_EXCL;
import static cop5556fa17.Scanner.Kind.OP_GE;
import static cop5556fa17.Scanner.Kind.OP_GT;
import static cop5556fa17.Scanner.Kind.OP_LARROW;
import static cop5556fa17.Scanner.Kind.OP_LE;
import static cop5556fa17.Scanner.Kind.OP_LT;
import static cop5556fa17.Scanner.Kind.OP_MINUS;
import static cop5556fa17.Scanner.Kind.OP_MOD;
import static cop5556fa17.Scanner.Kind.OP_NEQ;
import static cop5556fa17.Scanner.Kind.OP_OR;
import static cop5556fa17.Scanner.Kind.OP_PLUS;
import static cop5556fa17.Scanner.Kind.OP_Q;
import static cop5556fa17.Scanner.Kind.OP_RARROW;
import static cop5556fa17.Scanner.Kind.OP_TIMES;
import static cop5556fa17.Scanner.Kind.RSQUARE;
import static cop5556fa17.Scanner.Kind.SEMI;
import static cop5556fa17.Scanner.Kind.STRING_LITERAL;

import java.util.ArrayList;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.*;
import cop5556fa17.Parser.SyntaxException;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		ArrayList<ASTNode> a = new ArrayList<>();
		Token firstToken = t;
		Program p = null;
		if(t.kind == IDENTIFIER){
			consume();
		}
		else{
			throw new SyntaxException(t, "All legal programs must atleast have an identifier");
		}
		
		//p = new Program(firstToken, firstToken, a);
		int i = 0;
		while(t.kind != EOF){
			if(t.kind == KW_int ||t.kind == KW_boolean ||t.kind == KW_image || t.kind == KW_url ||t.kind == KW_file){
				i = 1;
			}
			else if(t.kind == IDENTIFIER){
				i = 2;
			}
			else{
				throw new SyntaxException(t, "Invalid String");
			}
			switch(i){
			
			case 1:
				Declaration d = null;
				d = declaration();
				a.add(d);
				if(t.kind == SEMI){
					consume();
				}
				else{
					throw new SyntaxException(t, "Semicolon missing");
				}
				break;
				
			case 2:
				Statement s = null;
				s = statement();
				a.add(s);
				if(t.kind == SEMI){
					consume();
				}
				else{
					throw new SyntaxException(t, "Semicolon missing");
				}
			}	
		}
		
		p = new Program(firstToken, firstToken, a);
		return p;
	}


	Declaration declaration() throws SyntaxException {
		int j = 0;
		Declaration d = null;
		if(t.kind == KW_int || t.kind == KW_boolean){
			j = 1;
		}
		if(t.kind == KW_image){
			j = 2;
		}
		if(t.kind == KW_url || t.kind == KW_file){
			j = 3;
		}
		
		switch(j){
		case 1:
			d = variableDeclaration();
			break;
			
		case 2:
			d = imageDeclaration();
			break;
			
		case 3:
			d = sourceSinkDeclaration();
			break;
		}
				
		return d;
	}
	
	Declaration variableDeclaration() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Token ident = null;
		varType();
		if(t.kind == Kind.IDENTIFIER){
			ident = t;
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == Kind.OP_ASSIGN){
			consume();
			e = expression();
		}
		
		Declaration d = new Declaration_Variable(firstToken, firstToken, ident, e);
		return d;
	}

	void varType() throws SyntaxException {
		if(t.kind == KW_int || t.kind == KW_boolean){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		return;
	}

	Declaration imageDeclaration() throws SyntaxException {
		Token firstToken = t;
		Expression e1 = null;
		Expression e2 = null;
		Source s = null;
		Token name = null;
		if(t.kind == Kind.KW_image){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == LSQUARE){
			consume();
			e1 = expression();
			if(t.kind == Kind.COMMA){
				consume();
			}
			else{
				throw new SyntaxException(t, "Invalid String");
			}
			e2 = expression();
			if(t.kind == Kind.RSQUARE){
				consume();
			}
			else{
				throw new SyntaxException(t, "Invalid String");
			}
		}
		
		if(t.kind == Kind.IDENTIFIER){
			name = t;
			consume();
			if(t.kind == OP_LARROW){
				consume();
				s = source();
			}
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		Declaration d = new Declaration_Image(firstToken, e1, e2, name, s);
		return d;
	}

	Declaration sourceSinkDeclaration() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Source s = null;
		Token ident = null;
		sourceSinkType();
		if(t.kind == IDENTIFIER){
			ident = t;
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == Kind.OP_ASSIGN){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		s = source();
		Declaration d = new Declaration_SourceSink(firstToken, firstToken, ident, s);
		return d;
	}

	void sourceSinkType() throws SyntaxException {
		if(t.kind == Kind.KW_url || t.kind == KW_file){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		return;
	}

	Source source() throws SyntaxException {
		Token firstToken = t;
		Source s = null;
		if(t.kind == STRING_LITERAL){
			s = new Source_StringLiteral(firstToken, firstToken.getText());
			consume();
		}
		else if(t.kind == Kind.OP_AT){
			consume();
			Expression e = expression();
			s = new Source_CommandLineParam(firstToken, e);
		}
		else if(t.kind == Kind.IDENTIFIER){
			s = new Source_Ident(firstToken, firstToken);
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		return s;
	}

	Statement statement() throws SyntaxException {
		LHS l = null;
		Statement s = null;
		Token firstToken = t;
		if(t.kind == IDENTIFIER){
			Index i = null;
			l = new LHS(firstToken, firstToken, i);
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == LSQUARE || t.kind == OP_ASSIGN || t.kind == Kind.OP_LARROW || t.kind == OP_RARROW){
			switch(t.kind){
			case LSQUARE:
			{
				consume();
				Index i = lhsSelector();
				if(t.kind == RSQUARE){
					consume();
					l = new LHS(firstToken, firstToken, i);
				}
				else{
					throw new SyntaxException(t, "Invalid String");
				}
				if(t.kind == OP_ASSIGN){
					consume();
				}
				else{
					throw new SyntaxException(t, "Invalid String"); 
				}
				Expression e = expression();
				s = new Statement_Assign(firstToken, l, e);
			}
			break;
			
			case OP_ASSIGN:
			{
				consume();
				Expression e = expression();
				s = new Statement_Assign(firstToken, l, e); 
			}
			break;
			
			case OP_RARROW:
			{
				consume();
				Sink si = sink();
				s = new Statement_Out(firstToken, firstToken, si);
			}
			break;
			
			case OP_LARROW:
			{
				consume();
				Source s1 = source();
				s = new Statement_In(firstToken, firstToken, s1); 
			}
			
			}
			
		}
		
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		return s;
	}

	Sink sink() throws SyntaxException {
		Token firstToken = t;
		Sink s = null;
		if(t.kind == IDENTIFIER){
			s = new Sink_Ident(firstToken, firstToken);
			consume();
		}
		else if(t.kind == KW_SCREEN){
			s = new Sink_SCREEN(firstToken);
			consume();
		}
		
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		return s;
	}

	Index lhsSelector() throws SyntaxException {
		Index i = null;
		if(t.kind == Kind.LSQUARE){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == KW_x || t.kind == KW_r){
			switch(t.kind){
			case KW_x:
			{
				i = xySelector();
			}
			break;
			
			case KW_r:
			{
				i = raSelector();
			}
			}
		}
		
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == RSQUARE){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		return i;
	}

	Index raSelector() throws SyntaxException {
		Token firstToken = t;
		Expression e1 = null;
		Expression e0 = new Expression_PredefinedName(firstToken, firstToken.kind);
		consume();
		if(t.kind == Kind.COMMA){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == Kind.KW_a){
			Token firstToken1 = t;
			e1 = new Expression_PredefinedName(firstToken1, firstToken1.kind);
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		Index i = new Index(firstToken, e0, e1);
		return i;
	}

	Index xySelector() throws SyntaxException {
		Token firstToken = t;
		Expression e1 = null;
		Expression e0 = new Expression_PredefinedName(firstToken, firstToken.kind);
		consume();
		if(t.kind == Kind.COMMA){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == KW_y){
			Token firstToken1 = t;
			e1 = new Expression_PredefinedName(firstToken1, firstToken1.kind);
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		Index i = new Index(firstToken, e0, e1);
		
		return i;
	}

	void consume() {
		t = scanner.nextToken();
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	public Expression expression() throws SyntaxException {
		Expression e = null;
		Expression e0 = null;
		Expression e1 = null;
		Expression e2 = null;
		e0 = orExpression();
		
		Token firstToken = t;
		if(t.kind == OP_Q){
			consume();
			e1 = expression();
			if(t.kind == OP_COLON){
				consume();
			}
			e2 = expression();
			e0 = new Expression_Conditional(firstToken, e0, e1, e2);
		}
		
		return e0;
	}

	Expression orExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = andExpression();
		
		while(t.kind == OP_OR){
			Token op = t;
			consume();
			e1 = andExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression andExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = eqExpression();
		
		while(t.kind == Kind.OP_AND){
			Token op = t;
			consume();
			e1 = eqExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		
		return e0;
	}

	Expression eqExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = relExpression();
		
		while(t.kind == OP_EQ || t.kind == OP_NEQ){
			Token op = t;
			consume();
			e1 = relExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
	
		return e0;
	}

	Expression relExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = addExpression();
		
		while(t.kind == OP_LT || t.kind == OP_GT || t.kind == OP_LE || t.kind == OP_GE){
			Token op = t;
			consume();
			e1 = addExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}

		return e0;
	}

	Expression addExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = multExpression();
		
		while(t.kind == OP_PLUS || t.kind == OP_MINUS){
			Token op = t;
			consume();
			e1 = multExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression multExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = unaryExpression();
		
		while(t.kind == OP_TIMES || t.kind == OP_DIV || t.kind == OP_MOD){
			Token op = t;
			consume();
			e1 = unaryExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression unaryExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		if(t.kind == OP_PLUS){
			Token op = t;
			consume();
			Expression e1 = unaryExpression();
			e = new Expression_Unary(firstToken, op, e1);
		}
		
		else if(t.kind == OP_MINUS){
			Token op = t;
			consume();
			Expression e1 = unaryExpression();
			e = new Expression_Unary(firstToken, op, e1);
		}
		
		else if(t.kind == OP_EXCL || t.kind == INTEGER_LITERAL || t.kind == Kind.LPAREN ||                               
				t.kind == IDENTIFIER || t.kind == KW_x || t.kind == KW_y || t.kind == KW_r || 
				t.kind == KW_a || t.kind == Kind.KW_X || t.kind == KW_Y || t.kind == Kind.KW_Z || 
				t.kind == Kind.KW_A || t.kind == Kind.KW_R || t.kind == Kind.KW_DEF_X || t.kind == Kind.KW_DEF_Y || 
				t.kind == Kind.KW_sin || t.kind == Kind.KW_cos || t.kind == Kind.KW_atan ||
				t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y ||
				t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r || t.kind == Kind.BOOLEAN_LITERAL){
			e = unaryExpressionNotPlusMinus();
		}
		
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		return e;
	}

	Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		if(t.kind == OP_EXCL){
			Token op = t;
			consume();
			Expression e1 = unaryExpression();
			e = new Expression_Unary(firstToken, op, e1);
		}
		
		else if(t.kind == INTEGER_LITERAL || t.kind == Kind.LPAREN || t.kind == Kind.KW_sin || t.kind == Kind.KW_cos ||
				 t.kind == Kind.KW_atan || t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y ||
				 t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r || t.kind == Kind.BOOLEAN_LITERAL){
			e = primary();
		}
		
		else if(t.kind == IDENTIFIER){
			e = identOrPixelSelectorExpression();
		}
		
		else if(t.kind == KW_x || t.kind == KW_y || t.kind == KW_r || t.kind == KW_a || 
				t.kind == Kind.KW_X || t.kind == KW_Y || t.kind == Kind.KW_Z || t.kind == Kind.KW_A 
				|| t.kind == Kind.KW_R || t.kind == Kind.KW_DEF_X || t.kind == Kind.KW_DEF_Y){
			e = new Expression_PredefinedName(firstToken, firstToken.kind);
			consume();
		}
		
		return e;
	}

	Expression identOrPixelSelectorExpression() throws SyntaxException {
		Token firstToken = t;
		Index i = null;
		Expression e = null;
		if(t.kind == IDENTIFIER){
			e = new Expression_Ident(firstToken, firstToken);
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == Kind.LSQUARE){
			consume();
			i = selector();
			if(t.kind == RSQUARE){
				consume();
			}
			else{
				throw new SyntaxException(t, "Invalid String");
			}
			e = new Expression_PixelSelector(firstToken, firstToken, i);
		}
		
		return e;
	}

	Index selector() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = expression();
		if(t.kind == Kind.COMMA){
			consume();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		Expression e1 = expression();
		Index i = new Index(firstToken, e0, e1);
		
		return i;
	}

	Expression primary() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		if(t.kind == INTEGER_LITERAL || t.kind == Kind.LPAREN || t.kind == Kind.KW_sin || t.kind == Kind.KW_cos ||
		   t.kind == Kind.KW_atan || t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y ||
		   t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r || t.kind == Kind.BOOLEAN_LITERAL){
			switch(t.kind){
			case INTEGER_LITERAL:
			{
				e = new Expression_IntLit(firstToken, firstToken.intVal());
				consume();
			}
			break;
			
			case BOOLEAN_LITERAL:
			{
				String str = firstToken.getText();
				boolean bvar = Boolean.parseBoolean(str);
				e = new Expression_BooleanLit(firstToken, bvar);
				consume();
			}
			break;
			
			case LPAREN:
			{
				consume();
				e = expression();
				if(t.kind == Kind.RPAREN){
					consume();
				}
				else{
					throw new SyntaxException(t, "Invalid String");
				}
			}
			break;
			
			default:
				e = functionApplication();
			}
		}
		
		return e;
	}

	Expression functionApplication() throws SyntaxException {
		Token firstToken = null;;
		Expression e = null;
		if(t.kind == Kind.KW_sin || t.kind == Kind.KW_cos || t.kind == Kind.KW_atan || 
		   t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y ||
		   t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r){
			firstToken = t;
			functionName();
		}
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		if(t.kind == Kind.LPAREN || t.kind == LSQUARE){
			switch(t.kind){
			case LPAREN:
			{
				consume();
				Expression e0 = expression();
				if(t.kind == Kind.RPAREN){
					consume();
				}
				else{
					throw new SyntaxException(t, "Invalid String");
				}
				e = new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind, e0);
			}
			break;
			
			case LSQUARE:
			{
				consume();
				Index i = selector();
				if(t.kind == RSQUARE){
					consume();
				}
				else{
					throw new SyntaxException(t, "Invalid String");
				}
				e = new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, i);
			}
			}
		}
		
		else{
			throw new SyntaxException(t, "Invalid String");
		}
		
		return e;
	}

	void functionName() {
		consume();
		return;
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
