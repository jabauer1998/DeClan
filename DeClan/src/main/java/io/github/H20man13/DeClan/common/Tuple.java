package io.github.H20man13.DeClan.common;

import java.util.Objects;

public class Tuple<SourceType extends Copyable<SourceType>, DestType extends Copyable<DestType>> implements Copyable<Tuple<SourceType, DestType>>{
    public SourceType source;
    public DestType dest;

    public Tuple(SourceType source, DestType dest){
        this.source = source;
        this.dest = dest;
    }

	@Override
    public int hashCode(){
        return source.hashCode() + dest.hashCode();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(source.toString());
        sb.append(", ");
        sb.append(dest.toString());
        sb.append(')');
        return sb.toString();
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

	@Override
	public Tuple<SourceType, DestType> copy() {
		return new Tuple<>(source.copy(), dest.copy());
	}
}
