package io.github.h20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.icode.exp.IdentExp;
import io.github.h20man13.DeClan.common.util.ConversionUtils;

public class DagVariableNode implements DagNode {
    private List<String> identifiers;
    private DagNode child;
    private List<DagNode> ancestors;
    private ValueType varType;
    private ScopeType scope;
    private boolean isDefinition;

    public DagVariableNode(boolean isDefinition, ScopeType scope, String ident, DagNode child, ValueType type){
        this.identifiers = new LinkedList<>();
        this.ancestors = new ArrayList<>();
        child.addAncestor(this);
        this.identifiers.add(ident);
        this.child = child;
        this.varType = type;
        this.scope = scope;
        this.isDefinition = isDefinition;
     }
    
    public boolean isDef() {
    	return isDefinition;
    }

    public DagNode getChild(){
        return child;
    }

    @Override
    public boolean containsId(IdentExp ident) {
        return this.identifiers.contains(ident.ident) && scope == ConversionUtils.assignScopeToDagScopeType(ident.scope);
    }

    @Override
    public boolean equals(Object dagNode) {
        if(dagNode instanceof DagVariableNode){
            DagVariableNode varNode = (DagVariableNode)dagNode;
            boolean typesEqual = this.varType == varNode.varType;
            boolean childEqual = this.child.hashCode() == varNode.child.hashCode();
            boolean scopesEqual = this.scope == varNode.scope;
            return typesEqual && scopesEqual && childEqual;
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

    @Override
    public ScopeType getScopeType() {
        return this.scope;
    }

    @Override
    public ValueType getValueType() {
        return this.varType;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(identifiers.toString());
    	sb.append("\n|\n|\nV\n");
    	sb.append(child.getIdentifiers().toString());
    	return sb.toString();
    }
}
