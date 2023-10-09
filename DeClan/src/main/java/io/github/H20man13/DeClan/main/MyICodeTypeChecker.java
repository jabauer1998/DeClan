package io.github.H20man13.DeClan.main;

import java.security.cert.CertPathValidatorException.BasicReason;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.ast.BinaryOperation;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.ParamAssign;
import io.github.H20man13.DeClan.common.icode.Place;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.IntEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.NullEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;

public class MyICodeTypeChecker {
    private List<ICode> inputICode;
    private ErrorLog errLog;
    private Environment<String, TypeCheckerQualities> variableQualities;
    private Environment<String, IntEntry> labels;
    private int instructionNumber;
    
    public MyICodeTypeChecker(List<ICode> inputICode, ErrorLog errLog){
        this.inputICode = inputICode;
        this.errLog = errLog;
        this.instructionNumber = 0;
        this.variableQualities = new Environment<String, TypeCheckerQualities>();
        this.labels = new Environment<String, IntEntry>();
        this.variableQualities.addScope();
        this.labels.addScope();
    }

    public void runTypeChecker(){
        instructionNumber = 0;
        for(ICode icode : inputICode){
            findLabel(icode);
            instructionNumber++;
        }
        boolean notAllVariablesFound = true;
        int passes = 0;
        while(notAllVariablesFound && passes < 10){
            instructionNumber = 0;
            boolean oneNotFound = false;
            for(ICode icode : inputICode){
                boolean found = typeCheckPossibleAssignmentsAndParamaters(icode);
                if(!found){
                    oneNotFound = true;
                }
                instructionNumber++;
            }
            if(!oneNotFound){
                notAllVariablesFound = false;
            }
            passes++;
        }
        instructionNumber = 0;
        for(ICode icode : inputICode){
            typeCheckRestOfInstructions(icode);
            instructionNumber++;
        }
    }

    public boolean identContainsQualities(String ident, Integer integer){
        if(variableQualities.entryExists(ident)){
            TypeCheckerQualities qual = variableQualities.getEntry(ident);
            return qual.containsQualities(integer);
        } else {
            return false;
        }
    }

    private void findLabel(ICode icode){
        if(icode instanceof Label){
            Label label = (Label)icode;
            if(labels.entryExists(label.label)){
                Position newPos = new Position(instructionNumber, 0);
                IntEntry labelEntry = labels.getEntry(label.label);
                errLog.add("Error redefinition of label at postition " + newPos + " originally declared ", new Position(labelEntry.getValue(), 0));
            } else {
                labels.addEntry(label.label, new IntEntry(instructionNumber));
            }
        }
    }

    private boolean typeCheckPossibleAssignmentsAndParamaters(ICode icode){
        if(icode instanceof Assign) return typeCheckPossibleAssignment((Assign)icode);
        else if(icode instanceof ParamAssign) return typeCheckPossibleParamAssign((ParamAssign)icode);
        else if(icode instanceof Proc) return typeCheckPossibleParamaters((Proc)icode);
        else if(icode instanceof Place) return typeCheckPossibleReturn((Place)icode);
        return true;
    }

    private boolean typeCheckPossibleParamAssign(ParamAssign assign){
        if(variableQualities.entryExists(assign.paramPlace)){
            TypeCheckerQualities qual = variableQualities.getEntry(assign.paramPlace);
            variableQualities.addEntry(assign.newPlace, qual);
            return true;
        }
        return false;
    }

    private boolean typeCheckPossibleReturn(Place returnAssign){
        if(variableQualities.entryExists(returnAssign.retPlace)){
            TypeCheckerQualities qual = variableQualities.getEntry(returnAssign.retPlace);
            variableQualities.addEntry(returnAssign.place, qual);
            return true;
        }
        return false;
    }

    private boolean typeCheckPossibleAssignment(Assign assign){
        Assign assignICode = (Assign)assign;
        TypeCheckerQualities qual = typeCheckExpression(assignICode.value);
        if(qual.missingQualities(TypeCheckerQualities.NA)){
            variableQualities.addEntry(assignICode.place, qual);
            return true;
        }
        return false;
    }

    private boolean typeCheckPossibleParamaters(Proc proc){
        boolean allFound = true;
        for(Tuple<String, String> param : proc.params){
            if(variableQualities.entryExists(param.source)){
                if(variableQualities.entryExists(param.dest)){
                    TypeCheckerQualities localQual = variableQualities.getEntry(param.source);
                    TypeCheckerQualities paramQual = variableQualities.getEntry(param.dest);
                    if(localQual.containsQualities(TypeCheckerQualities.BOOLEAN) && paramQual.missingQualities(TypeCheckerQualities.BOOLEAN)
                    || localQual.containsQualities(TypeCheckerQualities.INTEGER) && paramQual.missingQualities(TypeCheckerQualities.INTEGER)
                    || localQual.containsQualities(TypeCheckerQualities.REAL) && paramQual.missingQualities(TypeCheckerQualities.REAL)){
                        errLog.add("Error in function call " + proc.pname + ": param " + param.dest + " takes in a paramater of type " + paramQual + " the first call but takes in a paramater of type " + localQual + " the ext time from param " + param.source, new Position(instructionNumber, 0));
                    }
                } else {
                    TypeCheckerQualities sourceQual = variableQualities.getEntry(param.source);
                    variableQualities.addEntry(param.dest, sourceQual);
                }
            } else {
                allFound = false;
            }
        }
        return allFound;
    }

