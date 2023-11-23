package io.github.H20man13.DeClan.common.builder;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;

public class ProgramBuilder implements CompletableBuilder<Prog> {
    private IrBuilderContext ctx;
    private DataSectionBuilder variables;
    private ProcedureSectionBuilder procedures;
    private CodeSectionBuilder code;
    private IrRegisterGenerator gen;

    public ProgramBuilder(IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        this.ctx = ctx;
        this.variables = new DataSectionBuilder(ctx, gen, errLog);
        this.procedures = new ProcedureSectionBuilder(ctx, gen, errLog);
        this.code = new CodeSectionBuilder(ctx, gen, errLog);
    }

    public ProcedureSectionBuilder getProcedureSectionBuilder(){
        return this.procedures;
    }

    public DataSectionBuilder getDataSectionBuilder(){
        return this.variables;
    }

    public CodeSectionBuilder getCodeSectionBuilder(){
        return this.code;
    }

    @Override
    public Prog completeBuild() {
        return new Prog(variables.completeBuild(), procedures.completeBuild(), code.completeBuild(), new End());
    }
}
