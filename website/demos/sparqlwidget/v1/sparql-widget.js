/*

 TwitLogic SPARQL widget
 Copyright (C) 2010 by Joshua Shinavier
 http://twitlogic.fortytwo.net

 Acknowledgements:
 1) look and feel inspired by: http://twitter.com/goodies/widget_search
 2) "loading" gif by: http://ajaxload.info/
 3) Uses http://jquery.com and http://timeago.yarp.com

 */

// Relax type-checking for not-entirely-compliant RDF-JSON engines.
var strictAboutRDFJSONTypes = false;

var TwitLogic = {};

TwitLogic.SparqlWidget = function(settings) {

    // Timestamp of the latest tweet retrieved so far.
    var latestTimestamp;

    var totalTweets;

    // Elements
    var loadingIndicator;
    var statusMessage;
    var tweetStack;

    // formatting //////////////////////////////////////////////////////////////////

    String.prototype.trim = function () {
        return this.replace(/^\s*/, "").replace(/\s*$/, "");
    };

    function padNumber(n, digits) {
        var s = "" + n;
        for (var i = 0; i < digits - s.length; i++) {
            s = "0" + s;
        }
        return s;
    }

    function dateToXsdDateTime(date) {
        return padNumber(date.getUTCFullYear(), 4)
                + "-" + padNumber(date.getUTCMonth() + 1, 2)
                + "-" + padNumber(date.getUTCDate(), 2)
                + "T" + padNumber(date.getUTCHours(), 2)
                + ":" + padNumber(date.getUTCMinutes(), 2)
                + ":" + padNumber(date.getUTCSeconds(), 2)
                + ".000Z";
    }

    // layout and content //////////////////////////////////////////////////////////

    function error(message) {
        alert("Error: " + message);
    }

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
        latestTimestamp = tweetCreatedAt;
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
        el.setAttribute("style", 0 == totalTweets % 2
                ? "background: " + settings.appearance.tweet.backgroundColor1
                : "background: " + settings.appearance.tweet.backgroundColor2);

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

    // SPARQL and RDF //////////////////////////////////////////////////////////////

    function valueOfType(resource, property, required, type) {
        var json = resource[property];

        if (null == json) {
            if (required) {
                error("no value for property '" + property + "'");
            }
            return null;
        }

        // Type check
        /*
        var t = json.type;
        if (null == t) {
            error("no type for value of property '" + property + "'");
            return null;
        }
        if (t != type) {
            if (strictAboutRDFJSONTypes) {
                error("wrong type '" + t + "' for value of property '" + property + "' (should be '" + type + "')");
            }
        }*/

        //var v = json.value;
        var v = json[type];
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
        return valueOfType(resource, property, required, "literal");
//        return valueOfType(resource, property, required, "typed-literal");
    }

    function queryForTweets(query) {
        $.ajax({
            url: settings.query.sparqlEndpoint,
            type: "GET",
            data: "query=" + encodeURIComponent(query),

            // Temporary: allow for badly-escaped RDF-JSON
            //dataType: "text",
            //        dataType: "json",
                    dataType: "xml",

            cache: false,
            timeout: settings.query.connectionTimeout,
            beforeSend: function(request) {
                try {
                    //request.setRequestHeader('Accept', '');
                    //request.setRequestHeader("Accept", null);
//                    request.setRequestHeader("Accept", "application/sparql-results+json;q=0.0");
                    request.setRequestHeader("Accept", "application/sparql-results+xml;q=0.0");
                    loadingIndicator.style.visibility = "visible";
                    setStatus("Searching...");
                } catch (e) {
                    alert("error: " + e);
                }
                //alert("issuing query: " + query);
            },
            success: function(data, textStatus, request) {
                // Temporary: allow for badly-escaped RDF-JSON
                //data = JSON.parse(data);
                data = $.xml2json(data);

                // document.getElementById("widget-goes-here").appendChild(document.createTextNode(JSON.stringify(data)));

                var statusCode = parseInt(request.status);

                if (0 == statusCode) {
                    setStatus("No response from server.");
                    return;
                } else if (2 != statusCode / 100) {
                    alert("Error: received status code " + statusCode)
                }

                if (null == data) {
                    setStatus("No data received from server.");
                    return;
                }

                var results = data.results;
                if (null == results) {
                    setStatus("Invalid SPARQL JSON response (missing 'results' object).");
                    return;
                }

                var bindings = results.result;
                if (null == bindings) {
                    setStatus("Invalid SPARQL JSON response (missing 'bindings' object).");
                    return;
                }

                //alert("bindings.length = " + bindings.length);

                if (0 < bindings.length) {
                    setStatus("Loading...");
                }

                for (var i = bindings.length - 1; i >= 0; i--) {
                    var binding = bindings[i].binding;
                    var tweet = {};
                    for (var j = 0; j < binding.length; j++) {
                        var o = binding[j];
                        tweet[o.name] = o;
                    }
                    pushTweet(tweet);
                }

                if (0 == totalTweets) {
                    setStatus("No tweets! How sad.");
                } else {
                    setStatus(null);
                }
            },
            error: function(request, textStatus, errorThrown) {
                if ("timeout" == textStatus) {
                    setStatus("Connection timeout. Double-check query and data source.");
                } else {
                    //setStatus("Client error: check query, data source and connection: " + errorThrown + ", " + textStatus);
                    setErrorStatus(request, textStatus, errorThrown);
                }
            },
            complete: function(request, textStatus) {
                //alert("got response for request at URL: " + request.url);                
                loadingIndicator.style.visibility = "hidden";
            }
        });
    }

    // the tweet stack /////////////////////////////////////////////////////////////

    function updateTweets() {
        var q = settings.query.sparqlQuery
                .replace("# TIME FILTER #", "FILTER(?createdAt > \"" + latestTimestamp + "\"^^xsd:dateTime) .")
                .replace("# LIMIT #", "LIMIT " + settings.appearance.maxVisibleTweets);
        //alert("issuing query: " + q);
        queryForTweets(q);
    }

    /**
     *
     * @param tweet the SPARQL JSON representation of a tweet
     */
    function pushTweet(tweet) {
        var tweetEl = constructTweetElement(tweet);
        if (null == tweetEl) {
            error("null tweet element");
            return;
        }

        while (tweetStack.children.length >= settings.appearance.maxVisibleTweets) {
            tweetStack.removeChild(tweetStack.lastChild);
        }

        tweetStack.insertBefore(tweetEl, tweetStack.firstChild);

        totalTweets++;
    }

    // status //////////////////////////////////////////////////////////////////

    function clearStatus() {
        var status = statusMessage;

        if (status.hasChildNodes()) {
            while (status.childNodes.length >= 1) {
                status.removeChild(status.firstChild);
            }
        }

        return status;
    }

    function setStatus(message) {
        var status = clearStatus();

        if (null != message) {
            status.appendChild(document.createTextNode(message));
        }
    }

    function setErrorStatus(request, textStatus, errorThrown) {
        var status = clearStatus();

        var code = parseInt(request.status);
        if (4 == code / 100) {
            status.appendChild(document.createTextNode("Client error"));
        } else if (5 == code / 100) {
            if (503 == code) {
                status.appendChild(document.createTextNode("Server unavailable"));
            } else {
                status.appendChild(document.createTextNode("Server error"));
            }
        } else {
            status.appendChild(document.createTextNode("Error code " + code));
        }

        status.appendChild(document.createTextNode(" ("));

        var errorLink = document.createElement("a");
        status.appendChild(errorLink);
        errorLink.setAttribute("class", "tl-error-link");
        errorLink.setAttribute("href", "#");
        errorLink.onclick = function() {
            alert("Error response " + request.status + " from server.\n\n" + request.responseText);
        };
        errorLink.appendChild(document.createTextNode("click for details"));

        status.appendChild(document.createTextNode(")."));
    }

    ////////////////////////////////////////////////////////////////////////////

    function buildWidget() {
        var widget = document.createElement("div");
        widget.setAttribute("class", "tl-widget");
        widget.style.color = settings.appearance.shell.textColor;
        widget.style.width = "" + settings.appearance.width + "px";

        var doc = document.createElement("div");
        widget.appendChild(doc);
        doc.setAttribute("class", "tl-widget-contents");
        if (null != settings.appearance.shell.headerImage) {
            doc.style.backgroundImage = "url(" + settings.appearance.shell.headerImage + ")";
        }
        doc.style.backgroundColor = settings.appearance.shell.backgroundColor;

        var header = document.createElement("div");
        doc.appendChild(header);
        header.setAttribute("class", "tl-header");

        var h3 = document.createElement("h3");
        header.appendChild(h3);
        h3.appendChild(document.createTextNode(settings.comment));

        var h4 = document.createElement("h4");
        header.appendChild(h4);
        h4.appendChild(document.createTextNode(settings.title));

        statusMessage = document.createElement("div");
        header.appendChild(statusMessage);
        statusMessage.setAttribute("class", "tl-status-message");

        loadingIndicator = document.createElement("div");
        header.appendChild(loadingIndicator);
        loadingIndicator.setAttribute("class", "tl-busy-animation");

        var body = document.createElement("div");
        doc.appendChild(body);
        body.setAttribute("class", "tl-body");

        var timeline = document.createElement("div");
        body.appendChild(timeline);
        timeline.setAttribute("class", "tl-tweet-container");
        timeline.style.overflowY = settings.appearance.scroll ? "auto" : "hidden";
        timeline.style.height = "" + settings.appearance.height + "px";
        timeline.style.backgroundColor = settings.appearance.tweet.backgroundColor1;

        tweetStack = document.createElement("div");
        timeline.appendChild(tweetStack);
        tweetStack.setAttribute("class", "tl-tweet-stack");

        var footer = document.createElement("div");
        doc.appendChild(footer);
        footer.setAttribute("class", "tl-footer");
        if (null != settings.appearance.shell.footerImage) {
            footer.style.backgroundImage = "url(" + settings.appearance.shell.footerImage + ")";
        }
        footer.style.backgroundColor = settings.appearance.shell.backgroundColor;

        //        <a href="http://twitter.com" target="_blank">
        //            <img id="twitlogic-logo" height="14" src="images/twitlogic-minilogo.png" alt="TwitLogic">
        //        </a>

        var about = document.createElement("span");
        footer.appendChild(about);

        var aboutLink = document.createElement("a");
        about.appendChild(aboutLink);
        aboutLink.setAttribute("class", "tl-about-link");
        aboutLink.setAttribute("href", "http://twitlogic.fortytwo.net");
        aboutLink.setAttribute("target", "_blank");
        aboutLink.appendChild(document.createTextNode("SPARQL powered"));
//        aboutLink.appendChild(document.createTextNode("TwitLogic"));
        aboutLink.style.backgroundColor = settings.appearance.shell.backgroundColor;

        return widget;
    }

    ////////////////////////////////////////////////////////////////////////////

    return {
        widget: function() {
            return buildWidget();
        },
        start: function() {
            if (null == settings.query.queryWindow) {
                latestTimestamp = "2000-01-01T00:00:00.000Z";
            } else {
                //var d= new Date();
                var d = new Date(new Date().getTime() - settings.query.queryWindow);
                latestTimestamp = dateToXsdDateTime(d);
            }
            totalTweets = 0;
            //alert("latestTimestamp: " + latestTimestamp);

            $.get(settings.query.sparqlQueryUrl, '', function(data) {
                //alert(data);
                settings.query.sparqlQuery = data;
                updateTweets();
                setInterval(updateTweets, settings.query.updateInterval);
            });
        }
    };
}
