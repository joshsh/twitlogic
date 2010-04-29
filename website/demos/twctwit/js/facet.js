function tweets_query(graphs) {
    var q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
            "PREFIX sioct: <http://rdfs.org/sioc/types#>\n" +
            "PREFIX dc: <http://purl.org/dc/terms/>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT DISTINCT * WHERE {\n" +
            "  ?post rdf:type sioct:MicroblogPost .\n" +
            "  ?post sioc:content ?content .\n" +
            "  ?post sioc:has_creator ?user .\n" +
            "  ?user sioc:id ?screen_name .\n" +
            "  ?user sioc:account_of ?agent .\n" +
            "  ?agent foaf:depiction ?depiction .\n" +
            "  ?agent foaf:name ?name .\n" +
            "  ?post dc:created ?timestamp .\n" +
            "  OPTIONAL { ?post sioc:embeds_knowledge ?graph . }\n";
    for (var i = 0; i < graphs.length; i++) {
        if (i > 0) {
            q += "  UNION\n";
        }
        q += "  { ?post sioc:embeds_knowledge <" + graphs[i].value + "> . }\n";
    }
    //    "  FILTER ( ?timestamp > xsd:dateTime(\"" + MIN_TIMESTAMP_PLACEHOLDER + "\") )\n" +
    q += "}\n" +
         "ORDER BY DESC ( ?timestamp )";

    return q;
}

function sparql_query(query) {
    return "query=" + encodeURIComponent(query);
}

/*
 * Note: resources have the structure of SPARQL JSON results.
 */
function resources_equal(r1, r2) {
    //alert("" + r1 + ", " + r2 + " --> " + r1.value + ", " + r2.value + " --> " + (r1.value == r2.value));
    return (r1.value == r2.value);
}

function show_provenance() {
    var modaldiv = document.getElementById("modaldiv");
    modaldiv.style.visibility = "visible";
}

function hide_provenance() {
    var modaldiv = document.getElementById("modaldiv");
    modaldiv.style.visibility = "hidden";
}

function select_ohyeah_tweets(r) {
    $.ajax({
        type: "GET",
        url: "../../sparql", // SPARQL service URL
        //        data: "query=" + encodeURIComponent(RESOURCE_URI), // query parameter
        data: sparql_query(tweets_query(r.graphs)), // query parameter
        dataType: "json",
        success: function(data) {
            var modaltweets = document.getElementById("modaltweets");

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
        }
    });
}

/**
 * @author Shangguan
 */
/*
 * generate series of facets using SPARQL query
 */
function build_facets() {
    $.ajax({
        type: "GET",
        url: "../../sparql", // SPARQL service URL
        //        data: "query=" + encodeURIComponent(RESOURCE_URI), // query parameter
        data: "query=" + encodeURIComponent(
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "SELECT DISTINCT * WHERE {\n" +
                "  GRAPH ?g {\n" +
                "    <" + findTopic() + "> ?p ?o .\n" +
                "  }\n" +
                "  OPTIONAL { ?o rdfs:label ?olabel . } .\n" +
                "  OPTIONAL { ?p rdfs:label ?plabel . } .\n" +
                "  FILTER( ?p != sioc:topic )\n" +
                "}"
                ), // query parameter
        dataType: "json",
        success: function(data) {
            var t = _valueHash(data);
            $.each(t, function(key, value) {
                //var ln = _getLocalName(key);
                var ln = abbreviate(key);
                _buildFacet(ln, value, false);
            });

        }
    });
}

function build_inverse_facets() {
    $.ajax({
        type: "GET",
        url: "../../sparql", // SPARQL service URL
        //        data: "query=" + encodeURIComponent(RESOURCE_URI), // query parameter
        data: "query=" + encodeURIComponent(
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "SELECT DISTINCT * WHERE {\n" +
                "  GRAPH ?g {\n" +
                "    ?o ?p <" + findTopic() + "> .\n" +
                "  }\n" +
                "  OPTIONAL { ?o rdfs:label ?olabel . } .\n" +
                "  OPTIONAL { ?p rdfs:label ?plabel . } .\n" +
                "  FILTER( ?p != sioc:topic )\n" +
                "}"
                ), // query parameter
        dataType: "json",
        success: function(data) {
            var t = _valueHash(data);
            $.each(t, function(key, value) {
                //var ln = _getLocalName(key);
                var ln = abbreviate(key);
                _buildFacet(ln, value, true);
            });

        }
    });
}

