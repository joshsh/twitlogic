package net.fortytwo.twitlogic.services.bitly;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.CommonHttpClient;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class BitlyClient extends CommonHttpClient {
    private static final String UTF_8 = "UTF-8";

    private static final String
            SHORTEN_URL = "http://api.bit.ly/shorten",
            VERSION = "2.0.1";

    private enum Field {
        ERRORCODE("errorCode"),
        ERRORMESSAGE("errorMessage"),
        RESULTS("results"),
        HASH("hash"),
        SHORTKEYWORDURL("shortKeywordUrl"),
        SHORTURL("shortUrl"),
        USERHASH("userHash"),
        STATUSCODE("statusCode");

        private final String name;

        private Field(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
        /*
            "errorCode": 0,
    "errorMessage": "",
    "results": {
        "http://fortytwo.net/": {
            "hash": "4ceqWe",
            "shortKeywordUrl": "",
            "shortUrl": "http://bit.ly/1uXvqD",
            "userHash": "1uXvqD"
        }
    },
    "statusCode": "OK"
         */
    }

    private final String bitlyLogin;
    private final String bitlyAPIKey;

    // TODO: does bit.ly have a rate-limiting policy?
    private final RequestExecutor client = new DefaultRequestExecutor();

    public BitlyClient() throws BitlyClientException {
        TypedProperties conf = TwitLogic.getConfiguration();
        try {
            bitlyLogin = conf.getString(TwitLogic.BITLY_LOGIN);
            bitlyAPIKey = conf.getString(TwitLogic.BITLY_APIKEY);
        } catch (PropertyException e) {
            throw new BitlyClientException(e);
        }
    }

    public String shorten(final String longUrl) throws BitlyClientException {
        StringBuilder sb = new StringBuilder();
        sb.append(SHORTEN_URL)
                .append("?version=").append(VERSION)
                .append("&longUrl=").append(percentEncode(longUrl))
                .append("&login=").append(bitlyLogin)
                .append("&apiKey=").append(bitlyAPIKey);

        try {
            HttpGet request = new HttpGet(sb.toString());
            HttpResponse response = requestUntilSucceed(request, client);
            HttpEntity responseEntity = response.getEntity();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            responseEntity.writeTo(bos);
            JSONObject object = new JSONObject(bos.toString());
            bos.close();
            return object.getJSONObject(Field.RESULTS.toString())
                    .getJSONObject(longUrl)
                    .getString(Field.SHORTURL.toString());
        } catch (Exception e) {
            throw new BitlyClientException(e);
        }
    }

    public static String percentEncode(final String s) throws BitlyClientException {
        try {
            return URLEncoder.encode(s, UTF_8).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new BitlyClientException(e);
        }
    }
}
