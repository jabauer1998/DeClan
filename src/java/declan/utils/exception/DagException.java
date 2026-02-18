package declan.utils.exception;

public class DagException extends RuntimeException {
	public DagException(String methodName, String message) {
		super("Error in method " + methodName + "\n" + message);
	}
}
