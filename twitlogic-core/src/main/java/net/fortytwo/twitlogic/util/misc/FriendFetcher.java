package net.fortytwo.twitlogic.util.misc;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;
import net.fortytwo.twitlogic.services.twitter.errors.NotFoundException;
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
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class FriendFetcher {
    public static void main(final String[] args) {
        try {
            boolean reverse = false;
            if (args.length > 0 && args[0].toLowerCase().equals("followers")) {
                reverse = true;
            }
            new FriendFetcher().doit(reverse);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void doit(final boolean reverse) throws Exception {
        if (reverse) {
            System.out.println("finding followers of the given users");
        } else {
            System.out.println("finding users the given user follows");
        }

        Properties props = new Properties();
        props.load(new FileInputStream("/tmp/friendfetcher.properties"));
        TwitLogic.setConfiguration(props);
        CustomTwitterClient client = new CustomTwitterClient();

        String name = reverse ? "followers.txt" : "following.txt";

        OutputStream os = new FileOutputStream("/tmp/" + name);
        try {
            PrintStream ps = new PrintStream(os);

            FileInputStream fstream = new FileInputStream("/tmp/users.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String l;
            while ((l = br.readLine()) != null) {
                String id = l.trim();
                User user = new User(id);
                try {
                    if (reverse) {
                        for (User follower : client.getFollowers(user)) {
                            ps.println(follower.getId() + "\t" + id);
                        }
                    } else {
                        for (User followed : client.getFollowedUsers(user)) {
                            ps.println(id + "\t" + followed.getId());
                        }
                    }
                } catch (UnauthorizedException e) {
                    System.err.println("warning: not authorized to fetch followers of user '" + id + "'");
                } catch (NotFoundException e) {
                    System.err.println("user '" + id + "' not found");
                } catch (TwitterClientException e) {
                    System.err.println("twitter client exception: " + e.getMessage());
                }
            }
            in.close();
        } finally {
            os.close();
        }
    }
}
