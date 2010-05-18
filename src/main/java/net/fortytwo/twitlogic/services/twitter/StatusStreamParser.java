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
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Sep 4, 2009
 * Time: 12:14:37 AM
 */
public class StatusStreamParser {
    public enum ExitReason {
        END_OF_INPUT, HANDLER_QUIT, EXCEPTION_THROWN, NULL_RESPONSE,
        CONNECTION_REFUSED  // Note: this error actually occurs outside of the parser
    }

    private static final Logger LOGGER = TwitLogic.getLogger(StatusStreamParser.class);

    private final Handler<Tweet, TweetHandlerException> statusHandler;
    private final boolean recoverFromErrors;

    public StatusStreamParser(final Handler<Tweet, TweetHandlerException> statusHandler,
                              final boolean recoverFromErrors) {
        this.statusHandler = statusHandler;
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
        } else {
            return handleNormalStatusElement(el);
        }
    }

    private boolean handleDeleteStatusElement(final JSONObject el) {
        LOGGER.info("skipping 'delete' status element");
        return true;
    }

    private boolean handleLimitStatusElement(final JSONObject el) {
        LOGGER.warning("skipping 'limit' status element");
        return true;
    }

    private boolean handleNormalStatusElement(final JSONObject el) throws TweetHandlerException, TweetParseException {
        // Note: a reasonable optimization would be to perform a
        // check on the generated JSON object to see whether it is
        // an "interesting" status update (and discarding it if not)
        // before going on to parse all of its fields.
        Tweet status;
        status = new Tweet(el);

        return statusHandler.handle(status);
    }
}
