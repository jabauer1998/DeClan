package io.github.H20man13.DeClan.common.builder;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.section.SymbolSectionBuilder;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.main.MyIrFactory;

public abstract class StatementBuilder extends AssignmentBuilder implements ResetableBuilder{
    private IrBuilderContext ctx;
    private IrRegisterGenerator gen;
    private MyIrFactory factory;
    
    protected StatementBuilder(SymbolSectionBuilder symbols, IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog) {
        super(symbols, ctx, gen, errLog);
        this.ctx = ctx;
        this.gen = gen;
        this.intermediateCode = new LinkedList<ICode>();
        this.factory = new MyIrFactory(errLog);
    }

    public void incrimentForLoopLevel(){
        ctx.incrimentForLoopLevel();
    }

    public void deIncrimentForLoopLevel(){
        ctx.deIncrimentForLoopLevel();
    }

    public void buildForLoopBeginning(Exp currentValue, BinExp.Operator op, Exp target){
        int forLoopNumber = ctx.getForLoopNumber();
        int forLoopLevel = ctx.getForLoopLevel();
        intermediateCode.add(factory.produceLabel("FORBEG_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        BinExp bExp = new BinExp(currentValue, op, target);
        intermediateCode.add(factory.produceIfStatement(bExp, "FORLOOP_" + forLoopNumber + "_LEVEL_" + forLoopLevel, "FOREND_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        intermediateCode.add(factory.produceLabel("FORLOOP_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
    }

    public void buildForLoopEnd(){
        int forLoopNumber = ctx.getForLoopNumber();
        int forLoopLevel = ctx.getForLoopLevel();
        intermediateCode.add(factory.produceGoto("FORBEG_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        intermediateCode.add(factory.produceLabel("FOREND_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        ctx.incrimentForLoopNumber();
    }

    public void incrimentRepeatLoopLevel(){
        ctx.incrimentRepeatLoopLevel();
    }

    public void deIncrimentRepeatLoopLevel(){
        ctx.deIncrimentRepeatLoopLevel();
    }

    public void buildRepeatLoopBeginning(String exprResult){
        int repeatLoopLevel = ctx.getRepeatLoopLevel();
        int repeatLoopNumber = ctx.getRepeatLoopNumber();
        IdentExp identExp = new IdentExp(exprResult);
        intermediateCode.add(factory.produceLabel("REPEATBEG_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        intermediateCode.add(factory.produceIfStatement(identExp, "REPEATEND_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel, "REPEATLOOP_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        intermediateCode.add(factory.produceLabel("REPEATLOOP_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
    }

    public void buildRepeatLoopEnd(){
        int repeatLoopLevel = ctx.getRepeatLoopLevel();
        int repeatLoopNumber = ctx.getRepeatLoopNumber();
        intermediateCode.add(factory.produceGoto("REPEATBEG_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        intermediateCode.add(factory.produceLabel("REPEATEND_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        ctx.incrimentRepeatLoopNumber();
    }

    public void incrimentIfStatementLevel(){
        ctx.incrimentIfStatementLevel();
    }

    public void deIncrimentIfStatementLevel(){
        ctx.deIncrimentIfStatementLevel();
    }

    public void buildIfStatementBeginning(IdentExp test){
        int ifStatementLevel = ctx.getIfStatementLevel();
        int ifStatementNumber = ctx.getIfStatementNumber();
        int ifStatementSeqNumber = ctx.getIfStatementSeqNumber();
        intermediateCode.add(factory.produceIfStatement(test, "IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel, "IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        intermediateCode.add(factory.produceLabel("IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
    }

    public void buildElseIfStatementBeginning(){
        int ifStatementLevel = ctx.getIfStatementLevel();
        int ifStatementNumber = ctx.getIfStatementNumber();
        int ifStatementSeqNumber = ctx.getIfStatementSeqNumber();
        intermediateCode.add(factory.produceGoto("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        intermediateCode.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        ctx.incrimentIfStatementSeqNumber();
    }

    public void buildIfStatementEnd(){
        int ifStatementLevel = ctx.getIfStatementLevel();
        int ifStatementNumber = ctx.getIfStatementNumber();
        int ifStatementSeqNumber = ctx.getIfStatementSeqNumber();
        intermediateCode.add(factory.produceGoto("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        intermediateCode.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        intermediateCode.add(factory.produceLabel("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        ctx.incrimentIfStatementNumber();
    }

    public void incrimentWhileLoopLevel(){
        ctx.incrimentWhileLoopLevel();
    }

    public void deIncrimentWhileLoopLevel(){
        ctx.deIncrimentWhileLoopLevel();
    }

    public void buildWhileLoopBeginning(Exp test){
        int whileLoopLevel = ctx.getWhileLoopLevel();
        int whileLoopNumber = ctx.getWhileLoopNumber();
        int whileLoopSeqNumber = ctx.getWhileLoopSeqNumber();
        intermediateCode.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel, "WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        intermediateCode.add(factory.produceLabel("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        intermediateCode.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel, "WHILEEND_" + whileLoopNumber + "_LEVEL_" + whileLoopLevel));
        intermediateCode.add(factory.produceLabel("WHILESTAT_" +  whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
    }

    public void buildElseWhileLoopBeginning(){
        int whileLoopNumber = ctx.getWhileLoopNumber();
        int whileLoopLevel = ctx.getWhileLoopLevel();
        int whileLoopSeqNumber = ctx.getWhileLoopSeqNumber();
        intermediateCode.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        intermediateCode.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        ctx.incrimentWhileLoopSeqNumber();
    }

    public void buildWhileLoopEnd(){
        int whileLoopNumber = ctx.getWhileLoopNumber();
        int whileLoopSeqNumber = ctx.getWhileLoopSeqNumber();
        int whileLoopLevel = ctx.getWhileLoopLevel();
        intermediateCode.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        intermediateCode.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        intermediateCode.add(factory.produceLabel("WHILEEND_" + whileLoopNumber + "_LEVEL_" + whileLoopLevel));
        ctx.incrimentWhileLoopNumber();
    }

    public void buildLabel(String label){
        intermediateCode.add(factory.produceLabel(label));
    }

    public void buildGoto(String label){
        intermediateCode.add(factory.produceGoto(label));
    }

    public void buildInlineAssembly(String inlineAssembly, List<String> param){
        intermediateCode.add(factory.produceInlineAssembly(inlineAssembly, param));
    }

    public abstract String buildParamaterAssignment(String place, Assign.Type type);
}
