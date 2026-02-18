package io.github.h20man13.DeClan.common.region;

import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.exception.MalformedRegionException;
import io.github.h20man13.DeClan.common.flow.BasicBlock;
import io.github.h20man13.DeClan.common.icode.ICode;

public class BlockRegion extends Region {
        private BasicBlock block;
        
        public BlockRegion(BasicBlock block, RegionBase header, List<RegionBase> subRegions) {
                super(header, subRegions);
                this.block = block;
        }
        
        public RegionBase getInputRegion(RegionBase ofThis) {
                for(Tuple<RegionBase, RegionBase> edge: innerEdge) {
                        if(edge.dest.equals(ofThis)) {
                                return edge.source;
                        }
                }
                throw new MalformedRegionException("getInputRegion", ofThis, "Error instruction does not have a proper input region");
        }
        
        @Override
        public ICode getLastInstruction() {
                return block.getIcode().get(block.getIcode().size() - 1);
        }
        
        public int getNumberOfInstructions() {
                return block.getIcode().size();
        }
        
        public ICode getInstruction(int index) {
                return block.getIcode().get(index);
        }
        
        @Override
        public ICode getFirstInstruction() {
                return block.getIcode().get(0);
        }
}
