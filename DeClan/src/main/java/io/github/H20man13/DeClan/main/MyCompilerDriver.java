package io.github.H20man13.DeClan.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;

public class MyCompilerDriver {
    public static void main(String[] args) throws FileNotFoundException{
        Source source = new ReaderSource(new FileReader(args[0]));
        ErrorLog errLog = new ErrorLog();
        MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
        MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
        Program prog = parser.parseProgram();
        
        if(prog != null){
            MyTypeChecker typeChecker = new MyTypeChecker(errLog);
            prog.accept(typeChecker);
        }
        
        IrRegisterGenerator gen = new IrRegisterGenerator();
        if(prog != null){
            MyICodeGenerator codeGen = new MyICodeGenerator(errLog, gen);
            Prog program = codeGen.generateProgramIr(prog);
        }
    }
}
