package declan.middleware.analysis;

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
