package io.github.H20man13.DeClan.main;

import java.util.HashSet;
import java.util.Set;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.exception.ICodeLinkerException;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.gen.LabelGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
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
        for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                if(library.containsInternalVariableByIdent(identName)){
                    VarSymEntry libEntry = library.getInternalVariableByIdent(identName);
                    int libDataBegin = library.beginningOfDataSection();
                    int libDataEnd = library.endOfDataSection();
                    for(int z = libDataBegin; z <= libDataEnd; z++){
                        ICode icodeLib = library.getInstruction(z);
                        if(icodeLib instanceof Def){
                            Def assignLib = (Def)icodeLib;
                            if(assignLib.label.equals(libEntry.icodePlace)){
                                Exp exp = assignLib.val;
                                if(exp instanceof IdentExp){
                                    IdentExp identExp = (IdentExp)exp;

                                    if(identExp.scope == ICode.Scope.RETURN){
                                        ICode funcCallICode = library.getInstruction(z - 1);
                                        if(funcCallICode instanceof Call){
                                            Call funcCall = (Call)funcCallICode;
    
                                            if(!newProgram.containsProcedure(funcCall.pname)){
                                                if(library.containsProcedure(funcCall.pname))
                                                    fetchInternalProcedure(library, funcCall.pname, program, libraries, newProgram);
                                                else
                                                    fetchExternalProcedure(funcCall.pname, program, libraries, newProgram, library);
                                            }
    
                                            int numArgs = funcCall.params.size();
                                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                                Def arg = funcCall.params.get(argIndex);
                                                if(arg.val instanceof IdentExp){
                                                    IdentExp expVal = (IdentExp)arg.val;
                                                    if(library.containsVariableEntryWithICodePlace(expVal.ident, SymEntry.EXTERNAL)){
                                                        VarSymEntry entry = library.getVariableEntryByICodePlace(expVal.ident, SymEntry.EXTERNAL);
                                                        if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                                    } else {
                                                        fetchInternalDependentInstructions(library, program, libraries, expVal.ident, newProgram);
                                                    }
    
                                                    String oldPlace = arg.label;
                                                    if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                                        String newPlace = null;    
                                                        do{
                                                            newPlace = gen.genNext();
                                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
            
                                                        replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                                    }
                                                }
                                            }
    
                                            newProgram.addDataInstruction(funcCall);
                                        }
                                    }

                                    if(library.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                        if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram);
                                    }

                                    String oldPlace = identExp.ident;
                                    if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                        String newPlace = null;    
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                        replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                    }

                                    if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.label, program, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                                        replacePlaceAcrossProgramAndLibraries(assignLib.label, place, program, libraries, library);
                                    }

                                    if(!newProgram.dataSectionContainsInstruction(assignLib))
                                        newProgram.addDataInstruction(assignLib);
                                    if(!newProgram.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newProgram.addSymEntry(libEntry);
                                } else if(exp instanceof UnExp){
                                    UnExp unary = (UnExp)exp;
                                    if(unary.right instanceof IdentExp){
                                        IdentExp identExp = (IdentExp)unary.right;
                                        if(library.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = library.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, program, libraries, identExp.ident, newProgram);
                                        }

                                        String oldPlace = identExp.ident;
                                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                        }
                                    }

                                    if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.label, program, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                                        replacePlaceAcrossProgramAndLibraries(assignLib.label, place, program, libraries, library);
                                    }

                                    if(!newProgram.dataSectionContainsInstruction(assignLib))
                                        newProgram.addDataInstruction(assignLib);
                                    if(!newProgram.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newProgram.addSymEntry(libEntry);
                                } else if(exp instanceof BinExp){
                                    BinExp binary = (BinExp)exp;

                                    if(binary.left instanceof IdentExp){
                                        IdentExp leftIdent = (IdentExp)binary.left;
                                        if(library.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, program, libraries, leftIdent.ident, newProgram);
                                        }

                                        String oldPlace = leftIdent.ident;
                                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                        }
                                    }

                                    if(binary.right instanceof IdentExp){
                                        IdentExp rightIdent = (IdentExp)binary.right;
                                        if(library.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, program, libraries, rightIdent.ident, newProgram);
                                        }

                                        String oldPlace = rightIdent.ident;
                                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, library);
                                        }
                                    }

                                    if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.label, program, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                                        replacePlaceAcrossProgramAndLibraries(assignLib.label, place, program, libraries, library);
                                    }

                                    if(!newProgram.dataSectionContainsInstruction(assignLib))
                                        newProgram.addDataInstruction(assignLib);
                                    if(!newProgram.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newProgram.addSymEntry(libEntry);
                                } else {
                                    if(!placeIsUniqueAcrossProgramAndLibraries(assignLib.label, program, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                                        replacePlaceAcrossProgramAndLibraries(assignLib.label, place, program, libraries, library);
                                    }

                                    if(!newProgram.dataSectionContainsInstruction(assignLib))
                                        newProgram.addDataInstruction(assignLib);
                                    if(!newProgram.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newProgram.addSymEntry(libEntry);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void fetchExternalDependentInstructions(String identName, Lib single, Lib[] libraries, Lib newLib, Lib... libsToIgnore){
        for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                if(library.containsInternalVariableByIdent(identName)){
                    VarSymEntry libEntry = library.getInternalVariableByIdent(identName);
                    int beginData = library.beginningOfDataSection();
                    int endData = library.endOfDataSection();
                    for(int z = beginData; z <= endData; z++){
                        ICode icodeLib = library.getInstruction(z);
                        if(icodeLib instanceof Def){
                            Def assignLib = (Def)icodeLib;
                            if(assignLib.label.equals(libEntry.icodePlace)){
                                Exp exp = assignLib.val;
                                if(exp instanceof IdentExp){
                                    IdentExp identExp = (IdentExp)exp;

                                    if(identExp.scope == ICode.Scope.RETURN){
                                        ICode funcCallICode = library.getInstruction(z - 1);
                                        if(funcCallICode instanceof Call){
                                            Call funcCall = (Call)funcCallICode;
            
                                            if(!newLib.containsProcedure(funcCall.pname)){
                                                if(library.containsProcedure(funcCall.pname))
                                                    fetchInternalProcedure(library, funcCall.pname, single, libraries, newLib);
                                                else
                                                    fetchExternalProcedure(funcCall.pname, single, libraries, newLib, library);
                                            }
            
                                            int numArgs = funcCall.params.size();
                                            for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                                Def arg = funcCall.params.get(argIndex);
                                                if(arg.val instanceof IdentExp){
                                                    IdentExp val = (IdentExp)arg.val;
                                                    if(library.containsVariableEntryWithICodePlace(val.ident, SymEntry.EXTERNAL)){
                                                        VarSymEntry entry = library.getVariableEntryByICodePlace(val.ident, SymEntry.EXTERNAL);
                                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                                        else
                                                            fetchInternalDependentInstructions(library, single, libraries, val.ident, newLib);
                                                    }
            
                                                    String oldPlace = val.ident;
                                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                                        String newPlace = null;    
                                                        do{
                                                            newPlace = gen.genNext();
                                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                        
                                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                                    }
                                                }
                                            }
            
                                            newLib.addDataInstruction(funcCall);
                                        }
                                    }

                                    if(library.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib);
                                    }

                                    String oldPlace = identExp.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace = null;    
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }

                                    if(!placeIsUniqueAcrossLibraries(assignLib.label, single, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));

                                        replacePlaceAcrossLibraries(assignLib.label, place, single, libraries, library);
                                    }


                                    int endNewData = newLib.endOfDataSection();
                                    if(!newLib.dataSectionContainsInstruction(assignLib))
                                        newLib.addDataInstruction(assignLib);
                                    if(!newLib.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newLib.addSymEntry(libEntry);
                                } else if(exp instanceof UnExp){
                                    UnExp unary = (UnExp)exp;
                                    if(unary.right instanceof IdentExp){
                                        IdentExp identExp = (IdentExp)unary.right;
                                        if(library.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = library.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, identExp.ident, newLib);
                                        }

                                        String oldPlace = identExp.ident;
                                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                        }
                                    }

                                    if(!placeIsUniqueAcrossLibraries(assignLib.label, single, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));
                                        
                                        replacePlaceAcrossLibraries(assignLib.label, place, single, libraries, library);
                                    }


                                    if(!newLib.dataSectionContainsInstruction(assignLib))
                                        newLib.addDataInstruction(assignLib);
                                    if(!newLib.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newLib.addSymEntry(libEntry);
                                } else if(exp instanceof BinExp){
                                    BinExp binary = (BinExp)exp;

                                    if(binary.left instanceof IdentExp){
                                        IdentExp leftIdent = (IdentExp)binary.left;
                                        if(library.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, leftIdent.ident, newLib);
                                        }

                                        String oldPlace = leftIdent.ident;
                                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                        }
                                    }

                                    if(binary.right instanceof IdentExp){
                                        IdentExp rightIdent = (IdentExp)binary.right;
                                        if(library.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                        } else {
                                            fetchInternalDependentInstructions(library, single, libraries, rightIdent.ident, newLib);
                                        }

                                        String oldPlace = rightIdent.ident;
                                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                        }
                                    }

                                    if(!placeIsUniqueAcrossLibraries(assignLib.label, single, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));
                                        
                                        replacePlaceAcrossLibraries(assignLib.label, place, single, libraries, library);
                                    }

                                    if(!newLib.dataSectionContainsInstruction(assignLib))
                                        newLib.addDataInstruction(assignLib);
                                    if(!newLib.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newLib.addSymEntry(libEntry);
                                } else {
                                    if(!placeIsUniqueAcrossLibraries(assignLib.label, single, libraries)){
                                        String place = null;    
                                        do{
                                            place = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));
                                        
                                        replacePlaceAcrossLibraries(assignLib.label, place, single, libraries, library);
                                    }

                                    if(!newLib.dataSectionContainsInstruction(assignLib))
                                        newLib.addDataInstruction(assignLib);
                                    if(!newLib.containsVariableEntryWithIdentifier(identName, SymEntry.INTERNAL))
                                        newLib.addSymEntry(libEntry);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void fetchInternalDependentInstructions(Lib currentLib, Lib single, Lib[] libraries, String labelName, Lib newLib){
        int begin = currentLib.beginningOfDataSection();
        int end = currentLib.endOfDataSection();
        for(int i = begin; i <= end; i++){
            ICode icodeLib = currentLib.getInstruction(i);
            if(icodeLib instanceof Def){
                Def assign = (Def)icodeLib;
                if(assign.label.equals(labelName)){
                    Exp exp = assign.val;
                    if(exp instanceof IdentExp){
                        IdentExp identExp = (IdentExp)exp;

                        if(identExp.scope == ICode.Scope.RETURN){
                            ICode funcCallICode = currentLib.getInstruction(i - 1);
                            if(funcCallICode instanceof Call){
                                Call funcCall = (Call)funcCallICode;

                                if(!newLib.containsProcedure(funcCall.pname)){
                                    if(currentLib.containsProcedure(funcCall.pname))
                                        fetchInternalProcedure(currentLib, funcCall.pname, single, libraries, newLib);
                                    else
                                        fetchExternalProcedure(funcCall.pname, single, libraries, newLib, currentLib);
                                }

                                int numArgs = funcCall.params.size();
                                for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                    Def arg = funcCall.params.get(argIndex);
                                    if(arg.val instanceof IdentExp){
                                        IdentExp val = (IdentExp)arg.val;
                                        if(currentLib.containsVariableEntryWithICodePlace(val.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(val.ident, SymEntry.EXTERNAL);
                                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                                            else
                                                fetchInternalDependentInstructions(currentLib, single, libraries, val.ident, newLib);
                                        }

                                        String oldPlace = val.ident;
                                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
            
                                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                                        }
                                    }
                                }

                                newLib.addDataInstruction(funcCall);
                            }
                        }

                        if(currentLib.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib);
                        }

                        String oldPlace = identExp.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace = null;    
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                        }

                        if(!placeIsUniqueAcrossLibraries(assign.label, single, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));
                            
                            replacePlaceAcrossLibraries(assign.label, place, single, libraries, currentLib);
                        }

                        if(!newLib.dataSectionContainsInstruction(assign))
                            newLib.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newLib.addSymEntry(entry);
                        }
                    } else if(exp instanceof UnExp){
                        UnExp unary = (UnExp)exp;
                        if(unary.right instanceof IdentExp){
                            IdentExp identExp = (IdentExp)unary.right;
                            if(currentLib.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = currentLib.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, single, libraries, identExp.ident, newLib);
                            }

                            String oldPlace = identExp.ident;
                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                            }
                        }

                        if(!placeIsUniqueAcrossLibraries(assign.label, single, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));
                            
                            replacePlaceAcrossLibraries(assign.label, place, single, libraries, currentLib);
                        }

                        if(!newLib.dataSectionContainsInstruction(assign))
                            newLib.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newLib.addSymEntry(entry);
                        }
                    } else if(exp instanceof BinExp){
                        BinExp binary = (BinExp)exp;

                        if(binary.left instanceof IdentExp){
                            IdentExp leftIdent = (IdentExp)binary.left;
                            if(currentLib.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = currentLib.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, single, libraries, leftIdent.ident, newLib);
                            }

                            String oldPlace = leftIdent.ident;
                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                            }
                        }

                        if(binary.right instanceof IdentExp){
                            IdentExp rightIdent = (IdentExp)binary.right;
                            if(currentLib.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = currentLib.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, single, libraries, rightIdent.ident, newLib);
                            }

                            String oldPlace = rightIdent.ident;
                            if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, currentLib);
                            }
                        }

                        if(!placeIsUniqueAcrossLibraries(assign.label, single, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));
                            
                            replacePlaceAcrossLibraries(assign.label, place, single, libraries, currentLib);
                        }

                        if(!newLib.dataSectionContainsInstruction(assign))
                            newLib.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newLib.addSymEntry(entry);
                        }
                    } else {
                        if(!placeIsUniqueAcrossLibraries(assign.label, single, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(place, single, libraries));
                            
                            replacePlaceAcrossLibraries(assign.label, place, single, libraries, currentLib);
                        }

                        if(!newLib.dataSectionContainsInstruction(assign))
                            newLib.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newLib.addSymEntry(entry);
                        }
                    }
                    return;
                }
            }
        }
    }

     private void fetchInternalDependentInstructions(Lib currentLib, Prog program, Lib[] libraries, String labelName, Prog newProgram){
        int begin = currentLib.beginningOfDataSection();
        int end = currentLib.endOfDataSection();

        for(int i = begin; i <= end; i++){
            ICode icodeLib = currentLib.getInstruction(i);
            if(icodeLib instanceof Def){
                Def assign = (Def)icodeLib;
                if(assign.label.equals(labelName)){
                    Exp exp = assign.val;
                    if(exp instanceof IdentExp){
                        IdentExp identExp = (IdentExp)exp;
                        if(identExp.scope == ICode.Scope.RETURN){
                            ICode funcCallICode = currentLib.getInstruction(i - 1);
                            if(funcCallICode instanceof Call){
                                Call funcCall = (Call)funcCallICode;

                                if(!newProgram.containsProcedure(funcCall.pname)){
                                    if(currentLib.containsProcedure(funcCall.pname))
                                        fetchInternalProcedure(currentLib, funcCall.pname, program, libraries, newProgram);
                                    else
                                        fetchExternalProcedure(funcCall.pname, program, libraries, newProgram, currentLib);
                                }

                                int numArgs = funcCall.params.size();
                                for(int argIndex = 0; argIndex < numArgs; argIndex++){
                                    Def arg = funcCall.params.get(argIndex);
                                    if(arg.val instanceof IdentExp){
                                        IdentExp val = (IdentExp)arg.val;
                                        if(currentLib.containsVariableEntryWithICodePlace(val.ident, SymEntry.EXTERNAL)){
                                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(val.ident, SymEntry.EXTERNAL);
                                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                                            else
                                                fetchInternalDependentInstructions(currentLib, program, libraries, val.ident, newProgram);
                                        }

                                        String oldPlace = val.ident;
                                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                            String newPlace = null;    
                                            do{
                                                newPlace = gen.genNext();
                                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
            
                                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                                        }
                                    }
                                }

                                newProgram.addDataInstruction(funcCall);
                            }
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.label, program, libraries)){
                            String newPlace = null;    
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(assign.label, newPlace, program, libraries, currentLib);
                        }

                        if(!newProgram.dataSectionContainsInstruction(assign))
                            newProgram.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newProgram.addSymEntry(entry);
                        }
                    } else if(exp instanceof UnExp){
                        UnExp unary = (UnExp)exp;
                        if(unary.right instanceof IdentExp){
                            IdentExp identExp = (IdentExp)unary.right;
                            if(currentLib.containsVariableEntryWithICodePlace(identExp.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = currentLib.getVariableEntryByICodePlace(identExp.ident, SymEntry.EXTERNAL);
                                if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram);
                            }

                            String oldPlace = identExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                            }
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.label, program, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(assign.label, place, program, libraries, currentLib);
                        }

                        if(!newProgram.dataSectionContainsInstruction(assign))
                            newProgram.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newProgram.addSymEntry(entry);
                        }
                    } else if(exp instanceof BinExp){
                        BinExp binary = (BinExp)exp;

                        if(binary.left instanceof IdentExp){
                            IdentExp leftIdent = (IdentExp)binary.left;
                            if(currentLib.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = currentLib.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                                if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, program, libraries, leftIdent.ident, newProgram);
                            }

                            String oldPlace = leftIdent.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                            }
                        }

                        if(binary.right instanceof IdentExp){
                            IdentExp rightIdent = (IdentExp)binary.right;
                            if(currentLib.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                                VarSymEntry entry = currentLib.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                                if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram,  currentLib);
                            } else {
                                fetchInternalDependentInstructions(currentLib, program, libraries, rightIdent.ident, newProgram);
                            }

                            String oldPlace = rightIdent.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                                String newPlace = null;    
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, currentLib);
                            }
                        }

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.label, program, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(assign.label, place, program, libraries, currentLib);
                        }

                        if(!newProgram.dataSectionContainsInstruction(assign))
                            newProgram.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newProgram.addSymEntry(entry);
                        }
                    } else {
                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.label, program, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(assign.label, place, program, libraries, currentLib);
                        }

                        if(!newProgram.dataSectionContainsInstruction(assign))
                            newProgram.addDataInstruction(assign);
                        if(currentLib.containsVariableEntryWithICodePlace(labelName, SymEntry.INTERNAL)){
                            VarSymEntry entry = currentLib.getVariableEntryByICodePlace(labelName, SymEntry.INTERNAL);
                            if(!newProgram.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                newProgram.addSymEntry(entry);
                        }
                    }
                    return;
                }
            }
        }
    }

    private void fetchInternalProcedure(Prog program, String procName, Lib[] libraries, Prog newProg){
        if(program.containsProcedure(procName)){
            int beginningProcedure = program.beginningOfProcedure(procName);
            int endProcedure = program.endOfProcedure(procName);


            newProg.addProcedureHeader(procName);

            for(int instructionIndex = beginningProcedure + 1; instructionIndex <= (endProcedure - 1); instructionIndex++){
                ICode icode = program.getInstruction(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, program, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, program, libraries, program);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        IdentExp ident = unExp.right;
                        if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        IdentExp leftExp = binExp.left;
                        if(program.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String leftOldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, program, libraries, program);
                        }

                        IdentExp rightExp = binExp.right;
                        if(program.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String rightOldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, program, libraries, program);
                        }
                    }
                } else if (icode instanceof Def){
                    Def definition = (Def)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(definition.label, program, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
                        
                        replacePlaceAcrossProgramAndLibraries(definition.label, newPlace, program, libraries, program);
                    }

                    Exp assignExp = definition.val;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        IdentExp ident = unExp.right;
                        if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        IdentExp leftExp = binExp.left;
                        if(program.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String leftOldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, program, libraries, program);
                        }

                        IdentExp rightExp = binExp.right;
                        if(program.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String rightOldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, program, libraries, program);
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    IdentExp leftExp = exp.left;
                    if(program.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = program.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String leftOldPlace = leftExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, program, libraries)){
                        String newPlace;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                        replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, program, libraries, program);
                    }

                    IdentExp rightExp = exp.right;
                    if(program.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = program.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String oldPlace = rightExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                        String newPlace;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
    
                        replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, program, libraries, program);
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

                    if(!newProg.containsProcedure(call.pname)){
                        if(program.containsProcedure(call.pname))
                            fetchInternalProcedure(program, call.pname, program, libraries, newProg);
                        else
                            fetchExternalProcedure(call.pname, program, libraries, newProg, program);
                    }
                    
                    for(Def arg : call.params){
                        if(arg.val instanceof IdentExp){
                            IdentExp iExp = (IdentExp)arg.val;
                            String place = iExp.ident;

                            if(program.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                VarSymEntry entry = program.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                            }
                        }

                        String oldPlace = arg.label;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, program, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));
        
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

                newProg.addProcedureInstruction(procName, icode);
            }
        }
    }

    private void fetchInternalProcedure(Lib library, String procName, Prog prog, Lib[] libraries, Prog newProg){
        if(library.containsProcedure(procName)){
            int beginningProcedure = library.beginningOfProcedure(procName);
            int endProcedure = library.endOfProcedure(procName);

            newProg.addProcedureHeader(procName);

            for(int instructionIndex = beginningProcedure + 1; instructionIndex <= (endProcedure - 1); instructionIndex++){
                ICode icode = library.getInstruction(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                        
                        replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        IdentExp ident = (IdentExp)unExp.right;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        IdentExp leftExp = binExp.left;
                        if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, leftExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                        }

                        String leftOldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, prog, libraries, library);
                        }

                        IdentExp rightExp = binExp.right;
                        if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, rightExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    }
                } else if(icode instanceof Def){
                    Def definition = (Def)icode;
                    
                    if(!placeIsUniqueAcrossProgramAndLibraries(definition.label, prog, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                        
                        replacePlaceAcrossProgramAndLibraries(definition.label, newPlace, prog, libraries, library);
                    }

                    Exp assignExp = definition.val;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        IdentExp ident = (IdentExp)unExp.right;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        IdentExp leftExp = binExp.left;
                        if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, leftExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                        }

                        String leftOldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, prog, libraries, library);
                        }

                        IdentExp rightExp = binExp.right;
                        if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, rightExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, leftExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                        }

                        String oldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
                            replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(!newProg.placeDefinedInProcedure(procName, rightExp.ident)) {
                            fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
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

                    if(!newProg.containsProcedure(call.pname)){
                        if(library.containsProcedure(call.pname))
                            fetchInternalProcedure(library, call.pname, prog, libraries, newProg);
                        else
                            fetchExternalProcedure(call.pname, prog, libraries, newProg, library);
                    }
                    
                    for(Def arg : call.params){
                        if(arg.val instanceof IdentExp){
                            IdentExp iExp = (IdentExp)arg.val;
                            String place = iExp.ident;

                            if(library.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                VarSymEntry entry = library.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(!newProg.placeDefinedInProcedure(procName, place)) {
                                fetchInternalDependentInstructions(library, prog, libraries, place, newProg);
                            }
                        }

                        String oldPlace = arg.label;
                        if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
        
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

                newProg.addProcedureInstruction(procName, icode);
            }
        }
    }

    private void fetchInternalProcedure(Lib library, String procName, Lib single, Lib[] libraries, Lib newLib){
        if(library.containsProcedure(procName)){

            int beginningProcedure = library.beginningOfProcedure(procName);
            int endProcedure = library.endOfProcedure(procName);

            newLib.addProcedureHeader(procName);

            for(int instructionIndex = beginningProcedure + 1; instructionIndex <= (endProcedure - 1); instructionIndex++){
                ICode icode = library.getInstruction(instructionIndex);

                if(icode instanceof Assign){
                    Assign assignment = (Assign)icode;
                    
                    if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                        
                        replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library);
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)){
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        IdentExp ident = unExp.right;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        IdentExp leftExp = binExp.left;
                        if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, leftExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                        }

                        String leftOldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossLibraries(leftOldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(leftOldPlace, newPlace, single, libraries, library);
                        }

                        IdentExp rightExp = binExp.right;
                        if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, rightExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    }
                } else if(icode instanceof Def){
                    Def definition = (Def)icode;
                    
                    if(!placeIsUniqueAcrossLibraries(definition.label, single, libraries)){
                        String newPlace = null;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                        
                        replacePlaceAcrossLibraries(definition.label, newPlace, single, libraries, library);
                    }

                    Exp assignExp = definition.val;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)){
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    } else if(assignExp instanceof UnExp){
                        UnExp unExp = (UnExp)assignExp;
                        
                        IdentExp ident = unExp.right;
                        if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                        }

                        String oldPlace = ident.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    } else if(assignExp instanceof BinExp){
                        BinExp binExp = (BinExp)assignExp;

                        IdentExp leftExp = binExp.left;
                        if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, leftExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                        }

                        String leftOldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossLibraries(leftOldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(leftOldPlace, newPlace, single, libraries, library);
                        }

                        IdentExp rightExp = binExp.right;
                        if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, rightExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    }
                } else if(icode instanceof If){
                    If ifStat = (If)icode;

                    BinExp exp = ifStat.exp;
                    if(exp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)exp.left;
                        if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, leftExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                        }

                        String oldPlace = leftExp.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
                            replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                        }
                    }

                    if(exp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)exp.right;
                        if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(!newLib.placeDefinedInProcedure(procName, rightExp.ident)) {
                            fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                        }

                        String oldPlace = rightExp.ident;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
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

                    if(!newLib.containsProcedure(call.pname)){
                        if(library.containsProcedure(call.pname))
                            fetchInternalProcedure(library, call.pname, single, libraries, newLib);
                        else
                            fetchExternalProcedure(call.pname, single, libraries, newLib, library);
                    }

                    for(Def arg : call.params){
                        if(arg.val instanceof IdentExp){
                            IdentExp iExp = (IdentExp)arg.val;
                            String place = iExp.ident;

                            if(library.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                VarSymEntry entry = library.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                                if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(!library.placeDefinedInProcedure(procName, place)) {
                                fetchInternalDependentInstructions(library, single, libraries, place, newLib);
                            }
                        }

                        String oldPlace = arg.label;
                        if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                            String newPlace;
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
        
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

                newLib.addProcedureInstruction(procName, icode);
            }
        }
    }

    private void fetchExternalProcedure(String procName, Prog prog, Lib[] libraries, Prog newProg, Lib... libsToIgnore){
        loop: for(int i = 0; i < libraries.length; i++){
            Lib library = libraries[i];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                if(library.containsProcedure(procName)){
                    
                    int beginningProcedure = library.beginningOfProcedure(procName);
                    int endProcedure = library.endOfProcedure(procName);

                    newProg.addProcedureHeader(procName);


                    for(int instructionIndex = beginningProcedure + 1; instructionIndex <= (endProcedure - 1); instructionIndex++){
                        ICode icode = library.getInstruction(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossProgramAndLibraries(assignment.place, prog, libraries)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                                
                                replacePlaceAcrossProgramAndLibraries(assignment.place, newPlace, prog, libraries, library);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                IdentExp ident = unExp.right;
                                if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                IdentExp leftExp = binExp.left;
                                if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                }

                                String leftOldPlace = leftExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, prog, libraries, library);
                                }

                                IdentExp rightExp = binExp.right;
                                if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, rightExp.ident)){
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                                }

                                String oldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                }
                            }
                        } else if(icode instanceof Def){
                            Def assignment = (Def)icode;
                            
                            if(!placeIsUniqueAcrossProgramAndLibraries(assignment.label, prog, libraries)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                                
                                replacePlaceAcrossProgramAndLibraries(assignment.label, newPlace, prog, libraries, library);
                            }

                            Exp assignExp = assignment.val;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                IdentExp ident = unExp.right;
                                if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, ident.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                IdentExp leftExp = binExp.left;
                                if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                }

                                String leftOldPlace = leftExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, prog, libraries, library);
                                }

                                IdentExp rightExp = binExp.right;
                                if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, rightExp.ident)){
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                                }

                                String oldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                }
                            }
                        } else if(icode instanceof If){
                            If ifStat = (If)icode;

                            BinExp exp = ifStat.exp;
                            if(exp.left instanceof IdentExp){
                                IdentExp leftExp = (IdentExp)exp.left;
                                if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                }

                                String oldPlace = leftExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(!newProg.placeDefinedInProcedure(procName, rightExp.ident)){
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                                }

                                String oldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
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

                            if(!newProg.containsProcedure(call.pname)){
                                if(library.containsProcedure(call.pname))
                                    fetchInternalProcedure(library, call.pname, prog, libraries, newProg);
                                else
                                    fetchExternalProcedure(call.pname, prog, libraries, newProg, library);
                            }

                            for(Def arg : call.params){
                                if(arg.val instanceof IdentExp){
                                    IdentExp ident = (IdentExp)arg.val;
                                    String place = ident.ident;

                                    if(library.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(!newProg.placeDefinedInProcedure(procName, place)){
                                        fetchInternalDependentInstructions(library, prog, libraries, place, newProg);
                                    }
                                }

                                String oldPlace = arg.label;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
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

                        newProg.addProcedureInstruction(procName, icode);
                    }
                    break loop;
                }
            }
        }
    }

    private void fetchExternalProcedure(String procName, Lib single, Lib[] libraries, Lib newLib, Lib... libsToIgnore){
        loop: for(int i = 0; i < libraries.length; i++){
            Lib library = libraries[i];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                if(library.containsProcedure(procName)){
                    int beginningProcedure = library.beginningOfProcedure(procName);
                    int endProcedure = library.endOfProcedure(procName);

                    newLib.addProcedureHeader(procName);

                    for(int instructionIndex = beginningProcedure + 1; instructionIndex <= (endProcedure - 1); instructionIndex++){
                        ICode icode = library.getInstruction(instructionIndex);

                        if(icode instanceof Assign){
                            Assign assignment = (Assign)icode;
                            
                            if(!placeIsUniqueAcrossLibraries(assignment.place, single, libraries)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                                
                                replacePlaceAcrossLibraries(assignment.place, newPlace, single, libraries, library);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)){
                                    fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                }

                                String oldPlace = ident.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                    }

                                    String oldPlace = ident.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newLib.placeDefinedInProcedure(procName, leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                                    }

                                    String oldPlace = leftExp.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newLib.placeDefinedInProcedure(procName, rightExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                    }

                                    String oldPlace = rightExp.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }
                            }
                        } else if(icode instanceof Def){
                            Def assignment = (Def)icode;
                            
                            if(!placeIsUniqueAcrossLibraries(assignment.label, single, libraries)){
                                String newPlace = null;
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                                
                                replacePlaceAcrossLibraries(assignment.label, newPlace, single, libraries, library);
                            }

                            Exp assignExp = assignment.val;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                    if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)){
                                    fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                }

                                String oldPlace = ident.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                if(unExp.right instanceof IdentExp){
                                    IdentExp ident = (IdentExp)unExp.right;
                                    if(library.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newLib.placeDefinedInProcedure(procName, ident.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                    }

                                    String oldPlace = ident.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                if(binExp.left instanceof IdentExp){
                                    IdentExp leftExp = (IdentExp)binExp.left;
                                    if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newLib.placeDefinedInProcedure(procName, leftExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                                    }

                                    String oldPlace = leftExp.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }

                                if(binExp.right instanceof IdentExp){
                                    IdentExp rightExp = (IdentExp)binExp.right;
                                    if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newLib.placeDefinedInProcedure(procName, rightExp.ident)) {
                                        fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                    }

                                    String oldPlace = rightExp.ident;
                                    if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                        String newPlace;
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                    
                                        replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                    }
                                }
                            }
                        } else if(icode instanceof If){
                            If ifStat = (If)icode;

                            BinExp exp = ifStat.exp;
                            if(exp.left instanceof IdentExp){
                                IdentExp leftExp = (IdentExp)exp.left;
                                if(library.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                                    if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newLib.placeDefinedInProcedure(procName, leftExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, leftExp.ident, newLib);
                                }

                                String oldPlace = leftExp.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                }
                            }

                            if(exp.right instanceof IdentExp){
                                IdentExp rightExp = (IdentExp)exp.right;
                                if(library.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                                    VarSymEntry entry = library.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                                    if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(!newLib.placeDefinedInProcedure(procName, rightExp.ident)) {
                                    fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                }

                                String oldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
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

                            if(!newLib.containsProcedure(call.pname)){
                                if(library.containsProcedure(call.pname))
                                    fetchInternalProcedure(library, call.pname, single, libraries, newLib);
                                else
                                    fetchExternalProcedure(call.pname, single, libraries, newLib, library);
                            }

                            for(Def arg : call.params){
                                if(arg.val instanceof IdentExp){
                                    IdentExp iExp = (IdentExp)arg.val;
                                    String place = iExp.ident;

                                    if(library.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL)){
                                        VarSymEntry entry = library.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
                                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(!newLib.placeDefinedInProcedure(procName, place)) {
                                        fetchInternalDependentInstructions(library, single, libraries, place, newLib);
                                    }
                                }

                                String oldPlace = arg.label;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
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

                        newLib.addProcedureInstruction(procName, icode);
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

    private static boolean newPlaceWillBeUniqueAcrossProgramAndLibraries(String place, Prog program, Lib[] libraries){
        int placeCount = 0;

        if(program.containsPlace(place)){
            return false;
        }

        
        for(Lib library: libraries){
            if(library.containsPlace(place))
                return false;
        }

        return true;
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

    private static boolean newPlaceWillBeUniqueAcrossLibraries(String place, Lib single, Lib[] libraries){
        if(single.containsPlace(place)){
            return false;
        }

        
        for(Lib library: libraries){
            if(library.containsPlace(place))
                return false;
        }

        return true;
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
        int beginningOfDataSection = startingProgram.beginningOfDataSection();
        int endOfDataSection = startingProgram.endOfDataSection();

        for(int i = beginningOfDataSection; i <= endOfDataSection; i++){
            ICode instruction = startingProgram.getInstruction(i);
            if(instruction instanceof Def){
                Def assign = (Def)instruction;

                String originalPlace = assign.label;
                if(!placeIsUniqueAcrossProgramAndLibraries(originalPlace, startingProgram, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                    replacePlaceAcrossProgramAndLibraries(originalPlace, place, startingProgram, libraries, startingProgram);
                }

                Exp assignExp = assign.val;
                if(assignExp instanceof BinExp){
                    BinExp assignBinExp = (BinExp)assignExp;

                    if(assignBinExp.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)assignBinExp.left;
                        if(startingProgram.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = leftIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(startingProgram.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(startingProgram.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(startingProgram.containsVariableEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }

                    String origPlace = assignIdentExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                    }
                }

                newProg.addDataInstruction(instruction);
            } else if(instruction instanceof Call){
                Call call = (Call)instruction;
                
                if(!newProg.containsProcedure(call.pname)){
                    if(startingProgram.containsProcedure(call.pname))
                        fetchInternalProcedure(startingProgram, call.pname, startingProgram, libraries, newProg);
                    else
                        fetchExternalProcedure(call.pname, startingProgram, libraries, newProg, startingProgram);
                }
                   

                for(Def arg : call.params){
                    if(arg.val instanceof IdentExp){
                        IdentExp exp = (IdentExp)arg.val;
                        String value = exp.ident;

                        if(startingProgram.containsVariableEntryWithICodePlace(value, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(value, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg, startingProgram);
                        }
                    }

                    String paramName = arg.label;
                    if(!placeIsUniqueAcrossProgramAndLibraries(paramName, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(paramName, place, startingProgram, libraries, startingProgram);
                    }
                }

                newProg.addDataInstruction(instruction);
            }
        }
    }

    private void linkBssSection(Prog startingProgram, Lib[] libraries, Prog newProg){
        int beginningOfBssSection = startingProgram.beginningOfBssSection();
        int endOfBssSection = startingProgram.endOfBssSection();

        for(int i = beginningOfBssSection; i <= endOfBssSection; i++){
            ICode instruction = startingProgram.getInstruction(i);
            if(instruction instanceof Def){
                Def assign = (Def)instruction;

                String originalPlace = assign.label;
                if(!placeIsUniqueAcrossProgramAndLibraries(originalPlace, startingProgram, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                    replacePlaceAcrossProgramAndLibraries(originalPlace, place, startingProgram, libraries, startingProgram);
                }

                Exp assignExp = assign.val;
                if(assignExp instanceof BinExp){
                    BinExp assignBinExp = (BinExp)assignExp;

                    if(assignBinExp.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)assignBinExp.left;
                        if(startingProgram.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = leftIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(startingProgram.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(startingProgram.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                            replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(startingProgram.containsVariableEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = startingProgram.getVariableEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }

                    String origPlace = assignIdentExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(origPlace, place, startingProgram, libraries, startingProgram);
                    }
                }

                newProg.addDataInstruction(instruction);
            }
        }
    }

    private void linkDataSections(Lib startingLibrary, Lib[] libraries, Lib newLib){
        int beginningOfDataSection = startingLibrary.beginningOfDataSection();
        int endOfDataSection = startingLibrary.endOfDataSection();

        for(int i = beginningOfDataSection; i <= endOfDataSection; i++){
            ICode instruction = startingLibrary.getInstruction(i);
            if(instruction instanceof Def){
                Def assign = (Def)instruction;

                String originalPlace = assign.label;
                if(!placeIsUniqueAcrossLibraries(originalPlace, startingLibrary, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                    replacePlaceAcrossLibraries(originalPlace, place, startingLibrary, libraries, startingLibrary);
                }

                Exp assignExp = assign.val;
                if(assignExp instanceof BinExp){
                    BinExp assignBinExp = (BinExp)assignExp;

                    if(assignBinExp.left instanceof IdentExp){
                        IdentExp leftIdent = (IdentExp)assignBinExp.left;
                        if(startingLibrary.containsVariableEntryWithICodePlace(leftIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingLibrary.getVariableEntryByICodePlace(leftIdent.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }

                        String origPlace = leftIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                            replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                        }
                    }

                    if(assignBinExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignBinExp.right;
                        if(startingLibrary.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingLibrary.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                            replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                        }
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    if(assignUnExp.right instanceof IdentExp){
                        IdentExp rightIdent = (IdentExp)assignUnExp.right;
                        if(startingLibrary.containsVariableEntryWithICodePlace(rightIdent.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingLibrary.getVariableEntryByICodePlace(rightIdent.ident, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                        }

                        String origPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                            replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                        }
                    }
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(startingLibrary.containsVariableEntryWithICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = startingLibrary.getVariableEntryByICodePlace(assignIdentExp.ident, SymEntry.EXTERNAL);
                        if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                    }

                    String origPlace = assignIdentExp.ident;
                    if(!placeIsUniqueAcrossLibraries(origPlace, startingLibrary, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                        replacePlaceAcrossLibraries(origPlace, place, startingLibrary, libraries, startingLibrary);
                    }
                }
                newLib.addDataInstruction(assign);
            } else if(instruction instanceof Call){
                Call call = (Call)instruction;
                
                if(!newLib.containsProcedure(call.pname)){
                    if(startingLibrary.containsProcedure(call.pname))
                        fetchInternalProcedure(startingLibrary, call.pname, startingLibrary, libraries, newLib);
                    else
                        fetchExternalProcedure(call.pname, startingLibrary, libraries, newLib, startingLibrary);
                }
                   

                for(Def arg : call.params){
                    if(arg.val instanceof IdentExp){
                        IdentExp identExp = (IdentExp)arg.val;
                        String value = identExp.ident;

                        if(startingLibrary.containsVariableEntryWithICodePlace(value, SymEntry.EXTERNAL)){
                            VarSymEntry entry = startingLibrary.getVariableEntryByICodePlace(value, SymEntry.EXTERNAL);
                            if(!newLib.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib, startingLibrary);
                        }
                    }

                    String paramName = arg.label;
                    if(!placeIsUniqueAcrossLibraries(paramName, startingLibrary, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                        replacePlaceAcrossLibraries(paramName, place, startingLibrary, libraries, startingLibrary);
                    }
                }
                newLib.addDataInstruction(call);
            }
        }
    }

    private void linkCodeSection(Prog program, Lib[] libraries, Prog newProg){
        int newSymbolsBeginning = newProg.beginningOfSymbolSection();
        int newSymbolsEnd = newProg.endOfSymbolSection(); 

        int codeSectionBegin = program.beginningOfCodeSection();
        int codeSectionEnd = program.endOfCodeSection();

        for(int i = codeSectionBegin; i <= codeSectionEnd; i++){
            ICode icode = program.getInstruction(i);
            if(icode instanceof Assign){
                Assign assignment = (Assign)icode;

                String origPlace = assignment.place;
                if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, program, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(origPlace, place, program, libraries, program);
                }

                Exp assignExp = assignment.value;
                if(assignExp instanceof IdentExp){
                    IdentExp ident = (IdentExp)assignExp;
                    
                    if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String value = ident.ident; 
                    if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp unExp = (UnExp)assignExp;
                    
                    if(unExp.right instanceof IdentExp){
                        IdentExp ident = (IdentExp)unExp.right;
                        if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = ident.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    if(binExp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)binExp.left;
                        if(program.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = leftExp.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }

                    if(binExp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)binExp.right;
                        if(program.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = rightExp.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }
                }
            } else if(icode instanceof Def){
                Def assignment = (Def)icode;

                String origPlace = assignment.label;
                if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, program, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(origPlace, place, program, libraries, program);
                }

                Exp assignExp = assignment.val;
                if(assignExp instanceof IdentExp){
                    IdentExp ident = (IdentExp)assignExp;
                    
                    if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String value = ident.ident; 
                    if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp unExp = (UnExp)assignExp;
                    
                    if(unExp.right instanceof IdentExp){
                        IdentExp ident = (IdentExp)unExp.right;
                        if(program.containsVariableEntryWithICodePlace(ident.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(ident.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = ident.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    if(binExp.left instanceof IdentExp){
                        IdentExp leftExp = (IdentExp)binExp.left;
                        if(program.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = leftExp.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }

                    if(binExp.right instanceof IdentExp){
                        IdentExp rightExp = (IdentExp)binExp.right;
                        if(program.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }

                        String value = rightExp.ident; 
                        if(!placeIsUniqueAcrossProgramAndLibraries(value, program, libraries)){
                            String place;
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(value, place, program, libraries, program);
                        }
                    }
                }
            } else if(icode instanceof If){
                If ifStat = (If)icode;

                BinExp exp = ifStat.exp;
                if(exp.left instanceof IdentExp){
                    IdentExp leftExp = (IdentExp)exp.left;
                    if(program.containsVariableEntryWithICodePlace(leftExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = program.getVariableEntryByICodePlace(leftExp.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String origPlace = leftExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(origPlace, place, program, libraries, program);
                    }
                }

                if(exp.right instanceof IdentExp){
                    IdentExp rightExp = (IdentExp)exp.right;
                    if(program.containsVariableEntryWithICodePlace(rightExp.ident, SymEntry.EXTERNAL)){
                        VarSymEntry entry = program.getVariableEntryByICodePlace(rightExp.ident, SymEntry.EXTERNAL);
                        if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String origPlace = rightExp.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(origPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

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
                
                if(!newProg.containsProcedure(call.pname)){
                    if(program.containsProcedure(call.pname))
                        fetchInternalProcedure(program, call.pname, libraries, newProg);
                    else
                        fetchExternalProcedure(call.pname, program, libraries, newProg, program);
                }
                   

                for(Def arg : call.params){
                    if(arg.val instanceof IdentExp){
                        IdentExp argVal = (IdentExp)arg.val;
                        String value = argVal.ident;

                        if(program.containsVariableEntryWithICodePlace(value, SymEntry.EXTERNAL)){
                            VarSymEntry entry = program.getVariableEntryByICodePlace(value, SymEntry.EXTERNAL);
                            if(!newProg.containsVariableEntryWithIdentifier(entry.declanIdent, SymEntry.INTERNAL))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                        }
                    }
                    

                    String paramName = arg.label;
                    if(!placeIsUniqueAcrossProgramAndLibraries(paramName, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

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
            newProg.addProgramInstruction(icode);
        }
    }

    private enum ProcedureState{
        PROCEDURE_INIT,
        PROCEDURE_BODY,
        PROCEDURE_SKIP
    }

    private void linkProcedureSections(Lib library, Lib[] libraries, Lib newLib){

        int beginningOfLibProcedures = library.beginningOfProcedureSection();
        int endOfLibProcedures = library.endOfProcedureSection();

        ProcedureState state = ProcedureState.PROCEDURE_INIT;
        String procName = null;
        for(int i = beginningOfLibProcedures; i <= endOfLibProcedures; i++){
            ICode instruction = library.getInstruction(i);
            switch(state){
                case PROCEDURE_INIT:
                    if(instruction instanceof ProcLabel){
                        ProcLabel label = (ProcLabel)instruction;
                        procName = label.label;
                        if(newLib.containsProcedure(procName)){
                           state = ProcedureState.PROCEDURE_SKIP; 
                        } else {
                           state = ProcedureState.PROCEDURE_BODY;
                           newLib.addProcedureHeader(label.label);
                        }
                    }
                    break;
                case PROCEDURE_SKIP:
                    if(instruction instanceof Return){
                        state = ProcedureState.PROCEDURE_INIT;
                    }
                    break;
                case PROCEDURE_BODY:
                    if(instruction instanceof Return){
                        state = ProcedureState.PROCEDURE_INIT;
                    } else {
                        newLib.addProcedureInstruction(procName, instruction);
                    }
                    break;
                default:
                    throw new ICodeLinkerException(instruction, "Invalid state found when linking procedure -> " + state);
            }
        }

        for(Lib lib : libraries){
            int beginningOtherLibFromList = lib.beginningOfProcedureSection();
            int endOtherLibFromList = lib.endOfProcedureSection();
            for(int i = beginningOtherLibFromList; i <= endOtherLibFromList; i++){
                ICode instruction = lib.getInstruction(i);
                switch(state){
                    case PROCEDURE_INIT:
                        if(instruction instanceof ProcLabel){
                            ProcLabel label = (ProcLabel)instruction;
                            procName = label.label;
                            if(newLib.containsProcedure(procName)){
                               state = ProcedureState.PROCEDURE_SKIP; 
                            } else {
                               state = ProcedureState.PROCEDURE_BODY;
                               newLib.addProcedureHeader(procName);
                            }
                        }
                        break;
                    case PROCEDURE_SKIP:
                        if(instruction instanceof Return){
                            state = ProcedureState.PROCEDURE_INIT;
                        }
                        break;
                    case PROCEDURE_BODY:
                        if(instruction instanceof Return){
                            state = ProcedureState.PROCEDURE_INIT;
                        } else {
                            newLib.addProcedureInstruction(procName, instruction);
                        }
                        break;
                    default:
                        throw new ICodeLinkerException(instruction, "Invalid state found when linking procedure -> " + state);
                }
            }
        }
    }

    public Prog performLinkage(Prog program, Lib... libraries){
        Prog newProg = new Prog(true);
        linkDataSections(program, libraries, newProg);
        linkBssSection(program, libraries, newProg);
        linkCodeSection(program, libraries, newProg);
        return newProg;
    }

    public Prog performLinkage(Program prog, Library... libraries){
        Prog generatedProgram = generateProgram(errLog, prog);
        Lib[] libs = generateLibraries(errLog, libraries);
        return performLinkage(generatedProgram, libs);
    }

    public Lib performLinkage(Lib library, Lib... libraries){
        Lib newLib = new Lib(true);
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
