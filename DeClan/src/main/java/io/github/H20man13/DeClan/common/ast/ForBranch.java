package io.github.H20man13.DeClan.common.ast;

import java.util.List;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.exception.InterpreterException;
import io.github.H20man13.DeClan.common.position.Position;
import io.github.H20man13.DeClan.main.MyInterpreter;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.String;
import java.lang.StringBuilder;

/**
 * This is the ast class that allows for for Loops in the Declan Language
 * @author Jacob Bauer
 */
public class ForBranch extends Branch implements Statement {
    private final Assignment initAssign;
    private final Expression toCheck;
    private final Expression toMod;
    
    public ForBranch(Position start, Assignment initAssign, Expression toCheck, Expression toMod, List<Statement> toExecute){
      super(start, toExecute);
      this.toCheck = toCheck;
      this.initAssign = initAssign;
      this.toMod = toMod;
    }
  
    public List<Statement> getExecStatements(){
      return super.getExecStatements();
    }

    public Expression getTargetExpression(){
      return toCheck;
    }

    public Expression getModifyExpression(){
      return toMod;
    }

    public Assignment getInitAssignment(){
      return initAssign;
    }
    
    public boolean isSimplifiable() {
    	return initAssign.getVariableValue().isConstant() && toCheck.isConstant() && toMod.isConstant();
    }
    
    private static Object evaluateExpression(Expression exp) {
    	StringWriter errLog = new StringWriter();
    	StringWriter stdOut = new StringWriter();
    	ErrorLog log = new ErrorLog();
    	
    	MyInterpreter interpreter = new MyInterpreter(log, errLog, stdOut, new StringReader(""));
    	return exp.acceptResult(interpreter);
    }
    
    public int getLoopIterations() {
    	if(isSimplifiable()) {
    		Object init = evaluateExpression(initAssign.getVariableValue());
    		if(init instanceof Double) {
    			double dInit = (double)init;
    			Object cond = evaluateExpression(toCheck);
    			if(cond instanceof Double) {
    				double condD = (double)cond;
    				Object mod = evaluateExpression(toMod);
    				if(mod instanceof Double) {
    					double dMod = (double)mod;
    					return (int)Math.ceil(Math.abs(((dInit < condD) ? condD - dInit: dInit - condD) / dMod)); 
    				} else if(mod instanceof Integer) {
    					int iMod = (int)mod;
    					return (int)Math.ceil(Math.abs(((dInit < condD) ? condD - dInit: dInit - condD) / iMod)); 
    				} else {
    					throw new RuntimeException();
    				}
    			} else if(cond instanceof Integer) {
    				int condI = (int)cond;
    				Object mod = evaluateExpression(toMod);
    				if(mod instanceof Double) {
    					double dMod = (double)mod;
    					return (int)Math.ceil(Math.abs(((dInit < condI) ? condI - dInit: dInit - condI) / dMod)); 
    				} else if(mod instanceof Integer) {
    					int iMod = (int)mod;
    					return (int)Math.ceil(Math.abs(((dInit < condI) ? condI - dInit: dInit - condI) / iMod)); 
    				} else {
    					throw new RuntimeException();
    				}
    			} else {
    				throw new RuntimeException();
    			}
    		} else if(init instanceof Integer) {
    			int iInit = (int)init;
    			Object cond = evaluateExpression(toCheck);
    			if(cond instanceof Double) {
    				double condD = (double)cond;
    				Object mod = evaluateExpression(toMod);
    				if(mod instanceof Double) {
    					double dMod = (double)mod;
    					return (int)Math.ceil(Math.abs(((iInit < condD) ? condD - iInit: iInit - condD) / dMod)); 
    				} else if(mod instanceof Integer) {
    					int iMod = (int)mod;
    					return (int)Math.ceil(Math.abs(((iInit < condD) ? condD - iInit: iInit - condD) / iMod)); 
    				} else {
    					throw new RuntimeException();
    				}
    			} else if(cond instanceof Integer) {
    				int condI = (int)cond;
    				Object mod = evaluateExpression(toMod);
    				if(mod instanceof Double) {
    					double dMod = (double)mod;
    					return (int)Math.ceil(Math.abs(((iInit < condI) ? condI - iInit: iInit - condI) / dMod)); 
    				} else if(mod instanceof Integer) {
    					int iMod = (int)mod;
    					return (int)Math.ceil(Math.abs(((iInit < condI) ? condI - iInit: iInit - condI) / iMod)); 
    				} else {
    					throw new RuntimeException();
    				}
    			} else {
    				throw new RuntimeException();
    			}
    		}
    	}
    	throw new RuntimeException();
    }
  
    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append("FOR ");
      mystring.append(initAssign.toString());
      mystring.append(", ");
      mystring.append(getTargetExpression());
      if(toMod != null){
	mystring.append(", ");
	mystring.append(getModifyExpression().toString());
      }
      mystring.append(":\n");
      List<Statement> toExecc = getExecStatements();
      for(int i = 0; i < toExecc.size(); i++){
	mystring.append("\t Statement " + (i + 1));
	mystring.append(": ");
	mystring.append(toExecc.get(i).toString());
	mystring.append('\n');
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
