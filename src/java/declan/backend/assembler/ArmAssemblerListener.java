// Generated from src/java/declan/backend/assembler/ArmAssembler.g4 by ANTLR 4.13.2
package declan.backend.assembler;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ArmAssemblerParser}.
 */
public interface ArmAssemblerListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(ArmAssemblerParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(ArmAssemblerParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#instructionOrDirective}.
	 * @param ctx the parse tree
	 */
	void enterInstructionOrDirective(ArmAssemblerParser.InstructionOrDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#instructionOrDirective}.
	 * @param ctx the parse tree
	 */
	void exitInstructionOrDirective(ArmAssemblerParser.InstructionOrDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#instruction}.
	 * @param ctx the parse tree
	 */
	void enterInstruction(ArmAssemblerParser.InstructionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#instruction}.
	 * @param ctx the parse tree
	 */
	void exitInstruction(ArmAssemblerParser.InstructionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#wordDirective}.
	 * @param ctx the parse tree
	 */
	void enterWordDirective(ArmAssemblerParser.WordDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#wordDirective}.
	 * @param ctx the parse tree
	 */
	void exitWordDirective(ArmAssemblerParser.WordDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#byteDirective}.
	 * @param ctx the parse tree
	 */
	void enterByteDirective(ArmAssemblerParser.ByteDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#byteDirective}.
	 * @param ctx the parse tree
	 */
	void exitByteDirective(ArmAssemblerParser.ByteDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bInstr}.
	 * @param ctx the parse tree
	 */
	void enterBInstr(ArmAssemblerParser.BInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bInstr}.
	 * @param ctx the parse tree
	 */
	void exitBInstr(ArmAssemblerParser.BInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#blInstr}.
	 * @param ctx the parse tree
	 */
	void enterBlInstr(ArmAssemblerParser.BlInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#blInstr}.
	 * @param ctx the parse tree
	 */
	void exitBlInstr(ArmAssemblerParser.BlInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bxInstr}.
	 * @param ctx the parse tree
	 */
	void enterBxInstr(ArmAssemblerParser.BxInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bxInstr}.
	 * @param ctx the parse tree
	 */
	void exitBxInstr(ArmAssemblerParser.BxInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#ldmInstr}.
	 * @param ctx the parse tree
	 */
	void enterLdmInstr(ArmAssemblerParser.LdmInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#ldmInstr}.
	 * @param ctx the parse tree
	 */
	void exitLdmInstr(ArmAssemblerParser.LdmInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#ldrSignedInstr}.
	 * @param ctx the parse tree
	 */
	void enterLdrSignedInstr(ArmAssemblerParser.LdrSignedInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#ldrSignedInstr}.
	 * @param ctx the parse tree
	 */
	void exitLdrSignedInstr(ArmAssemblerParser.LdrSignedInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#ldrDefInstr}.
	 * @param ctx the parse tree
	 */
	void enterLdrDefInstr(ArmAssemblerParser.LdrDefInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#ldrDefInstr}.
	 * @param ctx the parse tree
	 */
	void exitLdrDefInstr(ArmAssemblerParser.LdrDefInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mlaInstr}.
	 * @param ctx the parse tree
	 */
	void enterMlaInstr(ArmAssemblerParser.MlaInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mlaInstr}.
	 * @param ctx the parse tree
	 */
	void exitMlaInstr(ArmAssemblerParser.MlaInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mlalInstr}.
	 * @param ctx the parse tree
	 */
	void enterMlalInstr(ArmAssemblerParser.MlalInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mlalInstr}.
	 * @param ctx the parse tree
	 */
	void exitMlalInstr(ArmAssemblerParser.MlalInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mrsInstr}.
	 * @param ctx the parse tree
	 */
	void enterMrsInstr(ArmAssemblerParser.MrsInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mrsInstr}.
	 * @param ctx the parse tree
	 */
	void exitMrsInstr(ArmAssemblerParser.MrsInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#msrDefInstr}.
	 * @param ctx the parse tree
	 */
	void enterMsrDefInstr(ArmAssemblerParser.MsrDefInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#msrDefInstr}.
	 * @param ctx the parse tree
	 */
	void exitMsrDefInstr(ArmAssemblerParser.MsrDefInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#msrPrivInstr}.
	 * @param ctx the parse tree
	 */
	void enterMsrPrivInstr(ArmAssemblerParser.MsrPrivInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#msrPrivInstr}.
	 * @param ctx the parse tree
	 */
	void exitMsrPrivInstr(ArmAssemblerParser.MsrPrivInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mulInstr}.
	 * @param ctx the parse tree
	 */
	void enterMulInstr(ArmAssemblerParser.MulInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mulInstr}.
	 * @param ctx the parse tree
	 */
	void exitMulInstr(ArmAssemblerParser.MulInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mullInstr}.
	 * @param ctx the parse tree
	 */
	void enterMullInstr(ArmAssemblerParser.MullInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mullInstr}.
	 * @param ctx the parse tree
	 */
	void exitMullInstr(ArmAssemblerParser.MullInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#stmInstr}.
	 * @param ctx the parse tree
	 */
	void enterStmInstr(ArmAssemblerParser.StmInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#stmInstr}.
	 * @param ctx the parse tree
	 */
	void exitStmInstr(ArmAssemblerParser.StmInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#strSignedInstr}.
	 * @param ctx the parse tree
	 */
	void enterStrSignedInstr(ArmAssemblerParser.StrSignedInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#strSignedInstr}.
	 * @param ctx the parse tree
	 */
	void exitStrSignedInstr(ArmAssemblerParser.StrSignedInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#strDefInstr}.
	 * @param ctx the parse tree
	 */
	void enterStrDefInstr(ArmAssemblerParser.StrDefInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#strDefInstr}.
	 * @param ctx the parse tree
	 */
	void exitStrDefInstr(ArmAssemblerParser.StrDefInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#swiInstr}.
	 * @param ctx the parse tree
	 */
	void enterSwiInstr(ArmAssemblerParser.SwiInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#swiInstr}.
	 * @param ctx the parse tree
	 */
	void exitSwiInstr(ArmAssemblerParser.SwiInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#swpInstr}.
	 * @param ctx the parse tree
	 */
	void enterSwpInstr(ArmAssemblerParser.SwpInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#swpInstr}.
	 * @param ctx the parse tree
	 */
	void exitSwpInstr(ArmAssemblerParser.SwpInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#addInstr}.
	 * @param ctx the parse tree
	 */
	void enterAddInstr(ArmAssemblerParser.AddInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#addInstr}.
	 * @param ctx the parse tree
	 */
	void exitAddInstr(ArmAssemblerParser.AddInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#andInstr}.
	 * @param ctx the parse tree
	 */
	void enterAndInstr(ArmAssemblerParser.AndInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#andInstr}.
	 * @param ctx the parse tree
	 */
	void exitAndInstr(ArmAssemblerParser.AndInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#eorInstr}.
	 * @param ctx the parse tree
	 */
	void enterEorInstr(ArmAssemblerParser.EorInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#eorInstr}.
	 * @param ctx the parse tree
	 */
	void exitEorInstr(ArmAssemblerParser.EorInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#subInstr}.
	 * @param ctx the parse tree
	 */
	void enterSubInstr(ArmAssemblerParser.SubInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#subInstr}.
	 * @param ctx the parse tree
	 */
	void exitSubInstr(ArmAssemblerParser.SubInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rsbInstr}.
	 * @param ctx the parse tree
	 */
	void enterRsbInstr(ArmAssemblerParser.RsbInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rsbInstr}.
	 * @param ctx the parse tree
	 */
	void exitRsbInstr(ArmAssemblerParser.RsbInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#adcInstr}.
	 * @param ctx the parse tree
	 */
	void enterAdcInstr(ArmAssemblerParser.AdcInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#adcInstr}.
	 * @param ctx the parse tree
	 */
	void exitAdcInstr(ArmAssemblerParser.AdcInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#sbcInstr}.
	 * @param ctx the parse tree
	 */
	void enterSbcInstr(ArmAssemblerParser.SbcInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#sbcInstr}.
	 * @param ctx the parse tree
	 */
	void exitSbcInstr(ArmAssemblerParser.SbcInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rscInstr}.
	 * @param ctx the parse tree
	 */
	void enterRscInstr(ArmAssemblerParser.RscInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rscInstr}.
	 * @param ctx the parse tree
	 */
	void exitRscInstr(ArmAssemblerParser.RscInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#orrInstr}.
	 * @param ctx the parse tree
	 */
	void enterOrrInstr(ArmAssemblerParser.OrrInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#orrInstr}.
	 * @param ctx the parse tree
	 */
	void exitOrrInstr(ArmAssemblerParser.OrrInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bicInstr}.
	 * @param ctx the parse tree
	 */
	void enterBicInstr(ArmAssemblerParser.BicInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bicInstr}.
	 * @param ctx the parse tree
	 */
	void exitBicInstr(ArmAssemblerParser.BicInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#tstInstr}.
	 * @param ctx the parse tree
	 */
	void enterTstInstr(ArmAssemblerParser.TstInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#tstInstr}.
	 * @param ctx the parse tree
	 */
	void exitTstInstr(ArmAssemblerParser.TstInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#teqInstr}.
	 * @param ctx the parse tree
	 */
	void enterTeqInstr(ArmAssemblerParser.TeqInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#teqInstr}.
	 * @param ctx the parse tree
	 */
	void exitTeqInstr(ArmAssemblerParser.TeqInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#cmpInstr}.
	 * @param ctx the parse tree
	 */
	void enterCmpInstr(ArmAssemblerParser.CmpInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#cmpInstr}.
	 * @param ctx the parse tree
	 */
	void exitCmpInstr(ArmAssemblerParser.CmpInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#cmnInstr}.
	 * @param ctx the parse tree
	 */
	void enterCmnInstr(ArmAssemblerParser.CmnInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#cmnInstr}.
	 * @param ctx the parse tree
	 */
	void exitCmnInstr(ArmAssemblerParser.CmnInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#movInstr}.
	 * @param ctx the parse tree
	 */
	void enterMovInstr(ArmAssemblerParser.MovInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#movInstr}.
	 * @param ctx the parse tree
	 */
	void exitMovInstr(ArmAssemblerParser.MovInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#mvnInstr}.
	 * @param ctx the parse tree
	 */
	void enterMvnInstr(ArmAssemblerParser.MvnInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#mvnInstr}.
	 * @param ctx the parse tree
	 */
	void exitMvnInstr(ArmAssemblerParser.MvnInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#stopInstr}.
	 * @param ctx the parse tree
	 */
	void enterStopInstr(ArmAssemblerParser.StopInstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#stopInstr}.
	 * @param ctx the parse tree
	 */
	void exitStopInstr(ArmAssemblerParser.StopInstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#op2}.
	 * @param ctx the parse tree
	 */
	void enterOp2(ArmAssemblerParser.Op2Context ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#op2}.
	 * @param ctx the parse tree
	 */
	void exitOp2(ArmAssemblerParser.Op2Context ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#shift}.
	 * @param ctx the parse tree
	 */
	void enterShift(ArmAssemblerParser.ShiftContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#shift}.
	 * @param ctx the parse tree
	 */
	void exitShift(ArmAssemblerParser.ShiftContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rList}.
	 * @param ctx the parse tree
	 */
	void enterRList(ArmAssemblerParser.RListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rList}.
	 * @param ctx the parse tree
	 */
	void exitRList(ArmAssemblerParser.RListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#rValue}.
	 * @param ctx the parse tree
	 */
	void enterRValue(ArmAssemblerParser.RValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#rValue}.
	 * @param ctx the parse tree
	 */
	void exitRValue(ArmAssemblerParser.RValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#poundExpression}.
	 * @param ctx the parse tree
	 */
	void enterPoundExpression(ArmAssemblerParser.PoundExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#poundExpression}.
	 * @param ctx the parse tree
	 */
	void exitPoundExpression(ArmAssemblerParser.PoundExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ArmAssemblerParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ArmAssemblerParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#andExpr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(ArmAssemblerParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#andExpr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(ArmAssemblerParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#relational}.
	 * @param ctx the parse tree
	 */
	void enterRelational(ArmAssemblerParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#relational}.
	 * @param ctx the parse tree
	 */
	void exitRelational(ArmAssemblerParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(ArmAssemblerParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(ArmAssemblerParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#bitwise}.
	 * @param ctx the parse tree
	 */
	void enterBitwise(ArmAssemblerParser.BitwiseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#bitwise}.
	 * @param ctx the parse tree
	 */
	void exitBitwise(ArmAssemblerParser.BitwiseContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(ArmAssemblerParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(ArmAssemblerParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnary(ArmAssemblerParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnary(ArmAssemblerParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#single}.
	 * @param ctx the parse tree
	 */
	void enterSingle(ArmAssemblerParser.SingleContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#single}.
	 * @param ctx the parse tree
	 */
	void exitSingle(ArmAssemblerParser.SingleContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(ArmAssemblerParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(ArmAssemblerParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#realNumber}.
	 * @param ctx the parse tree
	 */
	void enterRealNumber(ArmAssemblerParser.RealNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#realNumber}.
	 * @param ctx the parse tree
	 */
	void exitRealNumber(ArmAssemblerParser.RealNumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(ArmAssemblerParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(ArmAssemblerParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#address}.
	 * @param ctx the parse tree
	 */
	void enterAddress(ArmAssemblerParser.AddressContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#address}.
	 * @param ctx the parse tree
	 */
	void exitAddress(ArmAssemblerParser.AddressContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#preIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void enterPreIndexedAddressing(ArmAssemblerParser.PreIndexedAddressingContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#preIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void exitPreIndexedAddressing(ArmAssemblerParser.PreIndexedAddressingContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#postIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void enterPostIndexedAddressing(ArmAssemblerParser.PostIndexedAddressingContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#postIndexedAddressing}.
	 * @param ctx the parse tree
	 */
	void exitPostIndexedAddressing(ArmAssemblerParser.PostIndexedAddressingContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#shiftName}.
	 * @param ctx the parse tree
	 */
	void enterShiftName(ArmAssemblerParser.ShiftNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#shiftName}.
	 * @param ctx the parse tree
	 */
	void exitShiftName(ArmAssemblerParser.ShiftNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#psr}.
	 * @param ctx the parse tree
	 */
	void enterPsr(ArmAssemblerParser.PsrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#psr}.
	 * @param ctx the parse tree
	 */
	void exitPsr(ArmAssemblerParser.PsrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArmAssemblerParser#psrf}.
	 * @param ctx the parse tree
	 */
	void enterPsrf(ArmAssemblerParser.PsrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArmAssemblerParser#psrf}.
	 * @param ctx the parse tree
	 */
	void exitPsrf(ArmAssemblerParser.PsrfContext ctx);
}