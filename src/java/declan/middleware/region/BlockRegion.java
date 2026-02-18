package declan.middleware.region;

import java.util.LinkedList;
import java.util.List;

import declan.utils.Tuple;
import declan.utils.exception.MalformedRegionException;
import declan.utils.flow.BasicBlock;
import declan.middleware.icode.ICode;

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
