package io.github.H20man13.DeClan.common.symboltable.entry;

import io.github.H20man13.DeClan.common.Copyable;

public class TypeCheckerQualities implements Copyable<TypeCheckerQualities> {
    private int val;
    
    public TypeCheckerQualities(int value) {
        this.val = value;
    }
    
    public static final int INTEGER = 0b0000001;
    public static final int BOOLEAN = 0b0000010;
    public static final int STRING = 0b00000100;
    public static final int REAL = 0b0000001000;
    public static final int VOID = 0b0000010000;
    public static final int NA = 0b000000100000;
    public static final int NEG = 0b00001000000;
    public static final int NULL = 0b0010000000;
    public static final int CONST = 0b100000000;

    public boolean containsQualities(int quality){
        return (quality & this.val) == quality;
    }

    public boolean missingQualities(int quality){
        return (quality & this.val) == 0;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(this.containsQualities(TypeCheckerQualities.NA)){
            sb.append("NA");
        } else if(this.containsQualities(TypeCheckerQualities.STRING)){
            sb.append("STRING");
        } else if(this.containsQualities(TypeCheckerQualities.VOID)){
            sb.append("VOID");  
        } else if(this.containsQualities(TypeCheckerQualities.BOOLEAN)){
            sb.append("BOOLEAN");  
        } else {

            boolean first = true;
            if(this.containsQualities(TypeCheckerQualities.CONST)){
                first = false;
                sb.append("CONST");
            }
            
            if(this.containsQualities(TypeCheckerQualities.NEG)){
                if(first){
                    first = false;
                    sb.append("NEGATIVE");
                } else {
                    sb.append(" NEGATIVE");
                }
            } else if(this.containsQualities(TypeCheckerQualities.NULL)){
                if(first){
                    first = false;
                    sb.append("NULL");
                } else {
                    sb.append(" NULL");
                }
            }

            if(this.containsQualities(TypeCheckerQualities.REAL)){
                if(first){
                    first = false;
                    sb.append("REAL");
                } else {
                    sb.append(" REAL");
                }
            } else if(this.containsQualities(TypeCheckerQualities.INTEGER)){
                if(first){
                    first = false;
                    sb.append("INTEGER");
                } else {
                    sb.append(" INTEGER");
                }
            }
        }

        return sb.toString();
    }

    @Override
    public TypeCheckerQualities copy() {
        return new TypeCheckerQualities(val);
    }
}
