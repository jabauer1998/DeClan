package declan.middleware;

import java.lang.StackWalker.StackFrame;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.util.List;

import declan.utils.ErrorLog;
import declan.utils.position.Position;
import declan.utils.Tuple;
import declan.frontend.ast.BinaryOperation;
import declan.utils.exception.ICodeGeneratorException;
import declan.utils.exception.ICodeTypeCheckerException;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Call;
import declan.middleware.icode.Def;
import declan.middleware.icode.Goto;
import declan.middleware.icode.ICode;
import declan.middleware.icode.If;
import declan.middleware.icode.Prog;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.BoolExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.IntExp;
import declan.middleware.icode.exp.RealExp;
import declan.middleware.icode.exp.StrExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.icode.label.Label;
import declan.utils.pat.P;
import declan.utils.position.Position;
import declan.utils.symboltable.Environment;
import declan.utils.symboltable.entry.IntEntry;
import declan.utils.symboltable.entry.NullEntry;
import declan.utils.symboltable.entry.TypeCheckerQualities;
import declan.utils.symboltable.entry.VariableEntry;
import declan.utils.ConversionUtils;

public class MyICodeTypeChecker {
    private List<ICode> inputICode;
    private Environment<String, TypeCheckerQualities> variableQualities;
    private Environment<String, IntEntry> labels;
    private int instructionNumber;
    private ErrorLog errLog;
    
    public MyICodeTypeChecker(List<ICode> inputICode, ErrorLog errLog){
        this.inputICode = inputICode;
        this.instructionNumber = 0;
        this.variableQualities = new Environment<String, TypeCheckerQualities>();
        this.labels = new Environment<String, IntEntry>();
        this.variableQualities.addScope();
        this.labels.addScope();
    }

    public MyICodeTypeChecker(Prog program, ErrorLog errLog){
        this(program.getICode(), errLog);
    }

