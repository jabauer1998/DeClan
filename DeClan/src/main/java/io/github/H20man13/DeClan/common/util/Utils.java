package io.github.H20man13.DeClan.common.util;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.dag.DagNullNode;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.dag.DagValueNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode;
import io.github.H20man13.DeClan.common.flow.block.BasicBlock;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalCall;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.InternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.LiveInfo;
import io.github.H20man13.DeClan.common.token.IrTokenType;

public class Utils {
    private static DagNodeFactory factory = new DagNodeFactory();

    public static List<ICode> stripFromListExcept(List<ICode> list, ICode item){
        List<ICode> linkedList = new LinkedList<ICode>();

        for(ICode listItem : list){
            if(listItem.hashCode() != item.hashCode()){
                linkedList.add(listItem);
            }
        }

        return linkedList;
    }

    public static boolean setContainsExp(Set<Exp> returnSet, Exp exp){
        for(Exp expInSet : returnSet){
            if(expInSet.equals(exp)){
                return true;
            }
        }
        return false;
    }

    public static BinExp.Operator toBinOp(IrTokenType type){
        switch(type){
            case NE: return BinExp.Operator.NE;
            case EQ: return BinExp.Operator.EQ;
            case GE: return BinExp.Operator.GE;
            case GT: return BinExp.Operator.GT;
            case LE: return BinExp.Operator.LE;
            case LT: return BinExp.Operator.LT;
            case RADD: return BinExp.Operator.RADD;
            case RSUB: return BinExp.Operator.RSUB;
            case RMUL: return BinExp.Operator.RMUL;
            case IADD: return BinExp.Operator.IADD;
            case ISUB: return BinExp.Operator.ISUB;
            case IMUL: return BinExp.Operator.IMUL;
            case IDIV: return BinExp.Operator.IDIV;
            case RDIVIDE: return BinExp.Operator.RDIVIDE;
            case IXOR: return BinExp.Operator.IXOR;
            case IMOD: return BinExp.Operator.IMOD;
            case LOR: return BinExp.Operator.LOR;
            case LAND: return BinExp.Operator.LAND;
            case IAND: return BinExp.Operator.IAND;
            case IOR: return BinExp.Operator.IOR;
            case IRSHIFT: return BinExp.Operator.IRSHIFT;
            case ILSHIFT: return BinExp.Operator.ILSHIFT;
            default: return null;
        }
    }

    public static Exp getExpFromSet(Set<Tuple<String, Exp>> tuples, String name){
        for(Tuple<String, Exp> tuple : tuples){
            if(tuple.source.equals(name)){
                return tuple.dest;
            }
        }
        return null;
    }

    public static boolean containsExpInSet(Set<Tuple<String, Exp>> tuples, String name){
        for(Tuple<String, Exp> tuple : tuples){
            if(tuple.source.equals(name)){
                return true;
            }
        }
        return false;
    }

    public static BinExp.Operator getBinOp(DagOperationNode.Op op){
        switch(op){
            case IADD: return BinExp.Operator.IADD;
            case ISUB: return BinExp.Operator.ISUB;
            case IMUL: return BinExp.Operator.IMUL;
            case IDIV: return BinExp.Operator.IDIV;
            case RADD: return BinExp.Operator.RADD;
            case RSUB: return BinExp.Operator.RSUB;
            case RMUL: return BinExp.Operator.RMUL;
            case RDIVIDE: return BinExp.Operator.RDIVIDE;
            case IMOD: return BinExp.Operator.IMOD;
            case LAND: return BinExp.Operator.LAND;
            case LOR: return BinExp.Operator.LOR;
            case IAND: return BinExp.Operator.IAND;
            case IOR: return BinExp.Operator.IOR;
            case IXOR: return BinExp.Operator.IXOR;
            case ILSHIFT: return BinExp.Operator.ILSHIFT;
            case IRSHIFT: return BinExp.Operator.IRSHIFT;
            case EQ: return BinExp.Operator.EQ;
            case NE: return BinExp.Operator.NE;
            case GE: return BinExp.Operator.GE;
            case GT: return BinExp.Operator.GT;
            case LE: return BinExp.Operator.LE;
            case LT: return BinExp.Operator.LT;
            default: return null;
        }
    }

