package io.github.H20man13.DeClan.common.dag;

import java.util.LinkedList;

public class DagNodeFactory {
    public DagNodeFactory(){}

    public DagNode createRealAdditionNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.RADD, childNodes);
    }

    public DagNode createIntegerAdditionNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IADD, childNodes);
    }

    public DagNode createRealSubtractionNode(String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(nodeName, DagOperationNode.Op.RSUB, childNodes);
    }

    public DagNode createIntegerSubtractionNode(String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(nodeName, DagOperationNode.Op.ISUB, childNodes);
    }

    public DagNode createRealMultiplicationNode(String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(nodeName, DagOperationNode.Op.RMUL, childNodes);
    }

    public DagNode createIntegerMultiplicationNode(String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IMUL, childNodes);
    }

    public DagNode createRealDivisionNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.RDIVIDE, childNodes);
    }

    public DagNode createRealDivNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.RDIV, childNodes);
    }

    public DagNode createIntegerDivisionNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IDIVIDE, childNodes);
    }

    public DagNode createIntegerDivNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IDIV, childNodes);
    }

    public DagNode createIntegerModuleNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IMOD, childNodes);
    }

    public DagNode createLogicalAndNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.LAND, childNodes);
    }

    public DagNode createBitwiseAndNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IAND, childNodes);
    }

    public DagNode createLogicalOrNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.LOR, childNodes);
    }

    public DagNode createBitwiseOrNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IOR, childNodes);
    }

    public DagNode createLeftShiftNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.ILSHIFT, childNodes);
    }

     public DagNode createRightShiftNode(String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(nodeName, DagOperationNode.Op.IRSHIFT, childNodes);
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

    public DagNode createIntegerNegationNode(String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(nodeName, DagOperationNode.Op.INEG, childNodes);
    }

    public DagNode createRealNegationNode(String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(nodeName, DagOperationNode.Op.RNEG, childNodes);
    }

    public DagNode createNullNode(String nodeName){
        return new DagNullNode(nodeName);
    }

    public DagNode createBooleanNode(String nodeName, boolean value){
        return new DagValueNode(nodeName, DagValueNode.ValueType.BOOL, value);
    }

    public DagNode createIntNode(String nodeName, int value){
        return new DagValueNode(nodeName, DagValueNode.ValueType.INT, value);
    }

    public DagNode createRealNode(String nodeName, double value){
        return new DagValueNode(nodeName, DagValueNode.ValueType.REAL, value);
    }

    public DagNode createStringNode(String nodeName, String value){
        return new DagValueNode(nodeName, DagValueNode.ValueType.STRING, value);
    }

    public DagNode createVariableNode(String nodeName, DagNode child){
        return new DagVariableNode(nodeName, child);
    }

    public DagNode createInlineAssemblyNode(String nodeName, DagNode... children){
        LinkedList<DagNode> childs = new LinkedList<DagNode>();
        for(DagNode child : children){
            childs.add(child);
        }
        return new DagInlineAssemblyNode(nodeName, childs);
    }
}
