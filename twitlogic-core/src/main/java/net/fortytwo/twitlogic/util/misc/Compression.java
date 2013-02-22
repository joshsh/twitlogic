package net.fortytwo.twitlogic.util.misc;

/*
import net.contrapunctus.lzma.LzmaInputStream;
import net.contrapunctus.lzma.LzmaOutputStream;
import org.jvcompress.lzo.MiniLZO;
import org.jvcompress.util.MInt;
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.DataFormatException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Compression {
    public enum Algorithm {
        ZIP, GZIP, LZMA, MINILZO, DEFLATE
    }

    public static void main(final String[] args) {
        try {
            //testCompression(Algorithm.DEFLATE);
            //testCompression(Algorithm.GZIP);
            //testCompression(Algorithm.LZMA);
            //testCompression(Algorithm.MINILZO);
            //testCompression(Algorithm.ZIP);

            //testDecompression(Algorithm.DEFLATE);
            //testDecompression(Algorithm.GZIP);
            //testDecompression(Algorithm.LZMA);
            //testDecompression(Algorithm.MINILZO);
            //testDecompression(Algorithm.ZIP);

            testCompressionOfVariableSizedFile();
            //testDecompressionOfVariableSizedFile();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void testCompressionOfVariableSizedFile() throws Exception {
        int iterations = 1000;

        StringBuilder sb = new StringBuilder("lines");
        sb.append("\tbytes");
        for (Algorithm a : Algorithm.values()) {
            sb.append("\t");
            sb.append(a);
        }
        System.out.println(sb.toString());

        for (int lines = 100; lines < 5000; lines += 100) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = new FileInputStream("/tmp/tweet_transactions.txt");
            try {
                BufferedReader b = new BufferedReader(new InputStreamReader(is));
                for (int l = 0; l < lines; l++) {
                    bos.write(b.readLine().getBytes());
                }

                byte[] bytes = bos.toByteArray();
                sb = new StringBuilder();
                sb.append(lines);
                sb.append("\t");
                sb.append(bytes.length);

                for (Algorithm a : Algorithm.values()) {
                    long before = System.currentTimeMillis();
                    for (int k = 0; k < iterations; k++) {
                        compress(bytes, a);
                    }
                    long after = System.currentTimeMillis();
                    double ms = (after - before) / (iterations * 1.0);

                    sb.append("\t");
                    sb.append(ms);
                }
                System.out.println(sb.toString());
            } finally {
                is.close();
            }
        }
    }

    private static void testDecompressionOfVariableSizedFile() throws Exception {
        int iterations = 1000;

        StringBuilder sb = new StringBuilder("lines");
        sb.append("\tbytes");
        for (Algorithm a : Algorithm.values()) {
            sb.append("\t");
            sb.append(a);
        }
        System.out.println(sb.toString());

        for (int lines = 100; lines < 5000; lines += 100) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = new FileInputStream("/tmp/tweet_transactions.txt");
            try {
                BufferedReader b = new BufferedReader(new InputStreamReader(is));
                for (int l = 0; l < lines; l++) {
                    bos.write(b.readLine().getBytes());
                }

                byte[] bytes = bos.toByteArray();
                sb = new StringBuilder();
                sb.append(lines);
                sb.append("\t");
                sb.append(bytes.length);

                for (Algorithm a : Algorithm.values()) {
                    byte[] c = compress(bytes, a);

                    long before = System.currentTimeMillis();
                    for (int k = 0; k < iterations; k++) {
                        decompress(c, a);
                    }
                    long after = System.currentTimeMillis();
                    double ms = (after - before) / (iterations * 1.0);

                    sb.append("\t");
                    sb.append(ms);
                }
                System.out.println(sb.toString());
            } finally {
                is.close();
            }
        }
    }

    // DEFLATE: > 1589 /s on my Macbook Pro
    // GZIP: > 3469 /s on my Macbook Pro
    // LZMA: > 228 /s on my Macbook Pro
    // MINILZO: > 1252 /s on my Macbook Pro
    // ZIP: > 2469 /s on my Macbook Pro
    private static void testCompression(final Algorithm algo) throws Exception {
        String s = readInputStreamAsString(Compression.class.getResourceAsStream("exampleTransaction.xml"));
        byte[] b = s.getBytes();
        //System.out.println("b.length = " + b.length);

        int iterations = 10000;
        long before = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            byte[] c = compress(b, algo);
            //System.out.println(new String(c));
            //System.out.println("" + b.length + "\t" + c.length);
        }
        long after = System.currentTimeMillis();
        long d = after - before;

        System.out.println(algo + ": compressed " + iterations + " times in " + d + "ms ("
                + (iterations * 1000 / (d * 1.0)) + "/s)");
    }

    // DEFLATE: 12987 /s on my Macbook Pro
    // GZIP: 144 /s on my Macbook Pro
    // LZMA: 389 /s on my Macbook Pro
    // MINILZO: 14925 /s on my Macbook Pro
    // ZIP: 134 /s on my Macbook Pro
    private static void testDecompression(final Algorithm algo) throws Exception {
        String s = readInputStreamAsString(Compression.class.getResourceAsStream("exampleTransaction.xml"));
        byte[] b = s.getBytes();
        byte[] c = compress(b, algo);

        int iterations = 10000;
        long before = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            byte[] d = decompress(c, algo);
            //System.out.println("d.length = " + d.length);
            //System.out.println(new String(d));
        }
        long after = System.currentTimeMillis();
        long d = after - before;

        System.out.println(algo + ": decompressed " + iterations + " times in " + d + "ms ("
                + (iterations * 1000 / (d * 1.0)) + "/s)");
    }

    public static byte[] compress(final byte[] input,
                                  final Algorithm algo) throws IOException {

        if (Algorithm.DEFLATE == algo) {
            byte[] output = new byte[input.length];
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            return Arrays.copyOfRange(output, 0, compressedDataLength);
            /*
        } else if (Algorithm.MINILZO == algo) {
            MInt mint = new MInt();
            byte[] out = new byte[input.length];
            int[] dict = new int[128 * 1024];
            Arrays.fill(dict, 0);
            MiniLZO.lzo1x_1_compress(input, input.length, out, mint, dict);
            return Arrays.copyOfRange(out, 0, mint.v);
            */
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                OutputStream os;
                switch (algo) {
                    /*
                case LZMA:
                    os = new LzmaOutputStream(bos);
                        break;
                    */
                    case GZIP:
                        os = new GZIPOutputStream(bos);
                        break;
                    case ZIP:
                        os = new ZipOutputStream(bos);
                        ((ZipOutputStream) os).putNextEntry(new ZipEntry(""));
                        break;
                    default:
                        throw new IllegalStateException("unfamiliar algorithm: " + algo);
                }
                BufferedOutputStream bufos = new BufferedOutputStream(os);
                bufos.write(input);
                bufos.close();
                return bos.toByteArray();
            } finally {
                bos.close();
            }
        }
    }

    public static byte[] decompress(final byte[] input,
                                    final Algorithm algo) throws IOException {

         if (Algorithm.DEFLATE == algo) {
            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(input, 0, input.length);
            byte[] result = new byte[10 * input.length];
            int resultLength;
            try {
                resultLength = decompresser.inflate(result);
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
            decompresser.end();
            return Arrays.copyOfRange(result, 0, resultLength);
             /*
         } else if (Algorithm.MINILZO == algo) {
             byte[] out = new byte[10 * input.length];
             MInt mint = new MInt();
             MiniLZO.lzo1x_decompress(input, input.length, out, mint);
             return Arrays.copyOfRange(out, 0, mint.v);
             */
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(input);
                InputStream is;
                switch (algo) {
                    /*
                    case LZMA:
                        is = new LzmaInputStream(bis);
                        break;
                    */
                    case GZIP:
                        is = new GZIPInputStream(bis);
                        break;
                    case ZIP:
                        is = new ZipInputStream(bis);
                        ((ZipInputStream) is).getNextEntry();
                        break;
                    default:
                        throw new IllegalStateException("unfamiliar algorithm: " + algo);
                }
                int result = is.read();
                while (result != -1) {
                    bos.write(result);
                    result = is.read();
                }
                return bos.toByteArray();
            } finally {
                bos.close();
            }
        }
    }

    /*
    public static byte[] zip(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
            bufos.write(input);
            bufos.close();
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    public static byte[] unzip(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            InputStream is = new GZIPInputStream(new ByteArrayInputStream(input));
            int result = is.read();
            while (result != -1) {
                bos.write(result);
                result = is.read();
            }
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    public static byte[] lzmaCompress(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            //InputStream is = new ByteArrayInputStream(input);
            BufferedOutputStream bufos = new BufferedOutputStream(new LzmaOutputStream(bos));
            bufos.write(input);
            bufos.close();
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }
    */

    private static String readInputStreamAsString(final InputStream in)
            throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            buf.write(result);
            result = bis.read();
        }
        return buf.toString();
    }
}
