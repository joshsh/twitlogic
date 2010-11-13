package net.fortytwo.twitlogic.services.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.TweetParseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Sep 4, 2009
 * Time: 12:14:37 AM
 */
public class StatusStreamParser {
    public enum ExitReason {
        END_OF_INPUT, HANDLER_QUIT, EXCEPTION_THROWN, NULL_RESPONSE, CONNECTION_RESET,
        CONNECTION_REFUSED  // Note: this error actually occurs outside of the parser
    }

    private static final Logger LOGGER = TwitLogic.getLogger(StatusStreamParser.class);

    private final Handler<Tweet, TweetHandlerException> addHandler;
    private final Handler<Tweet, TweetHandlerException> deleteHandler;
    private final boolean recoverFromErrors;

    /*
    public StatusStreamParser(final Handler<Tweet, TweetHandlerException> addHandler,
                              final boolean recoverFromErrors) {
        this.addHandler = addHandler;
        this.deleteHandler = null;
        this.recoverFromErrors = recoverFromErrors;
    }*/

    /**
     * A streaming parser to receive and handle incoming status updates from Twitter
     *
     * @param addHandler        a handler for normal status updates
     * @param deleteHandler     a handler for "delete" requests
     * @param recoverFromErrors whether to tolerate per-tweet errors (e.g. badly-formatted status updates)
     */
    public StatusStreamParser(final Handler<Tweet, TweetHandlerException> addHandler,
                              final Handler<Tweet, TweetHandlerException> deleteHandler,
                              final boolean recoverFromErrors) {
        this.addHandler = addHandler;
        this.deleteHandler = deleteHandler;
        this.recoverFromErrors = recoverFromErrors;
    }

    public ExitReason parse(final InputStream is) throws IOException, TweetHandlerException {
        BufferedReader b = new BufferedReader(new InputStreamReader(is));

        ExitReason exitReason = ExitReason.EXCEPTION_THROWN;
        int lines = 0;
        int emptyLines = 0;
        try {
            LOGGER.info("begin reading from stream");
            // Break out when the end of input is reached, when the handler quits, or when an exception is thrown.
            while (true) {
                String line;

                try {
                    line = b.readLine();
                } catch (SocketException e) {
                    exitReason = ExitReason.CONNECTION_RESET;
                    break;
                }

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
                            throw new TweetHandlerException("Could not parse status element as JSON: \"" + line + "\"", e);
                        }

                        try {
                            if (!handleStatusElement(json)) {
                                exitReason = ExitReason.HANDLER_QUIT;
                                break;
                            }
                        } catch (TweetParseException e) {
                            if (recoverFromErrors) {
                                LOGGER.severe("failed to parse status element: " + e);
                                System.err.println("element json (followed by stack trace): " + json);
                                e.printStackTrace(System.err);
                            } else {
                                throw new TweetHandlerException(e);
                            }
                        }
                    } else {
                        //LOGGER.fine("empty line");
                        emptyLines++;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new TweetHandlerException(t);
        } finally {
            LOGGER.info("stop reading from stream (reason: " + exitReason + ")");
            LOGGER.info("    read " + lines + " lines (including " + emptyLines + " empty lines)");
            b.close();
        }

        return exitReason;
    }

    private boolean handleStatusElement(final JSONObject el) throws TweetParseException, TweetHandlerException {
        if (null != el.opt(TwitterAPI.Field.DELETE.toString())) {
            return handleDeleteStatusElement(el);
        } else if (null != el.opt(TwitterAPI.Field.LIMIT.toString())) {
            return handleLimitStatusElement(el);
        } else if (null != el.opt(TwitterAPI.Field.SCRUB_GEO.toString())) {
            return handleScrubGeoStatusElement(el);
        } else {
            return handleNormalStatusElement(el);
        }
    }

    private boolean handleDeleteStatusElement(final JSONObject el) throws TweetParseException, TweetHandlerException {
        if (null == deleteHandler) {
            LOGGER.info("skipping 'delete' status element");
            return true;
        } else {
            Tweet status;
            try {
                status = new Tweet(el
                        .getJSONObject(TwitterAPI.Field.DELETE.toString())
                        .getJSONObject(TwitterAPI.Field.STATUS.toString())
                        .getString(TwitterAPI.Field.ID.toString()));
            } catch (JSONException e) {
                throw new TweetParseException(e);
            } catch (NullPointerException e) {
                throw new TweetParseException(e);
            }

            return deleteHandler.handle(status);
        }
    }

    private boolean handleLimitStatusElement(final JSONObject el) {
        LOGGER.warning("skipping 'limit' status element");
        return true;
    }

    private boolean handleScrubGeoStatusElement(final JSONObject el) {
        LOGGER.warning("skipping 'scrub_geo' status element");
        return true;
    }

    private boolean handleNormalStatusElement(final JSONObject el) throws TweetHandlerException, TweetParseException {
        // Note: a reasonable optimization would be to perform a
        // check on the generated JSON object to see whether it is
        // an "interesting" status update (and discarding it if not)
        // before going on to parse all of its fields.
        return addHandler.handle(new Tweet(el));
    }
}
