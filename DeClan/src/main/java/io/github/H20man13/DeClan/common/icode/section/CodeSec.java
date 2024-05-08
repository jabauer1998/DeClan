package io.github.H20man13.DeClan.common.icode.section;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.Utils;

public class CodeSec implements ICode {
    public List<ICode> intermediateCode;

    public CodeSec(List<ICode> code){
        this.intermediateCode = code;    
    }

    public CodeSec(){
        this(new LinkedList<ICode>());
    }

    public void addInstruction(ICode instruction){
        this.intermediateCode.add(instruction);
    }

    public int getLength(){
        return intermediateCode.size();
    }

    public ICode getInstruction(int index){
        return intermediateCode.get(index);
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
        int size = intermediateCode.size();
        P[] patList = new P[size + 1];
        int i;
        for(i = 0; i < size + 1; i++){
            patList[i] = intermediateCode.get(i).asPattern();
        }
        patList[i] = P.END();
        return P.PAT(patList);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("CODE SECTION\r\n");
        for(ICode icode : intermediateCode){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append(' ');
            innerSb.append(icode.toString());
            innerSb.append("\r\n");
            sb.append(Utils.formatStringToLeadingWhiteSpace(innerSb.toString()));
        }
        sb.append("END\r\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof CodeSec){
            CodeSec objSec = (CodeSec)obj;
            
            if(objSec.intermediateCode.size() != intermediateCode.size())
                return false;

            int size = objSec.getLength();

            for(int i = 0; i < size; i++){
                ICode objICode = objSec.getInstruction(i);
                ICode icode = getInstruction(i);
                if(!objICode.equals(icode))
                    return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        for(ICode icode : intermediateCode){
            resultList.addAll(icode.genFlatCode());
        }
        resultList.add(new End());
        return resultList;
    }

    @Override
    public boolean containsPlace(String place) {
        for(ICode icode: intermediateCode){
            if(icode.containsPlace(place))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        for(ICode icode: intermediateCode){
            if(icode.containsLabel(label))
                return true;
        }
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        for(ICode icode: intermediateCode){
            icode.replacePlace(from, to);
        }
    }

    @Override
    public void replaceLabel(String from, String to) {
        for(ICode icode: intermediateCode){
            icode.replaceLabel(from, to);
        }
    }

    @Override
    public boolean containsParamater(String place) {
        return false;
    }

    @Override
    public boolean containsArgument(String place) {
        for(ICode instruction: intermediateCode){
            if(instruction.containsArgument(place)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> paramaterForFunctions(String place) {
        HashSet<String> newResult = new HashSet<String>();
        for(ICode instruction: intermediateCode){
            Set<String> instructionResult = instruction.paramaterForFunctions(place);
            newResult.addAll(instructionResult);
        }
        return newResult;
    }

    @Override
    public Set<String> argumentInFunctions(String place) {
        HashSet<String> newResult = new HashSet<String>();
        for(ICode instruction: intermediateCode){
            Set<String> instructionResult = instruction.argumentInFunctions(place);
            newResult.addAll(instructionResult);
        }
        return newResult;
    }

    @Override
    public Set<String> internalReturnForFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public Set<String> externalReturnForFunctions(String place) {
        HashSet<String> toRet = new HashSet<String>();
        for(ICode instr: intermediateCode){
            Set<String> toAdd = instr.externalReturnForFunctions(place);
            toRet.addAll(toAdd);
        }
        return toRet;
    }

    @Override
    public boolean containsExternalReturn(String place) {
        for(ICode instr: intermediateCode){
            if(instr.containsExternalReturn(place))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsInternalReturn(String place) {
        return false;
    }
}
