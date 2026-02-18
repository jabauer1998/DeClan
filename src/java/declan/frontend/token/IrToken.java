package declan.frontend.token;

import java.util.Objects;

import declan.utils.position.Position;

public class IrToken implements Token{
    private final IrTokenType type;
    private final String lexeme;
    private final Position position;

    public IrToken(Position position, IrTokenType type, String lexeme){
        this.type = type;
        this.lexeme = lexeme;
        this.position = position;
    }

    public IrTokenType getType(){
        return type;
    }

    public String getLexeme(){
        return lexeme;
    }

    public Position getPosition(){
        return position;
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append(type.toString());
		if (lexeme != null) {
			result.append(" ").append(lexeme);
		}
		result.append(" ").append(position);
		return result.toString();
	}

    @Override
	public int hashCode() {
		return Objects.hash(lexeme, position, type);
	}

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IrToken other = (IrToken) obj;
		return Objects.equals(lexeme, other.getLexeme()) && Objects.equals(position, other.getPosition()) && type == other.getType();
	}

    public static IrToken createString(String lexeme, Position pos){
        return new IrToken(pos, IrTokenType.STRING, lexeme);
    }

    public static IrToken createNumber(String lexeme, Position pos){
        return new IrToken(pos, IrTokenType.NUMBER, lexeme);
    }

    public static IrToken createId(String lexeme, Position pos){
        if(IrTokenType.reservedIr.containsKey(lexeme)){
            return new IrToken(pos, IrTokenType.reservedIr.get(lexeme), lexeme);
        } else {
            return new IrToken(pos, IrTokenType.ID, lexeme);
        }
    }

    public static IrToken create(IrTokenType type, String lexeme, Position pos){
        return new IrToken(pos, type, lexeme);
    }
}
