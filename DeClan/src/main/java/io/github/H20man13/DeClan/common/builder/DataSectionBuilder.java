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
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.main.MyIrFactory;

public class DataSectionBuilder implements IrBuilderTemplate<DataSec>{
    private List<ICode> output;
    private IrBuilderContext ctx;
    private IrRegisterGenerator gen;
    private MyIrFactory factory;

    public DataSectionBuilder(IrBuilderContext ctx, IrRegisterGenerator gen, ErrorLog errLog){
        this.output = new LinkedList<ICode>();
        this.ctx = ctx;
        this.gen = gen;
        this.factory = new MyIrFactory(errLog);
    }

    @Override
    public DataSec completeBuild() {
        return new DataSec(output);
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

    public String buildIntegerNotAssignment(Exp value){
        String place = gen.genNextRegister();
        UnExp unExp = new UnExp(Operator.INOT, value);
        output.add(factory.produceUnaryOperation(place, unExp));
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

    public String buildLogicalAndAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LAND, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IOR, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerAndAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IAND, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildIntegerExclusiveOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IXOR, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLogicalOrAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.LOR, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildLeftShiftAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.ILSHIFT, right);
        output.add(factory.produceBinaryOperation(place, binExp));
        return place;
    }

    public String buildRightShiftAssignment(Exp left, Exp right){
        String place = gen.genNextRegister();
        BinExp binExp = new BinExp(left, BinExp.Operator.IRSHIFT, right);
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

    public String buildParamaterAssignment(String value){
        String newPlace = gen.genNextRegister();
        output.add(factory.produceParamAssignment(newPlace, value));
        return newPlace;
    }

    public String buildVariableAssignment(String value){ 
        String place = gen.genNextRegister();
        output.add(factory.produceVariableAssignment(place, value));
        return place;
    }

    public void buildBeginLabel(){ 
        output.add(factory.produceLabel("begin"));
    }

    public void buildBeginGoto(){
        output.add(factory.produceGoto("begin"));
    }

    @Override
    public void resetBuilder() {
        this.output = new LinkedList<ICode>();
    }
}
