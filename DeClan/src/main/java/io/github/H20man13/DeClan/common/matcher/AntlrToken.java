package io.github.H20man13.DeClan.common.matcher;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public abstract class AntlrToken<ReturnType> {
    public abstract ReturnType accept(ParseTreeVisitor<ReturnType> Visitor);

    public abstract int getLineNumber();

    public abstract int getLinePosition();

    public abstract String getText();

    public abstract boolean equals(Object other);

    public abstract int hashCode();
}
