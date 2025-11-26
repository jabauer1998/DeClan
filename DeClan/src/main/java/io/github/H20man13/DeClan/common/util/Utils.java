package io.github.H20man13.DeClan.common.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NameNotFoundException;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.ast.Identifier;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNodeFactory;
import io.github.H20man13.DeClan.common.dag.DagNullNode;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.dag.DagValueNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode;
import io.github.H20man13.DeClan.common.exception.UtilityException;
import io.github.H20man13.DeClan.common.flow.BasicBlock;
import io.github.H20man13.DeClan.common.dag.DagNode.ScopeType;
import io.github.H20man13.DeClan.common.dag.DagNode.ValueType;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.label.StandardLabel;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.LiveInfo;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
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

    public static NullableExp getExpFromSet(Set<Tuple<String, NullableExp>> tuples, NullableExp name){
    	if(name instanceof IdentExp) {
    		IdentExp nExp = (IdentExp)name;
    		for(Tuple<String, NullableExp> tuple : tuples){
                if(tuple.source.equals(nExp.ident)){
                    return tuple.dest;
                }
            }
    	}
        
        throw new UtilityException("getExpFromSet", "Tuple with name " + name + " was not found");
    }

    public static boolean containsExpInSet(Set<Tuple<String, NullableExp>> tuples, NullableExp name){
    	if(name instanceof IdentExp) {
    		IdentExp nExp = (IdentExp)name;
    		for(Tuple<String, NullableExp> tuple : tuples){
                if(tuple.source.equals(nExp.ident)){
                    return true;
                }
            }
    	}
        return false;
    }
    
    public static boolean containsExpInSet(HashSet<Tuple<String, NullableExp>> killSet, String resTest) {
		for(Tuple<String, NullableExp> tuple : killSet){
            if(tuple.source.equals(resTest)){
                return true;
            }
		}
		return false;
    }
    
    public static boolean containsExpInSet(Set<Tuple<Exp, ICode.Type>> set, Exp toSearch) {
    	for(Tuple<Exp, ICode.Type> tuple: set) {
    		if(tuple.source.equals(toSearch))
    			return true;
    	}
    	return false;
    }
    
    public static boolean containsExpInSet(HashSet<Tuple<Exp, String>> set, Exp toSearch) {
    	for(Tuple<Exp, String> tuple: set) {
    		if(tuple.source.equals(toSearch))
    			return true;
    	}
    	return false;
    }
    
    public static String getVar(Set<Tuple<Exp, String>> set, Exp toSearch) {
    	for(Tuple<Exp, String> tuple: set) {
    		if(tuple.source.equals(toSearch))
    			return tuple.dest;
    	}
    	throw new UtilityException("getVar", "Error expression [" + toSearch.toString() + "] not found in set\n" + set.toString());
    }
    
    

    public static DagNode createBinaryNode(boolean isDefinition, ICode.Scope origScope, BinExp.Operator op, String place, DagNode left, DagNode right) {
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        switch(op){
            case IADD: return factory.createIntegerAdditionNode(isDefinition, scope, place, left, right);
            case ISUB: return factory.createIntegerSubtractionNode(isDefinition, scope, place, left, right);
            case LAND: return factory.createLogicalAndNode(isDefinition, scope, place, left, right);
            case IAND: return factory.createBitwiseAndNode(isDefinition, scope, place, left, right);
            case IOR: return factory.createBitwiseOrNode(isDefinition, scope, place, left, right);
            case IXOR: return factory.createBitwiseXorNode(isDefinition, scope, place, left, right);
            case ILSHIFT: return factory.createLeftShiftNode(isDefinition, scope, place, left, right);
            case IRSHIFT: return factory.createRightShiftNode(isDefinition, scope, place, left, right);
            case LOR: return factory.createLogicalOrNode(isDefinition, scope, place, left, right);
            case GT: return factory.createGreaterThanNode(isDefinition, scope, place, left, right);
            case GE: return factory.createGreaterThanOrEqualNode(isDefinition, scope, place, left, right);
            case LT: return factory.createLessThanNode(isDefinition, scope, place, left, right);
            case LE: return factory.createLessThanOrEqualNode(isDefinition, scope, place, left, right);
            case IEQ: return factory.createIntegerEqualsNode(isDefinition, scope, place, left, right);
            case INE: return factory.createIntegerNotEqualsNode(isDefinition, scope, place, left, right);
            case BEQ: return factory.createBooleanEqualsNode(isDefinition, scope, place, left, right);
            case BNE: return factory.createBooleanNotEqualsNode(isDefinition, scope, place, left, right);
            default: throw new UtilityException("createBinaryNode", "Error cant create binary node with operator " + op);
        }
    }

    public static DagNode createUnaryNode(boolean isDefinition, ICode.Scope origScope, UnExp.Operator op, String place, DagNode right){
        ScopeType scope = ConversionUtils.assignScopeToDagScopeType(origScope);
        switch(op){
            case BNOT: return factory.createNotNode(isDefinition, scope, place, right);
            case INOT: return factory.createBitwiseNotNode(isDefinition, scope, place, right);
            default: throw new UtilityException("createUnaryNode", "Cant create Unary Node with operator " + op);
        }
    }

    public static boolean beginningOfBlockIsLabel(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode firstICode = codeInBlock.get(0);
            if(firstICode instanceof StandardLabel){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean endOfBlockIsReturn(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode lastICode = codeInBlock.get(codeInBlock.size() - 1);
            if(lastICode instanceof Return){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean beginningOfBlockIsProcedureHeader(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode firstICode = codeInBlock.get(0);
            if(firstICode instanceof ProcLabel){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public static boolean endOfBlockIsProcedureCall(BasicBlock block) {
    	List<ICode> codeInBlock = block.getIcode();
    	if(codeInBlock.size() > 0){
            ICode lastICode = codeInBlock.get(codeInBlock.size() - 1);
            if(lastICode instanceof Call){
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
            } else if(lastICode instanceof Call){
                return true;
            } else if(lastICode instanceof Return){
            	return true;
            } else if(lastICode instanceof End){
            	return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public static boolean endOfBlockIsEnd(BasicBlock block){
        List<ICode> codeInBlock = block.getIcode();
        if(codeInBlock.size() > 0){
            ICode lastICode = codeInBlock.get(codeInBlock.size() - 1);
            if(lastICode instanceof End){
            	return true;
            } else {
                return false;
            }
        } else {
            return false;
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
  
  public enum WhiteSpaceType{
	  TRAILING,
	  LEADING
  }
  
  public static String padWhiteSpace(String input, int expectedLength, WhiteSpaceType type) {
	  int actualLength = input.length();
	  if(actualLength > expectedLength)
		  throw new UtilityException("padWhiteSpace", "The actual length of string-\n" + input.toString() + "(Length=" + actualLength + ")\n exceded the expected length " + expectedLength);
	  
	  if(actualLength == expectedLength)
		  return input;
	  
	  int numSpaces = expectedLength - actualLength;
	  StringBuilder sb = new StringBuilder();
	  
	  if(type == WhiteSpaceType.LEADING)
		  for(int i = 0; i < numSpaces; i++)
			  sb.append(' ');
	  
	  sb.append(input);
	  
	  if(type == WhiteSpaceType.TRAILING)
		 for(int i = 0; i < numSpaces; i++)
			 sb.append(' ');
	  
	  return sb.toString();
  }

  public static String to32BitBinary(Float realValue){
     Integer asInt = Float.floatToRawIntBits(realValue);
     return to32BitBinary(asInt);
  }

  public static void deleteFile(String fileName){
    File file = new File(fileName);
    if(file.exists()){
        file.delete();
    }
  }
  
  public static void createFile(String fileName) {
	  Utils.deleteFile(fileName);
	  File file = new File(fileName);
	  try {
		  file.createNewFile();
	  } catch(IOException exp) {
		  throw new RuntimeException(exp.toString());
	  }
  }
  
  public static void appendToFile(String fileName, String textToAppend) {
	  File file = new File(fileName);
	  try {
		FileWriter writer = new FileWriter(file, true);
		for(int i = 0; i < textToAppend.length(); i++) {
			char c = textToAppend.charAt(i);
			writer.append(c);
		}
		writer.flush();
		writer.close();
	} catch (IOException e) {
		throw new RuntimeException(e.toString());
	}
  }
  
  public static void writeToFile(String fileName, String textToAppend) {
	  File file = new File(fileName);
	  try {
		  FileWriter writer = new FileWriter(file);
		  for(int i = 0; i < textToAppend.length(); i++) {
			  char c = textToAppend.charAt(i);
			  writer.write(c);
		  }
		  writer.flush();
		  writer.close();
	  } catch(IOException exp) {
		  throw new RuntimeException(exp.toString());
	  }
  }
  
  public static int getLengthOfUnsignedNumber(int number) {
	  int count = 1;
	  while(number >= 10) {
		  count++;
		  number /= 10;
	  }
	  return count;
  }
  
  @SuppressWarnings("unchecked")
  public static <ClassType> Class<ClassType> getClassType(Class<?> type){
	  return (Class<ClassType>)type;
  }

  public static boolean scopeIsGlobal(NullableExp dest) {
	if(dest instanceof IdentExp) {
		IdentExp ident = (IdentExp)dest;
		if(ident.scope == ICode.Scope.GLOBAL)
			return true;
	}
	return false;
  }

  public static boolean beginningOfBlockIsSection(BasicBlock block) {
	List<ICode> intermediateCode = block.getIcode();
	if(intermediateCode.size() > 0) {
		ICode first = intermediateCode.getFirst();
		if(first instanceof SymSec)
			return true;
		else if(first instanceof DataSec)
			return true;
		else if(first instanceof BssSec)
			return true;
		else if(first instanceof CodeSec)
			return true;
		else if(first instanceof ProcSec)
			return true;
	}
	return false;
  }
  
  public static List<String> viewCommonElements(Prog program) {
	  LinkedList<String> myList = new LinkedList<String>();
	  for(int i = 0; i < program.getICode().size(); i++) {
		  ICode instruction = program.getInstruction(i);
		  if(instruction instanceof Assign) {
			  for(int j = i + 1; j < program.getICode().size(); j++) {
				  ICode instruction2 = program.getInstruction(j);
				  if(instruction2 instanceof Assign) {
					  if(instruction.equals(instruction2)) {
						  myList.add("Instruction " + instruction.toString() + " at " + i + " is also at " + j + "\n");
					  }
				  }
			  }
		  } else if(instruction instanceof Call) {
			  for(int j = i + 1; j < program.getICode().size(); j++) {
				  ICode instruction2 = program.getInstruction(j);
				  if(instruction2 instanceof Call) {
					  if(instruction.equals(instruction2)) {
						  myList.add("Instruction " + instruction.toString() + " at " + i + " is also at " + j + "\n");
					  }
				  }
			  }
		  } else if(instruction instanceof Goto) {
			  for(int j = i + 1; j < program.getICode().size(); j++) {
				  ICode instruction2 = program.getInstruction(j);
				  if(instruction2 instanceof Goto) {
					  if(instruction.equals(instruction2)) {
						  myList.add("Instruction " + instruction.toString() + " at " + i + " is also at " + j + "\n");
					  }
				  }
			  }
		  }
	  }
	  return myList;
  }

  public static int posOf(String str, char c, int count) {
	int myCount = 0;
	for(int i = 0; i < str.length(); i++) {
		char at = str.charAt(i);
		if(at == c) {
			myCount++;
			if(myCount == count)
				return i;
		}
	}
	throw new UtilityException("posOf", "Cant find " + count + " of " + c + " in " + "\"" + str + "\"");
  }

  public static <SetType extends Set<?>> SetType createNewSet(Class<SetType> setClass) {
	try {
		return setClass.newInstance();
	} catch(Exception exp) {
		throw new RuntimeException(exp);
	}
  }
}
