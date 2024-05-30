package io.github.H20man13.DeClan.main;

import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
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
import io.github.H20man13.DeClan.common.pat.P;

public class MyIrFactory {
    private ErrorLog errorLog;

    public MyIrFactory(ErrorLog errorLog){
        this.errorLog = errorLog;
    }

    public End produceEnd(){
        return new End();
    }

    public Assign produceVariableAssignment(ICode.Scope scope, String place, String variableName, Assign.Type type){
        return new Assign(scope, place, new IdentExp(variableName), type);
    }

    public Assign produceParamAssignment(String place, String value, Assign.Type type){
        return new Assign(ICode.Scope.PARAM, place, new IdentExp(value), type);
    }

    public Assign produceBooleanAssignment(ICode.Scope scope, String place, boolean trueOrFalse){
        return new Assign(scope, place, new BoolExp(trueOrFalse), Assign.Type.BOOL);
    }

    public Def produceBooleanDefinition(ICode.Scope scope, String place){
        return new Def(scope, place, ICode.Type.BOOL);
    }

    public Assign produceRealAssignment(ICode.Scope scope, String place, float value){
        return new Assign(scope, place, new RealExp(value), Assign.Type.REAL);
    }

    public Def produceRealDefinition(ICode.Scope scope, String place){
        return new Def(scope, place, ICode.Type.REAL);
    }

    public Assign produceIntAssignment(ICode.Scope scope, String place, int value){
        return new Assign(scope, place, new IntExp(value), Assign.Type.INT);
    }

    public Def produceIntDefinition(ICode.Scope scope, String place){
        return new Def(scope, place, ICode.Type.INT);
    }

    public Assign produceStringAssignment(ICode.Scope scope, String place, String value){
        return new Assign(scope, place, new StrExp(value), Assign.Type.STRING);
    }

    public Def produceStringDefinition(ICode.Scope scope, String place){
        return new Def(scope, place, ICode.Type.STRING);
    }

    public Return produceReturnStatement(){
        return new Return();
    }

    public Goto produceGoto(String name){
        return new Goto(name);
    }

    public Call produceProcedure(String funcName, List<Assign> argResults){
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

    public Assign produceUnaryOperation(ICode.Scope scope, String place, UnExp value, Assign.Type type){
        return new Assign(scope, place, value, type);
    }

    public Assign produceBinaryOperation(ICode.Scope scope, String place, BinExp value, Assign.Type type){
        return new Assign(scope, place, value, type);
    }

    public Assign procuceExternalReturnPlacement(String place, String returnPlace, Assign.Type type){
        return new Assign(Scope.EXTERNAL_RETURN, place, new IdentExp(returnPlace), type);
    }

    public Assign produceInternalReturnPlacement(String place, String returnPlace, Assign.Type type){
        return new Assign(Scope.INTERNAL_RETURN, place, new IdentExp(returnPlace), type);
    }

    public Inline produceInlineAssembly(String asm, List<String> param){
        return new Inline(asm, param);
    }
}
