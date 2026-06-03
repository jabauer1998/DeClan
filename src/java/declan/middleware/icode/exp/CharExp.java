package declan.middleware.icode.exp;

import java.util.Objects;

import declan.utils.pat.P;

public class CharExp implements Exp{
    public char c;
    
    public CharExp(char c){
	this.c = c;
    }

    public String toString(){
	return "" + c;
    }

    public int hashCode(){
	return Objects.hash(c);
    }

    public CharExp copy(){
	return new CharExp(c);
    }

    public P asPattern(boolean tf){
	if(tf)
	    return P.PAT(P.CHAR());
	else
	    return P.CHAR();
    }

    @Override
    public boolean containsPlace(String place){
	return false;
    }

    @Override
    public void replacePlace(String from, String to){
	//Do nothing
    }

    @Override
    public boolean isConstant(){
	return true;
    }

    @Override
    public boolean isBranch(){
	return false;
    }

    @Override
    public boolean isZero(){
	return c == '\0';
    }
}
