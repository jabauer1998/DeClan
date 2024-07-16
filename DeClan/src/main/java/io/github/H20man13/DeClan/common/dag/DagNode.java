package io.github.H20man13.DeClan.common.dag;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.exp.IdentExp;

public interface DagNode {
    public enum ScopeType{
        GLOBAL,
        RETURN,
        LOCAL,
        PARAM
    }

    public enum ValueType{
        STRING, INT, REAL, BOOL 
    }

    public boolean containsId(IdentExp ident);
    @Override
    public boolean equals(Object dagNode);
    public void addIdentifier(String ident);
    public List<String> getIdentifiers();
    public void addAncestor(DagNode ancestor);
    public void deleteAncestor(DagNode ancestor);
    public boolean isRoot();
    public List<DagNode> getChildren();
    public ScopeType getScopeType();
    public ValueType getValueType();
}
