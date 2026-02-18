package declan.middleware.icode.section;

import declan.middleware.icode.ICode;
import declan.utils.pat.P;

public class BssSec extends ICode {

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
        return P.PAT(P.BSS(), P.SECTION());
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof BssSec;
    }

    @Override
    public String toString(){
        return "BSS SECTION";
    }

    @Override
    public boolean containsPlace(String place) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsPlace'");
    }

    @Override
    public boolean containsLabel(String label) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsLabel'");
    }

    @Override
    public void replacePlace(String from, String to) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replacePlace'");
    }

    @Override
    public void replaceLabel(String from, String to) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replaceLabel'");
    }

	@Override
	public ICode copy() {
		return new BssSec();
	}
    
	
	@Override
	public int hashCode() {
		return BssSec.class.hashCode();
	}
}
