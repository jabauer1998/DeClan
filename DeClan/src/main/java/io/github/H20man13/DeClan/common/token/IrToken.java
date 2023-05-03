package io.github.H20man13.DeClan.common.token;

import java.util.Objects;

import io.github.H20man13.DeClan.common.Position;

public class IrToken {
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
        StringBuilder result = new StringBuilder(lexeme);
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
        if(DeClanTokenType.reserved.containsKey(lexeme)){
            return new IrToken(pos, IrTokenType.reserved.get(lexeme), lexeme);
        } else {
            return new IrToken(pos, IrTokenType.ID, lexeme);
        }
    }

    public static IrToken create(IrTokenType type, String lexeme, Position pos){
        return new IrToken(pos, type, lexeme);
    }
}