/*
 * given a sparql $json result, generate {$key, $value} object
 * $key: returned predicate URI
 * $value: returned values for the predicate in $key
 */
function _valueHash(json) {
    var hash = {};
    var predicates = [];
    var bindings = json.results.bindings;
    for (var i = 0; i < bindings.length; i++) {
        var predicate = bindings[i].p.value;
        var object = bindings[i].o;

        var flag = $.inArray(predicate, predicates);
        var values;
        if (-1 == flag) {
            predicates.push(predicate);
            values = [];
            hash[predicate] = values;
            values.length = 0;
        } else {
            values = hash[predicate];
        }

        var distinct = true;
        for (var j = 0; j < values.length; j++) {
            if (resources_equal(object, values[j])) {
                distinct = false;
                object = values[j];
            }
        }

        if (distinct) {
            values.push(object);

            // Find a human-friendly label.
            if (null != bindings[i].olabel) {
                object.label = bindings[i].olabel.value;
            } else {
                object.label = abbreviate(object.value);
            }

            object.graphs = [];
        }

        var graph = bindings[i].g;
        object.graphs.push(graph);
    }

    return hash;
}

function topicTweetsURL(topic) {
    return "?topic=" + encodeURIComponent(topic);
}

function _popup_provenance() {
    var win = document.createElement("div");
    win.appendChild(document.createTextNode("text goes here"));
}

function _value_row_element(r) {
    var row = document.createElement("tr");

    // value
    var valueCell = document.createElement("td");
    row.appendChild(valueCell);
    valueCell.setAttribute("class", "value");
    var a = document.createElement("a");
    valueCell.appendChild(a);
    a.setAttribute("href", topicTweetsURL(r.value));
    a.appendChild(document.createTextNode(r.label));

    // Oh yeah?
    var ohyeahCell = document.createElement("td");
    row.appendChild(ohyeahCell);
    ohyeahCell.setAttribute("class", "ohyeah");
    var b = document.createElement("img");
    ohyeahCell.appendChild(b);
    b.setAttribute("src", "img/ohyeah_button.png");
    b.setAttribute("width", "63");
    b.setAttribute("height", "17");
    b.setAttribute("alt", "Oh yeah?");

    b.onclick = function() {
        //alert("resource: " + r.value);
        select_ohyeah_tweets(r);
    };

    //button.setAttribute("onclick", "alert(\"resource: \"");
    return row;
}

/*
 * given an object of {$key, $value}, generate corresponding facet
 */
