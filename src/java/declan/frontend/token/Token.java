package io.github.h20man13.DeClan.common.token;

import io.github.h20man13.DeClan.common.position.Position;

public interface Token{
    public String getLexeme();
    public Position getPosition();
    @Override
    public String toString();
}
