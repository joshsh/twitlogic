package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.Handler;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Tweet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Sep 4, 2009
 * Time: 12:14:37 AM
 */
public class StatusStreamParser {
    public enum ExitReason {
        END_OF_INPUT, HANDLER_QUIT, EXCEPTION_THROWN
    }

    private static final Logger LOGGER = TwitLogic.getLogger(StatusStreamParser.class);

    private final Handler<Tweet, Exception> statusHandler;

    public StatusStreamParser(final Handler<Tweet, Exception> statusHandler) {
        this.statusHandler = statusHandler;
    }

    public ExitReason parse(final InputStream is) throws Exception {
        BufferedReader b = new BufferedReader(new InputStreamReader(is));

        ExitReason exitReason = ExitReason.EXCEPTION_THROWN;
        int lines = 0;
        int emptyLines = 0;
        try {
            LOGGER.info("begin reading from stream");
            while (true) {
                String line = b.readLine();
                if (null == line) {
                    exitReason = ExitReason.END_OF_INPUT;
                    break;
                } else {
                    lines++;
                    line = line.trim();
                    //System.out.println(line);
                    if (0 < line.length()) {
                        JSONObject json;
                        try {
                            json = new JSONObject(line);
                        } catch (JSONException e) {
                            throw new Exception("Could not parse status element as JSON: \"" + line + "\"", e);
                        }

                        if (!handleStatusElement(json)) {
                            exitReason = ExitReason.HANDLER_QUIT;
                            break;
                        }
                    } else {
                        //LOGGER.fine("empty line");
                        emptyLines++;
                    }
                }
            }
        } finally {
            LOGGER.info("stop reading from stream (reason: " + exitReason + ")");
            LOGGER.info("\tread " + lines + " lines incl. " + emptyLines + " empty");
            b.close();
        }

        return exitReason;
    }

    private boolean handleStatusElement(final JSONObject el) throws Exception {
        if (null != el.opt(TwitterAPI.Field.DELETE.toString())) {
            return handleDeleteStatusElement(el);
        } else {
            return handleNormalStatusElement(el);
        }
    }

    private boolean handleDeleteStatusElement(final JSONObject el) {
        LOGGER.info("skipping delete element");
        return true;
    }

    private boolean handleNormalStatusElement(final JSONObject el) throws Exception {
        // Note: a reasonable optimization would be to perform a
        // check on the generated JSON object to see whether it is
        // an "interesting" status update (and discarding it if not)
        // before going on to parse all of its fields.
        Tweet status = new Tweet(el);

        return statusHandler.handle(status);
    }
}
