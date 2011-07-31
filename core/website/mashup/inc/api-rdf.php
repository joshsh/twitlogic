<?php


define ("RDF_URI",1);
define ("RDF_LITERAL",2);
define ("RDF_BLANK",3);
define ("RDF_SYNTAX_RDFXML",1);
define ("RDF_SYNTAX_NT",2);

require_once( dirname(__FILE__). "/api-web.php");

class RdfStatement{
	var $s;
	var $p;
	var $o;
	var $o_type;
	var $s_type;
	
	public function RdfStatement($s, $p, $o, $o_type=RDF_URI, $s_type=RDF_URI){
		$this->s=$s;
		$this->p=$p;
		$this->o=$o;
		$this->o_type=$o_type;
		$this->s_type=$s_type;
	}
	
}

class RdfNtStream{
	var $number_of_triples=0;

	public function addStatement($stmt){
		$this->number_of_triples ++;
		$ret = RdfNtStream::nt_resource($stmt->s) . " " .RdfNtStream::nt_resource($stmt->p) ." ";
		switch ($stmt->o_type){
		case RDF_LITERAL:
			$ret.= RdfNtStream::nt_literal($stmt->o) ;
			break;
		case RDF_BLANK:
		case RDF_URI:
		default:
			$ret.= RdfNtStream::nt_resource($stmt->o);
			break;
		}
		
		echo $ret." .\n";
		return $ret;
	}	
	
	public static function nt_resource($node){
		return "<".$node.">";
	}

	public static function nt_literal($node){
		$node = str_replace('"', '\"', $node);
		return "\"".$node."\"";
	}
	
	public function getContentType(){
		return 'text/plain';
	}
	
	public function getNumberOfTriples(){
		return $this->number_of_triples;
	}	
}



class RdfGraph{

	var $syntax;
	var $map_uri_prefix = array();
	var $map_xmlns_prefix_uri = array();
	var $ary_stmt = array();
	var $xmlbase = "";
	var $number_of_triples=0;
	
	/**
	 * Constructor
	 *
	 * Some basic initialisation
	 */

	public function RdfGraph($syntax=RDF_SYNTAX_RDFXML) {
		$this->syntax= $syntax;
		
	}

	public function addPrefixUriMapping($prefix, $ns_uri){
		if (empty($prefix)){
			$this->map_uri_prefix[$ns_uri] = "";
			$this->map_xmlns_prefix_uri["xmlns"] = $ns_uri;
		}else{
			$this->map_uri_prefix[$ns_uri] = $prefix. ":";
			$this->map_xmlns_prefix_uri["xmlns:" .$prefix] = $ns_uri;
		}
	}
	public function setXmlBase($url){
		$this->xmlbase = $url;
	}
	
	public function addStatement($stmt){
		$this->ary_stmt[$stmt->s][]=$stmt;
		$this->number_of_triples ++;
	}
	
	public function load($url){
		//only load RDF/XML simple file
		$dom = new DomDocument();
		$dom->load($url);
		
		//parse prefix_uri mapping
		
		//parse statements
		$descriptions = $dom->documentElement->getElementsByTagName('rdf:Description');
		echo '<pre>'.dom_dump($descriptions).'<pre>';
	}
	
	function _getContent_rdfxml(){
		if (RDF_SYNTAX_RDFXML!=$this->syntax)
			return '';
			
		
		$memory = xmlwriter_open_memory();
		xmlwriter_set_indent( $memory,true); 
		
		xmlwriter_start_document($memory,'1.0','UTF-8');
		xmlwriter_start_element ($memory,'rdf:RDF'); 
		foreach ($this->map_xmlns_prefix_uri as $xmlns_prefix=>$uri){
			xmlwriter_write_attribute( $memory, $xmlns_prefix, $uri);
		}
		if (!empty($this->xmlbase))
			xmlwriter_write_attribute( $memory, "xml:base", $this->xmlbase);
		
		foreach ($this->ary_stmt as $subject=>$stmts){
			xmlwriter_start_element($memory,'rdf:Description'); // <rdf:Description>
			switch ($stmts[0]->s_type){
			case RDF_URI:
				xmlwriter_write_attribute( $memory, 'rdf:about', $subject );
				break;
			case RDF_BLANK:
				xmlwriter_write_attribute( $memory, 'rdf:nodeID', $subject );
				break;
			default:
				echo "ERROR";
			}
			
			foreach ($stmts as $stmt){
				switch ($stmt->o_type){
				case RDF_URI:
					xmlwriter_start_element($memory, $stmt->p); //
					xmlwriter_write_attribute( $memory, 'rdf:resource', $stmt->o );
					xmlwriter_end_element($memory); // 				
					break;
				case RDF_LITERAL:
					xmlwriter_write_element ($memory,  $stmt->p, $stmt->o); 
					break;
				case RDF_BLANK:
					xmlwriter_start_element($memory, $stmt->p); //
					xmlwriter_write_attribute( $memory, 'rdf:nodeID', $stmt->o );
					xmlwriter_end_element($memory); // 				
					break;
				}
			}
			
			xmlwriter_end_element($memory); // 
		}
		
		xmlwriter_end_element($memory); // </rdf:RDF>
		
		$xml = xmlwriter_output_memory($memory,true);
		$xml = $this->pretty_print($xml);
		return $xml;
	}

