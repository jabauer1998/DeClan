package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.source.ReaderSource;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyOptimizer;

public class MyOptimizerTest {
    private void comparePrograms(Prog optimized, String expected){
        String optimizedString = optimized.toString();
        assertTrue("The optimized program equals \n\n" + optimizedString + "\n\n and the expected equals \n\n" + expected, optimizedString.equals(expected));
    }

    @Test
    public void testSimpleCommonSubExpressionElimination(){
        String inputSource = "SYMBOL SECTION\n"
                           + "DATA SECTION\n"
                           + "DEF a := 1 <INT>\n"
                           + "DEF b := 2 <INT>\n" 
                           + "DEF GLOBAL i := a IADD b <INT>\n"
                           + "DEF GLOBAL z := a IADD b <INT>\n"
                           + "DEF GLOBAL f := (GLOBAL z) IADD (GLOBAL i) <INT>\n"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n"
                            + "DATA SECTION\r\n"
                            + " DEF a := 1 <INT>\r\n"
                            + " DEF b := 2 <INT>\r\n"
                            + " DEF GLOBAL i := a IADD b <INT>\r\n"
                            + " DEF GLOBAL f := (GLOBAL i) IADD (GLOBAL i) <INT>\r\n"
                            + "BSS SECTION\r\n"
                            + "CODE SECTION\r\n"
                            + "END\r\n"
                            + "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(null, prog);
        optimizer.performCommonSubExpressionElimination();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }

    @Test
    public void testComplexCommonSubExpressionElimination(){
        String inputSource = "SYMBOL SECTION\n"
                           + "DATA SECTION\n"
                           + " DEF GLOBAL t := TRUE <BOOL>\n"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + " LABEL block1\n"
                           + " DEF a := 1 <INT>\n"
                           + " DEF b := 2 <INT>\n" 
                           + " DEF i := a IADD b <INT>\n"
                           + " DEF z := a IADD b <INT>\n"
                           + " DEF f := z IADD i <INT>\n"
                           + " LABEL block2\n"
                           + " IF z IEQ t "
                           + " THEN block1 "
                           + " ELSE block2\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n" + //
                              "DATA SECTION\r\n" + //
                              " DEF GLOBAL t := TRUE <BOOL>\r\n" + //
	                          "BSS SECTION\r\n" + //
	                          "CODE SECTION\r\n" + //
	                          " LABEL block1\r\n" + //
	                          " DEF a := 1 <INT>\r\n" + //
	                          " DEF b := 2 <INT>\r\n" + //
	                          " DEF i := a IADD b <INT>\r\n" + //
	                          " DEF z := i <INT>\r\n" + //
	                          " DEF f := i IADD i <INT>\r\n" + //
	                          " LABEL block2\r\n" + //
	                          " IF z IEQ t\r\n" + //
	                          " THEN block1\r\n" + //
	                          " ELSE block2\r\n" + //
	                          "END\r\n" + //
	                          "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(null, prog);
        optimizer.performCommonSubExpressionElimination();

        Prog optimizedProg = optimizer.getICode();
        comparePrograms(optimizedProg, targetSource);
    }
    
    
    
    @Test 
    public void testDeadCodeElimination(){
        String inputSource = "SYMBOL SECTION\n"
                           + "DATA SECTION\n"
                           + " DEF GLOBAL g := TRUE <BOOL>\n"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + "LABEL block1\n"
                           + "DEF a := 1 <INT>\n"
                           + "DEF b := 60 <INT>\n" 
                           + "DEF i := a IADD a <INT>\n"
                           + "DEF h := i IADD a <INT>\n"
                           + "CALL func ([i -> x]<INT>)\n"
                           + "DEF f := (RETURN z) <INT>\n"
                           + "IF f IEQ (GLOBAL g) THEN block1 ELSE block1\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource =   "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " DEF GLOBAL g := TRUE <BOOL>\r\n" + //
                                "BSS SECTION\r\n" + //
                                "CODE SECTION\r\n" + //
                                " LABEL block1\r\n" + //
                                " DEF a := 1 <INT>\r\n" + //
                                " DEF i := a IADD a <INT>\r\n" + //
                                " CALL func([i -> x]<INT>)\r\n" + //
                                " DEF f := (RETURN z) <INT>\r\n" + //
                                " IF f IEQ (GLOBAL g)\r\n" + //
                                " THEN block1\r\n" + //
                                " ELSE block1\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(null, prog);
        optimizer.performDeadCodeElimination();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }
    
