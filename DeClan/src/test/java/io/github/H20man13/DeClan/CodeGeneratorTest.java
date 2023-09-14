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
import edu.depauw.declan.common.ast.Library;
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
    private void testFile(String fileName, String expectedResult){
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
    public void testIoLib(){
        String expected = "B begin_0\n" + //
                          "h: .WORD 0\n" + //
                          "begin_0: B begin_1\n" + //
                          "WriteLn: SWI 4\n" + //
                          "LDR R2, [R13]\n" + //
                          "SUB R13, R13, #2\n" + //
                          "MOV R15, R14\n" + //
                          "WriteInt: LDR R0, c\n" + //
                          "SWI 1\n" + //
                          "LDR R3, [R13]\n" + //
                          "SUB R13, R13, #2\n" + //
                          "MOV R15, R14\n" + //
                          "WriteReal: LDR R0, e\n" + //
                          "SWI 2\n" + //
                          "LDR R2, [R13]\n" + //
                          "SUB R13, R13, #2\n" + //
                          "MOV R15, R14\n" + //
                          "ReadInt: SWI 3\n" + //
                          "STR R0, h\n" + //
                          "LDR R3, [R13]\n" + //
                          "SUB R13, R13, #2\n" + //
                          "MOV R15, R14\n" + //
                          "STP\n";

        ErrorLog errLog = new ErrorLog();
        MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
        Library ioLib = stdLib.ioLibrary();
        IrRegisterGenerator gen = new IrRegisterGenerator();
        
        MyICodeGenerator igen = new MyICodeGenerator(errLog, gen);
        ioLib.accept(igen);

        List<ICode> icode = igen.getICode();

        MyOptimizer optimizer = new MyOptimizer(icode, gen);
        optimizer.runDataFlowAnalysis();
        optimizer.performDeadCodeElimination();

        icode = optimizer.getICode();

        StringWriter writer = new StringWriter();
        MyCodeGenerator codeGen = new MyCodeGenerator(optimizer.getLiveVariableAnalysis(), icode, gen, errLog);
        codeGen.codeGen(writer);

        for(LogItem item : errLog){
            assertTrue(item.toString(), false);
        }

        assertTrue("Error expected...\n " + expected + " \n but found... \n " + writer.toString(), writer.toString().equals(expected));
    }

    
}
