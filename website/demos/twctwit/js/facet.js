
/**
 * @author Shangguan
 */
/*
 * generate series of facets using SPARQL query
 */
function build_facets(){
    $.ajax({
        type: "GET",
        url: "../../sparql", // SPARQL service URL
//        data: "query=" + encodeURIComponent(RESOURCE_URI), // query parameter
        data: "query=" + encodeURIComponent(
                "SELECT * WHERE { <" + findTopic() + "> ?p ?o . }"
                ), // query parameter
        dataType: "json",
        success: function(data){
            var t = _valueHash(data);
            $.each(t, function(key, value){
                //var ln = _getLocalName(key);
                var ln = abbreviate(key);
                _buildFacet(ln, value);
            });
            
        }
    });
}

/*
 * given a sparql $json result, generate {$key, $value} object
 * $key: returned predicate URI
 * $value: returned values for the predicate in $key
 */
function _valueHash(json){
    var hash = {};
    var predicates = [];
    var bindings = json.results.bindings;
    for (var i = 0; i < bindings.length; i++) {
        var predicate = bindings[i].p.value;
        var object = bindings[i].o.value;
        var flag = $.inArray(predicate, predicates);
        if (flag > -1) {
            var oldValues = hash[predicate];
            oldValues.push(object);
            hash[predicate] = oldValues;
        }
        else {
            predicates.push(predicate);
            var values = [];
            values.push(object);
            hash[predicate] = values;
        }
    }
    return hash;
}

function topicTweetsURL(topic) {
    return "?topic=" + encodeURIComponent(topic);
}

/*
 * given an object of {$key, $value}, generate corresponding facet
 */
function _buildFacet(key, value){

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
        textToInsert += "<ul>";
        $.each(value, function(count, item){
            textToInsert += "<li><a href=\"" + topicTweetsURL(item) + "\">" + abbreviate(item) + "<\/a><\/li>";
        });
        textToInsert += "<\/ul>";
    }
    else {
        textToInsert += "<ul>";
        for (var i = 0; i < minItems; i += 1) {
            textToInsert += "<li><a href=\"" + topicTweetsURL(value[i]) + "\">" + abbreviate(value[i]) + "<\/a><\/li>";
        }
        textToInsert += "<\/ul>";
        textToInsert += "<ul class=\"more_values\" style=\"display: none;\">";
        for (var j = minItems; j < length; j += 1) {
            textToInsert += "<li><a href=\"" + topicTweetsURL(value[j]) + "\">" + abbreviate(value[j]) + "<\/a><\/li>";
        }
        textToInsert += "<\/ul>";
    }
    textToInsert += "<div class=\"cl\">&nbsp;<\/div>";
    textToInsert += "<div class=\"more_button\"><a href=\"#fourth\">More<\/a><\/div>";
    textToInsert += div_end; // end <div class="all_values">
    textToInsert += "<div class=\"cl\">&nbsp;<\/div>";
    textToInsert += div_end; // end <div class="sidebar_content">
    
    $('#sidebar').append(textToInsert);
}

/*
 * stripping out local name from a URI ($uri)
 */
function _getLocalName(uri){
    var regex = /#(\w+)$/i;
    var localName = uri.split(regex);
    return localName[1];
}

function _capitalize(string){
    return string.charAt(0).toUpperCase() + string.slice(1);
}

/*
 * toggle to show more/less values in a facet
 */
function _more(){
    $('.more_button a').live('click', function(){
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
function _collapse(){
    $('.open_button a').live('click', function(){
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

function init_facets(){
	$('#sidebar').html('');
    build_facets();
    _more();
    _collapse();
}
