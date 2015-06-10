from contextlib import closing
from urllib2 import urlopen
import sys
from html5lib import html5parser
	
def format(data):
	a = []
	a = data.split("\n")
	b = []
	
	for line in a:
		if line[0] == "|":
			line = "| "+line[1:]
			b.append(line)
	
	return "\n".join(b)

p = html5parser.HTMLParser(namespaceHTMLElements=False)

if len(sys.argv) == 3:
	if sys.argv[1] == '-f':
		with open(sys.argv[2], "rb") as f:
			document = p.parse(f)
	if sys.argv[1] == '-s':
		document = p.parse(sys.argv[2])
	if sys.argv[1] == '-u':
		with closing(urlopen(sys.argv[2])) as f:
			document = p.parse(f)
	output = p.tree.testSerializer(document)
	
	print format(output)
	
	

	
	