    public void runTypeChecker(){
        instructionNumber = 0;
        for(ICode icode : inputICode){
            try{
                findLabel(icode);
            } catch(ICodeTypeCheckerException exp){
                errLog.add(exp.toString(), new Position(this.instructionNumber, 0));
            }
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
                throw new ICodeTypeCheckerException(labelEntry.getClass().getEnclosingMethod().getName(), icode, instructionNumber, "Error redefinition of label at postition " + newPos + " originally declared ");
            } else {
                labels.addEntry(label.label, new IntEntry(instructionNumber));
            }
        }
    }

    private boolean typeCheckPossibleAssignmentsAndParamaters(ICode icode){
        if(icode instanceof Assign) return typeCheckPossibleAssignment((Assign)icode);
        else if(icode instanceof Call) return typeCheckPossibleParamaters((Call)icode);
        else if(icode instanceof Def) return typeCheckPossibleDefinition((Def)icode);
        return true;
    }

    private boolean typeCheckPossibleDefinition(Def assign){
        TypeCheckerQualities qual = typeCheckExpression(assign.val);
        ICode.Type displayedType = assign.type;

        if((qual.containsQualities(TypeCheckerQualities.INTEGER) && displayedType != Assign.Type.INT)
        || (qual.containsQualities(TypeCheckerQualities.REAL) && displayedType != Assign.Type.REAL)
        || (qual.containsQualities(TypeCheckerQualities.BOOLEAN) && displayedType != Assign.Type.BOOL)
        || (qual.containsQualities(TypeCheckerQualities.STRING) && displayedType != Assign.Type.STRING)){
            throw new ICodeTypeCheckerException("typeCheckPossibleDefinition", assign, instructionNumber, "Error invalid operation between types: \nType 1: " + qual.toString() + "\nType 2: " + displayedType);
        }

        if(qual.missingQualities(TypeCheckerQualities.NA)){
            TypeCheckerQualities newType = ConversionUtils.assignTypeToTypeCheckerQualities(displayedType);
            variableQualities.addEntry(assign.label, newType);
            return true;
        }
        return false;
    }

    private boolean typeCheckPossibleAssignment(Assign assign){
        TypeCheckerQualities qual = typeCheckExpression(assign.value);
        Assign.Type displayedType = assign.getType();

        if((qual.containsQualities(TypeCheckerQualities.INTEGER) && displayedType != Assign.Type.INT)
        || (qual.containsQualities(TypeCheckerQualities.REAL) && displayedType != Assign.Type.REAL)
        || (qual.containsQualities(TypeCheckerQualities.BOOLEAN) && displayedType != Assign.Type.BOOL)
        || (qual.containsQualities(TypeCheckerQualities.STRING) && displayedType != Assign.Type.STRING)){
            throw new ICodeTypeCheckerException("typeCheckPossibleAssignment", assign, instructionNumber, "Error in assignment " + assign + " expression is of type " + qual.toString() + " but is utilized in assignment of type " + displayedType);
        }

        if(qual.missingQualities(TypeCheckerQualities.NA)){
            TypeCheckerQualities newType = ConversionUtils.assignTypeToTypeCheckerQualities(displayedType);
            variableQualities.addEntry(assign.place, newType);
            return true;
        }
        return false;
    }

    private boolean typeCheckPossibleParamaters(Call proc){
        boolean allFound = true;
        for(Def param : proc.params){
            if(param.val instanceof IdentExp){
                IdentExp exp = (IdentExp)param.val;
                if(variableQualities.entryExists(exp.ident)){
                    TypeCheckerQualities sourceQual = variableQualities.getEntry(exp.ident);
                    ICode.Type displayedParamType = param.type;
                    if((sourceQual.containsQualities(TypeCheckerQualities.INTEGER) && displayedParamType != Assign.Type.INT)
                    || (sourceQual.containsQualities(TypeCheckerQualities.REAL) && displayedParamType != Assign.Type.REAL)
                    || (sourceQual.containsQualities(TypeCheckerQualities.BOOLEAN) && displayedParamType != Assign.Type.BOOL)
                    || (sourceQual.containsQualities(TypeCheckerQualities.STRING) && displayedParamType != Assign.Type.STRING)){
                        throw new ICodeTypeCheckerException("typeCheckPossibleParamaters", proc, instructionNumber, "Error in function call " + proc.pname + ": param " + exp.ident + " is of type " + sourceQual.toString() + " but it is used in paramater assignment of type " + displayedParamType.toString());
                    }
                    if(!variableQualities.entryExists(param.label)){
                        variableQualities.addEntry(param.label, ConversionUtils.assignTypeToTypeCheckerQualities(displayedParamType));
                    } else {
                        TypeCheckerQualities qual = variableQualities.getEntry(param.label);
                        if((qual.containsQualities(TypeCheckerQualities.INTEGER) && displayedParamType != Assign.Type.INT)
                        || (qual.containsQualities(TypeCheckerQualities.BOOLEAN) && displayedParamType != Assign.Type.BOOL)
                        || (qual.containsQualities(TypeCheckerQualities.STRING) && displayedParamType != Assign.Type.STRING)
                        || (qual.containsQualities(TypeCheckerQualities.REAL) && displayedParamType != Assign.Type.REAL)){
                            throw new ICodeTypeCheckerException("typeCheckPossibleParamaters", proc, instructionNumber, "Error in function call " + proc.pname + ": param " + param.label + " is of type " + sourceQual.toString() + " but it is given a value in paramater assignment of type " + displayedParamType.toString());
                        }
                    }
                } else {
                    allFound = false;
                }
            }
        }
        return allFound;
    }

    private void typeCheckRestOfInstructions(ICode icode){
        if(icode instanceof If) typeCheckIfStatement((If)icode);
        else if(icode instanceof Call) typeCheckProcedureLabel((Call)icode);
        else if(icode instanceof Goto) typeCheckGotoStatement((Goto)icode);
    }

    private void typeCheckProcedureLabel(Call icode){
        if(!labels.entryExists(icode.pname)){
            throw new ICodeTypeCheckerException("typeCheckProcedureLabel", icode, instructionNumber, "Error No label found for procedure entry " + icode.pname);
        }
    }

    private void typeCheckGotoStatement(Goto icode){
        if(!labels.entryExists(icode.label)){
            throw new ICodeTypeCheckerException("typeCheckProcedureLabel", icode, instructionNumber, "Error No label found for goto statement " + icode.label);
        }
    }

    private void typeCheckIfStatement(If ifStat){
        TypeCheckerQualities type = typeCheckExpression(ifStat.exp);
        if(!type.containsQualities(TypeCheckerQualities.BOOLEAN)){
            throw new ICodeTypeCheckerException("typeCheckIfStatement", ifStat, instructionNumber, "Error expected Boolean resulting type on the right hand side of the assignment");
        }

        if(!labels.entryExists(ifStat.ifTrue)){
            throw new ICodeTypeCheckerException("typeCheckIFStatement", ifStat, instructionNumber, "Error no label found for if statement label " + ifStat.ifTrue);
        }

        if(!labels.entryExists(ifStat.ifFalse)){
            throw new ICodeTypeCheckerException("typeCheckIfStatement", ifStat, instructionNumber, "Error no label found for if statement label " + ifStat.ifFalse);
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
            throw new ICodeTypeCheckerException("typeCheckBinaryExpression", expression, instructionNumber, "Error the left hand side of the binary expression contains a String value");
        }

        if(rightQual.containsQualities(TypeCheckerQualities.STRING)){
            throw new ICodeTypeCheckerException("typeCheckBinaryExpression", expression, instructionNumber, "Error the right hand side of the binary expression contains a String value");
        }
	
        if(expression.op == BinExp.Operator.IEQ || expression.op == BinExp.Operator.INE
        || expression.op == BinExp.Operator.BEQ || expression.op == BinExp.Operator.BNE
        || expression.op == BinExp.Operator.LT || expression.op == BinExp.Operator.LE 
        || expression.op == BinExp.Operator.GT || expression.op == BinExp.Operator.GE
        || expression.op == BinExp.Operator.LAND || expression.op == BinExp.Operator.LOR){
            return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
        } else if(expression.op == BinExp.Operator.IAND 
        || expression.op == BinExp.Operator.IOR || expression.op == BinExp.Operator.ILSHIFT 
        || expression.op == BinExp.Operator.IRSHIFT || expression.op == BinExp.Operator.IADD
        || expression.op == BinExp.Operator.ISUB || expression.op == BinExp.Operator.IXOR){
            return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);	
        } else {
            throw new ICodeTypeCheckerException("typeCheckBinaryExpression", expression, instructionNumber, "Unknown Operation type " + expression.op);
        }
    }

    private TypeCheckerQualities typeCheckUnaryExpression(UnExp expression){
        TypeCheckerQualities rightQual = typeCheckExpression(expression.right);

        if(rightQual.containsQualities(TypeCheckerQualities.STRING)){
            throw new ICodeTypeCheckerException("typeCheckUnaryExpression", expression, instructionNumber, "Error in unary operation cant have string as input for expression " + expression);
        }

        switch(expression.op){
            case BNOT: return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
            case INOT: return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);
            default:
                throw new ICodeTypeCheckerException("typeCheckUnaryExpression", expression, instructionNumber, "Error unknown Operation type " + expression.op);
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
