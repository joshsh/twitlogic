package net.fortytwo.twitlogic.services.twitter;

import net.fortytwo.twitlogic.util.CommonHttpClient;
import net.fortytwo.twitlogic.services.twitter.errors.UnauthorizedException;
import org.apache.http.client.HttpResponseException;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RestfulJSONClient extends CommonHttpClient {
    protected final RequestExecutor restAPIClient;

    public RestfulJSONClient() {
        restAPIClient = new RequestExecutor() {
            private final RateLimiter rateLimiter = new RateLimiter();
            private final HttpClient client = createClient(false);

            public HttpResponse execute(HttpUriRequest request) throws TwitterClientException {
                try {
                    rateLimiter.throttleRequest();
                } catch (InterruptedException e) {
                    throw new TwitterClientException(e);
                }

                HttpResponse response;
                try {
                    response = client.execute(request);
                } catch (SocketException e) {
                    throw new TwitterConnectionResetException(e);
                //} catch (HttpResponseException e) {
                //    throw new TwitterClientException(e);
                } catch (IOException e) {
                    throw new TwitterClientException(e);
                }

                rateLimiter.updateRateLimitStatus(response);
                return response;
            }
        };
    }

    protected JSONObject requestJSONObject(final HttpUriRequest request) throws TwitterClientException {
        try {
            HttpResponse response = requestUntilSucceed(request, restAPIClient);
            HttpEntity responseEntity = response.getEntity();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            responseEntity.writeTo(bos);
            JSONObject object = new JSONObject(bos.toString());
            bos.close();
            checkForTwitterAPIException(object);
            return object;
        } catch (IOException e) {
            throw new TwitterClientException(e);
        } catch (JSONException e) {
            throw new TwitterClientException(e);
        }
    }

    protected void checkForTwitterAPIException(final JSONObject json) throws TwitterAPIException {
        String msg = json.optString(TwitterAPI.ErrorField.ERROR.toString());

        if (null != msg && 0 < msg.length()) {
            System.out.println(json);
            if (msg.equals("Not authorized")) {
                throw new UnauthorizedException();
            } else {
                throw new TwitterAPIException(msg);
            }
        }
    }
}
