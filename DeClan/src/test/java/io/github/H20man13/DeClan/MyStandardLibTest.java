package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Library;
import io.github.H20man13.DeClan.common.ElaborateReaderSource;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyStandardLibrary;
import io.github.H20man13.DeClan.main.MyTypeChecker;

public class MyStandardLibTest {
    private static void compareLibs(String libName){
        ErrorLog errLog = new ErrorLog();
        String locLib = System.getenv("DECLIB");
        assertTrue("Environment variable DECLIB is not set!!!", !locLib.equals(null));

        String declanDir = locLib + "\\declan";
        String irDir = "locLib\\ir\\linkable";

        String declanFile = declanDir + '\\' + libName + ".declib";
        Lib declanLib = parseAndGenerateDeclanSource(declanFile, errLog);

        String irFile = irDir + '\\' + libName + ".ilib";
        Lib irLib = parseIrSource(irFile, errLog);

        assertTrue("Error ir lib \n\n" + irLib.toString() + "is not equal to declanLib " + declanLib.toString(), irLib.equals(declanLib));
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
        compareLibs("Real");
    }

    @Test
    public void parseIntegerLibrary(){
        compareLibs("Integer");
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
