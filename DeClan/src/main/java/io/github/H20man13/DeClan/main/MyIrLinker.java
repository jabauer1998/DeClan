package io.github.H20man13.DeClan.main;

import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;

public class MyIrLinker {
    private Prog program;
    private List<Lib> libraries;

    public MyIrLinker(ErrorLog errLog, String progSrc, String... libSrc) throws Exception{
        this(errLog, parseProgram(errLog, progSrc), parseLibraries(errLog, libSrc));
    }

    public MyIrLinker(ErrorLog errLog, Program prog, Library... libs){
        this(errLog, generateProgram(errLog, prog), generateLibraries(errLog, libs));
    }

    public MyIrLinker(ErrorLog errLog, Prog program, Lib... libraries){
        this.program = program;
        this.libraries = new LinkedList<Lib>();
        int size = libraries.length;
        for(int i = 0; i < size; i++){
            this.libraries.add(libraries[i]);
        }
    }

    private static Program parseProgram(ErrorLog errLog, String program) throws Exception{
        FileReader reader = new FileReader(program);
        Source source = new ReaderSource(reader);
        MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
        MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
        return parser.parseProgram();
    }

    private static Library[] parseLibraries(ErrorLog errLog, String... libraries) throws Exception{
        int size = libraries.length;
        Library[] parsedLibs = new Library[size];
        for(int i = 0; i < size; i++){
            String librarySrc = libraries[i];
            parsedLibs[i] = parseLibrary(errLog, librarySrc);
        }
        return parsedLibs;
    }

    private static Library parseLibrary(ErrorLog errLog, String library) throws Exception{
        FileReader reader = new FileReader(library);
        Source source = new ReaderSource(reader);
        MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
        MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
        return parser.parseLibrary();
    }

    private static Prog generateProgram(ErrorLog errorLog, Program prog){
        IrRegisterGenerator gen = new IrRegisterGenerator();
        MyICodeGenerator iGen = new MyICodeGenerator(errorLog, gen);
        return iGen.generateProgramIr(prog);
    }

    private static Lib[] generateLibraries(ErrorLog errLog, Library... libs){
        int size = libs.length;
        Lib[] toRet = new Lib[size];
        for(int i = 0; i < size; i++){
            toRet[i]  = generateLibrary(errLog, libs[i]);
        }
        return toRet;
    }

    private static Lib generateLibrary(ErrorLog errLog, Library lib){
        IrRegisterGenerator gen = new IrRegisterGenerator();
        MyICodeGenerator iGen = new MyICodeGenerator(errLog, gen);
        return iGen.generateLibraryIr(lib);
    }

    public Prog performStaticLink(){
        
    }
}
