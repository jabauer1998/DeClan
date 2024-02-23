package io.github.H20man13.DeClan.common.dag;

import java.util.LinkedList;
import java.util.List;

public class DagInlineAssemblyNode implements DagNode{
    private String operation;
    private List<DagNode> children;

    public DagInlineAssemblyNode(String operation, List<DagNode> children){
        this.operation = operation;
        this.children = children;
    }

    @Override
    public boolean containsId(String ident) {
        return ident == operation;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getScopeType'");
    }

    @Override
    public ValueType getValueType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getValueType'");
    }
}
