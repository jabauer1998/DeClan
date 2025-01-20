package io.github.H20man13.DeClan.common.dag;

import java.util.LinkedList;

import io.github.H20man13.DeClan.common.dag.DagNode.ScopeType;
import io.github.H20man13.DeClan.common.dag.DagNode.ValueType;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class DagNodeFactory {
    public DagNodeFactory(){}

    public DagNode createRealAdditionNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.RADD, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerAdditionNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IADD, childNodes, ValueType.INT);
    }

    public DagNode createRealSubtractionNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.RSUB, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerSubtractionNode(boolean isDefinition, ScopeType type, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(isDefinition, type, nodeName, DagOperationNode.Op.ISUB, childNodes, ValueType.INT);
    }

    public DagNode createRealMultiplicationNode(boolean isDefinition, ScopeType type, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(isDefinition, type, nodeName, DagOperationNode.Op.RMUL, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerMultiplicationNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode left, DagNode right){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(left);
        childNodes.add(right);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IMUL, childNodes, ValueType.INT);
    }

    public DagNode createRealDivisionNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.RDIVIDE, childNodes, ValueType.REAL);
    }

    public DagNode createIntegerDivNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IDIV, childNodes, ValueType.INT);
    }

    public DagNode createIntegerModuleNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IMOD, childNodes, ValueType.INT);
    }

    public DagNode createLogicalAndNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.LAND, childNodes, ValueType.BOOL);
    }

    public DagNode createBitwiseAndNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IAND, childNodes, ValueType.INT);
    }

    public DagNode createBitwiseXorNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IXOR, childNodes, ValueType.INT);
    }

    public DagNode createLogicalOrNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.LOR, childNodes, ValueType.BOOL);
    }

    public DagNode createBitwiseOrNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IOR, childNodes, ValueType.INT);
    }

    public DagNode createLeftShiftNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.ILSHIFT, childNodes, ValueType.INT);
    }

     public DagNode createRightShiftNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IRSHIFT, childNodes, ValueType.INT);
    }

    public DagNode createLessThanOrEqualNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.LE, childNodes, ValueType.BOOL);
    }

    public DagNode createLessThanNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.LT, childNodes, ValueType.BOOL);
    }

    public DagNode createGreaterThanOrEqualNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.GE, childNodes, ValueType.BOOL);
    }

    public DagNode createGreaterThanNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.GT, childNodes, ValueType.BOOL);
    }

    public DagNode createBooleanEqualsNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.BEQ, childNodes, ValueType.BOOL);
    }

    public DagNode createBooleanNotEqualsNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.BNE, childNodes, ValueType.BOOL);
    }
    
    public DagNode createIntegerEqualsNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.IEQ, childNodes, ValueType.BOOL);
    }

    public DagNode createIntegerNotEqualsNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1, DagNode node2){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        childNodes.add(node2);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.INE, childNodes, ValueType.BOOL);
    }

    public DagNode createNotNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.BNOT, childNodes, ValueType.BOOL);
    }

    public DagNode createBitwiseNotNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.INOT, childNodes, ValueType.INT);
    }

    public DagNode createIntegerNegationNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.INEG, childNodes, ValueType.INT);
    }

    public DagNode createRealNegationNode(boolean isDefinition, ScopeType scope, String nodeName, DagNode node1){
        LinkedList<DagNode> childNodes = new LinkedList<>();
        childNodes.add(node1);
        return new DagOperationNode(isDefinition, scope, nodeName, DagOperationNode.Op.RNEG, childNodes, ValueType.REAL);
    }

    public DagNode createNullNode(String nodeName){
        return new DagNullNode(nodeName);
    }

    public DagNode createBooleanNode(boolean isDefinition, Assign.Scope origScope, String nodeName, boolean value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(isDefinition, scope, nodeName, value, ValueType.BOOL);
    }

    public DagNode createIntNode(boolean isDefinition, Assign.Scope origScope, String nodeName, int value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(isDefinition, scope, nodeName, value, ValueType.INT);
    }

    public DagNode createRealNode(boolean isDefinition, Assign.Scope origScope, String nodeName, double value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(isDefinition, scope, nodeName, value, ValueType.REAL);
    }

    public DagNode createStringNode(boolean isDefinition, Assign.Scope origScope, String nodeName, String value){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        return new DagValueNode(isDefinition, scope, nodeName, value, ValueType.STRING);
    }

    public DagNode createDefaultVariableNode(boolean isDefinition, String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(isDefinition, ScopeType.LOCAL, nodeName, child, type);
    }

    public DagNode createParamVariableNode(boolean isDefinition, String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(isDefinition, ScopeType.PARAM, nodeName, child, type);
    }

    public DagNode createInternalReturnVariableNode(boolean isDefinition, String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(isDefinition, ScopeType.RETURN, nodeName, child, type);
    }

    public DagNode createExternalReturnVariableNode(boolean isDefinition, String nodeName, DagNode child, Assign.Type origType){
        ValueType type = ConversionUtils.assignTypeToDagValueType(origType);
        return new DagVariableNode(isDefinition, ScopeType.RETURN, nodeName, child, type);
    }

    public DagNode createInlineAssemblyNode(String nodeName, DagNode... children){
        LinkedList<DagNode> childs = new LinkedList<DagNode>();
        for(DagNode child : children){
            childs.add(child);
        }
        return new DagInlineAssemblyNode(nodeName, childs);
    }
}
