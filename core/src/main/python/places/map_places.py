import json
import urllib2

def wikipedia_name(name):
	w = name.replace(' ', '_')
	return w
	
f = open('/tmp/new_places-json.txt', 'r')

for line in f:
	try:
		o = json.loads(line)
	except:
		# TODO: output an error message
		continue
		
	place_type = o['place_type']
	name = o['name']
	full_name = o['full_name']
	id = o['id']
	country_code = o['country_code']
	
	#if 'US' != country_code:
	#	print country_code
		
	q = 'http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=' + urllib2.quote(name)
		
	response = urllib2.urlopen(q)
	text = response.read()
		
	w = json.loads(text)
	a = w[1]
	
	for p in a:
		wn = wikipedia_name(p)
		
		st = '<http://twitlogic.fortytwo.net/location/twitter/' + id + '> owl:sameAs <' + wn + "> . # " + place_type
		
		wu = 'http://en.wikipedia.org/wiki/' + wn
		out = id + '\t' + place_type + '\t' + full_name + '\t' + p + '\t' + wu + '\t' + st
		try:
			print out
		except:
			# TODO: output an error message
			continue


