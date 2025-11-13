package io.github.H20man13.DeClan.common.interfere;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.iterative.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;

public class InterferenceGraph {
	private Set<Tuple<String, String>> edges;
	private List<Tuple<String, ICode>> nodes;
	
	public InterferenceGraph(Lib program, LiveVariableAnalysis anal){
		this.nodes = new LinkedList<Tuple<String, ICode>>();
		this.edges = new HashSet<Tuple<String, String>>();
		
		for(ICode instr: program){
			if(instr instanceof Def) {
				Def def = (Def)instr;
				addNode(def.label, def);
			} else if(instr instanceof Assign) {
				Assign assign = (Assign)instr;
				addNode(assign.place, assign);
			}
		}
		
		for(Tuple<String, ICode> vert: nodes)
			for(String def: anal.getDefSet(vert.dest))
				for(String out: anal.getOutputSet(vert.dest)) {
					//Since a Interference Graph is an undirected graph add an edge 
					//in both directions
					addEdge(def, out);
					addEdge(out, def);
				}
	}
	
	private int countEdges(String node) {
		int edgeCount = 0;
		for(Tuple<String, String> edge: edges) {
			if(edge.source.equals(node))
				edgeCount++;
		}
		return edgeCount;
	}
	
	private Set<Tuple<String, String>> getEdges(String node){
		Set<Tuple<String, String>> edges = new HashSet<Tuple<String, String>>();
		for(Tuple<String, String> edge: edges){
			if(edge.source.equals(node))
				edges.add(edge);
		}
		return edges;
	}
	
	private void removeGraph(Tuple<String, ICode> node){
		edges.removeIf(new Predicate<Tuple<String, String>>(){
			@Override
			public boolean test(Tuple<String, String> t) {
				return t.source.equals(node.source) || t.dest.equals(node.source);
			}
		});
		nodes.remove(0);
	}
	
	private void simplifyGraph(Stack<InterfereColoredNode> colorStack, Set<String> spill) {
		int K = 14;
		do {
			Tuple<String, ICode> node = nodes.getFirst();
			int edgeCount = countEdges(node.source);
			if(edgeCount < K){
				Set<Tuple<String, String>> edges = getEdges(node.source);
				colorStack.push(new InterfereColoredNode(node, edges, new Color(0)));
				removeGraph(node);
			} else {
				Set<Tuple<String, String>> edges = getEdges(node.source);
				colorStack.push(new InterfereColoredNode(node, edges, new Spill()));
				removeGraph(node);
			}
		} while(!(edges.isEmpty() && nodes.isEmpty()));
	}
	
	private Color pickColor(Tuple<String, ICode> node, Map<String, Color> mapy){
		Set<Integer> colors = new HashSet<Integer>();
		for(Tuple<String, String> edge: edges){
			if(edge.source.equals(node.source)){
				if(mapy.containsKey(edge.dest)){
					colors.add(mapy.get(edge.dest).literalColor());
				}
			}
		}
		
		for(int color = 0; color <= 13; color++) {
			if(!colors.contains(color)) {
				mapy.put(node.source, new Color(color));
			}
		}
		
		return mapy.get(node.source);
	}
	
	private static boolean optimisticColoringSuccess(Map<String, Color> colors, Set<Tuple<String, String>> edges){
		HashSet<Integer> locColors = new HashSet<Integer>();
		for(Tuple<String, String> edge: edges) {
			if(colors.containsKey(edge.dest)){
				locColors.add(colors.get(edge.dest).literalColor());
			}
		}
		
		return locColors.size() < 14;
	}
	
	private Map<String, Color> assignColors(Stack<InterfereColoredNode> colorStack, Set<String> spillSet) {
		HashMap<String, Color> toRet = new HashMap<String, Color>();
		while(!colorStack.isEmpty()){
			InterfereColoredNode t = colorStack.pop();
			if(t.thrd.matches(new Color(0))) {
				nodes.add(t.fst);
				edges.addAll(t.snd);
				Color color = pickColor(t.fst, toRet);
				toRet.put(t.fst.source, color);
			} else if(t.thrd.matches(new Spill())) {
				if (optimisticColoringSuccess(toRet, t.snd)) {
					nodes.add(t.fst);
					edges.addAll(t.snd);
					Color color = pickColor(t.fst, toRet);
					toRet.put(t.fst.source, color);
				} else {
					spillSet.add(t.fst.source);
				}
			}
		}
		return toRet;
	}
	
	public Tuple<Map<String, Color>, Set<String>> colorGraph() {
		Stack<InterfereColoredNode> colorStack = new Stack<InterfereColoredNode>();
		Set<String> spill = new HashSet<String>();
		
		simplifyGraph(colorStack, spill);
		return new Tuple<>(assignColors(colorStack, spill), spill);
	}
	
	public List<Tuple<String, ICode>> getNodes(){
		return nodes;
	}
	
	private void addNode(String node, ICode instr) {
		this.nodes.add(new Tuple<String, ICode>(node, instr));
	}
	
	private void addEdge(String from, String to) {
		edges.add(new Tuple<String, String>(from, to));
	}
	
	
}
