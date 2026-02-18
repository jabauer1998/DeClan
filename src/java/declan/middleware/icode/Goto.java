package declan.middleware.icode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import declan.utils.pat.P;

public class Goto extends ICode {
	private static Set<Goto> gotos = new HashSet<Goto>();
	
	public static void clearGotos() {
		gotos = new HashSet<>();
	}
	
	public String label;
	private int seqNum;

	public Goto(String label) {
		this.label = label;
		recalculateIdentNumber();
	}
	
	private Goto(Goto other) {
		this.label = other.label;
		this.seqNum = other.seqNum;
	}

	@Override
	public String toString() {
		return "GOTO " + label;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Goto){
			Goto objGoto = (Goto)obj;

			return objGoto.label.equals(label) && this.seqNum == objGoto.seqNum;
		} else {
			return false;
		}
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return true;
	}

	@Override
	public P asPattern() {
		return P.PAT(P.GOTO(), P.ID());
	}

	@Override
	public boolean containsPlace(String place) {
		return false;
	}

	@Override
	public boolean containsLabel(String label) {
		return this.label.equals(label);
	}

	@Override
	public void replacePlace(String from, String to) {
		//DO nothing
	}

	@Override
	public void replaceLabel(String from, String to) {
		if(this.label.equals(from))
			this.label = to;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(label, seqNum);
	}
	
	public void recalculateIdentNumber() {
		for(seqNum = 0; gotos.contains(this); ++this.seqNum);
		gotos.add(this);
	}

	@Override
	public ICode copy() {
		return new Goto(this);
	}
}
