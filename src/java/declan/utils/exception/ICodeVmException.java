package declan.utils.exception;

import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.Exp;

public class ICodeVmException extends RuntimeException {
    public ICodeVmException(ICode icode, int instructionNumber, String message){
        super("Error insterpreting instruction [" + icode + "]\n" + " located at position " + instructionNumber + "\n" + message);
    }

    public ICodeVmException(Exp exp, int instructionNumber, String message){
        super("Error interpreting expression [" + exp + "]\n" + " located at position " + instructionNumber + "\n" + message);
    }
}
