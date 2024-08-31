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
                           + "GLOBAL a := 1 <INT>\n"
                           + "GLOBAL b := 2 <INT>\n" 
                           + "i := a IADD b <INT>\n"
                           + "z := a IADD b <INT>\n"
                           + "f := z IADD i <INT>\n"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n"
                            + "DATA SECTION\r\n"
                            + " GLOBAL a := 1 <INT>\r\n"
                            + " GLOBAL b := 2 <INT>\r\n"
                            + " i := a IADD b <INT>\r\n"
                            + " z := i <INT>\r\n"
                            + " f := i IADD i <INT>\r\n"
                            + "BSS SECTION\r\n"
                            + "CODE SECTION\r\n"
                            + "END\r\n"
                            + "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(prog);
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
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + " LABEL block1\n"
                           + " a := 1 <INT>\n"
                           + " b := 2 <INT>\n" 
                           + " i := a IADD b <INT>\n"
                           + " z := a IADD b <INT>\n"
                           + " f := z IADD i <INT>\n"
                           + " LABEL block2\n"
                           + " IF z EQ TRUE THEN block1 ELSE block2\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n" + //
                        "DATA SECTION\r\n" + //
                        "BSS SECTION\r\n" + //
                        "CODE SECTION\r\n" + //
                        " LABEL block1\r\n" + //
                        " a := 1 <INT>\r\n" + //
                        " b := 2 <INT>\r\n" + //
                        " i := a IADD b <INT>\r\n" + //
                        " z := i <INT>\r\n" + //
                        " f := i IADD i <INT>\r\n" + //
                        " LABEL block2\r\n" + //
                        " IF z EQ TRUE THEN block1 ELSE block2\r\n" + //
                        "END\r\n" + //
                        "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(prog);
        optimizer.performCommonSubExpressionElimination();

        Prog optimizedProg = optimizer.getICode();
        comparePrograms(optimizedProg, targetSource);
    }
    
    @Test 
    public void testDeadCodeElimination(){
        String inputSource = "SYMBOL SECTION\n"
                           + "DATA SECTION\n"
                           + "  DEF GLOBAL z := TRUE <BOOL>\n"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + "LABEL block1\n"
                           + "a := 1 <INT>\n"
                           + "b := 60 <INT>\n" 
                           + "i := a IADD a <INT>\n"
                           + "g := i IADD a <INT>\n"
                           + "CALL func ([i -> x]<INT>)\n"
                           + "f := (RETURN z) <INT>\n"
                           + "IF f EQ (GLOBAL z) THEN block1 ELSE block1\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource =   "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " DEF GLOBAL z := TRUE <BOOL>\r\n" + //
                                "BSS SECTION\r\n" + //
                                "CODE SECTION\r\n" + //
                                " LABEL block1\r\n" + //
                                " a := 1 <INT>\r\n" + //
                                " i := a IADD a <INT>\r\n" + //
                                " CALL func([i -> x]<INT>)\r\n" + //
                                " f := (RETURN z) <INT>\r\n" + //
                                " IF f EQ TRUE THEN block1 ELSE block1\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(prog);
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
                           + "GLOBAL a := 1 <INT>\n"
                           + "GLOBAL b := 2 <INT>\n"
                           + "BSS SECTION\n"
                           + "CODE SECTION\n"
                           + "i := a IADD b <INT>\n"
                           + "z := a IADD i <INT>\n"
                           + "f := z IADD i <INT>\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n"
                            + "DATA SECTION\r\n"
                            + " GLOBAL a := 1 <INT>\r\n"
                            + " GLOBAL b := 2 <INT>\r\n"
                            + "BSS SECTION\r\n"
                            + "CODE SECTION\r\n"
                            + " i := 1 IADD 2 <INT>\r\n"
                            + " z := 1 IADD i <INT>\r\n"
                            + " f := z IADD i <INT>\r\n"
                            + "END\r\n"
                            + "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        
        MyOptimizer optimizer = new MyOptimizer(prog);
        optimizer.performConstantPropogation();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }
}
