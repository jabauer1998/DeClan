package io.github.H20man13.DeClan.common.builder.section;

import java.util.LinkedList;
import java.util.List;

import javax.xml.crypto.Data;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.AssignmentBuilder;
import io.github.H20man13.DeClan.common.builder.IrBuilderContext;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.main.MyIrFactory;

public class DataSectionBuilder extends AssignmentBuilder implements CompletableBuilder<DataSec>{

    public DataSectionBuilder(SymbolSectionBuilder symbols, IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        super(symbols, ctx, gen, errLog);
    }

    @Override
    public DataSec completeBuild() {
        return new DataSec(intermediateCode);
    }

    @Override
    public String buildParamaterAssignment(String place) {
        return null;
    }
}
