package io.github.H20man13.DeClan.main;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalCall;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.InternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.IntEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.OpUtil;
import io.github.H20man13.DeClan.common.util.Utils;

public class MyICodeMachine {
    private Environment<String, IntEntry> procLabelAddresses;
    private Environment<String, IntEntry> labelAddresses;
    private Environment<String, VariableEntry> variableValues;
    private Stack<Integer> returnStack;
    private int programCounter;
    private State machineState;
    private ErrorLog errLog;
    private Writer standardOutput;
    private Writer standardError;
    private Reader standardIn;
    private Object tempReturnValue;

    public MyICodeMachine(ErrorLog errLog, Writer standardOutput, Writer standardError, Reader standardIn){
        this.labelAddresses = new Environment<String, IntEntry>();
        this.procLabelAddresses = new Environment<String, IntEntry>();
        this.returnStack = new Stack<Integer>();
        this.variableValues = new Environment<String, VariableEntry>();
        this.programCounter = 0;
        this.errLog = errLog;
        this.machineState = State.INIT;
        this.labelAddresses.addScope();
        this.variableValues.addScope();
        this.standardError = standardError;
        this.standardOutput = standardOutput;
        this.standardIn = standardIn;
        this.tempReturnValue = null;
    }

    private enum State{
        RETURN,
        INIT,
    }

    public void interpretICode(Prog program){
        List<ICode> icode = program.genFlatCode();
        interpretICode(icode);
    }

