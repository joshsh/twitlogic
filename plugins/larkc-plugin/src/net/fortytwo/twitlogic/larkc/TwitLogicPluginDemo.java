package net.fortytwo.twitlogic.larkc;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: josh
 * Date: 1/8/11
 * Time: 5:12 AM
 */
public class TwitLogicPluginDemo {
    private static final URI TEST_URI = new URIImpl("http://twitlogic.fortytwo.net/plugins/larkc/instances/test");

    public static void main(final String[] args) {
        try {
            new TwitLogicPluginDemo().test();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void test() throws Exception {
        Properties props = new Properties();
        InputStream in = new FileInputStream("/tmp/testing.properties");
        try {
            props.load(in);
        } finally {
            in.close();
        }
        TwitLogic.setConfiguration(props);

        TwitLogicPlugin plugin = new TwitLogicPlugin(TEST_URI);
        SetOfStatements s = plugin.invoke(new EmptySetOfStatements());

        CloseableIterator<Statement> iter = s.getStatements();
        try {
            while (iter.hasNext()) {
                System.out.println("" + iter.next());
            }
        } finally {
            iter.close();
        }
    }

    private class EmptySetOfStatements implements SetOfStatements {

        public CloseableIterator<Statement> getStatements() {
            return new CloseableIterator<Statement>() {
                private boolean closed = false;

                public boolean hasNext() {
                    return false;
                }

                public Statement next() {
                    return null;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public void close() {
                    closed = true;
                }

                public boolean isClosed() {
                    return closed;
                }
            };
        }

        public SetOfStatements toRDF(SetOfStatements setOfStatements) {
            return this;
        }
    }
}
