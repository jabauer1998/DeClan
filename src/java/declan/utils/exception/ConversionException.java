package declan.utils.exception;

public class ConversionException extends RuntimeException {
    public ConversionException(String funcName, String fromType, String toType){
        super("In function " + funcName + " cant convert from "+ fromType + " to " + toType);
    }
}
