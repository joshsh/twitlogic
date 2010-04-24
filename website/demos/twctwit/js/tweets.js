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
    }
    return RESOURCE_URI;
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
                // TODO
                var t = data.results.bindings[i].timestamp.value.split('.000Z')[0] + 'Z';
                item_time = $.timeago(t) + '<br/>';

                item_name = data.results.bindings[i].screen_name.value;
                item_realname = data.results.bindings[i].name.value;
                item_post = data.results.bindings[i].post.value;
                item_post = _parse_post(item_post, item_name);
                var user_url = 'http://twitter.com/' + item_name;
                var depiction_url = data.results.bindings[i].depiction.value;

                item_pic = '<a href="' + user_url + '"><img width="48" height="48" src="' + depiction_url + '"/></a>';
                item_descr = data.results.bindings[i].content.value;

                output = '<div class="item">' +
                         '<div class="item-pic">' + item_pic + '</div>' +
                         '<div class="item-descr">' +
                         '<a href="' + user_url + '">' + item_realname + '</a>' +
                         '<div id="tweet_content">' + item_descr + '</div>' +
                         '<div class="item-time"><a href="' + item_post + '">' + item_time + '</a></div></div></div>';

                $('#content_wrapper').prepend(output);
                $('div.item:last-child').addClass("last_tweet");
            }
        }
    });
}

function _parse_post(post_uri, screen_name) {
    var post_id = post_uri.split("\/");
    var item_post = "http://twitter.com/" + screen_name + "/status/" + post_id[post_id.length - 1];
    return item_post;
}
