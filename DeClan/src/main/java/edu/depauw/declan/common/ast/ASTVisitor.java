package edu.depauw.declan.common.ast;

/**
 * As part of the Visitor pattern, an ASTVisitor encapsulates an algorithm that
 * walks an abstract syntax tree. There is one overloaded version of the visit()
 * method for each type of ASTNode. The visitor is responsible for controlling
 * the traversal of the tree by calling .accept(this) on each subnode at the
 * appropriate time.
 * 
 * @author bhoward
 */
public interface ASTVisitor {
	void visit(Program program);

	// Declarations
	void visit(ConstDeclaration constDecl);

        void visit(VariableDeclaration varDecl);

        void visit(ProcedureDeclaration varDecl);

	// Statements
	void visit(ProcedureCall procedureCall);

	void visit(EmptyStatement emptyStatement);

        void visit(IfElifBranch ifStatement);
        
        void visit(ElseBranch ifStatement);

        void visit(WhileElifBranch whileStatement);

        void visit(ForBranch assignment);

        void visit(RepeatBranch RepeatStatement);

        void visit(Assignment assignment);

	// Expressions
	void visit(UnaryOperation unaryOperation);

	void visit(BinaryOperation binaryOperation);

	void visit(NumValue numValue);

        void visit(BoolValue boolValue);

        void visit(StrValue numValue);

	void visit(Identifier identifier);

        void visit(FunctionCall functionCall);

        void visit(ParamaterDeclaration declaration);

        void visit(Library library);

        void visit(Asm asm);
}
