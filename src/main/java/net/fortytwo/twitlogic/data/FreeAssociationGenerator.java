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
            NORMAL_TERM = Pattern.compile("[a-z]+([ ][a-z]+)*");

    private static final ValueFactory VALUE_FACTORY = new ValueFactoryImpl();

    private static final Random RANDOM = new Random();

    // A Set to help eliminate duplicate statements.
    protected static Set<String> DEFINED_TERMS;

    public static String normalizeTerm(final String term) {
        String s = term.trim().toLowerCase();

        Matcher m = WHITESPACE.matcher(s);
        s = m.replaceAll(" ");

        return s;
    }

    public static boolean isNormalTerm(final String term) {
        return NORMAL_TERM.matcher(term).matches();
    }

    protected static void associate(final String sourceTerm,
                                    final String targetTerm,
                                    final float weight,
                                    final RDFWriter writer) throws RDFHandlerException {
        String normSourceTerm = normalizeTerm(sourceTerm);
        String normTargetTerm = normalizeTerm(targetTerm);
        if (!isNormalTerm(normSourceTerm) || !isNormalTerm(normTargetTerm)) {
            System.err.println("One of {" + sourceTerm + ", " + targetTerm + "} could not be normalized. No association created.");
            return;
        }

        String encodedSourceTerm = encodeTerm(normSourceTerm);
        String encodedTargetTerm = encodeTerm(normTargetTerm);

        URI source = createResourceURI(encodedSourceTerm);
        URI target = createResourceURI(encodedTargetTerm);
        URI association = createResourceURI(encodedSourceTerm + "-" + encodedTargetTerm + "-" + RANDOM.nextInt(Integer.MAX_VALUE));

        Literal sourceLabel = VALUE_FACTORY.createLiteral(normSourceTerm);
        Literal targetLabel = VALUE_FACTORY.createLiteral(normTargetTerm);
        Literal w = VALUE_FACTORY.createLiteral(weight);

        if (!termAlreadyDefined(normSourceTerm)) {
            writer.handleStatement(VALUE_FACTORY.createStatement(source, RDF.TYPE, TwitLogic.TERM));
            writer.handleStatement(VALUE_FACTORY.createStatement(source, RDFS.LABEL, sourceLabel));
        }

        if (!termAlreadyDefined(normTargetTerm)) {
            writer.handleStatement(VALUE_FACTORY.createStatement(target, RDF.TYPE, TwitLogic.TERM));
            writer.handleStatement(VALUE_FACTORY.createStatement(target, RDFS.LABEL, targetLabel));
        }

        writer.handleStatement(VALUE_FACTORY.createStatement(association, RDF.TYPE, TwitLogic.ASSOCIATION));
        writer.handleStatement(VALUE_FACTORY.createStatement(association, TwitLogic.SOURCE, source));
        writer.handleStatement(VALUE_FACTORY.createStatement(association, TwitLogic.TARGET, target));
        writer.handleStatement(VALUE_FACTORY.createStatement(association, TwitLogic.WEIGHT, w));
    }


    private static String encodeTerm(final String normalTerm) {
        return normalTerm.replaceAll(" ", "_");
    }

    private static URI createResourceURI(final String resourceID) {
        return VALUE_FACTORY.createURI(TwitLogic.RESOURCES_NAMESPACE + resourceID);
    }


    private static boolean termAlreadyDefined(final String normalTerm) {
        boolean b = DEFINED_TERMS.contains(normalTerm);
        if (!b) {
            DEFINED_TERMS.add(normalTerm);
        }

        return b;
    }
}
