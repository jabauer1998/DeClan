package io.github.H20man13.DeClan.common;

public class Tuple<SourceType, DestType> {
    public SourceType source;
    public DestType dest;

    public Tuple(SourceType source, DestType dest){
        this.source = source;
        this.dest = dest;
    }

    @Override
    public int hashCode(){
        return source.hashCode();
    }

    @Override
    public boolean equals(Object dest){
        if(dest instanceof Tuple){
            Tuple<SourceType, DestType> destConv = (Tuple<SourceType, DestType>)dest;
            boolean sourceEquals = this.source.equals(destConv.source);
            boolean destEquals = this.dest.equals(destConv.dest);
            return sourceEquals && destEquals;
        } else {
            return false;
        }
    }
}
