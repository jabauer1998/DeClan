package declan.utils.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import declan.middleware.icode.ICode;

public class ExitNode implements FlowGraphNode{
    public FlowGraphNode exit;

    public ExitNode(FlowGraphNode exit){
        this.exit = exit;
    }

    @Override
    public List<ICode> getICode() {
        return new LinkedList<ICode>();
    }

    @Override
    public String toString(){
        return "EXIT";
    }

	@Override
	public FlowGraphNode copy() {
		return new ExitNode(exit);
	}

	@Override
	public BlockNode findEndData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockNode findEndBss() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkEndData() {
		return false;
	}

	@Override
	public boolean checkEndBss() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BlockNode findStartBss() {
		return null;
	}

	@Override
	public BlockNode findStartData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkStartData() {
		return false;
	}

	@Override
	public boolean checkStartBss() {
		return false;
	}
}
