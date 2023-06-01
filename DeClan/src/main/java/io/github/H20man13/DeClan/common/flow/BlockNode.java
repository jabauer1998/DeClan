package io.github.H20man13.DeClan.common.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.LinkException;

import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.BasicBlock;
import io.github.H20man13.DeClan.common.dag.DagGraph;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.dag.DagValueNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.LiveInfo;
import io.github.H20man13.DeClan.common.util.Utils;

public class BlockNode implements FlowGraphNode {
    protected BasicBlock block;
    protected List<FlowGraphNode> successors;
    protected List<FlowGraphNode> predecessors;
    protected Environment<String, LiveInfo> lifeInformation;
    protected DagNodeFactory factory;
    protected DagGraph dag;

    public BlockNode(BasicBlock block){
        this.block = block;
        this.successors = new ArrayList<FlowGraphNode>();
        this.predecessors = new ArrayList<FlowGraphNode>();
        this.lifeInformation = new Environment<String, LiveInfo>();
        this.lifeInformation.addScope();
        this.dag = new DagGraph();
        this.factory = new DagNodeFactory();
        this.updateLivelinessInformation();
        this.buildDag();
    }

    private void buildCodeFromDag(){
        List<ICode> result = new LinkedList<ICode>();
        List<ICode> initialList = this.getICode();

        if(initialList.size() > 0){
            ICode firstElem = initialList.get(0);
            if(firstElem instanceof Label){
                result.add(firstElem);
            }
        }

        for(DagNode node : this.dag.getDagNodes()){

            List<String> isAlive = new LinkedList<String>();
            for(String identifier: node.getIdentifiers()){
                if(this.lifeInformation.entryExists(identifier)){
                    LiveInfo info = this.lifeInformation.getEntry(identifier);
                    if(info.isAlive){
                        isAlive.add(identifier);
                    }
                }
            }

            String identifier = null;
            if(isAlive.size() > 0){
                identifier = isAlive.remove(0);
            } else {
                identifier = node.getIdentifiers().get(0);
            }

            if(node instanceof DagOperationNode){
                DagOperationNode node2 = (DagOperationNode)node;

                if(node2.getChildren().size() == 2){
                    //Its a Binary Operation
                    DagOperationNode.Op op = node2.getOperator();
                    BinExp.Operator op2 = Utils.getBinOp(op);

                    DagNode child1 = node2.getChildren().get(0);
                    DagNode child2 = node2.getChildren().get(1);

                    String identifier1 = Utils.getIdentifier(child1, lifeInformation);
                    String identifier2 = Utils.getIdentifier(child2, lifeInformation);

                    IdentExp exp1 = new IdentExp(identifier1);
                    IdentExp exp2 = new IdentExp(identifier2);

                    BinExp binExp = new BinExp(exp1, op2, exp2);

                    result.add(new Assign(identifier, binExp));
                } else if(node2.getChildren().size() == 1) {
                    //Its a Unary Operation
                    DagOperationNode.Op op = node2.getOperator();
                    UnExp.Operator op2 = Utils.getUnOp(op);

                    DagNode child1 = node2.getChildren().get(0);

                    String identifier1 = Utils.getIdentifier(child1, lifeInformation);

                    IdentExp exp1 = new IdentExp(identifier1);

                    UnExp unExp = new UnExp(op2, exp1);

                    result.add(new Assign(identifier, unExp));
                }
            } else if(node instanceof DagValueNode){
                DagValueNode valNode = (DagValueNode)node;
                Object value = valNode.getValue();
                Exp resultExp = Utils.valueToExp(value);
                result.add(new Assign(identifier, resultExp));
            } else if(node instanceof DagVariableNode){
                DagVariableNode varNode = (DagVariableNode)node;
                DagNode child = varNode.getChild();
                String identifier1 = Utils.getIdentifier(child, lifeInformation);
                IdentExp ident1 = new IdentExp(identifier1);
                result.add(new Assign(identifier, ident1));
            }

            for(String ident : isAlive){
                IdentExp ident1 = new IdentExp(identifier);
                result.add(new Assign(ident, ident1));
            }
        }

        if(initialList.size() > 0){
            int size = initialList.size();
            ICode lastElem = initialList.get(size - 1);
            if(lastElem.isBranch()){
                result.add(lastElem);
            }
        }
        
        block.setICode(result);
    }

