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
import io.github.H20man13.DeClan.common.exception.ICodeGeneratorException;
import io.github.H20man13.DeClan.common.exception.ICodeVmException;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.IntEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
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
        interpretICode(program.getICode());
    }

    private void interpretICode(List<ICode> instrs){
        ArrayList<ICode> instructions = new ArrayList<ICode>();
        instructions.addAll(instrs);
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
                    if(instruction instanceof Assign) interpretAssignment((Assign)instruction);
                    else if(instruction instanceof Def) interpretDefinition((Def)instruction);
                    else if(instruction instanceof Call) interpretProcedureCall((Call)instruction);
                    else if(instruction instanceof If) interpretIfStatement((If)instruction);
                    else if(instruction instanceof Goto) interpretGotoStatement((Goto)instruction);
                    else if(instruction instanceof End) interpretEndStatement((End)instruction);
                    else if(instruction instanceof Return) interpretReturnStatement((Return)instruction);
                    else if(instruction instanceof Label) interpretLabelStatement((Label)instruction);
                    else if(instruction instanceof Inline) interpretInlineAssembly((Inline)instruction);
                    else throw new ICodeVmException(instruction, this.programCounter, "Invalid instruction type " + instruction.getClass().getName());
                    continue;
                case RETURN:
                    if(instruction instanceof Assign){
                        //Then we need to perform this assignment before deallocating the stacks
                        Assign placement = (Assign)instruction;
                        if(placement.value instanceof IdentExp){
                            IdentExp val = (IdentExp)placement.value;
                            if(val.scope == ICode.Scope.RETURN){
                                if(!variableValues.entryExists(placement.place))
                                    throw new ICodeVmException(placement, this.programCounter, "Value assigned to (in this case " + placement.place + ") doesnt exist");
                                if(tempReturnValue != null){
                                    variableValues.addEntry(placement.place, new VariableEntry(false, tempReturnValue));
                                    tempReturnValue = null;
                                } else {
                                    VariableEntry entry = variableValues.getEntry(val.ident);
                                    //Now we need to Deallocate the Top of the Stack
                                    variableValues.removeScope();
                                    variableValues.addEntry(placement.place, entry);
                                }
                            } else {
                                this.programCounter--;
                                variableValues.removeScope();
                            }
                        } else {
                            //De incriment the program counter
                            this.programCounter--;
                            variableValues.removeScope();
                        }
                    } else if(instruction instanceof Def){
                        Def placement = (Def)instruction;
                        if(placement.val instanceof IdentExp){
                            IdentExp val = (IdentExp)placement.val;
                            if(val.scope == ICode.Scope.RETURN){
                                if(tempReturnValue != null){
                                    variableValues.addEntry(placement.label, new VariableEntry(false, tempReturnValue));
                                    tempReturnValue = null;
                                } else {
                                    VariableEntry entry = variableValues.getEntry(val.ident);
                                    //Now we need to Deallocate the Top of the Stack
                                    variableValues.removeScope();
                                    variableValues.addEntry(placement.label, entry);
                                }
                            } else {
                                this.programCounter--;
                                variableValues.removeScope();
                            }
                        } else {
                            //De incriment the program counter
                            this.programCounter--;
                            variableValues.removeScope();
                        }
                    } else {
                        //Deincriment the program counter in order to have the same icode but in the init state\
                        this.programCounter--;
                        variableValues.removeScope();
                    }
                    this.machineState = State.INIT;
                    continue;
                default: 
                    throw new ICodeVmException(instruction, this.programCounter, "Machine is in unpredictable State");
            }
        }
        this.labelAddresses.removeScope();
        this.variableValues.removeScope();
    }

    private void interpretInlineAssembly(Inline instruction) {
        if(instruction.inlineAssembly.startsWith("MULL")){
            //Then it is a multiply long instruction and we have to simulate that here
            List<IdentExp> paramaters = instruction.params;
            if(paramaters.size() == 4){
                //First get the two source paramaters
                IdentExp param1 = paramaters.get(2);
                VariableEntry entry1 = variableValues.getEntry(param1.ident);
                IdentExp param2 = paramaters.get(3);
                VariableEntry entry2 = variableValues.getEntry(param2.ident);

                Object obj1 = entry1.getValue();
                Object obj2 = entry2.getValue();

                if(!(obj1 instanceof Integer)){
                    throw new ICodeVmException(instruction, this.programCounter, "Error in MULL function inline assembly expected both arguments to be of type Integer but found " + param1.ident + "=" + obj1.getClass().getName());
                } else if(!(obj2 instanceof Integer)){
                    throw new ICodeVmException(instruction, this.programCounter, "Error in MULL function inline assembly expected both arguments to be of type Integer but found " + param2.ident + "=" + obj2.getClass().getName());
                }

                Integer int1 = (Integer)obj1;
                Integer int2 = (Integer)obj2;

                Long long1 = int1.longValue();
                Long long2 = int2.longValue();

                Long result = long1 * long2;

                Long smallLongMask = 0x7fffffffl;
                Long smallLong = result & smallLongMask;
                Long largeLong = (result >> 31) & smallLongMask;

                Integer smallInt = smallLong.intValue();
                Integer largeInt = largeLong.intValue();

                String largeRegister = paramaters.get(1).ident;
                String smallRegister = paramaters.get(0).ident;
                variableValues.addEntry(largeRegister, new VariableEntry(false, largeInt));
                variableValues.addEntry(smallRegister, new VariableEntry(false, smallInt));
            } else {
                throw new ICodeVmException(instruction, this.programCounter, "Error in MULL function in inline assembly expected 4 arguments but found " + paramaters.size());
            }
        }
        //Otherwise we just ignore the instruction
    }

    private void interpretAssignment(Assign assign){
        String place = assign.place;
        Object result = interpretExpression(assign.value);
        if(result != null){
            if(this.variableValues.entryExists(place)){
                VariableEntry entry = this.variableValues.getEntry(place);
                entry.setValue(result);
            } else {
                throw new ICodeVmException(assign, programCounter, "No entry found for " + place + " in left hand side of the assignemnt");
            }
        } else {
            throw new ICodeVmException(assign, programCounter, "Right hand side of the assignment resulted in a null value");
        }
    }

    private void interpretDefinition(Def def){
        String place = def.label;
        Object result = interpretExpression(def.val);
        if(result != null){
            this.variableValues.addEntry(place, new VariableEntry(false, result));
        } else {
            throw new ICodeVmException(def, programCounter, "Right hand side of the definition resulted in a null value");
        }
    }

    private void interpretEndStatement(End end){
        //Do nothing
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
        if(labelAddresses.entryExists(stat.label)){
            IntEntry address = labelAddresses.getEntry(stat.label);
            this.programCounter = address.getValue();
        } else {
            throw new ICodeVmException(stat, this.programCounter, "Label for " + stat + " not found!!!");
        }
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
            throw new ICodeVmException(statIf, this.programCounter, "Invalid Expression result for If Statement");
        }
    }

    private void interpretProcedureCall(Call procedure){
        if(procedure.pname.equals("WriteInt")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
                if(arg1.val instanceof IntExp){
                    try{
                        IntExp intExp = (IntExp)arg1.val;
                        standardOutput.append("" + intExp.value);
                    } catch(IOException exp){
                        throw new ICodeVmException(arg1, programCounter, exp.getMessage());
                    }
                } else if(arg1.val instanceof IdentExp){
                    IdentExp iExp = (IdentExp)arg1.val;
                    if(variableValues.entryExists(iExp.ident)){
                        VariableEntry entry = variableValues.getEntry(iExp.ident);
                        try{
                            Object val = entry.getValue();
                            Integer toInt = ConversionUtils.toInt(val);
                            standardOutput.append("" + toInt);
                        } catch(IOException exp){
                            throw new ICodeVmException(arg1, this.programCounter, exp.getMessage());
                        }
                    } else {
                        throw new ICodeVmException(arg1, programCounter, "Variable value was not found for " + iExp.ident);
                    }
                } else {
                    throw new ICodeVmException(procedure, programCounter, "Invalid expression type for paramater into WriteInt expected Int or Ident but found " + arg1.val.getClass().getName());
                }
            } else {
                throw new ICodeVmException(procedure, this.programCounter, "In procedure call expected 1 argument for function call " + procedure.pname);
            }
        } else if(procedure.pname.equals("WriteBool")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
                if(arg1.val instanceof BoolExp){
                    try{
                        BoolExp intExp = (BoolExp)arg1.val;
                        standardOutput.append("" + intExp.trueFalse);
                    } catch(IOException exp){
                        throw new ICodeVmException(arg1, programCounter, exp.getMessage());
                    }
                } else if(arg1.val instanceof IdentExp){
                    IdentExp iExp = (IdentExp)arg1.val;
                    if(variableValues.entryExists(iExp.ident)){
                        VariableEntry entry = variableValues.getEntry(iExp.ident);
                        try{
                            Object val = entry.getValue();
                            Boolean toBool = ConversionUtils.toBool(val);
                            standardOutput.append("" + toBool);
                        } catch(IOException exp){
                            throw new ICodeVmException(arg1, this.programCounter, exp.getMessage());
                        }
                    } else {
                        throw new ICodeVmException(arg1, programCounter, "Variable value was not found for " + iExp.ident);
                    }
                } else {
                    throw new ICodeVmException(procedure, programCounter, "Invalid expression type for paramater into WriteBool expected Bool or Ident but found " + arg1.val.getClass().getName());
                }
            } else {
                throw new ICodeVmException(procedure, this.programCounter, "In procedure call expected 1 argument for function call " + procedure.pname);
            }
        } else if(procedure.pname.equals("WriteLn")){
            try{
                standardOutput.append("\n");
            } catch(IOException exp){
                throw new ICodeVmException(procedure, this.programCounter, exp.getMessage());
            }
        } else if(procedure.pname.equals("WriteReal")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
                if(arg1.val instanceof RealExp){
                    try{
                        RealExp intExp = (RealExp)arg1.val;
                        standardOutput.append("" + intExp.realValue);
                    } catch(IOException exp){
                        throw new ICodeVmException(arg1, programCounter, exp.getMessage());
                    }
                } else if(arg1.val instanceof IdentExp){
                    IdentExp iExp = (IdentExp)arg1.val;
                    if(variableValues.entryExists(iExp.ident)){
                        VariableEntry entry = variableValues.getEntry(iExp.ident);
                        try{
                            Object val = entry.getValue();
                            Boolean toBool = ConversionUtils.toBool(val);
                            standardOutput.append("" + toBool);
                        } catch(IOException exp){
                            throw new ICodeVmException(arg1, this.programCounter, exp.getMessage());
                        }
                    } else {
                        throw new ICodeVmException(arg1, programCounter, "Variable value was not found for " + iExp.ident);
                    }
                } else {
                    throw new ICodeVmException(procedure, programCounter, "Invalid expression type for paramater into WriteReal expected Bool or Ident but found " + arg1.val.getClass().getName());
                }
            } else {
                throw new ICodeVmException(procedure, this.programCounter, "In procedure call expected 1 argument for function call " + procedure.pname);
            }
        } else if(procedure.pname.equals("WriteString")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
                if(arg1.val instanceof StrExp){
                    try{
                        StrExp intExp = (StrExp)arg1.val;
                        standardOutput.append("" + intExp.value);
                    } catch(IOException exp){
                        throw new ICodeVmException(arg1, programCounter, exp.getMessage());
                    }
                } else if(arg1.val instanceof IdentExp){
                    IdentExp iExp = (IdentExp)arg1.val;
                    if(variableValues.entryExists(iExp.ident)){
                        VariableEntry entry = variableValues.getEntry(iExp.ident);
                        try{
                            Object val = entry.getValue();
                            String toBool = val.toString();
                            standardOutput.append("" + toBool);
                        } catch(IOException exp){
                            throw new ICodeVmException(arg1, this.programCounter, exp.getMessage());
                        }
                    } else {
                        throw new ICodeVmException(arg1, programCounter, "Variable value was not found for " + iExp.ident);
                    }
                } else {
                    throw new ICodeVmException(procedure, programCounter, "Invalid expression type for paramater into WriteString expected Bool or Ident but found " + arg1.val.getClass().getName());
                }
            } else {
                throw new ICodeVmException(procedure, this.programCounter, "In procedure call expected 1 argument for function call " + procedure.pname);
            }
        } else if(procedure.pname.equals("readInt") || procedure.pname.equals("ReadInt")){
            Scanner scanner = new Scanner(standardIn);
            this.tempReturnValue = Integer.parseInt(scanner.nextLine());
            scanner.close();
            this.machineState = State.RETURN;  
        } else if(procedure.pname.equals("readBool") || procedure.pname.equals("ReadBool")){
            Scanner scanner = new Scanner(standardIn);
            this.tempReturnValue = Boolean.parseBoolean(scanner.nextLine());
            scanner.close();
            this.machineState = State.RETURN; 
        } else if(procedure.pname.equals("readReal") || procedure.pname.equals("ReadReal")){
           Scanner scanner = new Scanner(standardIn);
           this.tempReturnValue = Float.parseFloat(scanner.nextLine());
           scanner.close();
           this.machineState = State.RETURN;  
        } else if(procedure.pname.equals("RealBinaryAsInt") || procedure.pname.equals("realBinaryAsInt")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
                if(arg1.val instanceof RealExp){
                    RealExp expVal = (RealExp)arg1.val;
                    Integer toRet = Float.floatToRawIntBits(expVal.realValue);
                    tempReturnValue = toRet;
                    machineState = State.RETURN;
                } else if(arg1.val instanceof IdentExp){
                    IdentExp iExp = (IdentExp)arg1.val;
                    if(variableValues.entryExists(iExp.ident)){
                        VariableEntry entry = variableValues.getEntry(iExp.ident);
                        Object val = entry.getValue();
                        if(val != null){
                            Float valFloat = (Float)val;
                            Integer toRet = Float.floatToRawIntBits(valFloat);
                            tempReturnValue = toRet;
                            machineState = State.RETURN;
                        } else {
                            throw new ICodeVmException(arg1, this.programCounter, "In procedure " + procedure + " paramater has a null value");
                        }
                    } else {
                        throw new ICodeVmException(arg1, this.programCounter, "Entry for variable " + iExp.ident + " doesnt exist");
                    }
                }
            } else {
                throw new ICodeVmException(procedure, this.programCounter, "In procedure call " + procedure + " expected 1 argument for function call " + procedure.pname);
            }
        } else if(procedure.pname.equals("IntBinaryAsReal") || procedure.pname.equals("intBinaryAsReal")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
                if(arg1.val instanceof IntExp){
                    IntExp expVal = (IntExp)arg1.val;
                    Float toRet = Float.intBitsToFloat(expVal.value);
                    tempReturnValue = toRet;
                    machineState = State.RETURN;
                } else if(arg1.val instanceof IdentExp){
                    IdentExp iExp = (IdentExp)arg1.val;
                    if(variableValues.entryExists(iExp.ident)){
                        VariableEntry entry = variableValues.getEntry(iExp.ident);
                        Object val = entry.getValue();
                        if(val != null){
                            Integer valFloat = (Integer)val;
                            Float toRet = Float.intBitsToFloat(valFloat);
                            tempReturnValue = toRet;
                            machineState = State.RETURN;
                        } else {
                            throw new ICodeVmException(arg1, this.programCounter, "In procedure " + procedure + " paramater has a null value");
                        }
                    } else {
                        throw new ICodeVmException(arg1, this.programCounter, "Entry for variable " + iExp.ident + " doesnt exist");
                    }
                }
            } else {
                throw new ICodeVmException(procedure, this.programCounter, "In procedure call " + procedure + " expected 1 argument for function call " + procedure.pname);
            }
        } else {
            this.returnStack.push(this.programCounter);
        
            List<VariableEntry> argVals = new ArrayList<VariableEntry>();
            for(Def arg : procedure.params){
                if(arg.val.isConstant()){
                    Object val = ConversionUtils.getValue(arg.val);
                    argVals.add(new VariableEntry(false, val));
                } else if(arg.val instanceof IdentExp){
                    IdentExp valExp = (IdentExp)arg.val;
                    if(variableValues.entryExists(valExp.ident)){
                        VariableEntry argSource = this.variableValues.getEntry(valExp.ident);
                        argVals.add(argSource);
                    } else {
                        throw new ICodeVmException(arg, this.programCounter, "Variable value for " + valExp.ident + " does not exist");
                    }
                } else {
                    throw new ICodeVmException(arg, this.programCounter, "Error in procedure call " + procedure + " invalid expression type " + arg.val.getClass().getName());
                }
            }

            if(argVals.size() == procedure.params.size()){
                this.variableValues.addScope();

                for(int i = 0; i < procedure.params.size(); i++){
                    Def newArg = procedure.params.get(i);
                    VariableEntry sourceVal = argVals.get(i);
                    this.variableValues.addEntry(newArg.label, sourceVal);
                }

                IntEntry newAddress = this.labelAddresses.getEntry(procedure.pname);
                this.programCounter = newAddress.getValue();
            }
        }
    }

    private Object interpretExpression(Exp expression){
        if(expression instanceof BinExp) return interpretBinExp((BinExp)expression);
        else if(expression instanceof UnExp) return interpretUnExp((UnExp)expression);
        else if(expression instanceof IdentExp) return interpretIdentExp((IdentExp)expression);
        else {
            return ConversionUtils.getValue(expression);
        }
    }

    private Object interpretIdentExp(IdentExp exp){
        if(variableValues.entryExists(exp.ident)){
            VariableEntry entry = variableValues.getEntry(exp.ident);
            if(entry != null){
                return entry.getValue();
            } else {
                throw new ICodeVmException(exp, this.programCounter, "Error entry for " + exp + " is null ");
            }
        } else {
            throw new ICodeVmException(exp, this.programCounter, "Error cant find value for variable " + exp.ident);
        }
    }

    private Object interpretUnExp(UnExp expression){
        Object right = null;

        if(expression.right.isConstant()){
            right = ConversionUtils.getValue(expression.right);
        } else if(expression.right instanceof IdentExp) {
            IdentExp expressionRight = (IdentExp)expression.right;
            if(variableValues.entryExists(expressionRight.ident)){
                VariableEntry entry = variableValues.getEntry(expressionRight.ident);
                right = entry.getValue();
            } else {
                throw new ICodeVmException(expression, programCounter, "Variable on right hand side of unary expression does not exist");
            }
        } else {
            throw new ICodeVmException(expression, programCounter, "In expression " + expression + " cant find value on right hand side of the expression " + expression.right.toString());
        }

        if(right != null){
            switch(expression.op){
                case BNOT: return OpUtil.not(right);
                case INEG: return OpUtil.iNegate(right);
                case INOT: return OpUtil.bitwiseNot(right);
                case RNEG: return OpUtil.rNegate(right);
                default: 
                    throw new ICodeVmException(expression, programCounter, "Unknown unary operation " + expression);
            }
        } else {
            throw new ICodeVmException(expression, programCounter, "Right hand side value inary unary operation is a Null value");
        }
    }

    private Object interpretBinExp(BinExp expression){
        Object left = null;
        Object right = null;
        if(expression.left.isConstant()){
            left = ConversionUtils.getValue(expression.right);
        } else if(expression.left instanceof IdentExp){
            IdentExp leftIdent = (IdentExp)expression.right;
            if(variableValues.entryExists(leftIdent.ident)){
                VariableEntry entry = variableValues.getEntry(leftIdent.ident);
                left = entry.getValue();
            } else {
                throw new ICodeVmException(expression, this.programCounter, "In Expression " + expression + " cant find value on the left hand side of the expression " + expression.left.toString());
            }
        } else {
            throw new ICodeVmException(expression, this.programCounter, "Invalid expression type on the left hand side of expression");
        }

        if(expression.right.isConstant()){
            right = ConversionUtils.getValue(expression.right);
        } else if(expression.right instanceof IdentExp){
            IdentExp rightIdent = (IdentExp)expression.right;
            if(variableValues.entryExists(rightIdent.ident)){
                VariableEntry entry = variableValues.getEntry(rightIdent.ident);
                right = entry.getValue();
            } else {
                throw new ICodeVmException(expression, this.programCounter, "In Expression " + expression + " cant find value on the right hand side of the expression " + expression.right.toString());
            }
        } else {
            throw new ICodeVmException(expression, this.programCounter, "Invalid expression type on the right hand side of expression");
        }

        if(left != null && right != null){
            switch(expression.op){
                case IADD: return OpUtil.iAdd(left, right);
                case ISUB: return OpUtil.iSub(left, right);
                case IMUL: return OpUtil.iMul(left, right);
                case IAND: return OpUtil.bitwiseAnd(left, right);
                case IOR: return OpUtil.bitwiseOr(left, right);
                case IXOR: return OpUtil.bitwiseXor(left, right);
                case ILSHIFT: return OpUtil.leftShift(left, right);
                case IRSHIFT: return OpUtil.rightShift(left, right);
                case IDIV: return OpUtil.iDiv(left, right);
                case IMOD: return OpUtil.iMod(left, right);
                case EQ: return OpUtil.equal(left, right);
                case NE: return OpUtil.notEqual(left, right);
                case GE: return OpUtil.greaterThanOrEqualTo(left, right);
                case GT: return OpUtil.greaterThan(left, right);
                case LE: return OpUtil.lessThanOrEqualTo(left, right);
                case LT: return OpUtil.lessThan(left, right);
                case LAND: return OpUtil.and(left, right);
                case LOR: return OpUtil.or(left, right);
                default:
                    throw new ICodeVmException(expression, this.programCounter, "Unknown Binary operation"); 
            }
        } else {
            throw new ICodeVmException(expression, programCounter, "");
        }
    }
}
