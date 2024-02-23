package io.github.H20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DagNullNode implements DagNode {
    private List<String> identifiers;
    private List<DagNode> ancestors;

    public DagNullNode(String ident){
        this.identifiers = new LinkedList<String>();
        this.identifiers.add(ident);
        this.ancestors = new ArrayList<>();
    }

    @Override
    public boolean containsId(String ident) {
        return this.identifiers.contains(ident);
    }

    @Override
    public boolean equals(Object dagNode) {
        if(dagNode instanceof DagNullNode){
            return true;
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
        return ancestors.size() == 0;
    }

    @Override
    public List<DagNode> getChildren() {
        return new LinkedList<>();
    }

    @Override
    public List<String> getIdentifiers() {
        return identifiers;
    }

    @Override
    public ScopeType getScopeType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getScopeType'");
    }

    @Override
    public ValueType getValueType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getValueType'");
    }
}
