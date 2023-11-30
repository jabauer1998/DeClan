package io.github.H20man13.DeClan.main;

import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.BinExp.Operator;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.label.StandardLabel;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.InternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;
import io.github.H20man13.DeClan.common.pat.P;

public class MyIrFactory {
    private ErrorLog errorLog;

    public MyIrFactory(ErrorLog errorLog){
        this.errorLog = errorLog;
    }

    public End produceEnd(){
        return new End();
    }

    public Assign produceVariableAssignment(String place, String variableName){
        return new Assign(place, new IdentExp(variableName));
    }

    public ParamAssign produceParamAssignment(String place, String value){
        return new ParamAssign(place, value);
    }

    public Assign produceBooleanAssignment(String place, boolean trueOrFalse){
        return new Assign(place, new BoolExp(trueOrFalse));
    }

    public Assign produceRealAssignment(String place, double value){
        return new Assign(place, new RealExp(value));
    }

    public Assign produceIntAssignment(String place, int value){
        return new Assign(place, new IntExp(value));
    }

    public Assign produceStringAssignment(String place, String value){
        return new Assign(place, new StrExp(value));
    }

    public Return produceReturnStatement(){
        return new Return();
    }

    public Goto produceGoto(String name){
        return new Goto(name);
    }

    public Call produceProcedure(String funcName, List<Tuple<String, String>> argResults){
        return new Call(funcName, argResults);
    }

    public If produceIfStatement(Exp test, String ifTrue, String ifFalse){
        BoolExp trueExp = new BoolExp(true);
        BinExp bExp = new BinExp(test, Operator.EQ, trueExp);
        return new If(bExp, ifTrue, ifFalse);
    }

    public If produceIfStatement(BinExp exp, String ifTrue, String ifFalse){
        return new If(exp, ifTrue, ifFalse);
    }

    public StandardLabel produceLabel(String name){
        return new StandardLabel(name);
    }

    public ProcLabel produceProcedureLabel(String name){
        return new ProcLabel(name);
    }

    public Assign produceUnaryOperation(String place, UnExp value){
        return new Assign(place, value);
    }

    public Assign produceBinaryOperation(String place, BinExp value){
        return new Assign(place, value);
    }

    public ExternalPlace procuceExternalReturnPlacement(String place, String returnPlace){
        return new ExternalPlace(place, returnPlace);
    }

    public InternalPlace produceInternalReturnPlacement(String place, String returnPlace){
        return new InternalPlace(place, returnPlace);
    }

    public Inline produceInlineAssembly(String asm, List<String> param){
        return new Inline(asm, param);
    }
}
