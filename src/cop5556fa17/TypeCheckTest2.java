package cop5556fa17;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;


public class TypeCheckTest2 {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	/**
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			throw e;
		}
	}

	@Test
	public void test1() throws Exception {
		String input = "prog int k = 42; int k=12;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test2() throws Exception {
		String input = "prog int k = 42;\n boolean k=true;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test3() throws Exception {
		String input = "prog file k = 42;\n boolean k=true;";
		thrown.expect(SyntaxException.class);
		typeCheck(input);
	}

	@Test
	public void test4() throws Exception {
		String input = "prog file k = 42;\n boolean k=true;";
		thrown.expect(SyntaxException.class);
		typeCheck(input);
	}

	@Test
	public void test5() throws Exception {
		String input = "prog image[filepng,png] imageName <- imagepng; \n boolean ab=true;"; 
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test6() throws Exception {
		String input = "prog image[filepng,png] imageName; \n boolean ab=true;"; 
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test7() throws Exception {
		String input = "prog int abcd=(true&true);"; 
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test8() throws Exception {
		String input = "prog boolean abcd=(true&true|false&1);"; 
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test9() throws Exception {
		String input = "prog image x;"; 
		thrown.expect(SyntaxException.class);
		typeCheck(input);
	}

	@Test
	public void test10() throws Exception {
		String input = "prog image [10,11] abcd <- \"\";"; 
		//thrown.expect(SyntaxException.class);
		typeCheck(input);
	}

	@Test
	public void test11() throws Exception {
		String input = "prog image [10,11] abcd <- @(1234+234);"; 
		typeCheck(input);
	}

	@Test
	public void test12() throws Exception {
		String input = "prog int x_y=12; image [10,11] abcd <- x_y;"; 
		typeCheck(input);
	}

	// SourceSinkDeclaration
	@Test
	public void test13() throws Exception {
		String input = "prog url imageurl=\"https://www.google.com\";"; 
		typeCheck(input);
	}

	@Test
	public void test14() throws Exception {
		String input = "prog url imageurl1=\"https://www.google.com\"; url imageurl=imageurl1;"; 
		typeCheck(input);
	}

	@Test
	public void test15() throws Exception {
		String input = "prog url imageurl=@(1234+3454);";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	//Statements for Image Out and Image in
	@Test
	public void test16() throws Exception {
		String input = "prog image imageName;image imageName1 <- \"https://www.google.com\";"+ 
				"imageName -> SCREEN;";
		//thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test17() throws Exception {
		String input = "prog file fileName=\"filepng\"; image image1<- fileName;";
		typeCheck(input);
	}

	//LHS Assignment Statement
	@Test
	public void test18() throws Exception {
		String input = "prog image imageName; int array; array[[x,y]]=imageName[5,6];";
		typeCheck(input);
	}

	@Test
	public void test19() throws Exception {
		String input = "prog image imageName;array[[x,y]]=imageName[5,6];";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test20() throws Exception {
		String input = "prog image imageName;array[[x,y]]=imageName[5,6];";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test21() throws Exception {
		String input = "prog image imageName;int array;array[[r,A]]=imageName[5,6];";
		typeCheck(input);
	}

	@Test
	public void test22() throws Exception {
		String input = "prog int value1=10; int value2 =20; int sinValue=abs(sin(value1)); int cosValue=abs(cos(value2));";
		typeCheck(input);
	}

	@Test
	public void test23() throws Exception {
		String input = "prog int value1=10; int value2 =20; "
				+ "int sinValue; int cosValue; sinValue=abs(sin(atan(cos(value1))));"
				+ " cosValue=cart_x[value1*10,value2*20];";
		typeCheck(input);
	}

	@Test
	public void test24() throws Exception {
		String input = "prog int x_value=10; int y_value =20; int sinValue; int cosValue; "
				+ " sinValue=polar_a[x_value*1/2+12+13,y_value*1/3*1/2*3/4%2];"
				+ " cosValue=cart_y[x_value/2*2+2,(x_value>y_value)?sinValue/234:sinValue*10-23];"
				+ " int cotValue=polar_r[sinValue/cosValue,sinValue*100/400+cosValue];";
		typeCheck(input);
	}

	@Test
	public void test25() throws Exception {
		String input = "";
		thrown.expect(SyntaxException.class);
		typeCheck(input);
	}

	@Test
	public void test26() throws Exception {
		String input = "prog boolean k = false;\n k=k;";
		typeCheck(input);
	}

	@Test
	public void test27() throws Exception {
		String input = "prog int k=(5*5+12+3-5+4+false);";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void test28() throws Exception {
		String input = "prog int k=((5+6/0+1/2+2%3));";
		typeCheck(input);
	}
	@Test
	public void testUndec5() throws Exception {
		String input = "prog image image1; image image2; image1 <- image2;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}
	@Test
	public void testUndec6() throws Exception {
		String input = "prog  image image1; image image2; image1 <- @1;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}
	@Test
	public void testUndec7() throws Exception {
		String input = "prog file imagefile  = \"imageFile2017.\"; image image1;  image1 <- imagefile;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

}