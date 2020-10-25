package edu.depauw.declan.common.ast;

import java.util.List;

public class ElseBlock extends IfElseBlock{
  
  public ElseBlock(List<Statement> doIfTrue){
    super(doIfTrue);
  }
  
  public List<Statement> getStatements(){
    return super.getStatements();
  }
}
