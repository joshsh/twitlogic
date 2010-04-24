/**
 * @author Jinguang Zheng, Zhenning Shangguan
 */

var LATEST_TIMESTAMP; // the latest timestamp of all returned tweets

function init_tweets(){
    LATEST_TIMESTAMP = 0;
    $('#content_wrapper').html('');
    _fetchTweets(LATEST_TIMESTAMP);
    setInterval("_fetchTweets(LATEST_TIMESTAMP)", 20000); // update related tweets every 5sec
}

function _fetchTweets(timestamp){
    $.ajax({
        type: "GET",
        url: "data/tweets.json", // SPARQL service URL [todo: Josh, change this to SPARQL_SERVICE_URL]
        data: "query=" + encodeURIComponent(RESOURCE_URI) + "after=" + encodeURIComponent(timestamp), // query parameter [todo: Josh, chagne this to the actual SPARQL query]
        dataType: "json",
        error: function(){
            $('#content_wrapper').prepend('Error while retrieving related tweets.');
        },
        success: function(data){
            var item_descr;
            var item_pic;
            var item_time;
            var output;
            
            var num_of_tweets = data.results.bindings.length;
            if (num_of_tweets == 0) {
                $('#content_wrapper').html('No related tweets.');
                return;
            }
            
            // keeps track of LATEST_TIMESTAMP
            LATEST_TIMESTAMP = data.results.bindings[num_of_tweets - 1].timestamp.value;
            
            for (var i = 0; i < num_of_tweets; i++) {
                item_pic = '<a herf="' + data.results.bindings[i].depiction.value + '"><img src="' + data.results.bindings[i].depiction.value + '"></a>';
                item_descr = data.results.bindings[i].content.value;
                
                var t = data.results.bindings[i].timestamp.value.split('.000Z')[0] + 'Z';
                item_time = $.timeago(t) + '<br/>';
                
                item_name = data.results.bindings[i].screen_name.value;
                item_realname = data.results.bindings[i].name.value;
                item_post = data.results.bindings[i].post.value;
                item_post = _parse_post(item_post, item_name);
                
                output = '<div class="item"><div class="item-pic">' + item_pic + '</div><div class="item-descr"><a href="http://twitter.com/' + item_name + '">' + item_realname + '</a><div id="tweet_content">' + item_descr + '</div><div class="item-time"><a href="' + item_post + '">' + item_time + '</a></div></div></div>';
                
                $('#content_wrapper').prepend(output);
                $('div.item:last-child').addClass("last_tweet");
            }
        }
    });
}

function _parse_post(post_uri, screen_name){
    var post_id = post_uri.split("\/");
    var item_post = "http://twitter.com/" + screen_name + "/status/" + post_id[post_id.length - 1];
    return item_post;
}
