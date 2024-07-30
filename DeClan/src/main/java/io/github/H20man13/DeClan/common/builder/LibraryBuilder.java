package io.github.H20man13.DeClan.common.builder;

import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;

public class LibraryBuilder extends StatementBuilder implements CompletableBuilder<Lib> {

    public LibraryBuilder(IrBuilderContext ctx, IrRegisterGenerator gen){
        super(ctx, gen);
    }

    public void buildDataSectionHeader(){
        addInstruction(new DataSec());
    }

    public void buildSymbolSectionHeader(){
        addInstruction(new SymSec());
    }

    public void buildProcedureSectionHeader(){
        addInstruction(new ProcSec());
    }

    @Override
    public Lib completeBuild() {
        return new Lib(this.getInstructions());
    }
}