    public static UnExp.Operator getUnOp(DagOperationNode.Op op){
        switch(op){
            case INEG: return UnExp.Operator.INEG;
            case RNEG: return UnExp.Operator.RNEG;
            case BNOT: return UnExp.Operator.BNOT;
            case INOT: return UnExp.Operator.INOT;
            default: return null;
        }
    }

    public static String getIdentifier(DagNode node, Environment<String, LiveInfo> table){
        List<String> identifiers = node.getIdentifiers();
        for(String identifier : identifiers){
            if(table.entryExists(identifier)){
                LiveInfo life = table.getEntry(identifier);
                if(life.isAlive){
                    return identifier;
                }
            }
        }

        if(identifiers.size() > 0){
            return identifiers.get(0);
        } else {
            return null;
        }
    }

    public static Exp valueToExp(Object result) {
        if(result instanceof Boolean){
            return new BoolExp((boolean)result);
        } else if(result instanceof Integer){
            return new IntExp((int)result);
        } else if(result instanceof String){
            return new StrExp((String)result);
        } else if(result instanceof Float){
            return new RealExp((float)result);
        } else {
            return null;
        }
    }

    public static Object getValue(Exp value) {
        if(value instanceof BoolExp){
            return ((BoolExp)value).trueFalse;
        } else if(value instanceof IntExp){
            return ((IntExp)value).value;
        } else if(value instanceof RealExp){
            return ((RealExp)value).realValue;
        } else if(value instanceof StrExp){
            return ((StrExp)value).value;
        } else {
            return null;
        }
    }

    public static DagNode createBinaryNode(BinExp.Operator op, String place, DagNode left, DagNode right) {
        switch(op){
            case IADD: return factory.createIntegerAdditionNode(place, left, right);
            case ISUB: return factory.createIntegerSubtractionNode(place, left, right);
            case IMUL: return factory.createIntegerMultiplicationNode(place, left, right);
            case IDIV: return factory.createIntegerDivNode(place, left, right);
            case RADD: return factory.createRealAdditionNode(place, left, right);
            case RSUB: return factory.createRealSubtractionNode(place, left, right);
            case RMUL: return factory.createRealMultiplicationNode(place, left, right);
            case RDIVIDE: return factory.createRealDivisionNode(place, left, right);
            case LAND: return factory.createLogicalAndNode(place, left, right);
            case IAND: return factory.createBitwiseAndNode(place, left, right);
            case IOR: return factory.createBitwiseOrNode(place, left, right);
            case IXOR: return factory.createBitwiseXorNode(place, left, right);
            case ILSHIFT: return factory.createLeftShiftNode(place, left, right);
            case IRSHIFT: return factory.createRightShiftNode(place, left, right);
            case IMOD: return factory.createIntegerModuleNode(place, left, right);
            case LOR: return factory.createLogicalOrNode(place, left, right);
            case GT: return factory.createGreaterThanNode(place, left, right);
            case GE: return factory.createGreaterThanOrEqualNode(place, left, right);
            case LT: return factory.createLessThanNode(place, left, right);
            case LE: return factory.createLessThanOrEqualNode(place, left, right);
            case EQ: return factory.createEqualsNode(place, left, right);
            case NE: return factory.createNotEqualsNode(place, left, right);
            default: return null;
        }
    }

    public static DagNode createUnaryNode(UnExp.Operator op, String place, DagNode right){
        switch(op){
            case INEG: return factory.createIntegerNegationNode(place, right);
            case RNEG: return factory.createRealNegationNode(place, right);
            case BNOT: return factory.createNotNode(place, right);
            case INOT: return factory.createBitwiseNotNode(place, right);
            default: return null;
        }
    }

