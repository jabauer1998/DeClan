package io.github.H20man13.DeClan.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
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
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
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

    private void fetchExternalDependentInstructions(String identName, Prog program, Lib[] libraries, Prog newProgram, Map<Lib, List<ICode>> addedInstructions, Lib... libsToIgnore){
        ProcSec procSec = newProgram.procedures;
        SymSec newTable = newProgram.symbols;
        DataSec dataInstructions = newProgram.variables;

        loop: for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                SymSec libSymbols = library.symbols;
                if(libSymbols.containsEntryWithIdentifier(identName, SymEntry.INTERNAL)){
                    List<SymEntry> libEntries = libSymbols.getEntriesByIdentifier(identName, SymEntry.INTERNAL);
                    if(libEntries.size() > 0){
                        SymEntry libEntry = libEntries.get(0);
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
                                                fetchInternalProcedure(library, funcCall.pname, program, libraries, newProgram, addedInstructions);

                                            int numArgs = funcCall.params.size();
                                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                                Assign arg = funcCall.params.get(argIndex);
                                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, arg.value.toString(), newProgram, addedInstructions);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, addedInstructions)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries, addedInstructions));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, newPlace, program, libraries, library, addedInstructions);
                                            }

                                            if(!newProgram.dataSectionContainsInstruction(funcCall) && !newProgram.dataSectionContainsInstruction(assignLib)){
                                                dataInstructions.addInstruction(funcCall);
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
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram, addedInstructions);
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library, addedInstructions);
                                            }

                                            if(!newProgram.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof UnExp){
                                            UnExp unary = (UnExp)exp;
                                            if(unary.right instanceof IdentExp){
                                                IdentExp identExp = (IdentExp)unary.right;
                                                if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram, addedInstructions);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library, addedInstructions);
                                            }

                                            if(!newProgram.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof BinExp){
                                            BinExp binary = (BinExp)exp;

                                            if(binary.left instanceof IdentExp){
                                                IdentExp leftIdent = (IdentExp)binary.left;
                                                if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, leftIdent.ident, newProgram, addedInstructions);
                                                }
                                            }

                                            if(binary.right instanceof IdentExp){
                                                IdentExp rightIdent = (IdentExp)binary.right;
                                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, rightIdent.ident, newProgram, addedInstructions);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library, addedInstructions);
                                            }

                                            if(!newProgram.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof ExternalCall){
                                            ExternalCall call = (ExternalCall)exp;
                                            if(!procSec.containsProcedure(call.procedureName))
                                                fetchExternalProcedure(call.procedureName, program, libraries, newProgram, addedInstructions, library);
                                            Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                            int numberOfArgsInCall = call.arguments.size();

                                            if(numberOfArgsInCall != numberOfArgsInProc){
                                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(libIndex, 0));
                                            } else {
                                                List<Assign> newArgs = new LinkedList<Assign>();
                                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                                    Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                                    
                                                    if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                                        SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                                        if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, library);
                                                    } else {
                                                        fetchInternalDependentInstructions(library, program, libraries, value.source, newProgram, addedInstructions);
                                                    }

                                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                                }

                                                Call newCall = new Call(call.procedureName, newArgs);
                                                String toRetFrom = fetchedProcedure.placement.place;
                                                String toRetTo = assignLib.place;
                                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignLib.getType());

                                                if(!newProgram.dataSectionContainsInstruction(newCall) && !newProgram.dataSectionContainsInstruction(newPlace)){
                                                    List<ICode> addList = addedInstructions.get(library);
                                                    dataInstructions.addInstruction(newCall);
                                                    dataInstructions.addInstruction(newPlace);
                                                    addList.add(newCall);
                                                    addList.add(newPlace);
                                                }
                                                if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                    newTable.addEntry(libEntry);

                                                if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, addedInstructions)){
                                                    String place = null;    
                                                    do{
                                                        place = gen.genNext();
                                                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                                    replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library, addedInstructions);
                                                }
                                            }
                                        } else {
                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library, addedInstructions);
                                            }

                                            if(!newProgram.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
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
    }

    private void fetchExternalDependentInstructions(String identName, Lib single, Lib[] libraries, Lib newLib, Map<Lib, List<ICode>> addedInstructions, Lib... libsToIgnore){
        SymSec newTable = newLib.symbols;
        DataSec dataInstructions = newLib.variables;
        ProcSec procSec = newLib.procedures;
        loop: for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                SymSec libSymbols = library.symbols;
                if(libSymbols.containsEntryWithIdentifier(identName, SymEntry.INTERNAL)){
                    List<SymEntry> libEntries = libSymbols.getEntriesByIdentifier(identName, SymEntry.INTERNAL);
                    if(libEntries.size() > 0){
                        SymEntry libEntry = libEntries.get(0);
                        DataSec libData = library.variables;
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
                                                fetchInternalProcedure(library, funcCall.pname, single, libraries, newLib, addedInstructions);

                                            int numArgs = funcCall.params.size();
                                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                                Assign arg = funcCall.params.get(argIndex);
                                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, arg.value.toString(), newLib, addedInstructions);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, addedInstructions)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, addedInstructions));

                                                replacePlaceAcrossLibraries(assignLib.place, newPlace, single, libraries, library, addedInstructions);
                                            }

                                            if(!newLib.dataSectionContainsInstruction(funcCall) && !newLib.dataSectionContainsInstruction(assignLib)){
                                                dataInstructions.addInstruction(funcCall);
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
                                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib, addedInstructions);
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library, addedInstructions);
                                            }

                                            if(!newLib.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof UnExp){
                                            UnExp unary = (UnExp)exp;
                                            if(unary.right instanceof IdentExp){
                                                IdentExp identExp = (IdentExp)unary.right;
                                                if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib, addedInstructions);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                                
                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library, addedInstructions);
                                            }

                                            if(!newLib.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof BinExp){
                                            BinExp binary = (BinExp)exp;

                                            if(binary.left instanceof IdentExp){
                                                IdentExp leftIdent = (IdentExp)binary.left;
                                                if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, leftIdent.ident, newLib, addedInstructions);
                                                }
                                            }

                                            if(binary.right instanceof IdentExp){
                                                IdentExp rightIdent = (IdentExp)binary.right;
                                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, rightIdent.ident, newLib, addedInstructions);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                                
                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library, addedInstructions);
                                            }

                                            if(!newLib.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof ExternalCall){
                                            ExternalCall call = (ExternalCall)exp;
                                            if(!procSec.containsProcedure(call.procedureName))
                                                fetchExternalProcedure(call.procedureName, single, libraries, newLib, addedInstructions, library);
                                            Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                            int numberOfArgsInCall = call.arguments.size();

                                            if(numberOfArgsInCall != numberOfArgsInProc){
                                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(libIndex, 0));
                                            } else {
                                                List<Assign> newArgs = new LinkedList<Assign>();
                                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                                    Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                                    
                                                    if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                                        SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                                        if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                                    } else {
                                                        fetchInternalDependentInstructions(library, single, libraries, value.source, newLib, addedInstructions);
                                                    }

                                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                                }

                                                Call newCall = new Call(call.procedureName, newArgs);
                                                String toRetFrom = fetchedProcedure.placement.place;
                                                String toRetTo = assignLib.place;
                                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignLib.getType());

                                                if(!newLib.dataSectionContainsInstruction(newCall) && !newLib.dataSectionContainsInstruction(newPlace)){
                                                    List<ICode> addList = addedInstructions.get(library);
                                                    dataInstructions.addInstruction(newCall);
                                                    dataInstructions.addInstruction(newPlace);
                                                    addList.add(newCall);
                                                    addList.add(newPlace);
                                                }
                                                if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                    newTable.addEntry(libEntry);

                                                if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, addedInstructions)){
                                                    String place = null;    
                                                    do{
                                                        place = gen.genNext();
                                                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                                    
                                                    replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library, addedInstructions);
                                                }
                                            }
                                        } else {
                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, addedInstructions)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                                
                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library, addedInstructions);
                                            }

                                            if(!newLib.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
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
    }

    private void fetchInternalDependentInstructions(Lib currentLib, Lib single, Lib[] libraries, String labelName, Lib newLib, Map<Lib, List<ICode>> addedInstructions){
        DataSec data = currentLib.variables;
        SymSec libSymbols = currentLib.symbols;
        ProcSec procSec = newLib.procedures;
        SymSec newTable = newLib.symbols;
        DataSec dataInstructions = newLib.variables;
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
                                fetchInternalProcedure(currentLib, funcCall.pname, single, libraries, newLib, addedInstructions);

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Assign arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, arg.value.toString(), newLib, addedInstructions);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, addedInstructions));

                                replacePlaceAcrossLibraries(assign.place, newPlace, single, libraries, currentLib, addedInstructions);
                            }

                            if(!newLib.dataSectionContainsInstruction(funcCall) && !newLib.dataSectionContainsInstruction(assign)){
                                dataInstructions.addInstruction(funcCall);
                                dataInstructions.addInstruction(assign);
                            }
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        }
                    } else {
                        Exp exp = assign.value;
                        if(exp instanceof IdentExp){
                            IdentExp identExp = (IdentExp)exp;
                            if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib, addedInstructions);
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib, addedInstructions);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof UnExp){
                            UnExp unary = (UnExp)exp;
                            if(unary.right instanceof IdentExp){
                                IdentExp identExp = (IdentExp)unary.right;
                                if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib, addedInstructions);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib, addedInstructions);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof BinExp){
                            BinExp binary = (BinExp)exp;

                            if(binary.left instanceof IdentExp){
                                IdentExp leftIdent = (IdentExp)binary.left;
                                if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, leftIdent.ident, newLib, addedInstructions);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, rightIdent.ident, newLib, addedInstructions);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib, addedInstructions);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)exp;

                            if(!procSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, single, libraries, newLib, addedInstructions, currentLib);
                            Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Assign> newArgs = new LinkedList<Assign>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                    Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                    
                                    if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                        if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, currentLib);
                                    } else {
                                        fetchInternalDependentInstructions(currentLib, single, libraries, value.source, newLib, addedInstructions);
                                    }

                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }

                                Call newCall = new Call(call.procedureName, newArgs);
                                String toRetFrom = fetchedProcedure.placement.place;
                                String toRetTo = assign.place;
                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());

                                if(!newLib.dataSectionContainsInstruction(newCall) && !newLib.dataSectionContainsInstruction(newPlace)){
                                    List<ICode> addList = addedInstructions.get(currentLib);
                                    dataInstructions.addInstruction(newCall);
                                    dataInstructions.addInstruction(newPlace);
                                    addList.add(newCall);
                                    addList.add(newPlace);
                                }
                                
                                if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        newTable.addEntry(entry);
                                }

                                if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                                    String place = null;    
                                    do{
                                        place = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                    
                                    replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib, addedInstructions);
                                }
                            }
                        } else {
                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib, addedInstructions);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

     private void fetchInternalDependentInstructions(Lib currentLib, Prog program, Lib[] libraries, String labelName, Prog newProgram, Map<Lib, List<ICode>> addedInstructions){
        DataSec data = currentLib.variables;
        SymSec libSymbols = currentLib.symbols;
        ProcSec procSec = newProgram.procedures;
        SymSec newTable = newProgram.symbols;
        DataSec dataInstructions = newProgram.variables;
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
                                fetchInternalProcedure(currentLib, funcCall.pname, program, libraries, newProgram, addedInstructions);

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Assign arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, arg.value.toString(), newProgram, addedInstructions);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, addedInstructions)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries, addedInstructions));

                                replacePlaceAcrossProgramAndLibraries(assign.place, newPlace, program, libraries, currentLib, addedInstructions);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        }
                    } else {
                        Exp exp = assign.value;
                        if(exp instanceof IdentExp){
                            IdentExp identExp = (IdentExp)exp;
                            if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram, addedInstructions);
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib, addedInstructions);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof UnExp){
                            UnExp unary = (UnExp)exp;
                            if(unary.right instanceof IdentExp){
                                IdentExp identExp = (IdentExp)unary.right;
                                if(libSymbols.containsEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram, addedInstructions);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib, addedInstructions);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof BinExp){
                            BinExp binary = (BinExp)exp;

                            if(binary.left instanceof IdentExp){
                                IdentExp leftIdent = (IdentExp)binary.left;
                                if(libSymbols.containsEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, leftIdent.ident, newProgram, addedInstructions);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions,  currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, rightIdent.ident, newProgram, addedInstructions);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib, addedInstructions);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)exp;
                            if(!procSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, program, libraries, newProgram, addedInstructions, currentLib);
                            Proc fetchedProcedure = procSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Assign> newArgs = new LinkedList<Assign>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                    
                                    if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                        if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, addedInstructions, currentLib);
                                    } else {
                                        fetchInternalDependentInstructions(currentLib, program, libraries, value.source, newProgram, addedInstructions);
                                    }

                                    String place = fetchedProcedure.paramAssign.get(argIndex).place;
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }

                                List<ICode> addList = addedInstructions.get(currentLib);

                                Call newCall = new Call(call.procedureName, newArgs);
                                String toRetFrom = fetchedProcedure.placement.place;
                                String toRetTo = assign.place;
                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());

                                if(!newProgram.dataSectionContainsInstruction(newCall) && !newProgram.dataSectionContainsInstruction(newPlace)){
                                    dataInstructions.addInstruction(newCall);
                                    dataInstructions.addInstruction(newPlace);
                                    addList.add(newCall);
                                    addList.add(newPlace);
                                }
                                if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        newTable.addEntry(entry);
                                }

                                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, addedInstructions)){
                                    String place = null;    
                                    do{
                                        place = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                    replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib, addedInstructions);
                                }
                            }
                        } else {
                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, addedInstructions)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib, addedInstructions);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private void fetchInternalProcedure(Prog program, String procName, Lib[] libraries, Prog newProg, Map<Lib, List<ICode>> addedInstructions){
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        ProcSec libProcSec = program.procedures;
        SymSec libSymbols = program.symbols;
        ProcSec procedureSec = newProg.procedures;
        SymSec symbolTable = newProg.symbols;
        if(libProcSec.containsProcedure(procName) && !procedureSec.containsProcedure(procName)){
            Proc procedure = libProcSec.getProcedureByName(procName);

            for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                Assign assign = procedure.paramAssign.get(assignIndex);

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), program, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, program, libraries, program, addedInstructions);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, program, addedInstructions);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), program, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, program, libraries, program, addedInstructions);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, program, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(placement.place, place, program, libraries, program, addedInstructions);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, program, libraries, addedInstructions)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries, addedInstructions));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, program, libraries, program, addedInstructions);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;
                    
                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, program, libraries, newProg, addedInstructions);
                        Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                        int numberOfArgsInCall = call.arguments.size();

                        if(numberOfArgsInCall != numberOfArgsInProc){
                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                        } else {
                            List<Assign> newArgs = new LinkedList<Assign>();
                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                            
                                if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                                }

                                String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                            }

                            Call newCall = new Call(call.procedureName, newArgs);
                            Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, assignment.place, new IdentExp(fetchedProcedure.placement.place), fetchedProcedure.placement.getType());

                            List<ICode> addList = addedInstructions.get(program);
                            newProcedure.addInstruction(newCall);
                            newProcedure.addInstruction(newPlace);
                            addList.add(newCall);
                            addList.add(newPlace);

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
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }
                    }

                    if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifTrue, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                        program.replaceLabel(ifStat.ifTrue, newLabel);
                    }

                    if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifFalse, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                        program.replaceLabel(ifStat.ifFalse, newLabel);
                    }
                } else if(icode instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)icode;
                    
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, program, libraries, newProg, addedInstructions);
                    Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                    } else {
                        List<Assign> newArgs = new LinkedList<Assign>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                            
                            if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                            }

                            String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        List<ICode> addList = addedInstructions.get(program);
                        
                        Call newCall = new Call(call.procedureName, newArgs);
                        newProcedure.addInstruction(newCall);
                        addList.add(newCall);

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(program, call.pname, libraries, newProg, addedInstructions);
                    
                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }
                    }
                } else if(icode instanceof Goto){
                    Goto gotoICode = (Goto)icode;

                    if(!labelIsUniqueAcrossProgramAndLibraries(gotoICode.label, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                        program.replaceLabel(gotoICode.label, newLabel);
                    }
                } else if(icode instanceof Label){
                    Label labelICode = (Label)icode;

                    if(!labelIsUniqueAcrossProgramAndLibraries(labelICode.label, program, libraries, program)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(labelICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                        program.replaceLabel(labelICode.label, newLabel);
                    }
                }
                
                newProcedure.addInstruction(icode);
            }
        }
    }

    private void fetchInternalProcedure(Lib library, String procName, Prog prog, Lib[] libraries, Prog newProg, Map<Lib, List<ICode>> addedInstructions){
        ProcSec procedureSec = newProg.procedures;
        SymSec symbolTable = newProg.symbols;
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        ProcSec libProcSec = library.procedures;
        SymSec libSymbols = library.symbols;
        if(libProcSec.containsProcedure(procName) && !procedureSec.containsProcedure(procName)){
            Proc procedure = libProcSec.getProcedureByName(procName);

            for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                Assign assign = procedure.paramAssign.get(assignIndex);

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), prog, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, prog, libraries, library, addedInstructions);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, prog, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(assign.place, place, prog, libraries, library, addedInstructions);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), prog, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, prog, libraries, library, addedInstructions);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, prog, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                    replacePlaceAcrossProgramAndLibraries(placement.place, place, prog, libraries, library, addedInstructions);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries, addedInstructions)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries, addedInstructions));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library, addedInstructions);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(ident.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg, addedInstructions);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(ident.ident)) {
                                fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg, addedInstructions);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg, addedInstructions);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg, addedInstructions);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;
                    
                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, prog, libraries, newProg, addedInstructions, library);
                        Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                        int numberOfArgsInCall = call.arguments.size();

                        if(numberOfArgsInCall != numberOfArgsInProc){
                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                        } else {
                            List<Assign> newArgs = new LinkedList<Assign>();
                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                            
                                if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(value.source)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg, addedInstructions);
                                }

                                String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                            }

                            List<ICode> addList = addedInstructions.get(library);

                            Call newCall = new Call(call.procedureName, newArgs);
                            newProcedure.addInstruction(newCall);
                            addList.add(newCall);

                            Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, assignment.place, new IdentExp(fetchedProcedure.placement.place), assignment.getType());
                            newProcedure.addInstruction(newPlace);
                            addList.add(newPlace);

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
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(leftExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg, addedInstructions);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(rightExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg, addedInstructions);
                        }
                    }

                    if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifTrue, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                        library.replaceLabel(ifStat.ifTrue, newLabel);
                    }

                    if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifFalse, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                        library.replaceLabel(ifStat.ifFalse, newLabel);
                    }
                } else if(icode instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)icode;
                    
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, prog, libraries, newProg, addedInstructions, library);
                    Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                    } else {
                        List<Assign> newArgs = new LinkedList<Assign>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                            
                            if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(value.source)) {
                                fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg, addedInstructions);
                            }

                            String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        List<ICode> addList = addedInstructions.get(library);
                        
                        Call newCall = new Call(call.procedureName, newArgs);
                        newProcedure.addInstruction(newCall);
                        addList.add(newCall);

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(library, call.pname, prog, libraries, newProg, addedInstructions);
                    
                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(place)) {
                            fetchInternalDependentInstructions(library, prog, libraries, place, newProg, addedInstructions);
                        }
                    }
                } else if(icode instanceof Goto){
                    Goto gotoICode = (Goto)icode;

                    if(!labelIsUniqueAcrossProgramAndLibraries(gotoICode.label, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                        library.replaceLabel(gotoICode.label, newLabel);
                    }
                } else if(icode instanceof Label){
                    Label labelICode = (Label)icode;

                    if(!labelIsUniqueAcrossProgramAndLibraries(labelICode.label, prog, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(labelICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                        library.replaceLabel(labelICode.label, newLabel);
                    }
                }
                
                newProcedure.addInstruction(icode);
            }
        }
    }

    private void fetchInternalProcedure(Lib library, String procName, Lib single, Lib[] libraries, Lib newLib, Map<Lib, List<ICode>> addedInstructions){
        SymSec symbolTable = newLib.symbols;
        ProcSec procedureSec = newLib.procedures;
        ProcLabel newProcLabel = new ProcLabel(procName);
        Proc newProcedure = new Proc(newProcLabel);
        ProcSec libProcSec = library.procedures;
        SymSec libSymbols = library.symbols;
        if(libProcSec.containsProcedure(procName)){
            Proc procedure = libProcSec.getProcedureByName(procName);

            for(int assignIndex = 0; assignIndex < procedure.paramAssign.size(); assignIndex++){
                Assign assign = procedure.paramAssign.get(assignIndex);

                if(!placeIsUniqueAcrossLibraries(assign.value.toString(), single, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                    replacePlaceAcrossLibraries(assign.value.toString(), place, single, libraries, library, addedInstructions);
                }

                if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                    replacePlaceAcrossLibraries(assign.place, place, single, libraries, library, addedInstructions);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossLibraries(placement.place, single, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                    replacePlaceAcrossLibraries(placement.place, place, single, libraries, library, addedInstructions);
                }

                if(!placeIsUniqueAcrossLibraries(placement.value.toString(), single, libraries, addedInstructions)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                    replacePlaceAcrossLibraries(placement.value.toString(), place, single, libraries, library, addedInstructions);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries, addedInstructions)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, addedInstructions));
                        
                        replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library, addedInstructions);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(ident.ident)){
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib, addedInstructions);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(ident.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib, addedInstructions);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib, addedInstructions);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib, addedInstructions);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;

                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, single, libraries, newLib, addedInstructions, library);
                        Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                        int numberOfArgsInCall = call.arguments.size();

                        if(numberOfArgsInCall != numberOfArgsInProc){
                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                        } else {
                            List<Assign> newArgs = new LinkedList<Assign>();
                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                
                                if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(value.source)){
                                    fetchInternalDependentInstructions(library, single, libraries, value.source, newLib, addedInstructions);
                                }

                                String source = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(new Assign(Scope.ARGUMENT, source, new IdentExp(value.source), value.dest));
                            }

                            List<ICode> addList = addedInstructions.get(library);
                        
                            Call newCall = new Call(call.procedureName, newArgs);
                            newProcedure.addInstruction(newCall);
                            addList.add(newCall);

                            String toRetFrom = fetchedProcedure.placement.place;
                            String toRetTo = assignment.place;
                            Assign newPlace = new Assign(Assign.Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignment.getType());
                            newProcedure.addInstruction(newPlace);
                            addList.add(newPlace);
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
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(leftExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib, addedInstructions);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(rightExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib, addedInstructions);
                        }
                    }

                    if(!labelIsUniqueAcrossLibraries(ifStat.ifTrue, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                        library.replaceLabel(ifStat.ifTrue, newLabel);
                    }

                    if(!labelIsUniqueAcrossLibraries(ifStat.ifFalse, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                        library.replaceLabel(ifStat.ifFalse, newLabel);
                    }
                } else if(icode instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)icode;
                    
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, single, libraries, newLib, addedInstructions, library);
                    Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                    } else {
                        List<Assign> newArgs = new LinkedList<Assign>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            Tuple<String, Assign.Type> value = call.arguments.get(argIndex);

                            if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                            } else if(!newProcedure.containsPlace(value.source)) {
                                fetchInternalDependentInstructions(library, single, libraries, value.source, newLib, addedInstructions);
                            }

                            String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        List<ICode> addList = addedInstructions.get(library);

                        Call newCall = new Call(call.procedureName, newArgs);
                        newProcedure.addInstruction(newCall);
                        addList.add(newCall);

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(library, call.pname, single, libraries, newLib, addedInstructions);

                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                        } else if(!newProcedure.containsPlace(place)) {
                            fetchInternalDependentInstructions(library, single, libraries, place, newLib, addedInstructions);
                        }
                    }
                } else if(icode instanceof Goto){
                    Goto gotoICode = (Goto)icode;

                    if(!labelIsUniqueAcrossLibraries(gotoICode.label, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                        library.replaceLabel(gotoICode.label, newLabel);
                    }
                } else if(icode instanceof Label){
                    Label labelICode = (Label)icode;

                    if(!labelIsUniqueAcrossLibraries(labelICode.label, single, libraries, library)){
                        String newLabel;
                        LabelGenerator lGen = new LabelGenerator(labelICode.label);
                        do{
                            newLabel = lGen.genNext();
                        } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                        library.replaceLabel(labelICode.label, newLabel);
                    }
                }

                newProcedure.addInstruction(icode);
            }
        }
    }

    private void fetchExternalProcedure(String procName, Prog prog, Lib[] libraries, Prog newProg, Map<Lib, List<ICode>> addedInstructions, Lib... libsToIgnore){
        SymSec symbolTable = newProg.symbols;
        ProcSec procedureSec = newProg.procedures;
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

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), prog, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                            replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, prog, libraries, library, addedInstructions);
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, prog, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                            replacePlaceAcrossProgramAndLibraries(assign.place, place, prog, libraries, library, addedInstructions);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), prog, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                            replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, prog, libraries, library, addedInstructions);
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, prog, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, addedInstructions));

                            replacePlaceAcrossProgramAndLibraries(placement.place, place, prog, libraries, library, addedInstructions);
                        }

                        if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);


                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries, addedInstructions)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries, addedInstructions));
                                
                                replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library, addedInstructions);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(ident.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg, addedInstructions);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(ident.ident)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg, addedInstructions);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg, addedInstructions);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(rightExp.ident)){
                                        fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg, addedInstructions);
                                    }
                                }
                            } else if(assignExp instanceof ExternalCall){
                                ExternalCall call = (ExternalCall)assignExp;
                            
                                if(!procedureSec.containsProcedure(call.procedureName))
                                    fetchExternalProcedure(call.procedureName, prog, libraries, newProg, addedInstructions, library);
                                Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                                int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                int numberOfArgsInCall = call.arguments.size();

                                if(numberOfArgsInCall != numberOfArgsInProc){
                                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                                } else {
                                    List<Assign> newArgs = new LinkedList<Assign>();
                                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                        Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                        Assign newArg;

                                        if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                        } else if(!newProcedure.containsPlace(value.source)){
                                            fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg, addedInstructions);
                                        }

                                        String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();

                                        newArg = new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest);
                                        newArgs.add(newArg);
                                    }

                                    List<ICode> addList = addedInstructions.get(library);

                                    Call newCall = new Call(call.procedureName, newArgs);
                                    newProcedure.addInstruction(newCall);
                                    addList.add(newCall);
                                    Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, assignment.place, new IdentExp(fetchedProcedure.placement.place), assignment.getType());
                                    newProcedure.addInstruction(newPlace);
                                    addList.add(newPlace);

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
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg, addedInstructions);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(rightExp.ident)){
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg, addedInstructions);
                                }
                            }

                            if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifTrue, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                                library.replaceLabel(ifStat.ifTrue, newLabel);
                            }

                            if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifFalse, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                                library.replaceLabel(ifStat.ifFalse, newLabel);
                            }
                        } else if(icode instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)icode;
                            
                            if(!procedureSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, prog, libraries, newProg, addedInstructions, library);
                            Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Assign> newArgs = new LinkedList<Assign>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                    
                                    if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(value.source)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg, addedInstructions);
                                    }

                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }

                                List<ICode> addList = addedInstructions.get(library);
                                
                                Call newCall = new Call(call.procedureName, newArgs);
                                newProcedure.addInstruction(newCall);
                                addList.add(newCall);

                                continue;
                            }
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname))
                                fetchInternalProcedure(library, call.pname, prog, libraries, newProg, addedInstructions);

                            for(Assign arg : call.params){
                                String place = arg.value.toString();

                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(place)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, place, newProg, addedInstructions);
                                }
                            }
                        } else if(icode instanceof Goto){
                            Goto gotoICode = (Goto)icode;

                            if(!labelIsUniqueAcrossProgramAndLibraries(gotoICode.label, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                                library.replaceLabel(gotoICode.label, newLabel);
                            }
                        } else if(icode instanceof Label){
                            Label labelICode = (Label)icode;

                            if(!labelIsUniqueAcrossProgramAndLibraries(labelICode.label, prog, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(labelICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, prog, libraries, library));
                                library.replaceLabel(labelICode.label, newLabel);
                            }
                        }
                        newProcedure.addInstruction(icode);
                    }
                    break loop;
                }
            }
        }
    }

    private void fetchExternalProcedure(String procName, Lib single, Lib[] libraries, Lib newLib, Map<Lib, List<ICode>> addedInstructions, Lib... libsToIgnore){
        SymSec symbolTable = newLib.symbols;
        ProcSec procedureSec = newLib.procedures;
        DataSec dataSection = newLib.variables;
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

                        if(!placeIsUniqueAcrossLibraries(assign.value.toString(), single, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                            replacePlaceAcrossLibraries(assign.value.toString(), place, single, libraries, library, addedInstructions);
                        }

                        if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                            replacePlaceAcrossLibraries(assign.place, place, single, libraries, library, addedInstructions);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueAcrossLibraries(placement.value.toString(), single, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                            replacePlaceAcrossLibraries(placement.value.toString(), place, single, libraries, library, addedInstructions);
                        }

                        if(!placeIsUniqueAcrossLibraries(placement.place, single, libraries, addedInstructions)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries, addedInstructions));

                            replacePlaceAcrossLibraries(placement.place, place, single, libraries, library, addedInstructions);
                        }

                        if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);

                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries, addedInstructions)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, addedInstructions));
                                
                                replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library, addedInstructions);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(ident.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib, addedInstructions);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(ident.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib, addedInstructions);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib, addedInstructions);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib, addedInstructions);
                                    }
                                }
                            } else if(assignExp instanceof ExternalCall){
                                ExternalCall call = (ExternalCall)assignExp;

                                if(!procedureSec.containsProcedure(call.procedureName))
                                    fetchExternalProcedure(call.procedureName, single, libraries, newLib, addedInstructions, library);
                                Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                                int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                                int numberOfArgsInCall = call.arguments.size();

                                if(numberOfArgsInCall != numberOfArgsInProc){
                                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                                } else {
                                    List<Assign> newArgs = new LinkedList<Assign>();
                                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                        Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                        
                                        if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                            SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                        } else if(!newProcedure.containsPlace(value.source)) {
                                            fetchInternalDependentInstructions(library, single, libraries, value.source, newLib, addedInstructions);
                                        }

                                        String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                    }

                                    List<ICode> addList = addedInstructions.get(library);
                                
                                    Call newCall = new Call(call.procedureName, newArgs);
                                    newProcedure.addInstruction(newCall);
                                    addList.add(newCall);

                                    String toRetFrom = fetchedProcedure.placement.place;
                                    String toRetTo = assignment.place;
                                    Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignment.getType());
                                    newProcedure.addInstruction(newPlace);
                                    addList.add(newPlace);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib, addedInstructions);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib, addedInstructions);
                                }
                            }

                            if(!labelIsUniqueAcrossLibraries(ifStat.ifTrue, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                                library.replaceLabel(ifStat.ifTrue, newLabel);
                            }

                            if(!labelIsUniqueAcrossLibraries(ifStat.ifFalse, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                                library.replaceLabel(ifStat.ifFalse, newLabel);
                            }
                        } else if(icode instanceof ExternalCall){
                            ExternalCall call = (ExternalCall)icode;
                            
                            if(!procedureSec.containsProcedure(call.procedureName))
                                fetchExternalProcedure(call.procedureName, single, libraries, newLib, addedInstructions, library);
                            Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                            int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                            int numberOfArgsInCall = call.arguments.size();

                            if(numberOfArgsInCall != numberOfArgsInProc){
                                errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                            } else {
                                List<Assign> newArgs = new LinkedList<Assign>();
                                for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                    Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                    
                                    if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                    } else if(!newProcedure.containsPlace(value.source)) {
                                        fetchInternalDependentInstructions(library, single, libraries, value.source, newLib, addedInstructions);
                                    }
                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }

                                List<ICode> addList = addedInstructions.get(library);
                                
                                Call newCall = new Call(call.procedureName, newArgs);
                                newProcedure.addInstruction(newCall);

                                addList.add(newCall);

                                continue;
                            }
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname))
                                fetchInternalProcedure(library, call.pname, single, libraries, newLib, addedInstructions);

                            for(Assign arg : call.params){
                                String place = arg.value.toString();

                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, addedInstructions, library);
                                } else if(!newProcedure.containsPlace(place)) {
                                    fetchInternalDependentInstructions(library, single, libraries, place, newLib, addedInstructions);
                                }
                            }
                        } else if(icode instanceof Goto){
                            Goto gotoICode = (Goto)icode;

                            if(!labelIsUniqueAcrossLibraries(gotoICode.label, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                                library.replaceLabel(gotoICode.label, newLabel);
                            }
                        } else if(icode instanceof Label){
                            Label labelICode = (Label)icode;

                            if(!labelIsUniqueAcrossLibraries(labelICode.label, single, libraries, library)){
                                String newLabel;
                                LabelGenerator lGen = new LabelGenerator(labelICode.label);
                                do{
                                    newLabel = lGen.genNext();
                                } while(!labelIsUniqueAcrossLibraries(newLabel, single, libraries, library));
                                library.replaceLabel(labelICode.label, newLabel);
                            }
                        }

                        newProcedure.addInstruction(icode);
                    }
                    break loop;
                }
            }
        }
    }

    private static void replacePlaceAcrossProgramAndLibraries(String oldPlace, String newPlace, Prog program, Lib[] libraries, Lib libToAlwaysReplace, Map<Lib, List<ICode>> addedInstructions){
        libToAlwaysReplace.replacePlace(oldPlace, newPlace);
        List<ICode> extraToReplace = addedInstructions.get(libToAlwaysReplace);
        for(ICode extraInstruction: extraToReplace){
            extraInstruction.replacePlace(oldPlace, newPlace);
        }
        if(libToAlwaysReplace.containsExternalSymbolByPlace(newPlace)){
            SymEntry entryToLook = libToAlwaysReplace.getExternalSymbolByPlace(newPlace);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsExternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = program.getExternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        program.replacePlace(placeFrom, newPlace);
                        List<ICode> extraInLib = addedInstructions.get(program);
                        for(ICode extraICode: extraInLib){
                            extraICode.replacePlace(placeFrom, newPlace);
                        }
                    }
                } else if(program.containsInternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = program.getInternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        program.replacePlace(placeFrom, newPlace);
                        List<ICode> extraInLib = addedInstructions.get(program);
                        for(ICode extraICode: extraInLib){
                            extraICode.replacePlace(placeFrom, newPlace);
                        }
                    }
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getExternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
                            List<ICode> extraInLib = addedInstructions.get(library);
                            for(ICode extraICode: extraInLib){
                                extraICode.replacePlace(fromPlace, newPlace);
                            }
                        }
                    } else if(library.containsInternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getInternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
                            List<ICode> extraInLib = addedInstructions.get(library);
                            for(ICode extraICode: extraInLib){
                                extraICode.replacePlace(fromPlace, newPlace);
                            }
                        }
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalSymbolByPlace(newPlace)) {
            SymEntry entryToLook = libToAlwaysReplace.getInternalSymbolByPlace(newPlace);
            String ident = entryToLook.declanIdent;
            if(!libToAlwaysReplace.equals(program)){
                if(program.containsExternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = program.getExternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        program.replacePlace(placeFrom, newPlace);
                        List<ICode> extraInLib = addedInstructions.get(program);
                        for(ICode extraICode: extraInLib){
                            extraICode.replacePlace(placeFrom, newPlace);
                        }
                    }
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getExternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
                            List<ICode> extraInLib = addedInstructions.get(library);
                            for(ICode extraICode: extraInLib){
                                extraICode.replacePlace(fromPlace, newPlace);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void replacePlaceAcrossLibraries(String oldPlace, String newPlace, Lib lib, Lib[] libraries, Lib libToAlwaysReplace, Map<Lib, List<ICode>> addedInstructions){
        libToAlwaysReplace.replacePlace(oldPlace, newPlace);
        List<ICode> extraToReplace = addedInstructions.get(libToAlwaysReplace);
        for(ICode extraInstruction: extraToReplace){
            extraInstruction.replacePlace(oldPlace, newPlace);
        }
        if(libToAlwaysReplace.containsExternalSymbolByPlace(newPlace)){
            SymEntry entryToLook = libToAlwaysReplace.getExternalSymbolByPlace(newPlace);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsExternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = lib.getExternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        lib.replacePlace(placeFrom, newPlace);
                        List<ICode> extraInLib = addedInstructions.get(lib);
                        for(ICode extraICode: extraInLib){
                            extraICode.replacePlace(placeFrom, newPlace);
                        }
                    }
                } else if(lib.containsInternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = lib.getInternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        lib.replacePlace(placeFrom, newPlace);
                        List<ICode> extraInLib = addedInstructions.get(lib);
                        for(ICode extraICode: extraInLib){
                            extraICode.replacePlace(placeFrom, newPlace);
                        }
                    }
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getExternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
                            List<ICode> extraInLib = addedInstructions.get(library);
                            for(ICode extraICode: extraInLib){
                                extraICode.replacePlace(fromPlace, newPlace);
                            }
                        }
                    } else if(library.containsInternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getInternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
                            List<ICode> extraInLib = addedInstructions.get(library);
                            for(ICode extraICode: extraInLib){
                                extraICode.replacePlace(fromPlace, newPlace);
                            }
                        }
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalSymbolByPlace(newPlace)) {
            SymEntry entryToLook = libToAlwaysReplace.getInternalSymbolByPlace(newPlace);
            String ident = entryToLook.declanIdent;
            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsExternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = lib.getExternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        lib.replacePlace(placeFrom, newPlace);
                        List<ICode> extraInLib = addedInstructions.get(lib);
                        for(ICode extraICode: extraInLib){
                            extraICode.replacePlace(placeFrom, newPlace);
                        }
                    }
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getExternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
                            List<ICode> extraInLib = addedInstructions.get(library);
                            for(ICode extraICode: extraInLib){
                                extraICode.replacePlace(fromPlace, newPlace);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean placeIsUniqueAcrossProgramAndLibraries(String place, Prog program, Lib[] libraries, Map<Lib, List<ICode>> addedInstructions){
        Set<String> internalReturnsRepresented = new HashSet<String>();
        Set<String> argumentRepresented = new HashSet<String>();
        Set<String> paramaterRepresented = new HashSet<String>();
        Set<String> externalReturnsRepresented = new HashSet<String>();
        Set<String> externalIdentsRepresented = new HashSet<String>();
        Set<String> internalIdentsRepresented = new HashSet<String>();
        int localVariableCount = 0;


        int programLocalVariableCount = 0;
        boolean programContainsExternalReturn = program.containsExternalReturn(place);
        if(programContainsExternalReturn){
            externalReturnsRepresented.addAll(program.externalReturnForFunctions(place));
        }
        boolean programContainsInternalReturn = program.containsInternalReturn(place);
        if(programContainsInternalReturn){
            internalReturnsRepresented.addAll(program.internalReturnForFunctions(place));
        }
        if(program.containsExternalSymbolByPlace(place)){
            externalIdentsRepresented.add(program.getExternalSymbolByPlace(place).declanIdent);
        } else if(program.containsParamater(place)){
            paramaterRepresented.addAll(program.paramaterForFunctions(place));
        } else if(program.containsArgument(place)){
            argumentRepresented.addAll(program.argumentInFunctions(place));  
        } else if(program.containsInternalSymbolByPlace(place)){
            internalIdentsRepresented.add(program.getInternalSymbolByPlace(place).declanIdent);
        } else if(program.containsPlace(place) && !programContainsInternalReturn && !programContainsExternalReturn){
            programLocalVariableCount++;
        }
        List<ICode> programAddedInstructions = addedInstructions.get(program);
        for(ICode addedInstruction: programAddedInstructions){
            boolean programAddedContainsExternalReturn = addedInstruction.containsExternalReturn(place);
            if(programAddedContainsExternalReturn){
                externalReturnsRepresented.addAll(addedInstruction.externalReturnForFunctions(place));
            }
            boolean programAddedContainsInternalReturn = addedInstruction.containsInternalReturn(place);
            if(programAddedContainsInternalReturn){
                internalReturnsRepresented.addAll(addedInstruction.internalReturnForFunctions(place));
            }
            if(addedInstruction.containsParamater(place)){
                paramaterRepresented.addAll(addedInstruction.paramaterForFunctions(place));
            } else if(addedInstruction.containsArgument(place)){
                argumentRepresented.addAll(addedInstruction.argumentInFunctions(place));  
            } else if(program.containsPlace(place) && !programAddedContainsInternalReturn && !programAddedContainsExternalReturn){
                programLocalVariableCount++;
            }
        }

        if(programLocalVariableCount > 0){
            localVariableCount++;
        }

        for(Lib lib: libraries){
            int libLocalVariableCount = 0;
            boolean libContainsExternalReturn = lib.containsExternalReturn(place);
            if(libContainsExternalReturn){
                externalReturnsRepresented.addAll(lib.externalReturnForFunctions(place));
            }
            boolean libContainsInternalReturn = lib.containsInternalReturn(place);
            if(libContainsInternalReturn){
                internalReturnsRepresented.addAll(lib.internalReturnForFunctions(place));
            }
            if(lib.containsExternalSymbolByPlace(place)){
                externalIdentsRepresented.add(lib.getExternalSymbolByPlace(place).declanIdent);
            } else if(lib.containsParamater(place)){
                paramaterRepresented.addAll(lib.paramaterForFunctions(place));
            } else if(lib.containsArgument(place)){
                argumentRepresented.addAll(lib.argumentInFunctions(place));  
            } else if(lib.containsInternalSymbolByPlace(place)){
                internalIdentsRepresented.add(lib.getInternalSymbolByPlace(place).declanIdent);
            } else if(lib.containsPlace(place) && !libContainsInternalReturn && !libContainsExternalReturn){
                libLocalVariableCount++;
            }

            List<ICode> libAddedInstructions = addedInstructions.get(lib);
            for(ICode addedInstruction: libAddedInstructions){
                boolean libAddedContainsExternalReturn = addedInstruction.containsExternalReturn(place);
                if(libAddedContainsExternalReturn){
                    externalReturnsRepresented.addAll(addedInstruction.externalReturnForFunctions(place));
                }
                boolean libAddedContainsInternalReturn = addedInstruction.containsInternalReturn(place);
                if(libAddedContainsInternalReturn){
                    internalReturnsRepresented.addAll(addedInstruction.internalReturnForFunctions(place));
                }
                if(addedInstruction.containsParamater(place)){
                    paramaterRepresented.addAll(addedInstruction.paramaterForFunctions(place));
                } else if(addedInstruction.containsArgument(place)){
                    argumentRepresented.addAll(addedInstruction.argumentInFunctions(place));  
                } else if(addedInstruction.containsPlace(place) && !libAddedContainsInternalReturn && !libAddedContainsExternalReturn){
                    libLocalVariableCount++;
                }
            }

            if(libLocalVariableCount > 0){
                localVariableCount++;
            }
        }

        if(localVariableCount == 1 && externalIdentsRepresented.isEmpty() 
            && internalIdentsRepresented.isEmpty() && internalReturnsRepresented.isEmpty() 
            && externalReturnsRepresented.isEmpty() && argumentRepresented.isEmpty() 
            && paramaterRepresented.isEmpty()){
            return true;
        } else if(externalIdentsRepresented.size() == 1 && internalIdentsRepresented.size() == 1 
                && argumentRepresented.isEmpty() && paramaterRepresented.isEmpty() 
                && externalIdentsRepresented.isEmpty() && internalIdentsRepresented.isEmpty() 
                && localVariableCount == 0){
            for(String externalIdent: externalIdentsRepresented){
                for(String internalIdent: internalIdentsRepresented){
                    return externalIdent.equals(internalIdent);
                }
            }
            //Not Used
            return false;
        } else if(internalIdentsRepresented.size() == 1 && externalIdentsRepresented.isEmpty() 
                && paramaterRepresented.isEmpty() && argumentRepresented.isEmpty() 
                && internalReturnsRepresented.isEmpty() && externalReturnsRepresented.isEmpty() 
                && localVariableCount == 0){
            return true;
        } else if(paramaterRepresented.size() == 1 && argumentRepresented.size() == 1
                && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
                && internalReturnsRepresented.isEmpty() && externalReturnsRepresented.isEmpty()
                && localVariableCount == 0){
            for(String paramaterFunc: paramaterRepresented){
                for(String argumentFunc: argumentRepresented){
                    return paramaterFunc.equals(argumentFunc);
                }
            }
            return false; //Unused
        } else if (paramaterRepresented.size() == 1 && argumentRepresented.isEmpty()
            && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
            && internalReturnsRepresented.isEmpty() && externalReturnsRepresented.isEmpty()
            && localVariableCount == 0){
            return true;
        } else if(internalReturnsRepresented.size() == 1 && externalReturnsRepresented.size() == 1
                && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
                && argumentRepresented.isEmpty() && paramaterRepresented.isEmpty()
                && localVariableCount == 0){
            for(String internalReturn: internalReturnsRepresented){
                for(String externalReturn: externalReturnsRepresented){
                    return internalReturn.equals(externalReturn);
                }
            }
            return false; //Unused
        } else if(internalReturnsRepresented.size() == 1 && externalReturnsRepresented.isEmpty()
        && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
        && argumentRepresented.isEmpty() && paramaterRepresented.isEmpty()
        && localVariableCount == 0){
            return true;
        } else {
            return false;
        }
    }

    private static boolean placeIsUniqueAcrossLibraries(String place, Lib library, Lib[] libraries, Map<Lib, List<ICode>> addedInstructions){
        Set<String> internalReturnsRepresented = new HashSet<String>();
        Set<String> argumentRepresented = new HashSet<String>();
        Set<String> paramaterRepresented = new HashSet<String>();
        Set<String> externalReturnsRepresented = new HashSet<String>();
        Set<String> externalIdentsRepresented = new HashSet<String>();
        Set<String> internalIdentsRepresented = new HashSet<String>();
        int localVariableCount = 0;

        int singleLocalVariableCount = 0;
        boolean libraryContainsExternalReturn = library.containsExternalReturn(place);
        if(libraryContainsExternalReturn){
            externalReturnsRepresented.addAll(library.externalReturnForFunctions(place));
        }
        boolean libraryContainsInternalReturn = library.containsInternalReturn(place);
        if(libraryContainsInternalReturn){
            internalReturnsRepresented.addAll(library.internalReturnForFunctions(place));
        }
        if(library.containsExternalSymbolByPlace(place)){
            externalIdentsRepresented.add(library.getExternalSymbolByPlace(place).declanIdent);
        } else if(library.containsParamater(place)){
            paramaterRepresented.addAll(library.paramaterForFunctions(place));
        } else if(library.containsArgument(place)){
            argumentRepresented.addAll(library.argumentInFunctions(place));  
        } else if(library.containsInternalSymbolByPlace(place)){
            internalIdentsRepresented.add(library.getInternalSymbolByPlace(place).declanIdent);
        } else if(library.containsPlace(place) && !libraryContainsInternalReturn && !libraryContainsExternalReturn){
            singleLocalVariableCount++;
        }
        List<ICode> programAddedInstructions = addedInstructions.get(library);
        for(ICode addedInstruction: programAddedInstructions){
            boolean libraryAddedContainsExternalReturn = addedInstruction.containsExternalReturn(place);
            if(libraryAddedContainsExternalReturn){
                externalReturnsRepresented.addAll(addedInstruction.externalReturnForFunctions(place));
            }
            boolean libraryAddedContainsInternalReturn = addedInstruction.containsInternalReturn(place);
            if(libraryAddedContainsInternalReturn){
                internalReturnsRepresented.addAll(addedInstruction.internalReturnForFunctions(place));
            }
            if(addedInstruction.containsParamater(place)){
                paramaterRepresented.addAll(addedInstruction.paramaterForFunctions(place));
            } else if(addedInstruction.containsArgument(place)){
                argumentRepresented.addAll(addedInstruction.argumentInFunctions(place));  
            } else if(library.containsPlace(place) && !libraryAddedContainsInternalReturn && !libraryAddedContainsExternalReturn){
                singleLocalVariableCount++;
            }
        }

        if(singleLocalVariableCount > 0){
            localVariableCount++;
        }

        for(Lib lib: libraries){
            int libLocalVariableCount = 0;

            boolean libContainsExternalReturn = lib.containsExternalReturn(place);
            if(libContainsExternalReturn){
                externalReturnsRepresented.addAll(lib.externalReturnForFunctions(place));
            }
            boolean libContainsInternalReturn = lib.containsInternalReturn(place);
            if(libContainsInternalReturn){
                internalReturnsRepresented.addAll(lib.internalReturnForFunctions(place));
            }
            if(lib.containsExternalSymbolByPlace(place)){
                externalIdentsRepresented.add(lib.getExternalSymbolByPlace(place).declanIdent);
            } else if(lib.containsParamater(place)){
                paramaterRepresented.addAll(lib.paramaterForFunctions(place));
            } else if(lib.containsArgument(place)){
                argumentRepresented.addAll(lib.argumentInFunctions(place));  
            } else if(lib.containsInternalSymbolByPlace(place)){
                internalIdentsRepresented.add(lib.getInternalSymbolByPlace(place).declanIdent);
            } else if(lib.containsPlace(place) && !libContainsInternalReturn && !libContainsExternalReturn){
                libLocalVariableCount++;
            }

            List<ICode> libAddedInstructions = addedInstructions.get(lib);
            for(ICode addedInstruction: libAddedInstructions){
                boolean libAddedContainsExternalReturn = addedInstruction.containsExternalReturn(place);
                if(libAddedContainsExternalReturn){
                    externalReturnsRepresented.addAll(addedInstruction.externalReturnForFunctions(place));
                }
                boolean libAddedContainsInternalReturn = addedInstruction.containsInternalReturn(place);
                if(libAddedContainsInternalReturn){
                    internalReturnsRepresented.addAll(addedInstruction.internalReturnForFunctions(place));
                }
                if(addedInstruction.containsParamater(place)){
                    paramaterRepresented.addAll(addedInstruction.paramaterForFunctions(place));
                } else if(addedInstruction.containsArgument(place)){
                    argumentRepresented.addAll(addedInstruction.argumentInFunctions(place));  
                } else if(addedInstruction.containsPlace(place) && !libAddedContainsInternalReturn && !libAddedContainsExternalReturn){
                    libLocalVariableCount++;
                }
            }

            if(libLocalVariableCount > 0){
                localVariableCount++;
            }
        }

        if(localVariableCount == 1 && externalIdentsRepresented.isEmpty() 
            && internalIdentsRepresented.isEmpty() && internalReturnsRepresented.isEmpty() 
            && externalReturnsRepresented.isEmpty() && argumentRepresented.isEmpty() 
            && paramaterRepresented.isEmpty()){
            return true;
        } else if(externalIdentsRepresented.size() == 1 && internalIdentsRepresented.size() == 1 
                && argumentRepresented.isEmpty() && paramaterRepresented.isEmpty() 
                && externalIdentsRepresented.isEmpty() && internalIdentsRepresented.isEmpty() 
                && localVariableCount == 0){
            for(String externalIdent: externalIdentsRepresented){
                for(String internalIdent: internalIdentsRepresented){
                    return externalIdent.equals(internalIdent);
                }
            }
            //Not Used
            return false;
        } else if(internalIdentsRepresented.size() == 1 && externalIdentsRepresented.isEmpty() 
                && paramaterRepresented.isEmpty() && argumentRepresented.isEmpty() 
                && internalReturnsRepresented.isEmpty() && externalReturnsRepresented.isEmpty() 
                && localVariableCount == 0){
            return true;
        } else if(paramaterRepresented.size() == 1 && argumentRepresented.size() == 1
                && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
                && internalReturnsRepresented.isEmpty() && externalReturnsRepresented.isEmpty()
                && localVariableCount == 0){
            for(String paramaterFunc: paramaterRepresented){
                for(String argumentFunc: argumentRepresented){
                    return paramaterFunc.equals(argumentFunc);
                }
            }
            return false; //Unused
        } else if (paramaterRepresented.size() == 1 && argumentRepresented.isEmpty()
            && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
            && internalReturnsRepresented.isEmpty() && externalReturnsRepresented.isEmpty()
            && localVariableCount == 0){
            return true;
        } else if(internalReturnsRepresented.size() == 1 && externalReturnsRepresented.size() == 1
                && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
                && argumentRepresented.isEmpty() && paramaterRepresented.isEmpty()
                && localVariableCount == 0){
            for(String internalReturn: internalReturnsRepresented){
                for(String externalReturn: externalReturnsRepresented){
                    return internalReturn.equals(externalReturn);
                }
            }
            return false; //Unused
        } else if(internalReturnsRepresented.size() == 1 && externalReturnsRepresented.isEmpty()
        && internalIdentsRepresented.isEmpty() && externalIdentsRepresented.isEmpty()
        && argumentRepresented.isEmpty() && paramaterRepresented.isEmpty()
        && localVariableCount == 0){
            return true;
        } else {
            return false;
        }
    }

    private static boolean labelIsUniqueAcrossProgramAndLibraries(String label, Prog program, Lib[] libraries, Lib libraryToIgnore){
        if(!program.equals(libraryToIgnore)){
            if(program.containsLabel(label))
                return false;
        }

        for(Lib library : libraries){
            if(!library.equals(libraryToIgnore)){
                if(library.containsLabel(label))
                    return false;
            }
        }

        return true;
    }

    private static boolean labelIsUniqueAcrossLibraries(String label, Lib library, Lib[] libraries, Lib libToIgnore){
        if(!library.equals(libToIgnore)){
            if(library.containsLabel(label))
                return false;
        }

        for(Lib lib : libraries){
            if(!lib.equals(libToIgnore)){
                if(lib.containsLabel(label))
                    return false;
            }
        }

        return true;
    }

    private void linkDataSections(Prog startingProgram, Lib[] libraries, Prog newProg, Map<Lib, List<ICode>> addedInstructions){
        SymSec symbolTable = newProg.symbols;
        DataSec dataSec = newProg.variables;
        CodeSec codeSec = newProg.code;
        ProcSec procedures = newProg.procedures;
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
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, addedInstructions);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, addedInstructions);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, addedInstructions);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, addedInstructions);
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;
                
                    if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, startingProgram, libraries, addedInstructions)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries, addedInstructions));

                        replacePlaceAcrossProgramAndLibraries(assign.place, place, startingProgram, libraries, startingProgram, addedInstructions);
                    }
                
                    if(!procedures.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, startingProgram, libraries, newProg, addedInstructions);
                    Proc procedure = procedures.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = procedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                    } else {
                        List<Assign> newArgs = new LinkedList<Assign>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                            
                            if(programSymbolTable.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                SymEntry entry = programSymbolTable.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, addedInstructions);
                            }

                            String place = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        List<ICode> addList = addedInstructions.get(startingProgram);

                        Call newCall = new Call(call.procedureName, newArgs);
                        dataSec.addInstruction(newCall);
                        addList.add(newCall);

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assign.place;
                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                        dataSec.addInstruction(newPlace);
                        addList.add(newPlace);

                        continue;
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof ExternalCall){
                ExternalCall call = (ExternalCall)instruction;
                
                if(!procedures.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, startingProgram, libraries, newProg, addedInstructions);
                Proc procedure = procedures.getProcedureByName(call.procedureName);

                int numberOfArgsInProc = procedure.paramAssign.size();
                int numberOfArgsInCall = call.arguments.size();

                if(numberOfArgsInCall != numberOfArgsInProc){
                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                } else {
                    List<Assign> newArgs = new LinkedList<Assign>();
                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                        Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                        
                        if(programSymbolTable.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, addedInstructions);
                        }

                        String place = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                    }

                    List<ICode> addList = addedInstructions.get(startingProgram);
                    Call newCall = new Call(call.procedureName, newArgs);
                    dataSec.addInstruction(newCall);
                    addList.add(newCall);
                    continue;
                }
            }
        }
    }

    private void linkDataSections(Lib startingLibrary, Lib[] libraries, Lib newLib, Map<Lib, List<ICode>> addedInstructions){
        SymSec symbolTable = newLib.symbols;
        DataSec dataSec = newLib.variables;
        ProcSec procedures = newLib.procedures;

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
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, addedInstructions);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, addedInstructions);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, addedInstructions);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, addedInstructions);
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;
                
                    if(!placeIsUniqueAcrossLibraries(assign.place, startingLibrary, libraries, addedInstructions)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries, addedInstructions));

                        replacePlaceAcrossLibraries(assign.place, place, startingLibrary, libraries, startingLibrary, addedInstructions);
                    }
                
                    if(!procedures.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, startingLibrary, libraries, newLib, addedInstructions);
                    Proc procedure = procedures.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = procedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                    } else {
                        List<Assign> newArgs = new LinkedList<Assign>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                            
                            if(programSymbolTable.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                SymEntry entry = programSymbolTable.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, addedInstructions);
                            }

                            String place = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        List<ICode> addList = addedInstructions.get(startingLibrary);

                        Call newCall = new Call(call.procedureName, newArgs);
                        dataSec.addInstruction(newCall);
                        addList.add(newCall);

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assign.place;
                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                        dataSec.addInstruction(newPlace);
                        addList.add(newPlace);

                        continue;
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof ExternalCall){
                ExternalCall call = (ExternalCall)instruction;
                
                if(!procedures.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, startingLibrary, libraries, newLib, addedInstructions);
                Proc procedure = procedures.getProcedureByName(call.procedureName);

                int numberOfArgsInProc = procedure.paramAssign.size();
                int numberOfArgsInCall = call.arguments.size();

                if(numberOfArgsInCall != numberOfArgsInProc){
                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                } else {
                    List<Assign> newArgs = new LinkedList<Assign>();
                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                        Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                        
                        if(programSymbolTable.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, addedInstructions);
                        }
                        String place = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                    }

                    Call newCall = new Call(call.procedureName, newArgs);
                    dataSec.addInstruction(newCall);

                    List<ICode> addList = addedInstructions.get(startingLibrary);
                    addList.add(newCall);

                    continue;
                }
            }
        }
    }

    private void linkCodeSection(Prog program, Lib[] libraries, Prog newProg, Map<Lib, List<ICode>> addedInstructions){
        SymSec symbolTable = newProg.symbols;
        SymSec programTable = program.symbols;
        DataSec dataSection = newProg.variables;
        ProcSec procedureSec = newProg.procedures;
        CodeSec codeSection = newProg.code;
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
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp unExp = (UnExp)assignExp;
                    
                    if(unExp.right instanceof IdentExp){
                        IdentExp ident = (IdentExp)unExp.right;
                        if(programTable.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }
                    }
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    if(binExp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)binExp.left;
                        if(programTable.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }
                    }

                    if(binExp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)binExp.right;
                        if(programTable.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;

                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, program, libraries, addedInstructions)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, addedInstructions));

                        replacePlaceAcrossProgramAndLibraries(assignment.place, place, program, libraries, program, addedInstructions);
                    }
                
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, program, libraries, newProg, addedInstructions);
                    Proc procedure = procedureSec.getProcedureByName(call.procedureName);

                    int numberOfArgsInProc = procedure.paramAssign.size();
                    int numberOfArgsInCall = call.arguments.size();

                    if(numberOfArgsInCall != numberOfArgsInProc){
                        errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                    } else {
                        List<Assign> newArgs = new LinkedList<Assign>();
                        for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                            Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                            
                            if(programTable.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                SymEntry entry = programTable.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                            }

                            String place = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        List<ICode> addedToProgram = addedInstructions.get(program);

                        Call newCall = new Call(call.procedureName, newArgs);
                        codeSection.addInstruction(newCall);
                        addedToProgram.add(newCall);

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assignment.place;
                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignment.getType());
                        codeSection.addInstruction(newPlace);
                        addedToProgram.add(newPlace);

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
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                    }
                }

                if(exp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)exp.right;
                    if(programTable.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                    }
                }

                if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifTrue, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(ifStat.ifTrue);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                    
                    program.replaceLabel(ifStat.ifTrue, newLabel);
                }

                if(!labelIsUniqueAcrossProgramAndLibraries(ifStat.ifFalse, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(ifStat.ifFalse);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                    
                    program.replaceLabel(ifStat.ifFalse, newLabel);
                }
            } else if(icode instanceof ExternalCall){
                ExternalCall call = (ExternalCall)icode;
                
                if(!procedureSec.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, program, libraries, newProg, addedInstructions);
                Proc procedure = procedureSec.getProcedureByName(call.procedureName);

                int numberOfArgsInProc = procedure.paramAssign.size();
                int numberOfArgsInCall = call.arguments.size();

                if(numberOfArgsInCall != numberOfArgsInProc){
                    errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(i, 0));
                } else {
                    List<Assign> newArgs = new LinkedList<Assign>();
                    for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                        Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                        
                        if(programTable.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                        }

                        String place = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                    }

                    Call newCall = new Call(call.procedureName, newArgs);
                    codeSection.addInstruction(newCall);
                    List<ICode> addedToProgram = addedInstructions.get(program);
                    addedToProgram.add(newCall);
                    continue;
                }
            } else if(icode instanceof Call){
                Call call = (Call)icode;
                if(!procedureSec.containsProcedure(call.pname))
                    fetchInternalProcedure(program, call.pname, libraries, newProg, addedInstructions);

                for(Assign arg : call.params){
                    String place = arg.value.toString();

                    if(programTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg, addedInstructions);
                    }
                }
            } else if(icode instanceof Goto){
                Goto gotoICode = (Goto)icode;
                if(!labelIsUniqueAcrossProgramAndLibraries(gotoICode.label, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(gotoICode.label);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                    program.replaceLabel(gotoICode.label, newLabel);
                }
            } else if(icode instanceof Label){
                Label labelICode = (Label)icode;
                if(!labelIsUniqueAcrossProgramAndLibraries(labelICode.label, program, libraries, program)){
                    String newLabel;
                    LabelGenerator lGen = new LabelGenerator(labelICode.label);
                    do{
                        newLabel = lGen.genNext();
                    } while(!labelIsUniqueAcrossProgramAndLibraries(newLabel, program, libraries, program));
                    program.replaceLabel(labelICode.label, newLabel);
                }
            }

            codeSection.addInstruction(icode);
        }
    }

    private void linkProcedureSections(Lib library, Lib[] libraries, Lib newLib, Map<Lib, List<ICode>> addedInstructions){
        ProcSec procedures = newLib.procedures;
        ProcSec libraryProcSec = library.procedures;
        for(int i = 0; i < libraryProcSec.getLength(); i++){
            Proc procedure = libraryProcSec.getProcedureByIndex(i);
            if(!procedures.containsProcedure(procedure.label.label))
                fetchInternalProcedure(library, procedure.label.label, library, libraries, newLib, addedInstructions);
        }

        for(Lib lib : libraries){
            ProcSec libProcSec = lib.procedures;
            for(int i = 0; i < libProcSec.getLength(); i++){
                Proc procedure = libProcSec.getProcedureByIndex(i);
                if(!procedures.containsProcedure(procedure.label.label))
                    fetchInternalProcedure(lib, procedure.label.label, library, libraries, newLib, addedInstructions);
            }
        }
    }

    public Prog performLinkage(Prog program, Lib... libraries){
        Prog newProg = new Prog();
        HashMap<Lib, List<ICode>> addedInstructions = new HashMap<Lib, List<ICode>>();
        addedInstructions.put(program, new LinkedList<ICode>());
        for(Lib lib: libraries){
            addedInstructions.put(lib, new LinkedList<ICode>());
        }
        linkDataSections(program, libraries, newProg, addedInstructions);
        linkCodeSection(program, libraries, newProg, addedInstructions);
        return newProg;
    }

    public Prog performLinkage(Program prog, Library... libraries){
        Prog generatedProgram = generateProgram(errLog, prog);
        Lib[] libs = generateLibraries(errLog, libraries);
        return performLinkage(generatedProgram, libs);
    }

    public Lib performLinkage(Lib library, Lib... libraries){
        Lib newLib = new Lib();
        HashMap<Lib, List<ICode>> addedInstructions = new HashMap<Lib, List<ICode>>();
        addedInstructions.put(library, new LinkedList<ICode>());
        for(Lib lib: libraries){
            addedInstructions.put(lib, new LinkedList<ICode>());
        }
        linkDataSections(library, libraries, newLib, addedInstructions);
        linkProcedureSections(library, libraries, newLib, addedInstructions);
        return newLib;
    }

    public Lib performLinkage(Library library, Library... libraries){
        Lib lib = generateLibrary(errLog, library);
        Lib[] libs = generateLibraries(errLog, libraries);
        return performLinkage(lib, libs);
    }
}
