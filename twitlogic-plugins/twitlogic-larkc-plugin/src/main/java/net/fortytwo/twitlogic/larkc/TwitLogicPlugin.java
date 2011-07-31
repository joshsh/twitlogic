package net.fortytwo.twitlogic.larkc;

import eu.larkc.core.data.CloseableIterator;
import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import javax.jms.Message;

import eu.larkc.core.data.SetOfStatements;
import org.openrdf.model.Value;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * A streaming LarKC plugin which uses Twitter as a real-time data source for RDF statements.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TwitLogicPlugin extends StreamingPlugin {

    // Configuration properties specific to the TwitLogic LarKC plugin
    public static final String
            OVERFLOW_POLICY = "net.fortytwo.twitlogic.larkc.overflowPolicy",
            QUEUE_CAPACITY = "net.fortytwo.twitlogic.larkc.queueCapacity";

    public static final int DEFAULT_QUEUE_CAPACITY = 1000;

    private static final Logger LOGGER = TwitLogic.getLogger(TwitLogicPlugin.class);

    private boolean once = false;

    private final URI pluginName;

    public TwitLogicPlugin(final URI pluginName) {
        super(pluginName);
        this.pluginName = pluginName;
    }

    @Override
    public void onMessage(final Message message) {
        // Ignore messages for now.
    }

    @Override
    protected void initialiseInternal(final SetOfStatements workflowDescription) {
        Properties props = propertiesFromStatements(workflowDescription);
        TwitLogic.setConfiguration(props);
    }

    // Note: input is ignored.
    public SetOfStatements invokeInternal(final SetOfStatements input) {
        StreamingSetOfStatements s;

        if (once) {
            return null;
        }
        once = true;

        return new TwitterStream(findOverflowPolicy());
    }

    public void shutdown() {
    }

    private OverflowPolicy findOverflowPolicy() {
        String v = TwitLogic.getConfiguration().getProperty(OVERFLOW_POLICY, null);
        OverflowPolicy policy = null == v
                ? null
                : OverflowPolicy.valueOf(v);
        if (null == policy) {
            policy = OverflowPolicy.DROP_OLDEST;
        }

        LOGGER.info("using overflow policy " + policy);

        return policy;
    }

    private Properties propertiesFromStatements(final SetOfStatements statements) {
        Properties props = new Properties();
        CloseableIterator<Statement> iter = statements.getStatements();
        try {
            while (iter.hasNext()) {
                Statement st = iter.next();
                if (st.getSubject().equals(pluginName)) {
                    String p = st.getPredicate().stringValue();
                    if (p.startsWith("urn:")) {
                        Value v = st.getObject();
                        if (v instanceof Literal) {
                            String name = p.substring(4);
                            String value = ((Literal) v).getLabel().trim();

                            props.put(name, value);
                        }
                    }
                }
            }
        } finally {
            iter.close();
        }

        return props;
    }
}