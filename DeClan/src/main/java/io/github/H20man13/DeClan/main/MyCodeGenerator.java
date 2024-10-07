package io.github.H20man13.DeClan.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
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

    public MyCodeGenerator(String outputFile, LiveVariableAnalysis analysis, Prog program, ErrorLog errorLog){
        try{
            this.intermediateCode = program.getICode();
            this.cGen = new ArmCodeGenerator(outputFile);
            this.rGen = new ArmRegisterGenerator(cGen, analysis);
            this.iGen = new IrRegisterGenerator();
            String place;
            do{
                place = iGen.genNext();
            } while(program.containsPlace(place));
            this.errorLog = errorLog;
            this.codeGenFunctions = new HashMap<>();
            this.i = 0;
            initCodeGenFunctions();
        } catch(Exception exp){
            errorLog.add(exp.toString(), new Position(i, 0));
        }
    }


    private boolean genICode(ICode icode1, ICode icode2) throws Exception{
        P possibleTwoStagePattern = P.PAT(icode1.asPattern(), icode2.asPattern());
        if(codeGenFunctions.containsKey(possibleTwoStagePattern)){
            Callable<Void> codeGenFunction = codeGenFunctions.get(possibleTwoStagePattern);
            codeGenFunction.call();
            i++;
            return true;
        }
        return false;
    }

    private boolean genICode(ICode icode) throws Exception{
        P oneStagePattern = icode.asPattern();

        if(codeGenFunctions.containsKey(oneStagePattern)){
            codeGenFunctions.get(oneStagePattern).call();
            return true;
        } else {
            errorLog.add("Pattern \n\n" + oneStagePattern.toString() + "\n\n" + "not found", new Position(i, 0));
            return false;
        }
    }

    public void codeGen(){
        try{
            int size = intermediateCode.size();

            for(i = 0; i < size; i++){
                ICode icode1 = intermediateCode.get(i);
                if(i + 1 < size){
                    ICode icode2 = intermediateCode.get(i + 1);
                    if(!genICode(icode1, icode2) && !genICode(icode1)){
                        errorLog.add("Error cannot generate icode " + icode1, new Position(i, 0));
                    }
                } else if(!genICode(icode1)){
                    errorLog.add("Error cannot generate icode " + icode1, new Position(i, 0));
                }
            }

            cGen.writeToStream();
        } catch(Exception exp) {
            errorLog.add(exp.toString(), new Position(i, 0));
        }
    }

    private void initCodeGenFunctions() {
        //Init Multiply and Accumulate Patterns
        initMultiplyAndAccumulate0();

        //Init Proc with Return Pattern
        initCallWithReturn0();

        //Init Add Patterns
        initAdd0();
        
        //Init Sub Patterns
        initSub0();

        //Init Mul Patterns
        initMul0();

        //Init Div patterns
        initDiv0();

        //Init divide patterns
        initDivide0();

        //Init Mod patterns
        initMod0();

        //Init bitwise And patterns
        initBitwiseAnd0();

        //Init bitwise Or patterns
        initBitwiseOr0();

        //Init Xor Patterns
        initBitwiseExclusiveOr0();

        //Init Left Shift patterns
        initBitShiftLeft0();

        //Init Right Shift patterns
        initBitShiftRight0();

        //Init Ge patterns
        initGe0();

        //Init Gt Patterns
        initGt0();

        //Init Lt Patterns
        initLt0();

        //Init Le Patterns
        initLe0();

        //Init eq Patterns
        initEq0();

        //Initialize the ne Patterns
        initNe0();

        //Initialize Logical And Patterns
        initAnd0();

        //Initialize Logical Or Patterns
        initOr0();

        //Initialize the Neg patterns
        initNeg0();
        initNeg2();

        //Initiaize the BNot patterns
        initBnot1();

        //Init bitwise Not patterns
        initBitwiseNot0();

        //Initialize Bool Constant Patterns
        initBool0();
        //Initialize Real Constant Patterns
        initReal0();
        //Initialize Int Constant Patterns
        initInt0();
        //Initialize Id Patterns
        initId0();

        //Initialize If Statement Patterns
        initIf0();
        initIf4();
        initIf8();
        initIf12();
        initIf16();
        initIf23();

        //Init Goto Pattern
        initGoto0();

        //Init Label Pattern
        initLabel0();
        
        //Init Proc Label Pattern
        initProcLabel0();

        //Init End Pattern
        initEnd0();

        //Init Return Pattern
        initReturn0();

        //Init Proc Pattern
        initCall0();

        //Init Inline Assembly Pattern
        initInline0();
    }

    private void initMultiplyAndAccumulate0(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IdentExp ident1Left = (IdentExp)exp1.left;
                IdentExp ident1Right = (IdentExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);
                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident1Right.ident);

                if(ass1.place.equals(ident2Left.ident) || ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    if(ass1.place.equals(ident2Left.ident)){
                        String place2Right = rGen.getReg(ident2Right.ident, icode2);
                        cGen.addInstruction("LDR " + place2Right + ", " + ident2Right.ident);
                        cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Right);
                        cGen.addVariable(ass2.place, VariableLength.WORD);
                        cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                    } else {
                         String place2Left = rGen.getReg(ident2Left.ident, icode2);
                         cGen.addInstruction("LDR " + place2Left + ", " + ident2Left.ident);
                         cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                         cGen.addVariable(ass2.place, VariableLength.WORD);
                         cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                    }
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);
                    String place2Right = rGen.getReg(ident2Right.ident, icode2);

                    String finalPlace2 = rGen.getReg(ass2.place, icode2);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("MUL " + finalPlace2 + ", " + place2Left + ", " + place2Right);
                    cGen.addInstruction("STR " + finalPlace2 + ", " + ass2.place);
                }

                rGen.freeTempRegs();

                return null;
            }  
        });
    }

    private void initCallWithReturn0(){
        codeGenFunctions.put(Pattern.callWithReturn0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Call procICode = (Call)icode;
                int totalLength = procICode.params.size();
                int totalReturnStackLength = totalLength;
                int totalReturnStackLengthInBytes = totalReturnStackLength * 4;

                ICode retICode = intermediateCode.get(i + 1);
                Assign returnPlacement = (Assign)retICode;
                //The First thing we need to do is allocate all the code we can for the Paramaters
                int toAllocateToStack = totalReturnStackLengthInBytes + 8;
                int toPlaceReturnAddressOnStack = toAllocateToStack;
                int offset = toAllocateToStack;
                offset -= 4;
                cGen.addVariable(returnPlacement.value.toString(), offset);
                for(Def param: procICode.params){
                    if(param.type == ICode.Type.REAL){
                        offset -= 4;
                        cGen.addVariable(param.label, VariableLength.WORD, offset);
                    } else if(param.type == ICode.Type.BOOL){
                        offset -= 1;
                        cGen.addVariable(param.label, VariableLength.WORD, offset);
                    } else {
                        offset -= 4;
                        cGen.addVariable(param.label, VariableLength.WORD, offset);
                    } 
                }
                
                cGen.addInstruction("ADD R13, R13, #" + toAllocateToStack);
                cGen.addInstruction("STR R14, [R13, #-"+toPlaceReturnAddressOnStack+"]");
                for(int x = 0; x < totalLength; x++){
                    Def sourceDest = procICode.params.get(x);

                    String offSetRegister = iGen.genNext();
                    String offReg = rGen.getTempReg(offSetRegister, procICode);
                    cGen.addInstruction("LDR " + offReg + ", " + sourceDest.label);

                    String reg = rGen.getReg(sourceDest.val.toString(), procICode);
                    //TODO finish later 
                    cGen.addInstruction("LDR " + reg + ", " + sourceDest.label);
                    cGen.addInstruction("STR " + reg +  ", [R13,-" + offReg + "]");
                }
                cGen.addInstruction("BL " + procICode.pname);

                //Now to load the Return value from the Stack
                String temp = rGen.getTempReg(returnPlacement.value.toString(), retICode);
                cGen.addInstruction("LDR " + temp + ", " + returnPlacement.value.toString());
                cGen.addInstruction("LDR " + temp + ", [R13, -"+temp+"]");

                cGen.addVariable(returnPlacement.place, VariableLength.WORD);
                cGen.addInstruction("STR " + temp + ", " + returnPlacement.place);

                //Now to load the Return address from the Stack back into the Link Register R14
                cGen.addInstruction("LDR R14, [R13, #-"+toPlaceReturnAddressOnStack+"]");

                cGen.addInstruction("SUB R13, R13, #" + toAllocateToStack);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd0(){
        codeGenFunctions.put(Pattern.add0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub0(){
        codeGenFunctions.put(Pattern.sub0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul0(){
        codeGenFunctions.put(Pattern.mul0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initDiv0(){
        codeGenFunctions.put(Pattern.div0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("ADD R13, R13, #16");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("STR R14, [R13, #-16]");
                cGen.addInstruction("BL Div");
                cGen.addInstruction("LDR " + finalPlace + ", [R13, #-12]");
                cGen.addInstruction("LDR R14, [R13, #-16]");
                cGen.addInstruction("SUB R13, R13, #16");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initDivide0(){
        codeGenFunctions.put(Pattern.divide0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("ADD R13, R13, #16");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("STR R14, [R13, #-16]");
                cGen.addInstruction("BL Divide");
                cGen.addInstruction("LDR " + finalPlace + ", [R13, #-12]");
                cGen.addInstruction("LDR R14, [R13, #-16]");
                cGen.addInstruction("SUB R13, R13, #16");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMod0(){
        codeGenFunctions.put(Pattern.mod0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("STR " + leftReg + ", modTotal");
                cGen.addInstruction("STR " + rightReg + ", modDividend");
                cGen.addInstruction("B Mod");
                cGen.addInstruction("LDR " + finalPlace + ", modResult");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBitwiseAnd0(){
        codeGenFunctions.put(Pattern.bitwiseAnd0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("AND " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBitwiseOr0(){
        codeGenFunctions.put(Pattern.bitwiseOr0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("ORR " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBitwiseExclusiveOr0(){
        codeGenFunctions.put(Pattern.bitwiseExclusiveOr0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("EOR " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBitShiftLeft0(){
        codeGenFunctions.put(Pattern.leftShift0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("MOV " + finalPlace + ", " + leftReg + ", LSL " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBitShiftRight0(){
        codeGenFunctions.put(Pattern.rightShift0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("MOV " + finalPlace + ", " + leftReg + ", LSR " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGt0(){
        codeGenFunctions.put(Pattern.gt0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVGT " + finalPlace + ", #1");
                cGen.addInstruction("MOVLE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe0(){
        codeGenFunctions.put(Pattern.ge0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVGE " + finalPlace + ", #1");
                cGen.addInstruction("MOVLT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initLt0(){
        codeGenFunctions.put(Pattern.lt0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLT " + finalPlace + ", #1");
                cGen.addInstruction("MOVGE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe0(){
        codeGenFunctions.put(Pattern.le0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLE " + finalPlace + ", #1");
                cGen.addInstruction("MOVGT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq0(){
        codeGenFunctions.put(Pattern.eq0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
                cGen.addInstruction("MOVNE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe0(){
        codeGenFunctions.put(Pattern.ne0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    
    private void initAnd0(){
        codeGenFunctions.put(Pattern.and0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.BYTE);

                cGen.addInstruction("LDRB " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDRB " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("AND " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initOr0(){
        codeGenFunctions.put(Pattern.or0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);
                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.BYTE);

                cGen.addInstruction("LDRB " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDRB " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("ORR " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STRB " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initNeg0(){
        codeGenFunctions.put(Pattern.neg0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                UnExp assignExp = (UnExp)assignICode.value;
                
                IdentExp rightIdent = (IdentExp)assignExp.right;

                cGen.addVariable(assignICode.place, VariableLength.WORD);

                String reg = rGen.getReg(rightIdent.ident, assignICode);
                
                cGen.addInstruction("LDR " + reg + ", " + rightIdent.ident);
                
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + reg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL INeg");
                cGen.addInstruction("LDR " + reg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("STR " + reg + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initNeg2(){
        codeGenFunctions.put(Pattern.neg2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                UnExp assignExp = (UnExp)assignICode.value;
                
                IdentExp rightIdent = (IdentExp)assignExp.right;

                cGen.addVariable(assignICode.place, VariableLength.WORD);

                String reg = rGen.getReg(rightIdent.ident, assignICode);
                
                cGen.addInstruction("LDR " + reg + ", " + rightIdent.ident);
                
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + reg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RNeg");
                cGen.addInstruction("LDR " + reg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("STR " + reg + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBnot1(){
        codeGenFunctions.put(Pattern.bnot1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                UnExp assignExp = (UnExp)assignICode.value;
                
                IdentExp rightIdent = (IdentExp)assignExp.right;

                cGen.addVariable(assignICode.place, VariableLength.WORD);

                String reg = rGen.getReg(rightIdent.ident, assignICode);

                cGen.addInstruction("LDR " + reg + ", " + rightIdent.ident);
                cGen.addInstruction("TEQ " + reg + ", #0");
                cGen.addInstruction("MOVEQ " + reg + ", #1");
                cGen.addInstruction("MOVNE " + reg + ", #0");
                cGen.addInstruction("STR " + reg + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initBitwiseNot0(){
        codeGenFunctions.put(Pattern.bitwiseNot0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                UnExp assignExp = (UnExp)assignICode.value;
                
                IdentExp rightIdent = (IdentExp)assignExp.right;

                cGen.addVariable(assignICode.place, VariableLength.WORD);

                String reg = rGen.getReg(rightIdent.ident, assignICode);
                
                cGen.addInstruction("LDR " + reg + ", " + rightIdent.ident);
                cGen.addInstruction("MVN " + reg + ", " + reg);
                cGen.addInstruction("STR " + reg + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBool0(){
        codeGenFunctions.put(Pattern.bool0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BoolExp assignExp = (BoolExp)assignICode.value;

                String temp = iGen.genNext();
                if(assignExp.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                String reg = rGen.getReg(assignICode.place, assignICode);
                cGen.addInstruction("LDRB " + reg + ", " + temp);
                cGen.addInstruction("STR " + reg + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initInt0(){
        codeGenFunctions.put(Pattern.int0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                IntExp assignExp = (IntExp)assignICode.value;

                cGen.addVariable(assignICode.place, VariableLength.WORD, assignExp.value);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initReal0(){
        codeGenFunctions.put(Pattern.real0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                RealExp assignExp = (RealExp)assignICode.value;

                cGen.addVariable(assignICode.place, assignExp.realValue);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initId0(){
        codeGenFunctions.put(Pattern.id0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                IdentExp exp = (IdentExp)assignICode.value;
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                String tempReg = rGen.getReg(exp.ident, assignICode);
                cGen.addInstruction("LDR " + tempReg + ", " + exp.ident);
                cGen.addInstruction("STR " + tempReg + ", " + assignICode.place); 
                rGen.freeTempRegs();

                return null;
            }            
        });
    }

    public void initIf0(){
        codeGenFunctions.put(Pattern.if0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BLT " + ifStatement.ifTrue);
                cGen.addInstruction("BGE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf4(){
        codeGenFunctions.put(Pattern.if4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BGT " + ifStatement.ifTrue);
                cGen.addInstruction("BLE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf8(){
        codeGenFunctions.put(Pattern.if8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BLE " + ifStatement.ifTrue);
                cGen.addInstruction("BGT " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf12(){
        codeGenFunctions.put(Pattern.if12, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BGE " + ifStatement.ifTrue);
                cGen.addInstruction("BLT " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf16(){
        codeGenFunctions.put(Pattern.if16, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
                cGen.addInstruction("BEQ " + ifStatement.ifTrue);
                cGen.addInstruction("BNE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf23(){
        codeGenFunctions.put(Pattern.if23, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("TEQ " + leftReg + ", " + rightReg);
                cGen.addInstruction("BNE " + ifStatement.ifTrue);
                cGen.addInstruction("BEQ " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initGoto0(){
        codeGenFunctions.put(Pattern.goto0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Goto gotoICode = (Goto)icode;
                cGen.addInstruction("B " + gotoICode.label);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initLabel0(){
        codeGenFunctions.put(Pattern.label0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Label labelICode = (Label)icode;
                cGen.setLabel(labelICode.label);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initProcLabel0(){
        codeGenFunctions.put(Pattern.procLabel0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                ProcLabel labelICode = (ProcLabel)icode;
                cGen.setLabel(labelICode.label);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initEnd0(){
        codeGenFunctions.put(Pattern.end0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                cGen.addInstruction("STP");
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initReturn0(){
        codeGenFunctions.put(Pattern.return0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode instruction = intermediateCode.get(i);
                String register = iGen.genNext();
                String literalRegister = rGen.getTempReg(register, instruction);
                cGen.addInstruction("MOV R15, R14");
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initCall0(){
        codeGenFunctions.put(Pattern.call0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Call procICode = (Call)icode;
                int totalLength = procICode.params.size();
                int totalReturnStackLength = totalLength;
                int totalReturnStackLengthInBytes = totalReturnStackLength * 4;
                //The First thing we need to do is allocate all the code we can for the Paramaters
                int toAllocateToStack = totalReturnStackLengthInBytes + 4;
                int toPlaceReturnAddressOnStack = toAllocateToStack;
                int offset = toAllocateToStack;
                for(Def param: procICode.params){
                    offset -= 4;
                    cGen.addVariable(param.label, VariableLength.WORD, offset);
                }
                
                cGen.addInstruction("ADD R13, R13, #" + toAllocateToStack);
                cGen.addInstruction("STR R14, [R13, #-"+toPlaceReturnAddressOnStack+"]");
                for(int x = 0; x < totalLength; x++){
                    Def sourceDest = procICode.params.get(x);

                    String offSetRegister = iGen.genNext();
                    String offReg = rGen.getTempReg(offSetRegister, procICode);
                    cGen.addInstruction("LDR " + offReg + ", " + sourceDest.label);

                    String reg = rGen.getReg(sourceDest.val.toString(), procICode); 
                    cGen.addInstruction("LDR " + reg + ", " + sourceDest.val.toString());
                    cGen.addInstruction("STR " + reg +  ", [R13,-" + offReg + "]");
                }
                cGen.addInstruction("BL " + procICode.pname);

                //Now to load the Return address from the Stack back into the Link Register R14
                cGen.addInstruction("LDR R14, [R13, #-"+toPlaceReturnAddressOnStack+"]");

                cGen.addInstruction("SUB R13, R13, #" + toAllocateToStack);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initInline0(){
        codeGenFunctions.put(Pattern.inline0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Inline inline = (Inline)icode;

                String instruction = inline.inlineAssembly;

                StringBuilder resultInstruction = new StringBuilder();
                int index = 0;
                int paramIndex = 0;
                List<IdentExp> params = inline.params;
                while(index < instruction.length()){
                    char letterAtFirstIndex = instruction.charAt(index);
                    if(letterAtFirstIndex == '%'){
                        index++;
                        if(index >= instruction.length()){
                            break;
                        }
                        char formatSpecifierLetter = instruction.charAt(index);
                        if(formatSpecifierLetter == 'A' || formatSpecifierLetter == 'a'){
                            if(paramIndex < params.size()){
                                IdentExp addressParam = params.get(paramIndex);
                                resultInstruction.append(addressParam.ident);
                                paramIndex++;
                            } else {
                                errorLog.add("No paramater to substite %a found at " + paramIndex, new Position(i, index));
                            }
                        } else if(formatSpecifierLetter == 'R' || formatSpecifierLetter == 'r'){
                            if(paramIndex < params.size()){
                                IdentExp addresParam = params.get(paramIndex);
                                String regParam = rGen.getReg(addresParam.ident, icode);
                                resultInstruction.append(regParam);
                                paramIndex++;
                            } else {
                                errorLog.add("No paramater to substite %r found at " + paramIndex, new Position(i, index));
                            }
                        } else {
                            errorLog.add("Invalid adress or paramater specifier found expected %r or %a", new Position(i, index));
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
