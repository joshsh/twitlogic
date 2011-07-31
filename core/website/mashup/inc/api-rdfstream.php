<?php
/*
// sample test code - uncomment the following code to see the demo

// load CSV with header
$url = "http://tw.rpi.edu/ws/example/ex1.csv";



$handle = fopen($url, "r");

$props = fgetcsv($handle);

// generate RDF
$rdf = new RdfStream();
$rdf->output=RdfStream::RDF_SYNTAX_NT;


$map_prefix_ns = array();
$map_prefix_ns ["foaf"] ="http://xmlns.com/foaf/0.1/";
$map_prefix_ns ["rdfs"] ="http://www.w3.org/2000/01/rdf-schema#";
$map_prefix_ns ["dc"] ="http://purl.org/dc/elements/1.1/";
$map_prefix_ns ["rss"] ="http://purl.org/rss/1.0/";
$map_prefix_ns ["dgtwc"] ="http://data-gov.tw.rpi.edu/2009/data-gov-twc.rdf#";


$rdf->begin($map_prefix_ns,  "http://example.org/test.rdf", "http://example.org/prop-");

// content
while (($values = fgetcsv($handle)) !== FALSE) {
	$rdf->add_row_pair($props, $values);
}

$rdf->add_triple("","dc:source", $url);
$rdf->add_triple("","dgtwc:number_of_triples", $rdf->number_of_triples);


//footer
$rdf->end();

*/


class RdfStream
{
    const RDF_URI = 1;
    const RDF_STRING = 2;
    const RDF_DATE = 3;
    const RDF_DATETIME = 4;
    const RDF_INTEGER = 5;
    const RDF_SYNTAX_RDFXML = 1;
    const RDF_SYNTAX_NT = 2;

	var $number_of_triples = 0;
	var $rowid = 1;
	var $output = RdfStream::RDF_SYNTAX_RDFXML;
	var $uri_prev_subject = null;
	
	var $xmlbase =null;
	var $propns =null;
   	var $map_prefix_ns =null;
	
	

    public function add_row_pair($row_keys, $row_values, $key_subject=null, $normalize=false){
		$this->add_row( array_combine($row_keys, $row_values), $key_subject , $normalize);
	}
    public function add_row($row, $key_subject=null, $normalize=false){
		if (!empty($key_subject) && array_key_exists($key_subject, $row)){
			$subject = $row[$key_subject];
		}
		
		if ( !RdfStream::is_uri($subject) ){
			$subject = $this->create_subject();
		}

		foreach($row as $property=>$value){
			if (!empty($key_subject) && strcmp($property,$key_subject)===0){
				continue;
			}
			if ($normalize){
				$property = RdfStream::normalize_localname($property);
			}
			if ($this->output === RdfStream::RDF_SYNTAX_NT && !strpos($property, ":")){
				$property = $this->propns . $property;
			}

			
			if (is_array($value)){
				//a list of values
				foreach($value as $object){
					  $this->add_triple($subject, $property, $object);			
				}
			}else{
				$object =$value;
				$this->add_triple($subject, $property, $object);
			}
		}
	}
	
	private function create_subject(){
		$uri = sprintf("#entry%05d", $this->rowid);
		$this->rowid++;	
		
		return $uri;
	}
	
 	static public function normalize_localname($value){
		$temp = $value;
		$temp = str_replace(' ', '_', trim(preg_replace('/\W+/',' ', $temp )));
		$temp = strtolower($temp);
		if (is_numeric($temp)){
			$temp = "num".$temp;
		}
		return $temp;
	}
		
    public function add_triple($s, $p, $o, $o_type=null){
		//skip triple with empty subject, predicate or object
		if (!isset($s)|| !isset($p)|| !isset($o))
			return;
	
		$this->number_of_triples++;
		
		//detect object type in case it was not provided
		if (empty($o_type)){
			if (RdfStream::is_uri($o)){
				$o_type=RdfStream::RDF_URI;
			}else if (is_int($o)){
				$o_type=RdfStream::RDF_INTEGER;
			}else if (stristr($p, "date")>=0 && $date=RdfStream::get_xsddatetime($o)){
				if (strlen($date)>10)
					$o_type=RdfStream::RDF_DATETIME;
				else
					$o_type=RdfStream::RDF_DATE;
				$o=$date;
			}else{
				$o_type=RdfStream::RDF_STRING;
			}
		}
		
		switch($this->output){
			case RdfStream::RDF_SYNTAX_NT: 
				$this->nt_add_triple($s,$p,$o,$o_type);
				break;
			case RdfStream::RDF_SYNTAX_RDFXML: 
			default:
				$this->rdfxml_add_triple($s,$p,$o,$o_type);
				break;
		}
	}
	
