// Generated from io\github\H20man13\DeClan\main\assembler\ArmAssembler.g4 by ANTLR 4.0
package io.github.H20man13.DeClan.main.assembler;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;

public interface ArmAssemblerListener extends ParseTreeListener {
	void enterCmnInstr(ArmAssemblerParser.CmnInstrContext ctx);
	void exitCmnInstr(ArmAssemblerParser.CmnInstrContext ctx);

	void enterShift(ArmAssemblerParser.ShiftContext ctx);
	void exitShift(ArmAssemblerParser.ShiftContext ctx);

	void enterTstInstr(ArmAssemblerParser.TstInstrContext ctx);
	void exitTstInstr(ArmAssemblerParser.TstInstrContext ctx);

	void enterProgram(ArmAssemblerParser.ProgramContext ctx);
	void exitProgram(ArmAssemblerParser.ProgramContext ctx);

	void enterUnary(ArmAssemblerParser.UnaryContext ctx);
	void exitUnary(ArmAssemblerParser.UnaryContext ctx);

	void enterRsbInstr(ArmAssemblerParser.RsbInstrContext ctx);
	void exitRsbInstr(ArmAssemblerParser.RsbInstrContext ctx);

	void enterLdrDefInstr(ArmAssemblerParser.LdrDefInstrContext ctx);
	void exitLdrDefInstr(ArmAssemblerParser.LdrDefInstrContext ctx);

	void enterRscInstr(ArmAssemblerParser.RscInstrContext ctx);
	void exitRscInstr(ArmAssemblerParser.RscInstrContext ctx);

	void enterNumber(ArmAssemblerParser.NumberContext ctx);
	void exitNumber(ArmAssemblerParser.NumberContext ctx);

	void enterShiftName(ArmAssemblerParser.ShiftNameContext ctx);
	void exitShiftName(ArmAssemblerParser.ShiftNameContext ctx);

	void enterStmInstr(ArmAssemblerParser.StmInstrContext ctx);
	void exitStmInstr(ArmAssemblerParser.StmInstrContext ctx);

	void enterMulInstr(ArmAssemblerParser.MulInstrContext ctx);
	void exitMulInstr(ArmAssemblerParser.MulInstrContext ctx);

	void enterBxInstr(ArmAssemblerParser.BxInstrContext ctx);
	void exitBxInstr(ArmAssemblerParser.BxInstrContext ctx);

	void enterBicInstr(ArmAssemblerParser.BicInstrContext ctx);
	void exitBicInstr(ArmAssemblerParser.BicInstrContext ctx);

	void enterIdentifier(ArmAssemblerParser.IdentifierContext ctx);
	void exitIdentifier(ArmAssemblerParser.IdentifierContext ctx);

	void enterSbcInstr(ArmAssemblerParser.SbcInstrContext ctx);
	void exitSbcInstr(ArmAssemblerParser.SbcInstrContext ctx);

	void enterEorInstr(ArmAssemblerParser.EorInstrContext ctx);
	void exitEorInstr(ArmAssemblerParser.EorInstrContext ctx);

	void enterCmpInstr(ArmAssemblerParser.CmpInstrContext ctx);
	void exitCmpInstr(ArmAssemblerParser.CmpInstrContext ctx);

	void enterPrimary(ArmAssemblerParser.PrimaryContext ctx);
	void exitPrimary(ArmAssemblerParser.PrimaryContext ctx);

	void enterMrsInstr(ArmAssemblerParser.MrsInstrContext ctx);
	void exitMrsInstr(ArmAssemblerParser.MrsInstrContext ctx);

	void enterSwpInstr(ArmAssemblerParser.SwpInstrContext ctx);
	void exitSwpInstr(ArmAssemblerParser.SwpInstrContext ctx);

	void enterPsrf(ArmAssemblerParser.PsrfContext ctx);
	void exitPsrf(ArmAssemblerParser.PsrfContext ctx);

	void enterAdcInstr(ArmAssemblerParser.AdcInstrContext ctx);
	void exitAdcInstr(ArmAssemblerParser.AdcInstrContext ctx);

	void enterTeqInstr(ArmAssemblerParser.TeqInstrContext ctx);
	void exitTeqInstr(ArmAssemblerParser.TeqInstrContext ctx);

	void enterPoundExpression(ArmAssemblerParser.PoundExpressionContext ctx);
	void exitPoundExpression(ArmAssemblerParser.PoundExpressionContext ctx);

	void enterAndInstr(ArmAssemblerParser.AndInstrContext ctx);
	void exitAndInstr(ArmAssemblerParser.AndInstrContext ctx);

	void enterLdrSignedInstr(ArmAssemblerParser.LdrSignedInstrContext ctx);
	void exitLdrSignedInstr(ArmAssemblerParser.LdrSignedInstrContext ctx);

	void enterPsr(ArmAssemblerParser.PsrContext ctx);
	void exitPsr(ArmAssemblerParser.PsrContext ctx);

	void enterMsrDefInstr(ArmAssemblerParser.MsrDefInstrContext ctx);
	void exitMsrDefInstr(ArmAssemblerParser.MsrDefInstrContext ctx);

