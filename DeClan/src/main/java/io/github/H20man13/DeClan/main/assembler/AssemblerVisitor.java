package io.github.H20man13.DeClan.main.assembler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.AdcInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.AddInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.AddressContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.AndExprContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.AndInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.BInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.BicInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.BitwiseContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.BlInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.BxInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.ByteDirectiveContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.CmnInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.CmpInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.EorInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.ExpressionContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.IdentifierContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.InstructionContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.InstructionOrDirectiveContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.LdmInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.LdrDefInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.LdrSignedInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.MlaInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.MovInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.MrsInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.MsrDefInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.MsrPrivInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.MulInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.MvnInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.NumberContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.Op2Context;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.OrrInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.PostIndexedAddressingContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.PoundExpressionContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.PreIndexedAddressingContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.PrimaryContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.ProgramContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.PsrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.PsrfContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.RListContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.RValueContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.RealNumberContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.RelationalContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.RsbInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.RscInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.SbcInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.ShiftContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.ShiftNameContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.SingleContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.StmInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.StopInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.StrDefInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.StrSignedInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.SubInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.SwiInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.SwpInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.TeqInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.TermContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.TstInstrContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.UnaryContext;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.WordDirectiveContext;
import io.github.H20man13.DeClan.common.matcher.AntlrToken;
import io.github.H20man13.DeClan.common.matcher.AntlrTokenFactory;
import io.github.H20man13.DeClan.common.matcher.AntlrTokenMatcher;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerVisitor;

public class AssemblerVisitor implements ArmAssemblerVisitor<Integer> {
    private AntlrTokenMatcher<Integer> Matcher;
    private AntlrTokenFactory<Integer> Factory;
    private LinkedList<Integer> codeList;
    private Map<String, Integer> labelAdresses;
    private Map<InstructionOrDirectiveContext, Integer> instructionOrDirectiveAdresses;
    private InstructionOrDirectiveContext currentInstructionOrDirective;

    public AssemblerVisitor(){
        this.Matcher = new AntlrTokenMatcher<Integer>();
        this.Factory = new AntlrTokenFactory<Integer>();
        this.codeList = new LinkedList<Integer>();
        this.labelAdresses = null;
        this.instructionOrDirectiveAdresses = null;
        this.currentInstructionOrDirective = null;
    }

    public List<Integer> assembleCode(ProgramContext ctx){
        this.codeList = new LinkedList<Integer>();
        AddressCalculatorVisitor addressCalculator = new AddressCalculatorVisitor();
        this.instructionOrDirectiveAdresses = addressCalculator.caclulateAdresses(ctx);
        this.labelAdresses = generateLabelAdresses(this.instructionOrDirectiveAdresses);
        ctx.accept(this);
        return codeList;
    }

    private Map<String, Integer> generateLabelAdresses(Map<InstructionOrDirectiveContext, Integer> map){
        HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
        for(InstructionOrDirectiveContext ctx : map.keySet()){
            AntlrToken<Integer> LABEL = Factory.decorateToken(ctx.LABEL());
            if(Matcher.match(LABEL)){
                Integer address = map.get(ctx);
                String labelText = LABEL.getText();
                String labelIdent = labelText.substring(0, labelText.length() - 1);
                labelMap.put(labelIdent, address);
            }
        }
        return labelMap;
    }

    private static String getConditionSubString(String text, int defLength){
        if(text.length() < defLength + 2){
            return null;
        } else {
            String subString = text.substring(defLength, defLength + 2).toUpperCase();
            if(subString.equals("EQ")
            || subString.equals("NE")
            || subString.equals("CS")
            || subString.equals("CC")
            || subString.equals("MI")
            || subString.equals("PL")
            || subString.equals("VS")
            || subString.equals("VC")
            || subString.equals("HI")
            || subString.equals("LS")
            || subString.equals("GE")
            || subString.equals("LT")
            || subString.equals("GT")
            || subString.equals("LE")
            || subString.equals("AL")){
                return subString;
            } else {
                return null;
            }
        }
    }

    private static Integer getConditionCode(String text, int defLength){
        String conditionCode = getConditionSubString(text, defLength);

        if(conditionCode == null){
            return 14 << 28;
        } else {
            conditionCode = conditionCode.toUpperCase();
            if(conditionCode.equals("EQ")){
                return 0 << 28;
            } else if(conditionCode.equals("NE")){
                return 1 << 28;
            } else if(conditionCode.equals("CS")){
                return 2 << 28; 
            } else if(conditionCode.equals("CC")){
                return 3 << 28;
            } else if(conditionCode.equals("MI")){
                return 4 << 28;
            } else if(conditionCode.equals("PL")){
                return 5 << 28;
            } else if(conditionCode.equals("VS")){
                return 6 << 28;
            } else if(conditionCode.equals("VC")){
                return 7 << 28;
            } else if(conditionCode.equals("HI")){
                return 8 << 28;
            } else if(conditionCode.equals("LS")){
                return 9 << 28;
            } else if(conditionCode.equals("GE")){
                return 10 << 28;
            } else if(conditionCode.equals("LT")){
                return 11 << 28;
            } else if(conditionCode.equals("GT")){
                return 12 << 28;
            } else if(conditionCode.equals("LE")){
                return 13 << 28;
            } else if(conditionCode.equals("AL")){
                return 14 << 28;
            } else {
                return 14 << 28;
            }
        }
    }

