package io.github.H20man13.DeClan.common.builder;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.section.SymbolSectionBuilder;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.main.MyIrFactory;

public class ProcedureBuilder extends StatementBuilder implements CompletableBuilder<Proc>, ResetableBuilder {
    private IrBuilderContext ctx;
    private IrRegisterGenerator gen;
    private MyIrFactory factory;
    private ProcLabel label;
    private List<Assign> paramaters;
    private Assign returnPlace;
    private Return ret;
    private SymbolSectionBuilder symbols;
    private int paramNumber;

    public ProcedureBuilder(SymbolSectionBuilder symbols, IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        super(symbols, ctx, gen, errLog);
        this.ctx = ctx;
        this.gen = gen;
        this.factory = new MyIrFactory(errLog);
        this.symbols = symbols;
        this.paramNumber = 0;
        resetBuilder();
    }
    @Override
    public Proc completeBuild() {
        Proc toRet =  new Proc(label, paramaters, intermediateCode, returnPlace, ret);
        resetBuilder();
        return toRet;
    }
    @Override
    public void resetBuilder() {
        super.resetBuilder();
        this.label = null;
        this.paramaters = new LinkedList<Assign>();
        this.returnPlace = null;
        this.ret = null;
        this.paramNumber = 0;
    }

    public String buildParamaterAssignment(String value, Assign.Type type){
        String newPlace = gen.genNext();
        paramaters.add(factory.produceParamAssignment(newPlace, value, type));
        symbols.addParamSymEntry(SymEntry.INTERNAL, value, label.label, paramNumber);
        this.paramNumber++;
        return newPlace;
    }

    public void buildProcedureLabel(String name){
        this.label = factory.produceProcedureLabel(name);
    }

    public void buildReturnStatement(){
        this.ret = factory.produceReturnStatement();
    }

    public void buildInternalReturnPlacement(String dest, String source, Assign.Type type){
        this.returnPlace = factory.produceInternalReturnPlacement(dest, source, type);
        symbols.addReturnSymEntry(SymEntry.INTERNAL, dest, label.label);
    }
}
