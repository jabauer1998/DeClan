package io.github.H20man13.DeClan.common.flow;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.StyledEditorKit.BoldAction;

import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.dag.DagGraph;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.icode.BasicBlock;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.LiveInfo;

public class BlockNode implements FlowGraphNode {
    private BasicBlock block;
    private List<FlowGraphNode> successors;
    private List<FlowGraphNode> predecessors;
    private Environment<String, LiveInfo> lifeInformation;
    private DagNodeFactory factory;
    private DagGraph dag;

    public BlockNode(BasicBlock block){
        this.block = block;
        this.successors = new LinkedList<FlowGraphNode>();
        this.predecessors = new LinkedList<FlowGraphNode>();
        this.lifeInformation = new Environment<String, LiveInfo>();
        this.dag = new DagGraph();
        this.factory = new DagNodeFactory();
        this.updateLivelinessInformation();
        this.buildDag();
    }

    private void updateLivelinessInformation(){
        List<ICode> icode = this.block.getIcode();
        for(int i = icode.size() - 1; i >= 0; i--){
            ICode threeAddressInstr = icode.get(i);
            if(threeAddressInstr instanceof LetBin){
                LetBin binInstr = (LetBin)threeAddressInstr;
                String place = binInstr.place;

                if(!this.lifeInformation.entryExists(place)){
                    // -1 means no next use
                    this.lifeInformation.addEntry(place, new LiveInfo(false, -1));
                }

                String left = binInstr.left;
                this.lifeInformation.addEntry(left, new LiveInfo(true, i));

                String right = binInstr.right;
                this.lifeInformation.addEntry(right, new LiveInfo(true, i));
            } else if(threeAddressInstr instanceof LetUn){
                LetUn unInstruction = (LetUn)threeAddressInstr;
                
                String place = unInstruction.place;
                if(!this.lifeInformation.entryExists(place)){
                    this.lifeInformation.addEntry(place, new LiveInfo(false, -1));
                }

                String right = unInstruction.value;
                this.lifeInformation.addEntry(place, new LiveInfo(true, i));
            } else if(threeAddressInstr instanceof LetBool){
                LetBool boolInstr = (LetBool)threeAddressInstr;
                if(!this.lifeInformation.entryExists(boolInstr.place)){
                    this.lifeInformation.addEntry(boolInstr.place, new LiveInfo(false, -1));
                }
            } else if(threeAddressInstr instanceof LetInt){
                LetInt intInstr = (LetInt)threeAddressInstr;
                if(!this.lifeInformation.entryExists(intInstr.place)){
                    this.lifeInformation.addEntry(intInstr.place, new LiveInfo(false, -1));
                }
            } else if(threeAddressInstr instanceof LetReal){
                LetReal intInstr = (LetReal)threeAddressInstr;
                if(!this.lifeInformation.entryExists(intInstr.place)){
                    this.lifeInformation.addEntry(intInstr.place, new LiveInfo(false, -1));
                }
            } else if(threeAddressInstr instanceof LetString){
                LetString strInstr = (LetString)threeAddressInstr;
                if(!this.lifeInformation.entryExists(strInstr.place)){
                    this.lifeInformation.addEntry(strInstr.place, new LiveInfo(false, -1));
                }
            }
        }
    }

    private void buildDag(){
        List<ICode> icodes = this.block.getIcode();
        for(ICode icode : icodes){
            if(icode instanceof LetBin){
                LetBin binICode = (LetBin)icode;
                DagNode left = this.dag.searchForLatestChild(binICode.left);
                DagNode right = this.dag.searchForLatestChild(binICode.right);

                if(left == null){
                    left = factory.createValueNode(binICode.left);
                    this.dag.addDagNode(left);
                }

                if(right == null){
                    right = factory.createValueNode(binICode.right);
                    this.dag.addDagNode(right);
                }


                DagNode newNode = null;
                switch(binICode.op){
                    case ADD: newNode = factory.createAdditionNode(binICode.place, left, right);
                    case SUB: newNode = factory.createSubtractionNode(binICode.place, left, right);
                    case MUL: newNode = factory.createMultiplicationNode(binICode.place, left, right);
                    case DIV: newNode = factory.createDivisionNode(binICode.place, left, right);
                    case BAND: newNode = factory.createAndNode(binICode.place, left, right);
                    case MOD: newNode = factory.createModuleNode(binICode.place, left, right);
                    case BOR: newNode = factory.createOrNode(binICode.place, left, right);
                    case GT: newNode = factory.createGreaterThanNode(binICode.place, left, right);
                    case GE: newNode = factory.createGreaterThanOrEqualNode(binICode.place, left, right);
                    case LT: newNode = factory.createLessThanNode(binICode.place, left, right);
                    case LE: newNode = factory.createLessThanOrEqualNode(binICode.place, left, right);
                    case EQ: newNode = factory.createEqualsNode(binICode.place, left, right);
                    case NE: newNode = factory.createNotEqualsNode(binICode.place, left, right);
                }

                DagNode exists = this.dag.getDagNode(newNode);
                if(exists == null){
                    this.dag.addDagNode(newNode);
                } else if(exists instanceof DagOperationNode){
                    DagOperationNode opExists = (DagOperationNode)exists;
                    opExists.addIdentifier(binICode.place);
                }
            } else if(icode instanceof LetUn){
                LetUn unICode = (LetUn)icode;

                DagNode right = this.dag.searchForLatestChild(unICode.value);

                if(right == null){
                    right = factory.createValueNode(unICode.value);
                    this.dag.addDagNode(right);
                }

                DagNode newNode = null;

                switch(unICode.op){
                    case BNOT: newNode = factory.createNotNode(unICode.place, right);
                    case NEG: newNode = factory.createNegationNode(unICode.place, right);
                }

                DagNode exists = this.dag.getDagNode(newNode);
                if(exists == null){
                    this.dag.addDagNode(newNode);
                } else if(exists instanceof DagOperationNode){
                    DagOperationNode opExists = (DagOperationNode)exists;
                    opExists.addIdentifier(unICode.place);
                }
            } else if(icode instanceof LetVar){
                
            }
        }
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
}
