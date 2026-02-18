package declan.utils.exception;

import declan.middleware.dag.DagNode;

public class DagFormatException extends RuntimeException {
    public DagFormatException(DagNode node, String message){
        super("Error with node \n" + node.toString() + "\n" + message);
    }
}
