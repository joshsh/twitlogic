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

/**
 * This is an identifier template, created by LarKC plug-in Wizard
 *
 * @author LarKC plug-in Wizard
 */
public class TwitLogicPlugin extends StreamingPlugin {

    public static final String
            OVERFLOW_POLICY = "net.fortytwo.twitlogic.larkc.overflowPolicy",
            QUEUE_CAPACITY = "net.fortytwo.twitlogic.larkc.queueCapacity";

    //only first time when called, return results (anytime b.)
    private boolean once = false;

    public TwitLogicPlugin(URI pluginName) {
        super(pluginName);
    }

    @Override
    public void onMessage(final Message message) {
        // Ignore messages for now.
    }

    @Override
    protected void initialiseInternal(final SetOfStatements workflowDescription) {
    }

    public SetOfStatements invokeInternal(final SetOfStatements input) {
        StreamingSetOfStatements s;

        if (once) {
            return null;
        }
        once = true;

        Properties props = propertiesFromStatements(input);
        TwitLogic.setConfiguration(props);

        String v = TwitLogic.getConfiguration().getProperty(OVERFLOW_POLICY, null);
        OverflowPolicy policy = null == v
                ? null
                : OverflowPolicy.valueOf(v);
        if (null == policy) {
            policy = OverflowPolicy.DROP_OLDEST;
        }

        return new TwitterStream(policy);
    }

    public void shutdown() {
    }

    private static Properties propertiesFromStatements(final SetOfStatements statements) {
        Properties props = new Properties();
        CloseableIterator<Statement> iter = statements.getStatements();
        try {
            while (iter.hasNext()) {
                Statement st = iter.next();
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
        } finally {
            iter.close();
        }

        return props;
    }
}