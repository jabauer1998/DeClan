package io.github.H20man13.DeClan.common.exception;

public class NoSymbolFoundException extends RuntimeException {
    public NoSymbolFoundException(){
        super("No symbol was found inside of the Symol Section or Table");
    }
}
