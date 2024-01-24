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
        int programLength = instructions.size();
        for(int i = 0; i < programLength; i++){
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
        for(this.programCounter = 0; this.programCounter < programLength; this.programCounter++){
            ICode instruction = instructions.get(this.programCounter);
            switch(this.machineState){
                case INIT:
                    if(instruction instanceof Assign) interpretAssignment((Assign)instruction, programLength);
                    else if(instruction instanceof Call) interpretProcedureCall((Call)instruction, programLength);
                    else if(instruction instanceof If) interpretIfStatement((If)instruction, programLength);
                    else if(instruction instanceof Goto) interpretGotoStatement((Goto)instruction, programLength);
                    else if(instruction instanceof End) interpretEndStatement((End)instruction, programLength);
                    else if(instruction instanceof Return) interpretReturnStatement((Return)instruction, programLength);
                    else if(instruction instanceof Label) interpretLabelStatement((Label)instruction, programLength);
                    else if(instruction instanceof ParamAssign) interpretParamAssignment((ParamAssign)instruction, programLength);
                    else if(instruction instanceof InternalPlace) interpretInternalPlacement((InternalPlace)instruction, programLength);
                    else {
                        errorAndExit("Unexpected icode instruction found" + instruction.getClass(), this.programCounter, programLength);
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
                            errorAndExit("Return variable " + placement.retPlace + "was not allocated correctly", this.programCounter, programLength);
                        }
                    } else {
                        //Deincriment the program counter in order to have the same icode but in the init state\
                        this.programCounter--;
                        variableValues.removeScope();
                    }
                    this.machineState = State.INIT;
                    continue;
                default: 
                    errorAndExit("Machine is in unpredictable State ", this.programCounter, programLength);
            }
        }
        this.labelAddresses.removeScope();
        this.variableValues.removeScope();
    }

    private void interpretAssignment(Assign assign, int programLength){
        String place = assign.place;
        Object result = interpretExpression(assign.value, programLength);
        this.variableValues.addEntry(place, new VariableEntry(false, result));
    }

    private void interpretParamAssignment(ParamAssign assign, int programLength){
        String place = assign.newPlace;
        VariableEntry result = this.variableValues.getEntry(assign.paramPlace);
        this.variableValues.addEntry(place, result);
    }

    private void interpretInternalPlacement(InternalPlace placement, int programLength){
        VariableEntry entry = variableValues.getEntry(placement.retPlace);
        variableValues.addEntry(placement.place, entry);
    }

    private void interpretEndStatement(End end, int programLength){
        this.programCounter = programLength;
    }

    private void interpretLabelStatement(Label l, int programLength){
        //Do nothing this is just a Dummy method
    }

    private void interpretReturnStatement(Return returnV, int programLength){
        int returnAddress = returnStack.pop();
        this.programCounter = returnAddress;
        this.machineState = State.RETURN;
    }

    private void interpretGotoStatement(Goto stat, int programLength){
        if(labelAddresses.entryExists(stat.label)){
            IntEntry address = labelAddresses.getEntry(stat.label);
            this.programCounter = address.getValue();
        } else {
            errorAndExit("Label for " + stat + " not found!!!", programCounter, programLength);
        }
    }

    private void interpretIfStatement(If statIf, int programLength){
        Object valueExp = interpretBinExp(statIf.exp, programLength);

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
            errorAndExit("Invalid Expression result for If Statement", this.programCounter, programLength);
        }
    }

    private void interpretProcedureCall(Call procedure, int programLength){
        if(procedure.pname.equals("WriteInt")){
            if(procedure.params.size() == 1){
                Tuple<String, String> arg1 = procedure.params.get(0);
                if(variableValues.entryExists(arg1.source)){
                    VariableEntry entry = variableValues.getEntry(arg1.source);
                    try{
                        Object val = entry.getValue();
                        Integer toInt = Utils.toInt(val);
                        standardOutput.append("" + toInt);
                    } catch(IOException exp){
                        errorAndExit(exp.toString(), programCounter, programLength);
                    }
                } else {
                    errorAndExit("Error paramater " + arg1.source + " does not exist in function " + procedure, programCounter, programLength);
                }
            } else {
                errorAndExit("In procedure call " + procedure + " expected 1 argument for function call " + procedure.pname, programCounter, programLength);
            }
        } else if(procedure.pname.equals("WriteLn")){
            try{
                standardOutput.append("\n");
            } catch(IOException exp){
                errorAndExit(exp.toString(), programCounter, programLength);
            }
        } else if(procedure.pname.equals("WriteReal")){
            if(procedure.params.size() == 1){
                Tuple<String, String> arg1 = procedure.params.get(0);
                if(variableValues.entryExists(arg1.source)){
                    VariableEntry entry = variableValues.getEntry(arg1.source);
                    try{
                        Object val = entry.getValue();
                        Float dVal = Utils.toReal(val);
                        standardOutput.append("" + dVal);
                    } catch(IOException exp){
                        errorAndExit(exp.toString(), programCounter, programLength);
                    }
                } else {
                    errorAndExit("Error paramater " + arg1.source + " does not exist in function " + procedure, programCounter, programLength);
                }
            } else {
                errorAndExit("In procedure call " + procedure + " expected 1 argument for function call " + procedure.pname, programCounter, programLength);
            }
        } else if(procedure.pname.equals("readInt") || procedure.pname.equals("ReadInt")){
            Scanner scanner = new Scanner(standardIn);
            this.tempReturnValue = Integer.parseInt(scanner.nextLine());
            scanner.close();
            this.machineState = State.RETURN;  
        } else if(procedure.pname.equals("RealBinaryAsInt") || procedure.pname.equals("realBinaryAsInt")){
            if(procedure.params.size() == 1){
                Tuple<String, String> arg1 = procedure.params.get(0);
                if(variableValues.entryExists(arg1.source)){
                    VariableEntry entry = variableValues.getEntry(arg1.source);
                    Object val = entry.getValue();
                    Float valFloat = (Float)val;
                    Integer toRet = Float.floatToRawIntBits(valFloat);
                    tempReturnValue = toRet;
                } else {
                    errorAndExit("Error paramater " + arg1.source + " does not exist in function " + procedure, programCounter, programLength);
                }
            } else {
                errorAndExit("In procedure call " + procedure + " expected 1 argument for function call " + procedure.pname, programCounter, programLength);
            }
        } else if(procedure.pname.equals("IntBinaryAsReal") || procedure.pname.equals("intBinaryAsReal")){
            if(procedure.params.size() == 1){
                Tuple<String, String> arg1 = procedure.params.get(0);
                if(variableValues.entryExists(arg1.source)){
                    VariableEntry entry = variableValues.getEntry(arg1.source);
                    Object val = entry.getValue();
                    Integer valInt = (Integer)val;
                    Float toRet = Float.intBitsToFloat(valInt);
                    tempReturnValue = toRet;
                } else {
                    errorAndExit("Error paramater " + arg1.source + " does not exist in function " + procedure, programCounter, programLength);
                }
            }  else {
                errorAndExit("In procedure call " + procedure + " expected 1 argument for function call " + procedure.pname, programCounter, programLength);
            }  
        } else {
            this.returnStack.push(this.programCounter);
        
            List<VariableEntry> argVals = new ArrayList<VariableEntry>();
            for(Tuple<String, String> arg : procedure.params){
                if(this.variableValues.entryExists(arg.source)){
                    VariableEntry argSource = this.variableValues.getEntry(arg.source);
                    argVals.add(argSource);
                } else {
                    errorAndExit("Error in procedure call " + procedure + " cant find argument " + arg.source, programCounter, programLength);
                }
            }

            if(argVals.size() == procedure.params.size()){
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
    }

    private Object interpretExpression(Exp expression, int programLength){
        if(expression instanceof BinExp) return interpretBinExp((BinExp)expression, programLength);
        else if(expression instanceof UnExp) return interpretUnExp((UnExp)expression, programLength);
        else if(expression instanceof IdentExp) return interpretIdentExp((IdentExp)expression, programLength);
        else {
            return Utils.getValue(expression);
        }
    }

    private Object interpretIdentExp(IdentExp exp, int programLength){
        if(variableValues.entryExists(exp.ident)){
            VariableEntry entry = variableValues.getEntry(exp.ident);
            return entry.getValue();
        } else {
            errorAndExit("Error cant find value for variable " + exp.ident, this.programCounter, programLength);
            return null;
        }
    }

    private Object interpretUnExp(UnExp expression, int programLength){
        Object right = null;

        if(expression.right.isConstant()){
            right = Utils.getValue(expression.right);
        } else if(variableValues.entryExists(expression.right.toString())) {
            VariableEntry entry = variableValues.getEntry(expression.right.toString());
            right = entry.getValue();
        } else {
            errorAndExit("In expression " + expression + " cant find value on right hand side of the expression " + expression.right.toString(), programCounter, programLength);
        }

        if(right != null){
            switch(expression.op){
                case BNOT: return OpUtil.not(right);
                case INEG: return OpUtil.iNegate(right);
                case RNEG: return OpUtil.rNegate(right);
                default: return null;
            }
        } else {
            return null;
        }
    }

    private Object interpretBinExp(BinExp expression, int programLength){
        Object left = null;
        Object right = null;
        if(expression.left.isConstant()){
            left = Utils.getValue(expression.left);
        } else if(variableValues.entryExists(expression.left.toString())) {
            VariableEntry entry = variableValues.getEntry(expression.left.toString());
            left = entry.getValue();
        } else {
            errorAndExit("In expression " + expression+" cant find value on left hand side of the expression " + expression.left.toString(), programCounter, programLength);
        }

        if(expression.right.isConstant()){
            right = Utils.getValue(expression.right);
        } else if(variableValues.entryExists(expression.right.toString())) {
            VariableEntry entry = variableValues.getEntry(expression.right.toString());
            right = entry.getValue();
        } else {
            errorAndExit("In Expression " + expression + " cant find value on the right hand side of the expression " + expression.right.toString(), programCounter, programLength);
        }

        if(left != null && right != null){
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
        } else {
            return null;
        }
    }

    private void errorAndExit(String message, int position, int max){
        this.programCounter = max;
        errLog.add(message, new Position(position, 0));
    }
}
