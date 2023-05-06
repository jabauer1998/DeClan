package io.github.H20man13.DeClan.common.dag;

import java.util.LinkedList;

public class DagNodeFactory {
    public DagNodeFactory(){}

    public DagNode createAdditionNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.ADD, childNodes);
    }

    public DagNode createSubtractionNode(String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(nodeName, DagOperationNode.Op.SUB, childNodes);
    }

    public DagNode createMultiplicationNode(String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(nodeName, DagOperationNode.Op.MUL, childNodes);
    }

    public DagNode createDivisionNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.DIV, childNodes);
    }

    public DagNode createModuleNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.MOD, childNodes);
    }

    public DagNode createAndNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.BAND, childNodes);
    }

    public DagNode createOrNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.BOR, childNodes);
    }

    public DagNode createLessThanOrEqualNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.LE, childNodes);
    }

    public DagNode createLessThanNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.LT, childNodes);
    }

    public DagNode createGreaterThanOrEqualNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.GE, childNodes);
    }

    public DagNode createGreaterThanNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.GT, childNodes);
    }

    public DagNode createEqualsNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.EQ, childNodes);
    }

    public DagNode createNotEqualsNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.NE, childNodes);
    }

    public DagNode createNotNode(String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(nodeName, DagOperationNode.Op.BNOT, childNodes);
    }

    public DagNode createNegationNode(String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(nodeName, DagOperationNode.Op.NEG, childNodes);
    }
    

    public DagNode createValueNode(String nodeName){
        return new DagValueNode(nodeName);
    }
}
