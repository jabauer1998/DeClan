package io.github.H20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DagVariableNode implements DagNode {
    private HashSet<String> identifiers;
    private DagNode child;
    private List<DagNode> ancestors;

    public DagVariableNode(String ident, DagNode child){
        this.identifiers = new HashSet<>();
        this.ancestors = new ArrayList<>();
        child.addAncestor(this);
        this.identifiers.add(ident);
        this.child = child;
    }

    @Override
    public boolean containsId(String ident) {
        return this.identifiers.contains(ident);
    }
    @Override
    public boolean equals(DagNode dagNode) {
        if(dagNode instanceof DagVariableNode){
            DagVariableNode varNode = (DagVariableNode)dagNode;
            return this.child.hashCode() == varNode.child.hashCode();
        } else {
            return false;
        }
    }
    
    @Override
    public void addIdentifier(String ident) {
        this.identifiers.add(ident);
    }

    @Override
    public void addAncestor(DagNode ancestor) {
        this.ancestors.add(ancestor);
    }

    @Override
    public void deleteAncestor(DagNode ancestor) {
        for(int i = 0; i < ancestors.size(); i++){
            DagNode locAncestor = ancestors.get(i);
            if(locAncestor.hashCode() == ancestor.hashCode()){
                ancestors.remove(i);
            }
        }
    }

    @Override
    public boolean isRoot() {
        return this.ancestors.size() == 0;
    }

    @Override
    public List<DagNode> getChildren() {
        LinkedList<DagNode> list = new LinkedList<DagNode>();
        list.add(child);
        return list;
    }
}
