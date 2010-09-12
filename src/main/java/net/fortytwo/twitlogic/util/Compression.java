package net.fortytwo.twitlogic.util;

import net.contrapunctus.lzma.LzmaInputStream;
import net.contrapunctus.lzma.LzmaOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * User: josh
 * Date: Sep 12, 2010
 * Time: 9:42:34 AM
 */
public class Compression {
    public enum Algorithm {
        ZIP, LZMA
    }

    public static void main(final String[] args) {
        try {
            //testCompression(Algorithm.ZIP);
            //testCompression(Algorithm.LZMA);
            testDecompression(Algorithm.ZIP);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    // ZIP: > 5000 /s on my Macbook Pro
    // LZMA: > 330 /s on my Macbook Pro
    private static void testCompression(final Algorithm algo) throws Exception {
        String s = readInputStreamAsString(Compression.class.getResourceAsStream("exampleTransaction.xml"));
        byte[] b = s.getBytes();

        int iterations = 10000;
        long before = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            byte[] c = compress(b, algo);
        }
        long after = System.currentTimeMillis();
        long d = after - before;

        System.out.println("compressed " + iterations + " times in " + d + "ms ("
                + (iterations * 1000 / (d * 1.0)) + "/s)");
    }

    // ZIP: 187 /s on my Macbook Pro
    // LZMA: 195 /s on my Macbook Pro
    private static void testDecompression(final Algorithm algo) throws Exception {
        String s = readInputStreamAsString(Compression.class.getResourceAsStream("exampleTransaction.xml"));
        byte[] b = s.getBytes();
        byte[] c = compress(b, algo);

        int iterations = 10000;
        long before = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            byte[] d = decompress(c, algo);
            //System.out.println(new String(d));
        }
        long after = System.currentTimeMillis();
        long d = after - before;

        System.out.println("decompressed " + iterations + " times in " + d + "ms ("
                + (iterations * 1000 / (d * 1.0)) + "/s)");
    }

    public static byte[] compress(final byte[] input,
                                  final Algorithm algo) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            OutputStream os;
            switch (algo) {
                case LZMA:
                    os = new LzmaOutputStream(bos);
                    break;
                case ZIP:
                    os = new GZIPOutputStream(bos);
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

    public static byte[] decompress(final byte[] input,
                                    final Algorithm algo) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(input);
            InputStream is;
            switch (algo) {
                case LZMA:
                    is = new LzmaInputStream(bis);
                    break;
                case ZIP:
                    is = new GZIPInputStream(bis);
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
