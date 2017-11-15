package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
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
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

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
	FieldVisitor fv;

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
		CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

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
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg) 
			throws Exception {
		// TODO
		String fieldName = declaration_Variable.name;
		String fieldType = (declaration_Variable.firstToken.kind == Kind.KW_boolean) ? "Z" : "I";
		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();

		if (declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, fieldType);
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO
		Type expression_Binary_type = null;
		switch (expression_Binary.op) {
		
		case OP_EQ:
		case OP_NEQ:
		case OP_GT:
		case OP_GE:
		case OP_LT:
		case OP_LE:
		case OP_AND:
		case OP_OR: {
			expression_Binary_type = Type.BOOLEAN;
			Label startLabel = new Label();
			Label endLabel = new Label();
			expression_Binary.e0.visit(this, arg);
			expression_Binary.e1.visit(this, arg);
			switch (expression_Binary.op) {
			
			case OP_AND:
				mv.visitInsn(IAND);
				break;
				
			case OP_OR:
				mv.visitInsn(IOR);
				break;
				
			case OP_EQ:
			case OP_NEQ:
			case OP_GT:
			case OP_GE:
			case OP_LT:
			case OP_LE: {
				int op = 0;
				switch (expression_Binary.op) {
				case OP_EQ: op = IF_ICMPEQ;
					break;
				case OP_NEQ: op = IF_ICMPNE;
					break;
				case OP_GT: op = IF_ICMPGT;
					break;
				case OP_GE: op = IF_ICMPGE;
					break;
				case OP_LT : op = IF_ICMPLT;
					break;
				case OP_LE : op = IF_ICMPLE;
					break;
				default:
					break;
				}
				mv.visitJumpInsn(op, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel);
			}break;
				
			default:
				break;
				
			}
		}break;
		
		case OP_PLUS:
		case OP_MINUS:
		case OP_TIMES:
		case OP_DIV:
		case OP_MOD: {
			expression_Binary_type = Type.INTEGER;
			expression_Binary.e0.visit(this, arg);
			expression_Binary.e1.visit(this, arg);
			switch (expression_Binary.op) {
			case OP_PLUS: mv.visitInsn(IADD);
				break;
			case OP_MINUS: mv.visitInsn(ISUB);
				break;
			case OP_TIMES: mv.visitInsn(IMUL);
				break;
			case OP_DIV: mv.visitInsn(IDIV);
				break;
			case OP_MOD: mv.visitInsn(IREM);
			default:
				break;
			}
		}break;
		
		default:
			break;
			
		}
		
		if (expression_Binary_type == null)
			throw new UnsupportedOperationException();
		
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary_type);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
//		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO
		expression_Conditional.condition.visit(this, arg);
		Label t = new Label();
		Label f = new Label();
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(IF_ICMPEQ, t);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, f);
		mv.visitLabel(t);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitLabel(f);
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getType());
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.getType());
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO
		mv.visitLdcInsn(expression_IntLit.value);
		//throw new UnsupportedOperationException();
		CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		Type dec_type = TypeUtils.getType(statement_Out.getDec().firstToken);
		if (dec_type == Type.BOOLEAN || dec_type == Type.INTEGER) {
			CodeGenUtils.genLogTOS(GRADE, mv, dec_type);
			return null;
		}
		throw new UnsupportedOperationException();
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
		// TODO (see comment )
		throw new UnsupportedOperationException();
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statement_Assign.e.visit(this, arg);
		statement_Assign.lhs.visit(this, arg);
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		//throw new UnsupportedOperationException();
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		Type type = TypeUtils.getType(lhs.getDeclaration().firstToken);
		if (type == Type.INTEGER || type == Type.BOOLEAN) {
			mv.visitInsn(DUP);
			String intORbool = type==Type.INTEGER ? "I" : "Z";
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, intORbool);
			return null;
		}
		throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		mv.visitLdcInsn(expression_BooleanLit.value);
		//throw new UnsupportedOperationException();
		CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
//		return null;
	}


}
