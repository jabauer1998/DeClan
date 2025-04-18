package io.github.H20man13.DeClan.common;

import java.util.List;
import java.util.Set;

public interface CustomMeet<SetType extends Set<AnalysisType>, AnalysisType> {
	public SetType performMeet(List<SetType> list);
}
