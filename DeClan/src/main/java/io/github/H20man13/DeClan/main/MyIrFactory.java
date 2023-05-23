package io.github.H20man13.DeClan.main;

import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.BinExp.Operator;

public class MyIrFactory {
    private ErrorLog errorLog;

    public MyIrFactory(ErrorLog errorLog){
        this.errorLog = errorLog;
    }

    public End produceEnd(){
        return new End();
    }

    public LetVar produceVariableAssignment(String place, String variableName){
        return new LetVar(place, variableName);
    }

    public LetBool produceBooleanAssignment(String place, boolean trueOrFalse){
        return new LetBool(place, trueOrFalse);
    }

    public LetReal produceRealAssignment(String place, double value){
        return new LetReal(place, value);
    }

    public LetInt produceIntAssignment(String place, int value){
        return new LetInt(place, value);
    }

    public LetString produceStringAssignment(String place, String value){
        return new LetString(place, value);
    }

    public Return produceReturnStatement(){
        return new Return();
    }

    public Goto produceGoto(String name){
        return new Goto(name);
    }

    public Proc produceProcedure(String funcName, List<String> argResults){
        return new Proc(funcName, argResults);
    }

    public If produceIfStatement(Exp test, String ifTrue, String ifFalse){
        BoolExp trueExp = new BoolExp(true);
        BinExp bExp = new BinExp(test, Operator.EQ, trueExp);
        return new If(bExp, ifTrue, ifFalse);
    }

    public If produceIfStatement(BinExp exp, String ifTrue, String ifFalse){
        return new If(exp, ifTrue, ifFalse);
    }

    public Label produceLabel(String name){
        return new Label(name);
    }

    public LetUn produceUnaryOperation(String place, UnExp value){
        return new LetUn(place, value);
    }

    public LetBin produceBinaryOperation(String place, BinExp value){
        return new LetBin(place, value);
    }

    public Call produceProcedureCall(String place, String procedureName, List<String> arguments){
        return new Call(place, procedureName, arguments);
    }
}
