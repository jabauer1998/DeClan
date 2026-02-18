package declan.middleware.interfere;

import java.util.Set;

import declan.utils.CopyStr;
import declan.utils.Triple;
import declan.utils.Tuple;
import declan.middleware.icode.ICode;

public class InterfereColoredNode extends Triple<Tuple<CopyStr, ICode>, Set<Tuple<CopyStr, CopyStr>>, ColorType> {
	public InterfereColoredNode(Tuple<CopyStr, ICode> fst, Set<Tuple<CopyStr, CopyStr>> snd, ColorType thrd) {
		super(fst, snd, thrd);
	}
}
