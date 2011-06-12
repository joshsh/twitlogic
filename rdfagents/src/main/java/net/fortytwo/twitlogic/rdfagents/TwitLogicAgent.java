package net.fortytwo.twitlogic.rdfagents;

import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.SailBasedQueryProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;

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

        try {
            pub = new TwitLogicPubsubProvider(this, config);
        } catch (Exception e) {
            throw new RDFAgentException(e);
        }
        setPubsubProvider(pub);

        setQueryProvider(new SailBasedQueryProvider(this, pub.getSail()));
    }

    public void setRateLimit(final long minimumInterval) {
        pub.setMinimumUpdateInterval(minimumInterval);
    }
}