function _buildFacet(key, value, inverse) {
    if (inverse) {
        key = "is " + key + " of";
    }

    //*
    var sidebar = document.getElementById("sidebar");
    var sidebar_content = document.createElement("div");
    sidebar.appendChild(sidebar_content);
    sidebar_content.setAttribute("class", "sidebar_content");

    var h4 = document.createElement("div");
    sidebar_content.appendChild(h4);
    h4.setAttribute("class", "facet_predicate");
    var openLink = document.createElement("a");
    h4.appendChild(openLink);
    openLink.setAttribute("href", "#zeroeth");
    openLink.appendChild(document.createTextNode(key));

    var all_values = document.createElement("all_values");
    sidebar_content.appendChild(all_values);

    // dynamic html
    var length = value.length;
    var minItems = 10;
    if (length <= minItems) {
        var table = document.createElement("table");
        all_values.appendChild(table);
        table.setAttribute("class", "some_values");
        $.each(value, function(count, item) {
            table.appendChild(_value_row_element((item)));
        });
    }
    else {
        var table1 = document.createElement("table");
        all_values.appendChild(table1);
        for (var i = 0; i < minItems; i += 1) {
            table1.appendChild(_value_row_element((value[i])));
        }
        var table2 = document.createElement("table");
        all_values.appendChild(table2);
        table2.setAttribute("class", "more_values");
        table2.setAttribute("style", "display: none;");
        for (var j = minItems; j < length; j += 1) {
            table.appendChild(_value_row_element((value[j])));
        }
    }

    var cl = document.createElement("div");
    all_values.appendChild(cl);
    cl.setAttribute("class", "cl");
    cl.appendChild(document.createTextNode("&nbsp;"));

    if (length > minItems) {
        var more_button = document.createElement("div");
        all_values.appendChild(more_button);
        more_button.setAttribute("class", "more_button");
        var a = document.createElement("a");
        more_button.appendChild(a);
        a.setAttribute("href", "#fourth");
        a.appendChild(document.createTextNode("More"));
    }

    var cl2 = document.createElement("div");
    all_values.appendChild(cl2);
    cl2.setAttribute("class", "cl");
    cl2.appendChild(document.createTextNode("&nbsp;"));
    //*/

    /*
     var textToInsert = "";

     //static html
     var sb_start = "<div class=\"sidebar_content\">";
     var div_end = "<\/div>";
     //    var h4_start = "<h4 class=\"open_button\"><a href=\"#zeroeth\">" + _capitalize(key) + "<\/a><\/h4>";
     var h4_start = "<h4 class=\"open_button\"><a href=\"#zeroeth\">" + key + "<\/a><\/h4>";
     var allval_start = "<div class=\"all_values\">";

     textToInsert += sb_start;
     textToInsert += h4_start;
     textToInsert += allval_start;

     // dynamic html
     var length = value.length;
     var minItems = 10;
     if (length <= minItems) {
     textToInsert += "<table class=\"some_values\">";
     $.each(value, function(count, item) {
     textToInsert += value_row(item);
     });
     textToInsert += "<\/table>";
     }
     else {
     textToInsert += "<table>";
     for (var i = 0; i < minItems; i += 1) {
     textToInsert += value_row(value[i]);
     }
     textToInsert += "<\/table>";
     textToInsert += "<table class=\"more_values\" style=\"display: none;\">";
     for (var j = minItems; j < length; j += 1) {
     textToInsert += value_row(value[j]);
     }
     textToInsert += "<\/table>";
     }
     textToInsert += "<div class=\"cl\">&nbsp;<\/div>";
     if (length > minItems) {
     textToInsert += "<div class=\"more_button\"><a href=\"#fourth\">More<\/a><\/div>";
     }
     textToInsert += div_end; // end <div class="all_values">
     textToInsert += "<div class=\"cl\">&nbsp;<\/div>";
     textToInsert += div_end; // end <div class="sidebar_content">

     $('#sidebar').append(textToInsert);
     //*/
}

/*
 * stripping out local name from a URI ($uri)
 */
function _getLocalName(uri) {
    var regex = /#(\w+)$/i;
    var localName = uri.split(regex);
    return localName[1];
}

function _capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

/*
 * toggle to show more/less values in a facet
 */
function _more() {
    $('.more_button a').live('click', function() {
        var more = $(this).parent().parent().find('ul.more_values:eq(0)');
        if (more.is(':hidden')) {
            more.slideDown(100);
            $(this).attr({
                'innerHTML': 'Less'
            });
        }
        else {
            more.slideUp(100);
            $(this).attr({
                'innerHTML': 'More'
            });
        }
        return false;
    });
}

/*
 * toggle collapse a facet
 */
function _collapse() {
    $('.open_button a').live('click', function() {
        var parent = $(this).parent();
        var all = parent.parent().find('div.all_values:eq(0)');
        if (all.is(':hidden')) {
            all.slideDown(100);
            parent.css('background', 'url(img/arrow-open.gif) no-repeat 0 4px');
        }
        else {
            all.slideUp(100);
            parent.css('background', 'url(img/arrow-closed.gif) no-repeat 0 4px');
        }
        return false;
    });
}

function init_facets() {
    $('#sidebar').html('');
    build_inverse_facets();
    build_facets();
    _more();
    _collapse();
}
