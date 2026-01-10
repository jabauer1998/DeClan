// Generated from io\github\H20man13\DeClan\main\assembler\ArmAssembler.g4 by ANTLR 4.3
package io.github.H20man13.DeClan.main.assembler;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ArmAssemblerParser}.
 */
public interface ArmAssemblerListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#cmnInstr}.
	 * @param ctx the parse tree
	 */
	void enterCmnInstr(@NotNull ArmAssemblerParser.CmnInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#cmnInstr}.
	 * @param ctx the parse tree
	 */
	void exitCmnInstr(@NotNull ArmAssemblerParser.CmnInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#shift}.
	 * @param ctx the parse tree
	 */
	void enterShift(@NotNull ArmAssemblerParser.ShiftContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#shift}.
	 * @param ctx the parse tree
	 */
	void exitShift(@NotNull ArmAssemblerParser.ShiftContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#tstInstr}.
	 * @param ctx the parse tree
	 */
	void enterTstInstr(@NotNull ArmAssemblerParser.TstInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#tstInstr}.
	 * @param ctx the parse tree
	 */
	void exitTstInstr(@NotNull ArmAssemblerParser.TstInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(@NotNull ArmAssemblerParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(@NotNull ArmAssemblerParser.ProgramContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnary(@NotNull ArmAssemblerParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnary(@NotNull ArmAssemblerParser.UnaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rsbInstr}.
	 * @param ctx the parse tree
	 */
	void enterRsbInstr(@NotNull ArmAssemblerParser.RsbInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rsbInstr}.
	 * @param ctx the parse tree
	 */
	void exitRsbInstr(@NotNull ArmAssemblerParser.RsbInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#ldrDefInstr}.
	 * @param ctx the parse tree
	 */
	void enterLdrDefInstr(@NotNull ArmAssemblerParser.LdrDefInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#ldrDefInstr}.
	 * @param ctx the parse tree
	 */
	void exitLdrDefInstr(@NotNull ArmAssemblerParser.LdrDefInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rscInstr}.
	 * @param ctx the parse tree
	 */
	void enterRscInstr(@NotNull ArmAssemblerParser.RscInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rscInstr}.
	 * @param ctx the parse tree
	 */
	void exitRscInstr(@NotNull ArmAssemblerParser.RscInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(@NotNull ArmAssemblerParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(@NotNull ArmAssemblerParser.NumberContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#shiftName}.
	 * @param ctx the parse tree
	 */
	void enterShiftName(@NotNull ArmAssemblerParser.ShiftNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#shiftName}.
	 * @param ctx the parse tree
	 */
	void exitShiftName(@NotNull ArmAssemblerParser.ShiftNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#stmInstr}.
	 * @param ctx the parse tree
	 */
	void enterStmInstr(@NotNull ArmAssemblerParser.StmInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#stmInstr}.
	 * @param ctx the parse tree
	 */
	void exitStmInstr(@NotNull ArmAssemblerParser.StmInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mulInstr}.
	 * @param ctx the parse tree
	 */
	void enterMulInstr(@NotNull ArmAssemblerParser.MulInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mulInstr}.
	 * @param ctx the parse tree
	 */
	void exitMulInstr(@NotNull ArmAssemblerParser.MulInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mullInstr}.
	 * @param ctx the parse tree
	 */
	void enterMullInstr(@NotNull ArmAssemblerParser.MullInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mullInstr}.
	 * @param ctx the parse tree
	 */
	void exitMullInstr(@NotNull ArmAssemblerParser.MullInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bxInstr}.
	 * @param ctx the parse tree
	 */
	void enterBxInstr(@NotNull ArmAssemblerParser.BxInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bxInstr}.
	 * @param ctx the parse tree
	 */
	void exitBxInstr(@NotNull ArmAssemblerParser.BxInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bicInstr}.
	 * @param ctx the parse tree
	 */
	void enterBicInstr(@NotNull ArmAssemblerParser.BicInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bicInstr}.
	 * @param ctx the parse tree
	 */
	void exitBicInstr(@NotNull ArmAssemblerParser.BicInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(@NotNull ArmAssemblerParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(@NotNull ArmAssemblerParser.IdentifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#sbcInstr}.
	 * @param ctx the parse tree
	 */
	void enterSbcInstr(@NotNull ArmAssemblerParser.SbcInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#sbcInstr}.
	 * @param ctx the parse tree
	 */
	void exitSbcInstr(@NotNull ArmAssemblerParser.SbcInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#eorInstr}.
	 * @param ctx the parse tree
	 */
	void enterEorInstr(@NotNull ArmAssemblerParser.EorInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#eorInstr}.
	 * @param ctx the parse tree
	 */
	void exitEorInstr(@NotNull ArmAssemblerParser.EorInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#cmpInstr}.
	 * @param ctx the parse tree
	 */
	void enterCmpInstr(@NotNull ArmAssemblerParser.CmpInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#cmpInstr}.
	 * @param ctx the parse tree
	 */
	void exitCmpInstr(@NotNull ArmAssemblerParser.CmpInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(@NotNull ArmAssemblerParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(@NotNull ArmAssemblerParser.PrimaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mrsInstr}.
	 * @param ctx the parse tree
	 */
	void enterMrsInstr(@NotNull ArmAssemblerParser.MrsInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mrsInstr}.
	 * @param ctx the parse tree
	 */
	void exitMrsInstr(@NotNull ArmAssemblerParser.MrsInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#swpInstr}.
	 * @param ctx the parse tree
	 */
	void enterSwpInstr(@NotNull ArmAssemblerParser.SwpInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#swpInstr}.
	 * @param ctx the parse tree
	 */
	void exitSwpInstr(@NotNull ArmAssemblerParser.SwpInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#psrf}.
	 * @param ctx the parse tree
	 */
	void enterPsrf(@NotNull ArmAssemblerParser.PsrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#psrf}.
	 * @param ctx the parse tree
	 */
	void exitPsrf(@NotNull ArmAssemblerParser.PsrfContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#adcInstr}.
	 * @param ctx the parse tree
	 */
	void enterAdcInstr(@NotNull ArmAssemblerParser.AdcInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#adcInstr}.
	 * @param ctx the parse tree
	 */
	void exitAdcInstr(@NotNull ArmAssemblerParser.AdcInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#teqInstr}.
	 * @param ctx the parse tree
	 */
	void enterTeqInstr(@NotNull ArmAssemblerParser.TeqInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#teqInstr}.
	 * @param ctx the parse tree
	 */
	void exitTeqInstr(@NotNull ArmAssemblerParser.TeqInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#poundExpression}.
	 * @param ctx the parse tree
	 */
	void enterPoundExpression(@NotNull ArmAssemblerParser.PoundExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#poundExpression}.
	 * @param ctx the parse tree
	 */
	void exitPoundExpression(@NotNull ArmAssemblerParser.PoundExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#andInstr}.
	 * @param ctx the parse tree
	 */
	void enterAndInstr(@NotNull ArmAssemblerParser.AndInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#andInstr}.
	 * @param ctx the parse tree
	 */
	void exitAndInstr(@NotNull ArmAssemblerParser.AndInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#ldrSignedInstr}.
	 * @param ctx the parse tree
	 */
	void enterLdrSignedInstr(@NotNull ArmAssemblerParser.LdrSignedInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#ldrSignedInstr}.
	 * @param ctx the parse tree
	 */
	void exitLdrSignedInstr(@NotNull ArmAssemblerParser.LdrSignedInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#psr}.
	 * @param ctx the parse tree
	 */
	void enterPsr(@NotNull ArmAssemblerParser.PsrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#psr}.
	 * @param ctx the parse tree
	 */
	void exitPsr(@NotNull ArmAssemblerParser.PsrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#msrDefInstr}.
	 * @param ctx the parse tree
	 */
	void enterMsrDefInstr(@NotNull ArmAssemblerParser.MsrDefInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#msrDefInstr}.
	 * @param ctx the parse tree
	 */
	void exitMsrDefInstr(@NotNull ArmAssemblerParser.MsrDefInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#ldmInstr}.
	 * @param ctx the parse tree
	 */
	void enterLdmInstr(@NotNull ArmAssemblerParser.LdmInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#ldmInstr}.
	 * @param ctx the parse tree
	 */
	void exitLdmInstr(@NotNull ArmAssemblerParser.LdmInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#strDefInstr}.
	 * @param ctx the parse tree
	 */
	void enterStrDefInstr(@NotNull ArmAssemblerParser.StrDefInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#strDefInstr}.
	 * @param ctx the parse tree
	 */
	void exitStrDefInstr(@NotNull ArmAssemblerParser.StrDefInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#orrInstr}.
	 * @param ctx the parse tree
	 */
	void enterOrrInstr(@NotNull ArmAssemblerParser.OrrInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#orrInstr}.
	 * @param ctx the parse tree
	 */
	void exitOrrInstr(@NotNull ArmAssemblerParser.OrrInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mlaInstr}.
	 * @param ctx the parse tree
	 */
	void enterMlaInstr(@NotNull ArmAssemblerParser.MlaInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mlaInstr}.
	 * @param ctx the parse tree
	 */
	void exitMlaInstr(@NotNull ArmAssemblerParser.MlaInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#addInstr}.
	 * @param ctx the parse tree
	 */
	void enterAddInstr(@NotNull ArmAssemblerParser.AddInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#addInstr}.
	 * @param ctx the parse tree
	 */
	void exitAddInstr(@NotNull ArmAssemblerParser.AddInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bitwise}.
	 * @param ctx the parse tree
	 */
	void enterBitwise(@NotNull ArmAssemblerParser.BitwiseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bitwise}.
	 * @param ctx the parse tree
	 */
	void exitBitwise(@NotNull ArmAssemblerParser.BitwiseContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#postIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void enterPostIndexedAddressing(@NotNull ArmAssemblerParser.PostIndexedAddressingContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#postIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void exitPostIndexedAddressing(@NotNull ArmAssemblerParser.PostIndexedAddressingContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bInstr}.
	 * @param ctx the parse tree
	 */
	void enterBInstr(@NotNull ArmAssemblerParser.BInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bInstr}.
	 * @param ctx the parse tree
	 */
	void exitBInstr(@NotNull ArmAssemblerParser.BInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rList}.
	 * @param ctx the parse tree
	 */
	void enterRList(@NotNull ArmAssemblerParser.RListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rList}.
	 * @param ctx the parse tree
	 */
	void exitRList(@NotNull ArmAssemblerParser.RListContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(@NotNull ArmAssemblerParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(@NotNull ArmAssemblerParser.TermContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#subInstr}.
	 * @param ctx the parse tree
	 */
	void enterSubInstr(@NotNull ArmAssemblerParser.SubInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#subInstr}.
	 * @param ctx the parse tree
	 */
	void exitSubInstr(@NotNull ArmAssemblerParser.SubInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mlalInstr}.
	 * @param ctx the parse tree
	 */
	void enterMlalInstr(@NotNull ArmAssemblerParser.MlalInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mlalInstr}.
	 * @param ctx the parse tree
	 */
	void exitMlalInstr(@NotNull ArmAssemblerParser.MlalInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#instructionOrDirective}.
	 * @param ctx the parse tree
	 */
	void enterInstructionOrDirective(@NotNull ArmAssemblerParser.InstructionOrDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#instructionOrDirective}.
	 * @param ctx the parse tree
	 */
	void exitInstructionOrDirective(@NotNull ArmAssemblerParser.InstructionOrDirectiveContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#strSignedInstr}.
	 * @param ctx the parse tree
	 */
	void enterStrSignedInstr(@NotNull ArmAssemblerParser.StrSignedInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#strSignedInstr}.
	 * @param ctx the parse tree
	 */
	void exitStrSignedInstr(@NotNull ArmAssemblerParser.StrSignedInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(@NotNull ArmAssemblerParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(@NotNull ArmAssemblerParser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#realNumber}.
	 * @param ctx the parse tree
	 */
	void enterRealNumber(@NotNull ArmAssemblerParser.RealNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#realNumber}.
	 * @param ctx the parse tree
	 */
	void exitRealNumber(@NotNull ArmAssemblerParser.RealNumberContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#address}.
	 * @param ctx the parse tree
	 */
	void enterAddress(@NotNull ArmAssemblerParser.AddressContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#address}.
	 * @param ctx the parse tree
	 */
	void exitAddress(@NotNull ArmAssemblerParser.AddressContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#blInstr}.
	 * @param ctx the parse tree
	 */
	void enterBlInstr(@NotNull ArmAssemblerParser.BlInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#blInstr}.
	 * @param ctx the parse tree
	 */
	void exitBlInstr(@NotNull ArmAssemblerParser.BlInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#byteDirective}.
	 * @param ctx the parse tree
	 */
	void enterByteDirective(@NotNull ArmAssemblerParser.ByteDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#byteDirective}.
	 * @param ctx the parse tree
	 */
	void exitByteDirective(@NotNull ArmAssemblerParser.ByteDirectiveContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mvnInstr}.
	 * @param ctx the parse tree
	 */
	void enterMvnInstr(@NotNull ArmAssemblerParser.MvnInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mvnInstr}.
	 * @param ctx the parse tree
	 */
	void exitMvnInstr(@NotNull ArmAssemblerParser.MvnInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#wordDirective}.
	 * @param ctx the parse tree
	 */
	void enterWordDirective(@NotNull ArmAssemblerParser.WordDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#wordDirective}.
	 * @param ctx the parse tree
	 */
	void exitWordDirective(@NotNull ArmAssemblerParser.WordDirectiveContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rValue}.
	 * @param ctx the parse tree
	 */
	void enterRValue(@NotNull ArmAssemblerParser.RValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rValue}.
	 * @param ctx the parse tree
	 */
	void exitRValue(@NotNull ArmAssemblerParser.RValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#msrPrivInstr}.
	 * @param ctx the parse tree
	 */
	void enterMsrPrivInstr(@NotNull ArmAssemblerParser.MsrPrivInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#msrPrivInstr}.
	 * @param ctx the parse tree
	 */
	void exitMsrPrivInstr(@NotNull ArmAssemblerParser.MsrPrivInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#movInstr}.
	 * @param ctx the parse tree
	 */
	void enterMovInstr(@NotNull ArmAssemblerParser.MovInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#movInstr}.
	 * @param ctx the parse tree
	 */
	void exitMovInstr(@NotNull ArmAssemblerParser.MovInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#op2}.
	 * @param ctx the parse tree
	 */
	void enterOp2(@NotNull ArmAssemblerParser.Op2Context ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#op2}.
	 * @param ctx the parse tree
	 */
	void exitOp2(@NotNull ArmAssemblerParser.Op2Context ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#single}.
	 * @param ctx the parse tree
	 */
	void enterSingle(@NotNull ArmAssemblerParser.SingleContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#single}.
	 * @param ctx the parse tree
	 */
	void exitSingle(@NotNull ArmAssemblerParser.SingleContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#instruction}.
	 * @param ctx the parse tree
	 */
	void enterInstruction(@NotNull ArmAssemblerParser.InstructionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#instruction}.
	 * @param ctx the parse tree
	 */
	void exitInstruction(@NotNull ArmAssemblerParser.InstructionContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#swiInstr}.
	 * @param ctx the parse tree
	 */
	void enterSwiInstr(@NotNull ArmAssemblerParser.SwiInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#swiInstr}.
	 * @param ctx the parse tree
	 */
	void exitSwiInstr(@NotNull ArmAssemblerParser.SwiInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#relational}.
	 * @param ctx the parse tree
	 */
	void enterRelational(@NotNull ArmAssemblerParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#relational}.
	 * @param ctx the parse tree
	 */
	void exitRelational(@NotNull ArmAssemblerParser.RelationalContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#stopInstr}.
	 * @param ctx the parse tree
	 */
	void enterStopInstr(@NotNull ArmAssemblerParser.StopInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#stopInstr}.
	 * @param ctx the parse tree
	 */
	void exitStopInstr(@NotNull ArmAssemblerParser.StopInstrContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#preIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void enterPreIndexedAddressing(@NotNull ArmAssemblerParser.PreIndexedAddressingContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#preIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void exitPreIndexedAddressing(@NotNull ArmAssemblerParser.PreIndexedAddressingContext ctx);

	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#andExpr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(@NotNull ArmAssemblerParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#andExpr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(@NotNull ArmAssemblerParser.AndExprContext ctx);
}