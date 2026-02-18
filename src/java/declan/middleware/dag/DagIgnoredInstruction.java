package declan.middleware.dag;

import java.util.List;

import declan.utils.exception.DagException;
import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.IdentExp;

public class DagIgnoredInstruction implements DagNode {
	private ICode icode;
	
	public DagIgnoredInstruction(ICode icode) {
		this.icode = icode;
	}
	
	public ICode getIgnoredInstruction() {
		return icode;
	}

	@Override
	public boolean containsId(IdentExp ident) {
		return false;
	}

	@Override
	public void addIdentifier(String ident) {
		//Do nothing
	}

	@Override
	public List<String> getIdentifiers() {
		throw new DagException("getIdentifiers", "Cant fetch a identifier from an Ignored instruction");
	}
	
	@Override
	public void addAncestor(DagNode ancestor) {
		throw new DagException("deleteAncestor", "Cant add an ancestor from an Ignored instruction");
	}

	@Override
	public void deleteAncestor(DagNode ancestor) {
		throw new DagException("deleteAncestor", "Cant delete an ancestor from an Ignored instruction");
	}

	@Override
	public boolean isRoot() {
		//All ignored instructions are Roots by default because they have no children
		return true;
	}

	@Override
	public List<DagNode> getChildren() {
		throw new DagException("getChildren", "Cant fetch a child node from an Ignored instruction");	
	}

	@Override
	public ScopeType getScopeType() {
		throw new DagException("getScopeType", "Cant fetch a scope type from an Ignored instruction");
	}

	@Override
	public ValueType getValueType() {
		throw new DagException("getValueType", "Cant fetch a value type from an ignored instruction");
	}
	
	@Override
	public String toString() {
		return "IGNORED ( " + icode.toString() + " )";
	}
}
