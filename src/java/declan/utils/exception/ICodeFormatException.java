package declan.utils.exception;

import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.Exp;

public class ICodeFormatException extends RuntimeException {
    public ICodeFormatException(ICode instr, String message){
        super("Invalid format used in instruction [" + instr + "]\n" + message);
    }

    public ICodeFormatException(Exp instr, String message){
        super("Invalid format used in expression " + "(" + instr +")\n" + message);
    }
}
