package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.model.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 6, 2009
 * Time: 6:30:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlainLiteral implements Resource {
    private final String label;

    public PlainLiteral(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return "\"" + label + "\"";
    }

    public Type getType() {
        return Type.PLAIN_LITERAL;
    }

    public boolean equals(final Object other) {
        return other instanceof PlainLiteral
                && label.equals(((PlainLiteral) other).label);
    }

    public int hashCode() {
        return label.hashCode();
    }
}
