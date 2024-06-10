package io.github.H20man13.DeClan.common.exception;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.Exp;

public class ICodeFormatException extends RuntimeException {
    public ICodeFormatException(ICode instr, String message){
        super("Invalid format used in instruction [" + instr + "]\n" + message);
    }

    public ICodeFormatException(Exp instr, String message){
        super("Invalid format used in expression " + "(" + instr +")\n" + message);
    }
}
