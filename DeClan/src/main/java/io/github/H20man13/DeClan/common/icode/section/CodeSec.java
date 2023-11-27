package io.github.H20man13.DeClan.common.icode.section;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.End;
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
}
