package io.github.h20man13.DeClan.common.exception;

import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.exp.Exp;

public class ICodeTypeCheckerException extends RuntimeException {
    public ICodeTypeCheckerException(String functionName, ICode icode, int instructionNumber, String message){
        super("In function " + functionName + "\nThe icode " + icode.toString() + "\n at line " + instructionNumber + "\n" + message);
    }
    public ICodeTypeCheckerException(String functionName, Exp icode, int instructionNumber, String message){
        super("In function " + functionName + "\nThe expression " + icode.toString() + "\n at line " + instructionNumber + "\n" + message);
    }
}
