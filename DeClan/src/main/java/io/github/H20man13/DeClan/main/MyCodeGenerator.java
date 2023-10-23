package io.github.H20man13.DeClan.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.ast.VariableDeclaration;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.Pattern;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.arm.ArmRegisterGenerator;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator.VariableLength;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.ParamAssign;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class MyCodeGenerator {
    private List<ICode> intermediateCode;
    private ArmRegisterGenerator rGen;
    private ArmCodeGenerator cGen;
    private IrRegisterGenerator iGen;
    private ErrorLog errorLog;
    private Map<P, Callable<Void>> codeGenFunctions;

    private int i;

    public MyCodeGenerator(LiveVariableAnalysis analysis, List<ICode> intermediateCode, IrRegisterGenerator iGen, ErrorLog errorLog){
        this.intermediateCode = intermediateCode;
        this.cGen = new ArmCodeGenerator();
        this.rGen = new ArmRegisterGenerator(cGen, analysis);
        this.iGen = iGen;
        this.errorLog = errorLog;
        this.codeGenFunctions = new HashMap<>();
        this.i = 0;
        initCodeGenFunctions();
    }

    public void codeGen(Writer writer){
        try{
            for(i = 0; i < intermediateCode.size(); i++){
                P possibleTwoStagePattern = null;
                if(i + 1 < intermediateCode.size()){
                    possibleTwoStagePattern = P.PAT(intermediateCode.get(i).asPattern(), intermediateCode.get(i + 1).asPattern());
                }

                P oneStagePattern = intermediateCode.get(i).asPattern();


                if(possibleTwoStagePattern != null){
                    if(codeGenFunctions.containsKey(possibleTwoStagePattern)){
                        Callable<Void> codeGenFunction = codeGenFunctions.get(possibleTwoStagePattern);
                        codeGenFunction.call();
                    } else if(codeGenFunctions.containsKey(oneStagePattern)) {
                        Callable<Void> codeGenFunction = codeGenFunctions.get(oneStagePattern);
                        codeGenFunction.call();
                    } else {
                        errorLog.add("Pattern \n\n" + oneStagePattern.toString() + "\n\n" + "not found", new Position(i, 0));
                        break;
                    }
                } else {
                    if(codeGenFunctions.containsKey(oneStagePattern)){
                        codeGenFunctions.get(oneStagePattern).call();
                    } else {
                        errorLog.add("Pattern \n\n" + oneStagePattern.toString() + "\n\n" + "not found", new Position(i, 0));
                        break;
                    }
                }
            }
            cGen.writeToStream(writer);
        } catch(Exception exp) {
            errorLog.add(exp.toString(), new Position(i, 0));
        }
    }

    private void initCodeGenFunctions() {
        //Init Multiply and Accumulate Patterns
        initMultiplyAndAccumulate0();
        initMultiplyAndAccumulate1();
        initMultiplyAndAccumulate2();
        initMultiplyAndAccumulate3();
        initMultiplyAndAccumulate4();
        initMultiplyAndAccumulate5();
        initMultiplyAndAccumulate6();
        initMultiplyAndAccumulate7();
        initMultiplyAndAccumulate8();
        initMultiplyAndAccumulate9();
        initMultiplyAndAccumulate10();
        initMultiplyAndAccumulate11();

        initMultiplyAndAccumulate12();
        initMultiplyAndAccumulate13();
        initMultiplyAndAccumulate14();
        initMultiplyAndAccumulate15();
        initMultiplyAndAccumulate16();
        initMultiplyAndAccumulate17();
        initMultiplyAndAccumulate18();
        initMultiplyAndAccumulate19();
        initMultiplyAndAccumulate20();
        initMultiplyAndAccumulate21();
        initMultiplyAndAccumulate22();

        //Init Add Patterns
        initAdd0();
        initAdd1();
        initAdd2();
        initAdd3();
        initAdd4();
        initAdd5();
        initAdd6();
        initAdd7();
        initAdd8();
        initAdd9();
        
        //Init Sub Patterns
        initSub0();
        initSub1();
        initSub2();
        initSub3();
        initSub4();
        initSub5();
        initSub6();
        initSub7();
        initSub8();
        initSub9();

        //Init Mul Patterns
        initMul0();
        initMul1();
        initMul2();
        initMul3();
        initMul4();
        initMul5();
        initMul6();
        initMul7();
        initMul8();
        initMul9();

        //Init Div patterns
        initDiv0();
        initDiv1();
        initDiv2();
        initDiv3();
        initDiv4();
        initDiv5();
        initDiv6();
        initDiv7();
        initDiv8();
        initDiv9();

        //Init divide patterns
        initDivide0();
        initDivide1();
        initDivide2();
        initDivide3();
        initDivide4();
        initDivide5();
        initDivide6();
        initDivide7();
        initDivide8();
        initDivide9();

        //Init Mod patterns
        initMod0();
        initMod1();
        initMod2();
        initMod3();

        //Init Ge patterns
        initGe0();
        initGe1();
        initGe2();
        initGe3();
        initGe4();
        initGe5();
        initGe6();
        initGe7();
        initGe8();

        //Init Gt Patterns
        initGt0();
        initGt1();
        initGt2();
        initGt3();
        initGt4();
        initGt5();
        initGt6();
        initGt7();
        initGt8();

        //Init Lt Patterns
        initLt0();
        initLt1();
        initLt2();
        initLt3();
        initLt4();
        initLt5();
        initLt6();
        initLt7();
        initLt8();

        //Init Le Patterns
        initLe0();
        initLe1();
        initLe2();
        initLe3();
        initLe4();
        initLe5();
        initLe6();
        initLe7();
        initLe8();

        //Init eq Patterns
        initEq0();
        initEq1();
        initEq2();
        initEq3();
        initEq4();
        initEq5();
        initEq6();
        initEq7();
        initEq8();
        initEq9();
        initEq10();
        initEq11();

        //Initialize the ne Patterns
        initNe0();
        initNe1();
        initNe2();
        initNe3();
        initNe4();
        initNe5();
        initNe6();
        initNe7();
        initNe8();
        initNe9();
        initNe10();
        initNe11();

        //Initialize the Neg patterns
        initNeg0();
        initNeg1();
        initNeg2();
        initNeg3();

        //Initiaize the BNot patterns
        initBnot0();
        initBnot1();

        //Initialize Bool Constant Patterns
        initBool0();
        //Initialize Int Constant Patterns
        initInt0();
        //Initialize Id Patterns
        initId0();

        //Initialize If Statement Patterns
        initIf0();
        initIf1();
        initIf2();
        initIf3();

        initIf4();
        initIf5();
        initIf6();
        initIf7();

        initIf8();
        initIf9();
        initIf10();
        initIf11();
        
        initIf12();
        initIf13();
        initIf14();
        initIf15();

        initIf16();
        initIf17();
        initIf18();
        initIf19();
        initIf20();
        initIf21();
        initIf22();

        initIf23();
        initIf24();
        initIf25();
        initIf26();
        initIf27();
        initIf28();
        initIf29();

        //Init Goto Pattern
        initGoto0();

        //Init Label Pattern
        initLabel0();

        //Init End Pattern
        initEnd0();

        //Init Return Pattern
        initReturn0();

        //Init Proc Pattern
        initProc0();

        //Init Inline Assembly Pattern
        initInline0();

        //Init Param Assign Pattern
        initParamAssign0();
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

    private void initMultiplyAndAccumulate1(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate1, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IntExp int1Left = (IntExp)exp1.left;
                IdentExp ident1Right = (IdentExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String temp = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, VariableLength.WORD, int1Left.value);

                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + temp);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident2Right.ident);

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

    private void initMultiplyAndAccumulate2(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate2, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IdentExp ident1Left = (IdentExp)exp1.left;
                IntExp int1Right = (IntExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);

                String temp = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, VariableLength.WORD,  int1Right.value);


                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + temp);

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

    public void initMultiplyAndAccumulate3(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IntExp int1Left = (IntExp)exp1.left;
                IntExp int1Right = (IntExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String tempLeft = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(tempLeft, icode1);
                cGen.addVariable(tempLeft, VariableLength.WORD, int1Left.value);


                String tempRight = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(tempRight, icode1);
                cGen.addVariable(tempRight, VariableLength.WORD, int1Right.value);


                cGen.addInstruction("LDR " + place1Left + ", " + tempLeft);
                cGen.addInstruction("LDR " + place1Right +  ", " + tempRight);

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

    private void initMultiplyAndAccumulate4(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate4, new Callable<Void>() {
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

                IntExp int2Left = (IntExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);
                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident1Right.ident);

                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);
                    String temp = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp, icode2);
                    cGen.addVariable(temp, VariableLength.WORD, int2Left.value);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp, icode2);
                    cGen.addVariable(temp, VariableLength.WORD, int2Left.value);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp);

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

    private void initMultiplyAndAccumulate5(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate5, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IntExp int1Left = (IntExp)exp1.left;
                IdentExp ident1Right = (IdentExp)exp1.right;

                IntExp int2Left = (IntExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String temp = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, VariableLength.WORD, int1Left.value);

                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + temp);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident2Right.ident);

                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Left = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, VariableLength.WORD, int2Left.value);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);

                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp2Left = iGen.genNextRegister(); 
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, VariableLength.WORD, int2Left.value);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);

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

    private void initMultiplyAndAccumulate6(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate6, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IdentExp ident1Left = (IdentExp)exp1.left;
                IntExp int1Right = (IntExp)exp1.right;

                IntExp int2Left = (IntExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);

                String temp = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, VariableLength.WORD, int1Right.value);


                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + temp);

                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Left = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, VariableLength.WORD, int2Left.value);

                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp2Left = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, VariableLength.WORD, int2Left.value);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);

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

    public void initMultiplyAndAccumulate7(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IntExp int1Left = (IntExp)exp1.left;
                IntExp int1Right = (IntExp)exp1.right;

                IntExp int2Left = (IntExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String tempLeft = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(tempLeft, icode1);
                cGen.addVariable(tempLeft, VariableLength.WORD, int1Left.value);


                String tempRight = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(tempRight, icode1);
                cGen.addVariable(tempRight, VariableLength.WORD, int1Right.value);


                cGen.addInstruction("LDR " + place1Left + ", " + tempLeft);
                cGen.addInstruction("LDR " + place1Right +  ", " + tempRight);

                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Place = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Place, icode2);
                    cGen.addVariable(temp2Place, VariableLength.WORD, int2Left.value);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Place);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp2Place = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Place, icode2);
                    cGen.addVariable(temp2Place, VariableLength.WORD, int2Left.value);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Place);

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

    private void initMultiplyAndAccumulate8(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate8, new Callable<Void>() {
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
                IntExp int2Right = (IntExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);
                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident1Right.ident);

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);
                    String place2Left = rGen.getReg(ident2Left.ident, icode2);
                    cGen.addInstruction("LDR " + place2Left + ", " + ident2Left.ident);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp, icode2);
                    cGen.addVariable(temp, VariableLength.WORD, int2Right.value);
                    cGen.addInstruction("LDR " + place2Right + ", " + temp);

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

    private void initMultiplyAndAccumulate9(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate9, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IntExp int1Left = (IntExp)exp1.left;
                IdentExp ident1Right = (IdentExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IntExp int2Right = (IntExp)exp2.right;

                String temp = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, VariableLength.WORD, int1Left.value);

                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + temp);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident1Right.ident);

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, VariableLength.WORD, int2Right.value);
                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Right);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp2Right = iGen.genNextRegister(); 
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, VariableLength.WORD, int2Right.value);

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

    private void initMultiplyAndAccumulate10(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate10, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IdentExp ident1Left = (IdentExp)exp1.left;
                IntExp int1Right = (IntExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IntExp int2Right = (IntExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);

                String temp = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, VariableLength.WORD, int1Right.value);


                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + temp);

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);

                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Right);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, VariableLength.WORD, int2Right.value);
                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);

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

    public void initMultiplyAndAccumulate11(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate11, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IntExp int1Left = (IntExp)exp1.left;
                IntExp int1Right = (IntExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IntExp int2Right = (IntExp)exp2.right;

                String tempLeft = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(tempLeft, icode1);
                cGen.addVariable(tempLeft, VariableLength.WORD, int1Left.value);


                String tempRight = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(tempRight, icode1);
                cGen.addVariable(tempRight, VariableLength.WORD, int1Right.value);


                cGen.addInstruction("LDR " + place1Left + ", " + tempLeft);
                cGen.addInstruction("LDR " + place1Right +  ", " + tempRight);

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, VariableLength.WORD, int2Right.value);

                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Right);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, VariableLength.WORD, int2Right.value);
                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);

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

    private void initMultiplyAndAccumulate12(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate12, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                RealExp real1Left = (RealExp)exp1.left;
                IdentExp ident1Right = (IdentExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String temp = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, real1Left.realValue);

                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Left + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Left + "[R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");
                
                cGen.addInstruction("LDR " + place1Right +  ", " + ident2Right.ident);

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

    

    private void initMultiplyAndAccumulate13(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate13, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IdentExp ident1Left = (IdentExp)exp1.left;
                RealExp real1Right = (RealExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);

                String temp = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, real1Right.realValue);


                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);

                cGen.addInstruction("LDR " + place1Right +  ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Right + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Right + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initMultiplyAndAccumulate14(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate14, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                RealExp real1Left = (RealExp)exp1.left;
                RealExp real1Right = (RealExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String tempLeft = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(tempLeft, icode1);
                cGen.addVariable(tempLeft, real1Left.realValue);

                String tempRight = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(tempRight, icode1);
                cGen.addVariable(tempRight, real1Right.realValue);


                cGen.addInstruction("LDR " + place1Left + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Left + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Left + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + place1Right +  ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Right + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Right + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initMultiplyAndAccumulate15(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate15, new Callable<Void>() {
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

                RealExp real2Left = (RealExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);
                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident1Right.ident);

                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);
                    
                    String temp = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp, icode2);
                    cGen.addVariable(temp, real2Left.realValue);

                    cGen.addInstruction("LDR " + place2Left + ", " + temp);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp, icode2);
                    cGen.addVariable(temp, real2Left.realValue);

                    cGen.addInstruction("LDR " + place2Left + ", " + temp);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    private void initMultiplyAndAccumulate16(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate16, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                RealExp real1Left = (RealExp)exp1.left;
                IdentExp ident1Right = (IdentExp)exp1.right;

                RealExp real2Left = (RealExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String temp = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, real1Left.realValue);

                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Left + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Left + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + place1Right +  ", " + ident2Right.ident);
                
                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Left = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, real2Left.realValue);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp2Left = iGen.genNextRegister(); 
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, real2Left.realValue);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    private void initMultiplyAndAccumulate17(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate17, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IdentExp ident1Left = (IdentExp)exp1.left;
                RealExp real1Right = (RealExp)exp1.right;

                RealExp real2Left = (RealExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);

                String temp = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, real1Right.realValue);


                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);

                cGen.addInstruction("LDR " + place1Right +  ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Right + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Right + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Left = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, real2Left.realValue);

                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp2Left = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Left, icode2);
                    cGen.addVariable(temp2Left, real2Left.realValue);
                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Left);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    public void initMultiplyAndAccumulate18(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate18, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                RealExp real1Left = (RealExp)exp1.left;
                RealExp real1Right = (RealExp)exp1.right;

                RealExp real2Left = (RealExp)exp2.left;
                IdentExp ident2Right = (IdentExp)exp2.right;

                String tempLeft = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(tempLeft, icode1);
                cGen.addVariable(tempLeft, real1Left.realValue);


                String tempRight = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(tempRight, icode1);
                cGen.addVariable(tempRight, real1Right.realValue);


                cGen.addInstruction("LDR " + place1Left + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Left + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Left + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + place1Right +  ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Right + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Right + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                if(ass1.place.equals(ident2Right.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Place = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Place, icode2);
                    cGen.addVariable(temp2Place, real2Left.realValue);

                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Place);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String temp2Place = iGen.genNextRegister();
                    String place2Left = rGen.getTempReg(temp2Place, icode2);
                    cGen.addVariable(temp2Place, real2Left.realValue);

                    cGen.addInstruction("LDR " + place2Left + ", " + temp2Place);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Left + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Left + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    private void initMultiplyAndAccumulate19(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate19, new Callable<Void>() {
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
                RealExp real2Right = (RealExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);
                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                cGen.addInstruction("LDR " + place1Right +  ", " + ident1Right.ident);

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);
                    String place2Left = rGen.getReg(ident2Left.ident, icode2);
                    cGen.addInstruction("LDR " + place2Left + ", " + ident2Left.ident);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Left);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp, icode2);
                    cGen.addVariable(temp, real2Right.realValue);
                    cGen.addInstruction("LDR " + place2Right + ", " + temp);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Right + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Right + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    private void initMultiplyAndAccumulate20(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate20, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                RealExp real1Left = (RealExp)exp1.left;
                IdentExp ident1Right = (IdentExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                RealExp real2Right = (RealExp)exp2.right;

                String temp = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, real1Left.realValue);

                String place1Right = rGen.getReg(ident1Right.ident, icode1);

                cGen.addInstruction("LDR " + place1Left + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Left + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Left + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + place1Right +  ", " + ident1Right.ident);

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, real2Right.realValue);
                    
                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Right + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Right + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Right);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp2Right = iGen.genNextRegister(); 
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, real2Right.realValue);

                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Right + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Right + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    private void initMultiplyAndAccumulate21(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate21, new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                IdentExp ident1Left = (IdentExp)exp1.left;
                RealExp real1Right = (RealExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                RealExp real2Right = (RealExp)exp2.right;

                String place1Left = rGen.getReg(ident1Left.ident, icode1);

                String temp = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(temp, icode1);
                cGen.addVariable(temp, real1Right.realValue);

                cGen.addInstruction("LDR " + place1Left + ", " + ident1Left.ident);
                
                cGen.addInstruction("LDR " + place1Right +  ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Right + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Right + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);

                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Right);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, real2Right.realValue);
                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Right + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Right + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    public void initMultiplyAndAccumulate22(){
        codeGenFunctions.put(Pattern.multiplyAndAccumulate22, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode1 = intermediateCode.get(i);
                ICode icode2 = intermediateCode.get(i + 1);

                Assign ass1 = (Assign)icode1;
                Assign ass2 = (Assign)icode2;

                BinExp exp1 = (BinExp)ass1.value;
                BinExp exp2 = (BinExp)ass2.value;

                RealExp real1Left = (RealExp)exp1.left;
                RealExp real1Right = (RealExp)exp1.right;

                IdentExp ident2Left = (IdentExp)exp2.left;
                RealExp real2Right = (RealExp)exp2.right;

                String tempLeft = iGen.genNextRegister();
                String place1Left = rGen.getTempReg(tempLeft, icode1);
                cGen.addVariable(tempLeft, real1Left.realValue);


                String tempRight = iGen.genNextRegister();
                String place1Right = rGen.getTempReg(tempRight, icode1);
                cGen.addVariable(tempRight, real1Right.realValue);

                cGen.addInstruction("LDR " + place1Left + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Left + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Left + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + place1Right +  ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + place1Right + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + place1Right + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                if(ass1.place.equals(ident2Left.ident)){
                    String finalPlace = rGen.getReg(ass2.place, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, real2Right.realValue);

                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Right + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Right + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

                    cGen.addInstruction("MLA " + finalPlace + ", " + place1Left + ", " + place1Right + ", " + place2Right);
                    cGen.addVariable(ass2.place, VariableLength.WORD);
                    cGen.addInstruction("STR " + finalPlace + ", " + ass2.place);
                } else {
                    String finalPlace1 = rGen.getReg(ass1.place, icode1);
                    cGen.addVariable(ass1.place, VariableLength.WORD);
                    cGen.addInstruction("ADD " + finalPlace1 + ", " + place1Left + ", " + place1Right);
                    cGen.addInstruction("STR " + finalPlace1 + ", " + ass1.place);

                    String place2Left = rGen.getReg(ident2Left.ident, icode2);

                    String temp2Right = iGen.genNextRegister();
                    String place2Right = rGen.getTempReg(temp2Right, icode2);
                    cGen.addVariable(temp2Right, real2Right.realValue);
                    cGen.addInstruction("LDR " + place2Right + ", " + temp2Right);
                    cGen.addInstruction("ADD R13, R13, #12");
                    cGen.addInstruction("STR " + place2Right + ", [R13, #-4]");
                    cGen.addInstruction("STR R14, [R13, #-12]");
                    cGen.addInstruction("BL RealToInt");
                    cGen.addInstruction("LDR " + place2Right + ", [R13, #-8]");
                    cGen.addInstruction("LDR R14, [R13, #-12]");
                    cGen.addInstruction("SUB R13, R13, #12");

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

    private void initAdd1(){
        codeGenFunctions.put(Pattern.add1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd2(){
        codeGenFunctions.put(Pattern.add2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd3(){
        codeGenFunctions.put(Pattern.add3, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd4(){
        codeGenFunctions.put(Pattern.add4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftInt = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftInt.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd5(){
        codeGenFunctions.put(Pattern.add5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd6(){
        codeGenFunctions.put(Pattern.add6, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd7(){
        codeGenFunctions.put(Pattern.add7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd8(){
        codeGenFunctions.put(Pattern.add8, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initAdd9(){
        codeGenFunctions.put(Pattern.add9, new Callable<Void>() {
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
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + finalPlace + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL IntToReal");
                cGen.addInstruction("LDR " + finalPlace + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");
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

    private void initSub1(){
        codeGenFunctions.put(Pattern.sub1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub2(){
        codeGenFunctions.put(Pattern.sub2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place,  VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub3(){
        codeGenFunctions.put(Pattern.sub3, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub4(){
        codeGenFunctions.put(Pattern.sub4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub5(){
        codeGenFunctions.put(Pattern.sub5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place,  VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub6(){
        codeGenFunctions.put(Pattern.sub6, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub7(){
        codeGenFunctions.put(Pattern.sub7, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub8(){
        codeGenFunctions.put(Pattern.sub8, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("SUB " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initSub9(){
        codeGenFunctions.put(Pattern.sub9, new Callable<Void>() {
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

    private void initMul1(){
        codeGenFunctions.put(Pattern.mul1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul2(){
        codeGenFunctions.put(Pattern.mul2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul3(){
        codeGenFunctions.put(Pattern.mul3, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul4(){
        codeGenFunctions.put(Pattern.mul4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul5(){
        codeGenFunctions.put(Pattern.mul5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul6(){
        codeGenFunctions.put(Pattern.mul6, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul7(){
        codeGenFunctions.put(Pattern.mul7, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul8(){
        codeGenFunctions.put(Pattern.mul8, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");
                
                cGen.addInstruction("MUL " + finalPlace + ", " + leftReg + ", " + rightReg);
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMul9(){
        codeGenFunctions.put(Pattern.mul9, new Callable<Void>() {
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

    private void initDiv1(){
        codeGenFunctions.put(Pattern.div1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp,  VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
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

    private void initDiv2(){
        codeGenFunctions.put(Pattern.div2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
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

    private void initDiv3(){
        codeGenFunctions.put(Pattern.div3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
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

    private void initDiv4(){
        codeGenFunctions.put(Pattern.div4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp,  leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initDiv5(){
        codeGenFunctions.put(Pattern.div5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

     private void initDiv6(){
        codeGenFunctions.put(Pattern.div6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initDiv7(){
        codeGenFunctions.put(Pattern.div7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

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


    private void initDiv8(){
        codeGenFunctions.put(Pattern.div8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initDiv9(){
        codeGenFunctions.put(Pattern.div9, new Callable<Void>() {
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

    private void initDivide1(){
        codeGenFunctions.put(Pattern.divide1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp,  VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
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

    private void initDivide2(){
        codeGenFunctions.put(Pattern.divide2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
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

    private void initDivide3(){
        codeGenFunctions.put(Pattern.divide3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
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

    private void initDivide4(){
        codeGenFunctions.put(Pattern.divide4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp,  leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initDivide5(){
        codeGenFunctions.put(Pattern.divide5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

     private void initDivide6(){
        codeGenFunctions.put(Pattern.divide6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initDivide7(){
        codeGenFunctions.put(Pattern.divide7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

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


    private void initDivide8(){
        codeGenFunctions.put(Pattern.divide8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initDivide9(){
        codeGenFunctions.put(Pattern.div9, new Callable<Void>() {
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

    private void initMod1(){
        codeGenFunctions.put(Pattern.mod1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("STR " + leftReg + ", modTotal");
                cGen.addInstruction("STR " + rightReg + ", modDividend");
                cGen.addInstruction("B mod");
                cGen.addInstruction("LDR " + finalPlace + ", modResult");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMod2(){
        codeGenFunctions.put(Pattern.mod2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("STR " + leftReg + ", modTotal");
                cGen.addInstruction("STR " + rightReg + ", modDividend");
                cGen.addInstruction("B mod");
                cGen.addInstruction("LDR " + finalPlace + ", modResult");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initMod3(){
        codeGenFunctions.put(Pattern.mod3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("STR " + leftReg + ", modTotal");
                cGen.addInstruction("STR " + rightReg + ", modDividend");
                cGen.addInstruction("B mod");
                cGen.addInstruction("LDR " + finalPlace + ", modResult");
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
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe1(){
        codeGenFunctions.put(Pattern.ge1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe2(){
        codeGenFunctions.put(Pattern.ge2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe3(){
        codeGenFunctions.put(Pattern.ge3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe4(){
        codeGenFunctions.put(Pattern.ge4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe5(){
        codeGenFunctions.put(Pattern.ge5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe6(){
        codeGenFunctions.put(Pattern.ge6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                 cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                 cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe7(){
        codeGenFunctions.put(Pattern.ge7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                 cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGe8(){
        codeGenFunctions.put(Pattern.ge8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GE " + finalPlace + ", #1");
                cGen.addInstruction("MOV LT " + finalPlace + ", #0");
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
                cGen.addInstruction("MOV GT " + finalPlace + ", #1");
                cGen.addInstruction("MOV LE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initGt1(){
        codeGenFunctions.put(Pattern.gt1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GT " + finalPlace + ", #1");
                cGen.addInstruction("MOV LE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initGt2(){
        codeGenFunctions.put(Pattern.gt2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GT " + finalPlace + ", #1");
                cGen.addInstruction("MOV LE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initGt3(){
        codeGenFunctions.put(Pattern.gt3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GT " + finalPlace + ", #1");
                cGen.addInstruction("MOV LE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGt4(){
        codeGenFunctions.put(Pattern.gt4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

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

    private void initGt5(){
        codeGenFunctions.put(Pattern.gt5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV GT " + finalPlace + ", #1");
                cGen.addInstruction("MOV LE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initGt6(){
        codeGenFunctions.put(Pattern.gt6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVGT " + finalPlace + ", #1");
                cGen.addInstruction("MOVLE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGt7(){
        codeGenFunctions.put(Pattern.gt7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVGT " + finalPlace + ", #1");
                cGen.addInstruction("MOVLE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initGt8(){
        codeGenFunctions.put(Pattern.gt8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVGT " + finalPlace + ", #1");
                cGen.addInstruction("MOVLE " + finalPlace + ", #0");
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

    private void initLt1(){
        codeGenFunctions.put(Pattern.lt1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LT " + finalPlace + ", #1");
                cGen.addInstruction("MOV GE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLt2(){
        codeGenFunctions.put(Pattern.lt2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LT " + finalPlace + ", #1");
                cGen.addInstruction("MOV GE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLt3(){
        codeGenFunctions.put(Pattern.lt3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LT " + finalPlace + ", #1");
                cGen.addInstruction("MOV GE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initLt4(){
        codeGenFunctions.put(Pattern.lt4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LT " + finalPlace + ", #1");
                cGen.addInstruction("MOV GE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLt5(){
        codeGenFunctions.put(Pattern.lt5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLT " + finalPlace + ", #1");
                cGen.addInstruction("MOVGE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLt6(){
        codeGenFunctions.put(Pattern.lt6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLT " + finalPlace + ", #1");
                cGen.addInstruction("MOVGE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initLt7(){
        codeGenFunctions.put(Pattern.lt7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLT " + finalPlace + ", #1");
                cGen.addInstruction("MOVGE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initLt8(){
        codeGenFunctions.put(Pattern.lt8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

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
                cGen.addInstruction("MOV LE " + finalPlace + ", #1");
                cGen.addInstruction("MOV GT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe1(){
        codeGenFunctions.put(Pattern.le1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LE " + finalPlace + ", #1");
                cGen.addInstruction("MOV GT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe2(){
        codeGenFunctions.put(Pattern.le2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LE " + finalPlace + ", #1");
                cGen.addInstruction("MOV GT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe3(){
        codeGenFunctions.put(Pattern.le3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LE " + finalPlace + ", #1");
                cGen.addInstruction("MOV GT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe4(){
        codeGenFunctions.put(Pattern.le4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV LE " + finalPlace + ", #1");
                cGen.addInstruction("MOV GT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

      private void initLe5(){
        codeGenFunctions.put(Pattern.le5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightInt = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightInt.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);

                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLE " + finalPlace + ", #1");
                cGen.addInstruction("MOVGT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe6(){
        codeGenFunctions.put(Pattern.le6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLE " + finalPlace + ", #1");
                cGen.addInstruction("MOVGT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe7(){
        codeGenFunctions.put(Pattern.le7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightReal = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightReal.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVLE " + finalPlace + ", #1");
                cGen.addInstruction("MOVGT " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initLe8(){
        codeGenFunctions.put(Pattern.le8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL RealToInt");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");
                
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
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV EQ " + finalPlace + ", #1");
                cGen.addInstruction("MOV NE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq1(){
        codeGenFunctions.put(Pattern.eq1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV EQ " + finalPlace + ", #1");
                cGen.addInstruction("MOV NE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq2(){
        codeGenFunctions.put(Pattern.eq2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV EQ " + finalPlace + ", #1");
                cGen.addInstruction("MOV NE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq3(){
        codeGenFunctions.put(Pattern.eq3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
                cGen.addInstruction("MOVNE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    

    private void initEq4(){
        codeGenFunctions.put(Pattern.eq4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                BoolExp leftBool = (BoolExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                if(leftBool.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
                cGen.addInstruction("MOVNE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq5(){
        codeGenFunctions.put(Pattern.eq5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                BoolExp rightBool = (BoolExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                if(rightBool.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
                cGen.addInstruction("MOVNE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq6(){
        codeGenFunctions.put(Pattern.eq6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                BoolExp leftBool = (BoolExp)assignExp.left;
                BoolExp rightBool = (BoolExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                if(leftBool.trueFalse){
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 0);
                }

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                if(rightBool.trueFalse){
                    cGen.addVariable(tempRight, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(tempRight, VariableLength.BYTE, 0);
                }

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
                cGen.addInstruction("MOVNE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq7(){
        codeGenFunctions.put(Pattern.eq7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV EQ " + finalPlace + ", #1");
                cGen.addInstruction("MOV NE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq8(){
        codeGenFunctions.put(Pattern.eq8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);

                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOV EQ " + finalPlace + ", #1");
                cGen.addInstruction("MOV NE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq9(){
        codeGenFunctions.put(Pattern.eq9, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
                cGen.addInstruction("MOVNE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq10(){
        codeGenFunctions.put(Pattern.eq10, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL IntToReal");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");
                
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVEQ " + finalPlace + ", #1");
                cGen.addInstruction("MOVNE " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initEq11(){
        codeGenFunctions.put(Pattern.eq11, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL IntToReal");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
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
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe1(){
        codeGenFunctions.put(Pattern.ne1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, leftInt.value);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                return null;
            }
        });
    }

    private void initNe2(){
        codeGenFunctions.put(Pattern.ne2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                return null;
            }
        });
    }

    private void initNe3(){
        codeGenFunctions.put(Pattern.ne3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe4(){
        codeGenFunctions.put(Pattern.ne4, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                BoolExp leftBool = (BoolExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                if(leftBool.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.BYTE);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe5(){
        codeGenFunctions.put(Pattern.ne5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                BoolExp rightBool = (BoolExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                if(rightBool.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe6(){
        codeGenFunctions.put(Pattern.ne6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                BoolExp leftBool = (BoolExp)assignExp.left;
                BoolExp rightBool = (BoolExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                if(leftBool.trueFalse){
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 0);
                }

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                if(rightBool.trueFalse){
                    cGen.addVariable(tempRight, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(tempRight, VariableLength.BYTE, 0);
                }

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe7(){
        codeGenFunctions.put(Pattern.ne7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IdentExp rightIdent = (IdentExp)assignExp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, leftReal.realValue);

                String rightReg = rGen.getReg(rightIdent.ident, assignICode);
                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);

                cGen.addInstruction("LDR " + rightReg + ", " + rightIdent.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);

                return null;
            }
        });
    }

    private void initNe8(){
        codeGenFunctions.put(Pattern.ne8, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IdentExp leftIdent = (IdentExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String leftReg = rGen.getReg(leftIdent.ident, assignICode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + leftIdent.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                return null;
            }
        });
    }

    private void initNe9(){
        codeGenFunctions.put(Pattern.ne9, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe10(){
        codeGenFunctions.put(Pattern.ne10, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                RealExp leftReal = (RealExp)assignExp.left;
                IntExp rightInt = (IntExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, leftReal.realValue);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, VariableLength.WORD, rightInt.value);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);

                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + rightReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL IntToReal");
                cGen.addInstruction("LDR " + rightReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");

                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initNe11(){
        codeGenFunctions.put(Pattern.ne11, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                BinExp assignExp = (BinExp)assignICode.value;

                IntExp leftInt = (IntExp)assignExp.left;
                RealExp rightReal = (RealExp)assignExp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, assignICode);
                cGen.addVariable(tempLeft, VariableLength.WORD, leftInt.value);

                String tempRight = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(tempRight, assignICode);
                cGen.addVariable(tempRight, rightReal.realValue);

                String finalPlace = rGen.getReg(assignICode.place, assignICode);
                cGen.addVariable(assignICode.place, VariableLength.WORD);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("ADD R13, R13, #12");
                cGen.addInstruction("STR " + leftReg + ", [R13, #-4]");
                cGen.addInstruction("STR R14, [R13, #-12]");
                cGen.addInstruction("BL IntToReal");
                cGen.addInstruction("LDR " + leftReg + ", [R13, #-8]");
                cGen.addInstruction("LDR R14, [R13, #-12]");
                cGen.addInstruction("SUB R13, R13, #12");


                cGen.addInstruction("LDR " + rightReg + ", " + tempRight);

                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("MOVNE " + finalPlace + ", #1");
                cGen.addInstruction("MOVEQ " + finalPlace + ", #0");
                cGen.addInstruction("STR " + finalPlace + ", " + assignICode.place);
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
                
                String holderForNeg1 = iGen.genNextRegister();
                String tempReg = rGen.getTempReg(holderForNeg1, assignICode);
                cGen.addVariable(holderForNeg1, VariableLength.WORD,  -1);
                cGen.addInstruction("LDR " + tempReg + ", " + holderForNeg1);
                cGen.addInstruction("LDR " + reg + ", " + rightIdent.ident);
                cGen.addInstruction("MUL " + reg + ", " + reg + ", " + tempReg);
                cGen.addInstruction("STR " + reg + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initNeg1(){
        codeGenFunctions.put(Pattern.neg1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                UnExp assignExp = (UnExp)assignICode.value;
                
                IntExp rightInt = (IntExp)assignExp.right;

                cGen.addVariable(assignICode.place, VariableLength.WORD);


                String temp = iGen.genNextRegister();
                String rReg = rGen.getTempReg(temp, assignICode);
                cGen.addVariable(temp, VariableLength.WORD, rightInt.value);

                
                String holderForNeg1 = iGen.genNextRegister();
                String tempReg = rGen.getTempReg(holderForNeg1, assignICode);
                cGen.addVariable(holderForNeg1, VariableLength.WORD, -1);

                cGen.addInstruction("LDR " + tempReg + ", " + holderForNeg1);
                cGen.addInstruction("LDR " + rReg + ", " + temp);
                cGen.addInstruction("MUL " + rReg + ", " + rReg + ", " + tempReg);
                cGen.addInstruction("STR " + rReg + ", " + assignICode.place);
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
                
                String holderForNeg1 = iGen.genNextRegister();
                String tempReg = rGen.getTempReg(holderForNeg1, assignICode);
                cGen.addVariable(holderForNeg1, VariableLength.WORD,  -1);
                
                cGen.addInstruction("LDR " + tempReg + ", " + holderForNeg1);
                cGen.addInstruction("LDR " + reg + ", " + rightIdent.ident);
                cGen.addInstruction("MUL " + reg + ", " + reg + ", " + tempReg);
                cGen.addInstruction("STR " + reg + ", " + assignICode.place);

                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initBnot0(){
        codeGenFunctions.put(Pattern.bnot0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Assign assignICode = (Assign)icode;
                UnExp assignExp = (UnExp)assignICode.value;
                
                IdentExp rightIdent = (IdentExp)assignExp.right;

                cGen.addVariable(assignICode.place, VariableLength.WORD);

                String reg = rGen.getReg(rightIdent.ident, assignICode);

                cGen.addInstruction("LDR " + reg + ", " + rightIdent.ident);
                cGen.addInstruction("TST " + reg + ", #0");
                cGen.addInstruction("MOVEQ " + reg + ", #1");
                cGen.addInstruction("MOVNE " + reg + ", #0");
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
                cGen.addInstruction("TST " + reg + ", #0");
                cGen.addInstruction("MOVEQ " + reg + ", #1");
                cGen.addInstruction("MOVNE " + reg + ", #0");
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

                if(assignExp.trueFalse){
                    cGen.addVariable(assignICode.place, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(assignICode.place, VariableLength.BYTE, 0);
                }

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

    public void initIf1(){
        codeGenFunctions.put(Pattern.if1, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, left.value);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BLT " + ifStatement.ifTrue);
                cGen.addInstruction("BGE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf2(){
        codeGenFunctions.put(Pattern.if2, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BLT " + ifStatement.ifTrue);
                cGen.addInstruction("BGE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf3(){
        codeGenFunctions.put(Pattern.if3, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                cGen.addVariable(tempLeft, VariableLength.WORD, left.value);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
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

    public void initIf5(){
        codeGenFunctions.put(Pattern.if5, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, left.value);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BGT " + ifStatement.ifTrue);
                cGen.addInstruction("BLE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf6(){
        codeGenFunctions.put(Pattern.if6, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BGT " + ifStatement.ifTrue);
                cGen.addInstruction("BLE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf7(){
        codeGenFunctions.put(Pattern.if7, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                cGen.addVariable(tempLeft, VariableLength.WORD, left.value);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
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

    public void initIf9(){
        codeGenFunctions.put(Pattern.if9, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, left.value);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BLE " + ifStatement.ifTrue);
                cGen.addInstruction("BGT " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf10(){
        codeGenFunctions.put(Pattern.if10, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BLE " + ifStatement.ifTrue);
                cGen.addInstruction("BGT " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf11(){
        codeGenFunctions.put(Pattern.if11, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                cGen.addVariable(tempLeft, VariableLength.WORD, left.value);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
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

    public void initIf13(){
        codeGenFunctions.put(Pattern.if13, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, left.value);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BGE " + ifStatement.ifTrue);
                cGen.addInstruction("BLT " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf14(){
        codeGenFunctions.put(Pattern.if14, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("CMP " + leftReg + ", " + rightReg);
                cGen.addInstruction("BGE " + ifStatement.ifTrue);
                cGen.addInstruction("BLT " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf15(){
        codeGenFunctions.put(Pattern.if15, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                cGen.addVariable(tempLeft, VariableLength.WORD, left.value);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
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
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BEQ " + ifStatement.ifTrue);
                cGen.addInstruction("BNE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf17(){
        codeGenFunctions.put(Pattern.if17, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, left.value);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BEQ " + ifStatement.ifTrue);
                cGen.addInstruction("BNE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf18(){
        codeGenFunctions.put(Pattern.if18, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BEQ " + ifStatement.ifTrue);
                cGen.addInstruction("BNE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf19(){
        codeGenFunctions.put(Pattern.if19, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                cGen.addVariable(tempLeft, VariableLength.WORD, left.value);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BEQ " + ifStatement.ifTrue);
                cGen.addInstruction("BNE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf20(){
        codeGenFunctions.put(Pattern.if20, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                BoolExp left = (BoolExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                if(left.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BEQ " + ifStatement.ifTrue);
                cGen.addInstruction("BNE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf21(){
        codeGenFunctions.put(Pattern.if21, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                BoolExp right = (BoolExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                if(right.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE,  1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BEQ " + ifStatement.ifTrue);
                cGen.addInstruction("BNE " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf22(){
        codeGenFunctions.put(Pattern.if22, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                BoolExp left = (BoolExp)exp.left;
                BoolExp right = (BoolExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                if(left.trueFalse){
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 0);
                }

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                if(right.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE,  1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
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
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BNE " + ifStatement.ifTrue);
                cGen.addInstruction("BEQ " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf24(){
        codeGenFunctions.put(Pattern.if24, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, left.value);
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BNE " + ifStatement.ifTrue);
                cGen.addInstruction("BEQ " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf25(){
        codeGenFunctions.put(Pattern.if25, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BNE " + ifStatement.ifTrue);
                cGen.addInstruction("BEQ " + ifStatement.ifFalse);
                rGen.freeTempRegs();


                return null;
            }
        });
    }

    public void initIf26(){
        codeGenFunctions.put(Pattern.if26, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IntExp left = (IntExp)exp.left;
                IntExp right = (IntExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                cGen.addVariable(tempLeft, VariableLength.WORD, left.value);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                cGen.addVariable(temp, VariableLength.WORD, right.value);

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BNE " + ifStatement.ifTrue);
                cGen.addInstruction("BEQ " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf27(){
        codeGenFunctions.put(Pattern.if27, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                BoolExp left = (BoolExp)exp.left;
                IdentExp right = (IdentExp)exp.right;

                String temp = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(temp, icode);
                if(left.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE,  1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }
                String rightReg = rGen.getReg(right.ident, icode);

                cGen.addInstruction("LDR " + leftReg + ", " + temp);
                cGen.addInstruction("LDR " + rightReg + ", " + right.ident);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BNE " + ifStatement.ifTrue);
                cGen.addInstruction("BEQ " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    public void initIf28(){
        codeGenFunctions.put(Pattern.if28, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                IdentExp left = (IdentExp)exp.left;
                BoolExp right = (BoolExp)exp.right;

                String leftReg = rGen.getReg(left.ident, icode);

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                if(right.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                cGen.addInstruction("LDR " + leftReg + ", " + left.ident);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
                cGen.addInstruction("BNE " + ifStatement.ifTrue);
                cGen.addInstruction("BEQ " + ifStatement.ifFalse);
                rGen.freeTempRegs();

                return null;
            }
        });
    }

    private void initIf29(){
        codeGenFunctions.put(Pattern.if22, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                If ifStatement = (If)icode;
                BinExp exp = ifStatement.exp;

                BoolExp left = (BoolExp)exp.left;
                BoolExp right = (BoolExp)exp.right;

                String tempLeft = iGen.genNextRegister();
                String leftReg = rGen.getTempReg(tempLeft, icode);
                if(left.trueFalse){
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 1);
                } else {
                    cGen.addVariable(tempLeft, VariableLength.BYTE, 0);
                }

                String temp = iGen.genNextRegister();
                String rightReg = rGen.getTempReg(temp, icode);
                if(right.trueFalse){
                    cGen.addVariable(temp, VariableLength.BYTE,  1);
                } else {
                    cGen.addVariable(temp, VariableLength.BYTE, 0);
                }

                cGen.addInstruction("LDR " + leftReg + ", " + tempLeft);
                cGen.addInstruction("LDR " + rightReg + ", " + temp);
                cGen.addInstruction("TST " + leftReg + ", " + rightReg);
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
                String register = iGen.genNextRegister();
                String literalRegister = rGen.getTempReg(register, instruction);
                cGen.addInstruction("LDR " + literalRegister + ", [R13]");
                cGen.addInstruction("SUB R13, R13, #2");
                cGen.addInstruction("MOV R15, R14");
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initProc0(){
        codeGenFunctions.put(Pattern.proc0, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                Proc procICode = (Proc)icode;
                int totalLength = procICode.params.size();
                int totalReturnStackLength = totalLength;

                //Add the Return Address to the Stack
                cGen.addInstruction("ADD R13, R13, #4");
                cGen.addInstruction("STR R14, [R13, #-4]");
                //The First thing we need to do is allocate all the code we can for the Paramaters
                int offset = 4;
                for(Tuple<String, String> param: procICode.params){
                    cGen.addVariable(param.dest, VariableLength.WORD, offset);
                    offset = offset + 4;
                }
                cGen.addInstruction("ADD R13, R13, #" + (4 * totalReturnStackLength));
                for(int x = 0; x < totalLength; x++){
                    Tuple<String, String> sourceDest = procICode.params.get(x);

                    String offSetRegister = iGen.genNextRegister();
                    String offReg = rGen.getTempReg(offSetRegister, procICode);
                    cGen.addInstruction("LDR " + offReg + ", " + sourceDest.dest);

                    String reg = rGen.getReg(sourceDest.source, procICode); 
                    cGen.addInstruction("LDR " + reg + ", " + sourceDest.source);
                    
                    cGen.addInstruction("STR " + reg +  ", [R13,-" + offReg + "]");
                }
                cGen.addInstruction("BL " + procICode.pname);
                rGen.freeTempRegs();
                return null;
            }
        });
    }

    private void initParamAssign0(){
        codeGenFunctions.put(Pattern.paramAssign0, new Callable<Void>() {
           @Override
           public Void call() throws Exception {
                ICode icode = intermediateCode.get(i);
                ParamAssign paramAssign = (ParamAssign)icode;

                cGen.addVariable(paramAssign.newPlace, VariableLength.WORD);
                
                String offSetPlace = rGen.getReg(paramAssign.paramPlace, icode);
                cGen.addInstruction("LDR " + offSetPlace + ", " + paramAssign.paramPlace);
                cGen.addInstruction("LDR " + offSetPlace + ", [R13, -"+ offSetPlace +"]");
                cGen.addInstruction("STR " + offSetPlace + ", " + paramAssign.newPlace);

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
                List<String> params = inline.param;
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
                                String addressParam = params.get(paramIndex);
                                resultInstruction.append(addressParam);
                                paramIndex++;
                            } else {
                                errorLog.add("No paramater to substite %a found at " + paramIndex, new Position(i, index));
                            }
                        } else if(formatSpecifierLetter == 'R' || formatSpecifierLetter == 'r'){
                            if(paramIndex < params.size()){
                                String addresParam = params.get(paramIndex);
                                String regParam = rGen.getReg(addresParam, icode);
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
