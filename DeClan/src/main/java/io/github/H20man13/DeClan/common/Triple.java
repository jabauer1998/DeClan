package io.github.H20man13.DeClan.common;

public class Triple<FstType, SndType, ThrdType> {
	public FstType fst;
	public SndType snd;
	public ThrdType thrd;
	
	public Triple(FstType fst, SndType snd, ThrdType thrd){
		this.fst = fst;
		this.snd = snd;
		this.thrd = thrd;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Triple) {
			Triple<FstType, SndType, ThrdType> trip = (Triple<FstType, SndType, ThrdType>)obj;
			if(fst.equals(trip.fst))
				if(snd.equals(trip.snd))
					return thrd.equals(trip.thrd);
		}
		return false;
	}
}
