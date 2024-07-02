package io.github.H20man13.DeClan.common.exception;

public class OptimizerException extends RuntimeException {
    public OptimizerException(String methodName, String message){
        super("In method " + methodName + "\n" + message);
    }
}
