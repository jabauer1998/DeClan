package io.github.H20man13.DeClan.main;

import java.util.HashSet;
import java.util.Set;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.gen.LabelGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Assign.Scope;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
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
                if(libSymbols.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL)){
                    VarSymEntry libEntry = libSymbols.getVariableEntryByIdentifier(identName, SymEntry.INTERNAL);
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

                                        if(!procSec.containsProcedure(funcCall.pname)){
                                            if(libProc.containsProcedure(funcCall.pname))
                                                fetchInternalProcedure(library, funcCall.pname, program, libraries, newProgram);
                                            else
                                                fetchExternalProcedure(funcCall.pname, program, libraries, newProgram, library);
                                        }

                                        int numArgs = funcCall.params.size();
                                        for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                            Assign arg = funcCall.params.get(argIndex);
                                            if(libSymbols.containsVariableEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, arg.value.toString(), newProgram);
                                            }

                                            String oldPlace = arg.place;
                                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                            }
                                        }

                                        if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

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
                                        if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                            if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram);
                                        }

                                        String oldPlace = identExp.ident;
                                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                        }

                                        if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
                                        }

                                        if(!newProgram.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                            newTable.addEntry(libEntry);
                                    } else if(exp instanceof UnExp){
                                        UnExp unary = (UnExp)exp;
                                        if(unary.right instanceof IdentExp){
                                            IdentExp identExp = (IdentExp)unary.right;
                                            if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram);
                                            }

                                            String oldPlace = identExp.ident;
                                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                            }
                                        }

                                        if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
                                        }

                                        if(!newProgram.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                            newTable.addEntry(libEntry);
                                    } else if(exp instanceof BinExp){
                                        BinExp binary = (BinExp)exp;

                                        if(binary.left instanceof IdentExp){
                                            IdentExp leftIdent = (IdentExp)binary.left;
                                            if(libSymbols.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, leftIdent.ident, newProgram);
                                            }

                                            String oldPlace = leftIdent.ident;
                                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                            }
                                        }

                                        if(binary.right instanceof IdentExp){
                                            IdentExp rightIdent = (IdentExp)binary.right;
                                            if(libSymbols.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, program, libraries, rightIdent.ident, newProgram);
                                            }

                                            String oldPlace = rightIdent.ident;
                                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                            }
                                        }

                                        if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
                                        }

                                        if(!newProgram.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                            newTable.addEntry(libEntry);
                                    } else {
                                        if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.place, program, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(assignLib.place, place, program, libraries, library);
                                        }

                                        if(!newProgram.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
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

    private void fetchExternalDependentInstructions(String identName, Lib single, Lib[] libraries, Lib newLib, Lib... libsToIgnore){
        SymSec newTable = newLib.symbols;
        DataSec dataInstructions = newLib.variables;
        ProcSec procSec = newLib.procedures;
        loop: for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                SymSec libSymbols = library.symbols;
                if(libSymbols.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL)){
                    VarSymEntry libEntry = libSymbols.getVariableEntryByIdentifier(identName, SymEntry.INTERNAL);
                    DataSec libData = library.variables;
                    ProcSec libProcSec = library.procedures;
                    for(int z = 0; z < libData.getLength(); z++){
                        ICode icodeLib = libData.getInstruction(z);
                        if(icodeLib instanceof Assign){
                            Assign assignLib = (Assign)icodeLib;
                            if(assignLib.place.equals(libEntry.icodePlace)){
                                if(assignLib.getScope() == Assign.Scope.EXTERNAL_RETURN){
                                    ICode funcCallICode = libData.getInstruction(z - 1);
                                    if(funcCallICode instanceof Call){
                                        Call funcCall = (Call)funcCallICode;

                                        if(!procSec.containsProcedure(funcCall.pname)){
                                            if(libProcSec.containsProcedure(funcCall.pname))
                                                fetchInternalProcedure(library, funcCall.pname, single, libraries, newLib);
                                            else
                                                fetchExternalProcedure(funcCall.pname, single, libraries, newLib, library);
                                        }

                                        int numArgs = funcCall.params.size();
                                        for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                            Assign arg = funcCall.params.get(argIndex);
                                            if(libSymbols.containsVariableEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, single, libraries, arg.value.toString(), newLib);
                                            }

                                            String oldPlace = arg.place;
                                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
    
                                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                            }
                                        }

                                        if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));

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
                                        if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                            if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib);
                                        }

                                        String oldPlace = identExp.ident;
                                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));

                                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                        }

                                        if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                                            replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
                                        }

                                        if(!newLib.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                            newTable.addEntry(libEntry);
                                    } else if(exp instanceof UnExp){
                                        UnExp unary = (UnExp)exp;
                                        if(unary.right instanceof IdentExp){
                                            IdentExp identExp = (IdentExp)unary.right;
                                            if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib);
                                            }

                                            String oldPlace = identExp.ident;
                                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));

                                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                            }
                                        }

                                        if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries));
                                            
                                            replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
                                        }

                                        if(!newLib.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                            newTable.addEntry(libEntry);
                                    } else if(exp instanceof BinExp){
                                        BinExp binary = (BinExp)exp;

                                        if(binary.left instanceof IdentExp){
                                            IdentExp leftIdent = (IdentExp)binary.left;
                                            if(libSymbols.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, single, libraries, leftIdent.ident, newLib);
                                            }

                                            String oldPlace = leftIdent.ident;
                                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));

                                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                            }
                                        }

                                        if(binary.right instanceof IdentExp){
                                            IdentExp rightIdent = (IdentExp)binary.right;
                                            if(libSymbols.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                            } else {
                                                fetchInternalDependentInstructions(library, single, libraries, rightIdent.ident, newLib);
                                            }

                                            String oldPlace = rightIdent.ident;
                                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                                String newPlace = null;    
                                                do{
                                                    newPlace = gen.genNext();
                                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));

                                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                            }
                                        }

                                        if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries));
                                            
                                            replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
                                        }

                                        if(!newLib.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                            newTable.addEntry(libEntry);
                                    } else {
                                        if(!placeIsUniqueAcrossLibraries(assignLib.place, single, libraries)){
                                            String place = null;    
                                            do{
                                                place = gen.genNext();
                                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries));
                                            
                                            replacePlaceAcrossLibraries(assignLib.place, place, single, libraries, library);
                                        }

                                        if(!newLib.dataSectionContainsInstruction(assignLib))
                                            dataInstructions.addInstruction(assignLib);
                                        if(!newTable.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
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

    private void fetchInternalDependentInstructions(Lib currentLib, Lib single, Lib[] libraries, String labelName, Lib newLib){
        DataSec data = currentLib.variables;
        SymSec libSymbols = currentLib.symbols;
        ProcSec procSec = newLib.procedures;
        ProcSec libProcSec = currentLib.procedures;
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

                            if(!procSec.containsProcedure(funcCall.pname)){
                                if(libProcSec.containsProcedure(funcCall.pname))
                                    fetchInternalProcedure(currentLib, funcCall.pname, single, libraries, newLib);
                                else
                                    fetchExternalProcedure(funcCall.pname, single, libraries, newLib, currentLib);
                            }

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Assign arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsVariableEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, arg.value.toString(), newLib);
                                }

                                String oldPlace = arg.place;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
    
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));

                                replacePlaceAcrossLibraries(assign.place, newPlace, single, libraries, currentLib);
                            }

                            if(!newLib.dataSectionContainsInstruction(funcCall) && !newLib.dataSectionContainsInstruction(assign)){
                                dataInstructions.addInstruction(funcCall);
                                dataInstructions.addInstruction(assign);
                            }
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        }
                    } else {
                        Exp exp = assign.value;
                        if(exp instanceof IdentExp){
                            IdentExp identExp = (IdentExp)exp;
                            if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib);
                            }

                            String oldPlace = identExp.ident;
                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));

                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof UnExp){
                            UnExp unary = (UnExp)exp;
                            if(unary.right instanceof IdentExp){
                                IdentExp identExp = (IdentExp)unary.right;
                                if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib);
                                }

                                String oldPlace = identExp.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
    
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof BinExp){
                            BinExp binary = (BinExp)exp;

                            if(binary.left instanceof IdentExp){
                                IdentExp leftIdent = (IdentExp)binary.left;
                                if(libSymbols.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, leftIdent.ident, newLib);
                                }

                                String oldPlace = leftIdent.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
    
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, single, libraries, rightIdent.ident, newLib);
                                }

                                String oldPlace = rightIdent.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
    
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                                }
                            }

                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else {
                            if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(place, single, libraries));
                                
                                replacePlaceAcrossLibraries(assign.place, place, single, libraries, currentLib);
                            }

                            if(!newLib.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
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
        ProcSec libProcSec = currentLib.procedures;
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

                            if(!procSec.containsProcedure(funcCall.pname)){
                                if(libProcSec.containsProcedure(funcCall.pname))
                                    fetchInternalProcedure(currentLib, funcCall.pname, program, libraries, newProgram);
                                else
                                    fetchExternalProcedure(funcCall.pname, program, libraries, newProgram, currentLib);
                            }

                            int numArgs = funcCall.params.size();
                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                Assign arg = funcCall.params.get(argIndex);
                                if(libSymbols.containsVariableEntryWithICodePlace(arg.value.toString(), SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(arg.value.toString(), SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, arg.value.toString(), newProgram);
                                }

                                String oldPlace = arg.place;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(assign.place, newPlace, program, libraries, currentLib);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        }
                    } else {
                        Exp exp = assign.value;
                        if(exp instanceof IdentExp){
                            IdentExp identExp = (IdentExp)exp;
                            if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram);
                            }

                            String oldPlace = identExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof UnExp){
                            UnExp unary = (UnExp)exp;
                            if(unary.right instanceof IdentExp){
                                IdentExp identExp = (IdentExp)unary.right;
                                if(libSymbols.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram);
                                }

                                String oldPlace = identExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else if(exp instanceof BinExp){
                            BinExp binary = (BinExp)exp;

                            if(binary.left instanceof IdentExp){
                                IdentExp leftIdent = (IdentExp)binary.left;
                                if(libSymbols.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, leftIdent.ident, newProgram);
                                }

                                String oldPlace = leftIdent.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                                }
                            }

                            if(binary.right instanceof IdentExp){
                                IdentExp rightIdent = (IdentExp)binary.right;
                                if(libSymbols.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                    if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram,  currentLib);
                                } else {
                                    fetchInternalDependentInstructions(currentLib, program, libraries, rightIdent.ident, newProgram);
                                }

                                String oldPlace = rightIdent.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                    String newPlace = null;    
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                                }
                            }

                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    newTable.addEntry(entry);
                            }
                        } else {
                            if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries)){
                                String place = null;    
                                do{
                                    place = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, currentLib);
                            }

                            if(!newProgram.dataSectionContainsInstruction(assign))
                                dataInstructions.addInstruction(assign);
                            if(libSymbols.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                                if(!newTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
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

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), program, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, program, libraries, program);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, program, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(assign.place, place, program, libraries, program);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), program, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, program, libraries, program);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, program, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(placement.place, place, program, libraries, program);
                }

                if(libSymbols.containsVariableEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, program, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, program, libraries, program);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }

                            String oldPlace = ident.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }

                            String oldPlace = leftExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }

                            String oldPlace = rightExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                            }
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
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
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname)){
                        if(libProcSec.containsProcedure(call.pname))
                            fetchInternalProcedure(program, call.pname, program, libraries, newProg);
                        else
                            fetchExternalProcedure(call.pname, program, libraries, newProg, program);
                    }
                    
                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = arg.place;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
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

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), prog, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                    replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, prog, libraries, library);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, prog, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                    replacePlaceAcrossProgramAndLibraries(assign.place, place, prog, libraries, library);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), prog, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                    replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, prog, libraries, library);
                }

                if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, prog, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                    replacePlaceAcrossProgramAndLibraries(placement.place, place, prog, libraries, library);
                }

                if(libSymbols.containsVariableEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(ident.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProcedure.containsPlace(ident.ident)) {
                                fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                            }

                            String oldPlace = ident.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                            }

                            String oldPlace = leftExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                            }

                            String oldPlace = rightExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                            }
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(leftExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                        }

                        String oldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(rightExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
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
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname)){
                        if(libProcSec.containsProcedure(call.pname))
                            fetchInternalProcedure(library, call.pname, prog, libraries, newProg);
                        else
                            fetchExternalProcedure(call.pname, prog, libraries, newProg, library);
                    }
                    
                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProcedure.containsPlace(place)) {
                            fetchInternalDependentInstructions(library, prog, libraries, place, newProg);
                        }

                        String oldPlace = arg.place;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
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

                if(!placeIsUniqueAcrossLibraries(assign.value.toString(), single, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    }while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                    replacePlaceAcrossLibraries(assign.value.toString(), place, single, libraries, library);
                }

                if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                    replacePlaceAcrossLibraries(assign.place, place, single, libraries, library);
                }

                newProcedure.addParamater(assign);
            }

            if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                Assign placement = procedure.placement;
                if(!placeIsUniqueAcrossLibraries(placement.place, single, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                    replacePlaceAcrossLibraries(placement.place, place, single, libraries, library);
                }

                if(!placeIsUniqueAcrossLibraries(placement.value.toString(), single, libraries)){
                    String place = null;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                    replacePlaceAcrossLibraries(placement.value.toString(), place, single, libraries, library);
                }

                if(libSymbols.containsVariableEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                }

                newProcedure.placement = placement;
            }

            procedureSec.addProcedure(newProcedure);

            for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                ICode icode = procedure.instructions.get(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                        
                        replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(ident.ident)){
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        if(unExp.right instanceof IdentExp){
                            IdentExp ident = (IdentExp)unExp.right;
                            if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!newProcedure.containsPlace(ident.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                            }

                            String oldPlace = ident.ident;
                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
            
                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                            }
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        if(binExp.left instanceof IdentExp){
                            IdentExp leftExp = (IdentExp)binExp.left;
                            if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                            }

                            String oldPlace = leftExp.ident;
                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
            
                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                            }
                        }

                        if(binExp.right instanceof IdentExp){
                            IdentExp rightExp = (IdentExp)binExp.right;
                            if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                            }

                            String oldPlace = rightExp.ident;
                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
            
                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                            }
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(leftExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                        }

                        String oldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(rightExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
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
                } else if(icode instanceof Call){
                    Call call = (Call)icode;

                    if(!procedureSec.containsProcedure(call.pname)){
                        if(libProcSec.containsProcedure(call.pname))
                            fetchInternalProcedure(library, call.pname, single, libraries, newLib);
                        else
                            fetchExternalProcedure(call.pname, single, libraries, newLib, library);
                    }

                    for(Assign arg : call.params){
                        String place = arg.value.toString();

                        if(libSymbols.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newProcedure.containsPlace(place)) {
                            fetchInternalDependentInstructions(library, single, libraries, place, newLib);
                        }

                        String oldPlace = arg.place;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
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

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.value.toString(), prog, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                            replacePlaceAcrossProgramAndLibraries(assign.value.toString(), place, prog, libraries, library);
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.place, prog, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                            replacePlaceAcrossProgramAndLibraries(assign.place, place, prog, libraries, library);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueAcrossProgramAndLibraries(placement.value.toString(), prog, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                            replacePlaceAcrossProgramAndLibraries(placement.value.toString(), place, prog, libraries, library);
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(placement.place, prog, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, prog, libraries));

                            replacePlaceAcrossProgramAndLibraries(placement.place, place, prog, libraries, library);
                        }

                        if(libSymbols.containsVariableEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);


                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                                
                                replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(ident.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(!newProcedure.containsPlace(ident.ident)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                    }

                                    String oldPlace = leftExp.ident;
                                    if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                    
                                        replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(!newProcedure.containsPlace(rightExp.ident)){
                                        fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                                    }

                                    String oldPlace = rightExp.ident;
                                    if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                    
                                        replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                    }
                                }
                            }
                        } else if(icode instanceof If){
                            If ifStat = (If)icode;

                            BinExp exp = ifStat.exp;
                            if(exp.left instanceof IdentExp){
                                IdentExp leftExp = (IdentExp)exp.left;
                                if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                }

                                String oldPlace = leftExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(rightExp.ident)){
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                                }

                                String oldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
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
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname)){
                                if(libProcSec.containsProcedure(call.pname))
                                    fetchInternalProcedure(library, call.pname, prog, libraries, newProg);
                                else
                                    fetchExternalProcedure(call.pname, prog, libraries, newProg, library);
                            }

                            for(Assign arg : call.params){
                                String place = arg.value.toString();

                                if(libSymbols.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProcedure.containsPlace(place)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, place, newProg);
                                }

                                String oldPlace = arg.place;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
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

                        if(!placeIsUniqueAcrossLibraries(assign.value.toString(), single, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            }while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                            replacePlaceAcrossLibraries(assign.value.toString(), place, single, libraries, library);
                        }

                        if(!placeIsUniqueAcrossLibraries(assign.place, single, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                            replacePlaceAcrossLibraries(assign.place, place, single, libraries, library);
                        }

                        newProcedure.addParamater(assign);
                    }

                    if(procedure.placement != null && procedure.placement.getScope() == Scope.INTERNAL_RETURN){
                        Assign placement = procedure.placement;
                        if(!placeIsUniqueAcrossLibraries(placement.value.toString(), single, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                            replacePlaceAcrossLibraries(placement.value.toString(), place, single, libraries, library);
                        }

                        if(!placeIsUniqueAcrossLibraries(placement.place, single, libraries)){
                            String place = null;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, single, libraries));

                            replacePlaceAcrossLibraries(placement.place, place, single, libraries, library);
                        }

                        if(libSymbols.containsVariableEntryWithICodePlace(placement.value.toString(), SymEntry.EXTERNAL)){
                            VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(placement.value.toString(), SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        }

                        newProcedure.placement = placement;
                    }

                    procedureSec.addProcedure(newProcedure);

                    for(int instructionIndex = 0; instructionIndex < procedure.instructions.size(); instructionIndex++){
                        ICode icode = procedure.instructions.get(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                                
                                replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(ident.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                }

                                String oldPlace = ident.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(libSymbols.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newProcedure.containsPlace(ident.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                    }

                                    String oldPlace = ident.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                                    }

                                    String oldPlace = leftExp.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                    }

                                    String oldPlace = rightExp.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }
                            }
                        } else if(icode instanceof If){
                            If ifStat = (If)icode;

                            BinExp exp = ifStat.exp;
                            if(exp.left instanceof IdentExp){
                                IdentExp leftExp = (IdentExp)exp.left;
                                if(libSymbols.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                                }

                                String oldPlace = leftExp.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(libSymbols.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(rightExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                }

                                String oldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
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
                        } else if(icode instanceof Call){
                            Call call = (Call)icode;

                            if(!procedureSec.containsProcedure(call.pname)){
                                if(libProcSec.containsProcedure(call.pname))
                                    fetchInternalProcedure(library, call.pname, single, libraries, newLib);
                                else
                                    fetchExternalProcedure(call.pname, single, libraries, newLib, library);
                            }

                            for(Assign arg : call.params){
                                String place = arg.value.toString();

                                if(libSymbols.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = libSymbols.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                                    if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newProcedure.containsPlace(place)) {
                                    fetchInternalDependentInstructions(library, single, libraries, place, newLib);
                                }

                                String oldPlace = arg.place;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!placeIsUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
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
        if(libToAlwaysReplace.containsExternalVariableByPlace(newPlace)){
            VarSymEntry entryToLook = libToAlwaysReplace.getExternalVariableByPlace(newPlace);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsExternalVariableByIdent(ident)){
                    VarSymEntry entryToReplace = program.getExternalVariableByIdent(ident);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        program.replacePlace(placeFrom, newPlace);
                } else if(program.containsInternalVariableByIdent(ident)){
                    VarSymEntry entryToReplace = program.getInternalVariableByIdent(ident);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        program.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalVariableByIdent(ident)){
                        VarSymEntry entryToReplace = library.getExternalVariableByIdent(ident);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsInternalVariableByIdent(ident)){
                        VarSymEntry entryToReplace = library.getInternalVariableByIdent(ident);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalVariableByPlace(newPlace)) {
            VarSymEntry entryToLook = libToAlwaysReplace.getInternalVariableByPlace(newPlace);
            String ident = entryToLook.declanIdent;
            if(!libToAlwaysReplace.equals(program)){
                if(program.containsExternalVariableByIdent(ident)){
                    VarSymEntry entryToReplace = program.getExternalVariableByIdent(ident);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        program.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalVariableByIdent(ident)){
                        VarSymEntry entryToReplace = library.getExternalVariableByIdent(ident);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsExternalParamaterByPlace(newPlace)){
            ParamSymEntry entry  = libToAlwaysReplace.getExternalParamaterByPlace(newPlace);
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                    ParamSymEntry localEntry = program.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                } else if(program.containsInternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                    ParamSymEntry localEntry = program.getInternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                        ParamSymEntry localEntry = library.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsInternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                        ParamSymEntry localEntry = library.getInternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalParamaterByPlace(newPlace)){
            ParamSymEntry entry  = libToAlwaysReplace.getInternalParamaterByPlace(newPlace);
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                    ParamSymEntry localEntry = program.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                        ParamSymEntry localEntry = library.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsExternalReturnByPlace(newPlace)){
            RetSymEntry returnEntry = libToAlwaysReplace.getExternalReturnByPlace(newPlace);
            String funcName = returnEntry.funcName;

            if(!program.equals(libToAlwaysReplace)){
                if(program.containsExternalReturnByFunctionName(funcName)){
                    RetSymEntry myRetEntry = program.getExternalReturnByFunctionName(funcName);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                } else if(program.containsInternalReturnByFunctionName(funcName)){
                    RetSymEntry myRetEntry = program.getInternalReturnByFunctionName(funcName);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsExternalReturnByFunctionName(funcName)){
                        RetSymEntry myRetEntry = library.getExternalReturnByFunctionName(funcName);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsInternalReturnByFunctionName(funcName)){
                        RetSymEntry myRetEntry = library.getInternalReturnByFunctionName(funcName);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalReturnByPlace(newPlace)){
            RetSymEntry returnEntry = libToAlwaysReplace.getInternalReturnByPlace(newPlace);
            String funcName = returnEntry.funcName;

            if(!program.equals(libToAlwaysReplace)){
                if(program.containsExternalReturnByFunctionName(funcName)){
                    RetSymEntry myRetEntry = program.getExternalReturnByFunctionName(funcName);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsExternalReturnByFunctionName(funcName)){
                        RetSymEntry myRetEntry = library.getExternalReturnByFunctionName(funcName);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        }
    }

    private static void replacePlaceAcrossLibraries(String oldPlace, String newPlace, Lib lib, Lib[] libraries, Lib libToAlwaysReplace){
        libToAlwaysReplace.replacePlace(oldPlace, newPlace);
        if(libToAlwaysReplace.containsExternalVariableByPlace(newPlace)){
            VarSymEntry entryToLook = libToAlwaysReplace.getExternalVariableByPlace(newPlace);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsExternalVariableByIdent(ident)){
                    VarSymEntry entryToReplace = lib.getExternalVariableByIdent(ident);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        lib.replacePlace(placeFrom, newPlace);
                } else if(lib.containsInternalVariableByIdent(ident)){
                    VarSymEntry entryToReplace = lib.getInternalVariableByIdent(ident);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        lib.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalVariableByIdent(ident)){
                        VarSymEntry entryToReplace = library.getExternalVariableByIdent(ident);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsInternalVariableByIdent(ident)){
                        VarSymEntry entryToReplace = library.getInternalVariableByIdent(ident);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalVariableByPlace(newPlace)) {
            VarSymEntry entryToLook = libToAlwaysReplace.getInternalVariableByPlace(newPlace);
            String ident = entryToLook.declanIdent;
            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsExternalVariableByIdent(ident)){
                    VarSymEntry entryToReplace = lib.getExternalVariableByIdent(ident);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        lib.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalVariableByIdent(ident)){
                        VarSymEntry entryToReplace = library.getExternalVariableByIdent(ident);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsExternalParamaterByPlace(newPlace)){
            ParamSymEntry entry  = libToAlwaysReplace.getExternalParamaterByPlace(newPlace);
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                    ParamSymEntry localEntry = lib.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                } else if(lib.containsInternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                    ParamSymEntry localEntry = lib.getInternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                        ParamSymEntry localEntry = library.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsInternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                        ParamSymEntry localEntry = library.getInternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalParamaterByPlace(newPlace)){
            ParamSymEntry entry  = libToAlwaysReplace.getInternalParamaterByPlace(newPlace);
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                    ParamSymEntry localEntry = lib.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsExternalParamaterByFunctionNameAndNumber(funcName, paramNumber)){
                        ParamSymEntry localEntry = library.getExternalParamaterByFunctionNameAndNumber(funcName, paramNumber);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsExternalReturnByPlace(newPlace)){
            RetSymEntry returnEntry = libToAlwaysReplace.getExternalReturnByPlace(newPlace);
            String funcName = returnEntry.funcName;

            if(!lib.equals(libToAlwaysReplace)){
                if(lib.containsExternalReturnByFunctionName(funcName)){
                    RetSymEntry myRetEntry = lib.getExternalReturnByFunctionName(funcName);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                } else if(lib.containsInternalReturnByFunctionName(funcName)){
                    RetSymEntry myRetEntry = lib.getInternalReturnByFunctionName(funcName);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsExternalReturnByFunctionName(funcName)){
                        RetSymEntry myRetEntry = library.getExternalReturnByFunctionName(funcName);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsInternalReturnByFunctionName(funcName)){
                        RetSymEntry myRetEntry = library.getInternalReturnByFunctionName(funcName);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsInternalReturnByPlace(newPlace)){
            RetSymEntry returnEntry = libToAlwaysReplace.getInternalReturnByPlace(newPlace);
            String funcName = returnEntry.funcName;

            if(!lib.equals(libToAlwaysReplace)){
                if(lib.containsExternalReturnByFunctionName(funcName)){
                    RetSymEntry myRetEntry = lib.getExternalReturnByFunctionName(funcName);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsExternalReturnByFunctionName(funcName)){
                        RetSymEntry myRetEntry = library.getExternalReturnByFunctionName(funcName);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        }
    }

    private static boolean placeIsUniqueAcrossProgramAndLibraries(String place, Prog program, Lib[] libraries){
        Set<String> internalReturnsRepresented = new HashSet<String>();
        Set<String> argumentRepresented = new HashSet<String>();
        Set<String> paramaterRepresented = new HashSet<String>();
        Set<String> externalReturnsRepresented = new HashSet<String>();
        Set<String> externalIdentsRepresented = new HashSet<String>();
        Set<String> internalIdentsRepresented = new HashSet<String>();
        int localVariableCount = 0;

        if(program.containsExternalReturnByPlace(place)){
            externalReturnsRepresented.add(program.getExternalReturnByPlace(place).funcName);
        } else if(program.containsInternalReturnByPlace(place)){
            internalReturnsRepresented.add(program.getInternalReturnByPlace(place).funcName);
        } else if(program.containsExternalVariableByPlace(place)){
            externalIdentsRepresented.add(program.getExternalVariableByPlace(place).declanIdent);
        } else if(program.containsInternalParamaterByPlace(place)){
            paramaterRepresented.add(program.getInternalParamaterByPlace(place).funcName);
        } else if(program.containsExternalParamaterByPlace(place)){
            argumentRepresented.add(program.getExternalParamaterByPlace(place).funcName);
        } else if(program.containsInternalVariableByPlace(place)){
            internalIdentsRepresented.add(program.getInternalVariableByPlace(place).declanIdent);
        } else if(program.containsPlace(place)){
            localVariableCount++;
        }

        for(Lib lib: libraries){
            if(lib.containsExternalReturnByPlace(place)){
                externalReturnsRepresented.add(lib.getExternalReturnByPlace(place).funcName);
            } else if(lib.containsInternalReturnByPlace(place)){
                internalReturnsRepresented.add(lib.getInternalReturnByPlace(place).funcName);
            } else if(lib.containsExternalVariableByPlace(place)){
                externalIdentsRepresented.add(lib.getExternalVariableByPlace(place).declanIdent);
            } else if(lib.containsInternalParamaterByPlace(place)){
                paramaterRepresented.add(lib.getInternalParamaterByPlace(place).funcName);
            } else if(lib.containsExternalParamaterByPlace(place)){
                argumentRepresented.add(lib.getExternalParamaterByPlace(place).funcName);
            } else if(lib.containsInternalVariableByPlace(place)){
                internalIdentsRepresented.add(lib.getInternalVariableByPlace(place).declanIdent);
            } else if(lib.containsPlace(place)){
                localVariableCount++;
            }
        }

        if(localVariableCount <= 1 && externalIdentsRepresented.isEmpty() 
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

    private static boolean placeIsUniqueAcrossLibraries(String place, Lib library, Lib[] libraries){
        Set<String> internalReturnsRepresented = new HashSet<String>();
        Set<String> argumentRepresented = new HashSet<String>();
        Set<String> paramaterRepresented = new HashSet<String>();
        Set<String> externalReturnsRepresented = new HashSet<String>();
        Set<String> externalIdentsRepresented = new HashSet<String>();
        Set<String> internalIdentsRepresented = new HashSet<String>();
        int localVariableCount = 0;

        if(library.containsExternalReturnByPlace(place)){
            externalReturnsRepresented.add(library.getExternalReturnByPlace(place).funcName);
        } else if(library.containsInternalReturnByPlace(place)){
            internalReturnsRepresented.add(library.getInternalReturnByPlace(place).funcName);
        } else if(library.containsExternalVariableByPlace(place)){
            externalIdentsRepresented.add(library.getExternalVariableByPlace(place).declanIdent);
        } else if(library.containsInternalParamaterByPlace(place)){
            paramaterRepresented.add(library.getInternalParamaterByPlace(place).funcName);
        } else if(library.containsExternalParamaterByPlace(place)){
            argumentRepresented.add(library.getExternalParamaterByPlace(place).funcName);
        } else if(library.containsInternalVariableByPlace(place)){
            internalIdentsRepresented.add(library.getInternalVariableByPlace(place).declanIdent);
        } else if(library.containsPlace(place)){
            localVariableCount++;
        }

        for(Lib lib: libraries){
            if(lib.containsExternalReturnByPlace(place)){
                externalReturnsRepresented.add(lib.getExternalReturnByPlace(place).funcName);
            } else if(lib.containsInternalReturnByPlace(place)){
                internalReturnsRepresented.add(lib.getInternalReturnByPlace(place).funcName);
            } else if(lib.containsExternalVariableByPlace(place)){
                externalIdentsRepresented.add(lib.getExternalVariableByPlace(place).declanIdent);
            } else if(lib.containsInternalParamaterByPlace(place)){
                paramaterRepresented.add(lib.getInternalParamaterByPlace(place).funcName);
            } else if(lib.containsExternalParamaterByPlace(place)){
                argumentRepresented.add(lib.getExternalParamaterByPlace(place).funcName);
            } else if(lib.containsInternalVariableByPlace(place)){
                internalIdentsRepresented.add(lib.getInternalVariableByPlace(place).declanIdent);
            } else if(lib.containsPlace(place)){
                localVariableCount++;
            }
        }

        if(localVariableCount <= 1 && externalIdentsRepresented.isEmpty() 
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

    private void linkDataSections(Prog startingProgram, Lib[] libraries, Prog newProg){
        SymSec symbolTable = newProg.symbols;
        DataSec dataSec = newProg.variables;
        ProcSec procedures = newProg.procedures;
        ProcSec progProcedures = startingProgram.procedures;
        SymSec programSymbolTable = startingProgram.symbols;
        DataSec programDataSec = startingProgram.variables;
        for(int i = 0; i < programDataSec.getLength(); i++){
            ICode instruction = programDataSec.getInstruction(i);
            if(instruction instanceof Assign){
                Assign assign = (Assign)instruction;

                String originalPlace = assign.place;
                if(!placeIsUniqueAcrossProgramAndLibraries(originalPlace, startingProgram, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                    replacePlaceAcrossProgramAndLibraries(originalPlace, place, startingProgram, libraries, startingProgram);
                }

                Exp assignExp = assign.value;
                if(assignExp instanceof BinExp){
                    BinExp assignBinExp = (BinExp)assignExp;

                    if(assignBinExp.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)assignBinExp.left;
                        if(programSymbolTable.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = leftIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsVariableEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }

                    String origPlace = assignIdentExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof Call){
                Call call = (Call)instruction;
                
                if(!procedures.containsProcedure(call.pname)){
                    if(progProcedures.containsProcedure(call.pname))
                        fetchInternalProcedure(startingProgram, call.pname, startingProgram, libraries, newProg);
                    else
                        fetchExternalProcedure(call.pname, startingProgram, libraries, newProg, startingProgram);
                }
                   

                for(Assign arg : call.params){
                    String value = arg.value.toString();

                    if(programSymbolTable.containsVariableEntryWithICodePlace(value, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(value, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, startingProgram);
                    }

                    String paramName = arg.place;
                    if(!placeIsUniqueAcrossProgramAndLibraries(paramName, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(paramName, place, startingProgram, libraries, startingProgram);
                    }
                }

                dataSec.addInstruction(call);
            }
        }
    }

    private void linkDataSections(Lib startingLibrary, Lib[] libraries, Lib newLib){
        SymSec symbolTable = newLib.symbols;
        DataSec dataSec = newLib.variables;
        ProcSec procedures = newLib.procedures;
        ProcSec libProcedures = startingLibrary.procedures;
        SymSec programSymbolTable = startingLibrary.symbols;
        DataSec programDataSec = startingLibrary.variables;
        for(int i = 0; i < programDataSec.getLength(); i++){
            ICode instruction = programDataSec.getInstruction(i);
            if(instruction instanceof Assign){
                Assign assign = (Assign)instruction;

                String originalPlace = assign.place;
                if(!placeIsUniqueAcrossLibraries(originalPlace, startingLibrary, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries));

                    replacePlaceAcrossLibraries(originalPlace, place, startingLibrary, libraries, startingLibrary);
                }

                Exp assignExp = assign.value;
                if(assignExp instanceof BinExp){
                    BinExp assignBinExp = (BinExp)assignExp;

                    if(assignBinExp.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)assignBinExp.left;
                        if(programSymbolTable.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }

                        String origPlace = leftIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries));

                            replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(programSymbolTable.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries));

                            replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(programSymbolTable.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries));

                            replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(programSymbolTable.containsVariableEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                    }

                    String origPlace = assignIdentExp.ident;
                    if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries));

                        replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                    }
                }
                dataSec.addInstruction(assign);
            } else if(instruction instanceof Call){
                Call call = (Call)instruction;
                
                if(!procedures.containsProcedure(call.pname)){
                    if(libProcedures.containsProcedure(call.pname))
                        fetchInternalProcedure(startingLibrary, call.pname, startingLibrary, libraries, newLib);
                    else
                        fetchExternalProcedure(call.pname, startingLibrary, libraries, newLib, startingLibrary);
                }
                   

                for(Assign arg : call.params){
                    String value = arg.value.toString();

                    if(programSymbolTable.containsVariableEntryWithICodePlace(value, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programSymbolTable.getVariableEntryByICodePlace(value, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, startingLibrary);
                    }

                    String paramName = arg.place;
                    if(!placeIsUniqueAcrossLibraries(paramName, startingLibrary, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossLibraries(place, startingLibrary, libraries));

                        replacePlaceAcrossLibraries(paramName, place, startingLibrary, libraries, startingLibrary);
                    }
                }

                dataSec.addInstruction(call);
            }
        }
    }

    private void linkCodeSection(Prog program, Lib[] libraries, Prog newProg){
        SymSec symbolTable = newProg.symbols;
        SymSec programTable = program.symbols;
        ProcSec procedureSec = newProg.procedures;
        ProcSec progProcedures = program.procedures;
        CodeSec codeSection = newProg.code;
        CodeSec codeSec = program.code;

        for(int i = 0; i < codeSec.getLength(); i++){
            ICode icode = codeSec.getInstruction(i);
            if(icode instanceof Assign){
                Assign assignment = (Assign)icode;

                String origPlace = assignment.place;
                if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, program, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(origPlace, place, program, libraries, program);
                }

                Exp assignExp = assignment.value;
                if(assignExp instanceof IdentExp){
                    IdentExp ident = (IdentExp)assignExp;
                    
                    if(programTable.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programTable.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String value = ident.ident; 
                    if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp unExp = (UnExp)assignExp;
                    
                    if(unExp.right instanceof IdentExp){
                        IdentExp ident = (IdentExp)unExp.right;
                        if(programTable.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programTable.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = ident.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    if(binExp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)binExp.left;
                        if(programTable.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programTable.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = leftExp.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }

                    if(binExp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)binExp.right;
                        if(programTable.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = programTable.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = rightExp.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }
                }
            } else if(icode instanceof If){
                If ifStat = (If)icode;

                BinExp exp = ifStat.exp;
                if(exp.left instanceof IdentExp){
                    IdentExp leftExp = (IdentExp)exp.left;
                    if(programTable.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programTable.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String origPlace = leftExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(origPlace, place, program, libraries, program);
                    }
                }

                if(exp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)exp.right;
                    if(programTable.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programTable.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String origPlace = rightExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(origPlace, place, program, libraries, program);
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
            } else if(icode instanceof Call){
                Call call = (Call)icode;
                
                if(!procedureSec.containsProcedure(call.pname)){
                    if(progProcedures.containsProcedure(call.pname))
                        fetchInternalProcedure(program, call.pname, libraries, newProg);
                    else
                        fetchExternalProcedure(call.pname, program, libraries, newProg, program);
                }
                   

                for(Assign arg : call.params){
                    String value = arg.value.toString();

                    if(programTable.containsVariableEntryWithICodePlace(value, SymEntry.EXTERNAL)){
                        VarSymEntry entry = programTable.getVariableEntryByICodePlace(value, SymEntry.EXTERNAL);
                        if(!symbolTable.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String paramName = arg.place;
                    if(!placeIsUniqueAcrossProgramAndLibraries(paramName, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!placeIsUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(paramName, place, program, libraries, program);
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
