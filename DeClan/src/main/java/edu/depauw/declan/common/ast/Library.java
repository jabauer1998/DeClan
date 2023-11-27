package edu.depauw.declan.common.ast;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.Position;

public class Library extends AbstractASTNode{
    private final List<ConstDeclaration> constDecls;
    private final List<VariableDeclaration> varDecls;
    private final List<ProcedureDeclaration> procDecls;

    public Library(Position start, List<ConstDeclaration> constDecls, List<VariableDeclaration> varDecls, List<ProcedureDeclaration> procDecls){
        super(start);
        this.constDecls = constDecls;
        this.varDecls = varDecls;
        this.procDecls = procDecls;
    }

    public Library(Position start, List<Declaration> declarations){
        super(start);

        this.constDecls = new LinkedList<ConstDeclaration>();
        this.varDecls = new LinkedList<VariableDeclaration>();
        this.procDecls = new LinkedList<ProcedureDeclaration>();

        for(Declaration decl : declarations){
            if(decl instanceof ConstDeclaration){
                this.constDecls.add((ConstDeclaration)decl);
            } else if(decl instanceof ProcedureDeclaration){
                this.procDecls.add((ProcedureDeclaration)decl);
            } else if(decl instanceof VariableDeclaration){
                this.varDecls.add((VariableDeclaration)decl);
            }
        }
    }

    public List<ConstDeclaration> getConstDecls(){
        return this.constDecls;
    }

    public List<VariableDeclaration> getVarDecls(){
        return this.varDecls;
    }

    public List<ProcedureDeclaration> getProcDecls(){
        return this.procDecls;
    }

    public List<Declaration> getDecls(){
        List<Declaration> result = new LinkedList<Declaration>();
        result.addAll(this.constDecls);
        result.addAll(this.varDecls);
        result.addAll(this.procDecls);
        return result;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("CONST ");
        
        int constSize = constDecls.size();
        for(int i = 0; i < constSize; i++){
            Declaration constDecl = constDecls.get(i);
            sb.append(constDecl.toString());
            if(i < constSize - 1){
                sb.append("; ");
            }
        }
        sb.append(";\nVAR ");

        int varSize = varDecls.size();
        for(int i = 0; i < varSize; i++){
            Declaration varDecl = varDecls.get(i);
            sb.append(varDecl.toString());
            if(i < varSize - 1){
                sb.append("; ");
            }
        }
        sb.append(";\n");

        int procSize = procDecls.size();
        for(int i = 0; i < procSize; i++){
            Declaration procDecl = procDecls.get(i);
            sb.append(procDecl.toString());
            sb.append("\n");
        }

        return sb.toString();
    }
}
