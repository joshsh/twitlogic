package net.fortytwo.twitlogic.rdfagents;

import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.messaging.query.QueryProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import org.openrdf.model.Value;

import java.util.Properties;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 4:25 PM
 */
public class TwitLogicAgent extends RDFAgentImpl {

    private final TwitLogicPubsubProvider pub;

    public TwitLogicAgent(final Properties config,
                          final RDFAgentsPlatform platform,
                          final AgentId id) throws RDFAgentException {
        super(platform, id);
        pub = new TwitLogicPubsubProvider(this, config);
        setPubsubProvider(pub);
    }

    @Override
    public void setQueryProvider(QueryProvider<Value, Dataset> queryServer) {
        throw new UnsupportedOperationException("queries (as opposed to subscriptions) are not yet supported");
    }

    public void setRateLimit(final long minimumInterval) {
        pub.setMinimumUpdateInterval(minimumInterval);
    }
}
