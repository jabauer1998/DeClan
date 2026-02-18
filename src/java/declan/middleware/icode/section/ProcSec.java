package declan.middleware.icode.section;

import declan.middleware.icode.ICode;
import declan.utils.pat.P;

public class ProcSec extends ICode {
    public ProcSec(){
        //Do nothing
    }

    @Override
    public boolean isConstant() {
       return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof ProcSec;
    }

    @Override
    public P asPattern() {
        return P.PAT(P.PROC(), P.SECTION());
    }

    @Override
    public String toString(){
        return "PROC SECTION";
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
    public void replacePlace(String from, String to){
        //Do nothing
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing  
    }

	@Override
	public ICode copy() {
		return new ProcSec();
	}
	
	@Override
	public int hashCode() {
		return ProcSec.class.hashCode();
	}
}
