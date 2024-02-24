package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.pat.P;

public class ExternalCall implements ICode, Exp {
    public String procedureName;
    public List<Tuple<String, Assign.Type>> arguments;
    
    public ExternalCall(String procedureName, List<Tuple<String, Assign.Type>> arguments){
        this.procedureName = procedureName;
        this.arguments = arguments;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return true;
    }

    @Override
    public P asPattern() {
        return P.PAT(P.EXTERNAL(), P.CALL(), P.ID());
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ExternalCall){
            ExternalCall call = (ExternalCall)obj;

            if(!call.procedureName.equals(procedureName))
                return false;

            if(call.arguments.size() != arguments.size())
                return false;

            for(int i = 0; i < arguments.size(); i++){
                Tuple<String, Assign.Type> arg1 = call.arguments.get(i);
                Tuple<String, Assign.Type> arg2 = arguments.get(i);

                if(!arg1.equals(arg2))
                    return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("EXTERNAL CALL ");
        sb.append(procedureName);
        sb.append('(');

        for(int i = 0; i < arguments.size(); i++){
            Tuple<String, Assign.Type> arg = arguments.get(i);
            sb.append('(');
            sb.append(arg.source);
            sb.append(", [");
            sb.append(arg.dest);
            sb.append("])");
            if(i < arguments.size() - 1){
                sb.append(", ");
            }
        }

        sb.append(')');

        return sb.toString();
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        resultList.add(this);
        return resultList;
    }

    @Override
    public P asPattern(boolean hasContainer) {
        return asPattern();
    }
}
