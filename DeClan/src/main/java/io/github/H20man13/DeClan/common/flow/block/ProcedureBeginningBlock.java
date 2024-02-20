package io.github.H20man13.DeClan.common.flow.block;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;

public class ProcedureBeginningBlock extends BasicBlock {
    private ProcLabel label;
    private List<ParamAssign> paramaterAssignmants;
    
    public ProcedureBeginningBlock(ProcLabel label, List<ParamAssign> assignments, List<ICode> initialICode){
        super(initialICode);
        this.paramaterAssignmants = assignments;
        this.label = label;
    }

    public List<ICode> getAllICode(){
        LinkedList<ICode> icode = new LinkedList<ICode>();
        icode.add(label);
        icode.addAll(paramaterAssignmants);
        icode.addAll(super.getAllICode());
        return icode;
    }

    public List<ParamAssign> getParamaterAssignmants(){
        return paramaterAssignmants;
    }

    public ProcLabel getLabel(){
        return label;
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(label.toString());
        sb.append("\n");
        for(ParamAssign assign: paramaterAssignmants){
            sb.append(assign.toString());
            sb.append("\n");
        }
        sb.append(super.toString());
        return sb.toString();
    }
}
