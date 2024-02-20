package io.github.H20man13.DeClan.common.flow;

import java.util.List;
import java.util.Map;

import io.github.H20man13.DeClan.common.flow.block.ProcedureBeginningBlock;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.LiveInfo;

public class ProcedureEntryNode extends BlockNode{
    private ProcedureBeginningBlock block;

    public ProcedureEntryNode(ProcedureBeginningBlock block) {
        super(block);
        this.block = block;
    }

    public ProcedureBeginningBlock getBlock(){
        return block;
    }

    @Override
    public List<ICode> getAllICode(){
        return block.getAllICode();
    }

    public String toString(){
        return block.toString();
    }
}
