package declan.utils.exception;

import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.Exp;

public class ICodeTypeCheckerException extends RuntimeException {
    public ICodeTypeCheckerException(String functionName, ICode icode, int instructionNumber, String message){
        super("In function " + functionName + "\nThe icode " + icode.toString() + "\n at line " + instructionNumber + "\n" + message);
    }
    public ICodeTypeCheckerException(String functionName, Exp icode, int instructionNumber, String message){
        super("In function " + functionName + "\nThe expression " + icode.toString() + "\n at line " + instructionNumber + "\n" + message);
    }
}
