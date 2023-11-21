package io.github.H20man13.DeClan.common.icode.section;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public class CodeSec implements ICode {
    public List<ICode> intermediateCode;

    public CodeSec(List<ICode> code){
        this.intermediateCode = code;    
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
        for(ICode icode : intermediateCode){
            sb.append(icode.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
