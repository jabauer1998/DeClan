package io.github.H20man13.DeClan;

import java.io.File;
import java.io.FileReader;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ast.Library;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.source.ElaborateReaderSource;
import io.github.H20man13.DeClan.common.source.Source;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;

public class MyStandardLibTest {
    private static void compareLibs(String libName){
        ErrorLog errLog = new ErrorLog();
        String locLib = System.getenv("DECLIB");
        assertTrue("Environment variable DECLIB is not set!!!", !(locLib == null));

        String declanDir = locLib + "\\declan";
        String irDir = locLib +"\\ir\\linkable";

        String declanFile = declanDir + '\\' + libName + ".declib";
        Lib declanLib = parseAndGenerateDeclanSource(declanFile, errLog);

        String irFile = irDir + '\\' + libName + ".ilib";
        Lib irLib = parseIrSource(irFile, errLog);

        assertTrue("Number of instructions in irLib not equal to number of ir instructions in generated declan lib \nirLib: " + irLib.getSize() + "\ndeclanLib: " + declanLib.getSize(), irLib.getSize() == declanLib.getSize());
        for(int i = 0; i < irLib.getSize(); i++){
            ICode irLibInstr = irLib.getInstruction(i);
            ICode declanLibInstr = declanLib.getInstruction(i);
            assertTrue("Ir file instruction " + irLibInstr.toString() + "\n\n is not equal to declanLib instr \n\n" + declanLibInstr.toString(), declanLibInstr.equals(irLibInstr));
        }
    }

    private static Lib parseAndGenerateDeclanSource(String declanFile, ErrorLog errLog){
        File fileExists = new File(declanFile);
        assertTrue("Declan File does not exist", fileExists.exists());
        try{
            FileReader declanReader = new FileReader(declanFile);
            Source declanSource = new ElaborateReaderSource(declanFile, declanReader);
            MyDeClanLexer declanLexer = new MyDeClanLexer(declanSource, errLog);
            MyDeClanParser declanParser = new MyDeClanParser(declanLexer, errLog);
            Library declanLib = declanParser.parseLibrary();
            declanParser.close();
            MyICodeGenerator iGen = new MyICodeGenerator(errLog);
            return iGen.generateLibraryIr(declanLib);
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
            throw new RuntimeException(exp.toString());
        }
    }

    private static Lib parseIrSource(String irFile, ErrorLog errLog){
        File fileExists = new File(irFile);
        assertTrue("Ir file at path-\n" + irFile + "\n does not exist!!!", fileExists.exists());
        try{
            FileReader reader = new FileReader(irFile);
            Source source = new ElaborateReaderSource(irFile, reader);
            MyIrLexer lexer = new MyIrLexer(source, errLog);
            MyIrParser parser = new MyIrParser(lexer, errLog);
            return parser.parseLibrary();
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
            throw new RuntimeException(exp.toString());
        }
    }

    @Test
    public void parseMathLib(){
        compareLibs("Math");
    }

    @Test
    public void parseIoLib(){
        compareLibs("Io");
    }

    @Test
    public void parseRealLibrary(){
        compareLibs("RealOperations");
    }

    @Test
    public void parseIntegerLibrary(){
        compareLibs("IntOperations");
    }

    @Test
    public void parseConversionsLibrary(){
        compareLibs("Conversions");
    }

    @Test
    public void parseUtilsLibrary(){
        compareLibs("Utils");
    }
}
