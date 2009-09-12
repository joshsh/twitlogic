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

import net.fortytwo.twitlogic.TwitLogic;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 12, 2009
 * Time: 5:51:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class USFFreeAssociationNorm {
    private static final Random RANDOM = new Random();

    private static final ValueFactory VALUE_FACTORY = new ValueFactoryImpl();

    public static void main(final String[] args) throws Exception {
        if (2 != args.length) {
            showUsage();
        } else {
            convertAppendixAToNTriples(new File(args[0]), new File(args[1]));
        }
    }

    private static void showUsage() {
        System.out.println("Usage: <USF input directory> <ntriples output file>");
    }

    private static void convertAppendixAToNTriples(final File appendixADirectory,
                                                   final File ntriplesFile) throws IOException, RDFHandlerException {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(final File dir,
                                  final String name) {
                return name.startsWith("Cue_Target_Pairs.");
            }
        };

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
                        String line;
                        while (null != (line = b.readLine())) {
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

        String encodedSourceTerm = encodeTerm(normSourceTerm);
        String encodedTargetTerm = encodeTerm(normTargetTerm);

        URI source = createResourceURI(encodedSourceTerm);
        URI target = createResourceURI(encodedTargetTerm);
        URI association = createResourceURI(encodedSourceTerm + "-" + encodedTargetTerm + "-" + RANDOM.nextLong());

        Literal sourceLabel = VALUE_FACTORY.createLiteral(normSourceTerm);
        Literal targetLabel = VALUE_FACTORY.createLiteral(normTargetTerm);
        Literal w = VALUE_FACTORY.createLiteral(weight);

        writer.handleStatement(VALUE_FACTORY.createStatement(source, RDF.TYPE, TwitLogic.TERM));
        writer.handleStatement(VALUE_FACTORY.createStatement(source, RDFS.LABEL, sourceLabel));
        writer.handleStatement(VALUE_FACTORY.createStatement(target, RDF.TYPE, TwitLogic.TERM));
        writer.handleStatement(VALUE_FACTORY.createStatement(target, RDFS.LABEL, targetLabel));
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
}
