package io.github.H20man13.DeClan.main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    private void fetchExternalDependentInstructions(String identName, Prog program, Lib[] libraries, Prog newProgram, Lib... libsToIgnore){
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
                                                fetchInternalProcedure(library, funcCall.pname, program, libraries, newProgram);

                                            int numArgs = funcCall.params.size();
                                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                                Assign arg = funcCall.params.get(argIndex);
                                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, arg.value.toString(), newProgram);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, library)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries, library));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, newPlace, program, libraries, library);
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
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram);
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, library));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
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
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, library));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
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
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, leftIdent.ident, newProgram);
                                                }
                                            }

                                            if(binary.right instanceof IdentExp){
                                                IdentExp rightIdent = (IdentExp)binary.right;
                                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, program, libraries, rightIdent.ident, newProgram);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, library));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
                                            }

                                            if(!newProgram.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof ExternalCall){
                                            ExternalCall call = (ExternalCall)exp;
                                            if(!procSec.containsProcedure(call.procedureName))
                                                fetchExternalProcedure(call.procedureName, program, libraries, newProgram, library);
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
                                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                                    } else {
                                                        fetchInternalDependentInstructions(library, program, libraries, value.source, newProgram);
                                                    }

                                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                                }

                                                if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, library)){
                                                    String place = null;    
                                                    do{
                                                        place = gen.genNext();
                                                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, library));

                                                    replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
                                                }

                                                Call newCall = new Call(call.procedureName, newArgs);
                                                String toRetFrom = fetchedProcedure.placement.place;
                                                String toRetTo = assignLib.place;
                                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignLib.getType());

                                                if(!newProgram.dataSectionContainsInstruction(newCall) && !newProgram.dataSectionContainsInstruction(newPlace)){
                                                    dataInstructions.addInstruction(newCall);
                                                    dataInstructions.addInstruction(newPlace);
                                                }
                                                if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                    newTable.addEntry(libEntry);
                                            }
                                        } else {
                                            if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, library));

                                                replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
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

    private void fetchExternalDependentInstructions(String identName, Lib single, Lib[] libraries, Lib newLib, Lib... libsToIgnore){
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
                                                fetchInternalProcedure(library, funcCall.pname, single, libraries, newLib);

                                            int numArgs = funcCall.params.size();
                                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                                Assign arg = funcCall.params.get(argIndex);
                                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, arg.value.toString(), newLib);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, library)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, library));

                                                replacePlaceAcrossLibraries(assignLib.place, newPlace, single, libraries, library);
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
                                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib);
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
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
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));
                                                
                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
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
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, leftIdent.ident, newLib);
                                                }
                                            }

                                            if(binary.right instanceof IdentExp){
                                                IdentExp rightIdent = (IdentExp)binary.right;
                                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                                } else {
                                                    fetchInternalDependentInstructions(library, single, libraries, rightIdent.ident, newLib);
                                                }
                                            }

                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));
                                                
                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
                                            }

                                            if(!newLib.dataSectionContainsInstruction(assignLib))
                                                dataInstructions.addInstruction(assignLib);
                                            if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                newTable.addEntry(libEntry);
                                        } else if(exp instanceof ExternalCall){
                                            ExternalCall call = (ExternalCall)exp;
                                            if(!procSec.containsProcedure(call.procedureName))
                                                fetchExternalProcedure(call.procedureName, single, libraries, newLib, library);
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
                                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                                    } else {
                                                        fetchInternalDependentInstructions(library, single, libraries, value.source, newLib);
                                                    }

                                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                                }

                                                if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, library)){
                                                    String place = null;    
                                                    do{
                                                        place = gen.genNext();
                                                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));
                                                    
                                                    replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
                                                }

                                                Call newCall = new Call(call.procedureName, newArgs);
                                                String toRetFrom = fetchedProcedure.placement.place;
                                                String toRetTo = assignLib.place;
                                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assignLib.getType());

                                                if(!newLib.dataSectionContainsInstruction(newCall) && !newLib.dataSectionContainsInstruction(newPlace)){
                                                    dataInstructions.addInstruction(newCall);
                                                    dataInstructions.addInstruction(newPlace);
                                                }
                                                if(!newTable.containsEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                                    newTable.addEntry(libEntry);
                                            }
                                        } else {
                                            if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries, library)){
                                                String place = null;    
                                                do{
                                                    place = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));
                                                
                                                replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
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

    private void fetchInternalDependentInstructions(Lib currentLib, Lib single, Lib[] libraries, String labelName, Lib newLib){
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
                                fetchInternalProcedure(currentLib, funcCall.pname, single, libraries, newLib);

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Assign arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, arg.value.toString(), newLib);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, currentLib)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, currentLib));

                                replacePlaceAcrossLibraries(assign.place, newPlace, single, libraries, currentLib);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib);
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, currentLib));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, currentLib));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, leftIdent.ident, newLib);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, rightIdent.ident, newLib);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, currentLib));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
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
                                fetchExternalProcedure(call.procedureName, single, libraries, newLib, currentLib);
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
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                    } else {
                                        fetchInternalDependentInstructions(currentLib, single, libraries, value.source, newLib);
                                    }

                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }

                                if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, currentLib)){
                                    String place = null;    
                                    do{
                                        place = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, currentLib));
                                    
                                    replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
                                }

                                Call newCall = new Call(call.procedureName, newArgs);
                                String toRetFrom = fetchedProcedure.placement.place;
                                String toRetTo = assign.place;
                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());

                                if(!newLib.dataSectionContainsInstruction(newCall) && !newLib.dataSectionContainsInstruction(newPlace)){
                                    dataInstructions.addInstruction(newCall);
                                    dataInstructions.addInstruction(newPlace);
                                }
                                if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                                }
                            }
                        } else {
                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries, currentLib));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
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

     private void fetchInternalDependentInstructions(Lib currentLib, Prog program, Lib[] libraries, String labelName, Prog newProgram){
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
                                fetchInternalProcedure(currentLib, funcCall.pname, program, libraries, newProgram);

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Assign arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, arg.value.toString(), newProgram);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, currentLib)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries, currentLib));

                                replacePlaceAcrossProgramAndLibraries(assign.place, newPlace, program, libraries, currentLib);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram);
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, currentLib));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, currentLib));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, leftIdent.ident, newProgram);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, rightIdent.ident, newProgram);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, currentLib));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
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
                                fetchExternalProcedure(call.procedureName, program, libraries, newProgram, currentLib);
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
                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                    } else {
                                        fetchInternalDependentInstructions(currentLib, program, libraries, value.source, newProgram);
                                    }

                                    String place = fetchedProcedure.paramAssign.get(argIndex).place;
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }

                                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, currentLib)){
                                    String place = null;    
                                    do{
                                        place = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, currentLib));

                                    replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
                                }

                                Call newCall = new Call(call.procedureName, newArgs);
                                String toRetFrom = fetchedProcedure.placement.place;
                                String toRetTo = assign.place;
                                Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());

                                if(!newProgram.dataSectionContainsInstruction(newCall) && !newProgram.dataSectionContainsInstruction(newPlace)){
                                    dataInstructions.addInstruction(newCall);
                                    dataInstructions.addInstruction(newPlace);
                                }
                                if(libSymbols.containsEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                    if(!newTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        newTable.addEntry(entry);
                                }
                            }
                        } else {
                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, currentLib)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, currentLib));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
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

    private void fetchInternalProcedure(Prog program, String procName, Lib[] libraries, Prog newProg){
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

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, program));

                    replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, program, libraries, program);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, program));

                    replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, program);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, program));

                    replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, program, libraries, program);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, program, libraries, program)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, program));

                    replacePlaceAcrossProgramAndLibraries(placement.place, place, program, libraries, program);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, program, libraries, program)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries, program));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, program, libraries, program);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;
                    
                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, program, libraries, newProg);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                                }

                                String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                            }

                            Call newCall = new Call(call.procedureName, newArgs);
                            newProcedure.addInstruction(newCall);
                            Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, assignment.place, new IdentExp(fetchedProcedure.placement.place), fetchedProcedure.placement.getType());
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
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
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
                        fetchExternalProcedure(call.procedureName, program, libraries, newProg);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }

                            String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }
                        
                        newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(program, call.pname, libraries, newProg);
                    
                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
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

    private void fetchInternalProcedure(Lib library, String procName, Prog prog, Lib[] libraries, Prog newProg){
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

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                    replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, prog, libraries, library);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                    replacePlaceAcrossProgramAndLibraries(assign.place, place, prog, libraries, library);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                    replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, prog, libraries, library);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, prog, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                    replacePlaceAcrossProgramAndLibraries(placement.place, place, prog, libraries, library);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries, library)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries, library));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(ident.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProcedure.containsPlace(ident.ident)) {
                                fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;
                    
                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, prog, libraries, newProg, library);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(value.source)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg);
                                }

                                String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
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
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(leftExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(rightExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
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
                        fetchExternalProcedure(call.procedureName, prog, libraries, newProg, library);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProcedure.containsPlace(value.source)) {
                                fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg);
                            }

                            String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }
                        
                        newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(library, call.pname, prog, libraries, newProg);
                    
                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(place)) {
                            fetchInternalDependentInstructions(library, prog, libraries, place, newProg);
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

    private void fetchInternalProcedure(Lib library, String procName, Lib single, Lib[] libraries, Lib newLib){
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

                if(!placeIsUniqueAcrossLibraries(assign.value.toString(), single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                    replacePlaceAcrossLibraries(assign.value.toString(), place, single, libraries, library);
                }

                if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                    replacePlaceAcrossLibraries(assign.place, place, single, libraries, library);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossLibraries(placement.place, single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                    replacePlaceAcrossLibraries(placement.place, place, single, libraries, library);
                }

                if(!placeIsUniqueAcrossLibraries(placement.value.toString(), single, libraries, library)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                    replacePlaceAcrossLibraries(placement.value.toString(), place, single, libraries, library);
                }

                if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries, library)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, library));
                        
                        replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(ident.ident)){
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!newProcedure.containsPlace(ident.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                            }
                        }
                    } else if(assignExp instanceof ExternalCall){
                        ExternalCall call = (ExternalCall)assignExp;

                        if(!procedureSec.containsProcedure(call.procedureName))
                            fetchExternalProcedure(call.procedureName, single, libraries, newLib, library);
                        Proc fetchedProcedure = procedureSec.getProcedureByName(call.procedureName);

                        int numberOfArgsInProc = fetchedProcedure.paramAssign.size();
                        int numberOfArgsInCall = call.arguments.size();

                        if(numberOfArgsInCall != numberOfArgsInProc){
                            errLog.add("In call " + call.toString() + " expected " + numberOfArgsInCall + " but found procedure with " + numberOfArgsInProc + " arguments", new Position(instructionIndex, 0));
                        } else {
                            List<Assign> newArgs = new LinkedList<Assign>();
                            for(int argIndex = 0; argIndex < numberOfArgsInCall; argIndex++){
                                Tuple<String, Assign.Type> value = call.arguments.get(argIndex);
                                Tuple<String, String> newArg = new Tuple<String,String>("", "");
                                
                                if(libSymbols.containsEntryWithICodePlace(value.source, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(value.source, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(value.source)){
                                    fetchInternalDependentInstructions(library, single, libraries, value.source, newLib);
                                }

                                String source = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                newArgs.add(new Assign(Scope.ARGUMENT, source, new IdentExp(value.source), value.dest));
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
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(leftExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(rightExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
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
                        fetchExternalProcedure(call.procedureName, single, libraries, newLib, library);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!newProcedure.containsPlace(value.source)) {
                                fetchInternalDependentInstructions(library, single, libraries, value.source, newLib);
                            }

                            String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        } 
                        
                        newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                        continue;
                    }
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname))
                        fetchInternalProcedure(library, call.pname, single, libraries, newLib);

                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(place)) {
                            fetchInternalDependentInstructions(library, single, libraries, place, newLib);
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

    private void fetchExternalProcedure(String procName, Prog prog, Lib[] libraries, Prog newProg, Lib... libsToIgnore){
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

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                            replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, prog, libraries, library);
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                            replacePlaceAcrossProgramAndLibraries(assign.place, place, prog, libraries, library);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                            replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, prog, libraries, library);
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, prog, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries, library));

                            replacePlaceAcrossProgramAndLibraries(placement.place, place, prog, libraries, library);
                        }

                        if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);


                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries, library)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries, library));
                                
                                replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(ident.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library, library);
                                    } else if(!newProcedure.containsPlace(ident.ident)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(!newProcedure.containsPlace(rightExp.ident)){
                                        fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                                    }
                                }
                            } else if(assignExp instanceof ExternalCall){
                                ExternalCall call = (ExternalCall)assignExp;
                            
                                if(!procedureSec.containsProcedure(call.procedureName))
                                    fetchExternalProcedure(call.procedureName, prog, libraries, newProg, library);
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
                                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                        } else if(!newProcedure.containsPlace(value.source)){
                                            fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg);
                                        }

                                        String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();

                                        newArg = new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest);
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
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(rightExp.ident)){
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
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
                                fetchExternalProcedure(call.procedureName, prog, libraries, newProg, library);
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
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(!newProcedure.containsPlace(value.source)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, value.source, newProg);
                                    }

                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }
                                
                                newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                                continue;
                            }
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname))
                                fetchInternalProcedure(library, call.pname, prog, libraries, newProg);

                            for(Assign arg : call.params){
                                String place = arg.value.toString();

                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(place)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, place, newProg);
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

    private void fetchExternalProcedure(String procName, Lib single, Lib[] libraries, Lib newLib, Lib... libsToIgnore){
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

                        if(!placeIsUniqueAcrossLibraries(assign.value.toString(), single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                            replacePlaceAcrossLibraries(assign.value.toString(), place, single, libraries, library);
                        }

                        if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                            replacePlaceAcrossLibraries(assign.place, place, single, libraries, library);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueAcrossLibraries(placement.value.toString(), single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                            replacePlaceAcrossLibraries(placement.value.toString(), place, single, libraries, library);
                        }

                        if(!placeIsUniqueAcrossLibraries(placement.place, single, libraries, library)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries, library));

                            replacePlaceAcrossLibraries(placement.place, place, single, libraries, library);
                        }

                        if(libSymbols.containsEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            SymEntry entry = libSymbols.getEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);

                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries, library)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries, library));
                                
                                replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(ident.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newProcedure.containsPlace(ident.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                    }
                                }
                            } else if(assignExp instanceof ExternalCall){
                                ExternalCall call = (ExternalCall)assignExp;

                                if(!procedureSec.containsProcedure(call.procedureName))
                                    fetchExternalProcedure(call.procedureName, single, libraries, newLib, library);
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
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                        } else if(!newProcedure.containsPlace(value.source)) {
                                            fetchInternalDependentInstructions(library, single, libraries, value.source, newLib);
                                        }

                                        String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
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
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
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
                                fetchExternalProcedure(call.procedureName, single, libraries, newLib, library);
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
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newProcedure.containsPlace(value.source)) {
                                        fetchInternalDependentInstructions(library, single, libraries, value.source, newLib);
                                    }
                                    String place = fetchedProcedure.paramAssign.get(argIndex).value.toString();
                                    newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                                }
                                
                                newProcedure.addInstruction(new Call(call.procedureName, newArgs));

                                continue;
                            }
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname))
                                fetchInternalProcedure(library, call.pname, single, libraries, newLib);

                            for(Assign arg : call.params){
                                String place = arg.value.toString();

                                if(libSymbols.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    SymEntry entry = libSymbols.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(place)) {
                                    fetchInternalDependentInstructions(library, single, libraries, place, newLib);
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

    private static void replacePlaceAcrossProgramAndLibraries(String oldPlace, String newPlace, Prog program, Lib[] libraries, Lib libToAlwaysReplace){
        libToAlwaysReplace.replacePlace(oldPlace, newPlace);
        if(libToAlwaysReplace.containsExternalSymbolByPlace(newPlace)){
            SymEntry entryToLook = libToAlwaysReplace.getExternalSymbolByPlace(newPlace);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsExternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = program.getExternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        program.replacePlace(placeFrom, newPlace);
                    }
                } else if(program.containsInternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = program.getInternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        program.replacePlace(placeFrom, newPlace);
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
                        }
                    } else if(library.containsInternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getInternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
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
                        }
                    }
                }
            }
        }
    }

    private static boolean placeIsUniqueAcrossProgramAndLibraries(String place, Prog program, Lib[] libraries, Lib libraryToIgnore){
        if(libraryToIgnore.containsExternalSymbolByPlace(place)){
            SymEntry origEntry = libraryToIgnore.getExternalSymbolByPlace(place);

            int libsWithInternal = 0;
            if(!program.equals(libraryToIgnore)){
                if(program.containsExternalSymbolByPlace(place)){
                    SymEntry entryToMatch = program.getExternalSymbolByPlace(place);
                    if(!origEntry.declanIdent.equals(entryToMatch.declanIdent))
                        return false;
                } else if(program.containsInternalSymbolByPlace(place)){
                    SymEntry entryToMatch = program.getInternalSymbolByPlace(place);
                    if(origEntry.declanIdent.equals(entryToMatch.declanIdent))
                        libsWithInternal++;
                    else
                        return false;
                } else if (program.containsPlace(place)) {
                    return false;
                }
            }

            for(Lib library : libraries){
                if(!library.equals(libraryToIgnore)){
                    if(library.containsExternalSymbolByPlace(place)){
                        SymEntry entryToMatch = library.getExternalSymbolByPlace(place);
                        if(!origEntry.declanIdent.equals(entryToMatch.declanIdent))
                            return false;
                    } else if(library.containsInternalSymbolByPlace(place)){
                        SymEntry entryToMatch = library.getInternalSymbolByPlace(place);
                        if(origEntry.declanIdent.equals(entryToMatch.declanIdent)) {
                            libsWithInternal++;
                            if(libsWithInternal > 1)
                                return false;
                        } else { 
                            return false;
                        }
                    } else if (library.containsPlace(place)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            if(!program.equals(libraryToIgnore))
                if(program.containsPlace(place))
                    return false;

            for(Lib library : libraries){
                if(!library.equals(libraryToIgnore))
                    if(library.containsPlace(place))
                        return false;
            }

            return true;
        }
    }

    private static void replacePlaceAcrossLibraries(String oldPlace, String newPlace, Lib lib, Lib[] libraries, Lib libToAlwaysReplace){
        libToAlwaysReplace.replacePlace(oldPlace, newPlace);
        if(libToAlwaysReplace.containsExternalSymbolByPlace(newPlace)){
            SymEntry entryToLook = libToAlwaysReplace.getExternalSymbolByPlace(newPlace);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsExternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = lib.getExternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        lib.replacePlace(placeFrom, newPlace);
                    }
                } else if(lib.containsInternalSymbolByIdent(ident)){
                    List<SymEntry> entriesToReplace = lib.getInternalSymbolsByIdent(ident);
                    for(SymEntry entryToReplace: entriesToReplace){
                        String placeFrom = entryToReplace.icodePlace;
                        lib.replacePlace(placeFrom, newPlace);
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
                        }
                    } else if(library.containsInternalSymbolByIdent(ident)){
                        List<SymEntry> entriesToReplace = library.getInternalSymbolsByIdent(ident);
                        for(SymEntry entryToReplace: entriesToReplace){
                            String fromPlace = entryToReplace.icodePlace;
                            library.replacePlace(fromPlace, newPlace);
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
                        }
                    }
                }
            }
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

    private static boolean placeIsUniqueAcrossLibraries(String place, Lib library, Lib[] libraries, Lib libraryToIgnore){
        if(libraryToIgnore.containsExternalSymbolByPlace(place)){
            SymEntry origEntry = libraryToIgnore.getExternalSymbolByPlace(place);

            int libsWithInternal = 0;
            if(!library.equals(libraryToIgnore)){
                if(library.containsExternalSymbolByPlace(place)){
                    SymEntry entryToMatch = library.getExternalSymbolByPlace(place);
                    if(!origEntry.declanIdent.equals(entryToMatch.declanIdent))
                        return false;
                } else if(library.containsInternalSymbolByPlace(place)){
                    SymEntry entryToMatch = library.getInternalSymbolByPlace(place);
                    if(origEntry.declanIdent.equals(entryToMatch.declanIdent))
                        libsWithInternal++;
                    else
                        return false;
                } else if (library.containsPlace(place)) {
                    return false;
                }
            }

            for(Lib lib : libraries){
                if(!lib.equals(libraryToIgnore)){
                    if(lib.containsExternalSymbolByPlace(place)){
                        SymEntry entryToMatch = lib.getExternalSymbolByPlace(place);
                        if(!origEntry.declanIdent.equals(entryToMatch.declanIdent))
                            return false;
                    } else if(lib.containsInternalSymbolByPlace(place)){
                        SymEntry entryToMatch = lib.getInternalSymbolByPlace(place);
                        if(origEntry.declanIdent.equals(entryToMatch.declanIdent)) {
                            libsWithInternal++;
                            if(libsWithInternal > 1)
                                return false;
                        } else { 
                            return false;
                        }
                    } else if (lib.containsPlace(place)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            if(!library.equals(libraryToIgnore))
                if(library.containsPlace(place))
                    return false;

            for(Lib lib : libraries){
                if(!lib.equals(libraryToIgnore))
                    if(lib.containsPlace(place))
                        return false;
            }

            return true;
        }
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

    private void linkDataSections(Prog startingProgram, Lib[] libraries, Prog newProg){
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
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;
                
                    if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, startingProgram, libraries, startingProgram)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries, startingProgram));

                        replacePlaceAcrossProgramAndLibraries(assign.place, place, startingProgram, libraries, startingProgram);
                    }
                
                    if(!procedures.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, startingProgram, libraries, newProg);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                            }

                            String place = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        Call newCall = new Call(call.procedureName, newArgs);
                        dataSec.addInstruction(newCall);

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assign.place;
                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                        dataSec.addInstruction(newPlace);

                        continue;
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof ExternalCall){
                ExternalCall call = (ExternalCall)instruction;
                
                if(!procedures.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, startingProgram, libraries, newProg);
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
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String place = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                    }

                    Call newCall = new Call(call.procedureName, newArgs);
                    dataSec.addInstruction(newCall);
                    continue;
                }
            }
        }
    }

    private void linkDataSections(Lib startingLibrary, Lib[] libraries, Lib newLib){
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
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programSymbolTable.getEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programSymbolTable.getEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;
                
                    if(!placeIsUniqueAcrossLibraries(assign.place, startingLibrary, libraries, startingLibrary)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries, startingLibrary));

                        replacePlaceAcrossLibraries(assign.place, place, startingLibrary, libraries, startingLibrary);
                    }
                
                    if(!procedures.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, startingLibrary, libraries, newLib);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                            }

                            String place = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                        }

                        Call newCall = new Call(call.procedureName, newArgs);
                        dataSec.addInstruction(newCall);

                        String toRetFrom = procedure.placement.place;
                        String toRetTo = assign.place;
                        Assign newPlace = new Assign(Scope.EXTERNAL_RETURN, toRetTo, new IdentExp(toRetFrom), assign.getType());
                        dataSec.addInstruction(newPlace);

                        continue;
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof ExternalCall){
                ExternalCall call = (ExternalCall)instruction;
                
                if(!procedures.containsProcedure(call.procedureName))
                    fetchExternalProcedure(call.procedureName, startingLibrary, libraries, newLib);
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
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }
                        String place = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                    }

                    Call newCall = new Call(call.procedureName, newArgs);
                    dataSec.addInstruction(newCall);

                    continue;
                }
            }
        }
    }

    private void linkCodeSection(Prog program, Lib[] libraries, Prog newProg){
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
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp unExp = (UnExp)assignExp;
                    
                    if(unExp.right instanceof IdentExp){
                        IdentExp ident = (IdentExp)unExp.right;
                        if(programTable.containsEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }
                    }
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    if(binExp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)binExp.left;
                        if(programTable.containsEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }
                    }

                    if(binExp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)binExp.right;
                        if(programTable.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            SymEntry entry = programTable.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }
                    }
                } else if(assignExp instanceof ExternalCall){
                    ExternalCall call = (ExternalCall)assignExp;

                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, program, libraries, program)){
                        String place = null;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries, program));

                        replacePlaceAcrossProgramAndLibraries(assignment.place, place, program, libraries, program);
                    }
                
                    if(!procedureSec.containsProcedure(call.procedureName))
                        fetchExternalProcedure(call.procedureName, program, libraries, newProg);
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
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }

                            String place = procedure.paramAssign.get(argIndex).value.toString();
                            newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
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
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }
                }

                if(exp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)exp.right;
                    if(programTable.containsEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
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
                    fetchExternalProcedure(call.procedureName, program, libraries, newProg);
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
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String place = procedure.paramAssign.get(argIndex).value.toString();
                        newArgs.add(new Assign(Scope.ARGUMENT, place, new IdentExp(value.source), value.dest));
                    }
                    codeSection.addInstruction(new Call(call.procedureName, newArgs));
                    continue;
                }
            } else if(icode instanceof Call){
                Call call = (Call)icode;
                if(!procedureSec.containsProcedure(call.pname))
                    fetchInternalProcedure(program, call.pname, libraries, newProg);

                for(Assign arg : call.params){
                    String place = arg.value.toString();

                    if(programTable.containsEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                        SymEntry entry = programTable.getEntryByICodePlace(place, SymEntry.EXTERNAL);
                        if(!symbolTable.containsEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
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

    private void linkProcedureSections(Lib library, Lib[] libraries, Lib newLib){
        ProcSec procedures = newLib.procedures;
        ProcSec libraryProcSec = library.procedures;
        for(int i = 0; i < libraryProcSec.getLength(); i++){
            Proc procedure = libraryProcSec.getProcedureByIndex(i);
            if(!procedures.containsProcedure(procedure.label.label))
                fetchInternalProcedure(library, procedure.label.label, library, libraries, newLib);
        }

        for(Lib lib : libraries){
            ProcSec libProcSec = lib.procedures;
            for(int i = 0; i < libProcSec.getLength(); i++){
                Proc procedure = libProcSec.getProcedureByIndex(i);
                if(!procedures.containsProcedure(procedure.label.label))
                    fetchInternalProcedure(lib, procedure.label.label, library, libraries, newLib);
            }
        }
    }

    public Prog performLinkage(Prog program, Lib... libraries){
        Prog newProg = new Prog();
        linkDataSections(program, libraries, newProg);
        linkCodeSection(program, libraries, newProg);
        return newProg;
    }

    public Prog performLinkage(Program prog, Library... libraries){
        Prog generatedProgram = generateProgram(errLog, prog);
        Lib[] libs = generateLibraries(errLog, libraries);
        return performLinkage(generatedProgram, libs);
    }

    public Lib performLinkage(Lib library, Lib... libraries){
        Lib newLib = new Lib();
        linkDataSections(library, libraries, newLib);
        linkProcedureSections(library, libraries, newLib);
        return newLib;
    }

    public Lib performLinkage(Library library, Library... libraries){
        Lib lib = generateLibrary(errLog, library);
        Lib[] libs = generateLibraries(errLog, libraries);
        return performLinkage(lib, libs);
    }
}
