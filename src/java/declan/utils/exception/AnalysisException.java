package io.github.h20man13.DeClan.common.exception;

class AnalysisException extends RuntimeException {
	public AnalysisException(String methodName, String analysisType, String message) {
		super("In analysis type: " + analysisType + "\r\nIn method name: " + methodName + "\r\n" + message);
	}
}
