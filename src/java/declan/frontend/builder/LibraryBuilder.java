package declan.frontend.builder;

import declan.frontend.builder.template.CompletableBuilder;
import declan.frontend.IrRegisterGenerator;
import declan.middleware.icode.Lib;
import declan.middleware.icode.section.DataSec;
import declan.middleware.icode.section.ProcSec;
import declan.middleware.icode.section.SymSec;

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
