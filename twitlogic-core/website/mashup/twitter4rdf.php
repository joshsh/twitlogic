<?php
define("URL_TWITTER4RDF_USER", "http://tw.rpi.edu/ws/twitter4rdf.php?twitterid=");
define("URL_TWITTER4RDF_HASHTAG", "http://tw.rpi.edu/ws/twitter4rdf.php?hashtag=");
define("URL_TWITTER4RDF_SEARCH", "http://tw.rpi.edu/ws/twitter4rdf.php?search=");
define("URL_TWITTER", "http://twitter.com/");
define("URL_TWITTER_SEARCH", "http://search.twitter.com/search.json");
define("URL_TWITTER_USER", "http://twitter.com/statuses/user_timeline.json");
define("URL_TWITTER_STATUS_PATTERN", "http://twitter.com/statuses/show/%s.xml");
define("URL_TWITTERLOGIC_HASHTAG", "http://twitlogic.fortytwo.net/resource/hashtag/");

require_once( dirname(__FILE__). "/inc/api-rdfstream.php");


//compose twitter url
$url = $_GET["url"];
$twitterid = $_GET["twitterid"];
$q= $_GET["q"];
if (strlen($q)==0)
  $q= "%23".$_GET["hashtag"];

$pagelimit= $_GET["pagelimit"];
$redirect= !empty($_GET["redirect"]);


if (!empty($twitterid) && empty($url)){
	$params["screen_name"]= $twitterid;
	  $url = URL_TWITTER_USER."?";

  $params = array();
  foreach($params as $key=>$value)
	$url = $url . "$key=$value&";

  //load data
  $data = file_get_contents($url);
  $data = json_decode($data);
  $items = $data;
  
}else if (!empty($q) && empty($url)){

  //load data
  for ($page=1; $page<=min(15,$pagelimit); $page++){
   $params = array();
   $params["q"]= $q;
   if (!empty($page))
 	  $params["page"]= $page;
   $params["rpp"]= 100;

   $url = URL_TWITTER_SEARCH."?";
   foreach($params as $key=>$value)
	$url = $url . "$key=$value&";

    $data = file_get_contents($url);
    $data = json_decode($data);
    $data = $data->results;
    if (sizeof($data)==0)
      break;
    foreach ($data as $item){
       $items[]=$item;
    }
  }

}else{
  die();
}



if (empty($url)&&empty($twitterid)&&empty($hashtag)&&empty($search)){
?>
<fieldset>
<legend>by Twitter ID</legend>
<form Method="GET" Action="twitter4rdf.php" >
 twitter id:<input name="twitterid" size="50" /> <br /> e.g. jahendler, lidingpku <br />
 <input value="query" type="submit" /><br />
</form>
</fieldset>

<fieldset>
<legend>by Twitter Hashtag</legend>
<form Method="GET" Action="twitter4rdf.php" >
 twitter hashtag:<input name="hashtag" size="50" /> e.g. datagov <br />
 max number of results to be retured: <select name="pagelimit">
  <option value="1">100
  <option value="2">200
  <option value="3">300
  <option value="4">400
  <option value="5">500
  <option value="6">600
  <option value="7">700
  <option value="8">800
  <option value="9">900
  <option value="10">1000
 </select><br/>
 <input value="query" type="submit" /><br />
</form>
</fieldset>

<?php
exit();
}


$prop_ns ="http://data-gov.tw.rpi.edu/vocab/p/";
$xmlbase=null;



//print_r($items);

// header
$map_prefix_ns = array();
$map_prefix_ns [""]= $prop_ns; 
$map_prefix_ns ["foaf"] ="http://xmlns.com/foaf/0.1/";
$map_prefix_ns ["rdfs"] ="http://www.w3.org/2000/01/rdf-schema#";
$map_prefix_ns ["dc"] ="http://purl.org/dc/elements/1.1/";
$map_prefix_ns ["rss"] ="http://purl.org/rss/1.0/";

$rdf = new RdfStream();
$rdf->begin($map_prefix_ns, $xmlbase, $prop_ns);

// content
$rowid=1;
foreach ($items as $item)
{
//print_r($item);
  $row = Twitter_Parser::process_json_item($item, true);
  $rdf->add_row($row,"rss:link");
}


//$rdf->add_triple("","dc:source", $url);

//footer
$rdf->end();




class Twitter_Parser{
  static function get_rss_link($username){
	$url = "http://twitter.com/$username";
	$content = file_get_contents($url);
       if (preg_match_all ("/https?:[^\s<>\"',]+/", $content, $matches)){
          foreach($matches[0] as $match){
            $rev = strrev($match);
	     if (strncmp("ssr.",$rev,4)===0)
		return $match;
          }
       }
       return false;
  }

 
  static function assign_value($array, $property, $value){
	if (empty($value) || strcmp($value,"null")==0)
		return;
	$array[$property] = $value;
  } 
 
