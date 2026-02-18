package io.github.h20man13.DeClan.common.interfere;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

import io.github.h20man13.DeClan.common.CopyStr;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.analysis.iterative.LiveVariableAnalysis;
import io.github.h20man13.DeClan.common.icode.Assign;
import io.github.h20man13.DeClan.common.icode.Def;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.Lib;
import io.github.h20man13.DeClan.common.util.ConversionUtils;

public class InterferenceGraph {
        private Set<Tuple<CopyStr, CopyStr>> edges;
        private List<Tuple<CopyStr, ICode>> nodes;
        
        public InterferenceGraph(Lib program, LiveVariableAnalysis anal){
                this.nodes = new LinkedList<Tuple<CopyStr, ICode>>();
                this.edges = new HashSet<Tuple<CopyStr, CopyStr>>();
                
                for(ICode instr: program){
                        if(instr instanceof Def) {
                                Def def = (Def)instr;
                                addNode(def.label, def);
                        } else if(instr instanceof Assign) {
                                Assign assign = (Assign)instr;
                                addNode(assign.place, assign);
                        }
                }
                
                for(Tuple<CopyStr, ICode> vert: nodes)
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
                for(Tuple<CopyStr, CopyStr> edge: edges) {
                        if(edge.source.equals(node))
                                edgeCount++;
                }
                return edgeCount;
        }
        
        private Set<Tuple<CopyStr, CopyStr>> getEdges(String node){
                Set<Tuple<CopyStr, CopyStr>> edges = new HashSet<Tuple<CopyStr, CopyStr>>();
                for(Tuple<CopyStr, CopyStr> edge: edges){
                        if(edge.source.equals(node))
                                edges.add(edge);
                }
                return edges;
        }
        
        private void removeGraph(Tuple<CopyStr, ICode> node){
                edges.removeIf(new Predicate<Tuple<CopyStr, CopyStr>>(){
                        @Override
                        public boolean test(Tuple<CopyStr, CopyStr> t) {
                                return t.source.equals(node.source) || t.dest.equals(node.source);
                        }
                });
                nodes.remove(0);
        }
        
        private void simplifyGraph(Stack<InterfereColoredNode> colorStack, Set<String> spill) {
                int K = 14;
                do {
                        Tuple<CopyStr, ICode> node = nodes.get(0);
                        int edgeCount = countEdges(node.source.toString());
                        if(edgeCount < K){
                                Set<Tuple<CopyStr, CopyStr>> edges = getEdges(node.source.toString());
                                colorStack.push(new InterfereColoredNode(node, edges, new Color(0)));
                                removeGraph(node);
                        } else {
                                Set<Tuple<CopyStr, CopyStr>> edges = getEdges(node.source.toString());
                                colorStack.push(new InterfereColoredNode(node, edges, new Spill()));
                                removeGraph(node);
                        }
                } while(!(edges.isEmpty() && nodes.isEmpty()));
        }
        
        private Color pickColor(Tuple<CopyStr, ICode> node, Map<String, Color> mapy){
                Set<Integer> colors = new HashSet<Integer>();
                for(Tuple<CopyStr, CopyStr> edge: edges){
                        if(edge.source.equals(node.source)){
                                if(mapy.containsKey(edge.dest)){
                                        colors.add(mapy.get(edge.dest).literalColor());
                                }
                        }
                }
                
                for(int color = 0; color <= 13; color++) {
                        if(!colors.contains(color)) {
                                mapy.put(node.source.toString(), new Color(color));
                        }
                }
                
                return mapy.get(node.source);
        }
        
        private static boolean optimisticColoringSuccess(Map<String, Color> colors, Set<Tuple<CopyStr, CopyStr>> edges){
                HashSet<Integer> locColors = new HashSet<Integer>();
                for(Tuple<CopyStr, CopyStr> edge: edges) {
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
                                toRet.put(t.fst.source.toString(), color);
                        } else if(t.thrd.matches(new Spill())) {
                                if (optimisticColoringSuccess(toRet, t.snd)) {
                                        nodes.add(t.fst);
                                        edges.addAll(t.snd);
                                        Color color = pickColor(t.fst, toRet);
                                        toRet.put(t.fst.source.toString(), color);
                                } else {
                                        spillSet.add(t.fst.source.toString());
                                }
                        }
                }
                return toRet;
        }
        
        public ColoredGraph colorGraph() {
                Stack<InterfereColoredNode> colorStack = new Stack<InterfereColoredNode>();
                Set<String> spill = new HashSet<String>();
                
                simplifyGraph(colorStack, spill);
                return new ColoredGraph(assignColors(colorStack, spill), spill);
        }
        
        public List<Tuple<CopyStr, ICode>> getNodes(){
                return nodes;
        }
        
        private void addNode(String node, ICode instr) {
                this.nodes.add(new Tuple<CopyStr, ICode>(ConversionUtils.newS(node), instr));
        }
        
        private void addEdge(String from, String to) {
                edges.add(new Tuple<CopyStr, CopyStr>(ConversionUtils.newS(from), ConversionUtils.newS(to)));
        }
        
        
}
