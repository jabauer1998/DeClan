package declan.frontend.builder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import declan.utils.ErrorLog;
import declan.utils.Tuple;
import declan.frontend.IrRegisterGenerator;
import declan.middleware.icode.Assign;
import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.icode.exp.UnExp.Operator;

public class IrBuilderContext {
    private int nextForLoopNumber;
    private int forLoopNumber;
    private int forLoopLevel;
    private Stack<Integer> forLoopNumberStack;
    
    private int nextRepeatLoopNumber;
    private int repeatLoopNumber;
    private int repeatLoopLevel;
    private Stack<Integer> repeatLoopNumberStack;
    
    private int nextIfStatementNumber;
    private int ifStatementNumber;
    private int ifStatementSeqNumber;
    private int ifStatementLevel;
    private Stack<Integer> ifStatementNumberStack;
    private Stack<Integer> ifStatementSeqNumberStack;

    private int nextWhileLoopNumber;
    private int whileLoopNumber;
    private int whileLoopSeqNumber;
    private int whileLoopLevel;
    private Stack<Integer> whileLoopNumberStack;
    private Stack<Integer> whileLoopSeqNumberStack;

    public IrBuilderContext(){
        this.forLoopNumber = 0;
        this.repeatLoopNumber = 0;
        this.ifStatementNumber = 0;
        this.ifStatementSeqNumber = 0;
        this.whileLoopNumber = 0;
        this.whileLoopSeqNumber = 0;
        this.ifStatementLevel = 0;
        this.whileLoopLevel = 0;
        this.forLoopLevel = 0;
        this.repeatLoopLevel = 0;
        this.nextIfStatementNumber = 1;
        this.nextWhileLoopNumber = 1;
        this.nextForLoopNumber = 1;
        this.nextRepeatLoopNumber = 1;

        this.whileLoopNumberStack = new Stack<Integer>();
        this.ifStatementNumberStack = new Stack<Integer>();
        this.repeatLoopNumberStack = new Stack<Integer>();
        this.forLoopNumberStack = new Stack<Integer>();

        this.ifStatementSeqNumberStack = new Stack<Integer>();
        this.whileLoopSeqNumberStack = new Stack<Integer>();
    }

    public int getForLoopNumber(){
        return forLoopNumber;
    }

    public int getRepeatLoopNumber(){
        return this.repeatLoopNumber;
    }

    public int getIfStatementNumber(){
        return this.ifStatementNumber;
    }

    public int getIfStatementSeqNumber(){
        return this.ifStatementSeqNumber;
    }

    public int getWhileLoopNumber(){
        return this.whileLoopNumber;
    }

    public int getIfStatementLevel(){
        return this.ifStatementLevel;
    }

    public int getWhileLoopLevel(){
        return this.whileLoopLevel;
    }

    public int getForLoopLevel(){
        return this.forLoopLevel;
    }

    public int getRepeatLoopLevel(){
        return this.repeatLoopLevel;
    }

    public int getNextIfStatementNumber(){
        return this.nextIfStatementNumber;
    }

    public int getNextWhileLoopNumber(){
        return this.nextWhileLoopNumber;
    }

    public int getWhileLoopSeqNumber(){
        return this.whileLoopSeqNumber;
    }

    public int getNextForLoopNumber(){
        return this.nextForLoopNumber;
    }

    public int getNextRepeatLoopNumber(){
        return this.nextRepeatLoopNumber;
    }

    public void incrimentForLoopLevel(){
        this.forLoopNumberStack.push(this.forLoopNumber);
        this.forLoopNumber = this.nextForLoopNumber;
        this.nextForLoopNumber++;
        this.forLoopLevel++;
    }

    public void deIncrimentForLoopLevel(){
        this.forLoopNumber = this.forLoopNumberStack.pop();
        if((this.forLoopNumber + 2) == nextForLoopNumber){
            this.nextForLoopNumber--;
        } 
        forLoopLevel--;
    }

    public void incrimentForLoopNumber(){
        if(this.nextForLoopNumber > this.forLoopNumber+1){
            this.forLoopNumber = this.nextForLoopNumber;
            this.nextForLoopNumber++;
        } else {
            this.forLoopNumber++;
            this.nextForLoopNumber++;
        }
    }

    public void incrimentRepeatLoopLevel(){
        this.repeatLoopNumberStack.push(repeatLoopNumber);
        this.repeatLoopNumber = nextRepeatLoopNumber;
        this.nextRepeatLoopNumber++;
        repeatLoopLevel++;
    }

    public void deIncrimentRepeatLoopLevel(){
        this.repeatLoopNumber = repeatLoopNumberStack.pop();
        if((this.repeatLoopNumber + 2) == nextRepeatLoopNumber){
            this.nextRepeatLoopNumber--;
        } 
        repeatLoopLevel--;
    }

    public void incrimentRepeatLoopNumber(){
        if(nextRepeatLoopNumber > repeatLoopNumber+1){
            repeatLoopNumber = nextRepeatLoopNumber;
            nextRepeatLoopNumber++;
        } else {
            repeatLoopNumber++;
            nextRepeatLoopNumber++;
        }
    }

    public void incrimentIfStatementSeqNumber(){
        this.ifStatementSeqNumber++;
    }

    public void incrimentIfStatementNumber(){
        if(nextIfStatementNumber > ifStatementNumber+1){
            ifStatementNumber = nextIfStatementNumber;
            nextIfStatementNumber++;
        } else {
            ifStatementNumber++;
            nextIfStatementNumber++;
        }
        ifStatementSeqNumber = 0;
    }

    public void incrimentWhileLoopSeqNumber(){
        whileLoopSeqNumber++;
    }

    public void incrimentWhileLoopNumber(){
        if(nextWhileLoopNumber > whileLoopNumber+1){
            whileLoopNumber = nextWhileLoopNumber;
            nextWhileLoopNumber++;
        } else {
            whileLoopNumber++;
            nextWhileLoopNumber++;
        }
        whileLoopSeqNumber = 0;
    }

    public void incrimentIfStatementLevel(){
        this.ifStatementNumberStack.push(ifStatementNumber);
        this.ifStatementSeqNumberStack.push(ifStatementSeqNumber);
        this.ifStatementNumber = nextIfStatementNumber;
        this.nextIfStatementNumber++;
        this.ifStatementSeqNumber = 0;
        ifStatementLevel++;
    }

    public void deIncrimentIfStatementLevel(){
        this.ifStatementNumber = this.ifStatementNumberStack.pop();
        this.ifStatementSeqNumber = this.ifStatementSeqNumberStack.pop();
        if((this.ifStatementNumber + 2) == nextIfStatementNumber){
            this.nextIfStatementNumber--;
        }
        ifStatementLevel--;
    }

    public void incrimentWhileLoopLevel(){
        this.whileLoopNumberStack.push(this.whileLoopNumber);
        this.whileLoopSeqNumberStack.push(this.whileLoopSeqNumber);
        this.whileLoopNumber = this.nextWhileLoopNumber;
        this.nextWhileLoopNumber++;
        this.whileLoopSeqNumber = 0;
        whileLoopLevel++;
    }

    public void deIncrimentWhileLoopLevel(){
        this.whileLoopNumber = this.whileLoopNumberStack.pop();
        this.whileLoopSeqNumber = this.whileLoopSeqNumberStack.pop();
        if((this.whileLoopNumber + 2) == this.whileLoopNumber){
            this.nextWhileLoopNumber--;
        }
        whileLoopLevel--;
    }
}
