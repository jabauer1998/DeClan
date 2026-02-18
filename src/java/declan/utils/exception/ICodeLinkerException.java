package declan.utils.exception;

import declan.middleware.icode.ICode;

public class ICodeLinkerException extends RuntimeException {
    public ICodeLinkerException(ICode instruction, String message){
        super("Linker failed at " + instruction + "\n" + message);
    }
}
