package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public class ExternalCall implements ICode {
    public String procedureName;
    public List<String> arguments;
    public String toRet;
    
    public ExternalCall(String procedureName, List<String> arguments, String toRet){
        this.procedureName = procedureName;
        this.arguments = arguments;
        this.toRet = toRet;
    }

    public ExternalCall(String procedureName, List<String> arguments){
        this(procedureName, arguments, null);
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

            if(call.toRet == null && toRet != null || call.toRet != null && toRet == null)
                return false;

            if(!call.toRet.equals(toRet))
                return false;

            if(call.arguments.size() != arguments.size())
                return false;

            for(int i = 0; i < arguments.size(); i++){
                String arg1 = call.arguments.get(i);
                String arg2 = arguments.get(i);

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

        if(toRet != null){
            sb.append(toRet);
            sb.append(" := ");
        }

        sb.append("External Call ");
        sb.append(procedureName);
        sb.append('(');

        for(int i = 0; i < arguments.size(); i++){
            sb.append(arguments.get(i));
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
}
