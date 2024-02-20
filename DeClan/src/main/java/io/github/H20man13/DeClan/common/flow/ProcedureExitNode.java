package io.github.H20man13.DeClan.common.flow;

import java.util.List;
import java.util.Map;

import io.github.H20man13.DeClan.common.flow.block.ProcedureBeginningBlock;
import io.github.H20man13.DeClan.common.flow.block.ProcedureEndingBlock;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.LiveInfo;

public class ProcedureExitNode extends BlockNode{
    private ProcedureEndingBlock block;

    public ProcedureExitNode(ProcedureEndingBlock block) {
        super(block);
        this.block = block;
    }

    public ProcedureEndingBlock getBlock(){
        return block;
    }

    @Override
    public List<ICode> getAllICode(){
        return block.getAllICode();
    }

    @Override 
    public String toString(){
        return block.toString();
    }
}
