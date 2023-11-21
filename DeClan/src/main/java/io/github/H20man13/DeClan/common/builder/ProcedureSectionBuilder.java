package io.github.H20man13.DeClan.common.builder;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;

public class ProcedureSectionBuilder implements IrBuilderTemplate<ProcSec> {
    private ProcedureBuilder procBuilder;
    private List<Proc> procedures;

    public ProcedureSectionBuilder(IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        this.procBuilder = new ProcedureBuilder(ctx, gen, errLog);
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
