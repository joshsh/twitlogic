package net.fortytwo.twitlogic.data;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

/**
 * Program to convert the USF Free Association Norms into the N-Triples format.
 * See also: http://w3.usf.edu/FreeAssociation/
 * <p/>
 * User: josh
 * Date: Sep 12, 2009
 * Time: 5:51:08 PM
 */
public class USFFreeAssociationNorm extends FreeAssociationGenerator {

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

    private static void convertAppendixAToNTriples(final File appendixADirectory,
                                                   final File ntriplesFile) throws IOException, RDFHandlerException {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(final File dir,
                                  final String name) {
                return name.startsWith("Cue_Target_Pairs.");
            }
        };

        DEFINED_WORDS = new HashSet<String>();

        int associationCount = 0;
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
                            String subjectWord = cells[0];
                            String objectWord = cells[1];
                            float weight = Float.valueOf(cells[5]);

                            associate(subjectWord, objectWord, weight, writer);

                            associationCount++;
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

        System.out.println("created " + associationCount + " associations from " + fileCount + " files");
    }
}