  static function process_json_item($item, $redirect){
    $entry = array();

    $temp =$item->text;
    if (strlen($temp)>0){
       $entry ["dc:title"] = $temp;   
    }


    $temp =$item->id;
    if (strlen($temp)>0){
       $entry ["dc:identifier"] = $temp;   
       $entry ["rss:link"] = sprintf(URL_TWITTER_STATUS_PATTERN, $temp);   
    }

    $temp =$item->created_at;
    if (strlen($temp)>0){
       $entry ["dc:created"] = $temp;   
    }


    $temp =$item->from_user;
    if (strlen($temp)>0){
//       $entry ["dc:creator"][] = $temp;   
       $entry ["dc:creator"][] = URL_TWITTER. $temp;   
       $entry ["rdfs:seeAlso"][] = URL_TWITTER4RDF_USER . $temp;   
    }

    $temp =$item->user->screen_name;
    if (strlen($temp)>0){
//       $entry ["dc:creator"][] = $temp;   
       $entry ["dc:creator"][] = URL_TWITTER. $temp;   
       $entry ["rdfs:seeAlso"][] = URL_TWITTER4RDF_USER . $temp;   
    }


    $temp =$item->to_user;
    if (strlen($temp)>0){
//       $entry ["mentioned"][] = $temp;   
       $entry ["mentioned"][] = URL_TWITTER.$temp;
       $entry ["rdfs:seeAlso"][] = URL_TWITTER4RDF_USER .$temp;
    }

    $temp =$item->in_reply_to_screen_name;
    if (strlen($temp)>0){
//       $entry ["mentioned"][] = $temp;   
       $entry ["mentioned"][] = URL_TWITTER.$temp;
       $entry ["rdfs:seeAlso"][] = URL_TWITTER4RDF_USER .$temp;
    }

//print_r($item);
//print_r($entry );

    $twitter = Twitter_Parser::parse_message($entry["dc:title"], $redirect);

    $row = array_merge($entry,$twitter);

    return $row;

  }

  static function parse_message($text, $redirect=false){
   if (empty($text))
	return;


   $twitter = array();

   //extract links
   if (preg_match_all ('/https?:[^\s<>"\',]+/', $text, $matches)){
     foreach($matches as $match){
       $temp = $match[0];
       $twitter["url_raw"][] =  $temp;
	if ($redirect){
  	   if (strlen( $temp )<30){
		$temp1 = get_redirect_url( $temp );
		if ($temp1)
		       $twitter["url"][] = $temp1;
		else
		       $twitter["url"][] = $temp;
	   }
        } 
       if ( $follow_redirect)
          $twitter["final_url"][] = get_final_url( $temp );
     }
    // print_r($matches);
   }

   //expand links

   //extract hashtags
   if (preg_match_all ('/#[A-Za-z0-9-_]+/', $text, $matches)){
     foreach($matches[0] as $match){
	$temp = substr($match,1);

       $twitter["dc:subject"][] = $temp;
//       $twitter["dc:subject"][] = URL_TWITTER_HASHTAG. $temp;
       $twitter["rdfs:seeAlso"][] = URL_TWITTER4RDF_HASHTAG . $temp;
	$twitter["dc:relation"][] = URL_TWITTERLOGIC_HASHTAG . strtolower($temp);
     }
 //    print_r($matches);
   }

    return $twitter;
  }
}





/**
 * get_redirect_url()
 * Gets the address that the provided URL redirects to,
 * or FALSE if there's no redirect. 
 *
 * @param string $url
 * @return string
 */
function get_redirect_url($url){
	$redirect_url = null; 
 
	$url_parts = @parse_url($url);
	if (!$url_parts) return false;
	if (!isset($url_parts['host'])) return false; //can't process relative URLs
	if (!isset($url_parts['path'])) $url_parts['path'] = '/';
 
	$sock = fsockopen($url_parts['host'], (isset($url_parts['port']) ? (int)$url_parts['port'] : 80), $errno, $errstr, 30);
	if (!$sock) return false;
 
	$request = "HEAD " . $url_parts['path'] . (isset($url_parts['query']) ? '?'.$url_parts['query'] : '') . " HTTP/1.1\r\n"; 
	$request .= 'Host: ' . $url_parts['host'] . "\r\n"; 
	$request .= "Connection: Close\r\n\r\n"; 
	fwrite($sock, $request);
	$response = '';
	while(!feof($sock)) $response .= fread($sock, 8192);
	fclose($sock);
 
	if (preg_match('/^Location: (.+?)$/m', $response, $matches)){
		if ( substr($matches[1], 0, 1) == "/" )
			return $url_parts['scheme'] . "://" . $url_parts['host'] . trim($matches[1]);
		else
			return trim($matches[1]);
 
	} else {
		return false;
	}
 
}
 
/**
 * get_all_redirects()
 * Follows and collects all redirects, in order, for the given URL. 
 *
 * @param string $url
 * @return array
 */
function get_all_redirects($url){
	$redirects = array();
	while ($newurl = get_redirect_url($url)){
		if (in_array($newurl, $redirects)){
			break;
		}
		$redirects[] = $newurl;
		$url = $newurl;
	}
	return $redirects;
}
 
/**
 * get_final_url()
 * Gets the address that the URL ultimately leads to. 
 * Returns $url itself if it isn't a redirect.
 *
 * @param string $url
 * @return string
 */
function get_final_url($url){
	$redirects = get_all_redirects($url);
	if (count($redirects)>0){
		return array_pop($redirects);
	} else {
		return $url;
	}
}
?>