    private static String getTransferTypeSubString(String text, int defLength){
        String condCode = getConditionSubString(text, defLength);
        if(condCode == null){
            if(text.length() > defLength + 1){
                String transferSubString = text.substring(defLength, defLength + 2);
                if(transferSubString.toLowerCase().equals("sh")
                || transferSubString.toLowerCase().equals("sb")){
                    return transferSubString;
                } else if(transferSubString.charAt(0) == 'H' || transferSubString.charAt(0) == 'h'){
                    return "" + transferSubString.charAt(0);
                } else {
                    return null;
                }
            } else if(text.length() > defLength){
                char charAt = text.charAt(defLength);
                if(charAt == 'H' || charAt == 'h'){
                    return "" + charAt;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            if(text.length() > defLength + 3){
                String transferSubString = text.substring(defLength + 2, defLength + 4);
                if(transferSubString.toLowerCase().equals("sh")
                || transferSubString.toLowerCase().equals("sb")){
                    return transferSubString;
                } else if(transferSubString.charAt(0) == 'H' || transferSubString.charAt(0) == 'h'){
                    return "" + transferSubString.charAt(0);
                } else {
                    return null;
                }
            } else if(text.length() > defLength + 2){
                char charAt = text.charAt(defLength + 2);
                if(charAt == 'H' || charAt == 'h'){
                    return "" + charAt;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private static Integer getTransferTypeCode(String text, int defLength){
        String transferSubString = getTransferTypeSubString(text, defLength).toUpperCase();
        if(transferSubString.equals("H")){
            return (0b01 << 5);
        } else if(transferSubString.equals("SH")){
            return (0b11 << 5);
        } else if(transferSubString.equals("SB")){
            return (0b10 << 5); 
        } else {
            return 0;
        }
    }

    private String getAdressingModeSubString(String text, int defLength){
        String condCodeSubString = getConditionSubString(text, defLength);
        if(condCodeSubString == null){
            if(text.length() > defLength + 1){
                String subString = text.substring(defLength, defLength + 2).toUpperCase();
                if(subString.equals("FD")
                || subString.equals("ED")
                || subString.equals("FA")
                || subString.equals("EA")
                || subString.equals("IA")
                || subString.equals("IB")
                || subString.equals("DA")
                || subString.equals("DB")){
                    return subString;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            if(text.length() > defLength + 3){
                String subString = text.substring(defLength + 2, defLength + 4).toUpperCase();
                if(subString.equals("FD")
                || subString.equals("ED")
                || subString.equals("FA")
                || subString.equals("EA")
                || subString.equals("IA")
                || subString.equals("IB")
                || subString.equals("DA")
                || subString.equals("DB")){
                    return subString;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private Integer getAdressingModeCode(String text, int defLength){
        String subString = getAdressingModeSubString(text, defLength).toUpperCase();

        Integer POver = 24;
        Integer UOver = 23;
        if(subString.equals("FD")){
            return (0 << POver) | (1 << UOver);
        } else if(subString.equals("ED")){
            return (1 << POver) | (1 << UOver);
        } else if(subString.equals("FA")){
            return (0 << POver) | (0 << UOver);
        } else if(subString.equals("EA")){
            return (1 << POver) | (0 << UOver);
        } else if(subString.equals("IA")){
            return (0 << POver) | (1 << UOver);
        } else if(subString.equals("IB")){
            return (1 << POver) | (1 << UOver);
        } else if(subString.equals("DA")){
            return (0 << POver) | (0 << UOver);
        } else if(subString.equals("DB")){
            return (1 << POver) | (0 << UOver);
        } else {
            return 0;
        }
    }

    private boolean containsB(String text, int defLength){
        String conditionCode = getConditionSubString(text, defLength);
        if(conditionCode == null && text.length() > defLength){
            char possibleB = text.charAt(defLength);
            return possibleB == 'B' || possibleB == 'b';
        } else if(conditionCode == null) {
            return false;
        } else if(text.length() > defLength + 2) {
            char possibleB = text.charAt(defLength + 2);
            return possibleB == 'B' || possibleB == 'b';
        } else {
            return false;
        }
    }

    private boolean containsT(String text, int defLength){
        String conditionCode = getConditionSubString(text, defLength);
        if(conditionCode == null && text.length() > defLength){
            char possibleT = text.charAt(defLength);
            if((possibleT == 'B' || possibleT == 'b') && text.length() > defLength + 1){
                possibleT = text.charAt(defLength + 1);
            }
            return possibleT == 'T' || possibleT == 't';
        } else if(conditionCode == null) {
            return false;
        } else if(text.length() > defLength + 2) {
            char possibleT = text.charAt(defLength + 2);
            if((possibleT == 'B' || possibleT == 'b') && text.length() > defLength + 3){
                possibleT = text.charAt(defLength + 3);
            }
            return possibleT == 'T' || possibleT == 't';
        } else {
            return false;
        }
    }

    private boolean containsS(String text, int defLength){
        String conditionCode = getConditionSubString(text, defLength);
        if(conditionCode == null && text.length() > defLength){
            char possibleS = text.charAt(defLength);
            return possibleS == 'S' || possibleS == 's';
        } else if(conditionCode == null) {
            return false;
        } else if(text.length() > defLength + 2) {
            char possibleS = text.charAt(defLength + 2);
            return possibleS == 'S' || possibleS == 's';
        } else {
            return false;
        }
    } 

    @Override
    public Integer visit(ParseTree tree) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Integer visitChildren(RuleNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitChildren'");
    }

    @Override
    public Integer visitTerminal(TerminalNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTerminal'");
    }

    @Override
    public Integer visitErrorNode(ErrorNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitErrorNode'");
    }

    @Override
    public Integer visitMrsInstr(MrsInstrContext ctx) {
       AntlrToken<Integer> MRS_INSTR = Factory.decorateToken(ctx.MRS_INSTR());
       AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
       AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());
       AntlrToken<Integer> psr = Factory.decorateToken(ctx.psr());

       Integer result = 0;
       result = getConditionCode(MRS_INSTR.getText(), 3);

        //Set the necessary bits for the mrs instruction
        result |= (0b00010 << 23);
        result |= (0b001111 << 16);

        result |= psr.accept(this);

        Integer rd = generateNumberFromReg(ctx.REG().getText());
        result |= rd << 12;

        return result;
    }

    @Override
    public Integer visitSwpInstr(SwpInstrContext ctx) {
        AntlrToken<Integer> SWAP = Factory.decorateToken(ctx.SWAP());
        
        Integer result = getConditionCode(SWAP.getText(), 3);
        boolean swapByte = containsB(SWAP.getText(), 3);

        //Set the Instruction bit to hi
        result |= 0b1001 << 4;
        result |= 0b00010 << 23;

        if(swapByte){
            //Set the Byte bit to one
            result |= 1 << 22;
        } else {
            result |= 0 << 22;
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rm = generateNumberFromReg(ctx.REG(1).getText());
        result |= rm;

        Integer rn = generateNumberFromReg(ctx.REG(2).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitAdcInstr(AdcInstrContext ctx) {
        AntlrToken<Integer> ADDITION_WITH_CARRY = Factory.decorateToken(ctx.ADDITION_WITH_CARRY());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(ADDITION_WITH_CARRY.getText(), 3);
        boolean containsS = containsS(ADDITION_WITH_CARRY.getText(), 3);

        //Set Opcode Bits
        result |= (0b0101 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(containsS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitTeqInstr(TeqInstrContext ctx) {
        AntlrToken<Integer> TEST_EQUALITY = Factory.decorateToken(ctx.TEST_EQUALITY());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(TEST_EQUALITY.getText(), 3);

        //Set Opcode Bits
        result |= (0b1001 << 21);

        //set operand 2
        result |= op2.accept(this);

        Integer rn = generateNumberFromReg(ctx.REG().getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitShift(ShiftContext ctx) {
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> shiftName = Factory.decorateToken(ctx.shiftName());
        AntlrToken<Integer> poundExpression = Factory.decorateToken(ctx.poundExpression());
        AntlrToken<Integer> RPX = Factory.decorateToken(ctx.RPX());

        Integer result = 0;

        if(Matcher.match(shiftName, REG)){
            Integer shiftType = shiftName.accept(this);
            result |= (1 << 4);
            result |= (shiftType << 5);
            Integer regNumber = generateNumberFromReg(REG.getText());
            result |= (regNumber << 8);
        } else if(Matcher.match(shiftName, poundExpression)){
            Integer shiftType = shiftName.accept(this);
            result |= (0 << 4);
            result |= (shiftType << 5);
            Integer exprResult = poundExpression.accept(this);
            result |= (exprResult << 7);
        } else if(Matcher.match(RPX)){

        }

        return result;
    }

    @Override
    public Integer visitPoundExpression(PoundExpressionContext ctx) {
        // TODO Auto-generated method stub
        return ctx.expression().accept(this);
    }

    @Override
    public Integer visitTstInstr(TstInstrContext ctx) {
        AntlrToken<Integer> TEST_BITS = Factory.decorateToken(ctx.TEST_BITS());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());

        Integer result = getConditionCode(TEST_BITS.getText(), 3);

        //Set Opcode Bits
        result |= (0b1000 << 21);

        //set operand 2
        result |= op2.accept(this);

        Integer rn = generateNumberFromReg(REG.getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitStrSignedInstr(StrSignedInstrContext ctx) {
        AntlrToken<Integer> STORE_BYTE = Factory.decorateToken(ctx.STORE_SIGNED_REGISTER());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());
        
        AntlrToken<Integer> address = Factory.decorateToken(ctx.address());
        
        Integer result = getConditionCode(STORE_BYTE.getText(), 3);

        boolean containsB = containsB(STORE_BYTE.getText(), 3);
        boolean containsT = containsT(STORE_BYTE.getText(), 3);

        //Set the Byte bit to one if this is present
        if(containsB){
            result |= (1 << 22);
        }

        //Set the Write Bit
        if(containsT){
            result |= (1 << 21); 
        }

        //Set the load bit to 0
        result |= (0 << 20);

        Integer regNumber = generateNumberFromReg(REG.getText());
        result |= (regNumber << 12);

        if(Matcher.match(address)){
            result |= address.accept(this);
        }

        return result;
    }

    @Override
    public Integer visitAndInstr(AndInstrContext ctx) {
        AntlrToken<Integer> LOGICAL_AND = Factory.decorateToken(ctx.LOGICAL_AND());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(LOGICAL_AND.getText(), 3);

        boolean containsS = containsS(LOGICAL_AND.getText(), 3);

        //Set Opcode Bits
        result |= (0b0000 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(containsS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitUnary(UnaryContext ctx) {
        AntlrToken<Integer> PLUS = Factory.decorateToken(ctx.PLUS());
        AntlrToken<Integer> MINUS = Factory.decorateToken(ctx.MINUS());
        AntlrToken<Integer> single = Factory.decorateToken(ctx.single());

        if(Matcher.match(PLUS, single)){
            return single.accept(this);
        } else if(Matcher.match(MINUS, single)){
            return -single.accept(this);
        } else if(Matcher.match(single)){
            return single.accept(this);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitRsbInstr(RsbInstrContext ctx) {
        AntlrToken<Integer> REVERSE_SUBTRACTION = Factory.decorateToken(ctx.REVERSE_SUBTRACTION());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(REVERSE_SUBTRACTION.getText(), 3);
        boolean containsS = containsS(REVERSE_SUBTRACTION.getText(), 3);

        //Set Opcode Bits
        result |= (0b0011 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(containsS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitLdrDefInstr(LdrDefInstrContext ctx) {
        AntlrToken<Integer> LOAD_REGISTER = Factory.decorateToken(ctx.LOAD_REGISTER());
        AntlrToken<Integer> address = Factory.decorateToken(ctx.address());
        
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());

        boolean containsB = containsB(LOAD_REGISTER.getText(), 3);

        Integer result = getConditionCode(LOAD_REGISTER.getText(), 3);

        result |= (0b01 << 26);
        result |= 1 << 20;

        if(containsB){
            result |= 1 << 22;
        }

        Integer offsetRaw = address.accept(this);
        result |= offsetRaw;

        Integer regNum = generateNumberFromReg(REG.getText());
        result |= regNum << 12;

        return result;
    }

    @Override
    public Integer visitMsrDefInstr(MsrDefInstrContext ctx) {
        AntlrToken<Integer> MSR_INSTR = Factory.decorateToken(ctx.MSR_INSTR());
        AntlrToken<Integer> REG = Factory.decorateToken (ctx.REG());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());

        AntlrToken<Integer> psr = Factory.decorateToken(ctx.psr());

        Integer result = getConditionCode(MSR_INSTR.getText(), 3);

        //First Set the Instruction Bits to Hi
        result |= 0b1010011111 << 12;
        result |= 0b00010 << 23;

        //Set the Rm Register to a value
        Integer rm = generateNumberFromReg(REG.getText());
        result |= rm;

        result |= psr.accept(this);

        return result;
    }


    @Override
    public Integer visitRscInstr(RscInstrContext ctx) {
        AntlrToken<Integer> REVERSE_SUBTRACTION_WITH_CARRY = Factory.decorateToken(ctx.REVERSE_SUBTRACTION_WITH_CARRY());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(REVERSE_SUBTRACTION_WITH_CARRY.getText(), 3);

        boolean conS = containsS(REVERSE_SUBTRACTION_WITH_CARRY.getText(), 3);

        //Set Opcode Bits
        result |= (0b0111 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(conS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitLdmInstr(LdmInstrContext ctx) {
        AntlrToken<Integer> LOAD_MEMORY = Factory.decorateToken(ctx.LOAD_MEMORY());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> EXP = Factory.decorateToken(ctx.EXP());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());
        AntlrToken<Integer> BXOR = Factory.decorateToken(ctx.BXOR());

        AntlrToken<Integer> rList = Factory.decorateToken(ctx.rList());
        
        Integer result = getConditionCode(LOAD_MEMORY.getText(), 3);

        //Set the Load Store Bit to 1
        result |= 1<<20;

        //Set the P and U Bits with the addressing Mode
        result |= getAdressingModeCode(LOAD_MEMORY.getText(), 3);

        if(Matcher.match(EXP)){
            result |= (1 << 21);
        }
        
        if(Matcher.match(BXOR)){
            result |= (1 << 22);
        }

        Integer regNumber = generateNumberFromReg(REG.getText());
        result |= regNumber << 16;

        result |= rList.accept(this);

        return result;
    }

    @Override
    public Integer visitStrDefInstr(StrDefInstrContext ctx) {
        AntlrToken<Integer> STORE_REGISTER = Factory.decorateToken(ctx.STORE_REGISTER());
        AntlrToken<Integer> address = Factory.decorateToken(ctx.address());
        
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());

        Integer result = getConditionCode(STORE_REGISTER.getText(), 3);
        boolean containsB = containsB(STORE_REGISTER.getText(), 3);

        result |= (0b01 << 26);

        if(containsB){
            result |= 1 << 22;
        }

        Integer offsetRaw = address.accept(this);
        result |= offsetRaw;

        Integer regNum = generateNumberFromReg(REG.getText());
        result |= regNum << 12;

        return result;
    }

    @Override
    public Integer visitNumber(NumberContext ctx) {
        return Integer.parseInt(ctx.NUMBER().getText());
    }

    @Override
    public Integer visitStmInstr(StmInstrContext ctx) {
        AntlrToken<Integer> STORE_MEMORY = Factory.decorateToken(ctx.STORE_MEMORY());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> EXP = Factory.decorateToken(ctx.EXP());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());
        AntlrToken<Integer> BXOR = Factory.decorateToken(ctx.BXOR());
        AntlrToken<Integer> rList = Factory.decorateToken(ctx.rList());
        
        Integer result = getConditionCode(STORE_MEMORY.getText(), 3);

        //Set the Load Store Bit to 0
        result |= 0<<20;

        //Set the P and U Bits with the addressing Mode
        result |= getAdressingModeCode(STORE_MEMORY.getText(), 3);

        if(Matcher.match(EXP)){
            result |= (1 << 21);
        }
        
        if(Matcher.match(BXOR)){
            result |= (1 << 22);
        }

        Integer regNumber = generateNumberFromReg(ctx.REG().getText());
        result |= regNumber << 16;

        result |= rList.accept(this);

        return result;
    }

    @Override
    public Integer visitOrrInstr(OrrInstrContext ctx) {
        AntlrToken<Integer> LOGICAL_OR_INSTRUCTION = Factory.decorateToken(ctx.LOGICAL_OR_INSTRUCTION());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(LOGICAL_OR_INSTRUCTION.getText(), 3);

        boolean contS = containsS(LOGICAL_OR_INSTRUCTION.getText(), 3);

        //Set Opcode Bits
        result |= (0b1100 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitMlaInstr(MlaInstrContext ctx) {
        AntlrToken<Integer> MULTIPLY_AND_ACUMULATE = Factory.decorateToken(ctx.MULTIPLY_AND_ACUMULATE());

        Integer result = getConditionCode(MULTIPLY_AND_ACUMULATE.getText(), 3);

        boolean condS = containsS(MULTIPLY_AND_ACUMULATE.getText(), 3);

        //Set Instruction Bits to hi
        result |= 0b1001 << 4;

        //Set the Acumulate Bit to hi
        result |= (1 << 21);

        //Set the S bit to hi
        if(condS){
            result |= 1 << 20;
        }

        //Set RD to some value
        Integer regD = generateNumberFromReg(ctx.REG(0).getText());
        result |= regD << 16;

        Integer regM = generateNumberFromReg(ctx.REG(1).getText());
        result |= regM << 0;

        Integer regS = generateNumberFromReg(ctx.REG(2).getText());
        result |= regS << 8;

        Integer regN = generateNumberFromReg(ctx.REG(3).getText());
        result |= regN << 12;

        return result;
    }

    @Override
    public Integer visitMulInstr(MulInstrContext ctx) {
        AntlrToken<Integer> MULTIPLY = Factory.decorateToken(ctx.MULTIPLY());

        Integer result = getConditionCode(MULTIPLY.getText(), 3);

        boolean contS = containsS(MULTIPLY.getText(), 3);

        //Set Instruction Bits to hi
        result |= 0b1001 << 4;

        //Set the Acumulate Bit to low
        result |= (0 << 21);

        //Set the S bit to hi
        if(contS){
            result |= 1 << 20;
        }

        //Set RD to some value
        Integer regD = generateNumberFromReg(ctx.REG(0).getText());
        result |= regD << 16;

        Integer regM = generateNumberFromReg(ctx.REG(1).getText());
        result |= regM << 0;

        Integer regS = generateNumberFromReg(ctx.REG(2).getText());
        result |= regS << 8;

        return result;
    }

    @Override
    public Integer visitAddInstr(AddInstrContext ctx) {
        AntlrToken<Integer> ADDITION = Factory.decorateToken(ctx.ADDITION());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(ADDITION.getText(), 3);

        boolean contS = containsS(ADDITION.getText(), 3);

        //Set Opcode Bits
        result |= (0b0100 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitBxInstr(BxInstrContext ctx) {
        AntlrToken<Integer> BRANCH_WITH_EXCHANGE = Factory.decorateToken(ctx.BRANCH_WITH_EXCHANGE());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());

        Integer result = getConditionCode(BRANCH_WITH_EXCHANGE.getText(), 2);

        result |= 0b000100101111111111110001 << 4;
        result |= generateNumberFromReg(REG.getText());

        return result;
    }

    @Override
    public Integer visitBitwise(BitwiseContext ctx) {
        AntlrToken<Integer> term = Factory.decorateToken(ctx.term());
        AntlrToken<Integer> bitwise = Factory.decorateToken(ctx.bitwise());

        AntlrToken<Integer> BOR = Factory.decorateToken(ctx.BOR());
        AntlrToken<Integer> BAND = Factory.decorateToken(ctx.BAND());
        AntlrToken<Integer> BXOR = Factory.decorateToken(ctx.BXOR());

        if(Matcher.match(term, BOR, bitwise)){
            Integer termResult = term.accept(this);
            Integer bitwiseResult = bitwise.accept(this);
            return termResult | bitwiseResult; 
        } else if(Matcher.match(term, BAND, bitwise)){
            Integer termResult = term.accept(this);
            Integer bitwiseResult = term.accept(this);
            return termResult & bitwiseResult;
        } else if(Matcher.match(term, BXOR, bitwise)){
            Integer termResult = term.accept(this);
            Integer bitwiseResult = bitwise.accept(this);
            return termResult ^ bitwiseResult;
        } else if(Matcher.match(term)){
            return term.accept(this);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitBInstr(BInstrContext ctx) {
        AntlrToken<Integer> BRANCH = Factory.decorateToken(ctx.BRANCH());
        AntlrToken<Integer> expression = Factory.decorateToken(ctx.expression());

        Integer result = getConditionCode(BRANCH.getText(), 1);

        Integer expressionResult = expression.accept(this);
        return result | (expressionResult & 0xffffff) | (0b101 << 25);
    }

    @Override
    public Integer visitLdrSignedInstr(LdrSignedInstrContext ctx) {
        AntlrToken<Integer> LOAD_BYTE = Factory.decorateToken(ctx.LOAD_SIGNED_REGISTER());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());
        
        AntlrToken<Integer> address = Factory.decorateToken(ctx.address());
        
        Integer result = getConditionCode(LOAD_BYTE.getText(), 3);

        boolean contB = containsB(LOAD_BYTE.getText(), 3);
        boolean contT = containsT(LOAD_BYTE.getText(), 3);

        //Set the Byte bit to one if this is present
        if(contB){
            result |= (1 << 22);
        }

        //Set the Write Bit
        if(contT){
            result |= (1 << 21); 
        }

        //Set the load bit to 1
        result |= (1 << 20);

        Integer regNumber = generateNumberFromReg(REG.getText());
        result |= (regNumber << 12);

        if(Matcher.match(address)){
            result |= address.accept(this);
        }

        return result;
    }

    @Override
    public Integer visitBicInstr(BicInstrContext ctx) {
        AntlrToken<Integer> BIT_CLEAR_INSTRUCTION = Factory.decorateToken(ctx.BIT_CLEAR_INSTRUCTION());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(BIT_CLEAR_INSTRUCTION.getText(), 3);

        boolean contS = containsS(BIT_CLEAR_INSTRUCTION.getText(), 3);

        //Set Opcode Bits
        result |= (0b1110 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitRList(RListContext ctx) {
       AntlrToken<Integer> LCURL = Factory.decorateToken(ctx.LCURL());
       AntlrToken<Integer> RCURL = Factory.decorateToken(ctx.RCURL());

       Integer result = 0;
       for(RValueContext Context : ctx.rValue()){
            result |= Context.accept(this);
       }

       return result;
    }

    @Override
    public Integer visitTerm(TermContext ctx) {
        AntlrToken<Integer> unary = Factory.decorateToken(ctx.unary());
        AntlrToken<Integer> term = Factory.decorateToken(ctx.term());

        AntlrToken<Integer> TIMES = Factory.decorateToken(ctx.TIMES());
        AntlrToken<Integer> DIV = Factory.decorateToken(ctx.DIV());
        AntlrToken<Integer> MOD = Factory.decorateToken(ctx.MOD());
        AntlrToken<Integer> LSHIFT = Factory.decorateToken(ctx.LSHIFT());
        AntlrToken<Integer> RSHIFT = Factory.decorateToken(ctx.RSHIFT());

        if(Matcher.match(unary, TIMES, term)){
            Integer unaryResult = unary.accept(this);
            Integer termResult = term.accept(this);
            return unaryResult * termResult;
        } else if(Matcher.match(unary, DIV, term)){
            Integer unaryResult = unary.accept(this);
            Integer termResult = term.accept(this);
            return unaryResult / termResult;
        } else if(Matcher.match(unary, MOD, term)){
            Integer unaryResult = unary.accept(this);
            Integer termResult = term.accept(this);
            return unaryResult % termResult;
        } else if(Matcher.match(unary, LSHIFT, term)){
            Integer unaryResult = unary.accept(this);
            Integer termResult = term.accept(this);
            return unaryResult << termResult;
        } else if(Matcher.match(unary, RSHIFT, term)){
            Integer unaryResult = unary.accept(this);
            Integer termResult = term.accept(this);
            return unaryResult >> termResult;
        } else if(Matcher.match(unary)){
            return unary.accept(this);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitSubInstr(SubInstrContext ctx) {
        AntlrToken<Integer> SUBTRACTION = Factory.decorateToken(ctx.SUBTRACTION());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(SUBTRACTION.getText(), 3);
        boolean contS = containsS(SUBTRACTION.getText(), 3);

        //Set Opcode Bits
        result |= (0b0010 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitExpression(ExpressionContext ctx) {
        // TODO Auto-generated method stub
        AntlrToken<Integer> andExpr = Factory.decorateToken(ctx.andExpr());
        AntlrToken<Integer> expression = Factory.decorateToken(ctx.expression());
        AntlrToken<Integer> LOR = Factory.decorateToken(ctx.LOR());

        if(Matcher.match(andExpr, LOR, expression)){
            Integer andExprResult = andExpr.accept(this);
            Integer expressionResult = expression.accept(this);

            return (andExprResult != 0 || expressionResult != 0) ? 1 : 0;
        } else if(Matcher.match(andExpr)){
            return andExpr.accept(this);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitAddress(AddressContext ctx) {
        AntlrToken<Integer> expression = Factory.decorateToken(ctx.expression());
        AntlrToken<Integer> preIndexedAddressing = Factory.decorateToken(ctx.preIndexedAddressing());
        AntlrToken<Integer> postIndexedAddressing = Factory.decorateToken(ctx.postIndexedAddressing());

        Integer result = 0;
        if(Matcher.match(expression)) {
            Integer targetAdress = expression.accept(this);
            Integer currentAddress = instructionOrDirectiveAdresses.get(this.currentInstructionOrDirective);
            Integer offset = targetAdress - (currentAddress + 4);

            if(offset >= 0){
                result |= (1 << 23);
            } else {
                result |= (0 << 23);
            }

            offset = Math.abs(offset);
            result |= ((offset & 0xfff) << 0);

            result |= (0 << 25);

            result |= (1 << 24);

            result |= (15 << 16);

            return result;
        }
        else if(Matcher.match(preIndexedAddressing)) return preIndexedAddressing.accept(this);
        else if(Matcher.match(postIndexedAddressing)) return postIndexedAddressing.accept(this);
        else {
            return 0;
        }
    }

    @Override
    public Integer visitBlInstr(BlInstrContext ctx) {
        AntlrToken<Integer> BRANCH_WITH_LINK = Factory.decorateToken(ctx.BRANCH_WITH_LINK());
        AntlrToken<Integer> expression = Factory.decorateToken(ctx.expression());

        Integer returnValue = getConditionCode(BRANCH_WITH_LINK.getText(), 2);

        Integer expressionResult = expression.accept(this);
        return returnValue | (0b1011 << 24) | expressionResult;
    }

    @Override
    public Integer visitSbcInstr(SbcInstrContext ctx) {
        AntlrToken<Integer> SUBTRACTION_WITH_CARRY = Factory.decorateToken(ctx.SUBTRACTION_WITH_CARRY());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(SUBTRACTION_WITH_CARRY.getText(), 3);

        boolean contS = containsS(SUBTRACTION_WITH_CARRY.getText(), 3);

        //Set Opcode Bits
        result |= (0b0110 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitMvnInstr(MvnInstrContext ctx) {
        AntlrToken<Integer> MOVE_NEGATIVE = Factory.decorateToken(ctx.MOVE_NEGATIVE());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(MOVE_NEGATIVE.getText(), 3);
        boolean contS = containsS(MOVE_NEGATIVE.getText(), 3);

        //Set Opcode Bits
        result |= (0b1101 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG().getText());
        result |= rd << 12;

        return result;
    }

    private Integer generateNumberFromReg(String regText){
        StringBuilder numberBuilder = new StringBuilder();
        for(char c : regText.toCharArray()){
            if(Character.isDigit(c)){
                numberBuilder.append(c);
            }
        }
        return Integer.parseInt(numberBuilder.toString());
    }

    @Override
    public Integer visitRValue(RValueContext ctx) {
        AntlrToken<Integer> MINUS = Factory.decorateToken(ctx.MINUS());

        Integer result = 0;
        if(Matcher.match(MINUS)){
            //Then we need to compute the range and set the bits inside the list
            String beginRegText = ctx.REG(0).getText();
            String endRegText = ctx.REG(1).getText();

            Integer beginRegInteger = generateNumberFromReg(beginRegText);
            Integer endRegInteger = generateNumberFromReg(endRegText);

            for(Integer i = beginRegInteger; i <= endRegInteger; i++){
                result |= 1 << i;
            }
        } else {
            //Otherwise we just need to flip one bit
            String rawRegText = ctx.REG(0).getText();
            Integer numberFromReg = generateNumberFromReg(rawRegText);
            result |= (1 << numberFromReg);
        }

        return result;
    }

    @Override
    public Integer visitMsrPrivInstr(MsrPrivInstrContext ctx) {
        AntlrToken<Integer> MSR = Factory.decorateToken(ctx.MSR_INSTR());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());
        AntlrToken<Integer> psrf = Factory.decorateToken(ctx.psrf());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> poundExpression = Factory.decorateToken(ctx.poundExpression());

        Integer result = getConditionCode(MSR.getText(), 3);

        //Set the specified Instruction Bits to Hi
        result |= 0b10 << 23;
        result |= 0b1010001111 << 12;

        if(Matcher.match(REG)){
            result |= (0 << 15);
            Integer rm = generateNumberFromReg(REG.getText());
            result |= rm;
        } else {
            result |= (1 << 15);
            result |= poundExpression.accept(this);
        }

        //Set the Psrf flag bit to true
        result |= psrf.accept(this);

        return result;
    }

    @Override
    public Integer visitEorInstr(EorInstrContext ctx) {
        AntlrToken<Integer> EXCLUSIVE_OR = Factory.decorateToken(ctx.EXCLUSIVE_OR());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(EXCLUSIVE_OR.getText(), 3);

        boolean contS = containsS(EXCLUSIVE_OR.getText(), 3);
        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(ctx.REG(0).getText());
        result |= rd << 12;

        Integer rn = generateNumberFromReg(ctx.REG(1).getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitMovInstr(MovInstrContext ctx) {
        AntlrToken<Integer> MOVE = Factory.decorateToken(ctx.MOVE());
        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());

        Integer result = getConditionCode(MOVE.getText(), 3);

        boolean contS = containsS(MOVE.getText(), 3);
        //Set Opcode Bits
        result |= (0b1101 << 21);

        //set operand 2
        result |= op2.accept(this);

        //If S is present then Set the condition codes bit to 1
        if(contS){
            result |= (1 << 20);
        }

        Integer rd = generateNumberFromReg(REG.getText());
        result |= rd << 12;

        return result;
    }

    @Override
    public Integer visitOp2(Op2Context ctx) {
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> COMMA = Factory.decorateToken(ctx.COMMA());
        AntlrToken<Integer> shift = Factory.decorateToken(ctx.shift());
        AntlrToken<Integer> poundExpression = Factory.decorateToken(ctx.poundExpression());

        Integer result = 0;
        if(Matcher.match(REG, COMMA, shift)){
            //Set the Immediate Bit to zero
            result |= (0 << 25);

            Integer rm = generateNumberFromReg(REG.getText());
            result |= rm;

            result |= shift.accept(this);

            return result;

        } else if(Matcher.match(REG)){
            result |= (0 << 25);

            Integer rm = generateNumberFromReg(ctx.REG().getText());
            result |= rm;

            return result;
        } else if(Matcher.match(poundExpression)){
            //Otherwise set the immediate bit to one
            result |= (1 << 25);

            int initResult = poundExpression.accept(this);
            if(initResult > 255){
                initResult = 255;
            } else if(initResult < 0){
                initResult = 0;
            }

            initResult &= 0xff;

            result |= initResult;

            return result;

        } else {
            return 0;
        }
    }

    @Override
    public Integer visitCmpInstr(CmpInstrContext ctx) {
        AntlrToken<Integer> COMPARE = Factory.decorateToken(ctx.COMPARE());

        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());
        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());

        Integer result = getConditionCode(COMPARE.getText(), 3);

        //Set Opcode Bits
        result |= (0b1010 << 21);

        //set operand 2
        result |= op2.accept(this);

        Integer rn = generateNumberFromReg(REG.getText());
        result |= rn << 16;

        return result;
    }

    @Override
    public Integer visitInstruction(InstructionContext ctx) {
        AntlrToken<Integer> bInstr = Factory.decorateToken(ctx.bInstr());
        AntlrToken<Integer> blInstr = Factory.decorateToken(ctx.blInstr());
        AntlrToken<Integer> bxInstr = Factory.decorateToken(ctx.bxInstr());
        AntlrToken<Integer> ldmInstr = Factory.decorateToken(ctx.ldmInstr());
        AntlrToken<Integer> ldrSignedInstr = Factory.decorateToken(ctx.ldrSignedInstr());
        AntlrToken<Integer> ldrDefInstr = Factory.decorateToken(ctx.ldrDefInstr());
        AntlrToken<Integer> mlaInstr = Factory.decorateToken(ctx.mlaInstr());
        AntlrToken<Integer> mrsInstr = Factory.decorateToken(ctx.mrsInstr());
        AntlrToken<Integer> msrDefInstr = Factory.decorateToken(ctx.msrDefInstr());
        AntlrToken<Integer> msrPrivInstr = Factory.decorateToken(ctx.msrPrivInstr());
        AntlrToken<Integer> mulInstr = Factory.decorateToken(ctx.mulInstr());
        AntlrToken<Integer> stmInstr = Factory.decorateToken(ctx.stmInstr());
        AntlrToken<Integer> strSignedInstr = Factory.decorateToken(ctx.strSignedInstr());
        AntlrToken<Integer> strDefInstr = Factory.decorateToken(ctx.strDefInstr());
        AntlrToken<Integer> swiInstr = Factory.decorateToken(ctx.swiInstr());
        AntlrToken<Integer> swpInstr = Factory.decorateToken(ctx.swpInstr());
        AntlrToken<Integer> addInstr = Factory.decorateToken(ctx.addInstr());
        AntlrToken<Integer> andInstr = Factory.decorateToken(ctx.andInstr());
        AntlrToken<Integer> eorInstr = Factory.decorateToken(ctx.eorInstr());
        AntlrToken<Integer> subInstr = Factory.decorateToken(ctx.subInstr());
        AntlrToken<Integer> rsbInstr = Factory.decorateToken(ctx.rsbInstr());
        AntlrToken<Integer> adcInstr = Factory.decorateToken(ctx.adcInstr());
        AntlrToken<Integer> sbcInstr = Factory.decorateToken(ctx.sbcInstr());
        AntlrToken<Integer> rscInstr = Factory.decorateToken(ctx.rscInstr());
        AntlrToken<Integer> tstInstr = Factory.decorateToken(ctx.tstInstr());
        AntlrToken<Integer> teqInstr = Factory.decorateToken(ctx.teqInstr());
        AntlrToken<Integer> cmpInstr = Factory.decorateToken(ctx.cmpInstr());
        AntlrToken<Integer> cmnInstr = Factory.decorateToken(ctx.cmnInstr());
        AntlrToken<Integer> orrInstr = Factory.decorateToken(ctx.orrInstr());
        AntlrToken<Integer> movInstr = Factory.decorateToken(ctx.movInstr());
        AntlrToken<Integer> bicInstr = Factory.decorateToken(ctx.bicInstr());
        AntlrToken<Integer> mvnInstr = Factory.decorateToken(ctx.mvnInstr());
        AntlrToken<Integer> stopInstr = Factory.decorateToken(ctx.stopInstr());

        if(Matcher.match(bInstr)) return bInstr.accept(this);
        else if(Matcher.match(blInstr)) return blInstr.accept(this);
        else if(Matcher.match(bxInstr)) return bxInstr.accept(this);
        else if(Matcher.match(ldmInstr)) return ldmInstr.accept(this);
        else if(Matcher.match(ldrSignedInstr)) return ldrSignedInstr.accept(this);
        else if(Matcher.match(ldrDefInstr)) return ldrDefInstr.accept(this);
        else if(Matcher.match(mlaInstr)) return mlaInstr.accept(this);
        else if(Matcher.match(mrsInstr)) return mrsInstr.accept(this);
        else if(Matcher.match(msrDefInstr)) return msrDefInstr.accept(this);
        else if(Matcher.match(msrPrivInstr)) return msrPrivInstr.accept(this);
        else if(Matcher.match(mulInstr)) return mulInstr.accept(this);
        else if(Matcher.match(stmInstr)) return stmInstr.accept(this);
        else if(Matcher.match(strSignedInstr)) return strSignedInstr.accept(this);
        else if(Matcher.match(strDefInstr)) return strDefInstr.accept(this);
        else if(Matcher.match(swiInstr)) return swiInstr.accept(this);
        else if(Matcher.match(swpInstr)) return swpInstr.accept(this);
        else if(Matcher.match(addInstr)) return addInstr.accept(this);
        else if(Matcher.match(andInstr)) return andInstr.accept(this);
        else if(Matcher.match(eorInstr)) return eorInstr.accept(this);
        else if(Matcher.match(subInstr)) return subInstr.accept(this);
        else if(Matcher.match(rsbInstr)) return rsbInstr.accept(this);
        else if(Matcher.match(adcInstr)) return adcInstr.accept(this);
        else if(Matcher.match(sbcInstr)) return sbcInstr.accept(this);
        else if(Matcher.match(rscInstr)) return rscInstr.accept(this);
        else if(Matcher.match(tstInstr)) return tstInstr.accept(this);
        else if(Matcher.match(teqInstr)) return teqInstr.accept(this);
        else if(Matcher.match(cmpInstr)) return cmpInstr.accept(this);
        else if(Matcher.match(cmnInstr)) return cmnInstr.accept(this);
        else if(Matcher.match(orrInstr)) return orrInstr.accept(this);
        else if(Matcher.match(movInstr)) return movInstr.accept(this);
        else if(Matcher.match(bicInstr)) return bicInstr.accept(this);
        else if(Matcher.match(mvnInstr)) return mvnInstr.accept(this);
        else if(Matcher.match(stopInstr)) return stopInstr.accept(this);
        else{
            return 0;
        }
    }

    @Override
    public Integer visitSwiInstr(SwiInstrContext ctx) {
        AntlrToken<Integer> SWI = Factory.decorateToken(ctx.SOFTWARE_INTERRUPT());
        AntlrToken<Integer> expression = Factory.decorateToken(ctx.expression());

        Integer result = getConditionCode(SWI.getText(), 3);

        //Set Instruction Bits
        result |= 0b1111 << 24;

        result |= expression.accept(this);

        return result;
    }

    @Override
    public Integer visitRelational(RelationalContext ctx) {
        // TODO Auto-generated method stub
        AntlrToken<Integer> REQ = Factory.decorateToken(ctx.REQ());
        AntlrToken<Integer> RNE = Factory.decorateToken(ctx.RNE());
        AntlrToken<Integer> RLT = Factory.decorateToken(ctx.RLT());
        AntlrToken<Integer> RGT = Factory.decorateToken(ctx.RGT());
        AntlrToken<Integer> RLE = Factory.decorateToken(ctx.RLE());
        AntlrToken<Integer> RGE = Factory.decorateToken(ctx.RGE());

        
        AntlrToken<Integer> primary0 = Factory.decorateToken(ctx.primary(0));
        AntlrToken<Integer> primary1 = Factory.decorateToken(ctx.primary(1));

        if(Matcher.match(primary0, REQ, primary1)){
            Integer primary0Result = primary0.accept(this);
            Integer primary1Result = primary1.accept(this);

            return (primary0Result == primary1Result) ? 1 : 0;
        } else if(Matcher.match(primary0, RNE, primary1)){
            Integer primary0Result = primary0.accept(this);
            Integer primary1Result = primary1.accept(this);

            return (primary0Result != primary1Result) ? 1 : 0;
        } else if(Matcher.match(primary0, RLT, primary1)){
            Integer primary0Result = primary0.accept(this);
            Integer primary1Result = primary1.accept(this);

            return (primary0Result < primary1Result) ? 1 : 0;
        } else if(Matcher.match(primary0, RGT, primary1)){
            Integer primary0Result = primary0.accept(this);
            Integer primary1Result = primary1.accept(this);

            return (primary0Result > primary1Result) ? 1 : 0;
        } else if(Matcher.match(primary0, RLE, primary1)){
            Integer primary0Result = primary0.accept(this);
            Integer primary1Result = primary1.accept(this);

            return (primary0Result <= primary1Result) ? 1 : 0;
        } else if(Matcher.match(primary0, RGE, primary1)){
            Integer primary0Result = primary0.accept(this);
            Integer primary1Result = primary1.accept(this);

            return (primary0Result >= primary1Result) ? 1 : 0;
        } else if(Matcher.match(primary0)){
            return primary0.accept(this);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitAndExpr(AndExprContext ctx) {
        // TODO Auto-generated method stub
        AntlrToken<Integer> relationalExpr = Factory.decorateToken(ctx.relational());
        AntlrToken<Integer> andExpr = Factory.decorateToken(ctx.andExpr());
        AntlrToken<Integer> LAND = Factory.decorateToken(ctx.LAND());
        
        if(Matcher.match(relationalExpr, LAND, andExpr)){
            Integer resultAndExpr = andExpr.accept(this);
            Integer resultRelationalExpr = relationalExpr.accept(this);

            return resultAndExpr & resultRelationalExpr;
        } else if(Matcher.match(relationalExpr)){
            return relationalExpr.accept(this);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitPrimary(PrimaryContext ctx) {
        // TODO Auto-generated method stub
        AntlrToken<Integer> bitwise = Factory.decorateToken(ctx.bitwise());
        AntlrToken<Integer> primary = Factory.decorateToken(ctx.primary());
        AntlrToken<Integer> PLUS = Factory.decorateToken(ctx.PLUS());
        AntlrToken<Integer> MINUS = Factory.decorateToken(ctx.MINUS());

        if(Matcher.match(bitwise, PLUS, primary)){
            Integer bitwiseResult = bitwise.accept(this);
            Integer primaryResult = primary.accept(this);

            return bitwiseResult + primaryResult;
        } else if(Matcher.match(bitwise, MINUS, primary)){
            Integer bitwiseResult = bitwise.accept(this);
            Integer primaryResult = primary.accept(this);

            return bitwiseResult - primaryResult;
        } else if(Matcher.match(bitwise)){
            Integer bitwiseResult = bitwise.accept(this);

            return bitwiseResult;
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitPostIndexedAddressing(PostIndexedAddressingContext ctx) {
        Integer result = (0 << 24);

        AntlrToken<Integer> LBRACK = Factory.decorateToken(ctx.LBRACK());
        AntlrToken<Integer> RBRACK = Factory.decorateToken(ctx.RBRACK());
        AntlrToken<Integer> PLUS = Factory.decorateToken(ctx.PLUS());
        AntlrToken<Integer> MINUS = Factory.decorateToken(ctx.MINUS());

        AntlrToken<Integer> poundExpression = Factory.decorateToken(ctx.poundExpression());
        AntlrToken<Integer> shift = Factory.decorateToken(ctx.shift());

        if(Matcher.match(poundExpression)){
            // offset of expression bytes
            //so set the immediate bit to zero
            result |= (0 << 25);
            Integer expressionResult = poundExpression.accept(this);
            if(expressionResult < 0){
                // Then the down bit needs to be set
                result |= (0 << 23);
            } else {
                //Otherwise the down bit needs to be set to 1
                result |= (1 << 23);
            }

            //finally we need to set the base register to be equal to
            //the REG value
            Integer regNumber = generateNumberFromReg(ctx.REG(0).getText());
            result |= (regNumber << 16);
        } else {
            //Otherwise the offset is a shift
            //So we need to set the immediate bit to 1
            //So we can specify a register and a shift
            result |= (1 << 25);

            //Now set the Up or Down bit based on the number that is found
            if(Matcher.match(MINUS)){
                //Then we need to subtract
                result |= (0 << 23);
            } else {
                //Otherwise we need to add the offset by setting the up/down bit to 1
                result |= (1 << 23);
            }


            Integer rnRegNumber =generateNumberFromReg(ctx.REG(0).getText());
            result |= (rnRegNumber << 16);

            Integer rmRegNumber = generateNumberFromReg(ctx.REG(1).getText());
            result |= rmRegNumber;

            if(Matcher.match(shift)){
                //we need to place a shift inside the 4th bit
                result |= shift.accept(this);
            }
        }


        return result;
    }

    @Override
    public Integer visitPreIndexedAddressing(PreIndexedAddressingContext ctx) {
        AntlrToken<Integer> EXP = Factory.decorateToken(ctx.EXP());
        AntlrToken<Integer> poundExpression = Factory.decorateToken(ctx.poundExpression());
        
        AntlrToken<Integer> shift = Factory.decorateToken(ctx.shift());
        AntlrToken<Integer> PLUS = Factory.decorateToken(ctx.PLUS());
        AntlrToken<Integer> MINUS = Factory.decorateToken(ctx.MINUS());

        Integer result = (1 << 24); //Set pre bit to one

        //If we have a pound expression then we need to do an immediate offset with the result of the expression
        if(Matcher.match(poundExpression)){
            result |= (0 << 25); //Offset is immediate
            Integer expressionResult = poundExpression.accept(this);
            if(expressionResult < 0){
                // Then the down bit needs to be set to 0 for down
                result |= (0 << 23);
            } else {
                //Otherwise the down bit needs to be set to 1 for up
                result |= (1 << 23);
            }

            result |= Math.abs(expressionResult);

            //finally we need to set the base register to be equal to
            //the REG value
            Integer regNumber = generateNumberFromReg(ctx.REG(0).getText());
            result |= (regNumber << 16);

            if(Matcher.match(EXP)){
                //Set the write back bit to 1
                result |= (1 << 21);
            } else {
                result |= (0 << 21);
            }
        } else if(ctx.REG().size() == 1) {
            //Then it is a zero offset
            Integer rnRegNumber = generateNumberFromReg(ctx.REG(0).getText());
            result |= (rnRegNumber << 16);

            result |= 0 << 25;
            
            if(Matcher.match(EXP)){
                result |= (1 << 21);
            } else {
                result |= (0 << 21);
            }
        } else {
            //Otherwise it is a sifted offset with a register value
            //Otherwise the offset is a shift
            //So we need to set the immediate bit to 1
            //So we can specify a register and a shift
            result |= (1 << 25);

            //Now set the Up or Down bit based on the number that is found
            if(Matcher.match(MINUS)){
                //Then we need to subtract
                result |= (0 << 23);
            } else {
                //Otherwise we need to add the offset by setting the up/down bit to 1
                result |= (1 << 23);
            }


            Integer rnRegNumber = generateNumberFromReg(ctx.REG(0).getText());
            result |= (rnRegNumber << 16);

            Integer rmRegNumber = generateNumberFromReg(ctx.REG(1).getText());
            result |= rmRegNumber;

            if(Matcher.match(shift)){
                result |= shift.accept(this);
            }

            if(Matcher.match(EXP)){
                //Set the write back bit to 1
                result |= (1 << 21);
            }
        }
        
        return result;
    }

    @Override
    public Integer visitPsrf(PsrfContext ctx) {
        AntlrToken<Integer> CPSR_FLAG = Factory.decorateToken(ctx.CPSR_FLG());
        AntlrToken<Integer> SPSR_FLAG = Factory.decorateToken(ctx.SPSR_FLG());

        if(Matcher.match(CPSR_FLAG)){
            return (0 << 22);
        } else if(Matcher.match(SPSR_FLAG)){
            return (1 << 22);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitPsr(PsrContext ctx) {
        AntlrToken<Integer> CPSR = Factory.decorateToken(ctx.CPSR());
        AntlrToken<Integer> CPSR_ALL = Factory.decorateToken(ctx.CPSR_ALL());
        AntlrToken<Integer> SPSR = Factory.decorateToken(ctx.SPSR());
        AntlrToken<Integer> SPSR_ALL = Factory.decorateToken(ctx.SPSR_ALL());

        if(Matcher.match(CPSR)){
            return (0 << 22);
        } else if(Matcher.match(CPSR_ALL)){
            return (0 << 22);
        } else if(Matcher.match(SPSR)){
            return (1 << 22);
        } else if(Matcher.match(SPSR_ALL)){
            return (1 << 22);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitCmnInstr(CmnInstrContext ctx) {
        AntlrToken<Integer> COMPARE_NEGATIVE = Factory.decorateToken(ctx.COMPARE_NEGATIVE());

        AntlrToken<Integer> REG = Factory.decorateToken(ctx.REG());
        AntlrToken<Integer> op2 = Factory.decorateToken(ctx.op2());

        Integer result = getConditionCode(COMPARE_NEGATIVE.getText(), 3);

        //Set Opcode Bits
        result |= (0b1011 << 21);

        //set operand 2
        result |= op2.accept(this);

        Integer rn = generateNumberFromReg(REG.getText());
        result |= rn << 16;

        return result;
    }

    private boolean isByteValue = false;
    private Integer builtInteger = 0;
    private Integer shiftCount = 1;
	@Override
	public Integer visitProgram(ProgramContext ctx) {
		for(InstructionOrDirectiveContext instr : ctx.instructionOrDirective()){
            Integer result = instr.accept(this);
            if(isByteValue){
                builtInteger |= result << (32 - (shiftCount * 8));
                if(shiftCount == 4){
                    codeList.add(builtInteger);
                    builtInteger = 0;
                    shiftCount = 1;
                } else {
                    shiftCount++;
                }
            } else if(shiftCount > 1){
                //We need to append the latest Integer
                codeList.add(builtInteger);
                builtInteger = 0;
                shiftCount = 1;

                codeList.add(result);
            } else {
                codeList.add(result);
            }
            isByteValue = false;
        }
        return null;
	}

    @Override
    public Integer visitStopInstr(StopInstrContext ctx) {
        AntlrToken<Integer> STOP = Factory.decorateToken(ctx.STOP());
        return 0b00000110000000000000000000010000;
    }

    @Override
    public Integer visitInstructionOrDirective(InstructionOrDirectiveContext ctx) {
        AntlrToken<Integer> wordDirective = Factory.decorateToken(ctx.wordDirective());
        AntlrToken<Integer> byteDirective = Factory.decorateToken(ctx.byteDirective());
        AntlrToken<Integer> instruction = Factory.decorateToken(ctx.instruction());

        this.currentInstructionOrDirective = ctx;

        if(Matcher.match(wordDirective)){
            return wordDirective.accept(this);
        } else if(Matcher.match(byteDirective)){
            return byteDirective.accept(this); 
        } else if(Matcher.match(instruction)) {
            return instruction.accept(this);
        } else {
            return null;
        }
    }

    @Override
    public Integer visitByteDirective(ByteDirectiveContext ctx) {
        AntlrToken<Integer> number = Factory.decorateToken(ctx.number());
        isByteValue = true;
        return number.accept(this) & 0xff;
    }

    @Override
    public Integer visitWordDirective(WordDirectiveContext ctx) {
        AntlrToken<Integer> number = Factory.decorateToken(ctx.number());
        return number.accept(this);
    }

    @Override
    public Integer visitIdentifier(IdentifierContext ctx) {
        AntlrToken<Integer> IDENT = Factory.decorateToken(ctx.IDENT());

        if(this.labelAdresses.containsKey(ctx.getText())){
            return this.labelAdresses.get(ctx.getText());
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitSingle(SingleContext ctx) {
        AntlrToken<Integer> identifier = Factory.decorateToken(ctx.identifier());
        AntlrToken<Integer> number = Factory.decorateToken(ctx.number());

        if(Matcher.match(identifier)){
            return identifier.accept(this);
        } else if(Matcher.match(number)){
            return number.accept(this);
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitShiftName(ShiftNameContext ctx) {
        AntlrToken<Integer> LSL = Factory.decorateToken(ctx.LSL());
        AntlrToken<Integer> ASR = Factory.decorateToken(ctx.ASR());
        AntlrToken<Integer> LSR = Factory.decorateToken(ctx.LSR());
        AntlrToken<Integer> ROR = Factory.decorateToken(ctx.ROR());

        if(Matcher.match(LSL)){
            return 0;
        } else if(Matcher.match(ASR)){
            return 2;
        } else if(Matcher.match(LSR)){
            return 1;
        } else if(Matcher.match(ROR)){
            return 3;
        } else {
            return 0;
        }
    }

    @Override
    public Integer visitRealNumber(RealNumberContext ctx) {
        String number = ctx.getText();
        float asFloat = Float.parseFloat(number);
        long asLong = (long)asFloat;
        int asInt = (int)asLong;
        return asInt;
    }
}