package io.github.H20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DagValueNode implements DagNode{
    public enum ValueType{
        INT, REAL, BOOL, STRING
    }

    private List<String> identifiers;
    private List<DagNode> ancestors;
    private Object value;
    private ValueType type;

    public DagValueNode(String identifier, ValueType type, Object value){
        this.identifiers = new LinkedList<String>();
        this.identifiers.add(identifier);
        this.value = value;
        this.type = type;
        this.ancestors = new ArrayList<DagNode>();
    }

    @Override
    public boolean containsId(String ident) {
        return this.identifiers.contains(ident);
    }

    @Override
    public boolean equals(Object dagNode){
        if(dagNode instanceof DagValueNode){
            DagValueNode valDagNode = (DagValueNode)dagNode;
            return valDagNode.type == type && this.value.hashCode() == valDagNode.hashCode();
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
        return new LinkedList<>();
    }

    @Override
    public List<String> getIdentifiers() {
        return identifiers;
    }

    public ValueType getType(){
        return type;
    }

    public Object getValue(){
        return value;
    }
}
