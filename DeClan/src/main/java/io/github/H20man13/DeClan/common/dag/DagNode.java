package io.github.H20man13.DeClan.common.dag;

import java.util.List;

public interface DagNode {
    public boolean containsId(String ident);
    public boolean equals(DagNode dagNode);
    public void addIdentifier(String ident);
    public void addAncestor(DagNode ancestor);
    public void deleteAncestor(DagNode ancestor);
    public boolean isRoot();
    public List<DagNode> getChildren();
}
