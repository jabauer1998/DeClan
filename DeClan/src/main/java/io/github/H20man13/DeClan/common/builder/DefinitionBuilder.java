package io.github.H20man13.DeClan.common.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;

public class DefinitionBuilder extends SymbolBuilder{
    protected IrRegisterGenerator gen;

    protected DefinitionBuilder(IrRegisterGenerator gen){
        super();
        this.gen = gen;
    }

    public IdentExp buildDefinition(ICode.Scope scope, Exp value, ICode.Type type){
        String place = gen.genNext();
        addInstruction(new Def(scope, place, value, type));
        return new IdentExp(scope, place);
    }

    public IdentExp buildBinaryDefinition(ICode.Scope scope, IdentExp left, BinExp.Operator op,  IdentExp right, ICode.Type type){
        String place = gen.genNext();
        addInstruction(new Def(scope, place, new BinExp(left, op, right), type));
        return new IdentExp(scope, place);
    }

    public IdentExp buildUnaryDefinition(ICode.Scope scope, UnExp.Operator op, IdentExp right, ICode.Type type){
        String place = gen.genNext();
        addInstruction(new Def(scope, place, new UnExp(op, right), type));
        return new IdentExp(scope, place);
    }

    public void buildDefinition(ICode.Scope scope, String place, Exp value, ICode.Type type){
        addInstruction(new Def(scope, place, value, type));
    }

    public IdentExp buildParamaterDefinition(String funcName, Exp value, ICode.Type type){
        int paramDefinitionEnd = this.endOfParamAssign(funcName) + 1;
        String place = gen.genNext();
        addInstruction(paramDefinitionEnd, new Def(Scope.PARAM, place, value, type));
        return new IdentExp(Scope.PARAM, place);
    }

    public IdentExp buildFunctionCall(String funcName, List<Def> params, Exp retPlace, ICode.Type type){
        String place = gen.genNext();
        addInstruction(new Call(funcName, params));
        addInstruction(new Def(Scope.LOCAL, place, retPlace, type));
        return new IdentExp(Scope.LOCAL, place);
    }

    public IdentExp buildExternalFunctionCall(ICode.Scope scope, String funcName, List<Tuple<Exp, Assign.Type>> args, ICode.Type type){
        ArrayList<Tuple<Exp, Assign.Type>> newArgs = new ArrayList<Tuple<Exp, Assign.Type>>();
        newArgs.addAll(args);
        List<Def> newDefs = new LinkedList<Def>();
        for(int i = 0; i < newArgs.size(); i++){
            Tuple<Exp, Assign.Type> arg = newArgs.get(i);
            String next;
            if(containsExternalArgument(funcName, i))
                next = getArgumentPlace(funcName, i);
            else {
                next = gen.genNext();
                addParamEntry(next, SymEntry.EXTERNAL, funcName, i);
            }
            newDefs.add(new Def(Scope.PARAM, next, arg.source, arg.dest));
        }
        addInstruction(new Call(funcName, newDefs));
        String oldPlace;
        if(containsExternalReturn(funcName))
            oldPlace = getReturnPlace(funcName);
        else {
            oldPlace = gen.genNext();
            addReturnEntry(oldPlace, SymEntry.EXTERNAL, funcName);
        }
        String place = gen.genNext();
        addInstruction(new Def(Scope.LOCAL, place, new IdentExp(ICode.Scope.RETURN, oldPlace), type));
        return new IdentExp(Scope.LOCAL, place);
    }
}
