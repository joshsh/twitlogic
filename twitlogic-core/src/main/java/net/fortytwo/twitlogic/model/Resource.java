package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface Resource {
    enum Type {
        DOLLARTAG, HASHTAG, USER, PERSON, PLAIN_LITERAL, TYPED_LITERAL, URI_REFERENCE, TWEET
    }

    Type getType();
}
