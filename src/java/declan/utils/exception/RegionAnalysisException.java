package declan.utils.exception;

public class RegionAnalysisException extends AnalysisException {
	public RegionAnalysisException(String methodName, String message) {
		super(methodName, "Region-Based", message);
	}
}
