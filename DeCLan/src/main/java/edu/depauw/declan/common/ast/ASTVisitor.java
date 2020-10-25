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

	// Statements
	void visit(ProcedureCall procedureCall);

	void visit(EmptyStatement emptyStatement);

        void visit(IfElifBranch ifStatement);
        
        void visit(ElseBranch ifStatement);

        void visit(WhileElifBranch ifStatement);

	// Expressions
	void visit(UnaryOperation unaryOperation);

	void visit(BinaryOperation binaryOperation);

        void visit(BooleanOperation booleanOperation);

	void visit(NumValue numValue);

	void visit(Identifier identifier);
    
        void visit(VariableDeclaration varDecl);

        void visit(Assignment assignment);
}
