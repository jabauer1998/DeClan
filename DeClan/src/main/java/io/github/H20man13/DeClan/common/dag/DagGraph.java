package io.github.H20man13.DeClan.common.dag;

import java.util.ArrayList;
import java.util.List;

public class DagGraph {
    private List<DagNode> dagNodes;

    public DagGraph(){
        this.dagNodes = new ArrayList<DagNode>();
    }

    public DagNode searchForLatestChild(String identifier){
        for(int i = dagNodes.size(); i >= 0; i--){
            DagNode child = dagNodes.get(i);
            if(child.containsId(identifier)){
                return child;
            }
        }
        return null;
    }

    public void addDagNode(DagNode node){
        this.dagNodes.add(node);
    }

    public DagNode getDagNode(DagNode node){
        for(int i = dagNodes.size(); i >= 0; i--){
            DagNode nodeAtI = dagNodes.get(i);
            if(nodeAtI.equals(node)){
                return nodeAtI;
            }
        }
        return null;
    }
}
