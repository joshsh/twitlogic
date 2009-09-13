package net.fortytwo.twitlogic.data;

import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Literal;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import net.fortytwo.twitlogic.TwitLogic;

/**
 * Program to convert the USF Free Association Norms into the N-Triples format.
 * See also: http://w3.usf.edu/FreeAssociation/
 * 
 * User: josh
 * Date: Sep 12, 2009
 * Time: 5:51:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class USFFreeAssociationNorm {
    private static final Random RANDOM = new Random();

    private static final ValueFactory VALUE_FACTORY = new ValueFactoryImpl();

    // A Set to help eliminate duplicate statements.
    private static Set<String> DEFINED_TERMS;

    public static void main(final String[] args) throws Exception {
        System.out.println("args:");
        for (String s : args) {
            System.out.println("\t*) " + s);
        }
        if (2 != args.length) {
            showUsage();
        } else {
            convertAppendixAToNTriples(new File(args[0]), new File(args[1]));
        }
    }

    private static void showUsage() {
        System.out.println("Usage: <USF input directory> <ntriples output file>");
    }

    // Note: creates many duplicate rdf:type and rdfs:label statements for terms
    private static void convertAppendixAToNTriples(final File appendixADirectory,
                                                   final File ntriplesFile) throws IOException, RDFHandlerException {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(final File dir,
                                  final String name) {
                return name.startsWith("Cue_Target_Pairs.");
            }
        };

        DEFINED_TERMS = new HashSet<String>();

        int lineCount = 0;
        int fileCount = 0;
        OutputStream out = new FileOutputStream(ntriplesFile);
        try {
            RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, out);
            writer.startRDF();
            try {
                for (File f : appendixADirectory.listFiles(filter)) {
                    fileCount++;
                    BufferedReader b = new BufferedReader(new FileReader(f));
                    try {
                        // Skip the four header lines
                        for (int i = 0; i < 4; i++) {
                            b.readLine();
                        }

                        String line;
                        while (null != (line = b.readLine())) {
                            // If we've reached the footer, we're done with this file.
                            if (line.startsWith("<")) {
                                break;
                            }

                            String[] cells = line.split(", ");
                            String sourceTerm = cells[0];
                            String targetTerm = cells[1];
                            float weight = Float.valueOf(cells[5]);

                            associate(sourceTerm, targetTerm, weight, writer);

                            lineCount++;
                        }
                    } finally {
                        b.close();
                    }
                }
            } finally {
                writer.endRDF();
            }
        } finally {
            out.close();
        }

        System.out.println("converted " + lineCount + " lines of input from " + fileCount + " files");
    }

    private static void associate(final String sourceTerm,
                                  final String targetTerm,
                                  final float weight,
                                  final RDFWriter writer) throws RDFHandlerException {
        String normSourceTerm = TwitLogic.normalizeTerm(sourceTerm);
        String normTargetTerm = TwitLogic.normalizeTerm(targetTerm);
        if (!TwitLogic.isNormalTerm(normSourceTerm) || !TwitLogic.isNormalTerm(normTargetTerm)) {
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

    private static boolean termAlreadyDefined(final String normalTerm) {
        boolean b = DEFINED_TERMS.contains(normalTerm);
        if (!b) {
            DEFINED_TERMS.add(normalTerm);
        }

        return b;
    }

    private static String encodeTerm(final String normalTerm) {
        return normalTerm.replaceAll(" ", "_");
    }

    private static URI createResourceURI(final String resourceID) {
        return VALUE_FACTORY.createURI(TwitLogic.RESOURCES_NAMESPACE + resourceID);
    }
}
