package io.github.H20man13.DeClan.common.exception;

import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.ICode;

public class RegisterAllocatorException extends RuntimeException {
	public RegisterAllocatorException(String funcName, ICode icode, Set<Tuple<String, String>> registers, Set<Tuple<String, String>> tempRegisters, String message) {
		super("In function " + funcName + "\nIn instruction "+ icode + "\n [" + message + "]\nREGS: " + registers.toString() + "\nTEMP: " + tempRegisters.toString() + "\n");
	}
}