	private function println($str, $encode=false){
		if ($encode)
			$str =utf8_encode($str);
		echo $str . "\n";
	}

	private function rdfxml_add_triple($s, $p, $o, $o_type){
		//add rdf:description
		$this->rdfxml_desc_begin($s);
		
		//add attributes about the resource
		switch($o_type){
			case RdfStream::RDF_URI: 
				$this->println( "  <$p rdf:resource=\"$o\"/>");
				break;
			case RdfStream::RDF_INTEGER:
				$this->println( "  <$p rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">$o</$p>");
				break;
			case RdfStream::RDF_DATE:
				$this->println( "  <$p rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">$o</$p>");
				break;
			case RdfStream::RDF_DATETIME:
				$this->println( "  <$p rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">$o</$p>");
				break;
			case RdfStream::RDF_STRING:
			default:
				$this->println( "  <$p><![CDATA[$o]]></$p>");
		}
	}
	
	private function rdfxml_desc_begin($s){
		if (strcmp($s,$this->uri_prev_subject)!==0){
			$this->rdfxml_desc_end();
			$this->println( "<rdf:Description rdf:about=\"$s\">");
			$this->uri_prev_subject = $s;
		}
	}
	private function rdfxml_desc_end(){
		if (isset($this->uri_prev_subject))
			$this->println( "</rdf:Description>");	
	}
	

	public function begin($map_prefix_ns, $xmlbase=null, $propns=null){
		$this->xmlbase = $xmlbase;
		$this->propns = $propns;

		switch($this->output){
			case RdfStream::RDF_SYNTAX_NT: 
				header ("Content-Type: text/plain");
				foreach($map_prefix_ns as $prefix =>$ns){
					if (strlen($prefix)>0){
						$this->map_prefix_ns["/^".$prefix.":/"] = $ns;
					}
				}
				break;
			case RdfStream::RDF_SYNTAX_RDFXML: 
			default:
				header ("Content-Type: application/rdf+xml");

				$this->println( "<?xml version=\"1.0\" ?>");
				$this->println( "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ");
				
				if (!empty($this->xmlbase))
					$this->println( "  xml:base=\"$xmlbase\"");
					
				if (!empty($this->propns))
					$map_prefix_ns[""] =$this->propns;

				foreach($map_prefix_ns as $prefix =>$ns){
	
					if (strlen($prefix)>0){
						$this->println( "  xmlns:$prefix = \"$ns\" ");
					}else
						$this->println( "  xmlns = \"$ns\" ");
				}
				$this->println( ">");
				break;
		}
	}
	
	public function end(){
		switch($this->output){
			case RdfStream::RDF_SYNTAX_NT: 
				break;
			case RdfStream::RDF_SYNTAX_RDFXML: 
			default:
				$this->rdfxml_desc_end();
				$this->println( "</rdf:RDF>");		
		}
	}
	
	
	private function nt_add_triple($s,$p,$o,$o_type){
		if ( strncmp($s,"#",1)===0 || strlen($s)===0){
			$s = $this->xmlbase . $s;
		}

			if (!empty($this->map_prefix_ns) && strncmp($p, "http://",7)!==0){
				$p= preg_replace(array_keys($this->map_prefix_ns),array_values($this->map_prefix_ns), $p );
			}

		$ret = RdfStream::nt_resource($s) . " " .RdfStream::nt_resource($p) ." ";
		switch ($o_type){
		case RdfStream::RDF_STRING:
			$ret.= RdfStream::nt_literal($o) ;
			break;
		case RdfStream::RDF_DATE:
			$ret.= RdfStream::nt_literal($o, "http://www.w3.org/2001/XMLSchema#date") ;
			break;
		case RdfStream::RDF_DATETIME:
			$ret.= RdfStream::nt_literal($o, "http://www.w3.org/2001/XMLSchema#dateTime") ;
			break;
		case RdfStream::RDF_URI:
		default:
			$ret.= RdfStream::nt_resource($o);
			break;
		}
		
		$this->println( $ret." .");
	}		
	
	
	static public function nt_resource($node){
		return "<".$node.">";
	}

	static public function nt_literal($node, $xmltype=null){
		$node = str_replace('"', '\"', $node);
		$node ="\"".$node."\"";
		if (!empty($xmltype)){
			$node .="^^<".$xmltype.">";
		}
		return $node;
	}


  public static function is_uri($uri){
	if (empty($uri))
		return false;
    return preg_match ('/^https?:[^\s<>"\',]+$/', $uri);
  }

  public static function get_xsddatetime($date='', $bkeep=false){
	
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
}
?>