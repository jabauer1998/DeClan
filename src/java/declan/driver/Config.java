package io.github.h20man13.DeClan.common;

import java.util.HashMap;

public class Config {
    private HashMap<String, String> argToValue;

    public Config(){
        this.argToValue = new HashMap<String, String>();
    }

    public boolean containsFlag(String flag){
        return argToValue.containsKey(flag);
    }

    public void removeFlag(String flag){
        argToValue.remove(flag);
    }

    public String getValueFromFlag(String flag){
        return argToValue.get(flag);
    }

    public void addFlag(String flag, String value){
        this.argToValue.put(flag, value);
    }
}
