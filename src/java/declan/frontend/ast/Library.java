package io.github.h20man13.DeClan.common.ast;

import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.position.Position;

public class Library extends AbstractASTNode{
    private final List<ConstDeclaration> constDecls;
    private final List<ProcedureDeclaration> procDecls;

    public Library(Position start, List<ConstDeclaration> constDecls, List<VariableDeclaration> varDecls, List<ProcedureDeclaration> procDecls){
        super(start);
        this.constDecls = constDecls;
        this.procDecls = procDecls;
    }

    public Library(Position start, List<Declaration> declarations){
        super(start);

        this.constDecls = new LinkedList<ConstDeclaration>();
        this.procDecls = new LinkedList<ProcedureDeclaration>();

        for(Declaration decl : declarations){
            if(decl instanceof ConstDeclaration){
                this.constDecls.add((ConstDeclaration)decl);
            } else if(decl instanceof ProcedureDeclaration){
                this.procDecls.add((ProcedureDeclaration)decl);
            }
        }
    }

    public List<ConstDeclaration> getConstDecls(){
        return this.constDecls;
    }

    public List<ProcedureDeclaration> getProcDecls(){
        return this.procDecls;
    }

    public List<Declaration> getDecls(){
        List<Declaration> result = new LinkedList<Declaration>();
        result.addAll(this.constDecls);
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
