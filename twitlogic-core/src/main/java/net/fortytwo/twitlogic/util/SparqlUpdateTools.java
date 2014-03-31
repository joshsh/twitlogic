package net.fortytwo.twitlogic.util;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SparqlUpdateTools {
    private static String formatValue(final Value v) {
        if (v instanceof URI) {
            return "<" + v + ">";
        } else if (v instanceof Literal) {
            Literal l = (Literal) v;
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            sb.append(l.getLabel()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\""));
            sb.append("\"");
            if (null != l.getLanguage()) {
                sb.append("@").append(l.getLanguage());
            } else if (null != l.getDatatype()) {
                sb.append("^^<").append(l.getDatatype()).append(">");
            }
            return sb.toString();
        } else {
            throw new IllegalArgumentException("value, " + v + ", has unexpected type");
        }
    }

    public static String createSparqlInsertStatement(final Statement st) {
        StringBuilder sb = new StringBuilder("INSERT");
        Resource g = st.getContext();
        if (null != g) {
            assert (g instanceof URI);
            sb.append(" INTO <").append(g).append(">");
        }

        sb.append(" {\n\t");
        sb.append(formatValue(st.getSubject()))
                .append(" ").append(formatValue(st.getPredicate()))
                .append(" ").append(formatValue(st.getObject()));

        sb.append("\n}\n");
        return sb.toString();
    }

    public static void writeSparqlInsertStatements(final Collection<Statement> statements,
                                                   final OutputStream out) throws IOException {
        Writer w = new PrintWriter(out);
        for (Statement st : statements) {
            w.append(createSparqlInsertStatement(st));
        }
        w.close();
    }

    public static void dumpTripleStore(final Sail sail,
                                       final OutputStream out) throws SailException, IOException {
        Writer w = new PrintWriter(out);
        try {
            SailConnection sc = sail.getConnection();
            try {
                sc.begin();
                // TODO: one insert statement per statement is a bit verbose (when many statements share the same graph)
                CloseableIteration<? extends Statement, SailException> iter
                        = sc.getStatements(null, null, null, false);
                try {
                    while (iter.hasNext()) {
                        w.append(createSparqlInsertStatement(iter.next()));
                    }
                } finally {
                    iter.close();
                }
            } finally {
                sc.rollback();
                sc.close();
            }
        } finally {
            w.close();
        }
    }
}
