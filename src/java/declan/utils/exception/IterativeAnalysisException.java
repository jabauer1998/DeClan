package declan.utils.exception;

public class IterativeAnalysisException extends AnalysisException {
	public IterativeAnalysisException(String methodName, String message) {
		super(methodName, "Iterative-Algorithm", message);
	}
}
