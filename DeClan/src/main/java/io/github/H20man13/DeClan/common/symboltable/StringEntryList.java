package io.github.H20man13.DeClan.common.symboltable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import io.github.H20man13.DeClan.common.Copyable;

public class StringEntryList extends LinkedList<String> implements Copyable<StringEntryList> {
    @Override
    public StringEntryList copy() {
        StringEntryList toRet = new StringEntryList();
        toRet.addAll(this);
        return toRet;
    }
}
