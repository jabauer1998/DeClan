package io.github.H20man13.DeClan.common.dag;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.exception.DagFormatException;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;

public class DagInlineAssemblyNode implements DagNode{
    private String operation;
    private List<DagNode> children;

    public DagInlineAssemblyNode(String operation, List<DagNode> children){
        this.operation = operation;
        this.children = children;
    }

    @Override
    public boolean containsId(IdentExp ident) {
        throw new DagFormatException(this, "Cant check for Id " + ident + "in node type " + this.getClass().getName());
    }

    @Override
    public void addIdentifier(String ident) {
        this.operation = ident;
    }

    @Override
    public List<String> getIdentifiers() {
        LinkedList<String> ident = new LinkedList<String>();
        ident.add(operation);
        return ident;
    }

    @Override
    public void addAncestor(DagNode ancestor) {
        children.add(ancestor);
    }

    @Override
    public void deleteAncestor(DagNode ancestor) {
        children.remove(ancestor);
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public List<DagNode> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof DagInlineAssemblyNode){
            DagInlineAssemblyNode node = (DagInlineAssemblyNode)other;
            if(node.children.size() == this.children.size()){
                for(int i = 0; i < children.size(); i++){
                    DagNode otherChild = node.children.get(i);
                    DagNode thisChild = this.children.get(i);
                    if(otherChild != thisChild){
                        return false;
                    }
                }
                return this.operation.equals(node.operation);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public ScopeType getScopeType() {
        return null;
    }

    @Override
    public ValueType getValueType() {
        return null;
    }
}
