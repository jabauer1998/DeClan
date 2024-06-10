package io.github.H20man13.DeClan.common.builder;

import java.util.Collections;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;

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
