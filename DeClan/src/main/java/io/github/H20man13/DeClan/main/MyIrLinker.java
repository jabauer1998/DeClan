package io.github.H20man13.DeClan.main;

import java.util.HashSet;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.ast.Library;
import io.github.H20man13.DeClan.common.ast.Program;
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
import io.github.H20man13.DeClan.common.icode.Lib.SymbolSearchStrategy;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.inline.InlineParam;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
import io.github.H20man13.DeClan.common.util.Utils;

public class MyIrLinker {
    private IrRegisterGenerator gen;
    private ErrorLog errLog;
    private Config cfg;

    public MyIrLinker(Config cfg, ErrorLog errLog){
        this.errLog = errLog;
        this.gen = new IrRegisterGenerator();
        this.cfg = cfg;
        if(this.cfg != null)
        	if(this.cfg.containsFlag("debug"))
        		Utils.createFile("test/temp/linked.txt");
    }

    private static Prog generateProgram(ErrorLog errorLog, Program prog){
        MyICodeGenerator iGen = new MyICodeGenerator(null, errorLog);
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
        MyICodeGenerator iGen = new MyICodeGenerator(null, errLog);
        return iGen.generateLibraryIr(lib);
    }

    private void fetchExternalDependentInstructions(String identName, Prog program, Lib[] libraries, Prog newProgram, Lib... libsToIgnore){
        for(int libIndex = 0; libIndex < libraries.length; libIndex++){
            Lib library = libraries[libIndex];
            if(!Utils.arrayContainsValue(library, libsToIgnore)){
                if(library.containsEntry(identName, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                    VarSymEntry libEntry = library.getVariableData(identName, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
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
                                                    if(library.containsEntry(expVal.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                                        VarSymEntry entry = library.getVariableData(expVal.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                                        if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                                    if(library.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                                    if(!newProgram.containsEntry(identName, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        newProgram.addSymEntry(libEntry);
                                } else if(exp instanceof UnExp){
                                    UnExp unary = (UnExp)exp;
                                    IdentExp identExp = (IdentExp)unary.right;
                                    if(library.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                                    if(!newProgram.containsEntry(identName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        newProgram.addSymEntry(libEntry);
                                } else if(exp instanceof BinExp){
                                    BinExp binary = (BinExp)exp;

                                    IdentExp leftIdent = (IdentExp)binary.left;
                                    if(library.containsEntry(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, program, libraries, leftIdent.ident, newProgram);
                                    }

                                    String leftOldPlace = leftIdent.ident;
                                    if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, program, libraries)){
                                        String newPlace = null;    
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                        replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, program, libraries, library);
                                    }

                                    IdentExp rightIdent = (IdentExp)binary.right;
                                    if(library.containsEntry(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, program, libraries, rightIdent.ident, newProgram);
                                    }

                                    String rightOldPlace = rightIdent.ident;
                                    if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, program, libraries)){
                                        String newPlace = null;    
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                                        replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, program, libraries, library);
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
                                    if(!newProgram.containsEntry(identName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                                    if(!newProgram.containsEntry(identName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                if(library.containsEntry(identName, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                    VarSymEntry libEntry = library.getVariableData(identName, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
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
                                                    if(library.containsEntry(val.ident, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                                        VarSymEntry entry = library.getVariableData(val.ident, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                                        if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                                    if(library.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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


                                    if(!newLib.dataSectionContainsInstruction(assignLib))
                                        newLib.addDataInstruction(assignLib);
                                    if(!newLib.containsEntry(identName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        newLib.addSymEntry(libEntry);
                                } else if(exp instanceof UnExp){
                                    UnExp unary = (UnExp)exp;
                                    IdentExp identExp = unary.right;
                                    if(library.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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


                                    if(!newLib.dataSectionContainsInstruction(assignLib))
                                        newLib.addDataInstruction(assignLib);
                                    if(!newLib.containsEntry(identName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        newLib.addSymEntry(libEntry);
                                } else if(exp instanceof BinExp){
                                    BinExp binary = (BinExp)exp;

                                    IdentExp leftIdent = binary.left;
                                    if(library.containsEntry(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, leftIdent.ident, newLib);
                                    }

                                    String leftOldPlace = leftIdent.ident;
                                    if(!placeIsUniqueAcrossLibraries(leftOldPlace, single, libraries)){
                                        String newPlace = null;    
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                        replacePlaceAcrossLibraries(leftOldPlace, newPlace, single, libraries, library);
                                    }

                                    IdentExp rightIdent = (IdentExp)binary.right;
                                    if(library.containsEntry(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, rightIdent.ident, newLib);
                                    }

                                    String rightOldPlace = rightIdent.ident;
                                    if(!placeIsUniqueAcrossLibraries(rightOldPlace, single, libraries)){
                                        String newPlace = null;    
                                        do{
                                            newPlace = gen.genNext();
                                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                                        replacePlaceAcrossLibraries(rightOldPlace, newPlace, single, libraries, library);
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
                                    if(!newLib.containsEntry(identName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                                    if(!newLib.containsEntry(identName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                                        if(currentLib.containsEntry(val.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                            VarSymEntry entry = currentLib.getVariableData(val.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                        if(currentLib.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                newLib.addSymEntry(entry);
                        }
                    } else if(exp instanceof UnExp){
                        UnExp unary = (UnExp)exp;
                        
                        IdentExp identExp = unary.right;
                        if(currentLib.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                newLib.addSymEntry(entry);
                        }
                    } else if(exp instanceof BinExp){
                        BinExp binary = (BinExp)exp;

                        IdentExp leftIdent = binary.left;
                        if(currentLib.containsEntry(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, single, libraries, leftIdent.ident, newLib);
                        }

                        String leftOldPlace = leftIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(leftOldPlace, single, libraries)){
                            String newPlace = null;    
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                            replacePlaceAcrossLibraries(leftOldPlace, newPlace, single, libraries, currentLib);
                        }

                        IdentExp rightIdent = binary.right;
                        if(currentLib.containsEntry(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, single, libraries, rightIdent.ident, newLib);
                        }

                        String rightOldPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossLibraries(rightOldPlace, single, libraries)){
                            String newPlace = null;    
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));

                            replacePlaceAcrossLibraries(rightOldPlace, newPlace, single, libraries, currentLib);
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
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                                        if(currentLib.containsEntry(val.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                            VarSymEntry entry = currentLib.getVariableData(val.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        
                        if(currentLib.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, program, libraries, identExp.ident, newProgram);
                        }
                        
                        if(!placeIsUniqueAcrossProgramAndLibraries(identExp.ident, program, libraries)){
                            String newPlace = null;    
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(identExp.ident, newPlace, program, libraries, currentLib);
                        }
                        
                        if(!newProgram.dataSectionContainsInstruction(assign))
                            newProgram.addDataInstruction(assign);
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                newProgram.addSymEntry(entry);
                        }
                    } else if(exp instanceof UnExp){
                        UnExp unary = (UnExp)exp;
                        
                        IdentExp identExp = unary.right;
                        if(currentLib.containsEntry(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(identExp.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                        if(!placeIsUniqueAcrossProgramAndLibraries(assign.label, program, libraries)){
                            String place = null;    
                            do{
                                place = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(assign.label, place, program, libraries, currentLib);
                        }

                        if(!newProgram.dataSectionContainsInstruction(assign))
                            newProgram.addDataInstruction(assign);
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                newProgram.addSymEntry(entry);
                        }
                    } else if(exp instanceof BinExp){
                        BinExp binary = (BinExp)exp;

                        IdentExp leftIdent = binary.left;
                        if(currentLib.containsEntry(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(leftIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram, currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, program, libraries, leftIdent.ident, newProgram);
                        }

                        String leftOldPlace = leftIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, program, libraries)){
                            String newPlace = null;    
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, program, libraries, currentLib);
                        }

                        IdentExp rightIdent = binary.right;
                        if(currentLib.containsEntry(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(rightIdent.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProgram,  currentLib);
                        } else {
                            fetchInternalDependentInstructions(currentLib, program, libraries, rightIdent.ident, newProgram);
                        }

                        String rightOldPlace = rightIdent.ident;
                        if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, program, libraries)){
                            String newPlace = null;    
                            do{
                                newPlace = gen.genNext();
                            } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, program, libraries));

                            replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, program, libraries, currentLib);
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
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(currentLib.containsEntry(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = currentLib.getVariableData(labelName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProgram.containsEntry(entry.declanIdent, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(ident.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(ident.ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        if(program.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                    if(program.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                    if(program.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                            if(program.containsEntry(place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = program.getVariableData(place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                        
                        if(library.containsEntry(newPlace, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(newPlace, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(newPlace, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
                        		newProg.addSymEntry(entry);
                        	}
                        } else if(library.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        	if(!newProg.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		fetchInternalDependentInstructions(library, prog, libraries, newPlace, newProg);
                        	}
                        }
                    } else {
                    	if(library.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
                        		newProg.addSymEntry(entry);
                        	}
                        } else if(library.containsEntry(assignment.place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        	if(!newProg.containsEntry(assignment.place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		fetchInternalDependentInstructions(library, prog, libraries, assignment.place, newProg);
                        	}
                        }
                    }

                    Exp assignExp = assignment.value;
                    if(assignExp instanceof IdentExp){
                        IdentExp ident = (IdentExp)assignExp;
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                    
                    if(library.containsEntry(definition.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    	if(!newProg.containsEntry(definition.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                    		SymEntry entry = library.getVariableData(definition.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
                    		newProg.addSymEntry(entry);
                    	}
                    }
                    
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
                        
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                        } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newProg.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newProg.addSymEntry(entry);
                        	}
                        } else {
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
                    IdentExp leftExp = exp.left;
                    if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                    } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    	if(!newProg.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                    		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    		newProg.addSymEntry(entry);
                    	}
                    } else {
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

                    IdentExp rightExp = exp.right;
                    if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                    } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    	if(!newProg.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                    		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    		newProg.addSymEntry(entry);
                    	}
                    } else {
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

                            if(library.containsEntry(place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(library.containsEntry(place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newProg.containsEntry(place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newProg.addSymEntry(entry);
                            	}
                            } else {
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
                    
                    if(library.containsEntry(assignment.place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = library.getVariableData(assignment.place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                    } else if(library.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    	if(!newLib.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                    		SymEntry entry = library.getVariableData(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    		newLib.addSymEntry(entry);
                    	}
                    } else {
                        fetchInternalDependentInstructions(library, single, libraries, assignment.place, newLib);
                    }
                    
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
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                    
                    if(library.containsEntry(definition.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    	if(!newLib.containsEntry(definition.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                    		SymEntry entry = library.getVariableData(definition.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    		newLib.addSymEntry(entry);
                    	}
                    }
                    
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
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                        if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                        } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        	if(!newLib.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                        		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        		newLib.addSymEntry(entry);
                        	}
                        } else {
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
                    IdentExp leftExp = exp.left;
                    if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                    } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    	if(!newLib.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                    		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    		newLib.addSymEntry(entry);
                    	}
                    } else {
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

                    IdentExp rightExp = exp.right;
                    if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                    } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    	if(!newLib.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                    		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    		newLib.addSymEntry(entry);
                    	}
                    } else {
                        fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                    }

                    String rightOldPlace = rightExp.ident;
                    if(!placeIsUniqueAcrossLibraries(rightOldPlace, single, libraries)){
                        String newPlace;
                        do{
                            newPlace = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
    
                        replacePlaceAcrossLibraries(rightOldPlace, newPlace, single, libraries, library);
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

                            if(library.containsEntry(place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(library.containsEntry(place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newLib.containsEntry(place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newLib.addSymEntry(entry);
                            	}
                            } else {
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
                            
                            if(library.containsEntry(assignment.place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(assignment.place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(library.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newProg.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newProg.addSymEntry(entry);
                            	}
                            } else {
                                fetchInternalDependentInstructions(library, prog, libraries, assignment.place, newProg);
                            }

                            Exp assignExp = assignment.value;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                                
                                String rightOldPlace = ident.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, prog, libraries, library);
                                 }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                IdentExp ident = unExp.right;
                                String rightOldPlace = ident.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, prog, libraries, library);
                                }
                                
                                ident = unExp.right;
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                IdentExp leftExp = binExp.left;

                                String leftOldPlace = leftExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(leftOldPlace, newPlace, prog, libraries, library);
                                }
                                
                                if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                                }

                                IdentExp rightExp = binExp.right;

                                String oldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                }
                                
                                if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
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
                            
                            if(library.containsEntry(assignment.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newProg.containsEntry(assignment.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(assignment.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newProg.addSymEntry(entry);
                            	}
                            }

                            Exp assignExp = assignment.val;
                            if(assignExp instanceof IdentExp){
                                IdentExp ident = (IdentExp)assignExp;
                                
                                String rightOldPlace = ident.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, prog, libraries, library);
                                    
                                }
                                
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof UnExp){
                                UnExp unExp = (UnExp)assignExp;
                                
                                IdentExp ident = unExp.right;
                                
                                String rightOldPlace = ident.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(rightOldPlace, newPlace, prog, libraries, library);
                                }
                                
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                }
                            } else if(assignExp instanceof BinExp){
                                BinExp binExp = (BinExp)assignExp;

                                IdentExp leftExp = binExp.left;
                                if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
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
                                if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
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
                            IdentExp leftExp = exp.left;
                            if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newProg.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newProg.addSymEntry(entry);
                            	}
                            } else {
                                fetchInternalDependentInstructions(library, prog, libraries, leftExp.ident, newProg);
                            }

                            String leftPlace = leftExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(leftPlace, prog, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(leftPlace, newPlace, prog, libraries, library);
                            }
                            
                            IdentExp rightExp = exp.right;
                            if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                            } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newProg.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newProg.addSymEntry(entry);
                            	}
                            } else {
                                fetchInternalDependentInstructions(library, prog, libraries, rightExp.ident, newProg);
                            }

                            String rightPlace = rightExp.ident;
                            if(!placeIsUniqueAcrossProgramAndLibraries(rightPlace, prog, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
            
                                replacePlaceAcrossProgramAndLibraries(rightPlace, newPlace, prog, libraries, library);
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
                            	if(library.containsEntry(arg.label,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(arg.label,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(arg.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(arg.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(arg.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, arg.label, newProg);
                                }
                            	
                            	String oldPlace = arg.label;
                                if(!placeIsUniqueAcrossProgramAndLibraries(oldPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(oldPlace, newPlace, prog, libraries, library);
                                }
                            	
                                if(arg.val instanceof IdentExp){
                                    IdentExp ident = (IdentExp)arg.val;

                                    if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                            fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                    } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    	if(!newProg.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                    		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    		newProg.addSymEntry(entry);
                                    	}
                                    } else {
                                        fetchInternalDependentInstructions(library, prog, libraries, ident.ident, newProg);
                                    }
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
                        } else if(icode instanceof Inline) {
                        	Inline inlineAsm = (Inline)icode;
                        	
                        	for(InlineParam arg: inlineAsm.params) {
                        		if(library.containsEntry(arg.name.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(arg.name.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, prog, libraries, newProg, library);
                                } else if(library.containsEntry(arg.name.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newProg.containsEntry(arg.name.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(arg.name.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newProg.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, prog, libraries, arg.name.ident, newProg);
                                }

                                String leftPlace = arg.name.ident;
                                if(!placeIsUniqueAcrossProgramAndLibraries(leftPlace, prog, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(newPlace, prog, libraries));
                
                                    replacePlaceAcrossProgramAndLibraries(leftPlace, newPlace, prog, libraries, library);
                                }
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
                            
                            if(library.containsEntry(assignment.place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(assignment.place,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(library.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newLib.containsEntry(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(assignment.place, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newLib.addSymEntry(entry);
                            	}
                            } else {
                                fetchInternalDependentInstructions(library, single, libraries, assignment.place, newLib);
                            }
                            
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
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
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
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
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
                                if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
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
                                if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                }

                                String rightOldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossLibraries(rightOldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(rightOldPlace, newPlace, single, libraries, library);
                                }
                            }
                        } else if(icode instanceof Def){
                            Def assignment = (Def)icode;
                            
                            if(library.containsEntry(assignment.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newLib.containsEntry(assignment.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(assignment.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newLib.addSymEntry(entry);
                            	}
                            }
                            
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
                                
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
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
                                
                                IdentExp ident = (IdentExp)unExp.right;
                                if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
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
                                if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
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
                                if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                                }

                                String rightOldPlace = rightExp.ident;
                                if(!placeIsUniqueAcrossLibraries(rightOldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(rightOldPlace, newPlace, single, libraries, library);    
                                }
                            }
                        } else if(icode instanceof If){
                            If ifStat = (If)icode;

                            BinExp exp = ifStat.exp;
                            IdentExp leftExp = exp.left;
                            if(library.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(library.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newLib.containsEntry(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(leftExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newLib.addSymEntry(entry);
                            	}
                            } else {
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

                            IdentExp rightExp = exp.right;
                            if(library.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                VarSymEntry entry = library.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                    fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                            } else if(library.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            	if(!newLib.containsEntry(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                            		SymEntry entry = library.getVariableData(rightExp.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            		newLib.addSymEntry(entry);
                            	}
                            } else {
                                fetchInternalDependentInstructions(library, single, libraries, rightExp.ident, newLib);
                            }

                            String rightOldPlace = rightExp.ident;
                            if(!placeIsUniqueAcrossLibraries(rightOldPlace, single, libraries)){
                                String newPlace;
                                do{
                                    newPlace = gen.genNext();
                                } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
            
                                replacePlaceAcrossLibraries(rightOldPlace, newPlace, single, libraries, library);
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
                            	if(library.containsEntry(arg.label,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    VarSymEntry entry = library.getVariableData(arg.label,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                        fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                } else if(library.containsEntry(arg.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                	if(!newLib.containsEntry(arg.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                		SymEntry entry = library.getVariableData(arg.label, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                		newLib.addSymEntry(entry);
                                	}
                                } else {
                                    fetchInternalDependentInstructions(library, single, libraries, arg.label, newLib);
                                }
                            	
                            	String oldPlace = arg.label;
                                if(!placeIsUniqueAcrossLibraries(oldPlace, single, libraries)){
                                    String newPlace;
                                    do{
                                        newPlace = gen.genNext();
                                    } while(!newPlaceWillBeUniqueAcrossLibraries(newPlace, single, libraries));
                
                                    replacePlaceAcrossLibraries(oldPlace, newPlace, single, libraries, library);
                                }
                            	
                                if(arg.val instanceof IdentExp){
                                    IdentExp ident = (IdentExp)arg.val;

                                    if(library.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                        VarSymEntry entry = library.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                                            fetchExternalDependentInstructions(entry.declanIdent, single, libraries, newLib, library);
                                    } else if(library.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                                    	if(!newLib.containsEntry(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
                                    		SymEntry entry = library.getVariableData(ident.ident, procName, SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                                    		newLib.addSymEntry(entry);
                                    	}
                                    } else {
                                        fetchInternalDependentInstructions(library, single, libraries, ident.ident, newLib);
                                    }
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
        if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry entryToLook = libToAlwaysReplace.getVariableData(newPlace, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                    VarSymEntry entryToReplace = program.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        program.replacePlace(placeFrom, newPlace);
                } else if(program.containsEntry(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) ){
                    VarSymEntry entryToReplace = program.getVariableData(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        program.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                        VarSymEntry entryToReplace = library.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsEntry(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) ){
                        VarSymEntry entryToReplace = library.getVariableData(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
            VarSymEntry entryToLook = libToAlwaysReplace.getVariableData(newPlace, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String ident = entryToLook.declanIdent;
            if(!libToAlwaysReplace.equals(program)){
                if(program.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                    VarSymEntry entryToReplace = program.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        program.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                        VarSymEntry entryToReplace = library.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry entry  = libToAlwaysReplace.getVariableData(newPlace, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                    VarSymEntry localEntry = program.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                } else if(program.containsEntry(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM)){
                    VarSymEntry localEntry = program.getVariableData(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                        VarSymEntry localEntry = library.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsEntry(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM)){
                        VarSymEntry localEntry = library.getVariableData(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry entry  = libToAlwaysReplace.getVariableData(newPlace, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(program)){
                if(program.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                    VarSymEntry localEntry = program.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                        VarSymEntry localEntry = library.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry returnEntry = libToAlwaysReplace.getVariableData(newPlace, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String funcName = returnEntry.funcName;

            if(!program.equals(libToAlwaysReplace)){
                if(program.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                    VarSymEntry myRetEntry = program.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                } else if(program.containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                    VarSymEntry myRetEntry = program.getVariableData(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                        VarSymEntry myRetEntry = library.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                        VarSymEntry myRetEntry = library.getVariableData(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry returnEntry = libToAlwaysReplace.getVariableData(newPlace, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String funcName = returnEntry.funcName;

            if(!program.equals(libToAlwaysReplace)){
                if(program.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                    VarSymEntry myRetEntry = program.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        program.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                        VarSymEntry myRetEntry = library.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
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
        if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry entryToLook = libToAlwaysReplace.getVariableData(newPlace, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String ident = entryToLook.declanIdent;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                    VarSymEntry entryToReplace = lib.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        lib.replacePlace(placeFrom, newPlace);
                } else if(lib.containsEntry(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) ){
                    VarSymEntry entryToReplace = lib.getVariableData(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        lib.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                        VarSymEntry entryToReplace = library.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsEntry(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) ){
                        VarSymEntry entryToReplace = library.getVariableData(ident, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
            VarSymEntry entryToLook = libToAlwaysReplace.getVariableData(newPlace, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String ident = entryToLook.declanIdent;
            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                    VarSymEntry entryToReplace = lib.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                    String placeFrom = entryToReplace.icodePlace;
                    if(!placeFrom.equals(newPlace))
                        lib.replacePlace(placeFrom, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME)){
                        VarSymEntry entryToReplace = library.getVariableData(ident, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME);
                        String fromPlace = entryToReplace.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry entry  = libToAlwaysReplace.getVariableData(newPlace, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                    VarSymEntry localEntry = lib.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                } else if(lib.containsEntry(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM)){
                    VarSymEntry localEntry = lib.getVariableData(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                        VarSymEntry localEntry = library.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsEntry(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM)){
                        VarSymEntry localEntry = library.getVariableData(funcName, paramNumber, SymEntry.INTERNAL | SymEntry.PARAM);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry entry  = libToAlwaysReplace.getVariableData(newPlace, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) ;
            int paramNumber = entry.paramNumber;
            String funcName = entry.funcName;

            if(!libToAlwaysReplace.equals(lib)){
                if(lib.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                    VarSymEntry localEntry = lib.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                    String fromPlace = localEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!libToAlwaysReplace.equals(library)){
                    if(library.containsEntry(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM)){
                        VarSymEntry localEntry = library.getVariableData(funcName, paramNumber, SymEntry.EXTERNAL | SymEntry.PARAM);
                        String fromPlace = localEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry returnEntry = libToAlwaysReplace.getVariableData(newPlace, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String funcName = returnEntry.funcName;

            if(!lib.equals(libToAlwaysReplace)){
                if(lib.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                    VarSymEntry myRetEntry = lib.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                } else if(lib.containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                    VarSymEntry myRetEntry = lib.getVariableData(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                        VarSymEntry myRetEntry = library.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    } else if(library.containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                        VarSymEntry myRetEntry = library.getVariableData(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                        String fromPlace = myRetEntry.icodePlace;
                        if(!fromPlace.equals(newPlace))
                            library.replacePlace(fromPlace, newPlace);
                    }
                }
            }
        } else if(libToAlwaysReplace.containsEntry(newPlace, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            VarSymEntry returnEntry = libToAlwaysReplace.getVariableData(newPlace, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
            String funcName = returnEntry.funcName;

            if(!lib.equals(libToAlwaysReplace)){
                if(lib.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                    VarSymEntry myRetEntry = lib.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                    String fromPlace = myRetEntry.icodePlace;
                    if(!fromPlace.equals(newPlace))
                        lib.replacePlace(fromPlace, newPlace);
                }
            }

            for(Lib library: libraries){
                if(!library.equals(libToAlwaysReplace)){
                    if(library.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
                        VarSymEntry myRetEntry = library.getVariableData(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
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

        if(program.containsEntry(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            externalReturnsRepresented.add(program.getVariableData(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
        } else if(program.containsEntry(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            internalReturnsRepresented.add(program.getVariableData(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
        } else if(program.containsEntry(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            externalIdentsRepresented.add(program.getVariableData(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
        } else if(program.containsEntry(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            paramaterRepresented.add(program.getVariableData(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) .funcName);
        } else if(program.containsEntry(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            argumentRepresented.add(program.getVariableData(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
        } else if(program.containsEntry(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            internalIdentsRepresented.add(program.getVariableData(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
        } else if(program.containsPlace(place)){
            localVariableCount++;
        }

        for(Lib lib: libraries){
            if(lib.containsEntry(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                externalReturnsRepresented.add(lib.getVariableData(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
            } else if(lib.containsEntry(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                internalReturnsRepresented.add(lib.getVariableData(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
            } else if(lib.containsEntry(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                externalIdentsRepresented.add(lib.getVariableData(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
            } else if(lib.containsEntry(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                paramaterRepresented.add(lib.getVariableData(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) .funcName);
            } else if(lib.containsEntry(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                argumentRepresented.add(lib.getVariableData(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
            } else if(lib.containsEntry(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                internalIdentsRepresented.add(lib.getVariableData(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
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

        if(library.containsEntry(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            externalReturnsRepresented.add(library.getVariableData(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
        } else if(library.containsEntry(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            internalReturnsRepresented.add(library.getVariableData(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
        } else if(library.containsEntry(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            externalIdentsRepresented.add(library.getVariableData(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
        } else if(library.containsEntry(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            paramaterRepresented.add(library.getVariableData(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) .funcName);
        } else if(library.containsEntry(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            argumentRepresented.add(library.getVariableData(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
        } else if(library.containsEntry(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
            internalIdentsRepresented.add(library.getVariableData(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
        } else if(library.containsPlace(place)){
            localVariableCount++;
        }

        for(Lib lib: libraries){
            if(lib.containsEntry(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                externalReturnsRepresented.add(lib.getVariableData(place, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
            } else if(lib.containsEntry(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                internalReturnsRepresented.add(lib.getVariableData(place, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
            } else if(lib.containsEntry(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                externalIdentsRepresented.add(lib.getVariableData(place, SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
            } else if(lib.containsEntry(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                paramaterRepresented.add(lib.getVariableData(place, SymEntry.INTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) .funcName);
            } else if(lib.containsEntry(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                argumentRepresented.add(lib.getVariableData(place, SymEntry.EXTERNAL | SymEntry.PARAM, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).funcName);
            } else if(lib.containsEntry(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                internalIdentsRepresented.add(lib.getVariableData(place, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION).declanIdent);
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

                    IdentExp leftIdent = assignBinExp.left;
                    if(startingProgram.containsEntry(leftIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(leftIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }

                    String leftOrigPlace = leftIdent.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(leftOrigPlace, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(leftOrigPlace, place, startingProgram, libraries, startingProgram);
                    }

                    IdentExp rightIdent = assignBinExp.right;
                    if(startingProgram.containsEntry(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }

                    String rightOrigPlace = rightIdent.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(rightOrigPlace, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(rightOrigPlace, place, startingProgram, libraries, startingProgram);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    
                    IdentExp rightIdent = assignUnExp.right;
                    if(startingProgram.containsEntry(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(startingProgram.containsEntry(assignIdentExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(assignIdentExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                newProg.addDataInstruction(assign);
                
                if(!newProg.containsEntry(assign.label, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
	                if(startingProgram.containsEntry(assign.label, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)) {
	                	SymEntry entry = startingProgram.getVariableData(assign.label, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
	                	newProg.addSymEntry(entry);
	                }
                }
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

                        if(startingProgram.containsEntry(value,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = startingProgram.getVariableData(value,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                
                if(startingProgram.containsEntry(assign.label, SymEntry.GLOBAL | SymEntry.INTERNAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                	VarSymEntry entry = startingProgram.getVariableData(assign.label, SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                	newProg.addSymEntry(entry);
                }

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

                    IdentExp leftIdent = assignBinExp.left;
                    if(startingProgram.containsEntry(leftIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(leftIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }

                    String leftOrigPlace = leftIdent.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(leftOrigPlace, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(leftOrigPlace, place, startingProgram, libraries, startingProgram);
                    }

                    IdentExp rightIdent = (IdentExp)assignBinExp.right;
                    if(startingProgram.containsEntry(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, startingProgram, libraries, newProg);
                    }

                    String rightOrigPlace = rightIdent.ident;
                    if(!placeIsUniqueAcrossProgramAndLibraries(rightOrigPlace, startingProgram, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, startingProgram, libraries));

                        replacePlaceAcrossProgramAndLibraries(rightOrigPlace, place, startingProgram, libraries, startingProgram);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    
                    IdentExp rightIdent = (IdentExp)assignUnExp.right;
                    if(startingProgram.containsEntry(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(startingProgram.containsEntry(assignIdentExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingProgram.getVariableData(assignIdentExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                newProg.addBssInstruction(instruction);
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

                    IdentExp leftIdent = assignBinExp.left;
                    if(startingLibrary.containsEntry(leftIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingLibrary.getVariableData(leftIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                    }

                    String leftOrigPlace = leftIdent.ident;
                    if(!placeIsUniqueAcrossLibraries(leftOrigPlace, startingLibrary, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                        replacePlaceAcrossLibraries(leftOrigPlace, place, startingLibrary, libraries, startingLibrary);
                    }

                    IdentExp rightIdent = assignBinExp.right;
                    if(startingLibrary.containsEntry(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingLibrary.getVariableData(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, startingLibrary, libraries, newLib);
                    }

                    String rightOrigPlace = rightIdent.ident;
                    if(!placeIsUniqueAcrossLibraries(rightOrigPlace, startingLibrary, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossLibraries(place, startingLibrary, libraries));

                        replacePlaceAcrossLibraries(rightOrigPlace, place, startingLibrary, libraries, startingLibrary);
                    }
                } else if(assignExp instanceof UnExp){
                    UnExp assignUnExp = (UnExp)assignExp;
                    IdentExp rightIdent = assignUnExp.right;
                    if(startingLibrary.containsEntry(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingLibrary.getVariableData(rightIdent.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                } else if(assignExp instanceof IdentExp){
                    IdentExp assignIdentExp = (IdentExp)assignExp;
                    if(startingLibrary.containsEntry(assignIdentExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = startingLibrary.getVariableData(assignIdentExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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

                        if(startingLibrary.containsEntry(value,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = startingLibrary.getVariableData(value,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newLib.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                    
                    if(program.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                    
                    IdentExp ident = unExp.right;
                    if(program.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    IdentExp leftExp = binExp.left;
                    if(program.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String leftOldPlace = leftExp.ident; 
                    if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(leftOldPlace, place, program, libraries, program);
                    }

                    IdentExp rightExp = (IdentExp)binExp.right;
                    if(program.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String rightOldPlace = rightExp.ident; 
                    if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(rightOldPlace, place, program, libraries, program);
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
                    
                    if(program.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                    
                    IdentExp ident = (IdentExp)unExp.right;
                    if(program.containsEntry(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(ident.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
                } else if(assignExp instanceof BinExp){
                    BinExp binExp = (BinExp)assignExp;

                    IdentExp leftExp = binExp.left;
                    if(program.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String leftOldPlace = leftExp.ident; 
                    if(!placeIsUniqueAcrossProgramAndLibraries(leftOldPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(leftOldPlace, place, program, libraries, program);
                    }

                    IdentExp rightExp = binExp.right;
                    if(program.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                        VarSymEntry entry = program.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                        if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                            fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                    }

                    String rightOldPlace = rightExp.ident; 
                    if(!placeIsUniqueAcrossProgramAndLibraries(rightOldPlace, program, libraries)){
                        String place;
                        do{
                            place = gen.genNext();
                        } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                        replacePlaceAcrossProgramAndLibraries(rightOldPlace, place, program, libraries, program);
                    }
                }
            } else if(icode instanceof If){
                If ifStat = (If)icode;

                BinExp exp = ifStat.exp;
                
                IdentExp leftExp = exp.left;
                if(program.containsEntry(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    VarSymEntry entry = program.getVariableData(leftExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                }

                String leftOrigPlace = leftExp.ident;
                if(!placeIsUniqueAcrossProgramAndLibraries(leftOrigPlace, program, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(leftOrigPlace, place, program, libraries, program);
                }

                IdentExp rightExp = exp.right;
                if(program.containsEntry(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                    VarSymEntry entry = program.getVariableData(rightExp.ident,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                    if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
                        fetchExternalDependentInstructions(entry.declanIdent, program, libraries, newProg);
                }

                String rightOrigPlace = rightExp.ident;
                if(!placeIsUniqueAcrossProgramAndLibraries(rightOrigPlace, program, libraries)){
                    String place;
                    do{
                        place = gen.genNext();
                    } while(!newPlaceWillBeUniqueAcrossProgramAndLibraries(place, program, libraries));

                    replacePlaceAcrossProgramAndLibraries(rightOrigPlace, place, program, libraries, program);
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

                        if(program.containsEntry(value,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION)){
                            VarSymEntry entry = program.getVariableData(value,  SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION);
                            if(!newProg.containsEntry(entry.declanIdent,  SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME))
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
        if(this.cfg != null)
        	if(this.cfg.containsFlag("debug"))
        		Utils.writeToFile("test/temp/linked.txt", newProg.toString());
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
