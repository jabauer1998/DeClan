package io.github.H20man13.DeClan.common.symboltable.entry;

import java.util.List;

import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.main.MyTypeChecker.TypeCheckerTypes;

public class ProcedureTypeEntry implements Copyable<ProcedureTypeEntry> {
    private TypeCheckerTypes returnType;
    private List<TypeCheckerTypes> argumentTypes;
    
    public ProcedureTypeEntry(TypeCheckerTypes returnType, List<TypeCheckerTypes> argumentTypes){
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
    }

    public TypeCheckerTypes getReturnType(){
        return this.returnType;
    }

    public List<TypeCheckerTypes> getArgumentTypes(){
        return this.argumentTypes;
    }

    @Override
    public ProcedureTypeEntry copy() {
        return new ProcedureTypeEntry(this.returnType, this.argumentTypes);
    }
}
