package io.github.H20man13.DeClan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.main.MyCodeGenerator;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyOptimizer;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class CodeGeneratorTest {
    public void testFile(String fileName, String expectedResult){
        try {
            StringWriter outputWriter = new StringWriter();
            Source mySource = new ReaderSource(new FileReader(fileName));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            
            Program prog = parser.parseProgram();

            MyStandardLibrary lib = new MyStandardLibrary(errLog);

            IrRegisterGenerator gen = new IrRegisterGenerator();
            MyICodeGenerator icodeGen = new MyICodeGenerator(errLog, gen);
            lib.ioLibrary().accept(icodeGen);
            lib.mathLibrary().accept(icodeGen);
            prog.accept(icodeGen);

            List<ICode> resultICode = icodeGen.getICode();
            
            MyOptimizer optimizer = new MyOptimizer(resultICode, gen);
            optimizer.runDataFlowAnalysis();
            optimizer.performConstantPropogation();
            optimizer.performDeadCodeElimination();

            List<ICode> optimizedICode = optimizer.getICode();

            MyCodeGenerator codeGenerator = new MyCodeGenerator(optimizer.getLiveVariableAnalysis(), optimizedICode, gen, errLog);
            codeGenerator.codeGen(outputWriter);

            for(LogItem item : errLog){
                assertTrue(item.toString(), false);
            }

            assertTrue("Error expected...\n " + expectedResult + " \n but found... \n " + outputWriter.toString(), outputWriter.toString().equals(expectedResult));
        } catch (FileNotFoundException e) {
            assertFalse(e.toString(), true);    
        }
    }

    @Test
    public void testForLoopBasic(){
        String expectedResult = "B begin_0\r\n" + //
                                "g: .WORD 1\r\n" + //
                                "h: .WORD 0\r\n" + //
                                "C: .WORD 1\r\n" + //
                                "D: .WORD -1\r\n" + //
                                "o: .WORD 1\r\n" + //
                                "p: .WORD 0\r\n" + //
                                "F: .WORD 1\r\n" + //
                                "G: .WORD -1\r\n" + //
                                "q: .WORD 1\r\n" + //
                                "r: .WORD 0\r\n" + //
                                "I: .WORD 1\r\n" + //
                                "J: .WORD -1\r\n" + //
                                "s: .WORD 1\r\n" + //
                                "t: .WORD 0\r\n" + //
                                "L: .WORD 1\r\n" + //
                                "M: .WORD -1\r\n" + //
                                "v: .WORD 1\r\n" + //
                                "u: .WORD 1\r\n" + //
                                "w: .WORD 10\r\n" + //
                                "x: .WORD 1\r\n" + //
                                "P: .WORD 1\r\n" + //
                                "y: .WORD 0\r\n" + //
                                "begin_0: B begin_1\r\n" + //
                                "WriteLn: LDR R2, [R14]\r\n" + //
                                "SUB R14, R14, #2\r\n" + //
                                "WriteInt: LDR R3, [R14]\r\n" + //
                                "SUB R14, R14, #2\r\n" + //
                                "WriteReal: LDR R2, [R14]\r\n" + //
                                "SUB R14, R14, #2\r\n" + //
                                "ReadInt: LDR R2, D\r\n" + //
                                "LDR R3, C\r\n" + //
                                "MUL R3, R3, R2\r\n" + //
                                "STR R3, h\r\n" + //
                                "LDR R3, [R14]\r\n" + //
                                "SUB R14, R14, #2\r\n" + //
                                "begin_1: B begin_2\r\n" + //
                                "Round: LDR R3, G\r\n" + //
                                "LDR R2, F\r\n" + //
                                "MUL R2, R2, R3\r\n" + //
                                "STR R2, p\r\n" + //
                                "LDR R2, [R14]\r\n" + //
                                "SUB R14, R14, #2\r\n" + //
                                "Floor: LDR R2, J\r\n" + //
                                "LDR R3, I\r\n" + //
                                "MUL R3, R3, R2\r\n" + //
                                "STR R3, r\r\n" + //
                                "LDR R3, [R14]\r\n" + //
                                "SUB R14, R14, #2\r\n" + //
                                "Ceil: LDR R3, M\r\n" + //
                                "LDR R2, L\r\n" + //
                                "MUL R2, R2, R3\r\n" + //
                                "STR R2, t\r\n" + //
                                "LDR R2, [R14]\r\n" + //
                                "SUB R14, R14, #2\r\n" + //
                                "begin_2: B begin_3\r\n" + //
                                "FORBEG_0_LEVEL_0: LDR R3, u\r\n" + //
                                "LDR R3, w\r\n" + //
                                "TST R3, R3\r\n" + //
                                "B NE FORLOOP_0_LEVEL_0\r\n" + //
                                "B EQ FOREND_0_LEVEL_0\r\n" + //
                                "FORLOOP_0_LEVEL_0: ADD R14, R14, #3\r\n" + //
                                "LDR R3, u\r\n" + //
                                "LDR R3, b\r\n" + //
                                "STR R3, [R14,-R3]\r\n" + //
                                "BL WriteInt\r\n" + //
                                "LDR R2, u\r\n" + //
                                "LDR R3, P\r\n" + //
                                "ADD R2, R2, R3\r\n" + //
                                "STR R2, y\r\n" + //
                                "LDR R2, y\r\n" + //
                                "STR R2, u\r\n" + //
                                "B FORBEG_0_LEVEL_0\r\n" + //
                                "FOREND_0_LEVEL_0: ADD R14, R14, #2\r\n" + //
                                "BL WriteLn\r\n" + //
                                "STOP\r\n";
        testFile("test_source/ForLoopBasic.dcl", expectedResult);
    }
}
