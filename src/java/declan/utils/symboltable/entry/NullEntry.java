package declan.utils.symboltable.entry;

import declan.utils.Copyable;

public class NullEntry implements  Copyable<NullEntry>{
    public NullEntry(){}

    @Override
    public NullEntry copy() {
        return this;
    }
}
