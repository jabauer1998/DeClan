package io.github.H20man13.DeClan.common.builder;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.main.MyIrFactory;

public class CodeSectionBuilder implements IrBuilderTemplate<CodeSec> {
    private List<ICode> intermediateCode;
    private IrBuilderContext ctx;
    private IrRegisterGenerator gen;
    private MyIrFactory factory;
    private ErrorLog errLog;

    public CodeSectionBuilder(IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        this.ctx = ctx;
        this.gen = gen;
        this.errLog = errLog;
        this.factory = new MyIrFactory(errLog);
        resetBuilder();
    }

    @Override
    public CodeSec completeBuild() {
        CodeSec section = new CodeSec(intermediateCode);
        resetBuilder();
        return section;
    }

    @Override
    public void resetBuilder() {
        this.intermediateCode = new LinkedList<ICode>();
    }

    public String buildStringAssignment(String value){
        String place = gen.genNextRegister();
        intermediateCode.add(factory.produceStringAssignment(place, value));
        return place;
    }

    public String buildBoolAssignment(String value){
        String place = gen.genNextRegister();
        Assign result = null;
        if(value.equals("TRUE")){
            result = factory.produceBooleanAssignment(place, true);
        } else {
            result = factory.produceBooleanAssignment(place, false);
        }
        intermediateCode.add(result);
        return place;
    }

    public String buildNumAssignment(String value){
        ICode result = null;
        String place = gen.genNextRegister();
        if(value.contains(".")){
            result = factory.produceRealAssignment(place, Double.parseDouble(value));
        } else {
            result = factory.produceIntAssignment(place, Integer.parseInt(value));
        }
        intermediateCode.add(result);
        return place;
    }

    public String buildRealNegationAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unaryExp = new UnExp(UnExp.Operator.RNEG, value);
        intermediateCode.add(factory.produceUnaryOperation(place, unaryExp));
        return place;
    }

    public String buildIntegerNegationAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unaryExp = new UnExp(UnExp.Operator.INEG, value);
        intermediateCode.add(factory.produceUnaryOperation(place, unaryExp));
        return place;
    }

    public String buildIntegerNotAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unExp = new UnExp(Operator.INOT, value);
        intermediateCode.add(factory.produceUnaryOperation(place, unExp));
        return place;
    }

    public String buildNotAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unExp = new UnExp(UnExp.Operator.BNOT, value);
        intermediateCode.add(factory.produceUnaryOperation(place, unExp));
        return place;
    }

    public String buildIntegerAdditionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IADD ,right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRealAdditionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RADD ,right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerSubtractionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.ISUB, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRealSubtractionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RSUB, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerMultiplicationAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IMUL, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRealMultiplicationAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RMUL, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerDivAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IDIV, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    

    public String buildRealDivisionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RDIVIDE, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerModuloAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IMOD, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLessThanOrEqualAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LE, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLessThanAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LT, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildGreaterThanOrEqualToAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.GE, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildGreaterThanAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.GT, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLogicalAndAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LAND, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IOR, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerAndAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IAND, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerExclusiveOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IXOR, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLogicalOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LOR, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLeftShiftAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.ILSHIFT, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRightShiftAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IRSHIFT, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildEqualityAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.EQ, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildInequalityAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.NE, right);
        intermediateCode.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public void buildVariableAssignment(String place, String value){
        intermediateCode.add(factory.produceVariableAssignment(place, value));
    }

    public String buildVariableAssignment(String value){ 
        String place = gen.genNextRegister();
        intermediateCode.add(factory.produceVariableAssignment(place, value));
        return place;
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

    public void incrimentRepeatLoopLavel(){
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
        ctx.deIncrimentIfStatementLevel();
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

    public void buildProcedureCall(String name, List<Tuple<String, String>> args){
        intermediateCode.add(factory.produceProcedure(name, args));
    }

    public void buildLabel(String label){
        intermediateCode.add(factory.produceLabel(label));
    }

    public void buildGoto(String label){
        intermediateCode.add(factory.produceGoto(label));
    }

    public String buildExternalReturnPlacement(String dest){
        String reg = gen.genNextRegister();
        intermediateCode.add(factory.procuceExternalReturnPlacement(reg, dest));
        return reg;
    }

    public void buildInlineAssembly(String inlineAssembly, List<String> param){
        intermediateCode.add(factory.produceInlineAssembly(inlineAssembly, param));
    }
}
