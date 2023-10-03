package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
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
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerLexer;

public class CodeGeneratorTest {
    private void testDeclanFile(String fileName){
        ErrorLog errLog = new ErrorLog();
        try{
            FileReader reader = new FileReader(fileName);
            ReaderSource source = new ReaderSource(reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            MyStandardLibrary stdLib = new MyStandardLibrary(errLog);

            Program prog = parser.parseProgram();

            IrRegisterGenerator rGen = new IrRegisterGenerator();
            MyICodeGenerator gen = new MyICodeGenerator(errLog, rGen);

            stdLib.ioLibrary().accept(gen);
            stdLib.mathLibrary().accept(gen);

            prog.accept(gen);

            List<ICode> generatedICode = gen.getICode();

            MyOptimizer optimizer = new MyOptimizer(generatedICode, rGen);
            optimizer.runDataFlowAnalysis();
            optimizer.performDeadCodeElimination();

            List<ICode> optimizedICode = optimizer.getICode();

            MyCodeGenerator codeGenerator = new MyCodeGenerator(optimizer.getLiveVariableAnalysis(), optimizedICode, rGen, errLog);

            

            StringWriter writer = new StringWriter();
            codeGenerator.codeGen(writer);

            for(LogItem item : errLog){
                assertTrue(item.toString(), false);
            }
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        testDeclanFile("test_source/Conversions.dcl");
    }
}
