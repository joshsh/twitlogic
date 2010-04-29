
function _topicLink(uri) {
    var a = document.createElement("a");
    a.setAttribute("href", topicTweetsURL(uri));
    a.appendChild(document.createTextNode(abbreviate(uri)));
    return a;
}

function init_tags(uri) {
    $.ajax({
        type: "GET",
        url: "../../stream/relatedTags",
        data: "resource=" + uri,
        dataType: "json",
        success: function(data) {
            var rt = document.getElementById("relatedtags");
            var rt_list = document.getElementById("relatedtags_list");

            //alert("data.length: " + data.length);

            for (var i = 0; i < data.length; i++) {
                var a = _topicLink(data[i]);

                if (0 < i) {
                    rt_list.appendChild(document.createTextNode(", "));
                }

                rt_list.appendChild(a);
            }

            if (0 < data.length) {
                rt.style.visibility = "visible";
            }

            /*
            if (modaltweets.hasChildNodes()) {
                while (modaltweets.childNodes.length >= 1) {
                    modaltweets.removeChild(modaltweets.firstChild);
                }
            }

            //alert(data);
            var bindings = data.results.bindings;
            for (var i = 0; i < bindings.length; i++) {
                var b = bindings[i];
                var tweetEl = tweetElement(b);
                modaltweets.appendChild(tweetEl);
            }

            show_provenance();
            */
        }
    });
}