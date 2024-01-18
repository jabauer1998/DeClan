package io.github.H20man13.DeClan.common.builder;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.builder.section.DataSectionBuilder;
import io.github.H20man13.DeClan.common.builder.section.ProcedureSectionBuilder;
import io.github.H20man13.DeClan.common.builder.section.SymbolSectionBuilder;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;

public class LibraryBuilder implements CompletableBuilder<Lib> {
    private IrBuilderContext ctx;
    private DataSectionBuilder variables;
    private ProcedureSectionBuilder procedures;
    private SymbolSectionBuilder symbols;
    private IrRegisterGenerator gen;

    public LibraryBuilder(IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        this.ctx = ctx;
        this.symbols = new SymbolSectionBuilder(errLog);
        this.variables = new DataSectionBuilder(symbols, ctx, gen, errLog);
        this.procedures = new ProcedureSectionBuilder(symbols, ctx, gen, errLog);
    }

    public ProcedureSectionBuilder getProcedureSectionBuilder(){
        return this.procedures;
    }

    public DataSectionBuilder getDataSectionBuilder(){
        return this.variables;
    }

    @Override
    public Lib completeBuild() {
        return new Lib(symbols.completeBuild(), variables.completeBuild(), procedures.completeBuild());
    }
}
