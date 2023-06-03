package io.github.H20man13.DeClan.common.symboltable;

import io.github.H20man13.DeClan.common.Copyable;

public class LiveInfo implements Copyable<LiveInfo>{
    public int nextUse;
    public boolean isAlive;

    public LiveInfo(boolean isAlive, int nextUse){
        this.nextUse = nextUse;
        this.isAlive = isAlive;
    }

    @Override
    public LiveInfo copy() {
        return new LiveInfo(this.isAlive, this.nextUse);
    }
}
