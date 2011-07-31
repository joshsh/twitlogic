<?php

function fix_utf8encoding($x){
  if(mb_detect_encoding($x)=='UTF-8'){
    return $x;
  }else{
    return utf8_encode($x);
  }
} 

function extract_url($value){
//print_r($value);		

	$ret = array();
	$ret[0]= false;
	
	if (preg_match("/^\s*<a +href=\"\s*([^\"]+)\s*\">\s*(ht|f)tps?[^<]+<\/a>\s*$/", $value, $matches)==1){
		$ret[0]=true;
		$ret[1]=$matches[1];
//print_r($matches);		
	}else if (preg_match_all('/\s*(h?[t|f]tps?:[^\s<>"\']+)/', $value, $matches)>0){
//print_r($matches);		
		if (strcmp($value, $matches[1][0])==0){
			$ret[0]=true;
			$ret[1]=$matches[1][0];
		}else{
			$ret[0]=false;
			for ($i=0; $i<sizeof($matches[1]); $i++){
				$ret[]=$matches[1][$i];
			}
			$ret = array_unique($ret);
		}
	};

	return $ret;
}

function get_array_value($array, $key){
	if (is_array($array) && array_key_exists($key, $array))
		return $array[$key];
	else
		return false;
}

function get_lastmodified($url){
	$headers = get_headers($url,1);
	if (empty($headers))
		return false;
		
	return get_xsddatetime($headers["Last-Modified"]);
}

function get_xsddatetime($date='', $bkeep=false){
	
	if (empty($date)){
		//$datetime_lastmodified = date_create();
		//$datetime_lastmodified = date_format($datetime_lastmodified, "Y-m-d\TH:i:s\Z");
		return false; //date("Y-m-d\TH:i:s\Z"); 
	}else{
//		date_default_timezone_set("GMT");
	
		$temp = date_parse($date);
		if (!empty($temp) && $temp['year']!==false && $temp['month']!==false && $temp['day']!==false){
			$ret = sprintf("%04d-%02d-%02d",$temp['year'],$temp['month'],$temp['day']);
			if ($temp['hour']!==false && $temp['minute']!==false && $temp['second']!==false){
				return $ret . sprintf("T%02d:%02d:%02dZ",$temp['hour'],$temp['minute'],$temp['second']);
			}else{
				return $ret;
			}
		}else{
			if ($bkeep)
				return $date;
			else
				return false;
		}
	}
}


function get_lastmodified2($url)
{ 
	$head = ""; 
	$url_p = parse_url($url); 
	$host = $url_p["host"]; 
	if(!preg_match("/[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*/",$host)){
		// a domain name was given, not an IP
		$ip=gethostbyname($host);
		if(!preg_match("/[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*/",$ip)){
			//domain could not be resolved
			return -1;
		}
	//$port = intval($url_p["port"]); 
	}
	$port=80;
	$path = $url_p["path"]; 
	//echo "Getting " . $host . ":" . $port . $path . " ...";

	$fp = fsockopen($host, $port, $errno, $errstr, 20); 
	if(!$fp) { 
		return false; 
		} else { 
		fputs($fp, "HEAD "  . $url  . " HTTP/1.1\r\n"); 
		fputs($fp, "HOST: " . $host . "\r\n"); 
		fputs($fp, "User-Agent: http://www.example.com/my_application\r\n");
		fputs($fp, "Connection: close\r\n\r\n"); 
		$headers = ""; 
		while (!feof($fp)) { 
			$headers .= fgets ($fp, 128); 
			} 
		} 
	fclose ($fp); 
	//echo $errno .": " . $errstr . "<br />";
	$return = -2; 
	$arr_headers = explode("\n", $headers); 
	// echo "HTTP headers for <a href='" . $url . "'>..." . substr($url,strlen($url)-20). "</a>:";
	// echo "<div class='http_headers'>";
	foreach($arr_headers as $header) { 
		// if (trim($header)) echo trim($header) . "<br />";
		$s1 = "HTTP/1.1"; 
		$s2 = "Last-Modified:"; 
		if(substr(strtolower ($header), 0, strlen($s1)) == strtolower($s1)) $status = substr($header, strlen($s1)); 
		if(substr(strtolower ($header), 0, strlen($s2)) == strtolower($s2)) $date   = substr($header, strlen($s2));  
		//if(substr(strtolower ($header), 0, strlen($s3)) == strtolower($s3)) $newurl = substr($header, strlen($s3));  
		} 
	
	return $date; 
} 


//take a url and a array, ping it and update array, return array
function ping_url($url)
{ 
	
	//predicate that will have our url
	//$d_url= NS_DGP .'Dataset_url';
	$ret = array();

echo "\n pinging... $url\n";		

	// check url
	$url_info = parse_url($url);
	if ($url_info!==false && ( strncmp($url_info['scheme'],'http',4)==0 )){
		$head = get_headers($url, 1);
		if (empty($head) || empty($head['0'])){
			$ret["status"] = "server offline";
			return $ret;
		}
	}else{
		$ret["status"] = "not-http";
		return $ret;
	}

	//connected	
	$temp = get_array_value( $head, "0");
	if(!preg_match("/200/",$head['0']))
	{
		$ret["status"] = $head['0'];// file size
		return $ret;
	}

	$ret["status"] = 'alive';
	
	$temp = get_array_value( $head, "Last-Modified");
	if(strlen($temp)>0)//connected and have a date
	{
		$ret["modified"] = $temp;//store date for real
	}/*else{
		$temp = get_array_value( $head, "Date");
		if(strlen($temp)>0)//connected and have a date
			$ret["modified"] = $temp;//store date for real
	
	}*/
	
	$temp = get_array_value( $head, "Content-Length");
	if(strlen($temp)>0)
	{
		$ret["bytes"] = $temp;// file size
	}
	return $ret;
	
}

function download($url_input, $output_file){
	$src = fopen($url_input, 'r');
	$dest = fopen($output_file, 'w');
	echo stream_copy_to_stream($src, $dest) . " bytes copied to $output_file from $input_url\n";
	fclose($src);
	fclose($dest);
}

function download_many($url_input, $file_outputs){
	$sz_temp = file_get_contents($url_input);
	echo "accessing $url_input \n";
	foreach ($file_outputs as $file_output){
		echo file_put_contents($file_output, $sz_temp) . " bytes copied to $file_output \n";
	}
}


?>