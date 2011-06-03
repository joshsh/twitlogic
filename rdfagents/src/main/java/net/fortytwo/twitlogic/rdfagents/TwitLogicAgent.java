package net.fortytwo.twitlogic.rdfagents;

import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.messaging.subscribe.Publisher;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import org.openrdf.model.Value;

import java.util.Properties;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 4:25 PM
 */
public class TwitLogicAgent extends RDFAgent {


    public TwitLogicAgent(final Properties config,
                          final String localName,
                          final RDFAgentsPlatform platform,
                          final String... addresses) throws RDFAgentException {
        super(localName, platform, addresses);
        setPublisher(new TwitLogicPublisher(this, config));
    }

    @Override
    public void setQueryServer(QueryServer<Value, Dataset> queryServer) {
        throw new UnsupportedOperationException("queries (as opposed to subscriptions) are not yet supported");
    }

    @Override
    public void setPublisher(Publisher<Value, Dataset> publisher) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
