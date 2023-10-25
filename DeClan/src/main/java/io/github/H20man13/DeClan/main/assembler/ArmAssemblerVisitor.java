// Generated from io\github\H20man13\DeClan\main\assembler\ArmAssembler.g4 by ANTLR 4.0
package io.github.H20man13.DeClan.main.assembler;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;

public interface ArmAssemblerVisitor<T> extends ParseTreeVisitor<T> {
	T visitCmnInstr(ArmAssemblerParser.CmnInstrContext ctx);

	T visitShift(ArmAssemblerParser.ShiftContext ctx);

	T visitTstInstr(ArmAssemblerParser.TstInstrContext ctx);

	T visitProgram(ArmAssemblerParser.ProgramContext ctx);

	T visitUnary(ArmAssemblerParser.UnaryContext ctx);

	T visitRsbInstr(ArmAssemblerParser.RsbInstrContext ctx);

	T visitLdrDefInstr(ArmAssemblerParser.LdrDefInstrContext ctx);

	T visitRscInstr(ArmAssemblerParser.RscInstrContext ctx);

	T visitNumber(ArmAssemblerParser.NumberContext ctx);

	T visitShiftName(ArmAssemblerParser.ShiftNameContext ctx);

	T visitStmInstr(ArmAssemblerParser.StmInstrContext ctx);

	T visitMulInstr(ArmAssemblerParser.MulInstrContext ctx);

	T visitBxInstr(ArmAssemblerParser.BxInstrContext ctx);

	T visitBicInstr(ArmAssemblerParser.BicInstrContext ctx);

	T visitIdentifier(ArmAssemblerParser.IdentifierContext ctx);

	T visitSbcInstr(ArmAssemblerParser.SbcInstrContext ctx);

	T visitEorInstr(ArmAssemblerParser.EorInstrContext ctx);

	T visitCmpInstr(ArmAssemblerParser.CmpInstrContext ctx);

	T visitPrimary(ArmAssemblerParser.PrimaryContext ctx);

	T visitMrsInstr(ArmAssemblerParser.MrsInstrContext ctx);

	T visitSwpInstr(ArmAssemblerParser.SwpInstrContext ctx);

	T visitPsrf(ArmAssemblerParser.PsrfContext ctx);

	T visitAdcInstr(ArmAssemblerParser.AdcInstrContext ctx);

	T visitTeqInstr(ArmAssemblerParser.TeqInstrContext ctx);

	T visitPoundExpression(ArmAssemblerParser.PoundExpressionContext ctx);

	T visitAndInstr(ArmAssemblerParser.AndInstrContext ctx);

	T visitLdrSignedInstr(ArmAssemblerParser.LdrSignedInstrContext ctx);

	T visitPsr(ArmAssemblerParser.PsrContext ctx);

	T visitMsrDefInstr(ArmAssemblerParser.MsrDefInstrContext ctx);

	T visitLdmInstr(ArmAssemblerParser.LdmInstrContext ctx);

	T visitStrDefInstr(ArmAssemblerParser.StrDefInstrContext ctx);

	T visitOrrInstr(ArmAssemblerParser.OrrInstrContext ctx);

	T visitMlaInstr(ArmAssemblerParser.MlaInstrContext ctx);

	T visitAddInstr(ArmAssemblerParser.AddInstrContext ctx);

	T visitBitwise(ArmAssemblerParser.BitwiseContext ctx);

	T visitPostIndexedAddressing(ArmAssemblerParser.PostIndexedAddressingContext ctx);

	T visitBInstr(ArmAssemblerParser.BInstrContext ctx);

	T visitRList(ArmAssemblerParser.RListContext ctx);

	T visitTerm(ArmAssemblerParser.TermContext ctx);

	T visitSubInstr(ArmAssemblerParser.SubInstrContext ctx);

	T visitInstructionOrDirective(ArmAssemblerParser.InstructionOrDirectiveContext ctx);

	T visitStrSignedInstr(ArmAssemblerParser.StrSignedInstrContext ctx);

	T visitExpression(ArmAssemblerParser.ExpressionContext ctx);

	T visitRealNumber(ArmAssemblerParser.RealNumberContext ctx);

	T visitAddress(ArmAssemblerParser.AddressContext ctx);

	T visitBlInstr(ArmAssemblerParser.BlInstrContext ctx);

	T visitByteDirective(ArmAssemblerParser.ByteDirectiveContext ctx);

	T visitMvnInstr(ArmAssemblerParser.MvnInstrContext ctx);

	T visitWordDirective(ArmAssemblerParser.WordDirectiveContext ctx);

	T visitRValue(ArmAssemblerParser.RValueContext ctx);

	T visitMsrPrivInstr(ArmAssemblerParser.MsrPrivInstrContext ctx);

	T visitMovInstr(ArmAssemblerParser.MovInstrContext ctx);

	T visitOp2(ArmAssemblerParser.Op2Context ctx);

	T visitSingle(ArmAssemblerParser.SingleContext ctx);

	T visitInstruction(ArmAssemblerParser.InstructionContext ctx);

	T visitSwiInstr(ArmAssemblerParser.SwiInstrContext ctx);

	T visitRelational(ArmAssemblerParser.RelationalContext ctx);

	T visitStopInstr(ArmAssemblerParser.StopInstrContext ctx);

	T visitPreIndexedAddressing(ArmAssemblerParser.PreIndexedAddressingContext ctx);

	T visitAndExpr(ArmAssemblerParser.AndExprContext ctx);
}