    private void typeCheckRestOfInstructions(ICode icode){
        if(icode instanceof If) typeCheckIfStatement((If)icode);
        else if(icode instanceof Proc) typeCheckProcedureLabel((Proc)icode);
        else if(icode instanceof Goto) typeCheckGotoStatement((Goto)icode);
    }

    private void typeCheckProcedureLabel(Proc icode){
        if(!labels.entryExists(icode.pname)){
            errLog.add("Error No label found for procedure entry " + icode.pname, new Position(instructionNumber, 0));
        }
    }

    private void typeCheckGotoStatement(Goto icode){
        if(!labels.entryExists(icode.label)){
            errLog.add("Error No label found for goto statement " + icode.label, new Position(instructionNumber, 0));
        }
    }

    private void typeCheckIfStatement(If ifStat){
        TypeCheckerQualities type = typeCheckExpression(ifStat.exp);
        if(!type.containsQualities(TypeCheckerQualities.BOOLEAN)){
            errLog.add("Error expected Boolean resulting type on the right hand side of the assignment", new Position(instructionNumber, 0));
        }

        if(!labels.entryExists(ifStat.ifTrue)){
            errLog.add("Error no label found for if statement label " + ifStat.ifTrue, new Position(instructionNumber, 0));
        }

        if(!labels.entryExists(ifStat.ifFalse)){
            errLog.add("Error no label found for if statement label " + ifStat.ifFalse, new Position(instructionNumber, 0));
        }
    }

    private TypeCheckerQualities typeCheckExpression(Exp expression){
        if(expression instanceof BinExp) return typeCheckBinaryExpression((BinExp)expression);
        else if(expression instanceof UnExp) return typeCheckUnaryExpression((UnExp)expression);
        else if(expression instanceof IdentExp) return typeCheckIdentifier((IdentExp)expression);
        else if(expression instanceof IntExp) return typeCheckInteger();
        else if(expression instanceof RealExp) return typeCheckReal();
        else if(expression instanceof StrExp) return typeCheckString();
        else if(expression instanceof BoolExp) return typeCheckBoolean();
        else {
            return new TypeCheckerQualities(TypeCheckerQualities.NA);
        }
    }

    private TypeCheckerQualities typeCheckBinaryExpression(BinExp expression){
        TypeCheckerQualities leftQual = typeCheckExpression(expression.left);
        TypeCheckerQualities rightQual = typeCheckExpression(expression.right);

        if(leftQual.containsQualities(TypeCheckerQualities.STRING)){
            errLog.add("Error the left hand side of the binary expression contains a String value", new Position(instructionNumber, 0));
        }

        if(rightQual.containsQualities(TypeCheckerQualities.STRING)){
            errLog.add("Error the right hand side of the binary expression contains a String value", new Position(instructionNumber, 0));
        }
	
        if(expression.op == BinExp.Operator.EQ || expression.op == BinExp.Operator.NE 
        || expression.op == BinExp.Operator.LT || expression.op == BinExp.Operator.LE 
        || expression.op == BinExp.Operator.GT || expression.op == BinExp.Operator.GE
        || expression.op == BinExp.Operator.LAND || expression.op == BinExp.Operator.LOR){
            return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
        } else if(expression.op == BinExp.Operator.IDIV || expression.op == BinExp.Operator.IAND 
        || expression.op == BinExp.Operator.IOR || expression.op == BinExp.Operator.ILSHIFT 
        || expression.op == BinExp.Operator.IRSHIFT || expression.op == BinExp.Operator.IADD
        || expression.op == BinExp.Operator.ISUB || expression.op == BinExp.Operator.IMUL
        || expression.op == BinExp.Operator.IMOD){
            return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);	
        } else if(expression.op == BinExp.Operator.RDIVIDE || expression.op == BinExp.Operator.RADD
                || expression.op == BinExp.Operator.RSUB || expression.op == BinExp.Operator.RMUL){
            return new TypeCheckerQualities(TypeCheckerQualities.REAL);	
        } else {
            errLog.add("Unknown Operation type " + expression.op, new Position(instructionNumber, 0));
            return new TypeCheckerQualities(TypeCheckerQualities.NA);
        }
    }

    private TypeCheckerQualities typeCheckUnaryExpression(UnExp expression){
        TypeCheckerQualities rightQual = typeCheckExpression(expression.right);

        if(rightQual.containsQualities(TypeCheckerQualities.STRING)){
            errLog.add("Error in unary operation cant have string as input for expression " + expression, new Position(instructionNumber, 0));
        }

        switch(expression.op){
            case RNEG: return new TypeCheckerQualities(TypeCheckerQualities.REAL);
            case INEG: return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);
            case BNOT: return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
            default:
                errLog.add("Error unknown Operation type " + expression.op, new Position(instructionNumber, 0));
                return new TypeCheckerQualities(TypeCheckerQualities.NA);
        }
    }

    private TypeCheckerQualities typeCheckIdentifier(IdentExp ident){
        if(variableQualities.entryExists(ident.ident)){
            return variableQualities.getEntry(ident.ident);
        } else {
            return new TypeCheckerQualities(TypeCheckerQualities.NA);
        }
    }

    private TypeCheckerQualities typeCheckInteger(){
        return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);
    }

    private TypeCheckerQualities typeCheckString(){
        return new TypeCheckerQualities(TypeCheckerQualities.STRING);
    }

    private TypeCheckerQualities typeCheckReal(){
        return new TypeCheckerQualities(TypeCheckerQualities.REAL);
    }

    private TypeCheckerQualities typeCheckBoolean(){
        return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
    }

    
}