	void enterLdmInstr(ArmAssemblerParser.LdmInstrContext ctx);
	void exitLdmInstr(ArmAssemblerParser.LdmInstrContext ctx);

	void enterStrDefInstr(ArmAssemblerParser.StrDefInstrContext ctx);
	void exitStrDefInstr(ArmAssemblerParser.StrDefInstrContext ctx);

	void enterOrrInstr(ArmAssemblerParser.OrrInstrContext ctx);
	void exitOrrInstr(ArmAssemblerParser.OrrInstrContext ctx);

	void enterMlaInstr(ArmAssemblerParser.MlaInstrContext ctx);
	void exitMlaInstr(ArmAssemblerParser.MlaInstrContext ctx);

	void enterAddInstr(ArmAssemblerParser.AddInstrContext ctx);
	void exitAddInstr(ArmAssemblerParser.AddInstrContext ctx);

	void enterBitwise(ArmAssemblerParser.BitwiseContext ctx);
	void exitBitwise(ArmAssemblerParser.BitwiseContext ctx);

	void enterPostIndexedAddressing(ArmAssemblerParser.PostIndexedAddressingContext ctx);
	void exitPostIndexedAddressing(ArmAssemblerParser.PostIndexedAddressingContext ctx);

	void enterBInstr(ArmAssemblerParser.BInstrContext ctx);
	void exitBInstr(ArmAssemblerParser.BInstrContext ctx);

	void enterRList(ArmAssemblerParser.RListContext ctx);
	void exitRList(ArmAssemblerParser.RListContext ctx);

	void enterTerm(ArmAssemblerParser.TermContext ctx);
	void exitTerm(ArmAssemblerParser.TermContext ctx);

	void enterSubInstr(ArmAssemblerParser.SubInstrContext ctx);
	void exitSubInstr(ArmAssemblerParser.SubInstrContext ctx);

	void enterInstructionOrDirective(ArmAssemblerParser.InstructionOrDirectiveContext ctx);
	void exitInstructionOrDirective(ArmAssemblerParser.InstructionOrDirectiveContext ctx);

	void enterStrSignedInstr(ArmAssemblerParser.StrSignedInstrContext ctx);
	void exitStrSignedInstr(ArmAssemblerParser.StrSignedInstrContext ctx);

	void enterExpression(ArmAssemblerParser.ExpressionContext ctx);
	void exitExpression(ArmAssemblerParser.ExpressionContext ctx);

	void enterAddress(ArmAssemblerParser.AddressContext ctx);
	void exitAddress(ArmAssemblerParser.AddressContext ctx);

	void enterBlInstr(ArmAssemblerParser.BlInstrContext ctx);
	void exitBlInstr(ArmAssemblerParser.BlInstrContext ctx);

	void enterByteDirective(ArmAssemblerParser.ByteDirectiveContext ctx);
	void exitByteDirective(ArmAssemblerParser.ByteDirectiveContext ctx);

	void enterMvnInstr(ArmAssemblerParser.MvnInstrContext ctx);
	void exitMvnInstr(ArmAssemblerParser.MvnInstrContext ctx);

	void enterWordDirective(ArmAssemblerParser.WordDirectiveContext ctx);
	void exitWordDirective(ArmAssemblerParser.WordDirectiveContext ctx);

	void enterRValue(ArmAssemblerParser.RValueContext ctx);
	void exitRValue(ArmAssemblerParser.RValueContext ctx);

	void enterMsrPrivInstr(ArmAssemblerParser.MsrPrivInstrContext ctx);
	void exitMsrPrivInstr(ArmAssemblerParser.MsrPrivInstrContext ctx);

	void enterMovInstr(ArmAssemblerParser.MovInstrContext ctx);
	void exitMovInstr(ArmAssemblerParser.MovInstrContext ctx);

	void enterOp2(ArmAssemblerParser.Op2Context ctx);
	void exitOp2(ArmAssemblerParser.Op2Context ctx);

	void enterSingle(ArmAssemblerParser.SingleContext ctx);
	void exitSingle(ArmAssemblerParser.SingleContext ctx);

	void enterInstruction(ArmAssemblerParser.InstructionContext ctx);
	void exitInstruction(ArmAssemblerParser.InstructionContext ctx);

	void enterSwiInstr(ArmAssemblerParser.SwiInstrContext ctx);
	void exitSwiInstr(ArmAssemblerParser.SwiInstrContext ctx);

	void enterRelational(ArmAssemblerParser.RelationalContext ctx);
	void exitRelational(ArmAssemblerParser.RelationalContext ctx);

	void enterStopInstr(ArmAssemblerParser.StopInstrContext ctx);
	void exitStopInstr(ArmAssemblerParser.StopInstrContext ctx);

	void enterPreIndexedAddressing(ArmAssemblerParser.PreIndexedAddressingContext ctx);
	void exitPreIndexedAddressing(ArmAssemblerParser.PreIndexedAddressingContext ctx);

	void enterAndExpr(ArmAssemblerParser.AndExprContext ctx);
	void exitAndExpr(ArmAssemblerParser.AndExprContext ctx);
}