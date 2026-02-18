package declan.frontend.builder;

import java.util.Collections;

import declan.utils.ErrorLog;
import declan.frontend.builder.template.CompletableBuilder;
import declan.frontend.IrRegisterGenerator;
import declan.middleware.icode.End;
import declan.middleware.icode.Lib;
import declan.middleware.icode.Prog;
import declan.middleware.icode.section.BssSec;
import declan.middleware.icode.section.CodeSec;
import declan.middleware.icode.section.DataSec;
import declan.middleware.icode.section.ProcSec;
import declan.middleware.icode.section.SymSec;

public class ProgramBuilder extends StatementBuilder implements CompletableBuilder<Prog> {
    public ProgramBuilder(IrBuilderContext ctx, IrRegisterGenerator gen){
        super(ctx, gen);
    }

    public void buildCodeSectionHeader(){
        addInstruction(new CodeSec());
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

    public void buildBssSectionHeader(){
        addInstruction(new BssSec());
    }

    public void buildCodeSectionEnd(){
        addInstruction(new End());
    }

    @Override
    public Prog completeBuild() {
        return new Prog(this.getInstructions());
    }
}
