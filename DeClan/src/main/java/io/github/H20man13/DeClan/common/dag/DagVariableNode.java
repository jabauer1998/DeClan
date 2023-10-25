package io.github.H20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DagVariableNode implements DagNode {
    private List<String> identifiers;
    private DagNode child;
    private List<DagNode> ancestors;
    private VariableType varType;

    public enum VariableType {
        DEFAULT,
        PARAM,
        INTERNAL_RET,
        EXTERNAL_RET
    }

    public DagVariableNode(String ident, VariableType type, DagNode child){
        this.identifiers = new LinkedList<>();
        this.ancestors = new ArrayList<>();
        child.addAncestor(this);
        this.identifiers.add(ident);
        this.child = child;
        this.varType = type;
    }

    public DagNode getChild(){
        return child;
    }

    @Override
    public boolean containsId(String ident) {
        return this.identifiers.contains(ident);
    }
    @Override
    public boolean equals(Object dagNode) {
        if(dagNode instanceof DagVariableNode){
            DagVariableNode varNode = (DagVariableNode)dagNode;
            boolean typesEqual = this.varType == varNode.varType;
            boolean childEqual = this.child.hashCode() == varNode.child.hashCode();
            return typesEqual && childEqual;
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

    @Override
    public List<String> getIdentifiers() {
        return identifiers;
    }

    public VariableType getType(){
        return this.varType;
    }
}
