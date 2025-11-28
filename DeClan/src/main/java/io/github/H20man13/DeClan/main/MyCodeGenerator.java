package io.github.H20man13.DeClan.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.position.Position;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.iterative.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.analysis.iterative.RegisterAllocatorAnalysis;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.Pattern;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmAddressOffsets;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterResult;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator.VariableLength;
import io.github.H20man13.DeClan.common.ast.VariableDeclaration;
import io.github.H20man13.DeClan.common.exception.CodeGeneratorException;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Lib.SymbolSearchStrategy;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.Spill;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.inline.InlineParam;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;

public class MyCodeGenerator {
	private Lib intermediateCode;
	private RegisterAllocatorAnalysis rGen;
	private ArmCodeGenerator cGen;
	private IrRegisterGenerator iGen;
	private ErrorLog errorLog;
	private Map<P, Callable<Void>> codeGenFunctions;
	private FlowGraph flow;
	private ArmAddressOffsets offset;
	private int i;
	private int offsetNum;

	public MyCodeGenerator(String outputFile, Lib program, MyOptimizer opt, ErrorLog errLog, Config cfg) throws IOException {
		this.intermediateCode = program;
		this.offset = new ArmAddressOffsets();
		this.cGen = new ArmCodeGenerator(outputFile);
		this.rGen = new RegisterAllocatorAnalysis(opt, cfg);
		this.iGen = new IrRegisterGenerator();
		String place;
		do {
			place = iGen.genNext();
		} while (program.containsPlace(place));
		this.errorLog = errLog;
		this.codeGenFunctions = new HashMap<>();
		this.i = 0;
		this.offsetNum = 0;
		initCodeGenFunctions();
	}

	private boolean genICode(ICode icode1, ICode icode2) throws Exception {
		P possibleTwoStagePattern = P.PAT(icode1.asPattern(), icode2.asPattern());
		if (codeGenFunctions.containsKey(possibleTwoStagePattern)) {
			Callable<Void> codeGenFunction = codeGenFunctions.get(possibleTwoStagePattern);
			codeGenFunction.call();
			i+=2;
			return true;
		}
		return false;
	}

	private boolean genICode(ICode icode) throws Exception {
		P oneStagePattern = icode.asPattern();

		if (codeGenFunctions.containsKey(oneStagePattern)) {
			codeGenFunctions.get(oneStagePattern).call();
			i++;
			return true;
		} else {
			i++;
			errorLog.add("Pattern \n\n" + oneStagePattern.toString() + "\n\n" + "not found", new Position(i, 0));
			return false;
		}
	}
	
	private void skipSymbolTable() {
		i = 0;
		ICode instruction = intermediateCode.getInstruction(i);
		while(!(instruction instanceof DataSec)) {
			i++;
			instruction = intermediateCode.getInstruction(i);
		}
	}

	public void codeGen() throws Exception {
		int size = intermediateCode.getSize();

		skipSymbolTable();
		
		while (i < size) {
			ICode icode1 = intermediateCode.getInstruction(i);
			if (i + 1 < size) {
				ICode icode2 = intermediateCode.getInstruction(i + 1);
				if (!genICode(icode1, icode2) && !genICode(icode1)) {
					errorLog.add("Error cannot generate icode " + icode1, new Position(i, 0));
				}
			} else if (!genICode(icode1)) {
				errorLog.add("Error cannot generate icode " + icode1, new Position(i, 0));
			}
		}

		cGen.writeToStream();
	}

	private void initCodeGenFunctions() {
		// Init Proc with Return Pattern
		initCallWithReturn0();
		initCallWithReturn1();
		initCallWithReturn2();
		initCallWithReturn3();
		initCallWithReturn4();
		initCallWithReturn5();
		initCallWithReturn6();
		initCallWithReturn7();

		// Init Add Patterns
		initAdd0();
		initAdd1();

		// Init Sub Patterns
		initSub0();
		initSub1();

		// Init bitwise And patterns
		initBitwiseAnd0();
		initBitwiseAnd1();

		// Init bitwise Or patterns
		initBitwiseOr0();
		initBitwiseOr1();

		// Init Xor Patterns
		initBitwiseExclusiveOr0();
		initBitwiseExclusiveOr1();

		// Init Left Shift patterns
		initBitShiftLeft0();
		initBitShiftLeft1();

		// Init Right Shift patterns
		initBitShiftRight0();
		initBitShiftRight1();

		// Init Ge patterns
		initGe0();
		initGe1();

		// Init Gt Patterns
		initGt0();
		initGt1();

		// Init Lt Patterns
		initLt0();
		initLt1();

		// Init Le Patterns
		initLe0();
		initLe1();

		// Init ieq Patterns
		initIEq0();
		initIEq1();

		// Initialize the ine Patterns
		initINe0();
		initINe1();
		
		//Init boolean eq patterns
		initBEq0();
		initBEq1();
		
		//Init boolean ne patterns
		initBNe0();
		initBNe1();
		
		// Initialize Logical And Patterns
		initAnd0();
		initAnd1();

		// Initialize Logical Or Patterns
		initOr0();
		initOr1();

		// Initiaize the BNot patterns
		initBnot0();
		initBnot1();

		// Init bitwise Not patterns
		initBitwiseNot0();
		initBitwiseNot1();
		

		// Initialize Bool Constant Patterns
		initBool0();
		initBool1();
		
		// Initialize Real Constant Patterns
		initReal0();
		initReal1();
		
		// Initialize Int Constant Patterns
		initInt0();
		initInt1();
		
		// Initialize String Constant Patterns
		initStr0();
		initStr1();
		
		// Initialize Id Patterns
		initId0();
		initId1();
		initId2();
		initId3();
		initId4();
		initId5();
		initId6();
		initId7();

		// Initialize If Statement Patterns
		initIf0();
		initIf1();
		initIf2();
		initIf3();
		initIf4();
		initIf5();
		initIf6();
		initIf7();

		// Init Goto Pattern
		initGoto0();

		// Init Label Pattern
		initLabel0();

		// Init Proc Label Pattern
		initProcLabel0();
		
		//Init header patterns
		initDataSectionHeader();
		initBssSectionHeader();
		initCodeSectionHeader();
		initProcSectionHeader();
		
		//Init Spill Patterns
		initSpill0();
		initSpill1();
		initSpill2();
		initSpill3();

		// Init End Pattern
		initEnd0();

		// Init Return Pattern
		initReturn0();

		// Init Proc Pattern
		initCall0();

		// Init Inline Assembly Pattern
		initInline0();
	}

