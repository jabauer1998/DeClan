package declan.middleware.icode.exp;

import declan.utils.pat.P;
import java.util.Objects;

public class IntArrayExp implements Exp{
    private int[] value;
    private IdentExp size;

    public IntArrayExp(int size){
	value = new int[size];
    }

    public IntArrayExp(int[] data){
	value = new int[data.length];
        for(int i = 0; i < data.length; i++){
	    value[i] = data[i];
	}
    }

    public int[] getValue(){
	return value;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof IntArrayExp){
            return this.value.equals(((IntArrayExp)exp).value);
        }
        return false;
    } 

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
	sb.append("{");
	int index = 0;
	while(index < value.length){
	    if(index != 0)
		sb.append(", ");
	    sb.append(value[index]);
	    index++;
	}
	sb.append("}");
	return sb.toString();
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public P asPattern(boolean hasContainer) {
        if(hasContainer){
            return P.PAT(P.PAT(P.NEW(), P.ARRAY(), P.INT(), P.OF(), P.INT()));
        } else {
	    return P.PAT(P.NEW(), P.ARRAY(), P.INT(), P.OF(), P.INT());
	}
    }

    @Override
    public boolean containsPlace(String place) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        //Do nothing
    }
    
    @Override
    public int hashCode() {
    	return Objects.hashCode(value);
    }

    @Override
    public Exp copy() {
	 int[] newVal = new int[value.length];
	 for(int i = 0; i < newVal.length; i++){
	     newVal[i] = value[i];
	 }
	 return new IntArrayExp(newVal);
    }

    @Override
    public boolean isZero() {
	    return false;
    }
}


