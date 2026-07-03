package declan.middleware;

import java.io.IOException;
import java.io.Reader;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import declan.utils.ErrorLog;
import declan.utils.position.Position;
import declan.utils.Tuple;
import declan.utils.exception.ICodeGeneratorException;
import declan.utils.exception.ICodeVmException;
import declan.utils.exception.ProgramFinishedException;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Call;
import declan.middleware.icode.Def;
import declan.middleware.icode.End;
import declan.middleware.icode.Goto;
import declan.middleware.icode.ICode;
import declan.middleware.icode.If;
import declan.middleware.icode.Prog;
import declan.middleware.icode.Return;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.BoolExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.IntExp;
import declan.middleware.icode.exp.RealExp;
import declan.middleware.icode .exp.CharArrayExp;
import declan.middleware.icode.exp.CharExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.icode.exp.ArrayAccess;
import declan.middleware.icode.exp.IntArrayExp;
import declan.middleware.icode.exp.RealArrayExp;
import declan.middleware.icode.exp.BoolArrayExp;
import declan.middleware.icode.ArrayAssign;
import declan.middleware.icode.inline.Inline;
import declan.middleware.icode.inline.InlineParam;
import declan.middleware.icode.label.Label;
import declan.middleware.icode.label.ProcLabel;
import declan.utils.symboltable.Environment;
import declan.utils.symboltable.entry.IntEntry;
import declan.utils.symboltable.entry.VariableEntry;
import declan.utils.ConversionUtils;
import declan.utils.OpUtil;
import declan.utils.Utils;

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
    private PushbackReader standardIn;
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
        this.standardIn = new PushbackReader(standardIn);
        this.tempReturnValue = null;
    }

    private enum State{
        RETURN,
        INIT,
    }

    public void interpretICode(Prog program){
        interpretICode(program.getExecutableCode());
    }

    private void interpretICode(List<ICode> instrs){
        ArrayList<ICode> instructions = new ArrayList<ICode>();
        instructions.addAll(instrs);
        this.labelAddresses.addScope();
        this.procLabelAddresses.addScope();
        this.variableValues.addScope();
        try {
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
			    else if(instruction instanceof ArrayAssign) interpretArrayAssign((ArrayAssign)instruction);
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
	                    } else if(instruction instanceof ArrayAssign) {
				ArrayAssign placement = (ArrayAssign)instruction;
				if(placement.expression instanceof IdentExp){
	                            IdentExp val = (IdentExp)placement.expression;
	                            if(val.scope == ICode.Scope.RETURN){
	                                if(!variableValues.entryExists(placement.ident))
	                                    throw new ICodeVmException(placement, this.programCounter, "Value assigned to (in this case " + placement.ident + ") doesnt exist");
	                                if(tempReturnValue != null){
	                                    VariableEntry entry = variableValues.getEntry(placement.ident);
					    int index = (int)interpretExpression(placement.index, instruction);
					    Object obj = entry.getValue();
					    if(obj instanceof int[]){
						int[] iObj = (int[])obj;
						iObj[index] = (int)interpretExpression(val, instruction); 
					    } else if(obj instanceof char[]){
						char[] cObj = (char[])obj;
						cObj[index] = (char)interpretExpression(val, instruction);
					    } else if(obj instanceof float[]){
						float[] fObj = (float[])obj;
						fObj[index] = (float)interpretExpression(val, instruction);
					    } else if(obj instanceof boolean[]){
						boolean[] bObj = (boolean[])obj;
						bObj[index] = (boolean)interpretExpression(val, instruction);
					    }
	                                    tempReturnValue = null;
	                                } else {
	                                    Object obj = interpretExpression(val, instruction);
	                                    //Now we need to Deallocate the Top of the Stack
	                                    variableValues.removeScope();
					    int index = (int)interpretExpression(placement.index, instruction);
	                                    VariableEntry arr = variableValues.getEntry(placement.ident);
					    Object arrF = arr.getValue();
					    if(arrF instanceof int[]){
						int[] iObj = (int[])arrF;
						iObj[index] = (int)interpretExpression(val, instruction); 
					    } else if(arrF instanceof char[]){
						char[] cObj = (char[])arrF;
						cObj[index] = (char)interpretExpression(val, instruction);
					    } else if(arrF instanceof float[]){
						float[] fObj = (float[])arrF;
						fObj[index] = (float)interpretExpression(val, instruction);
					    } else if(arrF instanceof boolean[]){
						boolean[] bObj = (boolean[])arrF;
						bObj[index] = (boolean)interpretExpression(val, instruction);
					    }
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
	        this.procLabelAddresses.removeScope();
        } catch(ProgramFinishedException exp) {
        	//Program finished Successfully can exit State machine
        	this.labelAddresses.removeScope();
        	this.variableValues.removeScope();
        	this.procLabelAddresses.removeScope();
        }
    }

    private void interpretInlineAssembly(Inline instruction) {
            throw new ICodeVmException(instruction, this.programCounter, "Unexpected inline assembly in ICodeMachine. Method should be implemented within the machine itself");
        //Otherwise we just ignore the instruction
    }

    private void interpretAssignment(Assign assign){
        String place = assign.place;
        Object result = interpretExpression(assign.value, assign);
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

    private void interpretArrayAssign(ArrayAssign assign){
	String place = assign.ident;
	int res = (int)interpretExpression(assign.index, assign);
	if(variableValues.entryExists(place)){
	    VariableEntry entry = variableValues.getEntry(place);
	    Object arr = entry.getValue();
	    if(arr instanceof int[]){
		int[] iArr = (int[])arr;
		iArr[res] = (int)interpretExpression(assign.expression, assign);
	    } else if(arr instanceof float[]){
		float[] fArr = (float[])arr;
		fArr[res] = (float)interpretExpression(assign.expression, assign);
	    } else if(arr instanceof boolean[]){
		boolean[] bArr = (boolean[])arr;
		bArr[res] = (boolean)interpretExpression(assign.expression, assign);
	    } else if(arr instanceof char[]){
		char[] cArr = (char[])arr;
		cArr[res] = (char)interpretExpression(assign.expression, assign);
	    } else {
		throw new ICodeVmException(assign, programCounter, "No array type found for value");
	    }
	}
    }

    private void interpretDefinition(Def def){
        String place = def.label;
        Object result = interpretExpression(def.val, def);
        if(result != null){
            this.variableValues.addEntry(place, new VariableEntry(false, result));
        } else {
            throw new ICodeVmException(def, programCounter, "Right hand side of the definition resulted in a null value");
        }
    }

    private void interpretEndStatement(End end){
        throw new ProgramFinishedException();
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
        Object valueExp = interpretBinExp(statIf.exp, statIf);

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
	if(procedure.pname.equals("Multiply")){
	    Object argument1 = interpretExpression(procedure.params.get(0).val, procedure.params.get(0));
	    Object argument2 = interpretExpression(procedure.params.get(1).val, procedure.params.get(1));
	    tempReturnValue = ConversionUtils.toInt((ConversionUtils.toInt(argument1)) * (ConversionUtils.toInt(argument2)));
	    this.machineState = State.RETURN;
        } else if(procedure.pname.equals("RMul")) {
	    Object argument1 = interpretExpression(procedure.params.get(0).val, procedure.params.get(0));
	    Object argument2 = interpretExpression(procedure.params.get(1).val, procedure.params.get(1));
	    tempReturnValue = ConversionUtils.toReal((ConversionUtils.toReal(argument1) * ConversionUtils.toReal(argument2)));
	    this.machineState = State.RETURN;
        } else if(procedure.pname.equals("WriteChar")){
	    Def arg1 = procedure.params.get(0);
	    Object exp = interpretExpression(arg1.val, arg1);
            try{
                standardOutput.append("" + (char)exp);
		standardOutput.flush();
            } catch(IOException m){
                throw new ICodeVmException(arg1, programCounter, m.getMessage());
            }
	} else if(procedure.pname.equals("SkipChar")) {
	    try{
	      standardIn.skip(1);
	    } catch(Exception exp){}
	} else if(procedure.pname.equals("ReadChar")){
	    try {
		int val = standardIn.read();
		if(val <= -1) tempReturnValue = '\0';
	        else tempReturnValue = (char)val;
	    } catch(Exception exp) {
		throw new RuntimeException(exp);
	    }
            this.machineState = State.RETURN;  
        } else if(procedure.pname.equals("PeekChar")){
	    try {
		int val = standardIn.read();
		if(val <= -1) tempReturnValue = '\0';
		else {
		    char res = (char)val;
		    standardIn.unread(val);
		    tempReturnValue = res;
		}
	    } catch(Exception exp){
		throw new RuntimeException(exp);
	    }
	    this.machineState = State.RETURN;
	} else if(procedure.pname.equals("RealBinaryAsInt")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
		Object val = interpretExpression(arg1.val, arg1);
                Integer toRet = Float.floatToRawIntBits((float)val);
                tempReturnValue = toRet;
                machineState = State.RETURN;
             } else {
		throw new ICodeVmException(procedure, this.programCounter, "Not enough params in RealBinaryAsInt");
             }
        } else if(procedure.pname.equals("IntBinaryAsReal")){
            if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
		Object res = interpretExpression(arg1.val, arg1);
                Float toRet = Float.intBitsToFloat((int)res);
                tempReturnValue = toRet;
                machineState = State.RETURN;
            } else {
                throw new ICodeVmException(procedure, this.programCounter, "Not enough params in IntBinaryAsReal");
            }
        } else if(procedure.pname.equals("IntBinaryAsBool")){
	    if(procedure.params.size() == 1){
                Def arg1 = procedure.params.get(0);
		Object val = interpretExpression(arg1.val, arg1);
                tempReturnValue = ((int)val) != 0;
                machineState = State.RETURN;
             } else {
		throw new ICodeVmException(procedure, this.programCounter, "Not enough params in IntBinaryAsBool");
             }
	} else if(procedure.pname.equals("BoolBinaryAsInt")) {
		if(procedure.params.size() == 1){
		    Def arg1 = procedure.params.get(0);
                    Object res = interpretExpression(arg1.val, arg1);
                    tempReturnValue = (boolean)res ? 1 : 0;
                    machineState = State.RETURN;
		} else {
                        throw new ICodeVmException(procedure, this.programCounter, "Not enough params in BoolBinaryAsInt");
                }
	} else if(procedure.pname.equals("RealBinaryAsBool")) {
	        if(procedure.params.size() == 1){
		    Def arg1 = procedure.params.get(0);
                    Object res = interpretExpression(arg1.val, arg1);
                    tempReturnValue = (((float)res) != 0);
                    machineState = State.RETURN;
		} else {
                        throw new ICodeVmException(procedure, this.programCounter, "Not enough params in RealBinaryAsBool");
                }
	} else if(procedure.pname.equals("BoolBinaryAsReal")) {
	        if(procedure.params.size() == 1){
		    Def arg1 = procedure.params.get(0);
                    Object res = interpretExpression(arg1.val, arg1);
                    tempReturnValue = (float)(((boolean)res) ? 1.0 : 0.0);
                    machineState = State.RETURN;
		} else {
                        throw new ICodeVmException(procedure, this.programCounter, "Not enough params in BoolBinaryAsReal");
                }
	} else if(procedure.pname.equals("IntBinaryAsChar")) {
	        if(procedure.params.size() == 1){
		    Def arg1 = procedure.params.get(0);
                    Object res = interpretExpression(arg1.val, arg1);
                    tempReturnValue = (char)(int)(res);
                    machineState = State.RETURN;
		} else {
                        throw new ICodeVmException(procedure, this.programCounter, "Not enough params in IntBinaryAsChar");
                }
	} else if(procedure.pname.equals("CharBinaryAsInt")) {
	    if(procedure.params.size() == 1){
		    Def arg1 = procedure.params.get(0);
                    Object res = interpretExpression(arg1.val, arg1);
                    tempReturnValue = (int)(char)(res);
                    machineState = State.RETURN;
		} else {
                        throw new ICodeVmException(procedure, this.programCounter, "Not enough params in CharBinaryAsInt");
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
                        argVals.add(argSource.copy());
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

    private Object interpretExpression(Exp expression, ICode icode){
        if(expression instanceof BinExp) return interpretBinExp((BinExp)expression, icode);
        else if(expression instanceof UnExp) return interpretUnExp((UnExp)expression, icode);
        else if(expression instanceof IdentExp) return interpretIdentExp((IdentExp)expression, icode);
	else if(expression instanceof ArrayAccess) return interpretArrayAccess((ArrayAccess)expression, icode);
	else if(expression instanceof CharArrayExp) return interpretCharArrayExp((CharArrayExp)expression, icode);
	else if(expression instanceof BoolArrayExp) return interpretBoolArrayExp((BoolArrayExp)expression, icode);
	else if(expression instanceof IntArrayExp) return interpretIntArrayExp((IntArrayExp)expression, icode);
	else if(expression instanceof RealArrayExp) return interpretRealArrayExp((RealArrayExp)expression, icode);
        else if(expression.isConstant()) return ConversionUtils.getValue(expression);
        else {
        	throw new ICodeVmException(icode, this.programCounter, "Error unexpected Expression Found");
        }
    }

    private Object interpretCharArrayExp(CharArrayExp arrExp, ICode icode){
	return arrExp.getValue();
    }

    private Object interpretBoolArrayExp(BoolArrayExp bExp, ICode icode){
	return bExp.getValue();
    }

    private Object interpretIntArrayExp(IntArrayExp aExp, ICode icode){
	return aExp.getValue();
    }

    private Object interpretRealArrayExp(RealArrayExp rExp, ICode icode){
	return rExp.getValue();
    }

    private Object interpretArrayAccess(ArrayAccess expression, ICode icode){
	if(variableValues.entryExists(expression.name)){
	    VariableEntry entry = variableValues.getEntry(expression.name);
	    Exp index = expression.index;
	    Object inRes = (int)interpretExpression(index, icode);
	    if(entry.getValue() instanceof char[]){
		char[] cRes = (char[])entry.getValue();
		return cRes[(int)inRes];
	    } else if(entry.getValue() instanceof int[]){
		int[] iRes = (int[])entry.getValue();
		return iRes[(int)inRes];
	    } else if(entry.getValue() instanceof float[]){
		float[] fRes = (float[])entry.getValue();
		return fRes[(int)inRes];
	    } else if(entry.getValue() instanceof boolean[]){
		boolean[] bRes = (boolean[])entry.getValue();
		return bRes[(int)inRes];
	    } else {
		throw new ICodeVmException(expression, this.programCounter, "Error invalid array type found for " + icode.toString());
	    }
	} else {
	    throw new ICodeVmException(expression, this.programCounter, "Error array not found for " + icode.toString());
	}
    }

    private Object interpretIdentExp(IdentExp exp, ICode icode){
        if(variableValues.entryExists(exp.ident)){
            VariableEntry entry = variableValues.getEntry(exp.ident);
            if(entry != null){
                return entry.getValue();
            } else {
                throw new ICodeVmException(exp, this.programCounter, "Error entry for " + exp + " is null in instruction " + icode);
            }
        } else {
            throw new ICodeVmException(icode, this.programCounter, "Error cant find value for variable " + exp.ident + " in instruction " + icode);
        }
    }

    private Object interpretUnExp(UnExp expression, ICode icode){
        Object right = null;

        if(expression.right.isConstant()){
            right = ConversionUtils.getValue(expression.right);
        } else if(expression.right instanceof IdentExp) {
            IdentExp expressionRight = (IdentExp)expression.right;
            if(variableValues.entryExists(expressionRight.ident)){
                VariableEntry entry = variableValues.getEntry(expressionRight.ident);
                right = entry.getValue();
            } else {
                throw new ICodeVmException(expression, programCounter, "Variable on right hand side of unary expression does not exist in instruction " + icode);
            }
        } else {
            throw new ICodeVmException(expression, programCounter, "In expression " + expression + " cant find value on right hand side of the expression " + expression.right.toString() + " inside instruction " + icode);
        }

        if(right != null){
            switch(expression.op){
                case BNOT: return OpUtil.not(right);
                case INOT: return OpUtil.bitwiseNot(right);
                default: 
                    throw new ICodeVmException(expression, programCounter, "Unknown unary operation " + expression + "inside instruction " + icode);
            }
        } else {
            throw new ICodeVmException(expression, programCounter, "Right hand side value inary unary operation is a Null value in instruction " + icode);
        }
    }

    private Object interpretBinExp(BinExp expression, ICode icode){
        Object left = null;
        Object right = null;
        IdentExp leftIdent = expression.left;
        if(variableValues.entryExists(leftIdent.ident)){
            VariableEntry entry = variableValues.getEntry(leftIdent.ident);
            left = entry.getValue();
        } else {
            throw new ICodeVmException(expression, this.programCounter, "In Expression " + expression + " cant find value on the left hand side of the expression " + expression.left.toString() + " inside instruction " + icode);
        }

        IdentExp rightIdent = expression.right;
        if(variableValues.entryExists(rightIdent.ident)){
            VariableEntry entry = variableValues.getEntry(rightIdent.ident);
            right = entry.getValue();
        } else {
            throw new ICodeVmException(expression, this.programCounter, "In Expression " + expression + " cant find value on the right hand side of the expression " + expression.right.toString() + " inside instruction " + icode);
        }

	if(!(left instanceof Boolean) && !(left instanceof Integer)){
	    throw new ICodeVmException(expression, this.programCounter, "In Expression " + leftIdent + " in binary opration  " + expression + " found type of  " + left.getClass().getSimpleName());
	}

	if(!(right instanceof Boolean) && !(right instanceof Integer)){
	    throw new ICodeVmException(expression, this.programCounter, "In Expression " + rightIdent + " in binary opration  " + expression + " found type of  " + right.getClass().getSimpleName());
	}

        if(left != null && right != null){
            switch(expression.op){
                case IADD: return OpUtil.iAdd(left, right);
                case ISUB: return OpUtil.iSub(left, right);
                case IAND: return OpUtil.bitwiseAnd(left, right);
                case IOR: return OpUtil.bitwiseOr(left, right);
                case IXOR: return OpUtil.bitwiseXor(left, right);
                case ILSHIFT: return OpUtil.leftShift(left, right);
                case IRSHIFT: return OpUtil.rightShift(left, right);
                case BEQ: return OpUtil.equal(left, right);
                case BNE: return OpUtil.notEqual(left, right);
                case IEQ: return OpUtil.equal(left, right);
                case INE: return OpUtil.notEqual(left, right);
                case GE: return OpUtil.greaterThanOrEqualTo(left, right);
                case GT: return OpUtil.greaterThan(left, right);
                case LE: return OpUtil.lessThanOrEqualTo(left, right);
                case LT: return OpUtil.lessThan(left, right);
                case LAND: return OpUtil.and(left, right);
                case LOR: return OpUtil.or(left, right);
                default:
                    throw new ICodeVmException(expression, this.programCounter, "Unknown Binary operation inside icode " + icode.toString()); 
            }
        } else {
            throw new ICodeVmException(expression, programCounter, "left or right = null in instruction " + icode);
        }
    }
}


