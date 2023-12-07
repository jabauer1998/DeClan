package io.github.H20man13.DeClan.common.icode.section;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

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
            sb.append(' ');
            sb.append(icode.toString());
            sb.append("\r\n");
        }
        sb.append("End\r\n");
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
        return resultList;
    }
}
