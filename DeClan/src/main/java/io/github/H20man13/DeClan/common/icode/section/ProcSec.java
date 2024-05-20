package io.github.H20man13.DeClan.common.icode.section;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.Pattern;

public class ProcSec implements ICode {
    public List<Proc> procedures;

    public ProcSec(List<Proc> procedures){
        this.procedures = procedures;
    }

    public ProcSec(){
        this(new LinkedList<Proc>());
    }

    public int getLength(){
        return procedures.size();
    }

    public boolean containsProcedure(String name){
        for(Proc procedure : procedures){
            if(procedure.label.label.equals(name)){
                return true;
            }
        }
        return false;
    }

    public Proc getProcedureByName(String name){
        for(Proc procedure : procedures){
            if(procedure.label.label.equals(name)){
                return procedure;
            }
        }
        return null;
    }

    public Proc getProcedureByIndex(int index){
        return procedures.get(index);
    }

    public void addProcedure(Proc procedure){
        this.procedures.add(procedure);
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
    public boolean equals(Object obj){
        if(obj instanceof ProcSec){
            ProcSec procSec = (ProcSec)obj;

            if(procSec.getLength() != getLength())
                return false;

            for(int i = 0; i < getLength(); i++){
                Proc objProc = procSec.getProcedureByIndex(i);
                Proc proc = getProcedureByIndex(i);

                if(!objProc.equals(proc))
                    return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public P asPattern() {
        int size = procedures.size();
        P[] patList = new P[size];
        for(int i = 0; i < size; i++){
            patList[i] = procedures.get(i).asPattern();
        }
        return P.PAT(patList);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PROC SECTION\r\n");
        for(Proc procedure: procedures){
            StringBuilder innerSb = new StringBuilder();
            innerSb.append(' ');
            innerSb.append(procedure.toString());
            innerSb.append("\r\n");
            sb.append(innerSb);
        }
        return sb.toString();
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        for(Proc procedure: procedures){
            resultList.addAll(procedure.genFlatCode());
        }
        return resultList;
    }

    @Override
    public boolean containsPlace(String place) {
        for(Proc proc: procedures){
            if(proc.containsPlace(place))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        for(Proc proc: procedures){
            if(proc.containsLabel(label)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void replacePlace(String from, String to){
        for(Proc procedure: procedures){
            procedure.replacePlace(from, to);
        }
    }

    @Override
    public void replaceLabel(String from, String to) {
        for(Proc procedure: procedures){
            procedure.replaceLabel(from, to);
        }
    }
}
