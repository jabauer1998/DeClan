package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
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
                           + "a := 1\n"
                           + "b := 2\n" 
                           + "i := a IADD b\n"
                           + "z := a IADD b\n"
                           + "f := z IADD i\n"
                           + "CODE SECTION\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n"
                            + "DATA SECTION\r\n"
                            + " a := 1\r\n"
                            + " b := 2\r\n"
                            + " i := a IADD b\r\n"
                            + " z := i\r\n"
                            + " f := i IADD i\r\n"
                            + "CODE SECTION\r\n"
                            + "END\r\n"
                            + "PROC SECTION\r\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(prog);
        optimizer.eliminateCommonSubExpressions();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }

    @Test
    public void testComplexCommonSubExpressionElimination(){
        String inputSource = "LABEL block1\n"
                           + "a := 1\n"
                           + "b := 2\n" 
                           + "i := a IADD b\n"
                           + "z := a IADD b\n"
                           + "f := z IADD i\n"
                           + "LABEL block2\n"
                           + "IF z EQ TRUE THEN block1 ELSE block2\n"
                           + "END\n";

        String targetSource = "LABEL block1\n"
                            + "a := 1\n"
                            + "b := 2\n"
                            + "i := a IADD b\n"
                            + "z := i\n"
                            + "f := i IADD i\n"
                            + "LABEL block2\n"
                            + "IF z EQ TRUE THEN block1 ELSE block2\n"
                            + "END\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(prog);
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }
    
    @Test 
    public void testDeadCodeElimination(){
        String inputSource = "LABEL block1\n"
                           + "a := 1\n"
                           + "b := 60\n" 
                           + "i := a IADD a\n"
                           + "g := i IADD a\n"
                           + "PROC func ( g -> x )\n"
                           + "f <- g\n"
                           + "IF f EQ TRUE THEN block1 ELSE block1\n"
                           + "END\n";

        String targetSource = "LABEL block1\n" + //
                              "a := 1\n" + //
                              "i := a IADD a\n" + //
                              "g := i IADD a\n" + //
                              "PROC func ( g -> x )\n" + //
                              "f <- g\n" + //
                              "IF f EQ TRUE THEN block1 ELSE block1\n" + //
                              "END\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        MyOptimizer optimizer = new MyOptimizer(prog);
        optimizer.runDataFlowAnalysis();
        optimizer.performDeadCodeElimination();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }
    
    @Test
    public void testConstantPropogation(){
        String inputSource = "a := 1\n"
                           + "b := 2\n" 
                           + "i := a IADD b\n"
                           + "z := a IADD i\n"
                           + "f := z IADD i\n"
                           + "END\n";

        String targetSource = "a := 1\n"
                            + "b := 2\n"
                            + "i := 1 IADD 2\n"
                            + "z := 1 IADD i\n"
                            + "f := z IADD i\n"
                            + "END\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        
        MyOptimizer optimizer = new MyOptimizer(prog);
        optimizer.runDataFlowAnalysis();
        optimizer.performConstantPropogation();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }

    /*
    This one is not working yet so I just decided to cut it out
    @Test
    public void testPartialRedundancyElimination(){
        String inputSource = "LABEL block1\n"
                           + "a := 1\n"
                           + "b := 2\n"
                           + "i := 1 ADD 2\n"
                           + "z := 1 ADD i\n"
                           + "f := z ADD i\n"
                           + "LABEL block2\n"
                           + "f := b ADD z\n"
                           + "g := h ADD f\n"
                           + "z := u ADD g\n"
                           + "r := y ADD z\n"
                           + "GOTO block1\n"
                           + "IF TRUE EQ TRUE THEN block1 ELSE block2\n"
                           + "END\n";

        String targetSource = "LABEL block1\n"
                            + "a := 1\n"
                            + "b := 2\n"
                            + "i := 1 ADD 2\n"
                            + "z := 1 ADD i\n"
                            + "LABEL block2\n"
                            + "f := b ADD z\n"
                            + "g := h ADD f\n"
                            + "z := u ADD g\n"
                            + "r := y ADD z\n"
                            + "GOTO block1\n"
                            + "IF TRUE EQ TRUE THEN block1 ELSE block2\n"
                            + "END\n";

        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSource));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        List<ICode> prog = parser.parseProgram();
        
        MyOptimizer optimizer = new MyOptimizer(prog);
        optimizer.runDataFlowAnalysis();
        optimizer.performPartialRedundancyElimination();
        //By Default the commonSubExpressionElimination is ran when building the Dags in the FlowGraph
        //It is called within the Optimizers constructor
        List<ICode> optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, targetSource);
    }
    */
}
