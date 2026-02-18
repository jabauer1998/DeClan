package declan.utils.symboltable.entry;

import java.util.List;

import declan.utils.Copyable;

public class ProcedureTypeEntry implements Copyable<ProcedureTypeEntry> {
    private TypeCheckerQualities returnType;
    private List<TypeCheckerQualities> argumentTypes;
    
    public ProcedureTypeEntry(TypeCheckerQualities returnType, List<TypeCheckerQualities> argumentTypes){
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
    }

    public TypeCheckerQualities getReturnType(){
        return this.returnType;
    }

    public List<TypeCheckerQualities> getArgumentTypes(){
        return this.argumentTypes;
    }

    @Override
    public ProcedureTypeEntry copy() {
        return new ProcedureTypeEntry(this.returnType, this.argumentTypes);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for(int i = 0; i < argumentTypes.size(); i++){
            TypeCheckerQualities argumentType = argumentTypes.get(i);
            sb.append(argumentType.toString());
            if(i < argumentTypes.size() - 1){
                sb.append(", ");
            }
        }
        sb.append("): ");
        sb.append(returnType.toString());
        return sb.toString();
    }
}
