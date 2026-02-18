package declan.middleware.icode.section;

import declan.middleware.icode.ICode;
import declan.utils.pat.P;

public class CodeSec extends ICode {
    public CodeSec(){
        //Do nothing
    };

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public P asPattern() {
        return P.PAT(P.CODE(), P.SECTION());
    }

    @Override
    public String toString(){
        return "CODE SECTION";
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof CodeSec;
    }

    @Override
    public boolean containsPlace(String place) {
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        //Do noothing
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }

	@Override
	public ICode copy() {
		return new CodeSec();
	}
	
	@Override
	public int hashCode() {
		return CodeSec.class.hashCode();
	}
}
