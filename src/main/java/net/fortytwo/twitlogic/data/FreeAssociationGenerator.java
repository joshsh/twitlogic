package net.fortytwo.twitlogic.data;

import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.model.URI;
import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import net.fortytwo.twitlogic.TwitLogic;

import java.util.Set;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 13, 2009
 * Time: 9:40:13 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FreeAssociationGenerator {
    private static final Pattern
            WHITESPACE = Pattern.compile("\\s+"),
            NORMAL_WORD = Pattern.compile("[a-z]+([ ][a-z]+)*");

    private static final ValueFactory VALUE_FACTORY = new ValueFactoryImpl();

    private static final Random RANDOM = new Random();

    // A Set to help eliminate duplicate statements.
    protected static Set<String> DEFINED_WORDS;

    public static String normalizeWord(final String term) {
        String s = term.trim().toLowerCase();

        Matcher m = WHITESPACE.matcher(s);
        s = m.replaceAll(" ");

        return s;
    }

    public static boolean isNormalWord(final String term) {
        return NORMAL_WORD.matcher(term).matches();
    }

    protected static void associate(final String subjectWord,
                                    final String objectWord,
                                    final float weight,
                                    final RDFWriter writer) throws RDFHandlerException {
        String normSubjectWord = normalizeWord(subjectWord);
        String normObjectWord = normalizeWord(objectWord);
        if (!isNormalWord(normSubjectWord) || !isNormalWord(normObjectWord)) {
            System.err.println("One of {" + subjectWord + ", " + objectWord + "} could not be normalized. No association created.");
            return;
        }

        String encodedSubjectWord = encodeWord(normSubjectWord);
        String encodedObjectWord = encodeWord(normObjectWord);

        URI subject = createResourceURI(encodedSubjectWord);
        URI object = createResourceURI(encodedObjectWord);
        URI association = createResourceURI(encodedSubjectWord + "-" + encodedObjectWord + "-" + RANDOM.nextInt(Integer.MAX_VALUE));

        Literal subjectLabel = VALUE_FACTORY.createLiteral(normSubjectWord);
        Literal objectLabel = VALUE_FACTORY.createLiteral(normObjectWord);
        Literal w = VALUE_FACTORY.createLiteral(weight);

        if (!wordAlreadyDefined(normSubjectWord)) {
            writer.handleStatement(VALUE_FACTORY.createStatement(subject, RDF.TYPE, TwitLogic.WORD));
            writer.handleStatement(VALUE_FACTORY.createStatement(subject, RDFS.LABEL, subjectLabel));
        }

        if (!wordAlreadyDefined(normObjectWord)) {
            writer.handleStatement(VALUE_FACTORY.createStatement(object, RDF.TYPE, TwitLogic.WORD));
            writer.handleStatement(VALUE_FACTORY.createStatement(object, RDFS.LABEL, objectLabel));
        }

        writer.handleStatement(VALUE_FACTORY.createStatement(association, RDF.TYPE, TwitLogic.ASSOCIATION));
        writer.handleStatement(VALUE_FACTORY.createStatement(association, TwitLogic.SUBJECT, subject));
        writer.handleStatement(VALUE_FACTORY.createStatement(association, TwitLogic.OBJECT, object));
        writer.handleStatement(VALUE_FACTORY.createStatement(association, TwitLogic.WEIGHT, w));
    }


    private static String encodeWord(final String normalWord) {
        return normalWord.replaceAll(" ", "_");
    }

    private static URI createResourceURI(final String resourceID) {
        return VALUE_FACTORY.createURI(TwitLogic.RESOURCES_BASEURI + resourceID);
    }


    private static boolean wordAlreadyDefined(final String normalWord) {
        boolean b = DEFINED_WORDS.contains(normalWord);
        if (!b) {
            DEFINED_WORDS.add(normalWord);
        }

        return b;
    }
}
