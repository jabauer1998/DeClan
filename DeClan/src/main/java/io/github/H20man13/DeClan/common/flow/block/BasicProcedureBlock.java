package io.github.H20man13.DeClan.common.flow.block;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;

public class BasicProcedureBlock extends BasicBlock {
    private ProcLabel label;
    private List<ParamAssign> paramaterAssignmants;
    
    public BasicProcedureBlock(ProcLabel label, List<ParamAssign> assignments, List<ICode> initialICode){
        super(initialICode);
        this.paramaterAssignmants = assignments;
        this.label = label;
    }

    public List<ParamAssign> getParamaterAssignmants(){
        return paramaterAssignmants;
    }

    public ProcLabel getLabel(){
        return label;
    }
}
