// Generated from src/java/declan/backend/assembler/ArmAssembler.g4 by ANTLR 4.13.2
package declan.backend.assembler;
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
	 * Visit a parse tree produced by {@link ArmAssemblerParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(ArmAssemblerParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#instructionOrDirective}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstructionOrDirective(ArmAssemblerParser.InstructionOrDirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#instruction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstruction(ArmAssemblerParser.InstructionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#wordDirective}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWordDirective(ArmAssemblerParser.WordDirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#byteDirective}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitByteDirective(ArmAssemblerParser.ByteDirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBInstr(ArmAssemblerParser.BInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#blInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlInstr(ArmAssemblerParser.BlInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bxInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBxInstr(ArmAssemblerParser.BxInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#ldmInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLdmInstr(ArmAssemblerParser.LdmInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#ldrSignedInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLdrSignedInstr(ArmAssemblerParser.LdrSignedInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#ldrDefInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLdrDefInstr(ArmAssemblerParser.LdrDefInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mlaInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMlaInstr(ArmAssemblerParser.MlaInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mlalInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMlalInstr(ArmAssemblerParser.MlalInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mrsInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMrsInstr(ArmAssemblerParser.MrsInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#msrDefInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsrDefInstr(ArmAssemblerParser.MsrDefInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#msrPrivInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsrPrivInstr(ArmAssemblerParser.MsrPrivInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mulInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulInstr(ArmAssemblerParser.MulInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mullInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMullInstr(ArmAssemblerParser.MullInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#stmInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmInstr(ArmAssemblerParser.StmInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#strSignedInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrSignedInstr(ArmAssemblerParser.StrSignedInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#strDefInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrDefInstr(ArmAssemblerParser.StrDefInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#swiInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwiInstr(ArmAssemblerParser.SwiInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#swpInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwpInstr(ArmAssemblerParser.SwpInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#addInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddInstr(ArmAssemblerParser.AddInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#andInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndInstr(ArmAssemblerParser.AndInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#eorInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEorInstr(ArmAssemblerParser.EorInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#subInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubInstr(ArmAssemblerParser.SubInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rsbInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRsbInstr(ArmAssemblerParser.RsbInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#adcInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdcInstr(ArmAssemblerParser.AdcInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#sbcInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSbcInstr(ArmAssemblerParser.SbcInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rscInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRscInstr(ArmAssemblerParser.RscInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#orrInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrrInstr(ArmAssemblerParser.OrrInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bicInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBicInstr(ArmAssemblerParser.BicInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#tstInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTstInstr(ArmAssemblerParser.TstInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#teqInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTeqInstr(ArmAssemblerParser.TeqInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#cmpInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmpInstr(ArmAssemblerParser.CmpInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#cmnInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmnInstr(ArmAssemblerParser.CmnInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#movInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMovInstr(ArmAssemblerParser.MovInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#mvnInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMvnInstr(ArmAssemblerParser.MvnInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#stopInstr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStopInstr(ArmAssemblerParser.StopInstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#op2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp2(ArmAssemblerParser.Op2Context ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#shift}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShift(ArmAssemblerParser.ShiftContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRList(ArmAssemblerParser.RListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#rValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRValue(ArmAssemblerParser.RValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#poundExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPoundExpression(ArmAssemblerParser.PoundExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ArmAssemblerParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#andExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(ArmAssemblerParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#relational}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(ArmAssemblerParser.RelationalContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(ArmAssemblerParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#bitwise}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitwise(ArmAssemblerParser.BitwiseContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(ArmAssemblerParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(ArmAssemblerParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#single}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle(ArmAssemblerParser.SingleContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(ArmAssemblerParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#realNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealNumber(ArmAssemblerParser.RealNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(ArmAssemblerParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#address}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddress(ArmAssemblerParser.AddressContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#preIndexedAddressing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreIndexedAddressing(ArmAssemblerParser.PreIndexedAddressingContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#postIndexedAddressing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostIndexedAddressing(ArmAssemblerParser.PostIndexedAddressingContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#shiftName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftName(ArmAssemblerParser.ShiftNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#psr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPsr(ArmAssemblerParser.PsrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ArmAssemblerParser#psrf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPsrf(ArmAssemblerParser.PsrfContext ctx);
}