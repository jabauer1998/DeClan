package io.github.H20man13.DeClan.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;

import com.sun.tools.javac.code.Source;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.position.Position;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.iterative.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.Pattern;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.common.arm.ArmRegisterGenerator;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator.VariableLength;
import io.github.H20man13.DeClan.common.ast.VariableDeclaration;
import io.github.H20man13.DeClan.common.exception.CodeGeneratorException;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
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
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;

public class MyCodeGenerator {
	private List<ICode> intermediateCode;
	private ArmRegisterGenerator rGen;
	private ArmCodeGenerator cGen;
	private IrRegisterGenerator iGen;
	private ErrorLog errorLog;
	private Map<P, Callable<Void>> codeGenFunctions;

	private int i;

	public MyCodeGenerator(String outputFile, LiveVariableAnalysis analysis, Prog program, ErrorLog errLog) throws IOException {
		this.intermediateCode = program.getICode();
		this.cGen = new ArmCodeGenerator(outputFile);
		this.rGen = new ArmRegisterGenerator(cGen, analysis);
		this.iGen = new IrRegisterGenerator();
		String place;
		do {
			place = iGen.genNext();
		} while (program.containsPlace(place));
		this.errorLog = errLog;
		this.codeGenFunctions = new HashMap<>();
		this.i = 0;
		initCodeGenFunctions();
	}

