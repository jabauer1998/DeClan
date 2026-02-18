package declan.middleware.icode.label;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import declan.middleware.icode.ICode;
import declan.utils.pat.P;

public class ProcLabel extends Label {
    public ProcLabel(String label) {
        super(label);
    }

    @Override
    public P asPattern() {
        return P.PAT(P.PROC(), P.LABEL(), P.ID());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PROC LABEL ");
        sb.append(label);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ProcLabel){
            ProcLabel label = (ProcLabel)obj;

            return label.label.equals(this.label);
        } else {
            return false;
        }
    }

	@Override
	public ICode copy() {
		return new ProcLabel(label);
	}
}