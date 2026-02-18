package declan.utils.exception;

public class UtilityException extends RuntimeException {
    public UtilityException(String method, String message){
        super("In method " + method + ":\n" + message);
    }
}
