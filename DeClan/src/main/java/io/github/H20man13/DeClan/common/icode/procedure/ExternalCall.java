package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public class ExternalCall implements ICode {
    public String paramName;
    public List<String> arguments;
    public String toRet;
    
    public ExternalCall(String paramName, List<String> arguments, String toRet){
        this.paramName = paramName;
        this.arguments = arguments;
        this.toRet = toRet;
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
}
