package io.github.h20man13.DeClan.common.exception;

import io.github.h20man13.DeClan.common.ast.ASTNode;

public class ICodeGeneratorException extends RuntimeException {
    public ICodeGeneratorException(ASTNode astNode, String message){
        super("In ast node at " + astNode.getStart() + " with src text\n[\n" + astNode.toString() + "\n]\n" + message);
    }
}
