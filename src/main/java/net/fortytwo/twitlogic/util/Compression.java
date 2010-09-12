package net.fortytwo.twitlogic.util;

import net.contrapunctus.lzma.LzmaInputStream;
import net.contrapunctus.lzma.LzmaOutputStream;
import org.jvcompress.lzo.MiniLZO;
import org.jvcompress.util.MInt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * User: josh
 * Date: Sep 12, 2010
 * Time: 9:42:34 AM
 */
public class Compression {
    public enum Algorithm {
        ZIP, GZIP, LZMA, MINILZO
    }

    public static void main(final String[] args) {
        try {
            //testCompression(Algorithm.ZIP);
            //testCompression(Algorithm.LZMA);
            //testCompression(Algorithm.MINILZO);
            //testDecompression(Algorithm.ZIP);
            testDecompression(Algorithm.MINILZO);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    // GZIP: > 5000 /s on my Macbook Pro
    // MINILZO: > 2500 /s on my Macbook Pro
    // LZMA: > 330 /s on my Macbook Pro
    private static void testCompression(final Algorithm algo) throws Exception {
        String s = readInputStreamAsString(Compression.class.getResourceAsStream("exampleTransaction.xml"));
        byte[] b = s.getBytes();

        int iterations = 10000;
        long before = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            byte[] c = compress(b, algo);
            //System.out.println(new String(c));
            //System.out.println("" + b.length + "\t" + c.length);
        }
        long after = System.currentTimeMillis();
        long d = after - before;

        System.out.println("compressed " + iterations + " times in " + d + "ms ("
                + (iterations * 1000 / (d * 1.0)) + "/s)");
    }

    // GZIP: 187 /s on my Macbook Pro
    // MINILZO: 34800 /s on my Macbook Pro
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
        if (Algorithm.MINILZO == algo) {
            MInt mint = new MInt();
            byte[] out = new byte[input.length];
            int[] dict = new int[128 * 1024];
            Arrays.fill(dict, 0);
            MiniLZO.lzo1x_1_compress(input, input.length, out, mint, dict);
            return Arrays.copyOfRange(out, 0, mint.v);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                OutputStream os;
                switch (algo) {
                    case LZMA:
                        os = new LzmaOutputStream(bos);
                        break;
                    case GZIP:
                        os = new GZIPOutputStream(bos);
                        break;
                    case ZIP:
                        os = new ZipOutputStream(bos);
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
        if (Algorithm.MINILZO == algo) {
            byte[] out = new byte[10*input.length];
            MInt mint = new MInt();
            MiniLZO.lzo1x_decompress(input, input.length, out, mint);
            return Arrays.copyOfRange(out, 0, mint.v);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(input);
                InputStream is;
                switch (algo) {
                    case LZMA:
                        is = new LzmaInputStream(bis);
                        break;
                    case GZIP:
                        is = new GZIPInputStream(bis);
                        break;
                    case ZIP:
                        is = new ZipInputStream(bis);
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
