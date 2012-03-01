package net.fortytwo.twitlogic.services.cliqset;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.services.twitter.RestfulJSONClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class CliqsetClient extends RestfulJSONClient {
    public static void main(final String[] args) throws Exception {
        Properties conf = new Properties();
        conf.setProperty("net.fortytwo.twitlogic.twitter.username", "joshsh1");
        conf.setProperty("net.fortytwo.twitlogic.twitter.password", "testing");
        TwitLogic.setConfiguration(conf);

        CliqsetClient c = new CliqsetClient();
        c.processActivities();
    }

    private void processActivities() throws TwitterClientException {
        HttpGet request = new HttpGet("https://api.cliqset.com/200909/activity/joshsh1");

        JSONObject r = requestJSONObject(request);
       System.out.println(r);
    }
}
