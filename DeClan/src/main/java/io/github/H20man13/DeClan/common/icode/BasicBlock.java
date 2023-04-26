package io.github.H20man13.DeClan.common.icode;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;

public class BasicBlock implements ICode{
    private List<ICode> codeInBlock;
    
    public BasicBlock(List<ICode> codeInBlock){
        this.codeInBlock = codeInBlock;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(ICode icode : codeInBlock){
            sb.append(icode.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
}



