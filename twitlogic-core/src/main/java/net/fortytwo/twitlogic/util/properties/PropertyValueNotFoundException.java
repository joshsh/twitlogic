package net.fortytwo.twitlogic.util.properties;


/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PropertyValueNotFoundException extends PropertyException {
    public PropertyValueNotFoundException(final String propertyName) {
        super(propertyName);
    }
}
