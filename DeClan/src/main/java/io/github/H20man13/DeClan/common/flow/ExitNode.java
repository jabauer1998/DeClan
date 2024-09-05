package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.ICode;

public class ExitNode implements FlowGraphNode{
    public FlowGraphNode exit;

    public ExitNode(BlockNode exit){
        this.exit = exit;
        exit.addSuccessor(this);
    }

    @Override
    public List<ICode> getICode() {
        return new LinkedList<ICode>();
    }

    @Override
    public List<ICode> getAllICode(){
        return new LinkedList<ICode>();
    }

    @Override
    public String toString(){
        return "EXIT";
    }
}
