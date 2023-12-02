package io.github.H20man13.DeClan.common.flow.block;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Return;

public class ProcedureEndingBlock extends BasicBlock {
    private Return ret;
    
    public ProcedureEndingBlock(List<ICode> codeInBlock, Return ret) {
        super(codeInBlock);
        this.ret = ret;
    }
}
