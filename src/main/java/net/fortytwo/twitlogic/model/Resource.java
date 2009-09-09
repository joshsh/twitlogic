package net.fortytwo.twitlogic.model;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 5, 2009
 * Time: 1:23:07 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Resource {
    enum Type { HASHTAG, USER, LITERAL, URL }

    Type getType();
}
