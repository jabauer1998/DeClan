package declan.middleware.icode.exp;

import declan.utils.pat.P;
import java.util.Objects;

public class CharArrayExp implements Exp{
    private char[] value;

    public CharArrayExp(char[] value){
	this.value = new char[value.length];
	for(int i = 0; i < value.length; i++){
	    this.value[i] = value[i];
	}
    }
    
    public CharArrayExp(String value){
	this.value = new char[value.length() + 1];
	int i;
	for(i = 0; i < value.length(); i++){
	    this.value[i] = value.charAt(i);
	}
	this.value[i] = '\0';
    }

    public CharArrayExp(int size){
	value = new char[size];
    }

    public char[] getValue(){
	return value;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof CharArrayExp){
            return this.value.equals(((CharArrayExp)exp).value);
        }
        return false;
    } 

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
	sb.append('"');
	int index = 0;
	while(index < value.length){
	    if(value[index] == '\0')
		break;
	    sb.append(value[index]);
	    index++;
	}
	sb.append('"');
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
            return P.PAT(P.PAT(P.NEW(), P.ARRAY(), P.INT(), P.OF(), P.CHAR()));
        } else {
	    return P.PAT(P.NEW(), P.ARRAY(), P.INT(), P.OF(), P.CHAR());
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
	 char[] newVal = new char[value.length];
	 for(int i = 0; i < newVal.length; i++){
	     newVal[i] = value[i];
	 }
	 return new CharArrayExp(newVal);
    }

    @Override
    public boolean isZero() {
	    return false;
    }
}


