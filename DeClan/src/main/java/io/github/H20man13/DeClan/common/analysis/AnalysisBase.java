package io.github.H20man13.DeClan.common.analysis;

public interface AnalysisBase {
	public void run();
	
	public enum Direction{
        BACKWARDS,
        FORWARDS  
    }

    public enum Meet{
        UNION,
        INTERSECTION
    }
}
