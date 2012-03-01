package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface RDFS {
    public static final String NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    public static final String
            COMMENT = NAMESPACE + "comment",
            DOMAIN = NAMESPACE + "domain",
            FIRST = NAMESPACE + "first",
            ISDEFINEDBY = NAMESPACE + "isDefinedBy",
            LABEL = NAMESPACE + "label",
            MEMBER = NAMESPACE + "member",
            OBJECT = NAMESPACE + "object",
            PREDICATE = NAMESPACE + "predicate",
            RANGE = NAMESPACE + "range",
            REST = NAMESPACE + "rest",
            SEEALSO = NAMESPACE + "seeAlso",
            SUBCLASSOF = NAMESPACE + "subClassOf",
            SUBJECT = NAMESPACE + "subject",
            SUBPROPERTYOF = NAMESPACE + "subPropertyOf",
            TYPE = NAMESPACE + "type",
            VALUE = NAMESPACE + "value";
}