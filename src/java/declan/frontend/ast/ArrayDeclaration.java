package declan.frontend.ast;

import declan.utils.position.Position;


public class ArrayDeclaration extends AbstractASTNode implements Declaration{
    public enum Type{
	INT,
	REAL,
	BOOLEAN,
	CHAR,
	OBJECT
    };

    private Expression size;
    private String name;
    private Type type;
    
    public ArrayDeclaration(Position pos, String name, Expression size, Type type){
	super(pos);
	this.name = name;
	this.type = type;
	this.size = size;
    }

    public Expression getSize(){
	return size;
    }

    public String getName(){
	return name;
    }

    public Type getType(){
	return type;
    }

    @Override
    public String toString(){
	StringBuilder sb = new StringBuilder();
	sb.append(name);
	sb.append(" ARRAY ");
	sb.append(size);
	sb.append(" OF ");
	sb.append(type.toString());
	return sb.toString();
    }

    @Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <R> R acceptResult(DeclarationVisitor<R> visitor) {
		return visitor.visitResult(this);
	}
}