    public void interpretICode(List<ICode> instructions){
        this.labelAddresses.addScope();
        this.procLabelAddresses.addScope();
        this.variableValues.addScope();
        for(int i = 0; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof Label){
                Label label = (Label)instruction;
                labelAddresses.addEntry(label.label, new IntEntry(i));
            } else if(instruction instanceof ProcLabel){
                ProcLabel label = (ProcLabel)instruction;
                procLabelAddresses.addEntry(label.label, new IntEntry(i));
            }
        }
        this.machineState = State.INIT;
        this.programCounter = 0;
        for(this.programCounter = 0; this.programCounter < instructions.size(); this.programCounter++){
            ICode instruction = instructions.get(this.programCounter);
            switch(this.machineState){
                case INIT:
                    if(instruction instanceof Assign) interpretAssignment((Assign)instruction);
                    else if(instruction instanceof Call) interpretProcedureCall((Call)instruction);
                    else if(instruction instanceof If) interpretIfStatement((If)instruction);
                    else if(instruction instanceof Goto) interpretGotoStatement((Goto)instruction);
                    else if(instruction instanceof End) interpretEndStatement((End)instruction);
                    else if(instruction instanceof Return) interpretReturnStatement((Return)instruction);
                    else if(instruction instanceof Label) interpretLabelStatement((Label)instruction);
                    else if(instruction instanceof ParamAssign) interpretParamAssignment((ParamAssign)instruction);
                    else if(instruction instanceof InternalPlace) interpretInternalPlacement((InternalPlace)instruction);
                    else {
                        errorAndExit("Unexpected icode instruction found" + instruction.getClass(), this.programCounter, instructions.size());
                    }
                    continue;
                case RETURN:
                    if(instruction instanceof ExternalPlace){
                        //Then we need to perform this assignment before deallocating the stacks
                        ExternalPlace placement = (ExternalPlace)instruction;
                        if(tempReturnValue != null){
                            variableValues.addEntry(placement.place, new VariableEntry(false, tempReturnValue));
                            tempReturnValue = null;
                        }else if(variableValues.entryExists(placement.retPlace)){
                            VariableEntry entry = variableValues.getEntry(placement.retPlace);
                            //Now we need to Deallocate the Top of the Stack
                            variableValues.removeScope();
                            variableValues.addEntry(placement.place, entry);
                        } else {
                            errorAndExit("Return variable " + placement.retPlace + "was not allocated correctly", this.programCounter, instructions.size());
                        }
                    } else {
                        //Deincriment the program counter in order to have the same icode but in the init state\
                        this.programCounter--;
                        variableValues.removeScope();
                    }
                    this.machineState = State.INIT;
                    continue;
                default: 
                    errorAndExit("Machine is in unpredictable State ", this.programCounter, instructions.size());
            }
        }
        this.labelAddresses.removeScope();
        this.variableValues.removeScope();
    }

    private void interpretAssignment(Assign assign){
        String place = assign.place;
        Object result = interpretExpression(assign.value);
        this.variableValues.addEntry(place, new VariableEntry(false, result));
    }

    private void interpretParamAssignment(ParamAssign assign){
        String place = assign.newPlace;
        VariableEntry result = this.variableValues.getEntry(assign.paramPlace);
        this.variableValues.addEntry(place, result);
    }

    private void interpretInternalPlacement(InternalPlace placement){
        VariableEntry entry = variableValues.getEntry(placement.retPlace);
        variableValues.addEntry(placement.place, entry);
    }

    private void interpretEndStatement(End end){
        //Do nothing this is Just a Dummy method
    }

    private void interpretLabelStatement(Label l){
        //Do nothing this is just a Dummy method
    }

    private void interpretReturnStatement(Return returnV){
        int returnAddress = returnStack.pop();
        this.programCounter = returnAddress;
        this.machineState = State.RETURN;
    }

    private void interpretGotoStatement(Goto stat){
        IntEntry address = labelAddresses.getEntry(stat.label);
        this.programCounter = address.getValue();
    }

    private void interpretIfStatement(If statIf){
        Object valueExp = interpretBinExp(statIf.exp);

        if(valueExp instanceof Boolean){
            boolean value = (Boolean)valueExp;
            if(value){
                IntEntry labelAddr = labelAddresses.getEntry(statIf.ifTrue);
                this.programCounter = labelAddr.getValue();
            } else {
                IntEntry labelAddr = labelAddresses.getEntry(statIf.ifFalse);
                this.programCounter = labelAddr.getValue();
            }
        } else {
            errorAndExit("Invalid Expression result for If Statement", this.programCounter, 9999);
        }
    }

    private void interpretProcedureCall(Call procedure){
        if(procedure.pname.equals("WriteInt")){
            Tuple<String, String> arg1 = procedure.params.get(0);
            VariableEntry entry = variableValues.getEntry(arg1.source);

            try{
                Object val = entry.getValue();
                Integer toInt = Utils.toInt(val);
                standardOutput.append("" + toInt);
            } catch(IOException exp){
                errorAndExit(exp.toString(), programCounter, 999);
            }
        } else if(procedure.pname.equals("WriteLn")){
            try{
                standardOutput.append("\n");
            } catch(IOException exp){
                errorAndExit(exp.toString(), programCounter, 999);
            }
        } else if(procedure.pname.equals("WriteReal")){
            Tuple<String, String> arg1 = procedure.params.get(0);
            VariableEntry entry = variableValues.getEntry(arg1.source);

            try{
                Object val = entry.getValue();
                Double dVal = Utils.toDouble(val);
                standardOutput.append("" + dVal);
            } catch(IOException exp){
                errorAndExit(exp.toString(), programCounter, 999);
            }
        } else if(procedure.pname.equals("round") || procedure.pname.equals("Round")){
            Tuple<String, String> arg1 = procedure.params.get(0);
            VariableEntry entry = variableValues.getEntry(arg1.source);

            this.tempReturnValue = (int)Math.round(Utils.toDouble(entry.getValue()));
            this.machineState = State.RETURN;
        } else if(procedure.pname.equals("floor") || procedure.pname.equals("Floor")){
            Tuple<String, String> arg1 = procedure.params.get(0);
            VariableEntry entry = variableValues.getEntry(arg1.source);

            this.tempReturnValue = (int)Math.floor(Utils.toDouble(entry.getValue()));
            this.machineState = State.RETURN;
        } else if(procedure.pname.equals("ceil") || procedure.pname.equals("Ceil")){
            Tuple<String, String> arg1 = procedure.params.get(0);
            VariableEntry entry = variableValues.getEntry(arg1.source);

            this.tempReturnValue = (int)Math.ceil(Utils.toDouble(entry.getValue()));
            this.machineState = State.RETURN;
        } else if(procedure.pname.equals("readInt") || procedure.pname.equals("ReadInt")){
            Scanner scanner = new Scanner(standardIn);
            this.tempReturnValue = Integer.parseInt(scanner.nextLine());
            scanner.close();
            this.machineState = State.RETURN;  
        } else {
            this.returnStack.push(this.programCounter);
        
            List<VariableEntry> argVals = new ArrayList<VariableEntry>();
            for(Tuple<String, String> arg : procedure.params){
                VariableEntry argSource = this.variableValues.getEntry(arg.source);
                argVals.add(argSource);
            }

            this.variableValues.addScope();

            for(int i = 0; i < procedure.params.size(); i++){
                Tuple<String, String> newArg = procedure.params.get(i);
                VariableEntry sourceVal = argVals.get(i);
                this.variableValues.addEntry(newArg.dest, sourceVal);
            }

            IntEntry newAddress = this.labelAddresses.getEntry(procedure.pname);
            this.programCounter = newAddress.getValue();
        }
    }

    private Object interpretExpression(Exp expression){
        if(expression instanceof BinExp) return interpretBinExp((BinExp)expression);
        else if(expression instanceof UnExp) return interpretUnExp((UnExp)expression);
        else if(expression instanceof IdentExp) return interpretIdentExp((IdentExp)expression);
        else {
            return Utils.getValue(expression);
        }
    }

    private Object interpretIdentExp(IdentExp exp){
        VariableEntry entry = variableValues.getEntry(exp.ident);
        return entry.getValue();
    }

    private Object interpretUnExp(UnExp expression){
        Object right = null;

        if(expression.right.isConstant()){
            right = Utils.getValue(expression.right);
        } else {
            VariableEntry entry = variableValues.getEntry(expression.right.toString());
            right = entry.getValue();
        }

        switch(expression.op){
            case BNOT: return OpUtil.not(right);
            case INEG: return OpUtil.iNegate(right);
            case RNEG: return OpUtil.rNegate(right);
            default: return null;
        }
    }

    private Object interpretBinExp(BinExp expression){
        Object left = null;
        Object right = null;
        if(expression.left.isConstant()){
            left = Utils.getValue(expression.left);
        } else {
            VariableEntry entry = variableValues.getEntry(expression.left.toString());
            left = entry.getValue();
        }

        if(expression.right.isConstant()){
            right = Utils.getValue(expression.right);
        } else {
            VariableEntry entry = variableValues.getEntry(expression.right.toString());
            right = entry.getValue();
        }

        switch(expression.op){
            case IADD: return OpUtil.iAdd(left, right);
            case RADD: return OpUtil.rAdd(left, right);
            case ISUB: return OpUtil.iSub(left, right);
            case RSUB: return OpUtil.rSub(left, right);
            case IMUL: return OpUtil.iMul(left, right);
            case RMUL: return OpUtil.rMul(left, right);
            case IAND: return OpUtil.bitwiseAnd(left, right);
            case IOR: return OpUtil.bitwiseOr(left, right);
            case IXOR: return OpUtil.bitwiseXor(left, right);
            case ILSHIFT: return OpUtil.leftShift(left, right);
            case IRSHIFT: return OpUtil.rightShift(left, right);
            case IDIV: return OpUtil.iDiv(left, right);
            case RDIVIDE: return OpUtil.rDivide(left, right);
            case IMOD: return OpUtil.iMod(left, right);
            case EQ: return OpUtil.equal(left, right);
            case NE: return OpUtil.notEqual(left, right);
            case GE: return OpUtil.greaterThanOrEqualTo(left, right);
            case GT: return OpUtil.greaterThan(left, right);
            case LE: return OpUtil.lessThanOrEqualTo(left, right);
            case LT: return OpUtil.lessThan(left, right);
            case LAND: return OpUtil.and(left, right);
            case LOR: return OpUtil.or(left, right);
            default: return null;
        }
    }

    private void errorAndExit(String message, int position, int max){
        this.programCounter = max;
        errLog.add(message, new Position(position, 0));
    }
}
