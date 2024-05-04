package io.github.H20man13.DeClan.common.icode.section;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.Utils;

public class DataSec implements ICode {
    public List<ICode> intermediateCode;
    public DataSec(List<ICode> icode){
        this.intermediateCode = icode;
    }

    public DataSec(){
        this(new LinkedList<ICode>());
    }

    public void addInstruction(ICode icode){
        this.intermediateCode.add(icode);
    }

    public void addInstructions(List<ICode> icodes){
        this.intermediateCode.addAll(icodes);
    }

    public int getLength(){
        return intermediateCode.size();
    }

    public ICode getInstruction(int index){
        return intermediateCode.get(index);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof DataSec){
            DataSec dataSec = (DataSec)obj;
            
            if(dataSec.intermediateCode.size() != intermediateCode.size())
                return false;

            int size = dataSec.getLength();

            for(int i = 0; i < size; i++){
                ICode objICode = dataSec.getInstruction(i);
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
        P[] patList = new P[size];
        for(int i = 0; i < size; i++){
            patList[i] = intermediateCode.get(i).asPattern();
        }
        return P.PAT(patList);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("DATA SECTION\r\n");
        for(ICode icode : intermediateCode){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append(' ');
            innerSb.append(icode.toString());
            innerSb.append("\r\n");
            sb.append(Utils.formatStringToLeadingWhiteSpace(innerSb.toString()));
        }
        return sb.toString();
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        for(ICode icode : intermediateCode){
            resultList.addAll(icode.genFlatCode());
        }
        return resultList;
    }

    @Override
    public boolean containsPlace(String place) {
        for(ICode variables: intermediateCode){
            if(variables.containsPlace(place))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        for(ICode variables: intermediateCode){
            if(variables.containsLabel(label)) 
                return true;
        }
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        for(ICode intCode: intermediateCode){
            intCode.replacePlace(from, to);
        }
    }

    @Override
    public void replaceLabel(String from, String to) {
        for(ICode intCode: intermediateCode){
            intCode.replaceLabel(from, to);
        }
    }

    @Override
    public boolean containsParamater(String place) {
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
    public boolean containsArgument(String place) {
        for(ICode instruction: intermediateCode){
            if(instruction.containsArgument(place)){
                return true;
            }
        }
        return false;
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
    public boolean containsReturn(String place) {
        for(ICode instr: intermediateCode){
            if(instr.containsReturn(place))
                return true;
        }
        return false;
    }
}
