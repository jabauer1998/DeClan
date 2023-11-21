package io.github.H20man13.DeClan.common.builder;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;

public class LibraryBuilder implements IrBuilderTemplate<Lib> {
    private IrBuilderContext ctx;
    private DataSectionBuilder variables;
    private ProcedureSectionBuilder procedures;
    private IrRegisterGenerator gen;

    public LibraryBuilder(ErrorLog errLog, IrBuilderContext ctx, IrRegisterGenerator gen){
        this.ctx = ctx;
        this.variables = new DataSectionBuilder(ctx, gen, errLog);
        this.procedures = new ProcedureSectionBuilder(ctx, gen, errLog);
    }

    public ProcedureSectionBuilder getProcedureSectionBuilder(){
        return this.procedures;
    }

    public DataSectionBuilder getDataSectionBuilder(){
        return this.variables;
    }

    @Override
    public Lib completeBuild() {
        return new Lib(variables.completeBuild(), procedures.completeBuild(), new End());
    }

    @Override
    public void resetBuilder() {
        
    }
}
