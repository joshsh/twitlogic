package net.fortytwo.twitlogic.data;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

/**
 * Program to convert the Edinburgh Associative Thesaurus into the N-Triples format.
 * See also: http://www.eat.rl.ac.uk/
 * <p/>
 * User: josh
 * Date: Sep 13, 2009
 * Time: 9:39:51 PM
 */
public class EdinburghAssociativeThesaurus extends FreeAssociationGenerator {
    public static void main(final String[] args) throws Exception {
        System.out.println("args:");
        for (String s : args) {
            System.out.println("\t*) " + s);
        }
        if (2 != args.length) {
            showUsage();
        } else {
            convertSRConciseFileToNTriples(new File(args[0]), new File(args[1]));
        }
    }

    private static void showUsage() {
        System.out.println("Usage: <EAT sr_concise> <ntriples output file>");
    }

    private static void convertSRConciseFileToNTriples(final File srConciseFile,
                                                       final File ntriplesFile) throws IOException, RDFHandlerException {
        DEFINED_WORDS = new HashSet<String>();

        int associationCount = 0;
        OutputStream out = new FileOutputStream(ntriplesFile);
        try {
            RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, out);
            writer.startRDF();
            try {
                BufferedReader b = new BufferedReader(new FileReader(srConciseFile));
                try {
                    String line;
                    while (null != (line = b.readLine())) {
                        if (0 == line.length()) {
                            break;
                        }

                        String subjectWord = normalizeWord(line);

                        // There should be an even number of lines in this file.
                        line = b.readLine();

                        if (isNormalWord(subjectWord)) {
                            String[] cells = line.trim().split("[|]");

                            int totalWeight = 0;

                            for (int i = 1; i < cells.length; i += 2) {
                                totalWeight += Integer.valueOf(cells[i]);
                            }

                            for (int i = 0; i < cells.length; i += 2) {
                                String objectWord = cells[i];
                                float weight = Float.valueOf(cells[i + 1]) / totalWeight;

                                associate(subjectWord, objectWord, weight, writer);
                                associationCount++;
                            }
                        } else {
                            System.err.println(subjectWord + " could not be normalized. No association created.");
                        }
                    }
                } finally {
                    b.close();
                }
            } finally {
                writer.endRDF();
            }
        } finally {
            out.close();
        }

        System.out.println("created " + associationCount + " associations");
    }
}
