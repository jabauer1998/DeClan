package declan.utils.flow;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import declan.middleware.icode.ICode;

public class BasicBlock implements Iterable<ICode>{
    private List<ICode> codeInBlock;
    
    public BasicBlock(List<ICode> codeInBlock){
        this.codeInBlock = codeInBlock;
    }

    public BasicBlock(){
        this.codeInBlock = new LinkedList<ICode>();
    }

    public List<ICode> getIcode(){
        return this.codeInBlock;
    }

    public void setICode(List<ICode> list){
        this.codeInBlock = list;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(ICode icode : codeInBlock){
            sb.append(icode.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public Iterator<ICode> iterator() {
        return codeInBlock.iterator();
    }
}



