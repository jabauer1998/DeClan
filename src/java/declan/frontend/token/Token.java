package declan.frontend.token;

import declan.utils.position.Position;

public interface Token{
    public String getLexeme();
    public Position getPosition();
    @Override
    public String toString();
}
