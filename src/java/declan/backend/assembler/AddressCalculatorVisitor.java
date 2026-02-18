package declan.backend.assembler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import declan.backend.assembler.ArmAssemblerParser.AdcInstrContext;
import declan.backend.assembler.ArmAssemblerParser.AddInstrContext;
import declan.backend.assembler.ArmAssemblerParser.AddressContext;
import declan.backend.assembler.ArmAssemblerParser.AndExprContext;
import declan.backend.assembler.ArmAssemblerParser.AndInstrContext;
import declan.backend.assembler.ArmAssemblerParser.BInstrContext;
import declan.backend.assembler.ArmAssemblerParser.BicInstrContext;
import declan.backend.assembler.ArmAssemblerParser.BitwiseContext;
import declan.backend.assembler.ArmAssemblerParser.BlInstrContext;
import declan.backend.assembler.ArmAssemblerParser.BxInstrContext;
import declan.backend.assembler.ArmAssemblerParser.ByteDirectiveContext;
import declan.backend.assembler.ArmAssemblerParser.CmnInstrContext;
import declan.backend.assembler.ArmAssemblerParser.CmpInstrContext;
import declan.backend.assembler.ArmAssemblerParser.EorInstrContext;
import declan.backend.assembler.ArmAssemblerParser.ExpressionContext;
import declan.backend.assembler.ArmAssemblerParser.IdentifierContext;
import declan.backend.assembler.ArmAssemblerParser.InstructionContext;
import declan.backend.assembler.ArmAssemblerParser.InstructionOrDirectiveContext;
import declan.backend.assembler.ArmAssemblerParser.LdmInstrContext;
import declan.backend.assembler.ArmAssemblerParser.LdrDefInstrContext;
import declan.backend.assembler.ArmAssemblerParser.LdrSignedInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MlaInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MlalInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MovInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MrsInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MsrDefInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MsrPrivInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MulInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MullInstrContext;
import declan.backend.assembler.ArmAssemblerParser.MvnInstrContext;
import declan.backend.assembler.ArmAssemblerParser.NumberContext;
import declan.backend.assembler.ArmAssemblerParser.Op2Context;
import declan.backend.assembler.ArmAssemblerParser.OrrInstrContext;
import declan.backend.assembler.ArmAssemblerParser.PostIndexedAddressingContext;
import declan.backend.assembler.ArmAssemblerParser.PoundExpressionContext;
import declan.backend.assembler.ArmAssemblerParser.PreIndexedAddressingContext;
import declan.backend.assembler.ArmAssemblerParser.PrimaryContext;
import declan.backend.assembler.ArmAssemblerParser.ProgramContext;
import declan.backend.assembler.ArmAssemblerParser.PsrContext;
import declan.backend.assembler.ArmAssemblerParser.PsrfContext;
import declan.backend.assembler.ArmAssemblerParser.RListContext;
import declan.backend.assembler.ArmAssemblerParser.RValueContext;
import declan.backend.assembler.ArmAssemblerParser.RealNumberContext;
import declan.backend.assembler.ArmAssemblerParser.RelationalContext;
import declan.backend.assembler.ArmAssemblerParser.RsbInstrContext;
import declan.backend.assembler.ArmAssemblerParser.RscInstrContext;
import declan.backend.assembler.ArmAssemblerParser.SbcInstrContext;
import declan.backend.assembler.ArmAssemblerParser.ShiftContext;
import declan.backend.assembler.ArmAssemblerParser.ShiftNameContext;
import declan.backend.assembler.ArmAssemblerParser.SingleContext;
import declan.backend.assembler.ArmAssemblerParser.StmInstrContext;
import declan.backend.assembler.ArmAssemblerParser.StopInstrContext;
import declan.backend.assembler.ArmAssemblerParser.StrDefInstrContext;
import declan.backend.assembler.ArmAssemblerParser.StrSignedInstrContext;
import declan.backend.assembler.ArmAssemblerParser.SubInstrContext;
import declan.backend.assembler.ArmAssemblerParser.SwiInstrContext;
import declan.backend.assembler.ArmAssemblerParser.SwpInstrContext;
import declan.backend.assembler.ArmAssemblerParser.TeqInstrContext;
import declan.backend.assembler.ArmAssemblerParser.TermContext;
import declan.backend.assembler.ArmAssemblerParser.TstInstrContext;
import declan.backend.assembler.ArmAssemblerParser.UnaryContext;
import declan.backend.assembler.ArmAssemblerParser.WordDirectiveContext;
import declan.utils.matcher.AntlrToken;
import declan.utils.matcher.AntlrTokenFactory;
import declan.utils.matcher.AntlrTokenMatcher;

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

	@Override
	public Integer visitMullInstr(MullInstrContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visitMlalInstr(MlalInstrContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}
}