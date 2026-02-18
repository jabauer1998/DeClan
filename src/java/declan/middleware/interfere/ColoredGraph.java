package declan.middleware.interfere;

import java.util.Map;
import java.util.Set;

public class ColoredGraph {
	private Set<String> toSpill;
	private Map<String, Color> colored;
	
	public ColoredGraph(Map<String, Color> colored, Set<String> spill) {
		this.toSpill = spill;
		this.colored = colored;
	}
	
	public boolean isSpill(String spill) {
		return toSpill.contains(spill);
	}
	
	public Color getColor(String color) {
		return colored.get(color);
	}
}