    @Test 
    public void testDeadCodeElimination2(){
        String inputSource = "SYMBOL SECTION\n"
                           + "DATA SECTION\n"
                           + " DEF GLOBAL g := TRUE <BOOL>\n"
                           + " DEF GLOBAL b := 2 <INT>"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + "LABEL block1\n"
                           + "DEF a := 1 <INT>\n"
                           + "GLOBAL b := 60 <INT>\n" 
                           + "DEF i := (GLOBAL b) IADD a <INT>\n"
                           + "DEF h := i IADD a <INT>\n"
                           + "CALL func ([i -> x]<INT>)\n"
                           + "DEF f := (RETURN z) <INT>\n"
                           + "IF f IEQ (GLOBAL g) THEN block1 ELSE block1\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource =   "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " DEF GLOBAL g := TRUE <BOOL>\r\n" + //
                                "BSS SECTION\r\n" + //
                                "CODE SECTION\r\n" + //
                                " LABEL block1\r\n" + //
                                " DEF a := 1 <INT>\r\n" + //
                                " DEF GLOBAL b := 60 <INT>\r\n" +
                                " DEF i := (GLOBAL b) IADD a <INT>\r\n" + //
                                " CALL func([i -> x]<INT>)\r\n" + //
                                " DEF f := (RETURN z) <INT>\r\n" + //
                                " IF f IEQ (GLOBAL g)\r\n" + //
                                " THEN block1\r\n" + //
                                " ELSE block1\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(null, prog);
        optimizer.performDeadCodeElimination();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }
    
    @Test
    public void testConstantPropogation(){
        String inputSource = "SYMBOL SECTION\n"
                           + "DATA SECTION\n" 
                           + " DEF GLOBAL a := 1 <INT>\n"
                           + " DEF GLOBAL b := 2 <INT>\n"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + " DEF i := a IADD b <INT>\n"
                           + " DEF z := a IADD i <INT>\n"
                           + " DEF f := z IADD i <INT>\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n"
                            + "DATA SECTION\r\n"
                            + " DEF GLOBAL a := 1 <INT>\r\n"
                            + " DEF GLOBAL b := 2 <INT>\r\n"
                            + "BSS SECTION\r\n"
                            + "CODE SECTION\r\n"
                            + " DEF i := 3 <INT>\r\n"
                            + " DEF z := 4 <INT>\r\n"
                            + " DEF f := 7 <INT>\r\n"
                            + "END\r\n"
                            + "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        
        MyOptimizer optimizer = new MyOptimizer(null, prog);
        optimizer.performConstantPropogation();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }
    
    @Test
    public void testPartialRedundancyElimination() {
    	String inputSource = "SYMBOL SECTION\n"
			                + "DATA SECTION\n" 
			                + " DEF GLOBAL a := 1 <INT>\n"
			                + " DEF GLOBAL b := 2 <INT>\n"
			                + "BSS SECTION\n"
			                + "CODE SECTION\n"
			                + " DEF i := a IADD b <INT>\n"
			                + " DEF z := a IADD i <INT>\n"
			                + " DEF f := z IADD i <INT>\n"
			                + " IF i LT z\n"
			                + " THEN label1\n"
			                + " ELSE label2\n"
			                + " LABEL label1\n"
			                + " DEF g := z IADD i <INT>\n"
			                + " DEF h := z IADD z <INT>\n"
			                + " GOTO label3\n"
			                + " LABEL label2\n"
			                + " DEF j := z IADD f <INT>\n"
			                + " DEF h := z IADD z <INT>\n"
			                + " GOTO label3\n"
			                + " LABEL label3\n"
			                + " DEF l := z IADD i <INT>\n"
			                + " CALL func([l -> t]<INT>)\n"
			                + "END\n"
			                + "PROC SECTION\n";
    	
    	String targetSource = "SYMBOL SECTION\r\n"
			                + "DATA SECTION\r\n" 
			                + " DEF GLOBAL a := 1 <INT>\r\n"
			                + " DEF GLOBAL b := 2 <INT>\r\n"
			                + "BSS SECTION\r\n"
			                + "CODE SECTION\r\n"
			                + " DEF i := a IADD b <INT>\r\n"
			                + " DEF z := a IADD i <INT>\r\n"
			                + " DEF d := z IADD i <INT>\r\n"
			                + " DEF f := d <INT>\r\n"
			                + " IF i LT z\r\n"
			                + " THEN label1\r\n"
			                + " ELSE label2\r\n"
			                + " LABEL label1\r\n"
			                + " DEF g := d <INT>\r\n"
			                + " DEF h := z IADD z <INT>\r\n"
			                + " GOTO label3\r\n"
			                + " LABEL label2\r\n"
			                + " DEF j := z IADD f <INT>\r\n"
			                + " DEF h := z IADD z <INT>\r\n"
			                + " GOTO label3\r\n"
			                + " LABEL label3\r\n"
			                + " DEF l := d <INT>\r\n"
			                + " CALL func([l -> t]<INT>)\r\n"
			                + "END\r\n"
			                + "PROC SECTION\r\n";

		ErrorLog errLog = new ErrorLog();
		ReaderSource source = new ReaderSource(new StringReader(inputSource));
		MyIrLexer lexer = new MyIrLexer(source, errLog);
		MyIrParser parser = new MyIrParser(lexer, errLog);
		Prog prog = parser.parseProgram();
		
		MyOptimizer optimizer = new MyOptimizer(null, prog);
		optimizer.performPartialRedundancyElimination();
		//By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
		//It is called within the Optimizers constructor
		Prog optimizedProg = optimizer.getICode();
		
		comparePrograms(optimizedProg, targetSource);
    }
}
