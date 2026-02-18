package io.github.h20man13.DeClan.common.symboltable.entry;

import io.github.h20man13.DeClan.common.Copyable;

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
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	if(isAlive) {
    		sb.append("Is Alive at ");
    	} else {
    		sb.append("Is Dead at ");
    	}
    	
    	sb.append(nextUse);
    	return sb.toString();
    }
}
