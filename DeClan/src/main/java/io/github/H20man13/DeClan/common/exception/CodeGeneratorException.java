package io.github.H20man13.DeClan.common.exception;

public class CodeGeneratorException extends RuntimeException{
	public CodeGeneratorException(String method, String message) {
		super("In method: " + method + "\r\n" + message + "\r\n");
	}
}
