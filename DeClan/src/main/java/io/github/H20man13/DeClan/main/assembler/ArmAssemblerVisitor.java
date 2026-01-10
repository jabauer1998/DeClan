// Generated from io\github\H20man13\DeClan\main\assembler\ArmAssembler.g4 by ANTLR 4.3
package io.github.H20man13.DeClan.main.assembler;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ArmAssemblerParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ArmAssemblerVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#cmnInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmnInstr(@NotNull ArmAssemblerParser.CmnInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#shift}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShift(@NotNull ArmAssemblerParser.ShiftContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#tstInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTstInstr(@NotNull ArmAssemblerParser.TstInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(@NotNull ArmAssemblerParser.ProgramContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(@NotNull ArmAssemblerParser.UnaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rsbInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRsbInstr(@NotNull ArmAssemblerParser.RsbInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#ldrDefInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLdrDefInstr(@NotNull ArmAssemblerParser.LdrDefInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rscInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRscInstr(@NotNull ArmAssemblerParser.RscInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(@NotNull ArmAssemblerParser.NumberContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#shiftName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftName(@NotNull ArmAssemblerParser.ShiftNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#stmInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmInstr(@NotNull ArmAssemblerParser.StmInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mulInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulInstr(@NotNull ArmAssemblerParser.MulInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mullInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMullInstr(@NotNull ArmAssemblerParser.MullInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bxInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBxInstr(@NotNull ArmAssemblerParser.BxInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bicInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBicInstr(@NotNull ArmAssemblerParser.BicInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(@NotNull ArmAssemblerParser.IdentifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#sbcInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSbcInstr(@NotNull ArmAssemblerParser.SbcInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#eorInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEorInstr(@NotNull ArmAssemblerParser.EorInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#cmpInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmpInstr(@NotNull ArmAssemblerParser.CmpInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(@NotNull ArmAssemblerParser.PrimaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mrsInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMrsInstr(@NotNull ArmAssemblerParser.MrsInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#swpInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwpInstr(@NotNull ArmAssemblerParser.SwpInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#psrf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPsrf(@NotNull ArmAssemblerParser.PsrfContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#adcInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdcInstr(@NotNull ArmAssemblerParser.AdcInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#teqInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTeqInstr(@NotNull ArmAssemblerParser.TeqInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#poundExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPoundExpression(@NotNull ArmAssemblerParser.PoundExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#andInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndInstr(@NotNull ArmAssemblerParser.AndInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#ldrSignedInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLdrSignedInstr(@NotNull ArmAssemblerParser.LdrSignedInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#psr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPsr(@NotNull ArmAssemblerParser.PsrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#msrDefInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsrDefInstr(@NotNull ArmAssemblerParser.MsrDefInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#ldmInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLdmInstr(@NotNull ArmAssemblerParser.LdmInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#strDefInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrDefInstr(@NotNull ArmAssemblerParser.StrDefInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#orrInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrrInstr(@NotNull ArmAssemblerParser.OrrInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mlaInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMlaInstr(@NotNull ArmAssemblerParser.MlaInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#addInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddInstr(@NotNull ArmAssemblerParser.AddInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bitwise}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitwise(@NotNull ArmAssemblerParser.BitwiseContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#postIndexedAddressing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostIndexedAddressing(@NotNull ArmAssemblerParser.PostIndexedAddressingContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBInstr(@NotNull ArmAssemblerParser.BInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRList(@NotNull ArmAssemblerParser.RListContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(@NotNull ArmAssemblerParser.TermContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#subInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubInstr(@NotNull ArmAssemblerParser.SubInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mlalInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMlalInstr(@NotNull ArmAssemblerParser.MlalInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#instructionOrDirective}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstructionOrDirective(@NotNull ArmAssemblerParser.InstructionOrDirectiveContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#strSignedInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrSignedInstr(@NotNull ArmAssemblerParser.StrSignedInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(@NotNull ArmAssemblerParser.ExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#realNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealNumber(@NotNull ArmAssemblerParser.RealNumberContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#address}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddress(@NotNull ArmAssemblerParser.AddressContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#blInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlInstr(@NotNull ArmAssemblerParser.BlInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#byteDirective}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitByteDirective(@NotNull ArmAssemblerParser.ByteDirectiveContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mvnInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMvnInstr(@NotNull ArmAssemblerParser.MvnInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#wordDirective}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWordDirective(@NotNull ArmAssemblerParser.WordDirectiveContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRValue(@NotNull ArmAssemblerParser.RValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#msrPrivInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsrPrivInstr(@NotNull ArmAssemblerParser.MsrPrivInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#movInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMovInstr(@NotNull ArmAssemblerParser.MovInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#op2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp2(@NotNull ArmAssemblerParser.Op2Context ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#single}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle(@NotNull ArmAssemblerParser.SingleContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#instruction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstruction(@NotNull ArmAssemblerParser.InstructionContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#swiInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwiInstr(@NotNull ArmAssemblerParser.SwiInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#relational}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(@NotNull ArmAssemblerParser.RelationalContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#stopInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStopInstr(@NotNull ArmAssemblerParser.StopInstrContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#preIndexedAddressing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreIndexedAddressing(@NotNull ArmAssemblerParser.PreIndexedAddressingContext ctx);

	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#andExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(@NotNull ArmAssemblerParser.AndExprContext ctx);
}