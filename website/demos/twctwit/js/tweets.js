/**
 * @author Jinguang Zheng, Zhenning Shangguan
 */

var LATEST_TIMESTAMP; // the latest timestamp of all returned tweets
var TOTAL_TWEETS;

function init_tweets() {
    LATEST_TIMESTAMP = 0;
    TOTAL_TWEETS = 0;
    $('#content_wrapper').html('');
    _fetchTweets(LATEST_TIMESTAMP);
    setInterval("_fetchTweets(LATEST_TIMESTAMP)", 10000); // update related tweets every 10 seconds
}

// Ensures that each GET request is unique (to discourage client-side caching).
var count = 0;

function findTopic() {
    var i = location.href.lastIndexOf("?topic=");
    if (0 < i) {
        var s = location.href.substring(i + 7);
        s = s.substring(0, s.length - 1);
        s = decodeURIComponent(s);
        return s;
    } else {
        return RESOURCE_URI;
    }
}

function _fetchTweets(timestamp) {
    var topic = findTopic();

    $.ajax({
        type: "GET",
        url: "/stream/relatedTweets", // "related tweets" service URL
        data: "resource=" + encodeURIComponent(topic) + (0 == timestamp ? "" : "&after=" + encodeURIComponent(timestamp)) + "&limit=20" + "&count=" + ++count, // query parameter [todo: Josh, chagne this to the actual SPARQL query]
        dataType: "json",
        error: function() {
            $('#content_wrapper').html('Error while retrieving related tweets.');
        },
        success: function(data) {
            var item_descr;
            var item_pic;
            var item_time;
            var output;

            var num_of_tweets = data.results.bindings.length;
            if (0 == num_of_tweets) {
                if (0 == TOTAL_TWEETS) {
                    $('#content_wrapper').html('No related tweets.');
                    return;
                }
            } else {
                if (0 == TOTAL_TWEETS) {
                    $('#content_wrapper').html('');
                }
            }

            TOTAL_TWEETS += data.results.bindings.length;

            if (0 < data.results.bindings.length) {
                // keeps track of LATEST_TIMESTAMP
                LATEST_TIMESTAMP = data.results.bindings[0].timestamp.value;
                //alert("updated timestamp to " + LATEST_TIMESTAMP);
            }

            for (var i = num_of_tweets - 1; i >= 0; i--) {
                var tweetEl = tweetElement(data.results.bindings[i]);
                var content_wrapper = document.getElementById("content_wrapper");
                content_wrapper.insertBefore(tweetEl, content_wrapper.firstChild);
            }
        }
    });
}


function tweetElement(tweet) {
    var t = tweet.timestamp.value.split('.000Z')[0] + 'Z';
    var item_time = $.timeago(t);

    var item_name = tweet.screen_name.value;
    var item_realname = tweet.name.value;
    var item_post = tweet.post.value;
    var item_post = _parse_post(item_post, item_name);
    var item_descr = tweet.content.value;
    var user_url = 'http://twitter.com/' + item_name;
    var depiction_url = tweet.depiction.value;

    // top-level element
    var el = document.createElement("table");
    el.setAttribute("class", "tweet");
    var row = document.createElement("tr");
    el.appendChild(row);

    // profile picture
    var pic = document.createElement("td");
    row.appendChild(pic);
    pic.setAttribute("style", "vertical-align: top; padding: 5px;");
    var pic_a = document.createElement("a");
    pic.appendChild(pic_a);
    pic_a.setAttribute("href", item_post);
    pic_a.setAttribute("target", "_blank");
    var pic_img = document.createElement("img");
    pic_a.appendChild(pic_img);
    pic_img.setAttribute("class", "profile-image");
    pic_img.setAttribute("width", "48");
    pic_img.setAttribute("height", "48");
    pic_img.setAttribute("src", depiction_url);

    var body = document.createElement("td");
    row.appendChild(body);
    body.setAttribute("style", "vertical-align: top; text-align: left; padding: 5px; width: 100%;");

    // name
    var name = document.createElement("span");
    body.appendChild(name);
    var name_a = document.createElement("a");
    name.appendChild(name_a);
    name.setAttribute("target", "_blank");
    name_a.setAttribute("href", user_url);
    name_a.appendChild(document.createTextNode(item_realname));

    body.appendChild(document.createElement("br"));

    // text content
    var content = document.createElement("span");
    body.appendChild(content);
    content.setAttribute("class", "tweet-content");
    content.appendChild(document.createTextNode(item_descr));

    body.appendChild(document.createElement("br"));

    // timestamp element
    var timestamp = document.createElement("div");
    body.appendChild(timestamp);
    timestamp.setAttribute("class", "item-time");
    var timestamp_a = document.createElement("a");
    timestamp.appendChild(timestamp_a);
    timestamp_a.setAttribute("target", "_blank");
    timestamp_a.setAttribute("href", item_post);
    timestamp_a.appendChild(document.createTextNode(item_time));

    //  var spacer = document.createElement("span");
    //  footer.appendChild(spacer);
    //  footer.setAttribute("style", "width: 100%;");

    if (null != tweet.graph) {
        var nanotated = document.createElement("div");
        body.appendChild(nanotated);
        nanotated.setAttribute("style", "width: 100%; text-align: right;");
        var nanotated_a = document.createElement("a");
        nanotated.appendChild(nanotated_a);
        nanotated_a.setAttribute("style", "color: #FF0000; font-weight: bold;");
        nanotated_a.setAttribute("href", "http://wiki.github.com/joshsh/twitlogic/syntax-conventions");
        nanotated_a.setAttribute("target", "_blank");
        nanotated_a.appendChild(document.createTextNode("Nanotated!"));
    }

    return el;
}

function _parse_post(post_uri, screen_name) {
    var post_id = post_uri.split("\/");
    var item_post = "http://twitter.com/" + screen_name + "/status/" + post_id[post_id.length - 1];
    return item_post;
}
