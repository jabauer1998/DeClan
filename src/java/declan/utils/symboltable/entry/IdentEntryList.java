package declan.utils.symboltable.entry;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import declan.utils.Copyable;
import declan.middleware.icode.exp.IdentExp;

public class IdentEntryList extends LinkedList<IdentExp> implements Copyable<IdentEntryList> {
    @Override
    public IdentEntryList copy() {
        IdentEntryList toRet = new IdentEntryList();
        toRet.addAll(this);
        return toRet;
    }
}
