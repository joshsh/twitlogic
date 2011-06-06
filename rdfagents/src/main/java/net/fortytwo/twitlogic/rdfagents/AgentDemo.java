package net.fortytwo.twitlogic.rdfagents;

import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.jade.PubsubConsumerImpl;
import net.fortytwo.rdfagents.jade.QueryConsumerImpl;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.RDFAgentsPlatformImpl;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.query.QueryConsumer;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubConsumer;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: 6/3/11
 * Time: 12:22 PM
 */
public class AgentDemo {
    private static final Logger LOGGER = TwitLogic.getLogger(AgentDemo.class);

    public static void main( String[] args) {
        if (0 == args.length) {
            args = new String[]{"/Users/josh/projects/fortytwo/twitlogic/rdfagents/config/rdfagents.props"};
        }

        try {
            if (1 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                new AgentDemo().runDemo(p);
            } else {
                printUsage();
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:  agentdemo [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private void runDemo(final Properties config) throws Exception {
        final DatasetFactory datasetFactory = new DatasetFactory(new ValueFactoryImpl());
        for (RDFContentLanguage l : RDFContentLanguage.values()) {
            datasetFactory.addLanguage(l);
        }

        RDFAgentsPlatform p = new RDFAgentsPlatformImpl("twitlogic.fortytwo.net", 8889, config);


        RDFAgent twitlogic = new TwitLogicAgent(config, p,
                new AgentId("urn:x-agent:twitlogic@twitlogic.fortytwo.net", "xmpp://patabot.2@jabber.org"));
        RDFAgent consumer = new RDFAgentImpl(p,
                new AgentId("urn:x-agent:agent1@twitlogic.fortytwo.net", "xmpp://patabot.2@jabber.org"));

        QueryConsumer<Value, Dataset> client = new QueryConsumerImpl(consumer);
        PubsubConsumer<Value, Dataset> subscriber = new PubsubConsumerImpl(consumer);

        ConsumerCallback<Dataset> callback = new ConsumerCallback<Dataset>() {
            public void success(final Dataset answer) {
                System.out.println("received a query result or subscription update.  Answer follows:");
                try {
                    datasetFactory.write(System.out, answer, RDFContentLanguage.RDF_TRIG);
                } catch (LocalFailure e) {
                    e.printStackTrace(System.err);
                }
            }

            public void agreed() {
                System.out.println("agreed!");
            }

            public void refused(final ErrorExplanation explanation) {
                System.out.println("refused!");
            }

            public void remoteFailure(final ErrorExplanation explanation) {
                System.out.println("remote failure: " + explanation);
            }

            public void localFailure(final LocalFailure e) {
                System.out.println("local failure: " + e + "\n" + RDFAgents.stackTraceToString(e));
            }
        };

        //client.submit(new URIImpl("http://rdfs.org/sioc/types#MicroblogPost"), twitlogic.getIdentity(), callback);

        subscriber.submit(new URIImpl("http://rdfs.org/sioc/types#MicroblogPost"), twitlogic.getIdentity(), callback);
        //subscriber.submit(new URIImpl("http://twitlogic.fortytwo.net/post/twitter/76748841705144320"), twitlogic.getIdentity(), callback);
    }
}
