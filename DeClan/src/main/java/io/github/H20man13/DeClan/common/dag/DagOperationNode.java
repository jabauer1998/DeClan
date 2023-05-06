package io.github.H20man13.DeClan.common.dag;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DagOperationNode implements DagNode {
    public enum Op{
        ADD, SUB, DIV, MOD, BAND, 
        BOR, NEG, BNOT, GE, LE, 
        LT, GT, NE, EQ, MUL
    }

    private List<DagNode> children;
    private HashSet<String> identifiers;
    private boolean nodeIsKilled;
    private Op operation;

    
    public DagOperationNode(String identifier, Op operation, List<DagNode> children){
        this.children = new LinkedList<DagNode>();
        this.children.addAll(children);
        this.identifiers = new HashSet<String>();
        this.identifiers.add(identifier);
        this.nodeIsKilled = false;
        this.operation = operation;
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
    public boolean equals(DagNode dagNode) {
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
}
