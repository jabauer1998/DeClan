package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.BasicBlock;
import io.github.H20man13.DeClan.common.icode.ICode;

public interface FlowGraphNode {
    public List<ICode> getICode();
}
