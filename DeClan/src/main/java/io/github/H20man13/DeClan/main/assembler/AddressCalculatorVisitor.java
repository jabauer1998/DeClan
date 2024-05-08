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

public class AddressCalculatorVisitor implements ArmAssemblerVisitor<Integer> {
    private AntlrTokenMatcher<Integer> Matcher;
    private AntlrTokenFactory<Integer> Factory;
    private HashMap<InstructionOrDirectiveContext, Integer> adresses;
    private Integer currentAddress = 0;

    public AddressCalculatorVisitor(){
        this.Matcher = new AntlrTokenMatcher<Integer>();
        this.Factory = new AntlrTokenFactory<Integer>();
        this.adresses = new HashMap<InstructionOrDirectiveContext, Integer>();
        this.currentAddress = 0;
    }

    public Map<InstructionOrDirectiveContext, Integer> caclulateAdresses(ProgramContext ctx){
        this.adresses = new HashMap<InstructionOrDirectiveContext, Integer>();
        this.currentAddress = 0;
        ctx.accept(this);
        return this.adresses;
    }

    @Override
    public Integer visit(ParseTree tree) {
        return null;
    }

    @Override
    public Integer visitChildren(RuleNode node) {
        return null;
    }

    @Override
    public Integer visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public Integer visitErrorNode(ErrorNode node) {
        return null;
    }

    @Override
    public Integer visitMrsInstr(MrsInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitSwpInstr(SwpInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitAdcInstr(AdcInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitTeqInstr(TeqInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitShift(ShiftContext ctx) {
        return null;
    }

    @Override
    public Integer visitPoundExpression(PoundExpressionContext ctx) {
        return null;
    }

    @Override
    public Integer visitTstInstr(TstInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitStrSignedInstr(StrSignedInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitAndInstr(AndInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitUnary(UnaryContext ctx) {
        return null;
    }

    @Override
    public Integer visitRsbInstr(RsbInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitLdrDefInstr(LdrDefInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitMsrDefInstr(MsrDefInstrContext ctx) {
        return null;
    }


    @Override
    public Integer visitRscInstr(RscInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitLdmInstr(LdmInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitStrDefInstr(StrDefInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitNumber(NumberContext ctx) {
        return null;
    }

    @Override
    public Integer visitStmInstr(StmInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitOrrInstr(OrrInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitMlaInstr(MlaInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitMulInstr(MulInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitAddInstr(AddInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitBxInstr(BxInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitBitwise(BitwiseContext ctx) {
        return null;
    }

    @Override
    public Integer visitBInstr(BInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitLdrSignedInstr(LdrSignedInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitBicInstr(BicInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitRList(RListContext ctx) {
       return null;
    }

    @Override
    public Integer visitTerm(TermContext ctx) {
        return null;
    }

    @Override
    public Integer visitSubInstr(SubInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitExpression(ExpressionContext ctx) {
        return null;
    }

    @Override
    public Integer visitAddress(AddressContext ctx) {
        return null;
    }

    @Override
    public Integer visitBlInstr(BlInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitSbcInstr(SbcInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitMvnInstr(MvnInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitRValue(RValueContext ctx) {
        return null;
    }

    @Override
    public Integer visitMsrPrivInstr(MsrPrivInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitEorInstr(EorInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitMovInstr(MovInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitOp2(Op2Context ctx) {
        return null;
    }

    @Override
    public Integer visitCmpInstr(CmpInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitInstruction(InstructionContext ctx) {
        return 4;
    }

    @Override
    public Integer visitSwiInstr(SwiInstrContext ctx) {
        return null;
    }

    @Override
    public Integer visitRelational(RelationalContext ctx) {
        return null;
    }

    @Override
    public Integer visitAndExpr(AndExprContext ctx) {
        return null;
    }

    @Override
    public Integer visitPrimary(PrimaryContext ctx) {
        return null;
    }

    @Override
    public Integer visitPostIndexedAddressing(PostIndexedAddressingContext ctx) {
        return null;
    }

    @Override
    public Integer visitPreIndexedAddressing(PreIndexedAddressingContext ctx) {
        return null;
    }

    @Override
    public Integer visitPsrf(PsrfContext ctx) {
        return null;
    }

    @Override
    public Integer visitPsr(PsrContext ctx) {
        return null;
    }

    @Override
    public Integer visitCmnInstr(CmnInstrContext ctx) {
        return null;
    }

	@Override
	public Integer visitProgram(ProgramContext ctx) {
        for(InstructionOrDirectiveContext instructionOrDirective : ctx.instructionOrDirective()){
            int result = instructionOrDirective.accept(this);
            this.currentAddress += result;
        }
        return null;
	}

    @Override
    public Integer visitStopInstr(StopInstrContext ctx) {
        return null;
    }

    private static String getLabelText(String sourceText){
        StringBuilder sb = new StringBuilder();
        
        for(char c : sourceText.toCharArray()){
            if(c == '_' || Character.isLetterOrDigit(c)){
                sb.append(c);
            }
        }

        return sb.toString();
    }

    @Override
    public Integer visitInstructionOrDirective(InstructionOrDirectiveContext ctx) {
        AntlrToken<Integer> instruction = Factory.decorateToken(ctx.instruction());
        AntlrToken<Integer> wordDirective = Factory.decorateToken(ctx.wordDirective());
        AntlrToken<Integer> byteDirective = Factory.decorateToken(ctx.byteDirective());
        AntlrToken<Integer> LABEL = Factory.decorateToken(ctx.LABEL());

        if(Matcher.match(instruction)){
            while(this.currentAddress % 4 != 0){
                this.currentAddress++;
            }

            this.adresses.put(ctx, currentAddress);

            return instruction.accept(this);
        } else if(Matcher.match(byteDirective)) {
            this.adresses.put(ctx, currentAddress);
            return byteDirective.accept(this);
        } else if(Matcher.match(wordDirective)) {
            while(this.currentAddress % 4 != 0){
                this.currentAddress++;
            }

            this.adresses.put(ctx, currentAddress);

            return wordDirective.accept(this);
        } else {
            return null;
        }
    }

    @Override
    public Integer visitByteDirective(ByteDirectiveContext ctx) {
        return 1;
    }

    @Override
    public Integer visitWordDirective(WordDirectiveContext ctx) {
        return 4;
    }

    @Override
    public Integer visitIdentifier(IdentifierContext ctx) {
        return null;
    }

    @Override
    public Integer visitSingle(SingleContext ctx) {
        return null;
    }

    @Override
    public Integer visitShiftName(ShiftNameContext ctx) {
        return null;
    }

    @Override
    public Integer visitRealNumber(RealNumberContext ctx) {
        return null;
    }
}