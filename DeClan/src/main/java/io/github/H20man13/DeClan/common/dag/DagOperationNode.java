package io.github.H20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DagOperationNode implements DagNode {
    public enum Op{
        IADD, ISUB, IDIV, IMOD, LAND, 
        LOR, INEG, RNEG, BNOT, GE, LE, 
        LT, GT, NE, EQ, IMUL, RADD, RSUB,
        RMUL, RDIVIDE, IAND, IOR, ILSHIFT, 
        IRSHIFT, INOT, IXOR
    }

    private List<DagNode> children;
    private List<DagNode> ancestors;
    private List<String> identifiers;
    private boolean nodeIsKilled;
    private Op operation;

    
    public DagOperationNode(String identifier, Op operation, List<DagNode> children){
        this.children = new LinkedList<DagNode>();
        this.children.addAll(children);
        for(DagNode child : children){
            child.addAncestor(this);
        }
        this.identifiers = new LinkedList<String>();
        this.identifiers.add(identifier);
        this.nodeIsKilled = false;
        this.operation = operation;
        this.ancestors = new ArrayList<>();
    }

    public List<String> getIdentifiers(){
        return identifiers;
    }

    public void addIdentifier(String identifier){
        this.identifiers.add(identifier);
    }

    public void killDagNode(){
        this.nodeIsKilled = true;
    }

    public boolean isNodeKilled(){
        return this.nodeIsKilled;
    }

    @Override
    public boolean containsId(String ident) {
        return this.identifiers.contains(ident);
    }

    @Override
    public boolean equals(Object dagNode) {
        if(dagNode instanceof DagOperationNode){
            DagOperationNode opNode = (DagOperationNode)dagNode;
            boolean operationsAreEqual = opNode.operation == this.operation;
            boolean childrenAreEqual = compareChildren(opNode);
            return operationsAreEqual && childrenAreEqual;
        } else {
            return false;
        }
    }

    private boolean compareChildren(DagOperationNode opNode){
        if(this.children.size() == opNode.children.size()){
            for(int i = 0; i < this.children.size(); i++){
                DagNode node1 = this.children.get(i);
                DagNode node2 = opNode.children.get(i);
                if(node1.hashCode() != node2.hashCode()){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
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
        return this.children;
    }

    public Op getOperator(){
        return this.operation;
    }
}
