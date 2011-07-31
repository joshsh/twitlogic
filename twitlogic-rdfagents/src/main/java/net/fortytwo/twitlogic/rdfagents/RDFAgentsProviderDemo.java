package net.fortytwo.twitlogic.rdfagents;

import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.jade.RDFAgentsPlatformImpl;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.logging.Logger;

/**
 * User: josh
 * Date: 6/3/11
 * Time: 12:22 PM
 */
public class RDFAgentsProviderDemo {
    private static final Logger LOGGER = TwitLogic.getLogger(RDFAgentsProviderDemo.class);

    public static void main(String[] args) {
        // TODO: remove me
        if (0 == args.length) {
            args = new String[]{"/Users/josh/projects/fortytwo/twitlogic/rdfagents/config/rdfagents.props"};
        }

        try {
            if (1 == args.length) {
                TwitLogic.setConfiguration(RDFAgents.loadProps(args[0]));

                new RDFAgentsProviderDemo().runDemo(TwitLogic.getConfiguration());
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
        System.out.println("Usage:  demo [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private void runDemo(final TypedProperties config) throws Exception {
        final DatasetFactory datasetFactory = new DatasetFactory(new ValueFactoryImpl());
        for (RDFContentLanguage l : RDFContentLanguage.values()) {
            datasetFactory.addLanguage(l);
        }

        TypedProperties p = TwitLogic.getConfiguration();
        String platformName = p.getString(TwitLogic.RDFAGENTS_PLATFORM_NAME);
        int port = p.getInt(TwitLogic.RDFAGENTS_PLATFORM_PORT);
        String providerName = p.getString(TwitLogic.RDFAGENTS_AGENT_NAME);
        String xmppServer = p.getString("jade_mtp_xmpp_server");
        String xmppUserName = p.getString("jade_mtp_xmpp_username");

        RDFAgentsPlatform platform = new RDFAgentsPlatformImpl(platformName, datasetFactory, port, config);

        String address = RDFAgents.XMPP_URI_PREFIX + xmppUserName + "@" + xmppServer;// + "/acc";
        String name = RDFAgents.NAME_PREFIX + providerName + "@" + platformName;
        TwitLogicAgent agent = new TwitLogicAgent(config, platform, new AgentId(name, address));
        agent.setRateLimit(1000);
    }
}
