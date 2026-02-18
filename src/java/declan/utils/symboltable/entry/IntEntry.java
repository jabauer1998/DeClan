package declan.utils.symboltable.entry;

import declan.utils.Copyable;

public class IntEntry implements Copyable<IntEntry> {
    private int value;

    public IntEntry(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    @Override
    public IntEntry copy() {
        return new IntEntry(value);
    }
}
