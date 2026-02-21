package declan;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import declan.utils.ErrorLog;
import declan.middleware.icode.ICode;
import declan.middleware.icode.Prog;
import declan.utils.source.ReaderSource;
import declan.frontend.MyIrLexer;
import declan.frontend.MyIrParser;
import declan.middleware.MyOptimizer;

public class MyOptimizerTest {
    private void comparePrograms(Prog optimized, String expected){
        String optimizedString = optimized.toString();
        assertTrue("The optimized program equals \n\n" + optimizedString + "\n\n and the expected equals \n\n" + expected, optimizedString.equals(expected));
    }

    @Test
    public void testSimpleCommonSubExpressionElimination(){
        String inputSource = "SYMBOL SECTION\n"
                           + "BSS SECTION\n"
                           + "DEF a := 1 <INT>\n"
                           + "DEF b := 2 <INT>\n" 
                           + "DEF GLOBAL i := a IADD b <INT>\n"
                           + "DEF GLOBAL z := a IADD b <INT>\n"
                           + "DEF GLOBAL f := (GLOBAL z) IADD (GLOBAL i) <INT>\n"
                           + "DATA SECTION\n"
                           + "CODE SECTION\n"
                           + "END\n"
                           + "PROC SECTION\n";

        String targetSource = "SYMBOL SECTION\r\n"
                            + "DATA SECTION\r\n"
                            + " DEF a := 1 <INT>\r\n"
                            + " DEF b := 2 <INT>\r\n"
                            + " DEF GLOBAL i := a IADD b <INT>\r\n"
                            + " DEF GLOBAL z := (GLOBAL i) <INT>\r\n"
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
                           + "BSS SECTION\n"
                           + " DEF GLOBAL t := TRUE <BOOL>\n"
                           + "DATA SECTION\n"
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
                           + "BSS SECTION\n"
                           + " DEF GLOBAL g := TRUE <BOOL>\n"
                           + "DATA SECTION\n"
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
                           + "BSS SECTION\n"
                           + " DEF GLOBAL g := TRUE <BOOL>\n"
                           + " DEF GLOBAL b := 2 <INT>"
                           + "DATA SECTION\n"
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
                                " DEF GLOBAL b := 2 <INT>\r\n" +
                                "BSS SECTION\r\n" + //
                                "CODE SECTION\r\n" + //
                                " LABEL block1\r\n" + //
                                " DEF a := 1 <INT>\r\n" + //
                                " GLOBAL b := 60 <INT>\r\n" +
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
    public void testDeadCodeElimination3() {
        String inputSource = "SYMBOL SECTION\n"
                + "BSS SECTION\n"
                + " DEF GLOBAL b := 2 <INT>"
                + "DATA SECTION\n"
                + "CODE SECTION\n"
                + "DEF a := 1 <INT>\n"
                + "a := a IADD (GLOBAL b) <INT>\n" 
                + "a := 0 <INT>\n"
                + "a := a IADD (GLOBAL b) <INT>\n"
                + "IF a IEQ a\n" 
                + "THEN label1\n"
                + "ELSE label2\n"
                + "LABEL label1\n"
                + "a := 50 <INT>\n"
                + "GOTO labelEnd\n"
                + "LABEL label2\n"
                + "a := 70 <INT>\n"
                + "GOTO labelEnd\n"
                + "LABEL labelEnd\n"
                + "CALL print([a -> p]<INT>)\n"
                + "END\n"
                + "PROC SECTION\n";

        String targetSource =   "SYMBOL SECTION\r\n"
                        + "DATA SECTION\r\n"
                        + " DEF GLOBAL b := 2 <INT>\r\n"
                        + "BSS SECTION\r\n"
                        + "CODE SECTION\r\n"
                        + " DEF a := 0 <INT>\r\n"
                        + " a := a IADD (GLOBAL b) <INT>\r\n"
                        + " IF a IEQ a\r\n"
                        + " THEN label1\r\n"
                        + " ELSE label2\r\n"
                        + " LABEL label1\r\n"
                        + " a := 50 <INT>\r\n"
                        + " GOTO labelEnd\r\n"
                        + " LABEL label2\r\n"
                        + " a := 70 <INT>\r\n"
                        + " GOTO labelEnd\r\n"
                        + " LABEL labelEnd\r\n"
                        + " CALL print([a -> p]<INT>)\r\n"
                        + "END\r\n"
                        + "PROC SECTION\r\n";

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
                           + "BSS SECTION\n" 
                           + " DEF GLOBAL a := 1 <INT>\n"
                           + " DEF GLOBAL b := 2 <INT>\n"
                           + "DATA SECTION\n"
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
    public void testConstantToGlobal() {
        String inputSrc = "SYMBOL SECTION\r\n"
                                + "BSS SECTION\r\n"
                                + " DEF GLOBAL a := 1 <INT>\r\n"
                                + " DEF GLOBAL b := 2 <INT>\r\n"
                                + "DATA SECTION\r\n"
                                + "CODE SECTION\r\n"
                                + " DEF i := 3 <INT>\r\n"
                                + " DEF z := 4 <INT>\r\n"
                                + " DEF f := 7 <INT>\r\n"
                                + "END\r\n"
                                + "PROC SECTION\r\n";
        
        String testSrc = "SYMBOL SECTION\r\n"
                                   + "DATA SECTION\r\n"
                               + " DEF GLOBAL a := 1 <INT>\r\n"
                               + " DEF GLOBAL b := 2 <INT>\r\n"
                               + " DEF GLOBAL d := 7 <INT>\r\n"
                               + " DEF GLOBAL g := 4 <INT>\r\n"
                               + " DEF GLOBAL e := 3 <INT>\r\n"
                               + "BSS SECTION\r\n"
                               + "CODE SECTION\r\n"
                               + " DEF i := (GLOBAL e) <INT>\r\n"
                               + " DEF z := (GLOBAL g) <INT>\r\n"
                               + " DEF f := (GLOBAL d) <INT>\r\n"
                               + "END\r\n"
                               + "PROC SECTION\r\n";
        
        ErrorLog errLog = new ErrorLog();
        ReaderSource source = new ReaderSource(new StringReader(inputSrc));
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog prog = parser.parseProgram();
        
        MyOptimizer optimizer = new MyOptimizer(null, prog);
        optimizer.performMoveConstants();
        
        Prog optimizedProg = optimizer.getICode();

        comparePrograms(optimizedProg, testSrc);
    }
}