    private void updateLivelinessInformation(){
        List<ICode> icode = this.block.getIcode();
        for(int i = icode.size() - 1; i >= 0; i--){
            ICode threeAddressInstr = icode.get(i);
            if(threeAddressInstr instanceof Assign){
                Assign assignInstr = (Assign)threeAddressInstr;
                String place = assignInstr.place;

                if(!this.lifeInformation.entryExists(place)){
                    // -1 means no next use
                    this.lifeInformation.addEntry(place, new LiveInfo(false, -1));
                }

                if(assignInstr.value instanceof BinExp){
                    BinExp binExp = (BinExp)assignInstr.value;
    
                    if(!binExp.left.isConstant()){
                        String left = binExp.left.toString();
                        this.lifeInformation.addEntry(left, new LiveInfo(true, i));
                    }
    
                    if(!binExp.right.isConstant()){
                        String right = binExp.right.toString();
                        this.lifeInformation.addEntry(right, new LiveInfo(true, i));
                    }
                } else if(assignInstr.value instanceof UnExp){
                    UnExp unExp = (UnExp)assignInstr.value;

                    if(!unExp.right.isConstant()){
                        String right = unExp.right.toString();
                        this.lifeInformation.addEntry(place, new LiveInfo(true, i));
                    }
                }
            }
        }
    }

    private void buildDag(){
        List<ICode> icodes = this.block.getIcode();
        for(ICode icode : icodes){
            if(icode instanceof Assign){
                Assign assignICode = (Assign)icode;

                if(assignICode.value instanceof BinExp){
                    BinExp exp = (BinExp)assignICode.value;
                    
                    DagNode left = this.dag.searchForLatestChild(exp.left.toString());
                    DagNode right = this.dag.searchForLatestChild(exp.right.toString());

                    if(left == null){
                        left = factory.createNullNode(exp.left.toString());
                        this.dag.addDagNode(left);
                    }
    
                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        this.dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createBinaryNode(exp.op, assignICode.place, left, right);

                    DagNode exists = this.dag.getDagNode(newNode);
                    if(exists == null){
                        this.dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof UnExp){
                    UnExp exp = (UnExp)assignICode.value;

                    DagNode right = this.dag.searchForLatestChild(exp.right.toString());

                    if(right == null){
                        right = factory.createNullNode(exp.right.toString());
                        this.dag.addDagNode(right);
                    }

                    DagNode newNode = Utils.createUnaryNode(exp.op, assignICode.place, right);

                    DagNode exists = this.dag.getDagNode(newNode);
                    if(exists == null){
                        this.dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof IdentExp){
                    IdentExp exp = (IdentExp)icode;

                    DagNode right = this.dag.searchForLatestChild(exp.ident.toString());

                    if(right == null){
                        right = factory.createNullNode(exp.ident.toString());
                        this.dag.addDagNode(right);
                    }

                    DagNode newNode = factory.createVariableNode(assignICode.place, right);

                    DagNode exists = this.dag.getDagNode(newNode);
                    if(exists == null){
                        this.dag.addDagNode(newNode);
                    } else {
                        exists.addIdentifier(assignICode.place);
                    }
                } else if(assignICode.value instanceof BoolExp){
                    BoolExp boolICode = (BoolExp)assignICode.value;
                    DagNode newNode = factory.createBooleanNode(assignICode.place, boolICode.trueFalse);
                    this.dag.addDagNode(newNode);
                } else if(assignICode.value instanceof IntExp){
                    IntExp intICode = (IntExp)assignICode.value;
                    DagNode newNode = factory.createIntNode(assignICode.place, intICode.value);
                    this.dag.addDagNode(newNode);
                } else if(assignICode.value instanceof RealExp){
                    RealExp realICode = (RealExp)assignICode.value;
                    DagNode newNode = factory.createRealNode(assignICode.place, realICode.realValue);
                    this.dag.addDagNode(newNode);
                } else if(assignICode.value instanceof StrExp){
                    StrExp strICode = (StrExp)assignICode.value;
                    DagNode newNode = factory.createStringNode(assignICode.place, strICode.value);
                    this.dag.addDagNode(newNode);
                }
            }
        }
        buildCodeFromDag();
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

    @Override
    public void removeDeadCode() {
        while(true){
            List<DagNode> roots = this.dag.getRoots();

            List<DagNode> rootsNoLife = new LinkedList<DagNode>();
            for(DagNode root: roots){
                boolean rootContainsLife = false;
                for(String ident : root.getIdentifiers()){
                    if(this.lifeInformation.entryExists(ident)){
                        LiveInfo life = this.lifeInformation.getEntry(ident);
                        if(life.isAlive){
                            rootContainsLife = true;
                            break;
                        }
                    }
                }

                if(!rootContainsLife){
                    rootsNoLife.add(root);
                }
            }

            if(rootsNoLife.size() == 0){
                break;
            }

            for(DagNode root: rootsNoLife){
                this.dag.deleteDagNode(root);
            }
        }
        buildCodeFromDag();
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
    public List<ICode> getICode() {
        return block.getIcode();
    }
}
