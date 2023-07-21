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
        String expectedResult = "";
        testFile("test_source/ForLoopBasic.dcl", expectedResult);
    }
}