    public static boolean beginningOfBlockIsLabel(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode firstICode = codeInBlock.get(0);
            if(firstICode instanceof Label){
                return true;
            } else if(firstICode instanceof ProcLabel){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean endOfBlockIsJump(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode lastICode = codeInBlock.get(codeInBlock.size() - 1);
            if(lastICode instanceof If){
                return true;
            } else if(lastICode instanceof Goto){
                return true;
            } else if(lastICode instanceof Assign){
                Assign assignment = (Assign)lastICode;
                if(assignment.value instanceof ExternalCall)
                    return true;
                else
                    return false;  
            } else if(lastICode instanceof ExternalCall){
                return true;  
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static P binOpToPattern(BinExp.Operator op){
        switch(op){
            case IADD: return P.IADD();
            case ISUB: return P.ISUB();
            case IMUL: return P.IMUL();
            case IDIV: return P.IDIV();
            case LAND: return P.BAND();
            case IMOD: return P.IMOD();
            case RMUL: return P.RMUL();
            case RDIVIDE: return P.RDIVIDE();
            case RADD: return P.RADD();
            case RSUB: return P.RSUB();
            case IAND: return P.IAND();
            case IOR: return P.IOR();
            case IXOR: return P.IXOR();
            case ILSHIFT: return P.LSHIFT();
            case IRSHIFT: return P.RSHIFT();
            case LOR: return P.BOR();
            case GT: return P.GT();
            case GE: return P.GE();
            case LT: return P.LT();
            case LE: return P.LE();
            case EQ: return P.EQ();
            case NE: return P.NE();
            default: return null;
        }
    }

    public static P unOpToPattern(UnExp.Operator op){
        switch(op){
            case INEG: return P.INEG();
            case BNOT: return P.BNOT();
            case RNEG: return P.RNEG();
            case INOT: return P.INOT();
            default: return null;
        }
    }

    public static Float toReal(Object input){
        if(input instanceof Integer){
            Integer inti = (Integer)input;
            return inti.floatValue();
        } else if(input instanceof Float){
            Float fValue = (Float)input;
            return fValue.floatValue();
        } else if(input instanceof Boolean){
            Boolean bi = (Boolean)input;
            if(bi){
                return 1.0f;
            } else {
                return 0.0f;
            }
        } else {
            return null;
        }
    }

    public static Integer toInt(Object input){
        if(input instanceof Integer){
            return (Integer)input;
        } else if(input instanceof BigInteger){
            BigInteger lInput = (BigInteger)input;
            return lInput.intValue();
        } else if(input instanceof Float){
            Float di = (Float)input;
            return di.intValue();
        } else if(input instanceof Boolean){
            Boolean bi = (Boolean)input;
            if(bi){
                return 1;
            } else {
                return 0;
            }
        } else {
            return null;
        }
    }

    public static BigInteger toBigInt(Object input){
        if(input instanceof Integer){
            Integer inputInt = (Integer)input;
            return new BigInteger(Integer.toString(inputInt));
        } else if(input instanceof Float){
            Float inputint = (Float)input;
            return new BigInteger(Integer.toString(inputint.intValue()));
        } else if(input instanceof Boolean){
            Boolean inputBool = (Boolean)input;
            if(inputBool){
                return new BigInteger("1");
            } else {
                return new BigInteger("0");
            }
        } else {
            return null;
        }
    }

    public static Integer toRawInt(Object input){
        if(input instanceof Integer){
            return (Integer)input;
        } else if(input instanceof Float){
            Float dV = (Float)input;
            Integer result = Float.floatToIntBits(dV);
            return result;
        } else if(input instanceof Boolean){
            Boolean bi = (Boolean)input;
            if(bi) return 1;
            else return 0;
        } else {
            return null;
        }
    }

    public static Boolean toBool(Object input){
        if(input instanceof Integer){
            Integer inti = (Integer)input;
            return inti != 0;
        } else if(input instanceof Boolean){
            return (Boolean)input;
        } else if(input instanceof Float){
            Float di = (Float)input;
            return di != 0;
        } else {
            return null;
        }
    }

    public static <ArrayType> boolean arrayContainsValue(ArrayType toCheck, ArrayType[] array){
        for(ArrayType arrayVal : array){
            if(toCheck.equals(arrayVal)){
                return true;
            }
        }
        return false;
    }

  //this function is needed to change a Hex String from the Declan Format to the expected Java format
  public static String ifHexToInt(String lexeme){
    int length = lexeme.length();
    if(lexeme.charAt(0) == '0' && lexeme.length() > 1  
    && (lexeme.charAt(length - 1) == 'H' || lexeme.charAt(length - 1) == 'h')
    && !lexeme.contains(".")){
      String subStr = lexeme.substring(1, length - 1);
      Integer value = Integer.parseUnsignedInt(subStr, 16);
      return Integer.toUnsignedString(value);
    } else {
      return lexeme; //else return input it is fine
    }
  }

  //This function will be used to replace whatever the Leading whitespace of a String is into the other whitespace of the String
  public static String getLeadingWhiteSpace(String input){
      StringBuilder result = new StringBuilder();
      for(int i = 0; i < input.length(); i++){
        char characterAtI = input.charAt(i);
        if(characterAtI == '\t' || characterAtI == ' '){
            result.append(characterAtI);
        } else {
            break;
        }
     }
     return result.toString();
  }

  public static String formatStringToLeadingWhiteSpace(String input){
     StringBuilder result = new StringBuilder();
     int state = 0;
     String leadingWhiteSpace = getLeadingWhiteSpace(input);
     result.append(leadingWhiteSpace);
     int letterAt = leadingWhiteSpace.length();
     while(letterAt < input.length()){
        char letter = input.charAt(letterAt);
        if(state == 0){
            if(letter == '\n' || letter == '\r'){
                state = 1;
            } else {
                result.append(letter);
                letterAt++;
            }
        } else if(state == 1){
            if(letter == '\n' || letter == '\r' || letter == '\t' || letter == ' '){
                letterAt++;
            } else {
                result.append("\r\n");
                result.append(leadingWhiteSpace);
                state = 0;
            }
        }
     }

     result.append("\r\n");

     return result.toString();
  }

  public static String to32BitBinary(Integer intVal){
     StringBuilder sb = new StringBuilder();
     String endString = Integer.toBinaryString(intVal);
     int numZeros = 32 - endString.length();
     for(int i = 0; i < numZeros; i++){
        sb.append('0');
     }
     sb.append(endString);
     return sb.toString();
  }

  public static String to32BitBinary(Float realValue){
     Integer asInt = Float.floatToRawIntBits(realValue);
     return to32BitBinary(asInt);
  }

  public static boolean labelExistsInICode(String label, ICode instr){
        if(instr instanceof Label){
            Label instrLabel = (Label)instr;
            if(instrLabel.label.equals(label))
                return true;
        } else if(instr instanceof Goto){
            Goto instrGoto = (Goto)instr;
            if(instrGoto.label.equals(label))
                return true;
        } else if(instr instanceof If){
            If instrIf = (If)instr;
            if(instrIf.ifTrue.equals(label))
                return true;

            if(instrIf.ifFalse.equals(label))
                return true;
        }

        return false;
    }

    private static boolean placeExistsInICode(String place, ICode dataCode){
        if(dataCode instanceof Assign){
            Assign assign = (Assign)dataCode;
            
            if(assign.place.equals(place))
                return true;

            Exp value = assign.value;
            if(value instanceof IdentExp){
                IdentExp identVal = (IdentExp)value;
                if(identVal.ident.equals(place))
                    return true;
            } else if(value instanceof UnExp){
                UnExp unaryVal = (UnExp)value;
                if(unaryVal.right instanceof IdentExp){
                    IdentExp rightVal = (IdentExp)unaryVal.right;
                    if(rightVal.ident.equals(place))
                        return true;
                }
            } else if(value instanceof BinExp){
                BinExp binVal = (BinExp)value;
                
                if(binVal.left instanceof IdentExp){
                    IdentExp leftVal = (IdentExp)binVal.left;
                    if(leftVal.ident.equals(place))
                        return true;
                }

                if(binVal.right instanceof IdentExp){
                    IdentExp rightVal = (IdentExp)binVal.right;
                    if(rightVal.ident.equals(place))
                        return true;
                }
            }
        } else if(dataCode instanceof If){
            If ifStat = (If)dataCode;
            BinExp expression = ifStat.exp;

            if(expression.left instanceof IdentExp){
                IdentExp leftIdent = (IdentExp)expression.left;
                if(leftIdent.ident.equals(place))
                    return true;
            }

            if(expression.right instanceof IdentExp){
                IdentExp rightIdent = (IdentExp)expression.right;
                if(rightIdent.ident.equals(place))
                    return true;
            }
        } else if(dataCode instanceof ExternalPlace){
            ExternalPlace placement = (ExternalPlace)dataCode;
            if(placement.place.equals(place))
                return true;

            if(placement.retPlace.equals(place))
                return true;
        } else if(dataCode instanceof InternalPlace){
            InternalPlace placement = (InternalPlace)dataCode;
            if(placement.place.equals(place))
                return true;
            
            if(placement.retPlace.equals(place))
                return true;
        } else if(dataCode instanceof Inline){
            Inline inlineAssembly = (Inline)dataCode;
            for(String arg : inlineAssembly.param){
                if(arg.equals(place))
                    return true;
            }
        } else if(dataCode instanceof ExternalCall){
            ExternalCall call = (ExternalCall)dataCode;
            for(String arg: call.arguments){
                if(arg.equals(place))
                    return true;
            }
        } else if(dataCode instanceof Call){
            Call call = (Call)dataCode;
            for(Tuple<String, String> arg : call.params){
                if(arg.source.equals(place))
                    return true;

                if(arg.dest.equals(place))
                    return true;
            }
        } else if(dataCode instanceof ParamAssign){
            ParamAssign assign = (ParamAssign)dataCode;
            if(assign.newPlace.equals(place))
                return true;

            if(assign.paramPlace.equals(place))
                return true;
        } else {
            return false;
        }
        return false;
    }

    public static boolean labelExistsInProgram(String label, Prog program){
        if(labelExistsInLibrary(label, program))
            return true;

        CodeSec codeSec = program.code;
        for(int i = 0; i < codeSec.getLength(); i++){
            ICode instr = codeSec.getInstruction(i);
            if(labelExistsInICode(label, instr))
                return true;
        }

        return false;
    }

    public static boolean placeExistsInProgram(String place, Prog program){
        if(placeExistsInLibrary(place, program))
            return true;

        CodeSec codeSec = program.code;
        for(int i = 0; i < codeSec.getLength(); i++){
            ICode instr = codeSec.getInstruction(i);
            if(placeExistsInICode(place, instr))
                return true;
        }

        return false;
    }

    public static boolean labelExistsInLibrary(String label, Lib lib){
        ProcSec procedures = lib.procedures;
        for(int i = 0; i < procedures.getLength(); i++){
            Proc procedure = procedures.getProcedureByIndex(i);
            if(labelExistsInProcedure(label, procedure))
                return true;
        }

        return false;
    }

    public static boolean placeExistsInLibrary(String place, Lib lib){
        SymSec symbols = lib.symbols;
        for(int i = 0; i < symbols.getLength(); i++){
            SymEntry entry = symbols.getEntryByIndex(i);
            if(entry.icodePlace.equals(place))
                return true;
        }

        DataSec dataCodes = lib.variables;
        for(int i = 0; i < dataCodes.getLength(); i++){
            ICode dataCode = dataCodes.getInstruction(i);
            if(placeExistsInICode(place, dataCode))
                return true;
        }

        ProcSec procedures = lib.procedures;
        for(int i = 0; i < procedures.getLength(); i++){
            Proc procedure = procedures.getProcedureByIndex(i);
            if(placeExistsInProcedure(place, procedure))
                return true;
        }

        return false;
    }

    private static boolean placeExistsInProcedure(String place, Proc procedure){
        for(ParamAssign assign: procedure.paramAssign){
            if(placeExistsInICode(place, assign))
                return true;
        }

        for(ICode icode : procedure.instructions){
            if(placeExistsInICode(place, icode))
                return true;
        }

        if(procedure.placement != null)
            if(placeExistsInICode(place, procedure.placement))
                return true;

        return false;
    }

    private static boolean labelExistsInProcedure(String label, Proc procedure){
        for(ICode icode : procedure.instructions){
            if(labelExistsInICode(label, icode))
                return true;
        }

        return false;
    }
}
