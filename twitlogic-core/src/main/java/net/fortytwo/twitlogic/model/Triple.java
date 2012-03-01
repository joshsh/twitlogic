package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Triple {
    private final Resource subject;
    private final Resource predicate;
    private final Resource object;
    private final float weight;

    public Triple(final Resource subject,
                  final Resource predicate,
                  final Resource object) {
        this(subject, predicate, object, 1f);
    }

    public Triple(final Resource subject,
                  final Resource predicate,
                  final Resource object,
                  final float weight) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.weight = weight;
    }

    public Resource getSubject() {
        return subject;
    }

    public Resource getPredicate() {
        return predicate;
    }

    public Resource getObject() {
        return object;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(subject).append(" ");
        sb.append(predicate).append(" ");
        sb.append(object);

        return sb.toString();
    }

    public float getWeight() {
        return weight;
    }

    public boolean equals(final Object other) {
        if (other instanceof Triple) {
            Triple t = (Triple) other;
            return (subject.equals(t.subject)
                    && predicate.equals(t.predicate)
                    && object.equals(t.object));
        } else {
            return false;
        }
    }

    public int hashCode() {
        return subject.hashCode() + predicate.hashCode() + object.hashCode();
    }
}
