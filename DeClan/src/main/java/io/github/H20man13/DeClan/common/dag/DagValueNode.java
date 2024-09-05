package io.github.H20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class DagValueNode implements DagNode{

    private List<String> identifiers;
    private List<DagNode> ancestors;
    private Object value;
    private ValueType type;
    private ScopeType scope;
    private boolean isDefinition;

    public DagValueNode(boolean isDefinition, ScopeType scope, String identifier, Object value, ValueType type){
        this.identifiers = new LinkedList<String>();
        this.identifiers.add(identifier);
        this.value = value;
        this.type = type;
        this.ancestors = new ArrayList<DagNode>();
        this.scope = scope;
        this.type = type;
        this.isDefinition = isDefinition;
    }

    @Override
    public boolean containsId(IdentExp ident) {
        return this.identifiers.contains(ident.ident) && scope == ConversionUtils.assignScopeToDagScopeType(ident.scope);
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
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(identifiers.toString());
    	sb.append("\n|\n|\nV\n");
    	sb.append(value.toString());
    	return sb.toString();
    }

    @Override
    public ScopeType getScopeType() {
        return this.scope;
    }

    @Override
    public ValueType getValueType() {
        return this.type;
    }
    
    public boolean isDef() {
    	return isDefinition;
    }
}
