package io.github.H20man13.DeClan.main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class MyIrBuilder {
    private MyIrFactory factory;
    private IrRegisterGenerator gen;
    private List<ICode> output;

    private int forLoopNumber;
    private int forLoopLevel;
    private Stack<Integer> forLoopNumberStack;
    
    private int repeatLoopNumber;
    private int repeatLoopLevel;
    private Stack<Integer> repeatLoopNumberStack;
    
    private int nextIfStatementNumber;
    private int ifStatementNumber;
    private int ifStatementSeqNumber;
    private int ifStatementLevel;
    private Stack<Integer> ifStatementNumberStack;
    private Stack<Integer> ifStatementSeqNumberStack;

    private int nextWhileLoopNumber;
    private int whileLoopNumber;
    private int whileLoopSeqNumber;
    private int whileLoopLevel;
    private Stack<Integer> whileLoopNumberStack;
    private Stack<Integer> whileLoopSeqNumberStack;


    private int beginSeqNumber;

    public MyIrBuilder(ErrorLog errLog){
        this(errLog, new IrRegisterGenerator());
    }

    public MyIrBuilder(ErrorLog errLog, IrRegisterGenerator generator){
        this.factory = new MyIrFactory(errLog);
        this.gen = generator;
        this.output = new LinkedList<>();
        this.forLoopNumber = 0;
        this.repeatLoopNumber = 0;
        this.ifStatementNumber = 0;
        this.ifStatementSeqNumber = 0;
        this.whileLoopNumber = 0;
        this.whileLoopSeqNumber = 0;
        this.ifStatementLevel = 0;
        this.whileLoopLevel = 0;
        this.forLoopLevel = 0;
        this.repeatLoopLevel = 0;
        this.nextIfStatementNumber = 1;
        this.nextWhileLoopNumber = 1;
        this.forLoopNumberStack = new Stack<Integer>();
        this.repeatLoopNumberStack = new Stack<Integer>();
        this.ifStatementNumberStack = new Stack<Integer>();
        this.ifStatementSeqNumberStack = new Stack<Integer>();
        this.whileLoopNumberStack = new Stack<Integer>();
        this.whileLoopSeqNumberStack = new Stack<Integer>();
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
        Assign result = null;
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
            result = factory.produceRealAssignment(place, Double.parseDouble(value));
        } else {
            result = factory.produceIntAssignment(place, Integer.parseInt(value));
        }
        output.add(result);
        return place;
    }

    public String buildRealNegationAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unaryExp = new UnExp(UnExp.Operator.RNEG, value);
        output.add(factory.produceUnaryOperation(place, unaryExp));
        return place;
    }

    public String buildIntegerNegationAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unaryExp = new UnExp(UnExp.Operator.INEG, value);
        output.add(factory.produceUnaryOperation(place, unaryExp));
        return place;
    }

    public String buildNotAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unExp = new UnExp(UnExp.Operator.BNOT, value);
        output.add(factory.produceUnaryOperation(place, unExp));
        return place;
    }

    public String buildIntegerAdditionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IADD ,right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRealAdditionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RADD ,right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerSubtractionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.ISUB, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRealSubtractionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RSUB, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerMultiplicationAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IMUL, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRealMultiplicationAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RMUL, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerDivisionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IDIVIDE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerDivAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IDIV, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    

    public String buildRealDivisionAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RDIVIDE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRealDivAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.RDIV, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerModuloAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IMOD, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLessThanOrEqualAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLessThanAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LT, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildGreaterThanOrEqualToAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.GE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildGreaterThanAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.GT, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildAndAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.BAND, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.BOR, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildEqualityAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.EQ, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildInequalityAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.NE, right);
        output.add(factory.produceBinaryOperation(place, binExp));
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

    public void incrimentForLoopLevel(){
        this.forLoopNumberStack.push(forLoopNumber);
        this.forLoopNumber = 0;
        forLoopLevel++;
    }

    public void deIncrimentForLoopLevel(){
        this.forLoopNumber = this.forLoopNumberStack.pop();
        forLoopLevel--;
    }

    public void buildForLoopBeginning(Exp currentValue, Exp target){
        output.add(factory.produceLabel("FORBEG_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        BinExp bExp = new BinExp(currentValue, BinExp.Operator.NE, target);
        output.add(factory.produceIfStatement(bExp, "FORLOOP_" + forLoopNumber + "_LEVEL_" + forLoopLevel, "FOREND_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        output.add(factory.produceLabel("FORLOOP_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
    }

    public void buildForLoopEnd(){
        output.add(factory.produceGoto("FORBEG_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        output.add(factory.produceLabel("FOREND_" + forLoopNumber + "_LEVEL_" + forLoopLevel));
        forLoopNumber++;
    }

    public void incrimentRepeatLoopLevel(){
        this.repeatLoopNumberStack.push(repeatLoopNumber);
        repeatLoopNumber = 0;
        repeatLoopLevel++;
    }

    public void deIncrimentRepeatLoopLevel(){
        this.repeatLoopNumber = repeatLoopNumberStack.pop();
        repeatLoopLevel--;
    }

    public void buildRepeatLoopBeginning(String exprResult){
        String exprPlace = this.buildNumAssignment("0");
        IdentExp identExp = new IdentExp(exprPlace);
        output.add(factory.produceLabel("REPEATBEG_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        output.add(factory.produceIfStatement(identExp, "REPEATLOOP_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel, "REPEATEND_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
        output.add(factory.produceLabel("REPEATLOOP_" + repeatLoopNumber + "_LEVEL_" + repeatLoopLevel));
    }

    public void buildRepeatLoopEnd(){
        output.add(factory.produceLabel("REPEATEND_" + repeatLoopNumber));
        repeatLoopNumber++;
    }

    public void incrimentIfStatementLevel(){
        this.ifStatementNumberStack.push(ifStatementNumber);
        this.ifStatementSeqNumberStack.push(ifStatementSeqNumber);
        this.ifStatementNumber = nextIfStatementNumber;
        this.nextIfStatementNumber++;
        this.ifStatementSeqNumber = 0;
        ifStatementLevel++;
    }

    public void deIncrimentIfStatementLevel(){
        this.ifStatementNumber = this.ifStatementNumberStack.pop();
        this.ifStatementSeqNumber = this.ifStatementSeqNumberStack.pop();
        if((this.ifStatementNumber + 2) == nextIfStatementNumber){
            this.nextIfStatementNumber--;
        }
        ifStatementLevel--;
    }

    public void buildIfStatementBeginning(IdentExp test){
        output.add(factory.produceIfStatement(test, "IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel, "IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        output.add(factory.produceLabel("IFSTAT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
    }

    public void buildElseIfStatementBeginning(){
        output.add(factory.produceGoto("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        output.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        ifStatementSeqNumber++;
    }

    public void buildIfStatementEnd(){
        output.add(factory.produceGoto("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        output.add(factory.produceLabel("IFNEXT_" + ifStatementNumber + "_SEQ_" + ifStatementSeqNumber + "_LEVEL_" + ifStatementLevel));
        output.add(factory.produceLabel("IFEND_" + ifStatementNumber + "_LEVEL_" + ifStatementLevel));
        if(nextIfStatementNumber > ifStatementNumber+1){
            ifStatementNumber = nextIfStatementNumber;
            nextIfStatementNumber++;
        } else {
            ifStatementNumber++;
            nextIfStatementNumber++;
        }
        ifStatementSeqNumber = 0;
    }

    public void incrimentWhileLoopLevel(){
        this.whileLoopNumberStack.push(this.whileLoopNumber);
        this.whileLoopSeqNumberStack.push(this.whileLoopSeqNumber);
        this.whileLoopNumber = this.nextWhileLoopNumber;
        this.nextWhileLoopNumber++;
        this.whileLoopSeqNumber = 0;
        whileLoopLevel++;
    }

    public void deIncrimentWhileLoopLevel(){
        this.whileLoopNumber = this.whileLoopNumberStack.pop();
        this.whileLoopSeqNumber = this.whileLoopSeqNumberStack.pop();
        if((this.whileLoopNumber + 2) == this.whileLoopNumber){
            this.nextWhileLoopNumber--;
        }
        whileLoopLevel--;
    }

    public void buildWhileLoopBeginning(Exp test){
        output.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel, "WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        output.add(factory.produceLabel("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        output.add(factory.produceIfStatement(test, "WHILESTAT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel, "WHILEEND_" + whileLoopNumber + "_LEVEL_" + whileLoopLevel));
        output.add(factory.produceLabel("WHILESTAT_" +  whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
    }

    public void buildElseWhileLoopBeginning(){
        output.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        output.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        whileLoopSeqNumber++;
    }

    public void buildWhileLoopEnd(){
        output.add(factory.produceGoto("WHILECOND_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        output.add(factory.produceLabel("WHILENEXT_" + whileLoopNumber + "_SEQ_" + whileLoopSeqNumber + "_LEVEL_" + whileLoopLevel));
        output.add(factory.produceLabel("WHILEEND_" + whileLoopNumber + "_LEVEL_" + whileLoopLevel));
        if(nextWhileLoopNumber > whileLoopNumber+1){
            whileLoopNumber = nextWhileLoopNumber;
            nextWhileLoopNumber++;
        } else {
            whileLoopNumber++;
            nextWhileLoopNumber++;
        }
        whileLoopSeqNumber = 0;
    }

    public void buildProcedure(String name, List<Tuple<String, String>> args){
        output.add(factory.produceProcedure(name, args));
    }

    public void buildProcedureDeclaration(String name){
        output.add(factory.produceLabel(name));
    }

    public void buildReturnStatement(){
        output.add(factory.produceReturnStatement());
    }

    public void buildLabel(String label){
        output.add(factory.produceLabel(label));
    }

    public void buildGoto(String label){
        output.add(factory.produceGoto(label));
    }

    public String buildAlias(){
        return gen.genNextRegister();
    }

    public String buildReturnPlacement(String dest){
        String reg = gen.genNextRegister();
        output.add(factory.procuceReturnPlacement(reg, dest));
        return reg;
    }

    public void buildBeginLabel(){
        String begin = "begin_" + beginSeqNumber; 
        output.add(factory.produceLabel(begin));
        beginSeqNumber++;
    }

    public void buildBeginGoto(){
        String begin = "begin_" + beginSeqNumber;
        output.add(factory.produceGoto(begin));
    }
}