	function getContent_rss_1_0($items, $channel_title, $channel_description='', $channel_url='' ){
		if (RDF_SYNTAX_RDFXML!=$this->syntax)
			return '';
			
		
		
		$memory = xmlwriter_open_memory();
		xmlwriter_set_indent( $memory,true); 
		
		xmlwriter_start_document($memory,'1.0','UTF-8');
		xmlwriter_start_element ($memory,'rdf:RDF'); 
		
		//default namespace to rss
		$this->addPrefixUriMapping("dc", NS_DC);
		$this->addPrefixUriMapping("", NS_RSS);
		foreach ($this->map_xmlns_prefix_uri as $xmlns_prefix =>$uri){
			xmlwriter_write_attribute( $memory, $xmlns_prefix, $uri);
		}
		

		//print channel
		xmlwriter_start_element($memory,'channel'); // <rdf:channel>
		xmlwriter_write_attribute( $memory, 'rdf:about', $channel_url );
		xmlwriter_write_element ($memory,  'dc:date', date("Y-m-d\TH:i:s\Z") ); 
		if (!empty($channel_title))
			xmlwriter_write_element ($memory,  'title', $channel_title); 
		if (!empty($channel_description))
			xmlwriter_write_element ($memory,  'description', $channel_description); 
		if (!empty($channel_url)){
			xmlwriter_start_element($memory,'link'); // <rdf:channel>
			xmlwriter_write_attribute( $memory, 'rdf:resource', $channel_url );
			xmlwriter_end_element($memory); // </rdf:channel>
		}
		
		//we skipped the sequence stuff
		xmlwriter_end_element($memory); // </rdf:channel>

		//ksort ($this->ary_stmt);
		//print entries
		$pattern = array_keys($this->map_uri_prefix);
		$replacement  = array_values($this->map_uri_prefix);
		foreach ($this->ary_stmt as $subject=>$stmts){
			if (in_array($subject, $items)){
				//echo "\n\n Item \n";
				xmlwriter_start_element($memory,'item'); // <rdf:item>
				switch ($stmts[0]->s_type){
				case RDF_URI:
					xmlwriter_write_attribute( $memory, 'rdf:about', $subject );
					break;
				default:
					continue; 
				}
			}else{
				//echo "\n\n Description \n";
				xmlwriter_start_element($memory,'rdf:Description'); // <rdf:Description>
				switch ($stmts[0]->s_type){
				case RDF_URI:
					xmlwriter_write_attribute( $memory, 'rdf:about', $subject );
					break;
				case RDF_BLANK:
					xmlwriter_write_attribute( $memory, 'rdf:nodeID', $subject );
					break;
				default:
					echo "ERROR";
				}
			}


			
			foreach ($stmts as $stmt){
				$property = str_replace($pattern, $replacement, $stmt->p);
				//echo "\n\nproperty is " . $property . "\n";
				//echo "\n\nproperty is " . $stmt . "\n";
				//echo "o is " . $stmt->o . "\n\n";
				switch ($stmt->o_type){
				case RDF_URI:
					xmlwriter_start_element($memory, $property); //
					xmlwriter_write_attribute( $memory, 'rdf:resource', $stmt->o );
					xmlwriter_end_element($memory); // 				
					break;
				case RDF_LITERAL:
					xmlwriter_write_element ($memory,  $property, $stmt->o); 
					break;
				case RDF_BLANK:
					xmlwriter_start_element($memory, $property); //
					xmlwriter_write_attribute( $memory, 'rdf:nodeID', $stmt->o );
					xmlwriter_end_element($memory); // 				
					break;
				}
			}
			
			xmlwriter_end_element($memory); // <rdf:item> 
		}
		
		xmlwriter_end_element($memory); // </rdf:RDF>
		
		$xml = xmlwriter_output_memory($memory,true);
		$xml = $this->pretty_print($xml);
		return $xml;
	}
	
	private function pretty_print($xml){
		$xml = str_replace("xmlns:","\n    xmlns:",$xml);
		$xml = str_replace("xmlns=","\n    xmlns=",$xml);
		$xml = str_replace("xml:base=","\n    xml:base=",$xml);
		
		$xml = fix_utf8encoding($xml);
		return $xml;
	}
	
	public function getContent(){
		switch ($this->syntax){
		case RDF_SYNTAX_RDFXML: 
			return $this->_getContent_rdfxml();
		}
	}
	
	
	public function getContentType(){
		switch ($this->syntax){
		case RDF_SYNTAX_RDFXML: 
			return 'application/rdf+xml';
		}
	}

	public function getContentEncoding(){
		switch ($this->syntax){
		case RDF_SYNTAX_RDFXML: 
			return 'UTF-8';
		}
	}
	
	public function getNumberOfTriples(){
		return $this->number_of_triples;
	}
}

?>