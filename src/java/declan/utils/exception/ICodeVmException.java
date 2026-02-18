package io.github.h20man13.DeClan.common.exception;

import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.exp.Exp;

public class ICodeVmException extends RuntimeException {
    public ICodeVmException(ICode icode, int instructionNumber, String message){
        super("Error insterpreting instruction [" + icode + "]\n" + " located at position " + instructionNumber + "\n" + message);
    }

    public ICodeVmException(Exp exp, int instructionNumber, String message){
        super("Error interpreting expression [" + exp + "]\n" + " located at position " + instructionNumber + "\n" + message);
    }
}