	private String loadVariableToReg(IdentExp exp, ICode.Type type, ArmRegisterResult pre, ArmRegisterResult res) throws IOException{
		if(!pre.containsRegister(exp.ident) && res.containsRegister(exp.ident)){
			if (exp.scope == Scope.LOCAL) {
			    ArmRegisterResult regs = res;
			    int actualOffset = offset.findOffset(exp.ident, type);
			    
			    String reg = regs.getRegister(exp.ident);
				if (type == ICode.Type.BOOL) {
					if(actualOffset > 250) {
						cGen.addVariable("offset" + offsetNum, actualOffset);
						cGen.addInstruction("LDR " + reg + ", " + "offset" + offsetNum);
						cGen.addInstruction("LDRB " + reg + ", [R13, -" + reg + ']');
						this.offsetNum++;
					} else {
						cGen.addInstruction("LDRB " + reg + ", [R13, -#" + actualOffset + ']');
					}
				} else {
					if(actualOffset > 250) {
						cGen.addVariable("offset" + offsetNum, actualOffset);
						cGen.addInstruction("LDR " + reg + ", " + "offset" + offsetNum);
						cGen.addInstruction("LDR " + reg + ", [R13, -" + reg + ']');
						this.offsetNum++;
					} else {
						cGen.addInstruction("LDR " + reg + ", [R13, -#" + actualOffset + ']');
					}
				}
				return reg;
			} else if (exp.scope == Scope.PARAM) {
				ArmRegisterResult regs = res;
				int actualOffset = offset.findOffset(exp.ident, type);
				String reg = regs.getRegister(exp.ident);
				if (type == ICode.Type.BOOL) {
					if(actualOffset > 250) {
						cGen.addVariable("offset" + offsetNum, actualOffset);
						cGen.addInstruction("LDR " + reg + ", " + "offset" + offsetNum);
						cGen.addInstruction("LDRB " + reg + ", [R13, -" + reg + ']');
						this.offsetNum++;
					} else {
						cGen.addInstruction("LDRB " + reg + ", [R13, -#" + actualOffset + ']');
					}
				} else {
					if(actualOffset > 250) {
						cGen.addVariable("offset" + offsetNum, actualOffset);
						cGen.addInstruction("LDR " + reg + ", " + "offset" + offsetNum);
						cGen.addInstruction("LDR " + reg + ", [R13, -" + reg + ']');
						this.offsetNum++;
					} else {
						cGen.addInstruction("LDR " + reg + ", [R13, -#" + actualOffset + ']');
					}
				}
				return reg;
			} else if (exp.scope == Scope.RETURN) {
				ArmRegisterResult regs = res;
				
				int actualOffset = offset.findOffset(exp.ident, type);
				String reg = regs.getRegister(exp.ident);
				
				if (type == ICode.Type.BOOL) {
					if(actualOffset > 250) {
						cGen.addVariable("offset" + offsetNum, actualOffset);
						cGen.addInstruction("LDR " + reg + ", " + "offset" + offsetNum);
						cGen.addInstruction("LDRB " + reg + ", [R13, -" + reg + ']');
						this.offsetNum++;
					} else {
						cGen.addInstruction("LDRB " + reg + ", [R13, -#" + actualOffset + ']');
					}
				} else {
					if(actualOffset > 250) {
						cGen.addVariable("offset" + offsetNum, actualOffset);
						cGen.addInstruction("LDR " + reg + ", " + "offset" + offsetNum);
						cGen.addInstruction("LDR " + reg + ", [R13, -" + reg + ']');
						this.offsetNum++;
					} else {
						cGen.addInstruction("LDR " + reg + ", [R13, -#" + actualOffset + ']');
					}
				}
				return reg;
			} else {
				ArmRegisterResult newReg = res;
				String register = newReg.getRegister(exp.ident);
				if (type == ICode.Type.BOOL) {
					cGen.addInstruction("LDRB " + register + ", " + exp.ident);
				} else {
					cGen.addInstruction("LDR " + register + ", " + exp.ident);
				}
				return register;
			}
		}
		return res.getRegister(exp.ident);
	}

	private String genUnaryOp(String label, UnExp.Operator op, String rightReg, ArmRegisterResult res) throws Exception {
		String reg = res.getRegister(label);
		
		switch (op) {
		case BNOT:
			cGen.addInstruction("TEQ " + rightReg + ", #0");
			cGen.addInstruction("MOVEQ " + reg + ", #1");
			cGen.addInstruction("MOVNE " + reg + ", #0");
			break;
		case INOT:
			cGen.addInstruction("MVN " + reg + ", " + rightReg);
			break;
		default:
			throw new CodeGeneratorException("callWithReturn0", "Error cant load register from unarty operation " + op);
		}
		return reg;
	}

	private String genBinaryOp(String label, String leftReg, BinExp.Operator op, String rightReg, ArmRegisterResult res) throws Exception {
		String destReg = res.getRegister(label);
		switch (op) {
		case IADD:
			cGen.addInstruction("ADD " + destReg + ", " + leftReg + ", " + rightReg);
			break;
		case ISUB:
			cGen.addInstruction("SUB " + destReg + ", " + leftReg + ", " + rightReg);
			break;
		case IAND:
			cGen.addInstruction("AND " + destReg + ", " + leftReg + ", " + rightReg);
			break;
		case IOR:
			cGen.addInstruction("ORR " + destReg + ", " + leftReg + ", " + rightReg);
			break;
		case IXOR:
			cGen.addInstruction("EOR " + destReg + ", " + leftReg + ", " + rightReg);
			break;
		case IEQ:
			cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVEQ " + destReg + ", #1");
			cGen.addInstruction("MOVNE " + destReg + ", #0");
			break;
		case INE:
			cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVNE " + destReg + ", #1");
			cGen.addInstruction("MOVEQ " + destReg + ", #0");
			break;
		case GE:
			cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVGE " + destReg + ", #1");
			cGen.addInstruction("MOVLT " + destReg + ", #0");
			break;
		case GT:
			cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVGT " + destReg + ", #1");
			cGen.addInstruction("MOVLE " + destReg + ", #0");
			break;
		case LE:
			cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVLE " + destReg + ", #1");
			cGen.addInstruction("MOVGT " + destReg + ", #0");
			break;
		case LT:
			cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVLT " + destReg + ", #1");
			cGen.addInstruction("MOVGE " + destReg + ", #0");
			break;
		case ILSHIFT:
			cGen.addInstruction("MOV " + destReg + ", " + leftReg + ", LSL " + rightReg);
			break;
		case IRSHIFT:
			cGen.addInstruction("MOV " + destReg + ", " + leftReg + ", ASR " + rightReg);
			break;
		case BEQ:
			cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVEQ " + destReg + ", #1");
			cGen.addInstruction("MOVNE " + destReg + ", #0");
			break;
		case BNE:
			cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
			cGen.addInstruction("MOVNE " + destReg + ", #1");
			cGen.addInstruction("MOVEQ " + destReg + ", #0");
			break;
		case LAND:
			cGen.addInstruction("AND " + destReg + ", " + leftReg + ", " + rightReg);
			break;
		case LOR:
			cGen.addInstruction("ORR " + destReg + ", " + leftReg + ", " + rightReg);
			break;
		default:
			throw new CodeGeneratorException("initCallWithReturn0",
					"Unexpected/Unsupported Operation type " + op + " when building opoeration assembly");
		}
		return destReg;
	}

