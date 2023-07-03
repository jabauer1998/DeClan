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
}
