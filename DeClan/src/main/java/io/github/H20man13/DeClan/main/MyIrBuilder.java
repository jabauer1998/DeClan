package io.github.H20man13.DeClan.main;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.RegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.If.Op;

public class MyIrBuilder {
    private MyIrFactory factory;
    private RegisterGenerator gen;
    private List<ICode> output;

    private int forLoopNumber;
    private int repeatLoopNumber;
    
    private int ifStatementNumber;
    private int ifStatementSeqNumber;

    private int whileLoopNumber;
    private int whileLoopSeqNumber;

    public MyIrBuilder(ErrorLog errLog){
        this.factory = new MyIrFactory(errLog);
        this.gen = new RegisterGenerator();
        this.output = new LinkedList<>();
        this.forLoopNumber = 0;
        this.repeatLoopNumber = 0;
        this.ifStatementNumber = 0;
        this.ifStatementSeqNumber = 0;
        this.whileLoopNumber = 0;
        this.whileLoopSeqNumber = 0;
    }

    public List<ICode> getOutput(){
        return this.output;
    }

    public void buildEnd(){
        output.add(factory.produceEnd());
    }

    public String buildStringAssignment(String value){
        String place = gen.genNextRegister();
        output.add(factory.produceStringAssignment(place, value));
        return place;
    }

    public String buildBoolAssignment(String value){
        String place = gen.genNextRegister();
        LetBool result = null;
        if(value.equals("TRUE")){
            result = factory.produceBooleanAssignment(place, true);
        } else {
            result = factory.produceBooleanAssignment(place, false);
        }
        output.add(result);
        return place;
    }

    public String buildNumAssignment(String value){
        ICode result = null;
        String place = gen.genNextRegister();
        if(value.contains(".")){
            result = factory.produceIntAssignment(value, Integer.parseInt(value));
        } else {
            result = factory.produceRealAssignment(place, Double.parseDouble(value));
        }
        output.add(result);
        return place;
    }

    public String buildNegationAssignment(String value){
        String place = gen.genNextRegister();
        output.add(factory.produceUnaryOperation(place, LetUn.Op.NEG, value));
        return place;
    }

    public String buildNotAssignment(String value){
        String place = gen.genNextRegister();
        output.add(factory.produceUnaryOperation(place, LetUn.Op.BNOT, value));
        return place;
    }

    public String buildProcedureCall(String funcName, List<String> arguments){
        String place = gen.genNextRegister();
        output.add(factory.produceProcedureCall(place, funcName, arguments));
        return place;
    }

    public String buildAdditionAssignment(String left,  String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.ADD, right));
        return place;
    }

    public String buildSubtractionAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.SUB, right));
        return place;
    }

    public String buildMultiplicationAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.MUL, right));
        return place;
    }

    public String buildDivisionAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.DIV, right));
        return place;
    }

    public String buildModuloAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.MOD, right));
        return place;
    }

    public String buildLessThanOrEqualAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.LE, right));
        return place;
    }

    public String buildLessThanAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.LT, right));
        return place;
    }

    public String buildGreaterThanOrEqualToAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.GE, right));
        return place;
    }

    public String buildGreaterThanAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.GT, right));
        return place;
    }

    public String buildAndAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.BAND, right));
        return place;
    }

    public String buildOrAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.BOR, right));
        return place;
    }

    public String buildEqualityAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.EQ, right));
        return place;
    }

    public String buildInequalityAssignment(String left, String right){
        String place = gen.genNextRegister();
        output.add(factory.produceBinaryOperation(place, left, LetBin.Op.NE, right));
        return place;
    }

    public void buildVariableAssignment(String place, String value){
        output.add(factory.produceVariableAssignment(place, value));
    }

    public String buildVariableAssignment(String value){ 
        String place = gen.genNextRegister();
        output.add(factory.produceVariableAssignment(place, value));
        return place;
    }

    public void buildForLoopBeginning(String currentValue, String target){
        output.add(factory.produceLabel("FORBEG_" + forLoopNumber));
        output.add(factory.produceIfStatement(currentValue, Op.NE, target, "FORLOOP_" + forLoopNumber, "FOREND_" + forLoopNumber));
        output.add(factory.produceLabel("FORLOOP_" + forLoopNumber));
    }

    public void buildForLoopEnd(){
        output.add(factory.produceGoto("FORBEG_" + forLoopNumber));
        output.add(factory.produceLabel("FOREND_" + forLoopNumber));
        forLoopNumber++;
    }

    public void buildRepeatLoopBeginning(String exprResult){
        String exprPlace = this.buildNumAssignment("0");
        output.add(factory.produceLabel("REPEATBEG_" + repeatLoopNumber));
        output.add(factory.produceIfStatement(exprPlace, Op.LT, exprResult, "REPEATLOOP_" + repeatLoopNumber, "REPEATEND_" + repeatLoopNumber));
        output.add(factory.produceLabel("REPEATLOOP_" + repeatLoopNumber));
    }

    public void buildRepeatLoopEnd(){
        output.add(factory.produceLabel("REPEATEND_" + repeatLoopNumber));
        repeatLoopNumber++;
    }

    public void buildIfStatementBeginning(String test){
        output.add(factory.produceIfStatement(test, "IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber, "IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
        output.add(factory.produceLabel("IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
    }

    public void buildElseIfStatementBeginning(){
        output.add(factory.produceGoto("IFEND_" + ifStatementNumber));
        output.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
        ifStatementSeqNumber++;
    }

    public void buildIfStatementEnd(){
        output.add(factory.produceGoto("IFEND_" + ifStatementNumber));
        output.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber));
        output.add(factory.produceLabel("IFEND_" + ifStatementNumber));
        ifStatementNumber++;
        ifStatementSeqNumber = 0;
    }

    public void buildWhileLoopBeginning(String test){
        output.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber, "WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber, "WHILEEND_" + whileLoopNumber));
        output.add(factory.produceLabel("WHILESTAT_" +  whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
    }

    public void buildElseWhileLoopBeginning(){
        output.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        whileLoopSeqNumber++;
    }

    public void buildWhileLoopEnd(){
        output.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber));
        output.add(factory.produceLabel("WHILEEND_" + whileLoopNumber));
        whileLoopNumber++;
        whileLoopSeqNumber = 0;
    }

    public void buildProcedure(String name, List<String> args){
        output.add(factory.produceProcedure(name, args));
    }

    public void buildProcedureDeclaration(String name){
        output.add(factory.produceLabel(name));
    }

    public void buildReturnStatement(){
        output.add(factory.produceReturnStatement());
    }
}