	private int getStackFrameLength(Call call, Def def) {
		int totalLength = 4;
		this.offset.pushFrame();
		this.offset.pushAddress("returnAddr", ICode.Type.INT);

		if(def.type == ICode.Type.BOOL){
			if(def.val instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.val;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.BOOL);
					totalLength += 1;
				}
			}
			totalLength += 1;
		} else if(def.type == ICode.Type.INT) {
			if(def.val instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.val;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.INT);
					totalLength += 4;
				}
			}
		} else if(def.type == ICode.Type.REAL) {
			if(def.val instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.val;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.REAL);
					totalLength += 4;
				}
			}
		} else if(def.type == ICode.Type.STRING) {
			if(def.val instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.val;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.STRING);
					totalLength += 4;
				}
			}
		} else {
			throw new CodeGeneratorException("getStackFrameCall", "Error unexpected return type found");
		}

		for (Def param : call.params) {
			if(param.type == ICode.Type.BOOL){
				this.offset.pushAddress(param.label, ICode.Type.BOOL);
				totalLength += 1;
			} else if(param.type == ICode.Type.INT) {
				this.offset.pushAddress(param.label, ICode.Type.INT);
				totalLength += 4;
			} else if(param.type == ICode.Type.REAL) {
				this.offset.pushAddress(param.label, ICode.Type.REAL);
				totalLength += 4;
			} else if(param.type == ICode.Type.STRING){
				this.offset.pushAddress(param.label, ICode.Type.STRING);
				totalLength += 4;
			} else {
				throw new CodeGeneratorException("getStackFrameCall", "Error unexpected param type found");
			}
		}

		return totalLength;
	}
	
	private int getStackFrameLength(Call call) {
		int totalLength = 4;
		this.offset.pushFrame();
		
		this.offset.pushAddress("returnAddr", ICode.Type.INT);

		for (Def param : call.params) {
			if (param.type == ICode.Type.BOOL) {
				this.offset.pushAddress(param.label, ICode.Type.BOOL);
				totalLength += 1; // A boolean value of 8 bits/1 byte
			} else if(param.type == ICode.Type.REAL){
				this.offset.pushAddress(param.label, ICode.Type.REAL);
				totalLength += 4; // A word sized value(INT,REAL,STRING<-address) value of 32 bits/4 bytes
			} else if(param.type == ICode.Type.INT) {
				this.offset.pushAddress(param.label, ICode.Type.INT);
				totalLength += 4;
			} else if(param.type == ICode.Type.STRING) {
				this.offset.pushAddress(param.label, ICode.Type.INT);
			}
		}

		return totalLength;
	}

	private int getStackFrameLength(Call call, Assign def) {
		int totalLength = 4;
		this.offset.pushFrame();
		this.offset.pushAddress("returnAddr", ICode.Type.INT);

		if(def.getType() == ICode.Type.BOOL){
			if(def.value instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.value;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.BOOL);
					totalLength += 1;
				}
			}
			totalLength += 1;
		} else if(def.getType() == ICode.Type.INT) {
			if(def.value instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.value;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.INT);
					totalLength += 4;
				}
			}
		} else if(def.getType() == ICode.Type.REAL) {
			if(def.value instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.value;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.REAL);
					totalLength += 4;
				}
			}
		} else if(def.getType() == ICode.Type.STRING) {
			if(def.value instanceof IdentExp) {
				IdentExp identVal = (IdentExp)def.value;
				if(identVal.scope == ICode.Scope.RETURN) {
					this.offset.pushAddress(identVal.ident, ICode.Type.STRING);
					totalLength += 4;
				}
			}
		} else {
			throw new CodeGeneratorException("getStackFrameCall", "Error unexpected return type found");
		}

		for (Def param : call.params) {
			if(param.type == ICode.Type.BOOL){
				this.offset.pushAddress(param.label, ICode.Type.BOOL);
				totalLength += 1;
			} else if(param.type == ICode.Type.INT) {
				this.offset.pushAddress(param.label, ICode.Type.INT);
				totalLength += 4;
			} else if(param.type == ICode.Type.REAL) {
				this.offset.pushAddress(param.label, ICode.Type.REAL);
				totalLength += 4;
			} else if(param.type == ICode.Type.STRING){
				this.offset.pushAddress(param.label, ICode.Type.STRING);
				totalLength += 4;
			} else {
				throw new CodeGeneratorException("getStackFrameCall", "Error unexpected param type found");
			}
		}

		return totalLength;
	}

	private String genExpression(Exp expression, ICode.Type returnType, ArmRegisterResult pre, ArmRegisterResult res) throws Exception {
		if (expression instanceof IdentExp) {
			IdentExp exp = (IdentExp) expression;
			return loadVariableToReg(exp, returnType, pre, res);
		} else if (expression instanceof IntExp) {
			IntExp exp = (IntExp) expression;
			String newPlace = iGen.genNext();
			cGen.addVariable(newPlace, VariableLength.WORD, exp.value);
			String newReg = res.getRegister(newPlace);
			cGen.addInstruction("LDR " + newReg + ", " + newPlace);
			return newReg;
		} else if (expression instanceof RealExp) {
			RealExp exp = (RealExp) expression;
			String newPlace = iGen.genNext();
			cGen.addVariable(newPlace, exp.realValue);
			String newReg = res.getRegister(newPlace);
			cGen.addInstruction("LDR " + newReg + ", " + newPlace);
			return newReg;
		} else if (expression instanceof BoolExp) {
			BoolExp exp = (BoolExp) expression;
			String newPlace = iGen.genNext();
			cGen.addVariable(newPlace, VariableLength.BYTE, exp.trueFalse ? 1 : 0);
			String newReg = res.getRegister(newPlace);
			cGen.addInstruction("LDRB " + newReg + ", " + newPlace);
			return newReg;
		} else if (expression instanceof StrExp) {
			StrExp exp = (StrExp) expression;
			String newPlace = iGen.genNext();
			for (int i = 0; i < exp.value.length(); i++) {
				char letter = exp.value.charAt(i);
				if (i == 0)
					cGen.addVariable(newPlace + "_value", VariableLength.BYTE, (int) letter);
				else
					cGen.addVariable(VariableLength.BYTE, (int) letter);
			}
			cGen.addVariable(VariableLength.BYTE, (int) '\0');
			cGen.addVariable(newPlace, VariableLength.WORD, newPlace + "_value");
			return res.getRegister(newPlace);
		} else if (expression instanceof BinExp) {
			BinExp exp = (BinExp) expression;

			String leftReg = null;
			String rightReg = null;

			if (exp.op == BinExp.Operator.IEQ || exp.op == BinExp.Operator.INE || exp.op == BinExp.Operator.IADD
					|| exp.op == BinExp.Operator.ISUB || exp.op == BinExp.Operator.GE || exp.op == BinExp.Operator.GT
					|| exp.op == BinExp.Operator.LT || exp.op == BinExp.Operator.LE || exp.op == BinExp.Operator.IAND
					|| exp.op == BinExp.Operator.IOR || exp.op == BinExp.Operator.IXOR
					|| exp.op == BinExp.Operator.ILSHIFT || exp.op == BinExp.Operator.IRSHIFT) {
				leftReg = loadVariableToReg(exp.left, ICode.Type.INT, pre, res);
				rightReg = loadVariableToReg(exp.right, ICode.Type.INT, pre, res);
			} else if (exp.op == BinExp.Operator.LAND || exp.op == BinExp.Operator.LOR || exp.op == BinExp.Operator.BEQ
					|| exp.op == BinExp.Operator.BNE) {
				leftReg = loadVariableToReg(exp.left, ICode.Type.BOOL, pre, res);
				rightReg = loadVariableToReg(exp.right, ICode.Type.BOOL, pre, res);
			} else {
				throw new CodeGeneratorException("genExpression", "Unexpected/Unsupported Operation type " + exp.op);
			}

			String newPlace = iGen.genNext();
			return genBinaryOp(newPlace, leftReg, exp.op, rightReg, res);
		} else if (expression instanceof UnExp) {
			UnExp exp = (UnExp) expression;

			String rightReg = null;
			if (exp.op == UnExp.Operator.BNOT) {
				rightReg = loadVariableToReg(exp.right, ICode.Type.BOOL, pre, res);
			} else if (exp.op == UnExp.Operator.INOT) {
				rightReg = loadVariableToReg(exp.right, ICode.Type.INT, pre, res);
			} else {
				throw new CodeGeneratorException("genExpression",
						"Error cant load register from unarty operation " + exp.op);
			}
			String newPlace = iGen.genNext();
			return genUnaryOp(newPlace, exp.op, rightReg, res);
		} else {
			throw new CodeGeneratorException("genExpression",
					"Unsopported Exp found " + expression.getClass().getSimpleName());
		}
	}

	private void initCallWithReturn0() {
		codeGenFunctions.put(Pattern.callWithReturn0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Def returnPlacement = (Def) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(procICode);
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, ICode.Type.BOOL);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult preRes2 = rGen.getFilteredInputSet(returnPlacement);
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.val, returnPlacement.type, preRes2, res2);
				cGen.addInstruction("STRB " + temp + ", [R13, -" + offset.findOffset(returnPlacement.label, returnPlacement.type) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset(temp, returnPlacement.type) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}

	private void initCallWithReturn1() {
		codeGenFunctions.put(Pattern.callWithReturn1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Assign returnPlacement = (Assign) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(procICode);
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, ICode.Type.BOOL);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult pre2 = rGen.getFilteredInputSet(returnPlacement);
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.value, returnPlacement.getType(), pre2, res2);
				cGen.addInstruction("STRB " + temp + ", [R13, -" + offset.findOffset(returnPlacement.place, returnPlacement.getType()) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset(temp, returnPlacement.getType()) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}

	private void initCallWithReturn2() {
		codeGenFunctions.put(Pattern.callWithReturn2, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Def returnPlacement = (Def) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre1 = rGen.getFilteredInputSet(procICode);
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre1, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, ICode.Type.BOOL);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, ICode.Type.INT);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult pre2 = rGen.getFilteredInputSet(returnPlacement);
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.val, returnPlacement.type, pre2, res2);
				cGen.addInstruction("STR " + temp + ", [R13, -" + offset.findOffset(returnPlacement.label, returnPlacement.type) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset("returnAddr", ICode.Type.INT) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}

	private void initCallWithReturn3() {
		codeGenFunctions.put(Pattern.callWithReturn3, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Assign returnPlacement = (Assign) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre1 = rGen.getFilteredInputSet(procICode);
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre1, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult pre2 = rGen.getFilteredInputSet(returnPlacement);
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.value, returnPlacement.getType(), pre2, res2);
				cGen.addInstruction("STR " + temp + ", [R13, -" + offset.findOffset(returnPlacement.place, returnPlacement.getType()) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset(temp, ICode.Type.INT) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}

	private void initCallWithReturn4() {
		codeGenFunctions.put(Pattern.callWithReturn4, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Def returnPlacement = (Def) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre1 = rGen.getFilteredInputSet(retICode);
				
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre1, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, ICode.Type.BOOL);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult pre2 = rGen.getFilteredInputSet(returnPlacement);
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.val, returnPlacement.type, pre2, res2);
				cGen.addInstruction("STR " + temp + ", [R13, -" + offset.findOffset(returnPlacement.label, returnPlacement.type) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset(temp, ICode.Type.INT) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}

	private void initCallWithReturn5() {
		codeGenFunctions.put(Pattern.callWithReturn5, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Assign returnPlacement = (Assign) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre1 = rGen.getFilteredInputSet(procICode);
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre1, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, ICode.Type.BOOL);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult pre2 = rGen.getFilteredInputSet(returnPlacement);
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.value, returnPlacement.getType(), pre2, res2);
				cGen.addInstruction("STR " + temp + ", [R13, -" + offset.findOffset(returnPlacement.place, returnPlacement.getType()) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset(temp, ICode.Type.INT) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}
	
	private void initCallWithReturn6() {
		codeGenFunctions.put(Pattern.callWithReturn6, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Def returnPlacement = (Def) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre1 = rGen.getFilteredInputSet(procICode);
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre1, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, ICode.Type.BOOL);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult pre2 = rGen.getFilteredInputSet(returnPlacement);
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.val, returnPlacement.type, pre2, res2);
				cGen.addInstruction("STR " + temp + ", [R13, -" + offset.findOffset(returnPlacement.label, returnPlacement.type) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset(temp, ICode.Type.INT) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}

	private void initCallWithReturn7() {
		codeGenFunctions.put(Pattern.callWithReturn7, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.getInstruction(i + 1);
				Assign returnPlacement = (Assign) retICode;
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre1 = rGen.getFilteredInputSet(procICode);
				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre1, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				ArmRegisterResult res2 = rGen.getFilteredOutputSet(returnPlacement);
				ArmRegisterResult pre2 = rGen.getFilteredInputSet(returnPlacement);
				
				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp)returnPlacement.value, returnPlacement.getType(), pre2, res2);
				cGen.addInstruction("STR " + temp + ", [R13, -" + offset.findOffset(returnPlacement.place, returnPlacement.getType()) + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, [R13, #-"+ offset.findOffset(temp, ICode.Type.INT) +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}

	private void initAdd0() {
		codeGenFunctions.put(Pattern.add0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.label);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("ADD " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initAdd1() {
		codeGenFunctions.put(Pattern.add1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.place);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("ADD " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initSub0() {
		codeGenFunctions.put(Pattern.sub0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.label);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("SUB " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + destReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initSub1() {
		codeGenFunctions.put(Pattern.sub1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.place);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("ADD " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STR " + destReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initBitwiseAnd0() {
		codeGenFunctions.put(Pattern.bitwiseAnd0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.label);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("AND " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitwiseAnd1() {
		codeGenFunctions.put(Pattern.bitwiseAnd1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.place);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("AND " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitwiseOr0() {
		codeGenFunctions.put(Pattern.bitwiseOr0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.label);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("ORR " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitwiseOr1() {
		codeGenFunctions.put(Pattern.bitwiseOr1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.place);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("ORR " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitwiseExclusiveOr0() {
		codeGenFunctions.put(Pattern.bitwiseExclusiveOr0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.label);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("EOR " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitwiseExclusiveOr1() {
		codeGenFunctions.put(Pattern.add1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.place);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("EOR " + destReg + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitShiftLeft0() {
		codeGenFunctions.put(Pattern.leftShift0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.label);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("MOV " + destReg + ", " + leftReg + ", LSL " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitShiftLeft1() {
		codeGenFunctions.put(Pattern.leftShift1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.place);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("MOV " + destReg + ", " + leftReg + ", LSL " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitShiftRight0() {
		codeGenFunctions.put(Pattern.rightShift0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.label);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("MOV " + destReg + ", " + leftReg + ", ASR " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitShiftRight1() {
		codeGenFunctions.put(Pattern.rightShift1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult regs = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;
				
				String destReg = regs.getRegister(assignICode.place);
				String leftReg = regs.getRegister(leftIdent.ident);
				String rightReg = regs.getRegister(rightIdent.ident);

				cGen.addInstruction("MOV " + destReg + ", " + leftReg + ", ASR " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + destReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg+ ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + destReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initGt0() {
		codeGenFunctions.put(Pattern.gt0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGT " + finalPlace + ", #1");
				cGen.addInstruction("MOVLE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initGt1() {
		codeGenFunctions.put(Pattern.gt1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGT " + finalPlace + ", #1");
				cGen.addInstruction("MOVLE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initGe0() {
		codeGenFunctions.put(Pattern.ge0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGE " + finalPlace + ", #1");
				cGen.addInstruction("MOVLT " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initGe1() {
		codeGenFunctions.put(Pattern.ge1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGE " + finalPlace + ", #1");
				cGen.addInstruction("MOVLT " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initLt0() {
		codeGenFunctions.put(Pattern.lt0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLT " + finalPlace + ", #1");
				cGen.addInstruction("MOVGE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initLt1() {
		codeGenFunctions.put(Pattern.lt1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLT " + finalPlace + ", #1");
				cGen.addInstruction("MOVGE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initLe0() {
		codeGenFunctions.put(Pattern.le0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLE " + finalPlace + ", #1");
				cGen.addInstruction("MOVGT " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initLe1() {
		codeGenFunctions.put(Pattern.le1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLE " + finalPlace + ", #1");
				cGen.addInstruction("MOVGT " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initIEq0() {
		codeGenFunctions.put(Pattern.iEq0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult resRegs = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult preRegs = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, preRegs, resRegs);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, preRegs, resRegs);
				String finalPlace = resRegs.getRegister(assignICode.label);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initIEq1() {
		codeGenFunctions.put(Pattern.iEq1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult resRegs = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult preRegs = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp)assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, preRegs, resRegs);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, preRegs, resRegs);
				String finalPlace = resRegs.getRegister(assignICode.place);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initINe0() {
		codeGenFunctions.put(Pattern.iNe0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult resRegs = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult preRegs = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, preRegs, resRegs);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, preRegs, resRegs);
				String finalPlace = resRegs.getRegister(assignICode.label);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initINe1() {
		codeGenFunctions.put(Pattern.iNe1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult resRegs = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult preRegs = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp)assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, preRegs, resRegs);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, preRegs, resRegs);
				String finalPlace = resRegs.getRegister(assignICode.place);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initBEq0() {
		codeGenFunctions.put(Pattern.bEq0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBEq1() {
		codeGenFunctions.put(Pattern.bEq1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope()  == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope()  == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBNe0() {
		codeGenFunctions.put(Pattern.bNe0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBNe1() {
		codeGenFunctions.put(Pattern.bNe1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");

				if(assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(finalPlace, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initAnd0() {
		codeGenFunctions.put(Pattern.and0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("ANDB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}

	private void initAnd1() {
		codeGenFunctions.put(Pattern.and1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("ANDB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initOr0() {
		codeGenFunctions.put(Pattern.or0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("ORRB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initOr1() {
		codeGenFunctions.put(Pattern.or1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("ORRB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBnot0() {
		codeGenFunctions.put(Pattern.bnot0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def)icode;
				UnExp assignExp = (UnExp)assignICode.val;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("TEQ " + reg + ", #0");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initBnot1() {
		codeGenFunctions.put(Pattern.bnot1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def)icode;
				UnExp assignExp = (UnExp)assignICode.val;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.BOOL, pre, res);
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("TEQ " + reg + ", #0");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initBitwiseNot0() {
		codeGenFunctions.put(Pattern.bitwiseNot0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def)icode;
				UnExp assignExp = (UnExp)assignICode.val;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				
				String finalPlace = res.getRegister(assignICode.label);

				cGen.addInstruction("MVN " + finalPlace + ", " + reg);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	private void initBitwiseNot1() {
		codeGenFunctions.put(Pattern.bitwiseNot1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				UnExp assignExp = (UnExp)assignICode.value;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.INT, pre, res);
				
				String finalPlace = res.getRegister(assignICode.place);

				cGen.addInstruction("MVN " + finalPlace + ", " + reg);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + finalPlace + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + finalPlace + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + finalPlace + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initBool0() {
		codeGenFunctions.put(Pattern.bool0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def)icode;
				BoolExp assignExp = (BoolExp) assignICode.val;

				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.BYTE, assignExp.trueFalse ? 1 : 0);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					String reg = res.getRegister(assignICode.label);
					
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					String reg = res.getRegister(assignICode.label);
					
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					String reg = res.getRegister(assignICode.label);
					
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initBool1() {
		codeGenFunctions.put(Pattern.bool1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				BoolExp assignExp = (BoolExp) assignICode.value;

				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					String reg = res.getRegister(assignICode.place);
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					String reg = res.getRegister(assignICode.place);
					
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					String reg = res.getRegister(assignICode.place);
					
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					String reg = res.getRegister(assignICode.place);
					
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initInt0() {
		codeGenFunctions.put(Pattern.int0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def)icode;
				IntExp assignExp = (IntExp) assignICode.val;

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD, assignExp.value);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempValue = res.getRegister(assignICode.label);
					
					if(assignExp.value > 255 || assignExp.value < -255) {
						cGen.addVariable(assignICode.label, VariableLength.WORD, assignExp.value);
						cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label);
					} else {
						cGen.addInstruction("MOV " + tempValue  + ", #" + assignExp.value);
					}
					
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String tempValue = res.getRegister(assignICode.label);
					
					if(assignExp.value > 255 || assignExp.value < -255) {
						cGen.addVariable(assignICode.label, VariableLength.WORD, assignExp.value);
						cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label);
					} else {
						cGen.addInstruction("MOV " + tempValue  + ", #" + assignExp.value);
					}
					
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else {
					String tempValue = res.getRegister(assignICode.label);
					
					if(assignExp.value > 255 || assignExp.value < -255) {
						cGen.addVariable(assignICode.label, VariableLength.WORD, assignExp.value);
						cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label);
					} else {
						cGen.addInstruction("MOV " + tempValue  + ", #" + assignExp.value);
					}
					
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initInt1() {
		codeGenFunctions.put(Pattern.int1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				IntExp assignExp = (IntExp) assignICode.value;

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					String tempValue = res.getRegister(assignICode.place);
					if(assignExp.value > 255 || assignExp.value < -255) {
						cGen.addVariable(assignICode.place + "_value", VariableLength.WORD, assignExp.value);
						cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					} else {
						cGen.addInstruction("MOV " + tempValue  + ", #" + assignExp.value);
					}
					
					cGen.addInstruction("STR " + tempValue + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempValue = res.getRegister(assignICode.place);
					
					if(assignExp.value > 255 || assignExp.value < -255) {
						cGen.addVariable(assignICode.place, VariableLength.WORD, assignExp.value);
						cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place);
					} else {
						cGen.addInstruction("MOV " + tempValue  + ", #" + assignExp.value);
					}
					
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String tempValue = res.getRegister(assignICode.place);
					
					if(assignExp.value > 255 || assignExp.value < -255) {
						cGen.addVariable(assignICode.place, VariableLength.WORD, assignExp.value);
						cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place);
					} else {
						cGen.addInstruction("MOV " + tempValue  + ", #" + assignExp.value);
					}
					
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else {
					String tempValue = res.getRegister(assignICode.place);
					
					if(assignExp.value > 255 || assignExp.value < -255) {
						cGen.addVariable(assignICode.place, VariableLength.WORD, assignExp.value);
						cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place);
					} else {
						cGen.addInstruction("MOV " + tempValue  + ", #" + assignExp.value);
					}
					
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initReal0() {
		codeGenFunctions.put(Pattern.real0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def)icode;
				RealExp assignExp = (RealExp)assignICode.val;

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, assignExp.realValue);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempValue = res.getRegister(assignICode.label);
					
					cGen.addVariable(assignICode.label, assignExp.realValue);
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label);
					
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String tempValue = res.getRegister(assignICode.label);
					
					cGen.addVariable(assignICode.label, assignExp.realValue);
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label);
					
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else {
					String tempValue = res.getRegister(assignICode.label);
					
					cGen.addVariable(assignICode.label, assignExp.realValue);
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label);
					
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initReal1() {
		codeGenFunctions.put(Pattern.real1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				RealExp assignExp = (RealExp) assignICode.value;

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					String tempValue = res.getRegister(assignICode.place);
					cGen.addVariable(assignICode.place + "_value", assignExp.realValue);
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					
					cGen.addInstruction("STR " + tempValue + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempValue = res.getRegister(assignICode.place);
					
					cGen.addVariable(assignICode.place, assignExp.realValue);
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place);
					
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String tempValue = res.getRegister(assignICode.place);
					
					cGen.addVariable(assignICode.place, assignExp.realValue);
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place);
					
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				} else {
					String tempValue = res.getRegister(assignICode.place);
					
					cGen.addVariable(assignICode.place, assignExp.realValue);
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place);
					
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempValue + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initStr0() {
		codeGenFunctions.put(Pattern.str0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Def assignICode = (Def)icode;
				StrExp assignExp = (StrExp)assignICode.val;

				if(assignICode.scope == ICode.Scope.GLOBAL) {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					cGen.addVariable(assignICode.label, VariableLength.WORD, assignICode.label + "_value");
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.label, VariableLength.WORD, assignICode.label + "_value");
					
					String reg = res.getRegister(assignICode.label);
					int off = offset.findOffset(assignICode.label, assignICode.type);
					
					cGen.addInstruction("LDR " + reg + ", " + assignICode.label);
					cGen.addInstruction("STR " + reg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.label, VariableLength.WORD, assignICode.label + "_value");
					
					String reg = res.getRegister(assignICode.label);
					int off = offset.findOffset(assignICode.label, assignICode.type);
					
					cGen.addInstruction("LDR " + reg + ", " + assignICode.label);
					cGen.addInstruction("STR " + reg + ", [R13, #-" + off + ']');
				} else {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.label, VariableLength.WORD, assignICode.label + "_value");
					
					String reg = res.getRegister(assignICode.label);
					int off = offset.findOffset(assignICode.label, assignICode.type);
					
					cGen.addInstruction("LDR " + reg + ", " + assignICode.label);
					cGen.addInstruction("STR " + reg + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}
	
	private void initStr1() {
		codeGenFunctions.put(Pattern.str1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Assign assignICode = (Assign)icode;
				StrExp assignExp = (StrExp)assignICode.value;

				if(assignICode.getScope() == ICode.Scope.GLOBAL) {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)'\0');
					}					
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.place, VariableLength.WORD, assignICode.place + "_value");
					
					String reg = res.getRegister(assignICode.place);
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					
					cGen.addInstruction("LDR " + reg + ", " + assignICode.place);
					cGen.addInstruction("STR " + reg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.place, VariableLength.WORD, assignICode.place + "_value");
					
					String reg = res.getRegister(assignICode.place);
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					
					cGen.addInstruction("LDR " + reg + ", " + assignICode.place);
					cGen.addInstruction("STR " + reg + ", [R13, #-" + off + ']');
				} else {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.place, VariableLength.WORD, assignICode.place + "_value");
					
					String reg = res.getRegister(assignICode.place);
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					
					cGen.addInstruction("LDR " + reg + ", " + assignICode.place);
					cGen.addInstruction("STR " + reg + ", [R13, #-" + off + ']');
				}
				
				return null;
			}
		});
	}

	private void initId0() {
		codeGenFunctions.put(Pattern.id0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.BOOL, pre, res);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.BYTE);
					cGen.addInstruction("STRB " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STRB " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initId1() {
		codeGenFunctions.put(Pattern.id1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.REAL, pre, res);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initId2() {
		codeGenFunctions.put(Pattern.id2, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.INT, pre, res);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initId3() {
		codeGenFunctions.put(Pattern.id3, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.STRING, pre, res);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.label, assignICode.type);
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initId4() {
		codeGenFunctions.put(Pattern.id4, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp)assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.BOOL, pre, res);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STRB " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initId5() {
		codeGenFunctions.put(Pattern.id5, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp)assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.REAL, pre, res);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initId6() {
		codeGenFunctions.put(Pattern.id6, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp) assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.INT, pre, res);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}
	
	private void initId7() {
		codeGenFunctions.put(Pattern.id7, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp) assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.STRING, pre, res);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				} else {
					int off = offset.findOffset(assignICode.place, assignICode.getType());
					cGen.addInstruction("STR " + tempReg + ", [R13, #-" + off + ']');
				}

				return null;
			}
		});
	}

	public void initIf0() {
		codeGenFunctions.put(Pattern.if0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, pre, res);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BLT " + ifStatement.ifTrue);
				cGen.addInstruction("BGE " + ifStatement.ifFalse);		

				return null;
			}
		});
	}

	public void initIf1() {
		codeGenFunctions.put(Pattern.if1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, pre, res);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BGT " + ifStatement.ifTrue);
				cGen.addInstruction("BLE " + ifStatement.ifFalse);

				return null;
			}
		});
	}

	public void initIf2() {
		codeGenFunctions.put(Pattern.if2, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, pre, res);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BLE " + ifStatement.ifTrue);
				cGen.addInstruction("BGT " + ifStatement.ifFalse);

				return null;
			}
		});
	}

	public void initIf3() {
		codeGenFunctions.put(Pattern.if3, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, pre, res);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BGE " + ifStatement.ifTrue);
				cGen.addInstruction("BLT " + ifStatement.ifFalse);

				return null;
			}
		});
	}

	public void initIf4() {
		codeGenFunctions.put(Pattern.if4, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, pre, res);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("BEQ " + ifStatement.ifTrue);
				cGen.addInstruction("BNE " + ifStatement.ifFalse);

				return null;
			}
		});
	}

	public void initIf5() {
		codeGenFunctions.put(Pattern.if5, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, pre, res);
				
				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("BNE " + ifStatement.ifTrue);
				cGen.addInstruction("BEQ " + ifStatement.ifFalse);

				return null;
			}
		});
	}
	
	public void initIf6() {
		codeGenFunctions.put(Pattern.if6, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.BOOL, pre, res);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("BEQ " + ifStatement.ifTrue);
				cGen.addInstruction("BNE " + ifStatement.ifFalse);

				return null;
			}
		});
	}

	public void initIf7() {
		codeGenFunctions.put(Pattern.if7, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				ArmRegisterResult pre = rGen.getFilteredInputSet(icode);
				
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.BOOL, pre, res);
				String rightReg = loadVariableToReg(right, ICode.Type.BOOL, pre, res);
				
				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("BNE " + ifStatement.ifTrue);
				cGen.addInstruction("BEQ " + ifStatement.ifFalse);

				return null;
			}
		});
	}

	private void initGoto0() {
		codeGenFunctions.put(Pattern.goto0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Goto gotoICode = (Goto) icode;
				cGen.addInstruction("B " + gotoICode.label);
				return null;
			}
		});
	}

	private void initLabel0() {
		codeGenFunctions.put(Pattern.label0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Label labelICode = (Label) icode;
				cGen.setLabel(labelICode.label);
				return null;
			}
		});
	}

	private void initProcLabel0() {
		codeGenFunctions.put(Pattern.procLabel0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ProcLabel labelICode = (ProcLabel) icode;
				
				offset.pushFrame();
				
				offset.pushAddress("returnAddr", ICode.Type.INT);
				
				if(intermediateCode.containsEntry(labelICode.label, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)){
					VarSymEntry entry = intermediateCode.getVariableData(labelICode.label, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
					offset.pushAddress(entry.icodePlace, entry.codeType); 
				}
				
				int paramNum = 0;
				while(intermediateCode.containsEntry(labelICode.label, paramNum, SymEntry.INTERNAL | SymEntry.PARAM)){
					VarSymEntry entry = intermediateCode.getVariableData(labelICode.label, paramNum, SymEntry.INTERNAL | SymEntry.PARAM);
					offset.pushAddress(entry.icodePlace, entry.codeType);
					paramNum++;
				}
				
				int toAllocate = 0;
				offset.pushFrame();
				
				int x = i + 1;
				ICode instruction = null;
				do {
					instruction = intermediateCode.getInstruction(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							if (definition.type == ICode.Type.BOOL) {
								toAllocate += 1;
								offset.pushAddress(definition.label, ICode.Type.BOOL);
							} else if(definition.type == ICode.Type.STRING) {
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.STRING);
							} else if(definition.type == ICode.Type.REAL){
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.REAL);
							} else if(definition.type == ICode.Type.INT){
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.INT);
							}
						}
					}
					x++;
				} while (!(instruction instanceof Return));
				
				cGen.addInstruction("ADD R13, R13, #" + toAllocate);
				
				return null;
			}
		});
	}
	
	private void initDataSectionHeader() {
		codeGenFunctions.put(Pattern.dataSectionHeader, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				int x = i + 1;
				
				offset.pushFrame();
				
				int toAllocate = 0;
				ICode instruction = null;
				do {
					instruction = intermediateCode.getInstruction(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							if (definition.type == ICode.Type.BOOL) {
								toAllocate += 1;
								offset.pushAddress(definition.label, ICode.Type.BOOL);
							} else if(definition.type == ICode.Type.STRING) {
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.STRING);
							} else if(definition.type == ICode.Type.REAL){
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.REAL);
							} else if(definition.type == ICode.Type.INT){
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.INT);
							}
						}
					}
					x++;
				} while (!(instruction instanceof BssSec));
				
				cGen.addInstruction("ADD R13, R13, #" + toAllocate);
				
				return null;
			}
		});
	}
	
	private void initBssSectionHeader() {
		codeGenFunctions.put(Pattern.bssSectionHeader, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				int x = i - 1;
				ICode instruction = null;
				int localStack = 0;
				
				do {
					instruction = intermediateCode.getInstruction(x);
					if(instruction instanceof Def) {
						Def definition = (Def)instruction;
						if(definition.scope == ICode.Scope.LOCAL) {
							if(definition.type == ICode.Type.BOOL)
								localStack += 1;
							else
								localStack += 4;
						}
					}
					x--;
				} while(!(instruction instanceof DataSec));
				
				cGen.addInstruction("SUB R13, R13, #" + localStack);
				offset.popFrame();
				
				return null;
			}
		});
	}
	
	private void initCodeSectionHeader() {
		codeGenFunctions.put(Pattern.codeSectionHeader, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				int x = i + 1;
				int toAllocate = 0;
				ICode instruction = null;
				
				offset.pushFrame();
				
				do {
					instruction = intermediateCode.getInstruction(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							if (definition.type == ICode.Type.BOOL) {
								toAllocate += 1;
								offset.pushAddress(definition.label, ICode.Type.BOOL);
							} else if(definition.type == ICode.Type.STRING) {
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.STRING);
							} else if(definition.type == ICode.Type.REAL){
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.REAL);
							} else if(definition.type == ICode.Type.INT){
								toAllocate += 4;
								offset.pushAddress(definition.label, ICode.Type.INT);
							}
						}
					}
					x++;
				} while (!(instruction instanceof End));
				
				cGen.addInstruction("ADD R13, R13, #" + toAllocate);
				
				return null;
			}
		});
	}

	private void initEnd0() {
		codeGenFunctions.put(Pattern.end0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				int x = i - 1;
				ICode instruction = null;
				int localStack = 0;
				do {
					instruction = intermediateCode.getInstruction(x);
					if(instruction instanceof Def) {
						Def definition = (Def)instruction;
						if(definition.scope == ICode.Scope.LOCAL) {
							if(definition.type == ICode.Type.BOOL)
								localStack += 1;
							else
								localStack += 4;
						}
					}
					x--;
				} while(!(instruction instanceof CodeSec));
				
				offset.popFrame();
				cGen.addInstruction("SUB R13, R13, #" + localStack);
				cGen.addInstruction("STP");
				
				return null;
			}
		});
	}
	
	private void initProcSectionHeader() {
		codeGenFunctions.put(Pattern.procSectionHeader, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// DO nothing when this pattern is discovered
				return null;
			}
		});
	}
	
	private void initSpill0() {
		codeGenFunctions.put(Pattern.spill0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode instr = intermediateCode.getInstruction(i);
				
				Spill spill = (Spill)instr;
				
				String addr = spill.getAddr();
				ArmRegisterResult res = rGen.getFilteredInputSet(instr);
				String reg = res.getRegister(addr);
				
				offset.pushAddress(addr, ICode.Type.INT);
				int off = offset.findOffset(addr, ICode.Type.INT);
				
				cGen.addInstruction("ADD R13, R13, #" + off);
				cGen.addInstruction("STR " + reg + "[R13, #-" + off + ']');
				return null;
			}
		});
	}
	
	private void initSpill1() {
		codeGenFunctions.put(Pattern.spill1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode instr = intermediateCode.getInstruction(i);
				
				Spill spill = (Spill)instr;
				
				String addr = spill.getAddr();
				ArmRegisterResult res = rGen.getFilteredInputSet(instr);
				String reg = res.getRegister(addr);
				
				offset.pushAddress(addr, ICode.Type.REAL);
				int off = offset.findOffset(addr, ICode.Type.REAL);
				
				cGen.addInstruction("ADD R13, R13, #" + off);
				cGen.addInstruction("STR " + reg + "[R13, #-" + off + ']');
				return null;
			}
		});
	}
	
	private void initSpill2() {
		codeGenFunctions.put(Pattern.spill2, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode instr = intermediateCode.getInstruction(i);
				
				Spill spill = (Spill)instr;
				
				String addr = spill.getAddr();
				ArmRegisterResult res = rGen.getFilteredInputSet(instr);
				String reg = res.getRegister(addr);
				
				offset.pushAddress(addr, ICode.Type.STRING);
				int off = offset.findOffset(addr, ICode.Type.STRING);
				
				cGen.addInstruction("ADD R13, R13, #" + off);
				cGen.addInstruction("STR " + reg + "[R13, #-" + off + ']');
				return null;
			}
		});
	}
	
	private void initSpill3() {
		codeGenFunctions.put(Pattern.spill3, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode instr = intermediateCode.getInstruction(i);
				
				Spill spill = (Spill)instr;
				
				String addr = spill.getAddr();
				ArmRegisterResult res = rGen.getFilteredInputSet(instr);
				String reg = res.getRegister(addr);
				
				offset.pushAddress(addr, ICode.Type.BOOL);
				int off = offset.findOffset(addr, ICode.Type.BOOL);
				
				cGen.addInstruction("ADD R13, R13, #" + off);
				cGen.addInstruction("STRB " + reg + "[R13, #-" + off + ']');
				return null;
			}
		});
	}

	private void initReturn0() {
		codeGenFunctions.put(Pattern.return0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				int x = i - 1;
				ICode instruction = null;
				int localStack = 0;
				do {
					instruction = intermediateCode.getInstruction(x);
					if(instruction instanceof Def) {
						Def definition = (Def)instruction;
						if(definition.scope == ICode.Scope.LOCAL) {
							if(definition.type == ICode.Type.BOOL)
								localStack += 1;
							else
								localStack += 4;
						}
					}
					x--;
				} while(!(instruction instanceof ProcLabel));
				
				cGen.addInstruction("SUB R13, R13, #" + localStack);
				cGen.addInstruction("MOV R15, R14");
				offset.popFrame();
				
				return null;
			}
		});
	}

	private void initCall0() {
		codeGenFunctions.put(Pattern.call0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				Call procICode = (Call) icode;
				
				int totalLength = procICode.params.size();
				
				ArmRegisterResult res1 = rGen.getFilteredOutputSet(procICode);
				ArmRegisterResult pre1 = rGen.getFilteredInputSet(procICode);
				
				int totalStackFrameLength = getStackFrameLength(procICode);
				
				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);
				cGen.addInstruction("STR R14, [R13, #-" + offset.findOffset("returnAddr", ICode.Type.INT) + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, pre1, res1);
					if (sourceDest.type == ICode.Type.BOOL) {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STRB " + oldReg + ", [R13, #-" + offset1 + "]");
					} else {
						int offset1 = offset.findOffset(sourceDest.label, sourceDest.type);
						cGen.addInstruction("STR " + oldReg + ", [R13, #-" + offset1 + "]");
					}
				}

				// Now to load the Return address from the Stack back into the Link Register R14
				int off = offset.findOffset("returnAddr", ICode.Type.INT);
				cGen.addInstruction("LDR R14, [R13, #-"+ off +"]");
				
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				offset.popFrame();
				return null;
			}
		});
	}
	
	private enum InlineParamState{
		BEGIN,
		SPECIFIER,
	}

	private void initInline0() {
		codeGenFunctions.put(Pattern.inline0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.getInstruction(i);
				ArmRegisterResult res = rGen.getFilteredOutputSet(icode);
				
				Inline inline = (Inline) icode;

				String instruction = inline.inlineAssembly;

				StringBuilder resultInstruction = new StringBuilder();
				int index = 0;
				int paramIndex = 0;
				List<InlineParam> params = inline.params;
				InlineParamState state = InlineParamState.BEGIN; 
				
				while (index < instruction.length()) {					
					char letterAtIndex = instruction.charAt(index);
					switch(state) {
					case BEGIN: 
							if(letterAtIndex == '%'){
								state = InlineParamState.SPECIFIER;
							} else {
								resultInstruction.append(letterAtIndex);
							}
							index++;
							continue;
					case SPECIFIER:
							if (Character.toLowerCase(letterAtIndex) == 'a') {
								if (paramIndex < params.size()) {
									IdentExp addressParam = params.get(paramIndex).name;
									resultInstruction.append(addressParam.ident);
								} else {
									errorLog.add("No paramater to substite %a found at " + paramIndex,
											new Position(i, index));
								}
								index++;
								continue;
							} else if (Character.toLowerCase(letterAtIndex) == 'r'){
								if (paramIndex < params.size()) {
									IdentExp addresParam = params.get(paramIndex).name;
									String regParam = res.getRegister(addresParam.ident);
									resultInstruction.append(regParam);
									paramIndex++;
								} else {
									errorLog.add("No paramater to substite %r found at " + paramIndex,
											new Position(i, index));
								}
								index++;
								continue;
							} else if(Character.toLowerCase(letterAtIndex) == 'u' || Character.toLowerCase(letterAtIndex) == 'd'){
								index++;
								continue;
							} else {
								state = InlineParamState.BEGIN;
								resultInstruction.append(letterAtIndex);
								index++;
								continue;
							}
					}
				}

				cGen.addInstruction(resultInstruction.toString());
				return null;
			}
		});
	}

}
