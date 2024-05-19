package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
import io.github.H20man13.DeClan.common.pat.P;

public class Lib implements ICode {
    public DataSec variables;
    public ProcSec procedures;
    public SymSec symbols;
    public End end;

    public Lib(SymSec symbols, DataSec variables, ProcSec procedures){
        this.variables = variables;
        this.procedures = procedures;
        this.symbols = symbols;
    }

    public Lib(){
        this.variables = new DataSec();
        this.procedures = new ProcSec();
        this.symbols = new SymSec();
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public P asPattern() {
        return P.PAT(variables.asPattern(), procedures.asPattern());
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Lib){
            Lib lib = (Lib)obj;
            
            boolean symbolsEquals = lib.symbols.equals(symbols);
            boolean dataEquals = lib.variables.equals(variables);
            boolean procEquals = lib.procedures.equals(procedures);
            
            return symbolsEquals && dataEquals && procEquals;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(symbols.toString());
        sb.append(variables.toString());
        sb.append(procedures.toString());
        return sb.toString();
    }

    public boolean dataSectionContainsInstruction(ICode icode){
        int variablesLength = variables.getLength();
        for(int i = 0; i < variablesLength; i++){
            ICode instructionAtIndex = variables.getInstruction(i);
            if(instructionAtIndex.equals(icode))
                return true;
        }

        return false;
    }

    public boolean containsPlace(String place){
        if(symbols.containsPlace(place))
            return true;
        if(variables.containsPlace(place))
            return true;
        if(procedures.containsPlace(place))
            return true;

        return false;
    }

    public boolean containsLabel(String label){
        if(variables.containsLabel(label))
            return true;

        if(procedures.containsLabel(label))
            return true;

        return false;
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        resultList.addAll(variables.genFlatCode());
        resultList.addAll(procedures.genFlatCode());
        return resultList;
    }

    @Override
    public void replacePlace(String from, String to) {
        symbols.replacePlace(from, to);
        variables.replacePlace(from, to);
        procedures.replacePlace(from, to);
    }

    @Override
    public void replaceLabel(String from, String to) {
        variables.replaceLabel(from, to);
        procedures.replaceLabel(from, to);
    }

    public boolean containsExternalVariableByPlace(String place){
        return symbols.containsVariableEntryWithICodePlace(place, SymEntry.EXTERNAL);
    }

    public boolean containsInternalVariableByPlace(String place){
        return symbols.containsVariableEntryWithICodePlace(place, SymEntry.INTERNAL);
    }

    public boolean containsExternalVariableByIdent(String ident){
        return symbols.containsVariableEntryWithIdentifier(ident, SymEntry.EXTERNAL);
    }

    public boolean containsInternalVariableByIdent(String ident){
        return symbols.containsVariableEntryWithIdentifier(ident, SymEntry.INTERNAL);
    }

    public boolean containsExternalReturnByPlace(String place){
        return symbols.containsReturnEntryWithICodePlace(place, SymEntry.EXTERNAL);
    }

    public boolean containsInternalReturnByPlace(String place){
        return symbols.containsReturnEntryWithICodePlace(place, SymEntry.INTERNAL);
    }

    public boolean containsExternalReturnByFunctionName(String ident){
        return symbols.containsReturnEntryWithFunctionName(ident, SymEntry.EXTERNAL);
    }

    public boolean containsInternalReturnByFunctionName(String ident){
        return symbols.containsReturnEntryWithFunctionName(ident, SymEntry.INTERNAL);
    }

    public boolean containsExternalParamaterByPlace(String place){
        return symbols.containsParamaterEntryWithICodePlace(place, SymEntry.EXTERNAL);
    }

    public boolean containsInternalParamaterByPlace(String place){
        return symbols.containsParamaterEntryWithICodePlace(place, SymEntry.INTERNAL);
    }

    public boolean containsExternalReturnByFunctionNameAndNumber(String ident, int number){
        return symbols.containsParamaterEntryWithFunctionNameAndParamaterNumber(ident, number, SymEntry.EXTERNAL);
    }

    public boolean containsInternalReturnByFunctionName(String ident, int number){
        return symbols.containsParamaterEntryWithFunctionNameAndParamaterNumber(ident, number, SymEntry.INTERNAL);
    }

    public boolean containsParamater(String place){
        return procedures.containsParamater(place);
    }

    public VarSymEntry getInternalVariableByPlace(String place){
        return symbols.getVariableEntryByICodePlace(place, SymEntry.INTERNAL);
    }


    public VarSymEntry getInternalVariableByIdent(String ident){
        return symbols.getVariableEntryByIdentifier(ident, SymEntry.INTERNAL);
    }

    public VarSymEntry getExternalVariableByPlace(String place){
        return symbols.getVariableEntryByICodePlace(place, SymEntry.EXTERNAL);
    }

    public VarSymEntry getExternalVariableByIdent(String ident){
        return symbols.getVariableEntryByIdentifier(ident, SymEntry.EXTERNAL);
    }

    public RetSymEntry getInternalReturnByPlace(String place){
        return symbols.getReturnByICodePlace(place, SymEntry.INTERNAL);
    }


    public RetSymEntry getInternalReturnByFunctionName(String ident){
        return symbols.getReturnByFunctionName(ident, SymEntry.INTERNAL);
    }

    public RetSymEntry getExternalReturnByPlace(String place){
        return symbols.getReturnByICodePlace(place, SymEntry.EXTERNAL);
    }

    public RetSymEntry getExternalReturnByFunctionName(String ident){
        return symbols.getReturnByFunctionName(ident, SymEntry.EXTERNAL);
    }

    public ParamSymEntry getInternalParamaterByPlace(String place){
        return symbols.getParamaterByICodePlace(place, SymEntry.INTERNAL);
    }


    public ParamSymEntry getInternalParamaterByFunctionNameAndNumber(String ident, int paramNumber){
        return symbols.getParamaterByFunctionNameAndNumber(ident, paramNumber, SymEntry.INTERNAL);
    }

    public ParamSymEntry getExternalParamaterByPlace(String place){
        return symbols.getParamaterByICodePlace(place, SymEntry.EXTERNAL);
    }

    public ParamSymEntry getParamaterByFunctionNameAndNumber(String funcName, int number){
        return symbols.getParamaterByFunctionNameAndNumber(funcName, number, SymEntry.EXTERNAL);
    }

    @Override
    public boolean containsArgument(String place) {
        boolean variablesContainsArgument = variables.containsArgument(place);
        boolean proceduresContainsArgument = procedures.containsArgument(place);
        return variablesContainsArgument && proceduresContainsArgument;
    }

    @Override
    public Set<String> paramaterForFunctions(String place) {
        return procedures.paramaterForFunctions(place);
    }

    @Override
    public Set<String> argumentInFunctions(String place) {
        Set<String> newResult = new HashSet<String>();
        Set<String> varResult = variables.argumentInFunctions(place);
        Set<String> procResult = procedures.argumentInFunctions(place);
        newResult.addAll(varResult);
        newResult.addAll(procResult);
        return newResult;
    }

    @Override
    public Set<String> internalReturnForFunctions(String place) {
        return procedures.internalReturnForFunctions(place);
    }

    @Override
    public Set<String> externalReturnForFunctions(String place) {
        Set<String> newResult = new HashSet<String>();
        Set<String> varResult = variables.externalReturnForFunctions(place);
        Set<String> procResult = procedures.externalReturnForFunctions(place);
        newResult.addAll(varResult);
        newResult.addAll(procResult);
        return newResult;
    }

    @Override
    public boolean containsExternalReturn(String place) {
        if(variables.containsExternalReturn(place))
            return true;

        if(procedures.containsExternalReturn(place))
            return true;

        return false;
    }

    @Override
    public boolean containsInternalReturn(String place) {
        if(procedures.containsInternalReturn(place))
            return true;
        return false;
    }
}
