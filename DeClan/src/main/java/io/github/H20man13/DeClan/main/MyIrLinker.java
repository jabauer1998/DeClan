package io.github.H20man13.DeClan.main;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DataFormatException;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.gen.LabelGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.icode.Assign.Scope;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalCall;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.util.Utils;

public class MyIrLinker {
    private IrRegisterGenerator gen;
    private ErrorLog errLog;

    public MyIrLinker(ErrorLog errLog){
        this.errLog = errLog;
        this.gen = new IrRegisterGenerator();
    }

    private static Prog generateProgram(ErrorLog errorLog, Program prog){
        MyICodeGenerator iGen = new MyICodeGenerator(errorLog);
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
        MyICodeGenerator iGen = new MyICodeGenerator(errLog);
        return iGen.generateLibraryIr(lib);
    }

    private void fetchExternalDependentInstructions(String identName, Prog program, Lib[] libraries, SymSec newTable, DataSec dataInstructions, CodeSec codeSec, ProcSec procSec, Lib... libsToIgnore){
        loop: for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                SymSec libSymbols = library.symbols;
                if(libSymbols.containsEntryWithIdentifier(identName, SymEntry.INTERNAL)){
                    SymEntry libEntry = libSymbols.getEntryByIdentifier(identName, SymEntry.INTERNAL);
                    DataSec libData = library.variables;
                    ProcSec libProc = library.procedures;
                    for(int z = 0; z < libData.getLength(); z++){
                        ICode icodeLib = libData.getInstruction(z);
                        if(icodeLib instanceof Assign){
                            Assign assignLib = (Assign)icodeLib;

                            if(assignLib.place.equals(libEntry.icodePlace)){
                                if(assignLib.getScope() == Assign.Scope.EXTERNAL_RETURN){
                                    ICode funcCallICode = libData.getInstruction(z - 1);
                                    if(funcCallICode instanceof Call){
                                        Call funcCall = (Call)funcCallICode;

                                        if(!procSec.containsProcedure(funcCall.pname))
                                            fetchInternalProcedure(library, funcCall.pname, program, libraries, newTable, dataInstructions, codeSec, procSec);

                                        int numArgs = funcCall.params.size();
                                        for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                            Tuple<String, String> arg = funcCall.params.get(argIndex);
                                            if(libSymbols.containsEntryWithICodePlace(arg.source, SymEntry.EXTERNAL)){
                                                SymEntry entry = libSymbols.getEntryByICodePlace(arg.source, SymEntry.EXTERNAL);
                                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                                if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                    SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                                }
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, arg.source, newTable, dataInstructions, codeSec, procSec);
                                            }
                                        }

                                        if(!placeIsUniqueToProgramOrLibrary(assignLib.place, program, libraries, library)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!placeIsUniqueToProgramOrLibrary(newPlace, program, libraries, library));

                                            replacePlaceInLib(library, assignLib.place, newPlace);
                                        }

                                        if(!instructionExistsInNewProgram(funcCall, dataInstructions)){
                                            dataInstructions.addInstruction(funcCall);
                                        }

                                        if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                            dataInstructions.addInstruction(assignLib);
                                        }
                                    }
                                } else {
                                    Exp exp = assignLib.value;
                                    if(exp instanceof IdentExp){
                                        IdentExp identExp = (IdentExp)exp;
                                        if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                            if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                            if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                            }
                                        } else {
                                            fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newTable, dataInstructions, codeSec, procSec);
                                        }

                                        if(!placeIsUniqueToProgramOrLibrary(assignLib.place, program, libraries, library)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, library));

                                            replacePlaceInLib(library, assignLib.place, place);
                                        }

                                        if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                            dataInstructions.addInstruction(assignLib);
                                            newTable.addEntry(libEntry);
                                        }
                                    } else if(exp instanceof UnExp){
                                        UnExp unary = (UnExp)exp;
                                        if(unary.right instanceof IdentExp){
                                            IdentExp identExp = (IdentExp)unary.right;
                                            if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                                SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                                if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                    SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                                }
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newTable, dataInstructions, codeSec, procSec);
                                            }
                                        }

                                        if(!placeIsUniqueToProgramOrLibrary(assignLib.place, program, libraries, library)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, library));

                                            replacePlaceInLib(library, assignLib.place, place);
                                        }

                                        if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                            dataInstructions.addInstruction(assignLib);
                                            newTable.addEntry(libEntry);
                                        }
                                    } else if(exp instanceof BinExp){
                                        BinExp binary = (BinExp)exp;

                                        if(binary.left instanceof IdentExp){
                                            IdentExp leftIdent = (IdentExp)binary.left;
                                            if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                                SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                                if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                    SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                                }
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, leftIdent.ident, newTable, dataInstructions, codeSec, procSec);
                                            }
                                        }

                                        if(binary.right instanceof IdentExp){
                                            IdentExp rightIdent = (IdentExp)binary.right;
                                            if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                                SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                                if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                    SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                                }
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, rightIdent.ident, newTable, dataInstructions, codeSec, procSec);
                                            }
                                        }

                                        if(!placeIsUniqueToProgramOrLibrary(assignLib.place, program, libraries, library)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, library));

                                            replacePlaceInLib(library, assignLib.place, place);
                                        }

                                        if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                            dataInstructions.addInstruction(assignLib);
                                            newTable.addEntry(libEntry);
                                        }
                                    } else if(exp instanceof ExternalCall){
                                        ExternalCall call = (ExternalCall)exp;
                                        if(!procSec.containsProcedure(call.procedureName))
                                            fetchExternalProcedure(call.procedureName, program, libraries, newTable, dataInstructions, codeSec, procSec, library);
                                        Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                        int numberOfArgsInCall = call.arguments.size();

                                        if(numberOfArgsInCall != numberOfArgsInProc){
                                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(libIndex, 0));
                                        } else {
                                            List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                                String place = call.arguments.get(argIndex);
                                                Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                                
                                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                                    }
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, place, newTable, dataInstructions, codeSec, procSec);
                                                }

                                                newArg.source = place;
                                                newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                                newArgs.add(newArg);
                                            }

                                            if(!placeIsUniqueToProgramOrLibrary(assignLib.place, program, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, library));

                                                replacePlaceInLib(library, assignLib.place, place);
                                            }

                                            Call newCall = new Call(call.procedureName, newArgs);
                                            if(!instructionExistsInNewProgram(newCall, dataInstructions)){
                                                dataInstructions.addInstruction(newCall);
                                            }

                                            String toRetFrom = fetchedProcedure.placement.place;
                                            String toRetTo = assignLib.place;
                                            Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignLib.getType());
                                            if(!instructionExistsInNewProgram(newPlace, dataInstructions)){
                                                dataInstructions.addInstruction(newPlace);
                                                newTable.addEntry(libEntry);
                                            }
                                        }
                                    } else {
                                        if(!placeIsUniqueToProgramOrLibrary(assignLib.place, program, libraries, library)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, library));

                                            replacePlaceInLib(library, assignLib.place, place);
                                        }

                                        if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                            dataInstructions.addInstruction(assignLib);
                                            newTable.addEntry(libEntry);
                                        } 
                                    }
                                }


                                break loop;
                            }
                        }
                    }
                }
            }
        }
    }

    private void fetchExternalDependentInstructions(String identName, Lib single, Lib[] libraries, SymSec newTable, DataSec dataInstructions, ProcSec procSec, Lib... libsToIgnore){
        loop: for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                SymSec libSymbols = library.symbols;
                if(libSymbols.containsEntryWithIdentifier(identName, SymEntry.INTERNAL)){
                    SymEntry libEntry = libSymbols.getEntryByIdentifier(identName, SymEntry.INTERNAL);
                    DataSec libData = library.variables;
                    for(int z = 0; z < libData.getLength(); z++){
                        ICode icodeLib = libData.getInstruction(z);
                        if(icodeLib instanceof Assign){
                            Assign assignLib = (Assign)icodeLib;
                            if(assignLib.place.equals(libEntry.icodePlace)){
                                Exp exp = assignLib.value;
                                if(exp instanceof IdentExp){
                                    IdentExp identExp = (IdentExp)exp;
                                    if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                        if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                        if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newTable, dataInstructions, procSec);
                                    }

                                    if(!placeIsUniqueToLibrary(assignLib.place, single, libraries, library)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!placeIsUniqueToLibrary(place, single, libraries, library));
                                        replacePlaceInLib(library, assignLib.place, place);
                                    }

                                    if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                        dataInstructions.addInstruction(assignLib);
                                        newTable.addEntry(libEntry);
                                    }
                                } else if(exp instanceof UnExp){
                                    UnExp unary = (UnExp)exp;
                                    if(unary.right instanceof IdentExp){
                                        IdentExp identExp = (IdentExp)unary.right;
                                        if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                            if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                            if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                            }
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newTable, dataInstructions, procSec);
                                        }
                                    }

                                    if(!placeIsUniqueToLibrary(assignLib.place, single, libraries, library)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!placeIsUniqueToLibrary(place, single, libraries, library));
                                        replacePlaceInLib(library, assignLib.place, place);
                                    }

                                    if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                        dataInstructions.addInstruction(assignLib);
                                        newTable.addEntry(libEntry);
                                    }
                                } else if(exp instanceof BinExp){
                                    BinExp binary = (BinExp)exp;

                                    if(binary.left instanceof IdentExp){
                                        IdentExp leftIdent = (IdentExp)binary.left;
                                        if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                            if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                            if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                            }
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, leftIdent.ident, newTable, dataInstructions, procSec);
                                        }
                                    }

                                    if(binary.right instanceof IdentExp){
                                        IdentExp rightIdent = (IdentExp)binary.right;
                                        if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                            if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                            if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                            }
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, rightIdent.ident, newTable, dataInstructions, procSec);
                                        }
                                    }

                                    if(!placeIsUniqueToLibrary(assignLib.place, single, libraries, library)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!placeIsUniqueToLibrary(place, single, libraries, library));
                                        replacePlaceInLib(library, assignLib.place, place);
                                    }

                                    if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                        dataInstructions.addInstruction(assignLib);
                                        newTable.addEntry(libEntry);
                                    }
                                } else if(exp instanceof ExternalCall){
                                    ExternalCall call = (ExternalCall)exp;
                                    if(!procSec.containsProcedure(call.procedureName))
                                        fetchExternalProcedure(call.procedureName, single, libraries, newTable, dataInstructions, procSec, library);
                                    Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                                    int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                    int numberOfArgsInCall = call.arguments.size();

                                    if(numberOfArgsInCall != numberOfArgsInProc){
                                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(libIndex, 0));
                                    } else {
                                        List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                            String place = call.arguments.get(argIndex);
                                            Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                            
                                            if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                                SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                                if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                    SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                                }
                                            } else {
                                                fetchInternalDependentInstructions(library, single, libraries, place, newTable, dataInstructions, procSec);
                                            }

                                            newArg.source = place;
                                            newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                            newArgs.add(newArg);
                                        }

                                        if(!placeIsUniqueToLibrary(assignLib.place, single, libraries, library)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueToLibrary(place, single, libraries, library));
                                            replacePlaceInLib(library, assignLib.place, place);
                                        }

                                        Call newCall = new Call(call.procedureName, newArgs);
                                        if(!instructionExistsInNewProgram(newCall, dataInstructions)){
                                            dataInstructions.addInstruction(newCall);
                                        }

                                        String toRetFrom = fetchedProcedure.placement.place;
                                        String toRetTo = assignLib.place;
                                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignLib.getType());
                                        if(!instructionExistsInNewProgram(newPlace, dataInstructions)){
                                            dataInstructions.addInstruction(newPlace);
                                            newTable.addEntry(libEntry);
                                        }
                                    }
                                } else {
                                    if(!placeIsUniqueToLibrary(assignLib.place, single, libraries, library)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!placeIsUniqueToLibrary(place, single, libraries, library));
                                        replacePlaceInLib(library, assignLib.place, place);
                                    }

                                    if(!instructionExistsInNewProgram(assignLib, dataInstructions)){
                                        dataInstructions.addInstruction(assignLib);
                                        newTable.addEntry(libEntry);
                                    }
                                }
                                break loop;
                            }
                        }
                    }
                }
            }
        }
    }

    private void fetchInternalDependentInstructions(Lib currentLib, Lib single, Lib[] libraries, String labelName, SymSec newTable, DataSec dataInstructions, ProcSec procSec){
        DataSec data = currentLib.variables;
        SymSec libSymbols = currentLib.symbols;
        for(int i = 0; i < data.getLength(); i++){
            ICode icodeLib = data.getInstruction(i);
            if(icodeLib instanceof Assign){
                Assign assign = (Assign)icodeLib;
                if(assign.place.equals(labelName)){
                    if(assign.getScope() == Scope.EXTERNAL_RETURN){
                        ICode funcCallICode = data.getInstruction(i - 1);
                        if(funcCallICode instanceof Call){
                            Call funcCall = (Call)funcCallICode;

                            if(!procSec.containsProcedure(funcCall.pname))
                                fetchInternalProcedure(currentLib, funcCall.pname, single, libraries, newTable, dataInstructions, procSec);

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Tuple<String, String> arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsEntryWithICodePlace(arg.source, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.source, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, arg.source, newTable, dataInstructions, procSec);
                                }
                            }

                            if(!placeIsUniqueToLibrary(assign.place, single, libraries, currentLib)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueToLibrary(newPlace, single, libraries, currentLib));

                                replacePlaceInLib(currentLib, assign.place, newPlace);
                            }

                            if(!instructionExistsInNewProgram(funcCall, dataInstructions)){
                                dataInstructions.addInstruction(funcCall);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                            }
                        }
                    } else {
                        Exp exp = assign.value;
                        if(exp instanceof IdentExp){
                            IdentExp identExp = (IdentExp)exp;
                            if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newTable, dataInstructions, procSec);
                            }

                            if(!placeIsUniqueToLibrary(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToLibrary(place, single, libraries, currentLib));
                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        } else if(exp instanceof UnExp){
                            UnExp unary = (UnExp)exp;
                            if(unary.right instanceof IdentExp){
                                IdentExp identExp = (IdentExp)unary.right;
                                if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newTable, dataInstructions, procSec);
                                }
                            }

                            if(!placeIsUniqueToLibrary(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToLibrary(place, single, libraries, currentLib));
                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        } else if(exp instanceof BinExp){
                            BinExp binary = (BinExp)exp;

                            if(binary.left instanceof IdentExp){
                                IdentExp leftIdent = (IdentExp)binary.left;
                                if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, leftIdent.ident, newTable, dataInstructions, procSec);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, rightIdent.ident, newTable, dataInstructions, procSec);
                                }
                            }

                            if(!placeIsUniqueToLibrary(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToLibrary(place, single, libraries, currentLib));
                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        } else if(exp instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)exp;

                            if(!procSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, single, libraries, newTable, dataInstructions, procSec, currentLib);
                            Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    String place = call.arguments.get(argIndex);
                                    Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                    
                                    if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                        if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newTable, dataInstructions, procSec);
                                        if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(currentLib, single, libraries, place, newTable, dataInstructions, procSec);
                                    }

                                    newArg.source = place;
                                    newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(newArg);
                                }

                                if(!placeIsUniqueToLibrary(assign.place, single, libraries, currentLib)){
                                    String place = null;    
                                    do{
                                        place = gen.genNext();
                                    } while(!placeIsUniqueToLibrary(place, single, libraries, currentLib));
                                    replacePlaceInLib(currentLib, assign.place, place);
                                }

                                Call newCall = new Call(call.procedureName, newArgs);
                                if(!instructionExistsInNewProgram(newCall, dataInstructions)){
                                    dataInstructions.addInstruction(newCall);
                                }

                                String toRetFrom = fetchedProcedure.placement.place;
                                String toRetTo = assign.place;
                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                                if(!instructionExistsInNewProgram(newPlace, dataInstructions)){
                                    dataInstructions.addInstruction(newPlace);
                                    if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                        newTable.addEntry(entry);
                                    }
                                }
                            }
                        } else {
                            if(!placeIsUniqueToLibrary(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToLibrary(place, single, libraries, currentLib));
                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

     private void fetchInternalDependentInstructions(Lib currentLib, Prog program, Lib[] libraries, String labelName, SymSec newTable, DataSec dataInstructions, CodeSec codeSec,  ProcSec procSec){
        DataSec data = currentLib.variables;
        SymSec libSymbols = currentLib.symbols;
        for(int i = 0; i < data.intermediateCode.size(); i++){
            ICode icodeLib = data.getInstruction(i);
            if(icodeLib instanceof Assign){
                Assign assign = (Assign)icodeLib;
                if(assign.place.equals(labelName)){
                    if(assign.getScope() == Scope.EXTERNAL_RETURN){
                        ICode funcCallICode = data.getInstruction(i - 1);
                        if(funcCallICode instanceof Call){
                            Call funcCall = (Call)funcCallICode;

                            if(!procSec.containsProcedure(funcCall.pname))
                                fetchInternalProcedure(currentLib, funcCall.pname, program, libraries, newTable, dataInstructions, codeSec, procSec);

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Tuple<String, String> arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsEntryWithICodePlace(arg.source, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.source, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, arg.source, newTable, dataInstructions, codeSec, procSec);
                                }
                            }

                            if(!placeIsUniqueToProgramOrLibrary(assign.place, program, libraries, currentLib)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueToProgramOrLibrary(newPlace, program, libraries, currentLib));

                                replacePlaceInLib(currentLib, assign.place, newPlace);
                            }

                            if(!instructionExistsInNewProgram(funcCall, dataInstructions)){
                                dataInstructions.addInstruction(funcCall);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                            }
                        }
                    } else {
                        Exp exp = assign.value;
                        if(exp instanceof IdentExp){
                            IdentExp identExp = (IdentExp)exp;
                            if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newTable, dataInstructions, procSec);
                            }

                            if(!placeIsUniqueToProgramOrLibrary(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, currentLib));

                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        } else if(exp instanceof UnExp){
                            UnExp unary = (UnExp)exp;
                            if(unary.right instanceof IdentExp){
                                IdentExp identExp = (IdentExp)unary.right;
                                if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newTable, dataInstructions, procSec);
                                }
                            }

                            if(!placeIsUniqueToProgramOrLibrary(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, currentLib));

                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        } else if(exp instanceof BinExp){
                            BinExp binary = (BinExp)exp;

                            if(binary.left instanceof IdentExp){
                                IdentExp leftIdent = (IdentExp)binary.left;
                                if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, leftIdent.ident, newTable, dataInstructions, procSec);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                    if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, rightIdent.ident, newTable, dataInstructions, procSec);
                                }
                            }

                            if(!placeIsUniqueToProgramOrLibrary(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, currentLib));

                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        } else if(exp instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)exp;
                            if(!procSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, program, libraries, newTable, dataInstructions, procSec, currentLib);
                            Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    String place = call.arguments.get(argIndex);
                                    Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                    
                                    if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                        if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newTable, dataInstructions, codeSec, procSec);
                                        if(newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = newTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(currentLib, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(currentLib, program, libraries, place, newTable, dataInstructions, codeSec, procSec);
                                    }

                                    newArg.source = place;
                                    newArg.dest = fetchedProcedure.paramAssign.get(argIndex).place;
                                    newArgs.add(newArg);
                                }

                                if(!placeIsUniqueToProgramOrLibrary(assign.place, program, libraries, currentLib)){
                                    String place = null;    
                                    do{
                                        place = gen.genNext();
                                    } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, currentLib));

                                    replacePlaceInLib(currentLib, assign.place, place);
                                }

                                Call newCall = new Call(call.procedureName, newArgs);
                                if(!instructionExistsInNewProgram(newCall, dataInstructions)){
                                    dataInstructions.addInstruction(newCall);
                                }

                                String toRetFrom = fetchedProcedure.placement.place;
                                String toRetTo = assign.place;
                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                                if(!instructionExistsInNewProgram(newPlace, dataInstructions)){
                                    dataInstructions.addInstruction(newPlace);
                                    if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                        newTable.addEntry(entry);
                                    }
                                }
                            }
                        } else {
                            if(!placeIsUniqueToProgramOrLibrary(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, currentLib));

                                replacePlaceInLib(currentLib, assign.place, place);
                            }

                            if(!instructionExistsInNewProgram(assign, dataInstructions)){
                                dataInstructions.addInstruction(assign);
                                if(libSymbols.containsEntryWithICodePlace(assign.place, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(assign.place, SymEntry.INTERNAL);
                                    newTable.addEntry(entry);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private void fetchInternalProcedure(Prog program, String procName, Lib[] libraries, SymSec symbolTable, DataSec dataSection, CodeSec codeSection, ProcSec procedureSec){
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        ProcSec libProcSec = program.procedures;
        SymSec libSymbols = program.symbols;
        if(libProcSec.containsProcedure(procName) && !procedureSec.containsProcedure(procName)){
            Proc procedure = libProcSec.getProcedureByName(procName);

            for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                Assign assign = procedure.paramAssign.get(assignIndex);

                if(!placeIsUniqueToProgramOrLibrary(assign.value.toString(), program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, program));

                    replacePlaceInProgram(program, assign.value.toString(), place);
                }

                if(!placeIsUniqueToProgramOrLibrary(assign.place, program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, program));

                    replacePlaceInProgram(program, assign.place, place);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueToProgramOrLibrary(placement.value.toString(), program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, program));

                    replacePlaceInProgram(program, placement.value.toString(), place);
                }

                if(!placeIsUniqueToProgramOrLibrary(placement.place, program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, program));

                    replacePlaceInProgram(program, placement.place, place);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                            replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                    }
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueToProgramOrLibrary(assignment.place, program, libraries, program)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueToProgramOrLibrary(newPlace, program, libraries, program));
                        replacePlaceInProgram(program, assignment.place, newPlace);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                                }
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                                }
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                                }
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;
                    
                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                        Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                        int numberOfArgsInCall = call.arguments.size();

                        if(numberOfArgsInCall != numberOfArgsInProc){
                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                        } else {
                            List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                String place = call.arguments.get(argIndex);
                                Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                                    }
                                }

                                newArg.source = place;
                                newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(newArg);
                            }

                            Call newCall = new Call(call.procedureName, newArgs);
                            newProcedure.addInstruction(newCall);
                            Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, assignment.place, new IdentExp(fetchedProcedure.placement.place), procedure.placement.getType());
                            newProcedure.addInstruction(newPlace);

                            continue;
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }

                    if(!labelIsUniqueToProgramOrLibrary(ifStat.ifTrue, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                        replaceLabelInProgram(program, ifStat.ifTrue, newLabel);
                    }

                    if(!labelIsUniqueToProgramOrLibrary(ifStat.ifFalse, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                        replaceLabelInProgram(program, ifStat.ifFalse, newLabel);
                    }
                } else if(icode instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)icode;
                    
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                    Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                    } else {
                        List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            String place = call.arguments.get(argIndex);
                            Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                            if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                                }
                            }

                            newArg.source = place;
                            newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(newArg);
                        }
                        
                        newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(program, call.pname, libraries, symbolTable, dataSection, codeSection, procedureSec);
                    
                    for(Tuple<String, String> arg : call.params){
                        String place = arg.source;

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }
                } else if(icode instanceof Goto){
                    Goto gotoICode = (Goto)icode;

                    if(!labelIsUniqueToProgramOrLibrary(gotoICode.label, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                        replaceLabelInProgram(program, gotoICode.label, newLabel);
                    }
                } else if(icode instanceof Label){
                    Label labelICode = (Label)icode;

                    if(!labelIsUniqueToProgramOrLibrary(labelICode.label, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(labelICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                        replaceLabelInProgram(program, labelICode.label, newLabel);
                    }
                }
                
                newProcedure.addInstruction(icode);
            }
        }
    }

    private void fetchInternalProcedure(Lib library, String procName, Prog prog, Lib[] libraries, SymSec symbolTable, DataSec dataSection, CodeSec codeSection, ProcSec procedureSec){
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        ProcSec libProcSec = library.procedures;
        SymSec libSymbols = library.symbols;
        if(libProcSec.containsProcedure(procName) && !procedureSec.containsProcedure(procName)){
            Proc procedure = libProcSec.getProcedureByName(procName);

            for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                Assign assign = procedure.paramAssign.get(assignIndex);

                if(!placeIsUniqueToProgramOrLibrary(assign.value.toString(), prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                    replacePlaceInLib(library, assign.value.toString(), place);
                }

                if(!placeIsUniqueToProgramOrLibrary(assign.place, prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                    replacePlaceInLib(library, assign.place, place);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.EXTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueToProgramOrLibrary(placement.value.toString(), prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                    replacePlaceInLib(library, placement.value.toString(), place);
                }

                if(!placeIsUniqueToProgramOrLibrary(placement.place, prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                    replacePlaceInLib(library, placement.place, place);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                    }
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueToProgramOrLibrary(assignment.place, prog, libraries, library)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueToProgramOrLibrary(newPlace, prog, libraries, library));
                        replacePlaceInLib(library, assignment.place, newPlace);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, symbolTable, dataSection, codeSection, procedureSec);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, prog, libraries, ident.ident, symbolTable, dataSection, codeSection, procedureSec);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;
                    
                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                        Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                        int numberOfArgsInCall = call.arguments.size();

                        if(numberOfArgsInCall != numberOfArgsInProc){
                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                        } else {
                            List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                String place = call.arguments.get(argIndex);
                                Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, place, symbolTable, dataSection, codeSection, procedureSec);
                                }

                                newArg.source = place;
                                newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(newArg);
                            }

                            Call newCall = new Call(call.procedureName, newArgs);
                            newProcedure.addInstruction(newCall);
                            Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, assignment.place, new IdentExp(fetchedProcedure.placement.place), assignment.getType());
                            newProcedure.addInstruction(newPlace);

                            continue;
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                        }
                    }

                    if(!labelIsUniqueToProgramOrLibrary(ifStat.ifTrue, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                        replaceLabelInLib(library, ifStat.ifTrue, newLabel);
                    }

                    if(!labelIsUniqueToProgramOrLibrary(ifStat.ifFalse, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                        replaceLabelInLib(library, ifStat.ifFalse, newLabel);
                    }
                } else if(icode instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)icode;
                    
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                    Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                    } else {
                        List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            String place = call.arguments.get(argIndex);
                            Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                            if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, prog, libraries, place, symbolTable, dataSection, codeSection, procedureSec);
                            }

                            newArg.source = place;
                            newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(newArg);
                        }
                        
                        newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(library, call.pname, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                    
                    for(Tuple<String, String> arg : call.params){
                        String place = arg.source;

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, prog, libraries, place, symbolTable, dataSection, codeSection, procedureSec);
                        }
                    }
                } else if(icode instanceof Goto){
                    Goto gotoICode = (Goto)icode;

                    if(!labelIsUniqueToProgramOrLibrary(gotoICode.label, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                        replaceLabelInLib(library, gotoICode.label, newLabel);
                    }
                } else if(icode instanceof Label){
                    Label labelICode = (Label)icode;

                    if(!labelIsUniqueToProgramOrLibrary(labelICode.label, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(labelICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                        replaceLabelInLib(library, labelICode.label, newLabel);
                    }
                }
                
                newProcedure.addInstruction(icode);
            }
        }
    }

    private void fetchInternalProcedure(Lib library, String procName, Lib single, Lib[] libraries, SymSec symbolTable, DataSec dataSection, ProcSec procedureSec){
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        ProcSec libProcSec = library.procedures;
        SymSec libSymbols = library.symbols;
        if(libProcSec.containsProcedure(procName)){
            Proc procedure = libProcSec.getProcedureByName(procName);

            for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                Assign assign = procedure.paramAssign.get(assignIndex);

                if(!placeIsUniqueToLibrary(assign.value.toString(), single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueToLibrary(place, single, libraries, library));

                    replacePlaceInLib(library, assign.value.toString(), place);
                }

                if(!placeIsUniqueToLibrary(assign.place, single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToLibrary(place, single, libraries, library));

                    replacePlaceInLib(library, assign.place, place);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null){
                Assign placement = procedure.placement;
                if(!placeIsUniqueToLibrary(placement.place, single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToLibrary(place, single, libraries, library));

                    replacePlaceInLib(library, placement.place, place);
                }

                if(!placeIsUniqueToLibrary(placement.value.toString(), single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueToLibrary(place, single, libraries, library));

                    replacePlaceInLib(library, placement.value.toString(), place);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                    }
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueToLibrary(assignment.place, single, libraries, library)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueToLibrary(newPlace, single, libraries, library));
                        replacePlaceInLib(library, assignment.place, newPlace);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, symbolTable, dataSection, procedureSec);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, single, libraries, ident.ident, symbolTable, dataSection, procedureSec);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, symbolTable, dataSection, procedureSec);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, symbolTable, dataSection, procedureSec);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;

                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, single, libraries, symbolTable, dataSection, procedureSec, library);
                        Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                        int numberOfArgsInCall = call.arguments.size();

                        if(numberOfArgsInCall != numberOfArgsInProc){
                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                        } else {
                            List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                String place = call.arguments.get(argIndex);
                                Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                
                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, place, symbolTable, dataSection, procedureSec);
                                }

                                newArg.source = place;
                                newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(newArg);
                            }
                        
                            newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                            String toRetFrom = fetchedProcedure.placement.place;
                            String toRetTo = assignment.place;
                            newProcedure.addInstruction(new Assign(Assign.Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignment.getType()));
                        }

                        continue;
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, symbolTable, dataSection, procedureSec);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, symbolTable, dataSection, procedureSec);
                        }
                    }

                    if(!labelIsUniqueToLibrary(ifStat.ifTrue, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                        replaceLabelInLib(library, ifStat.ifTrue, newLabel);
                    }

                    if(!labelIsUniqueToLibrary(ifStat.ifFalse, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                        replaceLabelInLib(library, ifStat.ifFalse, newLabel);
                    }
                } else if(icode instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)icode;
                    
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, single, libraries, symbolTable, dataSection, procedureSec, library);
                    Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                    } else {
                        List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            String place = call.arguments.get(argIndex);
                            Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                            if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                }
                            } else {
                                fetchInternalDependentInstructions(library, single, libraries, place, symbolTable, dataSection, procedureSec);
                            }

                            newArg.source = place;
                            newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(newArg);
                        } 
                        
                        newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(library, call.pname, single, libraries, symbolTable, dataSection, procedureSec);

                    for(Tuple<String, String> arg : call.params){
                        String place = arg.source;

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        } else {
                            fetchInternalDependentInstructions(library, single, libraries, place, symbolTable, dataSection, procedureSec);
                        }
                    }
                } else if(icode instanceof Goto){
                    Goto gotoICode = (Goto)icode;

                    if(!labelIsUniqueToLibrary(gotoICode.label, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                        replaceLabelInLib(library, gotoICode.label, newLabel);
                    }
                } else if(icode instanceof Label){
                    Label labelICode = (Label)icode;

                    if(!labelIsUniqueToLibrary(labelICode.label, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(labelICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                        replaceLabelInLib(library, labelICode.label, newLabel);
                    }
                }

                newProcedure.addInstruction(icode);
            }
        }
    }

    private void fetchExternalProcedure(String procName, Prog prog, Lib[] libraries, SymSec symbolTable, DataSec dataSection, CodeSec codeSection, ProcSec procedureSec, Lib... libsToIgnore){
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        loop: for(int i = 0; i < libraries.length; i++){
            Lib library = libraries[i];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                ProcSec libProcSec = library.procedures;
                SymSec libSymbols = library.symbols;
                if(libProcSec.containsProcedure(procName)){
                    Proc procedure = libProcSec.getProcedureByName(procName);

                    for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                        Assign assign = procedure.paramAssign.get(assignIndex);

                        if(!placeIsUniqueToProgramOrLibrary(assign.value.toString(), prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                            replacePlaceInLib(library, assign.value.toString(), place);
                        }

                        if(!placeIsUniqueToProgramOrLibrary(assign.place, prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                            replacePlaceInLib(library, assign.place, place);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueToProgramOrLibrary(placement.value.toString(), prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                            replacePlaceInLib(library, placement.value.toString(), place);
                        }

                        if(!placeIsUniqueToProgramOrLibrary(placement.place, prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueToProgramOrLibrary(place, prog, libraries, library));

                            replacePlaceInLib(library, placement.place, place);
                        }

                        if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);


                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueToProgramOrLibrary(assignment.place, prog, libraries, library)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueToProgramOrLibrary(newPlace, prog, libraries, library));
                                replacePlaceInLib(library, assignment.place, newPlace);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, symbolTable, dataSection, codeSection, procedureSec);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, prog, libraries, ident.ident, symbolTable, dataSection, codeSection, procedureSec);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                                    }
                                }
                            } else if(assignExp instanceof ExternalCall){
                                ExternalCall call = (ExternalCall)assignExp;
                            
                                if(!procedureSec.containsProcedure(call.procedureName))
                                    fetchExternalProcedure(call.procedureName, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                                int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                int numberOfArgsInCall = call.arguments.size();

                                if(numberOfArgsInCall != numberOfArgsInProc){
                                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                                } else {
                                    List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                        String place = call.arguments.get(argIndex);
                                        Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                    
                                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                            }
                                        } else {
                                            fetchInternalDependentInstructions(library, prog, libraries, place, symbolTable, dataSection, codeSection, procedureSec);
                                        }

                                        newArg.source = place;
                                        newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                        newArgs.add(newArg);
                                    }

                                    Call newCall = new Call(call.procedureName, newArgs);
                                    newProcedure.addInstruction(newCall);
                                    Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, assignment.place, new IdentExp(fetchedProcedure.placement.place), assignment.getType());
                                    newProcedure.addInstruction(newPlace);

                                    continue;
                                }
                            }
                        } else if(icode instanceof If){
                            If ifStat = (If)icode;

                            BinExp exp = ifStat.exp;
                            if(exp.left instanceof IdentExp){
                                IdentExp leftExp = (IdentExp)exp.left;
                                if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, symbolTable, dataSection, codeSection, procedureSec);
                                }
                            }

                            if(!labelIsUniqueToProgramOrLibrary(ifStat.ifTrue, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                                replaceLabelInLib(library, ifStat.ifTrue, newLabel);
                            }

                            if(!labelIsUniqueToProgramOrLibrary(ifStat.ifFalse, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                                replaceLabelInLib(library, ifStat.ifFalse, newLabel);
                            }
                        } else if(icode instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)icode;
                            
                            if(!procedureSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                            Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    String place = call.arguments.get(argIndex);
                                    Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                    
                                    if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, prog, libraries, place, symbolTable, dataSection, codeSection, procedureSec);
                                    }

                                    newArg.source = place;
                                    newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(newArg);
                                }
                                
                                newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                                continue;
                            }
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname))
                                fetchInternalProcedure(library, call.pname, prog, libraries, symbolTable, dataSection, codeSection, procedureSec);

                            for(Tuple<String, String> arg : call.params){
                                String place = arg.source;

                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, symbolTable, dataSection, codeSection, procedureSec, library);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, place, symbolTable, dataSection, codeSection, procedureSec);
                                }
                            }
                        } else if(icode instanceof Goto){
                            Goto gotoICode = (Goto)icode;

                            if(!labelIsUniqueToProgramOrLibrary(gotoICode.label, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                                replaceLabelInLib(library, gotoICode.label, newLabel);
                            }
                        } else if(icode instanceof Label){
                            Label labelICode = (Label)icode;

                            if(!labelIsUniqueToProgramOrLibrary(labelICode.label, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(labelICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToProgramOrLibrary(newLabel, prog, libraries, library));
                                replaceLabelInLib(library, labelICode.label, newLabel);
                            }
                        }
                        newProcedure.addInstruction(icode);
                    }
                    break loop;
                }
            }
        }
    }

    private void fetchExternalProcedure(String procName, Lib single, Lib[] libraries, SymSec symbolTable, DataSec dataSection, ProcSec procedureSec, Lib... libsToIgnore){
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        loop: for(int i = 0; i < libraries.length; i++){
            Lib library = libraries[i];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                ProcSec libProcSec = library.procedures;
                SymSec libSymbols = library.symbols;
                if(libProcSec.containsProcedure(procName)){
                    Proc procedure = libProcSec.getProcedureByName(procName);

                    for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                        Assign assign = procedure.paramAssign.get(assignIndex);

                        if(!placeIsUniqueToLibrary(assign.value.toString(), single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueToLibrary(place, single, libraries, library));

                            replacePlaceInLib(library, assign.value.toString(), place);
                        }

                        if(!placeIsUniqueToLibrary(assign.place, single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueToLibrary(place, single, libraries, library));

                            replacePlaceInLib(library, assign.place, place);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueToLibrary(placement.value.toString(), single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueToLibrary(place, single, libraries, library));

                            replacePlaceInLib(library, placement.value.toString(), place);
                        }

                        if(!placeIsUniqueToLibrary(placement.place, single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueToLibrary(place, single, libraries, library));

                            replacePlaceInLib(library, placement.place, place);
                        }

                        if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                            }
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);

                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueToLibrary(assignment.place, single, libraries, library)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueToLibrary(newPlace, single, libraries, library));
                                replacePlaceInLib(library, assignment.place, newPlace);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, ident.ident, symbolTable, dataSection, procedureSec);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, ident.ident, symbolTable, dataSection, procedureSec);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, symbolTable, dataSection, procedureSec);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, symbolTable, dataSection, procedureSec);
                                    }
                                }
                            } else if(assignExp instanceof ExternalCall){
                                ExternalCall call = (ExternalCall)assignExp;

                                if(!procedureSec.containsProcedure(call.procedureName))
                                    fetchExternalProcedure(call.procedureName, single, libraries, symbolTable, dataSection, procedureSec, library);
                                Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                                int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                int numberOfArgsInCall = call.arguments.size();

                                if(numberOfArgsInCall != numberOfArgsInProc){
                                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                                } else {
                                    List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                        String place = call.arguments.get(argIndex);
                                        Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                        
                                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                    replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                            }
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, place, symbolTable, dataSection, procedureSec);
                                        }

                                        newArg.source = place;
                                        newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                        newArgs.add(newArg);
                                    }
                                
                                    newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                                    String toRetFrom = fetchedProcedure.placement.place;
                                    String toRetTo = assignment.place;
                                    newProcedure.addInstruction(new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignment.getType()));
                                }

                                continue;
                            }
                        } else if(icode instanceof If){
                            If ifStat = (If)icode;

                            BinExp exp = ifStat.exp;
                            if(exp.left instanceof IdentExp){
                                IdentExp leftExp = (IdentExp)exp.left;
                                if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, symbolTable, dataSection, procedureSec);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, symbolTable, dataSection, procedureSec);
                                }
                            }

                            if(!labelIsUniqueToLibrary(ifStat.ifTrue, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                                replaceLabelInLib(library, ifStat.ifTrue, newLabel);
                            }

                            if(!labelIsUniqueToLibrary(ifStat.ifFalse, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                                replaceLabelInLib(library, ifStat.ifFalse, newLabel);
                            }
                        } else if(icode instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)icode;
                            
                            if(!procedureSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, single, libraries, symbolTable, dataSection, procedureSec, library);
                            Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    String place = call.arguments.get(argIndex);
                                    Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                    
                                    if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                                replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                        }
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, place, symbolTable, dataSection, procedureSec);
                                    }

                                    newArg.source = place;
                                    newArg.dest = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(newArg);
                                }
                                
                                newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                                continue;
                            }
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname))
                                fetchInternalProcedure(library, call.pname, single, libraries, symbolTable, dataSection, procedureSec);

                            for(Tuple<String, String> arg : call.params){
                                String place = arg.source;

                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, symbolTable, dataSection, procedureSec, library);
                                    if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                        SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                        if(!entry.icodePlace.equals(newEntry.icodePlace))
                                            replacePlaceInLib(library, entry.icodePlace, newEntry.icodePlace);
                                    }
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, place, symbolTable, dataSection, procedureSec);
                                }
                            }
                        } else if(icode instanceof Goto){
                            Goto gotoICode = (Goto)icode;

                            if(!labelIsUniqueToLibrary(gotoICode.label, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                                replaceLabelInLib(library, gotoICode.label, newLabel);
                            }
                        } else if(icode instanceof Label){
                            Label labelICode = (Label)icode;

                            if(!labelIsUniqueToLibrary(labelICode.label, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(labelICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueToLibrary(newLabel, single, libraries, library));
                                replaceLabelInLib(library, labelICode.label, newLabel);
                            }
                        }

                        newProcedure.addInstruction(icode);
                    }
                    break loop;
                }
            }
        }
    }

    private static void replaceLabelInICode(ICode icode, String oldLabel, String newLabel){
        if(icode instanceof Label){
            Label label = (Label)icode;
            if(label.label.equals(oldLabel))
                label.label = newLabel;
        } else if(icode instanceof Goto){
            Goto gotoLabel = (Goto)icode;
            if(gotoLabel.label.equals(oldLabel))
                gotoLabel.label = newLabel;
        } else if(icode instanceof If){
            If ifLabel = (If)icode;
            if(ifLabel.ifTrue.equals(oldLabel))
                ifLabel.ifTrue = newLabel;

            if(ifLabel.ifFalse.equals(oldLabel))
                ifLabel.ifFalse = newLabel;
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
            } else if(exp instanceof ExternalCall){
                ExternalCall call = (ExternalCall)exp;

                List<String> newArgs = new LinkedList<String>();
                for(String arg : call.arguments){
                    if(arg.equals(oldPlace))
                        newArgs.add(newPlace);
                    else
                        newArgs.add(arg);
                }

                call.arguments = newArgs;
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

    private static void replaceLabelInLib(Lib library, String oldPlace, String newPlace){
        ProcSec procSection = library.procedures;
        for(Proc procedure : procSection.procedures){
            replaceLabelInProcedure(procedure, oldPlace, newPlace);
        }
    }

    private static void replaceLabelInProcedure(Proc proc, String oldLabel, String newLabel){
        for(ICode icode : proc.instructions)
            replaceLabelInICode(icode, oldLabel, newLabel);
    }

    private static void replacePlaceInProcedure(Proc proc, String oldPlace, String newPlace){
        for(Assign assign : proc.paramAssign)
            replacePlaceInICode(assign, oldPlace, newPlace);
        for(ICode icode : proc.instructions)
            replacePlaceInICode(icode, oldPlace, newPlace);
        if(proc.placement != null)
            replacePlaceInICode(proc.placement, oldPlace, newPlace);
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
            replacePlaceInProcedure(proc, oldPlace, newPlace);
        }
    }

    private static void replaceLabelInProgram(Prog program, String oldLabel, String newLabel){
        replaceLabelInLib(program, oldLabel, newLabel);

        CodeSec cSec = program.code;
        for(int i = 0; i < cSec.getLength(); i++){
            ICode instruction = cSec.getInstruction(i);
            replaceLabelInICode(instruction, oldLabel, newLabel);
        }
    }

    private static void replacePlaceInProgram(Prog program, String oldPlace, String newPlace){
        replacePlaceInLib(program, oldPlace, newPlace);

        CodeSec cSec = program.code;
        for(int i = 0; i < cSec.getLength(); i++){
            ICode instruction = cSec.getInstruction(i);
            replacePlaceInICode(instruction, oldPlace, newPlace);
        }
    }

    private static boolean placeIsUniqueToProgramOrLibrary(String place, Prog program, Lib[] libraries, Lib libraryToIgnore){
        if(!program.equals(libraryToIgnore))
            if(Utils.placeExistsInProgram(place, program))
                return false;

        for(Lib library : libraries){
            if(!library.equals(libraryToIgnore))
                if(Utils.placeExistsInLibrary(place, library))
                    return false;
        }

        return true;
    }

    private static boolean labelIsUniqueToProgramOrLibrary(String label, Prog program, Lib[] libraries, Lib libraryToIgnore){
        if(!program.equals(libraryToIgnore)){
            if(Utils.labelExistsInProgram(label, program))
                return false;
        }

        for(Lib library : libraries){
            if(!library.equals(libraryToIgnore)){
                if(Utils.labelExistsInLibrary(label, library))
                    return false;
            }
        }

        return true;
    }

    private static boolean placeIsUniqueToLibrary(String place, Lib library, Lib[] libraries, Lib libToIgnore){
        if(!library.equals(libToIgnore))
            if(Utils.placeExistsInLibrary(place, library))
                return false;

        for(Lib lib : libraries){
            if(!lib.equals(libToIgnore))
                if(Utils.placeExistsInLibrary(place, lib))
                    return false;
        }

        return true;
    }

    private static boolean labelIsUniqueToLibrary(String label, Lib library, Lib[] libraries, Lib libToIgnore){
        if(!library.equals(libToIgnore)){
            if(Utils.labelExistsInLibrary(label, library))
                return false;
        }

        for(Lib lib : libraries){
            if(!lib.equals(libToIgnore)){
                if(Utils.labelExistsInLibrary(label, lib))
                    return false;
            }
        }

        return true;
    }

    private static boolean instructionExistsInNewProgram(ICode codeToSearch, DataSec dataSec){
        for(int i = 0; i < dataSec.getLength(); i++){
            ICode icode = dataSec.getInstruction(i);
            if(icode.equals(codeToSearch))
                return true;
        }

        return false;
    }

    private void linkDataSections(Prog startingProgram, Lib[] libraries, SymSec symbolTable, DataSec dataSec, CodeSec codeSec, ProcSec procedures){
        SymSec programSymbolTable = startingProgram.symbols;
        DataSec programDataSec = startingProgram.variables;
        for(int i = 0; i < programDataSec.getLength(); i++){
            ICode instruction = programDataSec.getInstruction(i);
            if(instruction instanceof Assign){
                Assign assign = (Assign)instruction;
                Exp assignExp = assign.value;
                if(assignExp instanceof BinExp){
                    BinExp assignBinExp = (BinExp)assignExp;

                    if(assignBinExp.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)assignBinExp.left;
                        if(programSymbolTable.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(startingProgram, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(startingProgram, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(startingProgram, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                replacePlaceInProgram(startingProgram, entry.icodePlace, newEntry.icodePlace);
                        }
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;
                
                    if(!placeIsUniqueToProgramOrLibrary(assign.place, startingProgram, libraries, startingProgram)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueToProgramOrLibrary(place, startingProgram, libraries, startingProgram));

                        replacePlaceInProgram(startingProgram, assign.place, place);
                    }
                
                    if(!procedures.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                    Proc procedure = procedures.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = procedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                    } else {
                        List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            String place = call.arguments.get(argIndex);
                            Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                            if(programSymbolTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                SymEntry entry = programSymbolTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInProgram(startingProgram, entry.icodePlace, newEntry.icodePlace);
                                }
                            }

                            newArg.source = place;
                            newArg.dest = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(newArg);
                        }

                        Call newCall = new Call(call.procedureName, newArgs);

                        if(!instructionExistsInNewProgram(newCall, dataSec)){
                            dataSec.addInstruction(newCall);
                        }

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assign.place;
                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                        if(!instructionExistsInNewProgram(newPlace, dataSec)){
                            dataSec.addInstruction(newPlace);
                        }

                        continue;
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof ExternalCall){
                ExternalCall call = (ExternalCall)instruction;
                
                if(!procedures.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                Proc procedure = procedures.getProcedureByName(call.procedureName);

                int numberOfArgsInProc = procedure.paramAssign.size();
                int numberOfArgsInCall = call.arguments.size();

                if(numberOfArgsInCall != numberOfArgsInProc){
                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                } else {
                    List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                        String place = call.arguments.get(argIndex);
                        Tuple<String, String> newArg = new Tuple<String,String>("", "");
                        
                        if(programSymbolTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, symbolTable, dataSec, codeSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(startingProgram, entry.icodePlace, newEntry.icodePlace);
                            }
                        }

                        newArg.source = place;
                        newArg.dest = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(newArg);
                    }

                    Call newCall = new Call(call.procedureName, newArgs);

                    if(!instructionExistsInNewProgram(newCall, dataSec)){
                        dataSec.addInstruction(newCall);
                    }

                    continue;
                }
            }
        }
    }

    private void linkDataSections(Lib startingLibrary, Lib[] libraries, SymSec symbolTable, DataSec dataSec, ProcSec procedures){
        SymSec programSymbolTable = startingLibrary.symbols;
        DataSec programDataSec = startingLibrary.variables;
        for(int i = 0; i < programDataSec.getLength(); i++){
            ICode instruction = programDataSec.getInstruction(i);
            if(instruction instanceof Assign){
                Assign assign = (Assign)instruction;
                Exp assignExp = assign.value;
                if(assignExp instanceof BinExp){
                    BinExp assignBinExp = (BinExp)assignExp;

                    if(assignBinExp.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)assignBinExp.left;
                        if(programSymbolTable.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, symbolTable, dataSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(startingLibrary, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, symbolTable, dataSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(startingLibrary, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, symbolTable, dataSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(startingLibrary, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, symbolTable, dataSec, procedures);
                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                replacePlaceInLib(startingLibrary, entry.icodePlace, newEntry.icodePlace);
                        }
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;
                
                    if(!placeIsUniqueToLibrary(assign.place, startingLibrary, libraries, startingLibrary)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueToLibrary(place, startingLibrary, libraries, startingLibrary));

                        replacePlaceInLib(startingLibrary, assign.place, place);
                    }
                
                    if(!procedures.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, startingLibrary, libraries, symbolTable, dataSec, procedures);
                    Proc procedure = procedures.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = procedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                    } else {
                        List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            String place = call.arguments.get(argIndex);
                            Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                            if(programSymbolTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                SymEntry entry = programSymbolTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, symbolTable, dataSec, procedures);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInLib(startingLibrary, entry.icodePlace, newEntry.icodePlace);
                                }
                            }

                            newArg.source = place;
                            newArg.dest = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(newArg);
                        }

                        Call newCall = new Call(call.procedureName, newArgs);

                        if(!instructionExistsInNewProgram(newCall, dataSec)){
                            dataSec.addInstruction(newCall);
                        }

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assign.place;
                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                        if(!instructionExistsInNewProgram(newPlace, dataSec)){
                            dataSec.addInstruction(newPlace);
                        }

                        continue;
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof ExternalCall){
                ExternalCall call = (ExternalCall)instruction;
                
                if(!procedures.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, startingLibrary, libraries, symbolTable, dataSec, procedures);
                Proc procedure = procedures.getProcedureByName(call.procedureName);

                int numberOfArgsInProc = procedure.paramAssign.size();
                int numberOfArgsInCall = call.arguments.size();

                if(numberOfArgsInCall != numberOfArgsInProc){
                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                } else {
                    List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                        String place = call.arguments.get(argIndex);
                        Tuple<String, String> newArg = new Tuple<String,String>("", "");
                        
                        if(programSymbolTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, symbolTable, dataSec, procedures);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInLib(startingLibrary, entry.icodePlace, newEntry.icodePlace);
                            }
                        }

                        newArg.source = place;
                        newArg.dest = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(newArg);
                    }

                    Call newCall = new Call(call.procedureName, newArgs);

                    if(!instructionExistsInNewProgram(newCall, dataSec)){
                        dataSec.addInstruction(newCall);
                    }

                    continue;
                }
            }
        }
    }

    private void linkCodeSection(Prog program, Lib[] libraries, SymSec symbolTable, DataSec dataSection, CodeSec codeSection, ProcSec procedureSec){
        SymSec programTable = program.symbols;
        CodeSec codeSec = program.code;

        for(int i = 0; i < codeSec.getLength(); i++){
            ICode icode = codeSec.getInstruction(i);
            if(icode instanceof Assign){
                Assign assignment = (Assign)icode;
                Exp assignExp = assignment.value;

                if(assignExp instanceof IdentExp){
                    IdentExp ident = (IdentExp)assignExp;
                    if(programTable.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp unExp = (UnExp)assignExp;
                    
                    if(unExp.right instanceof IdentExp){
                        IdentExp ident = (IdentExp)unExp.right;
                        if(programTable.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    if(binExp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)binExp.left;
                        if(programTable.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }

                    if(binExp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)binExp.right;
                        if(programTable.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;

                    if(!placeIsUniqueToProgramOrLibrary(assignment.place, program, libraries, program)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueToProgramOrLibrary(place, program, libraries, program));

                        replacePlaceInProgram(program, assignment.place, place);
                    }
                
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                    Proc procedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = procedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                    } else {
                        List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            String place = call.arguments.get(argIndex);
                            Tuple<String, String> newArg = new Tuple<String,String>("", "");
                            
                            if(programTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                SymEntry entry = programTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                                if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                    SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                    if(!entry.icodePlace.equals(newEntry.icodePlace))
                                        replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                                }
                            }

                            newArg.source = place;
                            newArg.dest = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(newArg);
                        }
                        codeSection.addInstruction(new Call(call.procedureName, newArgs));

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assignment.place;
                        codeSection.addInstruction(new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignment.getType()));
                        continue;
                    }
                    codeSection.addInstruction(assignment);
                }
            } else if(icode instanceof If){
                If ifStat = (If)icode;

                BinExp exp = ifStat.exp;
                if(exp.left instanceof IdentExp){
                    IdentExp leftExp = (IdentExp)exp.left;
                    if(programTable.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                        }
                    }
                }

                if(exp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)exp.right;
                    if(programTable.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                        }
                    }
                }

                if(!labelIsUniqueToProgramOrLibrary(ifStat.ifTrue, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                    replaceLabelInProgram(program, ifStat.ifTrue, newLabel);
                }

                if(!labelIsUniqueToProgramOrLibrary(ifStat.ifFalse, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                    replaceLabelInProgram(program, ifStat.ifFalse, newLabel);
                }
            } else if(icode instanceof ExternalCall){
                ExternalCall call = (ExternalCall)icode;
                
                if(!procedureSec.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, program, libraries, symbolTable, dataSection, codeSection, procedureSec);
                Proc procedure = procedureSec.getProcedureByName(call.procedureName);

                int numberOfArgsInProc = procedure.paramAssign.size();
                int numberOfArgsInCall = call.arguments.size();

                if(numberOfArgsInCall != numberOfArgsInProc){
                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                } else {
                    List<Tuple<String, String>> newArgs = new LinkedList<Tuple<String, String>>();
                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                        String place = call.arguments.get(argIndex);
                        Tuple<String, String> newArg = new Tuple<String,String>("", "");
                        
                        if(programTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                            if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                                SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                                if(!entry.icodePlace.equals(newEntry.icodePlace))
                                    replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                            }
                        }

                        newArg.source = place;
                        newArg.dest = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(newArg);
                    }
                    codeSection.addInstruction(new Call(call.procedureName, newArgs));
                    continue;
                }
            } else if(icode instanceof Call){
                Call call = (Call)icode;
                if(!procedureSec.containsProcedure(call.pname))
                    fetchInternalProcedure(program, call.pname, libraries, symbolTable, dataSection, codeSection, procedureSec);

                for(Tuple<String, String> arg : call.params){
                    String place = arg.source;

                    if(programTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, symbolTable, dataSection, codeSec, procedureSec);
                        if(symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL)){
                            SymEntry newEntry = symbolTable.getEntryByIdentifier(entry.declanIdent, SymEntry.INTERNAL);
                            if(!entry.icodePlace.equals(newEntry.icodePlace))
                                replacePlaceInProgram(program, entry.icodePlace, newEntry.icodePlace);
                        }
                    }
                }
            } else if(icode instanceof Goto){
                Goto gotoICode = (Goto)icode;
                if(!labelIsUniqueToProgramOrLibrary(gotoICode.label, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                    replaceLabelInProgram(program, gotoICode.label, newLabel);
                }
            } else if(icode instanceof Label){
                Label labelICode = (Label)icode;
                if(!labelIsUniqueToProgramOrLibrary(labelICode.label, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(labelICode.label);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueToProgramOrLibrary(newLabel, program, libraries, program));
                    replaceLabelInProgram(program, labelICode.label, newLabel);
                }
            }

            codeSection.addInstruction(icode);
        }
    }

    private void linkProcedureSections(Lib library, Lib[] libraries, SymSec symbolTable, DataSec variables, ProcSec procedures){
        ProcSec libraryProcSec = library.procedures;
        for(int i = 0; i < libraryProcSec.getLength(); i++){
            Proc procedure = libraryProcSec.getProcedureByIndex(i);
            if(!procedures.containsProcedure(procedure.label.label))
                fetchInternalProcedure(library, procedure.label.label, library, libraries, symbolTable, variables, procedures);
        }

        for(Lib lib : libraries){
            ProcSec libProcSec = lib.procedures;
            for(int i = 0; i < libProcSec.getLength(); i++){
                Proc procedure = libProcSec.getProcedureByIndex(i);
                if(!procedures.containsProcedure(procedure.label.label))
                    fetchInternalProcedure(lib, procedure.label.label, library, libraries, symbolTable, variables, procedures);
            }
        }
    }

    public Prog performLinkage(Prog program, Lib... libraries){
        SymSec symbolTable = new SymSec();
        DataSec dataSec = new DataSec();
        CodeSec codeSec = new CodeSec();
        ProcSec procSec = new ProcSec();
        linkDataSections(program, libraries, symbolTable, dataSec, codeSec, procSec);
        linkCodeSection(program, libraries, symbolTable, dataSec, codeSec, procSec);
        return new Prog(symbolTable, dataSec, codeSec, procSec);
    }

    public Prog performLinkage(Program prog, Library... libraries){
        Prog generatedProgram = generateProgram(errLog, prog);
        Lib[] libs = generateLibraries(errLog, libraries);
        return performLinkage(generatedProgram, libs);
    }

    public Lib performLinkage(Lib library, Lib... libraries){
        SymSec symbolTable = new SymSec();
        DataSec dataSec = new DataSec();
        ProcSec procSec = new ProcSec();
        linkDataSections(library, libraries, symbolTable, dataSec, procSec);
        linkProcedureSections(library, libraries, symbolTable, dataSec, procSec);
        return new Lib(symbolTable, dataSec, procSec);
    }

    public Lib performLinkage(Library library, Library... libraries){
        Lib lib = generateLibrary(errLog, library);
        Lib[] libs = generateLibraries(errLog, libraries);
        return performLinkage(lib, libs);
    }
}
