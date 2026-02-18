package io.github.h20man13.DeClan.common.icode.label;

import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.pat.P;

public class StandardLabel extends Label {

    public StandardLabel(String label) {
        super(label);
    }

    @Override
    public P asPattern() {
        return P.PAT(P.LABEL(), P.ID());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("LABEL ");
        sb.append(label);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StandardLabel){
            StandardLabel label = (StandardLabel)obj;
            return label.label.equals(this.label);
        } else {
            return false;
        }
    }

	@Override
	public ICode copy() {
		return new StandardLabel(label);
	}
}
