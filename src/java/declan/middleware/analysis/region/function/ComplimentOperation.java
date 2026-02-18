package declan.middleware.analysis.region.function;

import java.util.HashSet;
import java.util.Set;

public class ComplimentOperation<SetType> implements SetExpression<SetType>  {
	private SetExpression<SetType> setToCompliment;
	private SetExpression<SetType> universalSet;
	
	public ComplimentOperation(SetExpression<SetType> setToCompliment, SetExpression<SetType> universalSet) {
		this.setToCompliment = setToCompliment;
		this.universalSet = universalSet;
	}

	@Override
	public Set<SetType> compute(Set<SetType> input) {
		HashSet<SetType> toRet = new HashSet<SetType>();
		Set<SetType> mySet = setToCompliment.compute(input);
		for(SetType elem: universalSet.compute(input)){
			if(!mySet.contains(elem)) {
				toRet.add(elem);
			}
		}
		return toRet;
	}
}
