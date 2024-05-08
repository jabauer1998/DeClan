package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.Utils;

public class Proc implements ICode {
    public ProcLabel label;
    public List<Assign> paramAssign;
    public List<ICode> instructions;
    public Assign placement;
    public Return returnStatement;

    public Proc(ProcLabel label, List<Assign> paramAssign, List<ICode> instructions, Assign place, Return returnStatement){
        this.label = label;
        this.paramAssign = paramAssign;
        this.instructions = instructions;
        this.placement = place;
        this.returnStatement = returnStatement;
    }

    public Proc(ProcLabel label){
        this.label = label;
        this.paramAssign = new LinkedList<Assign>();
        this.instructions = new LinkedList<ICode>();
        this.placement = null;
        this.returnStatement = new Return();
    }

    public ICode getInstruction(int index){
        return this.instructions.get(index);
    }

    public Assign getParamater(int index){
        return this.paramAssign.get(index);
    }

    public int getParamLength(){
        return this.paramAssign.size();
    }

    public int getInstructionLength(){
        return this.instructions.size();
    }

    public void addParamater(Assign assign){
        this.paramAssign.add(assign);
    }

    public void addInstruction(ICode instruction){
        this.instructions.add(instruction);
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public P asPattern() {
        int paramLength = paramAssign.size();
        int instructionsLength = instructions.size();
        int totalLength = paramLength + instructionsLength + 2;
        if(placement != null){
            totalLength++;
        }

        P[] globalPatterns = new P[instructionsLength];
        int globalIndex = 0;

        globalPatterns[globalIndex] = label.asPattern();
        globalIndex++;

        for(int i = 0; i < paramLength; i++){
            globalPatterns[globalIndex] = paramAssign.get(i).asPattern();
            globalIndex++;
        }
        
        
        for(int i = 0; i < instructionsLength; i++){
            globalPatterns[globalIndex] = instructions.get(i).asPattern();
            globalIndex++;
        }

        if(placement != null){
            globalPatterns[globalIndex] = placement.asPattern();
            globalIndex++;
        }

        globalPatterns[globalIndex] = returnStatement.asPattern();

        return P.PAT(globalPatterns);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Proc){
            Proc objProc = (Proc)obj;

            if(!objProc.label.equals(label))
                return false;

            if(objProc.paramAssign.size() != paramAssign.size())
                return false;

            for(int i = 0; i < paramAssign.size(); i++){
                Assign assign1 = paramAssign.get(i);
                Assign assign2 = objProc.paramAssign.get(i);

                if(!assign1.equals(assign2))
                    return false;
            }

            if(objProc.instructions.size() != instructions.size())
                return false;

            for(int i = 0; i < instructions.size(); i++){
                ICode instr1 = instructions.get(i);
                ICode instr2 = objProc.instructions.get(i);

                if(!instr1.equals(instr2))
                    return false;
            }

            if(objProc.placement == null && placement != null 
            || objProc.placement != null && placement == null)
                return false;

            if(objProc.placement != null && placement != null)
                if(!objProc.placement.equals(placement))
                return false;

            if(!objProc.returnStatement.equals(returnStatement))
                return false;

            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(label.toString());
        sb.append("\r\n");
        for(Assign assign: paramAssign){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append("  ");
            innerSb.append(assign.toString());
            innerSb.append("\r\n");
            sb.append(Utils.formatStringToLeadingWhiteSpace(innerSb.toString()));
        }

        for(ICode instruction: instructions){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append("  ");
            innerSb.append(instruction.toString());
            innerSb.append("\r\n");
            sb.append(Utils.formatStringToLeadingWhiteSpace(innerSb.toString()));
        }

        if(placement != null){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append("  ");
            innerSb.append(placement.toString());
            innerSb.append("\r\n");
            sb.append(Utils.formatStringToLeadingWhiteSpace(innerSb.toString()));
        }
        
        sb.append(" ");
        sb.append(returnStatement.toString());

        return sb.toString();
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> toRet = new LinkedList<ICode>();

        toRet.addAll(label.genFlatCode());

        for(Assign assign : paramAssign){
            toRet.addAll(assign.genFlatCode());
        }

        for(ICode icode: instructions){
            toRet.addAll(icode.genFlatCode());
        }

        if(placement != null)
            toRet.addAll(placement.genFlatCode());

        toRet.addAll(returnStatement.genFlatCode());

        return toRet;
    }

    @Override
    public boolean containsPlace(String place) {
        for(Assign assign: paramAssign){
            if(assign.containsPlace(place))
                return true;
        }

        for(ICode instruction: instructions){
            if(instruction.containsPlace(place))
                return true;
        }

        if(this.placement != null)
            if(this.placement.containsPlace(place))
                return true;

        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        if(this.label.containsLabel(label))
            return true;

        for(ICode instruction: instructions){
            if(instruction.containsLabel(label))
                return true;
        }

        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        for(Assign assign: paramAssign){
            assign.replacePlace(from, to);
        }

        for(ICode instruction: instructions){
            instruction.replacePlace(from, to);
        }

        if(this.placement != null)
            placement.replacePlace(from, to);
    }

    @Override
    public void replaceLabel(String from, String to) {
        this.label.replaceLabel(from, to);

        for(ICode instruction: instructions){
            instruction.replaceLabel(from, to);
        }
    }

    @Override
    public boolean containsParamater(String place) {
        for(Assign assign: paramAssign){
            if(assign.value.containsPlace(place)){
                return true;
            }
        }
        for(ICode instruction: instructions){
            if(instruction.containsParamater(place)){
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<String> paramaterForFunctions(String place) {
        HashSet<String> toRet = new HashSet<String>();
        for(Assign assign: paramAssign){
            if(assign.value.containsPlace(place)){
                toRet.add(label.label);
            }
        }

        for(ICode instruction: instructions){
            Set<String> instructionResult = instruction.paramaterForFunctions(place);
            toRet.addAll(instructionResult);
        }

        return toRet;
    }

    @Override
    public Set<String> argumentInFunctions(String place) {
        HashSet<String> toRet = new HashSet<String>();
        for(ICode instruction: instructions){
            Set<String> toAdd = instruction.argumentInFunctions(place);
            toRet.addAll(toAdd);
        }
        return toRet;
    }

    @Override
    public boolean containsArgument(String place) {
        return false;
    }

    @Override
    public Set<String> internalReturnForFunctions(String place) {
        HashSet<String> toRet = new HashSet<String>();
        if(placement != null){
            toRet.add(label.label);
        }
        return toRet;
    }

    @Override
    public Set<String> externalReturnForFunctions(String place) {
        HashSet<String> toRet = new HashSet<String>();
        for(ICode icode: instructions){
            Set<String> toAdd = icode.externalReturnForFunctions(place);
            toRet.addAll(toAdd);
        }
        return toRet;
    }

    @Override
    public boolean containsExternalReturn(String place) {
        for(ICode icode: instructions){
            if(icode.containsExternalReturn(place))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsInternalReturn(String place) {
        if(this.placement != null)
            return this.placement.containsInternalReturn(place);
        return false;
    }
}
