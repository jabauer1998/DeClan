package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.DagNode;
import io.github.H20man13.DeClan.common.flow.EntryNode;
import io.github.H20man13.DeClan.common.flow.ExitNode;
import io.github.H20man13.DeClan.common.icode.BasicBlock;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;

public class MyFlowGraphBuilder {
    private List<ICode> intermediateCode;
    
    public MyFlowGraphBuilder(List<ICode> intermediateCode){
        this.intermediateCode = intermediateCode;
    }

    public EntryNode buildFlowGraph(){
        if(intermediateCode.size() > 0){
            HashMap<String, BlockNode> labeledNodes = new HashMap<String, BlockNode>();
            List<BlockNode> dagNodes = new LinkedList<BlockNode>();

            for(int i = 0; i < intermediateCode.size(); i++){
                ICode icodeAtIndex = intermediateCode.get(i);
                if(icodeAtIndex instanceof BasicBlock){
                    BasicBlock blockAtIndex = (BasicBlock)icodeAtIndex;
                    BlockNode blockNode = new BlockNode(blockAtIndex);
                    
                    if(MyAnalysis.beginningOfBlockIsLabel(blockAtIndex)){
                        Label firstLabel = (Label)blockAtIndex.getIcode().get(0);
                        String labelName = firstLabel.label;
                        labeledNodes.put(labelName, blockNode);
                    }

                    dagNodes.add(blockNode);
                }
                else {
                    return null;  
                }
            }

            for(int i = 0; i < dagNodes.size(); i++){
                BlockNode node = dagNodes.get(i);
                BasicBlock block = node.getBlock();
                if(MyAnalysis.endOfBlockIsJump(block)){
                    ICode lastCode = block.getIcode().get(block.getIcode().size() - 1);
                    if(lastCode instanceof If){
                        If lastIf = (If)lastCode;
                        BlockNode trueNode = labeledNodes.get(lastIf.ifTrue);
                        node.addSuccessor(trueNode);
                        trueNode.addPredecessor(node);
                        BlockNode falseNode = labeledNodes.get(lastIf.ifFalse);
                        node.addSuccessor(falseNode);
                        falseNode.addPredecessor(node);
                    } else if(lastCode instanceof Goto){
                        Goto lastGoto = (Goto)lastCode;
                        BlockNode labeledNode = labeledNodes.get(lastGoto.label);
                        node.addSuccessor(labeledNode);
                        labeledNode.addPredecessor(node);
                    } else {
                        return null;
                    }
                } else if(i + 1 < dagNodes.size()){
                    BlockNode nextNode = dagNodes.get(i + 1);
                    node.addSuccessor(nextNode);
                    nextNode.addPredecessor(node);
                }
            }

            EntryNode entry = new EntryNode(dagNodes.get(0));

            ExitNode exit = new ExitNode(dagNodes.get(dagNodes.size() - 1));

            return entry;
        } else {
            return null;
        }
    }
}
