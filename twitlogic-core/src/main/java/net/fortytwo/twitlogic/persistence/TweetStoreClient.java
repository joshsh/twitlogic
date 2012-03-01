package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.util.CommonHttpClient;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TweetStoreClient extends CommonHttpClient {
    /*
    public void dumpTripleStore(final Sail sail,
                                final String endpointURI) throws TwitterClientException {
        HttpClient client = createClient(false);

        HttpUriRequest request = new HttpPost(endpointURI);

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("track", commaDelimit(keywords)));

        setEntity(request, formParams);

        HttpResponse response;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            throw new TwitterClientException(e);
        }

        if (301 == response.getStatusLine().getStatusCode()) {
            return response.getHeaders("Location")[0].getValue();
        } else {
            return originalURI;
        }
    } */
}
