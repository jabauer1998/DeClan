package io.github.H20man13.DeClan.common.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.dag.DagGraph;
import io.github.H20man13.DeClan.common.dag.DagInlineAssemblyNode;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.dag.DagValueNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode.VariableType;
import io.github.H20man13.DeClan.common.flow.block.BasicBlock;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.InternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.LiveInfo;
import io.github.H20man13.DeClan.common.util.Utils;

public class BlockNode implements FlowGraphNode {
    protected BasicBlock block;
    protected List<FlowGraphNode> successors;
    protected List<FlowGraphNode> predecessors;

    public BlockNode(BasicBlock block){
        this.block = block;
        this.successors = new ArrayList<FlowGraphNode>();
        this.predecessors = new ArrayList<FlowGraphNode>();
    }

    public BasicBlock getBlock(){
        return block;
    }

    public void addSuccessor(FlowGraphNode successor){
        this.successors.add(successor);
    }

    public void addPredecessor(FlowGraphNode predecessor){
        this.predecessors.add(predecessor);
    }

    public void removePredecessor(FlowGraphNode node){
        for(int i = 0; i < this.predecessors.size(); i++){
            if(node.hashCode() == predecessors.get(i).hashCode()){
                this.predecessors.remove(i);
                break;
            }
        }
    }

    public void removeSuccessor(FlowGraphNode node){
        for(int i = 0; i < this.successors.size(); i++){
            if(node.hashCode() == successors.get(i).hashCode()){
                this.successors.remove(i);
                break;
            }
        }
    }

    public List<FlowGraphNode> getPredecessors(){
        return this.predecessors;
    }

    public List<FlowGraphNode> getSuccessors(){
        return this.successors;
    }

    @Override
    public String toString(){
        return block.toString();
    }

    @Override
    public List<ICode> getICode() {
        return block.getIcode();
    }

    @Override
    public List<ICode> getAllICode(){
        return block.getIcode();
    }
}
