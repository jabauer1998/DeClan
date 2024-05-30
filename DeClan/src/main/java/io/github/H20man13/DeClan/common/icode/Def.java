package io.github.H20man13.DeClan.common.icode;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.pat.P;

public class Def implements ICode {

    public String label;
    public Type type;
    public Scope scope;

    public Def(ICode.Scope scope, String label, ICode.Type type){
        this.scope = scope;
        this.label = label;
        this.type = type;
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
        if(this.scope == Scope.GLOBAL){
            if(this.type == Type.INT) return P.PAT(P.GLOB(), P.DEF(), P.ID(), P.INT());
            else if(this.type == Type.BOOL) return P.PAT(P.GLOB(), P.DEF(), P.ID(), P.BOOL());
            else if(this.type == Type.STRING) return P.PAT(P.GLOB(), P.DEF(), P.ID(), P.STR());
            else if(this.type == Type.REAL) return P.PAT(P.GLOB(), P.DEF(), P.ID(), P.REAL());
        } else {
            if(this.type == Type.INT) return P.PAT(P.DEF(), P.ID(), P.INT());
            else if(this.type == Type.BOOL) return P.PAT(P.DEF(), P.ID(), P.BOOL());
            else if(this.type == Type.STRING) return P.PAT(P.DEF(), P.ID(), P.STR());
            else if(this.type == Type.REAL) return P.PAT(P.DEF(), P.ID(), P.REAL());
        }
        return P.PAT();
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> list = new LinkedList<ICode>();
        list.add(this);
        return list;
        
    }

    @Override
    public boolean containsPlace(String place) {
        return label.equals(place);
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        if(this.label.equals(from))
            this.label = to;
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
    
}
