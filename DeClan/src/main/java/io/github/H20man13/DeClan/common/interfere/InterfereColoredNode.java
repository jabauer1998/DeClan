package io.github.H20man13.DeClan.common.interfere;

import java.util.Set;

import io.github.H20man13.DeClan.common.Triple;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.ICode;

public class InterfereColoredNode extends Triple<Tuple<String, ICode>, Set<Tuple<String, String>>, ColorType> {
	public InterfereColoredNode(Tuple<String, ICode> fst, Set<Tuple<String, String>> snd, ColorType thrd) {
		super(fst, snd, thrd);
	}

}
