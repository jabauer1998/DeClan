package edu.depauw.declan.common.ast;

import java.util.List;

public abstract class IfElseBlock{
  private final List <Statement> doIfTrue;
  
  public IfElseBlock(List<Statement> doIfTrue){
    this.doIfTrue = doIfTrue;
  }
  
  public List<Statement> getStatements(){
    return doIfTrue;
  }
  
}
