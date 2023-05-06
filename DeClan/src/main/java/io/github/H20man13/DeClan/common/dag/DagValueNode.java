package io.github.H20man13.DeClan.common.dag;

public class DagValueNode implements DagNode {
    private String ident;

    public DagValueNode(String ident){
        this.ident = ident;
    }

    @Override
    public boolean containsId(String ident) {
        return this.ident.equals(ident);
    }

    @Override
    public boolean equals(DagNode dagNode) {
        if(dagNode instanceof DagValueNode){
            return dagNode.containsId(ident);
        } else {
            return false;
        }
    }
}
