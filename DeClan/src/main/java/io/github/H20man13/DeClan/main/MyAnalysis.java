package io.github.H20man13.DeClan.main;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.BasicBlock;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;

public class MyAnalysis {
    public static boolean beginningOfBlockIsLabel(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode firstICode = codeInBlock.get(0);
            if(firstICode instanceof Label){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean endOfBlockIsJump(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode lastICode = codeInBlock.get(codeInBlock.size() - 1);
            if(lastICode instanceof If){
                return true;
            } else if(lastICode instanceof Goto){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
