package io.github.h20man13.DeClan.common.exception;

public class DagException extends RuntimeException {
	public DagException(String methodName, String message) {
		super("Error in method " + methodName + "\n" + message);
	}
}
