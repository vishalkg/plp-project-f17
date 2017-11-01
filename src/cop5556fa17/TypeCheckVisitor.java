package cop5556fa17;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;

import java.net.URL;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
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
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
			this.t = t;
		}

	}		
		
	SymbolTable symbolTable = new SymbolTable();
	
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
		if (symbolTable.insert(declaration_Variable.name,declaration_Variable)) {
			Type type = TypeUtils.getType(declaration_Variable.firstToken);
			if (declaration_Variable.e != null) {
				if (TypeUtils.getType(declaration_Variable.firstToken)!=declaration_Variable.e.visit(this, arg))
					throw new SemanticException(declaration_Variable.e.firstToken, "Type mismatch.\n");
			}
			return type;
		}
		else
			throw new SemanticException(declaration_Variable.firstToken, "Symbol already present.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if (index.e0.visit(this, arg) == Type.INTEGER) {
			if (index.e1.visit(this, arg) == Type.INTEGER) {
				//return  !(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_A);
				index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_A));
				return Type.NONE;
			}
			else
				throw new SemanticException(index.e1.firstToken, "Return type not integer.\n");
		}
		else
			throw new SemanticException(index.e0.firstToken, "Return type not integer.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if (symbolTable.insert(declaration_Image.name, declaration_Image)) {
			if (declaration_Image.xSize != null || declaration_Image.ySize != null) {
				if (!(declaration_Image.xSize!=null && declaration_Image.ySize !=null &&
						(Type) declaration_Image.xSize.visit(this, arg)==Type.INTEGER && 
						(Type) declaration_Image.ySize.visit(this, arg)==Type.INTEGER))
					throw new SemanticException(declaration_Image.firstToken, "Retrun type of xSize OR ySize not integer.\n");
			}
			return Type.IMAGE;
		}
		else
			throw new SemanticException(declaration_Image.firstToken, "Symbol already present.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		Type type;
		try {
			URL url = new URL(source_StringLiteral.fileOrUrl);
			type = Type.URL;
		}
		catch (java.net.MalformedURLException e) {
			type = Type.FILE;
		}
		return type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Type type = (Type) source_CommandLineParam.visit(this, arg);
		if (type==Type.INTEGER)
			return type;
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Type type = (Type) symbolTable.lookupType(source_Ident.name).visit(this, arg);
		if (type == Type.URL || type == Type.FILE) {
			return type;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		if (symbolTable.insert(declaration_SourceSink.name,declaration_SourceSink)) {
			Type type = (Type) declaration_SourceSink.source.visit(this, arg);
			if (TypeUtils.getType(declaration_SourceSink.firstToken) != type)
				throw new SemanticException(declaration_SourceSink.source.firstToken, "Source rerurn type mismatch.\n");
			return type;
		}
		else
			throw new SemanticException(declaration_SourceSink.firstToken, "Symbol already present.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		return TypeUtils.getType(expression_IntLit.firstToken);
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		return Type.INTEGER;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		Type e_type = (Type) statement_Assign.e.visit(this, arg);
		Type lhs_type = TypeUtils.getType(statement_Assign.lhs.firstToken);
		if (lhs_type != e_type)
			throw new SemanticException(statement_Assign.e.firstToken, "Return type not consistent.\n");
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		return Type.NONE;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		return TypeUtils.getType(sink_SCREEN.firstToken);
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if (symbolTable.lookupType(sink_Ident.name).visit(this, arg) == Type.FILE)
			return Type.FILE;
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		return TypeUtils.getType(expression_BooleanLit.firstToken);
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		return (Type) symbolTable.lookupType(expression_Ident.name).visit(this, arg);
		//throw new UnsupportedOperationException();
	}

}
