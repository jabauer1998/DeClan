package io.github.H20man13.DeClan.common.flow;

public class ExitNode implements FlowGraphNode{
    private FlowGraphNode exit;

    public ExitNode(BlockNode exit){
        this.exit = exit;
        exit.addSuccessor(this);
    }
}
