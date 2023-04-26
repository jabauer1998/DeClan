package io.github.H20man13.DeClan.main;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.BasicBlock;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;

public class MyOptimizer {
    private List<ICode> intermediateCode;

    public MyOptimizer(List<ICode> intermediateCode){
        this.intermediateCode = intermediateCode;
    }

    private List<Integer> findFirsts(){
        List<Integer> firsts = new LinkedList<Integer>();
        for(int i = 0; i < intermediateCode.size(); i++){
            ICode intermediateInstruction = intermediateCode.get(i);
            if(i == 0){
                //First Statement is allways a leader
                firsts.add(i);
            } else if(intermediateInstruction instanceof Label){
                //Target of Jumps are allways leaders
                firsts.add(i);
            } else if(i + 1 < intermediateCode.size() && 
            (intermediateInstruction instanceof If || 
            intermediateInstruction instanceof Goto)){
                //First instruction following an IF and a Goto are leaders
                firsts.add(i + 1);
            }
        }

        return firsts;
    }

    public void breakIntoBasicBlocks(){
        List<Integer> firsts = findFirsts();
        List<ICode> basicBlocks = new LinkedList<ICode>();
        for(int leaderIndex = 0; leaderIndex < firsts.size(); leaderIndex++){
            int endIndex;
            if(leaderIndex + 1 < firsts.size()){
                endIndex = firsts.get(leaderIndex + 1) - 1;
            } else {
                endIndex = this.intermediateCode.size() - 1;
            }

            List<ICode> basicBlockList = new LinkedList<ICode>();
            for(int i = leaderIndex; i <= endIndex; i++){
                basicBlockList.add(intermediateCode.get(i));
            }

            basicBlocks.add(new BasicBlock(basicBlockList));
        }

        this.intermediateCode = basicBlocks;
    }

    public void commonSubExpressionElimination(){
        for(int i = 0; i < this.intermediateCode.size(); i++){
            
        }
    }

    private void commonSubExpressionElimination(ICode basicBlock){

    }
}
