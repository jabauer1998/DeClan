package edu.depauw.declan.common.ast;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.Position;

public class Library extends AbstractASTNode{
    private final List<Declaration> constDecls;
    private final List<Declaration> varDecls;
    private final List<Declaration> procDecls;

    public Library(Position start, List<Declaration> constDecls, List<Declaration> varDecls, List<Declaration> procDecls){
        super(start);
        this.constDecls = constDecls;
        this.varDecls = varDecls;
        this.procDecls = procDecls;
    }

    public Library(Position start, List<Declaration> declarations){
        super(start);

        this.constDecls = new LinkedList<Declaration>();
        this.varDecls = new LinkedList<Declaration>();
        this.procDecls = new LinkedList<Declaration>();

        for(Declaration decl : declarations){
            if(decl instanceof ConstDeclaration){
                this.constDecls.add(decl);
            } else if(decl instanceof ProcedureDeclaration){
                this.procDecls.add(decl);
            } else if(decl instanceof VariableDeclaration){
                this.varDecls.add(decl);
            }
        }
    }

    public List<Declaration> getConstDecls(){
        return this.constDecls;
    }

    public List<Declaration> getVarDecls(){
        return this.varDecls;
    }

    public List<Declaration> getProcDecls(){
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
