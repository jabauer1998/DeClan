package io.github.H20man13.DeClan.common.exception;

import io.github.H20man13.DeClan.common.icode.ICode;

public class ICodeTypeCheckerException extends RuntimeException {
    public ICodeTypeCheckerException(String functionName, ICode icode, int instructionNumber, String message){
        super("In function " + functionName + "\nThe icode " + icode.toString() + "\n at line " + instructionNumber + "\n" + message);
    }
}
