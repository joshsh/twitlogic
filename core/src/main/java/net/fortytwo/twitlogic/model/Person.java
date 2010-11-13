package net.fortytwo.twitlogic.model;

/**
 * User: josh
 * Date: Oct 6, 2009
 * Time: 6:18:19 PM
 */
public class Person implements Resource {
    private final User account;

    public Person(final User account) {
        this.account = account;
    }

    public Type getType() {
        return Type.PERSON;
    }

    public User getAccount() {
        return account;
    }

    public String toString() {
        return "[holds:" + account + "]";
    }

    public boolean equals(final Object other) {
        return other instanceof Person
                && account.equals(((Person) other).account);
    }

    public int hashCode() {
        return 7 * account.hashCode();
    }
}
