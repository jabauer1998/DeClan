package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CopyInt;
import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmAddressDescriptor;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmDescriptorState;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterDescriptor;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterElement;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterResult;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Type;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Spill;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.inline.InlineParam;
import io.github.H20man13.DeClan.common.interfere.InterferenceGraph;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.main.MyOptimizer;

public class RegisterAllocatorAnalysis extends IterativeAnalysis<ICode, HashMap<ICode, ArmDescriptorState>, ArmDescriptorState> implements 
CustomMeet<ArmDescriptorState> {
	private LiveVariableAnalysis anal;
	
	public RegisterAllocatorAnalysis(MyOptimizer optimizer, Config cfg){
		super(genFlowGraph(optimizer), Direction.FORWARDS, new ArmDescriptorState(), false, cfg, Utils.getClassType(HashMap.class));
		optimizer.runLiveVariableAnalysis();
		this.anal = optimizer.getLiveVariableAnalysis();
	}
	
	private static FlowGraph genFlowGraph(MyOptimizer opt) {
		opt.buildFlowGraph();
		return opt.getFlowGraph();
	}
	
	private static boolean canidateHasAllValuesInOtherPlaces(String reg, ArmDescriptorState state){
		Set<Tuple<CopyStr, ICode.Type>> addrs = state.getCandidateAddresses(reg);
		for(Tuple<CopyStr, ICode.Type> addr: addrs) {
			if(!state.isContainedInMoreThenOnePlace(addr.source.toString(), addr.dest))
				return false;
		}
		return true;
	}
	
	private static boolean canidateIsResultAndOnlyOperand(String reg, String defOp, ICode.Type type1, String otherOp, ICode.Type type2, ArmDescriptorState state) {
		Set<Tuple<CopyStr, ICode.Type>> addressesInR = state.getCandidateAddresses(reg);
		for(Tuple<CopyStr, ICode.Type> addr: addressesInR){
			boolean fstHalf = addr.equals(new Tuple<CopyStr, ICode.Type>(ConversionUtils.newS(defOp), type1));
			boolean sndHalf = !addr.equals(new Tuple<CopyStr, ICode.Type>(ConversionUtils.newS(otherOp), type2));
			if(!(fstHalf && sndHalf))
				return false;
		}
		return true;
	}
	
	private static boolean canidateIsResultAndSingleOperand(String reg, String defOp, ICode.Type type, String  otherOp1, ICode.Type op1Type, String  otherOp2, ICode.Type op2Type, ArmDescriptorState state) {
		Set<Tuple<CopyStr, ICode.Type>> addressesInR = state.getCandidateAddresses(reg);
		for(Tuple<CopyStr, ICode.Type> addr: addressesInR) {
			if(!addr.source.toString().equals(defOp) && !(addr.source.toString().equals(otherOp1) || addr.toString().equals(otherOp2)))
				return false; //TODO -- NEED TO FIX
		}
		return true;
	}
	
	private boolean candidateIsAllDead(String reg, ICode instr, ArmDescriptorState state) {
		Set<Tuple<CopyStr, ICode.Type>> addressesInR = state.getCandidateAddresses(reg);
		for(Tuple<CopyStr, ICode.Type> addr: addressesInR){
			if(anal.getOutputSet(instr).contains(addr.source.toString()))
				return false;
		}
		return true;
	}
	
	private static void addToMemory(String label, ICode.Type type, ArmDescriptorState state){
		state.addAddress(label, type);
	}
	
	private void loadUseReg(String place, ICode.Type type, ICode icode, String defPlace, ICode.Type typeDef, String otherPlace, ICode.Type otherType, ArmDescriptorState state) {
		if(state.containsPlaceInReg(place, type)){
			//Do nothing
		} else if(state.containsEmptyReg()) {
			String reg = state.pickEmptyReg();
			state.addRegValuePair(place, type, reg);
		} else {
			boolean regOk = false;
			for(String reg: state.getCanditateRegs()){
				if(canidateHasAllValuesInOtherPlaces(reg, state)){
					Set<Tuple<CopyStr, ICode.Type>> str = state.getCandidateAddresses(reg);
					state.clearRegisterAddresses(reg);
					for(Tuple<CopyStr, ICode.Type> addr: str){
						state.removeRegFromAddress(reg, addr.source.toString(), addr.dest);
					}
					state.addRegValuePair(place, type, reg);
					regOk = true;
					break;
				} else if(defOrOperandArentNull(defPlace, typeDef, otherPlace, otherType) && canidateIsResultAndOnlyOperand(reg, defPlace, typeDef, otherPlace, otherType, state)) {
					Set<Tuple<CopyStr, ICode.Type>> str = state.getCandidateAddresses(reg);
					state.clearRegisterAddresses(reg);
					for(Tuple<CopyStr, ICode.Type> addr: str){
						state.removeRegFromAddress(reg, addr.source.toString(), addr.dest);
					}
					state.addRegValuePair(place, type, reg);
					regOk = true;
					break;
				} else if(candidateIsAllDead(reg, icode, state)) {
					Set<Tuple<CopyStr, ICode.Type>> str = state.getCandidateAddresses(reg);
					state.clearRegisterAddresses(reg);
					for(Tuple<CopyStr, ICode.Type> addr: str){
						state.removeRegFromAddress(reg, addr.source.toString(), addr.dest);
					}
					state.addRegValuePair(place, type, reg);
					regOk = true;
					break;
				}
			}
			
			if(regOk == false){
				int lowest = Integer.MAX_VALUE;
				String myReg = null;
				for(String reg: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addrs = state.getCandidateAddresses(reg);
					if(addrs.size() < lowest) {
						myReg = reg;
						lowest = addrs.size();
					}
				}
				
				if(myReg != null) {
					Set<Tuple<CopyStr, ICode.Type>> addrs = state.getCandidateAddresses(myReg);
					for(Tuple<CopyStr, ICode.Type> addr: addrs) {
						state.addSpill(addr.source.toString(), addr.dest);
					}
				}
			}
		}
	}
	
	private boolean defOrOperandArentNull(String defPlace, ICode.Type typeDef, String otherPlace, ICode.Type otherType) {
		return (defPlace != null && typeDef != null) || (otherPlace != null && otherType != null);
	}

	private void loadNewReg(String place, ICode.Type type, ICode icode, String operand1, ICode.Type op1Type, String operand2Opt,  ICode.Type op2Type, ArmDescriptorState state) {
		if(state.containsOnlyPlaceInAReg(place, type)){
			//Do nothing
		} else if(state.containsEmptyReg()) {
			String reg = state.pickEmptyReg();
			state.addRegValuePair(place, type, reg);
		} else {
			boolean regOk = false;
			for(String reg: state.getCanditateRegs()){
				if(canidateHasAllValuesInOtherPlaces(reg, state)){
					Set<Tuple<CopyStr, ICode.Type>> str = state.getCandidateAddresses(reg);
					state.clearRegisterAddresses(reg);
					for(Tuple<CopyStr, ICode.Type> addr: str){
						state.removeRegFromAddress(reg, addr.source.toString(), addr.dest);
					}
					state.addRegValuePair(place, type, reg);
					regOk = true;
					break;
				} else if(((operand1 != null && op1Type != null) || (operand2Opt != null && op2Type != null)) && canidateIsResultAndSingleOperand(reg, place, type, operand1, op1Type, operand2Opt, op2Type, state)) {
					Set<Tuple<CopyStr, ICode.Type>> str = state.getCandidateAddresses(reg);
					state.clearRegisterAddresses(reg);
					for(Tuple<CopyStr, ICode.Type> addr: str){
						state.removeRegFromAddress(reg, addr.source.toString(), addr.dest);
					}
					state.addRegValuePair(place, type, reg);
					regOk = true;
					break;
				} else if(candidateIsAllDead(reg, icode, state)) {
					Set<Tuple<CopyStr, ICode.Type>> str = state.getCandidateAddresses(reg);
					state.clearRegisterAddresses(reg);
					for(Tuple<CopyStr, ICode.Type> addr: str){
						state.removeRegFromAddress(reg, addr.source.toString(), addr.dest);
					}
					state.addRegValuePair(place, type, reg);
					regOk = true;
					break;
				}
			}
			
			if(regOk == false){
				int lowest = Integer.MAX_VALUE;
				String myReg = null;
				for(String reg: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addrs = state.getCandidateAddresses(reg);
					if(addrs.size() < lowest) {
						myReg = reg;
						lowest = addrs.size();
					}
				}
				
				if(myReg != null) {
					Set<Tuple<CopyStr, ICode.Type>> addrs = state.getCandidateAddresses(myReg);
					for(Tuple<CopyStr, ICode.Type> addr: addrs) {
						state.addSpill(addr.source.toString(), addr.dest);
					}
				}
			}
		}
	}
	
	private ICode.Type opToArgType(BinExp.Operator op){
		switch(op) {
		case BEQ: return ICode.Type.BOOL;
		case BNE: return ICode.Type.BOOL;
		case GE: return ICode.Type.INT;
		case GT: return ICode.Type.INT;
		case IADD: return ICode.Type.INT;
		case IAND: return ICode.Type.INT;
		case IEQ: return ICode.Type.INT;
		case ILSHIFT: return ICode.Type.INT;
		case INE: return ICode.Type.INT;
		case IOR: return ICode.Type.INT;
		case IRSHIFT: return ICode.Type.INT;
		case ISUB: return ICode.Type.INT;
		case IXOR: return ICode.Type.INT;
		case LAND: return ICode.Type.BOOL;
		case LE: return ICode.Type.INT;
		case LOR: return ICode.Type.BOOL;
		case LT: return ICode.Type.INT;
		default: throw new RuntimeException();
		}
	}
	
	private ICode.Type opToArgType(UnExp.Operator op){
		switch(op) {
		case BNOT: return ICode.Type.BOOL;
		case INOT: return ICode.Type.INT;
		default: throw new RuntimeException();
		}
	}
	
	private void getRegs(ICode icode, ArmDescriptorState state){
		if(icode instanceof Def){
			Def def = (Def)icode;
			if(def.val instanceof BinExp) {
				BinExp exp = (BinExp)def.val;
				loadUseReg(exp.left.ident, opToArgType(exp.op), icode, def.label, def.type, exp.right.ident, opToArgType(exp.op), state);
				loadUseReg(exp.right.ident, opToArgType(exp.op), icode, def.label, def.type, exp.left.ident, opToArgType(exp.op), state);
				loadNewReg(def.label, def.type, icode, exp.left.ident, opToArgType(exp.op), exp.right.ident, opToArgType(exp.op), state);
			} else if(def.val instanceof UnExp) {
				UnExp exp = (UnExp)def.val;
				loadUseReg(exp.right.ident, opToArgType(exp.op), icode, def.label, def.type, null, null, state);
				loadNewReg(def.label, def.type, icode, exp.right.ident, opToArgType(exp.op), null, null, state);
			} else if(def.val instanceof IdentExp) {
				IdentExp exp = (IdentExp)def.val;
				
				loadUseReg(exp.ident, def.type, icode, def.label, def.type, null, null, state);
				loadDataToReg(exp.ident, def.type, def.label, def.type, state);
			} else {
				addToMemory(def.label, def.type, state);
			}
		} else if(icode instanceof Assign) {
			Assign def = (Assign)icode;
			if(def.value instanceof BinExp) {
				BinExp exp = (BinExp)def.value;
				loadUseReg(exp.left.ident, opToArgType(exp.op), icode, def.place, def.getType(), exp.right.ident, opToArgType(exp.op), state);
				loadUseReg(exp.right.ident, opToArgType(exp.op), icode, def.place, def.getType(), exp.left.ident, opToArgType(exp.op), state);
				loadNewReg(def.place, def.getType(), icode, exp.left.ident, opToArgType(exp.op), exp.right.ident, opToArgType(exp.op), state);
			} else if(def.value instanceof UnExp) {
				UnExp exp = (UnExp)def.value;
				
				loadUseReg(exp.right.ident, opToArgType(exp.op), icode, def.place, def.getType(), null, null, state);
				loadNewReg(def.place, def.getType(), icode, exp.right.ident, opToArgType(exp.op), null, null, state);
			} else if(def.value instanceof IdentExp) {
				IdentExp exp = (IdentExp)def.value;
				
				loadUseReg(exp.ident, def.getType(), icode, def.place, def.getType(), null, null, state);
				loadDataToReg(exp.ident, def.getType(), def.place, def.getType(), state);
			} else {
				throw new RuntimeException();
			}
		} else if(icode instanceof Call) {
			Call cicode = (Call)icode;
			for(Def param: cicode.params) {
				if(param.val instanceof BinExp) {
					BinExp exp = (BinExp)param.val;
					loadUseReg(exp.left.ident, opToArgType(exp.op), icode, param.label, param.type, exp.right.ident, opToArgType(exp.op), state);
					loadUseReg(exp.right.ident, opToArgType(exp.op), icode, param.label, param.type, exp.left.ident, opToArgType(exp.op), state);
					loadNewReg(param.label, param.type, icode, exp.left.ident, opToArgType(exp.op), exp.right.ident, opToArgType(exp.op), state);
				} else if(param.val instanceof UnExp) {
					UnExp exp = (UnExp)param.val;
					loadUseReg(exp.right.ident, opToArgType(exp.op), icode, param.label, param.type, null, null, state);
					loadNewReg(param.label, param.type, icode, exp.right.ident, opToArgType(exp.op), null, null, state);
				} else if(param.val instanceof IdentExp) {
					IdentExp exp = (IdentExp)param.val;
					
					loadUseReg(exp.ident, param.type, icode, param.label, param.type, null, null, state);
					loadDataToReg(exp.ident, param.type, param.label, param.type, state);
				} else {
					throw new RuntimeException();
				}
			}
		} else if(icode instanceof If) {
			If ifStat = (If)icode;
			
			BinExp exp = ifStat.exp;
			loadUseReg(exp.left.ident, opToArgType(exp.op), icode, null, null, exp.right.ident, opToArgType(exp.op), state);
			loadUseReg(exp.right.ident, opToArgType(exp.op), icode, null, null, exp.left.ident, opToArgType(exp.op), state);
		} else if(icode instanceof Inline) {
			Inline in = (Inline)icode;
			
			List<InlineParam> uses = new LinkedList<InlineParam>();
			List<InlineParam> defs = new LinkedList<InlineParam>();
			for(InlineParam param: in.params) {
				if(param.containsAllQual(InlineParam.IS_DEFINITION | InlineParam.IS_REGISTER)) {
					defs.add(param);
					
				} else if(param.containsAllQual(InlineParam.IS_USE | InlineParam.IS_REGISTER)) {
					uses.add(param);
					loadUseReg(param.name.ident, param.type, in, null, null, null, null, state);
				}
			}
			
			if(defs.isEmpty()) {
				if(uses.size() > 1) {
					InlineParam use1 = uses.get(0);
					InlineParam use2 = uses.get(1);
					
					loadUseReg(use1.name.ident, use1.type, icode, null, null, use2.name.ident, use2.type, state);
				} else if(!uses.isEmpty()) {
					InlineParam use1 = uses.get(0);
					
					loadUseReg(use1.name.ident, use1.type, icode, null, null, null, null, state);
				}
			} else {
				if(uses.size() > 1) {
					InlineParam def = defs.get(0);
					InlineParam use1 = uses.get(0);
					InlineParam use2 = uses.get(1);
					
					loadUseReg(use1.name.ident, use1.type, icode, def.name.ident, def.type, use2.name.ident, use2.type, state);
				} else if(!uses.isEmpty()) {
					InlineParam def = defs.get(0);
					InlineParam use1 = uses.get(0);
					
					loadUseReg(use1.name.ident, use1.type, icode, def.name.ident, def.type, null, null, state);
				}
			}
			
			for(InlineParam def: defs) {
				if(uses.size() > 1) {
					InlineParam uses1 = uses.get(0);
					InlineParam uses2 = uses.get(1);
					loadNewReg(def.name.ident, def.type, in, uses1.name.ident, uses1.type, uses2.name.ident, uses2.type, state);
				} else if(!uses.isEmpty()) {
					InlineParam uses1 = uses.get(0);
					loadNewReg(def.name.ident, def.type, in, uses1.name.ident, uses1.type, null, null, state);
				} else {
					loadNewReg(def.name.ident, def.type, in, null, null, null, null, state);
				}
			}
		} else if(icode instanceof Spill) {
			Spill spill = (Spill)icode;
			Set<String> regsToEmpty = new HashSet<String>();
			
			for(String reg: state.getCanditateRegs()){
				Set<Tuple<CopyStr, ICode.Type>> addrs = state.getCandidateAddresses(reg);
				for(Tuple<CopyStr, ICode.Type> addr: addrs){
					if(addr.source.toString().equals(spill.name)){
						regsToEmpty.add(reg);
						break;
					}
				}
			}
			
			for(String reg: regsToEmpty) {
				state.clearRegisterAddresses(reg);
			}
		}
	}

	private void loadDataToReg(String ident, Type type, String label, Type type2, ArmDescriptorState state){
		for(String reg: state.getCanditateRegs()){
			Set<Tuple<CopyStr, Type>> addrs = state.getCandidateAddresses(reg);
			for(Tuple<CopyStr, Type> addr: addrs){
				if(addr.source.toString().equals(ident)){
					if(addr.dest == type){
						state.addRegValuePair(label, type2, reg);
					}
				}
			}
		}
	}

	@Override
	public ArmDescriptorState transferFunction(ICode type, ArmDescriptorState inputState) {
		ArmDescriptorState state = inputState.copy();
		getRegs(type, state);
		return state;
	}

	@Override
	protected HashMap<ICode, ArmDescriptorState> copyOutputsFromFlowGraph(FlowGraph flow) {
		HashMap<ICode, ArmDescriptorState> newMap = newMap();
    	for(FlowGraphNode node: flow) {
    		if(node instanceof BlockNode) {
    			BlockNode block = (BlockNode)node;
    			List<ICode> code = block.getICode();
    			if(code.size() > 0) {
    				ICode last = code.getLast();
    				newMap.put(last, getOutputSet(last).copy());
    			}
    		}
    	}
    	return newMap;
	}

	@Override
	protected HashMap<ICode, ArmDescriptorState> copyInputsFromFlowGraph(FlowGraph flow) {
		HashMap<ICode, ArmDescriptorState> toRet = newMap();
		for(FlowGraphNode node: flow)
			if(node instanceof BlockNode) {
				BlockNode block = (BlockNode)node;
				List<ICode> icode = block.getICode();
				if(!icode.isEmpty()){
					ICode first = icode.getFirst();
					toRet.put(first, this.getInputSet(first).copy());
				}
			}
		return toRet;
	}

	@Override
	protected boolean changesHaveOccuredOnOutputs(HashMap<ICode, ArmDescriptorState> cached) {
		if(cfg == null || !cfg.containsFlag("debug")){
			Set<ICode> keys = cached.keySet();
	        for(ICode key : keys){
	            if(!containsOutputKey(key)){
	                return true;
	            }

	            ArmDescriptorState actualData = getOutputSet(key);
	            ArmDescriptorState cachedData = cached.get(key);

	            if(!actualData.equals(cachedData)){
	                return true;
	            }
	        }
	        return false;
    	} else {
    		Utils.createFile("test/temp/AnalysisCacheLog.txt");
    		boolean result = false;
    		Set<ICode> keys = cached.keySet();
	        for(ICode key : keys){
	            if(!containsOutputKey(key)){
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Actual map contains " + key + " and cached data does not\r\n");
	                result = true;
	                continue;
	            }

	            ArmDescriptorState actualData = getOutputSet(key);
	            ArmDescriptorState cachedData = cached.get(key);

	            if(!actualData.equals(cachedData)){
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "For " + key + " actual data not equal to cache data\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Actual data: " + actualData.toString() + "\r\n");
	            	Utils.appendToFile("test/temp/AnalysisCacheLog.txt", "Cached data: " + cachedData.toString() + "\r\n");
	                result = true;
	                continue;
	            }
	        }
	        
	        return result;
    	}
	}

	@Override
	protected boolean changesHaveOccuredOnInputs(HashMap<ICode, ArmDescriptorState> cached) {
		Set<ICode> keys = cached.keySet();
        for(ICode key : keys){
            if(!containsInputKey(key)){
                return true;
            }

            ArmDescriptorState actualData = getInputSet(key);
            ArmDescriptorState cachedData = cached.get(key);

            if(!actualData.equals(cachedData)){
                return true;
            }
        }

        return false;
	}

	@Override
	protected void runAnalysis(FlowGraph flowGraph, Direction direction,
			Function<List<ArmDescriptorState>, ArmDescriptorState> meetOperation, ArmDescriptorState semiLattice,
			boolean copyKey) {
		for(BlockNode block : flowGraph.getBlocks()){
        	if(block.getICode().size() > 0) {
        		ICode lastICode = block.getICode().getLast();
        		ArmDescriptorState semilatticeCopy = semiLattice.copy();
                if(copyKey)
                	addOutputSet(lastICode.copy(), semilatticeCopy);
                else
                	addOutputSet(lastICode, semilatticeCopy);
        	}
        }

        HashMap<ICode, ArmDescriptorState> outputCache = null;
        do{
        	analysisLoopStartAction();
        	outputCache = copyOutputsFromFlowGraph();
            
            for(BlockNode block : flowGraph.getBlocks()){
                ArmDescriptorState inputSet = new ArmDescriptorState();

                List<ArmDescriptorState> predecessorsLists = new LinkedList<ArmDescriptorState>();
                for(FlowGraphNode node : block.getPredecessors()){
                	if(!node.getICode().isEmpty()) {
                		ArmDescriptorState outputOfPredecessor = super.getOutputSet(node.getICode().getLast());
                		predecessorsLists.add(outputOfPredecessor);
                	}
                }

                inputSet = meetOperation.apply(predecessorsLists);

                for(int instrIndex = 0; instrIndex < block.getICode().size(); instrIndex++){
                	ICode instr = block.getICode().get(instrIndex);
                	if(copyKey) {
                		ICode copy = instr.copy();
                		addInputSet(copy, inputSet);
                        inputSet = transferFunction(instr, inputSet);
                        addOutputSet(copy, inputSet);
                        if(inputSet.containsSpill()) {
                        	List<Tuple<CopyStr, ICode.Type>> mySpills = inputSet.getSpill();
                        	for(Tuple<CopyStr, ICode.Type> mySpill: mySpills) {
                        		block.getICode().add(instrIndex, new Spill(mySpill.source.toString(), mySpill.dest));
                        		instrIndex++;
                        	}
                        }
                	} else {
                		addInputSet(instr, inputSet);
                        inputSet = transferFunction(instr, inputSet);
                        addOutputSet(instr, inputSet);
                        if(inputSet.containsSpill()) {
                        	List<Tuple<CopyStr, ICode.Type>> mySpills = inputSet.getSpill();
                        	for(Tuple<CopyStr, ICode.Type> mySpill: mySpills) {
                        		block.getICode().add(instrIndex, new Spill(mySpill.source.toString(), mySpill.dest));
                        		instrIndex++;
                        	}
                        }
                	}
                }
            }
            analysisLoopEndAction();
        } while(changesHaveOccuredOnOutputs(outputCache));
	}

	@Override
	public ArmDescriptorState performMeet(List<ArmDescriptorState> li) {
		ArmDescriptorState state = new ArmDescriptorState();
		
		for(ArmDescriptorState myState : li){
			for(String myReg : myState.getCanditateRegs()) {
				Set<Tuple<CopyStr, ICode.Type>> armAddresses = myState.getCandidateAddresses(myReg);
				Set<Tuple<CopyStr, ICode.Type>> curAddresses = state.getCandidateAddresses(myReg);
				if(curAddresses.isEmpty())
					for(Tuple<CopyStr, ICode.Type> addr: armAddresses)
						state.addRegValuePair(addr.source.toString(), addr.dest, myReg);
				else if(!curAddresses.equals(armAddresses)) {
					for(Tuple<CopyStr, ICode.Type> addr: armAddresses){
						state.addSpill(addr.source.toString(), addr.dest);
					}
				}
			}
		}
		
		for(ArmDescriptorState myState : li){
			Set<Tuple<CopyStr, ICode.Type>> addrs = myState.getAllAdresses();
			for(Tuple<CopyStr, ICode.Type> addr: addrs){
				Set<Tuple<CopyStr, ICode.Type>> addrsInAddr = myState.getAddressesWithPlace(addr.source.toString(), addr.dest);
				for(Tuple<CopyStr, ICode.Type> addrInAddr: addrsInAddr)
					state.addAddressPair(addr.source.toString(), addr.dest, addrInAddr.source.toString(), addrInAddr.dest);
			}
		}
		
		return state;
	}
	
	public ArmRegisterResult getFilteredInputSet(ICode icode){
		ArmRegisterResult retSet = new ArmRegisterResult();
		ArmDescriptorState state = super.getInputSet(icode);
		if(state == null)
			return new ArmRegisterResult();
		if(icode instanceof Def) {
			Def def = (Def)icode;
			if(def.val instanceof BinExp) {
				BinExp exp = (BinExp)def.val;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.label)){
						retSet.addResult(elem, elem);
					} else if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
						retSet.addResult(exp.left.ident, elem);
					} else if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
						retSet.addResult(exp.right.ident, elem);
					}
				}
			} else if(def.val instanceof UnExp) {
				UnExp un = (UnExp)def.val;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.label)){
						retSet.addResult(def.label, elem);
					} else if(ConversionUtils.setContainsName(addr, un.right.ident)) {
						retSet.addResult(un.right.ident, elem);
					}
				}
			} else if(def.val instanceof IdentExp) {
				IdentExp ident = (IdentExp)def.val;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.label)){
						retSet.addResult(def.label, elem);
					} else if(ConversionUtils.setContainsName(addr, ident.ident)) {
						retSet.addResult(ident.ident, elem);
					}
				}
			}
		} else if(icode instanceof Assign) {
			Assign def = (Assign)icode;
			if(def.value instanceof BinExp) {
				BinExp exp = (BinExp)def.value;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.place)){
						retSet.addResult(def.place, elem);
					} else if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
						retSet.addResult(exp.left.ident, elem);
					} else if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
						retSet.addResult(exp.right.ident, elem);
					}
				}
			} else if(def.value instanceof UnExp) {
				UnExp un = (UnExp)def.value;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.place)){
						retSet.addResult(def.place, elem);
					} else if(ConversionUtils.setContainsName(addr, un.right.ident)) {
						retSet.addResult(un.right.ident, elem);
					}
				}
			} else if(def.value instanceof IdentExp) {
				IdentExp ident = (IdentExp)def.value;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.place)){
						retSet.addResult(def.place, elem);
					} else if(ConversionUtils.setContainsName(addr, ident.ident)) {
						retSet.addResult(ident.ident, elem);
					}
				}
			}
		} else if(icode instanceof Call) {
			Call cICode = (Call)icode;
			for(Def def: cICode.params) {
				if(def.val instanceof BinExp) {
					BinExp exp = (BinExp)def.val;
					for(String elem: state.getCanditateRegs()) {
						Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
						if(ConversionUtils.setContainsName(addr, def.label)){
							retSet.addResult(def.label, elem);
						} else if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
							retSet.addResult(exp.left.ident, elem);
						} else if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
							retSet.addResult(exp.right.ident, elem);
						}
					}
				} else if(def.val instanceof UnExp) {
					UnExp un = (UnExp)def.val;
					for(String elem: state.getCanditateRegs()) {
						Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
						if(ConversionUtils.setContainsName(addr, def.label)){
							retSet.addResult(def.label, elem);
						} else if(ConversionUtils.setContainsName(addr, un.right.ident)) {
							retSet.addResult(un.right.ident, elem);
						}
					}
				} else if(def.val instanceof IdentExp) {
					IdentExp ident = (IdentExp)def.val;
					for(String elem: state.getCanditateRegs()) {
						Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
						if(ConversionUtils.setContainsName(addr, def.label)){
							retSet.addResult(def.label, elem);
						} else if(ConversionUtils.setContainsName(addr, ident.ident)) {
							retSet.addResult(ident.ident, elem);
						}
					}
				}
			}
		} else if(icode instanceof If) {
			If ifStat = (If)icode;
			
			BinExp exp = ifStat.exp;
			for(String elem: state.getCanditateRegs()) {
				Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
				if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
					retSet.addResult(exp.left.ident, elem);
				} else if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
					retSet.addResult(exp.right.ident, elem);
				}
			}
		} else if(icode instanceof Inline) {
			Inline inline = (Inline)icode;
			for(String elem: state.getCanditateRegs()) {
				Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
				for(InlineParam param: inline.params){
					if(param.containsAnyQual(InlineParam.IS_REGISTER)) {
						if(ConversionUtils.setContainsName(addr, param.name.ident)) {
							retSet.addResult(param.name.ident, elem);
						}
					}
				}
			}
		}
		
		return retSet;
	}
	
	public ArmRegisterResult getFilteredOutputSet(ICode icode){
		ArmRegisterResult retSet = new ArmRegisterResult();
		ArmDescriptorState state = super.getOutputSet(icode);
		if(state == null)
			return new ArmRegisterResult();
		if(icode instanceof Def) {
			Def def = (Def)icode;
			if(def.val instanceof BinExp) {
				BinExp exp = (BinExp)def.val;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.label)){
						retSet.addResult(def.label, elem);
					}
					if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
						retSet.addResult(exp.left.ident, elem);
					}
					if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
						retSet.addResult(exp.right.ident, elem);
					}
				}
			} else if(def.val instanceof UnExp) {
				UnExp un = (UnExp)def.val;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.label)){
						retSet.addResult(def.label, elem);
					}
					if(ConversionUtils.setContainsName(addr, un.right.ident)) {
						retSet.addResult(un.right.ident, elem);
					}
				}
			} else if(def.val instanceof IdentExp) {
				IdentExp ident = (IdentExp)def.val;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.label)){
						retSet.addResult(def.label, elem);
					}
					if(ConversionUtils.setContainsName(addr, ident.ident)) {
						retSet.addResult(ident.ident, elem);
					}
				}
			}
		} else if(icode instanceof Assign) {
			Assign def = (Assign)icode;
			if(def.value instanceof BinExp) {
				BinExp exp = (BinExp)def.value;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.place)){
						retSet.addResult(def.place, elem);
					}
					if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
						retSet.addResult(exp.left.ident, elem);
					}
					if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
						retSet.addResult(exp.right.ident, elem);
					}
				}
			} else if(def.value instanceof UnExp) {
				UnExp un = (UnExp)def.value;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.place)){
						retSet.addResult(def.place, elem);
					}
					if(ConversionUtils.setContainsName(addr, un.right.ident)) {
						retSet.addResult(un.right.ident, elem);
					}
				}
			} else if(def.value instanceof IdentExp) {
				IdentExp ident = (IdentExp)def.value;
				for(String elem: state.getCanditateRegs()) {
					Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
					if(ConversionUtils.setContainsName(addr, def.place)){
						retSet.addResult(def.place, elem);
					}
					if(ConversionUtils.setContainsName(addr, ident.ident)) {
						retSet.addResult(ident.ident, elem);
					}
				}
			}
		} else if(icode instanceof Call) {
			Call cICode = (Call)icode;
			for(Def def: cICode.params) {
				if(def.val instanceof BinExp) {
					BinExp exp = (BinExp)def.val;
					for(String elem: state.getCanditateRegs()) {
						Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
						if(ConversionUtils.setContainsName(addr, def.label)){
							retSet.addResult(def.label, elem);
						}
						if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
							retSet.addResult(exp.left.ident, elem);
						}
						if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
							retSet.addResult(exp.right.ident, elem);
						}
					}
				} else if(def.val instanceof UnExp) {
					UnExp un = (UnExp)def.val;
					for(String elem: state.getCanditateRegs()) {
						Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
						if(ConversionUtils.setContainsName(addr, def.label)){
							retSet.addResult(def.label, elem);
						}
						if(ConversionUtils.setContainsName(addr, un.right.ident)) {
							retSet.addResult(un.right.ident, elem);
						}
					}
				} else if(def.val instanceof IdentExp) {
					IdentExp ident = (IdentExp)def.val;
					for(String elem: state.getCanditateRegs()) {
						Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
						if(ConversionUtils.setContainsName(addr, def.label)){
							retSet.addResult(def.label, elem);
						}
						if(ConversionUtils.setContainsName(addr, ident.ident)) {
							retSet.addResult(ident.ident, elem);
						}
					}
				}
			}
		} else if(icode instanceof If) {
			If ifStat = (If)icode;
			
			BinExp exp = ifStat.exp;
			for(String elem: state.getCanditateRegs()) {
				Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
				if(ConversionUtils.setContainsName(addr, exp.left.ident)) {
					retSet.addResult(exp.left.ident, elem);
				}
				if(ConversionUtils.setContainsName(addr, exp.right.ident)) {
					retSet.addResult(exp.right.ident, elem);
				}
			}
		} else if(icode instanceof Inline) {
			Inline inline = (Inline)icode;
			for(String elem: state.getCanditateRegs()) {
				Set<Tuple<CopyStr, ICode.Type>> addr = state.getCandidateAddresses(elem);
				for(InlineParam param: inline.params){
					if(param.containsAnyQual(InlineParam.IS_REGISTER)) {
						if(ConversionUtils.setContainsName(addr, param.name.ident)) {
							retSet.addResult(param.name.ident, elem);
						}
					}
				}
			}
		}
		
		return retSet;
	}
}
