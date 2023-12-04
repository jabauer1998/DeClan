package io.github.H20man13.DeClan.main;

import java.io.FileReader;
import java.util.ArrayList;
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
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalCall;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.InternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.util.Utils;

public class MyIrLinker {
    private Prog program;
    private List<Lib> libraries;
    private IrRegisterGenerator gen;

    public MyIrLinker(ErrorLog errLog, String progSrc, String... libSrc) throws Exception{
        this(errLog, parseProgram(errLog, progSrc), parseLibraries(errLog, libSrc));
    }

    public MyIrLinker(ErrorLog errLog, Program prog, Library... libs){
        this(errLog, generateProgram(errLog, prog), generateLibraries(errLog, libs));
    }

    public MyIrLinker(ErrorLog errLog, Prog program, Lib... libraries){
        this.program = program;
        this.libraries = new LinkedList<Lib>();
        this.gen = new IrRegisterGenerator();
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

    private void fetchExternalDependentInstructions(String identName, int mask, SymSec newTable, List<ICode> dataInstructions, Lib... libsToIgnore){
        List<ICode> toRet = new LinkedList<ICode>();
        loop: for(int libIndex = 0; libIndex < libraries.size(); libIndex++){
            Lib library = libraries.get(libIndex);
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                SymSec libSymbols = library.symbols;
                if(libSymbols.containsEntryWithIdentifier(identName, SymEntry.INTERNAL | mask)){
                    SymEntry libEntry = libSymbols.getEntryByIdentifier(identName, SymEntry.INTERNAL | mask);
                    DataSec libData = library.variables;
                    List<ICode> libICode = libData.intermediateCode;
                    for(int z = 0; z <= libICode.size(); z++){
                        Assign assignLib = (Assign)libICode.get(z);
                        if(assignLib.place.equals(libEntry.icodePlace)){
                            Exp exp = assignLib.value;
                            if(exp instanceof IdentExp){
                                IdentExp identExp = (IdentExp)exp;
                                if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL | mask)){
                                    SymEntry symEntry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL | mask);
                                    fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, library);
                                } else {
                                    fetchInternalDependentInstructions(library, identExp.ident, mask, newTable, dataInstructions);
                                }
                            } else if(exp instanceof UnExp){
                                UnExp unary = (UnExp)exp;
                                if(unary.right instanceof IdentExp){
                                    IdentExp identExp = (IdentExp)unary.right;
                                    if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL | mask)){
                                        SymEntry symEntry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL | mask);
                                        fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, identExp.ident, mask, newTable, dataInstructions);
                                    }
                                }
                            } else if(exp instanceof BinExp){
                                BinExp binary = (BinExp)exp;

                                if(binary.left instanceof IdentExp){
                                    IdentExp leftIdent = (IdentExp)binary.left;
                                    if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL | mask)){
                                        SymEntry symEntry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL | mask);
                                        fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, leftIdent.ident, mask, newTable, dataInstructions);
                                    }
                                }

                                if(binary.right instanceof IdentExp){
                                    IdentExp rightIdent = (IdentExp)binary.right;
                                    if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL | mask)){
                                        SymEntry symEntry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL | mask);
                                        fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, rightIdent.ident, mask, newTable,  dataInstructions);
                                    }
                                }
                            }

                            if(placeExistsInNewProgram(assignLib.place, dataInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNextRegister();
                                } while(placeExistsInNewProgram(place, dataInstructions));

                                replacePlaceInLib(library, assignLib.place, place);
                            }

                            toRet.add(assignLib);
                            break loop;
                        }
                    }
                }
            }
        }
        dataInstructions.addAll(toRet);
    }

    private void fetchInternalDependentInstructions(Lib currentLib, String labelName, int mask, SymSec newTable, List<ICode> dataInstructions){
        DataSec data = currentLib.variables;
        SymSec libSymbols = currentLib.symbols;
        LinkedList<ICode> toRet = new LinkedList<ICode>();
        for(int i = 0; i < data.intermediateCode.size(); i++){
            Assign assign = (Assign)data.intermediateCode.get(i);
            if(assign.place.equals(labelName)){
                Exp exp = assign.value;
                if(exp instanceof IdentExp){
                    IdentExp identExp = (IdentExp)exp;
                    if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL | mask)){
                        SymEntry symEntry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL | mask);
                        fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, currentLib);
                    } else {
                        fetchInternalDependentInstructions(currentLib, identExp.ident, mask, newTable, dataInstructions);
                    }
                } else if(exp instanceof UnExp){
                    UnExp unary = (UnExp)exp;
                    if(unary.right instanceof IdentExp){
                        IdentExp identExp = (IdentExp)unary.right;
                        if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL | mask)){
                            SymEntry symEntry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL | mask);
                            fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, identExp.ident, mask, newTable, dataInstructions);
                        }
                    }
                } else if(exp instanceof BinExp){
                    BinExp binary = (BinExp)exp;

                    if(binary.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)binary.left;
                        if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL | mask)){
                            SymEntry symEntry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL | mask);
                            fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, leftIdent.ident, mask, newTable, dataInstructions);
                        }
                    }

                    if(binary.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)binary.right;
                        if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL | mask)){
                            SymEntry symEntry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL | mask);
                            fetchExternalDependentInstructions(symEntry.declanIdent, SymEntry.EXTERNAL | mask, newTable, dataInstructions, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, rightIdent.ident, mask, newTable, dataInstructions);
                        }
                    }
                }
                
                if(placeExistsInNewProgram(assign.place, dataInstructions)){
                    String place = null;    
                    do{
                        place = gen.genNextRegister();
                    } while(placeExistsInNewProgram(place, dataInstructions));

                    replacePlaceInLib(currentLib, assign.place, place);
                }
                dataInstructions.add(assign);
                break;
            }
        }
    }

    private static void replacePlaceInICode(ICode icode, String oldPlace, String newPlace){
        if(icode instanceof Assign){
            Assign icodeAssign = (Assign)icode;

            if(icodeAssign.place.equals(oldPlace))
                icodeAssign.place = newPlace;

            Exp exp = icodeAssign.value;
            if(exp instanceof BinExp){
                BinExp binExp = (BinExp)exp;

                if(binExp.left instanceof IdentExp){
                    IdentExp leftExp = (IdentExp)binExp.left;

                    if(leftExp.ident.equals(oldPlace))
                        leftExp.ident = newPlace;
                }

                if(binExp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)binExp.right;

                    if(rightExp.ident.equals(oldPlace))
                        rightExp.ident = newPlace;
                }
            } else if(exp instanceof UnExp){
                UnExp unExp = (UnExp)exp;

                if(unExp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)unExp.right;

                    if(rightExp.ident.equals(oldPlace))
                        rightExp.ident = newPlace;
                }
            } else if(exp instanceof IdentExp){
                IdentExp iExp = (IdentExp)exp;

                if(iExp.ident.equals(oldPlace))
                    iExp.ident = newPlace;
            }
        } else if(icode instanceof If){
            If ifICode = (If)icode;

            BinExp exp = ifICode.exp;
            if(exp.left instanceof IdentExp){
                IdentExp leftIdent = (IdentExp)exp.left;
                
                if(leftIdent.ident.equals(oldPlace))
                    leftIdent.ident = newPlace;
            }

            if(exp.right instanceof IdentExp){
                IdentExp rightIdent = (IdentExp)exp.right;

                if(rightIdent.ident.equals(oldPlace))
                    rightIdent.ident = newPlace;
            }
        } else if(icode instanceof Inline){
            Inline inlineICode = (Inline)icode;

            List<String> newParam = new ArrayList<>();
            for(String param : inlineICode.param){
                if(param.equals(oldPlace))
                    newParam.add(newPlace);
                else
                    newParam.add(param);
            }
            
            inlineICode.param = newParam;
        } else if(icode instanceof ExternalPlace){
            ExternalPlace place = (ExternalPlace)icode;

            if(place.place.equals(oldPlace))
                place.place = newPlace;

            if(place.retPlace.equals(oldPlace))
                place.retPlace = newPlace;
        } else if(icode instanceof InternalPlace){
            InternalPlace place = (InternalPlace)icode;

            if(place.place.equals(oldPlace))
                place.place = newPlace;

            if(place.retPlace.equals(oldPlace))
                place.retPlace = newPlace;
        } else if(icode instanceof ParamAssign){
            ParamAssign assign = (ParamAssign)icode;
            if(assign.newPlace.equals(oldPlace))
                assign.newPlace = newPlace;

            if(assign.paramPlace.equals(oldPlace))
                assign.paramPlace = newPlace;
        } else if(icode instanceof ExternalCall){
            ExternalCall call = (ExternalCall)icode;

            List<String> newArgs = new LinkedList<String>();
            for(String arg : call.arguments){
                if(arg.equals(oldPlace))
                    newArgs.add(newPlace);
                else
                    newArgs.add(arg);
            }

            call.arguments = newArgs;
        } else if(icode instanceof Call){
            Call call = (Call)icode;
            
            List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
            for(Tuple<String, String> arg : call.params){
                Tuple<String, String> newArg = new Tuple<String,String>("", "");
                if(arg.dest.equals(oldPlace))
                    newArg.dest = newPlace;
                else
                    newArg.dest = arg.dest;

                if(arg.source.equals(oldPlace))
                    newArg.source = newPlace;
                else
                    newArg.source = arg.source;
                newArgs.add(newArg);
            }

            call.params = newArgs;
        }
    }

    private static void replacePlaceInLib(Lib library, String oldPlace, String newPlace){
        SymSec symbols = library.symbols;
        for(SymEntry entry : symbols.entries)
            if(entry.icodePlace.equals(oldPlace))
                entry.icodePlace = newPlace;
        
        DataSec dataSection = library.variables;
        for(ICode icode : dataSection.intermediateCode)
            replacePlaceInICode(icode, oldPlace, newPlace);

        ProcSec procSection = library.procedures;
        for(Proc proc : procSection.procedures){
            for(ParamAssign assign : proc.paramAssign)
                replacePlaceInICode(assign, oldPlace, newPlace);
            for(ICode icode : proc.instructions)
                replacePlaceInICode(icode, oldPlace, newPlace);
            if(proc.placement != null)
                replacePlaceInICode(proc.placement, oldPlace, newPlace);
        }
    }

    private boolean placeExistsInNewProgram(String place, List<ICode> dataCodes){
        for(ICode dataCode : dataCodes){
            if(dataCode instanceof Assign){
                Assign assign = (Assign)dataCode;
                
                if(assign.place.equals(place))
                    return true;

                Exp value = assign.value;
                if(value instanceof IdentExp){
                    IdentExp identVal = (IdentExp)value;
                    if(identVal.ident.equals(place))
                        return true;
                } else if(value instanceof UnExp){
                    UnExp unaryVal = (UnExp)value;
                    if(unaryVal.right instanceof IdentExp){
                        IdentExp rightVal = (IdentExp)unaryVal.right;
                        if(rightVal.ident.equals(place))
                            return true;
                    }
                } else if(value instanceof BinExp){
                    BinExp binVal = (BinExp)value;
                    
                    if(binVal.left instanceof IdentExp){
                        IdentExp leftVal = (IdentExp)binVal.left;
                        if(leftVal.ident.equals(place))
                            return true;
                    }

                    if(binVal.right instanceof IdentExp){
                        IdentExp rightVal = (IdentExp)binVal.right;
                        if(rightVal.ident.equals(place))
                            return true;
                    }
                }
            } else if(dataCode instanceof If){
                If ifStat = (If)dataCode;
                BinExp expression = ifStat.exp;

                if(expression.left instanceof IdentExp){
                    IdentExp leftIdent = (IdentExp)expression.left;
                    if(leftIdent.ident.equals(place))
                        return true;
                }

                if(expression.right instanceof IdentExp){
                    IdentExp rightIdent = (IdentExp)expression.right;
                    if(rightIdent.ident.equals(place))
                        return true;
                }
            } else if(dataCode instanceof ExternalPlace){
                ExternalPlace placement = (ExternalPlace)dataCode;
                if(placement.place.equals(place))
                    return true;

                if(placement.retPlace.equals(place))
                    return true;
            } else if(dataCode instanceof Inline){
                Inline inlineAssembly = (Inline)dataCode;
                for(String arg : inlineAssembly.param){
                    if(arg.equals(place))
                        return true;
                }
            } else if(dataCode instanceof ExternalCall){
                ExternalCall call = (ExternalCall)dataCode;
                for(String arg: call.arguments){
                    if(arg.equals(place))
                        return true;
                }
            } else if(dataCode instanceof Call){
                Call call = (Call)dataCode;
                for(Tuple<String, String> arg : call.params){
                    if(arg.source.equals(place))
                        return true;

                    if(arg.dest.equals(place))
                        return true;
                }
            }
        }
        return false;
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

                if(assignBinExp.left instanceof IdentExp){
                    IdentExp leftIdent = (IdentExp)assignBinExp.left;
                    if(programSymbolTable.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.CONST)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.CONST);
                        fetchExternalDependentInstructions(entry.declanIdent, SymEntry.EXTERNAL | SymEntry.CONST, symbolTable, finalAssignments);
                    }
                }

                if(assignBinExp.right instanceof IdentExp){
                    IdentExp rightIdent = (IdentExp)assignBinExp.right;
                    if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.CONST)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.CONST);
                        fetchExternalDependentInstructions(entry.declanIdent, SymEntry.EXTERNAL | SymEntry.CONST, symbolTable, finalAssignments);
                    }
                }
            } else if(assignExp instanceof UnExp){
                UnExp assignUnExp = (UnExp)assignExp;
                if(assignUnExp.right instanceof IdentExp){
                    IdentExp rightIdent = (IdentExp)assignUnExp.right;
                    if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.CONST)){
                        SymEntry entry = symbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.CONST);
                        fetchExternalDependentInstructions(entry.declanIdent, SymEntry.EXTERNAL | SymEntry.CONST, symbolTable, finalAssignments);
                    }
                }
            } else if(assignExp instanceof IdentExp){
                IdentExp assignIdentExp = (IdentExp)assignExp;
                if(programSymbolTable.containsEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL | SymEntry.CONST)){
                    SymEntry entry = symbolTable.getEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL | SymEntry.CONST);
                    fetchExternalDependentInstructions(entry.declanIdent, SymEntry.EXTERNAL | SymEntry.CONST, symbolTable, finalAssignments);
                }
            }
            finalAssignments.add(icode);
        }

        return new DataSec(finalAssignments);
    }

    public Prog performLinkage(){
        SymSec symbolTable = new SymSec(new LinkedList<SymEntry>());
        DataSec finalDataSec = linkDataSections(symbolTable);
        CodeSec finalCodeSec = linkCodeSection(symbolTable);
        ProcSec finalProcedureSec = linkProcedureSections(symbolTable);
        return new Prog(symbolTable, finalDataSec, finalProcedureSec, finalCodeSec);
    }
}
