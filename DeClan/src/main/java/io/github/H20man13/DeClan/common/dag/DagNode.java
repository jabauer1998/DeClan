package io.github.H20man13.DeClan.common.dag;

import java.util.List;

public interface DagNode {
    public boolean containsId(String ident);
    @Override
    public boolean equals(Object dagNode);
    public void addIdentifier(String ident);
    public List<String> getIdentifiers();
    public void addAncestor(DagNode ancestor);
    public void deleteAncestor(DagNode ancestor);
    public boolean isRoot();
    public List<DagNode> getChildren();
}
