package io.github.H20man13.DeClan.common.builder.section;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.builder.IrBuilderContext;
import io.github.H20man13.DeClan.common.builder.ProcedureBuilder;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;

public class ProcedureSectionBuilder implements CompletableBuilder<ProcSec>, ResetableBuilder{
    private ProcedureBuilder procBuilder;
    private List<Proc> procedures;

    public ProcedureSectionBuilder(SymbolSectionBuilder symbols, IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        this.procBuilder = new ProcedureBuilder(symbols, ctx, gen, errLog);
        resetBuilder();
    }

    public ProcedureBuilder getProcedureBuilder(){
        return procBuilder;
    }

    @Override
    public ProcSec completeBuild() {
        ProcSec section =  new ProcSec(procedures);
        resetBuilder();
        return section;
    }

    public void addProcedure(Proc procedure){
        this.procedures.add(procedure);
    }

    @Override
    public void resetBuilder() {
        this.procedures = new LinkedList<Proc>();
    }
}
