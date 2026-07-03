package declan.middleware.icode.exp;

import java.util.Objects;
import java.util.regex.Pattern;
import declan.utils.pat.P;

public class CharExp implements Exp{
    public char c;
    
    public CharExp(char c){
	this.c = c;
    }

    public String toString(){
	StringBuilder sb = new StringBuilder();
	sb.append('\'');
	String escs = "\n\t\r\b\f\\\"'";
	if(c == '\n')
	    sb.append("\\n");
	else if(c == '\t')
	    sb.append("\\t");
	else if(c == '\r')
	    sb.append("\\r");
	else if(c == '\b')
	    sb.append("\\b");
	else if(c == '\f')
	    sb.append("\\f");
	else if(c == '\\')
	    sb.append("\\");
	else if(c == '\"')
	    sb.append("\\\"");
	else if(c == '\'')
	    sb.append("\\'");
	else
	    sb.append(c);
	sb.append('\'');
	return sb.toString();
    }

    public int hashCode(){
	return Objects.hash(c);
    }

    public CharExp copy(){
	return new CharExp(c);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof CharExp){
	    CharExp o = (CharExp)obj;
	    return o.c == c;
	} else {
	    return false;
	}
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


