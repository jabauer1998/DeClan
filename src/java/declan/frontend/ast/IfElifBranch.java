package declan.frontend.ast;

import java.util.List;

import declan.utils.position.Position;

/**  
 * This is the AST Node that supports if or elif clauses
 * @author Jacob Bauer
 */
public class IfElifBranch extends Branch implements Statement {
    private final Expression toCheck;
    private final Branch branchTo;
    private static Boolean firstif = true;
    
    public IfElifBranch(Position start, Expression toCheck, List<Statement> toExecute, Branch branchTo){
      super(start, toExecute);
      this.toCheck = toCheck;
      this.branchTo = branchTo;
    }
  
    public List<Statement> getExecStatements(){
      return super.getExecStatements();
    }

    public Expression getExpression(){
      return toCheck;
    }

    public Branch getNextBranch(){
      return branchTo;
    }

    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      if(firstif){
        mystring.append("IF => ");
	firstif = false;
      } else {
	mystring.append("ELIF => ");
      }
      mystring.append(getExpression().toString() + ":\n");
      List<Statement> stat = getExecStatements();
      for(int i = 0; i < stat.size(); i++){
        mystring.append("\tStatement " + i + " = ");
        mystring.append(stat.get(i).toString());
        mystring.append('\n');
      }
      if(getNextBranch() != null){
	      mystring.append(getNextBranch().toString());
      }
      return mystring.toString();
    }
  
    @Override
    public void accept(ASTVisitor visitor) {
      visitor.visit(this);
    }

    @Override
    public <R> R acceptResult(StatementVisitor<R> visitor) {
      return visitor.visitResult(this);
    }
}
