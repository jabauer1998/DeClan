package io.github.H20man13.DeClan.common.builder;

public interface IrBuilderTemplate<OutputType> {
    public OutputType completeBuild();
    void resetBuilder();
}
