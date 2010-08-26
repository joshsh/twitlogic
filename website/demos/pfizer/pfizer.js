// Note: this is a crude copy-and-tweak from sparql-widget.js

function constructTweetElement(tweet) {
    // User info
    var userScreenName = plainLiteralValue(tweet, "screenName", true);
    if (null == userScreenName) {
        return null;
    }
    var userUrl = "http://twitter.com/" + userScreenName;
    var userProfileImageUrl = uriValue(tweet, "profileImage", false);

    // Tweet info
    var tweetUri = uriValue(tweet, "tweet", true);
    if (null == tweetUri) {
        return null;
    }
    var tweetId = tweetUri.substring(1 + tweetUri.lastIndexOf("/"));
    var tweetUrl = "http://twitter.com/" + userScreenName + "/status/" + tweetId;
    var tweetText = plainLiteralValue(tweet, "text", true);
    if (null == tweetText) {
        return null;
    }
    var tweetCreatedAt = typedLiteralValue(tweet, "createdAt", true);
    if (null == tweetCreatedAt) {
        return null;
    }
    //latestTimestamp = tweetCreatedAt;
    var t = tweetCreatedAt.replace(".000", "");
    var timeAgo = $.timeago(t);
    var place = uriValue(tweet, "place", false);
    var placeName = plainLiteralValue(tweet, "placeName", false);

    // Other info
    var replyUrl = "http://twitter.com/"
            + "?status=@" + userScreenName
            + "&in_reply_to_status_id=" + tweetId
            + "&in_reply_to=" + userScreenName;

    var el = document.createElement("div");
    //el.setAttribute("id", "tweet-" + tweetId);
    el.setAttribute("class", "tl-tweet");
    //el.setAttribute("style", "opacity: 1; height: 100px;");

    var wrapperEl = document.createElement("div");
    el.appendChild(wrapperEl);
    wrapperEl.setAttribute("class", "tl-tweet-contents");
    /*
     el.setAttribute("style", 0 == totalTweets % 2
     ? "background: " + settings.appearance.tweet.backgroundColor1
     : "background: " + settings.appearance.tweet.backgroundColor2);
     */

    var avatarEl = document.createElement("div");
    wrapperEl.appendChild(avatarEl);
    avatarEl.setAttribute("class", "tl-profile-image");
    var imgEl = document.createElement("div");
    avatarEl.appendChild(imgEl);
    imgEl.setAttribute("class", "tl-img");
    var profileLink = document.createElement("a");
    imgEl.appendChild(profileLink);
    profileLink.setAttribute("class", "tl-img-a");
    profileLink.setAttribute("href", userUrl);
    profileLink.setAttribute("target", "_blank");
    var img = document.createElement("img");
    profileLink.appendChild(img);
    img.setAttribute("class", "tl-img-img");
    img.setAttribute("src", userProfileImageUrl);
    img.setAttribute("alt", userScreenName + " profile");

    var textEl = document.createElement("div");
    wrapperEl.appendChild(textEl);
    textEl.setAttribute("class", "tl-tweet-text");

    var p = document.createElement("p");
    textEl.appendChild(p);
    p.setAttribute("class", "tl-tweet-text-p");

    var userLink = document.createElement("a");
    p.appendChild(userLink);
    userLink.setAttribute("class", "tl-user");
    userLink.setAttribute("href", userUrl);
    userLink.setAttribute("target", "_blank");
    userLink.appendChild(document.createTextNode(userScreenName));

    // TODO: clickable topics in tweet text
    p.appendChild(document.createTextNode(" " + tweetText + " "));

    var i = document.createElement("div");
    p.appendChild(i);
    i.setAttribute("class", "tl-footnotes");

    var tweetLink = document.createElement("a");
    i.appendChild(tweetLink);
    tweetLink.setAttribute("class", "tl-timestamp");
    tweetLink.setAttribute("href", tweetUrl);
    // TODO
    // tweetLink.setAttribute("time", ...);
    tweetLink.setAttribute("target", "_blank");
    tweetLink.appendChild(document.createTextNode(timeAgo));

    if (null != place && null != placeName) {
        i.appendChild(document.createTextNode(" in "));

        var placeLink = document.createElement("a");
        i.appendChild(placeLink);
        placeLink.setAttribute("class", "tl-place-link");
        // FIXME
        placeLink.setAttribute("href", place);
        placeLink.setAttribute("target", "_blank");
        placeLink.appendChild(document.createTextNode(placeName));
    }

    i.appendChild(document.createTextNode(" "));

    var replyLink = document.createElement("a");
    i.appendChild(replyLink);
    replyLink.setAttribute("class", "tl-reply");
    replyLink.setAttribute("href", replyUrl);
    replyLink.setAttribute("target", "_blank");
    replyLink.appendChild(document.createTextNode("reply"));

    // TODO: Retweeted by / reply to

    return el;
}

function valueOfType(resource, property, required, type) {
    var json = resource[property];

    if (null == json) {
        if (required) {
            error("no value for property '" + property + "'");
        }
        return null;
    }

    // Type check
    var t = json.type;
    if (null == t) {
        error("no type for value of property '" + property + "'");
        return null;
    }
    if (t != type) {
        error("wrong type '" + t + "' for value of property '" + property + "' (should be '" + type + "')");
    }

    var v = json.value;
    if (null == v) {
        error("null value for property '" + property + "'");
    }

    return v;
}

function uriValue(resource, property, required) {
    return valueOfType(resource, property, required, "uri");
}

function plainLiteralValue(resource, property, required) {
    return valueOfType(resource, property, required, "literal");
}

function typedLiteralValue(resource, property, required) {
    return valueOfType(resource, property, required, "typed-literal");
}

function error(message) {
    alert("Error: " + message);
}

////////////////////////////////////////

function queryForTweets(sparqlEndpoint, query, connectionTimeout) {
    $.ajax({
        url: sparqlEndpoint,
        type: "GET",
        data: "query=" + encodeURIComponent(query),
        dataType: "text",
        cache: false,
        timeout: connectionTimeout,
        beforeSend: function(request) {
            request.setRequestHeader("Accept", "application/sparql-results+json");
            //loadingIndicator.style.visibility = "visible";
            //setStatus("Searching...");
            //alert("issuing query: " + query);
        },
        success: function(data, textStatus, request) {
data = JSON.parse(data);
            //alert("data: " + data + ", textStatus: " + textStatus + ", request: " + request);
            var statusCode = parseInt(request.status);

            if (0 == statusCode) {
                alert("No response from server.");
                return;
            } else if (2 != statusCode / 100) {
                alert("Error: received status code " + statusCode);
            }

            if (null == data) {
                alert("No data received from server.");
                return;
            }

            var results = data.results;
            if (null == results) {
                alert("Invalid SPARQL JSON response (missing 'results' object).");
                return;
            }
            var bindings = results.bindings;
            if (null == bindings) {
                alert("Invalid SPARQL JSON response (missing 'bindings' object).");
                return;
            }

            drawMap(bindings);
        },
        error: function(request, textStatus, errorThrown) {
            if ("timeout" == textStatus) {
                alert("Connection timeout. Double-check query and data source.");
            } else {
                alert("Client error: check query, data source and connection: " + errorThrown + ", " + textStatus);
            }
        },
        complete: function(request, textStatus) {
            //alert("request complete: " + request.responseText);
        }
    });
}
