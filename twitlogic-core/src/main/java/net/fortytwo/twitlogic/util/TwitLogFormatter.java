package net.fortytwo.twitlogic.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TwitLogFormatter extends Formatter {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public String format(final LogRecord record) {
        StringBuilder sb = new StringBuilder();

        String date = DATE_FORMAT.format(new Date(record.getMillis()));

        sb.append(record.getSequenceNumber()).append(")")
                .append(" ").append(record.getLevel())
                .append(" ").append(date)
                .append(" [").append(record.getThreadID()).append("]")
                .append(": ").append(record.getMessage());

        /*
        sb.append(" (").append(record.getSourceClassName())
                .append(".").append(record.getSourceMethodName())
                .append(", thread #").append(record.getThreadID()).append(")");
        */

        // TODO: resource bundle, thrown error, parameters

        sb.append("\n");

        return sb.toString();
    }
}
