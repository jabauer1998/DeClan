package declan.middleware.interfere;

public class Spill implements ColorType {
	public Spill() {}
	
	public boolean equals(Object obj) {
		return obj instanceof Spill;
	}
	
	public String toString() {
		return "SPILL";
	}

	@Override
	public boolean matches(Object obj) {
		return obj instanceof Spill;
	}
}
