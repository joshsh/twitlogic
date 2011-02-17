package net.fortytwo.twitlogic.util.misc;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.services.twitter.errors.UnauthorizedException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * User: josh
 * Date: 2/17/11
 * Time: 6:31 PM
 */
public class FriendFetcher {
    public static void main(final String[] args) {
        try {
            new FriendFetcher().doit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void doit() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("/tmp/friendfetcher.properties"));
        TwitLogic.setConfiguration(props);
        TwitterClient client = new TwitterClient();

        OutputStream os = new FileOutputStream("/tmp/following");
        try {
            PrintStream ps = new PrintStream(os);

            FileInputStream fstream = new FileInputStream("/tmp/users");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String l;
            while ((l = br.readLine()) != null) {
                String id = l.trim();
                User user = new User(id);
                try {
                for (User followed : client.getFollowedUsers(user)) {
                    ps.println(id + "\t" + followed.getId());
                }
                } catch (UnauthorizedException e) {
                    System.err.println("warning: not authorized to fetch followers of user '" + id + "'");
                }
            }
            in.close();
        } finally {
            os.close();
        }
    }
}
