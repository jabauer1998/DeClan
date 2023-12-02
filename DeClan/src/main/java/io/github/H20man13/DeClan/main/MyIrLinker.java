package io.github.H20man13.DeClan.main;

import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;

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

    private DataSec linkDataSections(SymSec symbolTable){
        List<ICode> finalAssignments = new LinkedList<ICode>();

        SymSec programSymbolTable = program.symbols;
        DataSec programDataSec = program.variables;
        List<ICode> programAssignments = programDataSec.intermediateCode;
        for(int i = 0; i < programAssignments.size(); i++){
            ICode icode = programAssignments.get(i);
            Assign assign = (Assign)icode;
            Exp assignExp = assign.value;
            if(assignExp instanceof BinExp){
                BinExp assignBinExp = (BinExp)assignExp;

            } else if(assignExp instanceof UnExp){
                UnExp assignUnExp = (UnExp)assignExp;

            } else if(assignExp instanceof IdentExp){
                IdentExp assignIdentExp = (IdentExp)assignExp;
                if(symbolTable.containsEntry(assignIdentExp.ident)){
                    SymEntry entry = symbolTable.getEntry(assignIdentExp.ident, SymEntry.EXTERNAL | SymEntry.CONST);
                    String identToSearch = entry.declanIdent;
                    for(int libIndex = 0; libIndex < libraries.size(); libIndex++){
                        Lib library = libraries.get(libIndex);
                        SymSec libSymbols = library.symbols;
                        if(libSymbols)
                    }
                }
            }
        }
    }

    public Prog performLinkage(){
        SymSec symbolTable = new SymSec(new LinkedList<SymEntry>());
        DataSec finalDataSec = linkDataSections(symbolTable);
        CodeSec finalCodeSec = linkCodeSections(symbolTable);
        ProcSec finalProcedureSec = linkProcedureSections(symbolTable);
        return new Prog(symbolTable, finalDataSec, finalProcedureSec, finalCodeSec);
    }
}
