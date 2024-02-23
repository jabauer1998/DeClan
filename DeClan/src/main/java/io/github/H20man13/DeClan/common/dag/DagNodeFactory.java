package io.github.H20man13.DeClan.common.dag;

import java.util.LinkedList;

import io.github.H20man13.DeClan.common.dag.DagNode.ScopeType;
import io.github.H20man13.DeClan.common.dag.DagNode.ValueType;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class DagNodeFactory {
    public DagNodeFactory(){}

    public DagNode createRealAdditionNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.RADD, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerAdditionNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IADD, childNodes, ValueType.INT);
    }

    public DagNode createRealSubtractionNode(ScopeType scope, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.RSUB, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerSubtractionNode(ScopeType type, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(type, nodeName, DagOperationNode.Op.ISUB, childNodes, ValueType.INT);
    }

    public DagNode createRealMultiplicationNode(ScopeType type, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(type, nodeName, DagOperationNode.Op.RMUL, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerMultiplicationNode(ScopeType scope, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IMUL, childNodes, ValueType.INT);
    }

    public DagNode createRealDivisionNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.RDIVIDE, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerDivNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IDIV, childNodes, ValueType.INT);
    }

    public DagNode createIntegerModuleNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IMOD, childNodes, ValueType.INT);
    }

    public DagNode createLogicalAndNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.LAND, childNodes, ValueType.BOOL);
    }

    public DagNode createBitwiseAndNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IAND, childNodes, ValueType.INT);
    }

    public DagNode createBitwiseXorNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IXOR, childNodes, ValueType.INT);
    }

    public DagNode createLogicalOrNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.LOR, childNodes, ValueType.BOOL);
    }

    public DagNode createBitwiseOrNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IOR, childNodes, ValueType.INT);
    }

    public DagNode createLeftShiftNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.ILSHIFT, childNodes, ValueType.INT);
    }

     public DagNode createRightShiftNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.IRSHIFT, childNodes, ValueType.INT);
    }

    public DagNode createLessThanOrEqualNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.LE, childNodes, ValueType.BOOL);
    }

    public DagNode createLessThanNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.LT, childNodes, ValueType.BOOL);
    }

    public DagNode createGreaterThanOrEqualNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.GE, childNodes, ValueType.BOOL);
    }

    public DagNode createGreaterThanNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.GT, childNodes, ValueType.BOOL);
    }

    public DagNode createEqualsNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.EQ, childNodes, ValueType.BOOL);
    }

    public DagNode createNotEqualsNode(ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.NE, childNodes, ValueType.BOOL);
    }

    public DagNode createNotNode(ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.BNOT, childNodes, ValueType.BOOL);
    }

    public DagNode createBitwiseNotNode(ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.INOT, childNodes, ValueType.INT);
    }

    public DagNode createIntegerNegationNode(ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.INEG, childNodes, ValueType.INT);
    }

    public DagNode createRealNegationNode(ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(scope, nodeName, DagOperationNode.Op.RNEG, childNodes, ValueType.REAL);
    }

    public DagNode createNullNode(String nodeName){
        return new DagNullNode(nodeName);
    }

    public DagNode createBooleanNode(Assign.Scope origScope, String nodeName, boolean value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(scope, nodeName, value, ValueType.BOOL);
    }

    public DagNode createIntNode(Assign.Scope origScope, String nodeName, int value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(scope, nodeName, value, ValueType.INT);
    }

    public DagNode createRealNode(Assign.Scope origScope, String nodeName, double value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(scope, nodeName, value, ValueType.REAL);
    }

    public DagNode createStringNode(Assign.Scope origScope, String nodeName, String value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(scope, nodeName, value, ValueType.STRING);
    }

    public DagNode createDefaultVariableNode(String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(ScopeType.LOCAL, nodeName, child, type);
    }

    public DagNode createParamVariableNode(String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(ScopeType.PARAM, nodeName, child, type);
    }

    public DagNode createInternalReturnVariableNode(String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(ScopeType.INTERNAL_RETURN, nodeName, child, type);
    }

    public DagNode createExternalReturnVariableNode(String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(ScopeType.EXTERNAL_RETURN, nodeName, child, type);
    }

    public DagNode createInlineAssemblyNode(String nodeName, DagNode... children){
        LinkedList<DagNode> childs = new LinkedList<DagNode>();
        for(DagNode child : children){
            childs.add(child);
        }
        return new DagInlineAssemblyNode(nodeName, childs);
    }
}
