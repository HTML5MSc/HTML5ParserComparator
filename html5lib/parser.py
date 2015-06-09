from contextlib import closing
from urllib2 import urlopen
import sys
from html5lib import html5parser

def convert(stripChars):
    def convertData(data):
        """convert the output of str(document) to the format used in the testcases"""
        data = data.split("\n")
        rv = []
        for line in data:
            if line.startswith("|"):
                rv.append(line[stripChars:])
            else:
                rv.append(line)
        return "\n".join(rv)
    return convertData
def convertTreeDump(data):
    return "\n".join(convert(3)(data).split("\n")[1:])
	
p = html5parser.HTMLParser(namespaceHTMLElements=False)

if len(sys.argv) == 3:
	if sys.argv[1] == 'f':
		with open(sys.argv[2], "rb") as f:
			document = p.parse(f)
	if sys.argv[1] == 's':
		document = p.parse(sys.argv[2])
	if sys.argv[1] == 'u':
		with closing(urlopen(sys.argv[2])) as f:
			document = p.parse(f)
	output = p.tree.testSerializer(document)
	print output
	
	

	
	

