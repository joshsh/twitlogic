package net.fortytwo.twitlogic.twitter;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import net.fortytwo.twitlogic.Handler;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 4, 2009
 * Time: 12:14:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatusStreamParser {
    private final Handler<TwitterStatus, Exception> statusHandler;

    public StatusStreamParser(final Handler<TwitterStatus, Exception> statusHandler) {
        this.statusHandler = statusHandler;
    }

    public void parse(final InputStream is) throws Exception {
        BufferedReader b = new BufferedReader(new InputStreamReader(is));
        
        try {
            while (true) {
                String line = b.readLine();
                if (null == line) {
                    break;
                } else {
                    line = line.trim();
                    if (0 < line.length()) {
                        JSONObject json = null;
                        try {
                            json = new JSONObject(line);
                        } catch (JSONException e) {
                            throw new Exception("Could not parse status element: \"" + line + "\"", e);
                        }

                        // Note: a reasonable optimization would be to perform a
                        // check on the generated JSON object to see whether it is
                        // an "interesting" status update (and discarding it if not)
                        // before going on to parse all of its fields.
                        TwitterStatus status = new TwitterStatus(json);

                        if (!statusHandler.handle(status)) {
                            break;
                        }
                    }
                }
            }

        } finally {
            b.close();
        }
    }
}
