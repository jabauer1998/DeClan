package io.github.H20man13.DeClan.common.symboltable;

public class LiveInfo {
    public int nextUse;
    public boolean isAlive;

    public LiveInfo(boolean isAlive, int nextUse){
        this.nextUse = nextUse;
        this.isAlive = isAlive;
    }
}
