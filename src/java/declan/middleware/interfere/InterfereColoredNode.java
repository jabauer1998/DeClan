package io.github.h20man13.DeClan.common.interfere;

import java.util.Set;

import io.github.h20man13.DeClan.common.CopyStr;
import io.github.h20man13.DeClan.common.Triple;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.icode.ICode;

public class InterfereColoredNode extends Triple<Tuple<CopyStr, ICode>, Set<Tuple<CopyStr, CopyStr>>, ColorType> {
	public InterfereColoredNode(Tuple<CopyStr, ICode> fst, Set<Tuple<CopyStr, CopyStr>> snd, ColorType thrd) {
		super(fst, snd, thrd);
	}
}
