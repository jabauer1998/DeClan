package io.github.h20man13.DeClan.common.exception;

import io.github.h20man13.DeClan.common.dag.DagNode;

public class DagFormatException extends RuntimeException {
    public DagFormatException(DagNode node, String message){
        super("Error with node \n" + node.toString() + "\n" + message);
    }
}
