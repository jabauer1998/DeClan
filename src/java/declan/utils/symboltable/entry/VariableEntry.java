package declan.utils.symboltable.entry;

import static declan.utils.MyIO.*;

import java.lang.String;
import java.lang.StringBuilder;

import declan.utils.Copyable;
import declan.utils.position.Position;

public class VariableEntry implements Copyable<VariableEntry>{
    
    private Boolean isConst; //variable to store the type or CONST
    private Object value; //variable to store the current value of the variable

    public VariableEntry(Boolean isConst, Object value){
        this.isConst = isConst;
	    this.value = value;
    }
    
    public Object getValue(){
	    return value;
    }

    public boolean isConst(){
	    return isConst;
    }
    
    public void setValue(Object value){
	    this.value = value;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(value.toString());
        if(isConst){
            sb.append(": CONST");
        } else {
            sb.append(": VAR");
        }
        return sb.toString();
    }

    @Override
    public VariableEntry copy() {
        return new VariableEntry(isConst, value);
    }
}
