package declan.middleware.dag;

import java.util.List;

import declan.middleware.icode.exp.IdentExp;

public interface DagNode {
    public enum ScopeType{
        GLOBAL,
        RETURN,
        LOCAL,
        PARAM
    }

    public enum ValueType{
        STRING, INT, REAL, BOOL, CHAR, INT_ARRAY, REAL_ARRAY, BOOL_ARRAY
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


