package io.github.H20man13.DeClan.common.exception;

import io.github.H20man13.DeClan.common.icode.ICode;

public class ICodeLinkerException extends RuntimeException {
    public ICodeLinkerException(ICode instruction, String message){
        super("Linker failed at " + instruction + "\n" + message);
    }
}