	private boolean genICode(ICode icode1, ICode icode2) throws Exception {
		P possibleTwoStagePattern = P.PAT(icode1.asPattern(), icode2.asPattern());
		if (codeGenFunctions.containsKey(possibleTwoStagePattern)) {
			Callable<Void> codeGenFunction = codeGenFunctions.get(possibleTwoStagePattern);
			codeGenFunction.call();
			i+= 2;
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
		ICode instruction = intermediateCode.get(i);
		while(!(instruction instanceof DataSec)) {
			i++;
			instruction = intermediateCode.get(i);
		}
	}

	public void codeGen() {
		try {
			int size = intermediateCode.size();

			skipSymbolTable();
			
			while (i < size) {
				ICode icode1 = intermediateCode.get(i);
				if (i + 1 < size) {
					ICode icode2 = intermediateCode.get(i + 1);
					if (!genICode(icode1, icode2) && !genICode(icode1)) {
						errorLog.add("Error cannot generate icode " + icode1, new Position(i, 0));
					}
				} else if (!genICode(icode1)) {
					errorLog.add("Error cannot generate icode " + icode1, new Position(i, 0));
				}
			}

			cGen.writeToStream();
		} catch (Exception exp) {
			errorLog.add(exp.toString(), new Position(i, 0));
		}
	}

	private void initCodeGenFunctions() {
		// Init Proc with Return Pattern
		initCallWithReturn0();
		initCallWithReturn1();
		initCallWithReturn2();
		initCallWithReturn3();
		initCallWithReturn4();
		initCallWithReturn5();

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

		// Init End Pattern
		initEnd0();

		// Init Return Pattern
		initReturn0();

		// Init Proc Pattern
		initCall0();

		// Init Inline Assembly Pattern
		initInline0();
	}

	private String loadVariableToReg(IdentExp exp, ICode.Type type, ICode icode) throws Exception {
		if (exp.scope == Scope.LOCAL) {
			String offset = rGen.getReg(exp.ident, icode);
			cGen.addInstruction("LDR " + offset + ", " + exp.ident);
			String newReg = offset;
			if (type == ICode.Type.BOOL) {
				cGen.addInstruction("LDRB " + newReg + ", [R13, -" + offset + ']');
			} else {
				cGen.addInstruction("LDR " + newReg + ", [R13, -" + offset + ']');
			}
			return newReg;
		} else if (exp.scope == Scope.PARAM) {
			String offset = rGen.getReg(exp.ident + "_inner", icode);
			cGen.addInstruction("LDR " + offset + ", " + exp.ident + "_inner");
			String newReg = offset;
			if (type == ICode.Type.BOOL) {
				cGen.addInstruction("LDRB " + newReg + ", [R13, -" + offset + ']');
			} else {
				cGen.addInstruction("LDR " + newReg + ", [R13, -" + offset + ']');
			}
			return newReg;
		} else if (exp.scope == Scope.RETURN) {
			String offset = rGen.getReg(exp.ident + "_outer", icode);
			cGen.addInstruction("LDR " + offset + ", " + exp.ident + "_outer");
			String newReg = offset;
			if (type == ICode.Type.BOOL) {
				cGen.addInstruction("LDRB " + newReg + ", [R13, -" + offset + ']');
			} else {
				cGen.addInstruction("LDR " + newReg + ", [R13, -" + offset + ']');
			}
			return newReg;
		} else {
			String newReg = rGen.getReg(exp.ident, icode);
			if (type == ICode.Type.BOOL) {
				cGen.addInstruction("LDRB " + newReg + ", " + exp.ident);
			} else {
				cGen.addInstruction("LDR " + newReg + ", " + exp.ident);
			}
			return newReg;
		}
	}

	private String genUnaryOp(String label, UnExp.Operator op, String rightReg, ICode icode) throws Exception {
		String destReg = rGen.getReg(label, icode);
		switch (op) {
		case BNOT:
			cGen.addInstruction("TEQ " + rightReg + ", #0");
			cGen.addInstruction("MOVEQ " + destReg + ", #1");
			cGen.addInstruction("MOVNE " + destReg + ", #0");
			break;
		case INOT:
			cGen.addInstruction("MVN " + destReg + ", " + rightReg);
			break;
		default:
			throw new CodeGeneratorException("callWithReturn0", "Error cant load register from unarty operation " + op);
		}
		return destReg;
	}

	private String genBinaryOp(String label, String leftReg, BinExp.Operator op, String rightReg, ICode icode)
			throws Exception {
		String destReg = rGen.getReg(label, icode);
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

		if (def.type == ICode.Type.BOOL)
			totalLength += 1;
		else
			totalLength += 4;

		for (Def param : call.params) {
			if (param.type == ICode.Type.BOOL) {
				totalLength += 1; // A boolean value of 8 bits/1 byte
			} else {
				totalLength += 4; // A word sized value(INT,REAL,STRING<-address) value of 32 bits/4 bytes
			}
		}

		return totalLength;
	}
	
	private int getStackFrameLength(Call call) {
		int totalLength = 4;

		for (Def param : call.params) {
			if (param.type == ICode.Type.BOOL) {
				totalLength += 1; // A boolean value of 8 bits/1 byte
			} else {
				totalLength += 4; // A word sized value(INT,REAL,STRING<-address) value of 32 bits/4 bytes
			}
		}

		return totalLength;
	}

	private int getStackFrameLength(Call call, Assign def) {
		int totalLength = 4;

		if (def.getType() == ICode.Type.BOOL)
			totalLength += 1;
		else
			totalLength += 4;

		for (Def param : call.params) {
			if (param.type == ICode.Type.BOOL) {
				totalLength += 1; // A boolean value of 8 bits/1 byte
			} else {
				totalLength += 4; // A word sized value(INT,REAL,STRING<-address) value of 32 bits/4 bytes
			}
		}

		return totalLength;
	}

	private String genExpression(Exp expression, ICode.Type returnType, ICode icode) throws Exception {
		if (expression instanceof IdentExp) {
			IdentExp exp = (IdentExp) expression;
			return loadVariableToReg(exp, returnType, icode);
		} else if (expression instanceof IntExp) {
			IntExp exp = (IntExp) expression;
			String newPlace = iGen.genNext();
			cGen.addVariable(newPlace, VariableLength.WORD, exp.value);
			String newReg = rGen.getTempReg(newPlace, icode);
			cGen.addInstruction("LDR " + newReg + ", " + newPlace);
			return newReg;
		} else if (expression instanceof RealExp) {
			RealExp exp = (RealExp) expression;
			String newPlace = iGen.genNext();
			cGen.addVariable(newPlace, exp.realValue);
			String newReg = rGen.getTempReg(newPlace, icode);
			cGen.addInstruction("LDR " + newReg + ", " + newPlace);
			return newReg;
		} else if (expression instanceof BoolExp) {
			BoolExp exp = (BoolExp) expression;
			String newPlace = iGen.genNext();
			cGen.addVariable(newPlace, VariableLength.BYTE, exp.trueFalse ? 1 : 0);
			String newReg = rGen.getTempReg(newPlace, icode);
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
			String newReg = rGen.getReg(newPlace, icode);
			cGen.addInstruction("LDR " + newReg + ", " + newPlace);
			return newReg;
		} else if (expression instanceof BinExp) {
			BinExp exp = (BinExp) expression;

			String leftReg = null;
			String rightReg = null;

			if (exp.op == BinExp.Operator.IEQ || exp.op == BinExp.Operator.INE || exp.op == BinExp.Operator.IADD
					|| exp.op == BinExp.Operator.ISUB || exp.op == BinExp.Operator.GE || exp.op == BinExp.Operator.GT
					|| exp.op == BinExp.Operator.LT || exp.op == BinExp.Operator.LE || exp.op == BinExp.Operator.IAND
					|| exp.op == BinExp.Operator.IOR || exp.op == BinExp.Operator.IXOR
					|| exp.op == BinExp.Operator.ILSHIFT || exp.op == BinExp.Operator.IRSHIFT) {
				leftReg = loadVariableToReg(exp.left, ICode.Type.INT, icode);
				rightReg = loadVariableToReg(exp.right, ICode.Type.INT, icode);
			} else if (exp.op == BinExp.Operator.LAND || exp.op == BinExp.Operator.LOR || exp.op == BinExp.Operator.BEQ
					|| exp.op == BinExp.Operator.BNE) {
				leftReg = loadVariableToReg(exp.left, ICode.Type.BOOL, icode);
				rightReg = loadVariableToReg(exp.right, ICode.Type.BOOL, icode);
			} else {
				throw new CodeGeneratorException("genExpression", "Unexpected/Unsupported Operation type " + exp.op);
			}

			String newPlace = iGen.genNext();
			return genBinaryOp(newPlace, leftReg, exp.op, rightReg, icode);
		} else if (expression instanceof UnExp) {
			UnExp exp = (UnExp) expression;

			String rightReg = null;
			if (exp.op == UnExp.Operator.BNOT) {
				rightReg = loadVariableToReg(exp.right, ICode.Type.BOOL, icode);
			} else if (exp.op == UnExp.Operator.INOT) {
				rightReg = loadVariableToReg(exp.right, ICode.Type.INT, icode);
			} else {
				throw new CodeGeneratorException("genExpression",
						"Error cant load register from unarty operation " + exp.op);
			}
			String newPlace = iGen.genNext();
			return genUnaryOp(newPlace, exp.op, rightReg, icode);
		} else {
			throw new CodeGeneratorException("genExpression",
					"Unsopported Exp found " + expression.getClass().getSimpleName());
		}
	}

	private void initCallWithReturn0() {
		codeGenFunctions.put(Pattern.callWithReturn0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Call procICode = (Call) icode;
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.get(i + 1);
				Def returnPlacement = (Def) retICode;

				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				int offset = totalStackFrameLength;

				cGen.addVariable(procICode.pname + "_return_address_outer", VariableLength.WORD, offset);
				offset -= 4;

				cGen.addVariable(((IdentExp) returnPlacement.val).ident + "_outer", VariableLength.BYTE, offset);
				offset -= 1;

				for (Def param : procICode.params) {
					if (param.type == ICode.Type.BOOL) {
						cGen.addVariable(param.label + "_outer", VariableLength.BYTE, offset);
						offset -= 1;
					} else {
						cGen.addVariable(param.label + "_outer", VariableLength.WORD, offset);
						offset -= 4;
					}
				}

				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);

				String myReg = rGen.getReg(procICode.pname + "_return_address_outer", procICode);
				cGen.addInstruction("LDR " + myReg + ", " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("STR R14, [R13, -" + myReg + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, procICode);

					String tempReg = rGen.getTempReg(sourceDest.label + "_outer", procICode);
					cGen.addInstruction("LDR " + tempReg + ", " + sourceDest.label + "_outer");
					if (sourceDest.type == ICode.Type.BOOL) {
						cGen.addInstruction("STRB " + oldReg + ", [R13, -" + tempReg + "]");
					} else {
						cGen.addInstruction("STR " + oldReg + ", [R13, -" + tempReg + "]");
					}
				}
				cGen.addInstruction("BL " + procICode.pname);

				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp) returnPlacement.val, returnPlacement.type, retICode);
				String offsetReg = rGen.getReg(returnPlacement.label + "_inner", retICode);
				cGen.addInstruction("LDR " + offsetReg + ", " + returnPlacement.label + "_inner");
				cGen.addInstruction("STRB " + temp + ", [R13, -" + offsetReg + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("LDR R14, [R13, -R14]");
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initCallWithReturn1() {
		codeGenFunctions.put(Pattern.callWithReturn1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Call procICode = (Call) icode;
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.get(i + 1);
				Assign returnPlacement = (Assign)retICode;

				int totalStackFrameLength = getStackFrameLength(procICode);
				int offset = totalStackFrameLength;

				cGen.addVariable(procICode.pname + "_return_address_outer", VariableLength.WORD, offset);
				offset -= 4;

				cGen.addVariable(((IdentExp) returnPlacement.value).ident + "_outer", VariableLength.BYTE, offset);
				offset -= 1;

				for (Def param : procICode.params) {
					if (param.type == ICode.Type.BOOL) {
						cGen.addVariable(param.label + "_outer", VariableLength.BYTE, offset);
						offset -= 1;
					} else {
						cGen.addVariable(param.label + "_outer", VariableLength.WORD, offset);
						offset -= 4;
					}
				}

				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);

				String myReg = rGen.getReg(procICode.pname + "_return_address_outer", procICode);
				cGen.addInstruction("LDR " + myReg + ", " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("STR R14, [R13, -" + myReg + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, procICode);

					String tempReg = rGen.getTempReg(sourceDest.label + "_outer", procICode);
					cGen.addInstruction("LDR " + tempReg + ", " + sourceDest.label + "_outer");
					if (sourceDest.type == ICode.Type.BOOL) {
						cGen.addInstruction("STRB " + oldReg + ", [R13, -" + tempReg + "]");
					} else {
						cGen.addInstruction("STR " + oldReg + ", [R13, -" + tempReg + "]");
					}
				}
				cGen.addInstruction("BL " + procICode.pname);

				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp) returnPlacement.value, returnPlacement.getType(), retICode);
				String offsetReg = rGen.getReg(returnPlacement.place + "_inner", retICode);
				cGen.addInstruction("LDR " + offsetReg + ", " + returnPlacement.place + "_inner");
				cGen.addInstruction("STRB " + temp + ", [R13, -" + offsetReg + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("LDR R14, [R13, -R14]");
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initCallWithReturn2() {
		codeGenFunctions.put(Pattern.callWithReturn2, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Call procICode = (Call) icode;
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.get(i + 1);
				Def returnPlacement = (Def) retICode;

				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				int offset = totalStackFrameLength;

				cGen.addVariable(procICode.pname + "_return_address_outer", VariableLength.WORD, offset);
				offset -= 4;

				cGen.addVariable(((IdentExp)returnPlacement.val).ident + "_outer", VariableLength.WORD, offset);
				offset -= 4;

				for (Def param : procICode.params) {
					if (param.type == ICode.Type.BOOL) {
						cGen.addVariable(param.label + "_outer", VariableLength.BYTE, offset);
						offset -= 1;
					} else {
						cGen.addVariable(param.label + "_outer", VariableLength.WORD, offset);
						offset -= 4;
					}
				}

				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);

				String myReg = rGen.getReg(procICode.pname + "_return_address_outer", procICode);
				cGen.addInstruction("LDR " + myReg + ", " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("STR R14, [R13, -" + myReg + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, procICode);

					String tempReg = rGen.getTempReg(sourceDest.label + "_outer", procICode);
					cGen.addInstruction("LDR " + tempReg + ", " + sourceDest.label + "_outer");
					if (sourceDest.type == ICode.Type.BOOL) {
						cGen.addInstruction("STRB " + oldReg + ", [R13, -" + tempReg + "]");
					} else {
						cGen.addInstruction("STR " + oldReg + ", [R13, -" + tempReg + "]");
					}
				}

				cGen.addInstruction("BL " + procICode.pname);

				String temp = loadVariableToReg((IdentExp) returnPlacement.val, returnPlacement.type, retICode);
				// Now to load the Return value from the Stack
				cGen.addVariable(returnPlacement.label, VariableLength.WORD);
				cGen.addInstruction("STR " + temp + ", " + returnPlacement.label);

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("LDR R14, [R13, -R14]");
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initCallWithReturn3() {
		codeGenFunctions.put(Pattern.callWithReturn3, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Call procICode = (Call) icode;
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.get(i + 1);
				Assign returnPlacement = (Assign)retICode;

				int totalStackFrameLength = getStackFrameLength(procICode);
				int offset = totalStackFrameLength;

				cGen.addVariable(procICode.pname + "_return_address_outer", VariableLength.WORD, offset);
				offset -= 4;

				cGen.addVariable(((IdentExp) returnPlacement.value).ident + "_outer", VariableLength.WORD, offset);
				offset -= 4;

				for (Def param : procICode.params) {
					if (param.type == ICode.Type.BOOL) {
						cGen.addVariable(param.label + "_outer", VariableLength.BYTE, offset);
						offset -= 1;
					} else {
						cGen.addVariable(param.label + "_outer", VariableLength.WORD, offset);
						offset -= 4;
					}
				}

				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);

				String myReg = rGen.getReg(procICode.pname + "_return_address_outer", procICode);
				cGen.addInstruction("LDR " + myReg + ", " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("STR R14, [R13, -" + myReg + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, procICode);

					String tempReg = rGen.getTempReg(sourceDest.label + "_outer", procICode);
					cGen.addInstruction("LDR " + tempReg + ", " + sourceDest.label + "_outer");
					if (sourceDest.type == ICode.Type.BOOL) {
						cGen.addInstruction("STRB " + oldReg + ", [R13, -" + tempReg + "]");
					} else {
						cGen.addInstruction("STR " + oldReg + ", [R13, -" + tempReg + "]");
					}
				}

				cGen.addInstruction("BL " + procICode.pname);

				String temp = loadVariableToReg((IdentExp) returnPlacement.value, returnPlacement.getType(), retICode);
				// Now to load the Return value from the Stack
				cGen.addInstruction("STR " + temp + ", " + returnPlacement.place);

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("LDR R14, [R13, -R14]");
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initCallWithReturn4() {
		codeGenFunctions.put(Pattern.callWithReturn4, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Call procICode = (Call) icode;
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.get(i + 1);
				Def returnPlacement = (Def) retICode;

				int totalStackFrameLength = getStackFrameLength(procICode, returnPlacement);
				int offset = totalStackFrameLength;

				cGen.addVariable(procICode.pname + "_return_address_outer", VariableLength.WORD, offset);
				offset -= 4;

				cGen.addVariable(((IdentExp) returnPlacement.val).ident + "_outer", VariableLength.WORD, offset);
				offset -= 4;

				for (Def param : procICode.params) {
					if (param.type == ICode.Type.BOOL) {
						cGen.addVariable(param.label + "_outer", VariableLength.BYTE, offset);
						offset -= 1;
					} else {
						cGen.addVariable(param.label + "_outer", VariableLength.WORD, offset);
						offset -= 4;
					}
				}

				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);

				String myReg = rGen.getReg(procICode.pname + "_return_address_outer", procICode);
				cGen.addInstruction("LDR " + myReg + ", " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("STR R14, [R13, -" + myReg + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, procICode);

					String tempReg = rGen.getTempReg(sourceDest.label + "_outer", procICode);
					cGen.addInstruction("LDR " + tempReg + ", " + sourceDest.label + "_outer");
					if (sourceDest.type == ICode.Type.BOOL) {
						cGen.addInstruction("STRB " + oldReg + ", [R13, -" + tempReg + "]");
					} else {
						cGen.addInstruction("STR " + oldReg + ", [R13, -" + tempReg + "]");
					}
				}
				cGen.addInstruction("BL " + procICode.pname);

				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp) returnPlacement.val, returnPlacement.type, retICode);
				String offsetReg = rGen.getReg(returnPlacement.label + "_inner", retICode);
				cGen.addInstruction("LDR " + offsetReg + ", " + returnPlacement.label + "_inner");
				cGen.addInstruction("STR " + temp + ", [R13, -" + offsetReg + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("LDR R14, [R13, -R14]");
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initCallWithReturn5() {
		codeGenFunctions.put(Pattern.callWithReturn5, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Call procICode = (Call) icode;
				int totalLength = procICode.params.size();
				ICode retICode = intermediateCode.get(i + 1);
				Assign returnPlacement = (Assign) retICode;

				int totalStackFrameLength = getStackFrameLength(procICode);
				int offset = totalStackFrameLength;

				cGen.addVariable(procICode.pname + "_return_address_outer", VariableLength.WORD, offset);
				offset -= 4;

				cGen.addVariable(((IdentExp) returnPlacement.value).ident + "_outer", VariableLength.WORD, offset);
				offset -= 4;

				for (Def param : procICode.params) {
					if (param.type == ICode.Type.BOOL) {
						cGen.addVariable(param.label + "_outer", VariableLength.BYTE, offset);
						offset -= 1;
					} else {
						cGen.addVariable(param.label + "_outer", VariableLength.WORD, offset);
						offset -= 4;
					}
				}

				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);

				String myReg = rGen.getReg(procICode.pname + "_return_address_outer", procICode);
				cGen.addInstruction("LDR " + myReg + ", " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("STR R14, [R13, -" + myReg + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, procICode);

					String tempReg = rGen.getTempReg(sourceDest.label + "_outer", procICode);
					cGen.addInstruction("LDR " + tempReg + ", " + sourceDest.label + "_outer");
					if (sourceDest.type == ICode.Type.BOOL) {
						cGen.addInstruction("STRB " + oldReg + ", [R13, -" + tempReg + "]");
					} else {
						cGen.addInstruction("STR " + oldReg + ", [R13, -" + tempReg + "]");
					}
				}
				cGen.addInstruction("BL " + procICode.pname);

				// Now to load the Return value from the Stack
				String temp = loadVariableToReg((IdentExp) returnPlacement.value, returnPlacement.getType(), retICode);
				String offsetReg = rGen.getReg(returnPlacement.place + "_inner", retICode);
				cGen.addInstruction("LDR " + offsetReg + ", " + returnPlacement.place + "_inner");
				cGen.addInstruction("STR " + temp + ", [R13, -" + offsetReg + ']');

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("LDR R14, [R13, -R14]");
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initAdd0() {
		codeGenFunctions.put(Pattern.add0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initAdd1() {
		codeGenFunctions.put(Pattern.add1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initSub0() {
		codeGenFunctions.put(Pattern.sub0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initSub1() {
		codeGenFunctions.put(Pattern.sub1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitwiseAnd0() {
		codeGenFunctions.put(Pattern.bitwiseAnd0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("AND " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitwiseAnd1() {
		codeGenFunctions.put(Pattern.bitwiseAnd1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("AND " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitwiseOr0() {
		codeGenFunctions.put(Pattern.bitwiseOr0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("ORR " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitwiseOr1() {
		codeGenFunctions.put(Pattern.bitwiseOr1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("ORR " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitwiseExclusiveOr0() {
		codeGenFunctions.put(Pattern.bitwiseExclusiveOr0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("EOR " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitwiseExclusiveOr1() {
		codeGenFunctions.put(Pattern.bitwiseExclusiveOr1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("EOR " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitShiftLeft0() {
		codeGenFunctions.put(Pattern.leftShift0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("MOV " + finalPlace + ", " + leftReg + ", LSL " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitShiftLeft1() {
		codeGenFunctions.put(Pattern.leftShift1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("MOV " + finalPlace + ", " + leftReg + ", LSL " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitShiftRight0() {
		codeGenFunctions.put(Pattern.rightShift0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("MOV " + finalPlace + ", " + leftReg + ", ASR " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitShiftRight1() {
		codeGenFunctions.put(Pattern.rightShift1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("MOV " + finalPlace + ", " + leftReg + ", ASR " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initGt0() {
		codeGenFunctions.put(Pattern.gt0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGT " + finalPlace + ", #1");
				cGen.addInstruction("MOVLE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initGt1() {
		codeGenFunctions.put(Pattern.gt1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGT " + finalPlace + ", #1");
				cGen.addInstruction("MOVLE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initGe0() {
		codeGenFunctions.put(Pattern.ge0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGE " + finalPlace + ", #1");
				cGen.addInstruction("MOVLT " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initGe1() {
		codeGenFunctions.put(Pattern.ge1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVGE " + finalPlace + ", #1");
				cGen.addInstruction("MOVLT " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initLt0() {
		codeGenFunctions.put(Pattern.lt0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLT " + finalPlace + ", #1");
				cGen.addInstruction("MOVGE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initLt1() {
		codeGenFunctions.put(Pattern.lt1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLT " + finalPlace + ", #1");
				cGen.addInstruction("MOVGE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initLe0() {
		codeGenFunctions.put(Pattern.le0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLE " + finalPlace + ", #1");
				cGen.addInstruction("MOVGT " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initLe1() {
		codeGenFunctions.put(Pattern.le1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVLE " + finalPlace + ", #1");
				cGen.addInstruction("MOVGT " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initIEq0() {
		codeGenFunctions.put(Pattern.iEq0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initIEq1() {
		codeGenFunctions.put(Pattern.iEq1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initINe0() {
		codeGenFunctions.put(Pattern.iNe0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initINe1() {
		codeGenFunctions.put(Pattern.iNe1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.INT, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initBEq0() {
		codeGenFunctions.put(Pattern.bEq0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBEq1() {
		codeGenFunctions.put(Pattern.bEq1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBNe0() {
		codeGenFunctions.put(Pattern.bNe0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBNe1() {
		codeGenFunctions.put(Pattern.bNe1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("MOVNE " + finalPlace + ", #1");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #0");

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initAnd0() {
		codeGenFunctions.put(Pattern.and0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("ANDB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initAnd1() {
		codeGenFunctions.put(Pattern.and1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("ANDB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initOr0() {
		codeGenFunctions.put(Pattern.or0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				BinExp assignExp = (BinExp) assignICode.val;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("ORRB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initOr1() {
		codeGenFunctions.put(Pattern.or1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				BinExp assignExp = (BinExp) assignICode.value;

				IdentExp leftIdent = (IdentExp) assignExp.left;
				IdentExp rightIdent = (IdentExp) assignExp.right;

				String leftReg = loadVariableToReg(leftIdent, ICode.Type.BOOL, assignICode);
				String rightReg = loadVariableToReg(rightIdent, ICode.Type.BOOL, assignICode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("ORRB " + finalPlace + ", " + leftReg + ", " + rightReg);

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBnot0() {
		codeGenFunctions.put(Pattern.bnot0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				UnExp assignExp = (UnExp) assignICode.val;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.BOOL, icode);
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("TEQ " + reg + ", #0");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initBnot1() {
		codeGenFunctions.put(Pattern.bnot1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				UnExp assignExp = (UnExp) assignICode.value;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.BOOL, icode);
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("TEQ " + reg + ", #0");
				cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
				cGen.addInstruction("MOVNE " + finalPlace + ", #0");
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.place, VariableLength.WORD);
					cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + finalPlace + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initBitwiseNot0() {
		codeGenFunctions.put(Pattern.bitwiseNot0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				UnExp assignExp = (UnExp) assignICode.val;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				
				String finalPlace = rGen.getReg(assignICode.label, assignICode);

				cGen.addInstruction("MVN " + finalPlace + ", " + reg);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.label + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBitwiseNot1() {
		codeGenFunctions.put(Pattern.bitwiseNot1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign) icode;
				UnExp assignExp = (UnExp) assignICode.value;

				IdentExp rightIdent = (IdentExp) assignExp.right;
				String reg = loadVariableToReg(rightIdent, ICode.Type.INT, assignICode);
				
				String finalPlace = rGen.getReg(assignICode.place, assignICode);

				cGen.addInstruction("MVN " + finalPlace + ", " + reg);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getTempReg(assignICode.place + "_offset", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + finalPlace + "[R13, -" + tempOffset + ']');
				}

				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initBool0() {
		codeGenFunctions.put(Pattern.bool0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def)icode;
				BoolExp assignExp = (BoolExp) assignICode.val;

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.BYTE, assignExp.trueFalse ? 1 : 0);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("MOV " + tempValue + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("MOV " + tempValue + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("MOV " + tempValue + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STRB " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initBool1() {
		codeGenFunctions.put(Pattern.bool1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				BoolExp assignExp = (BoolExp) assignICode.value;

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					String reg = rGen.getReg(assignICode.place, assignICode);
					cGen.addInstruction("MOV " + reg + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("STRB " + reg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("MOV " + tempValue + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("MOV " + tempValue + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("MOV " + tempValue + ", #" + (assignExp.trueFalse ? 1 : 0));
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STRB " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initInt0() {
		codeGenFunctions.put(Pattern.int0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def)icode;
				IntExp assignExp = (IntExp) assignICode.val;

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD, assignExp.value);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					cGen.addVariable(assignICode.label + "_value" + VariableLength.WORD, assignExp.value);
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					cGen.addVariable(assignICode.label + "_value" + VariableLength.WORD, assignExp.value);
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					cGen.addVariable(assignICode.label + "_value" + VariableLength.WORD, assignExp.value);
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initInt1() {
		codeGenFunctions.put(Pattern.int1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				IntExp assignExp = (IntExp) assignICode.value;

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					String random = iGen.genNext();
					cGen.addVariable(random, VariableLength.WORD, assignExp.value);
					String tempValue = rGen.getTempReg(random, assignICode);
					cGen.addInstruction("LDR " + tempValue + ", " + random);
					cGen.addInstruction("STR " + tempValue + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					cGen.addVariable(assignICode.place + "_value" + VariableLength.WORD, assignExp.value);
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					cGen.addVariable(assignICode.place + "_value" + VariableLength.WORD, assignExp.value);
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					cGen.addVariable(assignICode.place + "_value" + VariableLength.WORD, assignExp.value);
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initReal0() {
		codeGenFunctions.put(Pattern.real0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def)icode;
				RealExp assignExp = (RealExp) assignICode.val;

				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, assignExp.realValue);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					cGen.addVariable(assignICode.label + "_value", assignExp.realValue);
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					cGen.addVariable(assignICode.label + "_value", assignExp.realValue);
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					cGen.addVariable(assignICode.label + "_value", assignExp.realValue);
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initReal1() {
		codeGenFunctions.put(Pattern.real1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				RealExp assignExp = (RealExp) assignICode.value;

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					String random = iGen.genNext();
					cGen.addVariable(random, assignExp.realValue);
					String tempValue = rGen.getTempReg(random, assignICode);
					cGen.addInstruction("LDR " + tempValue + ", " + random);
					cGen.addInstruction("STR " + tempValue + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					cGen.addVariable(assignICode.place + "_value", assignExp.realValue);
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					cGen.addVariable(assignICode.place + "_value", assignExp.realValue);
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					cGen.addVariable(assignICode.place + "_value", assignExp.realValue);
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initStr0() {
		codeGenFunctions.put(Pattern.str0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def)icode;
				StrExp assignExp = (StrExp)assignICode.val;

				if (assignICode.scope == ICode.Scope.GLOBAL) {
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
							cGen.addVariable(assignICode.label + "_const_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.label + "_const_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.label + "_value", VariableLength.WORD, assignICode.label + "_const_value");
					
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.label + "_const_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.label + "_const_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.label + "_value", VariableLength.WORD, assignICode.label + "_const_value");
					
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.label + "_const_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.label + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.label + "_value", VariableLength.WORD, assignICode.label + "_const_value");
					
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					String tempValue = rGen.getTempReg(assignICode.label + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.label + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label);
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initStr1() {
		codeGenFunctions.put(Pattern.str1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				StrExp assignExp = (StrExp)assignICode.value;

				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
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
					
					String reg = rGen.getReg(assignICode.place, assignICode);
					cGen.addInstruction("LDR " + reg + ", " + assignICode.place + "_value");
					cGen.addInstruction("STR " + reg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.place + "_const_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.place + "_const_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.place + "_value", VariableLength.WORD, assignICode.place + "_const_value");
					
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.place + "_const_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.place + "_const_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.place + "_value", VariableLength.WORD, assignICode.place + "_const_value");
					
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				} else {
					String strToMem = assignExp.value;
					for(int i = 0; i < strToMem.length(); i++) {
						char letter = strToMem.charAt(i);
						if(i == 0) {
							cGen.addVariable(assignICode.place + "_const_value", VariableLength.BYTE, (int)letter);
						} else {
							cGen.addVariable(VariableLength.BYTE, (int)letter);
						}
					}
					
					if(strToMem.length() > 0) {
						cGen.addVariable(VariableLength.BYTE, (int)'\0');
					} else {
						cGen.addVariable(assignICode.place + "_value", VariableLength.BYTE, (int)'\0');
					}
					
					
					cGen.addVariable(assignICode.place + "_value", VariableLength.WORD, assignICode.place + "_const_value");
					
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					String tempValue = rGen.getTempReg(assignICode.place + "_value", assignICode);
					
					cGen.addInstruction("LDR " + tempValue + ", " + assignICode.place + "_value");
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place);
					cGen.addInstruction("STR " + tempValue + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initId0() {
		codeGenFunctions.put(Pattern.id0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.BOOL, assignICode);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.BYTE);
					cGen.addInstruction("STRB " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STRB " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initId1() {
		codeGenFunctions.put(Pattern.id1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.REAL, assignICode);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initId2() {
		codeGenFunctions.put(Pattern.id2, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.INT, assignICode);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initId3() {
		codeGenFunctions.put(Pattern.id3, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Def assignICode = (Def) icode;
				IdentExp exp = (IdentExp) assignICode.val;

				String tempReg = loadVariableToReg(exp, ICode.Type.STRING, assignICode);
				
				if (assignICode.scope == ICode.Scope.GLOBAL) {
					cGen.addVariable(assignICode.label, VariableLength.WORD);
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.label);
				} else if (assignICode.scope == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.scope == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.label + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.label, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.label + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initId4() {
		codeGenFunctions.put(Pattern.id4, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp) assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.BOOL, assignICode);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STRB " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STRB " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initId5() {
		codeGenFunctions.put(Pattern.id5, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp) assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.REAL, assignICode);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initId6() {
		codeGenFunctions.put(Pattern.id6, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp) assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.INT, assignICode);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	private void initId7() {
		codeGenFunctions.put(Pattern.id7, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Assign assignICode = (Assign)icode;
				IdentExp exp = (IdentExp) assignICode.value;

				String tempReg = loadVariableToReg(exp, ICode.Type.STRING, assignICode);
				
				if (assignICode.getScope() == ICode.Scope.GLOBAL) {
					cGen.addInstruction("STR " + tempReg + ", " + assignICode.place);
				} else if (assignICode.getScope() == ICode.Scope.PARAM) {
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else if (assignICode.getScope() == ICode.Scope.RETURN) {				
					String tempOffset = rGen.getTempReg(assignICode.place + "_inner", assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				} else {
					String tempOffset = rGen.getReg(assignICode.place, assignICode);
					cGen.addInstruction("LDR " + tempOffset + ", " + assignICode.place + "_inner");
					cGen.addInstruction("STR " + tempReg + "[R13, -" + tempOffset + ']');
				}
				
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	public void initIf0() {
		codeGenFunctions.put(Pattern.if0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, icode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BLT " + ifStatement.ifTrue);
				cGen.addInstruction("BGE " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	public void initIf1() {
		codeGenFunctions.put(Pattern.if1, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, icode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BGT " + ifStatement.ifTrue);
				cGen.addInstruction("BLE " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	public void initIf2() {
		codeGenFunctions.put(Pattern.if2, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, icode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BLE " + ifStatement.ifTrue);
				cGen.addInstruction("BGT " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	public void initIf3() {
		codeGenFunctions.put(Pattern.if3, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, icode);

				cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
				cGen.addInstruction("BGE " + ifStatement.ifTrue);
				cGen.addInstruction("BLT " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	public void initIf4() {
		codeGenFunctions.put(Pattern.if4, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, icode);

				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("BEQ " + ifStatement.ifTrue);
				cGen.addInstruction("BNE " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	public void initIf5() {
		codeGenFunctions.put(Pattern.if5, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.INT, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.INT, icode);
				
				cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
				cGen.addInstruction("BNE " + ifStatement.ifTrue);
				cGen.addInstruction("BEQ " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}
	
	public void initIf6() {
		codeGenFunctions.put(Pattern.if6, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.BOOL, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.BOOL, icode);

				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("BEQ " + ifStatement.ifTrue);
				cGen.addInstruction("BNE " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	public void initIf7() {
		codeGenFunctions.put(Pattern.if7, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				If ifStatement = (If) icode;
				BinExp exp = ifStatement.exp;

				IdentExp left = (IdentExp) exp.left;
				IdentExp right = (IdentExp) exp.right;

				String leftReg = loadVariableToReg(left, ICode.Type.BOOL, icode);
				String rightReg = loadVariableToReg(right, ICode.Type.BOOL, icode);
				
				cGen.addInstruction("TEQB " + leftReg + ", " + rightReg);
				cGen.addInstruction("BNE " + ifStatement.ifTrue);
				cGen.addInstruction("BEQ " + ifStatement.ifFalse);
				rGen.freeTempRegs();

				return null;
			}
		});
	}

	private void initGoto0() {
		codeGenFunctions.put(Pattern.goto0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Goto gotoICode = (Goto) icode;
				cGen.addInstruction("B " + gotoICode.label);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initLabel0() {
		codeGenFunctions.put(Pattern.label0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Label labelICode = (Label) icode;
				cGen.setLabel(labelICode.label);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initProcLabel0() {
		codeGenFunctions.put(Pattern.procLabel0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				int x = i + 1;
				int toAllocate = 0;
				ICode instruction = null;
				do {
					instruction = intermediateCode.get(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							if (definition.type == ICode.Type.BOOL) {
								toAllocate += 1;
							} else {
								toAllocate += 4;
							}
						}
					}
					x++;
				} while (!(instruction instanceof Return));

				int offSet = toAllocate;
				x = i + 1;
				while (offSet > 0) {
					instruction = intermediateCode.get(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							cGen.addVariable(definition.label + "_inner", VariableLength.WORD, offSet);
							if (definition.type == ICode.Type.BOOL) {
								offSet -= 1;
							} else {
								offSet -= 4;
							}
						}
					} else if (instruction instanceof Return) {
						break;
					}
					x++;
				}

				ProcLabel labelICode = (ProcLabel) icode;
				cGen.setLabel(labelICode.label);
				rGen.freeTempRegs();
				return null;
			}
		});
	}
	
	private void initDataSectionHeader() {
		codeGenFunctions.put(Pattern.dataSectionHeader, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				int x = i + 1;
				int toAllocate = 0;
				ICode instruction = null;
				do {
					instruction = intermediateCode.get(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							if (definition.type == ICode.Type.BOOL) {
								toAllocate += 1;
							} else {
								toAllocate += 4;
							}
						}
					}
					x++;
				} while (!(instruction instanceof BssSec));

				int offSet = toAllocate;
				x = i + 1;
				while (offSet > 0) {
					instruction = intermediateCode.get(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							cGen.addVariable(definition.label, VariableLength.WORD, offSet);
							if (definition.type == ICode.Type.BOOL) {
								offSet -= 1;
							} else {
								offSet -= 4;
							}
						}
					} else if (instruction instanceof BssSec) {
						break;
					}
					x++;
				}
				
				rGen.freeTempRegs();
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
					instruction = intermediateCode.get(x);
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
				rGen.freeTempRegs();
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
				do {
					instruction = intermediateCode.get(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							if (definition.type == ICode.Type.BOOL) {
								toAllocate += 1;
							} else {
								toAllocate += 4;
							}
						}
					}
					x++;
				} while (!(instruction instanceof End));

				int offSet = toAllocate;
				x = i + 1;
				while (offSet > 0) {
					instruction = intermediateCode.get(x);
					if (instruction instanceof Def) {
						Def definition = (Def) instruction;
						if (definition.scope == ICode.Scope.LOCAL) {
							cGen.addVariable(definition.label, VariableLength.WORD, offSet);
							if (definition.type == ICode.Type.BOOL) {
								offSet -= 1;
							} else {
								offSet -= 4;
							}
						}
					} else if (instruction instanceof End) {
						break;
					}
					x++;
				}
				
				rGen.freeTempRegs();
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
					instruction = intermediateCode.get(x);
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
				
				cGen.addInstruction("SUB R13, R13, #" + localStack);
				cGen.addInstruction("STP");
				rGen.freeTempRegs();
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

	private void initReturn0() {
		codeGenFunctions.put(Pattern.return0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				int x = i - 1;
				ICode instruction = null;
				int localStack = 0;
				do {
					instruction = intermediateCode.get(x);
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
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initCall0() {
		codeGenFunctions.put(Pattern.call0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Call procICode = (Call) icode;
				int totalLength = procICode.params.size();

				int totalStackFrameLength = getStackFrameLength(procICode);
				int offset = totalStackFrameLength;

				cGen.addVariable(procICode.pname + "_return_address_outer", VariableLength.WORD, offset);
				offset -= 4;

				for (Def param : procICode.params) {
					if (param.type == ICode.Type.BOOL) {
						cGen.addVariable(param.label + "_outer", VariableLength.BYTE, offset);
						offset -= 1;
					} else {
						cGen.addVariable(param.label + "_outer", VariableLength.WORD, offset);
						offset -= 4;
					}
				}

				cGen.addInstruction("ADD R13, R13, #" + totalStackFrameLength);

				String myReg = rGen.getReg(procICode.pname + "_return_address_outer", procICode);
				cGen.addInstruction("LDR " + myReg + ", " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("STR R14, [R13, -" + myReg + "]");

				for (int x = 0; x < totalLength; x++) {
					Def sourceDest = procICode.params.get(x);

					String oldReg = genExpression(sourceDest.val, sourceDest.type, procICode);

					String tempReg = rGen.getTempReg(sourceDest.label + "_outer", procICode);
					cGen.addInstruction("LDR " + tempReg + ", " + sourceDest.label + "_outer");
					if (sourceDest.type == ICode.Type.BOOL) {
						cGen.addInstruction("STRB " + oldReg + ", [R13, -" + tempReg + "]");
					} else {
						cGen.addInstruction("STR " + oldReg + ", [R13, -" + tempReg + "]");
					}
				}
				cGen.addInstruction("BL " + procICode.pname);

				// Now to load the Return address from the Stack back into the Link Register R14
				cGen.addInstruction("LDR R14, " + procICode.pname + "_return_address_outer");
				cGen.addInstruction("LDR R14, [R13, -R14]");
				cGen.addInstruction("SUB R13, R13, #" + totalStackFrameLength);
				rGen.freeTempRegs();
				return null;
			}
		});
	}

	private void initInline0() {
		codeGenFunctions.put(Pattern.inline0, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ICode icode = intermediateCode.get(i);
				Inline inline = (Inline) icode;

				String instruction = inline.inlineAssembly;

				StringBuilder resultInstruction = new StringBuilder();
				int index = 0;
				int paramIndex = 0;
				List<IdentExp> params = inline.params;
				while (index < instruction.length()) {
					char letterAtFirstIndex = instruction.charAt(index);
					if (letterAtFirstIndex == '%') {
						index++;
						if (index >= instruction.length()) {
							break;
						}
						char formatSpecifierLetter = instruction.charAt(index);
						if (formatSpecifierLetter == 'A' || formatSpecifierLetter == 'a') {
							if (paramIndex < params.size()) {
								IdentExp addressParam = params.get(paramIndex);
								resultInstruction.append(addressParam.ident);
								paramIndex++;
							} else {
								errorLog.add("No paramater to substite %a found at " + paramIndex,
										new Position(i, index));
							}
						} else if (formatSpecifierLetter == 'R' || formatSpecifierLetter == 'r') {
							if (paramIndex < params.size()) {
								IdentExp addresParam = params.get(paramIndex);
								String regParam = rGen.getReg(addresParam.ident, icode);
								resultInstruction.append(regParam);
								paramIndex++;
							} else {
								errorLog.add("No paramater to substite %r found at " + paramIndex,
										new Position(i, index));
							}
						} else {
							errorLog.add("Invalid adress or paramater specifier found expected %r or %a",
									new Position(i, index));
						}
					} else {
						resultInstruction.append(letterAtFirstIndex);
					}

					index++;
				}

				cGen.addInstruction(resultInstruction.toString());
				rGen.freeTempRegs();
				return null;
			}
		});
	}

}
