package net.fortytwo.twitlogic.vocabs;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:52:28 PM
 * To change this template use File | Settings | File Templates.
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