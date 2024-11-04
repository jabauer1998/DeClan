package io.github.H20man13.DeClan.common.exception;

public class RegionAnalysisException extends AnalysisException {
	public RegionAnalysisException(String methodName, String message) {
		super(methodName, "Region-Based", message);
	}
}
