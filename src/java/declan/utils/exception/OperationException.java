package declan.utils.exception;

public class OperationException extends RuntimeException {
    public OperationException(String funcName, String type1, String type2){
        super("In function " + funcName + " cant perform the binary operation between " + type1 + " and " + type2);
    }

    public OperationException(String funcName, String type1){
        super("In function " + funcName + " cant perform the unary operation on " + type1);
    }
}
