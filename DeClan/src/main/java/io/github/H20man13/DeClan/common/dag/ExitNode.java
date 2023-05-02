package io.github.H20man13.DeClan.common.dag;

public class ExitNode implements DagNode{
    private DagNode exit;

    public ExitNode(BlockNode exit){
        this.exit = exit;
        exit.addSuccessor(this);
    }
}
